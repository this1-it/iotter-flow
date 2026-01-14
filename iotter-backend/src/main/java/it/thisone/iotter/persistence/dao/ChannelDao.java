package it.thisone.iotter.persistence.dao;


import org.springframework.stereotype.Repository;

import it.thisone.iotter.persistence.ifc.IChannelDao;
import it.thisone.iotter.persistence.model.Channel;


/**
 * 	Bug #161 [PERSISTENCE][REST] automatic visualization cannot be created at device configuration via rest service
 * @author tisone
 *
 */
@Repository
public class ChannelDao extends BaseEntityDao<Channel> implements IChannelDao {
    public ChannelDao() {
        super();
        setClazz(Channel.class);
    }
    
    
}
