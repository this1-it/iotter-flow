# Coding Conventions

**Analysis Date:** 2026-02-02

## Naming Patterns

**Files:**
- View classes: `*View.java` (e.g., `LoginScreen.java`, `MainView.java`, `UsersView.java`)
- Form classes: `*Form.java` (e.g., `UserForm.java`)
- Service classes: `*Service.java` (e.g., `UserService.java`, `NetworkService.java`)
- Helper/Utility classes: `*Helper.java`, `*Utils.java` (e.g., `UIEventBusHelper.java`, `MapUtils.java`)
- DAO/Repository interfaces: `I*Dao.java` (e.g., `IUserDao.java`, `INetworkDao.java`)
- Custom field components: `*Select.java`, `*Field.java` (e.g., `RoleSelect.java`, `GeoLocationField.java`)
- Integration test classes: `*IT.java` (e.g., `LoginScreenIT.java`, `AboutViewIT.java`)

**Packages:**
- Lowercase, hierarchical organization (e.g., `it.thisone.iotter.persistence.service`)
- View packages by feature: `it.thisone.iotter.ui.users`, `it.thisone.iotter.ui.authentication`, `it.thisone.iotter.ui.about`
- Shared components: `it.thisone.iotter.ui.common`
- Event system: `it.thisone.iotter.ui.eventbus`
- Configuration: `it.thisone.iotter.config`, `it.thisone.iotter.i18n`

**Classes:**
- PascalCase: `UserForm`, `LoginScreen`, `MainLayout`, `UserService`
- Interfaces: Prefixed with `I`: `IUserDao`, `IDeviceUiFactory`, `IMainUI`
- Enums: PascalCase (e.g., `AccountStatus`, `NetworkGroupType`, `TracingAction`)

**Methods:**
- camelCase: `buildUI()`, `populateFields()`, `navigateAfterLogin()`, `isCreateBean()`, `setRequiredIndicatorVisible()`
- Boolean getters: `isReadOnly()`, `isCreateBean()`, `hasRole()`, `containsNetwork()`
- Builder methods: `build*()` (e.g., `buildLoginInformation()`, `buildFailureMessage()`)
- Helper methods: `get*()`, `load*()`, `create*()` (e.g., `getFieldsLayout()`, `loadNetworks()`, `createFormLayout()`)

**Variables:**
- camelCase: `username`, `accountStatus`, `firstName`, `loginForm`, `selectedGroups`
- Constants: `UPPERCASE_WITH_UNDERSCORES` (e.g., `SESSION_AUTHENTICATION_KEY`, `VIEW_NAME`)
- Serialization: `serialVersionUID` (e.g., `private static final long serialVersionUID = 1L;`)

## Code Style

**Formatting:**
- Indentation: 4 spaces (observed in all source files)
- Line endings: Unix/LF
- Braces: K&R style (opening brace on same line): `public void method() {`
- No automatic formatter configured in project; follow surrounding code style

**Brace Usage:**
```java
// Good: single-line bodies get their own lines
if (selectedNetwork == null) {
    if (getNetwork() != null) {
        networkSelect.setValue(getNetwork());
    }
}

// Try-catch blocks follow same pattern
try {
    Authentication authentication = authManager.authenticate(...);
} catch (AuthenticationException e) {
    event.getSource().setError(true);
}
```

**Line Length:**
- Typical wrapped at 100-120 characters
- Long method calls can span multiple lines with proper indentation

**Semicolons:**
- Always required; no Scala-style omission

## Import Organization

**Order (observed pattern):**
1. Standard Java imports (`java.*`)
2. Java collections and utilities (`java.util.*`)
3. Third-party libraries (Spring, Vaadin, etc.)
4. Project-specific imports (`it.thisone.iotter.*`)

**Path Aliases:**
- No custom path aliases used
- Full package paths in imports
- Avoid wildcard imports (`import x.y.*;`)

**Example from LoginScreen.java:**
```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import com.vaadin.flow.component.*;
import it.thisone.iotter.integration.AuthManager;
import it.thisone.iotter.ui.common.AuthenticatedUser;
```

## Error Handling

**Patterns:**
- Specific exception catching: Catch specific exception types, not generic `Exception`
  ```java
  try {
      Authentication authentication = authManager.authenticate(...);
  } catch (AuthenticationException e) {
      event.getSource().setError(true);
      Notification.show(buildFailureMessage(e));
  }
  ```

- Graceful degradation: Logged but non-fatal issues caught and handled
  ```java
  try {
      // operation
  } catch (IllegalArgumentException ignored) {
      // Already unregistered or never registered
      logger.trace("Subscriber was not registered: {}", subscriber.getClass().getSimpleName());
  }
  ```

- Exception type checking for user messages
  ```java
  if (e instanceof AccountExpiredException) {
      return getTranslation("login.account_expired");
  }
  if (e instanceof LockedException) {
      return getTranslation("login.account_locked");
  }
  ```

- Business logic exceptions: Use domain-specific exceptions like `EditorConstraintException`, `BackendServiceException`, `MaximumNumberSimultaneousLoginsException`

**Silent Failures (Antipattern):**
- Avoid empty catch blocks unless absolutely necessary and commented
- If ignored exception: Always add comment explaining why (see UIEventBus pattern)

## Logging

**Framework:** SLF4J with Logback

**Initialization:**
```java
private static final Logger logger = LoggerFactory.getLogger(UserService.class);
// Note: Some legacy code uses: public static Logger logger = LoggerFactory.getLogger(...);
```

**Patterns:**
- Debug logs for method entry/exit: `logger.debug("Registering subscriber: {}", subscriber.getClass().getSimpleName())`
- Error logs for exceptions: `logger.error("{} cannot add to group {} which belongs to owner {}", user.getUsername(), group.getName(), group.getOwner())`
- Trace logs for detail: `logger.trace("Subscriber was not registered: {}", ...)`
- Use parameterized messages: `logger.debug("User: {}", username)` NOT `logger.debug("User: " + username)`

**Where to Log:**
- Service layer: Log business logic events and errors
- Event bus operations: Log subscriber registration/unregistration
- Authentication events: Log login attempts and failures
- Data persistence: Log create/update/delete operations
- UI layer: Log event handling, user actions leading to state changes

## Comments

**When to Comment:**
- Complex business logic that isn't obvious from reading code
- Migration notes and compatibility workarounds (Vaadin 8 → Flow migration notes included)
- Performance-critical sections
- Unusual patterns or design decisions
- API contract descriptions for public methods

**Inline Comments:**
- Precede code blocks: `// Wire up event bus for PendingChangesEvent`
- Explain "why" not "what": Code shows what, comments explain why
- Keep comments close to relevant code

**Disabled Code:**
- Commented-out old implementations are acceptable during migration phases
- Example in `RoleSelect.java`: Legacy method implementations left as comments for reference
- Example in `UserForm.java`: `//configureExclusiveGroups(...)` marked but not used

**JSDoc/JavaDoc:**
- Used for public classes and public methods
- Format:
  ```java
  /**
   * UI content when the user is not logged in yet.
   */
  @Route("login")
  public class LoginScreen extends FlexLayout {

      /**
       * Register a subscriber to receive events.
       * Call this in onAttach() of your component.
       *
       * @param subscriber the object with @Subscribe methods
       */
      public void register(Object subscriber) {
  ```

- Used for complex behaviors and contracts
- Rarely seen on getters/setters with obvious purposes

## Function Design

**Size:**
- Methods are typically 10-50 lines; avoid exceeding 100 lines
- Single responsibility: Each method does one thing
- Example: `buildLoginInformation()` creates and returns a login info component; `login()` handles authentication flow

**Parameters:**
- Use value objects and domain entities, not primitive parameters when describing entities
- Dependency injection for service dependencies, not parameters
- Optional parameters: Use nullable references or builder pattern
- Example from `UserForm`: Constructor takes all dependencies injected, entity and network passed as parameters

**Return Values:**
- Void for operations (create, update, delete)
- Domain objects for queries: `loadNetworks()` returns `List<Network>`
- Booleans for checks: `containsNetwork()`, `isCreateBean()`
- Components for UI building: `buildLoginInformation()` returns `Component`

**Naming Matches Purpose:**
- `get*()`: Returns a property or related object
- `load*()`: Fetches from a service/database
- `build*()`: Constructs and returns a component/object
- `create*()`: Factory method creating new instances
- `*Listener`: Method handling events

## Module Design

**Exports:**
- Spring-managed components: Use `@Component`, `@Service`, `@Controller` annotations
- Public APIs in service layer for UI to consume
- Package-private classes for implementation details

**Barrel Files:**
- Not used; each class in its own file

**Dependency Injection:**
- Constructor injection preferred for immutable dependencies
- `@Autowired` on constructors or field injection
- Example from `LoginScreen`:
  ```java
  @Autowired
  public LoginScreen(AuthManager authManager) {
      this.authManager = authManager;
  }
  ```

**Spring Scopes:**
- `@UIScope` for components tied to a single browser tab/window
- `@Service` for singleton services
- `@Component` with `@Scope(SCOPE_PROTOTYPE)` for components needing multiple instances (e.g., `UserForm`)

## Serialization

**SerialVersionUID:**
- All serializable classes include: `private static final long serialVersionUID = 1L;`
- Format: Use `1L` regardless of actual serialization needs (historical pattern)

## Exception Naming

**Domain Exceptions:**
- `BackendServiceException`: Business logic violations, service failures
- `EditorConstraintException`: Form validation or data constraints
- `MaximumNumberSimultaneousLoginsException`: Login policy violations
- Inherit from meaningful base exceptions (not raw `Exception`)

## Enums and Constants

**Constants Location:**
- `it.thisone.iotter.config.Constants`: Application-wide constants (roles, field names, status values)
- `it.thisone.iotter.ui.main.UiConstants`: UI-specific constants
- Used for role names: `Constants.ROLE_ADMINISTRATOR`, `Constants.ROLE_SUPERUSER`, `Constants.ROLE_USER`

**Enum Usage:**
- Enums for finite, well-defined sets: `AccountStatus`, `NetworkGroupType`, `NetworkType`, `TracingAction`
- Methods on enums for behavior: `getRole()`, `equals(String name)`

## Collections

**Patterns Observed:**
- `List<T>` for ordered collections: `List<Role>`, `List<Network>`
- `Set<T>` for unique collections: `Set<NetworkGroup>` (in User entity)
- `Map<K,V>` for key-value: `LinkedHashMap<String, Object>` when order matters
- Stream API: `.stream().filter().map().collect()` for transformations
- Example: `groups.stream().filter(Objects::nonNull).forEach(groups::select);`

---

*Convention analysis: 2026-02-02*
