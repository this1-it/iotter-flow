# External Integrations

**Analysis Date:** 2026-02-02

## APIs & External Services

**MQTT (IoT Device Communication):**
- Eclipse Paho MQTT Client 1.2.5 - Device message ingestion
  - SDK/Client: `org.eclipse.paho:org.eclipse.paho.client.mqttv3`
  - Configuration: `iotter-mqtt/src/main/java/it/thisone/iotter/config/MqttConfig.java`
  - Integration: Spring Integration 5.5.20 (`spring-integration-mqtt`)
  - Inbound adapter: `MqttPahoMessageDrivenChannelAdapter` for device subscriptions
  - Outbound handler: `MqttPahoMessageHandler` for command publishing
  - Settings (from `mqtt.default.properties`):
    - Host: `mqtt.host` (default: docker_mqtt)
    - Port: `mqtt.port` (default: 1883)
    - QoS: `mqtt.qos` (default: 2)
    - Application name: `mqtt.application` (default: aernet)
    - Provisioning topic: `iotter/device/{deviceId}/provisioning`
    - Auth: Optional username/password (`mqtt.username`, `mqtt.password`)

**SMTP/Email (Notifications):**
- JavaMail API 1.6.2 - Email sending for alerts and notifications
  - SDK/Client: `com.sun.mail:javax.mail`
  - Configuration: Email service in `iotter-integration/src/main/java/it/thisone/iotter/integration/EmailService.java`
  - Settings (from `smtp.default.properties`):
    - Host: `mail.smtp.host` (default: webmail.aermec.com)
    - Port: `mail.smtp.port` (default: 587)
    - Username: `mail.smtp.username` for authentication
    - Password: `mail.smtp.password` (env variable recommended)
    - TLS: `mail.smtp.starttls.enable=true`
    - No-reply address: `mail.no-reply`
    - Catch-all fallback: `mail.catch-all`
  - Email types supported:
    - User registration confirmation
    - Password reset
    - Alarm notifications (configurable via `mail.alert` setting)
    - Data export notifications
  - Template engine: Apache Velocity 1.7 for HTML email templates

## Data Storage

**Databases:**

**Relational (Device metadata, users, configuration):**
- MySQL 5.7+ (via JNDI datasource)
  - Connection: `java:comp/env/jdbc/iotter2` (JNDI name)
  - Client: EclipseLink 2.6.2 JPA provider
  - Driver: mysql-connector-j 8.0.33
  - Configuration file: `iotter-backend/src/main/resources/persistence.default.properties`
  - ORM: Spring Data JPA 2.7.18 with repository pattern
  - Repositories: `iotter-backend/src/main/java/it/thisone/iotter/persistence/repository/`
  - Entities: `iotter-backend/src/main/java/it/thisone/iotter/persistence/model/`
  - DDL generation: `create-or-extend-tables` (can be overridden to drop-and-create)

**Time-series (IoT device measurements):**
- Apache Cassandra 4.x
  - Connection: Native transport protocol on port 9042
  - Contact points: `cassandra.cluster` (default: docker_cassandra)
  - Keyspace: `cassandra.keyspace` (default: aernet)
  - Client: Spring Data Cassandra 3.4.18
  - Driver: DataStax java-driver-core 4.17.0
  - Configuration: `iotter-cassandra/src/main/java/it/thisone/iotter/config/CassandraConfig.java`
  - Data access: `iotter-cassandra/src/main/java/it/thisone/iotter/cassandra/`
  - TTL: Configurable per data type (default measurements TTL: 604800 seconds = 7 days)
  - Rollup jobs: Scheduled cron job for data aggregation (default: every 2 hours `0 0 0/2 * * ?`)
  - Replication: Simple strategy with configurable replication factor (default: 1)
  - Data serialization: Jackson CBOR format for binary efficiency

**File Storage:**
- Local filesystem only - No cloud storage integration
  - Export files (CSV, Excel, PDF) written to temp directory
  - Exported files attached to email notifications
  - No persistent cloud storage backend configured

**Caching:**
- EHCache 2.6.9 - In-memory cache (local, not distributed)
  - Used for: JPA entity caching, query result caching
  - Configuration: Spring `@EnableCaching` annotation
  - Not suitable for multi-node deployments without coordination

## Authentication & Identity

**Auth Provider:**
- Custom Spring Security implementation (no OAuth/SAML)
  - Configuration: `it.thisone.iotter.config.SecurityConfig` (excluded from component scan in `Application.java`)
  - Password encoding: BCryptPasswordEncoder (Spring Security Crypto 5.8.15)
  - Implementation: Form-based login via Spring Security

**User Management:**
- Local user database in MySQL
  - Service: `iotter-backend/src/main/java/it/thisone/iotter/persistence/service/UserService.java`
  - Entities: User model in `it.thisone.iotter.persistence.model`
  - Default user: `supervisor.user` and `supervisor.pass` from bootstrap.properties

**Authorization:**
- Spring Security roles and permissions
- Device-based access control (users assigned to devices)

## Monitoring & Observability

**Error Tracking:**
- Not detected - No external error tracking service (Sentry, Rollbar, etc.)

**Logs:**
- SLF4J 1.7.36 (facade)
- Configuration via Log4J or Logback (implementation in classpath)
- Component-based logging categories:
  - `Constants.AsyncExecutor.LOG4J_CATEGORY` - Async operations
  - `Constants.MQTT.LOG4J_CATEGORY` - MQTT integration
  - `Constants.Notifications.LOG4J_CATEGORY` - Email/alerts
  - `Constants.RollUp.ROLL_UP_LOG4J_CATEGORY` - Data rollup jobs
- EclipseLink logging: Configured via `eclipselink.logging.level` property

## CI/CD & Deployment

**Hosting:**
- Spring Boot executable JAR deployment
- Embedded Tomcat servlet container (spring-boot-starter-web)
- Expected deployment environment: Application server with JNDI datasource configuration

**CI Pipeline:**
- Not detected - No GitHub Actions, Jenkins, GitLab CI configuration visible
- Maven-based local builds only

## Environment Configuration

**Required env vars:**
- Database connection (via JNDI):
  - Tomcat/servlet container JNDI datasource `jdbc/iotter2` must be configured
- Cassandra cluster:
  - `cassandra.cluster` - Host(s) for Cassandra connectivity
  - `cassandra.keyspace` - Keyspace name
- MQTT broker:
  - `mqtt.host` - MQTT broker address
  - `mqtt.port` - MQTT port (optional, defaults to 1883)
- SMTP:
  - `mail.smtp.host` - Email server
  - `mail.smtp.username` - SMTP auth username
  - `mail.smtp.password` - SMTP auth password (CRITICAL: Must be externalized from default properties)

**Secrets location:**
- DANGER: Hard-coded in `smtp.default.properties`:
  - `mail.smtp.password=G!uliano_2016` (SECURITY ISSUE)
  - Should be moved to environment variables or encrypted property source
- Bootstrap defaults in `bootstrap.properties`:
  - `supervisor.user=supervisor` (default admin)
  - `supervisor.pass=iotter` (default password - should be changed on first run)
- Cassandra replication properties in system properties or `cassandra.properties`

## Webhooks & Callbacks

**Incoming:**
- Not detected - No webhook endpoints for external systems

**Outgoing:**
- Not detected - No outbound webhooks to external services
- Internal event publishing via Spring's `EventBus` for intra-system communication
  - UI module publishes/listens to domain events for reactive updates

## Data Serialization

**JSON:**
- Jackson 2.8.5 - JSON serialization for REST APIs and inter-component communication
  - REST models: `iotter-rest-model/src/main/java/it/thisone/iotter/rest/model/`
  - Annotations: `com.fasterxml.jackson` (JsonInclude, JsonProperty, etc.)

**Binary Formats:**
- CBOR (Concise Binary Object Representation):
  - jackson-dataformat-cbor 2.8.5 - Efficient binary serialization for Cassandra
  - Used for storing/retrieving data points in time-series database
  - Mapper configured in `CassandraConfig`: `cborMapper()` bean

## Export Capabilities

**Data Export Formats:**
- CSV - Apache Commons CSV 1.0
- Excel - Apache POI 3.13 (poi, poi-ooxml)
- PDF - iTextPDF 5.3.4
- All exports: `iotter-exporter/src/main/java/it/thisone/iotter/exporter/`
- Exported files sent via email or downloaded by users

---

*Integration audit: 2026-02-02*
