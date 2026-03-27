# External Integrations

**Analysis Date:** 2026-03-27

## APIs & External Services

**Messaging and device transport:**
- MQTT broker - device provisioning, command dispatch, and message subscriptions
  - SDK/Client: `org.springframework.integration:spring-integration-mqtt` and `org.eclipse.paho:org.eclipse.paho.client.mqttv3` from `iotter-mqtt/pom.xml`
  - Auth: property keys `mqtt.username` and `mqtt.password` loaded by `iotter-mqtt/src/main/java/it/thisone/iotter/config/MqttConfig.java`

**Maps and geospatial UI:**
- Google Maps - interactive device map rendering in `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/maps/DevicesGoogleMap.java` and `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/networks/NetworkListing.java`
  - SDK/Client: `com.flowingcode.vaadin.addons:google-map` from `iotter-flow-ui-core/pom.xml`
  - Auth: property key `googlemap.apikey` from `iotter-flow-ui/src/main/resources/app.properties`
- Leaflet and ESRI map libraries - image overlays, leaflet maps, clustering, and vector maps in `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/maps/ImageOverlayMap.java` and `iotter-flow-ui/package.json`
  - SDK/Client: `org.vaadin.addons.componentfactory` Leaflet add-ons from `iotter-flow-ui-core/pom.xml`, plus `leaflet`, `esri-leaflet`, `esri-leaflet-vector`, and related browser packages in `iotter-flow-ui/package.json`
  - Auth: none detected in code

**Mail and notification delivery:**
- SMTP server - alarm notifications, password reset, and export email forwarding through `iotter-integration/src/main/java/it/thisone/iotter/integration/EmailService.java`
  - SDK/Client: Spring `JavaMailSender` from `iotter-integration/src/main/java/it/thisone/iotter/config/IntegrationConfig.java`
  - Auth: property keys `mail.smtp.host`, `mail.smtp.port`, `mail.smtp.username`, `mail.smtp.password`, `mail.no-reply`, `mail.catch-all`, and `mail.images` loaded from `smtp.properties` or `smtp.default.properties`

**Device and client APIs:**
- REST ingestion and management APIs - device data, configuration, provisioning, client administration, billing, and monitoring endpoints in `iotter-rest-endpoints/src/main/java/it/thisone/iotter/rest/DeviceDataService.java`, `iotter-rest-endpoints/src/main/java/it/thisone/iotter/rest/DeviceConfigurationService.java`, `iotter-rest-endpoints/src/main/java/it/thisone/iotter/rest/DeviceProvisioningService.java`, `iotter-rest-client-endpoints/src/main/java/it/thisone/iotter/rest/ClientUserEndpoint.java`, and `iotter-rest-billings/src/main/java/it/thisone/iotter/rest/BillingEndpoint.java`
  - SDK/Client: Jersey / JAX-RS from `iotter-flow-rest/pom.xml`
  - Auth: request header `api-key` on device and client endpoints in files such as `iotter-rest-endpoints/src/main/java/it/thisone/iotter/rest/DeviceDataService.java` and `iotter-rest-client-endpoints/src/main/java/it/thisone/iotter/rest/ClientEndpoint.java`

## Data Storage

**Databases:**
- MySQL via JNDI-backed `DataSource`
  - Connection: JNDI name `jdbc/iotter2` in `iotter-backend/src/main/resources/persistence.default.properties` and `iotter-backend/src/main/java/it/thisone/iotter/config/PersistenceJPAConfig.java`
  - Client: EclipseLink JPA plus Spring Data JPA from `iotter-backend/pom.xml`
- Cassandra
  - Connection: property keys `cassandra.cluster`, `cassandra.native_transport_port`, `cassandra.keyspace`, `cassandra.local_datacenter`, and `cassandra.roll_up_cron` loaded by `iotter-cassandra/src/main/java/it/thisone/iotter/config/CassandraConfig.java`
  - Client: Spring Data Cassandra and DataStax `CqlSession` in `iotter-cassandra/src/main/java/it/thisone/iotter/cassandra/CassandraClient.java`
- MySQL-backed FTP/proftpd metadata tables
  - Connection: JDBC `DataSource` consumed by `iotter-backend/src/main/java/it/thisone/iotter/persistence/dao/FtpUserDao.java`
  - Client: Spring JDBC DAO layer in `iotter-backend/src/main/java/it/thisone/iotter/persistence/dao/FtpUserDao.java`

**File Storage:**
- Local filesystem plus generated export/email assets
  - Email templates live under `iotter-integration/src/main/resources/mailing`
  - FTP user home directories are referenced in `iotter-backend/src/main/java/it/thisone/iotter/persistence/dao/FtpUserDao.java`
  - No cloud object storage client is detected

**Caching:**
- In-process Spring cache abstraction with cache names used from `iotter-backend/src/main/java/it/thisone/iotter/persistence/service/DeviceService.java`, `iotter-integration/src/main/java/it/thisone/iotter/integration/SubscriptionService.java`, and `iotter-rest-client-endpoints/src/main/java/it/thisone/iotter/rest/ClientSupervisorEndpoint.java`
- EclipseLink second-level cache coordination can switch to JMS if `Constants.AMQ.CACHE_COORDINATION` resolves to `jms` in `iotter-backend/src/main/java/it/thisone/iotter/config/PersistenceJPAConfig.java`

## Authentication & Identity

**Auth Provider:**
- Custom application authentication
  - Implementation: Spring Security is present but explicitly excluded from Boot autoconfiguration in `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/Application.java` and `iotter-flow-rest/src/main/java/it/thisone/iotter/rest/JerseyApplication.java`; request authorization is enforced through application services and `api-key` header checks in `iotter-rest-client-endpoints/src/main/java/it/thisone/iotter/rest/ClientEndpoint.java`
- Google reCAPTCHA keys are configured in `iotter-flow-ui/src/main/resources/app.properties`
  - Implementation: property-backed integration only; no external SDK usage was detected in the inspected Java modules

## Monitoring & Observability

**Error Tracking:**
- None detected for hosted services such as Sentry, Rollbar, or Bugsnag

**Logs:**
- SLF4J-based application logging throughout the codebase, with JavaMail session debug output routed through `it.thisone.iotter.config.Log4JavaMail` in `iotter-integration/src/main/java/it/thisone/iotter/config/IntegrationConfig.java`
- Operational monitor endpoint available at `iotter-rest-endpoints/src/main/java/it/thisone/iotter/rest/MonitorService.java`

## CI/CD & Deployment

**Hosting:**
- Self-hosted jar deployment on embedded Tomcat is implied by `iotter-flow-ui/pom.xml`, `iotter-flow-rest/pom.xml`, and the custom JNDI setup in `iotter-flow-ui/src/main/java/it/thisone/iotter/config/TomcatJndiConfig.java`

**CI Pipeline:**
- Not detected

## Environment Configuration

**Required env vars:**
- No OS environment variables are required by the inspected code paths
- Required runtime settings are instead supplied by classpath property files and JVM system properties:
- `persistence.properties` / `jpa.datasource` for relational persistence in `iotter-backend/src/main/java/it/thisone/iotter/config/PersistenceJPAConfig.java`
- `cassandra.properties`, `cassandra.cluster`, `cassandra.native_transport_port`, and `cassandra.keyspace` for Cassandra in `iotter-cassandra/src/main/java/it/thisone/iotter/config/CassandraConfig.java`
- `mqtt.properties` for MQTT connectivity in `iotter-mqtt/src/main/java/it/thisone/iotter/config/MqttConfig.java`
- `smtp.properties` and `mail_messages.properties` for outbound mail in `iotter-integration/src/main/java/it/thisone/iotter/config/IntegrationConfig.java`
- `app.properties` for UI/domain integrations such as `googlemap.apikey` in `iotter-flow-ui/src/main/java/it/thisone/iotter/config/AppConfig.java`
- `bootstrap.properties` for bootstrap users and supervisor metadata in `iotter-integration/src/main/java/it/thisone/iotter/config/IntegrationConfig.java`

**Secrets location:**
- Secret-bearing settings are loaded from classpath property files under module resource directories such as `iotter-flow-ui/src/main/resources/app.properties`, `iotter-flow-ui/src/main/resources/bootstrap.properties`, `iotter-mqtt/src/main/resources/mqtt.default.properties`, and `iotter-integration/src/main/resources/smtp.default.properties`
- No external secret manager or vault integration is detected

## Webhooks & Callbacks

**Incoming:**
- HTTP device callbacks via REST endpoints:
  - `iotter-rest-endpoints/src/main/java/it/thisone/iotter/rest/DeviceDataService.java`
  - `iotter-rest-endpoints/src/main/java/it/thisone/iotter/rest/DeviceConfigurationService.java`
  - `iotter-rest-endpoints/src/main/java/it/thisone/iotter/rest/DeviceProvisioningService.java`
- MQTT inbound subscriptions through `iotter-mqtt/src/main/java/it/thisone/iotter/mqtt/MqttInboundService.java`

**Outgoing:**
- MQTT outbound commands and provisioning through `iotter-mqtt/src/main/java/it/thisone/iotter/mqtt/MqttOutboundService.java`
- SMTP email delivery through `iotter-integration/src/main/java/it/thisone/iotter/integration/EmailService.java`

---

*Integration audit: 2026-03-27*
