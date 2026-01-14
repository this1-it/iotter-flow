package it.thisone.iotter.persistence.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.stereotype.Repository;

import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.ifc.IGroupWidgetDao;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;

@Repository
public class GroupWidgetDao extends BaseEntityDao<GroupWidget> implements
		IGroupWidgetDao {
	public GroupWidgetDao() {
		super();
		setClazz(GroupWidget.class);
	}

	@Override
	public GroupWidget findByName(String name, String owner) throws BackendServiceException {
		String fieldName = "name";
		String fieldOwner = "owner";
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<GroupWidget> cq = cb.createQuery(GroupWidget.class);
		Root<GroupWidget> root = cq.from(GroupWidget.class);
		Predicate predicate = cb.and(cb.equal(root.get(fieldName), name),
				cb.equal(root.get(fieldOwner), owner));
		List<GroupWidget> result = find(cq.select(root).where(predicate));
		if (result.size() == 1) {
			return result.get(0);
		}
		if (result.size() > 1) {
			throw new BackendServiceException(
					"more than one network with same name : " + name);
		}
		return null;
	}

	@Override
	public List<GroupWidget> findByNetwork(Network network) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<GroupWidget> cq = cb.createQuery(GroupWidget.class);
		Root<GroupWidget> root = cq.from(GroupWidget.class);
		Predicate predicate = cb.equal(
				root.get("group").get("network").get("id"), network.getId());
		return find(cq.select(root).where(predicate));
	}

	@Override
	public List<GroupWidget> findByNetworkGroup(NetworkGroup group) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<GroupWidget> cq = cb.createQuery(GroupWidget.class);
		Root<GroupWidget> root = cq.from(GroupWidget.class);
		Predicate predicate = cb.equal(root.get("group").get("id"),
				group.getId());
		return find(cq.select(root).where(predicate));
	}

	/*
	 * 
	 * SELECT e FROM Employee e WHERE e.department IN (SELECT DISTINCT d FROM
	 * Department d JOIN d.employees de JOIN de.project p WHERE p.name LIKE
	 * 'QA%')
	 * 
	 * CriteriaQuery<Employee> c = cb.createQuery(Employee.class);
	 * Root<Employee> emp = c.from(Employee.class);
	 * 
	 * Subquery<Department> sq = c.subquery(Department.class); Root<Department>
	 * dept = sq.from(Department.class); Join<Employee,Project> project =
	 * dept.join("employees").join("projects");
	 * sq.select(dept.<Integer>get("id")) .distinct(true)
	 * .where(cb.like(project.<String>get("name"), "QA%")); c.select(emp)
	 * .where(cb.in(emp.get("dept").get("id")).value(sq));
	 * 
	 * 
	 * SELECT gw FROM GroupWidget gw WHERE gw.group IN (SELECT d.groups FROM
	 * Device d WHERE d == dev)
	 * 
	 * CriteriaBuilder cb = entityManager.getCriteriaBuilder();
	 * CriteriaQuery<GroupWidget> cq = cb.createQuery(GroupWidget.class);
	 * Root<GroupWidget> gwRoot = cq.from(GroupWidget.class);
	 * 
	 * Subquery<NetworkGroup> sq = cq.subquery(NetworkGroup.class); Root<Device>
	 * dRoot = sq.from(Device.class);
	 * sq.select(dRoot.<NetworkGroup>get("groups")).where(cb.equal(dRoot,
	 * device)); cq.select(gwRoot).where(cb.in(gwRoot.get("group")).value(sq));
	 * 
	 * Predicate predicate = cb.in(gwRoot.get("group")).value(sq); return
	 * find(cq.select(gwRoot).where(predicate));
	 */

	/**
	 * find all group widgets related to a device
	 */
	@Override
	public List<GroupWidget> findByDevice(Device device) {

		List<GraphicFeed> feeds = findGraphFeedByDevice(device);
		List<GroupWidget> widgets = new ArrayList<GroupWidget>();

		for (GraphicFeed feed : feeds) {
			GroupWidget widget = feed.getWidget().getGroupWidget();
			if (!widgets.contains(widget)) {
				widgets.add(widget);
			}
		}
		
		List<GroupWidget> others = findByDeviceSerial(device.getSerial());
		for ( GroupWidget widget : others){
			if (!widgets.contains(widget)) {
				widgets.add(widget);
			}			
		}
		

		return widgets;
	}

	/**
	 * find all graph feeds which have channels belonging to a device
	 * 
	 * @param device
	 * @return
	 */
	@Override
	public List<GraphicFeed> findGraphFeedByDevice(Device device) {
		/*
		 * SELECT gf FROM GraphFeed gf WHERE gf.channel.id IN (SELECT c.id FROM
		 * Channel c. WHERE c.device == dev)
		 */

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<GraphicFeed> cq = cb.createQuery(GraphicFeed.class);
		Root<GraphicFeed> gfRoot = cq.from(GraphicFeed.class);
		Subquery<Channel> sq = cq.subquery(Channel.class);
		Root<Channel> device_root = sq.from(Channel.class);
		sq.select(device_root.<Channel> get("id")).where(
				cb.equal(device_root.get("device"), device));
		Predicate predicate = cb.in(gfRoot.get("channel").get("id")).value(sq);
		List<GraphicFeed> feeds = entityManager.createQuery(
				cq.select(gfRoot).where(predicate)).getResultList();
		return feeds;
	}

	/**
	 * find all graph feeds which have channels belonging to a device
	 * 
	 * @param device
	 * @return
	 */
	@Override
	public List<GraphicFeed> findGraphFeedByChannel(Channel chnl) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<GraphicFeed> cq = cb.createQuery(GraphicFeed.class);
		Root<GraphicFeed> gfRoot = cq.from(GraphicFeed.class);
		Predicate predicate = cb.equal(gfRoot.get("channel").get("id"), chnl.getId());
		List<GraphicFeed> feeds = entityManager.createQuery(
				cq.select(gfRoot).where(predicate)).getResultList();
		return feeds;
	}

	
	
	@Override
	public GroupWidget findByExternalId(String externalId, String serial) throws BackendServiceException {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<GroupWidget> c = cb.createQuery(GroupWidget.class);
		Root<GroupWidget> root = c.from(GroupWidget.class);
		Predicate predicate = cb.and(
				cb.equal(root.get("externalId"), externalId),
				cb.equal(root.get("device"), serial));

		List<GroupWidget> result = find(c.select(root).where(predicate));
		
		if (result.size() == 1) {
			return result.get(0);
		}
		
		if (result.size() > 1) {
			throw new BackendServiceException(
					"more than one entity with same externalId : " + serial);
		}
		return null;
	}

	@Override
	public List<GroupWidget> findExclusiveVisualizations(String serial) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<GroupWidget> c = cb.createQuery(GroupWidget.class);
		Root<GroupWidget> root = c.from(GroupWidget.class);
		Predicate predicate = cb.and(cb.equal(root.get("device"), serial), cb.isNotNull(root.get("externalId"))) ;
		List<GroupWidget> result = find(c.select(root).where(predicate));
		return result;
	}

	@Override
	public List<GroupWidget> findByCreator(String username) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<GroupWidget> c = cb.createQuery(GroupWidget.class);
		Root<GroupWidget> root = c.from(GroupWidget.class);
		Predicate predicate = cb.equal(root.get("creator"), username);
		return find(c.select(root).where(predicate));
	}

	
	@Override
	public List<GroupWidget> findByDeviceSerial(String serial) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<GroupWidget> c = cb.createQuery(GroupWidget.class);
		Root<GroupWidget> root = c.from(GroupWidget.class);
		Predicate predicate = cb.equal(root.get("device"), serial);
		return find(c.select(root).where(predicate));
	}
	
	
}
