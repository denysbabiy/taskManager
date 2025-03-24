package com.task.manager.domain;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@ExtendWith(MockitoExtension.class)
class TaskTest {

    private static final long ONE_HOUR_IN_MILLIS = Duration.ofHours(1).toMillis();

    @InjectMocks
    private Task testingInstance;

    @BeforeEach
    void setUp() {
        testingInstance = new Task();
        testingInstance.setStatus(TaskStatus.IN_PROGRESS);
    }

    @Test
    void shouldGetCurrentTimeSpentWhenTaskIsInProgress() {
        testingInstance.prePersist();
        testingInstance.setStartedAt(Instant.now().minus(Duration.ofHours(1)));

        var currentTimeSpent = testingInstance.getCurrentTimeSpent();

        assertThat(currentTimeSpent.toMillis(), greaterThanOrEqualTo(ONE_HOUR_IN_MILLIS));
    }

    @Test
    void shouldGetCurrentTimeSpentWhenTaskIsNotInProgress() {
        testingInstance.setTimeSpent(Duration.ofHours(2));
        testingInstance.endProgress();

        var currentTimeSpent = testingInstance.getCurrentTimeSpent();

        assertThat(currentTimeSpent, equalTo(Duration.ofHours(2)));
    }

    @Test
    void shouldStopProgressAndUpdateTimeSpentWhenEndProgressIsCalled() {
        testingInstance.prePersist();
        testingInstance.setStartedAt(Instant.now().minus(Duration.ofHours(1)));

        testingInstance.endProgress();

        assertThat(testingInstance.getStartedAt(), nullValue());
        assertThat(testingInstance.getTimeSpent().toMillis(), greaterThanOrEqualTo(ONE_HOUR_IN_MILLIS));
    }

    @Test
    void shouldSetStartedAtWhenStartProgressIsCalled() {
        testingInstance.startProgress();

        assertThat(testingInstance.getStartedAt(), notNullValue());
    }
}