package org.fao.geonet.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.routing.RoutingAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.fao.geonet.utils.Log;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

public class LogUtilTest {

    @Test
    public void retrievesHarvesterLogfilePath() {
        ThreadContext.put("logfile", "harvester_geonetwork40_TestHarvester_20251030083002.log");
        assertEquals("harvester_geonetwork40_TestHarvester_20251030083002.log", LogUtil.getHarvesterLogfilePath());
    }

    @Test
    public void throwsExceptionWhenRoutingAppenderNotFound() {
        LoggerContext mockCtx = mock(LoggerContext.class);
        Configuration mockConfig = mock(Configuration.class);

        when(mockCtx.getConfiguration()).thenReturn(mockConfig);
        when(mockConfig.getAppender("Harvester")).thenReturn(null);

        try (MockedStatic<LogManager> logManagerMock = mockStatic(LogManager.class);
             MockedStatic<Log> logMock = mockStatic(Log.class)) {

            logManagerMock.when(() -> LogManager.getContext(false)).thenReturn(mockCtx);
            logMock.when(Log::getLogfile).thenReturn(Paths.get("/mock/log/dir/mock.log").toFile());

            assertThrows(IllegalStateException.class, LogUtil::getHarvesterLogfilePath);
        }
    }

    @Test
    public void throwsExceptionWhenFileNodeNotFound() {
        LoggerContext mockCtx = mock(LoggerContext.class);
        Configuration mockConfig = mock(Configuration.class);
        RoutingAppender mockRoutingAppender = mock(RoutingAppender.class);

        when(mockCtx.getConfiguration()).thenReturn(mockConfig);
        when(mockConfig.getAppender("Harvester")).thenReturn(mockRoutingAppender);
        when(mockRoutingAppender.getRoutes().getRoutes()[0].getNode().getChildren())
            .thenReturn(Collections.emptyList());

        try (MockedStatic<LogManager> logManagerMock = mockStatic(LogManager.class);
             MockedStatic<Log> logMock = mockStatic(Log.class)) {

            logManagerMock.when(() -> LogManager.getContext(false)).thenReturn(mockCtx);
            logMock.when(Log::getLogfile).thenReturn(Paths.get("/mock/log/dir/mock.log").toFile());

            assertThrows(IllegalStateException.class, LogUtil::getHarvesterLogfilePath);
        }
    }
}
