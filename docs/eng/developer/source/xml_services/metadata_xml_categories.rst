.. _metadata_xml_categories:

Metadata Category services
==========================

.. index:: xml.metadata.category

Update Categories of a metadata record (xml.metadata.category)
--------------------------------------------------------------

The **xml.metadata.category** service updates the
categories of a metadata record using the list of categories provided.

.. note:: The previously assigned categories will be removed. If versioning for the metadata record is on, then the previously assigned categories will be available in the version history.

Requires authentication: Yes

Request
```````

Parameters:

- **id** or **uuid**: Identifier of metadata to update

- **_C**: (can be multiple elements)

  - **C**: Category identifier (integer). A list of categories and identifiers is stored in the categories table. It can be retrieved using the :ref:`xml.info` service.

Request example:

**POST**::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.category

  Mime-type:
  application/xml

  Post request:
  <?xml version="1.0" encoding="UTF-8"?>
  <request>
    <id>6</id>
    <_1/>
    <_2/>
  </request>

**GET**::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.category?id=6&_1&_2

Response
````````

Successful response (HTTP code 200) contains the identifier of the metadata whose categories have been updated.

Example::

  <?xml version="1.0" encoding="UTF-8"?>
  <request>
    <id>6</id>
  </request>

Unsuccessful response (HTTP code 500) is an XML document with details of the exception/problem that occurred:

Example::
 
 <error id="metadata-not-found">
   <message>Metadata not found</message>
   <class>MetadataNotFoundEx</class>
   .....
   <object>Metadata not found --> 6</object>
   <request>
      <language>eng</language>
      <service>xml.metadata.category</service>
   </request>
 </error>

See :ref:`exception_handling` for more details.

Errors
``````

- **Service not allowed (error id:
  service-not-allowed)**, when the user is not
  authenticated or their profile has no rights to execute the
  service. Returns 500 HTTP code.

- **Metadata not found (error id: metadata-not-found)** if 
  a metadata record with the identifier provided does not exist.
  Returns 500 HTTP code.

.. index:: xml.metadata.batch.update.categories

.. _metadata.batch.update.categories:

Batch update categories (xml.metadata.batch.update.categories)
--------------------------------------------------------------

The **xml.metadata.batch.update.categories** service updates the categories of a selected set of metadata using the categories sent as parameters.

.. note:: This service requires a previous call to the ``xml.metadata.select`` service (see :ref:`metadata.select`) to select the metadata records to update.

.. note:: Only those metadata records for which the user running the service has ownership rights on will be updated and all categories previously assigned will be deleted. If metadata versioning is on then category changes will be recorded in the version history.

Requires authentication: Yes

Request
-------

Parameters:

- **_C**: (can be multiple elements)

  - **C**: Category identifier (integer). A list of categories and identifiers is stored in the categories table. It can be retrieved using the :ref:`xml.info` service.


Example request:

**POST**::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.batch.update.categories

  Mime-type:
  application/xml

  Post request:
  <?xml version="1.0" encoding="UTF-8"?>
  <request>
    <_1/>
    <_2/>
  </request>

**GET**::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.batch.update.categories?_1&_2

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

If the request fails an HTTP status code error is returned and
the response is an XML document with the exception. An example of such a response is shown below:

::
 
 <error id="service-not-allowed">
   <message>Service not allowed</message>
   .....
   <object>xml.metadata.batch.update.categories</object>
   .....
 </error>

See :ref:`exception_handling` for more details.

Errors
``````

- **Service not allowed (error id:
  service-not-allowed)**, when the user is not
  authenticated or their profile has no rights to execute the
  service. Returns 500 HTTP code
