import requests

base_url = "http://localhost:8080"
# Login as owner to see all
resp = requests.post(f"{base_url}/api/auth/login", json={"email": "owner@fitconnect.com", "password": "Owner@123"})
token = resp.json()["token"]
headers = {"Authorization": f"Bearer {token}"}

# Get Rachan's user ID
users = requests.get(f"{base_url}/api/users", headers=headers)
# wait, there's no /api/users endpoint. 
