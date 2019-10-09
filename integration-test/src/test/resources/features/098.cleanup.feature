Feature: Cleanup Draft Tests
  Remove everything we created for the tests

  Scenario: Remove everything we created for the tests
    # Login as admin
    Given I login as admin/admin and navigate to catalog.edit#/board

  Scenario: Remove all records
    Then I wait 3 seconds for element having css "div.gn-editor-board .gn-select button.dropdown-toggle" to display
    Then I click on element having css "div.gn-editor-board .gn-select button.dropdown-toggle"
    Then I click on element having xpath "//a[@data-ng-click='selectAll()']"
    Then I click on element having css "div.gn-selection-actions"
    Then I click on element having css "div.gn-selection-actions i.fa-times"
    Then I accept alert
    And I wait 3 seconds for element having css "div.alert-warning" to display
        
  Scenario: Remove editors
    When I navigate to "{endPointToTest}/srv/eng/admin.console#/organization"
    Then I click on link having partial text "Edi Thor"
    Then I click on element having css "button.btn-danger"
    And I accept alert
    #Need to refresh because DOM elements still not garbage collected (chrome)
    Then I refresh page
    Then I click on link having partial text "Revi Ewer"
    Then I click on element having css "button.btn-danger"
    And I accept alert
    #Need to refresh because DOM elements still not garbage collected (chrome)
    Then I refresh page
    Then I click on link having partial text "Edi Thor"
    Then I click on element having css "button.btn-danger"
    And I accept alert
    And I wait for 1 sec
        
  Scenario: Logout
    And I sign out
