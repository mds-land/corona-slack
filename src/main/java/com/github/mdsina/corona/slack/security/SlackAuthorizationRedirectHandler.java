package com.github.mdsina.corona.slack.security;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.security.oauth2.endpoint.authorization.request.DefaultAuthorizationRedirectHandler;
import java.net.URI;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.inject.Singleton;
import lombok.SneakyThrows;

@Replaces(DefaultAuthorizationRedirectHandler.class)
@Singleton
public class SlackAuthorizationRedirectHandler extends DefaultAuthorizationRedirectHandler {

    @SneakyThrows
    @Override
    protected String expandedUri(
        @Nonnull String baseUrl, @Nonnull Map<String, Object> queryParams
    ) {
        // bug in direct string parsing of `UriBuilder.of(String str)`
        UriBuilder builder = UriBuilder.of(new URI(baseUrl));
        for (String k : queryParams.keySet()) {
            Object val = queryParams.get(k);
            if (val != null) {
                builder.queryParam(k, val);
            }
        }
        return builder.toString();
    }
}
