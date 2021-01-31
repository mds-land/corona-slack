package com.github.mdsina.corona.discord;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties("discord")
@Getter
@Setter
public class DiscordProperties {

    private String publicKey;
}
