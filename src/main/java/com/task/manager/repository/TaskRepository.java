package com.task.manager.repository;

import java.util.List;
import java.util.Optional;

import com.task.manager.domain.Task;
import com.task.manager.domain.TaskStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t WHERE t.status = :status")
    List<Task> findTasksByStatusWithLimit(@Param("status") TaskStatus status, Pageable pageable);

    Optional<Task> findByAssigneeIdAndStatus(Long userId, TaskStatus status);
}
