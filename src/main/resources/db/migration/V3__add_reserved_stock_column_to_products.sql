ALTER TABLE products
    ADD COLUMN reserved_stock BIGINT NOT NULL DEFAULT 0
        CHECK ( reserved_stock >= 0 AND reserved_stock <= stock ); 