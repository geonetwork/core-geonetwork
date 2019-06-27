Feature: GeoNetwork Load samples and template
  As an admin I should be able to load samples and templates.

  Scenario: Login as admin and load samples and templates
    Given I login as admin/admin and navigate to admin.console#/metadata
    Then element having id "gn-btn-loadTemplates" should be disabled
    Then element having id "gn-btn-loadSamples" should be disabled
    And I click on element having id "gn-schemas-selector"
    And I click on element having id "gn-selectAllSchemas"
    Then element having id "gn-btn-loadTemplates" should be enabled
    Then element having id "gn-btn-loadSamples" should be enabled
    And I click on element having id "gn-btn-loadTemplates"
    And I wait 5 seconds for element having css "div#gn-templates-reportStatus.panel-success" to display
    Then element having xpath "//*[@id='gn-templates-reportStatus']" should have attribute "class" with value "panel panel-default panel-success"
    And I click on element having id "gn-btn-loadSamples"
    And I wait 5 seconds for element having css "div#gn-samples-reportStatus.panel-success" to display
    Then element having xpath "//*[@id='gn-samples-reportStatus']" should have attribute "class" with value "panel panel-default panel-success"
    And I sign out
