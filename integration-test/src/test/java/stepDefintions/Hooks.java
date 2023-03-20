package stepDefintions;

import io.cucumber.java.After;
import io.cucumber.java.Scenario;

public class Hooks {
    @After("@NegativeTest")
    public void beforeScenario(Scenario scenario) {
        // System.out.println("In hooks");
        // System.out.println(scenario.getName());
        // System.out.println(scenario.getStatus());
    }
}
