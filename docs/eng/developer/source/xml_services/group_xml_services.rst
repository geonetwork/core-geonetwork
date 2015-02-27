.. _group_services:

Group services
==============

Group List (xml.info?type=groups)
---------------------------------

The **xml.info** service can be used to retrieve the user groups available in GeoNetwork. See :ref:`xml.info`.

Group maintenance
-----------------

.. index:: xml.group.create.update

Create/update a group (xml.group.create.update)
```````````````````````````````````````````````

The **xml.group.create.update** service can be used to
create a new group and update the information about an existing group.
Only users with **Administrator** profile can
create/update groups.

Requires authentication: Yes

Request
^^^^^^^

Parameters:

- **id**: Group identifier to update. If
  not provided a new group is created with the name, description
  and email parameters provided.

- **name**: (mandatory) Name of the group

- **description**: Group description

- **email**: email address for group notifications

Group update request example::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.group.create.update

  Mime-type:
  application/xml

  Post request:
  <request>
      <id>2</id>
      <name>sample</name>
      <description>Demo group</description>
      <email>group@mail.net</email>
  </request>

Response
^^^^^^^^

If the request executed successfully, then an HTTP 200 status code is
returned along with an XML document that confirms the operation that has taken place.  An example of a response to an update request is:::
 
 <response>
   <operation>updated</operation>
 </response>

An example of a response to a create request is:::
 
 <response>
   <operation>added</operation>
 </response>

If the request fails, then a HTTP 500 status code error is returned
and the response contains an XML document with the details of the exception/what
went wrong. An example of such a response is:::
 
 <error id="missing-parameter">
    <message>name</message>
    <class>MissingParameterEx</class>
    .....
 </error>

See :ref:`exception_handling` for more details.

Errors
^^^^^^

- **Service not allowed (error id: service-not-allowed)**, when the
  user is not authenticated or their profile is not permitted to
  execute the service. Returns 500 HTTP code

- **Missing parameter (error id: missing-parameter)**, when mandatory parameters
  are not provided. Returns 500 HTTP code

- **bad-parameter name**, when **name** it's
  empty. Returns 500 HTTP code

- **ERROR: duplicate key violates unique constraint
  "groups_name_key"**, when trying to create a new group using an existing
  group name. Returns 500 HTTP code

.. index:: xml.group.update

Update label translations (xml.group.update)
````````````````````````````````````````````

The **xml.group.update** service can be used to
update translations of a group name. Only users with
**Administrator** profile can update group name translations.

Requires authentication: Yes

Request
^^^^^^^

Parameters:

- **group**: Container for group information
- **id**: (mandatory) Group identifier to update
- **label**: (mandatory) This is just
  a container to hold the group names translated in the
  languages supported by GeoNetwork. Each translated label
  is enclosed in a tag that identifies the language code

Group label update request example::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.group.update

  Mime-type:
  application/xml

  Post request:  
  <request>
      <group id="2">
          <label>
              <es>Grupo de ejemplo</es>
          </label>
      </group>
  </request>

Response
^^^^^^^^

Group label update response example::

  <?xml version="1.0" encoding="UTF-8"?>
  <ok />

Errors
^^^^^^

- **Service not allowed (error id: service-not-allowed)**, when the
  user is not authenticated or their profile has no rights to
  execute the service. Returns 500 HTTP code

- **Missing parameter (error id: missing-parameter)**, when mandatory parameters
  are not provided. Returns 500 HTTP code

.. index:: xml.group.get

Get a group (xml.group.get)
```````````````````````````

The **xml.group.get** service can be used to
retrieve information on an existing group.

Requires authentification: Yes

Request
^^^^^^^

Parameters:

- **id**: (mandatory) Group identifier to retrieve

Group get request example::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.group.get

  Mime-type:
  application/xml

  Post request:
  <request>
      <id>2</id>
  </request>

Response
^^^^^^^^

If the request executed succesfully then an HTTP 200 status code is
returned and an XML document containing the group information is returned. An example response is:::
 
 <response>
   <record>
     <id>1</id>
     <name>all</name>
     <description/>
     <email/>
     <referrer/>
     <label>
       <ara>All</ara>
       <cat>All</cat>
       <chi>All</chi>
       <dut>Iedereen</dut>
       <eng>All</eng>
       .....
     </label>
   </record>
 </response>

If the request fails then an HTTP 500 status code error is returned
and the response contains an XML document with the details of the exception/what
went wrong. An example error response is:::
 
 <error id="missing-parameter">
   <message>id</message>
   <class>MissingParameterEx</class>
   .....
 </error>

See :ref:`exception_handling` for more details.

Errors
^^^^^^

- **Service not allowed (error id: service-not-allowed)**, when the
  user is not authenticated or their profile has no rights to
  execute the service. Returns 500 HTTP code

- **Missing parameter (error id: missing-parameter)**, when mandatory parameters
  are not provided. Returns 500 HTTP code

- **bad-parameter id**, when **id** parameter is
  empty/invalid. Returns 500 HTTP code

.. index:: xml.group.remove

Remove a group (xml.group.remove)
`````````````````````````````````

The **xml.group.remove** service can be used to
remove an existing group. Only users with
**Administrator** profile can delete groups.

Requires authentification: Yes

Request
^^^^^^^

Parameters:

- **id**: (mandatory) Group identifier to delete

Group remove request example::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.group.remove

  Mime-type:
  application/xml

  Post request:
  <request>
      <id>2</id>
  </request>

Response
^^^^^^^^

If the request executed succesfully then an HTTP 200 status code is
returned and an XML document confirming the remove operation is returned. An example response is:::
 
 <response>
   <operation>removed</operation>
 </response>

If the request fails then an HTTP 500 status code error is returned
and the response contains an XML document with the details of the exception/what
went wrong. An example error response is:::
 
 <error id="missing-parameter">
   <message>id</message>
   <class>MissingParameterEx</class>
   .....
 </error>

See :ref:`exception_handling` for more details.

Errors
^^^^^^

- **Service not allowed (error id: service-not-allowed)**, when the
  user is not authenticated or their profile has no rights to
  execute the service. Returns 500 HTTP code

- **Missing parameter (error id: missing-parameter)**, when mandatory parameters
  are not provided. Returns 500 HTTP code

- **bad-parameter id**, when **id** parameter is
  empty/invalid. Returns 500 HTTP code


