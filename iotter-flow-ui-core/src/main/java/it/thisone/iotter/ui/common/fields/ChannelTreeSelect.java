package it.thisone.iotter.ui.common.fields;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.AbstractCompositeField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;

import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;

import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.ChannelComparator;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.MeasureUnit;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.util.PopupNotification;

/* ------------------------------------------------------------------
 * Helper node for TreeGrid
 * ------------------------------------------------------------------ */
class TreeNode {

    private final String caption;
    private final Device device;
    private final Channel channel;

    TreeNode(String caption, Device device, Channel channel) {
        this.caption = caption;
        this.device = device;
        this.channel = channel;
    }

    public String getCaption() {
        return caption;
    }

    public Device getDevice() {
        return device;
    }

    public Channel getChannel() {
        return channel;
    }
}

/* ------------------------------------------------------------------
 * Main field
 * ------------------------------------------------------------------ */
public class ChannelTreeSelect
        extends AbstractCompositeField<HorizontalLayout, ChannelTreeSelect, Channel> {

    private static final long serialVersionUID = 1L;
 

    private static final String I18N_KEY = "channel.select";

    /* UI components */
    private final Button trigger;
    private final ContextMenu popup;
    private final TreeGrid<TreeNode> tree;
    private final ComboBox<MeasureUnit> measures;

    public ChannelTreeSelect() {
        super(null);

        HorizontalLayout layout = getContent();
        layout.setSpacing(true);

        /* Trigger button (PopupView replacement) */
        trigger = new Button(getI18nLabel("channel_selection"));
        popup = new ContextMenu(trigger);
        popup.setOpenOnClick(true);

        /* TreeGrid (Tree replacement) */
        tree = new TreeGrid<>();
        tree.addHierarchyColumn(TreeNode::getCaption)
            .setHeader(getI18nLabel("devices_in_group"));
        tree.setHeight("200px");
        tree.setWidth("350px");

        popup.add(tree);

        /* Measure selector */
        measures = new ComboBox<>();
        measures.setEnabled(false);

        layout.add(
            trigger,
            new Span(getI18nLabel("measure")),
            measures
        );
    }

    /* ------------------------------------------------------------------
     * Enable / readOnly propagation
     * ------------------------------------------------------------------ */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        trigger.setEnabled(enabled);
        measures.setEnabled(enabled);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        trigger.setEnabled(!readOnly);
        measures.setReadOnly(readOnly);
    }

    /* ------------------------------------------------------------------
     * Group population
     * ------------------------------------------------------------------ */
    public void setGroup(NetworkGroup group) {

        TreeData<TreeNode> data = new TreeData<>();
        buildTreeData(data, group);

        TreeDataProvider<TreeNode> provider =
                new TreeDataProvider<>(data);
        tree.setDataProvider(provider);

        data.getRootItems().forEach(tree::expand);

        tree.addSelectionListener(event ->
            event.getFirstSelectedItem().ifPresent(node -> {
                if (node.getChannel() != null) {
                    setModelValue(node.getChannel(), true);
                    trigger.setText(node.getCaption());
                    populateMeasures(node.getChannel(), null);
                    popup.close();
                } else {
                    PopupNotification.show(
                        getI18nLabel("invalid_selection")
                    );
                }
            })
        );
    }

    /* ------------------------------------------------------------------
     * Tree building
     * ------------------------------------------------------------------ */
    private void buildTreeData(TreeData<TreeNode> treeData,
                               NetworkGroup group) {

        List<Device> devices =
            UIUtils.getServiceFactory()
                   .getDeviceService()
                   .findByGroup(group);

        for (Device device : devices) {

            TreeNode deviceNode =
                    new TreeNode(device.toString(), device, null);
            treeData.addItem(null, deviceNode);

            List<Channel> channels =
                    new ArrayList<>(device.getChannels());
            Collections.sort(channels, new ChannelComparator());

            for (Channel channel : channels) {
                if (!channel.getMeasures().isEmpty()
                        && channel.getConfiguration().isActive()) {

                    String label = channel.toString();
                    MeasureUnit mu = channel.getDefaultMeasure();

                    if (mu != null) {
                        String unit =
                            UIUtils.getServiceFactory()
                                   .getDeviceService()
                                   .getUnitOfMeasureName(mu.getType());
                        label = label + " [" + unit + "]";
                    }

                    treeData.addItem(
                        deviceNode,
                        new TreeNode(label, device, channel)
                    );
                }
            }
        }
    }

    /* ------------------------------------------------------------------
     * Measure handling
     * ------------------------------------------------------------------ */
    public void setMeasure(MeasureUnit measure) {
        Channel channel = getValue();
        if (channel != null) {
            populateMeasures(channel, measure);
        }
    }

    public MeasureUnit getMeasure() {
        return measures.getValue();
    }

    private void populateMeasures(Channel channel, MeasureUnit value) {
        measures.setItems(channel.getMeasures());
        measures.setItemLabelGenerator(mu ->
            UIUtils.getServiceFactory()
                   .getDeviceService()
                   .getUnitOfMeasureName(mu.getType())
        );
        measures.setEnabled(true);
        measures.setValue(value);

        measures.addValueChangeListener(e ->
            PopupNotification.show(
                getI18nLabel("measure_unit_warning"),
                PopupNotification.Type.WARNING
            )
        );
    }

    /* ------------------------------------------------------------------
     * Presentation lifecycle
     * ------------------------------------------------------------------ */
    @Override
    protected void setPresentationValue(Channel value) {
        if (value == null) {
            trigger.setText(getI18nLabel("channel_selection"));
            measures.clear();
            measures.setEnabled(false);
        }
    }

    /* ------------------------------------------------------------------
     * i18n helpers
     * ------------------------------------------------------------------ */
    private String getI18nLabel(String key) {
        return getTranslation(getI18nKey() + "." + key);
    }

    private String getI18nKey() {
        return I18N_KEY;
    }
}
