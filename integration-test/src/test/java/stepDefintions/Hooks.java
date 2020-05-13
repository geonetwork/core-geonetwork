package stepDefintions;

import cucumber.api.Scenario;
import cucumber.api.java.After;

public class Hooks {
    @After("@NegativeTest")
    public void beforeScenario(Scenario scenario) {
        // System.out.println("In hooks");
        // System.out.println(scenario.getName());
        // System.out.println(scenario.getStatus());
    }
}
