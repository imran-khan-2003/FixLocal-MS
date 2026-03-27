import api from "./axios";

export const chatService = {
  getConversation: (bookingId) => api.get(`/chat/conversations/${bookingId}`),
  getMessages: (conversationId, params = {}) =>
    api.get(`/chat/conversations/${conversationId}/messages`, {
      params: { page: 0, size: 50, ...params },
    }),
  sendMessage: (bookingId, { content, attachment }) => {
    const formData = new FormData();
    formData.append("content", content);
    if (attachment) {
      formData.append("attachment", attachment);
    }

    return api.post(`/chat/bookings/${bookingId}/messages`, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
  },
};

export default chatService;