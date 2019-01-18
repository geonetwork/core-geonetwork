Feature: Create Draft
When trying to edit the published record, a draft is created

  Scenario: When trying to edit the published record, a draft is created

    # Edit published record
    Given I login as editortest/editorpass and navigate to catalog.edit#/board
    Then I click on link having text "itest-metadata"
    Then I click on element having css ".gn-md-edit-btn"
    And I wait 10 seconds for element having css "div.gn-title" to display
    Then I clear input field having css "div.gn-title input"
    Then I enter "itest-draft" into input field having css "div.gn-title input"
    Then I click on element having css "#gn-editor-btn-close"
    And I wait for 3 sec

    #Check metadata has been modified
    When I navigate to "{endPointToTest}/srv/eng/catalog.search#/search"
    Then I click on link having text "itest-draft"
    And I wait 10 seconds for element having css "div.gn-md-view" to display
    And I sign out

    # Check metadata view as anonymous
    When I navigate to "{endPointToTest}/srv/eng/catalog.search#/search"
    Then I click on link having text "itest-draft"
    And I wait 10 seconds for element having css "div.gn-md-view" to display
