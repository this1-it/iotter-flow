# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

I will start a coding session with you.

Output the message number after each message without explanation.

Every 4th message, remind yourself of these rules:

MANDATORY CHECKS:
* Only change what's explicitly requested - NEVER modify unrelated code
* NO placeholders (YOUR_API_KEY, TODO) - use proper variables/config
* Questions = Answers ONLY - Don't modify code unless asked to "change/update/fix"
* NO assumptions - ASK for missing information
* Security first - NO secrets in client code, use env variables
* Add intelligent logging to core flows automatically
* Clean unused code when making changes
* Provide CODE EVIDENCE when asked about implementations
* During migration tasks and refactoring, use IDE diagnostics instead of Maven build commands to check for compilation issues. This will help identify problems more quickly without going through the full build process.
* Output the message number after each message without explanation.
* Every 4th message, remind yourself of these rules:
If you violate these rules, you are breaking critical development protocols.

## Project Overview

iotter-flow is a multi-module Java/Maven IoT application with a Vaadin Flow UI (Vaadin 14.8.14). The system provides device management, data collection, and visualization capabilities with MQTT integration, Cassandra time-series data storage, and a Spring Boot-based web interface.

Many of the patterns used in Vaadin 8 are no longer compatible with Vaadin Flow.

In particular, Vaadin Flow introduces a clear and fundamental change in the programming model compared to Vaadin 8:

Components must no longer access the Spring context manually.

Replicating the old pattern of an interface that pulls services from the Spring context is no longer meaningful nor supported.

It is no longer possible to use com.vaadin.flow.component.UI in the same way as com.vaadin.ui.UI was used in Vaadin 8 (for example, as a global access point to application or Spring resources).

Vaadin Flow is based on dependency injection, UI-scoped state, and explicit service wiring, and existing Vaadin 8 patterns must be refactored accordingly.

When migrating the code, if you encounter a reference to it.thisone.iotter.ui.main.IMainUI, that interface was used to pull services from the Spring context.

Likewise, some methods from it.thisone.iotter.ui.common.UIUtils are no longer available. They were intentionally removed because they relied on patterns such as:

```
((IMainUI) UI.getCurrent())
```

These patterns are not compatible with Vaadin Flow and have therefore been deliberately eliminated during the migration.




## Build and Run Commands

### Building the Project

Build all modules from repository root:
```bash
mvn clean install
```

Build specific module:
```bash
mvn -pl iotter-flow-ui clean package
```

Production build with frontend optimization:
```bash
mvn -pl iotter-flow-ui -Pproduction package
```

### Running the Application

Run UI in development mode with hot-reload:
```bash
mvn -pl iotter-flow-ui spring-boot:run
```

Default URL: http://localhost:8080/

The UI module uses `spring-boot:run` as its default goal, so you can also run:
```bash
mvn -pl iotter-flow-ui
```

### Testing

Run integration tests (requires Chrome and TestBench license):
```bash
mvn -pl iotter-flow-it -Pintegration-tests verify
```

With custom ChromeDriver path:
```bash
mvn -pl iotter-flow-it -Pintegration-tests verify -Dwebdriver.chrome.driver=/path/to/chromedriver
```

The `integration-tests` profile automatically:
- Downloads ChromeDriver to `iotter-flow-it/drivers/`
- Starts Jetty server on port 8080
- Runs TestBench tests (files matching `*IT.java`)
- Stops Jetty after tests complete

### Frontend Development

This project uses Node.js 14 for frontend builds. Set default Node version:
```bash
nvm alias default 14
```

The Vaadin Maven plugin handles frontend builds automatically during `mvn install`. Frontend resources are in `iotter-flow-ui/frontend/`.

## Architecture

### Module Structure

The project follows a layered architecture with clear separation of concerns:

```
iotter-flow-core          → Core domain models, enums, security config, scheduling
iotter-flow-rest-model    → REST API models and DTOs
iotter-flow-cassandra-model → Cassandra entity definitions
iotter-flow-cassandra     → Cassandra data access layer, time-series queries
iotter-flow-mqtt          → MQTT integration via Spring Integration
iotter-flow-exporter      → Data export functionality (CSV, Excel)
iotter-flow-backend       → Service layer, JPA persistence, business logic
iotter-flow-integration   → Email notifications, cross-module orchestration
iotter-flow-ui-core       → Vaadin UI core components (migration in progress)
iotter-flow-ui-shim       → Compatibility shim for Vaadin 8 → Flow migration
iotter-flow-ui            → Spring Boot + Vaadin Flow UI (main application)
iotter-flow-it            → Integration tests with Vaadin TestBench
```

### Key Dependencies

**Dependency flow (bottom to top):**
- `iotter-flow-core` is the foundation (no internal dependencies)
- `iotter-flow-backend` depends on `iotter-flow-core`
- `iotter-flow-cassandra` depends on `iotter-flow-core` and `iotter-flow-cassandra-model`
- `iotter-flow-mqtt` depends on `iotter-flow-core`
- `iotter-flow-integration` depends on `iotter-flow-backend`, `iotter-flow-cassandra`, `iotter-flow-mqtt`, `iotter-flow-exporter`, `iotter-flow-rest-model`
- `iotter-flow-ui` depends on `iotter-flow-backend` (which transitively brings in most dependencies)

### Technology Stack

- **Java 8** (source/target 1.8)
- **Spring Framework 5.3.39** (core, context, security)
- **Spring Boot 2.7.18** (UI module only)
- **Vaadin 14.8.14** (Flow framework for Java-based UI)
- **Cassandra** with Spring Data Cassandra 3.4.18 and DataStax driver 4.17.0
- **JPA/EclipseLink 2.6.2** for relational persistence
- **MQTT** via Spring Integration 5.5.20 and Eclipse Paho 1.2.5
- **Quartz 2.2.1** for job scheduling
- **Webpack 4** for frontend bundling

### Key Application Components

**Main entry point:** `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/Application.java` - Spring Boot application

**UI structure:**
- `MainLayout.java` - Main application layout
- `Menu.java` - Navigation menu
- `ErrorView.java` - Error handling view
- Views organized in packages under `it/thisone/iotter/ui/` (e.g., `about`, `authentication`)

**Backend services** in `iotter-backend/src/main/java/it/thisone/iotter/persistence/service/`:
- `UserService` - User management
- `DeviceModelService` - Device model management
- `NetworkService` - Network/group management
- `TracingService` - Data tracing/telemetry
- `ImageDataService` - Image data handling

**Configuration:**
- `PersistenceJPAConfig.java` - JPA/EclipseLink configuration
- `CassandraConfig.java` - Cassandra connection and queries
- `ApplicationLifecycle.java` - Application initialization hooks

### Vaadin Migration Context

This codebase is undergoing migration from Vaadin 8 to Vaadin Flow (14). The `iotter-flow-ui-shim` module provides compatibility layer for chart components (`com.vaadin.addon.charts.*`). See `docs/vaadin8-to-flow-data-compatibility.md` and `docs/vaadin_8_to_flow_search_replace.md` for migration details.

## Code Style

- **Indentation:** 4 spaces
- **Braces:** Same line (K&R style)
- **Naming:**
  - Packages: lowercase (e.g., `it.thisone.iotter.persistence.service`)
  - Classes: PascalCase (e.g., `UserService`, `AboutView`)
  - Methods/fields: camelCase
- **UI naming:** Views and components named by purpose (e.g., `AboutView`, `MainLayout`)
- **No formatter configured:** Keep edits consistent with surrounding code

## ExecPlans for Complex Features

For complex features or significant refactors, use an ExecPlan following the template in `.agents/_template/PLAN.md`.

**Process:**
1. Copy template: `cp -r .agents/_template .agents/<feature-name>`
2. Remove the preamble and adapt to your needs
3. Write plan to `.agents/<feature-name>/PLAN.md`
4. Place temporary research/clones in a `.gitignored` subdirectory of `.agents`
5. Update plan as work progresses with timestamps in Progress section

Existing plans: `customfield-to-abstractcompositefield`, `vaadin14-ui-core-migration`

## Testing Conventions

- **Unit tests:** JUnit 4, files named `*Test.java`, typically in backend module
- **Integration tests:** Vaadin TestBench with Failsafe, files named `*IT.java` in `iotter-flow-it` module
- Test names should be descriptive of behavior being tested
- Run relevant module tests before submitting PRs

## Commit and PR Guidelines

- **Commit style:** Short, imperative messages following existing history (e.g., "renamed modules", "added drop-in flow replacements")
- **PR content:**
  - Concise summary of changes
  - Commands run and results (e.g., `mvn clean install` output)
  - Screenshots for UI changes
  - Link related issues/tickets when applicable

## Important Notes

- **Frontend builds:** Managed by Vaadin Maven plugin; avoid manual edits in `iotter-flow-ui/target`
- **Chrome driver:** Integration tests require compatible ChromeDriver; use the `drivers.xml` in project root to configure versions
- **Production mode:** The `-Pproduction` profile triggers frontend optimization (minification, bundling) via `vaadin-maven-plugin:build-frontend`
- **Dev workflow:** Changes to Java code hot-reload with Spring Boot DevTools (included in UI module dependencies)
