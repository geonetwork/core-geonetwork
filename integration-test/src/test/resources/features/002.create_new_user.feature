Feature: GeoNetwork Create new user
        As a user admin I should able to create new user.
 
 Scenario: I login and create a new user
        Given I navigate to "{endPointToTest}"
	      And I click on element having css "li.signin-dropdown"
        And I enter "{adminUser}" into input field having xpath "//*[@id='inputUsername']"
        And I enter "{adminPassword}" into input field having xpath "//*[@id='inputPassword']"
        And I click on element having css "form > button.btn-primary"
        And I wait for 1 sec
        And I navigate to "{endPointToTest}/srv/eng/admin.console#/organization"
        And I click on element having id "gn-btn-user-add"
        And I enter "etabeta" into input field having id "username"
        And I enter "PassTest" into input field having id "gn-user-password"
        And I enter "PassTest" into input field having id "gn-user-password2"
        And I enter "Eta" into input field having name "name"
        And I enter "Beta" into input field having name "surname"
        And I enter "etabeta@email.com" into input field having name "email"
        And I click on element having id "gn-btn-user-save"
        And I hover over element having css ".gn-user-info"
        And I click on element having css ".fa-sign-out"
        And I navigate to "{endPointToTest}"
	      And I click on element having css "li.signin-dropdown"
        And I enter "etabeta" into input field having xpath "//*[@id='inputUsername']"
        And I enter "PassTest" into input field having xpath "//*[@id='inputPassword']"
        And I click on element having css "form > button.btn-primary"
        And I wait for 1 sec
        And I hover over element having css ".gn-user-info"
        When I click on element having css ".fa-sign-out"
