package it.thisone.iotter.ui.wizards.event;

import it.thisone.iotter.ui.wizards.Wizard;

@SuppressWarnings("serial")
public class WizardCompletedEvent extends AbstractWizardEvent {

    public WizardCompletedEvent(Wizard source) {
        super(source);
    }

}
