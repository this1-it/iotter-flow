package it.thisone.iotter.ui.wizards;

import java.util.List;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;

import it.thisone.iotter.ui.wizards.event.WizardCancelledEvent;
import it.thisone.iotter.ui.wizards.event.WizardCompletedEvent;
import it.thisone.iotter.ui.wizards.event.WizardProgressListener;
import it.thisone.iotter.ui.wizards.event.WizardStepActivationEvent;
import it.thisone.iotter.ui.wizards.event.WizardStepSetChangedEvent;

/**
 * Displays a progress bar for a {@link Wizard}.
 */
@CssImport("./styles/wizard-progress-bar.css")
public class WizardProgressBar extends Composite<VerticalLayout> implements WizardProgressListener {

    private final Wizard wizard;
    private final ProgressBar progressBar = new ProgressBar();
    private final HorizontalLayout stepCaptions = new HorizontalLayout();
    private int activeStepIndex;

    public WizardProgressBar(Wizard wizard) {
        getElement().getClassList().add("wizard-progress-bar");
        this.wizard = wizard;

        stepCaptions.setWidthFull();
        stepCaptions.setPadding(false);
        stepCaptions.setSpacing(false);
        progressBar.setWidthFull();

        VerticalLayout layout = getContent();
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.setWidthFull();
        layout.add(stepCaptions, progressBar);
    }

    private void updateProgressBar() {
        int stepCount = wizard.getSteps().size();
        if (stepCount == 0) {
            progressBar.setValue(0.0);
            return;
        }
        float padding = (1.0f / stepCount) / 2;
        float progressValue = padding + activeStepIndex / (float) stepCount;
        progressBar.setValue(progressValue);
    }

    private void updateStepCaptions() {
        stepCaptions.removeAll();
        int index = 1;
        for (WizardStep step : wizard.getSteps()) {
            Span label = createCaptionLabel(index, step);
            stepCaptions.add(label);
            index++;
        }
    }

    private Span createCaptionLabel(int index, WizardStep step) {
        Span label = new Span(index + ". " + step.getCaption());
        label.addClassName("step-caption");
        label.getStyle().set("flex", "1 1 0");

        // Add styles for themeing.
        if (wizard.isCompleted(step)) {
            label.addClassName("completed");
        }
        if (wizard.isActive(step)) {
            label.addClassName("current");
        }
        if (wizard.isFirstStep(step)) {
            label.addClassName("first");
        }
        if (wizard.isLastStep(step)) {
            label.addClassName("last");
        }

        return label;
    }

    private void updateProgressAndCaptions() {
        updateProgressBar();
        updateStepCaptions();
    }

    @Override
    public void activeStepChanged(WizardStepActivationEvent event) {
        List<WizardStep> allSteps = wizard.getSteps();
        activeStepIndex = allSteps.indexOf(event.getActivatedStep());
        updateProgressAndCaptions();
    }

    @Override
    public void stepSetChanged(WizardStepSetChangedEvent event) {
        updateProgressAndCaptions();
    }

    @Override
    public void wizardCompleted(WizardCompletedEvent event) {
        progressBar.setValue(1.0f);
        updateStepCaptions();
    }

    @Override
    public void wizardCancelled(WizardCancelledEvent event) {
        // NOP, no need to react to cancellation
    }
}
