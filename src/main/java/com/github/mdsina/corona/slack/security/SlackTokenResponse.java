package com.github.mdsina.corona.slack.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.security.oauth2.endpoint.token.response.TokenResponse;
import java.util.Map;
import lombok.Getter;

@Getter
public class SlackTokenResponse extends TokenResponse {

    private String teamId;

    @JsonProperty("team")
    private void unpackNameFromNestedObject(Map<String, String> team) {
        teamId = team.get("id");
    }

    @JsonProperty("authed_user")
    private void unpackNameFromNestedObject2(Map<String, String> user) {
        setAccessToken(user.get("access_token"));
        setScope(user.get("scope"));
        setTokenType(user.get("token_type"));
    }
}
