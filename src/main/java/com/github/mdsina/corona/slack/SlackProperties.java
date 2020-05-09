package com.github.mdsina.corona.slack;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties("slack")
@Getter
@Setter
public class SlackProperties {

    private String channel;
    private String signingSecret;
    private String token;
    private String clientId;
    private String clientSecret;
}
