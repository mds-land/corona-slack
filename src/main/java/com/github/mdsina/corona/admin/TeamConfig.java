package com.github.mdsina.corona.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamConfig {

    private final String team;
    private final String token;
    private final boolean enabled;
    @Builder.Default
    private final ConfigParameters config = new ConfigParameters();
}
