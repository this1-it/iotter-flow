# Spring Events → Vaadin Flow UI Architecture

This document is **self-contained** and describes a **production-safe architecture** to propagate **Spring backend events** to **Vaadin Flow frontend components**, replacing legacy Vaadin 7/8 EventBus usage.

You can save this file directly as:

```
SPRING_EVENTS_TO_VAADIN_FLOW_UI.md
```

---

## 1. Context and Goal

In Vaadin Flow:

* Spring events are **application-wide**
* UI components are **session- and UI-scoped**
* Backend threads **cannot update UI directly**

There is **no implicit bridge** between Spring’s event system and Vaadin Flow’s UI threading model.

This architecture provides:

* Correct UI thread access
* Clean lifecycle management
* Multi-user safety
* Clear separation of concerns

---

## 2. Architecture Overview

```
Spring Event
    ↓
UI Event Bridge (Spring singleton)
    ↓
UI-scoped subscribers (Views)
    ↓
Component refresh
```

Each step is mandatory.
No layer may bypass the next.

---

## 3. Layer Responsibilities

### 3.1 Spring Event (Backend Domain Layer)

**Purpose**

* Represent a backend/domain change
* Contain no UI knowledge

**Characteristics**

* Plain Java object (POJO / record)
* Published from backend services
* No references to UI, components, or sessions

**Example**

```java
public record DeviceChangedEvent(Long deviceId) {}
```

```java
applicationEventPublisher.publishEvent(
    new DeviceChangedEvent(deviceId)
);
```

---

### 3.2 UI Event Bridge (Spring Singleton)

**Purpose**

* Listen to Spring events
* Re-emit them to UI-level subscribers
* Act as the *only* bridge between backend and UI world

**Rules**

* Singleton Spring bean
* Thread-safe listener storage
* Must not store UI or component references
* Must not contain routing logic

**Implementation**

```java
@Component
public class UiEventBridge {

    private final Set<Consumer<DeviceChangedEvent>> listeners =
            ConcurrentHashMap.newKeySet();

    public Registration register(Consumer<DeviceChangedEvent> listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    @EventListener
    public void onDeviceChanged(DeviceChangedEvent event) {
        listeners.forEach(listener -> listener.accept(event));
    }
}
```

**What this layer must NOT do**

* Access `UI`
* Know which view or component should refresh
* Filter events by UI state

---

### 3.3 UI-Scoped Subscribers (Views)

**Purpose**

* Subscribe to UI-level events
* Own lifecycle (attach / detach)
* Route events to components
* Enter the Vaadin UI thread

**Why Views**

* Views know which components exist
* Views are UI-scoped
* Views control lifecycle and cleanup

**Implementation**

import com.vaadin.flow.shared.Registration;


public record DeviceChangedEvent(
    Long deviceId,
    ChangeType changeType
) {}


```java
@Route("devices")
public class DeviceView extends VerticalLayout {

    private final Grid<Device> grid;
    private Registration registration;

    public DeviceView(DeviceService service, UiEventBridge bridge) {

        grid = new Grid<>(Device.class);
        grid.setItems(service.findAll());
        add(grid);

        UI ui = UI.getCurrent();

        registration = bridge.register(event ->
            ui.access(() -> {
                grid.getDataProvider().refreshAll();
            })
        );
    }

    @Override
    protected void onDetach(DetachEvent event) {
        registration.remove(); // mandatory
    }
}
```

**Mandatory rules**

* Always wrap UI updates in `UI.access(...)`
* Always unregister listeners in `onDetach`
* Never let components subscribe directly

---

### 3.4 Component Refresh Layer

**Purpose**

* Render updated data
* Remain passive and reusable

**Typical operations**

* `DataProvider.refreshAll()`
* `DataProvider.refreshItem(item)`
* Re-read state from backend services
* Update labels, counters, badges

**Example**

```java
grid.getDataProvider().refreshAll();
```

Components must **never**:

* Listen to Spring events
* Know about backend threads
* Manage subscriptions

---

## 4. Event Routing Strategy

Routing belongs **only to the View layer**.

Examples:

```java
if (event.deviceId().equals(selectedDeviceId)) {
    refreshGrid();
}
```

```java
if (!isVisible()) {
    return;
}
```

This keeps:

* Backend generic
* Bridge reusable
* UI logic explicit and debuggable

---

## 5. Threading Model

| Layer                 | Thread                    |
| --------------------- | ------------------------- |
| Spring Service        | Backend thread            |
| Spring Event Listener | Backend thread            |
| UI Event Bridge       | Backend thread            |
| UI update             | UI thread via `UI.access` |

**Crossing threads without `UI.access` is forbidden.**

---

## 6. Multi-User Behavior

* Each open UI registers independently
* A single Spring event is delivered to all UIs
* Each View decides locally whether to react

This is **correct and expected behavior**.

---

## 7. Push vs Non-Push

Without Push:

* UI updates appear on next client request

With Push:

```java
@Push
```

* Updates are immediate

The architecture does **not** change.

---

## 8. Anti-Patterns This Architecture Prevents

❌ Global static EventBus holding UI references
❌ Views acting as Spring `@EventListener`s
❌ Detached UI access
❌ Memory leaks on reload
❌ Cross-session UI updates
❌ Tight backend–UI coupling

---

## 9. Responsibility Summary

| Layer           | Responsibility                    |
| --------------- | --------------------------------- |
| Spring Event    | Describe backend change           |
| UI Event Bridge | Translate backend → UI world      |
| View            | Subscribe, route, enter UI thread |
| Component       | Render state only                 |

---

## 10. Golden Rule

> **Spring listens to backend events.**
> **Views listen to UI events.**
> **The bridge keeps them apart.**

---

## 11. When to Use This Architecture

* Migrating from Vaadin 7/8 EventBus
* Backend jobs trigger UI refresh
* Multiple users observe shared state
* Clean separation between backend and UI is required

---

End of document.
