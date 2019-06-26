Feature: GeoNetwork Create new user
  As a user admin I should able to create new user.

  Scenario: I login and delete the etabeta user
    Given I login as admin/admin and navigate to admin.console#/organization
    And I wait 10 seconds for element having css "a.list-group-item" to display
    And I click on link having partial text "Eta Beta"
    And I click on element having css "button.btn-danger"
    And I accept alert
    # TODO Check it does not exist
    And I sign out
