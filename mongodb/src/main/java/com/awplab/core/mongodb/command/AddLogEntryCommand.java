package com.awplab.core.mongodb.command;


import com.awplab.core.mongodb.Log;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by andyphillips404 on 2/25/15.
 */
@Command(scope = "mongo", name="add-log-entry")
@Service
public class AddLogEntryCommand implements Action {

    Logger logger = LoggerFactory.getLogger(AddLogEntryCommand.class);

    @Argument(index = 0, name = "level", description = "level", required = false)
    private String level;

    @Argument(index = 1, name = "entry", description = "log entry", required = false)
    private String logEntry;

    @Argument(index = 2, name = "database", description = "name of database", required = false)
    private String database;

    @Argument(index = 3, name = "collection", description = "name of collection", required = false)
    private String collection;

    @Override
    public Object execute() throws Exception {


        try (Log.MDCLoggerAutoClosable ignored = new Log.MDCLoggerAutoClosable(database, collection)){
            if (level.equalsIgnoreCase("warn")) logger.warn(logEntry);
            if (level.equalsIgnoreCase("info")) logger.info(logEntry);
            if (level.equalsIgnoreCase("error")) logger.error(logEntry);
            if (level.equalsIgnoreCase("debug")) logger.debug(logEntry);
        }

        return null;
    }

}
