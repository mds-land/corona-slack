package com.github.mdsina.corona.slack.web;

import com.github.mdsina.corona.slack.SlackStatsProducer;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import java.lang.invoke.MethodHandles;
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

        return Map.of(
            "blocks", producer.getSlackBlocksWithData(),
            "response_type", "in_channel",
            "channel", body.get("channel_id")
        );
    }

}
