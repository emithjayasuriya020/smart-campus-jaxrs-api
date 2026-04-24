# Smart Campus: Sensor & Room Management API
### Student Name: Emith Jayasuriya
### Student ID: w2120207/20231293
### Institution: Informatics Institute of Technology (IIT) / University of Westminster
### Tech Stack: JAX-RS / Jersey, Grizzly Embedded Server, Maven

### 1. Project Overview

This project implements a RESTful web service for managing campus infrastructure,
specifically rooms and their associated sensors. The system is built using JAX-RS (Jersey
implementation) and runs on an embedded Grizzly HTTP server. In compliance with the
coursework requirements, it uses an in-memory data store without any external database
dependencies.

### 2. Technical Analysis & Conceptual Report
## Part 1: Service Architecture
 
- **JAX-RS Lifecycle:** Resources are request-scoped by default (created per request). Static `ConcurrentHashMap`s in a `DataStore` class ensure data persists across requests and prevent race conditions during concurrent access.
- **HATEOAS Justification:** The `/api/v1` discovery endpoint uses HATEOAS to make the API self-documenting. This reduces client-side hardcoding, allowing the client to discover resource URIs dynamically as the system evolves.
---
 
## Part 2: Room Management
 
- **Returning IDs vs. Full Objects:** Returning only IDs reduces network bandwidth and payload size. However, returning full objects (as done here) is more developer-friendly because it minimises the number of subsequent API calls the client must make to get details.
- **DELETE Idempotency:** The `DELETE` implementation is idempotent. The first call removes the room (`204`), and subsequent calls for the same ID return `404`. Since the final state of the server is the same (room is gone), it satisfies the definition of idempotency.
---
 
## Part 3: Sensor Operations
 
- **`@Consumes` Mismatch:** If a client sends `text/plain` instead of `application/json`, the JAX-RS runtime will automatically return an HTTP `415 Unsupported Media Type` error because the media type does not match the `@Consumes` annotation.
- **Filtering Approach:** `@QueryParam` (e.g., `?type=CO2`) is used for filtering instead of path variables because query parameters are the semantic standard for refining a collection, whereas path variables are used to identify a specific resource.
---
 
## Part 4: Sub-Resource Locators
 
- **Complexity Management:** Delegating readings to `SensorReadingResource` using the Locator Pattern (`{sensorId}/readings`) implements Separation of Concerns. This prevents a single resource class from becoming a "God Object" and allows for cleaner code organisation.
---
 
## Part 5: Error Handling & Observability
 
- **Semantic Choice (422 vs 404):** `422 Unprocessable Entity` is returned when a sensor's `roomId` reference is invalid. This is superior to `404` because `404` implies the endpoint is missing, while `422` indicates the syntax is correct but the business logic/referential integrity is broken.
- **Logging Filters:** Using a single `LoggingFilter` for request/response logging is better than manual logging because it handles cross-cutting concerns. It ensures consistent logging across the entire API without duplicating code in every method.
- **Cybersecurity Risk:** Exposing raw stack traces allows attackers to identify frameworks, versions, and internal paths for targeted exploitation, identifying library versions (Jersey 2.x) and internal file paths. The `GlobalExceptionMapper` mitigates this Information Disclosure risk by returning sanitised JSON messages.


### 3. Endpoints Documentation
| Method   | Endpoint                        | Description                              |
|----------|---------------------------------|------------------------------------------|
| `GET`    | `/api/v1`                       | Discovery & HATEOAS Links                |
| `GET`    | `/api/v1/rooms`                 | List all rooms                           |
| `POST`   | `/api/v1/rooms`                 | Create a new room                        |
| `GET`    | `/api/v1/rooms/{roomId}`        | Get details of one room                  |
| `DELETE` | `/api/v1/rooms/{roomId}`        | Delete room (only if empty)              |
| `POST`   | `/api/v1/sensors`               | Register a new sensor                    |
| `GET`    | `/api/v1/sensors?type=X`        | List sensors (with optional filter)      |
| `GET`    | `/api/v1/sensors/{id}/readings` | Get reading history                      |
| `POST`   | `/api/v1/sensors/{id}/readings` | Add reading (fails if `MAINTENANCE`)     |

### 4. Setup and Execution
 #### 1. <p align="justify"><b>Prerequisites:</b> Ensure you have JDK 17 (or 25) and Maven installed. </b></p>
 #### 2. <p align="justify"><b>Build:</b> Run mvn clean install in the project root. </b></p>
 #### 3. <p align="justify"><b>Run:</b> Execute the Main.java class. </b></p>
 #### 4. <p align="justify"><b>Access:</b> The API will be available at http://localhost:8080/api/v1. </b></p>

### 5. Testing Guide (Sample Commands)
#### 1. Service Discovery

```bash
curl -X GET http://localhost:8080/api/v1
```
#### 2. Create a Room
 
```bash
curl -X POST http://localhost:8080/api/v1/rooms
  -H "Content-Type: application/json"
  -d '{"id":"L-1", "name":"Lab 1", "capacity":30}'
```
#### 3. Register Sensor

```bash
curl -X POST http://localhost:8080/api/v1/sensors
  -H "Content-Type: application/json"
  -d '{"id":"S-1", "type":"Temp", "roomId":"L-1", "status":"ACTIVE"}'
```

#### 4. Add Sensor reading (fails if `MAINTENANCE`)

```bash
curl -X POST http://localhost:8080/api/v1/sensors/S-1/readings
  -H "Content-Type: application/json"
  -d '{"value":25.0}'
```

#### 5. Get reading history
 
```bash
curl -X GET http://localhost:8080/api/v1/sensors/S-1/readings
```

#### 6. List sensors (with optional filter)
 
```bash
curl -X GET http://localhost:8080/api/v1/sensors?type=Temp
```

#### 7. Test 403 Forbidden:

Set sensor status to MAINTENANCE, then try to POST a reading.

```bash
curl -X POST http://localhost:8080/api/v1/sensors/S-1/readings \
  -H "Content-Type: application/json" \
  -d '{"value":30.0}'
```

#### 8. Test 409 Conflict (Room Deletion Safety)

Attempt to delete room L-1 while sensor S-1 is still assigned to it:

```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/L-1
```

Expected: 409 Conflict JSON error body.

#### 9. Test 422 Unprocessable Entity (Invalid Reference)

Attempt to register a sensor to a room ID that does not exist:

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"S-99","type":"CO2","roomId":"INVALID_ID","status":"ACTIVE"}'
```

Expected: 422 Unprocessable Entity JSON error body.

#### 10. Test 500 Safety Net (Security Check)

```bash
curl -X GET http://localhost:8080/api/v1/crash
```

Expected: 500 Internal Server Error with a sanitized JSON message and no stack trace.
