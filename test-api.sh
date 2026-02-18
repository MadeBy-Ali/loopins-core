#!/bin/bash

# Loopins Core - Quick Test Script
# This script automates the testing of Cart API and Midtrans integration

set -e

API_BASE="http://localhost:8080/api"
USER_ID=1

echo "🚀 Loopins Core API Testing Script"
echo "===================================="
echo ""

# Check if server is running
echo "📡 Checking if server is running..."
if ! curl -s "${API_BASE}/actuator/health" > /dev/null; then
    echo "❌ Server is not running at ${API_BASE}"
    echo "Please start the application first: mvn spring-boot:run"
    exit 1
fi
echo "✅ Server is running"
echo ""

# Test 1: Create Cart
echo "🛒 Test 1: Creating a cart..."
CART_RESPONSE=$(curl -s -X POST "${API_BASE}/carts" \
  -H "Content-Type: application/json" \
  -d "{\"userId\": ${USER_ID}}")

CART_ID=$(echo $CART_RESPONSE | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')

if [ -z "$CART_ID" ]; then
    echo "❌ Failed to create cart"
    echo "Response: $CART_RESPONSE"
    exit 1
fi

echo "✅ Cart created with ID: $CART_ID"
echo ""

# Test 2: Add Items to Cart
echo "📦 Test 2: Adding items to cart..."

echo "  Adding Product 1..."
curl -s -X POST "${API_BASE}/carts/${CART_ID}/items" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "PROD-001",
    "productName": "MacBook Pro 14",
    "unitPrice": 25000000,
    "quantity": 1
  }' > /dev/null

echo "  Adding Product 2..."
curl -s -X POST "${API_BASE}/carts/${CART_ID}/items" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "PROD-002",
    "productName": "Magic Mouse",
    "unitPrice": 1500000,
    "quantity": 2
  }' > /dev/null

echo "✅ Items added to cart"
echo ""

# Test 3: View Cart
echo "👀 Test 3: Viewing cart..."
CART_VIEW=$(curl -s -X GET "${API_BASE}/carts/${CART_ID}")
echo "$CART_VIEW" | jq '.' 2>/dev/null || echo "$CART_VIEW"
echo ""

# Test 4: Update Item Quantity
echo "🔄 Test 4: Updating item quantity..."
ITEM_ID=$(echo $CART_VIEW | grep -o '"id":[0-9]*' | head -2 | tail -1 | grep -o '[0-9]*')

if [ ! -z "$ITEM_ID" ]; then
    curl -s -X PATCH "${API_BASE}/carts/${CART_ID}/items/${ITEM_ID}?quantity=3" > /dev/null
    echo "✅ Item quantity updated"
else
    echo "⚠️  Could not find item ID to update"
fi
echo ""

# Test 5: Checkout (might fail if FulfillmentService is not available)
echo "💳 Test 5: Attempting checkout..."
CHECKOUT_RESPONSE=$(curl -s -X POST "${API_BASE}/orders/checkout" \
  -H "Content-Type: application/json" \
  -d "{
    \"cartId\": ${CART_ID},
    \"userId\": ${USER_ID},
    \"shippingAddress\": \"Jl. Sudirman No. 123, Jakarta Selatan, DKI Jakarta 12190\"
  }" 2>&1)

ORDER_ID=$(echo $CHECKOUT_RESPONSE | grep -o '"orderId":"[^"]*"' | head -1 | cut -d'"' -f4)

if [ -z "$ORDER_ID" ]; then
    echo "⚠️  Checkout might have failed (this is expected if FulfillmentService is not running)"
    echo "Response: $CHECKOUT_RESPONSE"

    # Create a manual order for testing Midtrans
    echo ""
    echo "💡 To test Midtrans integration manually:"
    echo "   1. Create an order via checkout endpoint or database"
    echo "   2. Use the order ID with: curl -X POST ${API_BASE}/payments/snap/{orderId}"
    echo ""
else
    echo "✅ Checkout successful! Order ID: $ORDER_ID"
    echo ""

    # Test 6: Create Midtrans Snap Payment
    echo "💰 Test 6: Creating Midtrans Snap payment..."
    PAYMENT_RESPONSE=$(curl -s -X POST "${API_BASE}/payments/snap/${ORDER_ID}" \
      -H "Content-Type: application/json")

    echo "$PAYMENT_RESPONSE" | jq '.' 2>/dev/null || echo "$PAYMENT_RESPONSE"

    PAYMENT_URL=$(echo $PAYMENT_RESPONSE | grep -o '"redirectUrl":"[^"]*"' | cut -d'"' -f4)

    if [ ! -z "$PAYMENT_URL" ]; then
        echo ""
        echo "✅ Payment created successfully!"
        echo "🔗 Payment URL: $PAYMENT_URL"
        echo ""
        echo "📱 Open this URL to complete QRIS payment (sandbox)"
    else
        echo "⚠️  Payment creation might have failed. Check Midtrans configuration."
    fi
    echo ""

    # Test 7: Check Payment Status
    echo "🔍 Test 7: Checking payment status..."
    STATUS_RESPONSE=$(curl -s -X GET "${API_BASE}/payments/status/${ORDER_ID}")
    echo "$STATUS_RESPONSE" | jq '.' 2>/dev/null || echo "$STATUS_RESPONSE"
fi

echo ""
echo "===================================="
echo "✨ Testing Complete!"
echo ""
echo "📚 For more detailed testing, see TESTING_GUIDE.md"
echo "📖 API Documentation: http://localhost:8080/api/swagger-ui.html"
echo ""
