# spring-boot-event-checkin


# Solstice Event Check-In Service

An asynchronous event check-in backend built with Java 21, Spring Boot, RabbitMQ, and webhook-based communication.

The system manages attendee check-in and badge printing through an event-driven workflow. Instead of blocking the check-in process while waiting for a badge printer to respond, print requests are published to RabbitMQ and the attendee remains in a `PENDING` state until the printer confirms completion through a webhook.

---

## Overview

Solstice Events Co. operates a multi-day technology conference where attendees are checked in by scanning their QR codes.

The original badge-printing workflow depended on a synchronous REST API:

```text
QR Scan
   |
   v
Check-In Service
   |
   v
Badge Printer REST API
   |
   | Wait for response
   v
Badge Printed
   |
   v
CHECKED_IN
````

This created a direct dependency between the kiosk and the printer. The kiosk could not complete the check-in operation until the printer responded.

The printer vendor subsequently deprecated the synchronous API.

The system was therefore redesigned around asynchronous messaging:

```text
QR Scan
   |
   v
Spring Boot Check-In API
   |
   | Create print request
   v
RabbitMQ
   |
   | Message
   v
Badge Printer
   |
   | Print completed
   v
Webhook
   |
   v
Spring Boot Check-In API
   |
   v
CHECKED_IN
```

The attendee is initially marked as `PENDING`. The attendee only becomes `CHECKED_IN` after the application receives confirmation that the badge was successfully printed.

---

## Key Features

* Asynchronous badge-printing workflow
* RabbitMQ message broker integration
* RESTful check-in API
* Webhook-based print completion callback
* Attendee state management
* Duplicate scan protection
* Simulated external badge-printer service
* Docker-based RabbitMQ infrastructure
* Java 21 and Spring Boot
* Maven build and dependency management

---

## Technology Stack

| Technology      | Role                              |
| --------------- | --------------------------------- |
| Java 21         | Application language              |
| Spring Boot 3.5 | Backend framework                 |
| Spring AMQP     | RabbitMQ integration              |
| RabbitMQ 4      | Message broker                    |
| Maven           | Build and dependency management   |
| Docker          | Local infrastructure              |
| REST            | HTTP API communication            |
| Webhooks        | Asynchronous completion callbacks |

---

## Architecture

```text
                         +----------------------+
                         |      Event Kiosk     |
                         |      QR Scanner      |
                         +----------+-----------+
                                    |
                                    | POST /api/checkin/{id}
                                    v
                         +----------------------+
                         |    Check-In API      |
                         |    Spring Boot       |
                         +----------+-----------+
                                    |
                                    | PrintRequest
                                    v
                         +----------------------+
                         |       RabbitMQ       |
                         |    print-requests    |
                         +----------+-----------+
                                    |
                                    | AMQP Message
                                    v
                         +----------------------+
                         |  Simulated Badge     |
                         |      Printer         |
                         +----------+-----------+
                                    |
                                    | Print completed
                                    v
                         +----------------------+
                         |   Webhook Endpoint   |
                         | /api/webhook/        |
                         |   print-completed    |
                         +----------+-----------+
                                    |
                                    v
                              CHECKED_IN
```

The architecture separates the check-in operation from the badge-printing operation.

This means the check-in service does not need to maintain an open synchronous request while the printer performs its work.

---

## Attendee State Lifecycle

An attendee progresses through the following states:

```text
NOT_CHECKED_IN
       |
       | QR scan
       v
    PENDING
       |
       | Printer completion webhook
       v
   CHECKED_IN
```

The `PENDING` state is important because it represents the period between accepting a check-in request and receiving confirmation that the badge has actually been printed.

An attendee in `CHECKED_IN` state cannot trigger another badge-print request.

---

## Project Structure

The project follows a conventional Spring Boot structure:

![Project Structure](https://github.com/user-attachments/assets/bb70ab5a-67c8-4727-bd86-87c4104ec1b1)

```text
solstice-checkin/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           └── solstice/
│       │               ├── config/
│       │               ├── controller/
│       │               ├── messaging/
│       │               ├── model/
│       │               └── service/
│       └── resources/
├── pom.xml
└── README.md
```

---

## Application Startup

The Spring Boot application runs on port `8081`.

![Application Successfully Running](https://github.com/user-attachments/assets/bd348a60-834e-4f30-9b75-bfadf6d04f51)

---

# Installation and Setup

## Prerequisites

Before running the application, install the following:

* Java 21
* Maven
* Docker
* Git

Verify Java:

```bash
java --version
```

Expected output should indicate Java 21.

Verify Maven:

```bash
mvn --version
```

Verify Docker:

```bash
docker --version
```

---

## 1. Clone the Repository

Clone the repository:

```bash
git clone https://github.com/YOUR-USERNAME/solstice-event-checkin.git
```

Move into the project directory:

```bash
cd solstice-event-checkin
```

---

## 2. Start RabbitMQ

RabbitMQ is used as the message broker between the check-in service and the badge-printer service.

Start RabbitMQ using Docker:

```bash
docker run -d \
  --name solstice-rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:4-management
```

Verify that the container is running:

```bash
docker ps
```

You should see the `solstice-rabbitmq` container running.

---

## 3. Open RabbitMQ Management

RabbitMQ provides a web-based management interface.

Open:

```text
http://localhost:15672
```

For the default local RabbitMQ container:

```text
Username: guest
Password: guest
```

The application uses:

```text
AMQP Host: localhost
AMQP Port: 5672
```

The print queue used by the application is:

```text
print-requests
```

![RabbitMQ Queue](https://github.com/user-attachments/assets/08a34ade-adde-40a3-b101-839a94f3ada0)

---

## 4. Build the Application

From the project root:

```bash
mvn clean package
```

A successful build should finish with:

```text
BUILD SUCCESS
```

---

## 5. Start Spring Boot

Run the application:

```bash
mvn spring-boot:run
```

The application starts on:

```text
http://localhost:8081
```

The root endpoint is not defined, so accessing:

```text
http://localhost:8081/
```

may return a `404 Not Found`.

This is expected.

The application is accessed through its API endpoints.

---

# API Usage

## Check In an Attendee

Endpoint:

```http
POST /api/checkin/{attendeeId}
```

Example:

```bash
curl -i -X POST \
  http://localhost:8081/api/checkin/A001
```

A successful request returns a pending status:

```json
{
  "attendeeId": "A001",
  "name": "Jacob Muema",
  "status": "PENDING"
}
```

![Check-In Request](https://github.com/user-attachments/assets/403aaa39-cfb8-4432-9094-503ee9c48991)

The API does not wait for the printer to complete its work.

Instead, it:

1. Validates the attendee.
2. Changes the state to `PENDING`.
3. Generates a print job ID.
4. Publishes a `PrintRequest` to RabbitMQ.
5. Returns the current attendee state.

---

# Message Queue

The print request is published to the RabbitMQ `print-requests` queue.

```text
Spring Boot
     |
     | PrintRequest
     v
RabbitMQ
     |
     v
print-requests
```

The queue decouples the check-in service from the badge printer.

---

# Simulated Badge Printer

For local development, the project includes a simulated badge-printer consumer.

The simulated printer consumes messages from RabbitMQ.

![Simulated Badge Printer](https://github.com/user-attachments/assets/9a68f41d-27e7-4978-a20c-7b9ed78b8cfd)

A typical processing flow is:

```text
PrintRequest received
        |
        v
Read job ID
        |
        v
Read attendee information
        |
        v
Simulate badge printing
        |
        v
Print completed
        |
        v
Send webhook callback
```

In a production deployment, this component would represent the external vendor's printer integration rather than a local simulated consumer.

---

# Print Completion Webhook

The printer sends a callback when the badge-printing operation has completed.

Endpoint:

```http
POST /api/webhook/print-completed
```

Example request:

```bash
curl -i -X POST \
  http://localhost:8081/api/webhook/print-completed \
  -H "Content-Type: application/json" \
  -d '{
    "jobId": "example-job-id",
    "attendeeId": "A001"
  }'
```

Example response:

```json
{
  "message": "Print completion received",
  "attendeeId": "A001",
  "status": "CHECKED_IN"
}
```

![Webhook Callback](https://github.com/user-attachments/assets/bb8473ab-ef7e-490f-982c-c5cd717b26c0)

The webhook is the event that moves the attendee from:

```text
PENDING
```

to:

```text
CHECKED_IN
```

---

# Duplicate Scan Protection

The system prevents an attendee who has already been checked in from receiving another badge.

For example:

```text
First Scan
    |
    v
NOT_CHECKED_IN
    |
    v
PENDING
    |
    v
CHECKED_IN
```

If the same attendee is scanned again:

```text
Second Scan
    |
    v
CHECKED_IN
    |
    v
No new print request
```

![Duplicate Scan Protection](https://github.com/user-attachments/assets/955aee69-4030-454b-95cd-614ee61fed3d)

This prevents duplicate badge printing.

---

# End-to-End Workflow

A complete check-in operation follows this sequence:

```text
1. Staff scans attendee QR code
                 |
                 v
2. Check-In API receives attendee ID
                 |
                 v
3. Attendee is validated
                 |
                 v
4. Attendee status becomes PENDING
                 |
                 v
5. PrintRequest is created
                 |
                 v
6. PrintRequest is published to RabbitMQ
                 |
                 v
7. Badge printer consumes the message
                 |
                 v
8. Badge is printed
                 |
                 v
9. Printer sends webhook callback
                 |
                 v
10. Check-In API receives callback
                 |
                 v
11. Attendee status becomes CHECKED_IN
```

---

# Test Data

The application contains three test attendees:

| ID   | Name        | Initial Status |
| ---- | ----------- | -------------- |
| A001 | Jacob Muema | NOT_CHECKED_IN |
| A002 | Jane Doe    | NOT_CHECKED_IN |
| A003 | John Smith  | NOT_CHECKED_IN |

The test scenario covers:

* Multiple attendees
* Successful asynchronous printing
* Pending state
* Webhook completion
* Duplicate scanning

---

# Testing the Complete Flow

## Step 1: Check In A001

```bash
curl -i -X POST \
  http://localhost:8081/api/checkin/A001
```

Expected status:

```text
PENDING
```

---

## Step 2: Verify RabbitMQ

Open:

```text
http://localhost:15672
```

Navigate to:

```text
Queues and Streams
```

Open:

```text
print-requests
```

The print request should appear in the queue.

---

## Step 3: Verify Printer Processing

The application logs should show the simulated badge printer receiving the request:

```text
BADGE PRINTER RECEIVED REQUEST
Attendee ID: A001
Attendee Name: Jacob Muema
Printing badge...

BADGE PRINTED SUCCESSFULLY
```

---

## Step 4: Send the Completion Webhook

```bash
curl -i -X POST \
  http://localhost:8081/api/webhook/print-completed \
  -H "Content-Type: application/json" \
  -d '{
    "jobId": "YOUR-JOB-ID",
    "attendeeId": "A001"
  }'
```

The attendee should now be:

```text
CHECKED_IN
```

---

## Step 5: Test Duplicate Scanning

Scan A001 again:

```bash
curl -i -X POST \
  http://localhost:8081/api/checkin/A001
```

The application should identify the attendee as already checked in and avoid creating another print request.

---

# Observability

The application logs important events throughout the asynchronous workflow.

Example:

```text
Print request sent for attendee: A001
Attendee A001 is now PENDING.
```

The simulated printer then logs:

```text
BADGE PRINTER RECEIVED REQUEST
Job ID: ...
Attendee ID: A001
Attendee Name: Jacob Muema
Printing badge...
```

After completion:

```text
BADGE PRINTED SUCCESSFULLY
```

The webhook then records:

```text
PRINT COMPLETION WEBHOOK RECEIVED
Job ID: ...
Attendee ID: A001
```

This provides visibility across the complete message-driven workflow.

---

# Synchronous vs Asynchronous Design

## Previous Synchronous Design

```text
Kiosk
  |
  v
Check-In Service
  |
  v
Printer REST API
  |
  | BLOCKING WAIT
  |
  v
Print Success
  |
  v
CHECKED_IN
```

The check-in service is directly dependent on the printer's immediate response.

## Current Asynchronous Design

```text
Kiosk
  |
  v
Check-In Service
  |
  v
RabbitMQ
  |
  v
Badge Printer

Check-In Service
  |
  v
PENDING

Later:

Badge Printer
  |
  v
Webhook
  |
  v
Check-In Service
  |
  v
CHECKED_IN
```

The asynchronous design allows the check-in service and badge printer to operate independently.

---

# Design Considerations

## Decoupling

RabbitMQ separates the check-in service from the badge-printer consumer.

The check-in service only needs to successfully publish a print request rather than waiting for the printer to finish.

## Asynchronous State

The `PENDING` state provides an explicit representation of an in-progress printing operation.

This prevents the system from incorrectly reporting an attendee as checked in before the badge has actually been printed.

## Duplicate Protection

The attendee state is checked before a new print request is generated.

An attendee already in `CHECKED_IN` state cannot trigger another badge-print request.

## Webhook-Based Completion

The webhook allows the printer to notify the check-in service when the asynchronous operation has completed.

---

# Development

Build the project:

```bash
mvn clean package
```

Run tests:

```bash
mvn test
```

Start the application:

```bash
mvn spring-boot:run
```

Stop the RabbitMQ container:

```bash
docker stop solstice-rabbitmq
```

Start it again:

```bash
docker start solstice-rabbitmq
```

Remove the container:

```bash
docker rm -f solstice-rabbitmq
```

---

# Future Improvements

For a production implementation, the following improvements would be considered:

* Persistent attendee storage using PostgreSQL or MySQL
* Redis or database-backed idempotency
* Persistent print-job records
* Authentication and authorization for webhook requests
* Webhook signature verification
* Retry handling for failed messages
* Dead-letter queues
* Message TTL configuration
* Distributed tracing
* Structured logging
* Metrics and monitoring
* Horizontal scaling of consumers
* Production-grade external printer integration
* Transactional state management

---

# Project Evidence

The following screenshots demonstrate the implemented workflow.

### Project Structure

![Project Structure](https://github.com/user-attachments/assets/bb70ab5a-67c8-4727-bd86-87c4104ec1b1)

### Application Running

![Application Running](https://github.com/user-attachments/assets/bd348a60-834e-4f30-9b75-bfadf6d04f51)

### RabbitMQ Queue

![RabbitMQ Queue](https://github.com/user-attachments/assets/08a34ade-adde-40a3-b101-839a94f3ada0)

### Check-In Request

![Check-In Request](https://github.com/user-attachments/assets/403aaa39-cfb8-4432-9094-503ee9c48991)

### Simulated Badge Printer

![Simulated Badge Printer](https://github.com/user-attachments/assets/9a68f41d-27e7-4978-a20c-7b9ed78b8cfd)

### Webhook Callback

![Webhook Callback](https://github.com/user-attachments/assets/bb8473ab-ef7e-490f-982c-c5cd717b26c0)

### Duplicate Scan Protection

![Duplicate Scan Protection](https://github.com/user-attachments/assets/955aee69-4030-454b-95cd-614ee61fed3d)

---


```

