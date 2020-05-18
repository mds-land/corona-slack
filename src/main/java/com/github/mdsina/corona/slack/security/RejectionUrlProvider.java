package com.github.mdsina.corona.slack.security;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.security.handlers.UnauthorizedRejectionUriProvider;
import java.util.Optional;
import javax.inject.Named;
import javax.inject.Singleton;

@Named("slack")
@Singleton
public class RejectionUrlProvider implements UnauthorizedRejectionUriProvider {

    @Override
    public Optional<String> getUnauthorizedRedirectUri(HttpRequest<?> request) {
        var uri = UriBuilder.of("/oauth/login")
            .queryParam("redirect_uri", request.getPath())
            .toString();
        return Optional.of(uri);
    }
}
