package com.task.manager.config;

import java.util.Map;

import javax.sql.DataSource;

import com.task.manager.config.properties.CustomDataSourceProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(CustomDataSourceProperties.class)
public class DataSourceConfig {

    private static final String MAIN = "main";
    private static final String BACKUP = "backup";

    private final CustomDataSourceProperties dataSourceProperties;

    @Bean
    @Qualifier("mainDataSource")
    public DataSource mainDataSource() {
        return DataSourceBuilder.create()
                .url(dataSourceProperties.getMain().getUrl())
                .driverClassName(dataSourceProperties.getMain().getDriverClassName())
                .username(dataSourceProperties.getMain().getUsername())
                .password(dataSourceProperties.getMain().getPassword())
                .build();
    }

    @Bean
    @Qualifier("backupDataSource")
    public DataSource backupDataSource() {
        return DataSourceBuilder.create()
                .url(dataSourceProperties.getBackup().getUrl())
                .driverClassName(dataSourceProperties.getBackup().getDriverClassName())
                .username(dataSourceProperties.getBackup().getUsername())
                .password(dataSourceProperties.getBackup().getPassword())
                .build();
    }

    @Bean
    @Primary
    public DataSource routingDataSource(@Qualifier("mainDataSource") DataSource mainDataSource,
            @Qualifier("backupDataSource") DataSource backupDataSource) {
        RoutingDataSource routingDataSource = new RoutingDataSource();
        routingDataSource.setTargetDataSources(Map.of(MAIN, mainDataSource, BACKUP, backupDataSource));
        routingDataSource.setDefaultTargetDataSource(mainDataSource);

        return routingDataSource;
    }

    @Bean
    public DataSourceInitializer mainDataSourceInitializer(@Qualifier("mainDataSource") DataSource mainDataSource) {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(mainDataSource);
        initializer.setDatabasePopulator(mainDatabasePopulator());

        return initializer;
    }

    private DatabasePopulator mainDatabasePopulator() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("schema-main.sql"));

        return populator;
    }

    @Bean
    public DataSourceInitializer backupDataSourceInitializer(@Qualifier("backupDataSource") DataSource backupDataSource) {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(backupDataSource);
        initializer.setDatabasePopulator(backupDatabasePopulator());

        return initializer;
    }

    private DatabasePopulator backupDatabasePopulator() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("schema-backup.sql"));

        return populator;
    }
}
