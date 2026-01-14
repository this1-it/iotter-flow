package it.thisone.iotter.persistence.ifc;

import java.util.List;

import org.springframework.stereotype.Repository;

import it.thisone.iotter.enums.TracingAction;
import it.thisone.iotter.persistence.model.Tracing;
import it.thisone.iotter.persistence.model.TracingCriteria;
@Repository
public interface ITracingDao extends IBaseEntityDao<Tracing>{

	public void trace(TracingAction action, String username, String administrator, String network, String device, String message);

	public long count(TracingCriteria criteria);

	public List<Tracing> search(TracingCriteria criteria, int offset, int limit);

	public String getHostAddress();

}
