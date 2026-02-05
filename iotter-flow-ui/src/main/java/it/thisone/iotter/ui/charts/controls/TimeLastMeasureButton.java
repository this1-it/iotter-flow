package it.thisone.iotter.ui.charts.controls;

import java.util.Date;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;

import it.thisone.iotter.ui.charts.AbstractChartAdapter;

public class TimeLastMeasureButton extends Button {

	private static final long serialVersionUID = -5379670082270644619L;
	private final AbstractChartAdapter chart;

	public TimeLastMeasureButton(String caption, AbstractChartAdapter adapter) {
		super();
		getElement().setProperty("title", caption);
		setIcon(VaadinIcon.BULLSEYE.create());
		addClassName("timefield");
		this.chart = adapter;
	}

	public Date getLastMeasureDate() {
		return chart.lastMeasureDate();
	}
}
