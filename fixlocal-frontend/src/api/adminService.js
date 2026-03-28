
import axios from './axios';

export const getAdminStats = () => {
  return axios.get("/api/admin/stats");
};

export const getAllDisputes = () => {
  return axios.get('/api/disputes');
};

export const updateDispute = (id, data) => {
  return axios.put(`/api/disputes/${id}`, data);
};
