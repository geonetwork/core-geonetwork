Feature: Create and publish record
  Reviewers should be able to publish and change the workflow status

  Scenario: Create new record
    Given I login as editortest/editorpass and navigate to catalog.edit#/create
    Then I click on link having partial text "preferred"
    And I wait 10 seconds for element having css "div.btn-group > button.btn-success" to display
    Then I click on element having css "div.btn-group > button.btn-success"
    And I wait 10 seconds for element having css "div.gn-title" to display
    Then I clear input field having css "div.gn-title input"
    Then I enter "itest-metadata" into input field having css "div.gn-title input"
    Then I click on element having id "gn-editor-btn-close"
    
  Scenario: Enable Workflow
    Then I wait 10 seconds for element having css "table.gn-results-editor tr td" to display
    Then I click on link having text "itest-metadata"
    Then I click on element having id "gn-button-manage-record"
    And I wait 3 seconds for element having css ".gn-md-actions-btn i.fa-code-fork" to display
    Then I click on element having css ".gn-md-actions-btn i.fa-code-fork"
    Then I wait 3 seconds for element having css "div.alert-success" to display
        
  Scenario: Add privileges to group for edit
    Then I click on element having id "gn-button-manage-record"
    And I wait 3 seconds for element having css ".gn-md-actions-btn i.fa-key" to display
    Then I click on element having css ".gn-md-actions-btn i.fa-key"
    And I wait for 1 sec
    Then I click on element having xpath "//*[@id="opsForm"]/table/tbody/tr[4]/td[5]/input"
    And I wait 5 seconds for element having css "#gn-share-btn-replace" to display
    Then I click on element having css "#gn-share-btn-replace"
    Then I wait 3 seconds for element having css "div.alert-success" to display
    Then I sign out

  Scenario: Publish record as reviewer
    Given I login as reviewertest/editorpass and navigate to catalog.edit#/board
    Then I click on link having text "itest-metadata"
    Then I click on element having id "gn-button-manage-record"
    And I wait 10 seconds for element having css ".gn-md-actions-btn i.fa-unlock" to display
    Then I click on element having css ".gn-md-actions-btn i.fa-unlock"
    Then I accept alert
    And I wait 5 seconds for element having css "div.alert-success" to display
    And I sign out
    
  Scenario: Check metadata view as anonymous
    When I navigate to "{endPointToTest}/srv/eng/catalog.search#/search"
    Then I enter "itest" into input field having css "div[data-ng-search-form] div.gn-form-any input"
    Then I click on element having css "div[data-ng-search-form] button.btn-primary i.fa-search"
    Then I wait for 1 sec
    Then I click on link having text "itest-metadata"
    And I wait 10 seconds for element having css "div.gn-md-view" to display
    Then element having css ".see-draft" should not be present
