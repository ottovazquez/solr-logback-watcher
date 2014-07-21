package org.apache.solr.logging.logback;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.apache.solr.logging.LogWatcher;

public class EventAppender extends AppenderBase<LoggingEvent> {

    final LogWatcher<LoggingEvent> watcher;

    public EventAppender(LogWatcher<LoggingEvent> watcher) {
        this.watcher = watcher;
    }

    @Override
    protected void append(LoggingEvent event) {
        watcher.add(event, event.getTimeStamp());
    }

    @Override
    public void stop() {
        watcher.reset();
    }

}
