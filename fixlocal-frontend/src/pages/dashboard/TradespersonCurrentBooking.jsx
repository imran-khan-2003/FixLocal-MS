import DashboardLayout from "../../components/DashboardLayout";
import BookingCard from "../../components/BookingCard";
import TradespersonLocationPanel from "../../components/TradespersonLocationPanel";
import ChatThread from "../../components/ChatThread";
import ConversationPanel from "../../components/ConversationPanel";
import { useTradespersonBookings } from "../../hooks/useTradespersonBookings";
import { bookingService } from "../../api/bookingService";
import chatService from "../../api/chatService";
import { useCallback, useMemo, useState, useEffect } from "react";

const ACTION_CONFIG = {
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
    primaryLabel: "Complete Job",
  },
};

const CANCELABLE_STATUSES = new Set(["ACCEPTED", "EN_ROUTE", "ARRIVED"]);

function TradespersonCurrentBooking() {
  const {
    currentBooking,
    loading,
    error,
    actionNotice,
    setActionNotice,
    refresh,
  } = useTradespersonBookings();
  const [submitting, setSubmitting] = useState(false);
  const [chatConversation, setChatConversation] = useState(null);
  const [chatMessages, setChatMessages] = useState([]);
  const [chatLoading, setChatLoading] = useState(false);
  const [chatError, setChatError] = useState("");

  const loadConversation = useCallback(
    async (bookingId) => {
      if (!bookingId) return;
      setChatLoading(true);
      setChatError("");
      try {
        const { data: conversation } = await chatService.getConversation(bookingId);
        setChatConversation({
          id: conversation.id,
          bookingId,
          tradespersonName: conversation.userName || "Customer",
          lastMessageAt: conversation.lastMessage?.createdAt,
        });
        const { data } = await chatService.getMessages(conversation.id);
        const items = data?.content || [];
        setChatMessages(
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
    },
    []
  );

  const sendMessage = useCallback(
    async (bookingId, { content, attachment }) => {
      if (!bookingId) return;
      try {
        await chatService.sendMessage(bookingId, { content, attachment });
        loadConversation(bookingId);
      } catch (err) {
        setChatError("Failed to send message");
      }
    },
    [loadConversation]
  );

  useEffect(() => {
    if (currentBooking?.id) {
      loadConversation(currentBooking.id);
    }
  }, [currentBooking?.id, loadConversation]);

  const handleAction = useCallback(
    async (booking, action) => {
      if (!booking || !action) return;
      setSubmitting(true);
      try {
        if (action === "START") await bookingService.startTrip(booking.id);
        if (action === "ARRIVED") await bookingService.markArrived(booking.id);
        if (action === "COMPLETE") await bookingService.complete(booking.id);
        setActionNotice("Booking updated.");
        refresh();
      } catch (err) {
        setActionNotice("Failed to update booking. Please retry.");
      } finally {
        setSubmitting(false);
      }
    },
    [refresh, setActionNotice]
  );

  const handleCancel = useCallback(
    async (booking) => {
      if (!booking) return;
      const confirmCancel = window.confirm("Cancel this booking?");
      if (!confirmCancel) return;
      const reason =
        window.prompt("Reason for cancellation?", "Cancelled from current view") ||
        "Cancelled from current view";
      setSubmitting(true);
      try {
        await bookingService.cancel(booking.id, reason);
        setActionNotice("Booking cancelled.");
        refresh();
      } catch (err) {
        setActionNotice("Failed to cancel booking. Please try again.");
      } finally {
        setSubmitting(false);
      }
    },
    [refresh, setActionNotice]
  );

  const actionConfig = useMemo(
    () => ACTION_CONFIG[currentBooking?.status] || {},
    [currentBooking?.status]
  );

  return (
    <DashboardLayout
      title="Current Engagement"
      subtitle="Track your ongoing job and share live location"
    >
      {error && <p className="text-red-500 mb-4">{error}</p>}
      {actionNotice && <p className="text-blue-600 text-sm mb-4">{actionNotice}</p>}
      {loading ? (
        <p className="text-slate-600">Loading current booking…</p>
      ) : !currentBooking ? (
        <p className="text-slate-600">
          You don't have an active booking right now. Accept a request to start sharing updates.
        </p>
      ) : (
        <div className="grid lg:grid-cols-2 gap-6">
          <div className="space-y-4">
            <BookingCard
              booking={currentBooking}
              showCustomerDetails
              onPrimaryAction={
                actionConfig.primaryAction
                  ? () => handleAction(currentBooking, actionConfig.primaryAction)
                  : undefined
              }
              primaryLabel={submitting ? "Working…" : actionConfig.primaryLabel}
              onDangerAction={
                CANCELABLE_STATUSES.has(currentBooking.status)
                  ? () => handleCancel(currentBooking)
                  : undefined
              }
              dangerLabel={
                CANCELABLE_STATUSES.has(currentBooking.status) ? "Cancel Booking" : undefined
              }
            />
            <ChatThread
              conversation={chatConversation}
              messages={chatMessages}
              loading={chatLoading}
              error={chatError}
              onSend={(content, attachment) =>
                sendMessage(currentBooking.id, { content, attachment })
              }
            />
          </div>
          <div className="space-y-4">
            <TradespersonLocationPanel booking={currentBooking} />
            <ConversationPanel
              conversations={currentBooking ? [currentBooking] : []}
              activeConversationId={chatConversation?.id}
              onSelect={() => loadConversation(currentBooking.id)}
            />
          </div>
        </div>
      )}
    </DashboardLayout>
  );
}

export default TradespersonCurrentBooking;