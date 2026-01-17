package it.thisone.iotter.config;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatJndiConfig {

    @Bean
    public TomcatServletWebServerFactory tomcatFactory() {
        return new TomcatServletWebServerFactory() {
            @Override
            protected TomcatWebServer getTomcatWebServer(Tomcat tomcat) {
                tomcat.enableNaming();
                return super.getTomcatWebServer(tomcat);
            }

            @Override
            protected void postProcessContext(Context context) {
                ContextResource resource = new ContextResource();
                resource.setName("jdbc/iotter2");
                resource.setType("javax.sql.DataSource");
                resource.setProperty("factory", "org.apache.tomcat.jdbc.pool.DataSourceFactory");
                resource.setProperty("driverClassName", "com.mysql.cj.jdbc.Driver");
                resource.setProperty("url", "jdbc:mysql://docker_mysql:3306/aernet?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
                resource.setProperty("username", "iotter");
                resource.setProperty("password", "iotter");
                resource.setProperty("maxActive", "20");
                resource.setProperty("maxIdle", "10");
                resource.setProperty("minIdle", "5");
                resource.setProperty("maxWait", "10000");

                context.getNamingResources().addResource(resource);
            }
        };
    }
}
