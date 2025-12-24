# VaultX Hyperledger Microservice

A Spring Boot microservice that bridges event-driven messaging (Kafka) with Hyperledger Fabric blockchain for immutable DID (Decentralized Identity) event storage. This service acts as the blockchain adapter layer for the VaultX security platform, ensuring all identity-related events are cryptographically hashed and persisted to a distributed ledger.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Core Components](#core-components)
  - [Kafka Consumer](#kafka-consumer)
  - [Apache Camel Routes](#apache-camel-routes)
  - [DID Event Processor](#did-event-processor)
  - [Chaincode Service](#chaincode-service)
  - [REST Controller](#rest-controller)
  - [Hyperledger Configuration](#hyperledger-configuration)
- [Chaincode (Smart Contract)](#chaincode-smart-contract)
- [Data Models](#data-models)
- [Request Flows](#request-flows)
  - [Kafka Event Ingestion Flow](#kafka-event-ingestion-flow)
  - [REST API Query Flow](#rest-api-query-flow)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Deployment](#deployment)
- [Technology Stack](#technology-stack)

---

## Overview

The **Hyperledger Microservice** is a critical component of the VaultX ecosystem responsible for:

1. **Consuming DID-related events** from multiple Kafka topics (user registration, key rotation, role changes, chat events)
2. **Processing and enriching events** with SHA-256 payload hashes
3. **Persisting events** immutably to a Hyperledger Fabric blockchain network
4. **Exposing REST APIs** for querying events and their on-chain history

The service uses Apache Camel for message routing and the Hyperledger Fabric Gateway SDK for blockchain interactions.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              VaultX Platform                                     │
└─────────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              Kafka Cluster                                       │
│  ┌─────────────────┐ ┌──────────────────┐ ┌───────────────┐ ┌────────────────┐  │
│  │ blockchain.     │ │ users.           │ │ users.        │ │ chats.events   │  │
│  │ transactions    │ │ registration     │ │ key-rotation  │ │                │  │
│  └────────┬────────┘ └────────┬─────────┘ └───────┬───────┘ └───────┬────────┘  │
└───────────┼───────────────────┼───────────────────┼─────────────────┼───────────┘
            │                   │                   │                 │
            └───────────────────┴───────────────────┴─────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                         Hyperledger Microservice                                 │
│                                                                                  │
│  ┌──────────────────┐    ┌──────────────────┐    ┌──────────────────────────┐   │
│  │  KafkaConsumer   │───▶│  DIDEventRoute   │───▶│   DIDEventProcessor      │   │
│  │  (Spring Kafka)  │    │  (Apache Camel)  │    │   • Parse JSON           │   │
│  └──────────────────┘    └──────────────────┘    │   • Generate eventId     │   │
│                                                   │   • Hash payload (SHA256)│   │
│                                                   └───────────┬──────────────┘   │
│                                                               │                  │
│                                                               ▼                  │
│  ┌──────────────────────────────────────────────────────────────────────────┐   │
│  │                      EventChainCodeService                                │   │
│  │   • saveEvent()  • getEvent()  • getEventsByUser()  • getEventHistory()  │   │
│  └──────────────────────────────────────────────────────────────────────────┘   │
│                                        │                                         │
│  ┌─────────────────────────────────────┼────────────────────────────────────┐   │
│  │          REST API (EventChainCodeController)                              │   │
│  │   POST /api/chaincode/createEvent                                         │   │
│  │   GET  /api/chaincode/queryEvent                                          │   │
│  │   GET  /api/chaincode/queryEventsByUser                                   │   │
│  │   GET  /api/chaincode/queryHistory                                        │   │
│  └───────────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                       Hyperledger Fabric Network                                 │
│  ┌────────────────────────────────────────────────────────────────────────┐     │
│  │                     VaultX Channel (vaultx-channel)                     │     │
│  │  ┌─────────────────────┐         ┌─────────────────────┐               │     │
│  │  │ peer0.vaultx.       │         │ peer1.vaultx.       │               │     │
│  │  │ example.com:7041    │◀───────▶│ example.com:7042    │               │     │
│  │  │ (vaultx-event)      │         │ (vaultx-event)      │               │     │
│  │  └─────────────────────┘         └─────────────────────┘               │     │
│  └────────────────────────────────────────────────────────────────────────┘     │
│  ┌───────────────────────────────────────────────────────────────────────────┐  │
│  │               Certificate Authority: ca.vaultx.example.com:7054           │  │
│  └───────────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────────┘
```

---

## Project Structure

```
hyperledger-ms/
├── blockchain/
│   └── chaincode/
│       └── vaultx-event/                    # Hyperledger Fabric chaincode (Node.js)
│           ├── index.js                     # Chaincode entry point
│           ├── package.json                 # Node.js dependencies
│           └── lib/
│               ├── eventContract.js         # Smart contract implementation
│               └── eventModel.js            # Event data model for world state
├── src/
│   ├── main/
│   │   ├── java/com/vaultx/hyperledger/
│   │   │   ├── HyperledgerMsApplication.java    # Spring Boot entry point
│   │   │   ├── config/
│   │   │   │   ├── HyperledgerConfig.java       # Fabric Gateway configuration
│   │   │   │   └── SwaggerConfig.java           # OpenAPI documentation config
│   │   │   ├── controller/
│   │   │   │   └── EventChainCodeController.java # REST API endpoints
│   │   │   ├── kafka/
│   │   │   │   └── KafkaConsumer.java           # Kafka topic listeners
│   │   │   ├── model/
│   │   │   │   ├── DIDEvent.java                # DID event domain model
│   │   │   │   ├── EventHistory.java            # Event history model
│   │   │   │   └── EventType.java               # Event type enumeration
│   │   │   ├── resources/
│   │   │   │   └── DIDEventRoute.java           # Apache Camel route definition
│   │   │   └── service/
│   │   │       ├── DIDEventProcessor.java       # Event processing logic
│   │   │       └── EventChainCodeService.java   # Blockchain interaction layer
│   │   └── resources/
│   │       ├── application-dev.properties       # Development configuration
│   │       ├── application-test.properties      # Test/production configuration
│   │       └── fabric/
│   │           ├── ca.vaultx.example.com-cert.pem
│   │           └── connection-profile-vaultx.yaml
│   └── test/
├── docker-compose.yaml                          # Docker orchestration
├── Dockerfile                                   # Multi-stage build
└── pom.xml                                      # Maven dependencies
```

---

## Core Components

### Kafka Consumer

**Class:** `KafkaConsumer.java`

The Kafka consumer subscribes to five distinct topics, each representing different event sources:

| Topic | Purpose |
|-------|---------|
| `blockchain.transactions` | General blockchain transaction events |
| `users.registration` | New user registration events |
| `users.key-rotation` | Public key rotation events |
| `users.role-change` | User role modification events |
| `chats.events` | Chat creation and messaging events |

Each listener delegates message processing to Apache Camel via `ProducerTemplate`, attaching an `eventSource` header to identify the origin:

```java
producerTemplate.sendBodyAndHeader(
    "direct:processDocumentMetadata", 
    message, 
    "eventSource", 
    eventSource
);
```

---

### Apache Camel Routes

**Class:** `DIDEventRoute.java`

A simple Camel route that acts as the integration backbone:

```java
from("direct:processDocumentMetadata")
    .process(DIDEventProcessor);
```

This route receives messages from the Kafka consumer and routes them through the `DIDEventProcessor` for enrichment and persistence.

---

### DID Event Processor

**Class:** `DIDEventProcessor.java`

Implements `org.apache.camel.Processor` and handles the core event processing logic:

1. **Deserialize** the incoming JSON message into a `DIDEvent` object
2. **Extract Kafka metadata** (offset) from message headers
3. **Generate event ID** (UUID) if not present
4. **Compute payload hash** using SHA-256 if not provided
5. **Set Kafka offset** for audit trail
6. **Delegate to chaincode service** for blockchain persistence

```java
DIDEvent event = objectMapper.readValue(message, DIDEvent.class);

if (event.getEventId() == null) {
    event.setEventId(UUID.randomUUID());
}
if (event.getPayloadHash() == null) {
    event.setPayloadHash(DigestUtils.sha256Hex(event.getPayload()));
}

event.setKafkaOffset(kafkaOffset);
chainCodeService.saveEvent(event);
```

---

### Chaincode Service

**Class:** `EventChainCodeService.java`

The blockchain interaction layer that wraps Hyperledger Fabric Gateway SDK operations:

| Method | Chaincode Function | Description |
|--------|-------------------|-------------|
| `saveEvent(DIDEvent)` | `createEvent` | Submit transaction to create a new event |
| `getEvent(UUID)` | `queryEvent` | Evaluate transaction to fetch event by ID |
| `getEventsByUser(UUID)` | `queryEventsByUser` | Rich query to find all events for a user |
| `getEventHistory(UUID)` | `queryHistory` | Retrieve the modification history of an event |

**Transaction submission** (writes) vs **transaction evaluation** (reads):
- `submitTransaction()` - Writes to the ledger, requires endorsement and ordering
- `evaluateTransaction()` - Read-only query against world state

---

### REST Controller

**Class:** `EventChainCodeController.java`

Exposes HTTP endpoints for direct blockchain interaction:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/chaincode/createEvent` | POST | Persist a new DID event |
| `/api/chaincode/queryEvent?eventId={uuid}` | GET | Retrieve single event |
| `/api/chaincode/queryEventsByUser?userId={uuid}` | GET | Get all events for a user |
| `/api/chaincode/queryHistory?eventId={uuid}` | GET | Get event modification history |

---

### Hyperledger Configuration

**Class:** `HyperledgerConfig.java`

Bootstraps the Hyperledger Fabric Gateway connection:

1. **Initialize CA Client** - Connects to `ca.vaultx.example.com` using TLS
2. **Enroll Admin** - Authenticates with CA credentials
3. **Create In-Memory Wallet** - Stores the X.509 identity
4. **Build Gateway** - Connects to the Fabric network using the connection profile
5. **Expose Beans** - `Gateway`, `Network`, and `Contract` for dependency injection

```java
@Bean
public Contract eventContract(Network network) {
    return network.getContract(contractName);  // "vaultx-event"
}
```

---

## Chaincode (Smart Contract)

**Location:** `blockchain/chaincode/vaultx-event/`

A Node.js-based Hyperledger Fabric chaincode implementing `fabric-contract-api`:

### Functions

| Function | Parameters | Description |
|----------|------------|-------------|
| `createEvent` | eventId, userId, publicKey, eventType, payload, payloadHash, kafkaOffset | Creates new event in world state |
| `queryEvent` | eventId | Returns event JSON by ID |
| `queryEventsByUser` | userId | CouchDB rich query by user ID |
| `queryHistory` | eventId | Returns ledger history for key |

### Event Model (World State)

```json
{
    "eventId": "uuid",
    "userId": "uuid",
    "publicKey": "base64-encoded-key",
    "eventType": "USER_REGISTERED",
    "payload": "json-string",
    "payloadHash": "sha256-hex",
    "kafkaOffset": 12345,
    "timestamp": "2025-07-05T12:00:00.000Z",
    "docType": "vaultxEvent"
}
```

---

## Data Models

### DIDEvent (Java)

```java
public class DIDEvent {
    private UUID eventId;        // Unique identifier (ledger key)
    private UUID userId;         // User who triggered the event
    private String publicKey;    // User's public key at event time
    private EventType eventType; // Type classification
    private Instant timestamp;   // When event was recorded
    private String payload;      // JSON event data
    private long kafkaOffset;    // Kafka message offset (audit)
    private String payloadHash;  // SHA-256 hash of payload
    private String docType;      // Document type for CouchDB queries
}
```

### EventType (Enum)

| Value | Description |
|-------|-------------|
| `USER_REGISTERED` | New user account creation |
| `USER_KEY_ROTATED` | Public key rotation event |
| `USER_ROLE_CHANGED` | Permission/role modification |
| `CHAT_CREATED` | New chat room/conversation |
| `FILE_UPLOAD` | File upload event |

### EventHistory

Represents a single entry in the ledger history:

```java
public class EventHistory {
    private String txId;       // Hyperledger transaction ID
    private String timestamp;  // When modification occurred
    private boolean isDelete;  // Whether this was a deletion
    private String value;      // State value at this point
}
```

---

## Request Flows

### Kafka Event Ingestion Flow

```
1. External Service publishes to Kafka topic (e.g., users.registration)
                    │
                    ▼
2. KafkaConsumer.consumeUserRegistrationEvents(message)
   └── Attaches eventSource="userRegistration" header
                    │
                    ▼
3. ProducerTemplate sends to "direct:processDocumentMetadata" (Camel)
                    │
                    ▼
4. DIDEventRoute forwards to DIDEventProcessor
                    │
                    ▼
5. DIDEventProcessor.process(exchange)
   ├── Deserialize JSON → DIDEvent
   ├── Extract kafka.OFFSET header
   ├── Generate UUID if eventId is null
   ├── Compute SHA-256 payloadHash if null
   └── Set kafkaOffset
                    │
                    ▼
6. EventChainCodeService.saveEvent(event)
   └── Contract.submitTransaction("createEvent", ...)
                    │
                    ▼
7. Hyperledger Fabric Network
   ├── Endorsement (peer0, peer1)
   ├── Ordering service
   └── Commit to world state
```

### REST API Query Flow

```
1. Client: GET /api/chaincode/queryEvent?eventId=<uuid>
                    │
                    ▼
2. EventChainCodeController.queryEvent(eventId)
                    │
                    ▼
3. EventChainCodeService.getEvent(eventId)
   └── Contract.evaluateTransaction("queryEvent", eventId)
                    │
                    ▼
4. Chaincode: VaultxEventContract.queryEvent(ctx, eventId)
   └── ctx.stub.getState(eventId)
                    │
                    ▼
5. Returns JSON → Deserialized to DIDEvent → ResponseEntity.ok(event)
```

---

## Configuration

### Application Properties

| Property | Description | Example |
|----------|-------------|---------|
| `fabric.ca.url` | Certificate Authority URL | `https://ca.vaultx.example.com:7054` |
| `fabric.ca.admin.name` | CA admin username | `admin` |
| `fabric.ca.admin.secret` | CA admin password | `adminpw` |
| `fabric.msp.id` | Membership Service Provider ID | `VaultXMSP` |
| `fabric.channel-name` | Fabric channel name | `vaultx-channel` |
| `fabric.contract-name` | Deployed chaincode name | `vaultx-event` |
| `fabric.network.config` | Connection profile path | `classpath:fabric/connection-profile-vaultx.yaml` |

### Environment Variables (Docker)

| Variable | Description |
|----------|-------------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile (`test` or `dev`) |
| `KAFKA_SERVER` | Kafka bootstrap server address |
| `OTEL_SDK_DISABLED` | Disable OpenTelemetry (set to `true`) |

---

## API Documentation

OpenAPI/Swagger documentation is available at:

```
http://localhost:8082/swagger-ui.html
```

Configured via `SwaggerConfig.java` with:
- **Title:** Hyperledger Microservice API
- **Version:** 1.0
- **License:** Apache 2.0

---

## Deployment

### Docker Build

```bash
# Build and run
docker-compose up --build

# The service will:
# - Build using Maven (multi-stage)
# - Use amazoncorretto:21 runtime
# - Connect to Fablo network (fablo_network_202507051259_basic)
# - Expose port 8082
```

### Network Requirements

The service must be able to reach:
- **Kafka broker** (configured in properties)
- **Hyperledger CA** (`ca.vaultx.example.com:7054`)
- **Hyperledger Peers** (`peer0.vaultx.example.com:7041`, `peer1.vaultx.example.com:7042`)

The Docker Compose configuration joins the existing Fablo network and sets up DNS aliases for the Fabric nodes.

### Local Development

```bash
# Using Maven
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Or with Docker
docker build -t hyperledger-ms .
docker run -p 8082:8082 --network fablo_network_202507051259_basic hyperledger-ms
```

---

## Technology Stack

| Layer | Technology | Version |
|-------|------------|---------|
| **Language** | Java | 21 |
| **Framework** | Spring Boot | 3.4.4 |
| **Messaging** | Spring Kafka | (managed) |
| **Integration** | Apache Camel | 4.11.0 |
| **Blockchain SDK** | Hyperledger Fabric Gateway | 2.2.9 |
| **Blockchain SDK** | Hyperledger Fabric SDK Java | 2.2.26 |
| **Cryptography** | BouncyCastle | 1.79 |
| **API Docs** | SpringDoc OpenAPI | 2.5.0 |
| **Build Tool** | Maven | 3.9.x |
| **Chaincode Runtime** | Node.js | ≥14 |
| **Chaincode API** | fabric-contract-api | 2.5.0 |

---

## License

Apache 2.0

---

## Related Resources

- [Hyperledger Fabric Documentation](https://hyperledger-fabric.readthedocs.io/)
- [Apache Camel Spring Boot](https://camel.apache.org/camel-spring-boot/latest/)
- [Spring Kafka Reference](https://docs.spring.io/spring-kafka/reference/)

