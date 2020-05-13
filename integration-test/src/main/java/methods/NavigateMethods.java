package methods;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import env.BaseTest;

import java.io.IOException;

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

    private String xsrfToken = null;
    private BasicCookieStore cookieStore = new BasicCookieStore();

    /**
     * Initialize the XSRF token
     */
    private void retrieveToken() {
        if (xsrfToken == null) {
            CloseableHttpClient httpClient =
                HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
            HttpResponse httpResponse = null;
            try {
                HttpPost request = new HttpPost(endPointToTest + "/srv/eng/info?type=me");
                request.addHeader("Accept", "application/json");
                request.addHeader("Content-type", "application/json");
                httpResponse = httpClient.execute(request);

                Header cookieString = httpResponse.getHeaders("Set-Cookie")[0];
                xsrfToken = cookieString.getElements()[0].getValue()
                ;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Call API without authentication and return status code.
     * @param url
     * @return  The status code
     */
    public Integer apiCallStatus(String method, String url) {
        return apiCallStatus(method, url, null, null);
    }

    /**
     * Call API with authentication if credential provided.
     * @param url
     * @param username
     * @param password
     * @return  The status code
     */
    public Integer apiCallStatus(String method, String url, String username, String password) {
        retrieveToken();
        CloseableHttpClient httpClient;
        if (StringUtils.isNotEmpty(username) &&
            StringUtils.isNotEmpty(password)) {
            CredentialsProvider provider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials
                = new UsernamePasswordCredentials(username,password);
            provider.setCredentials(AuthScope.ANY, credentials);

            httpClient = HttpClientBuilder.create()
                .setDefaultCookieStore(cookieStore)
                .setDefaultCredentialsProvider(provider)
                .build();
        } else {
            httpClient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
        }

        HttpRequestBase request;
        if (method.equals("POST")) {
            request = new HttpPost(url);
        } else if (method.equals("PUT")) {
            request = new HttpPut(url);
        } else if (method.equals("DELETE")) {
            request = new HttpDelete(url);
        } else {
            request = new HttpGet(url);
        }
        request.addHeader("X-XSRF-TOKEN", xsrfToken);
        request.addHeader("Accept", "application/json");
        request.addHeader("Content-type", "application/json");
//        StringEntity entity = new StringEntity(jsonString);
//        request.setEntity(entity);
        try {
            HttpResponse response = httpClient.execute(request);
            final String content = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            return response.getStatusLine().getStatusCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method to login as admin if null provided for user details
     *
     * @param username : String : Username
     * @param password : String : Password
     */
    public void loginAs(String username, String password) {
        BaseTest.driver.get(endPointToTest + "/srv/eng/catalog.signin");
        BaseTest.driver.findElement(By.xpath("//*[@id='inputUsername']")).sendKeys(username == null ? adminUser : username);
        BaseTest.driver.findElement(By.xpath("//*[@id='inputPassword']")).sendKeys(password == null ? adminPassword : password);
        BaseTest.driver.findElement(By.cssSelector("form > button.btn-primary")).click();
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
