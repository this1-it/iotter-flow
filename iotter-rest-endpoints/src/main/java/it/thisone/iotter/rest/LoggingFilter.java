package it.thisone.iotter.rest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.server.ContainerException;
import org.glassfish.jersey.server.ContainerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


//@Provider
public class LoggingFilter implements ContainerRequestFilter {

	private static Logger logger = LoggerFactory
			.getLogger(LoggingFilter.class);
 
	@Override
	public void filter(ContainerRequestContext request) throws IOException {
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        InputStream in = request.getEntityStream();
//        final StringBuilder b = new StringBuilder();
//        
//        try {
//            if (in.available() > 0) {
//                ReaderWriter.writeTo(in, out);
//
//                byte[] requestEntity = out.toByteArray();
//            	printRequestLine(b, request);
//                printEntity(b, requestEntity);
//
//                request.setEntityStream(new ByteArrayInputStream(requestEntity));
//            }
//            return request;
//        } catch (IOException ex) {
//        	logger.error("", ex);
//            throw new ContainerException(ex);
//        }
		
	}

	


    private void printRequestLine(StringBuilder b, ContainerRequest request) {
        b.append("* ").append("Server in-bound request").append('\n');
        b.append("> ").append(request.getMethod()).append(" ").
                append(request.getRequestUri().toASCIIString()).append('\n');
    }
    
    private void printEntity(StringBuilder b, byte[] entity) throws IOException {
        if (entity.length == 0)
            return;
        for (int i = 0; i < entity.length; i++) {
        	char ch = (char) (entity[i] & 0xFF);
			if (Character.isISOControl(ch)) {
				b.append("not printable char at position " + i).append("\n");
			}
		}
        
        b.append(new String(entity)).append("\n");
        //System.out.println("#### Intercepted Entity ####");
        logger.info(b.toString());
    }

}