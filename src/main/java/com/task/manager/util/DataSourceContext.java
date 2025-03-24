package com.task.manager.util;

import java.util.concurrent.atomic.AtomicReference;

public class DataSourceContext {

    private static final String MAIN = "main";
    private static final String BACKUP = "backup";
    private static final ThreadLocal<String> LOCAL_DATA_SOURCE_TYPE = new ThreadLocal<>();
    private static final AtomicReference<String> GLOBAL_DATA_SOURCE_TYPE = new AtomicReference<>(MAIN);

    public static void switchDataSourceToBackup() {
        GLOBAL_DATA_SOURCE_TYPE.set(BACKUP);
        LOCAL_DATA_SOURCE_TYPE.set(BACKUP);
    }

    public static void switchDataSourceToMain() {
        GLOBAL_DATA_SOURCE_TYPE.set(MAIN);
        LOCAL_DATA_SOURCE_TYPE.set(MAIN);
    }

    public static boolean isGlobalDataSourceBackup() {
        return BACKUP.equals(GLOBAL_DATA_SOURCE_TYPE.get());
    }

    public static String getCurrentDataSourceKey() {
        return LOCAL_DATA_SOURCE_TYPE.get() != null ? LOCAL_DATA_SOURCE_TYPE.get() : GLOBAL_DATA_SOURCE_TYPE.get();
    }

    public static void clear() {
        LOCAL_DATA_SOURCE_TYPE.remove();
    }
}
