# Codebase Structure

**Analysis Date:** 2026-03-23

## Directory Layout

```
iotter-flow/                          # Maven parent project root
├── pom.xml                           # Parent POM with modules, dependency management, Java 21 config
├── .planning/                        # Planning and design documentation
├── .codegraph/                       # Code knowledge graph (if initialized)
├── .agents/                          # GSD execution plans and research
│
├── iotter-core/                      # Foundation: enums, exceptions, constants
│   ├── src/main/java/it/thisone/iotter/
│   │   ├── enums/                    # DeviceType, Period, ExportFormat, Modbus*, TracingAction
│   │   ├── eventbus/                 # Event definitions (DeviceDataMessageEvent, DeviceConfiguredEvent, etc.)
│   │   ├── config/                   # Constants interface, SecurityConfig
│   │   ├── concurrent/               # Async executor configuration
│   │   ├── util/                     # Shared utility classes
│   │   ├── exceptions/               # BackendServiceException, custom domain exceptions
│   │   └── common/                   # Common shared logic
│
├── iotter-backend/                   # Service & persistence layer (JPA/EclipseLink)
│   ├── src/main/java/it/thisone/iotter/
│   │   ├── config/                   # PersistenceJPAConfig (Entity manager, datasource, transaction manager)
│   │   └── persistence/
│   │       ├── service/              # UserService, DeviceService, NetworkService, etc. (@Service, @Transactional)
│   │       ├── model/                # JPA entities (User, Device, Network, Role, Channel*, etc.)
│   │       ├── dao/                  # Interface-based DAOs (IUserDao, IDeviceDao, INetworkDao, etc.)
│   │       ├── ifc/                  # DAO interfaces (IUserDao.java, etc.)
│   │       ├── repository/           # Spring Data repositories for standard CRUD
│   │       ├── canonical/            # Canonical/DTO forms for API responses
│   │       └── proftpd/              # FTP importer related classes
│
├── iotter-cassandra-model/           # Cassandra entity definitions
│   ├── src/main/java/it/thisone/iotter/cassandra/model/
│   │   └── [Cassandra @Table entities for time-series data]
│
├── iotter-cassandra/                 # Cassandra data access layer
│   ├── src/main/java/it/thisone/iotter/
│   │   ├── config/                   # CassandraConfig (cluster config, keyspace setup, ObjectMapper)
│   │   └── cassandra/
│   │       ├── CassandraClient.java  # Main client for Cassandra operations
│   │       ├── CassandraFeeds.java   # Feed (channel data) queries
│   │       ├── CassandraMeasures.java # Measurement (sensor reading) queries
│   │       ├── CassandraRollup.java  # Rollup (aggregated) data operations
│   │       ├── CassandraAlarms.java  # Alarm-related queries
│   │       ├── CassandraAuth.java    # Authentication/authorization queries
│   │       ├── CassandraRegistry.java # Device registry queries
│   │       ├── *QueryBuilder.java    # Query builders (MeasuresQueryBuilder, FeedsQueryBuilder, etc.)
│   │       ├── RollupQueries.java    # Rollup aggregation queries
│   │       └── CustomRetryPolicy.java # Retry logic for transient failures
│
├── iotter-mqtt/                      # MQTT integration
│   ├── src/main/java/it/thisone/iotter/
│   │   ├── config/                   # MqttConfig, MqttDevelConfig (Spring Integration configuration)
│   │   └── mqtt/
│   │       ├── MqttInboundService.java  # Receives device messages from MQTT broker
│   │       ├── MqttOutboundService.java # Sends configuration to devices
│   │       └── MqttServiceException.java
│
├── iotter-integration/               # Background processing & integrations
│   ├── src/main/java/it/thisone/iotter/
│   │   ├── config/                   # IntegrationConfig (Quartz scheduler, email config, event bus)
│   │   ├── quartz/                   # Scheduled jobs
│   │   │   ├── RollupJob.java        # Data aggregation (min/max/avg over time windows)
│   │   │   ├── AlarmJob.java         # Alarm evaluation and notification
│   │   │   ├── ExporterJob.java      # Periodic data export
│   │   │   └── HealthCheckJob.java   # Device connectivity checks
│   │   ├── provisioning/             # Device provisioning events
│   │   ├── security/                 # Password hashing (StandardStringDigester)
│   │   └── AuthManager.java          # Authentication orchestration
│
├── iotter-exporter/                  # Data export functionality
│   ├── src/main/java/it/thisone/iotter/exporter/
│   │   ├── CDataPoint.java           # Export-specific data point format
│   │   └── [Export format converters and generators]
│
├── iotter-rest-model/                # REST API data transfer objects
│   ├── src/main/java/it/thisone/iotter/rest/
│   │   ├── model/                    # DTOs (DataPoint, DataRead, DataWrite, DeviceOnlineStatus, etc.)
│   │   └── util/                     # DataPointUtil for DTO conversion
│
├── iotter-rest-endpoints/            # REST API entry point
│   ├── src/main/java/it/thisone/iotter/
│   │   ├── config/                   # JerseyConfig (servlet registration, GZIP compression)
│   │   └── rest/
│   │       ├── DeviceDataService.java     # @Path("/device/data") - device measurements
│   │       ├── DeviceConfigurationService.java # Device config CRUD
│   │       ├── MonitorService.java        # @Path("/monitor") - health checks
│   │       ├── LoggingFilter.java         # Request/response logging
│   │       ├── GZIPReaderInterceptor.java # GZIP compression
│   │       ├── GenericExceptionMapper.java # Exception → JSON response mapping
│   │       └── ConstraintViolationExceptionMapper.java
│
├── iotter-rest-billings/             # Billing-related endpoints (optional module)
│
├── iotter-rest-client-endpoints/     # Client-specific endpoints
│
├── iotter-flow-rest/                 # Vaadin Flow - REST integration (if needed)
│
├── iotter-flow-ui-core/              # Vaadin Flow UI core components
│   ├── src/main/java/it/thisone/iotter/
│   │   └── ui/
│   │       ├── ifc/                  # UI factory interfaces (IUiFactory, IDeviceUiFactory, etc.)
│   │       └── util/                 # UI utilities (PopupNotification, MapUtils, etc.)
│
├── iotter-flow-ui-shim/              # Compatibility shim for chart components
│   ├── src/main/java/it/thisone/iotter/
│   │   └── ui/                       # Wrapper components for Vaadin Charts compatibility
│
├── iotter-flow-ui/                   # Main Vaadin Flow application (Spring Boot)
│   ├── pom.xml                       # Spring Boot 3.4.3, Vaadin 24.10.0, includes DevTools
│   ├── src/main/java/it/thisone/iotter/
│   │   ├── Application.java          # @SpringBootApplication, AppShellConfigurator, main() entry point
│   │   ├── ui/
│   │   │   ├── Application.java      # (redundant with above)
│   │   │   ├── MainLayout.java       # @RouterLayout - main app container with Menu
│   │   │   ├── Menu.java             # Navigation menu component
│   │   │   ├── ErrorView.java        # Error handling for 404s
│   │   │   ├── AuthenticationErrorView.java
│   │   │   │
│   │   │   ├── main/
│   │   │   │   └── MainView.java     # Dashboard/home view
│   │   │   ├── devices/
│   │   │   │   ├── DevicesView.java  # List/manage IoT devices (@Route)
│   │   │   │   └── DevicesListing.java # Reusable device grid component
│   │   │   ├── users/
│   │   │   │   ├── UsersView.java    # User administration
│   │   │   │   └── [User management components]
│   │   │   ├── networks/
│   │   │   │   ├── NetworksView.java # Network/group administration
│   │   │   │   └── [Network components]
│   │   │   ├── networkgroups/
│   │   │   │   └── NetworkGroupsView.java
│   │   │   ├── deviceconfigurations/
│   │   │   │   └── DeviceConfigurationsView.java
│   │   │   ├── groupwidgets/
│   │   │   │   └── GroupWidgetsView.java # Dashboard widgets (charts, gauges)
│   │   │   ├── tracing/
│   │   │   │   └── TracingView.java  # Device activity/data tracing
│   │   │   ├── modbusprofiles/
│   │   │   │   └── ModbusProfilesView.java # Modbus configuration
│   │   │   ├── modbusregisters/
│   │   │   ├── channels/
│   │   │   ├── charts/
│   │   │   ├── graphicfeeds/
│   │   │   ├── graphicwidgets/
│   │   │   ├── visualizers/
│   │   │   │   └── controlpanel/ # Visualization controls
│   │   │   ├── maps/              # Geographic mapping
│   │   │   ├── gridstack/         # Dashboard grid layout
│   │   │   ├── designer/          # Widget designer
│   │   │   ├── provisioning/      # Device provisioning UI
│   │   │   ├── signup/            # User registration
│   │   │   ├── authentication/
│   │   │   │   ├── LoginScreen.java  # @Route("login") - login form
│   │   │   │   └── CurrentUser.java  # Current user provider
│   │   │   ├── about/
│   │   │   │   └── AboutView.java  # About & version info
│   │   │   ├── eventbus/
│   │   │   │   └── [UI-level event listeners]
│   │   │   ├── providers/
│   │   │   │   └── [Provider factories for dependency injection]
│   │   │   ├── common/
│   │   │   │   ├── AuthenticatedUser.java # Current user wrapper
│   │   │   │   └── BaseView.java    # Base class for views
│   │   │   │
│   │   ├── config/
│   │   │   ├── Application.java      # Spring Boot config (excludes, component scan)
│   │   │   ├── SecurityConfig.java   # Spring Security (session management, filter chain)
│   │   │   └── [Other UI-specific configs]
│   │   │
│   │   └── i18n/
│   │       └── [i18n support classes]
│   │
│   ├── src/main/resources/
│   │   ├── application.properties    # Spring Boot config (port, logging, etc.)
│   │   ├── messages_en.properties    # English translations
│   │   ├── messages_de.properties    # German translations
│   │   ├── messages_es.properties    # Spanish translations
│   │   ├── messages_fr.properties    # French translations
│   │   ├── messages_it.properties    # Italian translations
│   │   ├── app.properties            # Custom application properties
│   │   ├── bootstrap.properties      # Bootstrap/initialization config
│   │   ├── cassandra.properties      # Cassandra connection details
│   │   └── simplelogger.properties   # Logging configuration
│   │
│   └── src/main/frontend/
│       ├── styles/
│       │   └── shared-styles.css     # Global CSS (included in MainLayout via @CssImport)
│       ├── [Vaadin Flow frontend code and custom components]
│
├── iotter-flow-it/                   # Integration tests with TestBench
│   ├── src/test/java/it/thisone/iotter/
│   │   └── ui/
│   │       ├── AbstractViewTest.java # Base test class with ChromeDriver setup
│   │       ├── authentication/
│   │       │   └── LoginScreenIT.java
│   │       └── about/
│   │           └── AboutViewIT.java
│   │
│   └── pom.xml                       # TestBench, Failsafe, ChromeDriver plugins
│
└── .git/                             # Version control
```

## Directory Purposes

**Core Foundation (iotter-core):**
- Purpose: Shared domain constants, enums, exception definitions
- Contains: Error codes, device types, periods, event classes
- Key files: `Constants.java`, `enums/`, `eventbus/`

**Backend Services & Persistence (iotter-backend):**
- Purpose: Business logic, JPA entity definitions, data access
- Contains: Spring @Service classes managing domain operations, JPA @Entity models, DAO interfaces
- Key files: `persistence/service/UserService.java`, `persistence/model/Device.java`, `PersistenceJPAConfig.java`

**Time-Series Data (iotter-cassandra, iotter-cassandra-model):**
- Purpose: Efficient query and aggregation of IoT measurements
- Contains: Cassandra client, query builders, rollup calculations
- Key files: `CassandraClient.java`, `MeasuresQueryBuilder.java`, `RollupJob` processing

**REST APIs (iotter-rest-endpoints, iotter-rest-model):**
- Purpose: External integration endpoints for device data ingestion
- Contains: Jersey @Path services, DTO models, exception mappers
- Key files: `DeviceDataService.java`, `DataPoint.java`, `JerseyConfig.java`

**Integration Services (iotter-integration, iotter-mqtt, iotter-exporter):**
- Purpose: Cross-cutting concerns (scheduling, messaging, export)
- Contains: Quartz jobs, MQTT services, event handling
- Key files: `RollupJob.java`, `MqttInboundService.java`

**Web UI (iotter-flow-ui, iotter-flow-ui-core, iotter-flow-ui-shim):**
- Purpose: Vaadin Flow web application for administration
- Contains: Views with @Route, Spring-managed components, UI factories
- Key files: `Application.java`, `MainLayout.java`, `DevicesView.java`, `LoginScreen.java`

**Testing (iotter-flow-it):**
- Purpose: End-to-end UI testing with TestBench
- Contains: Vaadin TestBench integration tests
- Key files: `*IT.java` test classes

## Key File Locations

**Entry Points:**
- `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/Application.java`: Spring Boot application entry point
- `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/MainLayout.java`: Main UI layout and routing container
- `iotter-rest-endpoints/src/main/java/it/thisone/iotter/config/JerseyConfig.java`: REST API servlet registration

**Configuration:**
- `iotter-backend/src/main/java/it/thisone/iotter/config/PersistenceJPAConfig.java`: JPA entity manager, datasource, transactions
- `iotter-cassandra/src/main/java/it/thisone/iotter/config/CassandraConfig.java`: Cassandra cluster, keyspace initialization
- `iotter-integration/src/main/java/it/thisone/iotter/config/IntegrationConfig.java`: Quartz scheduler, EventBus, background jobs
- `iotter-mqtt/src/main/java/it/thisone/iotter/config/MqttConfig.java`: MQTT broker connection, Spring Integration channels
- `iotter-flow-ui/src/main/java/it/thisone/iotter/config/SecurityConfig.java`: Spring Security, session management

**Core Models:**
- `iotter-backend/src/main/java/it/thisone/iotter/persistence/model/User.java`: User entity with roles
- `iotter-backend/src/main/java/it/thisone/iotter/persistence/model/Device.java`: IoT device entity
- `iotter-backend/src/main/java/it/thisone/iotter/persistence/model/Network.java`: Device network/group
- `iotter-backend/src/main/java/it/thisone/iotter/persistence/model/ChannelConfiguration.java`: Sensor channel definition
- `iotter-cassandra-model/src/main/java/it/thisone/iotter/cassandra/model/`: Cassandra @Table entities for time-series

**Core Services:**
- `iotter-backend/src/main/java/it/thisone/iotter/persistence/service/UserService.java`: User management
- `iotter-backend/src/main/java/it/thisone/iotter/persistence/service/DeviceService.java`: Device operations
- `iotter-backend/src/main/java/it/thisone/iotter/persistence/service/NetworkService.java`: Network management
- `iotter-backend/src/main/java/it/thisone/iotter/persistence/service/TracingService.java`: Audit/tracing

**REST Services:**
- `iotter-rest-endpoints/src/main/java/it/thisone/iotter/rest/DeviceDataService.java`: Device data ingestion API
- `iotter-rest-endpoints/src/main/java/it/thisone/iotter/rest/DeviceConfigurationService.java`: Config management API
- `iotter-rest-endpoints/src/main/java/it/thisone/iotter/rest/MonitorService.java`: Health check endpoints

**Scheduled Jobs:**
- `iotter-integration/src/main/java/it/thisone/iotter/quartz/RollupJob.java`: Data aggregation
- `iotter-integration/src/main/java/it/thisone/iotter/quartz/AlarmJob.java`: Alarm evaluation
- `iotter-integration/src/main/java/it/thisone/iotter/quartz/ExporterJob.java`: Data export

**Views:**
- `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/authentication/LoginScreen.java`: Login
- `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/main/MainView.java`: Dashboard
- `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/devices/DevicesView.java`: Device list/admin
- `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/users/UsersView.java`: User administration
- `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/groupwidgets/GroupWidgetsView.java`: Dashboard widgets
- `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/tracing/TracingView.java`: Activity logs

**UI Components & Utilities:**
- `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/Menu.java`: Navigation menu builder
- `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/util/PopupNotification.java`: Toast notifications
- `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/ifc/`: UI factory interfaces

**Testing:**
- `iotter-flow-it/src/test/java/it/thisone/iotter/ui/AbstractViewTest.java`: TestBench base class
- `iotter-flow-it/src/test/java/it/thisone/iotter/ui/authentication/LoginScreenIT.java`: Login test

**Resources:**
- `iotter-flow-ui/src/main/resources/messages_en.properties`: English i18n strings (51.7K)
- `iotter-flow-ui/src/main/resources/application.properties`: Spring Boot configuration
- `iotter-flow-ui/src/main/resources/cassandra.properties`: Cassandra connection
- `iotter-flow-ui/src/main/resources/bootstrap.properties`: Startup configuration
- `iotter-flow-ui/src/main/frontend/styles/shared-styles.css`: Global CSS

## Naming Conventions

**Files:**
- Views: `[Feature]View.java` (e.g., `DevicesView.java`, `UsersView.java`)
- Services: `[Domain]Service.java` (e.g., `UserService.java`, `DeviceService.java`)
- DAOs: `I[Domain]Dao.java` (e.g., `IUserDao.java`, `IDeviceDao.java`) - interface-based
- Tests: `[Class]Test.java` (unit) or `[Class]IT.java` (integration/TestBench)
- Entities: `[Domain].java` (e.g., `User.java`, `Device.java`, `Network.java`)
- Events: `[Domain][Action]Event.java` (e.g., `DeviceConfiguredEvent.java`, `DeviceDataMessageEvent.java`)
- Job classes: `[Action]Job.java` (e.g., `RollupJob.java`, `AlarmJob.java`)
- Utilities: `[Domain]Utils.java` or `[Domain][Purpose].java` (e.g., `MapUtils.java`, `PopupNotification.java`)

**Directories:**
- Feature modules: `iotter-[feature]` (e.g., `iotter-mqtt`, `iotter-cassandra`)
- UI packages: `ui/[domain]/` (e.g., `ui/devices/`, `ui/users/`, `ui/authentication/`)
- Service layer: `persistence/service/`
- Model entities: `persistence/model/`
- Data access: `persistence/dao/`, `persistence/repository/`
- Configuration: `config/` at module root level
- Events: `eventbus/` at core module level
- Enums: `enums/` at core module level

**Packages:**
- Base: `it.thisone.iotter` (company domain)
- Feature: `it.thisone.iotter.[feature]` (e.g., `it.thisone.iotter.mqtt`, `it.thisone.iotter.cassandra`)
- Persistence: `it.thisone.iotter.persistence.[layer]` (service, model, dao, repository)
- UI: `it.thisone.iotter.ui.[feature]` (e.g., `it.thisone.iotter.ui.devices`, `it.thisone.iotter.ui.users`)

## Where to Add New Code

**New Feature (e.g., sensor alerts):**
- Primary code: `iotter-backend/src/main/java/it/thisone/iotter/persistence/service/AlertService.java`
- Data model: `iotter-backend/src/main/java/it/thisone/iotter/persistence/model/Alert.java`
- DAO: `iotter-backend/src/main/java/it/thisone/iotter/persistence/dao/IAlertDao.java`
- REST endpoint: `iotter-rest-endpoints/src/main/java/it/thisone/iotter/rest/AlertService.java`
- Event: `iotter-core/src/main/java/it/thisone/iotter/eventbus/AlertEvent.java`
- UI view: `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/alerts/AlertsView.java`
- Tests: `iotter-flow-it/src/test/java/it/thisone/iotter/ui/alerts/AlertsViewIT.java`

**New Cassandra Query:**
- Add to: `iotter-cassandra/src/main/java/it/thisone/iotter/cassandra/[Purpose]QueryBuilder.java`
- Reference in service: `iotter-backend/src/main/java/it/thisone/iotter/persistence/service/[Domain]Service.java`

**New Component/Module:**
- Create module: `mkdir iotter-[feature]`
- Add to parent POM: `pom.xml` (new `<module>` entry)
- Follow standard Maven structure: `src/main/java`, `src/main/resources`, `src/test/java`
- Create module pom.xml with parent reference and dependencies

**Utilities/Helpers:**
- Shared across modules: `iotter-core/src/main/java/it/thisone/iotter/util/`
- UI utilities: `iotter-flow-ui-core/src/main/java/it/thisone/iotter/util/`
- Module-specific: `[module]/src/main/java/it/thisone/iotter/[feature]/util/`

## Special Directories

**iotter-flow-ui/src/main/frontend/ (Frontend Assets):**
- Purpose: Vaadin Flow client-side resources, custom components, styles
- Generated: No (managed by developer)
- Committed: Yes
- Webpack 4 bundles frontend resources during Maven build
- CSS imported via @CssImport annotations in Java components

**iotter-flow-ui/target/ (Build Output):**
- Purpose: Compiled WAR, generated frontend bundles
- Generated: Yes (mvn package)
- Committed: No
- Contains compiled classes, Vaadin bundled resources

**iotter-cassandra-model/ (Cassandra Entities):**
- Purpose: Separate from main backend model for time-series-specific annotations
- Contains: @Table entities for Cassandra (not JPA)
- Usage: Referenced by iotter-cassandra layer, not JPA persistence

**iotter-flow-ui-shim/ (Compatibility Layer):**
- Purpose: Bridge chart components from Vaadin 8 to Flow
- Contains: Wrapper/proxy classes for chart compatibility
- Usage: Included in UI module for gradual migration

**iotter-rest-* Modules:**
- `iotter-rest-model/`: DTO definitions for REST API contracts
- `iotter-rest-endpoints/`: Main REST service implementations
- `iotter-rest-billings/`, `iotter-rest-client-endpoints/`: Optional feature-specific endpoints
- `iotter-rest-cod-endpoints/`, `iotter-rest-sat-endpoints/`: Commented out (inactive)

---

*Structure analysis: 2026-03-23*
