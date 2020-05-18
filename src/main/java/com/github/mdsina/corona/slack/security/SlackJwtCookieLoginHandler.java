package com.github.mdsina.corona.slack.security;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.security.authentication.UserDetails;
import io.micronaut.security.token.jwt.cookie.JwtCookieConfiguration;
import io.micronaut.security.token.jwt.cookie.JwtCookieLoginHandler;
import io.micronaut.security.token.jwt.generator.AccessRefreshTokenGenerator;
import io.micronaut.security.token.jwt.generator.JwtGeneratorConfiguration;
import java.net.URI;
import java.util.Optional;
import javax.inject.Singleton;

@Singleton
@Replaces(JwtCookieLoginHandler.class)
public class SlackJwtCookieLoginHandler extends JwtCookieLoginHandler {

    private final RedirectUrlExtractor redirectUrlExtractor;

    public SlackJwtCookieLoginHandler(
        JwtCookieConfiguration jwtCookieConfiguration,
        JwtGeneratorConfiguration jwtGeneratorConfiguration,
        AccessRefreshTokenGenerator accessRefreshTokenGenerator,
        RedirectUrlExtractor redirectUrlExtractor
    ) {
        super(jwtCookieConfiguration, jwtGeneratorConfiguration, accessRefreshTokenGenerator);
        this.redirectUrlExtractor = redirectUrlExtractor;
    }

    @Override
    public HttpResponse loginSuccess(UserDetails userDetails, HttpRequest<?> request) {
        Optional<Cookie> cookieOptional = accessTokenCookie(userDetails, request);
        if (cookieOptional.isEmpty()) {
            return HttpResponse.serverError();
        }
        try {
            URI location = request.getParameters().get("redirect_uri", String.class)
                .map(redirectUrlExtractor::extractUrl)
                .orElseGet(() -> URI.create(jwtCookieConfiguration.getLoginSuccessTargetUrl()));
            return HttpResponse.seeOther(location)
                .cookie(cookieOptional.get());
        } catch (Exception e) {
            return HttpResponse.serverError();
        }
    }
}
