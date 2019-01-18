Feature: Create (draft) editor
  Setup environment to test draft feature

  Scenario: Setup environment to test draft feature
    Given I login as admin/admin and navigate to admin.console#/organization

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
    When I click on element having id "gn-btn-user-save"
    And I wait 5 seconds for element having css "div.alert.gn-info" to display
        
    # Add editor 2
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
    When I click on element having id "gn-btn-user-save"
    And I wait 5 seconds for element having css "div.alert.gn-info" to display

    # Add reviewer
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
    When I click on element having id "gn-btn-user-save"
    Then I wait 5 seconds for element having css "div.alert.gn-info" to display
        
    # Import templates, just in case
    Given I navigate to "{endPointToTest}/srv/eng/admin.console#/metadata"
    Then I click on element having css "div.list-group > li:nth-child(1) > h4 > i"
    Then I click on element having css "button > i.gn-recordtype-y"
    Then I wait 10 seconds for element having css "div.panel-success" to display
        
    # Logout admin
    And I sign out
