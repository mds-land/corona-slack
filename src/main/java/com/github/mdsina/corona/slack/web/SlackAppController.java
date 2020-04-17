package com.github.mdsina.corona.slack.web;

import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;

import com.github.mdsina.corona.slack.SlackStatsProducer;
import com.slack.api.model.block.LayoutBlock;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("/corona")
public class SlackAppController {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SlackStatsProducer producer;

    public SlackAppController(SlackStatsProducer producer) {
        this.producer = producer;
    }

    @Post(value = "/stats", consumes = {MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON})
    public Map<String, Object> dispatch(@Body Map body) {

        logger.debug(body.toString());

        String commandText = (String) body.get("text");
        List<String> countries = List.of(StringUtils.tokenizeToStringArray(commandText, ","));

        List<LayoutBlock> blocks;
        try {
            if (countries.isEmpty()) {
                blocks = producer.getSlackBlocksWithDataForTop();
            } else {
                blocks = producer.getSlackBlocksWithData(countries);
            }
        } catch (Throwable e) {
            logger.error("Error on slack api call:", e);
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
