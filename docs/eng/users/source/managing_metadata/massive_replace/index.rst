.. _massive_update:

Metadata Massive Replace
=======================

This section guides you through the process of replacing content in metadata records into the GeoNetwork catalogue.

To use this feature the user you must be **registered** as **Reviewer**, **User Administrator** or **Administrator**.

*This section will guide you through the steps to apply a massive update of metadata content.*

#. In the home page, do a search to filter the metadata.

#. Select from the results the metadata you want to apply the massive update.

#. Select **Actions on selection** > **Massive metadata update**.

	.. figure:: massive_update.png

This form allows to configure the replacements to apply to the metadata selection.
	
Define the replacement(s)
-------------------------

To define the replacements to apply to the selected metadata:

#. Select the package that contains the metadata element to replace:

	- Metadata.
	- Data Identification.
	- Service Identification.
	- Maintenance information.
	- Content information.
	- Distribution information.
#. Select the metadata element you want to replace in the package selected.
#. Specify the text content to replace.
#. Specify the new text content.
#. Multiple replacements can be specified clicking on **+** button.

Once all the replacements are defined, you can test it before applying it. Click **Test** button to produce a report with the metadata that will be updated:

	.. figure:: massive_update_test.png

.. note::
    The test process does not update the metadata with the replacements.

To apply the replacements and save the changes, click **Update metadata** button. A confirmation message is displayed before executing the process.

	.. figure:: massive_update_confirmation.png
	
The process will replace any occurrence of the text indicated in (3) with the text indicated in (4) for the metadata element indicated in (2) and can take several time, depending on the size of the selection. 

At the end of the process a summary is displayed.

#. Number of records processed: total records processed (selected records).
#. Number of records updated: total records that have been updated.

	.. figure:: massive_update_result.png
	

Replacements available
----------------------


Metadata section
''''''''''''''''
* Contact > Individual Name
* Contact > Organization Name
* Contact > Voice phone
* Contact > Fax phone
* Contact > Address
* Contact > City
* Contact > Province
* Contact > Postal code
* Contact > Country
* Contact > Email
* Contact > Online Resource > URL
* Contact > Online Resource > Application Profile
* Contact > Online Resource > Name
* Contact > Online Resource > Description
* Contact > Hours of service
* Contact > Contact Instructions

Data Identification section
''''''''''''''''''''''''''
* Abstract
* Purpose
* Keyword
* Citation > Individual Name
* Citation > Organization Name
* Citation > Voice phone
* Citation > Fax phone
* Citation > Address
* Citation > City
* Citation > Province
* Citation > Postal code
* Citation > Country
* Citation > Email
* Citation > Online Resource > URL
* Citation > Online Resource > Application Profile
* Citation > Online Resource > Name
* Citation > Online Resource > Description
* Citation > Hours of service
* Citation > Contact Instructions
* Point Of Contact > Individual Name
* Point Of Contact > Organization Name
* Point Of Contact > Voice phone
* Point Of Contact > Fax phone
* Point Of Contact > Address
* Point Of Contact > City
* Point Of Contact > Province
* Point Of Contact > Postal code
* Point Of Contact > Country
* Point Of Contact > Email
* Point Of Contact > Online Resource > URL
* Point Of Contact > Online Resource > Application Profile
* Point Of Contact > Online Resource > Name
* Point Of Contact > Online Resource > Description
* Point Of Contact > Hours of service
* Point Of Contact > Contact Instructions
* Resource Constraints > General Constraint > Use Limitation
* Resource Constraints > Legal Constraints > Use Limitation
* Resource Constraints > Legal Constraints > Other Constraints
* Resource Constraints > Security Constraint > Use Limitation
* Resource Constraints > Other Constraints'

Service Identification section
'''''''''''''''''''''''''''''

* Abstract
* Purpose
* Citation > Individual Name
* Citation > Organization Name
* Citation > Voice phone
* Citation > Fax phone
* Citation > Address
* Citation > City
* Citation > Province
* Citation > Postal code
* Citation > Country
* Citation > Email
* Citation > Online Resource > URL
* Citation > Online Resource > Application Profile
* Citation > Online Resource > Name
* Citation > Online Resource > Description
* Citation > Hours of service
* Citation > Contact Instructions
* Point Of Contact > Individual Name
* Point Of Contact > Organization Name
* Point Of Contact > Voice phone
* Point Of Contact > Fax phone
* Point Of Contact > Address
* Point Of Contact > City
* Point Of Contact > Province
* Point Of Contact > Postal code
* Point Of Contact > Country
* Point Of Contact > Email
* Point Of Contact > Online Resource > URL
* Point Of Contact > Online Resource > Application Profile
* Point Of Contact > Online Resource > Name
* Point Of Contact > Online Resource > Description
* Point Of Contact > Hours of service
* Point Of Contact > Contact Instructions
* Connect Point > URL
* Connect Point > Application Profile
* Connect Point > Name
* Connect Point > Description


Maintenance section
'''''''''''''''''''

* Contact > Individual Name
* Contact > Organization Name
* Contact > Voice phone
* Contact > Fax phone
* Contact > Address
* Contact > City
* Contact > Province
* Contact > Postal code
* Contact > Country
* Contact > Email
* Contact > Online Resource > URL
* Contact > Online Resource > Application Profile
* Contact > Online Resource > Name
* Contact > Online Resource > Description
* Contact > Hours of service
* Contact > Contact Instructions

Content information section
'''''''''''''''''''''''''''

* Feature Catalogue Citation > Individual Name
* Feature Catalogue Citation > Organization Name
* Feature Catalogue Citation > Voice phone
* Feature Catalogue Citation > Fax phone
* Feature Catalogue Citation > Address
* Feature Catalogue Citation > City
* Feature Catalogue Citation > Province
* Feature Catalogue Citation > Postal code
* Feature Catalogue Citation > Country
* Feature Catalogue Citation > Email
* Feature Catalogue Citation > Online Resource > URL
* Feature Catalogue Citation > Online Resource > Application Profile
* Feature Catalogue Citation > Online Resource > Name
* Feature Catalogue Citation > Online Resource > Description
* Feature Catalogue Citation > Hours of service
* Feature Catalogue Citation > Contact Instructions

Distribution information section
''''''''''''''''''''''''''''''''
     
* Distributor Contact > Individual Name
* Distributor Contact > Organization Name
* Distributor Contact > Voice phone
* Distributor Contact > Fax phone
* Distributor Contact > Address
* Distributor Contact > City
* Distributor Contact > Province
* Distributor Contact > Postal code
* Distributor Contact > Country
* Distributor Contact > Email
* Distributor Contact > Online Resource > URL
* Distributor Contact > Online Resource > Application Profile
* Distributor Contact > Online Resource > Name
* Distributor Contact > Online Resource > Description
* Distributor Contact > Hours of service
* Distributor Contact > Contact Instructions
* Fees
* Ordering Instructions
* Digital Transfer Options > URL
* Digital Transfer Options > Application Profile
* Digital Transfer Options > Name
* Digital Transfer Options > Description