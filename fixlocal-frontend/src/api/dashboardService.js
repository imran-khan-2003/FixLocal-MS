import api from "./axios";

const ACTIVE_STATUSES = new Set(["ACCEPTED", "EN_ROUTE", "ARRIVED"]);

const getItems = (data) => data?.content || data || [];

const summarizeBookings = (bookings = []) => {
  const totalBookings = bookings.length;
  const pendingRequests = bookings.filter((booking) => booking?.status === "PENDING").length;
  const activeBookings = bookings.filter((booking) => ACTIVE_STATUSES.has(booking?.status)).length;
  const completedBookings = bookings.filter((booking) => booking?.status === "COMPLETED").length;

  return {
    totalBookings,
    pendingRequests,
    activeBookings,
    completedBookings,
  };
};

async function getMyProfile() {
  const { data } = await api.get("/users/me");
  return data;
}

export const dashboardService = {
  // Microservices-compatible replacement for old monolith endpoint: /dashboard/user
  async getUserDashboard() {
    const { data } = await api.get("/bookings/user");
    const bookings = getItems(data);
    const summary = summarizeBookings(bookings);

    return {
      data: {
        upcomingBookings: summary.pendingRequests,
        activeBookings: summary.activeBookings,
        completedBookings: summary.completedBookings,
        totalBookings: summary.totalBookings,
      },
    };
  },

  // Microservices-compatible replacement for old monolith endpoint: /dashboard/tradesperson
  async getTradespersonDashboard() {
    const profile = await getMyProfile();
    const [{ data: bookingsData }, { data: reviewsData }] = await Promise.all([
      api.get("/bookings/tradesperson"),
      api.get(`/reviews/tradesperson/${profile?.id}`),
    ]);

    const bookings = getItems(bookingsData);
    const reviews = Array.isArray(reviewsData) ? reviewsData : [];
    const summary = summarizeBookings(bookings);

    const totalReviews = reviews.length;
    const averageRating =
      totalReviews > 0
        ? reviews.reduce((acc, review) => acc + Number(review?.rating || 0), 0) / totalReviews
        : 0;

    return {
      data: {
        pendingRequests: summary.pendingRequests,
        activeBookings: summary.activeBookings,
        completedBookings: summary.completedBookings,
        totalBookings: summary.totalBookings,
        averageRating,
        totalReviews,
        profile,
      },
    };
  },
};