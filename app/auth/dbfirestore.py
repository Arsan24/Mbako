import firebase_admin
from firebase_admin import credentials, firestore

# initialize Firebase Admin SDK
cred = credentials.Certificate("auth/c23-ps170-key.json")
firebase_admin.initialize_app(cred)
db = firestore.client()
