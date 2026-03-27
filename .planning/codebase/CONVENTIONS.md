# Coding Conventions

**Analysis Date:** 2026-03-27

## Naming Patterns

**Files:**
- Use `UpperCamelCase.java` for Java classes and components under `iotter-flow-ui/src/main/java` and `iotter-flow-ui-core/src/main/java`, for example `SecurityConfig.java`, `LoginScreen.java`, `UserForm.java`, and `PopupNotification.java`.
- Use lowercase, dash-separated filenames for frontend assets under `iotter-flow-ui/frontend/src` and `iotter-flow-ui/frontend/styles`, for example `gridstack-board.js`, `chartjs-init.js`, and `shared-styles.css`.
- Use `*IT.java` for browser-driven integration tests in `iotter-flow-it/src/test/java`, for example `LoginScreenIT.java` and `AboutViewIT.java`.
- Use `*Element.java` for TestBench page objects and element wrappers in `iotter-flow-it/src/test/java`, for example `LoginFormElement.java` and `MainLayoutElement.java`.

**Functions:**
- Use `lowerCamelCase` for Java methods and JavaScript methods, for example `filterChain()` in `iotter-flow-ui/src/main/java/it/thisone/iotter/config/SecurityConfig.java`, `buildUI()` in `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/authentication/LoginScreen.java`, and `loadLayout()` in `iotter-flow-ui/frontend/src/gridstack-board.js`.
- Name test methods as behavior statements with underscores, for example `loginForm_isLumoThemed()` in `iotter-flow-it/src/test/java/it/thisone/iotter/ui/authentication/LoginScreenIT.java`.

**Variables:**
- Use `lowerCamelCase` for fields and locals, for example `authManager`, `loginInformation`, `availableRoles`, and `layoutJson` in `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/authentication/LoginScreen.java`, `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/users/UserForm.java`, and `iotter-flow-ui/frontend/src/gridstack-board.js`.
- Reserve `UPPER_SNAKE_CASE` for constants, for example `SERVER_PORT` and `SUPPORTED_LOCALES` in `iotter-flow-it/src/test/java/it/thisone/iotter/ui/AbstractViewTest.java` and `iotter-flow-ui/src/main/java/it/thisone/iotter/i18n/FlowI18NProvider.java`.

**Types:**
- Use `UpperCamelCase` for classes, nested enums, and interfaces, including `Type` in `iotter-flow-ui-core/src/main/java/it/thisone/iotter/util/PopupNotification.java` and `IProvisioningProvider` in `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/provisioning/IProvisioningProvider.java`.
- Prefix some UI-facing interfaces with `I` in `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/ifc`, for example `IUiFactory.java` and `IDeviceUiFactory.java`. Follow the existing package style when adding to that area.

## Code Style

**Formatting:**
- No formatter is enforced in Maven or frontend config. No `.editorconfig`, `.prettierrc`, `eslint.config.*`, `checkstyle.xml`, or Spotless configuration is detected at the repository root or in `iotter-flow-ui/package.json`.
- Follow surrounding file style instead of normalizing aggressively. Indentation is inconsistent across modules: `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/main/MainView.java` uses tabs, while `iotter-flow-ui/src/main/java/it/thisone/iotter/config/SecurityConfig.java` and `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/authentication/LoginScreen.java` use 4 spaces.
- Keep line wrapping and blank-line usage close to the local file. `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/UIUtils.java` shows that whitespace can already be uneven; preserve local readability rather than bulk-reformatting.

**Linting:**
- No Java or frontend lint tool is enforced in the build files that were inspected: `pom.xml`, `iotter-flow-it/pom.xml`, `iotter-flow-ui/pom.xml`, `iotter-flow-ui-core/pom.xml`, and `iotter-flow-ui/package.json`.
- Treat compilation and targeted test execution as the primary quality gates.

## Import Organization

**Order:**
1. `java.*` imports first when present, for example `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/users/UserForm.java`.
2. Third-party framework imports next, grouped by library such as `org.springframework.*`, `org.slf4j.*`, `com.vaadin.*`, and Selenium/TestBench imports in `iotter-flow-it/src/test/java`.
3. Project imports last, for example `it.thisone.iotter.*` imports at the bottom of `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/authentication/LoginScreen.java`.

**Path Aliases:**
- No Java package aliasing is used. Import by full package name under `it.thisone.iotter.*`.
- Frontend code uses normal ESM imports and Vaadin’s `Frontend/` virtual path, for example `Frontend/generated/jar-resources/chart/Moment.js` in `iotter-flow-ui/frontend/src/chartjs-init.js`.

## Error Handling

**Patterns:**
- Convert framework exceptions into user-facing messages in the UI layer, for example `buildFailureMessage(AuthenticationException e)` in `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/authentication/LoginScreen.java`.
- Use early returns for guard clauses in JavaScript and utility code, for example `if (!this._grid) return;` in `iotter-flow-ui/frontend/src/gridstack-board.js`.
- Catch broad exceptions only at integration boundaries, log the issue, and return a safe fallback, for example `getTranslation()` in `iotter-flow-ui/src/main/java/it/thisone/iotter/i18n/FlowI18NProvider.java`.
- Use `Assert`-based failure messages in integration tests instead of custom helpers, for example `LoginScreenIT.java` and `AboutViewIT.java` in `iotter-flow-it/src/test/java`.

## Logging

**Framework:** `org.slf4j.Logger` with `org.slf4j.LoggerFactory`

**Patterns:**
- Declare a class logger near the top of the class, usually as `private static final Logger logger`, for example `iotter-flow-ui/src/main/java/it/thisone/iotter/i18n/FlowI18NProvider.java` and `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/AuthenticationErrorView.java`.
- Some files use weaker variants such as `public static Logger logger` without `final`, for example `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/users/UserForm.java` and `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/model/ChannelAdapterDataProvider.java`. Match the local file rather than refactoring unrelated logging style.
- Frontend JavaScript uses `console.warn` sparingly for client-side parse/runtime issues, for example `iotter-flow-ui/frontend/src/gridstack-board.js`.

## Comments

**When to Comment:**
- Add short comments where Flow migration constraints or Vaadin lifecycle details are non-obvious, for example the DOM lifecycle notes in `iotter-flow-ui/frontend/src/gridstack-board.js`.
- Keep migration placeholders as `TODO(...)` comments tied to a specific constraint, as seen throughout `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/maps` and `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/charts`.
- Avoid redundant comments for straightforward setters and layout code.

**JSDoc/TSDoc:**
- Javadoc is used selectively for framework-facing classes, helpers, and tests, for example `iotter-flow-it/src/test/java/it/thisone/iotter/ui/AbstractViewTest.java`, `iotter-flow-ui/src/main/java/it/thisone/iotter/i18n/FlowI18NProvider.java`, and `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/authentication/LoginScreen.java`.
- Frontend files do not use JSDoc. Use plain inline comments instead.

## Function Design

**Size:** 
- Keep configuration and entry-point methods small, for example `filterChain()` in `iotter-flow-ui/src/main/java/it/thisone/iotter/config/SecurityConfig.java`.
- UI forms and views often centralize setup in larger `build*`, `initialize*`, or `populate*` methods, for example `buildUI()` in `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/authentication/LoginScreen.java` and `initializeFields()` in `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/users/UserForm.java`.

**Parameters:** 
- Prefer constructor injection for Spring-managed UI classes, for example `LoginScreen(AuthManager authManager)` in `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/authentication/LoginScreen.java`.
- Pass services, event bus handles, and context objects explicitly into forms and views, for example the `UserForm(...)` constructor in `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/users/UserForm.java`.

**Return Values:** 
- Use `void` for imperative UI-building methods and event handlers.
- Return simple safe defaults on invalid input, for example `""` or `null` in `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/UIUtils.java`.

## Module Design

**Exports:** 
- Java modules expose concrete classes directly through their packages. There are no barrel files or facade exports in the Java code inspected.
- Frontend custom elements are registered directly in-source, for example `customElements.define('gridstack-board', GridstackBoard);` in `iotter-flow-ui/frontend/src/gridstack-board.js`.

**Barrel Files:** 
- Not used in the handwritten frontend sources under `iotter-flow-ui/frontend/src`.
- Ignore generated aggregators under `iotter-flow-ui/frontend/generated`; they are generated output, not a convention target.

---

*Convention analysis: 2026-03-27*
