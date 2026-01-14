package it.thisone.iotter.persistence.ifc;

import java.util.List;

import org.springframework.stereotype.Repository;

import it.thisone.iotter.persistence.model.MessageBundle;

@Repository
public interface IMessageBundleDao extends IBaseEntityDao<MessageBundle> {

	List<MessageBundle> findByTemplate(String template);

	MessageBundle find(String code, String type, String language);

}
