import requests
base_url = "http://localhost:8080"
resp = requests.post(f"{base_url}/api/auth/login", json={"email": "user@fitconnect.com", "password": "User@123"})
token = resp.json()["token"]
headers = {"Authorization": f"Bearer {token}"}

memberships = requests.get(f"{base_url}/api/memberships/my", headers=headers).json()
for m in memberships:
    print(f"ID: {m['id']}, Gym: {m['gymName']}, Amount: {m['amount']}, Status: {m['status']}")

payments = requests.get(f"{base_url}/api/payments/my", headers=headers).json()
for p in payments:
    print(f"Payment ID: {p['id']}, Gym: {p['gymName']}, Amount: {p['amount']}, Status: {p['status']}")
