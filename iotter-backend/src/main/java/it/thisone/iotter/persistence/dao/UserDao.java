package it.thisone.iotter.persistence.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import it.thisone.iotter.enums.AccountStatus;
import it.thisone.iotter.persistence.ifc.IUserDao;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.persistence.model.Role;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.persistence.model.UserCriteria;

@Repository
public class UserDao extends BaseEntityDao<User> implements IUserDao {
	public UserDao() {
		super();
		setClazz(User.class);
	}

	@Override
	public User findByName(String userName) {
		if (userName == null) return null;
		if (userName.trim().isEmpty()) return null;
		String fieldName = "username";
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(User.class);
		Root<User> root = cq.from(User.class);
		Predicate predicate = cb.equal(root.get(fieldName), userName);
		List<User> result = find(cq.select(root).where(predicate));
		if (result.size() > 0) {
			return result.get(0);
		}
		return null;
	}

	@Override
	public List<User> findByNetwork(Network network) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(User.class);
		Root<User> root = cq.from(User.class);
		Join<User, NetworkGroup> group = root.join("groups", JoinType.LEFT);
		Predicate predicate = cb.equal(group.get("network"), network);
		// distinct groups
		cq.distinct(true);
		return find(cq.select(root).where(predicate));
	}

	@Override
	public List<User> findUsers(String owner, String name, AccountStatus status) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(User.class);
		Root<User> root = cq.from(User.class);
		UserCriteria criteria = new UserCriteria();
		criteria.setOwner(owner);
		criteria.setEmail(name);
		criteria.setLastName(name);
		criteria.setUsername(name);
		criteria.setStatus(status);
		Predicate[] predicate = buildCriteria(cb, root, criteria);
		cq.where(predicate);
		return find(cq.select(root).where(predicate));
	}

	@Override
	public List<User> search(UserCriteria criteria, int offset, int limit) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(User.class);
		Root<User> root = cq.from(User.class);
		Predicate[] predicates = buildCriteria(cb, root, criteria);
		return find( cq.select(root).where(predicates), offset, limit);
	}
	
	
	@Override
	public List<User> findByRoleName(String roleName) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(User.class);
		Root<User> root = cq.from(User.class);
		Join<User, Role> roles = root.join("roles", JoinType.LEFT);
		Predicate predicate = cb.equal(roles.get("name"), roleName);
		return find(cq.select(root).where(predicate));
	}

	@Override
	public List<User> findByGroup(NetworkGroup group) {
		if (group.isNew()) return new ArrayList<>();
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(User.class);
		Root<User> root = cq.from(User.class);
		Join<User, NetworkGroup> groups = root.join("groups", JoinType.LEFT);
		Predicate predicate = cb.equal(groups.get("id"), group.getId());
		return find(cq.select(root).where(predicate));
	}

	private Predicate[] buildCriteria(CriteriaBuilder cb,
			 Root<User> root, UserCriteria criteria) {
		List<Predicate> andPredicates = new ArrayList<Predicate>();
		List<Predicate> orPredicates = new ArrayList<Predicate>();

		if (criteria.getGroup() != null) {
			Join<User, NetworkGroup> groups = root.join("groups", JoinType.LEFT);
			andPredicates.add(cb.equal(groups.get("id"), criteria.getGroup()));
		}
		
		if (criteria.getRole() != null) {
			Join<User, Role> roles = root.join("roles", JoinType.LEFT);
			andPredicates.add(cb.equal(roles.get("name"), criteria.getRole()));
		}		
		
		if (criteria.getOwner() != null) {
			andPredicates.add(cb.equal(root.get("owner"), criteria.getOwner()));
		}
		if (criteria.getEmail() != null) {
			orPredicates.add(cb.like(cb.lower(root.<String> get("email")),
					criteria.getEmail().toLowerCase() + "%"));
		}
		if (criteria.getLastName() != null) {
			orPredicates.add(cb.like(cb.lower(root.<String> get("lastName")),
					criteria.getLastName().toLowerCase() + "%"));
		}
		if (criteria.getUsername() != null) {
			orPredicates.add(cb.like(cb.lower(root.<String> get("username")),
					criteria.getUsername().toLowerCase() + "%"));
		}
		if (criteria.getStatus() != null) {
			orPredicates.add(cb.equal(root.get("accountStatus"), criteria.getStatus()));
		}

		return buildCriteriaPredicates(cb,  andPredicates, orPredicates);

	}

}
