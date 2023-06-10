from pydantic import BaseModel

# define the basemodel

# User Registration Data Model
class UserRegistration(BaseModel):
    username: str
    contact: str
    email: str
    password: str

# Item Data Model
class Item(BaseModel):
    image: str
    pname: str
    price: int