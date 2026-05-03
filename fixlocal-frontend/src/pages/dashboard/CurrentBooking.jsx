import { useState } from "react";
import DashboardLayout from "../../components/DashboardLayout";
import LiveLocationMap from "../../components/LiveLocationMap";
import PaymentSummary from "../../components/PaymentSummary";
import BookingCard from "../../components/BookingCard";
import ChatThread from "../../components/ChatThread";
import { bookingService } from "../../api/bookingService";
import disputeService from "../../api/disputeService";
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
    chatConversation,
    chatMessages,
    chatLoading,
    chatError,
    sendMessage,
  } = useCurrentBooking();
  const [chatVisible, setChatVisible] = useState(false);
  const [paymentBusy, setPaymentBusy] = useState(false);

  const normalizeInitiatePayload = (payload) => {
    const orderId = payload?.orderId || payload?.order_id;
    const amount = Number(payload?.amount);
    const keyId = payload?.keyId || import.meta.env.VITE_RAZORPAY_KEY_ID;
    const currency = payload?.currency || "INR";

    if (!orderId || !Number.isFinite(amount) || amount <= 0 || !keyId) {
      throw new Error(
        "Payment gateway response is incomplete. Please restart payment-service with Razorpay keys and try again."
      );
    }

    return { orderId, amount, keyId, currency };
  };

  const loadRazorpaySdk = async () => {
    if (window.Razorpay) return true;
    return new Promise((resolve) => {
      const script = document.createElement("script");
      script.src = "https://checkout.razorpay.com/v1/checkout.js";
      script.async = true;
      script.onload = () => resolve(true);
      script.onerror = () => resolve(false);
      document.body.appendChild(script);
    });
  };

  const launchRazorpayCheckout = async (booking, initiateData) => {
    const sdkLoaded = await loadRazorpaySdk();
    if (!sdkLoaded || !window.Razorpay) {
      throw new Error("Unable to load Razorpay checkout");
    }

    const userName = booking?.userName || "FixLocal User";

    return new Promise((resolve, reject) => {
      const options = {
        key: initiateData.keyId,
        amount: initiateData.amount,
        currency: initiateData.currency,
        name: "FixLocal",
        description: booking?.serviceDescription || "Service payment",
        order_id: initiateData.orderId,
        handler: async function (response) {
          try {
            await bookingService.payments.verify(booking.id, {
              orderId: response.razorpay_order_id,
              paymentId: response.razorpay_payment_id,
              signature: response.razorpay_signature,
            });
            resolve();
          } catch (verifyError) {
            reject(verifyError);
          }
        },
        modal: {
          ondismiss: () => reject(new Error("Payment cancelled")),
        },
        prefill: {
          name: userName,
        },
        theme: {
          color: "#2563eb",
        },
      };

      const razorpay = new window.Razorpay(options);
      razorpay.on("payment.failed", (err) => {
        reject(new Error(err?.error?.description || "Payment failed"));
      });
      razorpay.open();
    });
  };

  const handleCancel = async (booking, reason) => {
    await bookingService.cancel(booking.id, reason);
    setActionNotice("Booking cancelled");
    refresh();
  };

  const handlePaymentAction = async (booking, action) => {
    if (!booking || paymentBusy) return;
    setPaymentBusy(true);
    try {
      if (action === "initiate") {
        const amount = Number(booking.price ?? booking.initialOfferAmount ?? 0);
        const { data: initiateData } = await bookingService.payments.initiate(booking.id, amount);
        const checkoutData = normalizeInitiatePayload(initiateData);
        await launchRazorpayCheckout(booking, checkoutData);
      } else if (action === "capture") {
        await bookingService.payments.capture(booking.id);
      } else if (action === "refund") {
        await bookingService.payments.refund(booking.id);
      }

      setActionNotice("Payment updated successfully.");
      await refresh();
    } catch (err) {
      setActionNotice(err?.response?.data?.message || err?.message || "Payment action failed. Please retry.");
    } finally {
      setPaymentBusy(false);
    }
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
              onChat={() => setChatVisible((prev) => !prev)}
              onDispute={async (payload) =>
                disputeService.create({
                  bookingId: payload.bookingId,
                  reason: payload.reason,
                  desiredOutcome: payload.desiredOutcome,
                })
              }
              onPrimaryAction={() => handleCancel(activeBooking, "Cancelled from current view")}
              primaryLabel="Cancel booking"
            />
            <PaymentSummary
              booking={activeBooking}
              busy={paymentBusy}
              onInitiate={(booking) => handlePaymentAction(booking, "initiate")}
              onCapture={(booking) => handlePaymentAction(booking, "capture")}
              onRefund={(booking) => handlePaymentAction(booking, "refund")}
            />
            {chatVisible && (
              <ChatThread
                conversation={chatConversation}
                messages={chatMessages}
                loading={chatLoading}
                error={chatError}
                onSend={(content, attachment) =>
                  sendMessage(activeBooking.id, { content, attachment })
                }
              />
            )}
          </div>
          <div className="space-y-4">
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