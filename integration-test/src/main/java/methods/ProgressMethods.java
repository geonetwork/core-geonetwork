package methods;

import env.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class ProgressMethods extends SelectElementByType implements BaseTest {
    /**
     * Method to wait
     *
     * @param time   : String : Time to wait
     * @throws NumberFormatException
     * @throws InterruptedException
     */
    public void wait(String time) throws NumberFormatException, InterruptedException {
        // sleep method takes parameter in milliseconds
        Thread.sleep(Integer.parseInt(time) * 1000);
    }

    /**
     * Method to Explicitly wait for element to be enabled=click
     *
     * @param accessType : String : Locator type (id, name, class, xpath, css)
     * @param accessName : String : Locator value
     * @param duration   : String : Time to wait for element to be clickable
     */
    public void waitForElementToClick(String accessType, String accessName, String duration) {
        final By byEle = getelementbytype(accessType, accessName);
        final WebDriverWait wait = new WebDriverWait(BaseTest.driver,
            Duration.ofSeconds(Integer.parseInt(duration)));
        wait.until(ExpectedConditions.elementToBeClickable(byEle));
    }

    /**
     * Method to Explicitly wait for element to be displayed
     *
     * @param accessType : String : Locator type (id, name, class, xpath, css)
     * @param accessName : String : Locator value
     * @param duration   : String : Time to wait for element to be displayed
     */
    public void waitForElementToDisplay(String accessType, String accessName, String duration) {
        final By byEle = getelementbytype(accessType, accessName);
        final WebDriverWait wait = new WebDriverWait(BaseTest.driver,
            Duration.ofSeconds(Integer.parseInt(duration)));
        wait.until(ExpectedConditions.visibilityOfElementLocated(byEle));
    }

}
