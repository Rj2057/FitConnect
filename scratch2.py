import requests

base_url = "http://localhost:8080"
resp = requests.post(f"{base_url}/api/auth/login", json={"email": "owner@fitconnect.com", "password": "Owner@123"})
token = resp.json()["token"]
headers = {"Authorization": f"Bearer {token}"}

payments = requests.get(f"{base_url}/api/payments/gym/1", headers=headers).json()
for p in payments:
    print(f"User: {p['userName']}, Amount: {p['amount']}, Status: {p['status']}")
