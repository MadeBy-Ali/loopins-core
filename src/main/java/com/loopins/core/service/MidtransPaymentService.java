package com.loopins.core.service;

import com.loopins.core.config.MidtransConfig;
import com.loopins.core.domain.entity.Order;
import com.loopins.core.domain.entity.OrderItem;
import com.midtrans.httpclient.SnapApi;
import com.midtrans.httpclient.error.MidtransError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for Midtrans payment integration
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MidtransPaymentService {

    private final MidtransConfig midtransConfig;

    /**
     * Creates Midtrans Snap transaction for QRIS payment
     *
     * @param order The order to create payment for
     * @return Map containing token and redirect_url
     */
    public Map<String, String> createSnapTransaction(Order order) {
        try {
            log.info("Creating Midtrans Snap transaction for order: {}", order.getId());

            // Build transaction details
            JSONObject transactionDetails = new JSONObject();
            transactionDetails.put("order_id", order.getId());
            transactionDetails.put("gross_amount", order.getTotalAmount().longValue());

            // Build item details
            JSONArray itemDetails = new JSONArray();
            for (OrderItem item : order.getItems()) {
                JSONObject itemDetail = new JSONObject();
                itemDetail.put("id", item.getProductId());
                itemDetail.put("name", item.getProductName());
                itemDetail.put("price", item.getUnitPrice().longValue());
                itemDetail.put("quantity", item.getQuantity());
                itemDetails.put(itemDetail);
            }

            // Add shipping fee as item
            if (order.getShippingFee().longValue() > 0) {
                JSONObject shippingItem = new JSONObject();
                shippingItem.put("id", "SHIPPING");
                shippingItem.put("name", "Shipping Fee");
                shippingItem.put("price", order.getShippingFee().longValue());
                shippingItem.put("quantity", 1);
                itemDetails.put(shippingItem);
            }

            // Build customer details (supports both user and guest orders)
            JSONObject customerDetails = new JSONObject();
            customerDetails.put("first_name", order.getCustomerName());
            customerDetails.put("email", order.getCustomerEmail());

            // Add phone if available (guest orders)
            if (order.isGuestOrder() && order.getGuestPhone() != null) {
                customerDetails.put("phone", order.getGuestPhone());
            }

            // Add shipping address if available
            if (order.getShippingAddress() != null && !order.getShippingAddress().isEmpty()) {
                JSONObject shippingAddress = new JSONObject();
                shippingAddress.put("address", order.getShippingAddress());
                customerDetails.put("shipping_address", shippingAddress);
            }

            // Build enabled payments (QRIS included)
            JSONArray enabledPayments = new JSONArray();
            enabledPayments.put("gopay");
            enabledPayments.put("shopeepay");
            enabledPayments.put("qris");
            enabledPayments.put("other_qris");

            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("transaction_details", transactionDetails);
            requestBody.put("item_details", itemDetails);
            requestBody.put("customer_details", customerDetails);
            requestBody.put("enabled_payments", enabledPayments);

            // Set callbacks
            Map<String, String> callbacks = new HashMap<>();
            callbacks.put("finish", midtransConfig.getCallbackUrl() + "/finish");
            callbacks.put("error", midtransConfig.getCallbackUrl() + "/error");
            callbacks.put("pending", midtransConfig.getCallbackUrl() + "/pending");
            requestBody.put("callbacks", callbacks);

            // Create transaction using static API
            JSONObject result = SnapApi.createTransaction(requestBody);

            log.info("Midtrans Snap transaction created successfully for order: {}", order.getId());
            log.debug("Snap response: {}", result);

            Map<String, String> response = new HashMap<>();
            response.put("token", result.getString("token"));
            response.put("redirect_url", result.getString("redirect_url"));

            return response;

        } catch (MidtransError e) {
            log.error("Midtrans error creating transaction for order {}: {} - {}",
                    order.getId(), e.getMessage(), e.getResponseBody());
            throw new RuntimeException("Failed to create Midtrans transaction: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error creating Midtrans transaction for order {}: {}",
                    order.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to create Midtrans transaction", e);
        }
    }
}
