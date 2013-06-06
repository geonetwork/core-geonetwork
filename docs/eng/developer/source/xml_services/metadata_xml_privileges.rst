.. _metadata_xml_privileges:

Metadata Privilege services
===========================

.. index:: xml.metadata.privileges

Update privileges on a metadata record (xml.metadata.privileges)
----------------------------------------------------------------

The **xml.metadata.privileges** service updates the
privileges on a metadata record using a list of groups and privileges sent 
as parameters. 


.. note:: All previously assigned privileges will be deleted. If versioning for the metadata record is on, then the previously assigned privileges will be available in the version history.

Requires authentication: Yes

Request
```````

Parameters:

- **id** or **uuid**: Identifier of metadata to update

- **_G_O**: (can be multiple elements)

 - **G**: Group identifier
 - **O**: Privilege (Operation) identifier. Privilege identifiers:

  - 0: view
  - 1: download
  - 2: editing
  - 3: notify
  - 4: dynamic
  - 5: featured

 - Group and Operation Identifiers can be obtained using :ref:`xml.info` service.

Request example:

**POST**::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.privileges

  Mime-type:
  application/xml

  Post request:
  <?xml version="1.0" encoding="UTF-8"?>
  <request>
    <id>6</id>
    <_1_2 />
    <_1_1 />
  </request>

**GET**::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.privileges?id=6&_1_2&_1_1

Response
````````

If the request executed successfully then the XML response contains the identifier of the metadata whose privileges have been updated.

Example::

  <response>
    <id>6</id>
  </response>

If the request was unsuccessful then the XML response contains details of the error returned. An example of such a response is::
 
 <error id="metadata-not-found">
   <message>Metadata not found</message>
   <class>MetadataNotFoundEx</class> 
   .....
   <object>6</object>
   <request>
     <language>eng</language>
     <service>xml.metadata.privileges</service>
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

- **ERROR: insert or update on table "operationallowed"
  violates foreign key 'operationallowed_operationid_fkey »**, if an
  operation identifier provided is not valid. Returns 500 HTTP code

- **ERROR: insert or update on table "operationallowed"
  violates foreign key 'operationallowed_groupid_fkey »**, if a
  group identifier provided is not valid. Returns 500 HTTP code

.. index:: xml.metadata.batch.update.privileges

.. _metadata.batch.update.privileges:

Batch update privileges (xml.metadata.batch.update.privileges)
--------------------------------------------------------------

The **xml.metadata.batch.update.privileges** service updates the privileges on a selected set of metadata using the list of groups and privileges sent as parameters.

.. note:: This service requires a previous call to the ``xml.metadata.select`` service (see :ref:`metadata.select`) to select metadata records.

.. note:: Only those metadata records for which the user running the service has ownership rights on will be updated and all privileges previously assigned will be deleted.

Requires authentication: Yes

Request
```````

Parameters:

- **_G_O**: (can be multiple elements)

 - **G**: Group identifier
 - **O**: Privilege (Operation) identifier. Privilege identifiers:

  - 0: view
  - 1: download
  - 2: editing
  - 3: notify
  - 4: dynamic
  - 5: featured

 - Group and Operation Identifiers can be obtained using :ref:`xml.info` service.

Example request:

**POST**::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.batch.update.privileges

  Mime-type:
  application/xml

  Post request:
  <?xml version="1.0" encoding="UTF-8"?>
  <request>
    <_1_2 />
    <_1_1 />
  </request>

**GET**::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.batch.update.privileges?_1_2&_1_1

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
   <object>xml.metadata.batch.update.privileges</object>
   .....
 </error>

See :ref:`exception_handling` for more details.


Errors
``````

- **Service not allowed (error id:
  service-not-allowed)**, when the user is not
  authenticated or their profile has no rights to execute the
  service. Returns 500 HTTP code

- **ERROR: insert or update on table "operationallowed"
  violates foreign key 'operationallowed_operationid_fkey »**, if an
  operation identifier provided is not valid. Returns 500 HTTP code

- **ERROR: insert or update on table "operationallowed"
  violates foreign key 'operationallowed_groupid_fkey »**, if a
  group identifier provided is not valid. Returns 500 HTTP code

