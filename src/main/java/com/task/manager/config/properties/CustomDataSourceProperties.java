package com.task.manager.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "spring.datasource")
public class CustomDataSourceProperties {

    @NotNull
    private DataSource main;

    @NotNull
    private DataSource backup;

    @Data
    public static class DataSource {

        @NotBlank
        private String url;

        @NotBlank
        private String driverClassName;

        @NotBlank
        private String username;

        @NotBlank
        private String password;
    }
}
