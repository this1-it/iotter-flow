package it.thisone.iotter.rest;

import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * https://github.com/cloudskol/restskol
 */
// @Provider
public class RestRequestListener implements RequestEventListener {
	private static Logger logger = LoggerFactory
			.getLogger("performance");
    private final long startTime;
 
    public RestRequestListener() {
        startTime = System.currentTimeMillis();
    }
 
    @Override
    public void onEvent(RequestEvent requestEvent) {
        String path = requestEvent.getUriInfo().getAbsolutePath().getPath();
       switch (requestEvent.getType()) {
            case RESOURCE_METHOD_START:
                logger.info("{} START", path);
                break; 
            case FINISHED:
                long endTime = System.currentTimeMillis();
                logger.info("{} END {} ms.", path, endTime - startTime);
                break;
		default:
			break;
        }
    }
}