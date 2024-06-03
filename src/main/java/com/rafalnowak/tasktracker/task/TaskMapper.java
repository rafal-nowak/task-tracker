package com.rafalnowak.tasktracker.task;

import org.springframework.stereotype.Component;

@Component
public class TaskMapper {
    public TaskDto toDto(Task task) {
        return new TaskDto(task.getDescription());
    }

    public Task fromDto(TaskDto taskDto) {
        return new Task(taskDto.getDescription());
    }
}
