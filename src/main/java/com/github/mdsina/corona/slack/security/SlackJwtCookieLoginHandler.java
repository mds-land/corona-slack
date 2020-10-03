package com.github.mdsina.corona.slack.security;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.security.authentication.UserDetails;
import io.micronaut.security.config.RedirectConfiguration;
import io.micronaut.security.errors.PriorToLoginPersistence;
import io.micronaut.security.token.jwt.cookie.JwtCookieConfiguration;
import io.micronaut.security.token.jwt.cookie.JwtCookieLoginHandler;
import io.micronaut.security.token.jwt.generator.AccessRefreshTokenGenerator;
import io.micronaut.security.token.jwt.generator.AccessTokenConfiguration;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import javax.inject.Singleton;

@Singleton
@Replaces(JwtCookieLoginHandler.class)
public class SlackJwtCookieLoginHandler extends JwtCookieLoginHandler {

    private final RedirectUrlExtractor redirectUrlExtractor;
    private final RedirectConfiguration redirectConfiguration;

    public SlackJwtCookieLoginHandler(
        RedirectConfiguration redirectConfiguration,
        JwtCookieConfiguration jwtCookieConfiguration,
        AccessTokenConfiguration accessTokenConfiguration,
        AccessRefreshTokenGenerator accessRefreshTokenGenerator,
        PriorToLoginPersistence priorToLoginPersistence,
        RedirectUrlExtractor redirectUrlExtractor
    ) {
        super(
            redirectConfiguration,
            jwtCookieConfiguration,
            accessTokenConfiguration,
            accessRefreshTokenGenerator,
            priorToLoginPersistence
        );
        this.redirectUrlExtractor = redirectUrlExtractor;
        this.redirectConfiguration = redirectConfiguration;
    }

    @Override
    public MutableHttpResponse<?> loginSuccess(UserDetails userDetails, HttpRequest<?> request) {
        List<Cookie> cookies = getCookies(userDetails, request);
        if (cookies.isEmpty()) {
            return HttpResponse.serverError();
        }
        try {
            URI location = request.getParameters().get("redirect_uri", String.class)
                .map(redirectUrlExtractor::extractUrl)
                .orElseGet(() -> URI.create(redirectConfiguration.getLoginSuccess()));

            MutableHttpResponse<Object> response = HttpResponse.seeOther(location);

            Cookie cookie;
            for (Iterator i = cookies.iterator(); i.hasNext(); response = response.cookie(cookie)) {
                cookie = (Cookie) i.next();
            }
            return response;
        } catch (Exception e) {
            return HttpResponse.serverError();
        }
    }
}
