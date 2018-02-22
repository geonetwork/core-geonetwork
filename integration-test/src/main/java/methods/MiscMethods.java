package methods;

import java.util.Arrays;

public class MiscMethods {
    public boolean valid_locator_type(String type) {
        return Arrays.asList("id", "class", "css", "name", "xpath").contains(type);
    }

    // method to validate dropdown selector
    public boolean valid_option_by(String option_by) {
        return Arrays.asList("text", "value", "index").contains(option_by);
    }

    /**
     * Method to verify locator type
     *
     * @param type : String : Locator type (id, name, class, xpath, css)
     */
    public void validateLocator(String type) throws Exception {
        if (!valid_locator_type(type)) {
            throw new Exception("Invalid locator type - " + type);
        }
    }

    /**
     * Method to verify dropdown selector (text, value or index)
     *
     * @param optionBy : String : Locator type (text, value, index)
     */
    public void validateOptionBy(String optionBy) throws Exception {
        if (!valid_option_by(optionBy)) {
            throw new Exception("Invalid option by - " + optionBy);
        }
    }
}
