package it.thisone.iotter.ui.eventbus;

import com.google.common.eventbus.EventBus;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.spring.annotation.UIScope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * UI-scoped event bus for inter-component communication.
 * Each browser tab/window gets its own instance.
 *
 * This replaces the old Vaadin 8 pattern of accessing the event bus via UIUtils.getUIEventBus()
 * which relied on casting UI.getCurrent() to IMainUI.
 *
 * Usage:
 * - Inject UIEventBus via @Autowired in your component
 * - Call register(this) in onAttach() and unregister(this) in onDetach()
 * - Use @Subscribe annotation on methods to receive events
 * - Use post(event) to publish events
 */
@UIScope
@Component
public class UIEventBus {

    private static final Logger logger = LoggerFactory.getLogger(UIEventBus.class);

    private final EventBus eventBus;

    public UIEventBus() {
        this.eventBus = new EventBus("UIEventBus-" + System.identityHashCode(this));
        logger.debug("Created UIEventBus instance: {}", this.eventBus.identifier());
    }

    /**
     * Register a subscriber to receive events.
     * Call this in onAttach() of your component.
     *
     * @param subscriber the object with @Subscribe methods
     */
    public void register(Object subscriber) {
        logger.debug("Registering subscriber: {}", subscriber.getClass().getSimpleName());
        eventBus.register(subscriber);
    }

    /**
     * Unregister a subscriber from receiving events.
     * Call this in onDetach() of your component.
     *
     * @param subscriber the object to unregister
     */
    public void unregister(Object subscriber) {
        try {
            logger.debug("Unregistering subscriber: {}", subscriber.getClass().getSimpleName());
            eventBus.unregister(subscriber);
        } catch (IllegalArgumentException ignored) {
            // Already unregistered or never registered
            logger.trace("Subscriber was not registered: {}", subscriber.getClass().getSimpleName());
        }
    }

    /**
     * Post an event to all registered subscribers.
     * Ensures thread-safe UI updates via ui.access() if called from background thread.
     *
     * @param event the event to post
     */
    public void post(Object event) {
        UI ui = UI.getCurrent();
        if (ui != null && ui.isAttached()) {
            ui.access(() -> {
                logger.debug("Posting event: {}", event.getClass().getSimpleName());
                eventBus.post(event);
            });
        } else {
            // Direct post if already in UI thread or no UI context
            logger.debug("Posting event (no UI context): {}", event.getClass().getSimpleName());
            eventBus.post(event);
        }
    }

    /**
     * Get the underlying EventBus for advanced use cases.
     *
     * @return the Google EventBus instance
     */
    public EventBus getEventBus() {
        return eventBus;
    }
}
