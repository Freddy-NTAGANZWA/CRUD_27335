-- Service Appointment System - Database Schema
-- This file contains manual SQL queries for creating all tables

-- 1. ROLES TABLE
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- 2. LOCATIONS TABLE (Hierarchical - 5 Levels)
CREATE TABLE IF NOT EXISTS locations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    code VARCHAR(255) UNIQUE,
    parent_id BIGINT REFERENCES locations(id)
);

COMMENT ON TABLE locations IS 'Hierarchical location structure: Province → District → Sector → Cell → Village';

-- 3. USERS TABLE
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role_id BIGINT REFERENCES roles(id),
    village_id BIGINT REFERENCES locations(id)
);

-- 4. USER_PROFILES TABLE (One-to-One)
CREATE TABLE IF NOT EXISTS user_profiles (
    id BIGSERIAL PRIMARY KEY,
    phone VARCHAR(255),
    date_of_birth DATE,
    gender VARCHAR(50),
    user_id BIGINT UNIQUE REFERENCES users(id)
);

-- 5. CATEGORIES TABLE
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT
);

-- 6. SERVICES TABLE
CREATE TABLE IF NOT EXISTS services (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DOUBLE PRECISION
);

-- 7. SERVICE_CATEGORIES TABLE (Many-to-Many Join Table)
CREATE TABLE IF NOT EXISTS service_categories (
    service_id BIGINT REFERENCES services(id),
    category_id BIGINT REFERENCES categories(id),
    PRIMARY KEY (service_id, category_id)
);

-- 8. APPOINTMENTS TABLE
CREATE TABLE IF NOT EXISTS appointments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    specialist_id BIGINT REFERENCES users(id),
    service_id BIGINT REFERENCES services(id),
    appointment_time TIMESTAMP NOT NULL,
    notes TEXT,
    status VARCHAR(50)
);

-- Indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_locations_code ON locations(code);
CREATE INDEX IF NOT EXISTS idx_locations_parent ON locations(parent_id);
CREATE INDEX IF NOT EXISTS idx_appointments_user ON appointments(user_id);
CREATE INDEX IF NOT EXISTS idx_appointments_service ON appointments(service_id);
