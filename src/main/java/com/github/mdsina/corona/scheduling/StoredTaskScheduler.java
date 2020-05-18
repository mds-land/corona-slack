package com.github.mdsina.corona.scheduling;

import com.github.mdsina.corona.admin.ConfigParameters;
import com.github.mdsina.corona.admin.persistence.TeamConfigRepository;
import com.github.mdsina.corona.corona.CoronaSlackDataService;
import com.github.mdsina.corona.slack.SlackMessageSender;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.TaskScheduler;
import io.micronaut.scheduling.annotation.Scheduled;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class StoredTaskScheduler {

    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    private final TeamConfigRepository teamConfigRepository;
    private final SlackMessageSender slackMessageSender;
    private final CoronaSlackDataService coronaSlackDataService;
    private final TaskScheduler taskScheduler;

    public StoredTaskScheduler(
        TeamConfigRepository teamConfigRepository,
        SlackMessageSender slackMessageSender,
        CoronaSlackDataService coronaSlackDataService,
        @Named(TaskExecutors.SCHEDULED) TaskScheduler taskScheduler
    ) {
        this.teamConfigRepository = teamConfigRepository;
        this.slackMessageSender = slackMessageSender;
        this.coronaSlackDataService = coronaSlackDataService;
        this.taskScheduler = taskScheduler;
    }

    @Scheduled(fixedDelay = "20s")
    void updateJobs() {
        teamConfigRepository.getEnabledConfigs()
            .doOnSuccess(configs -> {
                log.debug("Update tasks configurations.");
                scheduledTasks.forEach((k, v) -> v.cancel(false));
                scheduledTasks.clear();

                configs.forEach(cfg -> scheduledTasks.compute(cfg.getTeam(), (key, oldV) -> {
                    log.debug("Reinstall task for team {}", cfg.getTeam());
                    if (oldV != null && !oldV.isCancelled()) {
                        oldV.cancel(false);
                    }

                    ConfigParameters params = cfg.getConfig();

                    return taskScheduler.schedule(
                        cfg.getConfig().getDailyCronString(),
                        () -> sendStatTask(params.getDailyChannel(), cfg.getToken(), params.getDailySections())
                    );
                }));
            })
            .subscribe();

    }

    private void sendStatTask(String channel, String token, List<String> sections) {
        coronaSlackDataService.getSectionedActualStatsBlocks(sections)
            .filter(s -> !s.isEmpty())
            .flatMapSingle(blocks -> slackMessageSender.sendMessage(channel, blocks, token))
            .doOnError(e -> log.error("Error occurred on running task", e))
            .subscribeOn(Schedulers.newThread())
            .subscribe();
    }
}
