package env;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Env {
    static WebDriver driver = null;
    static String browserName = null;
    static String endPointToTest = null;
    static String cloudBrowserConfigFile = null;
    static String cloudPlatformConfigFile = null;
    static String currentPath = System.getProperty("user.dir");
    static Properties prop = new Properties();

    private static Logger LOGGER = LoggerFactory.getLogger(Env.class);

    static {
        InputStream input = null;
        try {
            input = new FileInputStream(Env.currentPath + "/src/test/resources/system.properties");
            Env.prop.load(input);
        } catch (final Exception e) {
            e.printStackTrace();
            System.exit(0);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static WebDriver CreateWebDriver(String browser) {
        Env.LOGGER.debug("Browser: " + browser);

        switch (browser.toLowerCase()) {
        case "ff":
        case "firefox":
            Env.driver = new FirefoxDriver();
            break;

        case "ch":
        case "chrome":
            Env.driver = new ChromeDriver();
            break;

        case "phantom":
        case "phantomjs":
            Env.driver = new PhantomJSDriver();
            break;

        case "ie":
        case "internetexplorer":
            Env.driver = new InternetExplorerDriver();
            break;

        case "safari":
            Env.driver = new SafariDriver();
            break;

        default:
            Env.LOGGER.error("Invalid browser name " + browser);
            System.exit(0);
            break;
        }// switch

        Env.driver.manage().deleteAllCookies();
        Env.driver.manage().window().maximize();
        Env.driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
        Env.driver.manage().timeouts().setScriptTimeout(60, TimeUnit.SECONDS);
        Env.driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

        return Env.driver;
    }

    public static String getBrowserName() {
        Env.browserName = System.getProperty("browser");
        Env.cloudBrowserConfigFile = System.getProperty("cloud_config");

        if (Env.cloudBrowserConfigFile != null) {
            Env.LOGGER.debug("reading config file");
            try {
                Env.browserName = Env.cloudBrowserConfigFile.split("_")[0];
                final InputStream input = new FileInputStream(
                        Env.currentPath + "/src/main/java/cloudBrowserConfigs/" + Env.cloudBrowserConfigFile + ".properties");
                input.close();

            } catch (final Exception e) {
                e.printStackTrace();
                System.exit(0);
            }
        } else if (Env.browserName == null) {
            Env.browserName = "ff";
        }
        return Env.browserName;
    }

    public static String getEndPointToTest() {
        Env.endPointToTest = System.getProperty("endPointToTest");

        if (Env.endPointToTest == null) {
            Env.endPointToTest = Env.prop.getProperty("endPointToTest.url");
        }
        return Env.endPointToTest;
    }

    public static String getAdminPassword() {
        return Env.prop.getProperty("adminPassword");
    }

    public static String getAdminUser() {
        return Env.prop.getProperty("adminUser");
    }

}
