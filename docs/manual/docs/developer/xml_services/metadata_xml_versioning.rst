.. _metadata_xml_versioning:

Metadata Versioning services
============================

.. index:: xml.metadata.version

Start versioning a metadata record (xml.metadata.version)
---------------------------------------------------------

The **xml.metadata.version** service creates an initial version of the metadata record and its properties (categories, status, privileges) in the subversion repository.

Requires authentication: Yes

Request
```````

Parameters:

- **id** or **uuid**: Identifier of metadata to version

Request example:

**POST**::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.version

  Mime-type:
  application/xml

  Post request:
  <?xml version="1.0" encoding="UTF-8"?>
  <request>
    <id>6</id>
  </request>

**GET**::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.version?id=6

Response
````````

Successful response (HTTP status code 200) contains the identifier of the metadata for which versioning has been enabled.

Example::

  <?xml version="1.0" encoding="UTF-8"?>
  <request>
    <id>6</id>
  </request>

If the service was not completed successfully, then HTTP status code 500 is returned with an XML document containing details of the exception/problem. An example of such a document is as follows:::
 
 <error id="metadata-not-found">
   <message>Metadata not found</message>
   <class>MetadataNotFoundEx</class>
   .....
   <object>Metadata not found --> 6</object>
   <request>
     <language>eng</language>
     <service>xml.metadata.version</service>
   </request>
 </error>

See :ref:`exception_handling` for more details.


Errors
``````

- **Service not allowed (error id:
  service-not-allowed)**, when the user is not
  authenticated or their profile has no rights to execute the
  service. Returns 500 HTTP code

- **Metadata not found (error id: metadata-not-found)** if 
  a metadata record with the identifier provided does not exist. Returns 500 HTTP
  code

- **Operation Not Allowed**, if the user does not have editing rights over the 
  metadata record. Returns 500 HTTP code

.. index:: xml.metadata.batch.version

.. _metadata.batch.version:

Batch start versioning (xml.metadata.batch.version)
---------------------------------------------------

For each metadata record in the selected set, **xml.metadata.batch.version** creates an initial version of the metadata record and its properties (categories, status, privileges) in the subversion repository.

.. note:: This service requires a previous call to the ``xml.metadata.select`` service (see :ref:`metadata.select`) to select metadata records.

.. note:: Only those metadata records that the user running the service has editing rights over will be versioned. If a metadata record is already versioned then no action is taken.

Requires authentication: Yes

Request
```````

Parameters:

**None**

Example request:

**POST**::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.batch.version

  Mime-type:
  application/xml

  Post request:
  <?xml version="1.0" encoding="UTF-8"?>
  <request/>

**GET**::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.batch.version

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

- **done** - number of metadata records successfully updated
- **notOwner** - number of metadata records skipped because the user running this service did not have ownership rights
- **notFound** - number of metadata records skipped because they were not found (may have been deleted)

If the request fails an HTTP 500 status code error is returned and
the response is an XML document with the exception. An example of such a response is shown below:

::
 
 <error id="service-not-allowed">
   <message>Service not allowed</message>
   .....
   <object>xml.metadata.batch.update.version</object>
   .....
 </error>

See :ref:`exception_handling` for more details.

Errors
``````

- **Service not allowed (error id:
  service-not-allowed)**, when the user is not
  authenticated or their profile has no rights to execute the
  service. Returns 500 HTTP code
