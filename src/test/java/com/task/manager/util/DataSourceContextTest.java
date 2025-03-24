package com.task.manager.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
class DataSourceContextTest {

    private static final String BACKUP = "backup";
    private static final String MAIN = "main";

    @Test
    void shouldSetGlobalAndLocalToBackupWhenSwitchDataSourceToBackup() {
        DataSourceContext.switchDataSourceToBackup();

        assertThat(DataSourceContext.isGlobalDataSourceBackup(), is(true));
        assertThat(DataSourceContext.getCurrentDataSourceKey(), is(BACKUP));
    }

    @Test
    void shouldSetGlobalAndLocalToMainWhenSwitchDataSourceToMain() {
        DataSourceContext.switchDataSourceToMain();

        assertThat(DataSourceContext.isGlobalDataSourceBackup(), is(false));
        assertThat(DataSourceContext.getCurrentDataSourceKey(), is(MAIN));
    }

    @Test
    void shouldRemoveLocalDataSourceTypeButNotRemoveGlobalDataSourceTypeWhenClearIsCalled() {
        DataSourceContext.switchDataSourceToBackup();

        DataSourceContext.clear();

        assertThat(DataSourceContext.getCurrentDataSourceKey(), is(BACKUP));
    }
}