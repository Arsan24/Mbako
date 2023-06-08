import firebase_admin
from firebase_admin import credentials, firestore

# initialize Firebase Admin SDK
cred = credentials.Certificate("auth/serviceAccountKey.json")
firebase_admin.initialize_app(cred)
db = firestore.client()
