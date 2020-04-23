package com.github.mdsina.corona;

import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.TaskScheduler;
import io.reactivex.Observable;
import java.time.Duration;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Singleton
public class RetryableTaskRunner {

    @Named(TaskExecutors.SCHEDULED)
    private final TaskScheduler taskScheduler;

    public void run(Runnable runnable, Duration delay) {
        try {
            runnable.run();
        } catch (Throwable e) {
            log.error("Error occurred on running task. Task will be rescheduled.", e);
            taskScheduler.schedule(delay, () -> run(runnable, delay));
        }
    }

    public void run(Runnable runnable) {
        run(runnable, Duration.ofSeconds(60L));
    }
}
