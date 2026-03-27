# Technology Stack

**Analysis Date:** 2026-03-27

## Languages

**Primary:**
- Java 21 - main application code across all Maven modules in `pom.xml`, including `iotter-flow-ui`, `iotter-flow-rest`, `iotter-backend`, `iotter-cassandra`, and `iotter-mqtt`

**Secondary:**
- JavaScript/TypeScript - Vaadin frontend bundle and custom frontend dependencies in `iotter-flow-ui/package.json`
- HTML/CSS - email templates and frontend theme assets under `iotter-integration/src/main/resources/mailing` and `iotter-flow-ui/frontend`
- Properties/XML - runtime configuration in files such as `iotter-flow-ui/src/main/resources/application.properties`, `iotter-backend/src/main/resources/persistence.default.properties`, and `iotter-flow-ui/src/main/bundles/README.md`

## Runtime

**Environment:**
- JVM application runtime on Spring Boot 3.4.3 from `pom.xml`, with startup entry points in `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/Application.java` and `iotter-flow-rest/src/main/java/it/thisone/iotter/rest/JerseyApplication.java`
- Embedded Tomcat is used for the UI and REST applications via `iotter-flow-ui/src/main/java/it/thisone/iotter/config/TomcatJndiConfig.java` and `iotter-flow-rest/src/main/java/it/thisone/iotter/config/TomcatJndiConfig.java`
- Node.js-based frontend toolchain is required by Vaadin Vite builds in `iotter-flow-ui/pom.xml` and `iotter-flow-ui/package.json`

**Package Manager:**
- Maven - multi-module build declared in `pom.xml`
- npm - frontend dependency lockfile present at `iotter-flow-ui/package-lock.json`
- Lockfile: present for npm in `iotter-flow-ui/package-lock.json`

## Frameworks

**Core:**
- Spring Boot 3.4.3 - application bootstrap and embedded server support in `pom.xml`, `iotter-flow-ui/pom.xml`, and `iotter-flow-rest/pom.xml`
- Vaadin Flow 24.10.0 - server-driven UI framework from `pom.xml` and `iotter-flow-ui/pom.xml`
- Jersey 3.1.9 / JAX-RS 3.1.0 - REST endpoint stack declared in `pom.xml` and `iotter-flow-rest/pom.xml`
- Spring Data JPA 3.4.3 with EclipseLink 4.0.4 - relational persistence configured in `pom.xml` and `iotter-backend/src/main/java/it/thisone/iotter/config/PersistenceJPAConfig.java`
- Spring Data Cassandra 4.4.3 with DataStax Java Driver - time-series persistence in `pom.xml` and `iotter-cassandra/src/main/java/it/thisone/iotter/config/CassandraConfig.java`
- Spring Integration MQTT 6.4.3 with Eclipse Paho MQTT client - messaging integration in `iotter-mqtt/pom.xml` and `iotter-mqtt/src/main/java/it/thisone/iotter/config/MqttConfig.java`

**Testing:**
- JUnit 4 with Vaadin TestBench - browser integration tests in `iotter-flow-it/pom.xml`

**Build/Dev:**
- `spring-boot-maven-plugin` - local app startup and packaging in `iotter-flow-ui/pom.xml` and `iotter-flow-rest/pom.xml`
- `vaadin-maven-plugin` - frontend preparation and production build in `iotter-flow-ui/pom.xml`
- Vite 7.3.1 - frontend build pipeline in `iotter-flow-ui/package.json`

## Key Dependencies

**Critical:**
- `com.vaadin:vaadin-spring-boot-starter` - binds Vaadin UI into Spring Boot in `iotter-flow-ui/pom.xml`
- `org.springframework.boot:spring-boot-starter-web` - servlet/web runtime for both app entry modules in `iotter-flow-ui/pom.xml` and `iotter-flow-rest/pom.xml`
- `org.springframework.boot:spring-boot-starter-jersey` - REST container for endpoint modules in `iotter-flow-rest/pom.xml`
- `org.springframework.data:spring-data-jpa` - JPA repository layer in `iotter-backend/pom.xml`
- `org.springframework.data:spring-data-cassandra` - Cassandra integration in `iotter-cassandra/pom.xml`
- `org.springframework.integration:spring-integration-mqtt` - MQTT inbound/outbound channels in `iotter-mqtt/pom.xml`

**Infrastructure:**
- `com.mysql:mysql-connector-j` 8.3.0 - MySQL driver for JNDI-backed relational storage in `iotter-flow-ui/pom.xml` and `iotter-flow-rest/pom.xml`
- `org.apache.tomcat:tomcat-jdbc` - Tomcat JDBC pool for JNDI `DataSource` resources in `iotter-flow-ui/pom.xml` and `iotter-flow-rest/pom.xml`
- `org.hibernate.validator:hibernate-validator` 8.0.2.Final - bean validation in `iotter-flow-ui/pom.xml` and `iotter-backend/pom.xml`
- `com.fasterxml.jackson.dataformat:jackson-dataformat-cbor` 2.17.2 - CBOR payload handling in `iotter-rest-model/pom.xml` and `iotter-cassandra/pom.xml`
- `org.vaadin.addons.chartjs:...` - chart rendering support in `iotter-flow-ui-core/pom.xml`
- `com.flowingcode.vaadin.addons:google-map` and Leaflet add-ons - map UI integration in `iotter-flow-ui-core/pom.xml` and `iotter-flow-ui/package.json`

## Configuration

**Environment:**
- Configuration is driven primarily by classpath property files, not by `.env` files. The active property sets are loaded from files such as `iotter-backend/src/main/resources/persistence.default.properties`, `iotter-cassandra/src/main/resources/cassandra.default.properties`, `iotter-mqtt/src/main/resources/mqtt.default.properties`, `iotter-flow-ui/src/main/resources/app.properties`, `iotter-flow-ui/src/main/resources/bootstrap.properties`, and `iotter-integration/src/main/resources/smtp.default.properties`
- JVM system property overrides are built in for selected configs:
- `-Dpersistence.properties` and `-Djpa.datasource` in `iotter-backend/src/main/java/it/thisone/iotter/config/PersistenceJPAConfig.java`
- `-Dcassandra.properties`, `-Dcassandra.cluster`, `-Dcassandra.native_transport_port`, and `-Dcassandra.keyspace` in `iotter-cassandra/src/main/java/it/thisone/iotter/config/CassandraConfig.java`
- `-Dapp.properties` in `iotter-flow-ui/src/main/java/it/thisone/iotter/config/AppConfig.java` and `iotter-flow-rest/src/main/java/it/thisone/iotter/config/AppConfig.java`
- `-Dbootstrap.properties` in `iotter-integration/src/main/java/it/thisone/iotter/config/IntegrationConfig.java`

**Build:**
- Root multi-module build and version alignment: `pom.xml`
- UI packaging and Vaadin frontend build: `iotter-flow-ui/pom.xml`
- REST packaging: `iotter-flow-rest/pom.xml`
- Frontend dependency graph and Vite toolchain: `iotter-flow-ui/package.json`

## Platform Requirements

**Development:**
- Java 21 JDK as declared in `pom.xml`
- Maven installed locally for the multi-module build in `README.md`
- Node.js/npm available for Vaadin frontend preparation implied by `iotter-flow-ui/pom.xml` and `iotter-flow-ui/package.json`
- Local services are expected for MySQL, Cassandra, and MQTT based on default config targets in `iotter-flow-ui/src/main/java/it/thisone/iotter/config/TomcatJndiConfig.java`, `iotter-cassandra/src/main/resources/cassandra.default.properties`, and `iotter-mqtt/src/main/resources/mqtt.default.properties`

**Production:**
- Jar deployment model for `iotter-flow-ui` and `iotter-flow-rest` from `iotter-flow-ui/pom.xml` and `iotter-flow-rest/pom.xml`
- Production frontend bundle is built with `-Pproduction` via `iotter-flow-ui/pom.xml`
- External MySQL, Cassandra, MQTT broker, and SMTP server access is required by `iotter-backend`, `iotter-cassandra`, `iotter-mqtt`, and `iotter-integration`

---

*Stack analysis: 2026-03-27*
