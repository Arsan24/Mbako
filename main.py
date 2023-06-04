import uvicorn
from fastapi import FastAPI, UploadFile, HTTPException, File
from pydantic import BaseModel
import tensorflow as tf
import numpy as np
from PIL import Image

# create a new FastAPI app instance

app = FastAPI() 

# define the basemodel
    
def item(BaseModel):
    image: UploadFile

# load the .tflite model

interpreter = tf.lite.Interpreter('./model/ModelV3.tflite')
interpreter.allocate_tensors()

# Define the Preprocess and Prediction Class

classification = ['Rendah', 'Sedang', 'Tinggi'] #the classification labels

def preprocess_image(image):
    img = Image.open(image.file)
    img = img.resize((224, 224))
    img = np.array(img, dtype=np.float32) / 255.0
    img = np.expand_dims(img, axis=0)
    return img

def predict(image):
    preprocessed_image = preprocess_image(image)

    # Perform prediction using the loaded TFLite model
    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()

    interpreter.set_tensor(input_details[0]['index'], preprocessed_image)
    interpreter.invoke()

    output_tensor = interpreter.get_tensor(output_details[0]['index'])
    class_index = np.argmax(output_tensor)

    if 0 <= class_index < len(classification):
        class_name = classification[class_index]
    else:
        class_name = 'Unknown'

    return class_name

# Define the API endpoint

@app.post("/predict")
async def add_item(image: UploadFile = File(...)):
    class_name = predict(image)
    return {"class_name": class_name}

if __name__ == '__main__':
    uvicorn.run(app, port=8080, host="0.0.0.0", timeout_keep_alive=1200)