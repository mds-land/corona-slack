package com.github.mdsina.corona;

import com.github.mdsina.corona.slack.SlackStatsProducer;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.TaskScheduler;
import io.micronaut.scheduling.annotation.Scheduled;
import java.lang.invoke.MethodHandles;
import java.time.Duration;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DailyJobs {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SlackStatsProducer slackStatsProducer;
    protected final TaskScheduler taskScheduler;

    public DailyJobs(
        SlackStatsProducer slackStatsProducer,
        @Named(TaskExecutors.SCHEDULED) TaskScheduler taskScheduler
    ) {
        this.slackStatsProducer = slackStatsProducer;
        this.taskScheduler = taskScheduler;
    }

    @Scheduled(initialDelay = "10s", cron = "0 30 4 1/1 * ?")
//    @Scheduled(fixedDelay = "60s")
    void sendCoronaStats() {
        try {
            slackStatsProducer.sendStats();
        } catch (Throwable e) {
            logger.error("Error occurred on sending data to slack channel. Task will be rescheduled.", e);
            taskScheduler.schedule(Duration.ofSeconds(30L), this::sendCoronaStats);
        }
    }
}
