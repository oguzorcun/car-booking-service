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
- Bank transfer amount is always accepted as full (no price to compare in the booking request)

## Potential improvements (out of scope)

- Resiliency for credit-card-api 
  - timeouts, retries, circuit breaker
  - finer-grained response handling
- Security, observability for car-booking-api
  - authentication and rate limiting
  - logging/metrics/health
- Kafka:
  - Handle duplicate payment event by id (kinda already handled by BookingStatus check but not a guarantee)
  - Add DLT for cases like malformed event ser/de error
- Add price to CreateBookingRequest and verify with BankTransferPaymentEvent.paymentAmount before confirming a booking
- Add real vehicle repo
