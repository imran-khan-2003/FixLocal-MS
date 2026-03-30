import { useCallback, useEffect, useMemo, useState } from "react";
import { bookingService } from "../api/bookingService";

const ACTIVE_STATUSES = ["PENDING", "ACCEPTED", "EN_ROUTE", "ARRIVED"];
const CURRENT_STATUSES = ["ACCEPTED", "EN_ROUTE", "ARRIVED"];

export function useTradespersonBookings() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [actionNotice, setActionNotice] = useState("");

  const refresh = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const { data } = await bookingService.listForTradesperson();
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

  const currentBooking = useMemo(
    () => bookings.find((booking) => CURRENT_STATUSES.includes(booking.status)) || null,
    [bookings]
  );

  const activeBookings = useMemo(
    () => bookings.filter((booking) => ACTIVE_STATUSES.includes(booking.status)),
    [bookings]
  );

  const historyBookings = useMemo(
    () => bookings.filter((booking) => !ACTIVE_STATUSES.includes(booking.status)),
    [bookings]
  );

  return {
    bookings,
    activeBookings,
    historyBookings,
    currentBooking,
    loading,
    error,
    actionNotice,
    setActionNotice,
    refresh,
  };
}