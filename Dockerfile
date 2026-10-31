FROM eclipse-temurin:17-jdk

# Install Python
RUN apt-get update && apt-get install -y \
    python3 python3-venv python3-pip \
    libxcb1 libglib2.0-0 libgl1 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Install Python dependencies
COPY requirements.txt .
RUN python3 -m venv /opt/venv
ENV PATH="/opt/venv/bin:${PATH}"
RUN pip install --no-cache-dir -r requirements.txt

# Copy project files
COPY . .

# Build Java backend
RUN chmod +x mvnw
RUN ./mvnw clean package -Dmaven.test.skip=true

EXPOSE 8080

CMD ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar target/*.jar"]