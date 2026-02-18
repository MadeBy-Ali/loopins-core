package com.loopins.core.domain.entity;

import com.loopins.core.domain.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    // Guest customer info (for guest checkout)
    @Column(name = "guest_email")
    private String guestEmail;

    @Column(name = "guest_name")
    private String guestName;

    @Column(name = "guest_phone", length = 50)
    private String guestPhone;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private OrderStatus status = OrderStatus.DRAFT;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "shipping_fee", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "payment_url", columnDefinition = "TEXT")
    private String paymentUrl;

    @Column(name = "payment_reference")
    private String paymentReference;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = "ORDER-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }

    // Domain methods
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void calculateTotals() {
        this.subtotal = items.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalAmount = this.subtotal.add(this.shippingFee);
    }

    public void markAsCreated() {
        this.status = OrderStatus.CREATED;
    }

    public void markAsPaymentPending(String paymentUrl, String paymentReference) {
        this.status = OrderStatus.PAYMENT_PENDING;
        this.paymentUrl = paymentUrl;
        this.paymentReference = paymentReference;
    }

    public void markAsPaid() {
        this.status = OrderStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    public void markAsPaymentFailed() {
        this.status = OrderStatus.PAYMENT_FAILED;
    }

    public void markAsCancelled() {
        this.status = OrderStatus.CANCELLED;
    }

    public void markAsShipped() {
        this.status = OrderStatus.SHIPPED;
        this.shippedAt = LocalDateTime.now();
    }

    public void markAsCompleted() {
        this.status = OrderStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public boolean canBeConfirmed() {
        return this.status == OrderStatus.PAYMENT_PENDING;
    }

    public boolean canInitiatePayment() {
        return this.status == OrderStatus.DRAFT
            || this.status == OrderStatus.CREATED
            || this.status == OrderStatus.PAYMENT_FAILED;
    }

    public boolean canBeCancelled() {
        return this.status == OrderStatus.DRAFT
            || this.status == OrderStatus.CREATED
            || this.status == OrderStatus.PAYMENT_PENDING
            || this.status == OrderStatus.PAYMENT_FAILED;
    }

    public boolean isPaid() {
        return this.status == OrderStatus.PAID
            || this.status == OrderStatus.SHIPPED
            || this.status == OrderStatus.COMPLETED;
    }

    public boolean isGuestOrder() {
        return user == null && guestEmail != null;
    }

    public boolean isUserOrder() {
        return user != null;
    }

    public String getCustomerEmail() {
        return user != null ? user.getEmail() : guestEmail;
    }

    public String getCustomerName() {
        return user != null ? user.getUsername() : guestName;
    }
}

