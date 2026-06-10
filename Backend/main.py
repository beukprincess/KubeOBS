from kubernetes import client, config
import os
from fastapi import FastAPI, Depends, Request, Header, HTTPException, status
from dotenv import load_dotenv
import psutil
import bcrypt
from datetime import datetime, timezone
from sqlalchemy import Column, Integer, String, Boolean, ForeignKey, DateTime
from sqlalchemy.orm import declarative_base, relationship, Session
import models
import schemas
from database import get_db


load_dotenv()
ENV_VAR = os.getenv('API_TOKEN')
Base = declarative_base()

def verify_token(x_auth_token: str = Header(None)):
    if not ENV_VAR or x_auth_token != ENV_VAR:
        raise HTTPException(status_code=401, detail="Error: Unauthorized access")
    return x_auth_token

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
            detail="Користувач з таким Email вже зареєстрований"
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

# --- Secured X-Headers ---
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
