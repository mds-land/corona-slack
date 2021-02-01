package com.github.mdsina.corona.discord.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mdsina.corona.corona.CoronaSlackDataService;
import com.github.mdsina.corona.discord.DiscordProperties;
import com.github.mdsina.corona.discord.DiscordSignatureVerifier;
import com.github.mdsina.corona.discord.DiscordWebhooksClient;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("discord")
@RequiredArgsConstructor
@Slf4j
public class DiscordController {

    private final DiscordSignatureVerifier signatureVerifier;
    private final ObjectMapper objectMapper;
    private final DiscordWebhooksClient discordWebhooksClient;
    private final DiscordProperties discordProperties;
    private final CoronaSlackDataService coronaSlackDataService;

    @Post(consumes = {MediaType.APPLICATION_JSON}, produces = {MediaType.APPLICATION_JSON})
    public HttpResponse interactions(
        @Body String rawBody, // TODO: make as filter or interceptor
        @Header("X-Signature-Timestamp") String timestamp,
        @Header("X-Signature-Ed25519") String signature
    ) throws JsonProcessingException {
        log.debug(rawBody);
        log.debug(timestamp);
        log.debug(signature);

        try {
            signatureVerifier.verifyRequest(rawBody, timestamp, signature);
        } catch (Exception e) {
            return HttpResponse.unauthorized().body(Map.of("error", e.getMessage()));
        }

        Map<String, Object> body = objectMapper.readValue(rawBody, Map.class);
        int type = Integer.parseInt(body.get("type").toString());
        if (type == 1) {
            return HttpResponse.ok(Map.of("type", 1));
        }

        if (body.containsKey("data")) {
            Map<String, Object> data = (Map<String, Object>) body.get("data");

            if (data.get("name").toString().startsWith("corona")) {
                List<String> countries = List.of();

                if (data.containsKey("options")) {
                    var options = (List<Map<String, String>>) data.get("options");
                    countries = List.of(StringUtils.tokenizeToStringArray(options.get(0).get("value"), ","));
                }

                coronaSlackDataService.getDiscordSectionedActualStats(countries)
                    .flatMap(res -> discordWebhooksClient.postFollowMessage(
                        discordProperties.getApplicationId(),
                        body.get("token").toString(),
                        Map.of("embeds", List.of(Map.of(
                            "fields", res
                        )))
                    ))
                    .subscribe();

                return HttpResponse.ok(Map.of(
                    "type", 5
                ));
            }
        }

        return HttpResponse.accepted();
    }


}
