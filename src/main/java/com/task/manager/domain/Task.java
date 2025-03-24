package com.task.manager.domain;

import java.time.Duration;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "task")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long assigneeId;

    @Column
    private String title;

    @Column
    private String description;

    @Column
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Column(name = "time_spent")
    private Duration timeSpent;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        final Instant now = Instant.now();
        timeSpent = Duration.ZERO;
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public Duration getCurrentTimeSpent() {
        if (status == TaskStatus.IN_PROGRESS && startedAt != null) {
            return timeSpent.plus(Duration.between(startedAt, Instant.now()));
        }

        return timeSpent;
    }

    public void endProgress() {
        if (startedAt != null) {
            timeSpent = getCurrentTimeSpent();
            startedAt = null;
        }
    }

    public void startProgress() {
        startedAt = Instant.now();
    }
}
