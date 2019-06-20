Feature: Create Draft
When trying to edit the published record, a draft is created

  Scenario: Edit published record
    Given I login as editortest/editorpass and navigate to catalog.edit#/board
    Then I wait 10 seconds for element having css "table.gn-results-editor tr td" to display
    Then I click on link having text "itest-metadata"
    Then I click on element having css ".gn-md-edit-btn"
    And I wait 10 seconds for element having css "div.gn-title" to display
    Then I clear input field having css "div.gn-title input"
    Then I enter "itest-draft" into input field having css "div.gn-title input"
    Then I click on element having css "#gn-editor-btn-close"
    Then I wait for 3 sec

  Scenario: Check metadata is viewable by editor (new value)
    When I navigate to "{endPointToTest}/srv/eng/catalog.search#/search"
    Then I enter "itest" into input field having css "div[data-ng-search-form] div.gn-form-any input"
    Then I click on element having css "div[data-ng-search-form] button.btn-primary i.fa-search"
    Then I wait for 1 sec
    Then I click on link having text "itest-metadata"
    And I wait 2 seconds for element having css ".gn-view-approved .see-draft-not-approved" to display
    Then I click on element having css ".gn-view-approved .see-draft-not-approved"
    And I wait 2 seconds for element having css ".gn-view-not-approved .see-draft-approved" to display
    Then I click on element having css ".gn-view-not-approved .see-draft-approved"
    And I wait 2 seconds for element having css ".gn-view-approved .see-draft-not-approved" to display
    And I sign out

  Scenario: Check metadata view as anonymous (old value)
    When I navigate to "{endPointToTest}/srv/eng/catalog.search#/search"
    Then I enter "itest" into input field having css "div[data-ng-search-form] div.gn-form-any input"
    Then I click on element having css "div[data-ng-search-form] button.btn-primary i.fa-search"
    Then I wait for 1 sec
    Then I click on link having text "itest-metadata"
    And I wait 10 seconds for element having css "div.gn-md-view" to display
    Then element having css ".see-draft" should not be present
