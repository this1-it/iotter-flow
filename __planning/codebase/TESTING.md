# Testing Patterns

**Analysis Date:** 2026-03-23

## Test Framework

**Runner:**
- JUnit 4 (legacy framework)
- Vaadin TestBench for integration tests (browser-based UI testing)
- Maven Failsafe plugin for integration test execution

**Config files:**
- Integration tests profile defined in `iotter-flow-it/pom.xml`
- TestBench uses Chrome driver (configured via `drivers.xml`)
- Jetty server started automatically for integration tests on port 8080

**Run Commands:**
```bash
# Run all integration tests (requires integration-tests profile)
mvn -pl iotter-flow-it -Pintegration-tests verify

# Run with custom ChromeDriver path
mvn -pl iotter-flow-it -Pintegration-tests verify -Dwebdriver.chrome.driver=/path/to/chromedriver

# Run UI in development mode (watch for hot reload)
mvn -pl iotter-flow-ui spring-boot:run
```

## Test File Organization

**Location:**
- Integration tests (IT) in `iotter-flow-it/src/test/java/it/thisone/iotter/ui/`
- Parallel to application code package structure

**Naming:**
- Integration tests: `*IT.java` suffix (e.g., `LoginScreenIT.java`, `AboutViewIT.java`)
- Test base class: `AbstractViewTest.java`
- TestBench element wrappers: `*Element.java` suffix (e.g., `LoginFormElement`, `MainLayoutElement`)

**Structure:**
```
iotter-flow-it/
├── src/
│   └── test/
│       └── java/
│           └── it/
│               └── thisone/
│                   └── iotter/
│                       └── ui/
│                           ├── AbstractViewTest.java         # Base class for ITs
│                           ├── MainLayoutElement.java        # TestBench element
│                           ├── authentication/
│                           │   ├── LoginScreenIT.java        # Feature test
│                           │   └── LoginFormElement.java     # TestBench element
│                           └── about/
│                               └── AboutViewIT.java          # Feature test
```

## Test Structure

**Suite Organization:**

```java
public class LoginScreenIT extends AbstractViewTest {

    @Test
    public void loginAsAdmin_hasAdminViewLink() {
        // given authenticated as admin
        $(LoginFormElement.class).first().login("admin", "admin");

        // when selecting "About" from the sidebar menu
        final MainLayoutElement mainElem = $(MainLayoutElement.class).first();
        mainElem.clickMenuLink("admin");

        // then there is a link to admin's view
        Assert.assertTrue("Expected link to admin view",
                mainElem.hasMenuLink("admin"));
    }
}
```

**Patterns:**

- **Setup:** Test extends `AbstractViewTest` which handles WebDriver initialization
  - `@Before` hook in `AbstractViewTest.setup()` initializes Chrome driver
  - Driver navigates to application URL via `getDriver().get(getURL(route))`
  - CI environment support via `test.use.hub` property

- **Teardown:** Handled automatically by TestBench framework and Jetty plugin
  - `@Rule` `ScreenshotOnFailureRule` captures screenshots on test failure

- **Assertion pattern:** Standard JUnit `Assert` class
  - `Assert.assertTrue(message, condition)`
  - `Assert.assertFalse(message, condition)`

## Mocking

**Framework:** TestBench element wrappers for UI component mocking

**Patterns:**

TestBench elements wrap Vaadin Flow components for test interaction:

```java
public class LoginFormElement extends TestBenchElement {

    public void login(String username, String password) {
        // Use TestBench API to interact with form components
        $(TextFieldElement.class).id("username").setValue(username);
        $(PasswordFieldElement.class).id("password").setValue(password);
        $(ButtonElement.class).id("login").click();
    }
}
```

Component selection via TestBench API:
```java
$(LoginFormElement.class).first()           // Find first instance
$(MainLayoutElement.class).first()          // Find main layout
$(SpanElement.class).last()                 // Find last span
mainElem.$(SpanElement.class).last()        // Scoped search
```

**What to Mock:**
- Vaadin Flow UI components using TestBench element wrappers
- User authentication state via login form helper methods
- Menu navigation via layout element helpers

**What NOT to Mock:**
- Backend services (real Spring beans used)
- Database access (real persistence layer)
- Cassandra/time-series data (real if configured)
- MQTT connections (real if configured)

Integration tests use real application components; only UI interaction is mocked.

## Fixtures and Factories

**Test Data:**

Hard-coded test credentials in tests:
```java
// Standard test user accounts
$(LoginFormElement.class).first().login("admin", "admin");   // Admin user
$(LoginFormElement.class).first().login("user", "user");     // Regular user
```

TestBench element factories for component creation:
- Elements created via `$()` selector in TestBench
- No separate fixture files or factory classes
- Test data created at runtime by application initialization

**Location:**
- No separate fixture/factory files
- Test data managed within test methods
- Base class utilities in `AbstractViewTest`

## Coverage

**Requirements:** Not enforced

**View Coverage:**
```bash
# Coverage reports not explicitly configured in pom.xml
# JaCoCo or similar not visible in standard pom.xml
```

Integration tests focus on:
- Authentication flows
- Navigation between views
- Theme application verification
- User permission-based view visibility

## Test Types

**Unit Tests:**
- Scope: Individual service methods and components
- Approach: Not widely visible in this codebase; backend services not extensively unit tested
- Recommendation: Add unit tests for business logic in service layer

**Integration Tests:**
- Scope: UI flows, user interactions, view rendering
- Approach: TestBench-based end-to-end testing
- Files: `iotter-flow-it/src/test/java/it/thisone/iotter/ui/**/*IT.java`
- Examples: `LoginScreenIT`, `AboutViewIT`

**E2E Tests:**
- Framework: Vaadin TestBench (Chrome driver, parallel capable)
- Server: Jetty auto-started on port 8080
- Capabilities:
  - ParallelTest base class supports distributed execution
  - Screenshot capture on failure
  - JavaScript execution for client-side validation

## Common Patterns

**Async Testing:**

TestBench handles async JavaScript execution:
```java
attachEvent.getUI().getPage().executeJs(
    "return Intl.DateTimeFormat().resolvedOptions().timeZone;"
).then(String.class, tz -> {
    VaadinSession.getCurrent().setAttribute("browserTZ", tz);
});
```

Tests wait implicitly for element availability via TestBench API.

**Error Testing:**

Test failure assertions with meaningful messages:
```java
Assert.assertTrue("Expected link to admin view",
        $(MainLayoutElement.class).first().hasMenuLink("admin"));

Assert.assertFalse("Expected no link to admin view",
        $(MainLayoutElement.class).first().hasMenuLink("admin"));
```

Message serves as both assertion condition and failure documentation.

**Theme Testing:**

Custom assertion method to verify theme application:
```java
protected void assertThemePresentOnElement(
        WebElement element, Class<? extends AbstractTheme> themeClass) {
    String themeName = themeClass.getSimpleName().toLowerCase();
    Boolean hasStyle = (Boolean) executeScript("" +
            "var styles = Array.from(arguments[0]._template.content" +
            ".querySelectorAll('style'))" +
            ".filter(style => style.textContent.indexOf('" +
            themeName + "') > -1);" +
            "return styles.length > 0;", element);

    Assert.assertTrue("Element should have theme '" +
            themeClass.getSimpleName() + "'.", hasStyle);
}
```

Executes JavaScript to inspect component template styles.

## Test Configuration

**ChromeDriver Setup:**
- Configured via `drivers.xml` in project root
- Plugin `driver-binary-downloader-maven-plugin` auto-downloads matching version
- CI environment: Uses environment variable `webdriver.chrome.driver` if set
- Local development: Uses system PATH or explicit property

**CI Environment Detection:**
```java
private static boolean isUsingHub() {
    return Boolean.TRUE.toString().equals(
            System.getProperty(USE_HUB_PROPERTY));
}
```

When `test.use.hub=true`: Uses TestBench Hub (parallel grid execution)
Otherwise: Uses ChromeDriver directly via `TestBench.createDriver(new ChromeDriver())`

**Server Setup:**
- Jetty Maven plugin auto-starts server in `pre-integration-test` phase
- Deployed application available at `http://localhost:8080/`
- Server stopped in `post-integration-test` phase
- Port 8080 configurable via plugin configuration

## Test Base Classes

**AbstractViewTest:**
- Location: `iotter-flow-it/src/test/java/it/thisone/iotter/ui/AbstractViewTest.java`
- Purpose: Provides TestBench infrastructure for UI tests
- Features:
  - WebDriver initialization and setup
  - URL construction for routes
  - CI/Hub detection and configuration
  - Theme assertion utilities
  - Screenshot capture on failure (via Rule)

**Usage:**
```java
public class LoginScreenIT extends AbstractViewTest {

    // Constructor to set route
    public LoginScreenIT() {
        super("login");  // navigates to /login
    }

    @Test
    public void testMethod() {
        // WebDriver ready via getDriver()
        // URLs constructed via getURL()
    }
}
```

## Test Maintenance

**Page Object Pattern:**
- TestBench elements implement page object pattern
- Element classes wrap component interaction logic
- Example: `LoginFormElement` encapsulates form interaction

```java
public class LoginFormElement extends TestBenchElement {
    public void login(String username, String password) {
        // Reusable login method
    }
}
```

**Test Naming:**
- Descriptive test method names: `loginAsAdmin_hasAdminViewLink()`
- Pattern: `action_expectedOutcome()`
- Helps readers understand test intent without reading implementation

---

*Testing analysis: 2026-03-23*
