-- V3__Add_guest_order_support.sql
-- Add support for guest orders

-- 1. Make user_id nullable to support guest orders
ALTER TABLE orders ALTER COLUMN user_id DROP NOT NULL;

-- 2. Add guest customer info columns for orders
ALTER TABLE orders ADD COLUMN guest_email VARCHAR(255);
ALTER TABLE orders ADD COLUMN guest_name VARCHAR(255);
ALTER TABLE orders ADD COLUMN guest_phone VARCHAR(50);

-- 3. Add constraint: order must have either user_id or guest_email
ALTER TABLE orders ADD CONSTRAINT check_order_customer
    CHECK ((user_id IS NOT NULL) OR (guest_email IS NOT NULL AND guest_name IS NOT NULL));

-- 4. Add index for guest email lookup
CREATE INDEX idx_orders_guest_email ON orders(guest_email) WHERE guest_email IS NOT NULL;
