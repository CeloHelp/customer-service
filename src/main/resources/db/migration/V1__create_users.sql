-- V1__create_customers.sql
-- Criação inicial da tabela de clientes

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE customers (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           name VARCHAR(255) NOT NULL,
                           cpf VARCHAR(14) UNIQUE NOT NULL,
                           email VARCHAR(255) NOT NULL,
                           address VARCHAR(255)
);
