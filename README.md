# Smart Campus: Sensor & Room Management API
### Student Name: Emith Jayasuriya
### Institution: Informatics Institute of Technology (IIT) / University of Westminster
### Tech Stack: JAX-RS / Jersey, Grizzly Embedded Server, Maven

### 1. Project Overview

This project implements a RESTful web service for managing campus infrastructure,
specifically rooms and their associated sensors. The system is built using JAX-RS (Jersey
implementation) and runs on an embedded Grizzly HTTP server. In compliance with the
coursework requirements, it uses an in-memory data store without any external database
dependencies.

### 2. Technical Analysis & Conceptual Report
#### 2.1 JAX-RS Lifecycle & Data Integrity
<p align="justify"> <b>Technical Analysis:</b> In JAX-RS, resource classes (such as RoomResource) are request-scoped by default.
This means the server instantiates the class for every incoming request and discards it after the response is sent. <br>
<p align="justify"> <b>Implementation Strategy:</b> Since we are restricted from using external databases, data persistence is managed in a dedicated DataStore class. 
To prevent data loss during the request-response cycle, all resource collections are stored as static members. To handle concurrent access from multiple sensors or users, I utilized ConcurrentHashMap and CopyOnWriteArrayList. 
These thread-safe structures ensure referential integrity and prevent race conditions without the performance overhead of manual synchronized blocks.</p>

#### 2.2 HATEOAS & The Discovery Endpoint
<p align="justify"> <b>Justification:</b> The discovery endpoint at /api/v1 implements HATEOAS (Hypermedia as the Engine of Application State). 
By providing a JSON map of resource links (e.g., /rooms, /sensors), the API becomes self-documenting. 
This is superior to static documentation as it allows client-side developers to discover and navigate the API structure dynamically, reducing coupling between the client and specific URI paths.</p>

#### 2.3 Sub-Resource Locator Pattern
<p align="justify"> <b>Benefit:</b> The sensor reading history is implemented using the Sub-Resource Locator pattern via the /sensors/{id}/read path. 
This architectural choice promotes Separation of Concerns. The SensorResource class focuses on sensor metadata and inventory, while the logic for historical time-series data is delegated to SensorReadingResource. 
This keeps the codebase modular and maintainable as the API complexity grows.</p>

#### 2.4 Semantic Error Handling (422 vs 404)
<p align="justify"> <b>Justification:</b> When a client attempts to register a sensor to a roomId that does not exist, the API returns a 422 Unprocessable Entity status code. </p>
<ol> 404 Not Found is avoided here because it implies the endpoint (the URL) does not exist. </ol>
<ol> 422 is used because the JSON payload is syntactically correct, but it violates business logic constraints (referential integrity). This provides more precise feedback to the developer. </ol>

#### 2.5 Cybersecurity: Technical Information Disclosure
<p align="justify"> <b>Analysis:</b> A significant security risk in web services is the exposure of raw Java stack traces. These traces reveal internal file structures, class names, and library versions (e.g., Jersey 2.35), which attackers use to identify specific vulnerabilities. <br>
<p align="justify"> <b>The Safety Net:</b> I implemented a GlobalExceptionMapper<Throwable> which acts as a <b>Global Safety Net</b>. It intercepts all unexpected errors and returns a sanitized JSON body. This ensures that no internal server details are leaked, maintaining the security posture of the campus system.

### 3. Endpoints Documentation
| **Method** | **Endpoint**                | **Description**                              |
| ---------- | --------------------------- | -------------------------------------------- |
| **GET**    | `/api/v1`                   | Service Discovery & Metadata                 |
| **GET**    | `/api/v1/rooms`             | Retrieve all campus rooms                    |
| **POST**   | `/api/v1/rooms`             | Register a new room                          |
| **DELETE** | `/api/v1/rooms/{id}`        | Remove a room (Fails if sensors are present) |
| **GET**    | `/api/v1/sensors`           | List sensors (Filterable via `?type=`)       |
| **POST**   | `/api/v1/sensors/{id}/read` | Append a new sensor reading                  |

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
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id": "L3-01", "name": "Main Lab", "capacity": 50}'
```

#### 3. Test Deletion Conflict — `409 Conflict`
 
Assign a sensor to a room, then attempt to delete it:
 
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/L3-01
```
 
**Expected:** `409 Conflict` with a JSON error message.

#### 4. Test Security Safety Net — `500` with No Stack Trace
 
```bash
curl -X GET http://localhost:8080/api/v1/crash
```
 
**Expected:** `500 Internal Server Error` with a sanitised JSON body and **no stack trace**.
