Feature: Create Draft
        When trying to edit the published record, a draft is created
 
 Scenario: When trying to edit the published record, a draft is created
        Given I navigate to "{endPointToTest}"
        # Login as editor
        Then I wait 10 seconds for element having css "li.signin-dropdown" to display
	      When I click on element having css "li.signin-dropdown"
        And I enter "editortest" into input field having xpath "//*[@id='inputUsername']"
        And I enter "editorpass" into input field having xpath "//*[@id='inputPassword']"
        And I click on element having css "form > button.btn-primary"
        And I wait 1 seconds for element having css "div.search-over" to display
        
        # Edit published record       
        When I navigate to "{endPointToTest}/srv/eng/catalog.edit#/board"
        Then I click on link having text "Metadata"
        Then I click on element having css ".gn-md-edit-btn"
        And I wait 10 seconds for element having css "div.gn-title" to display
        Then I clear input field having css "div.gn-title input"
        Then I enter "Draft" into input field having css "div.gn-title input"
        Then I click on element having css "button.btn-primary > i.fa-save"
        And I wait for 3 sec
        
        #Check metadata has been modified
        When I navigate to "{endPointToTest}/srv/eng/catalog.search#/search"
        Then I accept alert
        Then I click on link having text "Draft"
        And I wait 10 seconds for element having css "div.gn-md-view" to display
                
        # Logout as editor  
        When I hover over element having css ".gn-user-info"  
        Then I wait 1 seconds for element having css ".fa-sign-out" to display
        Then I click on element having css ".fa-sign-out"
	      Then I wait 10 seconds for element having css "li.signin-dropdown" to display
        
        # Check metadata view as anonymous
        When I navigate to "{endPointToTest}/srv/eng/catalog.search#/search"
        Then I click on link having text "Draft"
        And I wait 10 seconds for element having css "div.gn-md-view" to display
