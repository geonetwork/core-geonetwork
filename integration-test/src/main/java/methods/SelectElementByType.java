package methods;

import org.openqa.selenium.By;

public class SelectElementByType {
    /**
     * Method to select element 'by' type
     *
     * @param type : String : 'By' type
     * @param access_name : String : Locator value
     * @return By
     */
    public By getelementbytype(String type, String access_name) {
        switch (type) {
        case "id":
            return By.id(access_name);
        case "name":
            return By.name(access_name);
        case "class":
            return By.className(access_name);
        case "xpath":
            return By.xpath(access_name);
        case "css":
            return By.cssSelector(access_name);
        case "linkText":
            return By.linkText(access_name);
        case "partialLinkText":
            return By.partialLinkText(access_name);
        case "tagName":
            return By.tagName(access_name);
        default:
            return null;

        }
    }
}
