package org.fao.geonet;

import org.springframework.core.io.InputStreamResource;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by Jesse on 12/9/13.
 */
public class TestDatabasePopulator extends ResourceDatabasePopulator {
    /**
     * Set the scripts which are urls to a file.  If the url contains ${webapp} then it will be replaced with the path to the
     * webapp directory of geonetwork: web/src/main/webapp
     * @param scripts
     */
    public void setInterpolatedScripts(String[] scripts) throws IOException {
        final String base = "src/main/webapp/WEB-INF/config.xml";
        File webappFile;
        if (new File("web/"+base).exists()) {
            webappFile = new File("web/"+base);
        } else if (new File("../web/"+base).exists()) {
            webappFile = new File("../web/"+base);
        } else if (new File(base).exists()) {
            webappFile = new File(base);
        } else {
            throw new AssertionError("Unable to locate the web/src/main/webapp webapp directory from the current directory: "+new File(".").getAbsolutePath());
        }

        String webapp = webappFile.getParentFile().getParentFile().getCanonicalPath().replace(File.separatorChar, '/');
        if (System.getProperty("os.name").toLowerCase().indexOf("win") ==  -1) {
            webapp = "/"+webapp;
        }
        for (String script : scripts) {
            String finalString = script.replace("${webapp}", webapp);
            final String classpathPrefix = "classpath:";
            if (finalString.startsWith(classpathPrefix)) {
                final String resourceString = finalString.substring(classpathPrefix.length());
                try {
                    addScript(new InputStreamResource(getClass().getClassLoader().getResourceAsStream(resourceString)));
                } catch (Exception e) {
                    throw new AssertionError("Failed to load data script as a from the classpath: "+resourceString);
                }
            } else {
                // series of hack for different platforms...
                try {
                    addScript(new InputStreamResource(new URL(finalString).openStream()));
                } catch (Exception e) {
                    String fileUrlPrefix = "file:/";
                    if (finalString.startsWith(fileUrlPrefix)) {
                        finalString = finalString.substring(fileUrlPrefix.length());
                    }
                    if (!new File(finalString).exists()) {
                        finalString = finalString.substring(1);
                    }
                    if (new File(finalString).exists()) {
                        addScript(new InputStreamResource(new FileInputStream(finalString)));
                    } else {
                        throw new AssertionError("Failed to load data script as a url: "+finalString);
                    }
                }
            }
        }
    }
}
