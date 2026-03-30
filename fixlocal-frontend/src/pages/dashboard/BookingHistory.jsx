import { useMemo, useState } from "react";
import DashboardLayout from "../../components/DashboardLayout";
import BookingCard from "../../components/BookingCard";
import { useBookingsData } from "../../hooks/useBookingsData";

function BookingHistory() {
  const { bookings, loading } = useBookingsData();
  const [filter, setFilter] = useState("COMPLETED");

  const filtered = useMemo(() => {
    return bookings.filter((booking) =>
      filter === "ALL" ? true : booking.status === filter
    );
  }, [bookings, filter]);

  return (
    <DashboardLayout
      title="Booking History"
      subtitle="Review completed, cancelled or rejected jobs"
    >
      <div className="flex justify-between items-center mb-6">
        <p className="text-sm text-slate-500">
          Showing {filtered.length} of {bookings.length} bookings
        </p>
        <select
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
          className="border rounded-lg px-3 py-2 text-sm"
        >
          <option value="ALL">All</option>
          <option value="COMPLETED">Completed</option>
          <option value="CANCELLED">Cancelled</option>
          <option value="REJECTED">Rejected</option>
        </select>
      </div>
      {loading ? (
        <p className="text-slate-600">Loading bookings…</p>
      ) : filtered.length === 0 ? (
        <p className="text-slate-600">No bookings match this filter.</p>
      ) : (
        <div className="grid gap-4">
          {filtered.map((booking) => (
            <BookingCard
              key={booking.id}
              booking={booking}
              showCustomerDetails
            />
          ))}
        </div>
      )}
    </DashboardLayout>
  );
}

export default BookingHistory;