Feature: GeoNetwork Login
  As a user I should be able to login into GeoNetwork.

  Scenario: I login with valid admin credential
    Given I navigate to "{endPointToTest}"
    And I wait 5 seconds for element having css "li.signin-dropdown" to display
    And I click on element having css "li.signin-dropdown"
    And I enter "{adminUser}" into input field having xpath "//*[@id='inputUsername']"
    And I enter "{adminPassword}" into input field having xpath "//*[@id='inputPassword']"
    And I click on element having css "form > button.btn-primary"
    And I wait 3 seconds for element having css ".gn-user-info" to display
    And I hover over element having css ".gn-user-info"
    When I click on element having css ".fa-sign-out"

  Scenario: I login with invalid credential
    Given I navigate to "{endPointToTest}"
    And I wait 5 seconds for element having css "li.signin-dropdown" to display
    And I click on element having css "li.signin-dropdown"
    And I enter "{adminUser}" into input field having xpath "//*[@id='inputUsername']"
    And I enter "AnInvalidPassword" into input field having xpath "//*[@id='inputPassword']"
    And I click on element having css "form > button.btn-primary"
    And I wait 3 seconds for element having xpath "//p[@data-ng-show='signinFailure']" to display
    Then element having xpath "//p[@data-ng-show='signinFailure']" should be present

# TODO: Create guest account when enabled or not

