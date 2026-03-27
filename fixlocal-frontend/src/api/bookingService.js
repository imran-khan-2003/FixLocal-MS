import api from "./axios";

export const bookingService = {
  listForUser: (status) =>
    api.get("/bookings/user", { params: status ? { status } : {} }),
  listForTradesperson: (status) =>
    api.get("/bookings/tradesperson", { params: status ? { status } : {} }),
  create: (payload) => api.post("/bookings", payload),
  submitOffer: (bookingId, payload) =>
    api.post(`/bookings/${bookingId}/offers`, payload),
  acceptOffer: (bookingId, offerId) =>
    api.patch(`/bookings/${bookingId}/offers/${offerId}/accept`),
  getById: (bookingId) => api.get(`/bookings/${bookingId}`),
  acceptBooking: (bookingId) => api.patch(`/bookings/${bookingId}/accept`),
  rejectBooking: (bookingId) => api.patch(`/bookings/${bookingId}/reject`),
  startTrip: (bookingId) => api.patch(`/bookings/${bookingId}/start-trip`),
  markArrived: (bookingId) => api.patch(`/bookings/${bookingId}/arrived`),
  complete: (bookingId) => api.patch(`/bookings/${bookingId}/complete`),
  cancel: (bookingId, reason) =>
    api.patch(`/bookings/${bookingId}/cancel`, { reason }),
  getLiveLocation: (bookingId) => api.get(`/bookings/${bookingId}/location`),
  updateLiveLocation: (bookingId, payload) =>
    api.post(`/bookings/${bookingId}/location`, payload),
  payments: {
    initiate: (bookingId, amount) =>
      api.post(`/bookings/${bookingId}/payments/initiate`, null, {
        params: { amount },
      }),
    authorize: (bookingId) => api.post(`/bookings/${bookingId}/payments/authorize`),
    capture: (bookingId) => api.post(`/bookings/${bookingId}/payments/capture`),
    refund: (bookingId) => api.post(`/bookings/${bookingId}/payments/refund`),
  },
};