from fastapi.testclient import TestClient
from main import app

client = TestClient(app)
#
def test_security_unauthorized_access():
    response = client.get("/nodes")
    assert response.status_code == 401
    assert response.json() == {"detail": "Error: Unauthorized access"}

#