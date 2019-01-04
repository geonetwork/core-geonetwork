Feature: Create and publish record
  An editor creates and publishes a record

  Scenario: Editor creates and publishes a record

    # Create new record
    Given I login as editortest/editorpass and navigate to catalog.edit#/create
    Then I click on link having partial text "preferred"
    And I wait 10 seconds for element having css "div.btn-group > button.btn-success" to display
    Then I click on element having css "div.btn-group > button.btn-success"
    And I wait 10 seconds for element having css "div.gn-title" to display
    Then I clear input field having css "div.gn-title input"
    Then I enter "itest-metadata" into input field having css "div.gn-title input"
    Then I click on element having css "button.btn-default > i.fa-sign-out"
    And I wait for 3 sec
        
    # Add privileges to group for edit
    When I navigate to "{endPointToTest}/srv/eng/catalog.edit#/board"
    Then I click on link having text "itest-metadata"
    Then I click on element having css "div.gn-md-actions-btn i.fa-cog"
    And I wait 3 seconds for element having css "i.fa-key" to display
    Then I click on element having css "i.fa-key"
    And I wait for 1 sec
    Then I click on element having xpath "//*[@id="opsForm"]/table/tbody/tr[4]/td[5]/input"
    And I wait for 1 sec
    Then I click on element having css "#gn-share-btn-replace"
    Then I wait 3 seconds for element having css "div.alert-success" to display
    And I sign out

    # Publish record as reviewer
    Given I login as reviewertest/editorpass and navigate to catalog.edit#/board
    Then I click on link having text "itest-metadata"
    Then I click on element having css "div.gn-md-actions-btn i.fa-cog"
    And I wait 10 seconds for element having css "i.fa-unlock" to display
    Then I click on element having css "i.fa-unlock"
    And I wait 5 seconds for element having css "div.alert-success" to display
    And I sign out
