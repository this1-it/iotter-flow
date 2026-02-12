package it.thisone.iotter.config;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

class BooleanSerializer extends JsonSerializer<Boolean> {

	@Override
	public void serialize(Boolean value, com.fasterxml.jackson.core.JsonGenerator gen, SerializerProvider serializers)
			throws IOException, JsonProcessingException {
		gen.writeString(value.toString());
	}

}