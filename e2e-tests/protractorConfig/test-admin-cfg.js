// Protractor configuration file
exports.config = {
  ${baseProtractorConfig},

  // Selector for the element housing the angular app - this defaults to
  // body, but is necessary if ng-app is on a descendant of <body>
  rootElement: 'body',

  // A callback function called once protractor is ready and available, and
  // before the specs are executed
  // You can specify a file containing code to run by setting onPrepare to
  // the filename string.
  onPrepare: function() {
    // The require statement must be down here, since jasmine-reporters
    // needs jasmine to be in the global and protractor does not guarantee
    // this until inside the onPrepare function.
    require('jasmine-reporters');
    jasmine.getEnv().addReporter(
      new jasmine.JUnitXmlReporter('target/e2e-test-output', true, true));

    browser.driver.get('http://localhost:${appPort}/geonetwork/srv/eng/catalog.signin');
        element(by.model('signinUsername')).sendKeys('admin');
        element(by.model('signinPassword')).sendKeys('admin');
        element(by.css('button')).click();

        // Login takes some time, so wait until it's done.
        // For the test app's login, we know it's done when it redirects to
        // index.html.
        browser.driver.wait(function() {
          return browser.driver.getCurrentUrl().then(function(url) {
            return url;
          });
        });
  },

  // ----- What tests to run -----
  //
  // Spec patterns are relative to the location of this config.
  specs: [
    '${project.basedir}/src/test/protractor/**/admin.*.spec.js',
  ],
};
