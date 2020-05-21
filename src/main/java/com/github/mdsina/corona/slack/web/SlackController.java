package com.github.mdsina.corona.slack.web;

import com.github.mdsina.corona.slack.SlackProperties;
import com.github.mdsina.corona.slack.persistence.SlackTokensRepository;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import java.net.URI;
import java.util.Map;
import javax.annotation.Nullable;
import reactor.core.publisher.Mono;

@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("slack")
public class SlackController {

    private final String baseUrl;
    private final SlackProperties slackProperties;
    private final HttpClient slackHttpClient;
    private final SlackTokensRepository slackTokensRepository;

    public SlackController(
        @Value("${corona.base.url:}") String baseUrl,
        SlackProperties slackProperties,
        @Client("https://slack.com") RxHttpClient slackHttpClient,
        SlackTokensRepository slackTokensRepository
    ) {
        this.baseUrl = baseUrl;
        this.slackProperties = slackProperties;
        this.slackHttpClient = slackHttpClient;
        this.slackTokensRepository = slackTokensRepository;
    }

    @Get(value = "/install{?code}", produces = MediaType.TEXT_HTML)
    public Mono<String> callback(@QueryValue @Nullable String code) {
        URI redirectUrl = UriBuilder.of(baseUrl).path("slack/install").build();
        var resultUrl = UriBuilder.of("/api/oauth.v2.access")
            .toString();

        return Mono.from(slackHttpClient
            .retrieve(HttpRequest.POST(resultUrl, Map.of(
                "client_id", slackProperties.getClientId(),
                "client_secret", slackProperties.getClientSecret(),
                "code", code,
                "redirect_uri", redirectUrl
            )).contentType(MediaType.APPLICATION_FORM_URLENCODED_TYPE), Map.class))
            .flatMap(r -> {
                boolean ok = Boolean.parseBoolean("" + r.get("ok"));
                if (ok) {
                    return Mono.just(r);
                }

                return Mono.error(new RuntimeException(r.toString()));
            })
            .flatMap(r -> slackTokensRepository.addOrUpdateToken(
                (String) ((Map) r.get("team")).get("id"),
                (String) r.get("access_token")
            ))
            .then(Mono.just("App installed. You can close this page."))
            .onErrorResume(e -> Mono.just("App installation failed. " + e.getMessage() + ". Try again"));
    }
}
