import firebase_admin
from firebase_admin import credentials, firestore
import os
import base64
import json

# Retrieve the base64-encoded service account key from the environment variable
encoded_key = os.environ.get('SERVICE_ACCOUNT_KEY')

# Decode the base64-encoded string and load it as JSON
key_data = base64.b64decode(encoded_key)
cred = credentials.Certificate(json.loads(key_data))

# initialize Firebase Admin SDK
firebase_admin.initialize_app(cred)
db = firestore.client()
