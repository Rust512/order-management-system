CREATE UNIQUE INDEX uq_user_on_status_created
    ON orders (user_id)
    WHERE order_status = 'CREATED';