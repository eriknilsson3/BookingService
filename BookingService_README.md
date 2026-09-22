# BookingService

BookingService is the Spring Boot microservice responsible for rooms and bookings in the guesthouse system.

The BookingService is intentionally kept as its own project and GitHub repository. Customer management is handled by the separate CustomerService.

## Responsibilities

BookingService is responsible for:

- Managing rooms.
- Providing at least 10 bookable rooms through database initialization.
- Creating, updating and cancelling bookings.
- Preventing double bookings for the same room and overlapping dates.
- Searching for available rooms by date and date range.
- Supporting single and double rooms.
- Supporting an optional extra bed for double rooms when the room allows it.
- Storing only the customer ID for a booking; customer data belongs to CustomerService.
- Validating customers by calling CustomerService over REST when a booking is created.
- Providing the guesthouse frontend.
- Validating JWTs on protected endpoints.
- Forwarding the JWT when BookingService calls CustomerService.

## Technology

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA / Hibernate
- MySQL
- Maven
- JWT authentication
- Docker
- Docker Compose
- GitHub Actions

## Project structure

```text
BookingService/
├── .github/
│   └── workflows/
│       └── ci.yml
├── .mvn/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── se/erik/bookingservice/
│   │   │       ├── booking/
│   │   │       ├── room/
│   │   │       ├── security/
│   │   │       ├── config/
│   │   │       ├── dto/
│   │   │       └── error/
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   │           ├── api-config.js
│   │           ├── *.html
│   │           ├── *.js
│   │           └── *.css
│   └── test/
├── Dockerfile
├── .dockerignore
├── .env.example
├── .gitignore
├── mvnw
├── pom.xml
└── README.md
```

The frontend is served by BookingService from Spring Boot's `static` resources.

## REST API

### Bookings

```text
GET    /bookings
GET    /bookings/{id}
GET    /bookings/customer/{customerId}
GET    /bookings/customer/{customerId}/active
GET    /bookings/customer/{customerId}/room/{roomId}
GET    /bookings/room/{roomId}
GET    /bookings/my

GET    /bookings/available?date=YYYY-MM-DD
GET    /bookings/available-range?start=YYYY-MM-DD&end=YYYY-MM-DD

POST   /bookings
PUT    /bookings/{id}
DELETE /bookings/{id}
```

The booking creation request includes:

```json
{
  "roomId": 1,
  "startDate": "2030-12-01",
  "endDate": "2030-12-05",
  "extraBed": false
}
```

For an extra-bed booking:

```json
{
  "roomId": 2,
  "startDate": "2030-12-01",
  "endDate": "2030-12-05",
  "extraBed": true
}
```

An extra bed is accepted only for a double room where `extraBedAllowed` is true.

### Rooms

```text
GET    /rooms
GET    /rooms/{id}
POST   /rooms
PUT    /rooms/{id}
DELETE /rooms/{id}
```

### Customer/booking service communication

CustomerService uses:

```text
GET /bookings/customer/{customerId}/active
```

to check whether a customer has active bookings before allowing account deletion.

BookingService calls CustomerService over REST when a booking is created to verify that the customer exists.

## Authentication

Booking modification endpoints use JWT authentication.

The token is sent as:

```text
Authorization: Bearer <JWT>
```

BookingService validates the token locally.

When BookingService calls CustomerService, the incoming Authorization header is forwarded so that the service-to-service request also carries the JWT.

## Frontend

The frontend is served by BookingService.

The frontend API URLs are configured centrally in:

```text
src/main/resources/static/api-config.js
```

The frontend uses:

- BookingService's own origin for BookingService API calls in production.
- A configurable CustomerService public URL for browser-to-CustomerService calls.
- `localhost` URLs only for local development.

Do not put passwords, JWT secrets or other private values in frontend JavaScript.

## Configuration

Create a local `.env` from `.env.example`.

Typical local variables are:

```text
PORT=8080
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3309/bookingdb
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=CHANGE_ME
JWT_SECRET=CHANGE_ME
CUSTOMER_SERVICE_BASE_URL=http://localhost:8081
FRONTEND_ORIGIN=http://localhost:8080
```

The real `.env` file must never be committed.

Production values are supplied through Railway environment variables.

For Railway, service-to-service communication should use Railway private networking, for example:

```text
CUSTOMER_SERVICE_BASE_URL=http://customer-service.railway.internal:8081
```

The actual Railway service hostname depends on the service name.

## Local development

### Run from IntelliJ

Use Java 17 and the local environment variables.

The application runs by default on:

```text
http://localhost:8080
```

The frontend can be opened from the BookingService application, for example:

```text
http://localhost:8080/login.html
```

### Run tests

Windows:

```powershell
.\mvnw test
```

Linux/macOS:

```bash
./mvnw test
```

The project contains service-layer unit tests and integration tests using MockMvc and the test database configuration.

## Docker

Build the service image locally:

```bash
docker build -t bookingservice .
```

Run it with the required environment variables and a reachable MySQL database.

The root project may also provide a Docker Compose setup that runs both microservices and their databases together.

## GitHub Actions

The CI workflow runs on pushes and pull requests targeting `main`.

The push-to-main flow is:

```text
Push to main
    |
    v
Checkout source
    |
    v
Set up Java 17
    |
    v
Run Maven tests
    |
    v
If tests pass:
    |
    +--> Log in to Docker Hub
    |
    +--> Build Docker image
    |
    +--> Push:
          username/bookingservice:latest
          username/bookingservice:<commit-sha>
```

Docker Hub credentials are stored in GitHub repository secrets:

```text
DOCKERHUB_USERNAME
DOCKERHUB_TOKEN
```

The Docker Hub token must be a Personal Access Token, not the normal account password.

## Docker image

The CI pipeline publishes:

```text
<DOCKERHUB_USERNAME>/bookingservice:latest
<DOCKERHUB_USERNAME>/bookingservice:<commit-sha>
```

The image is built only after the test step succeeds.

## Railway deployment

This repository is intended to be connected directly to Railway as a GitHub repository.

Railway detects the root `Dockerfile` and builds the service from the repository.

The intended deployment flow is:

```text
git push main
    |
    v
GitHub Actions
    |
    +--> tests
    |
    +--> Docker Hub image
    |
    v
Railway GitHub deployment
    |
    v
New BookingService deployment
```

Railway's `Wait for CI` feature can be enabled so that Railway waits for the GitHub Actions check to finish before deploying.

The frontend and BookingService are deployed together because the frontend is packaged under `src/main/resources/static`.

## Security

Never commit:

```text
.env
.env.*
target/
.idea/
```

`.env.example` is safe to commit because it contains placeholders only.

JWT secrets, database passwords, Docker Hub tokens and Railway secrets must be stored in environment variables or GitHub Secrets.

## Current testing

The integration test suite includes API-level tests for:

- Successful booking creation.
- Double-booking rejection with HTTP 409.
- Successful extra-bed booking and persistence of the extra-bed flag.

The assignment requires at least three integration tests that make actual API calls and verify application behavior.

## Assignment role

BookingService satisfies the Booking Service part of the guesthouse microservice assignment:

- rooms and bookings remain here;
- the frontend remains here;
- customer data is not stored here;
- CustomerService is used through REST;
- JWT authentication protects modifying endpoints;
- Docker and CI/CD are supported.

The optional ReviewService and Kubernetes requirements are separate assignment items and are not implemented by this repository.
