package it.thisone.iotter.persistence.dao;

import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import it.thisone.iotter.persistence.ifc.IIdentityProfileDao;
import it.thisone.iotter.persistence.model.IdentityProfile;
import it.thisone.iotter.persistence.model.User;

@Repository
public class IdentityProfileDao extends BaseEntityDao<IdentityProfile> implements IIdentityProfileDao {
    public IdentityProfileDao() {
        super();
        setClazz(IdentityProfile.class);
    }

    @Override
    public IdentityProfile findByUser(User user) {
        if (user == null || user.getId() == null) {
            return null;
        }
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<IdentityProfile> cq = cb.createQuery(IdentityProfile.class);
        Root<IdentityProfile> root = cq.from(IdentityProfile.class);
        Predicate predicate = cb.equal(root.get("user"), user);
        List<IdentityProfile> result = find(cq.select(root).where(predicate));
        return result.isEmpty() ? null : result.get(0);
    }
}
