from fastapi import APIRouter, HTTPException, status
from fastapi.security import HTTPBasic, HTTPBasicCredentials
from auth.schema import UserRegistration, UserLogin
from auth.dbfirestore import db
from passlib.hash import bcrypt
from function import generate_token, store_token, send_password_reset_email

router = APIRouter()
security = HTTPBasic()

# Register Endpoint
@router.post("/register")
async def register(user: UserRegistration):
    username = user.username
    user_ref = db.collection("users").document(username)
    user_data = user_ref.get()

    if user_data.exists:
        raise HTTPException(status_code=400, detail="Username sudah digunakan")

    hashed_password = bcrypt.hash(user.password) 
    user_ref.set({
        "username": username,
        "password": hashed_password,
        "email": user.email,
        "contact": user.contact
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
        raise HTTPException(status_code=401, detail="username atau kata sandi salah")

    stored_password = user_data.to_dict()["password"]

    if not bcrypt.verify(password, stored_password):
        raise HTTPException(status_code=401, detail="username atau kata sandi salah")

    return {"message": "Berhasil masuk!"}

# Forgot-password Endpoint
@router.post("/forgot-password")
async def forgot_password(email: str):
    # Check if the email exists in the database
    users_ref = db.collection("users").where("email", "==", email)
    users = users_ref.stream()

    if len(list(users)) == 0:
        raise HTTPException(status_code=404, detail="Email not found")

    # Generate and store the reset token
    reset_token = generate_token()
    store_token(users[0].get("username"), reset_token)

    # Send the password reset email
    send_password_reset_email(email, reset_token)

    return {"message": "Password reset email sent"}

# Reset Password Endpoint
@router.post("/reset-password")
async def reset_password(username: str, token: str, new_password: str):
    # Check if the token matches the stored token for the user
    user_ref = db.collection("users").document(username)
    user_data = user_ref.get()

    if not user_data.exists:
        raise HTTPException(status_code=404, detail="User not found")

    stored_token = user_data.to_dict().get("reset_token")

    if token != stored_token:
        raise HTTPException(status_code=401, detail="Invalid reset token")

    # Update the password in the database
    user_ref.update({"password": new_password, "reset_token": None})

    return {"message": "Password reset successful"}