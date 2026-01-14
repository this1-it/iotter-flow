package it.thisone.iotter.persistence.service;

import java.awt.font.ImageGraphicAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.RollbackException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.ChartScaleType;
import it.thisone.iotter.enums.GraphicWidgetType;
import it.thisone.iotter.enums.NetworkGroupType;
import it.thisone.iotter.enums.Protocol;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.ifc.IDeviceDao;
import it.thisone.iotter.persistence.ifc.IGroupWidgetDao;
import it.thisone.iotter.persistence.ifc.INetworkGroupDao;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.ChannelComparator;
import it.thisone.iotter.persistence.model.ChannelNumberComparator;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.persistence.model.User;

@Service
public class GroupWidgetService {
	public static Logger logger = LoggerFactory.getLogger(DeviceService.class);


	
	@Autowired
	private INetworkGroupDao groupDao;

	@Autowired
	private IGroupWidgetDao groupWidgetDao;

	@Autowired
	private IDeviceDao deviceDao;

	@Autowired
	private INetworkGroupDao networkGroupDao;

	public GroupWidgetService() {
		super();
	}

	// API
	@Transactional
	public void create(GroupWidget entity) {
		groupWidgetDao.create(entity);
	}

	@Transactional //(propagation = Propagation.REQUIRES_NEW)
	public void update(GroupWidget entity)  throws BackendServiceException {
		try {
			groupWidgetDao.update(entity);
			NetworkGroup group = entity.getGroup();
			if (group != null && group.isExclusive()) {
				group.setName(entity.getName());
				groupDao.update(group);
			}
		} catch (RollbackException e) {
        	throw new BackendServiceException(e);
		}
	}

	public GroupWidget findOne(String id) {
		return groupWidgetDao.findOne(id);
	}

	public List<GroupWidget> findAll() {
		return groupWidgetDao.findAll();
	}

	public List<GroupWidget> findByOwner(String owner) {
		return groupWidgetDao.findByOwner(owner);
	}

	public List<GroupWidget> findByCreator(String username) {
		return groupWidgetDao.findByCreator(username);
	}
	
	@Transactional
	public void deleteById(String entityId) {
		groupWidgetDao.deleteById(entityId);
	}

	public List<GroupWidget> findByNetworkGroup(NetworkGroup group) {
		return groupWidgetDao.findByNetworkGroup(group);
	}

	/**
	 * Automatic visualizations for a device has external id equal to serial
	 * 
	 * @param externalId
	 * @param serial
	 * @return
	 */
	public GroupWidget findByExternalId(String externalId, String serial) {
		try {
			return groupWidgetDao.findByExternalId(externalId, serial);
		} catch (BackendServiceException e) {
			logger.error("BackendServiceException", e);
		}
		return null;
	}

	@Deprecated
	protected void manageVisualizationFromDevice(Device device) {
		if (!device.isAvailableForVisualization()) {
			return;
		}

		if (device.getNetwork() != null) {
			// create device exclusive group
			getExclusiveGroup(device);
		}

		// eventually associate automatic visualization to exclusive group
		manageDefaultVisualization(device.getSerial(), null, null);

		if (device.getNetwork() != null) {
			// check association between device and exclusive group
			manageDeviceExclusiveGroup(device.getSerial());
		}

	}

	/**
	 * creates an automatic visualization for a device if not exists it contains
	 * a graph related to first active parameter of device
	 * 
	 * @param externalId
	 *            , table id or serial for default visualization
	 * @param serial
	 *            device id
	 * @param params
	 *            active parameter ids
	 * @return
	 */

	@Deprecated
	@Transactional
	public GroupWidget manageDefaultVisualization(String externalId, String serial, List<String> params) {

		Device device = deviceDao.findBySerial(serial);
		
		if (device == null) {
			return null;
		}

		if (externalId == null) {
			externalId = serial;
		}
		// manage default visualization with all parameter
		if (params == null || params.isEmpty()) {
			params = new ArrayList<String>();
			for (Channel chnl : device.getChannels()) {
				params.add(chnl.getNumber());
			}
		}

		List<Channel> activeChannels = new ArrayList<Channel>();
		for (Channel chnl : device.getChannels()) {
			if (chnl.getConfiguration().isActive() && params.contains(chnl.getNumber())) {
				activeChannels.add(chnl);
			}
		}

		if (activeChannels.isEmpty()) {
			return null;
		}

		if (device.getModel() != null && device.getModel().getProtocol().equals(Protocol.FTP)) {
			Collections.sort(activeChannels, new ChannelNumberComparator());
		} else {
			Collections.sort(activeChannels, new ChannelComparator());
		}

		GroupWidget entity = null;
		try {
			entity = groupWidgetDao.findByExternalId(externalId, serial);
		} catch (BackendServiceException e) {
			logger.error("BackendServiceException", e);
		}
		boolean defaultVisualization = externalId.equals(serial);
		NetworkGroup exclusiveGroup = null;
		if (device.getNetwork() != null) {
			exclusiveGroup = getExclusiveGroup(device);
		}

		if (entity != null) {
			entity.setOwner(device.getOwner());

			if (exclusiveGroup != null) {
				if (entity.getGroup() == null) {
					entity.setGroup(exclusiveGroup);
				}
			}

			// default visualization can be updated from web app
			if (defaultVisualization) {
				groupWidgetDao.update(entity);
				return entity;
			} else {
				// Feature #212 D32 ftp imported device creates/update different
				// default visualizations
				return updateExclusiveVisualization(entity, activeChannels);
			}
		}

		// create group widget

		Map<Channel, List<GraphicWidget>> channels = new HashMap<Channel, List<GraphicWidget>>();
		if (defaultVisualization) {
			channels.put(activeChannels.get(0), new ArrayList<GraphicWidget>());
		} else {
			for (Channel chnl : activeChannels) {
				channels.put(chnl, new ArrayList<GraphicWidget>());
			}
		}

		entity = new GroupWidget();

		if (exclusiveGroup != null) {
			entity.setGroup(exclusiveGroup);
		}

		if (defaultVisualization) {
			entity.setName(serial);
		} else {
			entity.setName(String.format("%s T%s", serial, externalId));
		}

		entity.setCreator(null);
		entity.setOwner(device.getOwner());
		// very important
		entity.setExternalId(externalId);
		entity.setDevice(serial);
		createOrUpdateGraphWidgets(entity, channels);
		groupWidgetDao.create(entity);
		return entity;
	}

	@Transactional
	@Deprecated
	private void manageDeviceExclusiveGroup(String serial) {
		Device device = deviceDao.findBySerial(serial);
		if (device != null && device.getNetwork() != null) {
			NetworkGroup group = getExclusiveGroup(device);
			if (!device.getGroups().contains(group)) {
				device.addGroup(group);
				deviceDao.update(device);
			}
		}
	}

	private NetworkGroup getExclusiveGroup(Device device) {
		if (device.getNetwork() == null) {
			return null;
		}
		try {
			NetworkGroup group = networkGroupDao.findByExternalId(device.getSerial(), device.getNetwork());
			if (group == null) {
				group = new NetworkGroup();
				group.setName(device.getSerial());
				group.setExternalId(device.getSerial());
				group.setExclusive(true);
				group.setNetwork(device.getNetwork());
				group.setGroupType(NetworkGroupType.DEVICE);
				networkGroupDao.create(group);
				group = networkGroupDao.merge(group);
			}
			return group;
		} catch (BackendServiceException e) {
			logger.error("BackendServiceException",e);
		}
		return null;

	}

	@Transactional
	public NetworkGroup createGroup(GroupWidget widget, List<User> users, List<Device> devices) {
		NetworkGroup group = widget.getGroup();
		if (group != null && group.isNew()) {
			networkGroupDao.create(group);
			group = networkGroupDao.merge(group);
			networkGroupDao.addMembers(group, users, devices);
		}
		return group;
	}

	@Transactional
	public NetworkGroup getExclusiveGroup(GroupWidget widget, Network network) {
		if (network == null) {
			return null;
		}
		try {
			NetworkGroup group = networkGroupDao.findByExternalId(widget.getExternalId(), network);
			if (group == null) {
				group = new NetworkGroup();
				group.setName(widget.getExternalId());
				group.setExternalId(widget.getExternalId());
				group.setExclusive(true);
				group.setGroupType(NetworkGroupType.GROUP_WIDGET);
				group.setNetwork(network);
				networkGroupDao.create(group);
				group = networkGroupDao.merge(group);
			}
			return group;
		} catch (BackendServiceException e) {
			logger.error("",e);
		}
		return null;
	}

	/**
	 * update an automatic visualization
	 * 
	 * @param entityId
	 * @param selectedChannels
	 * @return
	 */
	@Transactional
	public void updateExclusiveDeviceVisualization(String entityId, List<Channel> selectedChannels) {
		// Refresh GroupWidget entity
		GroupWidget entity = groupWidgetDao.findOne(entityId);
		if (entity == null) {
			return;
		}
		updateExclusiveVisualization(entity, selectedChannels);
	}

	private GroupWidget updateExclusiveVisualization(GroupWidget entity, List<Channel> selectedChannels) {
		// Prepare list of GraphWidget to remove
		List<GraphicWidget> removingWidgets = new ArrayList<GraphicWidget>();
		for (GraphicWidget graph : entity.getWidgets()) {
			for (GraphicFeed feed : graph.getFeeds()) {
				Channel channel = feed.getChannel();
				if (channel != null && !selectedChannels.contains(channel)) {
					removingWidgets.add(graph);
				}
			}
		}

		// Remove un-selected graphwidget
		for (GraphicWidget g : removingWidgets) {
			entity.getWidgets().remove(g);
		}

		// create map for current channels
		Map<Channel, List<GraphicWidget>> existingChannels = new HashMap<Channel, List<GraphicWidget>>();
		for (GraphicWidget graph : entity.getWidgets()) {
			GraphicFeed feed = null;
			if (!graph.getFeeds().isEmpty())
				feed = graph.getFeeds().get(0);
			if (feed != null) {
				Channel channel = feed.getChannel();
				if (!existingChannels.containsKey(channel)) {
					existingChannels.put(channel, new ArrayList<GraphicWidget>());
				}
				existingChannels.get(channel).add(graph);
			}
		}

		// ... extends map with new channel with graphwidget null for further
		// creation
		for (Channel channel : selectedChannels) {
			if (!existingChannels.containsKey(channel)) {
				existingChannels.put(channel, new ArrayList<GraphicWidget>());
			}
		}

		createOrUpdateGraphWidgets(entity, existingChannels);

		entity.setCreator(null);
		groupWidgetDao.update(entity);

		return entity;
	}

	private GraphicWidget createGraphWidgetFromChannel(Channel channel, GraphicWidgetType type) {
		GraphicWidget widget = new GraphicWidget();
		String label = String.format("%s [%d]", channel.toString(), channel.getConfiguration().getQualifier());
		widget.setLabel(label);
		widget.setType(type);

		widget.getOptions().setScale(ChartScaleType.LINEAR);
		widget.getOptions().setShowGrid(true);
		widget.setOwner(channel.getDevice().getOwner());

		GraphicFeed feed = new GraphicFeed();
		feed.setChannel(channel);
		feed.setWidget(widget);
		feed.setMeasure(channel.getDefaultMeasure());
		widget.addFeed(feed);

		return widget;
	}

	private void createOrUpdateGraphWidgets(GroupWidget entity, Map<Channel, List<GraphicWidget>> channels) {
		Device device = null;
		// Eventually add new graph
		for (Channel channel : channels.keySet()) {
			device = channel.getDevice();
			List<GraphicWidget> widgets = channels.get(channel);
			// Check if graph has been already created
			if (widgets.isEmpty()) {
				GraphicWidget widget = createGraphWidgetFromChannel(channel, GraphicWidgetType.MULTI_TRACE);
				entity.addGraphWidget(widget);
				// Fill new graphwidget to current channel
				channels.get(channel).add(widget);
				if (channel.getConfiguration().getSensor() == Constants.RAIN_GAUGE_SENSORS) {
					GraphicWidget histo = createGraphWidgetFromChannel(channel, GraphicWidgetType.HISTOGRAM);
					entity.addGraphWidget(histo);
					// Fill new graphwidget to current channel
					channels.get(channel).add(histo);
				}
			}
		}

		// Adapt graphs dimension & position
		int counter = 0;
		float aspectRatio = 4 / 3f;
		int canvasHeight = (int) (1600 / aspectRatio);
		int canvasWidth = 1600;
		int margin = 1;
		int heigth = 550;
		int width = canvasWidth;

		List<Channel> sortedChannels = new ArrayList<Channel>(channels.keySet());
		if (device != null && device.getModel() != null && device.getModel().getProtocol().equals(Protocol.FTP)) {
			Collections.sort(sortedChannels, new ChannelNumberComparator());
		} else {
			Collections.sort(sortedChannels, new ChannelComparator());
		}

		for (Channel channel : sortedChannels) {
			List<GraphicWidget> widgets = channels.get(channel);
			for (GraphicWidget widget : widgets) {
				setGraphWidgetPositionsAndColor(widget, counter, canvasWidth, canvasHeight, width, heigth, margin);
				counter++;
			}
		}
	}

	private void setGraphWidgetPositionsAndColor(GraphicWidget widget, int counter, int canvasWidth, int canvasHeight,
			int width, int heigth, int margin) {

		widget.setX(margin / (float) canvasWidth);
		widget.setY((margin + counter * (heigth + margin)) / (float) canvasHeight);

		widget.setWidth(width / (float) canvasWidth);
		widget.setHeight(heigth / (float) canvasHeight);

		// feature all automatic visualization in blue
		if (!widget.getFeeds().isEmpty()) {
			widget.getFeeds().get(0).getOptions().setFillColor("#0000FF");

			widget.getFeeds().get(0).getOptions().setFillColor("#3995bf");

		}
	}

	/**
	 * 
	 * @param serial
	 * @return automatic visualizations created from device
	 */
	public List<GroupWidget> findExclusiveVisualizations(String serial) {
		return groupWidgetDao.findExclusiveVisualizations(serial);
	}

	@Deprecated
	public List<String> findExclusiveVisualizationParams(String serial) {
		List<String> params = new ArrayList<>();
		try {
			GroupWidget entity = groupWidgetDao.findByExternalId(serial, serial);
			if (entity != null) {
				for (GraphicWidget widget : entity.getWidgets()) {
					for (GraphicFeed feed : widget.getFeeds()) {
						params.add(feed.getChannel().getNumber());
					}
				}
			}
		} catch (BackendServiceException e) {
		}
		return params;
	}

	@Transactional
	public void delete(GroupWidget widget) {
//		if (widget.getGroup() != null && widget.getGroup().isExclusive()) {
//			try {
//				groupDao.removeMembers(widget.getGroup());
//				groupDao.deleteById(widget.getGroup().getId());
//			} catch (Throwable e) {
//			}
//		}
		groupWidgetDao.delete(widget);
	}

	public boolean hasVisualizations(Device entity) {
		return !groupWidgetDao.findGraphFeedByDevice(entity).isEmpty();
	}

	public List<GroupWidget> findByNetwork(Network network) {
		return groupWidgetDao.findByNetwork(network);
	}

	public Collection<GroupWidget> findByDevice(Device entity) {
		Set<GroupWidget> widgets = new HashSet<GroupWidget>();
		if (entity == null)
			return widgets;
		// finds all graph feeds which have channels belonging to a device
		List<GraphicFeed> feeds = groupWidgetDao.findGraphFeedByDevice(entity);
		for (GraphicFeed feed : feeds) {
			GroupWidget widget = feed.getWidget().getGroupWidget();
			if (widget != null) {
				widgets.add(widget);
			}
		}
		widgets.addAll(groupWidgetDao.findExclusiveVisualizations(entity.getSerial()));
		return widgets;
	}

	@Transactional
	public List<GraphicFeed> updateWithMaterializeFeeds(GroupWidget entity) {
		List<GraphicFeed> feeds = new ArrayList<>();
		Device device = deviceDao.findBySerial(entity.getDevice());
		
		Map<String, Channel> map = new HashMap<String, Channel>();
		for (Channel chnl : device.getChannels()) {
			if (chnl.getMetaIdentifier() != null) {
				map.put(chnl.getMetaIdentifier(), chnl);
			}
		}
		
		for (GraphicWidget widget : entity.getWidgets()) {
			//logger.error("GroupWidget {} size {}", entity.getName(), widget.getFeeds().size() );
			for (GraphicFeed feed : widget.getFeeds()) {
				if (feed.getChannel() != null && feed.getChannel().getMetaIdentifier() == null) {
					feed.setChannel(null);
				}
				if (feed.getChannel() == null) {
					Channel chnl = map.get(feed.getMetaIdentifier());
					if (chnl != null) {
						feed.setChannel(chnl);
						feed.setMeasure(chnl.getDefaultMeasure());
						feeds.add(feed);
					}
				}
				else {
					feeds.add(feed);
				}
			}
		}
		entity.setOwner(device.getOwner());
		groupWidgetDao.update(entity);
		return feeds;
	}
	
	
	@Transactional
	public void applyRefresh(GraphicWidget gw, List<String> checked) {
		String id = gw.getGroupWidget().getId();
		GroupWidget entity = groupWidgetDao.findOne(id);
		if (entity != null) {
			for (GraphicWidget widget : entity.getWidgets()) {
				if (widget.equals(gw)) {
					for (GraphicFeed feed : widget.getFeeds()) {
						if (feed.getChannel() != null) {
							feed.setChecked(checked.contains(feed.getChannel().getKey()));
						}
					}					
				}

			}
			groupWidgetDao.update(entity);
		}
	}


}
