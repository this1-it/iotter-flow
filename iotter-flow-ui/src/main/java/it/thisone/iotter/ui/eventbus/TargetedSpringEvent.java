package it.thisone.iotter.ui.eventbus;

import org.springframework.context.ApplicationEvent;

/**
 * Base class for Spring events that should be routed to specific UI sessions.
 *
 * Extend this class and implement getRoutingKey() to specify which UIs
 * should receive the event.
 *
 * Example:
 * <pre>
 * public class DeviceDataEvent extends TargetedSpringEvent {
 *     private final String deviceId;
 *     private final Object data;
 *
 *     public DeviceDataEvent(Object source, String deviceId, Object data) {
 *         super(source);
 *         this.deviceId = deviceId;
 *         this.data = data;
 *     }
 *
 *     @Override
 *     public String getRoutingKey() {
 *         return "device:" + deviceId;
 *     }
 *
 *     // Override to transform to UI event, or return this to use as-is
 *     @Override
 *     public Object toUIEvent() {
 *         return new DeviceDataUIEvent(deviceId, data);
 *     }
 * }
 * </pre>
 */
public abstract class TargetedSpringEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    public TargetedSpringEvent(Object source) {
        super(source);
    }

    /**
     * Get the routing key for this event.
     * UIs that registered with this key will receive the event.
     *
     * Common patterns:
     * - "user:{userId}" - events for a specific user
     * - "device:{deviceId}" - events for a specific device
     * - "network:{networkId}" - events for a specific network
     *
     * @return the routing key
     */
    public abstract String getRoutingKey();

    /**
     * Transform this Spring event to a UI event.
     * Override to convert to a different event type for the UI layer.
     * Default returns this event as-is.
     *
     * @return the event to post to UIEventBus
     */
    public Object toUIEvent() {
        return this;
    }
}
