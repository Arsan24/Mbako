from fastapi import APIRouter, HTTPException, Form, File, Header, Depends
from fastapi.security import OAuth2PasswordBearer
from function import decodeJWT
from auth.schema import Item
from auth.dbfirestore import db
import base64
from PIL import Image
import io
from datetime import datetime
from typing import Optional 

router = APIRouter()
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/login")

# Get a list of all items.
@router.get("/api/items")
def get_items(
    authorization: str = Depends(oauth2_scheme),
    page: Optional[int] = None, 
    size: Optional[int] = None
):
    decoded_token = decodeJWT(authorization)
    if not decoded_token:
        return {"error": "Invalid token"}

    username = decoded_token.get("sub")
    if not username:
        return {"error": "Invalid token"}
    
    items = []
    collection_ref = db.collection('items')

    if page is not None and size is not None:
        start_index = (page - 1) * size
        end_index = start_index + size
        docs = collection_ref.stream()[start_index:end_index]
    else:
        docs = collection_ref.stream()

    for doc in docs:
        item = doc.to_dict()
        item_id = doc.id
        item['item_id'] = item_id
        items.append(item)

    return {
        "error": False,
        "message": "Get Item Success!",
        "listItems": items
    }

# Create a new item.
@router.post("/api/items")
async def create_item(
    authorization: str = Depends(oauth2_scheme),
    image: bytes = File(), 
    pname: str = Form(), 
    price: int = Form(),
    quantity: int = Form()
):
    decoded_token = decodeJWT(authorization)
    if not decoded_token:
        return {"error": "Invalid token"}

    username = decoded_token.get("sub")
    if not username:
        return {"error": "Invalid token"}
    
    img_base64 = base64.b64encode(image).decode('utf-8')
    createdAt = datetime.now()
    item = Item(image=img_base64, pname=pname, price=price, quantity=quantity, createdAt=createdAt)
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
@router.get("/api/items/:{item_id}")
def get_item(
    item_id: str | None,
    authorization: str = Depends(oauth2_scheme)
):
    decoded_token = decodeJWT(authorization)
    if not decoded_token:
        return {"error": "Invalid token"}

    username = decoded_token.get("sub")
    if not username:
        return {"error": "Invalid token"}
    
    item_ref = db.collection('items').document(item_id)
    item = item_ref.get()

    if item.exists:
        item_data = item.to_dict()
        if 'image' in item_data:
            image_base64 = item_data['image']
            image_bytes = base64.b64decode(image_base64)
            item_data['image'] = image_bytes
        return {
        "error": False,
        "message": "Get Item Success!",
        "listItems": item_data
    }
    else:
        raise HTTPException(status_code=404, detail="Barang tidak ditemukan")
    
# Update a specific item by item_id.
@router.put("/api/items/{item_id}")
async def update_item(
    item_id: str, 
    image: bytes = File(default=None), 
    pname: str = Form(), 
    price: int = Form(),
    quantity: int = Form()
):
    item_ref = db.collection('items').document(item_id)
    item = item_ref.get()

    if item.exists:
        item_data = item.to_dict()
        if image is not None:
            image_base64 = base64.b64encode(image).decode('utf-8')
            item_data['image'] = image_base64
        item_data['pname'] = pname
        item_data['price'] = price
        item_data['quantity'] = quantity
        item_ref.update(item_data)

        return {
            "error": False,
            "message": "Barang berhasil diubah!"
        }
    else:
        raise HTTPException(status_code=404, detail="Barang tidak ditemukan")

# Delete a specific item by item_id.
@router.delete("/api/items/{item_id}")
def delete_item(item_id: str):
 
    item_ref = db.collection('items').document(item_id)
    item_ref.delete()

    return {
        "error": False,
        "message": "Barang berhasil dihapus!"
    }

# update the stock on purchased and save the transaction record
@router.post("/api/items/{item_id}/buy")
async def buy_item(
    item_id: str, 
    quantity: int = Form(),
    username: str = Header(None)
):

    item_ref = db.collection('items').document(item_id)
    item = item_ref.get()

    if item.exists:
        item_data = item.to_dict()
        current_quantity = item_data.get('quantity', 0)
        item_price = item_data.get('price')
        if current_quantity >= quantity:
            total_price = item_price * quantity
            new_quantity = current_quantity - quantity
            item_ref.update({'quantity': new_quantity})

            # Save transaction record
            
            transaction_data = {
                'item_id': item_id,
                'buyer': username,
                'product': item_data.get('pname'),
                'quantity': quantity,
                'total_price': total_price,
                'timestamp': datetime.now().isoformat()
            }
            transaction_ref = db.collection('transactions').document()
            transaction_ref.set(transaction_data)

            return {
                "error": False,
                "message": f"Pembelian Berhasil!",
                "transactionResult": transaction_data
            }
        else:
            return {
                "error": True,
                "message": "Stok telah habis"
            }
    else:
        raise HTTPException(status_code=404, detail="Barang tidak ditemukan")