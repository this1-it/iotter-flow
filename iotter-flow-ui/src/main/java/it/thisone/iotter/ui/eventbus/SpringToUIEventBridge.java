package it.thisone.iotter.ui.eventbus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Bridge that listens for backend Spring events and routes them to UI sessions.
 *
 * Listens for TargetedSpringEvent subclasses and posts them to the appropriate
 * UI sessions via UIEventBusRegistry.
 *
 * Usage in backend:
 * <pre>
 * @Autowired
 * private ApplicationEventPublisher eventPublisher;
 *
 * public void onDeviceData(String deviceId, Object data) {
 *     eventPublisher.publishEvent(new DeviceDataEvent(this, deviceId, data));
 * }
 * </pre>
 */
@Component
public class SpringToUIEventBridge {

    private static final Logger logger = LoggerFactory.getLogger(SpringToUIEventBridge.class);

    private final UIEventBusRegistry registry;

    public SpringToUIEventBridge(UIEventBusRegistry registry) {
        this.registry = registry;
        logger.info("SpringToUIEventBridge initialized");
    }

    /**
     * Listen for all TargetedSpringEvent subclasses and route to UI.
     */
    @EventListener
    public void onTargetedEvent(TargetedSpringEvent event) {
        String key = event.getRoutingKey();
        Object uiEvent = event.toUIEvent();

        logger.debug("Routing Spring event {} to key: {}", event.getClass().getSimpleName(), key);

        registry.postToKey(key, uiEvent);
    }
}
