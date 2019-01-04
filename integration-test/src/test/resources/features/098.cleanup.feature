Feature: Cleanup Draft Tests
  Remove everything we created for the tests

  Scenario: Remove everything we created for the tests
    # Login as admin
    Given I login as admin/admin and navigate to catalog.edit#/board

    # Remove all records
    Then I click on element having css "div.gn-editor-board button.dropdown-toggle"
    Then I click on element having xpath "//a[@data-ng-click='selectAll()']"
    Then I click on element having css "div.gn-selection-actions"
    Then I click on element having css "div.gn-selection-actions i.fa-times"
    Then I accept alert
    And I wait 3 seconds for element having css "div.alert-warning" to display
        
    # Remove editors
    When I navigate to "{endPointToTest}/srv/eng/admin.console#/organization"
    Then I click on link having partial text "Edi Thor"
    Then I click on element having css "button.btn-danger"
    And I accept alert
    And I wait for 1 sec
    Then I click on link having partial text "Revi Ewer"
    Then I click on element having css "button.btn-danger"
    And I accept alert
    And I wait for 1 sec
    Then I click on link having partial text "Edi Thor"
    Then I click on element having css "button.btn-danger"
    And I accept alert
        
        # Logout
    And I sign out
