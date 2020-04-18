package com.github.mdsina.corona;

import com.github.mdsina.corona.slack.SlackStatsProducer;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
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
    void sendCoronaStats() {
        logger.debug("start daily task");
        try {
            ChatPostMessageResponse response = slackStatsProducer.sendStats();
            if (response.getError() != null) {
                throw new RuntimeException(response.toString());
            }
            logger.info("end daily task");
        } catch (Throwable e) {
            logger.error("Error occurred on sending data to slack channel. Task will be rescheduled.", e);
            taskScheduler.schedule(Duration.ofSeconds(30L), this::sendCoronaStats);
        }
    }
}
