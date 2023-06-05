import uvicorn
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from datetime import datetime
import firebase_admin
from firebase_admin import credentials, firestore

# create a new FastAPI app instance

app = FastAPI() 

# initialize Firebase Admin SDK
cred = credentials.Certificate("path/to/firebase_credentials.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

# define the basemodel
    
def item(BaseModel):
    image: str
    pname: str
    startprice: int
    increment: int
    timelimit: datetime

# Define the API endpoint

# get list of all items
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

# create a new item
@app.post("/api/items")
def create_item(item: item):
    item_data = item.dict()
    collection_ref = db.collection('items')
    doc_ref = collection_ref.document()
    doc_ref.set(item_data)

    return {"message": "item created successfully!"}

# get item by id
@app.get("/api/items/{item_id}")
def get_item(item_id: str):
    item_ref = db.collection('items').document(item_id)
    item = item_ref.get()

    if item.exists:
        return item.to_dict()
    else:
        raise HTTPException(status_code=404, detail="item not found")

# Run the app
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
    
