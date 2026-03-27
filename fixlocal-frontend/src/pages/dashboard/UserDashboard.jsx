import { useCallback, useEffect, useMemo, useState } from "react";
import { bookingService } from "../../api/bookingService";
import reviewService from "../../api/reviewService";
import { dashboardService } from "../../api/dashboardService";
import DashboardLayout from "../../components/DashboardLayout";
import BookingCard from "../../components/BookingCard";
import ConversationPanel from "../../components/ConversationPanel";
import PaymentSummary from "../../components/PaymentSummary";
import ChatThread from "../../components/ChatThread";
import chatService from "../../api/chatService";
import { useAuth } from "../../context/AuthContext";
import LiveLocationMap from "../../components/LiveLocationMap";
import { useLiveLocation } from "../../hooks/useLiveLocation";

function BookingDetailModal({ booking, onClose }) {
  if (!booking) return null;
  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
      <div className="bg-white rounded-2xl p-6 w-full max-w-lg shadow-xl">
        <h2 className="text-2xl font-bold mb-4">Booking Details</h2>
        <p className="text-sm text-slate-500 mb-2">Service</p>
        <p className="text-lg font-semibold mb-4">{booking.serviceDescription}</p>
        <p className="text-sm text-slate-500 mb-2">Address</p>
        <p className="text-slate-700 mb-4">{booking.serviceAddress}</p>
        <button className="mt-4 bg-slate-800 text-white px-4 py-2 rounded" onClick={onClose}>
          Close
        </button>
      </div>
    </div>
  );
}

function RatingModal({
  booking,
  ratingValue,
  onRatingChange,
  comment,
  onCommentChange,
  onClose,
  onSubmit,
  submitting,
  error,
}) {
  if (!booking) return null;

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
      <div className="bg-white rounded-2xl p-6 w-full max-w-md shadow-xl space-y-4">
        <div>
          <p className="text-xs uppercase text-slate-500">Rate booking</p>
          <h3 className="text-xl font-semibold text-slate-900">
            {booking.serviceDescription}
          </h3>
          <p className="text-sm text-slate-500">with {booking.tradespersonName || booking.tradesperson?.name || "tradesperson"}</p>
        </div>
        <div>
          <p className="text-sm font-semibold text-slate-700 mb-2">Your rating</p>
          <div className="flex gap-2 items-center">
            <div className="flex gap-1 text-2xl text-amber-400">
              {[1, 2, 3, 4, 5].map((star) => (
                <button
                  key={star}
                  type="button"
                  className={`focus:outline-none ${star <= ratingValue ? "" : "text-slate-300"}`}
                  onClick={() => onRatingChange(star)}
                  aria-label={`Set rating to ${star}`}
                >
                  {star <= ratingValue ? "★" : "☆"}
                </button>
              ))}
            </div>
            <span className="text-sm text-slate-600 font-semibold">{ratingValue}/5</span>
          </div>
        </div>
        <div>
          <label className="text-sm font-semibold text-slate-700" htmlFor="rating-comment">
            Comment
          </label>
          <textarea
            id="rating-comment"
            className="mt-2 w-full border border-slate-200 rounded-lg p-3 text-sm focus:outline-none focus:ring-2 focus:ring-slate-400"
            rows={4}
            value={comment}
            onChange={(e) => onCommentChange(e.target.value)}
            placeholder="Share details about your experience"
          />
        </div>
        {error && <p className="text-sm text-red-600">{error}</p>}
        <div className="flex justify-end gap-3">
          <button
            className="px-4 py-2 text-sm rounded-lg border border-slate-200"
            onClick={onClose}
            disabled={submitting}
          >
            Cancel
          </button>
          <button
            className="px-4 py-2 text-sm rounded-lg bg-amber-500 text-white disabled:opacity-70"
            onClick={onSubmit}
            disabled={submitting}
          >
            {submitting ? "Submitting..." : "Submit Review"}
          </button>
        </div>
      </div>
    </div>
  );
}

const ACTION_LABELS = {
  PENDING: "Cancel Request",
  ACCEPTED: "Cancel Booking",
  EN_ROUTE: "Cancel Booking",
  ARRIVED: "Cancel Booking",
};

function primaryActionLabel(status) {
  return ACTION_LABELS[status] || "";
}

const ACTIVE_BOOKING_STATUSES = new Set(["PENDING", "ACCEPTED", "EN_ROUTE", "ARRIVED"]);

function buildConversationFromBooking(booking, currentUserId) {
  if (!booking) return null;
  const participantName = booking.tradespersonName || booking.tradesperson?.name;
  return {
    id: booking.conversationId || booking.id,
    bookingId: booking.id,
    tradespersonName: participantName || "Tradesperson",
    lastMessage: booking.lastMessage?.content || booking.serviceDescription,
    lastMessageAt: booking.lastMessage?.createdAt,
    unreadCount: booking.unreadCount,
    participantId: booking.tradespersonId,
    mine: booking.tradespersonId === currentUserId,
  };
}

function UserDashboard() {
  const { user } = useAuth();
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [selectedBookingId, setSelectedBookingId] = useState(null);
  const [activeConversation, setActiveConversation] = useState(null);
  const [messages, setMessages] = useState([]);
  const [chatLoading, setChatLoading] = useState(false);
  const [chatError, setChatError] = useState("");
  const [chatVisible, setChatVisible] = useState(false);
  const [actionNotice, setActionNotice] = useState("");
  const [ratingBooking, setRatingBooking] = useState(null);
  const [ratingValue, setRatingValue] = useState(5);
  const [ratingComment, setRatingComment] = useState("");
  const [ratingError, setRatingError] = useState("");
  const [ratingSubmitting, setRatingSubmitting] = useState(false);

  const [inlineRatingBookingId, setInlineRatingBookingId] = useState(null);
  const [inlineRatingValues, setInlineRatingValues] = useState({});
  const [inlineRatingComments, setInlineRatingComments] = useState({});
  const [inlineRatingErrors, setInlineRatingErrors] = useState({});
  const [inlineRatingSubmitting, setInlineRatingSubmitting] = useState(false);

  const [summary, setSummary] = useState(null);
  const [summaryLoading, setSummaryLoading] = useState(false);
  const [summaryError, setSummaryError] = useState("");

  const fetchConversationMessages = useCallback(
    async (bookingId, seedConversation) => {
      if (!bookingId) return;
      setChatLoading(true);
      setChatError("");
      try {
        const conversationRes = await chatService.getConversation(bookingId);
        const conversation = conversationRes.data;
        setActiveConversation((prev) => ({
          ...(seedConversation || prev || {}),
          id: conversation.id,
          bookingId,
          tradespersonName:
            seedConversation?.tradespersonName ||
            prev?.tradespersonName ||
            conversation.tradespersonName ||
            "Tradesperson",
          lastMessageAt: conversation.lastMessage?.createdAt || prev?.lastMessageAt,
        }));
        const { data } = await chatService.getMessages(conversation.id);
        const items = data?.content || [];
        setMessages(
          items
            .slice()
            .reverse()
            .map((msg) => ({
              id: msg.id,
              content: msg.content,
              createdAt: msg.createdAt,
              mine: msg.senderId === user?.id,
              attachment: msg.attachment,
            }))
        );
      } catch (err) {
        setChatError("Failed to load chat");
      } finally {
        setChatLoading(false);
      }
    },
    [user?.id]
  );

  const fetchBookings = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const { data } = await bookingService.listForUser();
      const normalized = data?.content || data || [];
      setBookings(normalized);
    } catch (err) {
      setError("Failed to load bookings");
    } finally {
      setLoading(false);
    }
  }, [user?.id]);

  useEffect(() => {
    fetchBookings();
  }, [fetchBookings]);

  useEffect(() => {
    if (!user) return;
    let active = true;
    setSummaryLoading(true);
    setSummaryError("");
    dashboardService
      .getUserDashboard()
      .then(({ data }) => {
        if (active) setSummary(data);
      })
      .catch(() => {
        if (active) setSummaryError("Failed to load booking summary");
      })
      .finally(() => {
        if (active) setSummaryLoading(false);
      });
    return () => {
      active = false;
    };
  }, [user?.id]);

  const activeBookings = useMemo(
    () => bookings.filter((booking) => ACTIVE_BOOKING_STATUSES.has(booking.status)),
    [bookings]
  );

  const conversations = useMemo(
    () => activeBookings.map((booking) => buildConversationFromBooking(booking, user?.id)),
    [activeBookings, user?.id]
  );

  const selectedBooking = useMemo(
    () => bookings.find((booking) => booking.id === selectedBookingId) || null,
    [bookings, selectedBookingId]
  );

  const paymentBooking = useMemo(() => {
    if (selectedBooking) return selectedBooking;
    if (activeConversation) {
      const activeBooking = bookings.find(
        (booking) => booking.id === activeConversation.bookingId
      );
      if (activeBooking) return activeBooking;
    }
    return bookings[0] || null;
  }, [activeConversation, bookings, selectedBooking]);

  const enRouteBooking = useMemo(
    () =>
      bookings.find((booking) =>
        ["EN_ROUTE", "ARRIVED"].includes(booking.status)
      ) || null,
    [bookings]
  );

  const {
    location: liveLocation,
    loading: liveLocationLoading,
    error: liveLocationError,
    stale: liveLocationStale,
  } = useLiveLocation(enRouteBooking?.id, { enabled: Boolean(enRouteBooking) });

  const handleConversationSelect = useCallback(
    (conversation, { toggleOnly } = {}) => {
      if (!conversation) {
        setChatVisible(false);
        setActiveConversation(null);
        setMessages([]);
        return;
      }

      const isSameConversation =
        activeConversation &&
        (activeConversation.id === conversation.id ||
          activeConversation.bookingId === conversation.bookingId);

      if (toggleOnly && isSameConversation) {
        setChatVisible((prev) => !prev);
        return;
      }

      setActiveConversation(conversation);
      setChatError("");
      setMessages([]);
      setChatVisible(true);
      fetchConversationMessages(conversation.bookingId, conversation);
    },
    [activeConversation, fetchConversationMessages]
  );

  const handleSendMessage = useCallback(
    async (content, attachment) => {
      if (!activeConversation) return;
      try {
        await chatService.sendMessage(activeConversation.bookingId, { content, attachment });
        fetchConversationMessages(activeConversation.bookingId, activeConversation);
      } catch (err) {
        setChatError("Failed to send message");
      }
    },
    [activeConversation, fetchConversationMessages]
  );

  const handlePrimaryAction = useCallback(
    async (booking) => {
      if (!booking) return;
      const label = primaryActionLabel(booking.status);
      if (!label) return;
      const confirmation = window.confirm(
        `Are you sure you want to ${label.toLowerCase()} for ${booking.serviceDescription}?`
      );
      if (!confirmation) return;
      const reason = window.prompt("Reason for cancellation?", "Changed plans") ||
        "Cancelled via dashboard";
      try {
        await bookingService.cancel(booking.id, reason);
        setActionNotice("Booking updated successfully.");
        fetchBookings();
      } catch (err) {
        setActionNotice("Failed to update booking. Please try again.");
      }
    },
    [fetchBookings]
  );

  const handlePaymentAction = useCallback(
    async (booking, action) => {
      if (!booking) return;
      try {
        switch (action) {
          case "initiate":
            await bookingService.payments.initiate(
              booking.id,
              booking.price ?? booking.initialOfferAmount
            );
            break;
          case "authorize":
            await bookingService.payments.authorize(booking.id);
            break;
          case "capture":
            await bookingService.payments.capture(booking.id);
            break;
          case "refund":
            await bookingService.payments.refund(booking.id);
            break;
          default:
            return;
        }
        setActionNotice("Payment updated.");
        fetchBookings();
      } catch (err) {
        setActionNotice("Payment action failed. Please retry.");
      }
    },
    [fetchBookings]
  );

  const canRateBooking = useCallback((booking) => {
    if (!booking) return false;
    const alreadyRated = booking.reviewSubmitted || typeof booking.userRating === "number";
    return booking.status === "COMPLETED" && !alreadyRated;
  }, []);

  const openInlineRating = useCallback((booking) => {
    setInlineRatingBookingId(booking.id);
    setInlineRatingValues((prev) => ({ ...prev, [booking.id]: prev[booking.id] || 5 }));
    setInlineRatingComments((prev) => ({ ...prev, [booking.id]: prev[booking.id] || "" }));
    setInlineRatingErrors((prev) => ({ ...prev, [booking.id]: "" }));
  }, []);

  const cancelInlineRating = useCallback(() => {
    setInlineRatingBookingId(null);
    setInlineRatingSubmitting(false);
  }, []);

  const handleInlineRatingChange = useCallback((booking, value) => {
    setInlineRatingValues((prev) => ({ ...prev, [booking.id]: value }));
  }, []);

  const handleInlineCommentChange = useCallback((booking, value) => {
    setInlineRatingComments((prev) => ({ ...prev, [booking.id]: value }));
  }, []);

  const handleInlineRatingSubmit = useCallback(async (booking) => {
    if (!booking) return;
    const comment = (inlineRatingComments[booking.id] || "").trim();
    if (!comment) {
      setInlineRatingErrors((prev) => ({ ...prev, [booking.id]: "Please add a short comment." }));
      return;
    }
    setInlineRatingSubmitting(true);
    setInlineRatingErrors((prev) => ({ ...prev, [booking.id]: "" }));
    try {
      await reviewService.addReview(booking.id, {
        rating: inlineRatingValues[booking.id] || 5,
        comment,
      });
      setActionNotice("Thanks for rating your booking!");
      cancelInlineRating();
      fetchBookings();
    } catch (err) {
      setInlineRatingErrors((prev) => ({ ...prev, [booking.id]: "Unable to submit review. Please try again." }));
    } finally {
      setInlineRatingSubmitting(false);
    }
  }, [inlineRatingComments, inlineRatingValues, cancelInlineRating, fetchBookings]);

  return (
    <DashboardLayout title="My Bookings">
      {error && <p className="text-red-500 mb-4">{error}</p>}
      {actionNotice && <p className="text-sm text-blue-600 mb-4">{actionNotice}</p>}
      {summaryError && <p className="text-sm text-red-500 mb-4">{summaryError}</p>}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-6">
        {(summaryLoading ? [1, 2, 3, 4] : summary ? [
          { label: "Upcoming", value: summary.upcomingBookings },
          { label: "Active", value: summary.activeBookings },
          { label: "Completed", value: summary.completedBookings },
          { label: "Total", value: summary.totalBookings },
        ] : []).map((tile, idx) => (
          <div
            key={tile?.label ?? idx}
            className="bg-white border border-slate-100 rounded-2xl p-4 shadow-sm"
          >
            <p className="text-xs uppercase text-slate-500">
              {tile?.label || "Loading"}
            </p>
            <p className="text-2xl font-bold text-slate-900 mt-1">
              {tile?.value ?? "…"}
            </p>
          </div>
        ))}
      </div>
      {loading ? (
        <p className="text-slate-600">Loading bookings...</p>
      ) : (
        <div className="grid lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2 grid gap-4">
            {bookings.map((booking) => (
              <BookingCard
                key={booking.id}
                booking={booking}
                onView={() => setSelectedBookingId(booking.id)}
                onChat={() =>
                  handleConversationSelect(
                    buildConversationFromBooking(booking, user?.id),
                    { toggleOnly: true }
                  )
                }
                onPrimaryAction={() => handlePrimaryAction(booking)}
                primaryLabel={primaryActionLabel(booking.status)}
                onRateStart={canRateBooking(booking) ? openInlineRating : undefined}
                canRate={canRateBooking(booking)}
                isRating={inlineRatingBookingId === booking.id}
                ratingValue={inlineRatingValues[booking.id] || 5}
                ratingComment={inlineRatingComments[booking.id] || ""}
                onRatingChange={handleInlineRatingChange}
                onRatingCommentChange={handleInlineCommentChange}
                onRatingSubmit={handleInlineRatingSubmit}
                onRatingCancel={cancelInlineRating}
                ratingSubmitting={inlineRatingSubmitting}
                ratingError={inlineRatingErrors[booking.id]}
              />
            ))}
            {bookings.length === 0 && (
              <p className="text-slate-600">No bookings yet. Search for a tradesperson to get started.</p>
            )}
          </div>
          <div className="space-y-4">
            {enRouteBooking && (
              <div className="bg-white border border-slate-100 rounded-2xl p-4 space-y-3 shadow-sm">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-xs uppercase text-slate-500">Live tracking</p>
                    <p className="text-base font-semibold text-slate-900">
                      {enRouteBooking.tradespersonName || "Tradesperson"} is on the way
                    </p>
                  </div>
                  {liveLocationStale ? (
                    <span className="text-xs px-2 py-1 rounded-full bg-amber-100 text-amber-700">
                      Awaiting update
                    </span>
                  ) : (
                    <span className="text-xs px-2 py-1 rounded-full bg-emerald-100 text-emerald-700">
                      Live
                    </span>
                  )}
                </div>
                {liveLocationError && (
                  <p className="text-xs text-red-500">{liveLocationError}</p>
                )}
                <LiveLocationMap
                  location={liveLocation}
                  stale={liveLocationStale}
                  userAddress={enRouteBooking.serviceAddress}
                  destination={{
                    latitude: enRouteBooking.userLatitude,
                    longitude: enRouteBooking.userLongitude,
                    label: enRouteBooking.userCity,
                  }}
                />
                {liveLocationLoading && (
                  <p className="text-xs text-slate-500">Fetching latest location…</p>
                )}
                {!liveLocation && !liveLocationLoading && (
                  <p className="text-xs text-slate-500">
                    Tradesperson hasn’t shared their location yet. We’ll update this view automatically.
                  </p>
                )}
              </div>
            )}
            <PaymentSummary
              booking={paymentBooking}
              onInitiate={(booking) => handlePaymentAction(booking, "initiate")}
              onAuthorize={(booking) => handlePaymentAction(booking, "authorize")}
              onCapture={(booking) => handlePaymentAction(booking, "capture")}
              onRefund={(booking) => handlePaymentAction(booking, "refund")}
            />
            {chatVisible && (
              <>
                <ConversationPanel
                  conversations={conversations}
                  activeConversationId={activeConversation?.id || activeConversation?.bookingId}
                  onSelect={(conversation) => handleConversationSelect(conversation)}
                />
                <ChatThread
                  conversation={activeConversation}
                  messages={messages}
                  onSend={(content, attachment) => handleSendMessage(content, attachment)}
                  loading={chatLoading}
                  error={chatError}
                />
              </>
            )}
          </div>
        </div>
      )}
      <BookingDetailModal booking={selectedBooking} onClose={() => setSelectedBookingId(null)} />
    </DashboardLayout>
  );
}

export default UserDashboard;