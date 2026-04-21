import requests

base_url = "http://localhost:8080"
resp = requests.post(f"{base_url}/api/auth/login", json={"email": "user@fitconnect.com", "password": "User@123"})
token = resp.json()["token"]
headers = {"Authorization": f"Bearer {token}"}

gyms = requests.get(f"{base_url}/api/gyms", headers=headers).json()
# pick a gym we don't have membership for
gym_id = gyms[-1]["id"] 

# Create 1 month membership
mem_req = {
    "gymId": gym_id,
    "planName": "STANDARD",
    "durationMonths": 1
}
mem_resp = requests.post(f"{base_url}/api/memberships", json=mem_req, headers=headers)
print("Create membership:", mem_resp.status_code, mem_resp.text)
mem_id = mem_resp.json()["id"]

# Now simulate payments process
pay_req = {
    "gymId": gym_id,
    "amount": 1999.0,
    "description": "Standard"
}
pay_resp = requests.post(f"{base_url}/api/payments/process", json=pay_req, headers=headers)
print("Process payment:", pay_resp.status_code, pay_resp.text)

if pay_resp.status_code == 200:
    pay_id = pay_resp.json()["id"]
    conf_resp = requests.put(f"{base_url}/api/memberships/{mem_id}/confirm-payment/{pay_id}", headers=headers)
    print("Confirm payment:", conf_resp.status_code, conf_resp.text)
