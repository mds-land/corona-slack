package com.github.mdsina.corona.discord;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.retry.annotation.Retryable;
import java.util.Map;
import reactor.core.publisher.Mono;

@Retryable(delay = "2s")
@Client("https://discord.com/api/v8/webhooks")
public interface DiscordWebhooksClient {

    @Post(value = "/{applicationId}/{interactionToken}", processes = MediaType.APPLICATION_JSON)
    Mono<Map> postFollowMessage(
        @PathVariable String applicationId,
        @PathVariable String interactionToken,
        @Body Map resp
    );
}
