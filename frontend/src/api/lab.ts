import { api } from './axios';
import type { LabTest, LabTestInput } from '../types/api';

export const labApi = {
  list: () => api.get<LabTest[]>('/lab').then((r) => r.data),
  get: (id: number) => api.get<LabTest>(`/lab/${id}`).then((r) => r.data),
  bookTest: (body: LabTestInput) => api.post<LabTest>('/lab', body).then((r) => r.data),
  uploadResult: (id: number, result: string) =>
    api.put<LabTest>(`/lab/${id}/result`, { result }).then((r) => r.data),
  byPatient: (patientId: number) =>
    api.get<LabTest[]>(`/lab/patient/${patientId}`).then((r) => r.data),
};
