package it.thisone.iotter.persistence.ifc;



import org.springframework.stereotype.Repository;

import it.thisone.iotter.persistence.model.Role;
@Repository
public interface IRoleDao extends IBaseEntityDao<Role> {

	Role findByName(String name);


}
