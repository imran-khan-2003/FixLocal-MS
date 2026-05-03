const STATUS_STYLES = {
  INITIATED: "bg-amber-100 text-amber-700",
  AUTHORIZED: "bg-indigo-100 text-indigo-700",
  CAPTURED: "bg-emerald-100 text-emerald-700",
  REFUNDED: "bg-slate-200 text-slate-700",
  FAILED: "bg-red-100 text-red-700",
};

function PaymentSummary({
  booking,
  busy = false,
  onInitiate,
  onCapture,
  onRefund,
}) {
  if (!booking) return null;

  const paymentStatus = booking.paymentStatus || "NOT_INITIATED";
  const normalizedStatus = String(paymentStatus).toUpperCase();
  const statusLabel = normalizedStatus === "NOT_INITIATED"
    ? "Not initiated"
    : normalizedStatus.charAt(0) + normalizedStatus.slice(1).toLowerCase();
  const statusClass = STATUS_STYLES[normalizedStatus] || "bg-slate-100 text-slate-700";

  const price = Number(booking.price ?? booking.initialOfferAmount ?? 0);
  const canInitiate = normalizedStatus === "NOT_INITIATED" || normalizedStatus === "FAILED" || normalizedStatus === "REFUNDED";
  const canCapture = normalizedStatus === "AUTHORIZED";
  const canRefund = normalizedStatus === "AUTHORIZED" || normalizedStatus === "CAPTURED";

  return (
    <div className="bg-white rounded-2xl shadow border border-slate-100 p-4 space-y-3">
      <div className="flex items-center justify-between">
        <h3 className="text-lg font-semibold">Payment Status</h3>
        <span className={`px-2 py-1 rounded-full text-xs font-semibold ${statusClass}`}>
          {statusLabel}
        </span>
      </div>
      <p className="text-sm text-slate-500">Amount: ₹{Number.isFinite(price) ? price : 0}</p>
      <p className="text-xs text-slate-400">Intent ID: {booking.paymentIntentId || "-"}</p>
      <div className="flex flex-wrap gap-2 text-sm">
        <button
          className="bg-slate-100 px-3 py-1 rounded disabled:opacity-50"
          onClick={() => onInitiate?.(booking)}
          disabled={busy || !canInitiate || !(price > 0)}
        >
          Initiate
        </button>
        <button
          className="bg-slate-100 px-3 py-1 rounded disabled:opacity-50"
          onClick={() => onCapture?.(booking)}
          disabled={busy || !canCapture}
        >
          Capture
        </button>
        <button
          className="bg-slate-100 px-3 py-1 rounded disabled:opacity-50"
          onClick={() => onRefund?.(booking)}
          disabled={busy || !canRefund}
        >
          Refund
        </button>
      </div>
    </div>
  );
}

export default PaymentSummary;