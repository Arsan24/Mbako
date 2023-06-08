import uvicorn
from fastapi import FastAPI
from routes.users import router as users_router
from routes.items import router as items_router

app = FastAPI() 

# Define the API endpoint

app.include_router(users_router)
app.include_router(items_router)

@app.get("/")
def root():
    return {"Mbako Backend"}

# Run the app
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8080)
    
