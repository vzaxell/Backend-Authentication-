package bk_auth.tls_ssl.config;

import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpRedirectConfig {

    @Value("${app.http-redirect-port:8080}")
    private int httpPort;

    @Value("${server.port:8443}")
    private int httpsPort;

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> httpConnectorCustomizer() {
        return factory -> {
            Connector connector = new Connector();
            connector.setScheme("http");
            connector.setSecure(false);
            connector.setPort(httpPort);
            connector.setRedirectPort(httpsPort);
            factory.addAdditionalConnectors(connector);

            factory.addContextCustomizers(context -> {
                SecurityConstraint constraint = new SecurityConstraint();
                constraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                constraint.addCollection(collection);
                context.addConstraint(constraint);
            });
        };
    }
}