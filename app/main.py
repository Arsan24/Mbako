import uvicorn
from fastapi import FastAPI, Request, HTTPException, status 
from fastapi.responses import JSONResponse
from routes.users import router as users_router
from routes.items import router as items_router

app = FastAPI() 


# Exception handler

@app.exception_handler(HTTPException)
async def http_exception_handler(request: Request, exc: HTTPException):
    if exc.status_code == status.HTTP_422_UNPROCESSABLE_ENTITY:
        return JSONResponse(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            content={"error": True, "message": "Unprocessable Entity"},
        )
    else:
        return JSONResponse(
            status_code=exc.status_code,
            content={"error": True, "message": exc.detail},
        )

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
    
