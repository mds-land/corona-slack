package com.github.mdsina.corona.corona;

import com.github.mdsina.corona.RetryableTaskRunner;
import com.github.mdsina.corona.slack.SlackLayoutEntity;
import com.github.mdsina.corona.slack.SlackMessageSender;
import io.micronaut.context.annotation.Value;
import io.micronaut.scheduling.annotation.Scheduled;
import java.util.Map;
import javax.inject.Singleton;

@Singleton
public class CoronaScheduledTasks {

    private final SlackMessageSender slackMessageSender;
    private final CoronaDataProvider coronaDataProvider;
    private final RetryableTaskRunner retryableTaskRunner;

    @Value("${slack.channel:}")
    private final String channel;

    public CoronaScheduledTasks(
        SlackMessageSender slackMessageSender,
        CoronaDataProvider coronaDataProvider,
        RetryableTaskRunner retryableTaskRunner,
        @Value("${slack.channel:}") String channel
    ) {
        this.slackMessageSender = slackMessageSender;
        this.coronaDataProvider = coronaDataProvider;
        this.retryableTaskRunner = retryableTaskRunner;
        this.channel = channel;
    }

    @Scheduled(initialDelay = "10s", cron = "0 0 12 1/1 * ?")
    void sendCoronaStats() {
        retryableTaskRunner.run(() -> {
            Map<String, Map<String, Map>> daysData = coronaDataProvider.get2DaysData();

            slackMessageSender.sendMessage(
                channel,
                SlackLayoutEntity.builder()
                    .layoutBuilderType(CoronaSlackLayoutBuilder.TYPE)
                    .layoutData(Map.of(
                        "todayStat", daysData.get("todayStat"),
                        "yesterdayStat", daysData.get("yesterdayStat")
                    ))
                    .build()
            );
        });
    }
}
