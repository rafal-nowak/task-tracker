package com.rafalnowak.tasktracker.task;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Task {
    @Id
    @GeneratedValue
    private Long id;

    private String description;

    public Task(String description) {
        this.description = description;
    }
}
