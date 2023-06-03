FROM python:3.10

# Make working directories
RUN  mkdir -p  /mbako-api
WORKDIR  /mbako-api

# Upgrade pip with no cache
RUN pip install --no-cache-dir -U pip

# Copy application requirements file to the created working directory
COPY requirements.txt .

# Install application dependencies from the requirements file
RUN pip install -r requirements.txt

# Copy every file in the source folder to the created working directory
COPY  . .

# Expose to route 8080
EXPOSE 8080

ENV PYTHONUNBUFFERED=1

CMD ["uvicorn", "--host", "0.0.0.0", "--port", "8080", "main:app"]