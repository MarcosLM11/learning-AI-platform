# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Study Assistant** microservices platform built with Java 21 and Spring Boot 3.5.0. The system provides AI-powered document processing and management capabilities through a distributed architecture using Spring Cloud 2025.0.0 and Spring AI 1.0.0.

## Build and Development Commands

### Maven Commands
```bash
# Build all services (from root directory)
mvn clean install

# Run a specific service (from service directory)
cd EurekaServer && mvn spring-boot:run
cd ConfigServer && mvn spring-boot:run  
cd UsersService && mvn spring-boot:run
cd DocumentsService && mvn spring-boot:run
cd AIProcessingService && mvn spring-boot:run
cd SummarizationService && mvn spring-boot:run
cd QAGenerationService && mvn spring-boot:run

# Run tests for a specific service
cd [ServiceName] && mvn test

# Package a service
cd [ServiceName] && mvn package
```

### Docker Infrastructure
```bash
# Start supporting infrastructure (PostgreSQL, Kafka, MinIO)
docker-compose up -d

# Stop infrastructure
docker-compose down

# View logs
docker-compose logs -f [service_name]
```

## Service Architecture

### Service Dependencies and Startup Order
**CRITICAL**: Services must be started in this exact order due to dependencies:

1. **Infrastructure Services** (docker-compose):
   - PostgreSQL databases (ports 5432, 5433, 5434)
   - Apache Kafka (port 9092/9093)
   - MinIO (ports 9000/9001)

2. **Core Spring Cloud Services**:
   - **EurekaServer** (port 8761) - Service discovery registry
   - **ConfigServer** (port 8888) - Centralized configuration from Git repository

3. **Business Services** (can start in parallel after core services):
   - **UsersService** (port 8080) - User management with PostgreSQL
   - **DocumentsService** (port 8081) - Document processing with PostgreSQL, MinIO, and Kafka
   - **AIProcessingService** (port 8082) - AI processing coordinator for summaries and Q&A
   - **SummarizationService** (port 8083) - Document summarization using AI models
   - **QAGenerationService** (port 8084) - Q&A generation using AI models

### Infrastructure Components Details

#### PostgreSQL Databases
- **sa_users_db** (port 5432): User entities and authentication data
- **sa_documents_db** (port 5433): Document metadata, processing logs, tags
- **sa_ai_db** (port 5434): AI-generated summaries and Q&A pairs
- **Credentials**: postgres/postgres for all databases

#### Apache Kafka (KRaft Mode)
- **Client Port**: 9092
- **Controller Port**: 9093  
- **Mode**: KRaft (no Zookeeper dependency)
- **Image**: bitnami/kafka:latest
- **Environment**: Single-node controller+broker configuration

#### MinIO Object Storage
- **API Port**: 9000
- **Console Port**: 9001 (Web UI)
- **Credentials**: minioadmin/minioadmin
- **Purpose**: Store uploaded document files

### Configuration Management
- **Pattern**: All services use **bootstrap.yml** for config server discovery
- **Repository**: `https://github.com/MarcosLM11/learning-AI-platform-config.git`
- **Branch**: main
- **Authentication**: Requires `GITHUB_USERNAME` and `GITHUB_TOKEN` environment variables
- **Profile**: `dev` (active for all services)
- **Fail-Fast**: Enabled with 6 retry attempts (1s initial interval)
- **Dynamic Refresh**: Kafka-based Spring Cloud Bus integration

## Document Processing Architecture

### Core Processing Flow
1. **Upload**: Documents stored in MinIO with metadata in PostgreSQL
2. **Processing**: Asynchronous extraction using Apache Tika
3. **Features**: Language detection, page counting, text extraction
4. **AI Processing**: Automatic summarization and Q&A generation via Kafka events
5. **Auditing**: Complete processing logs with status tracking
6. **Deduplication**: SHA-256 hash-based duplicate detection

### AI Processing Pipeline
1. **DocumentsService** completes processing → publishes `document.processing.completed` event
2. **AIProcessingService** receives event → coordinates AI processing
3. **SummarizationService** processes `document.summarization.requested` → generates summary
4. **QAGenerationService** processes `document.qa.requested` → generates Q&A pairs
5. **Results** stored in `DocumentSummary` and `DocumentQA` entities with audit trail

### Key Processing Components
- **Apache Tika**: Text extraction and language detection (`TikaLanguageDetectionServiceImpl`)
- **MinIO Integration**: File storage via `DocumentsStorageServiceImpl`
- **Processing Status**: `UPLOADED → PROCESSING → COMPLETED/FAILED`
- **Audit Trail**: `DocumentProcessingLog` entities track all operations

### Entity Relationships
```
DocumentEntity (1) → (N) DocumentProcessingLog
DocumentEntity (1) → (N) DocumentTag

DocumentSummary → DocumentEntity (via documentId)
DocumentQA (1) → (N) QuestionAnswer
DocumentQA → DocumentEntity (via documentId)
```

## Development Patterns

### Service Communication
- **Service Discovery**: Eureka client registration required for all business services
- **Configuration**: Bootstrap configuration pulls from centralized config server
- **Event Bus**: Kafka-based Spring Cloud Bus for configuration refresh
- **Load Balancing**: Spring Cloud LoadBalancer for service-to-service calls
- **Circuit Breaking**: Resilience4j integration for fault tolerance

### Data Management
- **JPA/Hibernate**: Standard Spring Data JPA repositories
- **UUID Primary Keys**: All entities use UUID generators (`@GeneratedValue(generator = "UUID")`)
- **Auditing**: `@CreatedDate` and `@LastModifiedDate` on entities with `@EnableJpaAuditing`
- **Lombok**: Extensive use for boilerplate reduction (`@Data`, `@Builder`, `@Entity`)
- **Hibernate Types**: JSON storage in PostgreSQL using `@JdbcTypeCode(Types.JSON)`

### Error Handling
- **Custom Exceptions**: `DocumentNotFoundException`, `DocumentProcessingException`
- **Global Exception Handling**: `@ControllerAdvice` with standardized error responses
- **Processing Errors**: Stored in `DocumentEntity.processingError` field
- **Status Tracking**: `ProcessingStatus` enum (`UPLOADED`, `PROCESSING`, `COMPLETED`, `FAILED`)
- **Validation**: Bean Validation annotations with detailed error messages

## Service-Specific Notes

### DocumentsService
- Uses Hibernate Types for JSON/Map storage in PostgreSQL
- Async processing with `@Async` configuration
- Language detection with Tika Optimaize implementation
- Hash-based deduplication using `HashUtil`
- Publishes Kafka events on document processing completion

### AIProcessingService
- Coordinates AI processing by consuming document completion events
- Creates DocumentSummary and DocumentQA entities
- Publishes events to specialized AI services
- Uses separate PostgreSQL database for AI-related entities

### SummarizationService
- Processes text summarization requests via Kafka
- Placeholder implementation ready for Hugging Face integration
- Supports multiple languages and configurable summary lengths
- Publishes completion events with processing metrics

### QAGenerationService
- Generates Q&A pairs from document text via Kafka
- Supports multiple question types and difficulty levels
- Configurable question count (1-20 questions)
- Placeholder implementation ready for Hugging Face integration

### ConfigServer
- Git-backed configuration with clone-on-start
- Kafka bus integration for dynamic refresh
- Actuator endpoints: `/actuator/busrefresh`

### EurekaServer  
- Self-preservation enabled
- Basic authentication (admin/admin123)
- Custom eviction and renewal intervals

## Technology Stack

### Core Framework
- **Java**: 21 LTS
- **Spring Boot**: 3.5.0
- **Spring Cloud**: 2025.0.0
- **Spring AI**: 1.0.0
- **Maven**: Multi-module project structure

### Data Persistence
- **PostgreSQL**: 15+ (three separate databases)
- **Spring Data JPA**: 3.5.0 with Hibernate 6
- **Hibernate Types**: 2.21.1 for JSON support
- **Connection Pooling**: HikariCP (Spring Boot default)

### Message Streaming
- **Apache Kafka**: Latest with KRaft mode (no Zookeeper)
- **Spring Kafka**: Native Spring Boot integration
- **Spring Cloud Stream**: Event-driven microservices
- **Spring Cloud Bus**: Configuration refresh via Kafka

### Document Processing
- **Apache Tika**: 2.9.1 for text extraction
- **Optimaize Language Detector**: Multi-language support
- **MinIO**: 8.5.7 for object storage
- **Base64 Encoding**: For file content transfer

### Service Mesh & Discovery
- **Netflix Eureka**: Service registry and discovery
- **Spring Cloud Config**: Centralized configuration management
- **Spring Cloud LoadBalancer**: Client-side load balancing
- **Resilience4j**: Circuit breaker and retry patterns

### Development Tools
- **Lombok**: Boilerplate code reduction
- **Spring Boot DevTools**: Development experience
- **Spring Boot Actuator**: Production monitoring
- **Postman Collection**: API testing and documentation

### AI/ML Integration
- **Spring AI**: AI model integration framework
- **Hugging Face Models**: Ready for integration (placeholder implementations)
- **Text Summarization**: Configurable length and language support
- **Q&A Generation**: Multiple question types and difficulty levels

## API Documentation

### Base URLs
- **UsersService**: `http://localhost:8080/api/users`
- **DocumentsService**: `http://localhost:8081/api/documents`  
- **AIProcessingService**: `http://localhost:8082/api/ai-processing`
- **SummarizationService**: `http://localhost:8083/api/summarization`
- **QAGenerationService**: `http://localhost:8084/api/qa-generation`
- **EurekaServer**: `http://localhost:8761` (Dashboard)
- **ConfigServer**: `http://localhost:8888` (Configuration)

### Key Endpoints

#### Users Service (Port 8080)
- `POST /api/users` - Create new user
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users` - Get all users
- `PUT /api/users/{id}` - Update user
- `PUT /api/users/{id}/password` - Update password
- `DELETE /api/users/{id}` - Delete user

#### Documents Service (Port 8081)
- `POST /api/documents/upload` - Upload document (Base64 encoded)
- `GET /api/documents/{id}` - Get document metadata
- `GET /api/documents/{id}/content` - Get document content
- `DELETE /api/documents/{id}` - Delete document
- `GET /api/documents/user/{userId}` - Get user's documents

#### AI Processing Service (Port 8082)
- `GET /api/ai-processing/summaries/{documentId}` - Get document summary
- `GET /api/ai-processing/qa/{documentId}` - Get document Q&A
- `GET /api/ai-processing/summaries/user/{userId}` - Get user's summaries
- `GET /api/ai-processing/qa/user/{userId}` - Get user's Q&A pairs
- `GET /api/ai-processing/health` - Service health check

#### Summarization Service (Port 8083)
- `POST /api/summarization/summarize` - Generate summary (direct)
- `GET /api/summarization/models/default` - Get default model info
- `GET /api/summarization/languages/{lang}/supported` - Check language support
- `GET /api/summarization/health` - Service health check

#### QA Generation Service (Port 8084)
- `POST /api/qa-generation/generate` - Generate Q&A (direct)
- `GET /api/qa-generation/models/default` - Get default model info
- `GET /api/qa-generation/questions/max` - Get max questions limit
- `GET /api/qa-generation/languages/{lang}/supported` - Check language support
- `GET /api/qa-generation/health` - Service health check

### Service Discovery
- `GET http://localhost:8761` - Eureka Dashboard
- `GET http://localhost:8761/eureka/apps` - Registered services (JSON)

## Testing & Development

### Postman Collection
The project includes a comprehensive **Postman Collection** (`Study-Assistant-Postman-Collection.json`) with:
- Health checks for all services
- Complete CRUD operations for Users and Documents
- AI processing workflows
- Service discovery endpoints
- Pre-configured variables for testing

### Health Monitoring Endpoints
All services expose standard Spring Boot Actuator endpoints:
- `/actuator/health` - Service health status
- `/actuator/info` - Service information
- `/actuator/metrics` - Application metrics

### Testing Strategy
- **Unit Tests**: Service layer testing with MockitoJupiterExtension
- **Integration Tests**: Repository testing with `@DataJpaTest`
- **API Tests**: Controller testing with `@WebMvcTest`
- **Kafka Tests**: Event testing with `@EmbeddedKafka`

### Development Workflow
1. Start infrastructure: `docker-compose up -d`
2. Start EurekaServer and ConfigServer
3. Start business services in any order
4. Use Postman collection for API testing
5. Monitor services via Eureka Dashboard

## Environment Variables Required
- `GITHUB_USERNAME`: For config server Git repository access
- `GITHUB_TOKEN`: For config server Git repository access (personal access token)
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka connection string (defaults to localhost:9092)

## Security Considerations
- **Configuration Security**: GitHub tokens required for config access
- **Service Authentication**: Eureka basic auth (admin/admin123)
- **Database Security**: PostgreSQL with standard authentication
- **Object Storage**: MinIO with admin credentials
- **Network Security**: Services communicate via Eureka service discovery

## Troubleshooting

### Common Issues
1. **Service Registration Failed**: Check Eureka server is running on port 8761
2. **Config Server Connection**: Verify GitHub credentials and repository access
3. **Database Connection**: Ensure PostgreSQL containers are running with docker-compose
4. **Kafka Events Not Processing**: Check Kafka container health and topic creation
5. **MinIO Upload Errors**: Verify MinIO container is running on ports 9000/9001

### Debugging Commands
```bash
# Check service registration
curl http://admin:admin123@localhost:8761/eureka/apps

# Verify database connections
docker-compose ps

# Check Kafka topics
docker exec -it [kafka-container] kafka-topics.sh --bootstrap-server localhost:9092 --list

# MinIO health check
curl http://localhost:9000/minio/health/live
```

### Log Locations
- **Application Logs**: Spring Boot default logging to console
- **Kafka Logs**: Suppressed to WARN level in application configurations
- **Docker Logs**: `docker-compose logs -f [service-name]`