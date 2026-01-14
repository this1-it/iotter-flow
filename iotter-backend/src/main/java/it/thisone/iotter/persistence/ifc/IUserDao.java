package it.thisone.iotter.persistence.ifc;

import java.util.List;

import org.springframework.stereotype.Repository;

import it.thisone.iotter.enums.AccountStatus;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.persistence.model.UserCriteria;
@Repository
public interface IUserDao extends IBaseEntityDao<User> {
	
	public List<User> findUsers(String owner, String name, AccountStatus status);

	public List<User> findByRoleName(String roleName);

	public List<User> findByGroup(NetworkGroup group);

	public List<User> findByNetwork(Network network);

	public List<User> search(UserCriteria criteria, int offset, int limit);

	public User findByName(String userName);
}
