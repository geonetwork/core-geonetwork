package org.fao.geonet.util;

import org.apache.logging.log4j.ThreadContext;
import org.fao.geonet.utils.Log;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class LogUtilTest {

    @Test
    public void retrievesHarvesterLogfilePath() {
        ThreadContext.put("logfile", "harvester_geonetwork40_TestHarvester_20251030083002.log");
        try (MockedStatic<Log> logMock = mockStatic(Log.class)) {
            logMock.when(Log::getLogfile).thenReturn(Paths.get("logs/mock.log").toFile());

            assertEquals("harvester_geonetwork40_TestHarvester_20251030083002.log", LogUtil.getHarvesterLogfilePath());
        }
    }
}
