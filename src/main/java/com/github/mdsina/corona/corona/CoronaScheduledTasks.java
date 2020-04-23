package com.github.mdsina.corona.corona;

import com.github.mdsina.corona.RetryableTaskRunner;
import com.github.mdsina.corona.slack.SlackMessageSender;
import io.micronaut.context.annotation.Value;
import io.micronaut.scheduling.annotation.Scheduled;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class CoronaScheduledTasks {

    private final SlackMessageSender slackMessageSender;
    private final RetryableTaskRunner retryableTaskRunner;
    private final CoronaSlackDataService coronaSlackDataService;

    @Value("${slack.channel:}")
    private final String channel;

    public CoronaScheduledTasks(
        SlackMessageSender slackMessageSender,
        RetryableTaskRunner retryableTaskRunner,
        CoronaSlackDataService coronaSlackDataService,
        @Value("${slack.channel:}") String channel
    ) {
        this.slackMessageSender = slackMessageSender;
        this.retryableTaskRunner = retryableTaskRunner;
        this.coronaSlackDataService = coronaSlackDataService;
        this.channel = channel;
    }

    @Scheduled(cron = "0 0 12 1/1 * ?")
    void sendCoronaStats() {
        coronaSlackDataService.getActualStatsBlocks(null)
            .flatMap(blocks -> slackMessageSender.sendMessage(channel, blocks))
            .doOnError(e -> {
                log.error("Error occurred on running task. Task will be rescheduled.", e);
                retryableTaskRunner.run(this::sendCoronaStats);
            })
            .subscribeOn(Schedulers.newThread())
            .subscribe();
    }
}
