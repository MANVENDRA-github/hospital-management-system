import { api } from './axios';
import type { Bill, BillInput } from '../types/api';

export const billingApi = {
  get: (id: number) => api.get<Bill>(`/billing/${id}`).then((r) => r.data),
  create: (body: BillInput) => api.post<Bill>('/billing', body).then((r) => r.data),
  markPaid: (id: number) => api.put<Bill>(`/billing/${id}/pay`).then((r) => r.data),
  byPatient: (patientId: number) =>
    api.get<Bill[]>(`/billing/patient/${patientId}`).then((r) => r.data),
};
