# Canned Steps

selenium-cucumber-java comes with the following set of predefined steps.
You can add your own steps or change the ones you see here.

* [Navigation Steps](https://github.com/selenium-cucumber/selenium-cucumber-java/blob/master/doc/canned_steps.md#navigation-steps)
* [Assertion Steps](https://github.com/selenium-cucumber/selenium-cucumber-java/blob/master/doc/canned_steps.md#assertion-steps)
* [Input Steps](https://github.com/selenium-cucumber/selenium-cucumber-java/blob/master/doc/canned_steps.md#input-steps)
* [Click Steps](https://github.com/selenium-cucumber/selenium-cucumber-java/blob/master/doc/canned_steps.md#click-steps)
* [Progress Steps](https://github.com/selenium-cucumber/selenium-cucumber-java/blob/master/doc/canned_steps.md#progress-steps)
* [Screenshot Steps](https://github.com/selenium-cucumber/selenium-cucumber-java/blob/master/doc/canned_steps.md#screenshot-steps)
* [Configuration Steps](https://github.com/selenium/selenium-cucumber-java/blob/master/doc/canned_steps.md#configuration-steps)


## Navigation Steps

To open/close URL and to navigate between pages use following steps :

	Then I navigate to "([^\"]*)"
	Then I navigate to geonetwork
	Then I navigate forward
	Then I navigate back
	Then I refresh page

To switch between windows use following steps :

	Then I switch to new window
	Then I switch to previous window
	Then I switch to window having title "(.*?)"
	Then I close new window
	Then I switch to main window

To switch between frames use following steps :	

	Then I switch to frame having index \"(.*?)
	Then I switch to frame having id \"(.*?)
	Then I switch to frame having name \"(.*?)
	Then I switch to frame having xpath \"(.*?)
	Then I switch to frame having css \"(.*?)
	Then I switch to main content
	
To interact with browser use following steps :    

	Then I resize browser window size to width (\d+) and height (\d+)
	Then I maximize browser window
	Then I close browser

To zoom in/out webpage use following steps :

	Then I zoom in page
	Then I zoom out page

To zoom out webpage till necessary element displays use following steps :

	Then I zoom out page till I see element having id "(.*?)"
	Then I zoom out page till I see element having name "(.*?)"
	Then I zoom out page till I see element having class "(.*?)"
	Then I zoom out page till I see element having xpath "(.*?)"
	Then I zoom out page till I see element having css "(.*?)"

To reset webpage view use following step :

	Then I reset page view

To scroll webpage use following steps :
	
	Then I scroll to top of page
	Then I scroll to end of page

To scroll webpage to specific element use following steps :

	Then I scroll to element having id "(.*?)"
	Then I scroll to element having name "(.*?)"
	Then I scroll to element having class "(.*?)"
	Then I scroll to element having xpath "(.*?)"
	Then I scroll to element having css "(.*?)"

To hover over a element use following steps :

	Then I hover over element having id "(.*?)"
	Then I hover over element having name "(.*?)"
	Then I hover over element having class "(.*?)"
	Then I hover over element having xpath "(.*?)"
	Then I hover over element having css "(.*?)"


Assertion Steps
---------------
To assert that page title can be found use following step :

	Then I should see page title as "(.*?)"
	Then I should not see page title as "(.*?)"

	Then I should see page title having partial text as "(.*?)"
  Then I should not see page title having partial text as "(.*?)"
    
#### Steps For Asserting Element Text

To assert element text use any of the following steps :

	Then element having id "([^\"]*)" should have text as "(.*?)"
	Then element having name "([^\"]*)" should have text as "(.*?)"
	Then element having class "([^\"]*)" should have text as "(.*?)"
	Then element having xpath "([^\"]*)" should have text as "(.*?)"
	Then element having css "([^\"]*)" should have text as "(.*?)"

	Then element having id "([^\"]*)" should have partial text as "(.*?)"
	Then element having name "([^\"]*)" should have partial text as "(.*?)"
	Then element having class "([^\"]*)" should have partial text as "(.*?)"
	Then element having xpath "([^\"]*)" should have partial text as "(.*?)"
	Then element having css "([^\"]*)" should have partial text as "(.*?)"
	
	Then element having id "([^\"]*)" should not have text as "(.*?)"
	Then element having name "([^\"]*)" should not have text as "(.*?)"
	Then element having class "([^\"]*)" should not have text as "(.*?)"
	Then element having xpath "([^\"]*)" should not have text as "(.*?)"
	Then element having css "([^\"]*)" should not have text as "(.*?)"

	Then element having id "([^\"]*)" should not have partial text as "(.*?)"
	Then element having name "([^\"]*)" should not have partial text as "(.*?)"
	Then element having class "([^\"]*)" should not have partial text as "(.*?)"
	Then element having xpath "([^\"]*)" should not have partial text as "(.*?)"
	Then element having css "([^\"]*)" should not have partial text as "(.*?)"
	
#### Steps For Asserting Element Attribute

To assert element attribute use any of the following steps : 

	Then element having id "([^\"]*)" should have attribute "(.*?)" with value "(.*?)"
	Then element having name "([^\"]*)" should have attribute "(.*?)" with value "(.*?)"
	Then element having class "([^\"]*)" should have attribute "(.*?)" with value "(.*?)"
	Then element having xpath "([^\"]*)" should have attribute "(.*?)" with value "(.*?)"
	Then element having css "([^\"]*)" should have attribute "(.*?)" with value "(.*?)"
	
	Then element having id "([^\"]*)" should not have attribute "(.*?)" with value "(.*?)"
	Then element having name "([^\"]*)" should not have attribute "(.*?)" with value "(.*?)"
	Then element having class "([^\"]*)" should not have attribute "(.*?)" with value "(.*?)"
	Then element having xpath "([^\"]*)" should not have attribute "(.*?)" with value "(.*?)"
	Then element having css "([^\"]*)" should not have attribute "(.*?)" with value "(.*?)"
	

#### Steps For Asserting Element Accesibility

To assert that element is enabled use any of the following steps :

	Then element having id "([^\"]*)" should be enabled
	Then element having name "([^\"]*)" should be enabled
	Then element having class "([^\"]*)" should be enabled
	Then element having xpath "([^\"]*)" should be enabled
	Then element having css "([^\"]*)" should be enabled

To assert that element is disabled use any of the following steps :

	Then element having id "([^\"]*)" should be disabled
	Then element having name "([^\"]*)" should be disabled
	Then element having class "([^\"]*)" should be disabled
	Then element having xpath "([^\"]*)" should be disabled
	Then element having css "([^\"]*)" should be disabled

#### Steps For Asserting Element Visibility

To assert that element is present use any of the following steps :

	Then element having id "([^\"]*)" should be present
	Then element having name "([^\"]*)" should be present
	Then element having class "([^\"]*)" should be present
	Then element having xpath "([^\"]*)" should be present
	Then element having css "([^\"]*)" should be present
	
To assert that element is not present use any of the following steps:

	Then element having id "([^\"]*)" should not be present
	Then element having name "([^\"]*)" should not be present
	Then element having class "([^\"]*)" should not be present
	Then element having xpath "([^\"]*)" should not be present
	Then element having css "([^\"]*)" should not be present
	
#### Steps For Asserting Checkbox

To assert that checkbox is checked use any of the following steps :

	Then checkbox having id "(.*?)" should be checked
	Then checkbox having name "(.*?)" should be checked
	Then checkbox having class "(.*?)" should be checked
	Then checkbox having xpath "(.*?)" should be checked
	Then checkbox having css "(.*?)" should be checked

To assert that checkbox is unchecked use any of the following steps :

	Then checkbox having id "(.*?)" should be unchecked
	Then checkbox having name "(.*?)" should be unchecked
	Then checkbox having class "(.*?)" should be unchecked
	Then checkbox having xpath "(.*?)" should be unchecked
	Then checkbox having css "(.*?)" should be unchecked

#### Steps For Asserting Dropdown List

To assert that option by text from dropdown list selected use following steps :

	Then option "(.*?)" by text from dropdown having id "(.*?)" should be selected
	Then option "(.*?)" by text from dropdown having name "(.*?)" should be selected
	Then option "(.*?)" by text from dropdown having class "(.*?)" should be selected
	Then option "(.*?)" by text from dropdown having xpath "(.*?)" should be selected
	Then option "(.*?)" by text from dropdown having css "(.*?)" should be selected

To assert that option by value from dropdown list selected use following steps :

	Then option "(.*?)" by value from dropdown having id "(.*?)" should be selected
	Then option "(.*?)" by value from dropdown having name "(.*?)" should be selected
	Then option "(.*?)" by value from dropdown having class "(.*?)" should be selected
	Then option "(.*?)" by value from dropdown having xpath "(.*?)" should be selected
	Then option "(.*?)" by value from dropdown having css "(.*?)" should be selected
	
To assert that option by text from dropdown list unselected use following steps :

	Then option "(.*?)" by text from dropdown having id "(.*?)" should be unselected
	Then option "(.*?)" by text from dropdown having name "(.*?)" should be unselected
	Then option "(.*?)" by text from dropdown having class "(.*?)" should be unselected
	Then option "(.*?)" by text from dropdown having xpath "(.*?)" should be unselected
	Then option "(.*?)" by text from dropdown having css "(.*?)" should be unselected

To assert that option by value from dropdown list unselected use following steps :

	Then option "(.*?)" by value from dropdown having id "(.*?)" should be unselected
	Then option "(.*?)" by value from dropdown having name "(.*?)" should be unselected
	Then option "(.*?)" by value from dropdown having class "(.*?)" should be unselected
	Then option "(.*?)" by value from dropdown having xpath "(.*?)" should be unselected
	Then option "(.*?)" by value from dropdown having css "(.*?)" should be unselected    

#### Steps For Asserting Radio Button

To assert that radio button selected use any of the following steps :

	Then radio button having id "(.*?)" should be selected
	Then radio button having name "(.*?)" should be selected
	Then radio button having class "(.*?)" should be selected
	Then radio button having xpath "(.*?)" should be selected
	Then radio button having css "(.*?)" should be selected

To assert that radio button not selected use any of the following steps :

	Then radio button having id "(.*?)" should be unselected
	Then radio button having name "(.*?)" should be unselected
	Then radio button having class "(.*?)" should be unselected
	Then radio button having xpath "(.*?)" should be unselected
	Then radio button having css "(.*?)" should be unselected

To assert that radio button group selected by text use any of the following steps :

	Then option "(.*?)" by text from radio button group having id "(.*?)" should be selected
	Then option "(.*?)" by text from radio button group having name "(.*?)" should be selected
	Then option "(.*?)" by text from radio button group having class "(.*?)" should be selected
	Then option "(.*?)" by text from radio button group having xpath "(.*?)" should be selected
	Then option "(.*?)" by text from radio button group having css "(.*?)" should be selected
	
To assert that radio button group selected by value use any of the following steps :

	Then option "(.*?)" by value from radio button group having id "(.*?)" should be selected
	Then option "(.*?)" by value from radio button group having name "(.*?)" should be selected
	Then option "(.*?)" by value from radio button group having class "(.*?)" should be selected
	Then option "(.*?)" by value from radio button group having xpath "(.*?)" should be selected
	Then option "(.*?)" by value from radio button group having css "(.*?)" should be selected

To assert that radio button group not selected by text use any of the following steps :

	Then option "(.*?)" by text from radio button group having id "(.*?)" should be unselected
	Then option "(.*?)" by text from radio button group having name "(.*?)" should be unselected
	Then option "(.*?)" by text from radio button group having class "(.*?)" should be unselected
	Then option "(.*?)" by text from radio button group having xpath "(.*?)" should be unselected
	Then option "(.*?)" by text from radio button group having css "(.*?)" should be unselected

To assert that radio button group not selected by value use any of the following steps :

	Then option "(.*?)" by value from radio button group having id "(.*?)" should be unselected
	Then option "(.*?)" by value from radio button group having name "(.*?)" should be unselected
	Then option "(.*?)" by value from radio button group having class "(.*?)" should be unselected
	Then option "(.*?)" by value from radio button group having xpath "(.*?)" should be unselected
	Then option "(.*?)" by value from radio button group having css "(.*?)" should be unselected

#### Steps For Asserting Links

To assert that link is present use following steps :

	Then link having text "(.*?)" should be present
	Then link having partial text "(.*?)" should be present

To assert that link is not present use following steps :

	Then link having text "(.*?)" should not be present
	Then link having partial text "(.*?)" should not be present

#### Steps For Asserting Javascript Pop-Up Alert 

To assert text on javascipt pop-up alert use following step :

	Then I should see alert text as "(.*?)"

#### Steps For Asserting Difference in images : Not Implemented

To assert difference in actual image and expected image (from remotely hosted) use following steps :

	Then actual image having id "(.*?)" and expected image having url "(.*?)" should be similar
	Then actual image having name "(.*?)" and expected image having url "(.*?)" should be similar
	Then actual image having class "(.*?)" and expected image having url "(.*?)" should be similar
	Then actual image having xpath "(.*?)" and expected image having url "(.*?)" should be similar
	Then actual image having css "(.*?)" and expected image having url "(.*?)" should be similar
	Then actual image having url "(.*?)" and expected image having url "(.*?)" should be similar

To assert difference in actual image and expected image (from local machine) use following steps :

	Then actual image having id "(.*?)" and expected image having image_name "(.*?)" should be similar
	Then actual image having name "(.*?)" and expected image having image_name "(.*?)" should be similar
	Then actual image having class "(.*?)" and expected image having image_name "(.*?)" should be similar
	Then actual image having xpath "(.*?)" and expected image having image_name "(.*?)" should be similar
	Then actual image having css "(.*?)" and expected image having image_name "(.*?)" should be similar
	Then actual image having url "(.*?)" and expected image having image_name "(.*?)" should be similar

To assert difference in actual image and expected image (from same webpage) use following steps :

	Then actual image having id "(.*?)" and expected image having id "(.*?)" should be similar
	Then actual image having name "(.*?)" and expected image having name "(.*?)" should be similar
	Then actual image having class "(.*?)" and expected image having class "(.*?)" should be similar
	Then actual image having xpath "(.*?)" and expected image having xpath "(.*?)" should be similar
	Then actual image having css "(.*?)" and expected image having css "(.*?)" should be similar
	Then actual image having url "(.*?)" and expected image having url "(.*?)" should be similar


Input Steps
-----------

#### Steps For TextFields

To enter text into input field use following steps :

	Then I enter "([^\"]*)" into input field having id "([^\"]*)"
	Then I enter "([^\"]*)" into input field having name "([^\"]*)"
	Then I enter "([^\"]*)" into input field having class "([^\"]*)"
	Then I enter "([^\"]*)" into input field having xpath "([^\"]*)"
	Then I enter "([^\"]*)" into input field having css "([^\"]*)"
	
To clear input field use following steps :

	Then I clear input field having id "([^\"]*)"
	Then I clear input field having name "([^\"]*)"
	Then I clear input field having class "([^\"]*)" 
	Then I clear input field having xpath "([^\"]*)"
	Then I clear input field having css "([^\"]*)"

#### Steps For Dropdown List :

To select option by text from dropdown use following steps :

	Then I select "(.*?)" option by text from dropdown having id "(.*?)"
	Then I select "(.*?)" option by text from dropdown having name "(.*?)"
	Then I select "(.*?)" option by text from dropdown having class "(.*?)"
	Then I select "(.*?)" option by text from dropdown having xpath "(.*?)"
	Then I select "(.*?)" option by text from dropdown having css "(.*?)"

To select option by index from dropdown use following steps :

	Then I select (\d+) option by index from dropdown having id "(.*?)"
	Then I select (\d+) option by index from dropdown having name "(.*?)"
	Then I select (\d+) option by index from dropdown having class "(.*?)"
	Then I select (\d+) option by index from dropdown having xpath "(.*?)"
	Then I select (\d+) option by index from dropdown having css "(.*?)"

To select option by value from dropdown use following steps :

	Then I select "(.*?)" option by value from dropdown having id "(.*?)"
	Then I select "(.*?)" option by value from dropdown having name "(.*?)"
	Then I select "(.*?)" option by value from dropdown having class "(.*?)"
	Then I select "(.*?)" option by value from dropdown having xpath "(.*?)"
	Then I select "(.*?)" option by value from dropdown having css "(.*?)"

#### Steps For Multiselect List :

To select option by text from multiselect dropdown use following steps :

	Then I select "(.*?)" option by text from multiselect dropdown having id "(.*?)"
	Then I select "(.*?)" option by text from multiselect dropdown having name "(.*?)"
	Then I select "(.*?)" option by text from multiselect dropdown having class "(.*?)"
	Then I select "(.*?)" option by text from multiselect dropdown having xpath "(.*?)"
	Then I select "(.*?)" option by text from multiselect dropdown having css "(.*?)"
	
To select option by index from multiselect dropdown use following steps :

	Then I select (\d+) option by index from multiselect dropdown having id "(.*?)"
	Then I select (\d+) option by index from multiselect dropdown having name "(.*?)"
	Then I select (\d+) option by index from multiselect dropdown having class "(.*?)"
	Then I select (\d+) option by index from multiselect dropdown having xpath "(.*?)"
	Then I select (\d+) option by index from multiselect dropdown having css "(.*?)"

To select option by value from multiselect dropdown use following steps :

	Then I select "(.*?)" option by value from multiselect dropdown having id "(.*?)"
	Then I select "(.*?)" option by value from multiselect dropdown having name "(.*?)"
	Then I select "(.*?)" option by value from multiselect dropdown having class "(.*?)"
	Then I select "(.*?)" option by value from multiselect dropdown having xpath "(.*?)"
	Then I select "(.*?)" option by value from multiselect dropdown having css "(.*?)"

To deselect option by text from multiselect dropdown use following steps :

	Then I deselect "(.*?)" option by text from multiselect dropdown having id "(.*?)"
	Then I deselect "(.*?)" option by text from multiselect dropdown having name "(.*?)"
	Then I deselect "(.*?)" option by text from multiselect dropdown having class "(.*?)"
	Then I deselect "(.*?)" option by text from multiselect dropdown having xpath "(.*?)"
	Then I deselect "(.*?)" option by text from multiselect dropdown having css "(.*?)"
	
To deselect option by index from multiselect dropdown use following steps :

	Then I deselect (\d+) option by index from multiselect dropdown having id "(.*?)"
	Then I deselect (\d+) option by index from multiselect dropdown having name "(.*?)"
	Then I deselect (\d+) option by index from multiselect dropdown having class "(.*?)"
	Then I deselect (\d+) option by index from multiselect dropdown having xpath "(.*?)"
	Then I deselect (\d+) option by index from multiselect dropdown having css "(.*?)"
		
To deselect option by value from multiselect dropdown use following steps :

	Then I deselect "(.*?)" option by value from multiselect dropdown having id "(.*?)"
	Then I deselect "(.*?)" option by value from multiselect dropdown having name "(.*?)"
	Then I deselect "(.*?)" option by value from multiselect dropdown having class "(.*?)"
	Then I deselect "(.*?)" option by value from multiselect dropdown having xpath "(.*?)"
	Then I deselect "(.*?)" option by value from multiselect dropdown having css "(.*?)"
	
To select all options from multiselect use following steps : *Not Implemented*

	Then I select all options from multiselect dropdown having id "(.*?)"
	Then I select all options from multiselect dropdown having name "(.*?)"
	Then I select all options from multiselect dropdown having class "(.*?)"
	Then I select all options from multiselect dropdown having xpath "(.*?)"
	Then I select all options from multiselect dropdown having css "(.*?)"
	
To deselect all options from multiselect use following steps :

	Then I deselect all options from mutliselect dropdown having id "(.*?)"
	Then I deselect all options from mutliselect dropdown having name "(.*?)"
	Then I deselect all options from mutliselect dropdown having class "(.*?)"
	Then I deselect all options from mutliselect dropdown having xpath "(.*?)"
	Then I deselect all options from mutliselect dropdown having css "(.*?)"

#### Steps For Checkboxes

To check the checkbox use following steps :

	Then I check the checkbox having id "(.*?)"
	Then I check the checkbox having name "(.*?)"
	Then I check the checkbox having class "(.*?)"
	Then I check the checkbox having xpath "(.*?)"
	Then I check the checkbox having css "(.*?)"

To uncheck the checkbox use following steps :

	Then I uncheck the checkbox having id "(.*?)"
	Then I uncheck the checkbox having name "(.*?)"
	Then I uncheck the checkbox having class "(.*?)"
	Then I uncheck the checkbox having xpath "(.*?)"
	Then I uncheck the checkbox having css "(.*?)"

To toggle checkbox use following steps

	Then I toggle checkbox having id "(.*?)"
	Then I toggle checkbox having name "(.*?)"
	Then I toggle checkbox having class "(.*?)"
	Then I toggle checkbox having xpath "(.*?)"
	Then I toggle checkbox having css "(.*?)"

#### Steps For Radio Buttons

To select radio button use following steps :

	Then I select radio button having id "(.*?)"
	Then I select radio button having name "(.*?)"
	Then I select radio button having class "(.*?)"
	Then I select radio button having xpath "(.*?)"
	Then I select radio button having css "(.*?)"


To select one radio button by text from radio button group use following steps :

	Then I select "(.*?)" option by text from radio button group having id "(.*?)"
	Then I select "(.*?)" option by text from radio button group having name "(.*?)"
	Then I select "(.*?)" option by text from radio button group having class "(.*?)"
	Then I select "(.*?)" option by text from radio button group having xpath "(.*?)"
	Then I select "(.*?)" option by text from radio button group having css "(.*?)"

To select one radio button by value from radio button group use following steps :

	Then I select "(.*?)" option by value from radio button group having id "(.*?)"
	Then I select "(.*?)" option by value from radio button group having name "(.*?)"
	Then I select "(.*?)" option by value from radio button group having class "(.*?)"
	Then I select "(.*?)" option by value from radio button group having xpath "(.*?)"
	Then I select "(.*?)" option by value from radio button group having css "(.*?)"


Click Steps
-----------
To click on web element use following steps :

	Then I click on element having id "(.*?)"
	Then I click on element having name "(.*?)"
	Then I click on element having class "(.*?)"
	Then I click on element having xpath "(.*?)"
	Then I click on element having css "(.*?)"

To forcefully click on web element use following steps (if above steps do not work) :

	Then I forcefully click on element having id "(.*?)"
	Then I forcefully click on element having name "(.*?)"
	Then I forcefully click on element having class "(.*?)"
	Then I forcefully click on element having xpath "(.*?)"
	Then I forcefully click on element having css "(.*?)"

To double click on web element use following steps :

	Then I double click on element having id "(.*?)"
	Then I double click on element having name "(.*?)"
	Then I double click on element having class "(.*?)"
	Then I double click on element having xpath "(.*?)"
	Then I double click on element having css "(.*?)"

To click on links use following steps :

	Then I click on link having text "(.*?)"
	Then I click on link having partial text "(.*?)"

Progress Steps
--------------
To implicitly wait for specific time use following step :

	Then I wait for (\d+) sec
	
To wait for specific element to display use following steps :

	Then I wait (\d+) seconds for element having id "(.*?)" to display
	Then I wait (\d+) seconds for element having name "(.*?)" to display
	Then I wait (\d+) seconds for element having class "(.*?)" to display
	Then I wait (\d+) seconds for element having xpath "(.*?)" to display
	Then I wait (\d+) seconds for element having css "(.*?)" to display

To wait for specific element to enable use following steps : why it is clickable in code

	Then I wait (\d+) seconds for element having id "(.*?)" to enable
	Then I wait (\d+) seconds for element having name "(.*?)" to enable
	Then I wait (\d+) seconds for element having class "(.*?)" to enable
	Then I wait (\d+) seconds for element having xpath "(.*?)" to enable
	Then I wait (\d+) seconds for element having css "(.*?)" to enable

Javascript Handling Steps
-------------------------
To handle javascript pop-up use following steps :

	Then I accept alert 
	Then I dismiss alert
  

Screenshot Steps 
----------------
To take screenshot use following step :

	Then I take screenshot


Configuration Steps
-------------------
To print testing configuration use following step :

	Then I print configuration
