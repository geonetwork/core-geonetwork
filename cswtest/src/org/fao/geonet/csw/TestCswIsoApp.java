package org.fao.geonet.csw;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * JUnit test integrating the CSW ISO AP tests from TEAM ENGINE
 *
 * @author josegar
 */
public class TestCswIsoApp extends TestCase {
    public void testCsw() {
        String logDir = "logDir";
        String workDir = "work";
        String source = "scripts/csw_ap_iso1.0/ctl";
        String session = "s0001";

        try {
            InputStream is = TestCswIsoApp.class.getResourceAsStream("config.properties");
            Properties prop = new Properties();

            prop.load(is);
            logDir = prop.getProperty("logdir");
            workDir = prop.getProperty("workdir");
            source = prop.getProperty("source");
        } catch (IOException e) {
            // Ignore: use default values
        }

        String p1 = "-mode=test";
        String p2 = "-source=" + source;
        String p3 = "-workdir=" + workDir;
        String p4 = "-logdir=" + logDir;
        String p5 = "-session=" + session;

        String args[] = {p1, p2, p3, p4, p5};

        try {
            com.occamlab.te.Test.main(args);

            TestResultParser tr = new TestResultParser();
            tr.processLog(new File(logDir), session);

            // Show log with tests summary
            String p1Log = "-logdir=" + logDir;
            String p2Log = "-session=" + session;

            String argsLog[] = {p1Log, p2Log};

            com.occamlab.te.ViewLog.main(argsLog);

            // Fail test if any TEAM ENGINE test failed
            assertEquals(0, tr.getFailCount());

        } catch (Exception e) {
            e.printStackTrace();
            // Fail test if any exception
            fail();
        }
    }
}
