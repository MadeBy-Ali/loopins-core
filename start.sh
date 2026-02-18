#!/bin/bash

# Loopins Core - Quick Start Script
# This script helps you start the application quickly

set -e

echo "🚀 Loopins Core - Quick Start"
echo "============================="
echo ""

# Load .env file if it exists
if [ -f ".env" ]; then
    echo "📝 Loading environment variables from .env..."
    set -a
    source .env
    set +a
else
    echo "⚠️  No .env file found, using defaults from application.yml"
fi

# Step 1: Start PostgreSQL
echo "📦 Step 1: Starting PostgreSQL with Docker..."
if docker ps | grep -q loopins-postgres; then
    echo "✅ PostgreSQL is already running"
else
    if docker ps -a | grep -q loopins-postgres; then
        echo "🔄 Starting existing PostgreSQL container..."
        docker start loopins-postgres
    else
        echo "🆕 Creating new PostgreSQL container..."
        docker-compose up -d
    fi

    # Wait for PostgreSQL to be ready
    echo "⏳ Waiting for PostgreSQL to be ready..."
    sleep 5

    # Check if PostgreSQL is ready
    until docker exec loopins-postgres pg_isready -U loopins > /dev/null 2>&1; do
        echo "   Still waiting..."
        sleep 2
    done

    echo "✅ PostgreSQL is ready!"
fi
echo ""

echo "🔐 Step 2: Checking Midtrans Configuration..."

missing=false

mask() {
  local value="$1"
  echo "${value:0:4}****${value: -4}"
}

# Server Key
if [ -z "$MIDTRANS_SERVER_KEY" ]; then
    echo "❌ MIDTRANS_SERVER_KEY is NOT set"
    missing=true
else
    echo "✅ MIDTRANS_SERVER_KEY: $(mask "$MIDTRANS_SERVER_KEY")"
fi

# Client Key
if [ -z "$MIDTRANS_CLIENT_KEY" ]; then
    echo "❌ MIDTRANS_CLIENT_KEY is NOT set"
    missing=true
else
    echo "✅ MIDTRANS_CLIENT_KEY: $(mask "$MIDTRANS_CLIENT_KEY")"
fi

# Merchant ID
if [ -z "$MIDTRANS_MERCHANT_ID" ]; then
    echo "❌ MIDTRANS_MERCHANT_ID is NOT set"
    missing=true
else
    echo "✅ MIDTRANS_MERCHANT_ID: $(mask "$MIDTRANS_MERCHANT_ID")"
fi

# Environment
if [ -z "$MIDTRANS_IS_PRODUCTION" ]; then
    echo "⚠️  MIDTRANS_IS_PRODUCTION not set (defaulting to false)"
else
    echo "🌍 MIDTRANS ENVIRONMENT: $MIDTRANS_IS_PRODUCTION"
fi

if [ "$missing" = true ]; then
    echo ""
    echo "❌ Midtrans configuration incomplete."
    echo "   Please set all required variables in .env"
    exit 1
fi

echo "✅ Midtrans configuration OK"
echo ""

# Step 3: Build the application
echo "🔨 Step 3: Building the application..."
if [ -f "mvnw" ]; then
    echo "Using Maven wrapper..."
    ./mvnw clean package -DskipTests
elif command -v mvn &> /dev/null; then
    echo "Using system Maven..."
    mvn clean package -DskipTests
else
    echo "❌ Error: Maven not found. Please install Maven or use the Maven wrapper."
    exit 1
fi
echo ""

# Step 4: Run the application
echo "🎯 Step 4: Starting the application..."
echo "========================================"
echo ""
echo "📡 The application will be available at:"
echo "   API: http://localhost:8080/api"
echo "   Swagger UI: http://localhost:8080/api/swagger-ui.html"
echo ""
echo "Press Ctrl+C to stop the application"
echo ""
echo "========================================"
echo ""

if [ -f "mvnw" ]; then
    ./mvnw spring-boot:run
else
    mvn spring-boot:run
fi
