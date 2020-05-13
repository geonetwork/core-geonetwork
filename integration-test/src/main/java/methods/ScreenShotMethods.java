package methods;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import env.BaseTest;

public class ScreenShotMethods implements BaseTest {

    private static Logger LOGGER = LoggerFactory.getLogger(ScreenShotMethods.class);

    /** Method to take screen shot and save in ./Screenshots folder */
    public void takeScreenShot() throws IOException {
        final File scrFile = ((TakesScreenshot) BaseTest.driver).getScreenshotAs(OutputType.FILE);

        final DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        final Calendar cal = Calendar.getInstance();
        ScreenShotMethods.LOGGER.debug(dateFormat.format(cal.getTime()));

        final String scrFilepath = scrFile.getAbsolutePath();
        ScreenShotMethods.LOGGER.debug("scrFilepath: " + scrFilepath);

        Path newFileDir = Paths.get("screenshots");
        Files.createDirectory(newFileDir).toFile();
        final String path = newFileDir.toAbsolutePath().toString();
        ScreenShotMethods.LOGGER.debug("path: " + path + "+++");

        ScreenShotMethods.LOGGER.info("****\n" + path + "/screenshot" + dateFormat.format(cal.getTime()) + ".png");

        Path newFilePath = Paths.get(path + "/screenshot" + dateFormat.format(cal.getTime()) + ".png");
        FileUtils.copyFile(scrFile, Files.createFile(newFilePath).toFile());

    }
}
