# Development Guidelines

This is a Vaadin Flow project that rewrites a Vaadin 8 application.
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


## i18n support using iotter-flow-ui/src/main/java/it/thisone/iotter/i18n/FlowI18NProvider.java

 getTranslation() is a method provided by com.vaadin.flow.component.Component. It's available in any class that extends a Vaadin Flow component: 
                                                                                                                                                  
  ✓ Works in these classes:                                                                                                                       
  - Classes extending VerticalLayout, HorizontalLayout, FlexLayout                                                                                
  - Classes extending Div, Span, Button                                                                                                           
  - Any custom component extending Component                                                                                                      
 


# Repository Guidelines


## Project Structure & Module Organization
- `iotter-flow-ui/` is the Vaadin Flow UI module (Java views, resources, and web assets). Java lives in `iotter-flow-ui/src/main/java`, static assets in `iotter-flow-ui/src/main/webapp`, and frontend resources in `iotter-flow-ui/frontend`.
- `iotter-flow-it/` contains integration/UI tests using Vaadin TestBench, under `iotter-flow-it/src/test/java`.
- The root `pom.xml` aggregates the modules for multi-module builds.

## ExecPlans

When writing complex features or significant refactors, use an ExecPlan (as described in .agents/_template/PLANS.md) from design to implementation. Write new plans to the .agents dir, Place any temporary research, clones, etc., in a .gitignored subdirectory of .agents. But for permanent features, open a new subdir under .agents, so for example for the orchestrator, we would use .agents/orchestrator/PLAN.md ... start by doing `cp -r .agents/_template .agents/orchestrator`. You need to remove the preamble of each plan, do not copy blindly. And if you need to take a different approach than TDD just for a particular plan, put that clearly in the plan.


## Build, Test, and Development Commands
- `mvn clean install` builds all modules.
- `mvn -pl iotter-flow-ui jetty:run` runs the UI locally via Jetty (default goal in the UI module).
- `mvn -pl iotter-flow-ui -Pproduction package` builds the UI in production mode (frontend bundle generated).
- `mvn -pl iotter-flow-it -Pintegration-tests verify -Dwebdriver.chrome.driver=/path/to/chromedriver` runs integration tests; the profile downloads drivers into `iotter-flow-it/drivers/`.

## Coding Style & Naming Conventions
- Java: 4-space indentation, braces on the same line, standard Vaadin/Java conventions.
- Packages are lower-case (for example `com.vaadin.samples.*`), classes use `PascalCase`, methods and fields use `camelCase`.
- UI views and components are named by purpose (for example `AboutView`, `SampleCrudView`).
- No formatter or linter is configured; keep edits consistent with existing files.

## Testing Guidelines
- Unit tests use JUnit 4 (`*Test.java`) in the backend module.
- Integration/UI tests use Vaadin TestBench with Failsafe (`*IT.java`) in `iotter-flow-it`.
- Keep test names descriptive of the behavior, and prefer running the relevant module tests before PRs.

## Commit & Pull Request Guidelines
- Git history shows short, imperative commit messages (for example “renamed modules”); follow that style.
- PRs should include a concise summary, testing performed (command + result), and screenshots for UI changes.
- Link related issues/tickets when applicable.

## Configuration Tips
- The UI module relies on the Vaadin Maven plugin to prepare/build the frontend; avoid manual edits under `iotter-flow-ui/target`.
- Integration tests require a compatible ChromeDriver on your machine unless the downloader profile is used.
