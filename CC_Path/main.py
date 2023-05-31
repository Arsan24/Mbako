import uvicorn
from pydantic import BaseModel
from fastapi import FastAPI, HTTPException, Path, Query
import tensorflow as tf
import os

# create a new FastAPI app instance

app = FastAPI() 

# load the .tflite model

interpreter = tf.lite.Interpreter('../ML_Path/ModelV1.tflite')
interpreter.allocate_tensors()

# Define a Pydantic model for an item

port = int(os.getenv("PORT"))

class Item(BaseModel):
    image: str

# create a route fot the prediction

def predict():
    

@app.get("/")
def root():
    return ("Welcome to FastAPI")


@app.post("/")
def add_item(item: Item):
    result = predict(item.)
    return {result}


if __name__ == '__main__':
    uvicorn.run(app, host="0.0.0.0", port=port, timeout_keep_alive=1200)