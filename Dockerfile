FROM python:3.10.11

# Make working directories
WORKDIR  /app

# Upgrade pip with no cache
RUN pip install --upgrade pip

# Copy application requirements file to the created working directory
COPY requirements.txt .

# Install application dependencies from the requirements file
RUN pip install -r requirements.txt

# Copy every file in the source folder to the created working directory
COPY  . .

# Expose to route 8080
EXPOSE 8080

CMD ["uvicorn", "--host", "0.0.0.0", "--port", "8080", "main:app"]