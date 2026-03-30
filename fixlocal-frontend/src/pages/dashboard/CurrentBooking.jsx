import { useMemo, useState } from "react";
import DashboardLayout from "../../components/DashboardLayout";
import LiveLocationMap from "../../components/LiveLocationMap";
import PaymentSummary from "../../components/PaymentSummary";
import BookingCard from "../../components/BookingCard";
import { bookingService } from "../../api/bookingService";
import { useCurrentBooking } from "../../hooks/useCurrentBooking";

function CurrentBooking() {
  const {
    bookings,
    loading,
    refresh,
    actionNotice,
    setActionNotice,
    activeBooking,
    enRouteBooking,
    liveLocationState,
  } = useCurrentBooking();
  const [selectedBookingId, setSelectedBookingId] = useState(null);

  const handleCancel = async (booking, reason) => {
    await bookingService.cancel(booking.id, reason);
    setActionNotice("Booking cancelled");
    refresh();
  };

  return (
    <DashboardLayout title="Current Booking" subtitle="Track payments, chats and live map">
      {actionNotice && <p className="text-sm text-blue-600 mb-4">{actionNotice}</p>}
      {loading && <p className="text-slate-600">Loading…</p>}
      {!loading && !activeBooking && (
        <p className="text-slate-600">No active bookings. Check history or create a new request.</p>
      )}
      {activeBooking && (
        <div className="grid lg:grid-cols-2 gap-6">
          <div className="space-y-4">
            <BookingCard
              booking={activeBooking}
              onPrimaryAction={() => handleCancel(activeBooking, "Cancelled from current view")}
              primaryLabel="Cancel booking"
              onView={() => setSelectedBookingId(activeBooking.id)}
            />
            <PaymentSummary booking={activeBooking} />
          </div>
          <div>
            {enRouteBooking ? (
              <LiveLocationMap
                location={liveLocationState.location}
                stale={liveLocationState.stale}
                userAddress={enRouteBooking.serviceAddress}
                destination={{
                  latitude: enRouteBooking.userLatitude,
                  longitude: enRouteBooking.userLongitude,
                  label: enRouteBooking.userCity,
                }}
              />
            ) : (
              <p className="text-slate-500 text-sm">Live location available once the trip starts.</p>
            )}
          </div>
        </div>
      )}
    </DashboardLayout>
  );
}

export default CurrentBooking;