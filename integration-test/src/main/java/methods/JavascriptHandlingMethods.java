package methods;

import env.BaseTest;

public class JavascriptHandlingMethods implements BaseTest {
    /**
     * Method to handle alert
     *
     * @param decision : String : Accept or dismiss alert
     */
    public void handleAlert(String decision) {
        if (decision.equals("accept")) {
            BaseTest.driver.switchTo().alert().accept();
        } else {
            BaseTest.driver.switchTo().alert().dismiss();
        }
    }
}
