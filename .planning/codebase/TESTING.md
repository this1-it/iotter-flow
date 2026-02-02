# Testing Patterns

**Analysis Date:** 2026-02-02

## Test Framework

**Runner:**
- JUnit 4 (not JUnit 5)
- Maven Failsafe plugin for integration tests
- Vaadin TestBench for UI integration testing
- Config: `iotter-flow-it/pom.xml` (`maven-failsafe-plugin` v2.22.2)

**Assertion Library:**
- JUnit 4 Assert: `Assert.assertTrue()`, `Assert.assertFalse()`, `Assert.assertEquals()`
- No external assertion library (AssertJ, Hamcrest) in use

**Run Commands:**

```bash
# Integration tests (requires Chrome and TestBench license)
mvn -pl iotter-flow-it -Pintegration-tests verify

# With custom ChromeDriver path
mvn -pl iotter-flow-it -Pintegration-tests verify -Dwebdriver.chrome.driver=/path/to/chromedriver

# Standard build (no integration tests by default)
mvn clean install

# Run specific test file
mvn -pl iotter-flow-it -Pintegration-tests test -Dtest=LoginScreenIT
```

## Test File Organization

**Location:**
- Integration tests are co-located with the UI module they test
- Test source root: `iotter-flow-it/src/test/java/it/thisone/iotter/ui/`
- Test resources: `iotter-flow-it/src/test/resources/`

**Naming:**
- Suffix `IT` (Integration Test): `LoginScreenIT.java`, `AboutViewIT.java`
- Base class for test infrastructure: `AbstractViewTest.java`

**Structure:**
```
iotter-flow-it/
├── src/test/
│   ├── java/
│   │   └── it/thisone/iotter/ui/
│   │       ├── AbstractViewTest.java (base class)
│   │       ├── MainLayoutElement.java (TestBench element)
│   │       ├── authentication/
│   │       │   ├── LoginFormElement.java (TestBench element)
│   │       │   └── LoginScreenIT.java (test class)
│   │       └── about/
│   │           └── AboutViewIT.java (test class)
│   └── resources/
├── pom.xml (defines integration-tests profile)
└── drivers.xml (ChromeDriver version configuration)
```

## Test Structure

**Base Test Class:**

```java
public abstract class AbstractViewTest extends ParallelTest {
    private static final int SERVER_PORT = 8080;
    private final String route;

    @Rule
    public ScreenshotOnFailureRule rule = new ScreenshotOnFailureRule(this, false);

    @Before
    public void setup() throws Exception {
        if (isUsingHub()) {
            super.setup();
        } else {
            setDriver(TestBench.createDriver(new ChromeDriver()));
        }
        getDriver().get(getURL(route));
    }

    // Helper methods for assertions and navigation
}
```

**Integration Test Example:**

```java
public class LoginScreenIT extends AbstractViewTest {

    @Test
    public void loginForm_isLumoThemed() {
        // Given: User is on login screen
        LoginFormElement loginForm = $(LoginFormElement.class).first();

        // When: (Setup in @Before)

        // Then: Verify Lumo theme is applied
        assertThemePresentOnElement(loginForm, Lumo.class);
    }

    @Test
    public void loginAsAdmin_hasAdminViewLink() {
        // When authenticating as admin
        $(LoginFormElement.class).first().login("admin", "admin");

        // Then there is a link to admin's view
        Assert.assertTrue("Expected link to admin view",
                $(MainLayoutElement.class).first().hasMenuLink("admin"));
    }

    @Test
    public void loginAsUser_noAdminViewLink() {
        // When authenticating as a regular user
        $(LoginFormElement.class).first().login("user", "user");

        // Then there is no link to admin's view
        Assert.assertFalse("Expected no link to admin view",
                $(MainLayoutElement.class).first().hasMenuLink("admin"));
    }
}
```

**Pattern Breakdown:**
- Setup: `@Before` method runs before each test (`setup()`)
- Assertions: Test name includes predicate (e.g., `_isLumoThemed`, `_hasAdminViewLink`)
- Comments: Given-When-Then format or explicit comments for clarity
- Selenium queries: `$(ElementClass.class).first()` to find components
- Element operations: Methods on TestBench elements (e.g., `login()`, `hasMenuLink()`)

## TestBench Element Classes

**TestBench Elements:**
- Bridge between Selenium WebDriver and Vaadin components
- Custom element classes map to UI components
- Example: `LoginFormElement.java` wraps `LoginForm` component
- Example: `MainLayoutElement.java` wraps `MainLayout` component

**Element API Pattern:**
```java
public class LoginFormElement extends Element {
    public void login(String username, String password) {
        // Find username field, password field, submit
    }
}

public class MainLayoutElement extends Element {
    public boolean hasMenuLink(String label) {
        // Check if menu contains link with label
    }

    public void clickMenuLink(String label) {
        // Click menu link by label
    }
}
```

## Mocking

**Framework:** None explicitly used for unit tests

**Patterns:**
- No mocking framework detected in codebase (Mockito, etc.)
- Integration tests use real Spring context and database
- No fixtures or factory classes for test data

**What to Mock:**
- External services not under test (but code shows preference for real integration)
- Browser interactions abstracted through TestBench elements
- HTTP calls through Vaadin request/response cycle

**What NOT to Mock:**
- Data persistence (use real or H2 in-memory database)
- Spring services (prefer real beans in integration tests)
- UI components (TestBench handles interaction)

## Fixtures and Factories

**Test Data:**
- Not observed in integration tests
- Data setup happens via:
  1. Login with known credentials (e.g., "admin"/"admin", "user"/"user")
  2. Database initialization via `EmptyDbInitializator.java`
  3. Application startup populates default roles and users

**Location:**
- Database setup: `iotter-backend/src/main/java/it/thisone/iotter/persistence/service/EmptyDbInitializator.java`
- Test credentials hardcoded in tests or from application configuration

**Pattern:**
```java
// Tests use live database, populated on startup
// No factory classes or test fixtures observed
// Assumes default users exist: admin, user
```

## Coverage

**Requirements:** Not enforced (no coverage plugin detected)

**View Coverage:** Manual via `ScreenshotOnFailureRule` - captures screenshots when tests fail for visual inspection

**Profile Activation:**
- Integration tests run only with `-Pintegration-tests` profile
- Requires external dependencies:
  - Chrome browser or headless Chrome
  - ChromeDriver binary (version managed in `drivers.xml`)
  - Vaadin TestBench license

## Test Types

**Unit Tests:**
- Location: Backend modules have `src/test/java/` structure
- Not fully documented in this codebase
- Example test structure likely follows standard JUnit 4 with mocking for service layer
- Run with: `mvn test`

**Integration Tests:**
- Full Spring application startup
- Real database (H2 in-memory or configured database)
- Selenium WebDriver browser automation
- Location: `iotter-flow-it/src/test/java/it/thisone/iotter/ui/`
- Files matching `*IT.java`
- Run with: `mvn -Pintegration-tests verify`

**E2E Tests:**
- Not separately defined; integration tests serve E2E purpose
- Full application stack from UI through persistence
- Real user workflows tested (login, navigation, form submission)

## Infrastructure Setup

**Profile: integration-tests**

Activates in `iotter-flow-it/pom.xml`:

1. **Maven Failsafe Plugin** (v2.22.2)
   - Runs tests matching `*IT.java`
   - System properties: `webdriver.chrome.driver`, `trimStackTrace`, `enableAssertions`

2. **Jetty Maven Plugin** (v9.4.15)
   - Starts: `pre-integration-test` phase
   - Stops: `post-integration-test` phase
   - Port: 8080
   - Stop key: `iotter-flow-it`

3. **Driver Binary Downloader Plugin** (v1.0.17)
   - Downloads ChromeDriver to `iotter-flow-it/drivers/`
   - Configuration in `drivers.xml`
   - Runs in `pre-integration-test` phase

**CI Environment Support:**
- TestBench Hub support via `test.use.hub` property
- Reads `HOSTNAME` environment variable for CI deployments
- Parallel test execution via `ParallelTest` base class

## Common Patterns

**Async Testing:**
- Vaadin Flow handles async via JavaScript/WebDriver waits
- TestBench queries block until elements are found
- No explicit wait declarations in integration tests

**Error Testing:**
- Integration tests verify error messages via UI elements
- Example: `LoginScreenIT` verifies login failure notifications
- Pattern: Perform action, assert error state/message displayed

**Navigation Testing:**
- `UI.getCurrent().navigate(targetClass)` used in code
- Tests verify navigation via menu clicks
- Example: `mainElem.clickMenuLink("About")` then verify content

**State Verification:**
- Assert UI element state: `hasMenuLink()`, `getText()`, `isVisible()`
- Example: Verify admin link present after admin login
- Theme verification: `assertThemePresentOnElement(element, Lumo.class)`

**Form Testing:**
- Not directly tested in `*IT.java` files shown
- Form logic likely tested via integration tests when submitting forms
- Example would involve: Fill form fields via TestBench elements, click submit, verify result

## Test Isolation

**Database State:**
- Application startup initializes database with defaults
- Tests may modify data; no explicit cleanup shown
- Each test execution against same database

**UI State:**
- Each test method gets fresh browser session (via `@Before setup()`)
- Previous test state not preserved between test methods

**Parallel Execution:**
- Supported via `ParallelTest` base class
- Each test can run in parallel with own browser/server instance

## Dependencies

**Test Scope:**
```xml
<!-- Vaadin TestBench -->
<dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-testbench</artifactId>
    <scope>test</scope>
</dependency>

<!-- JUnit 4 (via transitive) -->
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <scope>test</scope>
</dependency>
```

---

*Testing analysis: 2026-02-02*
