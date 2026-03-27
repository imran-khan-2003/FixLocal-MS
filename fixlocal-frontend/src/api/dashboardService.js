import api from "./axios";

export const dashboardService = {
  getUserDashboard: () => api.get("/dashboard/user"),
  getTradespersonDashboard: () => api.get("/dashboard/tradesperson"),
};