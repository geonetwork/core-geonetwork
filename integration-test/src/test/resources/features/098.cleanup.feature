Feature: Cleanup Draft Tests
        Remove everything we created for the tests
 
 Scenario: Remove everything we created for the tests
        # Login as admin
        Given I navigate to "{endPointToTest}"
	      When I click on element having css "li.signin-dropdown"
        And I enter "{adminUser}" into input field having xpath "//*[@id='inputUsername']"
        And I enter "{adminPassword}" into input field having xpath "//*[@id='inputPassword']"
        And I click on element having css "form > button.btn-primary"
        And I wait 1 seconds for element having css "div.search-over" to display
        
        # Remove all records
        When I navigate to "{endPointToTest}/srv/eng/catalog.edit#/board"
        Then I click on element having css "div.gn-editor-board button.dropdown-toggle"
        Then I click on element having xpath "//a[@data-ng-click='selectAll()']"
        Then I click on element having css "div.gn-selection-actions"
        Then I click on element having css "div.gn-selection-actions i.fa-times"
        Then I accept alert
        And I wait 3 seconds for element having css "div.alert-warning" to display
        
        # Remove editors
        When I navigate to "{endPointToTest}/srv/eng/admin.console#/organization"
        Then I click on link having partial text "Edi Thor"
        Then I click on element having css "button.btn-danger"
        And I wait for 1 sec
        Then I click on link having partial text "Revi Ewer"
        Then I click on element having css "button.btn-danger"
        And I wait for 1 sec
        Then I click on link having partial text "Edi Thor"
        Then I click on element having css "button.btn-danger"
        
        # Logout   
        When I hover over element having css ".gn-user-info"  
        Then I wait 1 seconds for element having css ".fa-sign-out" to display
        Then I click on element having css ".fa-sign-out"
