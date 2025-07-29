package com.example.nagoyameshi.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebServerConfig {

	@Bean
	public TomcatServletWebServerFactory servletContainer() {
	    TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
	    factory.addConnectorCustomizers(connector -> {
	        connector.setMaxPartCount(100); 
	    });
	    return factory;
	}

}