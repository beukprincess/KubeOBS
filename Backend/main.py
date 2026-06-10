from kubernetes import client, config
import os
from fastapi import FastAPI, Depends, Request, Header, HTTPException, status
from dotenv import load_dotenv
import jwt
import psutil
import bcrypt
from datetime import timedelta,datetime, timezone
from sqlalchemy import Column, Integer, String, Boolean, ForeignKey, DateTime
from sqlalchemy.orm import declarative_base, relationship, Session
import models
import schemas
from database import get_db


load_dotenv()
ENV_VAR = os.getenv('API_TOKEN')
JWT_SECRET = os.getenv("JWT_SECRET")
JWT_ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 60 * 24

Base = declarative_base()

def verify_token(x_auth_token: str = Header(None)):
    if not ENV_VAR or x_auth_token != ENV_VAR:
        raise HTTPException(status_code=401, detail="Error: Unauthorized access")
    return x_auth_token

def get_current_user(authorization: str = Header(None), db: Session = Depends(get_db)):
    if not authorization:
        raise HTTPException(status_code=401, detail="No Header Authorization")
    
    try:
        token_type, token = authorization.split(" ")
        if token_type.lower() != "bearer":
            raise HTTPException(status_code=401, detail="Error: Invalid token type. Use Bearer")
        
        payload = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])
        user_id: int = payload.get("user_id")
        if user_id is None:
            raise HTTPException(status_code=401, detail="Error: Invalid token: missing user_id")
            
    except (ValueError, jwt.PyJWTError):
        raise HTTPException(status_code=401, detail="Error: Invalid or expired token")
        
    user = db.query(models.User).filter(models.User.id == user_id).first()
    if user is None:
        raise HTTPException(status_code=401, detail="Error: User not found")
        
    return user

app = FastAPI(title="Kubeobs API", version="1.0.1")


try:
    if os.getenv('KUBERNETES_SERVICE_HOST'):
        config.load_incluster_config()
        print("ServiceACcount config loaded")
    else:
        config.load_kube_config()
        print("kubeconfig")
except Exception as e:
    print(f"Error cfg {e}")

v1 = client.CoreV1Api()
custom_api = client.CustomObjectsApi()


@app.post("/auth/register", response_model=schemas.UserResponse, status_code=status.HTTP_201_CREATED)
def register_user(user_data: schemas.UserCreate, db: Session = Depends(get_db)):
    existing_user = db.query(models.User).filter(models.User.email == user_data.email).first()
    if existing_user:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Error: User with this Email already registered"
        )

    password_bytes = user_data.password.encode('utf-8')
    salt = bcrypt.gensalt()
    hashed_password_bytes = bcrypt.hashpw(password_bytes, salt)

    hashed_pwd = hashed_password_bytes.decode('utf-8')
    
    new_user = models.User(
        email=user_data.email,
        hashed_password=hashed_pwd
    )
    db.add(new_user)
    db.commit()
    db.refresh(new_user)

    return new_user

@app.post("/auth/login", response_model=schemas.TokenResponse)
def login_user(user_credentials: schemas.UserLogin, db: Session = Depends(get_db)):
    user = db.query(models.User).filter(models.User.email == user_credentials.email).first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Error: Check password or email"
        )

    password_bytes = user_credentials.password.encode('utf-8')
    hashed_password_bytes = user.hashed_password.encode('utf-8')
    
    if not bcrypt.checkpw(password_bytes, hashed_password_bytes):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Error: Check password or email"
        )

    #jwt creation
    expire = datetime.now(timezone.utc) + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode = {"user_id": user.id, "exp": expire}
    encoded_jwt = jwt.encode(to_encode, JWT_SECRET, algorithm=JWT_ALGORITHM)

    return {
        "access_token": encoded_jwt,
        "token_type": "bearer",
        "user": {
            "id": user.id,
            "email": user.email
        }
    }

# --- Secured X-Headers ---

@app.get("/clusters/{cluster_id}/metrics")
def get_dynamic_cluster_metrics(
    cluster_id: int, 
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    cluster = db.query(models.Cluster).filter(models.Cluster.id == cluster_id).first()
    if not cluster:
        raise HTTPException(status_code=404, detail="Кластер не знайдено")
        
    if cluster.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="Доступ заборонено: це не ваш кластер")
        
    try:
        configuration = client.Configuration()
        configuration.host = cluster.endpoint_url  
        configuration.verify_ssl = False 
        configuration.api_key = {"authorization": f"Bearer {cluster.cluster_token}"}
        
        dynamic_client = client.ApiClient(configuration)
        dynamic_custom_api = client.CustomObjectsApi(dynamic_client)
        
        raw_metrics = dynamic_custom_api.list_cluster_custom_object(
            group="metrics.k8s.io",
            version="v1beta1",
            plural="nodes"
        )
        
        nodes_summary = []
        for item in raw_metrics.get("items", []):
            node_name = item["metadata"]["name"]
            cpu_raw = item["usage"]["cpu"]       
            mem_raw = item["usage"]["memory"]    
            
            cpu_cores = int(cpu_raw.replace("n", "")) / 1_000_000_000 if "n" in cpu_raw else 0
            mem_numeric = int(mem_raw.replace("Ki", "")) / 1024 if "Ki" in mem_raw else 0
            
            nodes_summary.append({
                "node_name": node_name,
                "cpu_usage_cores": round(cpu_cores, 3),
                "memory_used_mb": round(mem_numeric, 2)
            })
            
        return {
            "status": "success",
            "cluster_id": cluster_id,
            "cluster_name": cluster.name,
            "cluster_metrics": nodes_summary
        }
        
    except Exception as e:
        raise HTTPException(
            status_code=500, 
            detail=f"Не вдалося підключитися до кластера або зняти метрики: {str(e)}"
        )

@app.get("/clusters", response_model=list[schemas.ClusterResponse])
def get_user_clusters(db: Session = Depends(get_db), current_user: models.User = Depends(get_current_user)):
    clusters = db.query(models.Cluster).filter(models.Cluster.user_id == current_user.id).all()
    return clusters

@app.get("/pods", dependencies=[Depends(verify_token)])
def get_pods():
    try:
        pods = v1.list_pod_for_all_namespaces(watch=False)
        return {"status": "success", "pods": [n.metadata.name for n in pods.items]}
    except Exception as e:
        return {"status": "error", "message": str(e)}

@app.get("/nodes", dependencies=[Depends(verify_token)])
def get_nodes():
    try:
        nodes = v1.list_node()
        return {"status": "success", "nodes": [n.metadata.name for n in nodes.items]}
    except Exception as e:
        return {"status": "error", "message": str(e)}

@app.get("/system/metrics", dependencies=[Depends(verify_token)])
def get_system_metrics():
    try:
        raw_metrics = custom_api.list_cluster_custom_object(
            group="metrics.k8s.io",
            version="v1beta1",
            plural="nodes"
        )
        
        nodes_summary = []
        
        for item in raw_metrics.get("items", []):
            node_name = item["metadata"]["name"]
            cpu_raw = item["usage"]["cpu"]       
            mem_raw = item["usage"]["memory"]    
            
            mem_numeric = int(mem_raw.replace("Ki", "")) / 1024 if "Ki" in mem_raw else 0
            
            nodes_summary.append({
                "node_name": node_name,
                "cpu_usage": cpu_raw,
                "memory_used_mb": round(mem_numeric, 2)
            })
            
        return {
            "status": "success",
            "source": "K3s Metrics Server",
            "nodes_count": len(nodes_summary),
            "cluster_metrics": nodes_summary
        }
    except Exception as e:
        return {"status": "error", "message": str(e)}
##
@app.get("/pods/health", dependencies=[Depends(verify_token)])
def get_pods_health():
    try:
        ret = v1.list_pod_for_all_namespaces(watch=False)
        detailed_pods = []
        
        for i in ret.items:
            restarts = 0
            age = 0


            if i.metadata.creation_timestamp:
                age = (datetime.now(timezone.utc) - i.metadata.creation_timestamp).total_seconds()


            if i.status.container_statuses:
                restarts = i.status.container_statuses[0].restart_count
                
            detailed_pods.append({
                "name": i.metadata.name,
                "namespace": i.metadata.namespace,
                "status": i.status.phase,
                "restarts": restarts,
                "age_seconds": round(age)
            })
            
        return {
            "status": "success",
            "total_pods": len(detailed_pods),
            "pods": detailed_pods
        }
    except Exception as e:
        return {"status": "error", "message": str(e)}

@app.get("/pods/{namespace}/{pod_name}/logs", dependencies=[Depends(verify_token)])
def get_pod_logs(namespace: str, pod_name: str, tail: int = 50):
    try:
        logs = v1.read_namespaced_pod_log(
            name=pod_name, 
            namespace=namespace, 
            tail_lines=tail
        )
        return {
            "status": "success", 
            "namespace": namespace,
            "pod": pod_name, 
            "logs": logs
        }
    except Exception as e:
        return {"status": "error", "message": f"Log read error: {str(e)}"}

@app.get("/services", dependencies=[Depends(verify_token)])
def get_services():
    try:
        return {"status": "success"}
    except Exception as e:
        return {"status" :"error"}
