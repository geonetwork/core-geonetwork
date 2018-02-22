GeoNetwork test build
=================

Based on [selenium-cucumber](https://github.com/selenium-cucumber/selenium-cucumber-java)


Running test
--------------

Go to your project directory from terminal and hit following command

* `mvn test -Dbrowser=phantomjs -Dphantomjs.binary.path=$DRIVERS_BIN/phantomjs -DendPointToTest=http://localhost:8080/geonewtwork`

* The url to test could by defined in **src/test/resources/system.properties** by changing the property **endPointToTest.url** or provided to the mvn command with the parameter `-DendPointToTest=YOUR_URL`

* By default the mvn command run all the test in the folder **src/test/resources/features**. To specify a different folder use the parameter `-Dcucumber.options="your path"`

* To specify a path for the reports use the option `-Dcucumber.options="--plugin html:./report` in case you need to specify your path, the path must go after `-Dcucumber.options="--plugin html:./report your path`


PhantomJS driver
-----------------------

The purpose of this software it's to run automated tests after builds, so it's used the phantomJS driver. 

The right binary could be downloaded from [here](http://phantomjs.org).

Gecko driver
-----------------------

If you want to see the running tests on firefox you can also use a geckodriver by running the mvn command with the parameters:

`mvn test -Dbrowser=firefox -Dwebdriver.gecko.driver=$DRIVERS_BIN/geckodriver`

Download the geckodriver from [here](https://github.com/mozilla/geckodriver/releases)


Writing a test
--------------

The cucumber features goes in the `features` library and should have the ".feature" extension.