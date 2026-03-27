# Codebase Structure

**Analysis Date:** 2026-03-27

## Directory Layout

```text
iotter-flow/
├── pom.xml                         # Reactor parent listing all Maven modules
├── docs/                           # Current implementation notes and archived migration docs
├── iotter-core/                    # Cross-cutting enums, config, events, exceptions, utilities
├── iotter-backend/                 # JPA entities, DAOs, repositories, domain services
├── iotter-cassandra/               # Cassandra access and time-series services
├── iotter-cassandra-model/         # Cassandra-side model objects and helpers
├── iotter-integration/             # Integration services and external-system orchestration
├── iotter-mqtt/                    # MQTT transport services
├── iotter-exporter/                # Export abstractions and export providers
├── iotter-rest-model/              # Shared REST DTO/model classes
├── iotter-rest-endpoints/          # Core JAX-RS resources and REST infrastructure
├── iotter-rest-billings/           # Billing-specific REST endpoints
├── iotter-rest-client-endpoints/   # Client-focused REST endpoints
├── iotter-flow-rest/               # Executable Spring Boot + Jersey REST app
├── iotter-flow-ui-shim/            # Compatibility/adapter layer for Flow migration helpers
├── iotter-flow-ui-core/            # Reusable Vaadin UI framework and shared components
├── iotter-flow-ui/                 # Executable Vaadin UI app and feature views
├── iotter-flow-it/                 # Integration-test module packaging the UI app for TestBench
└── .planning/codebase/             # Generated codebase maps consumed by GSD tooling
```

## Directory Purposes

**`docs/`:**
- Purpose: Keep implementation guides and migration/reference documents.
- Contains: Current docs in `docs/implementation/` and historical material in `docs/archive/`.
- Key files: `docs/index.md`, `docs/implementation/AbstractBaseEntityListing.md`, `docs/implementation/SPRING_EVENTS_TO_VAADIN_FLOW_UI.md`

**`iotter-core/`:**
- Purpose: Hold code shared by most modules without UI or endpoint concerns.
- Contains: `config`, `eventbus`, `exceptions`, `common`, `concurrent`, and enums under `src/main/java`.
- Key files: `iotter-core/src/main/java/it/thisone/iotter/config/EventBusConfig.java`, `iotter-core/src/main/java/it/thisone/iotter/config/CachingConfig.java`

**`iotter-backend/`:**
- Purpose: Own the relational domain model and business services.
- Contains: JPA entities in `iotter-backend/src/main/java/it/thisone/iotter/persistence/model/`, DAOs in `.../dao/`, DAO interfaces in `.../ifc/`, repositories in `.../repository/`, and service classes in `.../service/`.
- Key files: `iotter-backend/src/main/java/it/thisone/iotter/persistence/service/DeviceService.java`, `iotter-backend/src/main/java/it/thisone/iotter/persistence/model/Device.java`, `iotter-backend/src/main/java/it/thisone/iotter/persistence/repository/BaseEntityRepository.java`

**`iotter-rest-endpoints/`:**
- Purpose: Define reusable REST resources and request/response infrastructure.
- Contains: JAX-RS resources, filters, interceptors, exception mappers, object mapper providers, and Jersey-related config.
- Key files: `iotter-rest-endpoints/src/main/java/it/thisone/iotter/rest/DeviceProvisioningService.java`, `iotter-rest-endpoints/src/main/java/it/thisone/iotter/config/JerseyConfig.java`

**`iotter-flow-rest/`:**
- Purpose: Package and launch the REST application.
- Contains: Boot entry point and app-level config only.
- Key files: `iotter-flow-rest/src/main/java/it/thisone/iotter/rest/JerseyApplication.java`, `iotter-flow-rest/src/main/java/it/thisone/iotter/config/RestJerseyConfig.java`, `iotter-flow-rest/src/main/java/it/thisone/iotter/config/AppConfig.java`

**`iotter-flow-ui-core/`:**
- Purpose: Centralize reusable Vaadin patterns and shared widgets so feature packages stay focused on one entity or workflow.
- Contains: Base CRUD abstractions, validators, common fields, eventbus helpers, providers, wizard framework, chart utilities, and custom Flow component shims.
- Key files: `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/AbstractBaseEntityListing.java`, `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/AbstractBaseEntityForm.java`, `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/providers/BackendServices.java`

**`iotter-flow-ui-shim/`:**
- Purpose: Isolate Flow-compatibility helpers and low-level UI adapters used by the shared UI core.
- Contains: Java classes under `src/main/java` with minimal dependencies beyond Flow itself.
- Key files: `iotter-flow-ui-shim/pom.xml`

**`iotter-flow-ui/`:**
- Purpose: Hold the executable Vaadin app, route views, feature-specific forms/listings, theme files, and custom frontend resources.
- Contains: Java packages under `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/**`, Spring resources under `src/main/resources`, and frontend/theme files under `iotter-flow-ui/frontend`.
- Key files: `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/Application.java`, `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/MainLayout.java`, `iotter-flow-ui/frontend/styles/app-layout.css`

**`iotter-flow-it/`:**
- Purpose: Package browser-driven integration tests around the UI artifact.
- Contains: `pom.xml`, `drivers.xml`, and the `src/test/java` tree for TestBench tests.
- Key files: `iotter-flow-it/pom.xml`

## Key File Locations

**Entry Points:**
- `pom.xml`: Parent reactor and module list.
- `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/Application.java`: Vaadin UI application bootstrap.
- `iotter-flow-rest/src/main/java/it/thisone/iotter/rest/JerseyApplication.java`: REST application bootstrap.

**Configuration:**
- `iotter-flow-rest/src/main/java/it/thisone/iotter/config/AppConfig.java`: Shared properties and message source setup for boot apps.
- `iotter-flow-rest/src/main/java/it/thisone/iotter/config/SecurityConfig.java`: Spring Security session policy.
- `iotter-rest-endpoints/src/main/java/it/thisone/iotter/config/JerseyConfig.java`: Jersey support beans.
- `iotter-flow-ui/frontend/themes/iotter/theme.json`: Active Vaadin theme metadata.

**Core Logic:**
- `iotter-backend/src/main/java/it/thisone/iotter/persistence/service/`: Business services and transaction boundaries.
- `iotter-backend/src/main/java/it/thisone/iotter/persistence/model/`: JPA entity model.
- `iotter-core/src/main/java/it/thisone/iotter/`: Cross-cutting constants, events, enums, and exceptions.

**Testing:**
- `iotter-flow-it/src/test/java`: Browser-driven integration tests when present.
- `iotter-flow-it/pom.xml`: Failsafe, Jetty, and driver-download setup for TestBench.

## Naming Conventions

**Files:**
- Java classes use `UpperCamelCase.java`, with descriptive suffixes by role: `*View`, `*Listing`, `*Form`, `*Service`, `*Dao`, `*Repository`, `*Config`.
- Frontend resource files use lowercase dash-separated names such as `iotter-flow-ui/frontend/src/gridstack-board.js` and `iotter-flow-ui/frontend/styles/shared-styles.css`.

**Directories:**
- Maven modules use lowercase dash-separated names such as `iotter-flow-ui-core` and `iotter-rest-client-endpoints`.
- Java packages stay under `it.thisone.iotter.*`, usually segmented by concern such as `ui.devices`, `persistence.service`, or `rest`.

## Where to Add New Code

**New Feature:**
- Primary UI code: Put new routed screens in `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/<feature>/`.
- Shared UI primitives: Put reusable components, validators, fields, or base classes in `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/**`.
- Backend services and entities: Put business logic in `iotter-backend/src/main/java/it/thisone/iotter/persistence/service/` and persistence types in `iotter-backend/src/main/java/it/thisone/iotter/persistence/model/`.
- REST endpoints: Put new resources/providers in `iotter-rest-endpoints/src/main/java/it/thisone/iotter/rest/`; only boot-level wiring belongs in `iotter-flow-rest`.
- Tests: Put browser integration tests in `iotter-flow-it/src/test/java`.

**New Component/Module:**
- Implementation: If the code is a feature-specific Vaadin screen, place it in `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/<feature>/`.
- Implementation: If the code is framework-like and reused by multiple screens, place it in `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/`, `.../fields/`, `.../validators/`, `.../providers/`, or another matching shared package.
- Implementation: If the code is a new transport/integration capability, prefer the dedicated module namespace such as `iotter-mqtt`, `iotter-integration`, `iotter-cassandra`, or `iotter-exporter` instead of adding it to `iotter-flow-ui` or `iotter-flow-rest`.

**Utilities:**
- Shared backend helpers: Place them in `iotter-core/src/main/java/it/thisone/iotter/util/` or another `iotter-core` package if they are not persistence-specific.
- Shared UI helpers: Place them in `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/` or `iotter-flow-ui-core/src/main/java/it/thisone/iotter/util/`.

## Special Directories

**`iotter-flow-ui/frontend/generated/`:**
- Purpose: Vaadin-generated frontend bridge files and generated theme imports.
- Generated: Yes.
- Committed: Yes, present in the workspace and should be treated as generated output rather than hand-authored source.

**`docs/archive/`:**
- Purpose: Preserve historical migration and troubleshooting documents.
- Generated: No.
- Committed: Yes.

**`.planning/codebase/`:**
- Purpose: Store generated codebase maps for planning/execution tooling.
- Generated: Yes.
- Committed: Usually yes in the working tree used by the GSD workflow.

**`.agents/`:**
- Purpose: Store repository-local implementation plans and research artifacts.
- Generated: Mixed; some files are authored plans, some are temporary execution artifacts.
- Committed: Partially, depending on workflow conventions in this repo.

---

*Structure analysis: 2026-03-27*
