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
        And I click on link having partial text "Eta Beta"
        And I click on element having css "button.btn-danger"  
        And I hover over element having css ".gn-user-info"  
        When I click on element having css ".fa-sign-out"
