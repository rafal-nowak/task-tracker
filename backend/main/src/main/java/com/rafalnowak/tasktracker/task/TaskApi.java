package com.rafalnowak.tasktracker.task;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskApi {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @GetMapping
    public ResponseEntity<List<TaskDto>> getAllTasks() {
        return ResponseEntity.ok(taskRepository.findAll().stream()
                .map(taskMapper::toDto)
                .toList());
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody @Valid TaskDto taskDto) {
        taskRepository.save(taskMapper.fromDto(taskDto));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
