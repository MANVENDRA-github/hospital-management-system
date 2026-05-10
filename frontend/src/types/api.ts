export type Role = 'ADMIN' | 'DOCTOR' | 'PATIENT';

export interface AuthResponse {
  token: string;
  email: string;
  role: Role;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  role: Role;
}

export interface Patient {
  id: number;
  userId?: number | null;
  name: string;
  dateOfBirth?: string | null;
  gender?: string | null;
  phone?: string | null;
  address?: string | null;
}

export type PatientInput = Omit<Patient, 'id'>;

export interface Doctor {
  id: number;
  userId?: number | null;
  name: string;
  specialization: string;
  phone?: string | null;
  email?: string | null;
}

export type DoctorInput = Omit<Doctor, 'id'>;

export type AppointmentStatus = 'SCHEDULED' | 'CANCELLED' | 'COMPLETED';

export interface Appointment {
  id: number;
  patientId: number;
  doctorId: number;
  appointmentDate: string;
  reason?: string | null;
  status: AppointmentStatus;
}

export interface AppointmentInput {
  patientId: number;
  doctorId: number;
  appointmentDate: string;
  reason?: string | null;
}

export type PaymentStatus = 'PENDING' | 'PAID';

export interface Bill {
  id: number;
  patientId: number;
  appointmentId?: number | null;
  amount: number;
  description?: string | null;
  paymentStatus: PaymentStatus;
  createdAt: string;
}

export interface BillInput {
  patientId: number;
  appointmentId?: number | null;
  amount: number;
  description?: string | null;
}

export type LabTestStatus = 'PENDING' | 'COMPLETED';

export interface LabTest {
  id: number;
  patientId: number;
  doctorId: number;
  testName: string;
  testDate: string;
  result?: string | null;
  status: LabTestStatus;
}

export interface LabTestInput {
  patientId: number;
  doctorId: number;
  testName: string;
  testDate: string;
}
