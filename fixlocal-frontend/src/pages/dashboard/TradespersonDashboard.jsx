import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import DashboardLayout from "../../components/DashboardLayout";
import BookingCard from "../../components/BookingCard";
import ConversationPanel from "../../components/ConversationPanel";
import ChatThread from "../../components/ChatThread";
import { bookingService } from "../../api/bookingService";
import chatService from "../../api/chatService";
import { dashboardService } from "../../api/dashboardService";
import TradespersonLocationPanel from "../../components/TradespersonLocationPanel";

const ACTION_CONFIG = {
  PENDING: {
    primaryAction: "ACCEPT",
    primaryLabel: "Accept",
    secondaryAction: "REJECT",
    secondaryLabel: "Reject",
  },
  ACCEPTED: {
    primaryAction: "START",
    primaryLabel: "Start Trip",
  },
  EN_ROUTE: {
    primaryAction: "ARRIVED",
    primaryLabel: "Mark Arrived",
  },
  ARRIVED: {
    primaryAction: "COMPLETE",
    primaryLabel: "Complete",
  },
};

function TradespersonDashboard() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [actionNotice, setActionNotice] = useState("");
  const [filter, setFilter] = useState("PENDING");
  const [activeConversation, setActiveConversation] = useState(null);
  const [messages, setMessages] = useState([]);
  const [chatLoading, setChatLoading] = useState(false);
  const [chatError, setChatError] = useState("");
  const [summary, setSummary] = useState(null);
  const [summaryLoading, setSummaryLoading] = useState(false);
  const [summaryError, setSummaryError] = useState("");
  const [chatVisible, setChatVisible] = useState(false);

  const fetchBookings = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const { data } = await bookingService.listForTradesperson(
        filter !== "ALL" ? filter : undefined
      );
      setBookings(data?.content || data || []);
    } catch (err) {
      setError("Failed to load bookings");
    } finally {
      setLoading(false);
    }
  }, [filter]);

  useEffect(() => {
    fetchBookings();
  }, [fetchBookings]);

  useEffect(() => {
    let active = true;
    setSummaryLoading(true);
    setSummaryError("");
    dashboardService
      .getTradespersonDashboard()
      .then(({ data }) => {
        if (active) setSummary(data);
      })
      .catch(() => {
        if (active) setSummaryError("Failed to load performance summary");
      })
      .finally(() => {
        if (active) setSummaryLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  const handleAction = useCallback(
    async (booking, action) => {
      if (!booking) return;
      try {
        if (action === "ACCEPT") await bookingService.acceptBooking(booking.id);
        if (action === "REJECT") await bookingService.rejectBooking(booking.id);
        if (action === "START") await bookingService.startTrip(booking.id);
        if (action === "ARRIVED") await bookingService.markArrived(booking.id);
        if (action === "COMPLETE") await bookingService.complete(booking.id);
        setActionNotice("Booking updated.");
        fetchBookings();
      } catch (err) {
        setActionNotice("Action failed. Please retry.");
      }
    },
    [fetchBookings]
  );

  const conversations = useMemo(
    () =>
      bookings.map((booking) => ({
        id: booking.id,
        bookingId: booking.id,
        tradespersonName: booking.userName || "Customer",
        lastMessage: booking.lastMessage?.content || booking.serviceDescription,
        lastMessageAt: booking.lastMessage?.createdAt,
        participantId: booking.userId,
      })),
    [bookings]
  );

  const fetchConversation = useCallback(async (bookingId, seed) => {
    setChatLoading(true);
    setChatError("");
    try {
      const { data: conversation } = await chatService.getConversation(bookingId);
      setActiveConversation({
        ...(seed || {}),
        id: conversation.id,
        bookingId,
        tradespersonName: seed?.tradespersonName || conversation.userName || "Customer",
      });
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
            mine: msg.senderRole === "TRADESPERSON",
            attachment: msg.attachment,
          }))
      );
    } catch (err) {
      setChatError("Failed to load chat");
    } finally {
      setChatLoading(false);
    }
  }, []);

  const handleConversationSelect = useCallback(
    (conversation, { toggleOnly } = {}) => {
      if (!conversation) {
        setActiveConversation(null);
        setMessages([]);
        setChatVisible(false);
        return;
      }

      const isSameConversation = activeConversation?.bookingId === conversation.bookingId;

      if (toggleOnly && isSameConversation) {
        setChatVisible((prev) => !prev);
        return;
      }

      setActiveConversation(conversation);
      setChatVisible(true);
      fetchConversation(conversation.bookingId, conversation);
    },
    [activeConversation?.bookingId, fetchConversation]
  );

  const handleSendMessage = useCallback(
    async (content, attachment) => {
      if (!activeConversation) return;
      try {
        await chatService.sendMessage(activeConversation.bookingId, { content, attachment });
        fetchConversation(activeConversation.bookingId, activeConversation);
      } catch (err) {
        setChatError("Failed to send message");
      }
    },
    [activeConversation, fetchConversation]
  );

  const filteredBookings = bookings;

  const enRouteBooking = useMemo(
    () =>
      bookings.find((booking) => booking.status === "EN_ROUTE") || null,
    [bookings]
  );

  const getActionConfig = (status) => ACTION_CONFIG[status] || {};

  return (
    <DashboardLayout
      title="Tradesperson Console"
      subtitle="Manage requests, trips, and chats"
      actions={
        <div className="flex gap-3">
          <select
            className="border rounded-lg px-3 py-1 text-sm"
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
          >
            <option value="ALL">All</option>
            <option value="PENDING">Pending</option>
            <option value="ACCEPTED">Accepted</option>
            <option value="EN_ROUTE">En Route</option>
            <option value="ARRIVED">Arrived</option>
            <option value="COMPLETED">Completed</option>
          </select>
          <Link
            to="/dashboard/tradesperson/ratings"
            className="text-sm font-semibold text-blue-600 hover:underline"
          >
            View Ratings
          </Link>
        </div>
      }
    >
      {error && <p className="text-red-500 mb-4">{error}</p>}
      {summaryError && <p className="text-red-500 text-sm mb-4">{summaryError}</p>}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-6">
        {(summaryLoading ? [1, 2, 3, 4] : summary ? [
          { label: "Pending", value: summary.pendingRequests },
          { label: "Active", value: summary.activeBookings },
          { label: "Completed", value: summary.completedBookings },
          { label: "Total", value: summary.totalBookings },
          { label: "Avg Rating", value: `${(summary.averageRating ?? 0).toFixed(1)} ★` },
          { label: "Reviews", value: summary.totalReviews },
        ] : []).map((tile, idx) => (
          <div
            key={tile?.label ?? idx}
            className="bg-white border border-slate-100 rounded-2xl p-4 shadow-sm"
          >
            <p className="text-xs uppercase text-slate-500">
              {tile?.label || "Loading"}
            </p>
            <p className="text-2xl font-bold text-slate-900 mt-1">{tile?.value ?? "…"}</p>
          </div>
        ))}
      </div>
      {actionNotice && <p className="text-blue-600 text-sm mb-4">{actionNotice}</p>}
      {loading ? (
        <p className="text-slate-600">Loading requests...</p>
      ) : (
        <div className="grid lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2 grid gap-4">
            {filteredBookings.map((booking) => {
              const actionConfig = getActionConfig(booking.status);
              return (
                <BookingCard
                  key={booking.id}
                  booking={booking}
                  onChat={() =>
                    handleConversationSelect({
                      bookingId: booking.id,
                      id: booking.id,
                      tradespersonName: booking.userName || "Customer",
                    }, { toggleOnly: true })
                  }
                  onPrimaryAction={
                    actionConfig.primaryAction
                      ? () => handleAction(booking, actionConfig.primaryAction)
                      : undefined
                  }
                  primaryLabel={actionConfig.primaryLabel}
                  onSecondaryAction={
                    actionConfig.secondaryAction
                      ? () => handleAction(booking, actionConfig.secondaryAction)
                      : undefined
                  }
                  secondaryLabel={actionConfig.secondaryLabel}
                  showCustomerDetails
                />
              );
            })}
            {filteredBookings.length === 0 && (
              <p className="text-slate-600">No bookings in this state.</p>
            )}
          </div>
          <div className="space-y-4">
            <TradespersonLocationPanel booking={enRouteBooking} />
            {chatVisible && (
              <>
                <ConversationPanel
                  conversations={conversations}
                  activeConversationId={activeConversation?.id}
                  onSelect={(conversation) => handleConversationSelect(conversation)}
                />
                <ChatThread
                  conversation={activeConversation}
                  messages={messages}
                  onSend={handleSendMessage}
                  loading={chatLoading}
                  error={chatError}
                />
              </>
            )}
          </div>
        </div>
      )}
    </DashboardLayout>
  );
}

export default TradespersonDashboard;