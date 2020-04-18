package com.github.mdsina.corona;

import io.micronaut.context.annotation.Requires;
import io.micronaut.scheduling.annotation.Scheduled;
import javax.inject.Singleton;

@Requires(env = "dev")
@Singleton
public class DevDailyJobs {

    private final DailyJobs dailyJobs;

    public DevDailyJobs(DailyJobs dailyJobs) {
        this.dailyJobs = dailyJobs;
    }

    @Scheduled(fixedDelay = "10s")
    void sendCoronaStatsDev() {
        dailyJobs.sendCoronaStats();
    }
}
