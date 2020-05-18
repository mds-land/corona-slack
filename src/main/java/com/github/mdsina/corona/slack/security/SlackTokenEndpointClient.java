package com.github.mdsina.corona.slack.security;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.HttpClientConfiguration;
import io.micronaut.security.oauth2.endpoint.token.request.DefaultTokenEndpointClient;
import io.micronaut.security.oauth2.endpoint.token.request.context.TokenRequestContext;
import io.micronaut.security.oauth2.endpoint.token.response.TokenResponse;
import javax.annotation.Nonnull;
import javax.inject.Singleton;
import org.reactivestreams.Publisher;

@Replaces(DefaultTokenEndpointClient.class)
@Singleton
public class SlackTokenEndpointClient extends DefaultTokenEndpointClient {

    public SlackTokenEndpointClient(
        BeanContext beanContext,
        HttpClientConfiguration defaultClientConfiguration
    ) {
        super(beanContext, defaultClientConfiguration);
    }

    @Nonnull
    @Override
    public <G, R extends TokenResponse> Publisher<R> sendRequest(
        TokenRequestContext<G, R> requestContext
    ) {
        MutableHttpRequest<G> request = HttpRequest.POST(
            requestContext.getEndpoint().getUrl(),
            requestContext.getGrant())
            .contentType(requestContext.getMediaType())
            .accept(MediaType.APPLICATION_JSON_TYPE);

        secureRequest(request, requestContext);

        // TODO, looks like currently micronaut have no options to configure behavior of
        // DefaultOauthAuthorizationResponseHandler to create context with custom TokenResponse
        return (Publisher<R>) getClient(requestContext.getClientConfiguration().getName())
            .retrieve(request, Argument.of(SlackTokenResponse.class), requestContext.getErrorResponseType());
    }
}
