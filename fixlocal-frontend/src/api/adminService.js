import api from "./axios";

const withPage = (page = 0, size = 10) => ({ params: { page, size } });

export const adminService = {
  getUsers: (page = 0, size = 10) => api.get("/admin/users", withPage(page, size)),
  getTradespersons: (page = 0, size = 10) =>
    api.get("/admin/tradespersons", withPage(page, size)),
  blockUser: (userId) => api.patch(`/admin/users/${userId}/block`),
  unblockUser: (userId) => api.patch(`/admin/users/${userId}/unblock`),
};
