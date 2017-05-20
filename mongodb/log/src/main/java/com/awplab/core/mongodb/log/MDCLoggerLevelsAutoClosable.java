package com.awplab.core.mongodb.log;

import org.apache.log4j.MDC;
import org.ops4j.pax.logging.PaxLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andyphillips404 on 5/4/17.
 */
public class MDCLoggerLevelsAutoClosable implements AutoCloseable  {

    public static int WARNING = PaxLogger.LEVEL_WARNING;
    public static int ERROR = PaxLogger.LEVEL_ERROR;
    public static int INFO = PaxLogger.LEVEL_INFO;
    public static int DEBUG = PaxLogger.LEVEL_DEBUG;

    Map oldLoggingLevels = null;

    public static void setLoggerLevel(String loggerName, String level) {
        Object o = MDC.get(Log.MDC_KEY_LOGGER_LEVELS);
        Map map = new HashMap();
        if (o != null && o instanceof Map) {
            map.putAll((Map)o);
        }
        map.put(loggerName, level);
        MDC.put(Log.MDC_KEY_LOGGER_LEVELS, map);
    }

    public MDCLoggerLevelsAutoClosable(Object... loggerAndLevels) {
        oldLoggingLevels = (Map)MDC.get(Log.MDC_KEY_LOGGER_LEVELS);
        for (int x = 0; x < loggerAndLevels.length; x += 2) {
            setLoggerLevel(loggerAndLevels[x].toString(), loggerAndLevels[x+1].toString());
        }
    }

    @Override
    public void close() {
        if (oldLoggingLevels == null) {
            MDC.remove(Log.MDC_KEY_LOGGER_LEVELS);
        }
        else {
            MDC.put(Log.MDC_KEY_LOGGER_LEVELS, oldLoggingLevels);
        }
    }
}
