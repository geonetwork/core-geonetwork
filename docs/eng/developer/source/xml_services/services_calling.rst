.. _services_calling:

Calling specifications
======================

Calling XML services
--------------------

GeoNetwork provides access to its functions through the use of XML services. 
These services are much like HTML addresses but return XML instead of HTML. 
The advantage of using XML, is that XML services can be used in 
machine-to-machine interfaces. As an
example, consider the ``xml.info`` service - :ref:`xml.info`: you might have
an application which can use this service to get information about GeoNetwork 
users and metadata schemas in XML. The user information returned
by this service could then be used by your application to decide how to 
authenticate with GeoNetwork so that 
metadata records from a particular metadata schema could be retrieved from other 
GeoNetwork XML services and processed by your application.

As a general rule, XML services provided by GeoNetwork usually have a ``xml.`` 
prefix in their address. To keep things simple and uniform, 
GeoNetwork XML services accept XML documents (or convert parameters to XML documents) and return information, status and errors as XML documents (except for a few services that relate to file download and must return binary data).

Request
```````

Each service accepts a set of parameters, which must be embedded in the
request. A service can be called using different HTTP methods, depending on
the structure of its request:

- **GET** The parameters are sent as part of the URL address. On the server side, these parameters are grouped into a flat XML document with one root and several simple children. A service can be called this way only if the parameters it accepts are not structured. An example of such a request and the parameters encoded in XML is:

:: 

    Url Request:
    http://localhost:8080/geonetwork/srv/eng/main.search?hitsPerPage=10&any=

    Encoding:
    <request>
        <hitsPerPage>10</hitsPerPage>
        <any />
    </request>


- **POST** There are 3 variants of this method:

  #. **ENCODED** The request has one of the following content types: application/x-www-form-urlencoded or multipart/form-data. The first case is very common when sending web forms while the second one is used to send binary data (usually files) to the server. In these cases, the parameters are not structured so the rules of the GET method applies. Even if the second case could be used to send XML documents, this possibility is not considered on the server side.
  #. **XML** The content type is application/xml.  This is the common case when the client is not a browser but a specialised client. The request is a pure XML document in string form, encoded using the encoding specified into the prologue of the XML document. Using this form, any type of request can be made (structured or not) so any service can be called.
  #. **SOAP** The content type is application/soap+xml.  SOAP is a simple protocol used to access objects and services using XML.  Clients that use this protocol can embed XML requests into a SOAP structure.  On the server side, GeoNetwork will remove the SOAP structure and feed the content to the service. Its response will be embedded again into a SOAP structure and sent back to the caller. It makes sense to use this protocol if it is the only protocol understood by the client.

Response
````````

The response of an XML service always has a content type of
application/xml (the only exception are those
services which return binary data). The document encoding is the one
specified in the document prologue which is UTF-8 (all GeoNetwork services
return documents in the UTF-8 encoding).

On a GET request, the client can force a SOAP response by adding the
application/soap+xml content type to the Accept
header parameter.

.. index:: Exception Handling

.. _exception_handling:

Exception handling
------------------

A response document having an error root element means that the XML service
raised an exception. This can happen under several conditions: bad parameters,
internal errors et cetera. In this cases the returned XML document has the following structure:

- **error**: This is the root element of the document. It has a mandatory
  id attribute that represents the identifier of the error.
  See below for a list of identifier values.
  
  - **message**: A message related to the error. It can be a short
    description about the error type or it can contain some other
    information that details the id code.
  - **class**: The Java class name of the Exception that occurred.
  - **stack**: Execution path from method where Exception occurred to 
    earliest method called by GeoNetwork. Each level in the execution path
    has an ``at`` child.

    - **at**: Information about the code being called when the exception 
      occurred. It has the following mandatory attributes:

      - **class** Java class name of the method that was called. 
      - **file** Source file where the class is defined.
      - **method** Method name in **class**.
      - **line** Source code line number in **file**.

  - **object**: An optional container for parameters or other values
    that caused the exception. In case a parameter is an XML object,
    this container will contain that object in XML form.
  - **request**: A container for request information.

    - **language**: Language used when the service was called.
    - **service**: Name of the service that was called.

.. _error2_ids:

**Summary of error ids:**

=========================   ===============================     =============================
**id**                      Meaning of message element          Meaning of object element
=========================   ===============================     =============================
**error**                   General message, human readable     x
**bad-format**              Reason                              x
**bad-parameter**           Name of the parameter               Parameter value
**file-not-found**          x                                   File name
**file-upload-too-big**     x                                   x
**missing-parameter**       Name of the parameter               XML container where the
                                                                parameter should have been
                                                                present.
**object-not-found**        x                                   Object name
**operation-aborted**       Reason of abort                     If present, the object that 
                                                                caused the abort
**operation-not-allowed**   x                                   x
**resource-not-found**      x                                   Resource name
**service-not-allowed**     x                                   Service name
**service-not-found**       x                                   Service name
**user-login**              User login failed message           User name
**user-not-found**          x                                   User id or name
**metadata-not-found**      The requested metadata was not      Metadata id
                            found
=========================   ===============================     =============================


Below is an example of exception generated
by the mef.export service. The service complains about a missing parameter, as
you can see from the content of the id attribute. The object element contains
the xml request with an unknown test parameter while the mandatory UUID
parameter (as specified by the message element) is missing.

**An example of generated exception**::

    <error>
        <message>UUID</message>
        <class>MissingParameterEx</class>
        <stack>
            <at class="jeeves.utils.Util" file="Util.java" line="66"
                method="getParam"/>
            <at class="org.fao.geonet.services.mef.Export" file="Export.java"
                line="60" method="exec"/>
            <at class="jeeves.server.dispatchers.ServiceInfo" file="ServiceInfo.java"
                line="226" method="execService"/>
            <at class="jeeves.server.dispatchers.ServiceInfo" file="ServiceInfo.java"
                line="129" method="execServices"/>
            <at class="jeeves.server.dispatchers.ServiceManager" file="ServiceManager.java"
                line="370" method="dispatch"/>
        </stack>
        <object>
            <request>
                <asd>ee</asd>
            </request>
        </object>
        <request>
            <language>en</language>
            <service>mef.export</service>
        </request>
    </error>

