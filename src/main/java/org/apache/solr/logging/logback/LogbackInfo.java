package org.apache.solr.logging.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.apache.solr.logging.LoggerInfo;

public class LogbackInfo extends LoggerInfo {

    final Logger logger;

    public LogbackInfo(String name, ch.qos.logback.classic.Logger logger) {
        super(name);
        this.logger = logger;
    }

    @Override
    public String getLevel() {
        if (logger == null) {
            return null;
        }
        Level level = logger.getLevel();
        if (level == null) {
            return null;
        }
        return level.toString();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isSet() {
        return (logger != null && logger.getLevel() != null);
    }
}
