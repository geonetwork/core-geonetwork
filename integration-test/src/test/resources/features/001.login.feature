Feature: GeoNetwork Login
        As a user I should able to login into GeoNetwork.
 
 Scenario: I login with valid admin credential
        Given I navigate to "{endPointToTest}"
        And I wait 5 seconds for element having css "li.signin-dropdown" to display
        And I click on element having css "li.signin-dropdown"
        And I enter "{adminUser}" into input field having xpath "//*[@id='inputUsername']"
        And I enter "{adminPassword}" into input field having xpath "//*[@id='inputPassword']"
        And I click on element having css "form > button.btn-primary"
        And I wait for 1 sec
        And I hover over element having css ".gn-user-info"  
        When I click on element having css ".fa-sign-out"
