package com.famtree.famtree.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
public class EndpointConfig {
    @Bean
    public RequestMappingInfoHandlerMapping endpointHandlerMapping() {
        return new RequestMappingHandlerMapping();
    }
} 