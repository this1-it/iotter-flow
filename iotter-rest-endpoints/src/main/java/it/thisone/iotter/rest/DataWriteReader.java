package it.thisone.iotter.rest;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.thisone.iotter.rest.model.DataWrite;

/**
 * http://www.journaldev.com/2324/jackson-json-java-parser-api-example-tutorial
 */
//@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class DataWriteReader implements MessageBodyReader<DataWrite> {

	private static Logger flogger = LoggerFactory
			.getLogger("performance");

	@Autowired
	
	private ObjectMapper mapper;

    @Override
    public boolean isReadable(Class<?> clazz, Type type, Annotation[] antns, MediaType mt) {
        return DataWrite.class.isAssignableFrom(clazz);
    }

    @Override
    public DataWrite readFrom(Class<DataWrite> type,
            Type type1,
            Annotation[] antns,
            MediaType mt, MultivaluedMap<String, String> mm,
            InputStream in) throws IOException, WebApplicationException {
    	
		long startTime = System.currentTimeMillis();
		
		DataWrite mo = mapper.readValue(in, DataWrite.class);
        

		long endTime = System.currentTimeMillis() - startTime;
		flogger.info("read DataWrite {} ms.", endTime);
        return mo;
    }
}
