package it.thisone.iotter.rest;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

//import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * added com.sun.jersey.api.json.POJOMappingFeature to web.xml
 * @author bedinsky
 *
 */

@Provider
@Component
@Produces(MediaType.APPLICATION_JSON)
public class ObjectMapperResolver implements ContextResolver<ObjectMapper> {

	@Autowired
	
    private ObjectMapper mapper;

    public ObjectMapperResolver() {
    	super();
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }

}