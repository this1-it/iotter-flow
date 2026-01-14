package it.thisone.iotter.persistence.ifc;



import org.springframework.stereotype.Repository;

import it.thisone.iotter.persistence.model.UserToken;

@Repository
public interface IUserTokenDao extends IBaseEntityDao<UserToken> {
	public UserToken findCurrentToken(String owner, String action);
	public void deleteCurrentToken(String owner, String action);
	

	public UserToken findByValue(String value);
	public void deleteByOwner(String username);
	
}
