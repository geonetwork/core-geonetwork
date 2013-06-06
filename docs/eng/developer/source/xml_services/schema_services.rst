.. _schema_services:

Schema Services
===============

Metadata schemas can be plugged into GeoNetwork - see :ref:`schemaPlugins`. Any application that needs to:

- find information (eg. names, versions, namespaces, converters) about metadata schemas that are plugged into GeoNetwork should use the :ref:`xml.info` service
- find information about the schema elements and codelists should use the ``xml.schema.info`` service described in this section of the manual
- add, delete, update schema plugins in GeoNetwork should use the ``xml.metadata.schema.add``, ``xml.metadata.schema.delete`` and ``xml.metadata.schema.update`` services described in this section of the manual 

Metadata Schema Information (xml.info?type=schemas)
---------------------------------------------------

See :ref:`xml.info` for more details.

.. index:: xml.schema.info

Schema Element and Codelist Information (xml.schema.info)
---------------------------------------------------------

This service returns information about a set of schema elements or codelists.
The returned information consists of a localised label, a description,
conditions that the element must satisfy etc.

Request
```````

Requests to this service can only be made using the HTTP POST binding with
application/XML content type. Requests can ask for information on multiple  
elements and codelists from different schemas. Description of the request fields
is as follows:

- **element**: Must contain a **schema** and a **name** attribute. The schema attribute must be the name of a schema currently registered in GeoNetwork (see :ref:`xml.info` for more details). The name attribute must be the qualified name of the metadata schema element on which information is required. Other optional attributes can be specified to help determine the appropriate context for the metadata element. These optional attributes are:

  - **context**: The qualified name of the metadata schema element that is the parent of the element specified in the **name** attribute.
  - **fullContext**: The qualified xpath of the element specified in the **name** attribute.
  - **isoType**: For profiles of ISO19115/19139 only. The qualified name of the element in the base ISO19115/19139 schema that the element specified in the **name** attribute is a substitute for. eg. in the Marine Community Profile of ISO19115/19139, ``mcp:MD_Metadata`` is a substitute for ``gmd:MD_Metadata`` in the base ISO19115/19139 metadata schema.

- **codelist**: Accepts a **schema** and **name** attribute as for **element** but information on any codelist associated with the qualified name of the metadata schema element in the **name** attribute will be returned instead.

::

    <request>
        <element schema="iso19139" name="gmd:constraintLanguage" />
        <codelist schema="iso19115" name="DateTypCd" />
    </request>

.. note:: The text returned is localised into the language specified in the 
  the service call. eg. A call to /geonetwork/srv/eng/xml.schema.info
  will return text in the English (eng) language.

Response
````````

If the request executed successfully then HTTP status code 200 will be returned along with an XML document containing the response. The root field of the response will be populated with information about the element/codelist specified in the request. The fields of the response are:

- **element**: A container for information about a schema element. It has a
  name attribute which contains the qualified name of the element.

  - **label**: The human readable name of the element, localised
    into the language specified in the request.
  - **description**: A generic description of the element.
  - **condition \[0..1]**: This field is optional and indicates
    if the element must satisfy a condition eg. mandatory. The condition text
    is displayed with the element name so it intended to be human readable.

- **codelist**: A container for information about a codelist. It has a
  name attribute which contains the qualified name of the codelist.

  - **entry \[1..n]**: A container for a codelist entry. There can
    be more than one entry.

    - **code**: The entry code. This is the value that
      will be used in the metadata.
    - **label**: This is a human readable name for the code, used to
      show the code in the user interface. It is localised.
    - **description**: A localised description/meaning of the code. The 
      description is shown in the user interface with the label.

::

    <response>
        <element name="gmd:constraintLanguage">
            <label>Constraint language</label>
            <description>language used in Application Schema</description>
            <condition>mandatory</condition>
        </element>
        <codelist name="DateTypCd">
            <entry>
                <code>creation</code>
                <label>Creation</label>
                <description>date when the resource was brought into existence</description>
            </entry>
            <entry>
                <code>publication</code>
                <label>Publication</label>
                <description>date when the resource was issued</description>
            </entry>
            <entry>
                <code>revision</code>
                <label>Revision</label>
                <description>date identifies when the resource was examined
                or re-examined and improved or amended</description>
            </entry>
        </codelist>
    </response>

Error management
````````````````

If an exception occurs during the processing of the request, then an HTTP 500 
status code is returned along with an XML document describing the exception. See :ref:`exception_handling` for more details. 

Apart from exceptions, the service can encounter errors trying to retrieve an element/codelist information eg. if the requested element is not present. If such an error is encountered, then the object is copied from the response and an error attribute is added describing the error. An example of such a response is::

    <response>
        <element schema="iso19139" name="blablabla" error="not-found"/>
    </response>

.. _table_schema_errors:

Possible errors returned by xml.schema.info service:

=================   ============================================================
Error code          Description
=================   ============================================================
unknown-schema      The specified schema is not supported
unknown-namespace   The namespace of the specified prefix was not found
not-found           The requested element / codelist was not found
=================   ============================================================

.. index:: xml.metadata.schema.add

Add a metadata schema (xml.metadata.schema.add)
-----------------------------------------------

The **xml.metadata.schema.add** service can be used to add a metadata schema to GeoNetwork. The details of what the schema should contain are covered in the :ref:`schemaPlugins` section of this manual. 

Only **Administrator** users can run this service.

Requires authentification: Yes

Request
```````

Parameters:

- **schema**: (mandatory) Name of the schema to add.

*One* of the following parameters:

- **fname**: Server file name (full path) to metadata schema zip archive.
- **url**: Http URL of metadata schema zip archive.
- **uuid**: Uuid of metadata record in current catalog that has a metadata schema zip archive uploaded and stored with it.

Schema add request example::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.schema.add

  Mime-type:
  application/xml

  Post request:
  <request>
    <schema>iso19139.mcp</schema>
    <fname>/usr/local/src/git/schemaPlugins-2.8.x/dist/iso19139.mcp.zip</fname>
  </request>

Response
````````

If the request executed succesfully then an HTTP 200 status code is
returned and an XML document confirming success is returned. An example response is:::
 
 <response status="ok" message="Schema iso19139.mcp has been added/updated"/>

If the request fails then an HTTP 500 status code error is returned
and the response contains an XML document with the details of the exception/what
went wrong. An example error response is:::
 
 <error id="operation-aborted">
   <message>Schema already exists</message>
   <class>OperationAbortedEx</class>
   .....
 </error>
  
See :ref:`exception_handling` for more details.

.. index:: xml.metadata.schema.update

Update a metadata schema (xml.metadata.schema.update)
-----------------------------------------------------

The **xml.metadata.schema.update** service can be used to update a metadata schema in GeoNetwork. The details of what the schema should contain are covered in the :ref:`schemaPlugins` section of this manual. 

Only **Administrator** users can run this service.

Requires authentification: Yes

Request
```````

Parameters:

- **schema**: (mandatory) Name of the schema to update. Must be the name of a currently registered metadata schema in GeoNetwork.

*One* of the following parameters:

- **fname**: Server file name (full path) to metadata schema zip archive.
- **url**: Http URL of metadata schema zip archive.
- **uuid**: Uuid of metadata record in current catalog that has a metadata schema zip archive uploaded and stored with it.

Schema update request example::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.schema.update

  Mime-type:
  application/xml

  Post request:
  <request>
    <schema>iso19139.mcp</schema>
    <fname>/usr/local/src/git/schemaPlugins-2.8.x/dist/iso19139.mcp.zip</fname>
  </request>

Response
````````

If the request executed succesfully then an HTTP 200 status code is
returned and an XML document confirming success is returned. An example response is:::
 
 <response status="ok" message="Schema iso19139.mcp has been added/updated"/>

If the request fails then an HTTP 500 status code error is returned
and the response contains an XML document with the details of the exception/what
went wrong. An example error response is:::
 
 <error id="operation-aborted">
   <message>Schema doesn't exist</message>
   <class>OperationAbortedEx</class>
   .....
 </error>
  
See :ref:`exception_handling` for more details.

.. index:: xml.metadata.schema.delete

Delete a metadata schema (xml.metadata.schema.delete)
-----------------------------------------------------

The **xml.metadata.schema.delete** service can be used to delete a metadata schema in GeoNetwork. A metadata schema can only be deleted if:

- there are no metadata records in the catalog that use it 
- no other metadata schema is dependent on it

Only **Administrator** users can run this service.

Requires authentification: Yes

Request
```````

Parameters:

- **schema**: (mandatory) Name of the schema to delete. Must be the name of a currently registered metadata schema in GeoNetwork.

Schema delete request example::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.schema.delete

  Mime-type:
  application/xml

  Post request:
  <request>
    <schema>iso19139.mcp</schema>
  </request>

Response
````````

If the request executed without an exception then an HTTP 200 status code is
returned and an XML document giving status is returned. An example response is:::
 
 <response status="ok" message="Schema iso19139.mcp has been deleted"/>

Other responses may describe errors, in which case the status is set to "error". An example error response is:::
 
 <response status="error" message="Cannot remove schema iso19139 because there are records that belong to this schema in the catalog"/>

If the request fails due to an exception in the service then an HTTP 500 status code error is returned and the response contains an XML document with the details of the exception/what went wrong. An example error response is:::
 
 <error id="operation-aborted">
   <message>Schema doesn't exist</message>
   <class>OperationAbortedEx</class>
   .....
 </error>
  
See :ref:`exception_handling` for more details.
