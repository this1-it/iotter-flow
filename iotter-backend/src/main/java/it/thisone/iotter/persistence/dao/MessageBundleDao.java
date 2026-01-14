package it.thisone.iotter.persistence.dao;


import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.persistence.ifc.IMessageBundleDao;
import it.thisone.iotter.persistence.model.MessageBundle;
import it.thisone.iotter.persistence.model.User;


@Repository
public class MessageBundleDao extends BaseEntityDao<MessageBundle> implements IMessageBundleDao {
    public MessageBundleDao() {
        super();
        setClazz(MessageBundle.class);
    }

	@Override
	public List<MessageBundle> findByTemplate(String template) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<MessageBundle> cq = cb.createQuery(MessageBundle.class);
		Root<MessageBundle> root = cq.from(MessageBundle.class);
		Predicate predicate = cb.like(cb.lower(root.<String> get("code")), template + "%");
		return find(cq.select(root).where(predicate));
	}

	@Override
	@Cacheable(value = Constants.Cache.MESSAGES, key = "{#code,#type,#language}", unless = "#result == null")
	public MessageBundle find(String code, String type, String language) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<MessageBundle> cq = cb.createQuery(MessageBundle.class);
		Root<MessageBundle> root = cq.from(MessageBundle.class);
		
		Predicate[] predicates = new Predicate[] {
				cb.equal(root.get("code"), code),
				cb.equal(root.get("type"), type),
				cb.equal(root.get("language"), language)
		};
		
		List<MessageBundle> result = find(cq.select(root).where(predicates));
		if (result.size() > 0) {
			return result.get(0);
		}
		return null;

	}
}
