package it.thisone.iotter.ui.common.fields;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.data.TreeData;
import com.vaadin.flow.data.provider.TreeDataProvider;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.CustomField;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import org.vaadin.flow.components.PanelFlow;
import com.vaadin.flow.component.PopupView;
import com.vaadin.flow.component.Tree;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.ChannelComparator;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.MeasureUnit;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.util.PopupNotification;

// Helper class for tree nodes
class TreeNode {
    private String caption;
    private Device device;
    private Channel channel;
    
    public TreeNode(String caption, Device device, Channel channel) {
        this.caption = caption;
        this.device = device;
        this.channel = channel;
    }
    
    public String getCaption() { return caption; }
    public Device getDevice() { return device; }
    public Channel getChannel() { return channel; }
}

public class ChannelTreeSelect extends CustomField<Channel> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5206805427856901777L;
	private static Logger logger = LoggerFactory.getLogger(ChannelTreeSelect.class);
	private PanelFlow treePanel;
	private Tree<TreeNode> tree;
	private HorizontalLayout layout;
	private PopupView popup;
	private ComboBox<MeasureUnit> measures;
	
	private static final String name = "channel.select";
	private Channel currentValue;

	public ChannelTreeSelect() {
		super();
		treePanel = new PanelFlow();
		layout = new HorizontalLayout();
		layout.setSpacing(true);
		popup = new PopupView(new PopupFieldContent());
		popup.setHideOnMouseOut(false);
		layout.addComponent(popup);
		layout.addComponent(new Span(getI18nLabel("measure")));
		measures = new ComboBox<MeasureUnit>();
		measures.setSizeUndefined();
		measures.setEnabled(false);
		layout.addComponent(measures);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		measures.setEnabled(enabled);
		popup.setEnabled(enabled);
		
	}
	
	@Override
	public void setReadOnly(boolean readOnly) {
		super.setReadOnly(readOnly);
		popup.setEnabled(!readOnly);
	}

	@Override
	protected Component initContent() {
		return layout;
	}


	@Override
	protected void doSetValue(Channel value) {
		this.currentValue = value;
	}

	@Override
	public Channel getValue() {
		return this.currentValue;
	}

	@SuppressWarnings("serial")
	/**
	 * build list of available devices in group
	 * @param bean
	 */
	public void setGroup(NetworkGroup bean) {
		VerticalLayout content = new VerticalLayout();
		content.setSpacing(true);
		content.setPadding(true);

		tree = new Tree<>();
		TreeData<TreeNode> treeData = getDeviceTreeData(bean);
		TreeDataProvider<TreeNode> dataProvider = new TreeDataProvider<>(treeData);
		tree.setDataProvider(dataProvider);
		tree.setSizeFull();
		tree.setLabelCaptionGenerator(TreeNode::getCaption);

		// Expand all nodes
		treeData.getRootItems().forEach(rootItem -> tree.expand(rootItem));

		tree.addSelectionListener(event -> {
			if (!event.getAllSelectedItems().isEmpty()) {
				TreeNode selectedNode = event.getFirstSelectedItem().orElse(null);
				if (selectedNode != null && selectedNode.getChannel() != null) {
					Channel channel = selectedNode.getChannel();
					setValue(channel);
					populateMeasures(channel, null);
					popup.setPopupVisible(false);
				} else {
					PopupNotification.show(getI18nLabel("invalid_selection"));
				}
			}
		});

		content.addComponent(tree);
		content.setSizeFull();
		treePanel.setLabel(getI18nLabel("devices_in_group") + " : " + bean.getName());
		treePanel.setContent(content);
		treePanel.setHeight("200px");
		treePanel.setWidth("350px");

	}

	private TreeData<TreeNode> getDeviceTreeData(NetworkGroup bean) {
		List<Device> devices = UIUtils.getServiceFactory().getDeviceService().findByGroup(bean);
		TreeData<TreeNode> treeData = new TreeData<>();

		for (Device device : devices) {
			TreeNode deviceNode = new TreeNode(device.toString(), device, null);
			treeData.addItem(null, deviceNode);
			
			List<Channel> channels = new ArrayList<Channel>(device.getChannels());
			Collections.sort(channels, new ChannelComparator());
			for (Channel channel : channels) {
				if (!channel.getMeasures().isEmpty() && channel.getConfiguration().isActive()) {
					String channelLabel = channel.toString();
					
					MeasureUnit measureUnit = channel.getDefaultMeasure();
					if (measureUnit != null) {
						String channelUnit = UIUtils.getServiceFactory().getDeviceService().getUnitOfMeasureName(measureUnit.getType());
						channelLabel = String.format("%s [%s]", channel.toString(), channelUnit);
					}
					
					TreeNode channelNode = new TreeNode(channelLabel, device, channel);
					treeData.addItem(deviceNode, channelNode);
				}
			}
		}
		return treeData;
	}

	
	public void setMeasure(MeasureUnit measure) {
		Channel channel = getValue();
		if (channel != null) {
			populateMeasures(channel, measure);
		}
	}

	public MeasureUnit getMeasure() {
		return (MeasureUnit) measures.getValue();
	}
	

	private void populateMeasures(Channel channel, MeasureUnit value) {
		if (!channel.getMeasures().isEmpty()) {
			measures.setItems(channel.getMeasures());
			measures.setLabelCaptionGenerator(measure -> UIUtils.getServiceFactory().getDeviceService().getUnitOfMeasureName(measure.getType()));
			measures.setEnabled(true);
		}
	    measures.setValue(value);
	    measures.markAsDirty();
	    measures.addValueChangeListener(event-> {PopupNotification.show(getI18nLabel("measure_unit_warning"), Notification.Type.WARNING_MESSAGE);});
	
	}
	
	
	
    public String getI18nLabel(String key) {
		return UIUtils.localize(getI18nKey()  + "." + key);
    }

	public String getI18nKey() {
		return name;
	}

	
	class PopupFieldContent implements PopupView.Content {
		private static final long serialVersionUID = 1634116040954182604L;
		@Override
		public final Component getPopupComponent() {
			return treePanel;
		}
		@Override
		public final String getMinimizedValueAsHTML() {
			String value = getI18nLabel("channel_selection");
			Channel channel = getValue();
			if (channel != null) {
				 value = channel.toString();
			}
			return value;
		}
	};
	
}
