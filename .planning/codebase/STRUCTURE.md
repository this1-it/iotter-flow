# Codebase Structure

**Analysis Date:** 2026-02-02

## Directory Layout

```
iotter-flow/                                  # Maven parent module
├── iotter-core/                             # Shared domain models, enums, config
│   └── src/main/java/it/thisone/iotter/
│       ├── enums/                           # Domain enums (AccountStatus, NetworkType, etc.)
│       ├── config/                          # Spring config (EventBusConfig, CachingConfig, QuartzConfig)
│       ├── util/                            # Shared utilities
│       ├── eventbus/                        # Application-wide event definitions
│       ├── exceptions/                      # Domain exceptions
│       └── concurrent/                      # Threading/concurrency utilities
│
├── iotter-rest-model/                       # REST API DTOs and models
│   └── src/main/java/it/thisone/iotter/
│       └── rest/                            # REST request/response models
│
├── iotter-cassandra-model/                  # Cassandra entity definitions
│   └── src/main/java/it/thisone/iotter/
│       └── cassandra/model/                 # @Table entities for Cassandra
│
├── iotter-backend/                          # Service layer and JPA persistence
│   └── src/main/java/it/thisone/iotter/
│       ├── persistence/
│       │   ├── service/                     # Business logic (UserService, DeviceService, etc.)
│       │   ├── repository/                  # Spring Data JPA repositories
│       │   ├── dao/                         # Data access objects
│       │   ├── ifc/                         # DAO interfaces
│       │   ├── model/                       # JPA @Entity classes
│       │   ├── canonical/                   # Canonical data structures
│       │   ├── proftpd/                     # ProFTPD integration
│       │   └── util/                        # Persistence utilities
│       ├── config/                          # JPA/database config
│       └── util/                            # Backend utilities
│
├── iotter-cassandra/                        # Cassandra time-series data access
│   └── src/main/java/it/thisone/iotter/
│       ├── cassandra/                       # Query builders, client wrappers
│       ├── quartz/                          # Scheduled jobs for Cassandra
│       ├── exporter/                        # Data export from Cassandra
│       └── config/                          # Cassandra connection config
│
├── iotter-mqtt/                             # MQTT device communication
│   └── src/main/java/it/thisone/iotter/
│       ├── mqtt/                            # MQTT handlers, adapters
│       └── config/                          # MQTT/Spring Integration config
│
├── iotter-exporter/                         # Data export (CSV, Excel)
│   └── src/main/java/it/thisone/iotter/
│       └── exporter/                        # Export format handlers
│
├── iotter-integration/                      # Cross-module orchestration
│   └── src/main/java/it/thisone/iotter/
│       └── integration/                     # Email, webhooks, service coordination
│
├── iotter-flow-ui-core/                     # Shared UI abstractions
│   └── src/main/java/it/thisone/iotter/
│       ├── ui/
│       │   ├── common/                      # BaseView, utilities
│       │   ├── ifc/                         # UI factory interfaces
│       │   ├── main/                        # Main UI constants/interfaces
│       │   └── eventbus/                    # UI event definitions
│       └── util/                            # UI utilities (PopupNotification, MapUtils)
│
├── iotter-flow-ui-shim/                     # Vaadin 8 → Flow compatibility
│   └── src/main/java/it/thisone/iotter/
│       └── ui/shim/                         # Compatibility wrappers (e.g., chart components)
│
├── iotter-flow-ui/                          # Spring Boot + Vaadin Flow UI
│   ├── src/main/java/it/thisone/iotter/
│   │   ├── ui/
│   │   │   ├── Application.java             # Spring Boot entry point
│   │   │   ├── MainLayout.java              # Main app layout with navigation
│   │   │   ├── Menu.java                    # Navigation menu
│   │   │   ├── ErrorView.java               # 404 error handler
│   │   │   ├── AuthenticationErrorView.java # Auth error handler
│   │   │   ├── main/
│   │   │   │   └── MainView.java            # Root view/dashboard
│   │   │   ├── authentication/
│   │   │   │   ├── LoginScreen.java         # Login form
│   │   │   │   └── CurrentUser.java         # Current user provider
│   │   │   ├── users/
│   │   │   │   ├── UsersView.java           # User management view
│   │   │   │   ├── UsersListing.java        # User list component
│   │   │   │   ├── UserDetails.java         # User detail display
│   │   │   │   └── UserForm.java            # User edit form
│   │   │   ├── about/
│   │   │   │   └── AboutView.java           # About/info view
│   │   │   ├── groupwidgets/
│   │   │   │   └── GroupWidgetAdapterListing.java
│   │   │   ├── eventbus/                    # UI-scoped event bus
│   │   │   │   ├── UIEventBus.java          # UI event bus instance
│   │   │   │   ├── UIEventBusInitializer.java
│   │   │   │   └── UIEventBus*.java         # Supporting classes
│   │   │   └── i18n/                        # i18n configuration
│   │   └── config/
│   │       ├── Application.java             # App configuration
│   │       ├── VaadinConfig.java            # Vaadin-specific config
│   │       ├── SecurityConfig.java          # Spring Security config
│   │       ├── SessionRegistryConfig.java   # Session management
│   │       └── TomcatJndiConfig.java        # JNDI data source config
│   └── src/main/resources/
│       ├── application.properties           # Spring Boot config
│       ├── cassandra.properties             # Cassandra connection
│       ├── bootstrap.properties             # Bootstrap config
│       ├── app.properties                   # Application settings
│       ├── messages_en.properties           # English i18n
│       ├── messages_es.properties           # Spanish i18n
│       ├── messages_fr.properties           # French i18n
│       ├── messages_de.properties           # German i18n
│       ├── messages_it.properties           # Italian i18n
│       └── simplelogger.properties          # SLF4J config
│
└── iotter-flow-it/                          # Integration tests (Vaadin TestBench)
    └── src/test/java/...                    # *IT.java test classes
```

## Directory Purposes

**iotter-core:**
- Purpose: Foundation module with domain models, enums, and shared Spring config
- Contains: Enums (`AccountStatus`, `NetworkType`, `TracingAction`), utility classes, event bus factory, constants
- Key files: `enums/`, `config/EventBusConfig.java`, `config/QuartzConfig.java`

**iotter-backend:**
- Purpose: Service layer and JPA persistence for relational data
- Contains: Services (UserService, DeviceService), repositories, JPA entities, DAOs
- Key files: `persistence/service/`, `persistence/repository/`, `persistence/model/`

**iotter-cassandra:**
- Purpose: Time-series data access layer for telemetry and measurements
- Contains: Query builders (MeasuresQueryBuilder, FeedsQueryBuilder), Cassandra client, rollup logic
- Key files: `cassandra/CassandraClient.java`, `cassandra/MeasuresQueries.java`, `cassandra/RollupQueries.java`

**iotter-flow-ui:**
- Purpose: Main web application with Spring Boot and Vaadin Flow
- Contains: Views, components, layouts, authentication, configuration
- Key files: `ui/Application.java` (Spring Boot entry), `ui/MainLayout.java`, `ui/authentication/`

**iotter-flow-ui-core:**
- Purpose: Shared UI abstractions and event definitions
- Contains: `BaseView`, UI event classes, UI factory interfaces
- Key files: `ui/common/BaseView.java`, `ui/eventbus/` event definitions

**iotter-flow-ui-shim:**
- Purpose: Compatibility layer for Vaadin 8 → Flow migration
- Contains: Wrapper components for chart and other Vaadin 8 add-ons
- Used by: `iotter-flow-ui` for backward compatibility

**iotter-integration:**
- Purpose: Cross-module orchestration, email, export, MQTT coordination
- Contains: Email services, export coordination, domain event listeners
- Key files: Integration service classes that coordinate between modules

**iotter-mqtt:**
- Purpose: IoT device communication via MQTT protocol
- Contains: MQTT client configuration, message handlers, adapters
- Key files: `mqtt/` MQTT handler classes

**iotter-exporter:**
- Purpose: Data export functionality
- Contains: CSV/Excel export format handlers, document generators
- Key files: Export service classes

## Key File Locations

**Entry Points:**
- `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/Application.java`: Spring Boot application start point
- `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/MainLayout.java`: Main layout router for views
- `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/authentication/LoginScreen.java`: Login entry point

**Configuration:**
- `iotter-flow-ui/src/main/resources/application.properties`: Spring Boot app configuration
- `iotter-flow-ui/src/main/resources/cassandra.properties`: Cassandra connection
- `iotter-backend/src/main/java/it/thisone/iotter/config/PersistenceJPAConfig.java`: JPA/EclipseLink setup
- `iotter-cassandra/src/main/java/it/thisone/iotter/config/CassandraConfig.java`: Cassandra client config
- `iotter-flow-ui/src/main/java/it/thisone/iotter/config/SecurityConfig.java`: Spring Security

**Core Logic:**
- `iotter-backend/src/main/java/it/thisone/iotter/persistence/service/UserService.java`: User management business logic
- `iotter-backend/src/main/java/it/thisone/iotter/persistence/service/DeviceService.java`: Device management logic
- `iotter-cassandra/src/main/java/it/thisone/iotter/cassandra/CassandraMeasures.java`: Time-series queries
- `iotter-integration/`: Service orchestration and email/webhooks

**Testing:**
- `iotter-flow-it/src/test/java/`: Vaadin TestBench integration tests (*IT.java)
- `iotter-backend/src/test/java/`: Backend unit tests (*Test.java)

## Naming Conventions

**Files:**
- Views: `*View.java` (e.g., `UsersView.java`, `AboutView.java`)
- Services: `*Service.java` (e.g., `UserService.java`, `DeviceService.java`)
- Repositories: `*Repository.java` (e.g., `UserRepository.java`, `DeviceRepository.java`)
- DAOs: `*Dao.java` (e.g., `UserDao.java`) interfaces in `ifc/` directory
- Models/Entities: PascalCase without suffix (e.g., `User.java`, `Device.java`)
- Components: `*Listing.java`, `*Details.java`, `*Form.java` for UI components
- Query Builders: `*QueryBuilder.java` (e.g., `MeasuresQueryBuilder.java`)
- Test files: `*Test.java` (unit) or `*IT.java` (integration)

**Directories:**
- Package structure: `it.thisone.iotter.[module].[layer]` (e.g., `it.thisone.iotter.persistence.service`)
- UI views: Grouped by feature under `ui/` (e.g., `ui/users/`, `ui/authentication/`)
- Services: Grouped in `persistence/service/`
- Repositories: Grouped in `persistence/repository/`
- Models: Grouped in `persistence/model/`

## Where to Add New Code

**New Feature (e.g., "Devices"):**
- Primary code: `iotter-backend/src/main/java/it/thisone/iotter/persistence/service/DeviceService.java`
- Repository: `iotter-backend/src/main/java/it/thisone/iotter/persistence/repository/DeviceRepository.java`
- Model: `iotter-backend/src/main/java/it/thisone/iotter/persistence/model/Device.java`
- UI View: `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/devices/DevicesView.java`
- Tests: `iotter-backend/src/test/java/it/thisone/iotter/persistence/service/DeviceServiceTest.java`

**New UI Component:**
- Implementation: `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/` (if reusable) or `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/[feature]/` (if feature-specific)
- Event definitions: `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/eventbus/` (if needs cross-component communication)
- Base class: Extend `BaseView` for top-level views or Vaadin layout components

**New Service:**
- Implementation: `iotter-backend/src/main/java/it/thisone/iotter/persistence/service/[Name]Service.java`
- Interface: `iotter-backend/src/main/java/it/thisone/iotter/persistence/ifc/I[Name]Dao.java`
- Annotation: Use `@Service` on class
- Dependencies: Inject repositories via `@Autowired`

**Shared Utilities:**
- Backend utils: `iotter-core/src/main/java/it/thisone/iotter/util/`
- UI utils: `iotter-flow-ui-core/src/main/java/it/thisone/iotter/util/`
- Shared enums: `iotter-core/src/main/java/it/thisone/iotter/enums/`

**Cassandra Time-Series Data:**
- Queries: Add to `iotter-cassandra/src/main/java/it/thisone/iotter/cassandra/[Domain]Queries.java`
- Query Builder: Add to `iotter-cassandra/src/main/java/it/thisone/iotter/cassandra/[Domain]QueryBuilder.java`
- Models: `iotter-cassandra-model/src/main/java/it/thisone/iotter/cassandra/model/`

**Integration/Orchestration:**
- Email: `iotter-integration/src/main/java/it/thisone/iotter/integration/email/`
- MQTT handlers: `iotter-mqtt/src/main/java/it/thisone/iotter/mqtt/`
- Exports: `iotter-exporter/src/main/java/it/thisone/iotter/exporter/`

**i18n Messages:**
- Add keys to: `iotter-flow-ui/src/main/resources/messages_en.properties`
- Usage in code: `getTranslation("key")` in views

## Special Directories

**iotter-flow-ui/src/main/resources:**
- Purpose: Application configuration and localization
- Generated: No
- Committed: Yes
- Contains: property files for app config, Cassandra, logging, i18n

**iotter-flow-ui/src/main/webapp:**
- Purpose: Static web assets (images, icons)
- Generated: No
- Committed: Yes
- Contains: `icons/`, `img/` directories with PNG files

**iotter-flow-ui/target:**
- Purpose: Maven build output and Vaadin frontend build
- Generated: Yes (by Maven)
- Committed: No (in .gitignore)
- Contains: Compiled classes, bundled frontend, war/jar artifacts

**iotter-flow-ui/frontend:**
- Purpose: Vaadin Flow client-side components and resources
- Generated: Partially (by vaadin-maven-plugin during build)
- Committed: Yes
- Contains: CSS, TypeScript, HTML fragments for custom components

---

*Structure analysis: 2026-02-02*
