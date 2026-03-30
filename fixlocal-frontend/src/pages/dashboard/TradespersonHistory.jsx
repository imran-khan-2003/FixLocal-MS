import { useMemo, useState } from "react";
import DashboardLayout from "../../components/DashboardLayout";
import BookingCard from "../../components/BookingCard";
import { useTradespersonBookings } from "../../hooks/useTradespersonBookings";

const HISTORY_FILTERS = [
  { value: "ALL", label: "All" },
  { value: "COMPLETED", label: "Completed" },
  { value: "CANCELLED", label: "Cancelled" },
  { value: "REJECTED", label: "Rejected" },
];

function TradespersonHistory() {
  const { historyBookings, loading, error } = useTradespersonBookings();
  const [filter, setFilter] = useState("ALL");

  const filtered = useMemo(() => {
    if (filter === "ALL") return historyBookings;
    return historyBookings.filter((booking) => booking.status === filter);
  }, [filter, historyBookings]);

  return (
    <DashboardLayout
      title="Booking History"
      subtitle="Review completed, cancelled or rejected jobs"
    >
      {error && <p className="text-red-500 mb-4">{error}</p>}
      <div className="flex flex-wrap items-center justify-between gap-3 mb-6">
        <p className="text-sm text-slate-500">
          Showing {filtered.length} of {historyBookings.length} past bookings
        </p>
        <select
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
          className="border rounded-lg px-3 py-2 text-sm"
        >
          {HISTORY_FILTERS.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>
      {loading ? (
        <p className="text-slate-600">Loading bookings…</p>
      ) : filtered.length === 0 ? (
        <p className="text-slate-600">No bookings match this filter.</p>
      ) : (
        <div className="grid gap-4">
          {filtered.map((booking) => (
            <BookingCard key={booking.id} booking={booking} showCustomerDetails />
          ))}
        </div>
      )}
    </DashboardLayout>
  );
}

export default TradespersonHistory;