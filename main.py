import uvicorn
from fastapi import FastAPI, HTTPException, Depends
from fastapi.security import HTTPBasic, HTTPBasicCredentials
from pydantic import BaseModel
import firebase_admin
from firebase_admin import credentials, firestore
from passlib.hash import bcrypt

app = FastAPI() 
security = HTTPBasic()

# initialize Firebase Admin SDK
cred = credentials.Certificate("serviceAccountKey.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

# define the basemodel

# User Registration Data Model
class UserRegistration(BaseModel):
    username: str
    contact: str
    email: str
    password: str

# User Login Data Model
class UserLogin(BaseModel):
    username: str
    password: str

# Item Data Model
class Item(BaseModel):
    image: str
    pname: str
    price: int

# Define the API endpoint


# Register Endpoint
@app.post("/register")
async def register(user: UserRegistration):
    username = user.username
    user_ref = db.collection("users").document(username)
    user_data = user_ref.get()

    if user_data.exists:
        raise HTTPException(status_code=400, detail="Username sudah digunakan")

    hashed_password = bcrypt.hash(user.password) 
    user_ref.set({
        "username": username,
        "password": hashed_password,
        "email": user.email,
        "contact": user.contact
    })

    return {"User Berhasil Registrasi"}

# User Login Endpoint
@app.post("/login")
async def login(credentials: HTTPBasicCredentials):
    username = credentials.username
    password = credentials.password

    user_ref = db.collection("users").document(username)
    user_data = user_ref.get()

    if not user_data.exists:
        raise HTTPException(status_code=401, detail="username atau kata sandi salah")

    stored_password = user_data.to_dict()["password"]

    if not bcrypt.verify(password, stored_password):
        raise HTTPException(status_code=401, detail="username atau kata sandi salah")

    return {"message": "Berhasil masuk!"}

# Get a list of all items.
@app.get("/api/items")
def get_items():
 
    items = []
    collection_ref = db.collection('items')
    docs = collection_ref.stream()

    for doc in docs:
        item = doc.to_dict()
        item['id'] = doc.id
        items.append(item)

    return items

# Create a new item.
@app.post("/api/items")
async def create_item(item: Item):
 
    item_data = item.dict()
    collection_ref = db.collection('items')
    doc_ref = collection_ref.document()
    doc_ref.set(item_data)

    return {"message": "Barang berhasil dimasukkan!"}

# Get a specific item by item_id.
@app.get("/api/items/{item_id}")
def get_item(item_id: str):
 
    item_ref = db.collection('items').document(item_id)
    item = item_ref.get()

    if item.exists:
        return item.to_dict()
    else:
        raise HTTPException(status_code=404, detail="Barang tidak ditemukan")
    
# Update a specific item by item_id.
@app.put("/api/items/{item_id}")
async def update_item(item_id: str, item: Item):
 
 
    updated_item = item.dict()   
    item_ref = db.collection('items').document(item_id)
    item_ref.update(updated_item)

    return {"message": "Barang berhasil diubah"}


# Delete a specific item by item_id.
@app.delete("/api/items/{item_id}")
def delete_item(item_id: str):
 
    item_ref = db.collection('items').document(item_id)
    item_ref.delete()

    return {"message": "Barang berhasil dihapus!"}

# Run the app
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8080)
    
