-- V2__Add_guest_cart_support.sql
-- Add support for guest/anonymous carts

-- 1. Make user_id nullable to support guest carts
ALTER TABLE cart ALTER COLUMN user_id DROP NOT NULL;

-- 2. Drop the unique constraint that required user_id
ALTER TABLE cart DROP CONSTRAINT IF EXISTS unique_active_cart;

-- 3. Add session_id for tracking anonymous users
ALTER TABLE cart ADD COLUMN session_id VARCHAR(255);

-- 4. Add index for session-based cart lookup
CREATE INDEX idx_cart_session_id ON cart(session_id) WHERE session_id IS NOT NULL;

-- 5. Add constraint: cart must have either user_id or session_id
ALTER TABLE cart ADD CONSTRAINT check_cart_owner
    CHECK ((user_id IS NOT NULL AND session_id IS NULL) OR (user_id IS NULL AND session_id IS NOT NULL));

-- 6. Add unique constraint for active guest carts
CREATE UNIQUE INDEX unique_active_session_cart
    ON cart(session_id, status)
    WHERE session_id IS NOT NULL AND status = 'ACTIVE';

-- 7. Add unique constraint for active user carts
CREATE UNIQUE INDEX unique_active_user_cart
    ON cart(user_id, status)
    WHERE user_id IS NOT NULL AND status = 'ACTIVE';
