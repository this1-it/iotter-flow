package it.thisone.iotter.config;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestJerseyConfig extends ResourceConfig {

    public RestJerseyConfig() {
        packages("it.thisone.iotter.rest");
    }
}
