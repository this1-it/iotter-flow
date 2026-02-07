package it.thisone.iotter.ui.devices;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.enums.ChartScaleType;
import it.thisone.iotter.enums.GraphicWidgetType;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.ui.common.AbstractBaseEntityDetails;
import it.thisone.iotter.ui.common.EditorConstraintException;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.ChartUtils;


public class DeviceRollupActivity 
extends AbstractBaseEntityDetails<Device> {
	// TODO(flow-migration): manual refactor required for Vaadin 8 APIs removed in Flow (dialogs/tabs/legacy layout or UIUtils context access).

	/**
	 * 
	 */
	private static final long serialVersionUID = 5961351166441753740L;
	
	public DeviceRollupActivity(Device item) {
		super(item, Device.class, DeviceForm.NAME, new String[] {}, false);
		getSelectButton().setVisible(false);
		getRemoveButton().setVisible(false);
		getCancelButton().setCaption(getTranslation("basic.editor.close"));
		setDetails(getContentLayout());
	}

	
	public float[] getWindowDimension() {
		return UIUtils.L_DIMENSION;
	}
	
	public String getWindowStyle() {
		return "device-editor";
	}

	private Layout getContentLayout() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		mainLayout.setPadding(true);
		mainLayout.setSizeFull();
		Device device = this.getBean();
		
		Channel chnl = null;
		
		for (Channel channel : device.getChannels()) {
			if (channel.getConfiguration().isActive()) {
				chnl = channel;
				break;
			}
		}
		

		GraphicWidget widget = new GraphicWidget();
		widget.setType(GraphicWidgetType.MULTI_TRACE);
		
		if (chnl != null) {
			widget.setLabel(chnl.getConfiguration().getDisplayName());
			widget.getOptions().setScale(ChartScaleType.LINEAR);
			widget.getOptions().setShowGrid(false);
			widget.getOptions().setExporting(false);
			widget.getOptions().setRealTime(false);
			widget.getOptions().setZoomable(false);
			GraphicFeed feed = new GraphicFeed();
			feed.getOptions().setFillColor(ChartUtils.hexColor(0));
			feed.setChannel(chnl);
			feed.setMeasure(chnl.getDefaultMeasure());
			widget.addFeed(feed);
		}

//		RollupActivityChartAdapter chartAdapter = new RollupActivityChartAdapter(widget);
//		chartAdapter.setHeight(100, Unit.PERCENTAGE);
//		chartAdapter.setWidth(100, Unit.PERCENTAGE);
//		chartAdapter.register();
//
//		mainLayout.addComponent(chartAdapter);
//		mainLayout.setExpandRatio(chartAdapter, 1f);
		return mainLayout;

		
	}


	@Override
	protected void onRemove() throws EditorConstraintException {
		
	}




	


}
