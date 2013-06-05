.. _configuration:

System configuration
====================

Many GeoNetwork System configuration parameters can be changed using the
web interface. Database parameters can be changed using the GAST application.

.. important:: Configuration of these parameters is critically important for 
   for a GeoNetwork catalogue in an operational context. Misunderstanding 
   these settings may result in a system that does not function as
   expected. For example, downloads may fail to be correctly processed, or 
   metadata harvesting from other servers may not work.

To get to the System configuration parameters, you must be logged on as administrator first. Open the Administration page and select System configuration (The link is inside the red ellipse).

.. important:: New installations of GeoNetwork use admin for both username
   and password. It is important to change the password using the links in the
   Administration page the first time you log on!

.. figure:: web-config-where.png

    *The link to the System configuration page*

Clicking the link bring up the system configuration menu. A detailed description of these parameters follows.

.. figure:: web-config-options-1.png

    *The configuration options - part 1*

.. figure:: web-config-options-2.png

    *The configuration options - part 2*

.. figure:: web-config-options-3.png

    *The configuration options - part 3*

Note: at the bottom of the page (you will need to scroll down) there are three buttons with the following purpose:

 - **Back** Simply returns to the main administration page, ignoring any changes you may have made. 
 - **Save** Saves the current options. If some options are invalid, the system will show a dialogue with the wrong parameter and will focus its text field on the page. Once the configuration is saved a success dialogue will be shown. 
 - **Refresh** Reads the settings from the database again and refreshes the options with those values.

Site parameters
```````````````

*Catalogue identifier* A universally unique identifier (uuid) that distinguishes your catalogue from any other catalogue. This a unique identifier for your catalogue and its best to leave it as a uuid. 

*Name* The name of the GeoNetwork node. Information that helps identify the catalogue to a human user.

*Organization* The organization the node belongs to. Again, this is information that helps identify the catalogue to a human user.

Server parameters
`````````````````

Here you have to enter the details of the web address of your GeoNetwork node. This address is important because it will be used to build addresses that access services and data on the GeoNetwork node. In particular:

#. building links to data file uploaded with a metadata record in the editor.
#. when the OGC CSW server is asked to describe its capabilities. The GetCapabilities operation returns an XML document with HTTP links to the CSW services provided by the server. These links are dynamically built using the host and port values.


*Protocol* The HTTP protocol used to access the server. Choosing http means that all communication with GeoNetwork will be visible to anyone listening to the protocol. Since this includes usernames and passwords this is not secure. Choosing https means that all communication with GeoNetwork will be encrypted and thus much harder for a listener to decode. 

*Host* The node’s address or IP number. If your node is publicly accessible from the Internet, you have to use the domain name. If your node is hidden inside your private network and you have a firewall or web server that redirects incoming requests to the node, you have to enter the public address of the firewall or web server. A typical configuration is to have an Apache web server on address A that is publicly accessible and redirects the requests to a Tomcat server on a private address B. In this case you have to enter A in the host parameter.

*Port* The node’s port (usually 80 or 8080). If the node is hidden, you have to enter the port on the public firewall or web server. 


Intranet Parameters
```````````````````

A common need for an organisation is to automatically discriminate between anonymous internal users that access the node from within an organisation (Intranet) and anonymous external users from the Internet. GeoNetwork defines anonymous users from inside the organisation as belonging to the group *Intranet*, while anonymous users from outside the organisation are defined by the group *All*. To automatically distinguish users that belong to the Intranet group you need to tell GeoNetwork the intranet IP address and netmask.

*Network* The intranet address in IP form (eg. 147.109.100.0).

*Netmask* The intranet netmask (eg. 255.255.255.0).


Metadata Search Results
```````````````````````

Configuration settings in this group determine what the limits are on user interaction with the search results.

*Maximum Selected Records* The maximum number of search results that a user can select and process with the batch operations eg. Set Privileges, Categories etc.


Multi-Threaded Indexing
```````````````````````

Configuration settings in this group determine how many processor threads are allocated to indexing tasks in GeoNetwork. If your machine has many processor cores, you can now determine how many to allocate to GeoNetwork indexing tasks. This can bring dramatic speed improvements on large indexing tasks (eg. changing the privileges on 20,000 records) because GeoNetwork can split the indexing task into a number of pieces and assign them to different processor cores.

*Number of processing threads* The maximum number of processing threads that can be allocated to an indexing task. 

Note: this option is only available for databases that have been tested. Those databases are PostGIS and Oracle. You should also carefully consider how many connections to the database you allocate in the database configuration as each thread could tie up one database connection for the duration of a long indexing session (for example). See :ref:`adv_configuration` for more details of how to configure the number of connections in the database connection pool.

Lucene Index Optimizer
```````````````````````

Configuration settings in this group determine when the Lucene Index Optimizer is run. By default, this takes place at midnight each day. With recent upgrades to Lucene, particularly Lucene 3.6.1, the optimizer is becoming less useful, so this configuration group will very likely be removed in future versions.

Z39.50 configuration
````````````````````

GeoNetwork can act as a Z39.50 server. Z39.50 is the name of an older communication protocol used for distributed searching across metadata catalogs.

*Enable*: Check this option to enable the Z39.50 server, uncheck it to disable the Z39.50 server. 

*Port*: This is the port on which GeoNetwork will be listening for incoming Z39.50 requests. Z3950 servers can run on any port, but 210 (not recommended), 2100 and 6668 are common choices. If you have multiple GeoNetwork nodes running on the same machine then you need to make sure each one has a different port number.

GeoNetwork must be restarted to put any changes to these values into use.

OAI Provider
````````````

Options in this group control the way in which the OAI Server in GeoNetwork responds to OAIPMH harvest requests from remote sites.

*Datesearch*: OAI Harvesters may request records from GeoNetwork in a date range. GeoNetwork can use one of two date fields from the metadata to check for a match with this date range. The default choice is *Temporal extent*, which is the temporal extent from the metadata record. The other option, *Modification date*, uses the modification date of the metadata record in the GeoNetwork database. The modification date is the last time the metadata record was updated in or harvested by GeoNetwork.

*Resumption Token Timeout*: Metadata records that match an OAI harvest search request are usually returned to the harvester in groups with a fixed size (eg. in groups of 10 records). With each group a resumption token is included so that the harvester can request the next group of records. The resumption token timeout is the time (in seconds) that GeoNetwork OAI server will wait for a resumption token to be used. If the timeout is exceeded GeoNetwork OAI server will drop the search results and refuse to recognize the resumption token. The aim of this feature is to ensure that resources in the GeoNetwork OAI server are released.

*Cache size*: The maximum number of concurrent OAI harvests that the GeoNetwork OAI server can support.

GeoNetwork must be restarted to put any changes to the resumption token timeout and the Datesearch options into use.

.. _xlink_config:

XLink resolver
``````````````

The XLink resolver replaces the content of elements with an attribute @xlink:href (except for srv:operatesOn element) with the content obtained from the URL content of @xlink:href. The XLink resolver should be enabled if you want to harvest metadata fragments or reuse fragments of metadata in your metadata records.

*Enable*: Enables/disables the XLink resolver. 

Note: to improve performance GeoNetwork will cache content that is not in the local catalog.

.. _search_stats_config:

Search Statistics 
`````````````````

Enables/disables search statistics capture. Search statistics are stored in the database and can be queried using the Search Statistics interface on the Administration page. There is very little compute overhead involved in storing search statistics as they are written to the database in a background thread. However database storage for a very busy site must be carefully planned.

Multilingual Settings
`````````````````````

Options in this group determine how GeoNetwork will search metadata in multiple languages.

*Enable auto-detecting search request language:* If this option is selected, Geonetwork will analyse the search query and attempt to detect the language that is used before defaulting to the GUI language.

*Search results in requested language sorted on top:* If this option is selected, a sort clause will be added to each query to ensure that results in the current language are always sorted on top. This is different from increasing priority of the language in that it overrides the relevance of the result.  For example, if a german result has very high relevance but the search language is french then the french results will all come before the german result.

*Search only in requested language* The options in this section determines how documents are sorted/prioritised relative to the language in the document compared to the search language.

- *All documents in all languages (No preferences)* - The search language is ignored and will have no effect on the ordering of the results

- *Prefer documents with translations requested language* - Documents with a translation in the search language (anywhere in the document) will be prioritized over documents without any elements in the search language

- *Prefer documents whose language is the requested language* - Documents that are the same language as the search language (ie. the documents that are specified as being in the same language as the search language) are prioritized over documents that are not.

- *Translations in requested language* - The search results will only contain documents that have some translations in the search language.  

- *Document language is the requested language* - The search results will contain documents whose metadata language is specified as being the in search language


Data-For-Download Service
`````````````````````````

GeoNetwork editor supports uploading one or more files that can be stored with the metadata record. When such a record is displayed in the search results, a 'Download' button is provided which will allow the user to select which file they want to download. This option group determines how that download will occur. 

*Use GeoNetwork simple file download service:* Clicking on any file stored with the metadata record will deliver that file directly to the user via the browser.

*Use GeoNetwork disclaimer and constraints service:* Clicking on any file stored with the metadata record will deliver a zip archive to the user (via the browser) that contains the data file, the metadata record itself and a summary of the resource constraint metadata as an html document. In addition, the user will need to provide some details (name, organisation, email and optional comment) and view the resource constraints before they can download the zip archive.


Clickable hyperlinks
````````````````````
Enables/disables hyperlinks in metadata content. If a URL is present in the metadata content, GeoNetwork will detect this and make it into a clickable hyperlink when it displays the metadata content.

Local rating 
````````````
Enables/disables local rating of metadata records.

Automatic fixes
```````````````

For each metadata schema, GeoNetwork has an XSLT that it can apply to a metadata record belonging to that schema. This XSLT is called update-fixed-info.xsl and the aim of this XSLT is to allow fixed schema, site and GeoNetwork information to be applied to a metadata record every time the metadata record is saved in the editor. As an example, GeoNetwork uses this XSLT to build and store the URL of any files uploaded and stored with the metadata record in the editor.

*Enable*: Enabled by default. It is recommended you do not use the GeoNetwork default or advanced editor when auto-fixing is disabled.  See http://trac.osgeo.org/geonetwork/ticket/368 for more details.

INSPIRE
```````
Enables/disables the INSPIRE support: 

- CSW GetCapabilities includes the INSPIRE section (ie. ExtendedCapabilities) that administrator can customize in xml/csw/capabilities_inspire.xml 
  and response support language extensions. The language provided defines:

 - Natural language fields are returned in the language requested (see :ref:`csw_configuration`)

 - The end-points are returned for the language requested

- INSPIRE themes are indexed (check that INSPIRE themes thesaurus is available and reindex the catalog)

- Enables/disables INSPIRE search panel: Add INSPIRE criteria in the advanced search panel (eg. Annex, INSPIRE theme)


Metadata Views
``````````````

Options in this section enable/disable metadata element groups in the metadata editor/viewer.

*Enable simple view*: The simple view in the metadata editor/viewer:
- removes much of the hierarchy from nested metadata records (such as ISO19115/19139)
- will not let the user add metadata elements that are not already in the metadata record
It is intended to provide a flat, simple view of the metadata record. A disadvantage of the simple view is that some of the context information supplied by the nesting in the metadata record is lost.
*Enable ISO view*: The ISO19115/19139 metadata standard defines three groups of elements:
- Minimum: those elements that are mandatory
- Core: the elements that should be present in any metadata record describing a geographic dataset
- All: all the elements
*Enable INSPIRE view*: Enables the metadata element groups defined in the EU INSPIRE directive.
*Enable XML view*: This is a raw text edit view of the XML record. You can disable this if (for example), you don't want inexperienced users to be confused by the XML presentation provided by this view.

Metadata Privileges
```````````````````

*Only set privileges to user's groups*: If enabled then only the groups that the user belongs to will be displayed in the metadata privileges page (unless the user is an Administrator). At the moment this option cannot be disabled and is likely to be deprecated in the next version of GeoNetwork.

.. _editing_harvested_records:

Harvesting
``````````

*Allow editing on harvested records*: Enables/Disables editing of harvested records in the catalogue. By default, harvested records cannot be edited.

Proxy
`````

For some functions (eg. harvesting) GeoNetwork must be able to connect to remote sites. This may not be possible if an organisation uses proxy servers. If your organisation uses a proxy server then GeoNetwork must be configured to use the proxy server in order to correctly route outgoing requests to remote sites.

*Use*: Checking this box will display the proxy configuration options panel.

.. figure:: web-config-options-proxy.png

    *The proxy configuration options*

*Host*: The proxy server name or address to use (usually an IP address).

*Port*: The proxy server port to use.

*Username* (optional): a username should be provided if the proxy server requires authentication.

*Password* (optional): a password should be provided if the proxy server requires authentication.

Feedback 
````````
GeoNetwork needs to send email if:
 - you are using the User Self-registration system or the Metadata Status workflow
 - a file uploaded with a metadata record is downloaded 
 - a user provides feedback using the online form. 

You have to configure the mail server GeoNetwork should use in order to enable it to send these emails.

*Email*: This is the email address that will be used to send the email (the From address).

*SMTP host*: the mail server address to use when sending email.

*SMTP port*: the mail server SMTP port (usually 25).

Removed metadata
````````````````

Defines the directory used to store a backup of metadata and data after a delete action. This
directory is used as a backup directory to allow system administrators to recover metadata and possibly
related data after erroneous deletion. By default the removed directory
is created in the GeoNetwork data folder.

