package com.promisetracker.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "promisetracker.rate-limit")
public class RateLimitProperties {

    private int authRequestsPerMinute = 10;
}
