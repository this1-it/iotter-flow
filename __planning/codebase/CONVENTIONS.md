# Coding Conventions

**Analysis Date:** 2026-03-23

## Naming Patterns

**Files:**
- Classes: PascalCase (e.g., `UserService`, `MainLayout`, `GraphicFeedForm`)
- Test classes: Test name + `IT` suffix for integration tests (e.g., `LoginScreenIT.java`, `AboutViewIT.java`)
- Test base classes: `Abstract` prefix (e.g., `AbstractViewTest.java`)
- Test element classes (TestBench): Component name + `Element` suffix (e.g., `LoginFormElement`, `MainLayoutElement`)

**Functions/Methods:**
- camelCase (e.g., `findDeviceCacheable()`, `getUnitOfMeasureName()`, `ensureFieldsInitialized()`)
- Accessor methods follow Java conventions: `get`/`set` prefixes (e.g., `getCurrentUser()`, `setCurrentUser()`)
- Boolean methods may use `is` prefix (e.g., `isReadOnly()`, `isUsingHub()`)

**Variables:**
- camelCase for local variables and fields (e.g., `currentUser`, `formLayout`, `eventPoster`)
- Constants: UPPER_SNAKE_CASE (e.g., `ACTION_BUTTON_WIDTH`, `USE_HUB_PROPERTY`)
- Static logger fields: `logger` or prefixed (e.g., `public static Logger logger`)

**Types:**
- Classes: PascalCase
- Interfaces: PascalCase with optional `I` prefix (e.g., `IDeviceDao`, `IUiFactory`)
- Enums: PascalCase (e.g., `GraphicWidgetType`, `NetworkGroupType`)
- Packages: lowercase with dots (e.g., `it.thisone.iotter.persistence.service`)

**UI Components/Views:**
- View classes: Named by feature + `View` suffix (e.g., `MainView`, `UsersView`, `DevicesView`)
- Form classes: Named by entity + `Form` suffix (e.g., `GraphicFeedForm`, `ChartThresholdForm`)
- Listing classes: Named by entity + `Listing` suffix (e.g., `GraphicFeedListing`)
- Field component classes: Named by type + `Field` suffix (e.g., `ChannelSelect`, `ChartPlotOptionsField`)

## Code Style

**Formatting:**
- Indentation: 4 spaces (tabs converted to spaces)
- Braces: K&R style (opening brace on same line as declaration)
- Line length: No strict limit enforced; aim for readability
- No automatic formatter configured (follow surrounding code style)

**Linting:**
- No project-specific linting rules configured
- Follow Java conventions and Spring best practices

## Import Organization

**Order:**
1. Standard Java imports (`java.*`, `javax.*`, `jakarta.*`)
2. Third-party imports (Spring, Vaadin, Apache Commons, etc.)
3. Project imports (`it.thisone.iotter.*`)

**Path Aliases:**
- No path aliases used; imports use full package paths

**Wildcard imports:**
- Not used; explicit imports preferred

## Error Handling

**Patterns:**
- Custom exception: `BackendServiceException` used for business logic errors
- Service methods throw `BackendServiceException` with descriptive messages
- DAO methods may catch generic exceptions and re-throw with context
- Try-catch blocks used for checked exceptions; failures logged to SLF4J

**Example:**
```java
@Transactional
public void create(User entity) throws BackendServiceException {
    try {
        userDao.create(entity);
    } catch (Throwable t) {
        logger.error("Error creating user", t);
        throw new BackendServiceException("Failed to create user", t);
    }
}
```

## Logging

**Framework:** SLF4J with LoggerFactory

**Logger declaration:**
```java
public static Logger logger = LoggerFactory.getLogger(ClassName.class);
```

**Patterns:**
- Info level: Configuration, service initialization (e.g., `logger.info("PersistenceJPAConfig initialized.")`)
- Error level: Exceptions and failures (e.g., `logger.error("Error creating user", t)`)
- Debug level: Detailed diagnostics in configuration (e.g., `logger.debug("AppConfig initialized")`)
- Use parameterized logging: `logger.info("Configuring JPA DataSource: {}", env)` instead of string concatenation

**When to log:**
- Service initialization and configuration changes
- Error conditions and exceptions
- Business operation outcomes (create, update, delete)
- Data validation failures

## Comments

**When to comment:**
- Algorithm-heavy code requiring explanation
- Non-obvious business logic (e.g., validation rules, calculations)
- Complex conditionals or state management
- References to external specifications or standards
- Known limitations or workarounds

**JavaDoc/Documentation:**
- Sparse use of JavaDoc in backend services
- Method-level JavaDoc used for public API methods with `@throws` documentation
- Class-level JavaDoc limited; code is self-documenting where possible
- UI components include JavaDoc headers describing purpose

**Example:**
```java
/**
 * Register a user device association.
 *
 * @param entity user entity
 * @param serial device serial number
 * @throws BackendServiceException if device or user not found
 */
public void userRegistration(User entity, String serial) throws BackendServiceException {
    // validation and registration logic
}
```

## Function Design

**Size:** Methods average 10-50 lines; longer methods (100+ lines) are specialized like `GraphicFeedForm.getFieldsLayout()`

**Parameters:**
- Simple types and entity objects passed as parameters
- Collections passed as parameters for batch operations
- Avoid builder patterns; use constructor injection for Spring components

**Return Values:**
- Services return entities or collections
- DAO methods may return null for missing entities (checked by callers)
- Cacheable methods use null-checking: `unless="#result == null"`
- Form methods return layout components or void

**Null handling:**
- Defensive null checks before dereferencing
- Null checks before collection operations (e.g., `if (user != null) { user.getRoles().size(); }`)
- Optional not widely used; explicit null checks preferred

## Module Design

**Exports:**
- Service classes decorated with `@Service` for Spring autowiring
- DAO interfaces (e.g., `IUserDao`) define contracts; implementations in same package
- UI components exported as public classes in view packages
- Util classes in `it.thisone.iotter.util` package

**Barrel Files:**
- Not used; direct imports from specific classes preferred

**Spring Component Annotations:**
```java
@Service           // Service layer classes
@Configuration     // Configuration classes
@Component         // Generic Spring components
```

## Transactional Boundaries

**Pattern:** `@Transactional` decorator on service methods managing data modifications

**Variants:**
- `@Transactional` - Read-write transaction
- `@Transactional(readOnly=true)` - Read-only transaction (optimization)
- Some commented-out transactional annotations suggest migration work

**Example:**
```java
@Transactional
public void create(User entity) {
    userDao.create(entity);
}

@Transactional(readOnly=true)
public User findOne(String id) {
    return userDao.findOne(id);
}
```

## Caching

**Pattern:** `@Cacheable` and `@CacheEvict` decorators on service methods

**Common cached methods:**
- Device lookups by serial: `@Cacheable(value = Constants.Cache.DEVICE, key="#serial")`
- Unit of measure codes: `@Cacheable(value = Constants.Cache.UNIT_OF_MEASURE)`
- Condition: `unless="#result == null"` prevents caching null results

**Example:**
```java
@Cacheable(value = Constants.Cache.DEVICE, key="#serial", unless="#result == null")
public Device findDeviceCacheable(String serial) {
    // lookup and return
}
```

## Serialization

**Pattern:** `private static final long serialVersionUID` field in components

**Usage:**
- Required in Vaadin Flow components and forms (serializable across sessions)
- Value is generated unique ID, not versioned

**Example:**
```java
public class MainView extends VerticalLayout {
    private static final long serialVersionUID = 1L;
}
```

---

*Convention analysis: 2026-03-23*
