.. _category_services:

Category services
=================

Category List (xml.info&type=categories)
----------------------------------------

The **xml.info** service can be used to retrieve the categories available in GeoNetwork. See :ref:`xml.info`.

Category maintenance
--------------------

.. index:: xml.category.create.update

Create/update a category (xml.category.create.update)
`````````````````````````````````````````````````````

The **xml.category.create.update** service can be used to
create a new category and update the information about an existing category.
Only users with **Administrator** profile can
create/update categories.

Requires authentication: Yes

Request
^^^^^^^

Parameters:

- **id**: Category identifier to update. If
  not provided a new category is created with name provided.

- **name**: (mandatory) Name of the category

Category update request example::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.category.create.update

  Mime-type:
  application/xml

  Post request:
  <request>
      <id>2</id>
      <name>folios</name>
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
  "categories_name_key"**, when trying to create a new category using an existing
  category name. Returns 500 HTTP code

.. index:: xml.category.update

Update label translations (xml.category.update)
```````````````````````````````````````````````

The **xml.category.update** service can be used to
update translations of a category name. Only users with
**Administrator** profile can update category name translations.

Requires authentication: Yes

Request
^^^^^^^

Parameters:

- **category**: Container for category information
- **id**: (mandatory) Category identifier to update
- **label**: (mandatory) This is just
  a container to hold the category names translated in the
  languages supported by GeoNetwork. Each translated label
  is enclosed in a tag that identifies the language code

Category label update request example::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.category.update

  Mime-type:
  application/xml

  Post request:  
  <request>
      <category id="2">
          <label>
              <eng>folios</eng>
          </label>
      </category>
  </request>

Response
^^^^^^^^

Category label update response example::

  <ok />

Errors
^^^^^^

- **Service not allowed (error id: service-not-allowed)**, when the
  user is not authenticated or their profile has no rights to
  execute the service. Returns 500 HTTP code

- **Missing parameter (error id: missing-parameter)**, when mandatory parameters
  are not provided. Returns 500 HTTP code

.. index:: xml.category.get

Get a category (xml.category.get)
`````````````````````````````````

The **xml.category.get** service can be used to
retrieve information on an existing category.

Requires authentification: Yes

Request
^^^^^^^

Parameters:

- **id**: (mandatory) Category identifier to retrieve

Category get request example::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.category.get

  Mime-type:
  application/xml

  Post request:
  <request>
      <id>2</id>
  </request>

Response
^^^^^^^^

If the request executed succesfully then an HTTP 200 status code is
returned and an XML document containing the category information is returned. An example response is:::
 
 <response>
   <record>
     <id>2</id>
     <name>datasets</name>
     <label>
       <ara>Datasets</ara>
       <cat>Conjunts de dades</cat>
       <eng>Datasets</eng>
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

.. index:: xml.category.remove

Remove a category (xml.category.remove)
```````````````````````````````````````

The **xml.category.remove** service can be used to
remove an existing category. Only users with
**Administrator** profile can delete categories.

Requires authentification: Yes

Request
^^^^^^^

Parameters:

- **id**: (mandatory) Category identifier to delete

Category remove request example::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.category.remove

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


