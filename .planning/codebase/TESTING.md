# Testing Patterns

**Analysis Date:** 2026-03-27

## Test Framework

**Runner:**
- JUnit 4 with Vaadin TestBench integration tests.
- Config: `iotter-flow-it/pom.xml`

**Assertion Library:**
- `org.junit.Assert` from JUnit 4, as used in `iotter-flow-it/src/test/java/it/thisone/iotter/ui/authentication/LoginScreenIT.java` and `iotter-flow-it/src/test/java/it/thisone/iotter/ui/about/AboutViewIT.java`.

**Run Commands:**
```bash
mvn -pl iotter-flow-it -Pintegration-tests verify    # Run the TestBench integration suite
mvn -pl iotter-flow-ui -am spring-boot:run           # Start the UI locally for manual verification
Not applicable                                       # Watch mode is not configured
Not applicable                                       # Coverage command is not configured
```

## Test File Organization

**Location:**
- Automated tests are centralized in the dedicated integration-test module `iotter-flow-it/src/test/java`.
- No handwritten unit-test source files were detected under `iotter-flow-ui/src/test/java` or `iotter-flow-ui-core/src/test/java`.

**Naming:**
- Name browser-driven tests `*IT.java`, for example `iotter-flow-it/src/test/java/it/thisone/iotter/ui/authentication/LoginScreenIT.java`.
- Name reusable TestBench wrappers `*Element.java`, for example `iotter-flow-it/src/test/java/it/thisone/iotter/ui/authentication/LoginFormElement.java`.
- Use package structure that mirrors the UI area under test, for example `it/thisone/iotter/ui/about` and `it/thisone/iotter/ui/authentication`.

**Structure:**
```text
iotter-flow-it/src/test/java/it/thisone/iotter/ui/
├── AbstractViewTest.java
├── MainLayoutElement.java
├── about/AboutViewIT.java
└── authentication/
    ├── LoginFormElement.java
    └── LoginScreenIT.java
```

## Test Structure

**Suite Organization:**
```typescript
public class LoginScreenIT extends AbstractViewTest {

    @Test
    public void loginAsAdmin_hasAdminViewLink() {
        $(LoginFormElement.class).first().login("admin", "admin");

        Assert.assertTrue("Expected link to admin view",
                $(MainLayoutElement.class).first().hasMenuLink("admin"));
    }
}
```

**Patterns:**
- Extend `AbstractViewTest` from `iotter-flow-it/src/test/java/it/thisone/iotter/ui/AbstractViewTest.java` for all browser tests so driver setup and base URL navigation stay consistent.
- Let the base class open the route in `@Before setup()`, then drive the UI via TestBench `$()` selectors.
- Use `given/when/then` comments for readability inside tests, as in `iotter-flow-it/src/test/java/it/thisone/iotter/ui/about/AboutViewIT.java`.
- Keep assertions direct and message-bearing with `Assert.assertTrue` and `Assert.assertFalse`.

## Mocking

**Framework:** Not used in the detected automated tests.

**Patterns:**
```typescript
Not applicable: the active test suite drives a deployed application through Selenium/TestBench instead of mocking collaborators.
```

**What to Mock:**
- Not applicable in the current committed test suite.

**What NOT to Mock:**
- Do not mock Vaadin components or browser interactions inside `iotter-flow-it`; tests are written as full browser flows against the deployed WAR from `iotter-flow-ui`.

## Fixtures and Factories

**Test Data:**
```typescript
$(LoginFormElement.class).first().login("user", "user");
$(LoginFormElement.class).first().login("admin", "admin");
```

**Location:**
- Test data is inline in the test methods, primarily the seeded credentials used by `iotter-flow-it/src/test/java/it/thisone/iotter/ui/authentication/LoginScreenIT.java` and `iotter-flow-it/src/test/java/it/thisone/iotter/ui/about/AboutViewIT.java`.
- Reusable browser actions live in TestBench element wrappers such as `iotter-flow-it/src/test/java/it/thisone/iotter/ui/authentication/LoginFormElement.java` and `iotter-flow-it/src/test/java/it/thisone/iotter/ui/MainLayoutElement.java`.

## Coverage

**Requirements:** None enforced.

**View Coverage:**
```bash
Not applicable
```

## Test Types

**Unit Tests:**
- Not detected in the inspected modules. If adding unit tests, place them under the owning module’s `src/test/java` and do not model them after the browser-only `iotter-flow-it` patterns.

**Integration Tests:**
- The committed automated suite is integration-level UI testing with Vaadin TestBench, Selenium ChromeDriver, Jetty startup, and Maven Failsafe, all defined in `iotter-flow-it/pom.xml`.
- `AbstractViewTest` supports both local ChromeDriver and a TestBench hub via the `test.use.hub` system property and `HOSTNAME` environment variable.

**E2E Tests:**
- TestBench browser tests in `iotter-flow-it/src/test/java` are the effective end-to-end layer for the UI.
- No separate Playwright, Cypress, or frontend Jest/Vitest suite is configured in `iotter-flow-ui/package.json`.

## Common Patterns

**Async Testing:**
```typescript
waitUntil(ExpectedConditions.presenceOfElementLocated(By.id("vaadinLoginUsername")));
```
- Use explicit waits inside page objects before interacting with dynamic components, as in `iotter-flow-it/src/test/java/it/thisone/iotter/ui/authentication/LoginFormElement.java`.

**Error Testing:**
```typescript
Assert.assertFalse("Expected no link to admin view",
        $(MainLayoutElement.class).first().hasMenuLink("admin"));
```
- Negative-path coverage is implemented as UI state assertions after login or navigation, not as exception assertions.

---

*Testing analysis: 2026-03-27*
