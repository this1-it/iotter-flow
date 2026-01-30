package it.thisone.iotter.ui.eventbus;

import com.vaadin.flow.component.UI;

/**
 * Helper for registering UI event bus with routing keys.
 *
 * Inject this in your component to easily register/unregister for backend events.
 *
 * Example usage in a component:
 * <pre>
 * @Autowired
 * private UIEventBusHelper eventBusHelper;
 *
 * @Override
 * protected void onAttach(AttachEvent event) {
 *     super.onAttach(event);
 *     // Register for events targeted to this device
 *     eventBusHelper.registerForKey("device:" + deviceId);
 *     // Register for @Subscribe methods
 *     eventBusHelper.getEventBus().register(this);
 * }
 *
 * @Override
 * protected void onDetach(DetachEvent event) {
 *     eventBusHelper.getEventBus().unregister(this);
 *     eventBusHelper.unregisterFromKey("device:" + deviceId);
 *     super.onDetach(event);
 * }
 *
 * @Subscribe
 * public void onDeviceData(DeviceDataUIEvent event) {
 *     // Handle event
 * }
 * </pre>
 */
public class UIEventBusHelper {

    private final UI ui;
    private final UIEventBus eventBus;
    private final UIEventBusRegistry registry;

    public UIEventBusHelper(UI ui, UIEventBus eventBus, UIEventBusRegistry registry) {
        this.ui = ui;
        this.eventBus = eventBus;
        this.registry = registry;
    }

    /**
     * Register the current UI to receive events for the given key.
     *
     * @param key the routing key (e.g., "device:123", "user:456")
     */
    public void registerForKey(String key) {
        registry.register(key, ui, eventBus);
    }

    /**
     * Unregister the current UI from events for the given key.
     *
     * @param key the routing key
     */
    public void unregisterFromKey(String key) {
        registry.unregister(key, ui);
    }

    /**
     * Get the UIEventBus for direct event posting and subscriber registration.
     *
     * @return the UIEventBus
     */
    public UIEventBus getEventBus() {
        return eventBus;
    }

    /**
     * Post an event to the local UI event bus.
     *
     * @param event the event to post
     */
    public void post(Object event) {
        eventBus.post(event);
    }

    /**
     * Register a subscriber to receive events.
     *
     * @param subscriber the object with @Subscribe methods
     */
    public void register(Object subscriber) {
        eventBus.register(subscriber);
    }

    /**
     * Unregister a subscriber.
     *
     * @param subscriber the subscriber to unregister
     */
    public void unregister(Object subscriber) {
        eventBus.unregister(subscriber);
    }
}
