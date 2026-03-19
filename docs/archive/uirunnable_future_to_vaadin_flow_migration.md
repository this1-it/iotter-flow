# Migrating `UIRunnableFuture` (Vaadin 8) to Vaadin Flow

This document provides a **drop-in migration recipe** to remove the legacy
`UIRunnableFuture` / `UIRunnable` pattern used in Vaadin 7/8 and replace it with
**idiomatic Vaadin Flow concurrency**.

This file is meant to be **downloaded as-is** and committed to the repository.

---

## 1. Why `UIRunnableFuture` must be removed

`UIRunnableFuture` existed to solve Vaadin 7/8 problems:

- Manual UI locking
- Background → UI thread synchronization
- Completion events via `EventRouter`
- Reflection-based listener dispatch (`ReflectTools`)
- Custom future orchestration (`CountDownLatch`)

In **Vaadin Flow**, these concerns are already handled by the framework.

### Key difference

| Concern | Vaadin 8 | Vaadin Flow |
|------|---------|-------------|
| UI thread safety | manual | automatic |
| UI synchronization | custom | `UI.access()` |
| Futures | wrapped | standard Java |
| Completion callbacks | `EventRouter` | `CompletableFuture` |
| Locking | developer-managed | framework-managed |

**Conclusion:** `UIRunnableFuture` is obsolete and should be **deleted**, not migrated.

---

## 2. Identify legacy usage

Search for the following symbols:

```
UIRunnableFuture
UIRunnable
addCompleteListener
CompleteListener
runInBackground
runInUI
EventRouter
ReflectTools
```

Typical Vaadin 8 usage:

```java
UIRunnableFuture task = new UIRunnableFuture(ui);
executor.submit(task);
task.addCompleteListener(evt -> onComplete());
```

---

## 3. Canonical Vaadin Flow replacement

### Old (Vaadin 8)

```java
UIRunnableFuture task =
    new UIRunnableFuture(
        () -> loadData(),
        () -> updateUI(),
        ui
    );

executor.submit(task);

task.addCompleteListener(evt -> {
    Notification.show("Done");
});
```

### New (Vaadin Flow)

```java
CompletableFuture
    .runAsync(() -> loadData(), executor)
    .whenComplete((v, ex) -> {
        ui.access(() -> {
            if (ex != null) {
                handleError(ex);
            } else {
                updateUI();
                Notification.show("Done");
            }
        });
    });
```

---

## 4. Method mapping

| Legacy API | Vaadin Flow equivalent |
|----------|------------------------|
| `runInBackground()` | `CompletableFuture.runAsync()` |
| `runInUI(Throwable)` | `ui.access(() -> ...)` |
| completion event | `whenComplete((v, ex) -> ...)` |
| cancel / done | `CompletableFuture` API |

---

## 5. Replacing `CompleteListener`

### Old

```java
task.addCompleteListener(evt -> onComplete());
```

### New

```java
CompletableFuture
    .runAsync(...)
    .whenComplete((v, ex) ->
        ui.access(this::onComplete)
    );
```

If multiple listeners are required, use standard Java collections:

```java
List<Runnable> listeners = new CopyOnWriteArrayList<>();
```

---

## 6. Optional helper (recommended)

If you want a compact reusable abstraction, introduce a **small utility**, not a framework.

```java
public final class UiAsync {

    private UiAsync() {}

    public static CompletableFuture<Void> run(
            UI ui,
            Executor executor,
            Runnable background,
            Consumer<Throwable> uiCallback) {

        return CompletableFuture
            .runAsync(background, executor)
            .whenComplete((v, ex) ->
                ui.access(() -> uiCallback.accept(ex))
            );
    }
}
```

Usage:

```java
UiAsync.run(
    UI.getCurrent(),
    executor,
    this::loadData,
    ex -> {
        if (ex == null) {
            updateUI();
        } else {
            handleError(ex);
        }
    }
);
```

---

## 7. Mandatory deletion checklist

After migration, **delete entirely**:

```
UIRunnableFuture.java
UIRunnable.java
CompleteEvent
CompleteListener
EventRouter usage
ReflectTools usage
CountDownLatch usage (UI-related)
```

Verify no remaining imports from:

```
com.vaadin.event.EventRouter
com.vaadin.util.ReflectTools
```

---

## 8. Vaadin Flow concurrency rules (authoritative)

- ❌ Never touch UI components outside `UI.access()`
- ✅ Background threads are allowed
- ✅ `UI.access()` returns `Future<Void>`
- ✅ Prefer `CompletableFuture` over `Executor.submit()`
- ❌ No manual UI/session locking
- ❌ No reflection-based event routing

---

## 9. Migration success criteria

- Project compiles without `UIRunnable*`
- No `EventRouter` or reflection-based listeners
- No `CountDownLatch` coordinating UI logic
- All UI updates happen inside `UI.access()`
- Background logic uses plain Java concurrency

---

## 10. Summary

**Vaadin Flow removes the need for custom UI task frameworks.**

If you see:

- `UIRunnableFuture`
- `EventRouter`
- `ReflectTools`
- `CountDownLatch` tied to UI logic

→ **Delete and replace with `CompletableFuture + UI.access()`**

This reduces complexity, improves correctness, and aligns with Flow’s design.

---

_End of document_

