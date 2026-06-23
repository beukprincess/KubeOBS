FROM python:3.10-alpine

ENV APP_HOME /app
WORKDIR $APP_HOME

RUN apk add --no-cache gcc musl-dev linux-headers python3-dev
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY . .

CMD [ "uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"  ]
