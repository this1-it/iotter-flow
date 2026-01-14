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
