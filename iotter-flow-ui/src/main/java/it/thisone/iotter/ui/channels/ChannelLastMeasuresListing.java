package it.thisone.iotter.ui.channels;

import java.text.ChoiceFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.ListDataProvider;

import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.ui.ifc.IChannelListingField;
import it.thisone.iotter.ui.ifc.ITabContent;
import it.thisone.iotter.ui.model.ChannelAdapter;
import it.thisone.iotter.ui.model.ChannelAdapterDataProvider;

public class ChannelLastMeasuresListing extends VerticalLayout implements IChannelListingField, ITabContent {

	private static final long serialVersionUID = 2001077544797472399L;

	private final Grid<ChannelAdapter> grid = new Grid<>(ChannelAdapter.class, false);
	private Collection<Channel> channels;

	private ChannelAdapterDataProvider provider;

	public ChannelLastMeasuresListing() {
		this.provider = new ChannelAdapterDataProvider();
		this.channels = new ArrayList<>();
		setSizeFull();
		grid.setSizeFull();
		grid.addColumn(ChannelAdapter::getDisplayName).setKey("displayName")
				.setHeader(getTranslation("device.displayName"));
		grid.addColumn(ChannelAdapter::getTypeVar).setKey("typeVar").setHeader(getTranslation("device.typeVar"));
		grid.addColumn(ChannelAdapter::getLastMeasureDate).setKey("lastMeasureDate")
				.setHeader(getTranslation("device.lastMeasureDate"));
		grid.addColumn(ChannelAdapter::getLastMeasureValue).setKey("lastMeasureValue")
				.setHeader(getTranslation("device.lastMeasureValue"));
		grid.addColumn(ChannelAdapter::getMeasureUnit).setKey("measureUnit")
				.setHeader(getTranslation("device.measureUnit"));
		grid.addColumn(ChannelAdapter::getLastMeasure).setKey("lastMeasure")
				.setHeader(getTranslation("device.lastMeasure"));
		grid.addColumn(ChannelAdapter::getPattern).setKey("pattern").setHeader(getTranslation("device.pattern"));
		add(grid);
		setFlexGrow(1f, grid);
	}

	public void setItems(List<Channel> channels, ChoiceFormat cf) {
		provider.setMeasureRenderer(cf);
		provider.addChannels(channels);
		provider.refresh();
		grid.setDataProvider(provider);

	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends Collection<Channel>> getType() {
		return (Class<? extends Collection<Channel>>) (Class<?>) Collection.class;
	}

	@Override
	public void setValue(Collection<Channel> collection) {
		channels = collection;

	}

	@Override
	public Collection<Channel> getValue() {
		return channels;
	}

	@Override
	public void validate() throws ValidationException {
	}

	@Override
	public void commit() throws ValidationException {
	}

	@Override
	public boolean isLoaded() {
		return grid.getDataProvider() instanceof ListDataProvider
				&& ((ListDataProvider<ChannelAdapter>) grid.getDataProvider()).getItems().iterator().hasNext();
	}

	@Override
	public void lazyLoad() {
		if (!isLoaded()) {
			refresh();
		}
	}

	@Override
	public void refresh() {
		provider.addChannels(channels);
		provider.refresh();
		grid.setDataProvider(provider);
	}
}
