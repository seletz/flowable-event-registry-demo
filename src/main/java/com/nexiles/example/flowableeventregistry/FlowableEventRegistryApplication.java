package com.nexiles.example.flowableeventregistry;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.eventregistry.api.EventRepositoryService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;

@Slf4j
@SpringBootApplication
public class FlowableEventRegistryApplication {
    private final RepositoryService repositoryService;
    private final EventRepositoryService eventRepositoryService;

    public FlowableEventRegistryApplication(RepositoryService repositoryService, EventRepositoryService eventRepositoryService) {
        this.repositoryService = repositoryService;
        this.eventRepositoryService = eventRepositoryService;
    }

    public static void main(String[] args) {
        SpringApplication.run(FlowableEventRegistryApplication.class, args);
    }

    @EventListener(ApplicationStartedEvent.class)
    public void started() {
        log.info("Application started.");

        log.info("Processes deployed: {}", repositoryService.createDeploymentQuery().count());
        log.info("Events deployed:    {}", eventRepositoryService.createDeploymentQuery().count());

    }
}
