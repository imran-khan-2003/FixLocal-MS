## Dashboard Enhancements (2026-03-22)

- **User Dashboard**
  - Live bookings with cancellation/payment workflows
  - Conversational sidebar + chat thread wired to `/api/v1/chat`
  - Booking detail modal and payment summary orchestrations

- **Tradesperson Dashboard**
  - State filters (Pending, Accepted, En Route, Arrived, Completed)
  - Action buttons: Accept, Start Trip, Mark Arrived, Complete
  - Shared chat workspace mirroring user experience

- **Admin Dashboard**
  - KPI grid covering totals, verification pipeline, conversations, blocks
  - Operational notes checklist for quick context

- **Components**
  - `BookingCard` timelines for status history, dual CTA support
  - `ChatThread` now supports attachments + loading/errors states
