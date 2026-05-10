import { api } from './axios';
import type { Patient, PatientInput } from '../types/api';

export const patientsApi = {
  list: () => api.get<Patient[]>('/patients').then((r) => r.data),
  get: (id: number) => api.get<Patient>(`/patients/${id}`).then((r) => r.data),
  create: (body: PatientInput) => api.post<Patient>('/patients', body).then((r) => r.data),
  update: (id: number, body: PatientInput) =>
    api.put<Patient>(`/patients/${id}`, body).then((r) => r.data),
  remove: (id: number) => api.delete<void>(`/patients/${id}`).then(() => undefined),
};
