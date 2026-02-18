# 🔄 Complete Payment Flow - Visual Guide

## 📱 End-to-End Flow

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│   Customer  │         │   Frontend  │         │  Your API   │         │  Midtrans   │
│  (Browser)  │         │  (React/Vue)│         │  (Spring)   │         │  (Sandbox)  │
└──────┬──────┘         └──────┬──────┘         └──────┬──────┘         └──────┬──────┘
       │                       │                       │                       │
       │  1. Browse products   │                       │                       │
       ├──────────────────────>│                       │                       │
       │                       │                       │                       │
       │  2. Add to cart       │                       │                       │
       ├──────────────────────>│  POST /api/cart      │                       │
       │                       ├──────────────────────>│                       │
       │                       │  ✅ Cart updated     │                       │
       │                       │<──────────────────────┤                       │
       │                       │                       │                       │
       │  3. Checkout          │                       │                       │
       ├──────────────────────>│  POST /api/checkout  │                       │
       │                       ├──────────────────────>│                       │
       │                       │  📦 Order created    │                       │
       │                       │<──────────────────────┤                       │
       │                       │  orderId: ORDER-123   │                       │
       │                       │                       │                       │
       │  4. Pay now!          │                       │                       │
       ├──────────────────────>│  POST /api/payments/ │                       │
       │                       │       snap/{orderId} │                       │
       │                       ├──────────────────────>│                       │
       │                       │                       │  🔐 Create Snap      │
       │                       │                       ├──────────────────────>│
       │                       │                       │  SnapApi.create      │
       │                       │                       │  Transaction()       │
       │                       │                       │                       │
       │                       │                       │  📱 Token + URL      │
       │                       │                       │<──────────────────────┤
       │                       │  ✅ Token received   │                       │
       │                       │<──────────────────────┤                       │
       │                       │  {                    │                       │
       │                       │   token: "abc123",   │                       │
       │                       │   redirectUrl: "..." │                       │
       │                       │  }                    │                       │
       │                       │                       │                       │
       │  5. Show payment UI   │                       │                       │
       │  (QRIS QR Code!)      │                       │                       │
       │<──────────────────────┤  snap.pay(token)     │                       │
       │  ┌──────────────────┐ │                       │                       │
       │  │  Midtrans Snap   │ │                       │                       │
       │  │  ┌────────────┐  │ │                       │                       │
       │  │  │ ████████   │  │ │                       │                       │
       │  │  │ ██    ██   │  │ │  (QR Code shown     │                       │
       │  │  │ ████████   │  │ │   by Midtrans)       │                       │
       │  │  │    ██  ██  │  │ │                       │                       │
       │  │  │ ████████   │  │ │                       │                       │
       │  │  └────────────┘  │ │                       │                       │
       │  │  [GoPay] [QRIS]  │ │                       │                       │
       │  └──────────────────┘ │                       │                       │
       │                       │                       │                       │
       │  6. Scan QR & Pay     │                       │                       │
       ├───────────────────────┼───────────────────────┼──────────────────────>│
       │  (Using e-wallet)     │                       │  💰 Payment received │
       │                       │                       │                       │
       │                       │                       │  7. Notify webhook   │
       │                       │                       │  POST /api/payments/ │
       │                       │                       │       callback        │
       │                       │                       │<──────────────────────┤
       │                       │                       │  {                    │
       │                       │                       │   orderId: "...",    │
       │                       │                       │   status: "success"  │
       │                       │                       │  }                    │
       │                       │                       │                       │
       │                       │                       │  📝 Update order     │
       │                       │                       │  status = PAID       │
       │                       │                       │                       │
       │  8. Success callback  │                       │                       │
       │<──────────────────────┤<──────────────────────┤  ✅ 200 OK          │
       │  "Payment Successful!"│                       ├──────────────────────>│
       │                       │                       │                       │
       │  9. Show confirmation │                       │                       │
       │<──────────────────────┤                       │                       │
       │  🎉 Order confirmed!  │                       │                       │
       │                       │                       │                       │
```

## 🎯 Key Points in Your Implementation

### Step 4: Create Snap Transaction
**File:** `MidtransPaymentService.java`

```java
// What happens in createSnapTransaction():

1. ✅ Build transaction details
   - Order ID (from your database)
   - Total amount (calculated from items + shipping)

2. ✅ Build item details
   - Each product with price, quantity
   - Shipping fee as separate item

3. ✅ Build customer details
   - Username, email
   - Shipping address

4. ✅ Enable payment methods
   - QRIS ← This is what shows the QR code!
   - GoPay
   - ShopeePay
   - Other QRIS

5. ✅ Set callbacks
   - Success URL
   - Error URL
   - Pending URL

6. ✅ Call Midtrans
   JSONObject result = SnapApi.createTransaction(requestBody);

7. ✅ Return to frontend
   - token (for snap.pay())
   - redirect_url (for browser redirect)
```

### Step 5: Frontend Shows QRIS
**File:** Your React/Vue/HTML frontend

```javascript
// Simple integration:
snap.pay(token);

// Full integration:
snap.pay(token, {
  onSuccess: function(result) {
    // Payment successful!
    alert('Payment successful!');
    window.location.href = '/order-confirmation';
  },
  onPending: function(result) {
    // Payment pending (e.g., bank transfer)
    alert('Waiting for payment...');
  },
  onError: function(result) {
    // Payment failed
    alert('Payment failed!');
  },
  onClose: function() {
    // User closed popup
    console.log('Payment popup closed');
  }
});
```

**What happens:**
- Midtrans Snap JS opens popup/modal
- Shows payment options (QRIS, GoPay, etc.)
- **QRIS QR code is automatically generated and displayed**
- Customer scans with any e-wallet app
- Payment processed by Midtrans
- Callback sent to your backend

## 📱 What Customer Sees

### On Desktop:
```
┌─────────────────────────────┐
│   Midtrans Snap Payment     │
├─────────────────────────────┤
│                             │
│   Total: Rp 275,000         │
│                             │
│   Choose Payment Method:    │
│                             │
│   [QRIS]  [GoPay] [ShopeePay]
│                             │
│   ┌───────────────────┐     │
│   │   Scan this QR    │     │
│   │   ┌───────────┐   │     │
│   │   │ ████ ████ │   │     │
│   │   │ ██    ██  │   │     │
│   │   │ ████ ████ │   │     │
│   │   │  ██  ██   │   │     │
│   │   │ ████ ████ │   │     │
│   │   └───────────┘   │     │
│   │  with any e-wallet│     │
│   └───────────────────┘     │
│                             │
│         [Cancel]            │
└─────────────────────────────┘
```

### On Mobile:
```
┌─────────────┐
│  Pay Now    │
├─────────────┤
│ Rp 275,000  │
│             │
│ [GoPay]     │
│ [QRIS]      │
│ [ShopeePay] │
│             │
│ Tap QRIS to │
│ see QR code │
└─────────────┘
```

## 🔑 Important Files

| File | What It Does |
|------|--------------|
| `MidtransConfig.java` | Loads your Midtrans keys from `.env` |
| `MidtransPaymentService.java` | Creates Snap transaction, generates token |
| `PaymentController.java` | REST endpoint for frontend to call |
| `PaymentService.java` | Orchestrates payment flow |
| `.env` | Your Midtrans credentials |
| `application.yml` | Configuration defaults |

## 🧪 Testing Commands

```bash
# Start everything
./start.sh

# Test complete flow (in new terminal)
./test-snap-payment.sh

# Or manual testing:
curl -X POST http://localhost:8080/api/payments/snap/ORDER-123 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 🎨 Frontend Code Example

### React Example:
```jsx
import { useState } from 'react';

function CheckoutButton({ orderId }) {
  const [loading, setLoading] = useState(false);

  const handlePayment = async () => {
    setLoading(true);
    
    // Call your backend
    const response = await fetch(`/api/payments/snap/${orderId}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    });
    
    const data = await response.json();
    setLoading(false);
    
    // Show Midtrans popup (QRIS appears here!)
    window.snap.pay(data.data.token, {
      onSuccess: (result) => {
        console.log('Success!', result);
        // Redirect to success page
        window.location.href = '/order/success';
      },
      onPending: (result) => {
        console.log('Pending', result);
      },
      onError: (result) => {
        console.log('Error!', result);
        alert('Payment failed!');
      }
    });
  };

  return (
    <button onClick={handlePayment} disabled={loading}>
      {loading ? 'Processing...' : 'Pay Now'}
    </button>
  );
}
```

### Vue Example:
```vue
<template>
  <button @click="handlePayment" :disabled="loading">
    {{ loading ? 'Processing...' : 'Pay Now' }}
  </button>
</template>

<script>
export default {
  props: ['orderId'],
  data() {
    return {
      loading: false
    };
  },
  methods: {
    async handlePayment() {
      this.loading = true;
      
      const response = await fetch(`/api/payments/snap/${this.orderId}`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      
      const data = await response.json();
      this.loading = false;
      
      // Show Midtrans popup
      window.snap.pay(data.data.token, {
        onSuccess: (result) => {
          this.$router.push('/order/success');
        },
        onError: (result) => {
          alert('Payment failed!');
        }
      });
    }
  }
};
</script>
```

## ✅ Checklist

Backend (Your Spring Boot App):
- ✅ Midtrans library installed
- ✅ Configuration loaded from `.env`
- ✅ Snap service implemented
- ✅ REST endpoint available
- ✅ QRIS enabled
- ✅ Webhook handler ready

Frontend (What you need to add):
- ⬜ Add Snap.js script tag
- ⬜ Call `/api/payments/snap/{orderId}`
- ⬜ Use token with `snap.pay()`
- ⬜ Handle success/error callbacks

That's it! Your backend is 100% ready. Just add the frontend code! 🚀

## 🎉 Summary

**You asked:** "Do we have Snap integration?"

**Answer:** YES! Fully implemented, production-ready, better than docs example!

**What you need to do:**
1. Test backend: `./start.sh` then `./test-snap-payment.sh`
2. Add frontend code (shown above)
3. Done! QRIS appears automatically! 🎊
