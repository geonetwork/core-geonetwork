.. _services_download:

File download services
======================

This section provides a detailed explanation of GeoNetwork file download services. These are the services you would use if you want to download a file attached to a metadata record as 'Data for Download' (usually in onlineResources section of an ISO record) or perhaps as a gmx:FileName (where allowed).

The two services, used together, can be used to create a simple click through licensing scheme for file resources attached to metadata records in GeoNetwork.

.. index:: xml.file.disclaimer

xml.file.disclaimer
-------------------

Retrieves information from the metadata about constraints or restrictions on the resources attached to the metadata record. The information is xml and an xhtml presentation of the constraints and restrictions.

Note: only users that have download rights over the record will be able to use this service. To obtain these rights your application will need to use ``xml.user.login`` - see :ref:`xml.user.login`.

Request
```````

Called with a metadata id or uuid, one or more file names (if more than one file is attached to the metadata record as 'data for download') and access (which is almost always private). Example::

    <request>
        <uuid>d8c8ca11-ecc8-45dc-b424-171a9e212220</uuid>
        <fname>roam-rsf-aus-bathy-topo-contours.sff</fname>
        <fname>mse09_M8.nc</fname>
        <access>private</access>
    </request>

Response
````````

The service returns a copy of the request parameters, a copy of the metadata record xml and an HTML version of the license annex generated from the metadata record by the XSL ``metadata-license-annex.xsl`` (in the ``INSTALL_DIR/web/geonetwork/xsl`` directory).

Example of an ``xml.file.disclaimer`` response for a GeoNetwork node (Note: the <metadata> and <license> elements are not shown in full as they are too big)::

    <response>
        <id>22</id>
        <uuid>d8c8ca11-ecc8-45dc-b424-171a9e212220</uuid>
        <fname>roam-rsf-aus-bathy-topo-contours.sff</fname>
        <fname>mse09_M8.nc</fname>
        <access>private</access>
        <metadata>
            <gmd:MD_Metadata xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gts="http://www.isotc211.org/2005/gts" xmlns:gsr="http://www.isotc211.org/2005/gsr" xmlns:gss="http://www.isotc211.org/2005/gss" xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:geonet="http://www.fao.org/geonetwork">
                <!--.........-->
            </gmd:MD_Metadata>
        </metadata>
        <license>
            <html>
                <head>
                    <link href="http://localhost:8080/geonetwork/favicon.ico" rel="shortcut icon" type="image/x-icon" />
                    <link href="http://localhost:8080/geonetwork/favicon.ico" rel="icon" type="image/x-icon" />
                    <link rel="stylesheet" type="text/css" href="http://localhost:8080/geonetwork/geonetwork.css" />
                    <link rel="stylesheet" type="text/css" href="http://localhost:8080/geonetwork/modalbox.css" />
                </head>
                <body>
                    <!--.........-->
                </body>
            </html>
        </license>
    </response>

The idea behind this service is that you will receive an HTML presentation of the constraints/restrictions on the resource that you can show to a user for an accept/decline response.

The HTML presentation is controlled by the server so together with the ``xml.file.download`` service, this is the way that GeoNetwork can be used to provide a simple click-through licensing system for file resources attached to metadata records.

To signify acceptance of the license and download the resources you should use the ``xml.file.download`` service.

If an exception occurred then the service returns HTTP status code 500 and an XML document describing what went wrong. An example of such a response is::
 
 <error id="metadata-not-found">
   <message>Metadata not found</message>
   <class>MetadataNotFoundEx</class>
   .......
 </error>

See :ref:`exception_handling` for more details.

Errors
``````

- **IllegalArgumentException**: Request must contain a UUID or an ID parameter.

- **MetadataNotFoundException**: Metadata not found.

- **OperationNowAllowedException**: you don't have download permission over this record.

.. index:: xml.file.download

xml.file.download
-----------------

After your application has received any license conditions that go with the file resources attached to the metadata record from ``xml.file.disclaimer``, you can use this service to download the resources.

.. note:: only users that have download rights over the record will be able to use this service. To obtain these rights your application will need to use ``xml.user.login`` - see :ref:`xml.user.login`.

.. note:: this service is protected against users and/or applications that do not go through the ``xml.file.disclaimer`` service first.

Request
```````

Called with a metadata id or uuid, one or more file names (if more than one file is attached to the metadata record as 'data for download'), access (which is almost always private) and details of the user who has accepted the license and wants to download the files. Example::

    <request>
        <uuid>d8c8ca11-ecc8-45dc-b424-171a9e212220</uuid>
        <fname>roam-rsf-aus-bathy-topo-contours.sff</fname>
        <fname>mse09_M8.nc</fname>
        <access>private</access>
        <name>Aloyisus Wankania</name>
        <org>Allens Butter Factory</org>
        <email>A.Wankania@allens.org</email>
        <comments>Gimme the data buddy</comments>
    </request>

Response
````````

The service returns HTTP status code 200 along with a zip archive containing:

 - the file resources requested in the **fname** parameter(s)
 - a copy of the metadata record (as a mef) - called ``metadata.zip``
 - a copy of the html license generated (as provided by the ``xml.file.disclaimer`` service) - called ``license-annex.html``

If an exception occurs or the ``xml.file.disclaimer`` service has not been executed by the same user, then a zero-length file will be returned. Unlike other GeoNetwork services, no other indication of an exception is given.

