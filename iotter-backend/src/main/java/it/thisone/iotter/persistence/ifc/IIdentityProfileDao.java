package it.thisone.iotter.persistence.ifc;

import org.springframework.stereotype.Repository;

import it.thisone.iotter.persistence.model.IdentityProfile;
import it.thisone.iotter.persistence.model.User;

@Repository
public interface IIdentityProfileDao extends IBaseEntityDao<IdentityProfile> {
    IdentityProfile findByUser(User user);
}
