import requests

base_url = "http://localhost:8080"
resp = requests.post(f"{base_url}/api/auth/login", json={"email": "user@fitconnect.com", "password": "User@123"})
token = resp.json()["token"]
headers = {"Authorization": f"Bearer {token}"}

gyms = requests.get(f"{base_url}/api/gyms", headers=headers).json()
gym_id = gyms[0]["id"]

# Create 12 month membership
mem_req = {
    "gymId": gym_id,
    "planName": "STANDARD",
    "durationMonths": 12
}
mem_resp = requests.post(f"{base_url}/api/memberships", json=mem_req, headers=headers).json()
print("Created:", mem_resp)

payments = requests.get(f"{base_url}/api/payments/my", headers=headers).json()
payment_id = payments[-1]["id"]
print("Refunding payment ID:", payment_id)

refund_resp = requests.put(f"{base_url}/api/payments/{payment_id}/refund", headers=headers).json()
print("Refund:", refund_resp)

memberships = requests.get(f"{base_url}/api/memberships/my", headers=headers).json()
print("Memberships after refund:", memberships)
