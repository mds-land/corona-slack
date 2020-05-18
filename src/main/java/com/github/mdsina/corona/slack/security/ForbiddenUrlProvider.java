package com.github.mdsina.corona.slack.security;

import io.micronaut.http.HttpRequest;
import io.micronaut.security.handlers.ForbiddenRejectionUriProvider;
import java.util.Optional;
import javax.inject.Singleton;

@Singleton
public class ForbiddenUrlProvider implements ForbiddenRejectionUriProvider {

    @Override
    public Optional<String> getForbiddenRedirectUri(HttpRequest<?> request) {
        return Optional.empty();
    }
}
