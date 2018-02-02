Feature: Create and publish record
        An editor creates and publishes a record
 
 Scenario: Editor creates and publishes a record
        # Login as editor
        When I navigate to "{endPointToTest}"
        And I wait 10 seconds for element having css "li.signin-dropdown" to display
	      Then I click on element having css "li.signin-dropdown"
        And I enter "editortest" into input field having xpath "//*[@id='inputUsername']"
        And I enter "editorpass" into input field having xpath "//*[@id='inputPassword']"
        And I click on element having css "form > button.btn-primary"
        And I wait 1 seconds for element having css "div.search-over" to display
        
        # Create new record
        When I navigate to "{endPointToTest}/srv/eng/catalog.edit#/create"
        Then I click on link having partial text "preferred"
        And I wait 10 seconds for element having css "div.btn-group > button.btn-success" to display
        Then I click on element having css "div.btn-group > button.btn-success"
        And I wait 10 seconds for element having css "div.gn-title" to display
        Then I clear input field having css "div.gn-title input"
        Then I enter "Metadata" into input field having css "div.gn-title input"
        Then I click on element having css "button.btn-default > i.fa-sign-out"
        And I wait for 3 sec
        
        # Add privileges to group for edit
        When I navigate to "{endPointToTest}/srv/eng/catalog.edit#/board"
        Then I click on link having text "Metadata"
        Then I click on element having css "div.gn-md-actions-btn i.fa-cog"
        And I wait 3 seconds for element having css "i.fa-key" to display
        Then I click on element having css "i.fa-key"
        Then I click on element having xpath "//*[@id="opsForm"]/table/tbody/tr[4]/td[5]/input"
        Then I click on element having css "div.gn-privileges-popup .fa-eraser"
        Then I wait 3 seconds for element having css "div.alert-success" to display
        
        # Logout as editor
        When I hover over element having css ".gn-user-info"  
        Then I wait 1 seconds for element having css ".fa-sign-out" to display
        Then I click on element having css ".fa-sign-out"
        
        # Login as reviewer
	      When I click on element having css "li.signin-dropdown"
        And I enter "reviewertest" into input field having xpath "//*[@id='inputUsername']"
        And I enter "editorpass" into input field having xpath "//*[@id='inputPassword']"
        And I click on element having css "form > button.btn-primary"
        And I wait 1 seconds for element having css "div.search-over" to display
        
        # Publish record
        When I navigate to "{endPointToTest}/srv/eng/catalog.edit#/board"
        Then I click on link having text "Metadata"
        Then I click on element having css "div.gn-md-actions-btn i.fa-cog"
        And I wait 10 seconds for element having css "i.fa-unlock" to display
        Then I click on element having css "i.fa-unlock"
        And I wait 5 seconds for element having css "div.alert-success" to display
        
        # Logout editor
        When I hover over element having css ".gn-user-info"  
        Then I wait 1 seconds for element having css ".fa-sign-out" to display
        Then I click on element having css ".fa-sign-out"
	      Then I wait 10 seconds for element having css "li.signin-dropdown" to display
