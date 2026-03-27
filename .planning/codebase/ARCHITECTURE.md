# Architecture

**Analysis Date:** 2026-03-27

## Pattern Overview

**Overall:** Multi-module layered monolith with separate Spring Boot entry modules for the Vaadin UI and Jersey REST API.

**Key Characteristics:**
- The reactor root `pom.xml` composes domain, persistence, integration, REST, UI, and test modules into one Maven workspace.
- The executable applications live in `iotter-flow-ui` and `iotter-flow-rest`, while most reusable code lives in library modules such as `iotter-flow-backend`, `iotter-flow-ui-core`, `iotter-core`, `iotter-cassandra`, and `iotter-rest-endpoints`.
- The UI is split into reusable framework code in `iotter-flow-ui-core`, compatibility helpers in `iotter-flow-ui-shim`, and feature views in `iotter-flow-ui`.

## Layers

**Boot / Composition Layer:**
- Purpose: Assemble Spring context and start an executable application.
- Location: `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/Application.java`, `iotter-flow-rest/src/main/java/it/thisone/iotter/rest/JerseyApplication.java`
- Contains: `@SpringBootApplication` classes, top-level component scan, application theme, runtime exclusions, Jersey bootstrap.
- Depends on: All modules under the `it.thisone.iotter` package tree via component scanning.
- Used by: `mvn -pl iotter-flow-ui -am spring-boot:run` and `mvn -pl iotter-flow-rest -am spring-boot:run`.

**Presentation Layer (Vaadin UI):**
- Purpose: Render browser-facing screens, navigation, CRUD listings/forms, dashboards, maps, charts, and authentication pages.
- Location: `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/**`
- Contains: Routed views such as `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/devices/DevicesView.java`, feature listings/forms, `MainLayout`, login screen, and UI event bridge classes.
- Depends on: `iotter-flow-ui-core`, `iotter-flow-ui-shim`, backend services, security classes, Vaadin Flow, and frontend assets under `iotter-flow-ui/frontend`.
- Used by: End users accessing the Vaadin app.

**Presentation Framework Layer (UI Core):**
- Purpose: Provide reusable UI primitives and patterns so feature modules stay thin.
- Location: `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/**`
- Contains: Base views/forms/listings such as `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/BaseView.java`, `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/AbstractBaseEntityForm.java`, `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/AbstractBaseEntityListing.java`, shared fields, validators, wizard framework, chart helpers, and `BackendServices`.
- Depends on: Vaadin libraries, `iotter-flow-ui-shim`, backend/integration/exporter modules.
- Used by: Feature-specific UI code in `iotter-flow-ui`.

**REST API Layer:**
- Purpose: Expose HTTP endpoints and request/response infrastructure.
- Location: `iotter-rest-endpoints/src/main/java/it/thisone/iotter/rest/**`, `iotter-flow-rest/src/main/java/it/thisone/iotter/config/**`
- Contains: JAX-RS resources such as `iotter-rest-endpoints/src/main/java/it/thisone/iotter/rest/DeviceProvisioningService.java`, serializers, interceptors, exception mappers, filters, and Jersey configuration in `iotter-rest-endpoints/src/main/java/it/thisone/iotter/config/JerseyConfig.java` plus `iotter-flow-rest/src/main/java/it/thisone/iotter/config/RestJerseyConfig.java`.
- Depends on: Backend services, integration services, `iotter-rest-model`, Jackson, Jersey, Spring Boot.
- Used by: External clients and internal integrations.

**Application / Service Layer:**
- Purpose: Hold business workflows, transactional boundaries, caching, and orchestration across persistence and integrations.
- Location: `iotter-backend/src/main/java/it/thisone/iotter/persistence/service/**`, plus service-style facades in `iotter-integration`, `iotter-mqtt`, `iotter-cassandra`, and `iotter-exporter`
- Contains: Services such as `iotter-backend/src/main/java/it/thisone/iotter/persistence/service/DeviceService.java`, `UserService`, `NetworkService`, `GroupWidgetService`, and UI-facing aggregation via `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/providers/BackendServices.java`.
- Depends on: DAO/repository interfaces, domain models, core events/enums, and external integration modules.
- Used by: Vaadin views/forms, Jersey resources, background subscribers.

**Persistence Layer:**
- Purpose: Access relational data and encapsulate database queries.
- Location: `iotter-backend/src/main/java/it/thisone/iotter/persistence/dao/**`, `iotter-backend/src/main/java/it/thisone/iotter/persistence/ifc/**`, `iotter-backend/src/main/java/it/thisone/iotter/persistence/repository/**`
- Contains: DAO implementations such as `iotter-backend/src/main/java/it/thisone/iotter/persistence/dao/DeviceDao.java`, Spring Data repositories such as `iotter-backend/src/main/java/it/thisone/iotter/persistence/repository/BaseEntityRepository.java`, and JPA entities under `iotter-backend/src/main/java/it/thisone/iotter/persistence/model/**`.
- Depends on: JPA/EclipseLink, Spring Data JPA, core enums and exceptions.
- Used by: Service layer.

**Cross-Cutting Core Layer:**
- Purpose: Provide common enums, exceptions, event infrastructure, caching config, and application-level support classes.
- Location: `iotter-core/src/main/java/it/thisone/iotter/**`
- Contains: `config`, `eventbus`, `exceptions`, `common`, `concurrent`, and enum packages such as `iotter-core/src/main/java/it/thisone/iotter/config/EventBusConfig.java` and `iotter-core/src/main/java/it/thisone/iotter/eventbus/DeviceUpdatedEvent.java`.
- Depends on: Spring core libraries and basic Java dependencies.
- Used by: Backend, REST, integration, and UI modules.

## Data Flow

**Vaadin CRUD Flow:**

1. A route view such as `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/devices/DevicesView.java` is entered through `MainLayout`.
2. The view obtains a feature listing from Spring, initializes it with context such as the active `Network`, and adds the listing layout to the page.
3. The listing extends `AbstractBaseEntityListing` from `iotter-flow-ui-core` and delegates entity editing to an `AbstractBaseEntityForm` subclass such as `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/devices/DeviceForm.java`.
4. Forms and listings call domain services directly, usually through injected services or the `BackendServices` facade in `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/providers/BackendServices.java`.
5. Service classes such as `iotter-backend/src/main/java/it/thisone/iotter/persistence/service/DeviceService.java` call DAOs/repositories and mutate JPA entities under `iotter-backend/src/main/java/it/thisone/iotter/persistence/model/**`.

**REST Request Flow:**

1. `iotter-flow-rest/src/main/java/it/thisone/iotter/rest/JerseyApplication.java` starts the Spring Boot REST runtime.
2. `iotter-flow-rest/src/main/java/it/thisone/iotter/config/RestJerseyConfig.java` scans `it.thisone.iotter.rest` for resources and providers.
3. A resource such as `iotter-rest-endpoints/src/main/java/it/thisone/iotter/rest/DeviceConfigurationService.java` handles the HTTP request.
4. The resource invokes service-layer collaborators from backend and integration modules.
5. Results are serialized through Jackson/Jersey providers such as `iotter-rest-endpoints/src/main/java/it/thisone/iotter/rest/ObjectMapperResolver.java` and exception mappers under the same package.

**Backend Event to UI Flow:**

1. Backend code publishes a Spring event that extends the targeted UI event contract used by `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/eventbus/SpringToUIEventBridge.java`.
2. `SpringToUIEventBridge` converts the Spring event into a UI event and routes it by key through `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/eventbus/UIEventBusRegistry.java`.
3. `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/eventbus/UIEventBusInitializer.java` ensures each Vaadin `UI` gets its own `UIEventBus` instance and unregisters it on detach.
4. Target UIs receive the event inside `ui.access(...)`, and feature components refresh themselves without touching backend threads directly.

**State Management:**
- Server-side state is centered on Spring beans, JPA entities, and service-layer transactions.
- Vaadin UI state is component-driven and scoped to the current `UI` or session.
- Frontend state is minimal; the browser shell mostly renders server-driven Vaadin components plus custom JS/CSS assets from `iotter-flow-ui/frontend`.

## Key Abstractions

**Reusable CRUD Shell:**
- Purpose: Standardize list/detail/form behavior across many entity screens.
- Examples: `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/AbstractBaseEntityListing.java`, `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/AbstractBaseEntityForm.java`, `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/users/UsersListing.java`
- Pattern: Template Method with feature subclasses supplying editors, details, removal logic, and feature-specific grid/filter setup.

**Backend Service Aggregator for UI:**
- Purpose: Reduce constructor fan-out in complex UI components by grouping service dependencies.
- Examples: `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/providers/BackendServices.java`, `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/graphicwidgets/GraphicWidgetFactory.java`
- Pattern: Facade over many service and integration beans.

**Targeted UI Event Routing:**
- Purpose: Deliver backend-originated events to only the relevant open Vaadin UIs.
- Examples: `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/eventbus/SpringToUIEventBridge.java`, `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/eventbus/UIEventBusRegistry.java`, `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/eventbus/UIEventBusHelper.java`
- Pattern: Spring event listener plus per-UI event bus registry with routing keys.

**Domain Persistence Model:**
- Purpose: Keep the business entities and database mappings centralized and reusable across UI and REST surfaces.
- Examples: `iotter-backend/src/main/java/it/thisone/iotter/persistence/model/Device.java`, `iotter-backend/src/main/java/it/thisone/iotter/persistence/model/Network.java`, `iotter-backend/src/main/java/it/thisone/iotter/persistence/repository/DeviceRepository.java`
- Pattern: JPA entity model with DAO/service access around it.

## Entry Points

**Vaadin UI Application:**
- Location: `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/Application.java`
- Triggers: Local dev boot, packaged UI runtime.
- Responsibilities: Start Spring Boot, expose the Vaadin theme, and scan application beans while excluding the shared `SecurityConfig`.

**REST Application:**
- Location: `iotter-flow-rest/src/main/java/it/thisone/iotter/rest/JerseyApplication.java`
- Triggers: Local dev boot, packaged REST runtime.
- Responsibilities: Start Spring Boot for the REST surface and load Jersey resources/providers.

**Main Layout / Route Shell:**
- Location: `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/MainLayout.java`
- Triggers: Every routed Vaadin navigation using `layout = MainLayout.class`.
- Responsibilities: Navigation chrome, login gate forwarding, user info display, client timezone capture, and route title handling.

**Jersey Resource Scan:**
- Location: `iotter-flow-rest/src/main/java/it/thisone/iotter/config/RestJerseyConfig.java`
- Triggers: REST application startup.
- Responsibilities: Register the `it.thisone.iotter.rest` package as the source of endpoints and providers.

## Error Handling

**Strategy:** Let each boundary own translation. Backend services throw or wrap domain exceptions, REST adapters convert them into HTTP responses, and the UI mostly handles errors through component-level notifications/dialogs rather than a single global handler.

**Patterns:**
- REST providers centralize transport-level handling in `iotter-rest-endpoints/src/main/java/it/thisone/iotter/rest/GenericExceptionMapper.java`, `ConstraintViolationExceptionMapper.java`, and related filters/readers.
- Service methods such as `iotter-backend/src/main/java/it/thisone/iotter/persistence/service/DeviceService.java` catch lower-level exceptions where needed, log them, and rethrow `BackendServiceException` or return `null` on legacy paths.
- UI forms and listings inherit shared commit and validation behavior from `AbstractBaseEntityForm` and `AbstractBaseEntityListing`, which keeps user-facing CRUD behavior consistent.

## Cross-Cutting Concerns

**Logging:** SLF4J is used across modules. Representative files are `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/eventbus/SpringToUIEventBridge.java`, `iotter-flow-rest/src/main/java/it/thisone/iotter/config/AppConfig.java`, and `iotter-backend/src/main/java/it/thisone/iotter/persistence/service/DeviceService.java`.

**Validation:** Vaadin Binder validation lives in UI forms under `iotter-flow-ui` and reusable validators under `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/validators/**`. REST validation is supplemented by Jersey exception mappers in `iotter-rest-endpoints/src/main/java/it/thisone/iotter/rest/**`.

**Authentication:** Shared security infrastructure is configured in `iotter-flow-rest/src/main/java/it/thisone/iotter/config/SecurityConfig.java` and consumed in the UI through `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/AuthenticatedUser.java` plus route guarding in `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/MainLayout.java`.

---

*Architecture analysis: 2026-03-27*
