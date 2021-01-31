package com.github.mdsina.corona.discord.web;

import com.github.mdsina.corona.discord.DiscordSignatureVerifier;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("discord")
@RequiredArgsConstructor
@Slf4j
public class DiscordController {

    private final DiscordSignatureVerifier signatureVerifier;

    @Post(consumes = {MediaType.APPLICATION_JSON})
    public HttpResponse interactions(
        @Body String rawBody, // TODO: make as filter or interceptor
        @Header("X-Signature-Timestamp") String timestamp,
        @Header("X-Signature-Ed25519") String signature
    ) {
        log.debug(rawBody);

        try {
            signatureVerifier.verifyRequest(rawBody, timestamp, signature);
        } catch (Exception e) {
            return HttpResponse.unauthorized().body(e.getMessage());
        }

        return HttpResponse.accepted();
    }
}
