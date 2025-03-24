package com.task.manager.util;

import org.mapstruct.Builder;
import org.mapstruct.MapperConfig;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.ReportingPolicy.ERROR;

@MapperConfig(
        componentModel = SPRING,
        unmappedTargetPolicy = ERROR,
        nullValueCheckStrategy = ALWAYS,
        builder = @Builder(disableBuilder = true)
)
public interface CommonMapperConfig {
}
