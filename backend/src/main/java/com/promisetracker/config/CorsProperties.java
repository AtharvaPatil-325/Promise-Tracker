package com.promisetracker.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "promisetracker.cors")
public class CorsProperties {

    private List<String> allowedOrigins;
}
