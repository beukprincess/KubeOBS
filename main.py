from kubernetes import client, config
import os
from fastapi import FastAPI, Depends, Request, Header, HTTPException
from dotenv import load_dotenv
import psutil


load_dotenv()
ENV_VAR = os.getenv('API_TOKEN')

def verify_token(x_auth_token: str = Header(None)):
    if not ENV_VAR or x_auth_token != ENV_VAR:
        raise HTTPException(status_code=401, detail="Error")
    return x_auth_token

app = FastAPI(title="Kubeobs API", dependencies=[Depends(verify_token)])


try:
    if os.getenv('KUBERNETES_SERVICE_HOST'):
        config.load_incluster_config()
        print("Завантажено внутрішній конфіг кластера")
    else:
        config.load_kube_config()
        print("kubeconfig")
except Exception as e:
    print(f"Помилка завантаження конфігу: {e}")

v1 = client.CoreV1Api()


@app.get("/pods")
def get_nodes():
    try:
        pods = v1.list_pod_for_all_namespaces(watch=False)
        return {"status": "success", "pods": [n.metadata.name for n in pods.items]}
    except Exception as e:
        return {"status": "error", "message": str(e)}

@app.get("/nodes")
def get_nodes():
    try:
        nodes = v1.list_node()
        return {"status": "success", "nodes": [n.metadata.name for n in nodes.items]}
    except Exception as e:
        return {"status": "error", "message": str(e)}

@app.get("/system/metrics")
def get_system_metrics():
    try:
        cpu_usage = psutil.cpu_percent(interval=None)
        ram = psutil.vitrual_memory()
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

@app.get("/pods/health")
def get_pods_health():
    try:
        ret = v1.list_pod_for_all_namespaces(watch=False)
        detailed_pods = []
        
        for i in ret.items:
            restarts = 0
            if i.status.container_statuses:
                restarts = i.status.container_statuses[0].restart_count
                
            detailed_pods.append({
                "name": i.metadata.name,
                "namespace": i.metadata.namespace,
                "status": i.status.phase,
                "restarts": restarts,
                "age_seconds": (i.metadata.creation_timestamp.utcnow() - i.metadata.creation_timestamp).total_seconds() if i.metadata.creation_timestamp else 0
            })
            
        return {
            "status": "success",
            "total_pods": len(detailed_pods),
            "pods": detailed_pods
        }
    except Exception as e:
        return {"status": "error", "message": str(e)}

@app.get("/services")
def get_services():
    try:
        return {"status": "success"}
    except Exception as e:
        return {"status" :"error"}
