.. _metadata_xml_status:

Metadata Status services
========================

.. index:: xml.metadata.status

Update Status on a metadata record (xml.metadata.status)
--------------------------------------------------------

The **xml.metadata.status** service updates the
status on a metadata record using the status and changeMessage provided
as parameters. 

.. note:: The previously assigned status will be removed. If versioning for the metadata record is on, then the previously assigned status will be available in the version history.

Requires authentication: Yes

Request
```````

Parameters:

- **id** or **uuid**: Identifier of metadata to update

- **status**: One of the status identifiers take from the database table ``statusvalues``. Status identifiers can be retrieved using the :ref:`xml.info` service. The core status identifiers are:

 - 0: unknown
 - 1: draft
 - 2: approved
 - 3: retired
 - 4: submitted
 - 5: rejected

- **changeMessage**: description of why the status has changed.

Request example:

**POST**::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.status

  Mime-type:
  application/xml

  Post request:
  <?xml version="1.0" encoding="UTF-8"?>
  <request>
    <id>6</id>
    <status>5</status>
    <changeMessage>Completely unacceptable: consistency rules ignored<changeMessage/>
  </request>

**GET**::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.status?id=6&status=5&changeMessage=Do%20it%20all%20again%20nitwit

.. note:: URL encoding of changeMessage.

Response
````````

If the request executes successfully then HTTP status code 200 is returned along with an XML document which contains the identifier of the metadata whose status has been updated.

Example::

  <response>
    <id>6</id>
  </response>

If an error occurred then HTTP status code 500 is returned along with an XML document which contains details of what went wrong. An example of such an error response is:

::
 
 <error id="metadata-not-found">
   <message>Metadata not found</message>
   <class>MetadataNotFoundEx</class> 
   .....
 </error>

See :ref:`exception_handling` for more details.

Errors
``````

- **Service not allowed (error id:
  service-not-allowed)**, when the user is not
  authenticated or their profile has no rights to execute the
  service. Returns 401 HTTP code

- **Metadata not found (error id: metadata-not-found)** if 
  a metadata record with the identifier provided does not exist

- **Only the owner of the metadata can set the status. User is not the owner of the metadata**, if the user does not have ownership rights over the metadata record.

.. index:: xml.metadata.batch.update.status

.. _metadata.batch.update.status:

Batch update status (xml.metadata.batch.update.status)
------------------------------------------------------

The **xml.metadata.batch.update.status** service updates the status on a selected set of metadata using the status and changeMessage sent as parameters.

.. note:: This service requires a previous call to the ``xml.metadata.select`` service (see :ref:`metadata.select`) to select metadata records.

.. note:: Only those metadata records for which the user running the service has ownership rights on will be updated and all status values previously assigned will be deleted. If metadata versioning is on then status changes will be recorded in the version history.

Requires authentication: Yes

Request
```````

Parameters:

- **status**: One of the status identifiers take from the database table ``statusvalues``. Status identifiers can be retrieved using the :ref:`xml.info` service. The core status identifiers are:

 - 0: unknown
 - 1: draft
 - 2: approved
 - 3: retired
 - 4: submitted
 - 5: rejected

- **changeMessage**: description of why the status has changed.

Example request:

**POST**::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.batch.update.status

  Mime-type:
  application/xml

  Post request:
  <?xml version="1.0" encoding="UTF-8"?>
  <request>
    <status>5</status>
    <changeMessage>Completely unacceptable: consistency rules ignored<changeMessage/>
  </request>

**GET**::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.batch.update.status?&status=5&changeMessage=Do%20it%20all%20again%20nitwit

.. note:: URL encoding of changeMessage.

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
   <noChange>0</noChange>
 </response>

The response fields are:

- **done** - number of metadata records successfully updated
- **notOwner** - number of metadata records skipped because the user running this service did not have ownership rights
- **notFound** - number of metadata records skipped because they were not found (may have been deleted)
- **noChange** - number of metadata records whose ownership was unchanged by the operation.

If the request fails an HTTP 500 status code error is returned and
the response is an XML document with the exception. An example of such a response is shown below:

::
 
 <error id="service-not-allowed">
   <message>Service not allowed</message>
   <class>ServiceNotAllowedEx</class>
   .....
 </error>

See :ref:`exception_handling` for more details.

Errors
``````

- **Service not allowed (error id:
  service-not-allowed)**, when the user is not
  authenticated or their profile has no rights to execute the
  service. Returns 500 HTTP code

.. index:: xml.metadata.status.get

Get status of a metadata record (xml.metadata.status.get)
---------------------------------------------------------

This service gets the status of a particular metadata record specified by id or uuid as a parameter. 

Requires authentication: No.

Request
```````

Parameters:

- **id** or **uuid**: Identifier of metadata to obtain status of.

Example request:

**POST**::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.status.get

  Mime-type:
  application/xml

  Post request:
  <?xml version="1.0" encoding="UTF-8"?>
  <request>
    <id>5</id>
  </request>

**GET**::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.status.get?&id=5

Response
````````

If the request executed successfully a HTTP 200 status code is
returned and the XML with status values for the metadata record (note: all changesin status are returned in the response) is returned. An example follows::

 <response>
   <record>
    <statusid>5</statusid>
    <userid>4</userid>
    <changedate>2012-12-27T14:58:04</changedate>
    <changemessage>Do it all again</changemessage>
    <name>rejected</name>
   </record>
   <record>
    <statusid>4</statusid>
    <userid>6</userid>
    <changedate>2012-12-27T14:32:10</changedate>
    <changemessage>Ready for review</changemessage>
    <name>submitted</name>
   </record>
  </response> 

If the request did not execute successfully then HTTP 500 status code error is returned along with an XML document which includes details of the exception/what went wrong. An example of such a request is:::
 
 <error id="metadata-not-found">
   <message>Metadata not found</message>
   <class>MetadataNotFoundEx</class>
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
  metadata-not-found)**, when the metadata record requested is not
  found. Returns 500 HTTP code


Defining status actions
-----------------------

The behaviour of GeoNetwork when a status changes can be defined by the programmer.  See :ref:`java_metadata_status_actions`.
