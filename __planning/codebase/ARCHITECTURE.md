# Architecture

**Analysis Date:** 2026-03-23

## Pattern Overview

**Overall:** Layered multi-module architecture with clear separation between UI, business logic, persistence, and external integrations. Spring Boot with Vaadin Flow (v24.10.0) on the frontend, JPA/EclipseLink for relational persistence, and Cassandra for time-series data.

**Key Characteristics:**
- Multi-module Maven project (18 active modules) with strict dependency layering
- Spring component-based dependency injection with explicit service wiring
- Vaadin Flow routing with MainLayout as the primary RouterLayout
- Event-driven architecture using Spring EventBus for cross-module communication
- RESTful APIs via Jersey 2 for device data ingestion and external integrations

## Layers

**Presentation (Vaadin UI):**
- Purpose: Web-based user interface for device administration and monitoring
- Location: `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/`
- Contains: Views (Pages with @Route), Components, Layouts, EventBus subscribers
- Depends on: `iotter-flow-ui-core`, `iotter-backend`, Spring Security
- Used by: Browser clients via HTTP/WebSocket

**Business Logic (Service Layer):**
- Purpose: Core application logic, transactions, domain model operations
- Location: `iotter-backend/src/main/java/it/thisone/iotter/persistence/service/`
- Contains: Spring @Service classes (UserService, DeviceModelService, NetworkService, TracingService, etc.)
- Depends on: Data Access Layer (DAOs/Repositories), Cassandra layer, Event system
- Used by: UI views, REST endpoints, background jobs

**Persistence (Data Access Layer):**
- Purpose: JPA/EclipseLink ORM and JDBC data access
- Location: `iotter-backend/src/main/java/it/thisone/iotter/persistence/`
- Contains: JPA entities (model/), DAOs (dao/), Spring Data Repositories (repository/), canonical forms (canonical/)
- Depends on: `iotter-core` for enums and exceptions
- Used by: Service layer

**Time-Series Storage (Cassandra Layer):**
- Purpose: Efficient storage and query of high-frequency IoT sensor data
- Location: `iotter-cassandra/src/main/java/it/thisone/iotter/cassandra/`
- Contains: CassandraClient, CassandraFeeds, CassandraMeasures, RollupQueries, specialized query builders
- Depends on: DataStax Driver 4.17.0, Cassandra entity models
- Used by: Service layer for telemetry and time-window queries

**REST API Layer:**
- Purpose: Device data ingestion, configuration updates, monitoring endpoints
- Location: `iotter-rest-endpoints/src/main/java/it/thisone/iotter/rest/`
- Contains: Jersey @Path services (DeviceDataService, DeviceConfigurationService, MonitorService, etc.)
- Depends on: Service layer, REST models, security components
- Used by: IoT devices, third-party systems, external integrations

**Core/Shared Infrastructure:**
- Purpose: Constants, enums, shared exceptions, event definitions, utility code
- Location: `iotter-core/src/main/java/it/thisone/iotter/`
- Contains: enums (DeviceType, Period, ExportFormat, ModbusConfiguration*), exceptions, EventBus definitions, config Constants, utility classes
- Depends on: Nothing (foundation layer)
- Used by: All other modules

**Integration/Background Processing:**
- Purpose: Scheduled jobs, email notifications, MQTT message handling, data export
- Location: `iotter-integration/src/main/java/it/thisone/iotter/`
- Contains: Quartz jobs (AlarmJob, RollupJob, HealthCheckJob, ExporterJob), MQTT integration, email sending
- Depends on: Service layer, Cassandra layer, Export functionality
- Used by: Scheduler, MQTT broker

**MQTT Integration:**
- Purpose: Real-time device messaging via MQTT protocol
- Location: `iotter-mqtt/src/main/java/it/thisone/iotter/mqtt/`
- Contains: MqttInboundService, MqttOutboundService, Spring Integration configuration
- Depends on: Eclipse Paho 1.2.5, Spring Integration 6.4.3
- Used by: IoT devices, integration services

**Data Export:**
- Purpose: CSV and Excel export functionality for device data
- Location: `iotter-exporter/src/main/java/it/thisone/iotter/exporter/`
- Contains: Export orchestration, format converters, file generation
- Depends on: Service layer, Cassandra layer
- Used by: Export scheduled job, UI export actions

## Data Flow

**Device Data Ingestion:**

1. IoT device sends measurements via REST API → `iotter-rest-endpoints/DeviceDataService`
2. Service validates device credentials and data format → `iotter-backend/DeviceService`
3. Data stored in Cassandra time-series table → `iotter-cassandra/CassandraMeasures`
4. Event published: `DeviceDataMessageEvent` → EventBus
5. Subscribers process: alarms, rollups, notifications
6. UI components subscribed to EventBus receive real-time updates

**Device Configuration Update (Write):**

1. User updates device config in UI → REST API call to `DeviceConfigurationService`
2. Service validates and persists to JPA → `iotter-backend/DeviceConfigurationService`
3. Configuration revision tracked in `DeviceHistory` entity
4. Event published: `DeviceConfiguredEvent` → EventBus
5. MQTT outbound service sends config to device → `MqttOutboundService`
6. Device confirms via acknowledgment message

**Historical Data Visualization:**

1. UI view (e.g., `GroupWidgetsView`) requests data for time range
2. Service calls Cassandra query builder → `CassandraFeeds` or `MeasuresQueryBuilder`
3. Cassandra returns raw or rolled-up data (aggregates by time window)
4. Service transforms to UI-compatible format (DataPoint objects)
5. View renders in chart component (Vaadin Charts via shim)

**Scheduled Data Processing:**

1. Quartz scheduler triggers job at configured time
2. Job (e.g., RollupJob) reads raw measurements from Cassandra
3. Computes aggregates (min/max/avg) over time windows
4. Stores rollup data back to Cassandra
5. Job completes; next schedule triggered

**State Management:**

- UI component state: Local component fields, VaadinSession attributes
- User session: Stored in Spring Security SecurityContext
- Application state: Spring beans (singletons), cached in EclipseLink ORM cache
- Real-time updates: EventBus subscribers update UI components reactively

## Key Abstractions

**Service Pattern:**
- Purpose: Business logic encapsulation with transaction management
- Examples: `iotter-backend/service/UserService.java`, `NetworkService.java`, `DeviceService.java`
- Pattern: Spring @Service class with @Transactional methods, dependency injection of DAOs

**View Pattern (Vaadin Flow):**
- Purpose: UI routes with consistent navigation and layout
- Examples: `DevicesView`, `UsersView`, `NetworksView` all extend BaseView
- Pattern: @Route annotation, dependency injection via constructor, lazy initialization in onAttach()

**DAO/Repository Pattern:**
- Purpose: Data access abstraction for JPA entities
- Examples: `iotter-backend/dao/IUserDao.java`, `iotter-backend/repository/` (Spring Data repos)
- Pattern: Interface-based DAOs for flexibility, Spring Data Repositories for standard CRUD

**EventBus Pattern:**
- Purpose: Decoupled event publishing and subscription across modules
- Examples: `DeviceDataMessageEvent`, `DeviceConfiguredEvent`, `RollupEvent`
- Pattern: Spring ApplicationEvent-based (EventBusWrapper), @EventListener subscribers, asynchronous processing

**Query Builder Pattern (Cassandra):**
- Purpose: Fluent API for complex time-series queries
- Examples: `MeasuresQueryBuilder`, `FeedsQueryBuilder`, `RollupQueryBuilder`
- Pattern: Builder pattern for composing Cassandra CQL queries with pagination, filtering, aggregation

## Entry Points

**Web Application:**
- Location: `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/Application.java`
- Triggers: Spring Boot startup (mvn spring-boot:run)
- Responsibilities: Spring Boot configuration, Vaadin app shell, theme setup (parity theme)

**REST Endpoints:**
- Location: `iotter-rest-endpoints/src/main/java/it/thisone/iotter/config/JerseyConfig.java`
- Triggers: HTTP requests to /api/* paths
- Responsibilities: Jersey servlet registration, exception mapping, logging filters

**Scheduled Jobs:**
- Location: `iotter-integration/src/main/java/it/thisone/iotter/config/IntegrationConfig.java`
- Triggers: Quartz scheduler (configured cron expressions)
- Responsibilities: RollupJob, AlarmJob, ExporterJob, HealthCheckJob instantiation and triggering

**MQTT Inbound:**
- Location: `iotter-mqtt/src/main/java/it/thisone/iotter/mqtt/MqttInboundService.java`
- Triggers: MQTT messages on subscribed topics
- Responsibilities: Parse device messages, publish to EventBus, update cache

## Error Handling

**Strategy:** Layered exception handling with context-specific responses

**Patterns:**

- **Domain Exceptions:** `it.thisone.iotter.exceptions.BackendServiceException` - caught by services, logged, and wrapped in REST responses
- **REST Exception Mapping:** `GenericExceptionMapper`, `ConstraintViolationExceptionMapper` - Jersey mappers convert exceptions to JSON error responses with HTTP status codes
- **UI Error Handling:** ErrorView implements `HasErrorParameter<NotFoundException>` for 404s, components show Notification popups for validation errors
- **Cassandra Errors:** Custom retry policy in `iotter-cassandra/CustomRetryPolicy.java` handles transient connection failures
- **Error Codes:** Defined in `iotter-core/config/Constants.java` (DEVICE_NOT_FOUND_ERROR_CODE, USER_NOT_AUTHORIZED, etc.) for standardized error communication

## Cross-Cutting Concerns

**Logging:** SLF4J with logback, configurable per module and log category (mqtt, async executor). Custom categories defined in Constants.

**Validation:**
- JPA Bean Validation via Hibernate Validator (8.0.2)
- Input validation in service methods before persistence
- UI-side validation in Vaadin form components

**Authentication:**
- Spring Security 6.5.9 with session management (max 1 concurrent session per user)
- LoginScreen validates credentials via AuthManager
- SecurityContext holds authenticated user throughout request/session
- Authorization checks in services (Constants.ROLE_SUPERUSER) and UI views

**Transactions:**
- @Transactional on service methods (order=10)
- JpaTransactionManager coordinates with EclipseLink ORM
- Cassandra operations generally non-transactional (eventual consistency model)

**Caching:**
- EclipseLink ORM cache for JPA entities
- Spring Cache abstraction (@EnableCaching) for frequently accessed data
- Manual cache invalidation on entity updates

**Aspect-Oriented Programming:**
- AspectJ 1.9.21 enabled via @EnableAspectJAutoProxy
- Used for cross-cutting concerns (logging, profiling, etc.)

---

*Architecture analysis: 2026-03-23*
