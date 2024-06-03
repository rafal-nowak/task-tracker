package com.rafalnowak.tasktracker.task;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskInitializer implements CommandLineRunner {
    private final TaskRepository taskRepository;

    @Override
    public void run(String... args) throws Exception {
        taskRepository.save(new Task("Start with learning Docker Swarm"));
    }
}
