from fastapi import APIRouter, HTTPException, Form, Path, File, Header, Depends, UploadFile
from fastapi.security import OAuth2PasswordBearer
from function import decodeJWT, upload_to_bucket
from auth.schema import Item
from auth.dbfirestore import db
from datetime import datetime
from typing import Optional 
import itertools

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
        docs = itertools.islice(collection_ref.stream(), start_index, end_index)
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
    image: UploadFile = File(), 
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
    
    image_url = upload_to_bucket(image.file)
    createdAt = datetime.now()
    item = Item(image=image_url, pname=pname, price=price, quantity=quantity, createdAt=createdAt)
    item_data = item.dict()
    collection_ref = db.collection('items')
    doc_ref = collection_ref.document()
    item_data['item_id'] = doc_ref.id
    doc_ref.set(item_data)

    return {
        "error": False,
        "message": "Barang berhasil terdaftar!"
    }

# Get a specific item by item_id.
@router.get("/api/items/id")
def get_item(
    item_id: str = Form(),
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
        return {
        "error": False,
        "message": "Get Item Success!",
        "listItems": item_data
    }
    else:
        raise HTTPException(status_code=404, detail="Barang tidak ditemukan")
    
# Update a specific item by item_id.
@router.put("/api/items/update")
async def update_item(
    authorization: str = Depends(oauth2_scheme),
    item_id: str = Form(), 
    image: UploadFile = File(default=None), 
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

    item_ref = db.collection('items').document(item_id)
    item = item_ref.get()

    if item.exists:
        item_data = item.to_dict()
        item_data['image'] = image
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
@router.delete("/api/items/delete")
def delete_item(
    authorization: str = Depends(oauth2_scheme),
    item_id: str = Form()
):
    decoded_token = decodeJWT(authorization)
    if not decoded_token:
        return {"error": "Invalid token"}

    username = decoded_token.get("sub")
    if not username:
        return {"error": "Invalid token"}

    item_ref = db.collection('items').document(item_id)
    item_ref.delete()

    return {
        "error": False,
        "message": "Barang berhasil dihapus!"
    }

# update the stock on purchased and save the transaction record
@router.post("/api/items/buy")
async def buy_item(
    authorization: str = Depends(oauth2_scheme),
    item_id: str = Form(), 
    quantity: str = Form()
):
    if not item_id:
        return {"error": "Item ID is missing"}

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
            }
        else:
            return {
                "error": True,
                "message": "Stok telah habis"
            }
    else:
        raise HTTPException(status_code=404, detail="Barang tidak ditemukan")