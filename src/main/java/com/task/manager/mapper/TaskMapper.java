package com.task.manager.mapper;

import java.time.Duration;

import com.task.manager.domain.Task;
import com.task.manager.dto.CreateTaskDto;
import com.task.manager.dto.TaskDto;
import com.task.manager.dto.UpdateTaskDto;
import com.task.manager.dto.UpdateTaskStatusDto;
import com.task.manager.util.CommonMapperConfig;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = CommonMapperConfig.class)
public interface TaskMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timeSpent", ignore = true)
    @Mapping(target = "startedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", constant = "TODO")
    Task fromCreateTaskDto(CreateTaskDto createTaskDto);

    @Mapping(target = "timeSpent", source = "task", qualifiedByName = "mapTimeSpent")
    TaskDto fromTask(Task task);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assigneeId", ignore = true)
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "timeSpent", ignore = true)
    @Mapping(target = "startedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateTaskStatus(@MappingTarget Task task, UpdateTaskStatusDto updateTaskStatusDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timeSpent", ignore = true)
    @Mapping(target = "startedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateTask(@MappingTarget Task task, UpdateTaskDto updateTaskDto);

    @Named("mapTimeSpent")
    default String mapTimeSpent(Task task) {
        Duration duration = task.getCurrentTimeSpent() != null ? task.getCurrentTimeSpent() : Duration.ZERO;

        return DurationFormatUtils.formatDuration(duration.toMillis(), "HH:mm:ss");
    }
}
