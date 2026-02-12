package it.thisone.iotter.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.annotation.PostConstruct;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.io.output.WriterOutputStream;
//import org.codehaus.jackson.map.AnnotationIntrospector;
//import org.codehaus.jackson.map.ObjectMapper;
//import org.codehaus.jackson.map.SerializationConfig.Feature;
//import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
//import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.InputDecorator;
import com.fasterxml.jackson.core.io.OutputDecorator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

import it.thisone.iotter.rest.model.DataPoint;

import com.fasterxml.jackson.core.type.TypeReference;
/*
 * 
 * https://metabroadcast.com/blog/using-jackson-2-with-jersey
 */

@Configuration
@ComponentScan({ "it.thisone.iotter.rest" })
public class JerseyConfig {

	private final Logger logger = LoggerFactory.getLogger(JerseyConfig.class);

	@PostConstruct
	public void init() {
		logger.info("JerseyConfig initialized.");
	}

	// @Bean(name = "restMapper")
	// public ObjectMapper restMapper() {
	// 	ObjectMapper mapper = new ObjectMapper();
	// 	mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
	// 	mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	// 	mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
	// 	// ignore null fields globally
	// 	mapper.setSerializationInclusion(Include.NON_NULL);
	// 	mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
	// 	SimpleModule simpleModule = new SimpleModule("BooleanAsString", new Version(1, 0, 0, null, null, null));
	// 	simpleModule.addSerializer(Boolean.class, new BooleanSerializer());
	// 	simpleModule.addSerializer(boolean.class, new BooleanSerializer());
	// 	mapper.registerModule(simpleModule);
	// 	return mapper;
	// }

	@Bean
	public Client jerseyClient() {
		return ClientBuilder.newClient();
	}

	// @Bean(name = "cborMapper")
	// public ObjectMapper cborMapper() {
	// 	CBORFactory f = new CBORFactory();
	// 	ObjectMapper mapper = new ObjectMapper(f);
	// 	mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
	// 	mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	// 	mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
	// 	// ignore null fields globally
	// 	mapper.setSerializationInclusion(Include.NON_NULL);
	// 	mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
	// 	return mapper;
	// }

	// @Bean(name = "gzipMapper")
	// public ObjectMapper gzipMapper() {

	// 	ObjectMapper mapper = new ObjectMapper(new JsonFactory().setInputDecorator(new InputDecorator() {
	// 		/**
	// 		 * 
	// 		 */
	// 		private static final long serialVersionUID = 1L;

	// 		@Override
	// 		public InputStream decorate(IOContext context, InputStream inputStream) throws IOException {
	// 			return new GZIPInputStream(inputStream);
	// 		}

	// 		@Override
	// 		public InputStream decorate(IOContext context, byte[] bytes, int offset, int length) throws IOException {
	// 			return new GZIPInputStream(new ByteArrayInputStream(bytes, offset, length));
	// 		}

	// 		@Override
	// 		public Reader decorate(IOContext context, Reader reader) throws IOException {
	// 			return new InputStreamReader(new GZIPInputStream(new ReaderInputStream(reader)),
	// 					StandardCharsets.UTF_8);
	// 		}
	// 	}).setOutputDecorator(new OutputDecorator() {
	// 		/**
	// 		 * 
	// 		 */
	// 		private static final long serialVersionUID = 1L;

	// 		@Override
	// 		public OutputStream decorate(IOContext context, OutputStream outputStream) throws IOException {
	// 			return new GZIPOutputStream(outputStream);
	// 		}

	// 		@Override
	// 		public Writer decorate(IOContext context, Writer writer) throws IOException {
	// 			return new OutputStreamWriter(
	// 					new GZIPOutputStream(new WriterOutputStream(writer, StandardCharsets.UTF_8)));
	// 		}
	// 	}));

	// 	mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	// 	mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	// 	mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
	// 	// ignore null fields globally
	// 	mapper.setSerializationInclusion(Include.NON_NULL);
	// 	mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
	// 	SimpleModule simpleModule = new SimpleModule("BooleanAsString", new Version(1, 0, 0, null, null, null));
	// 	simpleModule.addSerializer(Boolean.class, new BooleanSerializer());
	// 	simpleModule.addSerializer(boolean.class, new BooleanSerializer());
	// 	mapper.registerModule(simpleModule);
	// 	return mapper;
	// }

	// @Bean(name = "dataPointListObjectReader")
	// public ObjectReader dataPointListObjectReader() {
	// 	return cborMapper().readerFor(new TypeReference<List<DataPoint>>() {
	// 	});
	// }

}
