from pydantic import BaseModel
from datetime import datetime
from fastapi import UploadFile

# define the basemodel

# User Registration Data Model
class UserRegistration(BaseModel):
    username: str
    contact: str
    email: str
    password: str

# Item Data Model
class Item(BaseModel):
    image: UploadFile
    pname: str
    price: int
    quantity: int
    createdAt: datetime