function PaymentSummary({ booking, onInitiate, onAuthorize, onCapture, onRefund }) {
  if (!booking) return null;
  return (
    <div className="bg-white rounded-2xl shadow border border-slate-100 p-4 space-y-2">
      <h3 className="text-lg font-semibold">Payment Status</h3>
      <p className="text-sm text-slate-500">Status: {booking.paymentStatus || "Not initiated"}</p>
      <p className="text-xs text-slate-400">Intent ID: {booking.paymentIntentId || "-"}</p>
      <div className="flex flex-wrap gap-2 text-sm">
        <button className="bg-slate-100 px-3 py-1 rounded" onClick={() => onInitiate?.(booking)}>
          Initiate
        </button>
        <button className="bg-slate-100 px-3 py-1 rounded" onClick={() => onAuthorize?.(booking)}>
          Authorize
        </button>
        <button className="bg-slate-100 px-3 py-1 rounded" onClick={() => onCapture?.(booking)}>
          Capture
        </button>
        <button className="bg-slate-100 px-3 py-1 rounded" onClick={() => onRefund?.(booking)}>
          Refund
        </button>
      </div>
    </div>
  );
}

export default PaymentSummary;