package methods;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import env.BaseTest;

public class ClickElementsMethods extends SelectElementByType implements BaseTest {
    // SelectElementByType eleType= new SelectElementByType();
    private WebElement element = null;

    /**
     * Method to click on an element
     *
     * @param accessType : String : Locator type (id, name, class, xpath, css)
     * @param accessName : String : Locator value
     */
    public void click(String accessType, String accessName) {
        element = BaseTest.wait.until(ExpectedConditions.presenceOfElementLocated(getelementbytype(accessType, accessName)));
        element.click();
    }

    /**
     * Method to forcefully click on an element
     *
     * @param accessType : String : Locator type (id, name, class, xpath, css)
     * @param accessName : String : Locator value
     */
    public void clickForcefully(String accessType, String accessName) {
        element = BaseTest.wait.until(ExpectedConditions.presenceOfElementLocated(getelementbytype(accessType, accessName)));
        final JavascriptExecutor executor = (JavascriptExecutor) BaseTest.driver;
        executor.executeScript("arguments[0].click();", element);
    }

    /**
     * Method to Double click on an element
     *
     * @param accessType : String : Locator type (id, name, class, xpath, css)
     * @param accessName : String : Locator value
     */
    public void doubleClick(String accessType, String accessValue) {
        element = BaseTest.wait.until(ExpectedConditions.presenceOfElementLocated(getelementbytype(accessType, accessValue)));

        final Actions action = new Actions(BaseTest.driver);
        action.moveToElement(element).doubleClick().perform();
    }
}