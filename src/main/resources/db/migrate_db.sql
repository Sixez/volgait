CREATE EXTENSION IF NOT EXISTS cube;
CREATE EXTENSION IF NOT EXISTS earthdistance;

CREATE TABLE vit_accounts (
    id bigserial PRIMARY KEY,
    username varchar(255) NOT NULL,
    password varchar(255) NOT NULL,
    admin boolean NOT NULL DEFAULT false,
    balance double precision NOT NULL DEFAULT 0,

    CONSTRAINT username_unique UNIQUE (username),
    CONSTRAINT balance_check CHECK (balance >= 0)
);

CREATE TABLE vit_transport (
    id bigserial PRIMARY KEY,
    owner_id bigint NOT NULL,
    description varchar(255),
    transport_type varchar(255) NOT NULL,
    model varchar(255) NOT NULL,
    identifier varchar(12) NOT NULL,
    color varchar(64) NOT NULL,
    can_be_rented boolean NOT NULL,
    latitude double precision NOT NULL,
    longitude double precision NOT NULL,
    minute_price double precision,
    day_price double precision,

    CONSTRAINT identifier_unique UNIQUE (identifier),
    CONSTRAINT fk_transport_owner FOREIGN KEY(owner_id) REFERENCES vit_accounts(id) ON DELETE CASCADE,
    CONSTRAINT transport_type_check CHECK (transport_type in ('Car', 'Bike', 'Scooter'))
);

CREATE TABLE vit_rents (
    id bigserial PRIMARY KEY,
    transport_id bigint NOT NULL,
    user_id bigint NOT NULL,
    time_start timestamp NOT NULL,
    time_end timestamp,
    price_of_unit double precision NOT NULL,
    price_type varchar(16) NOT NULL,
    final_price double precision,

    CONSTRAINT fk_rents_user FOREIGN KEY(user_id) REFERENCES vit_accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_rents_transport FOREIGN KEY(transport_id) REFERENCES vit_transport(id) ON DELETE CASCADE,
    CONSTRAINT price_type_check CHECK (price_type in ('Minutes', 'Days'))
);