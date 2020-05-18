package com.github.mdsina.corona.corona.web;

import com.github.mdsina.corona.corona.CoronaSlackDataService;
import com.github.mdsina.corona.slack.SlackMessageSender;
import com.github.mdsina.corona.slack.SlackVerificationService;
import com.github.mdsina.corona.slack.persistence.SlackTokensRepository;
import com.github.mdsina.corona.util.BodyUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/corona")
public class CoronaCommandsController {

    private final CoronaSlackDataService coronaSlackDataService;
    private final SlackMessageSender slackMessageSender;
    private final SlackVerificationService verificationService;
    private final SlackTokensRepository slackTokensRepository;

    @Post(value = "/stats", consumes = {MediaType.APPLICATION_FORM_URLENCODED})
    public String stats(
        @Body String rawBody, // TODO: make as filter or interceptor
        @Header("X-Slack-Request-Timestamp") String timestamp,
        @Header("X-Slack-Signature") String signature
    ) {
        log.debug(rawBody);
        Map<String, String> body = BodyUtils.parseFormToMap(rawBody);

        verificationService.verifyRequest(rawBody, timestamp, signature);

        slackTokensRepository
            .getTeamToken(body.get("team_id"))
            .flatMapCompletable(token ->
                coronaSlackDataService
                    .getActualStatsBlocks(getCountriesFromBody(body))
                    .map(blocks -> slackMessageSender.sendMessage(body.get("channel_id"), blocks, token))
                    .ignoreElement()
            )
            .subscribe();

        return "Ok, stats will be send shortly.";
    }

    @Post(value = "/image", consumes = {MediaType.APPLICATION_FORM_URLENCODED})
    public String graph(
        @Body String rawBody, // TODO: make as filter or interceptor
        @Header("X-Slack-Request-Timestamp") String timestamp,
        @Header("X-Slack-Signature") String signature
    ) {
        log.debug(rawBody);
        Map<String, String> body = BodyUtils.parseFormToMap(rawBody);

        verificationService.verifyRequest(rawBody, timestamp, signature);

        slackTokensRepository
            .getTeamToken(body.get("team_id"))
            .flatMapCompletable(token ->
                coronaSlackDataService
                    .getHistoricalStatsBlocks(getCountriesFromBody(body))
                    .map(blocks -> slackMessageSender.sendMessage(body.get("channel_id"), blocks, token))
                    .ignoreElement()
            )
            .subscribe();

        return "Ok, chart will be send shortly.";
    }

    private List<String> getCountriesFromBody(Map<String, String> body) {
        String commandText = body.get("text");

        return Optional.ofNullable(commandText)
            .map(t -> List.of(StringUtils.tokenizeToStringArray(t, ",")))
            .orElse(List.of());
    }
}
