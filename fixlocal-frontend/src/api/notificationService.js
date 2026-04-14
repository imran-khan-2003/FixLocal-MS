import api from "./axios";

export const notificationService = {
  list: (page = 0, size = 10) =>
    api.get("/notifications", {
      params: { page, size, sort: "createdAt,desc" },
    }),

  markAsRead: (id) => api.put(`/notifications/${id}/read`),

  markAllAsRead: () => api.put("/notifications/read-all"),
};
