package com.github.mdsina.corona.slack.security;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.uri.UriBuilder;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Singleton;

@Singleton
public class RedirectUrlExtractor {

    private final String baseUrl;

    public RedirectUrlExtractor(@Value("${corona.base.url:}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public URI extractUrl(String redirectUrl) {
        URI baseUri = URI.create(baseUrl);

        String baseUriPath = baseUri.getPath();
        if (baseUriPath.lastIndexOf("/") == baseUriPath.length() - 1) {
            baseUriPath = baseUriPath.substring(0, baseUriPath.length() - 1);
        }
        URI trimmedRedirectUri = URI.create(redirectUrl.replaceFirst(baseUriPath, ""));

        UriBuilder uriBuilder = UriBuilder.of(trimmedRedirectUri.getRawPath());

        if (trimmedRedirectUri.getQuery() != null) {
            String redirectUriQuery = trimmedRedirectUri.getQuery();

            Map<String, String> queryPairs = new LinkedHashMap<>();
            String[] pairs = redirectUriQuery.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                queryPairs.put(
                    URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8),
                    URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8)
                );
            }

            for (Entry<String, String> entry : queryPairs.entrySet()) {
                uriBuilder = uriBuilder.queryParam(entry.getKey(), entry.getValue());
            }
        }

        return URI.create(uriBuilder.build().toString());
    }
}
