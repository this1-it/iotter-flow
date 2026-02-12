package it.thisone.iotter.ui.wizards.event;

import it.thisone.iotter.ui.wizards.Wizard;

public class AbstractWizardEvent {

    private final Wizard source;

    protected AbstractWizardEvent(Wizard source) {
        this.source = source;
    }

    /**
     * Returns the {@link Wizard} component that was the source of this event.
     * 
     * @return the source {@link Wizard} of this event.
     */
    public Wizard getWizard() {
        return source;
    }
}
