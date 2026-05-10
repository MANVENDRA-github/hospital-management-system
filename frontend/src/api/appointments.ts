import { api } from './axios';
import type { Appointment, AppointmentInput } from '../types/api';

export const appointmentsApi = {
  list: () => api.get<Appointment[]>('/appointments').then((r) => r.data),
  get: (id: number) => api.get<Appointment>(`/appointments/${id}`).then((r) => r.data),
  book: (body: AppointmentInput) =>
    api.post<Appointment>('/appointments', body).then((r) => r.data),
  cancel: (id: number) =>
    api.put<Appointment>(`/appointments/${id}/cancel`).then((r) => r.data),
  complete: (id: number) =>
    api.put<Appointment>(`/appointments/${id}/complete`).then((r) => r.data),
  byPatient: (patientId: number) =>
    api.get<Appointment[]>(`/appointments/patient/${patientId}`).then((r) => r.data),
  byDoctor: (doctorId: number) =>
    api.get<Appointment[]>(`/appointments/doctor/${doctorId}`).then((r) => r.data),
};
