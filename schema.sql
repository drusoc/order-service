CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    total_price DOUBLE PRECISION NOT NULL DEFAULT 0,
    total_weight DOUBLE PRECISION NOT NULL DEFAULT 0,
    delivery_address_id UUID NOT NULL,
    payment_transaction_id UUID,
    delivery_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_orders_user_id ON orders (user_id);

CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    price_at_purchase DOUBLE PRECISION NOT NULL,
    discount_at_purchase SMALLINT DEFAULT 0,
    final_price_per_item DOUBLE PRECISION NOT NULL,
    quantity INTEGER NOT NULL,
    seller_name VARCHAR(255) NOT NULL,
    article VARCHAR(255) NOT NULL,
    barcode VARCHAR(255) NOT NULL,
    weight DOUBLE PRECISION DEFAULT 0,
    width DOUBLE PRECISION DEFAULT 0,
    height DOUBLE PRECISION DEFAULT 0,
    depth DOUBLE PRECISION DEFAULT 0
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);
