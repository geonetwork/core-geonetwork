
================================================================================
===
=== GeoNetwork 2.4.3 minor bug fix release: List of changes
===
================================================================================
--------------------------------------------------------------------------------
--- Changes
--------------------------------------------------------------------------------
- INSPIRE support, including a specific search form (disabled by default, enable
  in the System preferences panel)
- CSW ISO profile updates and test suite
- GeoServer upgrade to v2.0.1 with the REST API and SLD Styler included
- Search speed improvements (more to come in v2.6.0, due in August 2010!)
- Added Portuguese language and improvements for others

================================================================================
===
=== GeoNetwork 2.4.2 minor bug fix release: List of changes
===
================================================================================
--------------------------------------------------------------------------------
--- Bug fixes
--------------------------------------------------------------------------------

- 158: CSW harvesting. Send preferred outputSchema from Capabilties in requests
- 155: CSW harverting only supports 2.0.2 servers 
- 156:Proxy server is not used for all CSW harvesting operations (also fixed
  OGCWXS harvester)
- Fix for ticket 125: Increase perfomance of showing metadata executing increase
  popularity asynchronously
- Fixed keyword identifier with no #. See #147.
- Close existing Lucene searcher. Fixed type issue.
- Case insensitive UUID handling
- Fixed bad attribute name. Thanks Richard Walker.
- Fixed javascript error with IE8 (#145). Thanks to Christopher and Andrew.
- Fixed UUID generation when inserting metadata with option : "Generate UUID ..."
  (#144).
- updated change log
- Inline documentation

================================================================================
===
=== GeoNetwork 2.4.1 minor bug fix release: List of changes
===
================================================================================
--------------------------------------------------------------------------------
--- Bug fixes
--------------------------------------------------------------------------------

- Corrected Dutch translation
- Fixed force rebuild index on startup.
- Fixed hardcoded english strings in javascript. Use the translate(tagName)
  function and the js attribute in loc file now.
- Added login support for CSW operations from CSW test page to easily test
  transactions.
- Translation fix. Thanks Jean Pommier.
- Added doc to disabled caching and use Saxon.
- #141 Fixed XSL compilation error for RSS services (due to additional bracket).
  Thanks to Roger and Jean.
- Fixed typo in codelists (#140).
- Fixed category search menu. See #139
- Fixed keyword autocompletion. Thanks Richard Walker. #134

================================================================================
===
=== GeoNetwork 2.4.0 Final: List of changes (See also changes 2.4.0.txt)
===
================================================================================
--------------------------------------------------------------------------------
--- Changes
--------------------------------------------------------------------------------

- Added MD5 checksum creation to installer build process
- Alter sections of manual that describe system configuration to include
- Bring fgdc thumbnail handling into line with ISO (ie. add thumbnail
  upload/button to editor)
- bulk import.
- Capabilities_Filter section is mandatory
- CSW / Fix fallback to POST method if existing.
- csw:csw-2.0.2-GetCapabilities-tc7.1 and ...-tc7.2 
- details on shibboleth and reorganisation for authentication.
- Documentation updates related to System configuration
- Enhancement - ticket #131 - add thumbnail display for fgdc metadata
- Fix bug with permissions when GN in Z server role
- Fix for Oracle SQL create database script
- Fix includes in summary/brief metadata returned by z3950 so that they work
  with saxon
- Fix links to manual.pdf
- Fixed checkBoxAsBoolean for multiple checkboxes, thanks FXP
- Folder related to Jeeves
- folder related to xslt caching
- For type gco:Boolean use a checkbox control in the editor. If user checks /
  unchecks the box, the value of a hidden input is set to true or false
  respectively.
- group authentication options to include self-registration, clarify choices
- Handle <image type="unknown" ..> links to thumbnails from all standards by
  scaling down
- harvest from geonetwork node wasn't updating thumbnails
- harvested records should not be synced by metadata sync in gast
- Make sure gast picks up xslt transformer factory choices from services
- Minor fix required to pass two tests from OGC CSW 2.0.2 test suite
- modalbox fixes including tabbing between form fields
- Prevent confusing error caused by attempting to add a thumbnail with empty
  filename
- Remove unnecessary include which caused saxon to return an error when doing
- Remove unused xalan namespace from xslt
- removing geonetwork APIs from SVN
- Show thumbnail for fgdc in full metadata view but not in embedded view
- Small documentation updates to trunk
- Tidy validation error/no-error reporting and fix ticket #127
- updated, synchronized files for all languages

================================================================================
===
=== GeoNetwork 2.4.x RC2: List of changes (See also changes 2.4.0RC2.txt)
===
================================================================================
--------------------------------------------------------------------------------
--- Changes
--------------------------------------------------------------------------------
- improve french translation (merge from geocat.ch and GeoSource, Thanks Annina 
  and Etienne)
- improve CSW dc mapping response for service metadata in ISO or ISO profil
- harvesting / OGC-WxS harvester set coupledResource elements and not only 
  operatesOn.
- admin / misc styling fix (admin css, missing localised string mainly in JS 
  alert, sorting list). 
- admin / make common templates for metadata import and batch import forms.
- admin / if no templates available, display message rather than an empty list.
- admin, search / if no categories, hide option and set default option to none 
  (to be continued). Some users are not using categories at all.
- add some FIXME to be discussed for cleaning. Mainly in XSL templates and 
  localised stuff in JS. In JS, it could be better to create an array of 
  localised string needed in JS files. This could allow to make a better 
  separation with JS and XSL files (ie. remove all JS from XSL files) and call 
  this array to retrieve localised string from JS.
- edit / edit buttons : localisation and truncate title if larger than XX
  character.
- Do not capitalize all element name to avoid Point Of Contact. Fixe capitalize 
  in localisation files if needed.
- import / Add batch MEF import
- edit / sort enumeration (eg. in ISO topic category and service direction)
 
--------------------------------------------------------------------------------
--- Bug fixes
--------------------------------------------------------------------------------

- Add support for proxies to OAI and OGC (thumbnail) harvesters
- Delete public/private resources after MEF backup in both Delete and
  MassiveDelete
- Fix bug in metadata copy/paste reported by Heikki (Ticket 104)
- Add results page for metadata batch import and metadata copy/paste
- csw / SummaryComparator should not trigger exception when numeric comparison 
  occurs on wrong data type (eg. scale denominator). Those values are pushed to 
  the bottom of the list.
- edit / Use new layout for gmd:MD_Metadata/gmd:Contact
- edit / When duplicating metadata only groupOwner could be set (not a multiple
  select box)
- Fix some JS error for non escaped character in localised file (french mainly).

================================================================================
===
=== GeoNetwork 2.4.x RC1: List of changes
===
================================================================================
--------------------------------------------------------------------------------
--- Changes
--------------------------------------------------------------------------------

- Allow Featured map result to be limited to a specified bounding box (global is
  default)
  
- Fix to prevent Stack Overflow when validating large metadata documents
  The setting now defaults to 2 megabyte (-Xss2M) but should be increased even 
  further (to -Xss10M or even -Xss20M for very large metadata documents)
  (thanks to Richard Fozzard)
  
- Updated German translation (thanks to David Arndt)

- Updated French translation (thanks to Etienne)

- Fixed bug #90 and #91 - Thanks to Tom Kralidis

- Fixed missing loc file for csw:records.

- Ensure that Metadata.data column is of type longtext for MySQL. Thanks to Tom.

- WebDAV harvesting improvements

- ArcSDE metadata harvesting

- import cleanup

================================================================================
===
=== GeoNetwork 2.4.x RC0: List of changes
===
================================================================================
--------------------------------------------------------------------------------
--- Known issues
--------------------------------------------------------------------------------

- On some Windows systems, installing in the default Program files folder causes
  a stylesheet compilation error. The workaround is to install in a directory
  without spaces in the folder names. E.g. in c:\geonetwork

- In Postgresql an error occurs related to type casting while migrating from 
  version 2.0.3. Using the older version of the jdbc driver version 7.4 seems 
  to resolve this problem for Postgres v7 and v8.x. The old driver can be found 
  at http://jdbc.postgresql.org/download.html#archived

--------------------------------------------------------------------------------
--- Changes
--------------------------------------------------------------------------------

- Added russian translation (Thanks to Igor V. Burago) #93

- Improve CSW 2.0.2 ISO Profil support
  (http://trac.osgeo.org/geonetwork/wiki/CSW202Improvements)

- Added import XML/MEF file
  (http://trac.osgeo.org/geonetwork/wiki/MetadataImport)

- Added SelectionManager
  (http://trac.osgeo.org/geonetwork/wiki/SelectionManager)

- Ajax Editor Controls and other Editor Enhancements  
  (http://trac.osgeo.org/geonetwork/wiki/AjaxEditorControlsAndValidation)

- More operations on a selected set of metadata records 
  (http://trac.osgeo.org/geonetwork/wiki/MoreMassiveOperations)

- Improve user interface for file upload/download
  (http://trac.osgeo.org/geonetwork/wiki/FileUploadAndDownload)

- Restore editing rights and ownership enhancements
  (http://trac.osgeo.org/geonetwork/wiki/Permissions)
  
- User Self-Registration Service
  (http://trac.osgeo.org/geonetwork/wiki/SelfRegistration)
  option to Administration->System Configuration to enable/disable
  UserSelfRegistration

- Add Shibboleth as an authentication option
  (http://trac.osgeo.org/geonetwork/wiki/ShibbolethAuth)

- Upgraded GeoServer to version 1.7.3

- Upgraded Jetty servlet container to version 6.1.14

- Moved data folder out of WEB-INF folder to ./data in the root of the
  application

- Added multilingual support in installer

- Added french translation of the documentation

- Added file system harvester to harvest metadata from local directory (from the
  server perspective)

- Added ArcSDE harvester to harvest metadata from an ArcSDE geodatabase 
  (requires dummy library to be replaced with ESRI Java API to work)

- Added support for printing search result in PDF format 
  (http://trac.osgeo.org/geonetwork/wiki/PrintPdf)

- Added support to harvest the OGC:GetCapabilities (WMS, WFS, WCS and WPS)
  documents to produce metadata for services and layers/featuretypes/coverages
  in ISO19139/119 format (http://trac.osgeo.org/geonetwork/wiki/ISO19119impl)

--------------------------------------------------------------------------------
--- Bug fixes
--------------------------------------------------------------------------------
