.. _formatter:

Formatter
=========

Introduction
------------

The metadata.show service (the metadata viewer) displays a metadata document using the default metadata display stylesheets.  However it can be useful to provide alternate stylesheets for displaying the metadata.  Consider a central catalog that is used by several partners.  Each partner might have special branding and wish to emphasize particular components of the metadata document.  

To this end the metadata.formatter.html and metadata.formatter.xml services allow an alternate stylesheet to be used for displaying the metadata.  The urls of interest to an end-user are:

 * /geonetwork/srv/<langCode>/metadata.formatter.html?xsl=<formatterId>&id=<metadataId>

  * Applies the stylesheet identified by xsl parameter to the metadata identified by id param and returns the document with the *html* contentType

 * /geonetwork/srv/<langCode>/metadata.formatter.xml?xsl=<formatterId>&id=<metadataId>

  * Applies the stylesheet identified by xsl parameter to the metadata identified by id param and returns the document with the *xml* contentType

 * /geonetwork/srv/<langCode>/metadata.formatter.list

  * Lists all of the metadata formatter ids
  
Another use-case for metadata formatters is to embed the metadata in other websites.  Often a metadata document contains a very large amount of data and perhaps only a subset is interesting for a particular website or perhaps the branding/stylesheets needs to be customized to match the website.

Administration
--------------

A metadata formatter is a bundle of files that can be uploaded to Geonetwork as a zip file (or in the simplest case just upload the xsl).  

An administration user interface exists for managing these bundles.  The starting page of the ui contains a list of the available bundles and has a field for uploading new bundles.  There are three upload options:

 * *Single xsl file* - A new bundle will be created for the xsl file and the name of the bundle will be based on the xsl file name
 * *Zip file (flat)* - A zip file which contains a view.xsl file and other required resources at the root of the zip file so that when unzipped the files will be unzipped into the current directory
 * *Zip file (folder)* - A zip file with a single folder that contains a view.xsl file and the other required resources so that when unzipped a single directory will be created that contains the formatter resources.

If a bundle is uploaded any existing bundles with the same name will be replaced with the new version.

See Bundle format section below for more details about what files can be contained in the format bundle.

When a format in the formatter list is selected the following options become enabled:

 * Delete - Delete the format bundler from Geonetwork
 * Download - Download the bundle.  This allows the administrator to download the bundle and edit the contents then upload at a later date
 * Edit - This provides some online edit capabilities of the bundle.  At the moment it allows editing of *existing* text files.  Adding new files etc... maybe added in the future but is not possible at the moment.  When edit is clicked a dialog with a list of all editable files are displayed in a tree and double clicking on a file will open a new window/tab with a text area containing the contents of the file.  The webpage has buttons for saving the file or viewing a metadata with the style.  The view options do *NOT* save the document before execution, that must be done before pressing the view buttons.

Bundle format
-------------

A format bundle is at minimum a single xsl file.  If the xsl file is uploaded it can have any name.  On the server a folder will be created that contains the xsl file but renamed to view.xsl.

If a zip file is uploaded the zip file must contain a file view.xsl.  The view.xsl file is the entry point of the transformation.  It can reference other xsl stylesheets if necessary as well as link to css stylesheets or images that are contained within the bundle or elsewhere.  

The view.xsl stylesheet is executed on an xml file with essentially the following format:

- root 

 - url - text of the url tag is the base url to make requests to geonetwork.  An example is /geonetwork/
 - locUrl - text of the url tag is the localised url to make requests to geonetwork.  An example is /geonetwork/srv/eng/
 - resourceUrl - a base url for accessing a resource from the bundle.  An example of image tag might be::

                 <img src="{/root/resourceURL}/img.png"/>

 - <metadata> - the root of the metadata document
 - loc

  - lang - the text of this tag is the lang code of the localization files that are loaded in this section
  - <bundle loc file> - the contents of the bundles loc/<locale>/\*.xml files

 - strings - the contents of geonetwork/loc/<locale>/xml/strings.xml
 - schemas

  - <schema> - the name of the schema of the labels and codelists strings to come

   - labels - the localised labels for the schema as defined in the schema_plugins/<schema>/loc/<locale>/labels.xml
   - codelists - the localised codelists labels for the schema as defined in the schema_plugins/<schema>/loc/<locale>/codelists.xml
   - strings - the localised strings for the schema as defined in the schema_plugins/<schema>/loc/<locale>/strings.xml

If the view.xsl output needs to access resources in the formatter bundle (like css files or javascript files) the xml document contains a tag: resourceUrl that contains the url for obtaining that resource.  An example of an image tag is:: 

 <img src="{/root/resourceURL}/img.png"/>

By default the strings, labels, etc... will be localized based on the language provided in the URL.  For example if the url is /geonetwork/srv/eng/metadata.formatter.html?xsl=default&id=32 then the language code that is used to look up the localization will be eng.  However if the language code does not exist it will fall back to the Geonetwork platform default and then finally just load the first local it finds. 

Schemas and geonetwork strings all have several different translations but extra strings, etc... can be added to the formatter bundle under the loc directory.  The structure would be::
 
 loc/<langCode>/strings.xml

The name of the file does not have to be strings.xml.  All xml files in the loc/<langCode>/ directory will be loaded and added to the xml document.

The format of the formatter bundle is as follows::
 
 config.properties
 view.xsl
 loc/<langCode>/

Only the view.xsl is required.  If a single xsl file is uploaded then the rest of the directory structure will be created and some files will be added with default values.  So a quick way to get started on a bundle is to upload an empty xsl file and then download it again.  The downloaded zip file will have the correct layout and contain any other optional files.

Config.properties
-----------------

The config.properties file contains some configuration options used when creating the xml document.  Some of the properties include:

 - *fixedLang* - sets the language of the strings to the fixed language, this ensures that the formatter will always use the same language for its labels, strings, etc... no matter what language code is in the url.
 - *loadGeonetworkStrings* - if true or non-existent then geonetwork strings will be added to the xml document before view.xsl is applied.  The default is true so if this parameter is not present then the strings will be loaded
 - *schemasToLoad* - defines which schema localization files should be loaded and added to the xml document before view.xsl is applied

  - if a comma separated list then only those schemas will be loaded
  - if all then all will be loaded
  - if none then no schemas will be loaded

 - *applicableSchemas* - declares which schemas the bundle can format

  - A comma separated list indicates specifically which schemas the bundle applies to
  - If the value is all (or value is empty) then all schemas are considered supported
  
