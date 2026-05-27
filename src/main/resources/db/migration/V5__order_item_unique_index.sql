CREATE UNIQUE INDEX uq_product_per_order
    ON order_items (order_id, product_id);