Feature: GeoNetwork Manage group
  As an admin I should be able to manage groups.

  Scenario: I login and create group1 with only mandatory field ie. name
    Given I login as admin/admin and navigate to admin.console#/organization/groups
    And I wait 5 seconds for element having id "gn-btn-group-add" to display
    And I click on element having id "gn-btn-group-add"
    Then element having id "gn-btn-group-save" should be disabled
    And I enter "itest-group1" into input field having id "groupname"
    Then element having id "gn-btn-group-save" should be enabled
    Then I click on element having id "gn-btn-group-save"
    Then I wait for 1 sec
    # Check group is created
    Then I click on link having partial text "itest-group1"
    Then element having xpath "//a[normalize-space(text()) = 'itest-group1']" should be present


  Scenario: I create group2 with all group properties
    Given I click on element having id "gn-btn-group-add"
    And I enter "itest-group2" into input field having id "groupname"
    # FIXME   And I enter "Test group with all properties set for automatic testing" into input field having xpath "//textarea[name='description']"
    And I select "2" option by value from dropdown having name "category"

    And I enter "itest-group2@@@@thisnotanemail.org" into input field having name "email"
    Then element having id "gn-btn-group-save" should be disabled
    And I clear input field having name "email"
    And I enter "itest-group2@email.org" into input field having name "email"
    Then element having id "gn-btn-group-save" should be enabled

    And I enter "https://geonetwork-opensource.org/" into input field having name "website"
    # TODO: Save should be disabled on invalid URL ?

    # TODO: set other properties

    Then I click on element having id "gn-btn-group-save"
    Then I wait for 1 sec

  Scenario: Check group is created
    Then I click on link having partial text "itest-group2"
    # TODO: Check all properties are here

    # TODO: Check group presence by using the API.
    # Then I check API operation GET /groups as admin/admin and expect status 200

  Scenario: I create the same group twice should return an exception
    And I click on element having id "gn-btn-group-add"
    And I enter "itest-group1" into input field having id "groupname"
    Then I click on element having id "gn-btn-group-save"
    # FIXME Then element having xpath "//*[normalize-space(text()) = 'A group with name \'itest-group1\' already exist.']" should be present


  Scenario: I delete all groups created
    Given I click on link having partial text "itest-group1"
    And I click on element having id "gn-btn-group-delete"
    And I accept alert
    Then I wait for 1 sec
    # Check group is deleted
    Then element having xpath "//a[normalize-space(text()) = 'itest-group1']" should not be present

    Then I click on link having partial text "itest-group2"
    And I click on element having id "gn-btn-group-delete"
    And I accept alert
    Then I wait for 1 sec

    Then I sign out
