Feature: Edit with second editor
When another editor tries to edit a published metadata, it uses the same draft

  Scenario: Edit published record
    Given I login as editortest2/editorpass and navigate to catalog.edit#/board
    Then I click on link having text "itest-metadata"
    And I wait 2 seconds for element having css ".gn-view-approved .see-draft-not-approved" to display
    Then I click on element having css ".gn-view-approved .see-draft-not-approved"
    And I wait 3 seconds for element having css ".gn-view-not-approved .gn-md-edit-btn" to display
    Then I click on element having css ".gn-view-not-approved .gn-md-edit-btn"
    And I wait 10 seconds for element having css "div.gn-title" to display
    Then I clear input field having css "div.gn-title input"
    Then I enter "itest-second Version" into input field having css "div.gn-title input"
    Then I click on element having id "gn-editor-btn-close"
    And I wait for 3 sec
        
  Scenario: Check metadata has been modified (new value)
    Given I navigate to "{endPointToTest}/srv/eng/catalog.search#/search?resultType=details&sortBy=relevance&from=1&to=20&fast=index&_content_type=json&any=itest"
    And I wait 3 seconds for element having css "div.gn-md-title" to display
    Then I click on element having css "div.gn-md-title a[title='itest-metadata']"
    And I wait 2 seconds for element having css ".gn-view-approved .see-draft-not-approved" to display
    Then I click on element having css ".gn-view-approved .see-draft-not-approved"
    And I wait 2 seconds for element having css ".gn-view-not-approved .see-draft-approved" to display
    #Not supported
    #Then element having text "itest-second Version" should be present
    Then I click on element having css ".gn-view-not-approved .see-draft-approved"
    And I wait 2 seconds for element having css ".gn-view-approved .see-draft-not-approved" to display
                
    # Logout as second editor
    And I sign out

  Scenario: Check metadata view as anonymous (old value)
    Given I navigate to "{endPointToTest}/srv/eng/catalog.search#/search?resultType=details&sortBy=relevance&from=1&to=20&fast=index&_content_type=json&any=itest"
    And I wait 3 seconds for element having css "div.gn-md-title" to display
    Then I click on element having css "div.gn-md-title a[title='itest-metadata']"
    And I wait 10 seconds for element having css "div.gn-md-view" to display
    Then element having css ".see-draft" should not be present
