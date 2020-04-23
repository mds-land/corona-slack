package com.github.mdsina.corona.corona.web;

import com.github.mdsina.corona.corona.CoronaSlackDataService;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.reactivex.Single;
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

    @Post(value = "/stats", consumes = {MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON})
    public Single<Map<String, Object>> stats(@Body Map body) {
        log.debug(body.toString());

        return coronaSlackDataService
            .getActualStatsResponse(getCountriesFromBody(body), body.get("channel_id"));
    }

    @Post(value = "/image", consumes = {MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON})
    public Single<Map<String, Object>> graph(@Body Map body) {
        log.debug(body.toString());

        return coronaSlackDataService
            .getHistoricalStatsResponse(getCountriesFromBody(body), body.get("channel_id"));
    }

    private List<String> getCountriesFromBody(Map body) {
        String commandText = (String) body.get("text");

        return Optional.ofNullable(commandText)
            .map(t -> List.of(StringUtils.tokenizeToStringArray(t, ",")))
            .orElse(List.of());
    }
}
