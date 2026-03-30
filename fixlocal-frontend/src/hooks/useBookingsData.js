import { useCallback, useEffect, useState } from "react";
import { bookingService } from "../api/bookingService";

export function useBookingsData() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [actionNotice, setActionNotice] = useState("");

  const refresh = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const { data } = await bookingService.listForUser();
      setBookings(data?.content || data || []);
    } catch (err) {
      setError("Failed to load bookings");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

  return {
    bookings,
    loading,
    error,
    refresh,
    actionNotice,
    setActionNotice,
  };
}