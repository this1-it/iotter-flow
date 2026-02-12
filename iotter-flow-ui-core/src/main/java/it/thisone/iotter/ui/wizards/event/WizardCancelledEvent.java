package it.thisone.iotter.ui.wizards.event;

import it.thisone.iotter.ui.wizards.Wizard;

@SuppressWarnings("serial")
public class WizardCancelledEvent extends AbstractWizardEvent {

    public WizardCancelledEvent(Wizard source) {
        super(source);
    }

}
