package env;

import methods.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public interface BaseTest {
    public static WebDriver driver = Env.CreateWebDriver(Env.getBrowserName());
    public static String endPointToTest = Env.getEndPointToTest();
    public static String adminPassword = Env.getAdminPassword();
    public static String adminUser = Env.getAdminUser();
    public static WebDriverWait wait = new WebDriverWait(BaseTest.driver,
        Duration.ofSeconds(10L));

    MiscMethods miscmethodObj = new MiscMethods();
    NavigateMethods navigationObj = new NavigateMethods();
    AssertionMethods assertionObj = new AssertionMethods();
    ClickElementsMethods clickObj = new ClickElementsMethods();
    ConfigurationMethods configObj = new ConfigurationMethods();
    InputMethods inputObj = new InputMethods();
    ProgressMethods progressObj = new ProgressMethods();
    JavascriptHandlingMethods javascriptObj = new JavascriptHandlingMethods();
    ScreenShotMethods screenshotObj = new ScreenShotMethods();
}
