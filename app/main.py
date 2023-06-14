import uvicorn
from fastapi import FastAPI, Request, status 
from fastapi.exceptions import HTTPException, RequestValidationError
from fastapi.responses import JSONResponse
from fastapi_sessions import SessionMiddleware, CORSMiddleware
from routes.users import router as users_router
from routes.items import router as items_router

app = FastAPI() 


# Exception handler

@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    return JSONResponse(
        status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
        content={"error": True, "message": "Validation Error", "details": exc.errors()},
    )

@app.exception_handler(HTTPException)
async def http_exception_handler(request: Request, exc: HTTPException):
    return JSONResponse(
        status_code=exc.status_code,
        content={"error": True, "message": exc.detail},
    )

# Add session middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
    allow_credentials=True,
)
secret_key="9f6bd57617dc7827602641f05b611bf2e33f674c3b57010d7e5e42a50035bcf2"
app.add_middleware(SessionMiddleware, secret_key)

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
    
