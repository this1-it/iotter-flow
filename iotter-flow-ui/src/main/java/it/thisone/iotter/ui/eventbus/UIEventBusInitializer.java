package it.thisone.iotter.ui.eventbus;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.spring.annotation.SpringComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * Initializes UIEventBus for each new UI.
 *
 * This class implements VaadinServiceInitListener to hook into the Vaadin
 * service lifecycle. For each new UI that is created, it:
 * 1. Gets the UI-scoped UIEventBus from Spring
 * 2. Stores it in the UI session for easy access
 * 3. Cleans up registry entries on UI detach
 *
 * Vaadin Spring automatically discovers this bean via @SpringComponent annotation.
 */
@SpringComponent
public class UIEventBusInitializer implements VaadinServiceInitListener {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(UIEventBusInitializer.class);

    private final ApplicationContext applicationContext;
    private final UIEventBusRegistry registry;

    @Autowired
    public UIEventBusInitializer(ApplicationContext applicationContext, UIEventBusRegistry registry) {
        this.applicationContext = applicationContext;
        this.registry = registry;
        logger.info("UIEventBusInitializer created");
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        logger.debug("Vaadin service init - adding UI init listener");

        event.getSource().addUIInitListener(uiInitEvent -> {
            UI ui = uiInitEvent.getUI();
            logger.debug("UI initialized: {}", ui.getUIId());

            // Get the UI-scoped UIEventBus from Spring context
            // The @UIScope annotation ensures each UI gets its own instance
            UIEventBus eventBus = applicationContext.getBean(UIEventBus.class);

            // Store in UI session for easy access
            ui.getSession().setAttribute(UIEventBus.class, eventBus);

            // Clean up registry on detach
            ui.addDetachListener(detachEvent -> {
                logger.debug("UI detached: {}", ui.getUIId());
                registry.unregisterAll(ui);
            });
        });
    }
}
