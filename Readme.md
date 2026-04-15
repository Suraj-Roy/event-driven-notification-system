# Event-Driven Notification System

A sophisticated, microservices-based notification system built with **Spring Boot** and **Apache Kafka** that demonstrates modern event-driven architecture patterns. This system provides reliable, scalable, and traceable notification delivery across multiple channels including email and push notifications.


## Architecture Overview

```mermaid
graph LR
    %% Main Flow
    Client --> API[API Gateway]
    API -->|notification-request| KafkaReq[Kafka Topic]
    KafkaReq --> Router[Notification Router]
    
    %% Routing
    Router -->|notification-email| KafkaEmail[Kafka Topic]
    Router -->|notification-push| KafkaPush[Kafka Topic]
    
    %% Processing
    KafkaEmail --> Email[Email Service]
    KafkaPush --> Push[Push Service]
    
    %% Success Scenarios
    Email -->|Success| EmailProvider[Email Provider]
    Push -->|Success| PushProvider[Push Provider]
    
    %% Failure Scenarios (DLT)
    Router -.->|Failed| DLTTopic[.DLT Topics]
    Email -.->|Failed| DLTTopic
    Push -.->|Failed| DLTTopic
    
    DLTTopic --> DLT[DLT Monitor]
```

## Core Concepts

### Event-Driven Architecture
- **Decoupled Communication**: Services communicate through events rather than direct calls
- **Asynchronous Processing**: Non-blocking message processing improves system responsiveness
- **Scalability**: Each service can scale independently based on load

### Kafka Core Concepts Used
- **Topics**: Messages are categorized into topics (`notification-request`, `notification-email`, `notification-push`)
- **Partitions**: Each topic has 3 partitions for parallel processing and load distribution
- **Consumer Groups**: Different services use different group IDs (`router-group`, `email-service-group`, `push-service-group`)
- **Message Keys**: Using `userId` as key ensures all messages for a user go to the same partition (ordering guarantee)
- **Dead Letter Queues**: Failed messages are routed to `.DLT` topics for monitoring

### Idempotency
- **Duplicate Prevention**: Redis-based caching prevents duplicate message processing
- **Message Deduplication**: Each message is processed only once regardless of retries
- **Consistency Guarantees**: Ensures reliable notification delivery without spam

### Distributed Tracing
- **Correlation IDs**: Unique identifiers track requests across all services
- **MDC (Mapped Diagnostic Context)**: Enhanced logging with request context
- **End-to-End Visibility**: Complete observability of message flow through the system

## Kafka Commands & Operations

### Topic Management
```bash
# List all topics
kafka-topics.sh --bootstrap-server localhost:9092 --list

# Describe topic configuration
kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic notification-request

# Create topic manually (if needed)
kafka-topics.sh --bootstrap-server localhost:9092 --create --topic test-topic --partitions 3 --replication-factor 1

# Delete topic
kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic test-topic
```

### Consumer Management
```bash
# List consumer groups
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list

# Describe consumer group details
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group router-group

```

### Message Inspection
```bash
# Consume messages from topic (for debugging)
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic notification-request --from-beginning

# Produce test message
kafka-console-producer.sh --bootstrap-server localhost:9092 --topic notification-request
```

### Monitoring
```bash
# Check broker health
kafka-broker-api-versions.sh --bootstrap-server localhost:9092

# View log files
docker logs kafka
```

## System Components

### API Gateway (`api-gateway`)
**Entry point for all notification requests**

- **RESTful API**: HTTP endpoints for submitting notification requests
- **Request Validation**: Input validation and sanitization
- **Event Publishing**: Converts HTTP requests to Kafka events
- **Response Handling**: Provides immediate acknowledgment to clients

**Key Technologies**: Spring WebMVC, Spring Kafka, Jackson

### Notification Router (`notification-router`)
**Intelligent message routing and distribution**

- **Topic Routing**: Routes messages to appropriate service-specific topics
- **Event Transformation**: Enriches and transforms events for downstream services
- **Error Handling**: Comprehensive error handling with dead letter queue support
- **Load Balancing**: Distributes messages across available consumers

**Key Technologies**: Spring Kafka, Custom Error Handling, MDC Logging

### Email Service (`email-service`)
**Email notification processing and delivery**

- **Email Processing**: Handles email-specific notification logic
- **Idempotency Management**: Redis-based duplicate prevention
- **Template Support**: Supports email templating and personalization
- **Delivery Tracking**: Tracks email delivery status

**Key Technologies**: Spring Data Redis, Spring Kafka, Email Libraries

### Push Service (`push-service`)
**Push notification processing and delivery**

- **Push Processing**: Handles push notification logic for mobile/web clients
- **Device Management**: Manages device tokens and registration
- **Platform Integration**: Integrates with push notification providers (FCM, APNs)
- **Idempotency Management**: Prevents duplicate push notifications

**Key Technologies**: Spring Data Redis, Spring Kafka, Push SDKs

### DLT Monitor (`dlt-monitor`)
**Dead Letter Queue monitoring and alerting**

- **Failed Message Processing**: Monitors messages that failed processing
- **Alert System**: Sends alerts for critical failures
- **Recovery Mechanisms**: Provides tools for message recovery
- **Analytics**: Tracks failure patterns and system health

**Key Technologies**: Spring Kafka, Alerting Systems, Monitoring

### Notification Common (`notification-common`)
**Shared utilities and data structures**

- **Event Models**: Common event definitions and DTOs
- **Constants**: Shared constants for topics, error codes, etc.
- **Utilities**: Common utility functions across services
- **Validation**: Shared validation logic

**Key Technologies**: Jackson, Java 21

## Technology Stack

### Core Framework
- **Spring Boot 4.0.5**: Modern Java application framework
- **Java 21**: Latest Java LTS version with enhanced features
- **Maven**: Dependency management and build automation

### Messaging & Events
- **Apache Kafka 4.2.0**: Distributed streaming platform
- **Spring Kafka**: Kafka integration with Spring
- **JSON Serialization**: Jackson for message serialization

### Data Storage
- **Redis 8.6.2**: In-memory data store for caching and idempotency
- **Spring Data Redis**: Redis integration with Spring

### Monitoring & Observability
- **Prometheus v3.11.2**: Metrics collection and monitoring
- **Grafana**: Visualization and dashboarding
- **Grafana Loki**: Log aggregation and analysis
- **Grafana Tempo**: Distributed tracing
- **Grafana Alloy**: OpenTelemetry collection and processing
- **MinIO**: Object storage for Loki data
- **Spring Boot Actuator**: Application health and metrics
- **OpenTelemetry**: Distributed tracing and observability
- **MDC Logging**: Structured logging with context

### Development Tools
- **Lombok**: Reduces boilerplate code
- **Spring Boot Test**: Comprehensive testing framework
- **Docker**: Containerization and orchestration
- **Jib Maven Plugin**: Container image building

## Quick Start

### Prerequisites
- Java 21+
- Maven 3.8+
- Docker & Docker Compose

### 1. Start Infrastructure Services
```bash
cd infra/default
docker-compose up -d
```

This starts:
- **Kafka** (port 9092) - Message broker
- **Redis** (port 6379) - Cache and idempotency store
- **Prometheus** (port 9090) - Metrics collection
- **Grafana** (port 3000) - Visualization dashboards
- **Grafana Loki** (port 3100) - Log aggregation
- **Grafana Tempo** (ports 4317, 4318, 3200) - Distributed tracing
- **MinIO** (port 9000) - Object storage for Loki
- **Grafana Alloy** (port 12345) - OpenTelemetry collector

### 2. Build All Services
```bash
# Build common library first
cd notification-common
mvn clean install

# Build all services
mvn clean install
```

### 3. Start Services

**Option A: Run locally (development)**
```bash
# Start each service in separate terminals
cd api-gateway && mvn spring-boot:run
cd notification-router && mvn spring-boot:run
cd email-service && mvn spring-boot:run
cd push-service && mvn spring-boot:run
cd dlt-monitor && mvn spring-boot:run
```

**Option B: Run with Docker (production-like)**
```bash
# Build container images first
mvn clean install jib:dockerBuild

# Start all services with docker-compose
cd infra/default
docker-compose up -d api-gateway notification-router email-service push-service dlt-monitor
```

### 4. Send a Test Notification
```bash
curl -X POST http://localhost:8080/api/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "type": "EMAIL",
    "recipient": "user@example.com",
    "subject": "Welcome!",
    "body": "Welcome to our platform!",
    "metadata": {
      "source": "registration",
      "priority": "high"
    }
  }'
```

## Monitoring & Observability

### Access Points
- **Grafana Dashboards**: `http://localhost:3000` (admin/admin)
- **Prometheus Metrics**: `http://localhost:9090`
- **Loki Logs**: `http://localhost:3100`
- **Tempo Traces**: `http://localhost:3200`
- **Alloy Collector**: `http://localhost:12345`
- **Health Checks**: Spring Actuator endpoints at `/actuator/health`

### Key Metrics
- Message throughput per service
- Error rates and failure patterns
- Consumer lag and processing times
- Redis cache hit rates
- JVM performance metrics
- Distributed tracing spans

### Observability Stack Details
- **Loki**: Centralized log aggregation with labels and filtering
- **Tempo**: Distributed tracing with OpenTelemetry integration
- **Alloy**: Collects metrics, logs, and traces from all services
- **MinIO**: Provides S3-compatible storage for Loki data
- **Prometheus**: Scrapes metrics from all Spring Boot Actuator endpoints



## Message Flow

1. **Request Submission**: Client sends HTTP request to API Gateway
2. **Event Creation**: API Gateway validates and creates `NotificationEvent`
3. **Kafka Publishing**: Event is published to `notification-request` topic
4. **Routing**: Notification Router consumes and routes to appropriate topic:
   - `notification-email` for email notifications
   - `notification-push` for push notifications
5. **Service Processing**: Target service processes the notification
6. **Idempotency Check**: Service checks Redis to prevent duplicates
7. **Delivery**: Notification is sent to external service (email provider, push service)
8. **Acknowledgment**: Message is acknowledged in Kafka
9. **Error Handling**: Failed messages go to DLT for monitoring

## Error Handling & Reliability

### Retry Mechanisms
- **Automatic Retries**: Configurable retry policies for transient failures
- **Exponential Backoff**: Prevents system overload during failures
- **Dead Letter Queue**: Failed messages are preserved for analysis

### Failure Monitoring
- **DLT Monitor**: Continuously monitors dead letter queues
- **Alert Integration**: Sends alerts for critical failures
- **Recovery Tools**: Manual and automatic message recovery

### Data Consistency
- **Transactional Processing**: Ensures message processing atomicity
- **Idempotency**: Guarantees exactly-once processing semantics
- **Acknowledgment Management**: Reliable message delivery confirmation

## Key Design Patterns

### Publisher-Subscriber Pattern
- Decouples message producers from consumers
- Enables flexible message routing and filtering
- Supports multiple consumers per message type

## Kafka Error Handling & Retry Patterns

### Exponential Backoff Strategy
```java
// From KafkaErrorConfig.java
ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0); // 1s → 2s → 4s
backOff.setMaxElapsedTime(15000L); // Max 15 seconds total retry time
```

### Dead Letter Queue (DLQ) Pattern
- **Automatic DLT Creation**: Failed messages are automatically routed to `.DLT` topics
- **Non-Retryable Exceptions**: `DeserializationException` and `IllegalArgumentException` skip directly to DLT
- **DLT Monitoring**: `dlt-monitor` service watches `notification-email.DLT` and `notification-push.DLT`

### Manual Acknowledgment Pattern
```java
@KafkaListener(topics = KafkaTopics.NOTIFICATION_REQUEST, groupId = "router-group")
public void consume(NotificationEvent event, Acknowledgment ack) {
    try {
        // Process message
        routingProducer.route(event);
        ack.acknowledge(); // Manual acknowledgment
    } catch (Exception e) {
        // Will trigger retry mechanism
        throw e;
    }
}
```

### Message Ordering Guarantee
```java
// Using userId as key ensures ordering per user
kafkaTemplate.send(KafkaTopics.NOTIFICATION_REQUEST, event.getUserId(), event)
```

## Kafka Configuration Details

### Topic Configuration
```java
// From KafkaTopicConfig.java
@Bean
public NewTopic requestTopic() {
    return TopicBuilder.name(KafkaTopics.NOTIFICATION_REQUEST)
            .partitions(3)      // 3 partitions for parallel processing
            .replicas(1)        // 1 replica (single broker setup)
            .build();
}
```

### Docker Kafka Setup
```yaml
# From docker-compose.yml
kafka:
  image: apache/kafka:4.2.0
  environment:
    KAFKA_NODE_ID: 1
    KAFKA_PROCESS_ROLES: broker,controller
    KAFKA_AUTO_CREATE_TOPICS_ENABLE: "false"  # Topics created programmatically
```

## Service Endpoints & Ports

### Application Services
- **API Gateway**: `http://localhost:8080` - Main REST API
- **Notification Router**: `http://localhost:8081` - Internal routing service
- **Email Service**: `http://localhost:8082` - Email processing
- **Push Service**: `http://localhost:8083` - Push notification processing
- **DLT Monitor**: `http://localhost:8085` - Dead Letter Queue monitoring

### Infrastructure Services
- **Kafka**: `localhost:9092` - Message broker
- **Redis**: `localhost:6379` - Cache store
- **Prometheus**: `http://localhost:9090` - Metrics
- **Grafana**: `http://localhost:3000` - Dashboards
- **Loki**: `http://localhost:3100` - Logs
- **Tempo**: `http://localhost:3200` - Traces
- **MinIO**: `http://localhost:9000` - Object storage

## Development Guidelines

### Code Structure
```
├── api-gateway/          # REST API entry point
├── notification-router/  # Message routing logic
├── email-service/        # Email processing
├── push-service/         # Push notification processing
├── dlt-monitor/          # Dead Letter Queue monitoring
├── notification-common/  # Shared DTOs and utilities
└── infra/               # Infrastructure configuration
    ├── default/         # Docker Compose setup
    ├── observability/   # Monitoring stack configs
    └── prometheus/      # Prometheus configuration
```

### Building & Deployment
```bash
# Build all services
mvn clean install

# Build Docker images
mvn clean install jib:dockerBuild

```

### Environment Variables
Key environment variables for configuration:
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka broker addresses
- `REDIS_HOST`: Redis server hostname
- `SPRING_PROFILES_ACTIVE`: Active Spring profile
- `OTEL_SERVICE_NAME`: OpenTelemetry service name
- `MANAGEMENT_TRACING_SAMPLING_PROBABILITY`: Tracing sampling rate

## Troubleshooting

### Common Issues

**Kafka Connection Issues**
```bash
# Check Kafka topics
kafka-topics.sh --bootstrap-server localhost:9092 --list

# Check consumer groups
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
```

**Redis Connection Issues**
```bash
# Test Redis connection
redis-cli ping

# Check Redis keys
redis-cli keys "*"
```

**Service Health Checks**
```bash
# Check all service health
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8085/actuator/health
```

**Docker Issues**
```bash
# Check container logs
docker logs kafka
docker logs redis
docker logs api-gateway-ms

# Restart services
docker-compose restart
```

## API Documentation

### Endpoints

#### Send Notification
```http
POST /api/notifications
Content-Type: application/json

{
  "userId": "string",
  "type": "EMAIL|PUSH",
  "recipient": "string",
  "subject": "string (EMAIL only)",
  "body": "string",
  "metadata": {
    "key": "value"
  }
}
```

#### Health Check
```http
GET /actuator/health
```

#### Metrics
```http
GET /actuator/metrics
GET /actuator/prometheus
```

### Example Requests

**Email Notification**
```bash
curl -X POST http://localhost:8080/api/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "type": "EMAIL",
    "recipient": "user@example.com",
    "subject": "Welcome!",
    "body": "Welcome to our platform!",
    "metadata": {
      "source": "registration",
      "priority": "high"
    }
  }'
```

**Push Notification**
```bash
curl -X POST http://localhost:8080/api/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user456",
    "type": "PUSH",
    "recipient": "device_token_123",
    "body": "New message received!",
    "metadata": {
      "platform": "android",
      "priority": "normal"
    }
  }'
```


### Manual Testing
```bash
# Test Kafka topics
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic notification-request --from-beginning

# Test Redis operations
redis-cli set test:key "test:value"
redis-cli get test:key
```

## Performance Considerations

### Kafka Optimization
- **Partition Count**: 3 partitions per topic for parallel processing
- **Replication Factor**: 1 for single-node setup (increase for production)
- **Batch Size**: Configurable batch sizes for throughput optimization
- **Compression**: Enable compression for large messages

### Redis Optimization
- **Connection Pooling**: Use connection pools for high throughput
- **TTL Settings**: Configure appropriate TTL for idempotency keys
- **Memory Management**: Monitor memory usage and set eviction policies

### JVM Tuning
```bash
# Recommended JVM settings for production
-Xms512m -Xmx1g
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+UseStringDeduplication
```

## Security Considerations

### Authentication & Authorization
- Implement API key authentication for the gateway
- Use OAuth2/JWT for service-to-service communication
- Enable SSL/TLS for all external communications

### Data Protection
- Encrypt sensitive data in transit and at rest
- Implement proper secrets management
- Use environment variables for configuration secrets

### Network Security
- Configure firewall rules for service communication
- Use network policies in Kubernetes deployments
- Enable Kafka SSL authentication for production

## Contributing

### Development Workflow
1. Fork the repository
2. Create a feature branch
3. Make changes with proper tests
4. Run the full test suite
5. Submit a pull request

### Code Standards
- Follow Java 21 coding conventions
- Use meaningful variable and method names
- Add proper Javadoc comments
- Ensure all tests pass before submission

### Git Hooks
```bash
# Install pre-commit hooks
./scripts/install-hooks.sh

# Run code formatting
mvn spotless:apply

# Run static analysis
mvn checkstyle:check
```

