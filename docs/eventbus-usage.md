# UIEventBus Usage Guide

This document describes how to use the UIEventBus system for inter-component communication in iotter-flow.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           Backend Services                               │
│                                                                          │
│  @Autowired ApplicationEventPublisher                                   │
│       │                                                                  │
│       ▼                                                                  │
│  eventPublisher.publishEvent(new MyTargetedEvent(...))                  │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        SpringToUIEventBridge                             │
│                                                                          │
│  @EventListener                                                          │
│  Listens for TargetedSpringEvent subclasses                             │
│  Routes to UIEventBusRegistry by key                                    │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         UIEventBusRegistry                               │
│                                                                          │
│  Maps routing keys to UI sessions                                       │
│  - "device:123" → [UI-1, UI-5]                                          │
│  - "user:456" → [UI-2]                                                  │
│  - "network:789" → [UI-1, UI-2, UI-3]                                   │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    UI Layer (per browser tab)                            │
│                                                                          │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │  UIEventBus (@UIScope - one per tab)                             │   │
│  │  - Components register with @Subscribe methods                   │   │
│  │  - Events posted via ui.access() for thread safety               │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                              │                                          │
│                              ▼                                          │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │  Vaadin Components                                               │   │
│  │  - Register on attach, unregister on detach                      │   │
│  │  - Receive events via @Subscribe methods                         │   │
│  └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
```

## Key Classes

| Class | Location | Purpose |
|-------|----------|---------|
| `UIEventBus` | `iotter-flow-ui` | UI-scoped Google EventBus wrapper |
| `UIEventBusRegistry` | `iotter-flow-ui` | Routes events by key to specific UIs |
| `UIEventBusInitializer` | `iotter-flow-ui` | VaadinServiceInitListener for setup/cleanup |
| `TargetedSpringEvent` | `iotter-flow-ui` | Base class for routable Spring events |
| `SpringToUIEventBridge` | `iotter-flow-ui` | Listens for Spring events, routes to UI |
| `UIEventBusHelper` | `iotter-flow-ui` | Convenience wrapper for components |
| `UIEventBusHelperFactory` | `iotter-flow-ui` | Creates helpers with current UI context |

---

## Tier 1: Intra-UI Communication (Form ↔ Listing)

For communication between components in the **same browser tab**.

### Step 1: Inject UIEventBus

```java
@Autowired
private UIEventBus eventBus;
```

### Step 2: Register/Unregister in Lifecycle

```java
@Override
protected void onAttach(AttachEvent event) {
    super.onAttach(event);
    eventBus.register(this);
}

@Override
protected void onDetach(DetachEvent event) {
    eventBus.unregister(this);
    super.onDetach(event);
}
```

### Step 3: Subscribe to Events

```java
@Subscribe
public void onPendingChanges(PendingChangesEvent event) {
    refreshData();
}
```

### Step 4: Post Events

```java
// After saving data
eventBus.post(new PendingChangesEvent());
```

### Complete Example: UsersListing

```java
@Route("users")
public class UsersListing extends VerticalLayout {

    @Autowired
    private UIEventBus eventBus;

    @Autowired
    private UserService userService;

    private Grid<User> grid;

    @Override
    protected void onAttach(AttachEvent event) {
        super.onAttach(event);
        eventBus.register(this);
        refreshData();
    }

    @Override
    protected void onDetach(DetachEvent event) {
        eventBus.unregister(this);
        super.onDetach(event);
    }

    @Subscribe
    public void onPendingChanges(PendingChangesEvent event) {
        // Refresh when another component saves data
        refreshData();
    }

    private void refreshData() {
        grid.setItems(userService.findAll());
    }
}
```

---

## Tier 2: Backend → UI Communication (Targeted)

For sending events from backend services to **specific UI sessions**.

### Step 1: Create a Targeted Event

Extend `TargetedSpringEvent` and implement `getRoutingKey()`:

```java
package it.thisone.iotter.ui.eventbus.events;

import it.thisone.iotter.ui.eventbus.TargetedSpringEvent;

public class DeviceDataReceivedEvent extends TargetedSpringEvent {

    private final String deviceId;
    private final Object data;

    public DeviceDataReceivedEvent(Object source, String deviceId, Object data) {
        super(source);
        this.deviceId = deviceId;
        this.data = data;
    }

    @Override
    public String getRoutingKey() {
        // Only UIs watching this device will receive the event
        return "device:" + deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public Object getData() {
        return data;
    }
}
```

### Step 2: Publish from Backend Service

```java
@Service
public class DeviceDataService {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void processIncomingData(String deviceId, Object data) {
        // Process data...

        // Notify watching UIs
        eventPublisher.publishEvent(
            new DeviceDataReceivedEvent(this, deviceId, data)
        );
    }
}
```

### Step 3: Register UI Component for Key

```java
@Route("device/:deviceId")
public class DeviceMonitorView extends VerticalLayout implements HasUrlParameter<String> {

    @Autowired
    private UIEventBusHelperFactory helperFactory;

    private UIEventBusHelper helper;
    private String deviceId;

    @Override
    public void setParameter(BeforeEvent event, String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    protected void onAttach(AttachEvent event) {
        super.onAttach(event);

        helper = helperFactory.create();
        helper.registerForKey("device:" + deviceId);
        helper.register(this);
    }

    @Override
    protected void onDetach(DetachEvent event) {
        helper.unregister(this);
        helper.unregisterFromKey("device:" + deviceId);
        super.onDetach(event);
    }

    @Subscribe
    public void onDeviceData(DeviceDataReceivedEvent event) {
        // Update UI with new data
        updateChart(event.getData());
    }
}
```

---

## Tier 3: Transforming Spring Events to UI Events

If you want to transform the Spring event before it reaches UI components:

```java
public class DeviceDataReceivedEvent extends TargetedSpringEvent {

    private final String deviceId;
    private final SensorReading reading;

    // ... constructor ...

    @Override
    public String getRoutingKey() {
        return "device:" + deviceId;
    }

    @Override
    public Object toUIEvent() {
        // Transform to a simpler UI event
        return new DeviceDataUIEvent(deviceId, reading.getValue(), reading.getTimestamp());
    }
}
```

The UI component then subscribes to `DeviceDataUIEvent` instead:

```java
@Subscribe
public void onDeviceData(DeviceDataUIEvent event) {
    // Handle the transformed event
}
```

---

## Routing Key Patterns

Use consistent key patterns across your application:

| Pattern | Use Case |
|---------|----------|
| `device:{deviceId}` | Events for a specific device |
| `user:{userId}` | Events for a specific user (all their tabs) |
| `network:{networkId}` | Events for all devices in a network |
| `alert:{alertId}` | Events for a specific alert |
| `widget:{widgetId}` | Events for a specific dashboard widget |

### Multiple Keys

A UI can register for multiple keys:

```java
@Override
protected void onAttach(AttachEvent event) {
    helper = helperFactory.create();

    // Watch multiple devices
    for (String deviceId : watchedDevices) {
        helper.registerForKey("device:" + deviceId);
    }

    // Also watch network-level events
    helper.registerForKey("network:" + networkId);

    helper.register(this);
}
```

---

## Existing Event Classes

Located in `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/eventbus/`:

| Event | Purpose |
|-------|---------|
| `PendingChangesEvent` | Notify that data has been saved |
| `DeviceChangedEvent` | Notify device selection changed |
| `CloseOpenWindowsEvent` | Request to close open dialogs |
| `WidgetRefreshEvent` | Trigger widget data refresh |

---

## Thread Safety

The `UIEventBus.post()` method automatically wraps calls in `ui.access()` for thread safety when called from background threads:

```java
// Safe to call from any thread
eventBus.post(new MyEvent());
```

For backend events routed via `UIEventBusRegistry`, the same safety is applied:

```java
// In UIEventBusRegistry.postToKey()
ui.access(() -> eventBus.post(event));
```

---

## Base Class Integration

### AbstractBaseEntityForm

The form base class has built-in support via `setEventPoster()`:

```java
public class MyForm extends AbstractBaseEntityForm<MyEntity> {

    @Autowired
    public MyForm(MyEntity entity, UIEventBus eventBus) {
        super(entity, MyEntity.class, "my.form");
        setEventPoster(eventBus::post);  // Enable event posting on save
    }
}
```

### AbstractWidgetVisualizer

Widget visualizers can register for refresh events:

```java
public class MyWidgetVisualizer extends AbstractWidgetVisualizer {

    public MyWidgetVisualizer(IWidget widget, UIEventBus eventBus) {
        super(widget);
        setEventBusFunctions(eventBus::register, eventBus::unregister);
    }

    @Override
    public void register() {
        super.register();  // Registers this for @Subscribe events
    }
}
```

---

## Debugging

Enable debug logging for event flow:

```properties
# application.properties
logging.level.it.thisone.iotter.ui.eventbus=DEBUG
```

Log output examples:
```
DEBUG UIEventBus - Registering subscriber: UsersListing
DEBUG UIEventBusRegistry - Registering UI 5 for key: device:123
DEBUG SpringToUIEventBridge - Routing Spring event DeviceDataReceivedEvent to key: device:123
DEBUG UIEventBusRegistry - Posting event DeviceDataReceivedEvent to 2 UIs for key: device:123
DEBUG UIEventBus - Posting event: DeviceDataReceivedEvent
```

---

## Migration from Vaadin 8

The old pattern `UIUtils.getUIEventBus()` is no longer supported:

```java
// OLD (Vaadin 8) - throws UnsupportedOperationException
EventBusWrapper eventBus = UIUtils.getUIEventBus();

// NEW (Vaadin Flow) - use Spring injection
@Autowired
private UIEventBus eventBus;
```

For classes in `iotter-flow-ui-core` that can't directly depend on `UIEventBus`:

```java
// Use Consumer<Object> pattern
private Consumer<Object> eventPoster;

public void setEventPoster(Consumer<Object> eventPoster) {
    this.eventPoster = eventPoster;
}

// In calling code (iotter-flow-ui)
form.setEventPoster(eventBus::post);
```
