-- V1__Initial_schema.sql
-- Core Service Database Schema

-- Users table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL DEFAULT 'CUSTOMER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Cart table
CREATE TABLE cart (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_active_cart UNIQUE (user_id, status)
);

-- Cart items table
CREATE TABLE cart_item (
    id SERIAL PRIMARY KEY,
    cart_id INTEGER NOT NULL REFERENCES cart(id) ON DELETE CASCADE,
    product_id VARCHAR(100) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    unit_price DECIMAL(19, 2) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT positive_quantity CHECK (quantity > 0),
    CONSTRAINT positive_price CHECK (unit_price >= 0)
);

-- Orders table
CREATE TABLE orders (
    id VARCHAR(50) PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    cart_id INTEGER REFERENCES cart(id),
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    subtotal DECIMAL(19, 2) NOT NULL DEFAULT 0,
    shipping_fee DECIMAL(19, 2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(19, 2) NOT NULL DEFAULT 0,
    shipping_address TEXT,
    payment_url TEXT,
    payment_reference VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP,
    shipped_at TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT positive_amounts CHECK (subtotal >= 0 AND shipping_fee >= 0 AND total_amount >= 0)
);

-- Order items table
CREATE TABLE order_item (
    id SERIAL PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id VARCHAR(100) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    unit_price DECIMAL(19, 2) NOT NULL,
    quantity INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT positive_order_quantity CHECK (quantity > 0),
    CONSTRAINT positive_order_price CHECK (unit_price >= 0)
);

-- Indexes for performance
CREATE INDEX idx_cart_user_id ON cart(user_id);
CREATE INDEX idx_cart_status ON cart(status);
CREATE INDEX idx_cart_item_cart_id ON cart_item(cart_id);
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_order_item_order_id ON order_item(order_id);

-- Idempotency table for payment callbacks
CREATE TABLE payment_callback_log (
    id SERIAL PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL REFERENCES orders(id),
    callback_reference VARCHAR(255) NOT NULL UNIQUE,
    callback_type VARCHAR(50) NOT NULL,
    payload TEXT,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_callback UNIQUE (order_id, callback_reference)
);

CREATE INDEX idx_payment_callback_order_id ON payment_callback_log(order_id);
CREATE INDEX idx_payment_callback_reference ON payment_callback_log(callback_reference);

