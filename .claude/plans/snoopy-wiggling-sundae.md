# Plan: Fix UserForm to use @Autowired AuthenticatedUser

## Problem
`UserForm` currently receives `currentUser` as a constructor parameter. The workaround of adding null guards is incorrect because:
1. Services are assigned AFTER `super()` returns, but `getFieldsLayout()` is called DURING `super()`
2. The current null guards make the form partially functional during construction

## Root Cause
`AbstractBaseEntityForm` → `AbstractForm` (Firitin) calls `createContent()` → `getFieldsLayout()` during the `super()` constructor, before subclass fields are assigned.

## Solution: Make UserForm a Spring Prototype Bean with Deferred Initialization

### Step 1: Convert UserForm to Spring Prototype Bean
**File:** `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/users/UserForm.java`

1. Add `@Component` and `@Scope(SCOPE_PROTOTYPE)` annotations
2. Add `@Autowired` for `AuthenticatedUser` and all services
3. Remove service parameters from constructor (keep only `entity` and `network`)
4. Move service-dependent initialization from `initializeFields()` to `onAttach()`

```java
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserForm extends AbstractBaseEntityForm<User> {

    @Autowired
    private AuthenticatedUser authenticatedUser;
    @Autowired
    private RoleService roleService;
    @Autowired
    private NetworkService networkService;
    @Autowired
    private NetworkGroupService networkGroupService;
    @Autowired
    private GroupWidgetService groupWidgetService;

    private boolean initialized = false;

    public UserForm(User entity, Network network) {
        super(entity, User.class, "user.editor", network);
        // Services not available here yet - defer to onAttach()
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (!initialized) {
            initialized = true;
            initializeServiceDependentFields();
            bindFields();
        }
    }
}
```

### Step 2: Split initializeFields() into Two Parts
1. **Basic fields** (no service deps) - called during construction via `getFieldsLayout()`
2. **Service-dependent fields** (roles, networks, groups) - called in `onAttach()`

```java
private void initializeBasicFields() {
    // TextField, PasswordField, etc. - no service dependencies
    username = new TextField();
    // ... other basic fields

    // Create empty RoleSelect - will be populated in onAttach()
    role = new RoleSelect(new ArrayList<>());
    networkSelect = new NetworkSelect(new ArrayList<>());
    groups = new NetworkGroupSelect(new ArrayList<>(), true);
    exclusiveGroups = new NetworkGroupSelect(new ArrayList<>(), true);
}

private void initializeServiceDependentFields() {
    UserDetailsAdapter currentUser = authenticatedUser.get().orElseThrow();

    // Populate role select based on current user's permissions
    List<Role> roles = new ArrayList<>();
    if (currentUser.hasRole(Constants.ROLE_SUPERVISOR)) {
        roles = roleService.findAll();
    } else {
        roles.add(roleService.findByName(Constants.ROLE_ADMINISTRATOR));
        roles.add(roleService.findByName(Constants.ROLE_SUPERUSER));
        roles.add(roleService.findByName(Constants.ROLE_USER));
    }
    role.setItems(roles);

    // Populate network select
    networkSelect.setItems(loadNetworks());

    // Configure groups
    configureNetworkSelection();
    preselectGroups(getEntity().getGroups());
    // ...
}
```

### Step 3: Update UsersListing to use ObjectProvider
**File:** `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/users/UsersListing.java`

```java
@Autowired
private ObjectProvider<UserForm> userFormProvider;

@Override
public AbstractBaseEntityForm<User> getEditor(User item) {
    UserForm form = userFormProvider.getObject(item, network);
    return form;
}
```

### Step 4: Remove Null Guards
Remove all the temporary null guards added previously:
- `initializeFields()` - remove `if (currentUser != null && roleService != null)`
- `applyVisibilityRules()` - remove `currentUser != null &&`
- `loadNetworks()` - remove `if (networkService != null)`
- `loadNetworkGroups()` - remove `if (networkGroupService != null)`
- `addVisualizationsTab()` - remove `if (groupWidgetService == null) return`
- `configureExclusiveGroups()` - remove `&& networkGroupService != null`

### Step 5: Update UsersListing.getEditor()
Remove ALL service parameters from the `new UserForm(...)` call:

**Before:**
```java
return new UserForm(item, network, networkService, networkGroupService,
                   groupWidgetService, roleService,
                   authenticatedUser.get().orElse(null));
```

**After:** Using `ObjectProvider`:
```java
return userFormProvider.getObject(item, network);
```

All services are now injected via `@Autowired` in `UserForm`.

## Files to Modify

| File | Changes |
|------|---------|
| `UserForm.java` | Add `@Component`, `@Scope`; add `@Autowired` for ALL 5 services; split initialization; add `onAttach()` |
| `UsersListing.java` | Use `ObjectProvider<UserForm>` instead of `new UserForm(...)`; remove service params |

## Key Changes Summary

1. **UserForm becomes a prototype bean** - Spring manages dependency injection
2. **ALL services via @Autowired** (no constructor parameters):
   - `AuthenticatedUser authenticatedUser`
   - `RoleService roleService`
   - `NetworkService networkService`
   - `NetworkGroupService networkGroupService`
   - `GroupWidgetService groupWidgetService`
3. **Constructor simplified** - only takes `(User entity, Network network)`
4. **Two-phase initialization**:
   - Phase 1 (constructor): Basic UI components with empty data
   - Phase 2 (onAttach): Service-dependent data population
5. **No null guards needed** - Services guaranteed available in `onAttach()`

## Verification

1. `mvn -pl iotter-flow-ui clean compile` - Build check
2. Run app: `mvn -pl iotter-flow-ui spring-boot:run`
3. Login and open Users view
4. Create new user - verify role dropdown is populated
5. Edit existing user - verify all fields load correctly
