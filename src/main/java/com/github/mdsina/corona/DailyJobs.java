package com.github.mdsina.corona;

import io.micronaut.scheduling.annotation.Scheduled;
import java.util.concurrent.ExecutionException;
import javax.inject.Singleton;

@Singleton
public class DailyJobs {

    private final SlackStatsSender slackStatsSender;

    public DailyJobs(SlackStatsSender slackStatsSender) {
        this.slackStatsSender = slackStatsSender;
    }

    @Scheduled(cron = "0 30 4 1/1 * ?")
    void sendCoronaStats() throws ExecutionException, InterruptedException {
        slackStatsSender.sendStats();
    }
}
