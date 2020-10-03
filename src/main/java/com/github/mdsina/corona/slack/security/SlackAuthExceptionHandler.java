package com.github.mdsina.corona.slack.security;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.security.authentication.AuthorizationException;
import io.micronaut.security.authentication.DefaultAuthorizationExceptionHandler;
import javax.inject.Singleton;

@Singleton
@Replaces(DefaultAuthorizationExceptionHandler.class)
public class SlackAuthExceptionHandler extends DefaultAuthorizationExceptionHandler {

    @Override
    protected String getRedirectUri(HttpRequest<?> request, AuthorizationException exception) {
        if (exception.isForbidden()) {
            // based on default value from previous version:
            // https://github.com/micronaut-projects/micronaut-security/blob/1.4.x/security/src/main/java/io/micronaut/security/handlers/RedirectRejectionHandler.java#L93
            return "/";
        }

        return UriBuilder.of("/oauth/login")
            .queryParam("redirect_uri", request.getPath())
            .toString();
    }
}
