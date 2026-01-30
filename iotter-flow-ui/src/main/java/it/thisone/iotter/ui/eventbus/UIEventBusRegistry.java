package it.thisone.iotter.ui.eventbus;

import com.vaadin.flow.component.UI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Registry for routing backend Spring events to specific UI sessions.
 *
 * UIs register with a custom key (e.g., user ID, device ID, network ID).
 * Backend events can then be routed to specific UIs by key.
 *
 * A single UI can register multiple keys (e.g., user watches multiple devices).
 * Multiple UIs can register the same key (e.g., user has multiple tabs).
 */
@Component
public class UIEventBusRegistry {

    private static final Logger logger = LoggerFactory.getLogger(UIEventBusRegistry.class);

    // Maps custom key -> set of (UI, UIEventBus) pairs
    private final ConcurrentMap<String, ConcurrentMap<UI, UIEventBus>> keyToEventBuses = new ConcurrentHashMap<>();

    // Maps UI -> set of registered keys (for cleanup on detach)
    private final ConcurrentMap<UI, ConcurrentMap<String, Boolean>> uiToKeys = new ConcurrentHashMap<>();

    /**
     * Register a UI's event bus with a custom key.
     * Call this when UI needs to receive events for a specific entity/context.
     *
     * @param key the routing key (e.g., "user:123", "device:456", "network:789")
     * @param ui the Vaadin UI
     * @param eventBus the UI's event bus
     */
    public void register(String key, UI ui, UIEventBus eventBus) {
        logger.debug("Registering UI {} for key: {}", ui.getUIId(), key);

        keyToEventBuses
            .computeIfAbsent(key, k -> new ConcurrentHashMap<>())
            .put(ui, eventBus);

        uiToKeys
            .computeIfAbsent(ui, u -> new ConcurrentHashMap<>())
            .put(key, Boolean.TRUE);
    }

    /**
     * Unregister a UI from a specific key.
     *
     * @param key the routing key
     * @param ui the Vaadin UI
     */
    public void unregister(String key, UI ui) {
        logger.debug("Unregistering UI {} from key: {}", ui.getUIId(), key);

        ConcurrentMap<UI, UIEventBus> eventBuses = keyToEventBuses.get(key);
        if (eventBuses != null) {
            eventBuses.remove(ui);
            if (eventBuses.isEmpty()) {
                keyToEventBuses.remove(key);
            }
        }

        ConcurrentMap<String, Boolean> keys = uiToKeys.get(ui);
        if (keys != null) {
            keys.remove(key);
        }
    }

    /**
     * Unregister a UI from all keys.
     * Call this when UI is detached.
     *
     * @param ui the Vaadin UI
     */
    public void unregisterAll(UI ui) {
        logger.debug("Unregistering UI {} from all keys", ui.getUIId());

        ConcurrentMap<String, Boolean> keys = uiToKeys.remove(ui);
        if (keys != null) {
            for (String key : keys.keySet()) {
                ConcurrentMap<UI, UIEventBus> eventBuses = keyToEventBuses.get(key);
                if (eventBuses != null) {
                    eventBuses.remove(ui);
                    if (eventBuses.isEmpty()) {
                        keyToEventBuses.remove(key);
                    }
                }
            }
        }
    }

    /**
     * Post an event to all UIs registered with the given key.
     * Each UI receives the event in its own ui.access() context.
     *
     * @param key the routing key
     * @param event the event to post
     */
    public void postToKey(String key, Object event) {
        ConcurrentMap<UI, UIEventBus> eventBuses = keyToEventBuses.get(key);
        if (eventBuses == null || eventBuses.isEmpty()) {
            logger.debug("No UIs registered for key: {}", key);
            return;
        }

        logger.debug("Posting event {} to {} UIs for key: {}",
            event.getClass().getSimpleName(), eventBuses.size(), key);

        eventBuses.forEach((ui, eventBus) -> {
            if (ui.isAttached()) {
                ui.access(() -> eventBus.post(event));
            }
        });
    }

    /**
     * Get all keys registered by a UI.
     *
     * @param ui the Vaadin UI
     * @return collection of registered keys
     */
    public Collection<String> getKeysForUI(UI ui) {
        ConcurrentMap<String, Boolean> keys = uiToKeys.get(ui);
        return keys != null ? keys.keySet() : Collections.emptySet();
    }

    /**
     * Check if any UI is registered for a key.
     *
     * @param key the routing key
     * @return true if at least one UI is registered
     */
    public boolean hasRegistrations(String key) {
        ConcurrentMap<UI, UIEventBus> eventBuses = keyToEventBuses.get(key);
        return eventBuses != null && !eventBuses.isEmpty();
    }
}
