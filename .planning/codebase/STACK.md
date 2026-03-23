# Technology Stack

**Analysis Date:** 2026-03-23

## Languages

**Primary:**
- Java 21 - All backend services, configuration, REST endpoints, business logic
- TypeScript 5.9.3 - Frontend type safety layer
- JavaScript (React, Lit) - Frontend components and logic

**Secondary:**
- HTML/CSS - Vaadin UI templates and theming
- XML - Maven POM configuration files

## Runtime

**Environment:**
- Java 21 (source/target)
- Spring Boot 3.4.3

**Package Manager:**
- Maven 3.x
- npm 14 - Frontend dependency management

## Frameworks

**Core:**
- Vaadin Flow 24.10.0 - Java-based web UI framework
- Spring Framework 6.2.5 - Core dependency injection and configuration
- Spring Boot 3.4.3 - Application bootstrap and auto-configuration
- Spring Security 6.5.9 - Authentication and session management

**Persistence:**
- Spring Data JPA 3.4.3 - Relational database access layer
- EclipseLink 4.0.4 - JPA implementation for MySQL
- Spring Data Cassandra 4.4.3 - Time-series data access
- DataStax Java Driver 4.17.0 - Cassandra client

**Messaging & Integration:**
- Spring Integration 6.4.3 - Message-driven architecture
- Spring Integration MQTT 6.4.3 - MQTT protocol support
- Eclipse Paho 1.2.5 - MQTT client library
- Apache Velocity 1.7 - Email template rendering
- Angus Mail 2.0.3 - Jakarta Mail implementation

**Testing:**
- JUnit 4.11 - Unit test framework
- Vaadin TestBench - Integration testing framework
- Jetty - Embedded server for integration tests
- Maven Failsafe - Integration test runner

**Build/Dev:**
- Vaadin Maven Plugin 24.10.0 - Frontend build orchestration
- Vite 7.3.1 - Frontend module bundler
- Spring Boot Maven Plugin 3.4.3 - Application packaging and run
- Maven Compiler Plugin 3.8.1 - Java compilation

**Scheduling:**
- Quartz Scheduler 2.2.1 - Job scheduling (via transitive dependencies)

## Key Dependencies

**Critical:**
- Jackson 2.17.2 - JSON serialization/deserialization
- Commons Lang3 3.14.0 - String and collection utilities
- Guava 33.0.0-jre - Collections and utility functions
- Hibernate Validator 8.0.2.Final - Bean validation

**Data Export:**
- Apache POI 5.2.5 - Excel (.xlsx, .xls) export
- iText 5.5.13.1 - PDF generation
- Apache Commons CSV 1.0 - CSV file handling

**Mapping & Geospatial:**
- Leaflet 1.9.4 - Interactive maps (frontend)
- Esri Leaflet 3.0.12 - Esri map service integration
- Google Maps (@flowingcode/google-map 3.9.0) - Map embedding
- proj4 2.15.0 - Coordinate system transformations

**UI Components:**
- GridStack 7.2.3 - Dashboard grid layout
- React 18.3.1 - React component library
- React Router 7.12.0 - Frontend routing
- Lit 3.3.2 - Lightweight web components

**Infrastructure:**
- Tomcat JDBC Connection Pool (embedded) - Connection pooling
- MySQL Connector/J 8.3.0 - MySQL database driver
- Netty 4.1.118.Final - Async networking (via Cassandra driver)
- SLF4J 2.0.16 - Logging facade

**Bundling & Packaging:**
- Rollup plugins - Code bundling utilities
- Workbox - Service worker generation
- Brotli - Compression plugin

## Configuration

**Environment:**
- Property files: `application.properties` in `iotter-flow-ui` and `iotter-flow-rest` modules
- JNDI datasource lookup for database connections
- External property files loaded via `PropertiesLoaderUtils` (e.g., `cassandra.properties`, `mqtt.properties`)
- System properties for cluster role configuration: `cluster.role` (default: "master")

**Key Configuration Files:**
- `iotter-flow-ui/src/main/resources/application.properties` - UI module settings
  - Vaadin dev mode live reload enabled
  - Circular reference allowed for Spring beans
  - Session persistence disabled
  - Management shutdown endpoint enabled
- `iotter-flow-rest/src/main/resources/application.properties` - REST API settings
  - Server port: 8081
  - Same circular reference and session settings

**Runtime Configuration:**
- JNDI lookups for datasources at `java:comp/env/`
- Property file loading from classpath resources
- Spring Security session registry for single-session-per-user enforcement
- Cassandra keyspace auto-creation (master node only)

## Build Profiles

**Production:**
- Profile: `production`
- Activation: `-Pproduction` flag
- Effect: Triggers Vaadin frontend optimization via `vaadin-maven-plugin:build-frontend`
  - Minification and bundling of JavaScript
  - CSS optimization
  - Asset compression (Brotli)

**Development:**
- Default profile (no flag needed)
- Spring Boot DevTools enabled for hot reload
- Vaadin dev mode with live reload
- No frontend optimization

## Platform Requirements

**Development:**
- Java 21 JDK
- Maven 3.6+
- Node.js 14+ (for frontend builds)
- Chrome/Chromium (for integration tests)

**Production:**
- Java 21 JRE
- MySQL 5.7 or later (relational data)
- Apache Cassandra 3.x or later (time-series data)
- MQTT broker (for device communication)
- Tomcat 10+ or equivalent servlet container (optional if using embedded)

**Testing Requirements:**
- ChromeDriver compatible with Chrome version
- Maven Failsafe plugin
- Jetty for embedded server

---

*Stack analysis: 2026-03-23*
