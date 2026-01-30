package it.thisone.iotter.ui.eventbus;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.spring.annotation.UIScope;

import org.springframework.stereotype.Component;

/**
 * Factory for creating UIEventBusHelper instances.
 *
 * This is UI-scoped, so each browser tab gets its own factory instance
 * that creates helpers bound to that UI.
 *
 * Usage:
 * <pre>
 * @Autowired
 * private UIEventBusHelperFactory helperFactory;
 *
 * // In onAttach or elsewhere when UI.getCurrent() is available
 * UIEventBusHelper helper = helperFactory.create();
 * </pre>
 */
@UIScope
@Component
public class UIEventBusHelperFactory {

    private final UIEventBus eventBus;
    private final UIEventBusRegistry registry;

    public UIEventBusHelperFactory(UIEventBus eventBus, UIEventBusRegistry registry) {
        this.eventBus = eventBus;
        this.registry = registry;
    }

    /**
     * Create a helper for the current UI.
     *
     * @return new UIEventBusHelper bound to UI.getCurrent()
     * @throws IllegalStateException if called outside UI context
     */
    public UIEventBusHelper create() {
        UI ui = UI.getCurrent();
        if (ui == null) {
            throw new IllegalStateException("No UI available. Call from UI thread (e.g., in onAttach)");
        }
        return new UIEventBusHelper(ui, eventBus, registry);
    }

    /**
     * Create a helper for a specific UI.
     *
     * @param ui the target UI
     * @return new UIEventBusHelper bound to the given UI
     */
    public UIEventBusHelper create(UI ui) {
        return new UIEventBusHelper(ui, eventBus, registry);
    }
}
