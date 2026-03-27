import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

const WS_BASE_URL = import.meta.env.VITE_WS_BASE_URL || "http://localhost:8080/ws";

export function createStompClient(token) {
  const client = new Client({
    webSocketFactory: () => new SockJS(WS_BASE_URL),
    connectHeaders: token
      ? {
          Authorization: `Bearer ${token}`,
        }
      : {},
    reconnectDelay: 5000,
    debug: import.meta.env.DEV ? (msg) => console.debug("[STOMP]", msg) : undefined,
  });

  client.activate();
  return client;
}

export function disconnectClient(client) {
  if (client) {
    try {
      client.deactivate();
    } catch (error) {
      console.error("Failed to deactivate STOMP client", error);
    }
  }
}