# Architecture

**Analysis Date:** 2026-02-02

## Pattern Overview

**Overall:** Layered N-tier architecture with clear separation between UI, service, and persistence layers. Multi-module Maven project with Spring dependency injection and Vaadin Flow for reactive web UI.

**Key Characteristics:**
- Modular architecture with 13 distinct Maven modules
- Service-oriented business logic with Spring components
- JPA-based relational persistence layer (EclipseLink)
- NoSQL time-series data store (Cassandra) for high-frequency telemetry
- Spring Boot used only in UI module for web server bootstrap
- Vaadin Flow 14.8.14 for component-based web UI
- Event-driven communication via Google Guava EventBus

## Layers

**UI Layer (Vaadin Flow):**
- Purpose: Provides component-based web interface for user interactions
- Location: `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/`
- Contains: Views, components, layouts, event bus infrastructure
- Depends on: `iotter-flow-ui-core`, `iotter-backend`, `iotter-integration`
- Used by: End users via HTTP/WebSocket

**UI Core Layer:**
- Purpose: Shared UI abstractions, utilities, and event definitions
- Location: `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/`
- Contains: Base classes (`BaseView`), UI event definitions, UI factories, UI utilities
- Depends on: `iotter-core`, `iotter-rest-model`
- Used by: `iotter-flow-ui` module

**Service Layer:**
- Purpose: Business logic, orchestration, and transaction management
- Location: `iotter-backend/src/main/java/it/thisone/iotter/persistence/service/`
- Contains: Services like `UserService`, `DeviceService`, `NetworkService`
- Depends on: DAOs, repositories, models
- Used by: UI layer via Spring autowiring

**Data Access Layer (JPA):**
- Purpose: Relational data persistence and queries
- Location: `iotter-backend/src/main/java/it/thisone/iotter/persistence/`
- Contains: JPA repositories, DAOs, model entities
- Depends on: `iotter-core` (enums, constants), JPA/EclipseLink
- Used by: Service layer, `iotter-integration` module

**Time-Series Data Layer (Cassandra):**
- Purpose: High-frequency telemetry and sensor data storage
- Location: `iotter-cassandra/src/main/java/it/thisone/iotter/cassandra/`
- Contains: Cassandra query builders, client wrappers, data access objects
- Depends on: `iotter-cassandra-model`, Spring Data Cassandra
- Used by: Integration services, data export functionality

**Core Layer:**
- Purpose: Shared domain models, enums, security config, scheduling
- Location: `iotter-core/src/main/java/it/thisone/iotter/`
- Contains: Enums (`AccountStatus`, `NetworkType`), utility classes, event bus config, constants
- Depends on: Only external libraries
- Used by: All other modules

**Integration Layer:**
- Purpose: Cross-module orchestration, email, MQTT, export services
- Location: `iotter-integration/src/main/java/it/thisone/iotter/integration/`
- Contains: Email notifications, MQTT device communication, data export logic
- Depends on: `iotter-backend`, `iotter-cassandra`, `iotter-mqtt`, `iotter-exporter`
- Used by: `iotter-flow-ui` for domain operations

**MQTT Module:**
- Purpose: IoT device communication via MQTT protocol
- Location: `iotter-mqtt/src/main/java/it/thisone/iotter/mqtt/`
- Contains: MQTT integration with Spring Integration, message handlers
- Depends on: `iotter-core`, Eclipse Paho MQTT client
- Used by: Integration layer, device data collection

**Exporter Module:**
- Purpose: Data export functionality (CSV, Excel, etc.)
- Location: `iotter-exporter/src/main/java/it/thisone/iotter/exporter/`
- Contains: Export format handlers and document generators
- Depends on: `iotter-core`, export libraries
- Used by: Integration layer for data downloads

## Data Flow

**User Login Flow:**

1. User accesses `/login` route → `LoginScreen` renders
2. Credentials submitted → Spring Security validates via `SecurityConfig`
3. Authentication succeeds → `CurrentUser` populated with `UserDetailsAdapter`
4. User navigated to `MainLayout` which renders `Menu` and current view
5. `UIEventBus` (UI-scoped) initialized for component communication

**Data View/Display Flow:**

1. User navigates to view (e.g., `UsersView`)
2. View's `onAttach()` injects services via Spring DI
3. View calls service method (e.g., `networkService.findOne()`)
4. Service delegates to repository (e.g., `UserRepository`)
5. Repository executes JPA query via `CrudRepository` interface
6. JPA/EclipseLink manages transaction and entity lifecycle
7. Data returned to service, service returns to view
8. View renders components with data, publishes events via `UIEventBus`

**Device Telemetry Collection Flow:**

1. IoT device sends data to `/api/telemetry` endpoint (REST)
2. Backend service processes and validates measurement
3. Data stored in Cassandra via `CassandraMeasures` or `CassandraFeeds`
4. Cassandra query builders (`MeasuresQueryBuilder`, `FeedsQueryBuilder`) construct CQL queries
5. Optional: Rollup aggregations computed via `CassandraRollup` and `RollupQueries`
6. Historical data available for UI visualization and export

**State Management:**

- **Request-scoped state:** Spring transactions within service layer
- **UI-scoped state:** `UIEventBus` for inter-component communication within same browser tab
- **Session state:** Spring Security manages session and `CurrentUser`
- **Application state:** Shared via `@Autowired` Spring beans
- **Device state:** Persisted in JPA (metadata) + Cassandra (time-series data)

## Key Abstractions

**Service Pattern:**
- Purpose: Encapsulate business logic and coordinate DAOs
- Examples: `UserService` (`iotter-backend/src/main/java/it/thisone/iotter/persistence/service/UserService.java`), `DeviceService`, `NetworkService`
- Pattern: Spring `@Service` beans with `@Transactional` methods, dependency-injected DAOs

**Repository Pattern:**
- Purpose: Abstract data access with Spring Data JPA
- Examples: `UserRepository` (`iotter-backend/src/main/java/it/thisone/iotter/persistence/repository/UserRepository.java`), `DeviceRepository`
- Pattern: Extend `BaseEntityRepository<T>` which extends `CrudRepository`, use derived query methods

**Model Entities:**
- Purpose: JPA-mapped domain objects
- Examples: `User`, `Device`, `Network` in `iotter-backend/src/main/java/it/thisone/iotter/persistence/model/`
- Pattern: Standard JPA `@Entity` classes with `@Id`, relationships via `@OneToMany`, `@ManyToOne`

**Query Builders (Cassandra):**
- Purpose: Construct dynamic CQL queries for time-series data
- Examples: `MeasuresQueryBuilder`, `FeedsQueryBuilder`, `RollupQueryBuilder` in `iotter-cassandra/src/main/java/it/thisone/iotter/cassandra/`
- Pattern: Fluent API for building CQL WHERE clauses and LIMIT/ALLOW FILTERING

**Event Bus:**
- Purpose: Decouple components via event publishing/subscription
- Examples: `UIEventBus` (UI-scoped), Guava `EventBus` (application-wide)
- Pattern: Components call `eventBus.register(this)` in `onAttach()`, define `@Subscribe` methods

**View/Component Hierarchy:**
- Purpose: Structured composition of Vaadin Flow components
- Examples: `BaseView` in `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/BaseView.java`, `MainLayout`, `UsersView`
- Pattern: Extend `BaseView` or Vaadin layout components, inject services via Spring

## Entry Points

**Application Bootstrap:**
- Location: `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/Application.java`
- Triggers: Maven command `mvn -pl iotter-flow-ui spring-boot:run`
- Responsibilities: Spring Boot app startup, component scanning, exclusion of auto-configs

**Route Entry (Vaadin Router):**
- Location: `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/MainLayout.java`
- Triggers: User navigation to URL path
- Responsibilities: Renders main layout, navigation menu, delegates to view components

**Login Entry Point:**
- Location: `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/authentication/LoginScreen.java`
- Triggers: Unauthenticated access or `/login` route
- Responsibilities: Collects credentials, integrates with Spring Security

**Error Handling Entry Point:**
- Location: `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/ErrorView.java`
- Triggers: Navigation to non-existent route
- Responsibilities: Renders user-friendly 404 error page

## Error Handling

**Strategy:** Hierarchical exception handling with Spring Security integration and view-level error handlers

**Patterns:**

- **Service Layer:** Services throw business exceptions (`BackendServiceException` in `iotter-core/src/main/java/it/thisone/iotter/exceptions/`), caught by UI
- **Transaction Rollback:** Spring `@Transactional` auto-rolls back on exceptions
- **Route Errors:** `ErrorView` implements `HasErrorParameter<NotFoundException>` to handle 404s
- **Authentication Errors:** `AuthenticationErrorView` handles Spring Security failures
- **Logging:** SLF4J logging throughout layers via `Logger` instances (e.g., `UserService`)

## Cross-Cutting Concerns

**Logging:**
- Framework: SLF4J with slf4j-log4j12 backend
- Configuration: `simplelogger.properties` in `iotter-flow-ui/src/main/resources/`
- Pattern: Static `Logger logger = LoggerFactory.getLogger(Class.class)` in each class

**Validation:**
- Framework: Hibernate Validator 6.1.0
- Pattern: Bean validation annotations (`@NotNull`, `@Email`, etc.) on model entities
- Integration: Automatic validation in Spring services

**Authentication & Authorization:**
- Framework: Spring Security 5.8.15
- Configuration: `SecurityConfig` in `iotter-flow-ui/src/main/java/it/thisone/iotter/config/SecurityConfig.java`
- Pattern: `CurrentUser` holds `UserDetailsAdapter` with roles (e.g., `ROLE_SUPERUSER`)
- Usage: Views check roles before rendering via `authenticatedUser.get().orElse(null)`

**Transaction Management:**
- Framework: Spring Framework with JPA/EclipseLink
- Pattern: `@Transactional` annotations on service methods
- Configuration: `PersistenceJPAConfig` in `iotter-backend/src/main/java/it/thisone/iotter/config/PersistenceJPAConfig.java`

**Caching:**
- Framework: Spring Caching abstraction
- Configuration: `CachingConfig` in `iotter-core/src/main/java/it/thisone/iotter/config/CachingConfig.java`
- Pattern: `@Cacheable`, `@CacheEvict` on service methods

**Scheduling:**
- Framework: Quartz 2.2.1
- Configuration: `QuartzConfig` in `iotter-core/src/main/java/it/thisone/iotter/config/QuartzConfig.java`
- Usage: Background jobs for data rollups, cleanup, synchronization

**Internationalization (i18n):**
- Resource files: `messages_en.properties`, `messages_es.properties`, etc. in `iotter-flow-ui/src/main/resources/`
- Pattern: Views call `getTranslation("key")` for locale-aware strings

---

*Architecture analysis: 2026-02-02*
