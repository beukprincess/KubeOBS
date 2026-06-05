from kubernetes import client, config
import os
from fastapi import FastAPI, Depends, Request, Header, HTTPException
from dotenv import load_dotenv
import psutil
from passlib.context import CryptContext
from datetime import datetime, timezone
from sqlalchemy import Column, Integer, String, Boolean, ForeignKey, DateTime
from sqlalchemy.orm import declarative_base, relationship
import models
import schemas
from database import get_db

pwd_context = CryptContext(schemas=["bcrypt"], deprecated="auto")

load_dotenv()
ENV_VAR = os.getenv('API_TOKEN')
Base = declarative_base()

def verify_token(x_auth_token: str = Header(None)):
    if not ENV_VAR or x_auth_token != ENV_VAR:
        raise HTTPException(status_code=401, detail="Error: Unauthorized access")
    return x_auth_token

app = FastAPI(title="Kubeobs API", version="1.0.0")


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


@app.post("/auth/register", response_model=schemas.UserResponse, status_code=status.HTTP_201_CREATED)
def register_user(user_data: schemas.UserCreate, db: Session = Depends(get_db)):
    existing_user = db.query(models.User).filter(models.User.email == user_data.email).first()
    if existing_user:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Користувач з таким Email вже зареєстрований"
        )

    hashed_pwd = pwd_context.hash(user_data.password)

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
        cpu_usage = psutil.cpu_percent(interval=None)
        ram = psutil.virtual_memory()
        disk = psutil.disk_usage('/')

        return {
                "status": "success",
                "metrics": {
                    "cpu_percentage": cpu_usage,
                    "ram":{
                        "total_gb": round(ram.total / (1024**3), 2),
                        "used_gb": round(ram.used / (1024**3), 2),
                        "percentage": ram.percent
                        },
                    "disk":{
                        "total_gb": round(disk.total / (1024**3), 2),
                        "free_gb": round(disk.free / (1024**3), 2),
                        "percentage": disk.percent
                        }
                }
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

@app.get("/services", dependencies=[Depends(verify_token)])
def get_services():
    try:
        return {"status": "success"}
    except Exception as e:
        return {"status" :"error"}
