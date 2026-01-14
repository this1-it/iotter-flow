package it.thisone.iotter.persistence.dao;


import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import it.thisone.iotter.persistence.ifc.IUserTokenDao;
import it.thisone.iotter.persistence.model.UserToken;

@Repository
public class UserTokenDao extends BaseEntityDao<UserToken> implements IUserTokenDao {
    
	public UserTokenDao() {
        super();
        setClazz(UserToken.class);
    }

	@Override
	public UserToken findCurrentToken(String owner, String action) {
		UserToken token = null;
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<UserToken> cq = cb.createQuery(UserToken.class);
		Root<UserToken> root = cq.from(UserToken.class);
		Predicate predicate = cb.and(cb.equal(root.get("owner"), owner), cb.equal(root.get("action"), action));
		List<UserToken> tokens = find(cq.select(root).where(predicate));
		
		for (UserToken userToken : tokens) {
			if (userToken.isExpired()) {
				delete(userToken);
			}
			else {
				token = userToken;
			}
		}
		return token;
	}

	@Override
	public UserToken findByValue(String value) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<UserToken> c = cb.createQuery(UserToken.class);
		Root<UserToken> root = c.from(UserToken.class);
		Predicate predicate = cb.equal(root.get("token"), value);
		List<UserToken> result = find(c.select(root).where(predicate));
		if (!result.isEmpty()) {
			return result.get(0);
		}
		return null;
	}

	@Override
	public void deleteCurrentToken(String owner, String action) {
		UserToken token = findCurrentToken(owner, action);
		if (token != null) {
			delete(token);
		}
	}
	
	
}
