.. _services_site_info_forwarding:

Site Information and Request Forwarding Services
================================================

Services in this section provide information about the site (eg. name, users, groups, schemas etc) and access to the site forwarding service which can be used by JavaScript clients.

.. index:: xml.info

.. _xml.info:

Site Information (xml.info)
---------------------------

This service can be used to retrieve information about a GeoNetwork site. The information that can be requested includes: site name and id, users, groups, metadata schemas as well as lists of privileges, metadata status values, spatial regions, local metadata categories and so on. 

Request
```````

The XML request should contain at least one type parameter to indicate the
kind of information to retrieve. Multiple type parameters can be specified.
The set of allowed values is:

- **site**: Returns general information about the site like its name, id, etc...

- **users**: Depending upon the profile of the user making the call, information about users of the site will be returned. The rules are:
 
 - Administrators can see all users
 - User administrators can see all users they administer and
   all other user administrators in the same group set. The group set
   is defined by all groups visible to the user administrator (except for
   the All and Intranet groups).
 - An authenticated user can only see their own information.
 - A guest cannot see any user information at all.

- **groups**: Returns all user groups visible to the requesting user. Note: If the user is not authenticated, only the ``Intranet`` and ``All`` groups will be returned.

- **sources**: Returns all GeoNetwork sources (remote sites) that are known about at the site. This will include:

 - Node name and siteId
 - All source UUIDs and site names that have been discovered through harvesting
 - All source UUIDs and site names from MEF files imported by the site

- **schemas**: Returns all registered metadata schemas for the site

- **categories**: Returns the metadata categories for the site

- **operations**: Returns all possible operations on metadata

- **regions**: Returns all geographical regions usable for spatial queries

- **status**: Returns all possible status values for metadata records

Request example::

    <request>
        <type>site</type>
        <type>groups</type>
    </request>

Response
````````

Each type parameter produces an XML subtree in an info container element. An example response to a request for site, categories and groups information would look like the following::

    <info>
        <site>...</site>
        <categories>...</categories>
        <groups>...</groups>
    </info>

The structure of each possible subtree is as follows:

Site
^^^^

- **site**: This is the container for site information

  - **name**: Human readable site name
  - **siteId**: Universal unique identifier (uuid) of the site
  - **platform**: Container for GeoNetwork development version information

    - **name**: Platform name. Always ``geonetwork``.
    - **version**: Platform version, given in the X.Y.Z format
    - **subVersion**: Additional version notes, like ’alpha-1’ or ’beta-2’.
      
Example site information::
  
      <site>
          <name>My site</name>
          <organisation>FAO</organization>
          <siteId>0619cc50-708b-11da-8202-000d9335906e</siteId>
          <platform>
              <name>geonetwork</name>
              <version>2.2.0</version>
          </platform>
      </site>

Users
^^^^^

- **users**: This is the container for user information

  - **user \[0..n]**: A user of the system

    - **id**: The local identifier of the user
    - **username**: The login name
    - **surname**: The user’s surname. Used for display
      purposes.
    - **name**: The user’s name. Used for display purposes.
    - **profile**: User’s profile. eg. Administrator, Editor, UserAdmin etc...
    - **address**: The user’s address.
    - **state**: The user’s state.
    - **zip**: The user’s address zip/postal code.
    - **country**: The user’s country.
    - **email**: The user’s email address.
    - **organisation**: The user’s organisation.
    - **kind**: The type of organisation (eg. NGO, Government)

Example response::
  
      <users>
          <user>
              <id>3</id>
              <username>eddi</username>
              <surname>Smith</surname>
              <name>John</name>
              <profile>Editor</profile>
              <address/>
              <state/>
              <zip/>
              <country/>
              <email/>
              <organisation/>
              <kind>gov</kind>
          </user>
      </users>

Groups
^^^^^^

- **groups**: This is the container for groups

  - **group \[2..n]**: This is a GeoNetwork group. There will always be at
    least two groups: the Internet and Intranet groups. This element has an
    id attribute which represents the local identifier for the group.

    - **name**: Group name
    - **description**: Group description
    - **referrer**: The user responsible for this group
    - **email**: The email address to notify when a data file uploaded with the metadata is downloaded
    - **label**: The localised labels used to show the group in the user interface. See :ref:`localised_entities`.

Example response::
  
      <groups>
          <group id="1">
              <name>editors</name>
              <label>
                  <eng>Editors</eng>
                  <fre>Éditeurs</fre>
              </label>
          </group>
      </groups>

Sources
^^^^^^^

- **sources**: This is the container for sources.

  - **source \[0..n]**: A source known to the GeoNetwork node.

    - **name**: Source name
    - **UUID**: Source universal unique identifier

Example response for a source::
  
      <sources>
          <source>
              <name>My Host</name>
              <UUID>0619cc50-708b-11da-8202-000d9335906e</uuid>
          </source>
      </sources>

Schemas
^^^^^^^

- **schemas**: This is the container for the schema information

  - **schema \[0..n]**: A metadata schema.

    - **name** - the name of the schema - this is the name by which the schema is known to GeoNetwork. It is also the name of the directory in ``GEONETWORK_DATA_DIR/config/schema_plugins`` under which the schema can be found.
    - **id** - A unique identifier assigned to the schema in the ``schema-ident.xml`` file.
    - **version** - a version string assigned to the schema in the ``schema-ident.xml`` file.
    - **namespaces** - namespaces used by the metadata schema and records that belong to that schema. This is a string suitable for use as a namespace definition in an XML file.
    - **edit** - if true then records that use this schema can be edited by GeoNetwork, if false then they can't.
    - **conversions** - information about the GeoNetwork services that can be called to convert metadata that use this schema into other XML formats. If there are valid conversions registered for this schema then this element will have a **converter** child for each one of these conversions. Each **converter** child has the following attributes which are intended to be used when searching for a particular format that may be produced by a conversion:

      - **name** - the name of the GeoNetwork service that invokes the converter
      - **nsUri** - the namespace URI of the XML produced by the conversion
      - **schemaLocation** - the schema location (URL) of the namespace URI
      - **xslt** - the name of the XSLT in the plugin schema convert subdirectory that is invoked by the GeoNetwork service to carry out the conversion.

Example response for schemas:

::
 
 <schemas>
  <schema>
    <name>iso19139</name>
    <id>3f95190a-dde4-11df-8626-001c2346de4c</id>
    <version>1.0</version>
    <namespaces>xmlns:gts="http://www.isotc211.org/2005/gts" xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:gss="http://www.isotc211.org/2005/gss" xmlns:gml="http://www.opengis.net/gml" xmlns:gsr="http://www.isotc211.org/2005/gsr" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:xlink="http://www.w3.org/1999/xlink"</namespaces>
    <convertDirectory>/usr/local/src/git/geonetwork-2.8.x/web/src/main/webapp/WEB-INF/data/config/schema_plugins/iso19139/convert/</convertDirectory>
    <edit>true</edit>
    <conversions>
      <converter name="xml_iso19139" nsUri="http://www.isotc211.org/2005/gmd" schemaLocation="www.isotc211.org/2005/gmd/gmd.xsd" xslt="" />
      <converter name="xml_iso19139Tooai_dc" nsUri="http://www.openarchives.org/OAI/2.0/" schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc.xsd" xslt="oai_dc.xsl" />
    </conversions>
  </schema> 
  ...
 </schemas>

Looking at the example schema (iso19139) above, there are two converters. The first is invoked by calling the GeoNetwork service ``xml_iso19139`` (eg. ``http://somehost/geonetwork/srv/eng/xml_iso19139?uuid=<uuid of metadata>``). It produces an XML format with namespace URI ``http://www.isotc211.org/gmd`` with schemaLocation ``http://www.isotc211.org/2005/gmd/gmd.xsd`` and xslt name ``xml_iso19139`` because the xslt attribute is set to the empty string.

Categories
^^^^^^^^^^

- **categories**: This is the container for categories.

  - **category \[0..n]**: A single GeoNetwork category. This
    element has an id attribute which represents the local
    identifier for the category. 

    - **name**: Category name
    - **label**: The localised labels used to show the category in the user interface. See :ref:`localised_entities`.

Example response::
  
      <categories>
          <category id="1">
              <name>datasets</name>
              <label>
                  <eng>Datasets</eng>
                  <fre>Jeux de données</fre>
              </label>
          </category>
      </categories>

Operations
^^^^^^^^^^

- **operations**: This is the container for the operations

  - **operation \[0..n]**: This is a possible operation on
    a metadata record. This element has an id attribute which represents
    the local identifier for the operation.

    - **name**: Short name for the operation.
    - **reserved**: Can be y or n and is used to
      distinguish between system reserved and user defined
      operations.
    - **label**: The localised labels used to show the operation in the user interface. See :ref:`localised_entities`.

Example response for operations::
  
      <operations>
          <operation id="0">
              <name>view</name>
              <label>
                  <eng>View</eng>
                  <fre>Voir</fre>
              </label>
          </operation>
      </operations>

Regions
^^^^^^^

- **regions**: This is the container for geographical regions

  - **region \[0..n]**: This is a region container element.
    This element has an id attribute which represents the local
    identifier for the operation.

    - **north**: North coordinate of the bounding box.
    - **south**: South coordinate of the bounding box.
    - **west**: West coordinate of the bounding box.
    - **east**: east coordinate of the bounding box.
    - **label**: The localised labels used to show the region in the user interface. See :ref:`localised_entities`.

Example response for regions::
  
      <regions>
          <region id="303">
              <north>82.99</north>
              <south>26.92</south>
              <west>-37.32</west>
              <east>39.24</east>
              <label>
                  <eng>Western Europe</eng>
              </label>
          </region>
      </regions>

Status
^^^^^^

- **statusvalues**: This is the container for the metadata status value information.
 
  - **status \[0..n]**: A metadata status value. This element has an id attribute
    which represents the local identifier of the status value.

    - **name**: The status value name
    - **reserved**: Can be y or n and is used to
      distinguish between system reserved and user defined
      status values.
    - **label**: The localised labels used to show the
      status value in the user interface. See :ref:`localised_entities`.

Example response for status::

  <statusvalues>
    <status id="0">
      <name>unknown</name>
      <reserved>y</reserved>
      <label>
        <eng>Unknown</eng>
      </label>
    </status>
    ...
  </statusvalues>

z3950repositories
^^^^^^^^^^^^^^^^^

- **z3950repositories**: This is the container for the Z3950 repositories that have been configured for this site.
 
  - **repository \[0..n]**: A Z3950 Repository container. 

    - **id**: The repository id. This should be used when referring 
      to the repository in GeoNetwork services (eg. xml.harvest.* services - 
      see :ref:`services_harvesting`).
    - **label**: The human readable name for the repository.

Example response for z3950repositories::

  <z3950repositories>
    <repository>
      <id code="act" serverCode="cbb945ec-36ea-11df-9735-ebfc367b61a6">act</id>
      <label>ACT Geographic Data Directory</label>
    </repository>
    .....
  </z3950repositories>


.. _localised_entities:

Localised entities
``````````````````

Localised entities in the responses from this service have a label element which contains localised strings in all supported languages. This element has a child for each supported language. Each child has a name reflecting the language code and content set to the localised text. Example::

    <label>
        <eng>Editors</eng>
        <fre>Éditeurs</fre>
        <esp>Editores</esp>
    </label>

.. index:: xml.forward

Request Forwarding (xml.forward)
--------------------------------

This is a request forwarding service. It can be used by JavaScript code to 
connect to a remote host because a JavaScript program cannot access any machine other than its server (the same origin policy, see http://en.wikipedia.org/wiki/Same_origin_policy). For example, it is used by the harvesting web interface to query a remote host and retrieve the list of site ids.

Request
```````

The details of the request::

    <request>
        <site>
            <url>...</url>
            <type>...</type>
            <account>
                <username>...</username>
                <password>...</password>
            </account>
        </site>
        <params>...</params>
    </request>

Where:

- **site**: A container for site information where the request will be forwarded.
- **url**: Refers to the remote URL to connect to. Usually it points to a
  GeoNetwork XML service but it can point to any XML service.
- **type**: If set to GeoNetwork then use GeoNetwork authentication schema. 
  Any other value, or if the element is missing, refers to a generic node.
- **account**: This element is optional. If present, the provided credentials will be used to authenticate to the remote site.
- **params**: Container for the request parameters.

Request for info from a remote server::

    <request>
        <site>
            <url>http://mynode.org:8080/geonetwork/srv/eng/xml.info</url>
        </site>
        <params>
            <request>
                <type>site<type>
            </request>
        </params>
    </request>

.. note:: This service uses the proxy configuration. See ``System Configuration`` section of the user manual.

Response
````````

Response from the remote service.

