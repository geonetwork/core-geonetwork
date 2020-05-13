Feature: Edit with reviewer
When a reviewer tries to edit a published metadata, it uses the same draft

    
  Scenario: Edit published record
    Given I login as reviewertest/editorpass and navigate to catalog.edit#/board
        
    Then I click on link having text "itest-metadata"
    Then I click on element having css "a.gn-md-edit-btn"
    And I wait 10 seconds for element having css "div.gn-title" to display
    Then I clear input field having css "div.gn-title input"
    Then I enter "itest-third Version" into input field having css "div.gn-title input"
    Then I click on element having id "gn-editor-btn-save"
    And I wait for 3 sec
        
  Scenario: Check metadata has been modified (new value)
    When I navigate to "{endPointToTest}/srv/eng/catalog.search#/search?resultType=details&sortBy=relevance&from=1&to=20&fast=index&_content_type=json&any=itest"
    Then I accept alert
    Then I click on link having text "itest-metadata"
    And I wait 4 seconds for element having css ".gn-view-approved .see-draft-not-approved" to display
    Then I click on element having css ".gn-view-approved .see-draft-not-approved"
    And I wait 4 seconds for element having css ".gn-view-not-approved .see-draft-approved" to display
    Then I click on element having css ".gn-view-not-approved .see-draft-approved"
    And I wait 4 seconds for element having css ".gn-view-approved .see-draft-not-approved" to display
    And I sign out
    
  Scenario: Approve record as reviewer
    Given I login as reviewertest/editorpass and navigate to catalog.edit#/board
    Then I click on link having text "itest-metadata"
    And I wait 5 seconds for element having css ".gn-view-approved .see-draft-not-approved" to display
    Then I click on element having css ".gn-view-approved .see-draft-not-approved"
    Then I click on element having css ".gn-view-not-approved #gn-button-manage-record"
    And I wait 3 seconds for element having css ".gn-view-not-approved .gn-md-actions-btn i.fa-code-fork" to display
    Then I click on element having css ".gn-view-not-approved .gn-md-actions-btn i.fa-code-fork"
    Then I wait 3 seconds for element having id "gn-workflow-status-change" to display
    Then I wait 3 seconds for element having css "#gn-workflow-status-change input[type='radio']" to be enabled
    Then I select "2" option by value from radio button group having name "status"
    Then I click on element having id "gn-record-status-accept"
    And I wait 5 seconds for element having css "div.alert-success" to display
    And I sign out
    
  Scenario: Check metadata view as anonymous (new value)
    Given I navigate to "{endPointToTest}/srv/eng/catalog.search#/search?resultType=details&sortBy=relevance&from=1&to=20&fast=index&_content_type=json&any=itest"
    Then I click on link having text "itest-third Version"
    And I wait 10 seconds for element having css "div.gn-md-view" to display
    Then element having css ".see-draft" should not be present
        
  Scenario: Unpublish record
    Given I login as reviewertest/editorpass and navigate to catalog.edit#/board
    Then I click on link having text "itest-third Version"
    Then I click on element having id "gn-button-manage-record"
    And I wait 10 seconds for element having css "i.fa-lock" to display
    Then I click on element having css "i.fa-lock"
    And I wait 5 seconds for element having css "div.alert-success" to display
    And I sign out

  Scenario: Check metadata not viewed as anonymous
    When I navigate to "{endPointToTest}/srv/eng/catalog.search#/search"
    Then I enter "itest" into input field having css "div[data-ng-search-form] div.gn-form-any input"
    Then I click on element having css "div[data-ng-search-form] button.btn-primary i.fa-search"
    Then element having xpath "//*a[title='itest-third Version']" should not be present
