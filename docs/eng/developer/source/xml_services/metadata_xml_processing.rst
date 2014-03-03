.. _metadata_xml_processing:

Metadata Processing services
============================

These services allow processing of one or more metadata records using an XSLT.

Rules for constructing a process XSLT for use with these services  
-----------------------------------------------------------------

Typically an XSLT used with this service will accept some parameters then filter the elements of the metadata record being processed, changing some elements and copying the rest. So the rules for constructing such an XSLT are:

- Accept parameters using ``xsl:param`` - values for these will be specified as part of the request. eg. ``<xsl:param name="url"/>``
- Add templates that match and process the required metadata elements. For example:

::
 
 <xsl:template match="gmd:identificationInfo/*">
 	<!-- Do some work processing and/or copying contents of this element -->
 </xsl:template>

- Include a template that matches all content, refers any specific matches to templates provided in the previous step or just copies the metadata elements (nodes) and attributes without changing them.

::
 
 <xsl:template match="@*|node()">
   <xsl:copy>
     <xsl:apply-templates select="@*|node()"/>
   </xsl:copy>
 </xsl:template>

- Put the process XSLT into the process directory of the relevant metadata schema plugin. eg. if your process XSLT applies to iso19139 metadata records then it should be in the process directory of the iso19139 schema (``GEONETWORK_DATA_DIR/config/schema_plugins/iso19139/process``).

.. index:: xml.metadata.processing

Process a metadata record with an XSLT (xml.metadata.processing)
----------------------------------------------------------------

This service applies an XSLT to a metadata record specified by **id** or **uuid**.

Request
```````
Parameters:

- **id** or **uuid**: Identifier of metadata to process.

- **save**: Set to '1' to save the processed metadata (default), '0' will not save the processed metadata and will return the processed metadata for inspection.

- **process**: Name of an XSLT in the process directory of a metadata schema in GeoNetwork. For example, ``anonymizer.xsl`` exists in the process directory of metadata schema iso19139 - to use this XSLT you would specific ``anonymizer`` as the process parameter value.

- Parameters of the process XSLT in order. Each parameter of the process XSLT needs to be specified with a value if no default exists in the process XSLT or with no value if the default is suitable. You will need to examine the process XSLT to determine which parameters to specify and what the default values are if any.

Example request for the anonymizer process XSLT:

**POST**::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.processing

  Mime-type:
  application/xml

  Post request:
  <?xml version="1.0" encoding="UTF-8"?>
  <request>
    <id>6</id>
    <save>0</save>
    <process>anonymizer</process>
    <email>john.p.bead@bonce.com<email/>
  </request>

**GET**::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.processing?&id=6&save=0&process=anonymizer&email=john.p.bead%40bonce.com

Response
````````

If the processing specified in the request succeeded and the parameter ``save`` was set to '1' or left out, then the XML response contains the id of the metadata record that was processed. For example:

::
 
 <response>
  <id>1</id>
 </response>

If the processing specified in the request succeeded and the parameter ``save`` was set to '0', then the XML response contains the id of the metadata record and the processed metadata record. For example, if processing an iso19139 metadata record then the response would contain the processed iso19139 metadata record as follows:

::
 
 <response>
  <id>2</id>
  <record>
    <gmd:MD_Metadata ...>
      .....
    </gmd:MD_Metadata>
  </record>
 </response>

If the processing specified in the request failed, an XML error response is returned with the reason. For example, here is the response when processing was requested on a metadata record belonging to a metadata schema that does not have the specified processing XSLT:

::
 
 <error id="bad-parameter">
   <message>Processing failed</message>
   .....
   <object>Not found:0, Not owner:0, No process found:1.</object>
   .....
 </error>

See :ref:`exception_handling` for more details.

Errors
``````

- **Service not allowed (error id:
  service-not-allowed)**, when the user is not
  authenticated or their profile has no rights to execute the
  service. Returns 500 HTTP code

- **Bad Parameter (error id:
  bad-parameter)**, when the processing (XSLT transform) returns
  an empty metadata record (explanation is returned in XML - see example response
  above). Returns 500 HTTP code

.. index:: xml.metadata.batch.processing

.. _metadata.batch.processing:

Batch process metadata records with an XSLT (xml.metadata.batch.processing)
---------------------------------------------------------------------------

The **xml.metadata.batch.processing** service applies an XSLT to each metadata record in a selected set of metadata records. 

.. note:: This service requires a previous call to the ``xml.metadata.select`` service (see :ref:`metadata.select`) to select metadata records.

.. note:: This service is only available to users with UserAdmin or Administrator profile.

.. note:: Only those metadata records for which the user running the service has editing rights on will be processed. If metadata versioning is on then any changes  to the metadata records will be recorded in the version history.

Requires authentication: Yes

Request
```````

Parameters:

- **save**: Set to '1' to save the processed metadata (default), '0' will not save the processed metadata.

- **process**: Name of an XSLT in the process directory of a metadata schema in GeoNetwork. For example, the anonymizer XSLT exists in the process directory of metadata schema iso19139 - to use this XSLT you would specific ``anonymizer`` as the process parameter value.

- Parameters of the process XSLT in order. Each parameter of the process XSLT needs to be specified with a value if no default exists in the process XSLT or with no value if the default is suitable. You will need to examine the process XSLT to determine which parameters to specify and what the default values are if any.

Example request for the anonymizer process XSLT:

**POST**::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.batch.processing

  Mime-type:
  application/xml

  Post request:
  <?xml version="1.0" encoding="UTF-8"?>
  <request>
    <save>0</save>
    <process>anonymizer</process>
    <email>john.p.bead@bonce.com<email/>
  </request>

**GET**::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.batch.processing?&save=0&process=anonymizer&email=john.p.bead%40bonce.com

Response
````````

If the request executed successfully then HTTP 200 status code is returned and
an XML document with a summary of how the metadata records in the selected set 
have been processed. An example of such a response is shown below:

::
 
 <response>
   <done>5</done>
   <notProcessFound>2</notProcessFound>
   <notOwner>0</notOwner>
   <notFound>0</notFound>
 </response>

The response fields are:

- **done** - number of metadata records successfully updated
- **notProcessFound** - number of metadata records skipped because the process XSLT was not present in their metadata schema
- **notOwner** - number of metadata records skipped because the user running this service did not have ownership rights
- **notFound** - number of metadata records skipped because they were not found (may have been deleted)

If the request fails an HTTP 500 status code error is returned and
the response is an XML document with the exception. An example of such a response is shown below:

::
 
 <error id="service-not-allowed">
   <message>Service not allowed</message>
   .....
   <object>xml.metadata.batch.processing</object>
   .....
 </error>

See :ref:`exception_handling` for more details.

Errors
``````

- **Service not allowed (error id:
  service-not-allowed)**, when the user is not
  authenticated or their profile has no rights to execute the
  service. Returns 500 HTTP code

.. index:: xml.metadata.batch.update.children

Batch update child records (xml.metadata.batch.update.children)
---------------------------------------------------------------

The **xml.metadata.batch.update.children** service copies metadata elements from the parent metadata record to all child metadata elements. 

- This service works only for iso19139 (or profile) child metadata records ie. metadata records whose gmd:parentIdentifier is set to the uuid of a metadata record in the catalog.
- Any child metadata records that do not have the same metadata schema as the parent metadata record will be skipped.
- The service actually executes an XSLT in the metadata schema directory of the parent metadata record. The XSLT is called ``update-child-from-parent-info.xsl``. It is run on each child metadata record and is passed parameters from the request as required. This design has been chosen to make customization of the service reasonably straight forward.

.. note:: If user of this service does not have edit privileges over a child metadata record then that record will be skipped.


Requires authentication: Yes

Request
```````

Parameters:

- **id**: GeoNetwork internal integer id of parent metadata record.
- **parentUuid**: Uuid of parent metadata record.
- **schema**: Metadata schema name in GeoNetwork.
- **childrenIds**: GeoNetwork internal integer ids of child metadata records (comma separated)
- **updateMode**: 'replace' means replace content in the children with content from the parent. 'add' means add content from the parent to the child metadata records. 
- Parameters for ``update-child-from-parent-info.xsl``. Examine the relevant XSLT to determine which parameters to specify.

Example request:

**POST**::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.batch.update.children

  Mime-type:
  application/xml

  Post request:
  <?xml version="1.0" encoding="UTF-8"?>
  <request>
   <id>1</id>
   <parentUuid>da165110-88fd-11da-a88f-000d939bc5d8</parentUuid>
   <childrenIds>4,</childrenIds>
   <schema>iso19139</schema>
   <updateMode>replace</updateMode>
   <gmd-descriptiveKeywords>true</gmd-descriptiveKeywords>
   <gmd-contact>true</gmd-contact>
   <gmd-extent>true</gmd-extent>
   <gmd-pointOfContact>true</gmd-pointOfContact>
   <gmd-metadataMaintenance>true</gmd-metadataMaintenance>
  </request>

**GET**::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.batch.update.children?&id=1&parentUuid=da165110-88fd-11da-a88f-000d939bc5d8&childrenIds=4,&schema=iso19139&updateMode=replace&gmd-descriptiveKeywords=true&gmd-contact=true&gmd-extent=true&gmd-pointOfContact=true&gmd-metadataMaintenance=true

Response
````````

If the request executed successfully a HTTP 200 status code is
returned and some XML describing what was processed. An example of such an XML
response is:

::
 
 <response>1 child/children updated for metadata da165110-88fd-11da-a88f-000d939bc5d8.</response>

If the request fails an HTTP 500 status code error is returned and
the response contains an XML document with details of the exception. An example of such a response is::
 
 <error id="metadata-not-found">
   <message>Metadata not found</message>
   <class>MetadataNotFoundEx</class>
   .....
   <object>Could not find metadata parent record --> 1</object>
   .....
 </error>

See :ref:`exception_handling` for more details.

Errors
``````

- **Service not allowed (error id:
  service-not-allowed)**, when the user is not
  authenticated or their profile has no rights to execute the
  service. Returns 500 HTTP code

- **Metadata not found (error id:
  metadata-not-found)**, when the parent metadata record doesn't
  exist. Returns 500 HTTP code
