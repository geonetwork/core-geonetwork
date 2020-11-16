# GeoNetwork test build


Based on [selenium-cucumber](https://github.com/selenium-cucumber/selenium-cucumber-java)


## Running tests

Download the appropriate driver for the browser you'd like to use (see list below). Ensure you get the correct version to match the browser on your system. Copy the driver into the `integration-test` directory and make sure that it is executable.

With GeoNetwork running and accessible on http://localhost:8080/geonetwork, go to the `integration-test` directory in terminal and run following command:

* With Chrome 

`mvn clean test -Dbrowser=chrome`

* With phantomJS 

`mvn clean test -Dbrowser=phantomjs`

* With firefox

`mvn test -Dbrowser=firefox`

Additional configuration parameters:

* If you are not testing GeoNetwork running on localhost:8080 then you can change the property **endPointToTest.url** in `src/test/resources/system.properties` or by passing the parameter `-DendPointToTest=YOUR_URL` to the mvn command.

* By default, the mvn command will run all the tests in the folder `src/test/resources/features`. To specify a different folder use the parameter `-Dcucumber.options="your path"`

* To display the output in the console use the parameter `-Dcucumber.options="--plugin pretty"`

* By default, an HTML report it is created in the folder `./target/cucumberHtmlReport`. To specify a path for the reports use the option `-Dcucumber.options="--plugin html:./report` in case you need to specify your path, the path must go after `-Dcucumber.options="--plugin html:./report your path`



## Driver downloads:

PhantomJS: (http://phantomjs.org).

Gecko (Firefox): (https://github.com/mozilla/geckodriver/releases)

Chrome: (http://chromedriver.chromium.org/downloads).


## Writing a test

The cucumber features go in the `features` library and should have the ".feature" extension.

Documentation about the syntax supported can be found in [Canned Steps](./doc/canned_steps.md).

### The filename of each test follows these conventions:

*WXX.short_human_readable_description_of_the_test.features*

Where **W** is the group test id and **XX** is the test unique numeric identifier in the group. The tests are executed in ascending order on the value of **WXX**. 

**WXX** is assigned following this basic rules:

* **XX** value is a numeric value between **01** and **97** not used by any other test.
* A test **XX** comes after a **YY** if the data created with **YY** is necessary for **XX**. 
* The data changes introduced with **XX** must not corrupt any test **YY** > **XX**.
* A new group **W** is created when the previous one is full or when special conditions on data are needed.
* The data created with **XX** must be removed in the **98** test following the descending order in id numeration.

The last two tests for each group **W** are:

*W98.cleanup.feature* that removes all the data created from the tests in the group. It's important to keep the database clean for other groups of tests.
*W99.exit.feature* closes the browser.

### A test should be written following these conventions:

* The *feature* section is the extended human readable description of the test.
* The *scenario* section describes the steps involved in the test and the data produced.
* Avoid absolute urls and admin credentials in tests (use variables *{endPointToTest}, {adminUser}, {adminPassword}*). 
* It's always recommended , to write resilient tests, to use css identifiers over others systems (xpath, classes). Consider to eventually introduce new ones in GN code.
* The data created must be removed in the **98** test following the descending order in id numeration.


