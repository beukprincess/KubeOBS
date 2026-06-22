from kubernetes import client, config
import os
from fastapi import FastAPI, Depends, Request, Header, HTTPException, status, WebSocket, WebSocketDisconnect, Query
from fastapi.security import HTTPBearer
from dotenv import load_dotenv
import jwt
import psutil
import bcrypt
from datetime import timedelta,datetime, timezone
from sqlalchemy import Column, Integer, String, Boolean, ForeignKey, DateTime
from sqlalchemy.orm import declarative_base, relationship, Session
import models
import schemas
from database import get_db, engine
import asyncio

load_dotenv()
JWT_SECRET = os.getenv("JWT_SECRET")
JWT_ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 60 * 24
security = HTTPBearer()
app = FastAPI(title="Kubeobs API", version="1.0.1")

Base = declarative_base()

@app.on_event("startup")
def startup_event():
    models.Base.metadata.create_all(bind=engine)


def get_current_user(token: str = Depends(security), db: Session = Depends(get_db)):

    try:
        payload = jwt.decode(token.credentials, JWT_SECRET, algorithms=[JWT_ALGORITHM])
        user_id: int = payload.get("user_id")
        if user_id is None:
            raise HTTPException(status_code=401, detail="Error: Invalid token: missing user_id")
    except jwt.PyJWTError:
        raise HTTPException(status_code=401, detail="Error: Invalid or expired token")

    user = db.query(models.User).filter(models.User.id == user_id).first()
    if user is None:
        raise HTTPException(status_code=401, detail="Error: User not found")

    return user



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
##
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


@app.get("/pods")
def get_pods(current_user: models.User = Depends(get_current_user)):
    try:
        pods = v1.list_pod_for_all_namespaces(watch=False)
        return {"status": "success", "pods": [n.metadata.name for n in pods.items]}
    except Exception as e:
        return {"status": "error", "message": str(e)}

@app.get("/nodes")
def get_nodes(current_user: models.User = Depends(get_current_user)):
    try:
        nodes = v1.list_node()
        return {"status": "success", "nodes": [n.metadata.name for n in nodes.items]}
    except Exception as e:
        return {"status": "error", "message": str(e)}

@app.get("/clusters", response_model=list[schemas.ClusterResponse])
def get_user_clusters(db: Session = Depends(get_db), current_user: models.User = Depends(get_current_user)):
    clusters = db.query(models.Cluster).filter(models.Cluster.user_id == current_user.id).all()
    return clusters

@app.post("/clusters", response_model=schemas.ClusterResponse, status_code=status.HTTP_201_CREATED)
def add_cluster(
    cluster_data: schemas.ClusterCreate, 
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    new_cluster = models.Cluster(
        name=cluster_data.name,
        endpoint_url=cluster_data.endpoint_url,
        cluster_token=cluster_data.cluster_token,
        user_id=current_user.id
    )
    db.add(new_cluster)
    db.commit()
    db.refresh(new_cluster)
    return new_cluster
#
@app.get("/system/metrics")
def get_system_metrics(current_user: models.User = Depends(get_current_user)):
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

@app.websocket("/ws/metrics/{cluster_id}")
async def websocket_metrics(
    websocket: WebSocket, 
    cluster_id: int, 
    token: str = Query(...), 
    db: Session = Depends(get_db)
):
    try:
        payload = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])
        user_id = payload.get("user_id")
        exp = payload.get("exp")
        if user_id is None:
            await websocket.close(code=1008)
            return
    except Exception as e:
        print(f"Помилка розшифровки JWT: {e}")
        await websocket.close(code=1008)
        return

    await websocket.accept()

    cluster = db.query(models.Cluster).filter(models.Cluster.id == cluster_id, models.Cluster.user_id == user_id).first()
    if not cluster:
        await websocket.send_json({"error": "Cluster not found or access denied"})
        await websocket.close()
        return

    configuration = client.Configuration()
    configuration.host = cluster.endpoint_url
    configuration.api_key['authorization'] = f"Bearer {cluster.cluster_token}"
    configuration.verify_ssl = False 

    api_client = client.ApiClient(configuration)
    k8s_client = client.CoreV1Api(api_client) 
    metrics_client = client.CustomObjectsApi(api_client)

    try:
        while True:

            if exp and datetime.now(timezone.utc).timestamp() > exp:
                await websocket.send_json({
                    "error": "Time expired. Please re-login.", 
                    "type": "auth_expired"
                })
                await websocket.close(code=1008)
                break

            alerts = []
            
            pod_metrics_map = {}
            try:
                raw_pod_metrics = metrics_client.list_cluster_custom_object("metrics.k8s.io", "v1beta1", "pods")
                for item in raw_pod_metrics.get("items", []):
                    p_name = item["metadata"]["name"]
                    p_ns = item["metadata"]["namespace"]
                    
                    total_mem_ki = 0
                    raw_cpu = "0"
                    if item.get("containers"):
                        raw_cpu = item["containers"][0]["usage"]["cpu"]
                        for c in item["containers"]:
                            mem_str = c["usage"]["memory"]
                            total_mem_ki += int(mem_str.replace("Ki", "")) if "Ki" in mem_str else 0
                            
                    pod_metrics_map[f"{p_ns}/{p_name}"] = {
                        "memory_mb": round(total_mem_ki / 1024, 2),
                        "cpu_raw": raw_cpu
                    }
            except Exception as e:
                print(f"Metrics (pods) error: {e}")
            
            pods = k8s_client.list_pod_for_all_namespaces()
            pods_data = []
            
            for pod in pods.items:
                restarts = sum(c.restart_count for c in pod.status.container_statuses) if pod.status.container_statuses else 0
                status_phase = pod.status.phase
                
                if restarts >= 5:
                    alerts.append({
                        "level": "warning", 
                        "message": f"Warning. Pod {pod.metadata.name} was restarted {restarts} times!"
                    })
                if status_phase in ["Failed", "CrashLoopBackOff", "Unknown"]:
                    alerts.append({
                        "level": "critical", 
                        "message": f"Critical error: Pod {pod.metadata.name} is in {status_phase} state!"
                    })

                metrics_key = f"{pod.metadata.namespace}/{pod.metadata.name}"
                pod_res = pod_metrics_map.get(metrics_key, {"memory_mb": 0, "cpu_raw": "0"})

                pods_data.append({
                    "name": pod.metadata.name,
                    "namespace": pod.metadata.namespace,
                    "status": status_phase,
                    "restarts": restarts,
                    "memory_mb": pod_res["memory_mb"],
                    "cpu_raw": pod_res["cpu_raw"]
                })
            
            nodes_usage = []
            try:
                raw_node_metrics = metrics_client.list_cluster_custom_object("metrics.k8s.io", "v1beta1", "nodes")
                for item in raw_node_metrics.get("items", []):
                    mem_raw = item["usage"]["memory"]
                    mem_mb = round(int(mem_raw.replace("Ki", "")) / 1024, 2) if "Ki" in mem_raw else 0
                    nodes_usage.append({
                        "node_name": item["metadata"]["name"],
                        "cpu_usage": item["usage"]["cpu"],
                        "memory_used_mb": mem_mb
                    })
            except Exception:
                pass

            await websocket.send_json({
                "cluster_id": cluster.id,
                "cluster_name": cluster.name,
                "total_pods": len(pods_data),
                "nodes_usage": nodes_usage,
                "alerts": alerts,
                "pods": pods_data
            })
            
            await asyncio.sleep(1)

    except WebSocketDisconnect:
        pass
    except Exception as e:
        await websocket.send_json({"error": f"Connection error: {str(e)}"})
        await websocket.close()

@app.get("/pods/health")
def get_pods_health(current_user: models.User = Depends(get_current_user)):
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
        return {"status": "success", "total_pods": len(detailed_pods), "pods": detailed_pods}
    except Exception as e:
        return {"status": "error", "message": str(e)}

@app.get("/pods/{namespace}/{pod_name}/logs")
def get_pod_logs(namespace: str, pod_name: str, tail: int = 50, current_user: models.User = Depends(get_current_user)):
    try:
        logs = v1.read_namespaced_pod_log(name=pod_name, namespace=namespace, tail_lines=tail)
        return {"status": "success", "namespace": namespace, "pod": pod_name, "logs": logs}
    except Exception as e:
        return {"status": "error", "message": f"Log read error: {str(e)}"}

@app.get("/services")
def get_services(current_user: models.User = Depends(get_current_user)):
    try:
        return {"status": "success"}
    except Exception as e:
        return {"status" :"error"}
