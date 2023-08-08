package stepDefintions;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "classpath:features",
    plugin = {
        "pretty",
        "html:target/cucumberHtmlReport.html"
    }
// pretty:target/cucumber-json-report.json
)
public class RunCukeTest {
}
