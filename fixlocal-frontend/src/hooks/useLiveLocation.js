import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { bookingService } from "../api/bookingService";
import { createStompClient, disconnectClient } from "../utils/liveLocation";
import { useAuth } from "../context/AuthContext";

const STALE_THRESHOLD_MINUTES = 5;

function isStale(timestamp) {
  if (!timestamp) return true;
  const updatedAt = new Date(timestamp).getTime();
  if (Number.isNaN(updatedAt)) return true;
  const diffMinutes = (Date.now() - updatedAt) / (1000 * 60);
  return diffMinutes > STALE_THRESHOLD_MINUTES;
}

export function useLiveLocation(bookingId, { enabled = true } = {}) {
  const { token } = useAuth() || {};
  const [location, setLocation] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const clientRef = useRef(null);
  const subscriptionRef = useRef(null);

  const destination = useMemo(() => {
    if (!bookingId) return null;
    return `/topic/bookings/${bookingId}/location`;
  }, [bookingId]);

  const updateState = useCallback((payload) => {
    setLocation((prev) => ({
      ...prev,
      ...payload,
      stale: payload.stale ?? isStale(payload.updatedAt),
    }));
  }, []);

  const fetchLatest = useCallback(async () => {
    if (!bookingId || !enabled) return;
    setLoading(true);
    setError(null);
    try {
      const { data } = await bookingService.getLiveLocation(bookingId);
      updateState(data);
    } catch (err) {
      setError("Unable to fetch live location");
    } finally {
      setLoading(false);
    }
  }, [bookingId, enabled, updateState]);

  useEffect(() => {
    if (!destination || !token || !enabled) return undefined;

    clientRef.current = createStompClient(token);
    clientRef.current.onConnect = () => {
      subscriptionRef.current = clientRef.current.subscribe(destination, (message) => {
        if (!message.body) return;
        try {
          const payload = JSON.parse(message.body);
          updateState(payload);
        } catch (err) {
          console.error("Failed to parse live location payload", err);
        }
      });
    };

    clientRef.current.onStompError = (frame) => {
      console.error("STOMP error", frame.headers["message"], frame.body);
    };

    return () => {
      if (subscriptionRef.current) {
        try {
          subscriptionRef.current.unsubscribe();
        } catch {}
      }
      disconnectClient(clientRef.current);
      subscriptionRef.current = null;
      clientRef.current = null;
    };
  }, [destination, enabled, token, updateState]);

  useEffect(() => {
    fetchLatest();
  }, [fetchLatest]);

  return {
    location,
    loading,
    error,
    stale: location ? location.stale : true,
    refresh: fetchLatest,
  };
}