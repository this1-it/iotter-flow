# Plan: ThingsBoard-style Layout for iotter-flow

## Context

The iotter-flow application currently uses a basic `FlexLayout`-based sidebar menu that looks outdated. The goal is to replicate the ThingsBoard Cloud layout: a **dark top header bar** with branding/user info and a **left drawer sidebar** with icon+label navigation items on a light gray background.

The parity theme (`@Theme("parity")`) is already active and already contains CSS for `AppLayout` with the exact color scheme needed (dark header `#11294D`, light drawer `#F2F5F7`, blue active nav item highlight). Vaadin version is **24.10.0**, so `SideNav`/`SideNavItem` components are available natively.

## Files to Modify

1. **`iotter-flow-ui/src/main/java/it/thisone/iotter/ui/MainLayout.java`** — Rewrite: `FlexLayout` → `AppLayout`
2. **`iotter-flow-ui/src/main/java/it/thisone/iotter/ui/Menu.java`** — Delete (absorbed into MainLayout)
3. **`iotter-flow-ui/frontend/styles/shared-styles.css`** — Remove obsolete `.menu-*` rules, add navbar-right styles

No view files need changes (`AppLayout` implements `RouterLayout`, so `@Route(layout = MainLayout.class)` continues to work).

## Implementation

### Step 1: Rewrite `MainLayout.java`

Change superclass to `AppLayout`. Key structure:

```
MainLayout extends AppLayout
├── setPrimarySection(Section.DRAWER)  // triggers parity theme's dark header + light drawer styles
├── Navbar (addToNavbar):
│   ├── DrawerToggle (hamburger, auto-styled white by parity theme)
│   ├── H2 "view-title" (dynamic page title, white text)
│   ├── Spacer div (flex-grow: 1)
│   └── Right section (HorizontalLayout):
│       ├── Button (SEARCH icon, tertiary, placeholder)
│       ├── Button (BELL icon, tertiary, placeholder)
│       ├── User info div (email + role spans, white text)
│       └── Avatar
├── Drawer header (addToDrawer):
│   └── Header element with VaadinIcon.CLUSTER + "Iotter Flow" title (using i18n key menu.title)
├── Drawer nav (addToDrawer):
│   └── Scroller wrapping SideNav:
│       ├── SideNavItem("About", AboutView.class, INFO_CIRCLE)
│       ├── SideNavItem("Users", UsersView.class, USERS)
│       ├── SideNavItem("Devices", DevicesView.class, CONNECT)
│       ├── SideNavItem("GroupWidgets", GroupWidgetsView.class, CHART_LINE)
│       ├── SideNavItem("Networks", NetworksView.class, FILE_TREE_SUB)
│       ├── SideNavItem("Device Configs", DeviceConfigurationsView.class, COGS)
│       └── SideNavItem("Tracing", TracingView.class, ARCHIVES)
└── Drawer footer (addToDrawer):
    └── Footer element with logout Button (SIGN_OUT icon, using i18n key menu.logout)
```

Implement `AfterNavigationObserver` to dynamically update the H2 page title from `@PageTitle` annotation on each navigation.

Preserve from current code:
- `onAttach`: browser timezone JS detection → `VaadinSession.setAttribute("browserTZ", tz)`
- `onAttach`: `Ctrl+L` shortcut → session invalidate + navigate to "login"
- Logout handler: `VaadinSession.getCurrent().getSession().invalidate()` + `UI.getCurrent().navigate("login")`

### Step 2: Delete `Menu.java`

All functionality absorbed into MainLayout. No other file references `Menu` directly.

### Step 3: Update `shared-styles.css`

**Remove** (no longer needed):
- `.main-layout` rule
- `.menu-bar`, `.menu-header`, `.menu-bar vaadin-tabs`, `.menu-bar vaadin-tab`, `.menu-link`, `.menu-link > span`, `.menu-button` rules
- `@media (max-width: 800px)` block for `.main-layout`, `.menu-bar`, `.menu-bar vaadin-tabs`, `.show-tabs`, `.menu-button`

**Keep**:
- `:root` font-family overrides
- `.login-information` and its media query
- `.product-form` and its media queries
- `.Available`, `.Coming`, `.Discontinued` color rules

**Add**:
```css
.navbar-right {
    display: flex;
    align-items: center;
    gap: var(--lumo-space-s);
    margin-right: var(--lumo-space-m);
}
.navbar-right .user-info {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    color: var(--lumo-base-color);
    font-size: var(--lumo-font-size-xs);
    line-height: 1.3;
}
.navbar-right .user-name {
    font-weight: 500;
    font-size: var(--lumo-font-size-s);
}
.navbar-right vaadin-button {
    color: var(--lumo-base-color);
}
.navbar-spacer {
    flex: 1;
}
```

### Parity Theme CSS Already Provides (no changes needed)

- `app-layout.css`: Dark navbar (`--gm-app-header-color`), light drawer (`--gm-surface-primary-color`), white toggle/title color
- `main-layout.css`: `.view-title` (white), `.view-header`, `.drawer-section`, `.app-nav`, `.app-nav-footer` classes
- `vcf-nav-item` active state: blue highlight `rgba(24, 106, 222, 0.2)`, hover state

## Verification

1. Run `mvn -pl iotter-flow-ui spring-boot:run`
2. Navigate to http://localhost:8080/ — should see:
   - Dark navy header bar with hamburger toggle, page title, and right-side section
   - Left drawer with light gray background and vertical nav items with icons
   - Clicking nav items routes to views and highlights active item in blue
   - Drawer collapses/expands via hamburger toggle
3. Test `Ctrl+L` shortcut redirects to login
4. Test logout button in drawer footer
5. Resize browser to verify responsive drawer overlay on narrow screens (handled by AppLayout automatically)
