### Skill Tags, Multi-Service Profiles, Chat & Escrow

### Overview

Tradespersons can now describe their capabilities using free-form skill tags and publish multiple service offerings with different prices/durations. These attributes are exposed in profile DTOs (`UserResponseDTO`, `TradespersonDTO`) and are queryable when users search for providers.

### Skill Tags

* Max 15 tags per tradesperson, each tag trimmed and truncated to 50 chars.
* Managed via `PUT /api/v1/users/me/skill-tags` with body:

```json
{
  "tags": ["plumbing", "water heater", "emergency"]
}
```

### Service Offerings & Geo Search

CRUD endpoints under `/api/v1/users/me/services` allow tradespersons to manage services they provide:

| Method | Endpoint | Request |
|--------|----------|---------|
| POST   | `/api/v1/users/me/services` | `ServiceOfferingRequest` (name, description, basePrice, durationMinutes)
| PUT    | `/api/v1/users/me/services/{serviceId}` | Same payload as POST to update existing offering
| DELETE | `/api/v1/users/me/services/{serviceId}` | Removes offering

Example request:

```json
{
  "name": "Water Heater Installation",
  "description": "Install standard electric or gas heaters",
  "basePrice": 250,
  "durationMinutes": 90
}
```

### Search Filtering & Geo-fencing

### Conversation & Chat Messaging

For each booking, FixLocal automatically provisions a conversation between the user and the tradesperson. REST endpoints:

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/v1/chat/conversations/{bookingId}` | Fetch or auto-create conversation for a booking |
| GET | `/api/v1/chat/conversations/{conversationId}/messages` | Paginated chat history |
| POST | `/api/v1/chat/bookings/{bookingId}/messages` | Send message with optional attachment (multipart). Fields: `content`, `attachment` |

Messages stream live over WebSocket topic `/topic/chat/{conversationId}`. Attachments up to 5MB are stored server-side with metadata on each `ChatMessage` (id, name, MIME type, size).

### Escrow & Payment Lifecycle

Bookings support escrow-style payments:

| Step | Endpoint | Description |
|------|----------|-------------|
| Initiate | `POST /api/v1/bookings/{bookingId}/payments/initiate?amount=...` | Creates a payment intent, marks booking paymentStatus=INITIATED |
| Authorize | `POST .../payments/authorize` | Authorizes held funds (paymentStatus=AUTHORIZED) |
| Capture | `POST .../payments/capture` | Captures funds, allowed once booking is COMPLETED (paymentStatus=CAPTURED) |
| Refund | `POST .../payments/refund` | Refunds held funds (paymentStatus=REFUNDED) |

Payment transitions are exposed via `EscrowService` and surfaced on `Booking` as `paymentStatus` + `paymentIntentId` for downstream integrations.

### Admin Insights & Alerts

Admins can:

* List all users/tradespersons/bookings with pagination.
* Block/unblock accounts, verify tradespersons.
* Fetch KPI snapshot via `GET /api/v1/admin/stats` which now includes:
  * Total users/tradespersons/bookings
  * Completed/pending/cancelled/rejected bookings
  * Average platform rating (from verified tradespersons with reviews)
  * Active conversations count
  * Pending verifications & blocked accounts
* Monitor high cancellation and rejection rates to trigger manual reviews (future automation can subscribe to these metrics for alerts).

`GET /api/v1/tradespersons/search` parameters:

| Param | Description |
|-------|-------------|
| `city` (required) | City context for search |
| `occupation` | Optional specialization filter |
| `minRating` | Minimum average rating |
| `tag` | Filter to tradespersons that include the specified skill tag |
| `latitude`, `longitude`, `radiusKm` | When provided together, results are limited to tradespersons whose lastKnownLatitude/Longitude fall within the radius and are sorted by distance |

Each `TradespersonDTO` now includes `distanceKm` when distance ranking is active.
