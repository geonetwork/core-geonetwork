Feature: Create (draft) editor
  Setup environment to test draft feature

  Scenario: Add editortest
    Given I login as admin/admin and navigate to admin.console#/organization
    Then I maximize browser window

    # Add editor
    And I click on element having id "gn-btn-user-add"
    And I enter "editortest" into input field having id "username"
    And I enter "editorpass" into input field having id "gn-user-password"
    And I enter "editorpass" into input field having id "gn-user-password2"
    And I enter "Edi" into input field having name "name"
    And I enter "Thor" into input field having name "surname"
    And I enter "editortest@email.com" into input field having name "email"
    And I click on element having css "div[data-gn-multiselect*='Editor'] option"
    And I double click on element having css "div[data-gn-multiselect*='Editor'] option"
    And I wait 3 seconds for element having css "select[data-ng-model='currentSelectionRight'] > option" to display
    Then I scroll to top of page
    And I click on element having id "gn-btn-user-save"
    And I wait 5 seconds for element having css "div.alert.gn-info" to display
        
    # Add editor 2
  Scenario: Add editortest2
    Then I navigate to "{endPointToTest}/srv/eng/admin.console#/organization"
    And I click on element having id "gn-btn-user-add"
    And I enter "editortest2" into input field having id "username"
    And I enter "editorpass" into input field having id "gn-user-password"
    And I enter "editorpass" into input field having id "gn-user-password2"
    And I enter "Edi" into input field having name "name"
    And I enter "Thor 2" into input field having name "surname"
    And I enter "editortest@email.com" into input field having name "email"
    And I click on element having css "div[data-gn-multiselect*='Editor'] option"
    And I double click on element having css "div[data-gn-multiselect*='Editor'] option"
    And I wait 3 seconds for element having css "select[data-ng-model='currentSelectionRight'] > option" to display
    Then I scroll to top of page
    When I click on element having id "gn-btn-user-save"
    And I wait 5 seconds for element having css "div.alert.gn-info" to display

    # Add reviewer
  Scenario: Add reviewertest
    And I click on element having id "gn-btn-user-add"
    And I enter "reviewertest" into input field having id "username"
    And I enter "editorpass" into input field having id "gn-user-password"
    And I enter "editorpass" into input field having id "gn-user-password2"
    And I enter "Revi" into input field having name "name"
    And I enter "Ewer" into input field having name "surname"
    And I enter "reviewer@email.com" into input field having name "email"
    And I click on element having css "div[data-gn-multiselect*='Reviewer'] option"
    And I double click on element having css "div[data-gn-multiselect*='Reviewer'] option"
    And I wait 3 seconds for element having css "select[data-ng-model='currentSelectionRight'] > option" to display
    Then I scroll to top of page
    When I click on element having id "gn-btn-user-save"
    And I wait 5 seconds for element having css "div.alert.gn-info" to display
        
    # Logout admin
    And I sign out
