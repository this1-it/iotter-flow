# External Integrations

**Analysis Date:** 2026-03-23

## APIs & External Services

**Maps & Geospatial:**
- Google Maps - Embedded interactive maps on UI
  - Package: `@flowingcode/google-map` 3.9.0
  - Integration: Frontend React component
  - Auth: None (public API, may require API key for production)

- Esri ArcGIS Services - GIS data and mapping
  - Package: `esri-leaflet`, `esri-leaflet-vector` (frontend)
  - Integration: Leaflet plugin for map service layers
  - Auth: Inline URL-based authentication (API key in URL)

- OpenStreetMap (via Leaflet) - Base map tiles
  - Integration: Free tile service via Leaflet
  - Auth: No authentication required

## Data Storage

**Databases:**

- **MySQL** (Relational)
  - Connection: JNDI lookup at `java:comp/env/` via `iotter-flow-backend`
  - Client: EclipseLink JPA (Jakarta Persistence 3.1.0)
  - Config: `iotter-backend/src/main/java/it/thisone/iotter/config/PersistenceJPAConfig.java`
  - Driver: MySQL Connector/J 8.3.0
  - DataSource: Tomcat JDBC connection pool
  - Purpose: Device metadata, user accounts, configuration, alerts
  - Entities: `iotter-backend/src/main/java/it/thisone/iotter/persistence/model/`

- **Apache Cassandra** (Time-Series)
  - Connection: Direct cluster connection configured in `iotter-cassandra/src/main/java/it/thisone/iotter/config/CassandraConfig.java`
  - Client: Spring Data Cassandra 4.4.3 + DataStax Java Driver 4.17.0
  - Purpose: High-frequency telemetry, sensor readings, time-windowed data
  - Config: External `cassandra.properties` file (classpath resource)
  - Keyspace: Auto-created on startup (master node only)
  - Replication: Configurable (default: 1)
  - Entities: `iotter-cassandra-model/src/main/java/it/thisone/iotter/cassandra/entity/`

**File Storage:**
- Local filesystem only - No cloud storage integration detected

**Caching:**
- Spring Cache abstraction enabled in `PersistenceJPAConfig` and `CassandraConfig`
- Implementation: Not explicitly configured (likely defaults to No-Op cache)

## Authentication & Identity

**Auth Provider:**
- Custom Spring Security implementation
- Location: `iotter-flow-rest/src/main/java/it/thisone/iotter/config/SecurityConfig.java`

**Implementation Details:**
- Session-based authentication via Spring Security
- Single session per user enforced: `sessionManagement.maximumSessions(1)`
- SessionRegistry implementation: `SessionRegistryImpl`
- Password encoding: BCrypt (via `BCryptPasswordEncoder`)
- No OAuth2/OpenID Connect detected
- No LDAP/SSO integration detected

**User Details:**
- Adapter: `iotter-integration/src/main/java/it/thisone/iotter/security/UserDetailsAdapter.java`
- User service: `iotter-backend/src/main/java/it/thisone/iotter/persistence/service/UserService.java`

## Messaging

**MQTT** (IoT Device Communication)
- Broker Connection: TCP/IP configured in `iotter-mqtt/src/main/java/it/thisone/iotter/config/MqttConfig.java`
- Client Library: Eclipse Paho 1.2.5
- Framework: Spring Integration MQTT 6.4.3
- Connection Details:
  - Host: Configurable via `mqtt.properties`
  - Port: Configurable (default: 1883)
  - QoS: Configurable (inbound: default 2, outbound: default 0)
  - Client ID: Generated dynamically with application name prefix
  - Auth: Optional username/password from `mqtt.properties`
  - Max inflight: 32000
  - Retained messages: Disabled

**Channels:**
- Inbound: `MqttPahoMessageDrivenChannelAdapter` (managed by Spring Integration)
  - Topic subscriptions: Dynamically configured per device
  - Auto-startup: Disabled by default
  - Payload: Raw bytes
  - Scheduler: Thread pool task scheduler

- Outbound: `MqttPahoMessageHandler`
  - Default retained: False
  - Default async: False

**Message Routing:**
- Location: `iotter-mqtt/src/main/java/it/thisone/iotter/mqtt/`
- Pattern: Spring Integration message-driven channel adapters
- Processing: Asynchronous via `@Async` annotation

## Email Notifications

**Email Service:**
- Implementation: `iotter-integration/src/main/java/it/thisone/iotter/integration/EmailService.java`
- Transport: Jakarta Mail (Angus Mail 2.0.3)
- Configuration: Spring `JavaMailSender` bean in `IntegrationConfig`

**Email Features:**
- Template rendering: Apache Velocity 1.7
- Supported types: Text, HTML, attachments (MIME multipart)
- Mock implementation: `iotter-integration/src/main/java/it/thisone/iotter/config/JavaMailSenderMock.java`
- Location: `iotter-integration/src/main/resources/mail.properties` (configuration file)

**Message Types:**
- Notifications via `NotificationService`
- Alarms via `AlarmService`
- Exports via `ExportService`

## Job Scheduling

**Quartz Scheduler** (Transitive via Cassandra config)
- Framework: Quartz 2.2.1
- Configuration: `iotter-core/src/main/java/it/thisone/iotter/config/QuartzConfig.java`
- Factory Bean: `SchedulerFactoryBean`
- Custom Job Factory: `AutowiringSpringBeanJobFactory` for Spring autowiring support

**Scheduled Jobs:**
- Location: `iotter-core/src/main/java/it/thisone/iotter/quartz/`
- Health Check Job: `HealthCheckJob` - Periodic health monitoring
- Rollup Job: `RollupCronJob` - Time-series data aggregation
- Custom cron expressions in `QuartzConfig` constants (e.g., every 10s, 30s, 5m, etc.)

**Datasource:**
- Uses JNDI lookup via `lookUpDataSource()` in QuartzConfig
- Direct connection pool lookup at: `java:comp/env/`

## Monitoring & Observability

**Error Tracking:**
- No dedicated error tracking service detected (Sentry, Datadog, etc.)
- Default Java exception handling

**Logs:**
- Framework: SLF4J 2.0.16 (facade)
- Logger category constants: `Constants.LOG4J_CATEGORY` throughout modules
- Categories: AsyncExecutor, MQTT, Cassandra, Notifications, etc.
- Implementation: Not specified (runtime selection via SLF4J bindings)

**Metrics:**
- Spring Management endpoints: `/actuator` endpoints available
  - Shutdown endpoint: Enabled (`management.endpoints.shutdown.enabled=true`)

## CI/CD & Deployment

**Hosting:**
- Not specified - Can run on any Java application server
- Tested with: Tomcat (embedded via Spring Boot)
- Embedded Jetty: Used for integration tests

**CI Pipeline:**
- Not detected in codebase - External CI/CD system assumed

**Build Process:**
- Maven multi-module builds
- Vaadin frontend compilation via `vaadin-maven-plugin:prepare-frontend` (dev)
- Vaadin frontend optimization via `vaadin-maven-plugin:build-frontend` (production)
- Spring Boot fat JAR packaging via `spring-boot-maven-plugin`

## Environment Configuration

**Required Environment Variables:**
- Database connection properties (JNDI datasource configuration)
- MQTT broker host and port
- MQTT broker credentials (if authentication enabled)
- Cassandra cluster contact points and port
- Email server SMTP configuration (host, port, credentials)

**Configuration File Locations:**
- Classpath properties files:
  - `cassandra.properties` - Cassandra connection and replication
  - `mqtt.properties` - MQTT broker connection details
  - `mail.properties` - Email server configuration
  - `quartz.properties` - Job scheduler configuration
  - `application.properties` - Application-level settings

**Secrets Storage:**
- Environment variables expected (external configuration)
- `mqtt.properties` contains credentials (username/password)
- JNDI datasources managed by application server

## Webhooks & Callbacks

**Incoming:**
- REST endpoints defined in `iotter-rest-endpoints/` module
- Jersey 2.x REST framework (JAX-RS 3.1.0)
- Config: `iotter-rest-endpoints/src/main/java/it/thisone/iotter/config/JerseyConfig.java`
- JSON serialization: Custom Jackson configuration with GZIP compression support

**Device Data Endpoints:**
- DataPoint ingestion endpoints
- Device status endpoints
- Configuration endpoints

**Outgoing:**
- None detected (no outbound webhooks/callbacks to external services)
- Internal event bus via Spring Event mechanism
- Location: `iotter-core/src/main/java/it/thisone/iotter/config/EventBusConfig.java`

## Data Export

**Export Formats:**
- CSV - Via Apache Commons CSV
- Excel (.xlsx, .xls) - Via Apache POI 5.2.5
- PDF - Via iText 5.5.13.1

**Export Service:**
- Location: `iotter-integration/src/main/java/it/thisone/iotter/integration/ExportService.java`
- Event: `ExportStartEvent` published on UI
- Processing: Asynchronous via `@Async`
- Delivery: Email attachment or download

---

*Integration audit: 2026-03-23*
