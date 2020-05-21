package com.github.mdsina.corona.wotd;

import com.github.mdsina.corona.scheduling.RetryableTaskRunner;
import com.github.mdsina.corona.slack.SlackLayoutEntity;
import com.github.mdsina.corona.slack.SlackMessageSender;
import io.micronaut.context.annotation.Value;
import io.micronaut.scheduling.annotation.Scheduled;
import java.util.Map;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Singleton
public class WotdScheduledTasks {

    private final RetryableTaskRunner retryableTaskRunner;
    private final SlackMessageSender slackMessageSender;
    private final WotdParser wotdParser;

    @Value("${slack.channel:}")
    private final String channel;

    public WotdScheduledTasks(
        RetryableTaskRunner retryableTaskRunner,
        SlackMessageSender slackMessageSender,
        WotdParser wotdParser,
        @Value("${slack.channel:}") String channel
    ) {
        this.retryableTaskRunner = retryableTaskRunner;
        this.slackMessageSender = slackMessageSender;
        this.wotdParser = wotdParser;
        this.channel = channel;
    }

    @Scheduled(cron = "0 15 10 1/1 * ?")
//    @Scheduled(fixedDelay = "20s")
    void sendWotd() {
        slackMessageSender
            .sendMessage(
                channel,
                SlackLayoutEntity.builder()
                    .layoutBuilderType(WotdSlackLayoutBuilder.TYPE)
                    .layoutData(Map.of("wotd", wotdParser.getAndParse()))
                    .build()
            )
            .subscribeOn(Schedulers.elastic())
            .doOnError(e -> {
                log.error("Error occurred on running task. Task will be rescheduled.", e);
                retryableTaskRunner.run(this::sendWotd);
            })
            .subscribe();
    }
}
