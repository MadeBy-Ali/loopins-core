-- Products catalog table
CREATE TABLE products (
    id VARCHAR(100) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(19, 2) NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT positive_price CHECK (price >= 0),
    CONSTRAINT non_negative_stock CHECK (stock >= 0)
);

-- Seed products (base IDs — size variants share same price)
INSERT INTO products (id, name, price, stock) VALUES
    ('mbok-jamu-men-vest-001', 'Mbok Jamu Batik Vest - Men', 599000, 100),
    ('mbok-jamu-women-vest-001', 'Mbok Jamu Batik Vest - Women', 649000, 100);
