import api from "./axios";

const reviewService = {
  addReview: (bookingId, payload) => api.post(`/reviews/${bookingId}`, payload),
  getTradespersonReviews: (tradespersonId) =>
    api.get(`/reviews/tradesperson/${tradespersonId}`),
};

export default reviewService;