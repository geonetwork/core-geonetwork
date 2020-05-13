Feature: GeoNetwork Exit
  As a user at some point I will close the browser...

  Scenario: Closing the browser
    Given I navigate to "{endPointToTest}"
    And I wait for 1 sec
    Then I close browser
