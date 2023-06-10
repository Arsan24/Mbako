from fastapi import APIRouter, HTTPException
from fastapi.security import HTTPBasic, HTTPBasicCredentials
from auth.schema import UserRegistration
from auth.dbfirestore import db
from passlib.hash import bcrypt
from firebase_admin import firestore
from function import generate_token, store_token, send_password_reset_email

router = APIRouter()
security = HTTPBasic()

# Register Endpoint
@router.post("/register")
async def register(username: str, contact: str, email: str, password: str):
    username = username
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

# User Login Endpoint
@router.post("/login")
async def login(credentials: HTTPBasicCredentials):
    username = credentials.username
    password = credentials.password

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
async def forgot_password(email: str):
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
async def reset_password(username: str, token: str, new_password: str):
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