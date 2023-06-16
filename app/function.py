from random import randint
from google.cloud import storage
from auth.dbfirestore import db
from firebase_admin import firestore
import smtplib
from email.message import EmailMessage
import uuid
import time
import jwt
import os
import asyncio

app_email = os.environ.get('SMTP_EMAIL')
app_password = os.environ.get('SMTP_PASS')


# Configure smtp settings
smtp_host = "smtp.gmail.com"
smtp_port = 465
smtp_username = "m.shafanatama@gmail.com"
smtp_password = "sllbpeqebvfberoc"

# Create a connection to the SMTP server
def establish_smtp_connection():
    try:
        smtp_server = smtplib.SMTP_SSL(smtp_host, smtp_port)
        smtp_server.login(smtp_username, smtp_password)
        return smtp_server
    except Exception as e:
        raise Exception("Failed to establish SMTP connection") from e


# Generate a random token
def generate_token():
    return str(randint(100000, 999999))

# Store the token in the database
def store_token(user_id, token):
    user_ref = db.collection("users").document(user_id)
    user_ref.set({"reset_token": token}, merge=True)

# Delete Reset Token after few minutes
async def delete_reset_token(user_id):
    await asyncio.sleep(600)
    user_ref = db.collection("users").document(user_id)
    user_ref.update({"reset_token": firestore.DELETE_FIELD})

# Send password reset email
def send_password_reset_email(email, token):
    smtp_server = None
    try:
        smtp_server = establish_smtp_connection()

        subject = "Reset Password"
        body =  f"Token untuk reset password telah dikirim ke email: {email}.\n" \
                f"Token: {token}\n\n" \
                f"Token ini hanya bisa digunakan selama 10 menit,\n" \
                f"setelah 10 menit berlalu token tidak akan valid.\n" \
                f"Apabila anda tidak melakukan permintaan reset password, abaikan saja pesan ini.\n\n" \
                f"Mbako Project" 


        msg = EmailMessage()
        msg['Subject'] = subject
        msg['From'] = smtp_username
        msg['To'] = email
        msg.set_content(body)

        smtp_server.send_message(msg)

        smtp_server.quit()

        return {
            "message": "Email reset password telah dikirim"
            }
    except Exception as e:
        if smtp_server:
            smtp_server.quit()
        raise Exception("Failed to send password reset email") from e

secret_key = '9f6bd57617dc7827602641f05b611bf2e33f674c3b57010d7e5e42a50035bcf2'
#os.environ.get('SECRET_KEY')

# Generate access token
def generate_access_token(username: str):
    payload = {
        "exp": time.time() + 3600, 
        "sub": username
    }
    access_token = jwt.encode(payload, secret_key, algorithm='HS256')
    return access_token

def decodeJWT(access_token: str):
    try:
        decode_token = jwt.decode(access_token, secret_key, algorithms=['HS256'])
        return decode_token if decode_token['exp'] >= time.time() else None
    except jwt.DecodeError:
        return {"error": "Invalid token"}  
    except jwt.ExpiredSignatureError:
        return {"error": "token expired"}  
    
# Store image in Cloud Buckets
def upload_to_bucket(image_file):
    client = storage.Client()
    bucket = client.bucket('mbakoitem-bucket')
    image_name = f"{uuid.uuid4().hex}.jpg"
    blob = bucket.blob(f"mbako-image/{image_name}") 
    blob.upload_from_file(image_file, content_type='image/jpeg')

    return blob.public_url