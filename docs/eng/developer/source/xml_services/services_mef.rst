.. _services_mef:

MEF services
============

This section describes the services related to the Metadata Exchange Format.
These services allow import/export metadata using the MEF (Metadata Exchange Format) format. 

.. note:: before using these services please review the section on the MEF format at :ref:`mef`

.. index:: mef.export

.. _mef_export:

mef.export
----------

This service exports GeoNetwork metadata using the MEF file format. The metadata record can be specified using a uuid or the currently selected set of metadata records can be used - see :ref:`metadata.select` for more details on how to select a set of metadata records.

This service is public but metadata access rules apply. For a partial export,
the view privilege is enough but for a full export the download privilege is
also required. Without a login step, only partial exports on public metadata are
allowed.

This service uses the system temporary directory to build the MEF file. By default the tmp directory is ``INSTALL_DIR/web/geonetwork/data``. You will need to 
ensure that sufficient disk space is available on that filesystem for full 
exports (ie. those that include data files uploaded with the metadata record) 
to be successful. Alternatively, you can specify a different directory
by configuring the ``uploadDir`` parameter in the ``general`` section of the
``INSTALL_DIR/web/geonetwork/WEB-INF/config.xml`` file.


Request
```````

This service accepts requests in GET/POST and XML form. The input
parameters are all optional and are as follows:

- **uuid** the universal unique identifier of the metadata to be exported. If this parameter is optional then the selected set of metadata will be exported. To select a set of metadata see :ref:`metadata.select`.
- **format** which MEF format to use. Can be one of: *simple*, *partial*, *full*. Default is *full* - which means thumbnails and data files uploaded with the metadata record are included in the MEF.
- **skipUuid** (*true|false*) If set to *true*, the metadata record UUIDs will not be exported into the MEF ``info.xml`` file. Without a UUID (which is a unique key inside the database) the metadata records in a MEF can be repeatedly imported as they will receive a new UUID on import. The default value is *false*.
- **version** (*true|false*) If set to *true*, MEF Version 2.0 is used, otherwise MEF Version 1.0 is used. This parameter needs to be present if related records are being included in the MEF - see the **relation** parameter below.
- **resolveXlink** (*true|false*) If set to *true*, then any XLinks in the metadata records are resolved before the records are exported. If *false*, the metadata records are exported with unresolved XLinks. Default is *true*. 
- **removeXlinkAttributes** (*true|false*) If set to *true*, then any XLink attributes in the metadata records are removed before the records are exported. Default is *true*. 
- **relation** (*true|false*) If set to *true* *and* **version** is also set to *true*, then related records (eg.parent, feature catalog) are exported into the MEF with the metadata record. Default is *true*.

Example POST request::
 
 <request>
  <uuid>f80bca2e-ff75-4107-8999-4c1864cb1b1b</uuid>
  <format>full</format>
  <skipUuid>true</skipUuid>
  <version>true</version>
  <relation>true</relation>
 </request>

 URL: 
 http://localhost:8080/geonetwork/srv/eng/mef.export

Response
````````

If successful the service returns HTTP status code 200 and the response is a MEF file with name as follows:

- prefix is ``export-``
- MEF format: *simple*, *partial*, *full* 
- current time in milliseconds since 1970
- ``zip`` extension

eg. ``export-full-1357949862822.zip``

If an exception occurred then service returns HTTP status code 500 and an XML document describing what went wrong. An example of such a response is::
 
 <error id="metadata-not-found">
   <message>Metadata not found</message>
   <class>MetadataNotFoundEx</class>
   .......
 </error>

See :ref:`exception_handling` for more details.

.. index:: mef.import

mef.import
----------

This service is used to import a metadata record in the MEF format.

Only users with the **Administrator** profile can run this service.

Request
```````

The service accepts a multipart/form-data POST request
with a single **mefFile** parameter that must contain the MEF
information.

Response
````````

If the import is successful, the service returns HTTP status code 200 along with an XML document containing the GeoNetwork integer id of the created metadata. Example::

    <ok>123</ok>

If an exception occurred then service returns HTTP status code 500 and an XML document describing what went wrong. An example of such a response is::
 
 <error id="metadata-not-found">
   <message>Metadata not found</message>
   <class>MetadataNotFoundEx</class>
   .......
 </error>

See :ref:`exception_handling` for more details.

Notes
`````

- Version 1.0 of the MEF format does not capture the metadata owner (the creator) and the group owner.  During import, the user that is performing this operation will become the metadata owner and the group owner will be set to null.
