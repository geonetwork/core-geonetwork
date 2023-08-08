package stepDefintions;

import env.BaseTest;
import io.cucumber.java.BeforeAll;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;

public class UserStepDefinitions implements BaseTest {

//    private BrowserWebDriverContainer browserContainer =
//        new BrowserWebDriverContainer()
//        .withCapabilities(new ChromeOptions())
//        .withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL, new File("build"));

    @BeforeAll
    public static void beforeScenario() {
        DockerComposeContainer container =
            new DockerComposeContainer(new File("src/test/resources/docker-compose.yml"))
                .withLocalCompose(true)
                .withTailChildContainers(true)
                .withExposedService("geonetwork", 8080, Wait.forListeningPort());

        container.start();
//        browserContainer.start();
    }
}
