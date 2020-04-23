package com.github.mdsina.corona.corona.web;

import com.github.mdsina.corona.corona.CoronaSlackDataService;
import com.github.mdsina.corona.slack.SlackMessageSender;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Controller("/corona")
public class CoronaCommandsController {

    private final CoronaSlackDataService coronaSlackDataService;
    private final SlackMessageSender slackMessageSender;

    @Post(value = "/stats", consumes = {MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON})
    public String stats(@Body Map body) {
        log.debug(body.toString());

        coronaSlackDataService
            .getActualStatsBlocks(getCountriesFromBody(body))
            .map(blocks -> slackMessageSender.sendMessage((String) body.get("channel_id"), blocks))
            .subscribe();

        return "Ok, stats will be send shortly.";
    }

    @Post(value = "/image", consumes = {MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON})
    public String graph(@Body Map body) {
        log.debug(body.toString());

        coronaSlackDataService
            .getHistoricalStatsBlocks(getCountriesFromBody(body))
            .map(blocks -> slackMessageSender.sendMessage((String) body.get("channel_id"), blocks))
            .subscribe();

        return "Ok, chart will be send shortly.";
    }

    private List<String> getCountriesFromBody(Map body) {
        String commandText = (String) body.get("text");

        return Optional.ofNullable(commandText)
            .map(t -> List.of(StringUtils.tokenizeToStringArray(t, ",")))
            .orElse(List.of());
    }
}
