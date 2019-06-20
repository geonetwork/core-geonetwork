# GeoNetwork test build


Based on [selenium-cucumber](https://github.com/selenium-cucumber/selenium-cucumber-java)


## Running test

Go to your project directory from terminal and hit following command

* With Chrome 

`mvn test -Dbrowser=chrome -Dwebdriver.chrome.driver=chromedriver -DendPointToTest=http://localhost:8080/geonetwork`

* With phantomJS 

`mvn test -Dbrowser=phantomjs -Dphantomjs.binary.path=$DRIVERS_BIN/phantomjs -DendPointToTest=http://localhost:8080/geonetwork`


* The url to test could by defined in **src/test/resources/system.properties** by changing the property **endPointToTest.url** or provided to the mvn command with the parameter `-DendPointToTest=YOUR_URL`

* By default the mvn command run all the test in the folder **src/test/resources/features**. To specify a different folder use the parameter `-Dcucumber.options="your path"`

* To specify a path for the reports use the option `-Dcucumber.options="--plugin html:./report` in case you need to specify your path, the path must go after `-Dcucumber.options="--plugin html:./report your path`



## PhantomJS driver

The purpose of this software it's to run automated tests after builds, so it's used the phantomJS driver. 

The right binary could be downloaded from [here](http://phantomjs.org).

## Gecko driver

If you want to see the running tests on firefox you can also use a geckodriver by running the mvn command with the parameters:

`mvn test -Dbrowser=firefox -Dwebdriver.gecko.driver=$DRIVERS_BIN/geckodriver`

Download the geckodriver from [here](https://github.com/mozilla/geckodriver/releases)

## Chrome driver

Download Chrome driver from [here](http://chromedriver.chromium.org/downloads).


## Writing a test

The cucumber features goes in the `features` library and should have the ".feature" extension.

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


