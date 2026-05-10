# Entity-relationship diagram

Each service owns its own database. Cross-service references are by ID only (no FK constraints across services — relationships are logical, enforced at the application layer).

```mermaid
erDiagram
    USERS {
        bigint id PK
        varchar email UK
        varchar password
        varchar role "ADMIN | DOCTOR | PATIENT"
    }

    PATIENTS {
        bigint id PK
        bigint user_id "logical FK to users.id"
        varchar name
        date date_of_birth
        varchar gender
        varchar phone
        varchar address
    }

    DOCTORS {
        bigint id PK
        bigint user_id "logical FK to users.id"
        varchar name
        varchar specialization
        varchar phone
        varchar email
    }

    APPOINTMENTS {
        bigint id PK
        bigint patient_id "logical FK to patients.id"
        bigint doctor_id "logical FK to doctors.id"
        timestamp appointment_date
        varchar reason
        varchar status "SCHEDULED | CANCELLED | COMPLETED"
    }

    BILLS {
        bigint id PK
        bigint patient_id "logical FK to patients.id"
        bigint appointment_id "logical FK to appointments.id"
        decimal amount
        varchar description
        varchar payment_status "PENDING | PAID"
        timestamp created_at
    }

    LAB_TESTS {
        bigint id PK
        bigint patient_id "logical FK to patients.id"
        bigint doctor_id "logical FK to doctors.id"
        varchar test_name
        timestamp test_date
        text result
        varchar status "PENDING | COMPLETED"
    }

    USERS ||--o| PATIENTS : "owns"
    USERS ||--o| DOCTORS : "owns"
    PATIENTS ||--o{ APPOINTMENTS : "books"
    DOCTORS ||--o{ APPOINTMENTS : "attends"
    PATIENTS ||--o{ BILLS : "billed"
    APPOINTMENTS ||--o| BILLS : "auto-generates"
    PATIENTS ||--o{ LAB_TESTS : "undergoes"
    DOCTORS ||--o{ LAB_TESTS : "orders"
```
