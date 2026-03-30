import { useMemo } from "react";
import { useBookingsData } from "./useBookingsData";
import { useLiveLocation } from "./useLiveLocation";

export function useCurrentBooking() {
  const bookingsData = useBookingsData();

  const activeBooking = useMemo(
    () =>
      bookingsData.bookings.find((booking) =>
        ["EN_ROUTE", "ARRIVED", "ACCEPTED", "PENDING"].includes(booking.status)
      ) || null,
    [bookingsData.bookings]
  );

  const enRouteBooking = useMemo(
    () =>
      bookingsData.bookings.find((booking) =>
        ["EN_ROUTE", "ARRIVED"].includes(booking.status)
      ) || null,
    [bookingsData.bookings]
  );

  const liveLocationState = useLiveLocation(enRouteBooking?.id, {
    enabled: Boolean(enRouteBooking),
  });

  return {
    ...bookingsData,
    activeBooking,
    enRouteBooking,
    liveLocationState,
  };
}