package it.thisone.iotter.ui.gridstack;

import java.util.List;

import it.thisone.iotter.enums.GraphicWidgetType;
import it.thisone.iotter.persistence.model.GraphicWidget;

public final class GridstackLayoutUtils {

    private static final int GRID_COLUMNS = 12;

    private GridstackLayoutUtils() {
    }

    public static String convertLegacyToGridJson(List<GraphicWidget> widgets) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (GraphicWidget widget : widgets) {
            if (widget.getParent() != null) {
                continue;
            }
            if (!first) {
                sb.append(",");
            }
            first = false;

            int[] defaultSize = getDefaultGridSize(widget.getType());
            int gx = Math.round(widget.getX() * GRID_COLUMNS);
            int gw = Math.max(1, Math.round(widget.getWidth() * GRID_COLUMNS));
            int gy = Math.round(widget.getY() * 20);
            int gh = defaultSize[1];

            if (gx + gw > GRID_COLUMNS) {
                gx = 0;
            }

            sb.append("{\"id\":\"").append(escapeJson(widget.getId())).append("\"");
            sb.append(",\"x\":").append(gx);
            sb.append(",\"y\":").append(gy);
            sb.append(",\"w\":").append(gw);
            sb.append(",\"h\":").append(gh);
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    public static int[] getDefaultGridSize(GraphicWidgetType type) {
        if (type == null) {
            return new int[]{6, 6};
        }
        switch (type) {
            case MULTI_TRACE:
                return new int[]{12, 8};
            case HISTOGRAM:
                return new int[]{12, 8};
            case LABEL:
                return new int[]{12, 1};
            case LAST_MEASURE_TABLE:
                return new int[]{5, 4};
            case LAST_MEASURE:
                return new int[]{3, 3};
            case TABLE:
                return new int[]{6, 8};
            case EMBEDDED:
                return new int[]{6, 8};
            case WEBPAGE:
                return new int[]{6, 8};
            case WIND_ROSE:
                return new int[]{6, 8};
            case CUSTOM:
            default:
                return new int[]{6, 6};
        }
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
