package com.task.manager.job;

import com.task.manager.util.DataSourceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class SwitchDataSourceToMainJob {

    @Scheduled(fixedRate = 18000)
    public void switchDataSourceToMain() {
        if (DataSourceContext.isGlobalDataSourceBackup()) {
            try {
                log.info("Switching to main data source");
                DataSourceContext.switchDataSourceToMain();
            } finally {
                DataSourceContext.clear();
            }
        }
    }
}
