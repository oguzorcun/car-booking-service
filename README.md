# Car Booking Service

Spring Boot microservice exposing a REST API

## Running locally
Requires
- JDK 21
- Maven
- Docker

```bash
# Start PostgreSQL, Kafka and Wiremock
docker compose up -d

# Build and run (check JAVA_HOME is 21)
mvn spring-boot:run
```
REST API with Swagger served on http://localhost:8080

## Functionality
- **Serves a REST API** 
  - `POST /bookings` creates a booking depending on the payment mode of the booking
    - rejects the booking when the vehicle already has a `CONFIRMED` or `PENDING_PAYMENT` booking that
      overlaps the requested rental period, so back-to-back rentals (one ends the day the next starts)
      stay possible

- **Consumes a kafka topic** 
  - `bank-transfer-payment-events` and confirms pending bookings 
    - event:    `BankTransferPaymentEvent`
      - `BankTransferPaymentEvent.transactionDetails`:
      - format:   `<TxnRef(12 chars)> <BookingId(10 chars)>`
      - example   `TXN987654321 BKG0012345`

- **Runs a scheduled job**
  - cancels bookings that are still `PENDING_PAYMENT` less than 48 hours before rental start

## Decisions
- API-spec first approach, single source of truth, less brittle APIs
- Auto generates car-booking-api driver classes and credit-card-api client classes using spring maven plugin
- Maps at boundaries to prevent leaking generated classes into domain layer

## Assumptions

- The vehicle id is validated by in-memory mock repository
- The rental period is half-open: `rentalStartDate` is the first rented day and `rentalEndDate` the
  return day, so a booking starting on the return day of another one does not clash with it
- A pending bank transfer holds the vehicle until it is paid or cancelled by the scheduled job
- Bank transfer amount is always accepted as full (no price to compare in the booking request)

## Potential improvements (out of scope)

- Vehicle availability is checked before saving, which leaves a race between two concurrent requests
  for the same vehicle and period; fixing it needs `SERIALIZABLE` isolation with retry or a Postgres
  `EXCLUDE USING gist` constraint over the vehicle id and date range
- Resiliency for credit-card-api 
  - timeouts, retries, circuit breaker
  - finer-grained response handling
- Security, observability for car-booking-api
  - authentication and rate limiting
  - logging/metrics/health
- Kafka:
  - Handle duplicate payment event by id (kinda already handled by BookingStatus check but not a guarantee)
  - Add DLT for cases like malformed event ser/de error
  - finer-grained error handling
- Add price to CreateBookingRequest and verify with BankTransferPaymentEvent.paymentAmount before confirming a booking
- Add real vehicle repo
