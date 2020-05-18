package com.github.mdsina.corona.slack.security;


import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.http.uri.UriTemplate;
import io.micronaut.security.oauth2.configuration.OauthConfigurationProperties;
import io.micronaut.security.oauth2.url.DefaultOauthRouteUrlBuilder;
import io.micronaut.security.oauth2.url.OauthRouteUrlBuilder;
import io.micronaut.web.router.exceptions.RoutingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.inject.Singleton;

@Replaces(DefaultOauthRouteUrlBuilder.class)
@Singleton
public class SlackOauthRouteUrlBuilder implements OauthRouteUrlBuilder {

    private final String loginUriTemplate;
    private final String callbackUriTemplate;
    private final String baseUrl;
    private final RedirectUrlExtractor redirectUrlExtractor;

    /**
     * @param oauthConfigurationProperties The oauth configuration
     * @param redirectUrlExtractor
     */
    public SlackOauthRouteUrlBuilder(
        OauthConfigurationProperties oauthConfigurationProperties,
        RedirectUrlExtractor redirectUrlExtractor,
        @Value("${corona.base.url:}") String baseUrl
    ) {
        this.loginUriTemplate = oauthConfigurationProperties.getLoginUri();
        this.callbackUriTemplate = oauthConfigurationProperties.getCallbackUri();
        this.redirectUrlExtractor = redirectUrlExtractor;
        this.baseUrl = baseUrl;
    }

    @Override
    public URL buildLoginUrl(@Nullable HttpRequest originating, String providerName) {
        return build(originating, providerName, loginUriTemplate);
    }

    @Override
    public URL buildCallbackUrl(@Nullable HttpRequest originating, String providerName) {
        return build(originating, providerName, callbackUriTemplate);
    }

    @Override
    public URI buildLoginUri(@Nullable String providerName) {
        try {
            return new URI(getPath(loginUriTemplate, providerName));
        } catch (URISyntaxException e) {
            throw new RoutingException("Error building a URI for the path [" + loginUriTemplate + "]", e);
        }
    }

    @Override
    public URI buildCallbackUri(@Nullable String providerName) {
        try {
            return new URI(getPath(callbackUriTemplate, providerName));
        } catch (URISyntaxException e) {
            throw new RoutingException("Error building a URI for the path [" + callbackUriTemplate + "]", e);
        }
    }

    /**
     * Builds a URL with the provided arguments
     *
     * @param originating  The originating request
     * @param providerName The oauth provider name
     * @param uriTemplate  The URI template
     *
     * @return The URL
     */
    protected URL build(@Nullable HttpRequest originating, String providerName, String uriTemplate) {
        return buildUrl(originating, getPath(uriTemplate, providerName));
    }

    /**
     * Builds the path portion of the URL
     *
     * @param uriTemplate  The uri template
     * @param providerName The provider name
     *
     * @return The URL path
     */
    protected String getPath(String uriTemplate, String providerName) {
        Map<String, Object> uriParams = new HashMap<>(1);
        uriParams.put("provider", providerName);
        return UriTemplate.of(uriTemplate).expand(uriParams);
    }

    @Override
    public URL buildUrl(@Nullable HttpRequest current, String path) {
        UriBuilder uriBuilder = UriBuilder.of(baseUrl)
            .path(path);

        return Optional.ofNullable(current)
            .flatMap(a -> a.getParameters().get("redirect_uri", String.class))
            .map(redirectUrl -> getUrl(() -> uriBuilder
                .queryParam("redirect_uri", redirectUrlExtractor.extractUrl(redirectUrl).toString())
                .build()
                .toURL()
            ))
            .orElseGet(() -> getUrl(() ->
                uriBuilder
                    .build()
                    .toURL()
            ));
    }

    private interface URLProvider {

        URL getUrl() throws MalformedURLException, IllegalArgumentException;
    }

    private URL getUrl(URLProvider provider) {
        try {
            return provider.getUrl();
        } catch (MalformedURLException e) {
            throw new RoutingException("Error building an absolute URL for the path", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }
}
