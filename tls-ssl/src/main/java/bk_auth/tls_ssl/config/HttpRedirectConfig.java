package bk_auth.tls_ssl.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Abre un conector HTTP adicional. Spring Security (requiresSecure)
 * redirige cualquier petición recibida aquí hacia el puerto HTTPS.
 */
@Configuration
public class HttpRedirectConfig {

    @Value("${app.http-redirect-port:8080}")
    private int httpPort;

    @Value("${server.port:8443}")
    private int httpsPort;

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> httpConnectorCustomizer() {
        return factory -> {
            Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
            connector.setScheme("http");
            connector.setSecure(false);
            connector.setPort(httpPort);
            connector.setRedirectPort(httpsPort);
            factory.addAdditionalTomcatConnectors(connector);
        };
    }
}
