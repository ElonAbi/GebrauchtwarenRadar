package de.kleinanzeigen.app.search;

import de.kleinanzeigen.app.searchprofile.SearchProfile;
import de.kleinanzeigen.app.searchprofile.SearchProfileRepository;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

@Component
public class SearchJobScheduler {

    private static final Logger log = LoggerFactory.getLogger(SearchJobScheduler.class);

    private final SearchProfileRepository repository;
    private final SearchExecutionService executionService;
    private final TaskScheduler taskScheduler;
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public SearchJobScheduler(SearchProfileRepository repository, SearchExecutionService executionService) {
        this.repository = repository;
        this.executionService = executionService;
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("search-job-");
        scheduler.initialize();
        this.taskScheduler = scheduler;
    }

    @PostConstruct
    void init() {
        repository.findAll().forEach(this::scheduleProfile);
    }

    public void scheduleProfile(SearchProfile profile) {
        cancelProfile(profile.getId());
        Runnable task = () -> {
            try {
                executionService.execute(profile.getId());
            } catch (Exception ex) {
                log.error("Failed to execute search profile {}", profile.getId(), ex);
            }
        };
        long periodMillis = Duration.ofMinutes(profile.getFrequencyMinutes()).toMillis();
        ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(task, periodMillis);
        scheduledTasks.put(profile.getId(), future);
        log.info("Scheduled search profile {} to run every {} minutes", profile.getId(), profile.getFrequencyMinutes());
    }

    public void cancelProfile(Long profileId) {
        ScheduledFuture<?> future = scheduledTasks.remove(profileId);
        if (future != null) {
            future.cancel(false);
            log.info("Cancelled schedule for search profile {}", profileId);
        }
    }
}
