package methods;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import env.BaseTest;

public class NavigateMethods extends SelectElementByType implements BaseTest {
    // SelectElementByType eleType= new SelectElementByType();
    private WebElement element = null;
    private String old_win = null;
    private String lastWinHandle;

    /** Method to quite webdriver instance */
    public void closeDriver() {
        BaseTest.driver.quit();
    }

    /** Method to close new window */
    public void closeNewWindow() {
        BaseTest.driver.close();
    }

    /**
     * Method to return key by OS wise
     *
     * @return Keys : Return control or command key as per OS
     */
    public Keys getKey() {
        final String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return Keys.CONTROL;
        } else if (os.contains("nux") || os.contains("nix")) {
            return Keys.CONTROL;
        } else if (os.contains("mac")) {
            return Keys.COMMAND;
        } else {
            return null;
        }
    }

    /**
     * Method to hover on element
     *
     * @param accessType : String : Locator type (id, name, class, xpath, css)
     * @param accessName : String : Locator value
     */
    public void hoverOverElement(String accessType, String accessName) {
        final Actions action = new Actions(BaseTest.driver);
        element = BaseTest.wait.until(ExpectedConditions.presenceOfElementLocated(getelementbytype(accessType, accessName)));
        action.moveToElement(element).perform();
    }

    /** Method to maximize browser */
    public void maximizeBrowser() {
        BaseTest.driver.manage().window().maximize();
    }

    /**
     * Method to navigate back & forward
     *
     * @param direction : String : Navigate to forward or backward
     */
    public void navigate(String direction) {
        if (direction.equals("back")) {
            BaseTest.driver.navigate().back();
        } else {
            BaseTest.driver.navigate().forward();
        }
    }

    /**
     * Method to open link
     *
     * @param url : String : URL for navigation
     */
    public void navigateTo(String url) {
        BaseTest.driver.get(url);
    }

    /**
     * Method to resize browser
     *
     * @param width : int : Width for browser resize
     * @param height : int : Height for browser resize
     */
    public void resizeBrowser(int width, int height) {
        BaseTest.driver.manage().window().setSize(new Dimension(width, height));
    }

    /**
     * Method to scroll page to top or end
     *
     * @param to : String : Scroll page to Top or End
     * @throws Exception
     */
    public void scrollPage(String to) throws Exception {
        final JavascriptExecutor executor = (JavascriptExecutor) BaseTest.driver;
        if (to.equals("end")) {
            executor.executeScript(
                    "window.scrollTo(0,Math.max(document.documentElement.scrollHeight,document.body.scrollHeight,document.documentElement.clientHeight));");
        } else if (to.equals("top")) {
            executor.executeScript(
                    "window.scrollTo(Math.max(document.documentElement.scrollHeight,document.body.scrollHeight,document.documentElement.clientHeight),0);");
        } else {
            throw new Exception("Exception : Invalid Direction (only scroll \"top\" or \"end\")");
        }
    }

    /**
     * Method to scroll page to particular element
     *
     * @param accessType : String : Locator type (id, name, class, xpath, css)
     * @param accessName : String : Locator value
     */
    public void scrollToElement(String accessType, String accessName) {
        element = BaseTest.wait.until(ExpectedConditions.presenceOfElementLocated(getelementbytype(accessType, accessName)));
        final JavascriptExecutor executor = (JavascriptExecutor) BaseTest.driver;
        executor.executeScript("arguments[0].scrollIntoView();", element);
    }

    /**
     * Method to switch frame using web element frame
     *
     * @param accessType : String : Locator type (index, id, name, class, xpath, css)
     * @param accessName : String : Locator value
     */
    public void switchFrame(String accessType, String accessName) {
        if (accessType.equalsIgnoreCase("index")) {
            BaseTest.driver.switchTo().frame(accessName);
        } else {
            element = BaseTest.wait.until(ExpectedConditions.presenceOfElementLocated(getelementbytype(accessType, accessName)));
            BaseTest.driver.switchTo().frame(element);
        }
    }

    /** method to switch to default content */
    public void switchToDefaultContent() {
        BaseTest.driver.switchTo().defaultContent();
    }

    /** Method to switch to new window */
    public void switchToNewWindow() {
        old_win = BaseTest.driver.getWindowHandle();
        for (final String winHandle : BaseTest.driver.getWindowHandles()) {
            lastWinHandle = winHandle;
        }
        BaseTest.driver.switchTo().window(lastWinHandle);
    }

    /** Method to switch to old window */
    public void switchToOldWindow() {
        BaseTest.driver.switchTo().window(old_win);
    }

    /**
     * Method to switch to window by title
     *
     * @param windowTitle : String : Name of window title to switch
     * @throws Exception
     */
    public void switchToWindowByTitle(String windowTitle) throws Exception {
        // System.out.println("++"+windowTitle+"++");
        old_win = BaseTest.driver.getWindowHandle();
        boolean winFound = false;
        for (final String winHandle : BaseTest.driver.getWindowHandles()) {
            final String str = BaseTest.driver.switchTo().window(winHandle).getTitle();
            // System.out.println("**"+str+"**");
            if (str.equals(windowTitle)) {
                winFound = true;
                break;
            }
        }
        if (!winFound) {
            throw new Exception("Window having title " + windowTitle + " not found");
        }
    }

    /**
     * Method to zoom in/out page
     *
     * @param inOut : String : Zoom in or out
     */
    public void zoomInOut(String inOut) {
        final WebElement Sel = BaseTest.driver.findElement(getelementbytype("tagName", "html"));
        if (inOut.equals("ADD")) {
            Sel.sendKeys(Keys.chord(getKey(), Keys.ADD));
        } else if (inOut.equals("SUBTRACT")) {
            Sel.sendKeys(Keys.chord(getKey(), Keys.SUBTRACT));
        } else if (inOut.equals("reset")) {
            Sel.sendKeys(Keys.chord(getKey(), Keys.NUMPAD0));
        }
    }

    /**
     * Method to zoom in/out web page until web element displays
     *
     * @param accessType : String : Locator type (id, name, class, xpath, css)
     * @param inOut : String : Zoom in or out
     * @param accessName : String : Locator value
     */
    public void zoomInOutTillElementDisplay(String accessType, String inOut, String accessName) {
        final Actions action = new Actions(BaseTest.driver);
        element = BaseTest.wait.until(ExpectedConditions.presenceOfElementLocated(getelementbytype(accessType, accessName)));
        while (true) {
            if (element.isDisplayed()) {
                break;
            } else {
                action.keyDown(getKey()).sendKeys(inOut).keyUp(getKey()).perform();
            }
        }
    }
}
