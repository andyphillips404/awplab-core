package com.awplab.core.common;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

/**
 * Created by andyphillips404 on 4/25/16.
 */
public class TemporaryFile extends File implements AutoCloseable {

    Logger logger = LoggerFactory.getLogger(TemporaryFile.class);

    public TemporaryFile(String pathname) {
        super(pathname);
    }

    public TemporaryFile(String parent, String child) {
        super(parent, child);
    }

    public TemporaryFile(File parent, String child) {
        super(parent, child);
    }

    public TemporaryFile(URI uri) {
        super(uri);
    }


    public static TemporaryFile wrapByAbsolutePath(File file) {
        return new TemporaryFile(file.getAbsolutePath());
    }

    public static TemporaryFile randomFile() {
        return randomFile(null);
    }

    public static TemporaryFile randomFile(String suffix) {
        return new TemporaryFile(SystemUtils.getJavaIoTmpDir(), UUID.randomUUID().toString() + (suffix == null ? "" : suffix));

    }

    @Override
    public void close()  {
        try {
            FileUtils.forceDelete(this);
        }
        catch (IOException ex) {
            logger.warn("Unable to delete temporary file: " + this.getAbsolutePath(), ex);
        }
    }
}
