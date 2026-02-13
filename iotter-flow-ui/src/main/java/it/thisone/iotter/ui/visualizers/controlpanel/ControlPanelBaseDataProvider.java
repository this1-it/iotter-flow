package it.thisone.iotter.ui.visualizers.controlpanel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.data.provider.ListDataProvider;

import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.ui.graphicwidgets.ControlPanelBaseConstants;
import it.thisone.iotter.ui.visualizers.controlpanel.IconSetResolver.IconSetEnum;

public class ControlPanelBaseDataProvider extends ListDataProvider<GraphicFeed> implements ControlPanelBaseConstants {
    private static final long serialVersionUID = -8583546406470216014L;
    private int positions;
    private String currentFilter;
    private final Map<String, Integer> feedPositions = new HashMap<>();
    public static Logger logger = LoggerFactory.getLogger(ControlPanelBaseDataProvider.class);

    public ControlPanelBaseDataProvider() {
        super(new ArrayList<>());
    }

    public void addFeeds(List<GraphicFeed> feeds) {
        for (GraphicFeed feed : feeds) {
            addFeed(feed);
        }
    }

    public boolean addFeed(GraphicFeed feed) {
        String itemId = feed.getMetaData();
        if (itemId == null) {
            return false;
        }
        String sectionId = feed.getSection();
        if (sectionId == null) {
            return false;
        }

        sectionId = sectionId.toLowerCase();
        Integer positionId = null;
        if (sectionId.contains(":")) {
            String[] entries = StringUtils.split(sectionId, ":");
            sectionId = entries[0];
            try {
                positionId = Integer.parseInt(entries[1]);
            } catch (NumberFormatException ignored) {
            }
        }

        if (!Arrays.asList(NAMES).contains(sectionId)) {
            return false;
        }

        boolean exists = getItems().stream().anyMatch(existing -> itemId.equals(existing.getMetaData()));
        if (exists) {
            return false;
        }

        positions++;
        if (positionId == null) {
            positionId = positions;
        }

        feedPositions.put(itemId, positionId);
        feed.setSection(sectionId);

        if (sectionId.equals(RESET) && feed.getResourceID() == null) {
            feed.setResourceID(IconSetEnum.ICON_SET_08.name());
        }

        if (sectionId.equals(QUICKCOM) && feed.getResourceID() == null) {
            feed.setResourceID(IconSetEnum.ICON_SET_01.name());
        }

        if (!sectionId.equals(QUICKCOM) && !sectionId.equals(RESET)) {
            feed.setResourceID(null);
        }

        getItems().add(feed);
        refreshAll();
        return true;
    }

    public void filterSection(String sectionId) {
        currentFilter = sectionId;
        setFilter(feed -> sectionId == null || sectionId.equals(feed.getSection()));
        logItems(sectionId);
    }

    public void removeAllFilters() {
        currentFilter = null;
        setFilter(null);
    }

    public void removeAllItems() {
        getItems().clear();
        feedPositions.clear();
        positions = 0;
        refreshAll();
    }

    public int countChecked() {
        return (int) getItems().stream()
                .filter(feed -> currentFilter == null || currentFilter.equals(feed.getSection()))
                .filter(GraphicFeed::isChecked)
                .count();
    }

    public List<GraphicFeed> getFeeds() {
        removeAllFilters();
        List<GraphicFeed> sortedFeeds = getItems().stream()
                .sorted((f1, f2) -> {
                    int sectionCompare = f1.getSection().compareTo(f2.getSection());
                    if (sectionCompare != 0) {
                        return sectionCompare;
                    }
                    Integer pos1 = feedPositions.getOrDefault(f1.getMetaData(), 0);
                    Integer pos2 = feedPositions.getOrDefault(f2.getMetaData(), 0);
                    return Integer.compare(pos1, pos2);
                })
                .collect(Collectors.toList());

        for (GraphicFeed feed : sortedFeeds) {
            String sectionId = feed.getSection();
            if (sectionId.indexOf(":") > 0) {
                feed.setSection(sectionId.substring(0, sectionId.indexOf(':')));
            }
        }

        logItems("all");
        return sortedFeeds;
    }

    private void logItems(String caller) {
    }
}
