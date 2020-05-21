package com.github.mdsina.corona.slack.security;

import io.micronaut.security.authentication.UserDetails;
import io.micronaut.security.oauth2.endpoint.token.response.OauthUserDetailsMapper;
import io.micronaut.security.oauth2.endpoint.token.response.TokenResponse;
import java.util.List;
import java.util.Map;
import javax.inject.Named;
import javax.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@Named("slack")
@Singleton
public class SlackUserDetailsMapper implements OauthUserDetailsMapper {

    @Override
    public Publisher<UserDetails> createUserDetails(TokenResponse tokenResponse) {
        var token = (SlackTokenResponse) tokenResponse;

        return Mono.just(new UserDetails(token.getAccessToken(), List.of("USER"), Map.of("team_id", token.getTeamId())));
    }

}
