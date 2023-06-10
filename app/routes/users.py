from fastapi import APIRouter, HTTPException, Form
from fastapi.security import HTTPBasic, HTTPBasicCredentials
from auth.schema import UserRegistration
from auth.dbfirestore import db
from passlib.hash import bcrypt
from firebase_admin import firestore
from function import generate_token, store_token, send_password_reset_email
from typing import Annotated

router = APIRouter()
security = HTTPBasic()

# Register Endpoint
@router.post("/register")
async def register(
    username: Annotated[str, Form()],
    contact: Annotated[str, Form()],
    email: Annotated[str, Form()],
    password: Annotated[str, Form()]
):
    user_ref = db.collection("users").document(username)
    user_data = user_ref.get()

    if user_data.exists:
        raise HTTPException(status_code=400, detail="Username sudah digunakan")

    hashed_password = bcrypt.hash(password) 
    user_ref.set({
        "username": username,
        "password": hashed_password,
        "email": email,
        "contact": contact
    })

    return {"User Berhasil Registrasi"}

@router.get("/register")
def get_users():

    users = []
    collection_ref = db.collection('users')
    docs = collection_ref.stream()

    for doc in docs:
        user = doc.to_dict()
        user['id'] = doc.id
        users.append(user)

    return users

# User Login Endpoint
@router.post("/login")
async def login(
    username: str = Form(...),
    password: str = Form(...)
):

    user_ref = db.collection("users").document(username)
    user_data = user_ref.get()

    if not user_data.exists:
        raise HTTPException(status_code=401, detail="Username atau kata sandi salah")

    stored_password = user_data.to_dict()["password"]

    if not bcrypt.verify(password, stored_password):
        raise HTTPException(status_code=401, detail="Username atau kata sandi salah")

    return {"message": "Berhasil masuk!"}

# Forgot-password Endpoint
@router.post("/forgot-password")
async def forgot_password(email: str = Form(...)):
    # Check if the email exists in the database
    users_ref = db.collection("users").where("email", "==", email)
    users = users_ref.get()

    if len(list(users)) == 0:
        raise HTTPException(status_code=404, detail="Email tidak ditemukan")

    # Generate and store the reset token
    reset_token = generate_token()
    for user in users:
        store_token(user.id, reset_token)

    # Send the password reset email
    send_password_reset_email(email, reset_token)

    return {"message": "Token reset paswword telah dikirim melalui email"}

# Reset Password Endpoint
@router.post("/reset-password")
async def reset_password(
    username: str = Form(...), 
    token: str = Form(...), 
    new_password: str = Form(...)
):
    user_ref = db.collection("users").document(username)
    user_data = user_ref.get()

    if not user_data.exists:
        raise HTTPException(status_code=404, detail="Name pengguna tidak ditemukan")

    stored_token = user_data.to_dict().get("reset_token")

    if token != stored_token:
        raise HTTPException(status_code=401, detail="Token tidak valid")

    # Update the password in the database
    new_pass = bcrypt.hash(new_password) # Because Login needed to read from hash and so that new password stored in hash
    user_ref.update({"password": new_pass})
    user_ref.update({"reset_token": firestore.DELETE_FIELD})

    return {"message": "Password berhasil diubah"}