import requests

base_url = "http://localhost:8080"
# Login as owner
resp = requests.post(f"{base_url}/api/auth/login", json={"email": "owner@fitconnect.com", "password": "Owner@123"})
token = resp.json()["token"]
headers = {"Authorization": f"Bearer {token}"}

gyms = requests.get(f"{base_url}/api/gyms", headers=headers).json()
for gym in gyms:
    gym_id = gym['id']
    mems = requests.get(f"{base_url}/api/memberships/gym/{gym_id}", headers=headers).json()
    for m in mems:
        print(f"Gym: {gym['name']}, User: {m['userName']}, Start: {m['startDate']}, End: {m['endDate']}, Amount: {m['amount']}, Status: {m['status']}")
