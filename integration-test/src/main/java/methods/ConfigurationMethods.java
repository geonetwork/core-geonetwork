package methods;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import env.BaseTest;

public class ConfigurationMethods implements BaseTest {

    private static Logger LOGGER = LoggerFactory.getLogger(ConfigurationMethods.class);

    /** Method to print desktop configuration */
    public void printDesktopConfiguration() {
        final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
        final Calendar cal = Calendar.getInstance();

        ConfigurationMethods.LOGGER.debug("Following are machine configurations : \n");
        ConfigurationMethods.LOGGER.debug("Date (MM/DD/YYYY) and Time (HH:MM:SS) : " + dateFormat.format(cal.getTime()));

        final Capabilities cap = ((RemoteWebDriver) BaseTest.driver).getCapabilities();
        ConfigurationMethods.LOGGER.debug("Browser : " + cap.getBrowserName());
        ConfigurationMethods.LOGGER.debug("Platform : " + cap.getPlatform());
    }
}
