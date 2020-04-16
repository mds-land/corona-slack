package com.github.mdsina.corona;

import com.github.mdsina.corona.slack.SlackStatsProducer;
import io.micronaut.scheduling.annotation.Scheduled;
import java.util.concurrent.ExecutionException;
import javax.inject.Singleton;

@Singleton
public class DailyJobs {

    private final SlackStatsProducer slackStatsProducer;

    public DailyJobs(SlackStatsProducer slackStatsProducer) {
        this.slackStatsProducer = slackStatsProducer;
    }

    @Scheduled(cron = "0 30 4 1/1 * ?")
    void sendCoronaStats() throws ExecutionException, InterruptedException {
        slackStatsProducer.sendStats();
    }
}
