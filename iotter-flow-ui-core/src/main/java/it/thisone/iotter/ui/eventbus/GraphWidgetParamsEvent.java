package it.thisone.iotter.ui.eventbus;

public class GraphWidgetParamsEvent {
    private final String parent;
    public GraphWidgetParamsEvent(String id) {
    	this.parent = id;
    }
	public String getParent() {
		return parent;
	}
}
