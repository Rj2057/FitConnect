ALTER TABLE IF EXISTS memberships
    ADD COLUMN IF NOT EXISTS duration_months INTEGER;

UPDATE memberships
SET duration_months = GREATEST(
    1,
    COALESCE(
        (
            EXTRACT(YEAR FROM age(end_date, start_date)) * 12
            + EXTRACT(MONTH FROM age(end_date, start_date))
        )::INTEGER,
        1
    )
)
WHERE duration_months IS NULL;

ALTER TABLE IF EXISTS memberships
    ALTER COLUMN duration_months SET DEFAULT 1;

ALTER TABLE IF EXISTS memberships
    ALTER COLUMN duration_months SET NOT NULL;

ALTER TABLE IF EXISTS gyms
    ADD COLUMN IF NOT EXISTS monthly_fee DOUBLE PRECISION;

UPDATE gyms
SET monthly_fee = 0
WHERE monthly_fee IS NULL;

ALTER TABLE IF EXISTS gyms
    ALTER COLUMN monthly_fee SET DEFAULT 0;

ALTER TABLE IF EXISTS gyms
    ALTER COLUMN monthly_fee SET NOT NULL;

ALTER TABLE IF EXISTS trainer_bookings
    ADD COLUMN IF NOT EXISTS trainer_response_message VARCHAR(1000);

ALTER TABLE IF EXISTS trainer_bookings
    ADD COLUMN IF NOT EXISTS trainer_proposed_time_slot VARCHAR(255);

CREATE TABLE IF NOT EXISTS equipment_rental_listings (
    id BIGSERIAL PRIMARY KEY,
    seller_owner_id BIGINT NOT NULL REFERENCES users(id),
    seller_gym_id BIGINT NOT NULL REFERENCES gyms(id),
    equipment_name VARCHAR(255) NOT NULL,
    details VARCHAR(2000),
    monthly_rent_price NUMERIC(12, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS equipment_rental_transactions (
    id BIGSERIAL PRIMARY KEY,
    seller_owner_id BIGINT NOT NULL REFERENCES users(id),
    buyer_owner_id BIGINT NOT NULL REFERENCES users(id),
    seller_gym_id BIGINT NOT NULL REFERENCES gyms(id),
    buyer_gym_id BIGINT NOT NULL REFERENCES gyms(id),
    equipment_name VARCHAR(255) NOT NULL,
    details VARCHAR(2000),
    monthly_rent_price NUMERIC(12, 2) NOT NULL,
    purchased_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
