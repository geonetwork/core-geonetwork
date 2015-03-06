.. _metadata_xml_insert_update_delete:

Metadata insert, update and delete services
===========================================

These services provide insert, update and delete operations for metadata records. They could be used by a metadata editing program external to GeoNetwork.

This is the Create, Update, Delete part of the metadata CRUD operations in GeoNetwork. For read/retrieve operations (the R in CRUD) see :ref:`metadata_xml_search_retrieve`.

.. index:: xml.metadata.insert

Insert metadata (xml.metadata.insert)
-------------------------------------

The **xml.metadata.insert** service allows you to insert a new record into the catalogue. 

Requires authentication: Yes

Request
```````

Parameters:

- **data**: (mandatory) Contains the
  metadata record

- **group** (mandatory): Owner group
  identifier for metadata

- **isTemplate**: indicates if the
  metadata content is a new template or not. Default value:
  "n"

- **title**: Metadata title. Only
  required if isTemplate = "y"

- **category** (mandatory): Metadata
  category. Use "_none_" value to don't assign any
  category

- **styleSheet** (mandatory): Stylesheet
  name to transform the metadata before inserting in the
  catalog. Use "_none_" if you don't have a stylesheet to apply

- **validate**: Indicates if the metadata
  should be validated before inserting in the catalog. Values:
  on, off (default)
  
- **uuidAction**: 

    - nothing (default value): uses the metadata uuid in the xml file, if the uuid already exists in the database throws an exception.
    - overwrite: if the uuid exists in the database, overwrites the metadata with the inserted one.
    - generateUUID: generates a new uuid for the metadata to be inserted.


Insert metadata request example::

  Url:
  http://localhost:8080/geonetwork/srv/eng/metadata.insert

  Mime-type:
  application/xml

  Post request:
  <?xml version="1.0" encoding="UTF-8"?>
  <request>
    <group>2</group>
    <category>_none_</category>
    <styleSheet>_none_</styleSheet>
    <data>
      <gmd:MD_Metadata xmlns:gmd="http://www.isotc211.org/2005/gmd"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      ...
         </gmd:DQ_DataQuality>
        </gmd:dataQualityInfo>
      </gmd:MD_Metadata>
    </data>
  </request>

Response
````````

If request is executed successfully HTTP 200 status code is
returned along with an XML document with id details of the record inserted. 
Example success response:

::
 
 <response>
   <id>31</id>
   <uuid>9c623013-8d90-4e61-ae61-8e96800f3b08</uuid>
 </response>

If request fails an HTTP status code 500 is returned and
the response contains an XML document with the exception. 
Example error response:

::
 
 <error id="error">
   <message>Unique index or primary key violation: "CONSTRAINT_INDEX_1 ON PUBLIC.METADATA(UUID)"; SQL statement: INSERT INTO Metadata (id, schemaId, data, createDate, changeDate, source, uuid, is Template, isHarvested, root, owner, doctype, groupOwner) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) [23001-152]</message>
   <class>JdbcSQLException</class>
   <stack>..</stack>
   <request>...</request>
 </error>

See :ref:`exception_handling` for more details.

If validate parameter is set to "on" and the provided metadata
is not valid with respect to the XSD and schematrons in use for the metadata 
schema then an exception report is returned.

Example validation metadata report:

::

  <?xml version="1.0" encoding="UTF-8"?>
  <error id="xsd-validation-error">
    <message>XSD Validation error(s)</message>
    <class>XSDValidationErrorEx</class>
    <stack>
      <at class="org.fao.geonet.services.metadata.ImportFromDir"
        file="ImportFromDir.java" line="297" method="validateIt" />
      <at class="org.fao.geonet.services.metadata.ImportFromDir"
        file="ImportFromDir.java" line="281" method="validateIt" />
      <at class="org.fao.geonet.services.metadata.Insert"
        file="Insert.java" line="102" method="exec" />
      <at class="jeeves.server.dispatchers.ServiceInfo"
        file="ServiceInfo.java" line="238" method="execService" />
      <at class="jeeves.server.dispatchers.ServiceInfo"
        file="ServiceInfo.java" line="141" method="execServices" />
      <at class="jeeves.server.dispatchers.ServiceManager"
        file="ServiceManager.java" line="377" method="dispatch" />
      <at class="jeeves.server.JeevesEngine"
        file="JeevesEngine.java" line="621" method="dispatch" />
      <at class="jeeves.server.sources.http.JeevesServlet"
        file="JeevesServlet.java" line="174" method="execute" />
      <at class="jeeves.server.sources.http.JeevesServlet"
        file="JeevesServlet.java" line="99" method="doPost" />
      <at class="javax.servlet.http.HttpServlet"
        file="HttpServlet.java" line="727" method="service" />
    </stack>
    <object>
      <xsderrors>
        <error>
          <message>ERROR(1) org.xml.sax.SAXParseException: cvc-datatype-valid.1.2.1: '' is not a valid value for 'dateTime'. (Element: gco:DateTime with parent element: gmd:date)</message>
          <xpath>gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:DateTime</xpath>
        </error>
        <error>
          <message>ERROR(2) org.xml.sax.SAXParseException: cvc-type.3.1.3: The value '' of element 'gco:DateTime' is not valid. (Element: gco:DateTime with parent element: gmd:date)</message>
          <xpath>gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:DateTime</xpath>
        </error>
        <error>
          <message>ERROR(3) org.xml.sax.SAXParseException: cvc-datatype-valid.1.2.1: '' is not a valid value for 'integer'. (Element: gco:Integer with parent element: gmd:denominator)</message>
          <xpath>gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialResolution/gmd:MD_Resolution/gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer</xpath>
        </error>
        <error>
          <message>ERROR(4) org.xml.sax.SAXParseException: cvc-type.3.1.3: The value '' of element 'gco:Integer' is not valid. (Element: gco:Integer with parent element: gmd:denominator)</message>
          <xpath>gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialResolution/gmd:MD_Resolution/gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer</xpath>
        </error>
      </xsderrors>
    </object>
    <request>
      <language>eng</language>
      <service>xml.metadata.insert</service>
    </request>
  </error>

Errors
``````

- **Service not allowed (error id:
  service-not-allowed)**, when the user is not
  authenticated or their profile has no rights to execute the
  service. Returns 500 HTTP code

- **Missing parameter (error id:
  missing-parameter)**, when mandatory parameters are
  not provided. Returns 500 HTTP code

- **bad-parameter XXXX**, when a
  mandatory parameter is empty. Returns 500 HTTP code

- **ERROR: duplicate key violates unique
  constraint "metadata_uuid_key"**, if another
  metadata record in catalog has the same uuid of the metadata
  record being inserted. Returns 500 HTTP code

.. index:: xml.metadata.update

Update metadata (xml.metadata.update)
-------------------------------------

The **xml.metadata.update** service allows you to update a metadata record in the catalog.

Requires authentication: Yes

Request
```````

Parameters:

- **id** Identifier of the metadata to update

- **version**: (mandatory) This parameter
  is used by the GeoNetwork editor to avoid concurrent updates to the same
  metadata record. This is not accessible to the service user at present so
  this parameter can be set to any integer value.

- **isTemplate**: indicates if the
  metadata content is a new template or not. Default value: "n"

- **showValidationErrors**: Indicates if
  the metadata should be validated before updating in the
  catalog.

- **minor**: If the metadata update is a minor change (changedate will not be
  updated, notification of change in metadata will not be sent) then this 
  parameter should be set to "true", "false" otherwise.

- **title**: Metadata title (for templates)

- **data** (mandatory) Contains the metadata record.

Update metadata request example::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.update

  Mime-type:
  application/xml

  Post request:

  <?xml version="1.0" encoding="UTF-8"?>
  <request>
    <id>11</id>
    <version>1</version>
    <data><![CDATA[
      <gmd:MD_Metadata xmlns:gmd="http://www.isotc211.org/2005/gmd"
                       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      
      ...
      
            </gmd:DQ_DataQuality>
        </gmd:dataQualityInfo>
      </gmd:MD_Metadata>]]>
    </data>
  </request>

Response
````````

If request is executed successfully HTTP 200 status code is
returned and an XML document with details of the successful request.
Example success response:

::
 
  <response>
    <id>32</id>
    <showvalidationerrors>false</showvalidationerrors>
    <minor>false</minor>
  </response>


If request fails an HTTP status code 500 (server error) is returned and
the response is an XML document with the exception.
Example error response:

::
 
  <error id="bad-parameter">
    <message>id</message>
    <class>BadParameterEx</class>
    <stack>...</stack>
    <request>...</request>
  </error>

See :ref:`exception_handling` for more details.

Errors
``````

- **Service not allowed (error id:
  service-not-allowed)**, when the user is not
  authenticated or his profile has no rights to execute the
  service. Returns 500 HTTP code

- **Missing parameter (error id:
  missing-parameter)**, when mandatory parameters are
  not provided. Returns 500 HTTP code

- **bad-parameter XXXX**, when a
  mandatory parameter is empty or when the update id doesn't exist. 
  Returns 500 HTTP code

- **Concurrent update (error id:
  client)**, when the version number provided is
  different from the current version number (Metadata record is in use by 
  another user). Returns 500 HTTP code

.. index:: xml.metadata.delete

Delete metadata (xml.metadata.delete)
-------------------------------------

The **xml.metadata.delete** service removes a 
metadata record from the catalog. The metadata record is
backed up in MEF format in ``GEONETWORK_DATA_DIR/removed``. 

Requires authentication: Yes

Request
```````

Parameters:

- **id** or **uuid**: (mandatory) Identifier of the metadata to delete

Example request::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.delete

  Mime-type:
  application/xml

  Post request:
  <?xml version="1.0" encoding="UTF-8"?>
  <request>
    <id>10</id>
  </request>

Response
````````

If request executed successfully HTTP 200 status code is
returned and an XML document with details of what has been deleted. 
Example success response:

::
 
 <response>
   <id>32</id>
 </response>

If request fails an HTTP 500 status code error is returned and
the response is an XML document with the exception.
Example error response:

::
 
 <error id="metadata-not-found">
   <message>Metadata not found</message>
   <class>MetadataNotFoundEx</class>
   <stack>...</stack>
   <request>...</request>
 </error>

See :ref:`exception_handling` for more details.

Errors
``````

- **Service not allowed (error id:
  service-not-allowed)**, when the user is not
  authenticated or their profile has no rights to execute the
  service. Returns 500 HTTP code

- **Metadata not found (error id:
  metadata-not-found)**, if the identifier provided did not correspond
  to an existing metadata record. Returns 500 HTTP code

- **Operation not allowed** **(error id: operation-not-allowed)**, when
  the user is not authorized to edit the metadata. To edit a metadata one of the
  following must be true:
  
  - The user is the metadata owner
  - The user is an Administrator
  - The user has edit rights over the metadata
  - The user is a Reviewer and/or UserAdmin and the
    metadata groupOwner is one of his groups
	
  Returns 500 HTTP code.


.. index:: xml.metadata.batch.delete

.. _metadata.batch.delete:

Batch Delete (xml.metadata.batch.delete)
----------------------------------------

The **xml.metadata.batch.delete** service deletes the metadata records in the selected set.

.. note:: This service requires a previous call to the ``xml.metadata.select`` service (see :ref:`metadata.select`) to select the metadata records to delete.

.. note:: Only those metadata records for which the user running the service has ownership rights on will be deleted. If metadata versioning is on then deletions will be recorded in the version history.

Requires authentication: Yes

Request
-------

Parameters: **None**

Example request:

**POST**::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.batch.delete

  Mime-type:
  application/xml

  Post request:
  <?xml version="1.0" encoding="UTF-8"?>
  <request/>

**GET**::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.batch.delete

Response
````````

If the request executed successfully then HTTP 200 status code is returned and 
an XML document with a summary of how the metadata records in the selected set 
have been processed. An example of such a response is shown below:

::
 
 <response>
 	 <done>5</done>
   <notOwner>0</notOwner>
   <notFound>0</notFound>
 </response>

The response fields are:

- **done** - number of metadata records successfully deleted
- **notOwner** - number of metadata records skipped because the user running this service did not have ownership rights
- **notFound** - number of metadata records skipped because they were not found (may have been deleted)

If the request fails an HTTP 500 status code error is returned and
the response is an XML document with the exception. An example of such a response is shown below:

::
 
 <error id="service-not-allowed">
   Service not allowed
   <object>xml.metadata.batch.delete</object>
 </error>

See :ref:`exception_handling` for more details.


Errors
``````

- **Service not allowed (error id:
  service-not-allowed)**, when the user is not
  authenticated or their profile has no rights to execute the
  service. Returns 500 HTTP code
