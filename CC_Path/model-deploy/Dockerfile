FROM python:3.10

WORKDIR /app

COPY requirements.txt .

RUN pip --no-cache-dir-- --upgrade install -r requirements.txt

COPY . .

EXPOSE 8000

ENV PYTHONUNBUFFERED=1

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]