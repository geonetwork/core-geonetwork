package stepDefintions;

import java.io.IOException;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import env.BaseTest;
import env.Env;
import junit.framework.Assert;
import methods.TestCaseFailed;

import static org.junit.Assert.assertEquals;

public class PredefinedStepDefinitions implements BaseTest {
    // Navigation Steps

    // step to assert javascript pop-up alert text
    @Then("^I should see alert text as \"(.*?)\"$")
    public void check_alert_text(String actualValue) throws TestCaseFailed {
        assertionObj.checkAlertText(actualValue);
    }

    // check checkbox steps
    @Then("^I check the checkbox having (.+) \"(.*?)\"$")
    public void check_checkbox(String type, String accessName) throws Exception {
        miscmethodObj.validateLocator(type);
        inputObj.checkCheckbox(type, accessName);
    }

    // step to check attribute value
    @Then("^element having (.+) \"([^\"]*)\" should\\s*((?:not)?)\\s+have attribute \"(.*?)\" with value \"(.*?)\"$")
    public void check_element_attribute(String type, String accessName, String present, String attrb, String value)
            throws Exception {
        miscmethodObj.validateLocator(type);
        assertionObj.checkElementAttribute(type, attrb, value, accessName, present.isEmpty());
    }

    // step to check element enabled or not
    @Then("^element having (.+) \"([^\"]*)\" should\\s*((?:not)?)\\s+be (enabled|disabled)$")
    public void check_element_enable(String type, String accessName, String present, String state) throws Exception {
        miscmethodObj.validateLocator(type);
        boolean flag = state.equals("enabled");
        if (!present.isEmpty()) {
            flag = !flag;
        }
        assertionObj.checkElementEnable(type, accessName, flag);
    }

    // Switch between windows

    // step to check element partial text
    @Then("^element having (.+) \"([^\"]*)\" should\\s*((?:not)?)\\s+have partial text as \"(.*?)\"$")
    public void check_element_partial_text(String type, String accessName, String present, String value)
            throws Exception {
        miscmethodObj.validateLocator(type);
        assertionObj.checkElementPartialText(type, value, accessName, present.isEmpty());
    }

    // steps to check link presence
    @Then("^link having text \"(.*?)\" should\\s*((?:not)?)\\s+be present$")
    public void check_element_presence(String accessName, String present) throws TestCaseFailed, Exception {
        assertionObj.checkElementPresence("linkText", accessName, present.isEmpty());
    }

    // step to check element present or not
    @Then("^element having (.+) \"(.*?)\" should\\s*((?:not)?)\\s+be present$")
    public void check_element_presence(String type, String accessName, String present) throws Exception {
        miscmethodObj.validateLocator(type);
        assertionObj.checkElementPresence(type, accessName, present.isEmpty());
    }

    // step to check element text
    @Then("^element having (.+) \"([^\"]*)\" should\\s*((?:not)?)\\s+have text as \"(.*?)\"$")
    public void check_element_text(String type, String accessName, String present, String value) throws Exception {
        miscmethodObj.validateLocator(type);
        assertionObj.checkElementText(type, value, accessName, present.isEmpty());
    }

    // Switch between frame

    // steps to check partail link presence
    @Then("^link having partial text \"(.*?)\" should\\s*((?:not)?)\\s+be present$")
    public void check_partial_element_presence(String accessName, String present) throws TestCaseFailed, Exception {
        assertionObj.checkElementPresence("partialLinkText", accessName, present.isEmpty());
    }

    // step to check element partial text
    @Then("^I should\\s*((?:not)?)\\s+see page title having partial text as \"(.*?)\"$")
    public void check_partial_text(String present, String partialTextTitle) throws TestCaseFailed {
        // System.out.println("Present :" + present.isEmpty());
        assertionObj.checkPartialTitle(partialTextTitle, present.isEmpty());
    }

    // To interact with browser

    /**
     * page title checking
     *
     * @param present
     *            :
     * @param title
     *            :
     */
    @Then("^I should\\s*((?:not)?)\\s+see page title as \"(.+)\"$")
    public void check_title(String present, String title) throws TestCaseFailed {
        // System.out.println("Present :" + present.isEmpty());
        assertionObj.checkTitle(title, present.isEmpty());
    }

    // clear input field steps
    @Then("^I clear input field having (.+) \"([^\"]*)\"$")
    public void clear_text(String type, String accessName) throws Exception {
        miscmethodObj.validateLocator(type);
        inputObj.clearText(type, accessName);
    }

    // click on web element
    @Then("^I click on element having (.+) \"(.*?)\"$")
    public void click(String type, String accessName) throws Exception {
        miscmethodObj.validateLocator(type);
        clickObj.click(type, accessName);
    }

    // zoom in/out page

    // Forcefully click on element
    @Then("^I forcefully click on element having (.+) \"(.*?)\"$")
    public void click_forcefully(String type, String accessName) throws Exception {
        miscmethodObj.validateLocator(type);
        clickObj.clickForcefully(type, accessName);
    }

    // steps to click on link
    @Then("^I click on link having text \"(.*?)\"$")
    public void click_link(String accessName) {
        clickObj.click("linkText", accessName);
    }

    // zoom out webpage till necessary element displays

    // Step to click on partial link
    @Then("^I click on link having partial text \"(.*?)\"$")
    public void click_partial_link(String accessName) {
        clickObj.click("partialLinkText", accessName);
    }

    // reset webpage view use

    // Step to close the browser
    @Then("^I close browser$")
    public void close_browser() {
        navigationObj.closeDriver();
    }

    // scroll webpage

    // Close new window
    @Then("^I close new window$")
    public void close_new_window() {
        navigationObj.closeNewWindow();
    }

    // scroll webpage to specific element

    // deselect option by text/value from multiselect
    @Then("^I deselect \"(.*?)\" option by (.+) from multiselect dropdown having (.+) \"(.*?)\"$")
    public void deselect_option_from_multiselect_dropdown(String option, String optionBy, String type,
            String accessName) throws Exception {
        miscmethodObj.validateLocator(type);
        miscmethodObj.validateOptionBy(optionBy);
        inputObj.deselectOptionFromDropdown(type, optionBy, option, accessName);
    }

    // hover over element

    // deselect option by index from multiselect
    @Then("^I deselect (\\d+) option by index from multiselect dropdown having (.+) \"(.*?)\"$")
    public void deselect_option_from_multiselect_dropdown_by_index(String option, String type, String accessName)
            throws Exception {
        miscmethodObj.validateLocator(type);
        inputObj.deselectOptionFromDropdown(type, "selectByIndex", option, accessName);
    }

    // Assertion steps

    // Steps to dismiss java script
    @Then("^I dismiss alert$")
    public void dismiss_alert() {
        javascriptObj.handleAlert("dismiss");
    }

    // double click on web element
    @Then("^I double click on element having (.+) \"(.*?)\"$")
    public void double_click(String type, String accessValue) throws Exception {
        miscmethodObj.validateLocator(type);
        clickObj.doubleClick(type, accessValue);
    }

    // enter text into input field steps
    @Then("^I enter \"([^\"]*)\" into input field having (.+) \"([^\"]*)\"$")
    public void enter_text(String text, String type, String accessName) throws Exception {
        text = text.replace("{adminPassword}", adminPassword);
        text = text.replace("{adminUser}", adminUser);
        miscmethodObj.validateLocator(type);
        inputObj.enterText(type, text, accessName);
    }

    // Step to handle java script
    @Then("^I accept alert$")
    public void handle_alert() {
        javascriptObj.handleAlert("accept");
    }

    // Note: Doesn't work on Windows firefox
    @Then("^I hover over element having (.+) \"(.*?)\"$")
    public void hover_over_element(String type, String accessName) throws Exception {
        miscmethodObj.validateLocator(type);
        navigationObj.hoverOverElement(type, accessName);
    }

    // step to assert checkbox is checked or unchecked
    @Then("^checkbox having (.+) \"(.*?)\" should be (checked|unchecked)$")
    public void is_checkbox_checked(String type, String accessName, String state) throws Exception {
        miscmethodObj.validateLocator(type);
        final boolean flag = state.equals("checked");
        assertionObj.isCheckboxChecked(type, accessName, flag);
    }

    // step to select dropdown list
    @Then("^option \"(.*?)\" by (.+) from dropdown having (.+) \"(.*?)\" should be (selected|unselected)$")
    public void is_option_from_dropdown_selected(String option, String by, String type, String accessName, String state)
            throws Exception {
        miscmethodObj.validateLocator(type);
        final boolean flag = state.equals("selected");
        assertionObj.isOptionFromDropdownSelected(type, by, option, accessName, flag);
    }

    // steps to assert option by text from radio button group selected/unselected
    @Then("^option \"(.*?)\" by (.+) from radio button group having (.+) \"(.*?)\" should be (selected|unselected)$")
    public void is_option_from_radio_button_group_selected(String option, String attrb, String type, String accessName,
            String state) throws Exception {
        miscmethodObj.validateLocator(type);
        final boolean flag = state.equals("selected");
        assertionObj.isOptionFromRadioButtonGroupSelected(type, attrb, option, accessName, flag);
    }

    // steps to assert radio button checked or unchecked
    @Then("^radio button having (.+) \"(.*?)\" should be (selected|unselected)$")
    public void is_radio_button_selected(String type, String accessName, String state) throws Exception {
        miscmethodObj.validateLocator(type);
        final boolean flag = state.equals("selected");
        assertionObj.isRadioButtonSelected(type, accessName, flag);
    }

    // step to maximize browser
    @Then("^I maximize browser window$")
    public void maximize_browser() {
        navigationObj.maximizeBrowser();
    }

    // Step to navigate backward
    @Then("^I navigate back")
    public void navigate_back() {
        navigationObj.navigate("back");
    }

    // Step to navigate forward
    @Then("^I navigate forward")
    public void navigate_forward() {
        navigationObj.navigate("forward");
    }

    // Step to navigate to specified URL
    @Then("^I navigate to \"([^\"]*)\"$")
    public void navigate_to(String link) {
        navigationObj.navigateTo(link.replace("{endPointToTest}", endPointToTest));
    }

    @Given("^I login as admin$")
    public void i_login_as_admin() throws Throwable {
        navigationObj.loginAs(null, null);
    }

    @Given("^I login as admin and navigate to (.*)$")
    public void i_login_as_admin_and_navigate(String page) throws Throwable {
        navigationObj.loginAs(null, null);
        navigate_to(endPointToTest + "/srv/eng/" + page);
    }

    @Given("^I login as (.*)/(.*) and navigate to (.*)")
    public void i_login_as_user_and_navigate(String username, String password, String page) throws Throwable {
        navigationObj.loginAs(username, password);
        navigate_to(endPointToTest + "/srv/eng/" + page);
    }

    @And("^I sign out$")
    public void i_sign_out() throws Throwable {
        navigationObj.navigateTo(endPointToTest + "/signout");
    }

    @Then("^I check API operation (GET|POST|DELETE|PUT) ([^ ]*) and expect status ([0-9]{3})$")
    public void apicheck_status(String method, String url, int status) throws Exception {
        apicheck_auth_status(method, url, null, null, status);
    }

    @Then("^I check API operation (GET|POST|DELETE|PUT) ([^ ]*) as (.*)/(.*) and expect status ([0-9]{3})$")
    public void apicheck_auth_status(String method, String url, String username, String password, int status) throws Exception {
        Integer statusReturned = navigationObj.apiCallStatus(method, endPointToTest + "/srv/api/0.1" + url, username, password);
        Assert.assertEquals((int) statusReturned, status);
        //             .andExpect(jsonPath("$[*].name", hasItem("all")))
    }


    @Then("^I print configuration$")
    public void print_config() {
        configObj.printDesktopConfiguration();
    }

    // Input steps

    // steps to refresh page
    @Then("^I refresh page$")
    public void refresh_page() {
        driver.navigate().refresh();
    }

    @Then("^I reset page view$")
    public void reset_page_zoom() {
        navigationObj.zoomInOut("reset");
    }

    // step to resize browser
    @Then("^I resize browser window size to width (\\d+) and height (\\d+)$")
    public void resize_browser(int width, int heigth) {
        navigationObj.resizeBrowser(width, heigth);
    }

    @Then("^I scroll to (top|end) of page$")
    public void scroll_page(String to) throws Exception {
        navigationObj.scrollPage(to);
    }

    @Then("^I scroll to element having (.+) \"(.*?)\"$")
    public void scroll_to_element(String type, String accessName) throws Exception {
        miscmethodObj.validateLocator(type);
        navigationObj.scrollToElement(type, accessName);
    }

    // select option by text/value from dropdown
    @Then("^I select \"(.*?)\" option by (.+) from dropdown having (.+) \"(.*?)\"$")
    public void select_option_from_dropdown(String option, String optionBy, String type, String accessName)
            throws Exception {
        miscmethodObj.validateLocator(type);
        miscmethodObj.validateOptionBy(optionBy);
        inputObj.selectOptionFromDropdown(type, optionBy, option, accessName);
    }

    // select option by index from dropdown
    @Then("^I select (\\d+) option by index from dropdown having (.+) \"(.*?)\"$")
    public void select_option_from_dropdown_by_index(String option, String type, String accessName) throws Exception {
        miscmethodObj.validateLocator(type);
        inputObj.selectOptionFromDropdown(type, "selectByIndex", option, accessName);
    }

    // select option by text/value from multiselect
    @Then("^I select \"(.*?)\" option by (.+) from multiselect dropdown having (.+) \"(.*?)\"$")
    public void select_option_from_multiselect_dropdown(String option, String optionBy, String type, String accessName)
            throws Exception {
        miscmethodObj.validateLocator(type);
        miscmethodObj.validateOptionBy(optionBy);
        inputObj.selectOptionFromDropdown(type, optionBy, option, accessName);
    }

    // step to select option from mutliselect dropdown list
    /*
     * @Then("^I select all options from multiselect dropdown having (.+) \"(.*?)\"$"
     * ) public void select_all_option_from_multiselect_dropdown(String type,String
     * accessName) throws Exception { miscmethod.validateLocator(type); //inputObj.
     * //select_all_option_from_multiselect_dropdown(type, access_name) }
     */

    // select option by index from multiselect
    @Then("^I select (\\d+) option by index from multiselect dropdown having (.+) \"(.*?)\"$")
    public void select_option_from_multiselect_dropdown_by_index(String option, String type, String accessName)
            throws Exception {
        miscmethodObj.validateLocator(type);
        inputObj.selectOptionFromDropdown(type, "selectByIndex", option, accessName);
    }

    // steps to select option by text from radio button group
    @Then("^I select \"(.*?)\" option by (.+) from radio button group having (.+) \"(.*?)\"$")
    public void select_option_from_radio_btn_group(String option, String by, String type, String accessName)
            throws Exception {
        miscmethodObj.validateLocator(type);
        // miscmethodObj.validateOptionBy(optionBy);
        inputObj.selectOptionFromRadioButtonGroup(type, option, by, accessName);
    }

    // step to select radio button
    @Then("^I select radio button having (.+) \"(.*?)\"$")
    public void select_radio_button(String type, String accessName) throws Exception {
        miscmethodObj.validateLocator(type);
        inputObj.selectRadioButton(type, accessName);
    }

    // Step to switch to frame by web element
    @Then("^I switch to frame having (.+) \"(.*?)\"$")
    public void switch_frame_by_element(String method, String value) {
        navigationObj.switchFrame(method, value);
    }

    // step to switch to main content
    @Then("^I switch to main content$")
    public void switch_to_default_content() {
        navigationObj.switchToDefaultContent();
    }

    // Switch to new window
    @Then("^I switch to new window$")
    public void switch_to_new_window() {
        navigationObj.switchToNewWindow();
    }

    // Click element Steps

    // Switch to old window
    @Then("^I switch to previous window$")
    public void switch_to_old_window() {
        navigationObj.switchToOldWindow();
    }

    // Switch to new window by window title
    @Then("^I switch to window having title \"(.*?)\"$")
    public void switch_to_window_by_title(String windowTitle) throws Exception {
        navigationObj.switchToWindowByTitle(windowTitle);
    }

    @Then("^I take screenshot$")
    public void take_screenshot() throws IOException {
        screenshotObj.takeScreenShot();
    }

    // steps to toggle checkbox
    @Then("^I toggle checkbox having (.+) \"(.*?)\"$")
    public void toggle_checkbox(String type, String accessName) throws Exception {
        miscmethodObj.validateLocator(type);
        inputObj.toggleCheckbox(type, accessName);
    }

    // uncheck checkbox steps
    @Then("^I uncheck the checkbox having (.+) \"(.*?)\"$")
    public void uncheck_checkbox(String type, String accessName) throws Exception {
        miscmethodObj.validateLocator(type);
        inputObj.uncheckCheckbox(type, accessName);
    }

    // Progress methods

    // step to unselect option from mutliselect dropdown list
    @Then("^I deselect all options from multiselect dropdown having (.+) \"(.*?)\"$")
    public void unselect_all_option_from_multiselect_dropdown(String type, String accessName) throws Exception {
        miscmethodObj.validateLocator(type);
        inputObj.unselectAllOptionFromMultiselectDropdown(type, accessName);
    }

    // wait for specific period of time
    @Then("^I wait for (\\d+) sec$")
    public void wait(String time) throws NumberFormatException, InterruptedException {
        progressObj.wait(time);
    }

    // wait for specific element to enable for specific period of time
    @Then("^I wait (\\d+) seconds for element having (.+) \"(.*?)\" to be enabled$")
    public void wait_for_ele_to_click(String duration, String type, String accessName) throws Exception {
        miscmethodObj.validateLocator(type);
        progressObj.waitForElementToClick(type, accessName, duration);
    }

    // JavaScript handling steps

    // wait for specific element to display for specific period of time
    @Then("^I wait (\\d+) seconds for element having (.+) \"(.*?)\" to display$")
    public void wait_for_ele_to_display(String duration, String type, String accessName) throws Exception {
        miscmethodObj.validateLocator(type);
        progressObj.waitForElementToDisplay(type, accessName, duration);
    }

    // steps to zoom in page
    @Then("^I zoom in page$")
    public void zoom_in() {
        navigationObj.zoomInOut("ADD");
    }

    // Screen shot methods

    // steps to zoom out page
    @Then("^I zoom out page$")
    public void zoom_out() {
        navigationObj.zoomInOut("SUBTRACT");
    }

    // Configuration steps

    // steps to zoom out till element displays
    @Then("^I zoom out page till I see element having (.+) \"(.*?)\"$")
    public void zoom_till_element_display(String type, String accessName) throws Exception {
        miscmethodObj.validateLocator(type);
        navigationObj.zoomInOutTillElementDisplay(type, "substract", accessName);
    }
}
