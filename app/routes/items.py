from fastapi import APIRouter, HTTPException, status
from auth.schema import Item
from auth.dbfirestore import db


router = APIRouter()

# Get a list of all items.
@router.get("/api/items")
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
@router.post("/api/items")
async def create_item(item: Item):
 
    item_data = item.dict()
    collection_ref = db.collection('items')
    doc_ref = collection_ref.document()
    doc_ref.set(item_data)

    return {"message": "Barang berhasil dimasukkan!"}

# Get a specific item by item_id.
@router.get("/api/items/{item_id}")
def get_item(item_id: str):
 
    item_ref = db.collection('items').document(item_id)
    item = item_ref.get()

    if item.exists:
        return item.to_dict()
    else:
        raise HTTPException(status_code=404, detail="Barang tidak ditemukan")
    
# Update a specific item by item_id.
@router.put("/api/items/{item_id}")
async def update_item(item_id: str, item: Item):
 
 
    updated_item = item.dict()   
    item_ref = db.collection('items').document(item_id)
    item_ref.update(updated_item)

    return {"message": "Barang berhasil diubah"}


# Delete a specific item by item_id.
@router.delete("/api/items/{item_id}")
def delete_item(item_id: str):
 
    item_ref = db.collection('items').document(item_id)
    item_ref.delete()

    return {"message": "Barang berhasil dihapus!"}