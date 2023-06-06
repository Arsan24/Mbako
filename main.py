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
  
class item(BaseModel):
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
async def create_item(item: Item):
 
    item_data = item.dict()
    collection_ref = db.collection('items')
    doc_ref = collection_ref.document()
    doc_ref.set(item_data)

    return {"message": "Item created successfully!"}

# Get a specific item by item_id.
@app.get("/api/items/{item_id}")
def get_item(item_id: str):
 
    item_ref = db.collection('items').document(item_id)
    item = item_ref.get()

    if item.exists:
        return item.to_dict()
    else:
        raise HTTPException(status_code=404, detail="Item not found")
    
# Update a specific item by item_id.
@app.put("/api/items/{item_id}")
async def update_item(item_id: str, item: Item):
 
 
    updated_item = item.dict()   
    item_ref = db.collection('items').document(item_id)
    item_ref.update(updated_item)

    return {"message": "Item updated successfully!"}


# Delete a specific item by item_id.
@app.delete("/api/items/{item_id}")
def delete_item(item_id: str):
 
    item_ref = db.collection('items').document(item_id)
    item_ref.delete()

    return {"message": "Item deleted successfully!"}

# Run the app
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
    
