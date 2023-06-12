from fastapi import APIRouter, HTTPException, Form, File
from auth.schema import Item
from auth.dbfirestore import db
import base64
from PIL import Image
import io

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
        encoded_image = item['image']
        image_bytes = base64.b64decode(encoded_image)
        decoded_image = Image.open(io.BytesIO(image_bytes))
        item['image'] = decoded_image
        items.append(item)

    return items

# Create a new item.
@router.post("/api/items")
async def create_item(
    image: bytes = File(), 
    pname: str = Form(), 
    price: int = Form(),
    quantity: int = Form()
):
    
    img_base64 = base64.b64encode(image).decode('utf-8')
    item = Item(image=img_base64, pname=pname, price=price, quantity=quantity)
    item_data = item.dict()
    collection_ref = db.collection('items')
    doc_ref = collection_ref.document()
    item_data['id'] = doc_ref.id
    doc_ref.set(item_data)

    return {
        "error": False,
        "message": "Barang berhasil terdaftar!"
    }

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
async def update_item(
    item_id: str, 
    image: str, 
    pname: str, 
    price: int,
    quantity: int
):
 
    item = Item(image=image, pname=pname, price=price, quantity=quantity)
    updated_item = item.dict()   
    item_ref = db.collection('items').document(item_id)
    item_ref.update(updated_item)

    return {
        "error": False,
        "message": "Barang berhasil diubah!"
    }

# Delete a specific item by item_id.
@router.delete("/api/items/{item_id}")
def delete_item(item_id: str):
 
    item_ref = db.collection('items').document(item_id)
    item_ref.delete()

    return {
        "error": False,
        "message": "Barang berhasil dihapus!"
    }

@router.post("/api/items/{item_id}/buy")
async def buy_item(item_id: str, quantity: int):
    # Retrieve the item from the database
    item_ref = db.collection('items').document(item_id)
    item = item_ref.get()

    if item.exists:
        # Get the current quantity of the item
        current_quantity = item.get('quantity', 0)

        if current_quantity >= quantity:
            # Calculate the new quantity after selling
            new_quantity = current_quantity - quantity

            # Update the quantity in the database
            item_ref.update({'quantity': new_quantity})

            return {
                "error": False,
                "message": f"Item berhasil dibeli, sisa stok: {new_quantity}"
            }
        else:
            return {
                "error": True,
                "message": "Stok telah habis"
            }
    else:
        raise HTTPException(status_code=404, detail="Barang tidak ditemukan")