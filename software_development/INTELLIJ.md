# IntelliJ IDE

JetBrains provide a the IntelliJ IDE, a community edition is available and
is documented here.

This IDE is recommended for excellent Maven integration, and very fast build times. 
It is especially good at working with large multi-module projects such as GeoNetwork.

## Setting up

1. Open project in IntelliJ, it will create an `.idea` folder (which is covered by `.gitignore`)

2. Use *File* > *Project Structure* to confirm Java 11 is used

4. Configuration to make *Maven* tools window easier to follow:

   * *Group Modules*
   * *Always Show ArtifactId*

   ![configuration](intelij-maven-config.png)

5. Use the *Maven* tools window to:

   * *Toggle "Skip Tests" Mode*
   * Optional: Enable the `env-dev` profile for xslt development, or `env-ui` for user interface development.
   * *Execute Maven Goal*: `clean install`

## Building

1. After doing the maven build once, select menu *Build* > *Build Project*.

2. The build progress is shown in the *Build* tools window.

3. This is an incremental build, so only modified files are compiled in the future.

## Running

1. Menu *Run* > *Edit Configurations...*

2. Add new *Maven* configuration:
   
   * Working Directory: `core-geonetwork/web`
     
     This will be displayed as "gn-web-app" once selected.

   * Command Line: `jetty:run`

   * Optional: Use of profiles ``env-dev`` for XSLT development, or ``env-ui`` for user-interface development.

   ![maven jetty run configuration](intelij-maven-run.png)

3. Use **Add Run Options** to **Add before launch task**.

   Add a maven goal `proccess-resources` before launch.
   
   ![maven process-resources goal](intelij-maven-resources.png)

4. Run

   ![maven run configuration](intelij-maven-configuration.png)

5. Testing
   
   * http://localhost:8080/geonetwork
   * user: admin
   * password: admin

6. Live debugging
   
   * Debug using the run configuration created above
   * The `env-dev` allows updates while running
   * Use *Build Project* to compile, allowing methods to be rewritten interactively.
   * Use *Maven* tool window to run `mvn process-resources` to
     copy changes into the running web application.
