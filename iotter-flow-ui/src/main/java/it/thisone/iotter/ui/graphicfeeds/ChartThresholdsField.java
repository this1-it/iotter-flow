package it.thisone.iotter.ui.graphicfeeds;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.customfield.CustomField;

import it.thisone.iotter.persistence.model.ChartThreshold;
import it.thisone.iotter.persistence.model.GraphicFeed;

public class ChartThresholdsField extends CustomField<List<ChartThreshold>> {

    private static final long serialVersionUID = -2936983441775994254L;

    private List<ChartThreshold> currentValue;
    private final ChartThresholdMasterDetailsListing listing;

    public ChartThresholdsField() {
        listing = new ChartThresholdMasterDetailsListing();
    }

    @Override
    protected List<ChartThreshold> generateModelValue() {
        return currentValue;
    }

    @Override
    protected void setPresentationValue(List<ChartThreshold> newValue) {
        currentValue = newValue;
        List<ChartThreshold> thresholds = new ArrayList<>();
        if (newValue != null) {
            thresholds.addAll(newValue);
        }
        listing.setThresholds(thresholds);
    }

    protected Component initContent() {
        return listing;
    }

    public void setFeed(GraphicFeed feed) {
        listing.setFeed(feed);
    }
}
