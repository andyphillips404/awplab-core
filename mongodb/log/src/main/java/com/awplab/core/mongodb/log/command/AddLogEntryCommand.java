package com.awplab.core.mongodb.log.command;


import com.awplab.core.common.TemporaryFile;
import com.awplab.core.mongodb.log.Log;
import com.awplab.core.mongodb.log.LogFile;
import com.awplab.core.mongodb.log.MDCAutoClosable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * Created by andyphillips404 on 2/25/15.
 */
@Command(scope = "mongo-log", name="add-log-entry")
@Service
public class AddLogEntryCommand implements Action {

    Logger logger = LoggerFactory.getLogger(AddLogEntryCommand.class);

    @Argument(index = 0, name = "level", description = "level", required = true)
    private String level;

    @Argument(index = 1, name = "entry", description = "log entry", required = true)
    private String logEntry;

    @Option(name = "-d", description = "name of database")
    private String database;

    @Option(name = "-c", description = "name of collection")
    private String collection;

    @Option(name = "-g", description = "name of GridFS collection")
    private String gridFSCollection;

    @Option(name = "--throwable", description = "throws a runtime exception with this message")
    private String throwableMessage;

    @Option(name = "--attach-file-url", description = "attaches a file downloaded form this url")
    private String attachFileUrl;

    @Override
    public Object execute() throws Exception {


        final MDCAutoClosable logClosable = new MDCAutoClosable();
        if (database != null) logClosable.with(Log.MDC_KEY_DATABASE, database);
        if (collection != null) logClosable.with(Log.MDC_KEY_COLLECTION, collection);
        if (gridFSCollection != null) logClosable.with(Log.MDC_KEY_GRIDFS_COLLECTION, gridFSCollection);

        try {
            if (attachFileUrl != null) {
                TemporaryFile temporaryFile = TemporaryFile.randomFile("-" + FilenameUtils.getName(attachFileUrl));
                FileUtils.copyURLToFile(new URL(attachFileUrl), temporaryFile);
                logClosable.with("downloaded-file", new LogFile(temporaryFile));
            }

            if (level.equalsIgnoreCase("warn")) logger.warn(logEntry, throwableMessage != null ? new RuntimeException(throwableMessage) : null);
            if (level.equalsIgnoreCase("info")) logger.info(logEntry, throwableMessage != null ? new RuntimeException(throwableMessage) : null);
            if (level.equalsIgnoreCase("error")) logger.error(logEntry, throwableMessage != null ? new RuntimeException(throwableMessage) : null);
            if (level.equalsIgnoreCase("debug")) logger.debug(logEntry, throwableMessage != null ? new RuntimeException(throwableMessage) : null);
        }
        finally {
            logClosable.close();
        }

        return null;
    }

}
