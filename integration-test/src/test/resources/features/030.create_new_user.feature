Feature: GeoNetwork Create new user
  As a user admin I should able to create new user.

  Scenario: I login and create the user etabeta
    Given I login as admin/admin and navigate to admin.console#/organization
    And I click on element having id "gn-btn-user-add"
    And I enter "etabeta" into input field having id "username"
    And I enter "PassTest" into input field having id "gn-user-password"
    And I enter "PassTest" into input field having id "gn-user-password2"
    And I enter "Eta" into input field having name "name"
    And I enter "Beta" into input field having name "surname"
    And I enter "etabeta@email.com" into input field having name "email"
    And I click on element having id "gn-btn-user-save"
    And I wait 10 seconds for element having css ".alert-success" to display
    And I sign out

    Then I login as etabeta/PassTests and navigate to catalog.search
    And I sign out
