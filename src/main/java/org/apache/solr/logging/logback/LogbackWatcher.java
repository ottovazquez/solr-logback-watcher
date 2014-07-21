package org.apache.solr.logging.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import com.google.common.base.Throwables;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.logging.CircularList;
import org.apache.solr.logging.ListenerConfig;
import org.apache.solr.logging.LogWatcher;
import org.apache.solr.logging.LoggerInfo;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getILoggerFactory;

public class LogbackWatcher extends LogWatcher<LoggingEvent> {

    final String name = LoggerContext.class.getName();
    AppenderBase appender = null;

    @Override
    public String getName() {
        return "Logback (" + name + ")";
    }

    @Override
    public List<String> getAllLevels() {
        return Arrays.asList(
                Level.ALL.toString(),
                Level.TRACE.toString(),
                Level.DEBUG.toString(),
                Level.INFO.toString(),
                Level.WARN.toString(),
                Level.ERROR.toString(),
                Level.OFF.toString());
    }

    @Override
    public void setLogLevel(String category, String level) {
        getLogger(category).setLevel(Level.toLevel(level, null));
    }

    @Override
    public Collection<LoggerInfo> getAllLoggers() {
        Logger root = getRootLogger();
        Map<String, LoggerInfo> map = new HashMap<>();
        LoggerContext loggerContext = (LoggerContext) getILoggerFactory();

        for (Logger logger : loggerContext.getLoggerList()) {
            String name = logger.getName();
            if (logger == root) {
                continue;
            }
            map.put(name, new LogbackInfo(name, logger));

            while (true) {
                int dot = name.lastIndexOf(".");
                if (dot < 0)
                    break;
                name = name.substring(0, dot);
                if (!map.containsKey(name)) {
                    map.put(name, new LogbackInfo(name, null));
                }
            }
        }

        map.put(LoggerInfo.ROOT_NAME, new LogbackInfo(LoggerInfo.ROOT_NAME, root));
        return map.values();

    }

    @Override
    public void setThreshold(String level) {
        if (appender == null) {
            throw new IllegalStateException("Must have an appender");

        }

        getRootLogger().setLevel(Level.toLevel(level));
    }

    @Override
    public String getThreshold() {
        if (appender == null) {
            throw new IllegalStateException("Must have an appender");

        }
        return getRootLogger().getLevel().toString();
    }


    @Override
    public void registerListener(ListenerConfig cfg) {
        if (history != null) {
            throw new IllegalStateException("History already registered");

        }
        history = new CircularList<>(cfg.size);
        appender = new EventAppender(this);
        appender.start();

        Logger logger = getRootLogger();
        logger.addAppender(appender);
        logger.setLevel(Level.toLevel(cfg.threshold, logger.getLevel()));
    }

    @Override
    public long getTimestamp(LoggingEvent event) {
        return event.getTimeStamp();
    }

    @Override
    public SolrDocument toSolrDocument(LoggingEvent event) {
        SolrDocument doc = new SolrDocument();
        doc.setField("time", new Date(event.getTimeStamp()));
        doc.setField("level", event.getLevel().toString());
        doc.setField("logger", event.getLoggerName());
        doc.setField("message", event.getFormattedMessage());
        ThrowableProxy t = (ThrowableProxy) event.getThrowableProxy();
        if (t != null) {
            doc.setField("trace", Throwables.getStackTraceAsString(t.getThrowable()));

        }

        return doc;
    }

    private Logger getLogger(String name) {
        return (Logger) org.slf4j.LoggerFactory.getLogger(name);
    }

    private Logger getRootLogger() {
        return getLogger(Logger.ROOT_LOGGER_NAME);
    }
}
