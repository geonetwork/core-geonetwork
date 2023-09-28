.. _system_configuration:

System configuration
====================

Introduction
------------

GeoNetwork configuration parameters can be changed to suit the needs of your site. There are two groups of parameters:

- parameters that can be changed through the web interface.

- parameters not accessible from the web interface and that must be
  changed when the system is not running

The first group of parameters can be queried or changed through the two services described in this section: ``xml.config.get`` and ``xml.config.set``. 

The second group of parameters must be changed manually by editing the ``config-*.xml`` files in ``INSTALL_DIR/web/geonetwork/WEB-INF``. 

The GAST tool can be used to help configure the database parameters (see the section on the GAST tool in the user manual).

.. index:: xml.config.get

xml.config.get
--------------

This service returns the system configuration parameters as an XML document.

Request
```````

Parameters: *None*

Response
````````

The response is an XML tree similar to the system hierarchy into the
settings structure. The response has the following elements:

- **site**: A container for site information.

  - **name**: Site name.
  - **organisation**: Site organisation name.
  - **svnUuid**: Subversion Uuid (used for metadata versioning)
  - **siteId**: Uuid of site (used to uniquely identify site)

- **platform**: Details of development platform.

  - **version**: Version string of software.
  - **subVersion**: Additional version string.

- **server**: A container for server information.

  - **host**: Name of the host from which the site is reached.
  - **port**: Port number of the previous host.
  - **protocol**: http or https.

- **Intranet**: Information about the Intranet of the organisation.

  - **network**: IP address that specifies the intranet.
  - **netmask**: netmask used to identify intranet.

- **z3950**: Configuration of Z39.50 server.

  - **enable**: true means that the Z39.50 server component is running.
  - **port**: Port number to use to listen for incoming Z39.50 requests.

- **proxy**: Proxy configuration

  - **use**: true means proxy is used when connecting to external nodes.
  - **host**: Proxy server host.
  - **port**: Proxy server port.
  - **username**: Proxy credentials.
  - **password**: Proxy credentials.

- **feedback**: A container for feedback information

  - **email**: Feedback/Info email address
  - **mailServer**: Email server to use to send feedback emails

    - **host**: Email server address
    - **port**: Port number of email service on email server

- **removedMetadata**: A container for removed metadata information

  - **dir**: Folder used to store removed metadata in MEF format

- **ldap**: A container for LDAP parameters (see System Configuration in Users Manual for more information)

- **selectionmanager**: A container for selection manager configuration

  - **maxrecords**: Maximum number of records that can be selected

- **csw**: A container for csw server configuration

  - **enable**: CSW server is enabled if set to true.
  - **contactId**: Identifier of GeoNetwork user who is the contact for the CSW server.
  - **metadataPublic**: If set to true then metadata inserted through the CSW server will be made public immediately.

- **shib**: A container for Shibboleth parameters (see System Configuration in Users Manual for more information)

- **userSelfRegistration**: A container for user self-registration service configuration

  - **enable**: enabled if set to true.

- **clickablehyperlinks**: A container for configuration of clickable hyper-links in metadata content 

  - **enable**: enabled if set to true. ie. hyperlinks in metadata content will be automatically turned into clickable HTML links

- **localrating**: A container for configuration of local rating versus remote rating

  - **enable**: local rating enabled if set to true.

- **downloadservice**: A container for configuration of file download interface on links built from ISO online resources with file download protocol

  - **leave**: don't build links or modify ISO online resources with file download protocol
  - **simple**: download file immediately when user clicks on link
  - **withdisclaimer**: when user clicks on link, display metadata resource restrictions and disclaimers before downloading file

- **xlinkResolver**: A container for configuration of XLink resolver service

  - **enable**: XLinks in metadata records will be resolved if set to true

- **autofixing**: A container for configuration of autofixing service

  - **enable**: Autofixing (ie. update-fixed-info.xsl) will be applied to metadata records when they are saved in the editor

- **searchStats**: A container for configuration of search statistics collection

  - **enable**: if true then search stats will collected on searches made through the GeoNetwork user interface

- **indexOptimzer**: A container to configure if and when Lucene index optimization will take place (likely to be deprecated in the next release of GeoNetwork)

  - **enable**: if true then enable optimization at the scheduled interval 

- **oai**: A container to configure the Open Archives Initiative (OAI) server in GeoNetwork

  - **mdmode**: if '1' then OAI date searches uses the metadata temporal extent, if '2' then the modification date from the database is used
  - **tokentimeout**: time in seconds that a continuation token passed to a client can be used
  - **cachesize**: number of client sessions that the server can manage

- **inspire**: A container to configure the Inspire options in GeoNetwork

  - **enable**: if true then inspire indexing of ISO metadata will be enabled
  - **enableSearchPanel**: if true then inspire search panel will be shown in the search interface

- **harvester**: A container to configure harvesting options

  - **enableEditing**: if true then harvested records can be edited, false means editing will not be enabled

- **metadata**: A container to configure the different view/edit tabs shown to the user in the viewer/editor 

  - **enableSimpleView**: simple (or default) mode means that only those elements present in the template/record will be displayed, new elements cannot be added - true means simple (or default) view is enabled, false means the tab will not be shown
  - **enableIsoView**: true means that tabs showing mandatory/core/all groupings of metadata elements will be present in the viewer/editor for ISO records
  - **enableInspireView**: true means that tabs showing inspire groupings of metadata elements will be present in the viewer/editor for ISO records
  - **enableXmlView**: true means that the tab showing the XML of the metadata record will be present in the viewer/editor
  - **defaultView**: 'simple', 'advanced', 'iso', 'xml' determines which tab will be the default view (ie. the view used when no previous view has been selected by the user in their current session). 

- **threadedindexing**: A container to configure multi-threaded indexing

  - **maxThreads**: Number of threads to be used during multi-threaded indexing 

- **autodetect**: A parameter to configure language detection in search terms

  - **enable**: if true then language detection is enabled

- **requestedLanguage**: A parameter to configure which indexes will be searched and which languages will be used to display results

  - **only**: 'off' - all languages ignored, 'prefer_locale' - prefer documents with translations to requested language, 'prefer_docLanguage' - prefer documents whose language is the requested language, 'only_locale' - translations in requested language, 'only_docLocale' - document language is the requested language

Example of xml.config.get response::

 <?xml version="1.0" encoding="UTF-8"?>
 <config>
  <site>
    <name>My GeoNetwork catalogue</name>
    <organization>My organization</organization>
    <svnUuid>c7799284-e786-4425-a6cf-824bb07e478e</svnUuid>
    <siteId>b7ce20f2-888a-4139-8802-916730c4be06</siteId>
  </site>
  <platform>
    <version>2.8.0</version>
    <subVersion>RC2</subVersion>
  </platform>
  <server>
    <host>localhost</host>
    <port>8080</port>
    <protocol>http</protocol>
  </server>
  <intranet>
    <network>127.0.0.1</network>
    <netmask>255.0.0.0</netmask>
  </intranet>
  <z3950>
    <enable>true</enable>
    <port>2100</port>
  </z3950>
  <proxy>
    <use>false</use>
    <host/>
    <port/>
    <username/>
    <password/>
  </proxy>
  <feedback>
    <email/>
    <mailServer>
      <host/>
      <port>25</port>
    </mailServer>
  </feedback>
  <removedMetadata>
    <dir>WEB-INF/data/removed</dir>
  </removedMetadata>
  <ldap>
    <use>false</use>
    <host/>
    <port/>
    <defaultProfile>RegisteredUser</defaultProfile>
    <uidAttr>uid</uidAttr>
    <distinguishedNames>
      <base>dc=fao,dc=org</base>
      <users>ou=people</users>
    </distinguishedNames>
    <userAttribs>
      <name>cn</name>
      <profile>profile</profile>
      <group/>
    </userAttribs>
    <defaultGroup/>
  </ldap>
  <selectionmanager>
    <maxrecords>1000</maxrecords>
  </selectionmanager>
  <csw>
    <enable>true</enable>
    <contactId/>
    <metadataPublic>false</metadataPublic>
  </csw>
  <shib>
    <use>false</use>
    <path>/geonetwork/srv/eng/shib.user.login</path>
    <attrib>
      <username>REMOTE_USER</username>
      <surname>Shib-Person-surname</surname>
      <firstname>Shib-InetOrgPerson-givenName</firstname>
      <profile>Shib-EP-Entitlement</profile>
      <group/>
      <organizationName/>
      <postalAddress/>
      <phone/>
      <email/>
      <fullName/>
    </attrib>
    <defaultGroup/>
  </shib>
  <userSelfRegistration>
    <enable>false</enable>
  </userSelfRegistration>
  <clickablehyperlinks>
    <enable>true</enable>
  </clickablehyperlinks>
  <localrating>
    <enable>false</enable>
  </localrating>
  <downloadservice>
    <leave>false</leave>
    <simple>true</simple>
    <withdisclaimer>false</withdisclaimer>
  </downloadservice>
  <xlinkResolver>
    <enable>false</enable>
  </xlinkResolver>
  <autofixing>
    <enable>true</enable>
  </autofixing>
  <searchStats>
    <enable>false</enable>
  </searchStats>
  <indexoptimizer>
    <enable>true</enable>
    <at>
      <hour>0</hour>
      <min>0</min>
      <sec>0</sec>
    </at>
    <interval>
      <day>0</day>
      <hour>24</hour>
      <min>0</min>
    </interval>
  </indexoptimizer>
  <oai>
    <mdmode>1</mdmode>
    <tokentimeout>3600</tokentimeout>
    <cachesize>60</cachesize>
  </oai>
  <inspire>
    <enable>false</enable>
    <enableSearchPanel>false</enableSearchPanel>
  </inspire>
  <harvester>
    <enableEditing>false</enableEditing>
  </harvester>
  <metadata>
    <enableSimpleView>true</enableSimpleView>
    <enableIsoView>true</enableIsoView>
    <enableInspireView>false</enableInspireView>
    <enableXmlView>true</enableXmlView>
    <defaultView>simple</defaultView>
  </metadata>
  <metadataprivs>
    <usergrouponly>false</usergrouponly>
  </metadataprivs>
  <threadedindexing>
    <maxthreads>1</maxthreads>
  </threadedindexing>
  <autodetect>
    <enable>false</enable>
  </autodetect>
  <requestedLanguage>
    <only>prefer_locale</only>
  </requestedLanguage>
 </config>

.. index:: xml.config.set

xml.config.set
--------------

This service is used to update the system configuration. It is
restricted to users with the *Administrator* profile.

Request
```````

The request format is the same as the XML document produced by the ``xml.config.get`` service. To use the ``xml.config.set`` service in the simplest way:

#. Call ``xml.config.get`` to obtain an XML document describing the current configuration.
#. Update the content of the elements you want to change.
#. POST the modified XML document describing the new configuration to ``xml.config.set``.

So a typical POST request would look like:::
 
 Url: http://localhost:8080/geonetwork/srv/eng/xml.config.set

 <request>
   <config>
    .....
   </config>
 </request>

Response
````````

If the request executed successfully then HTTP status code 200 is returned along with an XML document confirming success. The success response is:::
 
    <response>ok</response>

If an exception occurred then an XML document with the details of the exception is returned. 

See :ref:`exception_handling` for more details.

