package com.github.mdsina.corona.admin;

import com.github.mdsina.corona.admin.persistence.TeamConfigRepository;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.views.View;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/admin")
public class ManagingController {

    private final TeamConfigRepository teamConfigRepository;

    @View("admin/edit")
    @Get(value = "/manage", produces = MediaType.TEXT_HTML)
    public Maybe<Map<String, Object>> manage(Authentication authentication) {
        String teamId = (String) authentication.getAttributes().get("team_id");

        return teamConfigRepository.getConfig(teamId)
            .switchIfEmpty(Maybe.error(() -> new RuntimeException("Install Application to your workspace first!")))
            .map(config -> Map.of(
                "teamId", authentication.getAttributes().get("team_id"),
                "config", config.getConfig()
            ));
    }

    @View("admin/edit")
    @Post(value = "/manage", produces = MediaType.TEXT_HTML, consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public Single<Map<String, Object>> managePost(Authentication authentication, @Body ConfigParameters request) {
        String teamId = (String) authentication.getAttributes().get("team_id");

        return teamConfigRepository.addOrUpdateConfig(teamId, request)
            .andThen(Single.just(Map.of(
                "teamId", teamId,
                "config", request
            )));
    }
}
