import { api } from './axios';
import type { Doctor, DoctorInput } from '../types/api';

export const doctorsApi = {
  list: () => api.get<Doctor[]>('/doctors').then((r) => r.data),
  get: (id: number) => api.get<Doctor>(`/doctors/${id}`).then((r) => r.data),
  create: (body: DoctorInput) => api.post<Doctor>('/doctors', body).then((r) => r.data),
  update: (id: number, body: DoctorInput) =>
    api.put<Doctor>(`/doctors/${id}`, body).then((r) => r.data),
  remove: (id: number) => api.delete<void>(`/doctors/${id}`).then(() => undefined),
  bySpecialization: (specialization: string) =>
    api
      .get<Doctor[]>(`/doctors/specialization/${encodeURIComponent(specialization)}`)
      .then((r) => r.data),
};
