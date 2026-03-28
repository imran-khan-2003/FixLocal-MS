import { useState } from 'react';
import DisputeForm from './DisputeForm';

function BookingCard({
  booking,
  onView,
  onChat,
  onPrimaryAction,
  primaryLabel,
  secondaryLabel,
  onSecondaryAction,
  onRateStart,
  canRate,
  isRating,
  ratingValue,
  ratingComment,
  onRatingChange,
  onRatingCommentChange,
  onRatingSubmit,
  onRatingCancel,
  ratingSubmitting,
  ratingError,
  showCustomerDetails = false,
  onDispute, 
}) {
  const [showDisputeForm, setShowDisputeForm] = useState(false);

  const handleDisputeSubmit = (disputeData) => {
    if (onDispute) {
      onDispute(disputeData);
    }
    setShowDisputeForm(false);
  };

  const price = booking.price ?? booking.initialOfferAmount;
  const contactDetails = [
    booking.userName && `Customer: ${booking.userName}`,
    booking.userPhone && `Phone: ${booking.userPhone}`,
    booking.userCity && `City: ${booking.userCity}`,
  ].filter(Boolean);
  const timeline = [
    { label: "Accepted", value: booking.acceptedAt },
    { label: "En Route", value: booking.enRouteAt },
    { label: "Arrived", value: booking.arrivedAt },
    { label: "Completed", value: booking.completedAt },
  ];

  const hasRating = typeof booking.userRating === "number";

  return (
    <div className="bg-white shadow rounded-2xl p-4 border border-slate-100 space-y-4">
      <div className="flex justify-between items-center">
        <div>
          <p className="text-xs uppercase text-slate-500">Service</p>
          <h3 className="text-lg font-semibold text-slate-900">
            {booking.serviceDescription}
          </h3>
          <p className="text-xs text-slate-500 mt-1">{booking.serviceAddress}</p>
        </div>
        <span className="text-xs px-3 py-1 rounded-full bg-blue-50 text-blue-600">
          {booking.status?.replace("_", " ")}
        </span>
      </div>
      <p className="text-sm text-slate-600">
        {new Date(booking.bookingStartTime).toLocaleString()} →{" "}
        {new Date(booking.bookingEndTime).toLocaleString()}
      </p>
      <p className="text-sm font-semibold text-slate-700">Price: ₹ {price}</p>
      {showCustomerDetails && contactDetails.length > 0 && (
        <div className="text-xs text-slate-500">
          {contactDetails.map((line) => (
            <p key={line}>{line}</p>
          ))}
        </div>
      )}
      {(hasRating || canRate || isRating) && (
        <div className="flex flex-wrap items-center gap-2 text-sm">
          {hasRating && (
            <div className="flex items-center gap-2 text-yellow-600">
              <div className="flex">
                {[1, 2, 3, 4, 5].map((star) => (
                  <span key={star}>{star <= booking.userRating ? "★" : "☆"}</span>
                ))}
              </div>
              <span className="font-semibold">{booking.userRating}/5</span>
              {booking.reviewedAt && (
                <span className="text-xs text-slate-500">
                  {new Date(booking.reviewedAt).toLocaleDateString()}
                </span>
              )}
            </div>
          )}
          {!isRating && canRate && (
            <button
              type="button"
              className="text-xs uppercase tracking-wide text-blue-600 font-semibold"
              onClick={() => onRateStart && onRateStart(booking)}
            >
              Rate experience
            </button>
          )}
        </div>
      )}
      {isRating && (
        <div className="border border-amber-100 bg-amber-50 rounded-xl p-4 space-y-3">
          <div className="flex items-center gap-3">
            <div className="flex gap-1 text-2xl text-amber-500">
              {[1, 2, 3, 4, 5].map((star) => (
                <button
                  key={star}
                  type="button"
                  className={`focus:outline-none ${star <= ratingValue ? "" : "text-slate-300"}`}
                  onClick={() => onRatingChange && onRatingChange(booking, star)}
                  aria-label={`Set rating to ${star}`}
                >
                  {star <= ratingValue ? "★" : "☆"}
                </button>
              ))}
            </div>
            <span className="text-sm font-semibold text-slate-700">{ratingValue}/5</span>
          </div>
          <textarea
            className="w-full border border-slate-200 rounded-lg p-3 text-sm focus:outline-none focus:ring-2 focus:ring-amber-200"
            rows={3}
            placeholder="Tell others about your experience"
            value={ratingComment}
            onChange={(e) => onRatingCommentChange && onRatingCommentChange(booking, e.target.value)}
          />
          {ratingError && <p className="text-sm text-red-600">{ratingError}</p>}
          <div className="flex gap-3">
            <button
              type="button"
              className="px-4 py-2 text-sm rounded-lg border border-slate-200"
              onClick={() => onRatingCancel && onRatingCancel(booking)}
              disabled={ratingSubmitting}
            >
              Cancel
            </button>
            <button
              type="button"
              className="px-4 py-2 text-sm rounded-lg bg-amber-500 text-white disabled:opacity-70"
              onClick={() => onRatingSubmit && onRatingSubmit(booking)}
              disabled={ratingSubmitting}
            >
              {ratingSubmitting ? "Submitting..." : "Submit Review"}
            </button>
          </div>
        </div>
      )}
      <div className="flex flex-wrap gap-6 text-xs text-slate-500">
        {timeline.map(
          (entry) =>
            entry.value && (
              <div key={entry.label}>
                <p className="uppercase tracking-wide">{entry.label}</p>
                <p className="text-slate-700 font-medium">
                  {new Date(entry.value).toLocaleString()}
                </p>
              </div>
            )
        )}
      </div>
      <div className="mt-2 flex flex-wrap gap-2">
        {onView && (
          <button className="text-blue-600 text-sm" onClick={() => onView(booking)}>
            View Details
          </button>
        )}
        {onChat && (
          <button className="text-blue-600 text-sm" onClick={() => onChat(booking)}>
            Chat
          </button>
        )}
        <button onClick={() => setShowDisputeForm(true)} className="text-red-600 text-sm">Report an Issue</button>
        {onSecondaryAction && secondaryLabel && (
          <button
            className="border border-slate-200 text-sm px-3 py-1 rounded"
            onClick={() => onSecondaryAction(booking)}
          >
            {secondaryLabel}
          </button>
        )}
        {onPrimaryAction && primaryLabel && (
          <button
            className="bg-blue-600 text-white text-sm px-3 py-1 rounded"
            onClick={() => onPrimaryAction(booking)}
          >
            {primaryLabel}
          </button>
        )}
        {!isRating && canRate && (
          <button
            type="button"
            className="bg-amber-500 text-white text-sm px-3 py-1 rounded"
            onClick={() => onRateStart && onRateStart(booking)}
          >
            Rate Now
          </button>
        )}
      </div>
      {showDisputeForm && (
        <DisputeForm
          bookingId={booking.id}
          onSubmit={handleDisputeSubmit}
          onCancel={() => setShowDisputeForm(false)}
        />
      )}
    </div>
  );
}

export default BookingCard;
