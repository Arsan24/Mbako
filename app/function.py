from random import randint
from auth.dbfirestore import db
import smtplib
from email.message import EmailMessage
from main import secret_key
import time 
import jwt

# Configure smtp settings
smtp_host = "smtp.gmail.com"
smtp_port = 465
smtp_username = "m.shafanatama@gmail.com"
smtp_password = "sllbpeqebvfberoc"

# Create a connection to the SMTP server
smtp_server = smtplib.SMTP_SSL(smtp_host, smtp_port)
smtp_server.login(smtp_username, smtp_password)

# Generate a random token
def generate_token():
    return str(randint(100000, 999999))

# Store the token in the database
def store_token(user_id, token):
    user_ref = db.collection("users").document(user_id)
    user_ref.set({"reset_token": token}, merge=True)
   

# Send password reset email
def send_password_reset_email(email, token):
    msg = EmailMessage()
    msg['Subject'] = "Reset Password"
    msg['From'] = smtp_username
    msg['To'] = email
    msg.set_content(f"Password reset email sent to {email}. Token: {token}")

    smtp_server.send_message(msg)

    return {"message": "Email reset password telah dikirim"}

# Generate access token
def generate_access_token(username: str):
    payload = {
        "exp": time.time() + 1800, 
        "sub": username
    }
    access_token = jwt.encode(payload, secret_key, algorithm='HS256')
    return access_token