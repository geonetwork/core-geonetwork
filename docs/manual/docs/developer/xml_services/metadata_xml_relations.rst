.. _metadata_xml_relations:

Metadata Relation services
==========================

This section describes the services used to show, get, insert and delete relations between metadata records in GeoNetwork. If the metadata schema has elements that support relationships between metadata records (eg. ISO19115/19139), then the relationships are stored in the Lucene index with the metadata record. If a relationship concept does not exist in the metadata schema, then the relationship is stored in the Relations table as follows:

==========  ============================    ====================================
Field       Datatype                        Description
==========  ============================    ====================================
id          foreign key to Metadata(id)     Source metadata 
relatedId   foreign key to Metadata(id)     Metadata related to the source
==========  ============================    ====================================

.. index:: xml.relation

Get all related records for a metadata record (xml.relation)
------------------------------------------------------------

This service retrieves all the related records for a source metadata record specified by id in the parameters. The relationships can come from the Lucene index and/or the Relations table in the database. 

Request
```````

- **id (integer)**: This is the local GeoNetwork
  identifier of the metadata whose relations are requested.

Here is an example of POST/XML request::

    <request>
        <id>10</id>
    </request>

Response
````````

If the request executed successfully then HTTP status code 200 is returned along with an XML document containing a relations root element and a relation child for each type of relation found. Example::
 
 <relations>
   <relation type="parent">
      <id>3</id>
      <uuid>da165110-88fd-11da-a88f-000d939bc5d8</uuid>
      <title>....</title>
      <abstract>....</abstract>
   </relation>
 </relations>

Each relation element has a type attribute indicating the type of relation with the metadata record id specified in the parameters. The XML elements returned vary for each type attribute as follows:

- **relation type=parent**: elements describe the parent metadata record of the specified metadata record:

 - **id**: GeoNetwork internal id (integer)
 - **uuid**: Metadata uuid
 - **title**: Metadata title
 - **abstract**: A brief explanation of the metadata

- **relation type=children**: **metadata** element describes the child metadata record of the specified metadata record

 - **metadata**: container for child metadata record
 
   - **title**: Metadata title
   - **abstract**: A brief explanation of the metadata
	 - *Other elements returned by a brief presentation of the child metadata record*

- **relation type=services**: multiple **metadata** elements describing the service metadata records that operate on the specified metadata record:

 - **metadata**: container for a service metadata record
 
   - **title**: Metadata title
   - **abstract**: A brief explanation of the metadata
	 - *Other elements returned by a brief presentation of the service metadata record*

- **relation type=fcats**: multiple **metadata** elements describing the feature catalog metadata records that are related to the specified metadata record

 - **metadata**: container for feature catalog metadata record
 
   - **title**: Metadata title
   - **abstract**: A brief explanation of the metadata
	 - *Other elements returned by a brief presentation of the feature catalog metadata record*

- **relation type=hasfeaturecat**: **metadata** element describing the metadata record that has a feature catalog relation to this feature catalog record

 - **metadata**: container for metadata record
 
   - **title**: Metadata title
   - **abstract**: A brief explanation of the metadata
	 - *Other elements returned by a brief presentation of the metadata record*

- **relation type=related**: elements describe a related metadata record to the specified metadata record:

 - **id**: GeoNetwork internal id (integer)
 - **uuid**: Metadata uuid
 - **title**: Metadata title
 - **abstract**: A brief explanation of the metadata

If the response did not execute successfully then an HTTP error code 500 is returned along with an XML document describing the exception/what went wrong. An example of such an error response is:::
 
 <error id="metadata-not-found">
   <message>Metadata not found</message>
   <class>MetadataNotFoundEx</class>
   .....
 </error>

See :ref:`exception_handling` for more details.

Manage Relationships in the Relations table
-------------------------------------------

These services manage relationships between metadata records that are held in the Relations table ie. they are relationships that cannot be held in a metadata record.

.. index:: xml.relation.get

xml.relation.get
````````````````

This service retrieves all the related records for a source metadata record specified by id in the parameters. The related records are those that are in the Relations table ie. they are those that cannot be held in a metadata record.

Request
^^^^^^^

- **id (integer)** or **uuid**: This is the local GeoNetwork
  identifier of the metadata or uuid of metadata whose relations are requested.

- **relation (string, ’normal’)**: This optional
  parameter identifies the kind of relation that the client wants to
  be returned. It can be one of these values:

  - **normal**: The service performs a query into the id field
    and returns all relatedId records.
  - **reverse**: The service performs a query into the relatedId
    field and returns all id records.
  - **full**: Includes both normal and reverse queries
    (duplicated ids are removed).

Here is an example of POST/XML request::

    <request>
        <id>10</id>
        <relation>full</relation>
    </request>

Response
^^^^^^^^

If the request executed successfully then HTTP status code 200 is returned along with an XML document containing a response root element and metadata children
depending on the relations found. Example::

    <response>
        <metadata>...</metadata>
        <metadata>...</metadata>
        ...
    </response>

Each metadata element has the the structure returned by the brief template of the metadata schema presentation XSLT. Typical brief elements are:

- **title**: Metadata title
- **abstract**: A brief explanation of the metadata
- **keyword**: Keywords found inside the metadata
- **image**: Information about thumbnails
- **link**: A link to the source site
- **geoBox**: coordinates of the bounding box
- **geonet:info**: A container for GeoNetwork related information

Example of a brief metadata record presentation for *fgdc-std*::

    <metadata>
        <title>Globally threatened species of the world</title>
        <abstract> Contains information on animals.</abstract>
        <keyword>biodiversity</keyword>
        <keyword>endangered animal species</keyword>
        <keyword>endangered plant species</keyword>
        <link type="url">http://www.mysite.org</link>
        <geoBox>
            <westBL>-180.0</westBL>
            <eastBL>180.0</eastBL>
            <southBL>-90.0</southBL>
            <northBL>90.0</northBL>
        </geoBox>
        <geonet:info>
            <id>11</id>
            <schema>fgdc-std</schema>
            <createDate>2005-03-31T19:13:31</createDate>
            <changeDate>2007-03-12T14:52:46</changeDate>
            <isTemplate>n</isTemplate>
            <title/>
            <source>38b75c1b-634b-443e-9c36-a12e89b4c866</source>
            <UUID>84b4190b-de43-4bd7-b25f-6ed47eb239ac</uuid>
            <isHarvested>n</isHarvested>
            <view>true</view>
            <admin>false</admin>
            <edit>false</edit>
            <notify>false</notify>
            <download>true</download>
            <dynamic>false</dynamic>
            <featured>false</featured>
        </geonet:info>
    </metadata>

If the response did not execute successfully then an HTTP error code 500 is returned along with an XML document describing the exception/what went wrong. See :ref:`exception_handling` for more details.

.. note:: this service returns an empty response if the metadata record specified in the parameters doesn't exist.

.. index:: xml.relation.insert

xml.relation.insert
```````````````````

This service creates a relationship between a parent metadata record and a child metadata record. The relationship is held in the Relations table ie. relationships inserted using this service are those that cannot be held in a metadata record.

Request
^^^^^^^

- **parentId (integer)** or **parentUuid**: This is the 
  identifier of the metadata which we are inserting a relationship for.

- **childId (integer)** or **parentUuid**: This is the 
  identifier of the metadata which will be related to metadata record 
  specified by **parentId** or **parentUuid**.

Here is an example of POST/XML request::

    <request>
        <parentId>1</parentId>
        <childId>2</childId>
    </request>

Response
^^^^^^^^

Normally an HTTP status code 200 is returned along with an XML document containing a response root element with an attribute indicating whether the relationship already exists and the parentId and childId parameters from the request. Example::
 
 <response alreadyExist="false">
   <parentId>1</parentId>
   <childId>2</childId>
 </response>

.. note:: this service returns this response even if the metadata records specified in the parameters do not exist.

If the response did not execute successfully then an HTTP error code 500 is returned along with an XML document describing the exception/what went wrong. See :ref:`exception_handling` for more details.

.. index:: xml.relation.delete

xml.relation.delete
```````````````````

This service deletes a relationship between a parent metadata record and a child metadata record. The relationship is held in the Relations table ie. relationships removed using this service are those that cannot be held in a metadata record.

Request
^^^^^^^

- **parentId (integer)** or **parentUuid**: This is the 
  identifier of the metadata which we are removing the relationship from.

- **childId (integer)** or **parentUuid**: This is the 
  identifier of the metadata which is related to metadata record 
  specified by **parentId** or **parentUuid**.

Here is an example of POST/XML request::

    <request>
        <parentId>1</parentId>
        <childId>2</childId>
    </request>

Response
^^^^^^^^

Normally an HTTP status code 200 is returned along with an XML document with an empty response root element.
 
.. note:: this service returns an empty response regardless of whether the parent and/or child metadata metadata records specified in the id parameters exist or not.

If the response did not execute successfully then an HTTP error code 500 is returned along with an XML document describing the exception/what went wrong. See :ref:`exception_handling` for more details.
