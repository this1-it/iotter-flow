# Technology Stack

**Analysis Date:** 2026-02-02

## Languages

**Primary:**
- Java 8 (1.8) - All backend modules, JPA entities, Spring services, MQTT integration
- XML - Maven configuration and Spring contexts

**Secondary:**
- JavaScript/TypeScript - Frontend web components (Vaadin Flow)
- HTML/CSS - UI templates and theming

## Runtime

**Environment:**
- Java Runtime Environment 8 (JRE 1.8)
- Node.js 14 - Frontend build tooling (specified in CLAUDE.md)

**Package Manager:**
- Maven 3+ - Java/backend dependency management (required via `<prerequisites>`)
- npm - Frontend dependency management (managed by Vaadin Maven plugin)
- Lockfile: `pom.xml` (Maven), `package-lock.json` (npm)

## Frameworks

**Core Backend:**
- Spring Framework 5.3.39 - Dependency injection, context, transaction management
- Spring Boot 2.7.18 - Application startup and auto-configuration (UI module only)
- Spring Security 5.8.15 - Authentication, authorization, password encoding
- Spring Data JPA 2.7.18 - ORM repository abstraction
- Spring Integration 5.5.20 - Messaging and MQTT integration

**Frontend:**
- Vaadin 14.8.14 (Flow) - Java-based reactive web framework
  - Location: `iotter-flow-ui` module
  - Components: @vaadin/* web components (grid, form, dialog, etc.)
  - Themes: Lumo and Material Design styles
- Webpack 4.42.0 - Frontend bundling and asset pipeline
- Babel 7 - JavaScript transpilation for browser compatibility

**Persistence:**
- JPA 2.1 / EclipseLink 2.6.2 - ORM for relational database
  - Configuration: `iotter-backend/src/main/java/it/thisone/iotter/config/PersistenceJPAConfig.java`
  - Entities scanned: `it.thisone.iotter.persistence.model`
- Cassandra 4.x - Time-series data storage
  - Client: Spring Data Cassandra 3.4.18
  - Driver: DataStax java-driver-core 4.17.0
  - Configuration: `iotter-cassandra/src/main/java/it/thisone/iotter/config/CassandraConfig.java`

**Scheduling:**
- Quartz Scheduler 2.2.1 - Cron job scheduling (rollup jobs)
  - Configuration: Integrated in `CassandraConfig` for data aggregation

**Caching:**
- EHCache 2.6.9 - In-memory caching
- Spring Cache abstraction - `@EnableCaching` in `PersistenceJPAConfig` and `CassandraConfig`

**Testing:**
- JUnit 4.11 - Unit test framework

**Build/Dev:**
- Maven plugins:
  - spring-boot-maven-plugin 2.7.18 - Spring Boot application packaging
  - vaadin-maven-plugin 14.8.14 - Frontend asset compilation and web component imports
- Maven compiler with source/target 1.8
- Spring Boot DevTools - Live reload during development

## Key Dependencies

**Critical:**
- mysql-connector-j 8.0.33 - MySQL database driver
- jackson-core, jackson-databind 2.8.5 - JSON serialization/deserialization
- jackson-dataformat-cbor 2.8.5 - CBOR binary format for Cassandra storage
- slf4j-api 1.7.36 - Logging facade
- commons-lang3 3.5, commons-io 2.12.0 - Utility libraries

**Infrastructure:**
- eclipse-persistence (EclipseLink) 2.6.2 - JPA provider
- javax.persistence 2.1.0 - JPA API
- org.apache.commons:commons-csv 1.0 - CSV export support
- org.apache.poi:poi, poi-ooxml 3.13 - Excel export support
- itextpdf 5.3.4 - PDF export support

**Messaging/Integration:**
- eclipse-paho.client.mqttv3 1.2.5 - MQTT client library
- spring-integration-mqtt 5.5.20 - MQTT inbound/outbound channel adapters
- velocity 1.7 - Email template engine
- javax.mail 1.6.2 - Email sending via SMTP

**Security:**
- spring-security-core, spring-security-web, spring-security-config 5.8.15
- spring-security-crypto 5.8.15 - BCrypt password encryption (`BCryptPasswordEncoder`)
- hibernate-validator 5.1.1.Final / 6.1.0.Final - Bean validation

**Utilities:**
- guava 19.0 - Collections and utilities
- netty-handler 4.1.10.Final - Async networking (transitive via Cassandra)
- aspectjweaver, aspectjrt 1.8.0 - AOP support for caching
- javax.annotation-api 1.3.2 - Common annotations support

## Configuration

**Environment:**
- JNDI Datasource: `java:comp/env/jdbc/iotter2` for MySQL (configured via servlet container)
- System properties for cluster role:
  - `cassandra.cluster` - Cassandra contact points (default: docker_cassandra)
  - `cassandra.keyspace` - Cassandra keyspace name (default: aernet)
  - `cassandra.native_transport_port` - Cassandra port (default: 9042)
  - `cassandra.local_datacenter` - Cassandra datacenter name (default: dc1)
  - `mqtt.host` - MQTT broker host (default: docker_mqtt)
  - `persistence.properties` - Override JPA properties file
  - `cassandra.properties` - Override Cassandra properties file
  - `CLUSTER_ROLE` - Set to "master" for DDL generation (default: master)

**Build:**
- `pom.xml` - Root aggregator
  - Vaadin BOM (Bill of Materials) version 14.8.14 imported
  - Maven repositories: vaadin-prereleases, vaadin-addons, central
- `iotter-flow-ui/pom.xml` - Application entry point
  - Spring Boot dependencies managed via spring-boot-dependencies BOM 2.7.18
  - Production profile (`-Pproduction`) enables frontend optimization via `build-frontend` goal

**Properties Files:**
- `iotter-backend/src/main/resources/persistence.default.properties` - JPA configuration
  - `jpa.datasource` - JNDI name
  - `eclipselink.ddl-generation` - Schema generation mode
  - `eclipselink.logging.level` - EclipseLink logging

- `iotter-cassandra/src/main/resources/cassandra.default.properties` - Cassandra configuration
  - `cassandra.cluster` - Contact points (comma-separated)
  - `cassandra.keyspace` - Keyspace name
  - `cassandra.replication` - Replication strategy
  - `cassandra.roll_up_cron` - Rollup job schedule

- `iotter-mqtt/src/main/resources/mqtt.default.properties` - MQTT configuration
  - `mqtt.host` - Broker hostname
  - `mqtt.application` - Client application name
  - `mqtt.qos` - Quality of service level
  - `mqtt.poolsize` - Thread pool size

- `iotter-integration/src/main/resources/smtp.default.properties` - Email configuration
  - `mail.smtp.host` - SMTP server
  - `mail.smtp.port` - SMTP port (default: 587)
  - `mail.smtp.auth` - Authentication enabled
  - `mail.smtp.starttls.enable` - TLS required
  - `mail.no-reply` - No-reply email address
  - `mail.catch-all` - Fallback email address

- `iotter-flow-ui/src/main/resources/application.properties` - Spring Boot configuration
  - `vaadin.devmode.liveReload.enabled=true` - Development mode live reload

- `iotter-flow-ui/src/main/resources/bootstrap.properties` - Application bootstrap
  - `supervisor.user` - Default admin user
  - `supervisor.email` - Admin email
  - `models` - Device model configuration

## Platform Requirements

**Development:**
- Java Development Kit (JDK) 8
- Maven 3 or higher
- Node.js 14.x for frontend builds
- MySQL 5.7+ database server (via JNDI datasource)
- Cassandra 4.x or compatible (via docker_cassandra by default)
- MQTT broker (via docker_mqtt by default, e.g., Mosquitto)

**Production:**
- Deployment target: Spring Boot executable JAR
- Application runs on embedded Tomcat (Spring Boot starter-web)
- Requires configured JNDI datasource for MySQL
- Cassandra cluster connectivity
- MQTT broker connectivity
- SMTP server for email notifications

---

*Stack analysis: 2026-02-02*
