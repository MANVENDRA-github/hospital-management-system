-- Creates one database per microservice on first boot of the postgres container.
-- The owning user is created by the postgres image from POSTGRES_USER.

CREATE DATABASE auth_db;
CREATE DATABASE patient_db;
CREATE DATABASE doctor_db;
CREATE DATABASE appointment_db;
CREATE DATABASE billing_db;
CREATE DATABASE lab_db;
