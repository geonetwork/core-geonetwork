Feature: Edit with second editor
When another editor tries to edit a published metadata, it uses the same draft

  Scenario: When another editor tries to edit a published metadata, it uses the same draft
    # Edit published record
    Given I login as editortest2/editorpass and navigate to catalog.edit#/board
    Then I click on link having text "itest-draft"
    Then I click on element having css "a.gn-md-edit-btn"
    And I wait 10 seconds for element having css "div.gn-title" to display
    Then I clear input field having css "div.gn-title input"
    Then I enter "itest-second Version" into input field having css "div.gn-title input"
    Then I click on element having css "#gn-editor-btn-close"
    And I wait for 3 sec
        
    #Check metadata has been modified
    When I navigate to "{endPointToTest}/srv/eng/catalog.search#/search"
    Then I click on link having text "itest-second Version"
    And I wait 10 seconds for element having css "div.gn-md-view" to display
                
    # Logout as second editor
    And I sign out

    # Check metadata view as anonymous
    When I navigate to "{endPointToTest}/srv/eng/catalog.search#/search"
    Then I click on link having text "itest-second Version"
    And I wait 10 seconds for element having css "div.gn-md-view" to display
