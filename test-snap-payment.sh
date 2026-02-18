#!/bin/bash

# Test Midtrans Snap Payment Flow
# This script tests the complete cart → checkout → payment flow

set -e

BASE_URL="http://localhost:8080/api"
TOKEN=""

echo "🧪 Testing Midtrans Snap Payment Flow"
echo "======================================"
echo ""

# Step 1: Login
echo "1️⃣  Logging in..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123"
  }')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | sed 's/"token":"//')

if [ -z "$TOKEN" ]; then
    echo "❌ Login failed!"
    echo "Response: $LOGIN_RESPONSE"
    exit 1
fi

echo "✅ Logged in successfully"
echo ""

# Step 2: Add items to cart
echo "2️⃣  Adding items to cart..."
curl -s -X POST "$BASE_URL/cart" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "PROD-001",
    "quantity": 2
  }' > /dev/null

curl -s -X POST "$BASE_URL/cart" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "PROD-002",
    "quantity": 1
  }' > /dev/null

echo "✅ Items added to cart"
echo ""

# Step 3: View cart
echo "3️⃣  Viewing cart..."
CART_RESPONSE=$(curl -s -X GET "$BASE_URL/cart" \
  -H "Authorization: Bearer $TOKEN")

echo "$CART_RESPONSE" | grep -q "PROD-001"
echo "✅ Cart retrieved"
echo ""

# Step 4: Create checkout/order
echo "4️⃣  Creating checkout..."
CHECKOUT_RESPONSE=$(curl -s -X POST "$BASE_URL/checkout" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "shippingAddress": "Jl. Test Street No. 123, Jakarta",
    "shippingMethod": "JNE_REG"
  }')

ORDER_ID=$(echo $CHECKOUT_RESPONSE | grep -o '"orderId":"[^"]*' | sed 's/"orderId":"//')

if [ -z "$ORDER_ID" ]; then
    echo "❌ Checkout failed!"
    echo "Response: $CHECKOUT_RESPONSE"
    exit 1
fi

echo "✅ Order created: $ORDER_ID"
echo ""

# Step 5: Create Snap payment
echo "5️⃣  Creating Midtrans Snap payment..."
SNAP_RESPONSE=$(curl -s -X POST "$BASE_URL/payments/snap/$ORDER_ID" \
  -H "Authorization: Bearer $TOKEN")

echo ""
echo "📦 Snap Payment Response:"
echo "========================"
echo "$SNAP_RESPONSE" | jq '.' 2>/dev/null || echo "$SNAP_RESPONSE"
echo ""

# Extract token and URL
SNAP_TOKEN=$(echo $SNAP_RESPONSE | grep -o '"token":"[^"]*' | sed 's/"token":"//')
REDIRECT_URL=$(echo $SNAP_RESPONSE | grep -o '"redirectUrl":"[^"]*' | sed 's/"redirectUrl":"//')

if [ -z "$SNAP_TOKEN" ]; then
    echo "❌ Snap payment creation failed!"
    echo "Response: $SNAP_RESPONSE"
    exit 1
fi

echo "✅ Snap payment created successfully!"
echo ""
echo "🎉 SUCCESS! Payment Details:"
echo "======================================"
echo "Order ID:     $ORDER_ID"
echo "Snap Token:   $SNAP_TOKEN"
echo "Payment URL:  $REDIRECT_URL"
echo ""
echo "📱 Frontend Integration:"
echo "------------------------"
echo "Use this token in your frontend:"
echo ""
echo "  snap.pay('$SNAP_TOKEN', {"
echo "    onSuccess: function(result) { console.log(result); },"
echo "    onPending: function(result) { console.log(result); },"
echo "    onError: function(result) { console.log(result); }"
echo "  });"
echo ""
echo "Or open this URL in browser to test payment:"
echo "$REDIRECT_URL"
echo ""
echo "✅ Test completed successfully!"
