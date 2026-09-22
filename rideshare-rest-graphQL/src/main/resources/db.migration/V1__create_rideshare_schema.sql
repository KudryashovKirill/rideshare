CREATE TABLE users (
    id UUID PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    birth_date DATE,
    version INT NOT NULL DEFAULT 0
);

CREATE TABLE rides (
    id UUID PRIMARY KEY,
    driver_id UUID NOT NULL,
    departure_city VARCHAR(100) NOT NULL,
    arrival_city VARCHAR(100) NOT NULL,
    departure_time TIMESTAMP,
    arrival_time TIMESTAMP,
    total_seats INT NOT NULL,
    free_seats INT NOT NULL,
    price INT,
    status VARCHAR(50),
    version INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_rides_driver FOREIGN KEY (driver_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_rides_driver_id ON rides (driver_id);

CREATE TABLE bookings (
    id UUID PRIMARY KEY,
    ride_id UUID NOT NULL,
    passenger_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    requested_seats INT NOT NULL,
    version INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_bookings_ride FOREIGN KEY (ride_id) REFERENCES rides (id) ON DELETE CASCADE,
    CONSTRAINT fk_bookings_passenger FOREIGN KEY (passenger_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_bookings_ride_id ON bookings (ride_id);
CREATE INDEX idx_bookings_passenger_id ON bookings (passenger_id);