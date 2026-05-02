from kubernetes import client, config
import os
from fastapi import FastAPI, Depends, Request, Header, HTTPException
from dotenv import load_dotenv

def verify_token(x_auth_token: str = Header(None)):
    if not ENV_VAR or x_auth_token != ENV_VAR:
        raise HTTPException(status_code=401, detail="Error")
    return x_auth_token

load_dotenv()
app = FastAPI(dependencies=[Depends(verify_token)])
ENV_VAR = os.getenv('API_TOKEN')


try:
    if os.getenv('KUBERNETES_SERVICE_HOST'):
        config.load_incluster_config()
        print("Завантажено внутрішній конфіг кластера")
    else:
        config.load_kube_config()
        print("Завантажено локальний kubeconfig")
except Exception as e:
    print(f"Помилка завантаження конфігу: {e}")

v1 = client.CoreV1Api()

@app.get("/nodes")
def get_nodes():
    try:
        nodes = v1.list_node()
        return {"status": "success", "nodes": [n.metadata.name for n in nodes.items]}
    except Exception as e:
        return {"status": "error", "message": str(e)}
