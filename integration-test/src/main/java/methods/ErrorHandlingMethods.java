package methods;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorHandlingMethods {

    private static Logger LOGGER = LoggerFactory.getLogger(ConfigurationMethods.class);

    // print error for desktop
    private void printErrorDesktop() {
        ErrorHandlingMethods.LOGGER.error("\nInappropraite desktop browser : \"#{ENV['BROWSER']}\"");
        ErrorHandlingMethods.LOGGER.error("\nUsage : cucumber BROWSER=browser_name");
        ErrorHandlingMethods.LOGGER.error("\nBrowser Supported  :\n");
        ErrorHandlingMethods.LOGGER.error("\n1.ie\n2.chrome\n3.ff\n4.safari\n5.opera\n6.phantomjs");
        System.exit(0);
    }

    // print error if invalid platform
    public void printInvalidPlatform() {
        ErrorHandlingMethods.LOGGER.error("\nOops... Invalid Platform");
        ErrorHandlingMethods.LOGGER.error("\nSupported platform are \"android\" and \"iOS\".");
        ErrorHandlingMethods.LOGGER.error("\nTo run on Desktop no need to mention platform.");
        System.exit(0);
    }

    // Method to check browser type
    public void validateParameters(String platform, String browserType, String appPath) {
        if (platform.equals("desktop")) {
            if (Arrays.asList("ff", "ie", "chrome", "safari", "opera", "phantomjs").contains(browserType)) {
                printErrorDesktop();
            }
        } else if (platform.equals("android") || platform.equals("iOS")) {
            ErrorHandlingMethods.LOGGER.error("Not Implemented...");
        } else {
            printInvalidPlatform();
        }
    }
}
