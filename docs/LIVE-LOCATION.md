# Live Location Feature

## Overview

When a tradesperson starts a trip (booking status **EN_ROUTE**), the backend stores periodic GPS points (`LiveLocation` documents with a 15-minute TTL) and broadcasts them over `/topic/bookings/{bookingId}/location`.

- **REST**: `GET /api/v1/bookings/{bookingId}/location` returns the last known coordinates + `stale` flag.
- **WebSocket**: Live `LiveLocationEvent` payloads are pushed automatically as tradespersons call `POST /api/v1/bookings/{bookingId}/location`.

## Frontend Flows

### User Dashboard
1. `useLiveLocation(bookingId)` fetches the initial coordinate then subscribes to the booking-specific WebSocket destination.
2. `LiveLocationMap` renders a Leaflet map with the tradesperson marker and shows stale/awaiting states.
3. When the user captured their city + GPS at booking time, the map also shows their destination pin and the OSRM-generated path so they can see how far away the tradesperson is.
4. Automatically appears for bookings whose status is `EN_ROUTE` or `ARRIVED`.

### Tradesperson Dashboard
1. A **Live location** panel is shown when at least one booking is `EN_ROUTE`.
2. Tradesperson can pull browser GPS (`navigator.geolocation`) or manually enter lat/lng, then hit **Share location`** which POSTs to the backend.
3. After each update, the panel calls the OSRM public routing service to draw a suggested route between the tradesperson’s latest position and the user’s saved coordinates; the panel shows distance + ETA and renders a Leaflet preview map.
4. Users watching the booking receive updates instantly via WebSocket.

## Manual QA Checklist
1. Launch backend (`mvn spring-boot:run`, requires JDK 17+) and frontend (`npm run dev`).
2. Create a booking, accept it, and click **Start Trip** as the tradesperson.
3. In the tradesperson dashboard, use the Live location panel to submit coordinates (GPS or manual).
4. Log in as the user: confirm the dashboard shows the map, updates coordinates live, and marks stale if no updates arrive for >5 minutes.
5. Complete/cancel the booking: verify map disappears on next render and backend removes the LiveLocation entry.