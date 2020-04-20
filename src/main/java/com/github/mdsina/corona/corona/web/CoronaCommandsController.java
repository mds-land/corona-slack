package com.github.mdsina.corona.corona.web;

import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;

import com.github.mdsina.corona.corona.CoronaDataProvider;
import com.github.mdsina.corona.corona.CoronaSlackLayoutBuilder;
import com.slack.api.model.block.LayoutBlock;
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

    private final CoronaSlackLayoutBuilder layoutBuilder;
    private final CoronaDataProvider dataProvider;

    @Post(value = "/stats", consumes = {MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON})
    public Map<String, Object> dispatch(@Body Map body) {

        log.debug(body.toString());

        String commandText = (String) body.get("text");
        List<String> countries = Optional.ofNullable(commandText)
            .map(t -> List.of(StringUtils.tokenizeToStringArray(t, ",")))
            .orElse(List.of());

        List<LayoutBlock> blocks;
        try {
            Map<String, Map<String, Map>> daysData = dataProvider.get2DaysData();
            blocks = layoutBuilder.buildBlocks(Map.of(
                "todayStat", daysData.get("todayStat"),
                "yesterdayStat", daysData.get("yesterdayStat"),
                "countries", countries
            ));
        } catch (Throwable e) {
            log.error("Error on build response:", e);
            blocks = List.of(section(s -> s.text(markdownText(
                ":x: Cannot retrieve corona statistics: " + e.getMessage()
            ))));
        }

        return Map.of(
            "blocks", blocks,
            "response_type", "in_channel",
            "channel", body.get("channel_id")
        );
    }

}
