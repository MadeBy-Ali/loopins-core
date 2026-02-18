#!/bin/bash

# Test Guest Cart Flow
# This script tests the complete guest cart flow without requiring user login

set -e

BASE_URL="http://localhost:8080"
API_BASE="${BASE_URL}/carts"

echo "🧪 Testing Guest Cart Flow"
echo "============================"
echo ""

# Generate a unique session ID for this test
SESSION_ID="guest-$(uuidgen | tr '[:upper:]' '[:lower:]')"
echo "🔑 Generated Session ID: $SESSION_ID"
echo ""

# Step 1: Create a guest cart
echo "📦 Step 1: Creating guest cart..."
CART_RESPONSE=$(curl -s -X POST "${API_BASE}" \
  -H "Content-Type: application/json" \
  -d "{
    \"sessionId\": \"${SESSION_ID}\"
  }")

echo "$CART_RESPONSE" | jq .

CART_ID=$(echo "$CART_RESPONSE" | jq -r '.data.id')
echo "✅ Guest cart created with ID: $CART_ID"
echo ""

# Step 2: Add first item to cart
echo "🛒 Step 2: Adding first product to cart..."
curl -s -X POST "${API_BASE}/${CART_ID}/items" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "PROD-001",
    "productName": "Wireless Mouse",
    "unitPrice": 299000,
    "quantity": 2
  }' | jq .

echo "✅ Product added to cart"
echo ""

# Step 3: Add second item
echo "🛒 Step 3: Adding second product to cart..."
curl -s -X POST "${API_BASE}/${CART_ID}/items" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "PROD-002",
    "productName": "Mechanical Keyboard",
    "unitPrice": 850000,
    "quantity": 1
  }' | jq .

echo "✅ Second product added"
echo ""

# Step 4: Retrieve cart by session ID
echo "📋 Step 4: Retrieving cart by session ID..."
curl -s -X GET "${API_BASE}/session/${SESSION_ID}" | jq .
echo ""

# Step 5: Update item quantity
echo "🔄 Step 5: Updating first item quantity to 3..."
ITEM_ID=$(curl -s -X GET "${API_BASE}/${CART_ID}" | jq -r '.data.items[0].id')
curl -s -X PATCH "${API_BASE}/${CART_ID}/items/${ITEM_ID}?quantity=3" | jq .
echo "✅ Quantity updated"
echo ""

# Step 6: View final cart
echo "📊 Step 6: Final cart summary..."
FINAL_CART=$(curl -s -X GET "${API_BASE}/${CART_ID}")
echo "$FINAL_CART" | jq .

SUBTOTAL=$(echo "$FINAL_CART" | jq -r '.data.subtotal')
TOTAL_ITEMS=$(echo "$FINAL_CART" | jq -r '.data.totalItems')

echo ""
echo "✅ Guest Cart Test Completed!"
echo "================================"
echo "Cart ID: $CART_ID"
echo "Session ID: $SESSION_ID"
echo "Total Items: $TOTAL_ITEMS"
echo "Subtotal: Rp $SUBTOTAL"
echo ""
echo "💡 This cart is now ready for checkout without requiring user login!"
echo "When user logs in, call: POST /carts/merge?sessionId=${SESSION_ID}&userId=<USER_ID>"
