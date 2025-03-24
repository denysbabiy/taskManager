package com.task.manager.config;

import com.task.manager.util.DataSourceContext;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceContext.getCurrentDataSourceKey();
    }
}
