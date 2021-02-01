package com.github.mdsina.corona.discord;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.retry.annotation.Retryable;
import java.util.Map;
import reactor.core.publisher.Mono;

@Retryable(delay = "2s")
@Client("https://discord.com/api/v8/interactions")
public interface DiscordInteractionsClient {

    @Post(value = "/{interactionId}/{interactionToken}/callback", processes = MediaType.APPLICATION_JSON)
    Mono<Map> postInterractionMessage(@PathVariable String interactionId, @PathVariable String interactionToken);
}
