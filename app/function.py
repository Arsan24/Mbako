
from random import randint
from auth.dbfirestore import db

# Generate a random token
def generate_token():
    return str(randint(100000, 999999))

# Store the token in the database
def store_token(username, token):
    user_ref = db.collection("users").document(username)
    user_ref.update({"reset_token": token})

# Send password reset email (dummy implementation)
def send_password_reset_email(email, token):
    print(f"Password reset email sent to {email}. Token: {token}")