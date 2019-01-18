Feature: Edit with reviewer
When a reviewer tries to edit a published metadata, it uses the same draft

  Scenario: When a reviewer tries to edit a published metadata, it uses the same draft
    Given I login as reviewertest/editorpass and navigate to catalog.edit#/board
        
    # Edit published record
    Then I click on link having text "itest-second Version"
    Then I click on element having css "a.gn-md-edit-btn"
    And I wait 10 seconds for element having css "div.gn-title" to display
    Then I clear input field having css "div.gn-title input"
    Then I enter "itest-third Version" into input field having css "div.gn-title input"
    Then I click on element having css "button.btn-primary > i.fa-save"
    And I wait for 3 sec
        
    #Check metadata has been modified
    When I navigate to "{endPointToTest}/srv/eng/catalog.search#/search"
    Then I accept alert
    Then I click on link having text "itest-third Version"
    And I wait 10 seconds for element having css "div.gn-md-view" to display
        
    # Unpublish record
    When I navigate to "{endPointToTest}/srv/eng/catalog.edit#/board"
    Then I click on link having text "itest-third Version"
    Then I click on element having css "div.gn-md-actions-btn i.fa-cog"
    And I wait 10 seconds for element having css "i.fa-lock" to display
    Then I click on element having css "i.fa-lock"
    And I wait 5 seconds for element having css "div.alert-success" to display
    And I sign out

    # Check metadata not viewed as anonymous
    When I navigate to "{endPointToTest}/srv/eng/catalog.search#/search"
    Then element having xpath "//*a[title='Integration test third Version']" should not be present
