.. _index:

Exercise 1
==========


Purpose of the exercise
-----------------------

This exercise will guide you through the GeoNetwork Metadata Management Tool to familiarize yourself with configuring and using the software. 

Focus will be on:

.. image:: OSGeo_project.png
   :align: right

- Basic configuration
- Create a metadata record
- Add Extent information
- Add Thumbnail (Preview) images
- Change Categories, Privileges
- Parent/Child relations
- Create Templates
- Create a multilingual metadata record

.. image:: OSGeo_project.png
   :align: center

`Optional exercises`

- Harvest from a remote CSW server
- Configure GeoServer and create a Web Map Service
- Harvest metadata from the Web Map Server


Basic Configuration
-------------------

Open the M2T website at `<https://lrcdtovsdvdb001.cihs.ad.gov.on.ca/geonetwork/>`_. 

.. image:: OSGeo_project.png
   :align: left

- You should now see the M2T application in your browser

.. image:: OSGeo_project.png
   :align: left
   
- Log in using the username admin and password admin

.. image:: OSGeo_project.png
   :align: left
 
- You will see a new Administration option in the menu. Select it to open the administration page.


First you'll change your personal settings:

.. image:: OSGeo_project.png
   :align: right

- Change the password
- Change the user information

.. note::
	Depending on your User Profile you may see less options than in the adjacent image.

You can use the Training Manual for details on each parameter to change. 

.. note:: 
	Some screenshots may differ from the M2T user interface.

.. image:: OSGeo_project.png
   :align: right

- Change the password of your account

.. image:: OSGeo_project.png
   :align: left

- Update your contact details in the User management section


Creating and Editing Metadata in GeoNetwork
-------------------------------------------

.. image:: OSGeo_project.png
   :align: left

- Create a new metadata record through the Administration - New metadata option.

.. image:: OSGeo_project.png
   :align: left

- Select the template you would like to use as the basis of your new metadata record.
- Fill out the fields in the editor that properly describe the data you would like to describe. We’ll use the Default view for now.

.. image:: OSGeo_project.png
   :align: left

- Note that fields marked in Red are still empty and are required to be filled out to create a valid record.


Add the Geographic Extent
-------------------------

We’ll then switch to the Group By - Identification View to add bounding boxes that describe the extent of your data.

.. image:: OSGeo_project.png
   :align: left

- Add the Bounding Box Extent

.. image:: OSGeo_project.png
   :align: left

- Select the Bounding Box using the interactive map. You have several options to create the bounding box that is valid for your data. Here, simply select it from the drop down menu or draw it manually using the Draw Rectangle tool.
- We’ll do the same, now describing the Extent as a polygon.

.. image:: OSGeo_project.png
   :align: left

- Use the Draw Polygon tool and draw a polygon on the map (double click to close the polygon).
- If you added a Place keyword for this record, you can generate the bounding box using the Compute Extent function under Other options. Make sure the metadata has been saved before computing the extent.

.. image:: OSGeo_project.png
   :align: left

- Switch back to the Default View.


Adding a Preview Image (or Thumbnail)
-------------------------------------

Add a thumbnail using the Thumbnail wizard under Other options. 

.. image:: OSGeo_project.png
   :align: left

- Select an image of your data (use any image you want for now) and add it as Large and Small preview.

.. image:: OSGeo_project.png
   :align: left

- Once saved you see the result and can go back to editing.


Validating your Metadata Record
-------------------------------

.. image:: OSGeo_project.png
   :align: left

- If your metadata is complete, you can validate the structure and content of it by selecting the “Check” function at the top of the editor. 

.. image:: OSGeo_project.png
   :align: left

- The result will be displayed in a popup validation report.


Assigning Privileges and Categories to a Metadata Record
--------------------------------------------------------

- Finally you will need to set the privileges for this metadata record. These can be set by selecting Privileges from the “Other actions” button. 

.. image:: OSGeo_project.png
   :align: left

- Select all privileges for this record to make it available to All your GeoNetwork users.

.. image:: OSGeo_project.png
   :align: left

- Also set the Categories that your new record should become part of.


Parent - Child Relations
------------------------

The last step will be to link this metadata record to a Parent metadata record.

.. image:: OSGeo_project.png
   :align: left

- Select the “Add or update parent metadata section” option.
- The editor will switch to the By Package-Metadata view.

.. image:: OSGeo_project.png
   :align: left

- Click on the Parent identifier (+) sign and then on the search icon next to the textfield.

.. image:: OSGeo_project.png
   :align: left

- Search for the metadata record you would like to link by Clicking the small Magnifier icon. Select the record and then Create Relation.

.. image:: OSGeo_project.png
   :align: left

- The relation has been created by adding the Parent identifier of the selected record(s).

.. image:: OSGeo_project.png
   :align: left

- You can now see the relation once you Saved and Closed the editor.

You can create similar type of relations with metadata describing Services or Feature Catalogs.


Create a Template
-----------------

The process of creating a template is very straightforward. A Template is identical to a regular metadata record, except that it is marked as being a Template.
You will start with a copy of the metadata record you created before. 
You will also add French as a second language your metadata record is written in, creating a multilingual metadata template.

.. image:: OSGeo_project.png
   :align: left

- Copy the metadata, creating a new one by selecting the Create button (you have be in the full metadata view for this). Select the Group the template should be part of.

.. image:: OSGeo_project.png
   :align: left

- In Edit mode, change the value of the drop down at the bottom of the editor to Template.

.. image:: OSGeo_project.png
   :align: left

- Add the French language support to the Template by switching to the Group by - Metadata view. Click the (+) sign to add another language. The Other language option is located at the bottom of the view.
- Save the record to make it available as a template for later use.


Create a Multilingual Metadata Record
-------------------------------------

We will create a new multilingual metadata record now. This is essentially the same process as creating a normal metadata, except that we will use the multilingual metadata template.
- Select Administration > New Metadata and then select the Template for Vector data in ISO19139 (multilingual). Hit Create.

.. image:: OSGeo_project.png
   :align: left

- The Editor will open with a basic multilingual metadata record to be filled out. Start changing information in the template for your “Core dataset”.
- At the bottom of the page you could first remove the languages that are not relevant for your purpose and possibly add a new language. Use the small x to eliminate a language. Use the + to add a new language. Save and close the record to propagate the change and reopen the Editor again.

.. image:: OSGeo_project.png
   :align: left

- Using the drop down selection boxes you can change the language of a field you want to fill out. Then fill the field with the required text.

.. image:: OSGeo_project.png
   :align: left

Fields in English

.. image:: OSGeo_project.png
   :align: left

Fields in French

.. image:: OSGeo_project.png
   :align: left

- In the Tutorial “Mastering Advanced GeoNetwork” you can learn how to enable the Google Translate function to allow automatic translation of text.


Create a new User Group and a new User - Administration
-------------------------------------------------------

Read the section on User and Group Administration and perform the following steps:
- Change the Sample Group name to “foss4g2010” group (No spaces in the name!).

.. image:: OSGeo_project.png
   :align: left

- Translate the Group name foss4g2010 in the localization panel. This will ensure that the Group name is correctly displayed (you can also translate group names into other languages if useful).

.. image:: OSGeo_project.png
   :align: left

- Add a new user with the Profile of Content Reviewer


Quick Start Guide - Getting started
-----------------------------------

We now have a GeoNetwork system that can be used. There are many more options that have not been covered. The Mastering Advanced GeoNetwork Tutorial available from `<http://thor.geocat.net/downloads/training/FOSS4G_Mastering_Advanced_GeoNetwork.pdf/>`-
 discusses more advanced configuration and installation options. 


Harvesting Management - Harvest metadata from a CSW catalog
-----------------------------------------------------------

It this exercise you will retrieve metadata records from a remote catalog into your own local GeoNetwork catalog. Although a proprietary GeoNetwork to GeoNetwork harvesting process will give better results due to some advanced capabilities, we will demonstrate harvesting of metadata from an OGC-CSW server because it is fully standards based.
- If you are not logged onto your GeoNetwork application as Administrator, please do so first.
- Proceed to the Administration - Harvesting management page
We will use the following CSW URL:
`<http://www.nationaalgeoregister.nl/geonetwork/srv/eng/csw?request=GetCapabilities&service=CSW&version=2.0.2>`-
- Select Add to insert a new Harvesting task

.. image:: OSGeo_project.png
   :align: left

- Select the Catalog Services Type

- Fill out the properties of this harvesting task. Use the default interval for now and Add a search criteria with Free text natuur to limit our search (and load on the server)

.. image:: OSGeo_project.png
   :align: right

- Add Privileges for All and select all checkboxes
- Save the configuration

- You will now need to Activate and Run the harvesting task. Select the task and hit Activate. Then do the same and hit Run. You can hit the Refresh button from time to time to see if the task was completed. When successful your catalog will have harvested over 40 records from the NGR catalog.

.. image:: OSGeo_project.png
   :align: left

- Go back to the home page and perform a search on “natuur” to see the result.


Configure GeoServer and setup a WMS layer
-----------------------------------------

We’ll now configure the GeoServer software that is part of the GeoNetwork installation.
Next we’ll create an interactive map service for Capitals in Africa (OGC-WMS). 
To prepare, open the web pages on the CDROM again and select “Data for exercises” in the top left column. 

The data we need is located at:
`<http://thor.geocat.net/downloads/training/training_data.zip>`-

Download and uncompress is. Copy the \capitals_africa\captl_pt.zip file into 
c:\Program Files\geonetwork\data\geoserver_data\data\ 
or  c:\geonetwork\data\geoserver_data\data\ on Windows Vista
and unzip it. You should have a folder named captl_pt that contains the shape file.

.. image:: OSGeo_project.png
   :align: left

.. image:: OSGeo_project.png
   :align: left

Metadata related to the Cities data set
---------------------------------------

Title: “VMap0 Capital Cities” 
Abstract: 
“Robust derivative of VMap0 - Ed5 data layers with harmonized encoding. The CAPTL_PT shapefile data layer is comprised of 55 derivative vector framework library features derived based on 1:1 000 000 data originally from VMap0, 5th Edition. The layer provides nominal analytical/mapping at 1:1 000 000. Data processing complete globally, this is an African subset. Acronyms and Abbreviations: VMap0 - Vector Map for Level 0.”
Keywords: 

.. image:: OSGeo_project.png
   :align: left
   
.. image:: OSGeo_project.png
   :align: left


Setting up harvesting of the WMS Service metadata
-------------------------------------------------

We’ll now use this service in our GeoNetwork catalog to automatically generate new metadata records.
Switch back to the GeoNetwork window and open the “Administration” page.

.. image:: OSGeo_project.png
   :align: left
   
.. image:: OSGeo_project.png
   :align: left
