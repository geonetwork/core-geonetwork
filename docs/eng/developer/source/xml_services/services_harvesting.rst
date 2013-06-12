.. _services_harvesting:

Harvesting services
===================

This section describes the services used to create, update and manage GeoNetwork
harvesters. These services allow complete control over harvester behaviour.
Authentication is required for all services described in this section. In addition, these services can only be run by users with the **Administrator** profile.


.. index:: xml.harvesting.get

Get harvester definitions (xml.harvesting.get)
----------------------------------------------

Retrieves information about one or all configured harvesters.

Request
```````

Called without parameters, this service returns all harvesters. Example::

    <request/>

Otherwise, an **id** parameter can be specified to request the definition of a specific harvester instance::

    <request>
        <id>123</id>
    </request>

Response
````````

When called without parameters the service returns HTTP status code 200 along
with an XML document with all harvester instances. The XML document has a root element called ``nodes`` with a ``node`` child for each harvester.

**Example of an xml.harvesting.get response for a GeoNetwork harvester**::

    <nodes>
        <node id="125" type="geonetwork">
            <site>
                <name>test 1</name>
                <uuid>0619cc50-708b-11da-8202-000d9335aaae</uuid>
                <account>
                    <use>false</use>
                    <username />
                    <password />
                </account>
                <host>http://www.fao.org/geonetwork</host>
                <createRemoteCategory>true</createRemoteCategory>
                <mefFormatFull>true</mefFormatFull>
                <xslfilter/>
            </site>
            <content>
                <validate>true</validate>
                <importxslt>none</importxslt>
            </content>
            <options>
                <every>0 0 0/3 ? * *</every>
                <oneRunOnly>false</oneRunOnly>
                <status>inactive</status>
            </options>
            <searches>
                <search>
                    <freeText />
                    <title />
                    <abstract />
                    <keywords />
                    <digital>false</digital>
                    <hardcopy>false</hardcopy>
                    <source>
                        <UUID>0619cc50-708b-11da-8202-000d9335906e</uuid>
                        <name>Food and Agriculture organisation</name>
                    </source>
                </search>
            </searches>
            <groupsCopyPolicy>
                <group name="all" policy="copy"/>
                <group name="mygroup" policy="createAndCopy"/>
            </groupsCopyPolicy>
            <categories>
                <category id="4"/>
            </categories>
            <info>
                <lastRun />
                <running>false</running>
            </info>
        </node>
    </nodes>

If you specify a harvester **id** parameter in the request, then the XML document returned has a ``node`` root element that describes the harvester.

**Example of an xml.harvesting.get response for a WebDAV harvester**::

    <node id="165" type="webdav">
        <site>
            <name>test 1</name>
            <UUID>0619cc50-708b-11da-8202-000d9335aaae</uuid>
            <url>http://www.mynode.org/metadata</url>
            <icon>default.gif</icon>
            <account>
                <use>true</use>
                <username>admin</username>
                <password>admin</password>
            </account>
        </site>
        <options>
            <every>0 0 0/3 ? * *</every>
            <oneRunOnly>false</oneRunOnly>
            <recurse>false</recurse>
            <validate>true</validate>
            <status>inactive</status>
        </options>
        <privileges>
            <group id="0">
                <operation name="view" />
            </group>
            <group id="14">
                <operation name="download" />
            </group>
        </privileges>
        <categories>
            <category id="2"/>
        </categories>
        <info>
            <lastRun />
            <running>false</running>
        </info>
    </node>

Each harvester has some common XML elements, plus 
additional elements that are specific to each harvesting type.

The common XML elements are described at :ref:`harvesting_nodes`.

If an error occurred then HTTP status code 500 is returned along with an XML document which contains details of what went wrong. An example of such an error response is:

::
 
 <error id="object-not-found">
   <message>Object not found</message>
   <class>ObjectNotFoundEx</class> 
   .....
 </error>

See :ref:`exception_handling` for more details.

Errors
``````

- **ObjectNotFoundEx** If a harvester definition with the specified **id**
  cannot be found.

.. index:: xml.harvesting.add

.. _xml.harvesting.add:

Create harvester instance (xml.harvesting.add)
----------------------------------------------

Create a new harvester. The harvester can be of any type supported by
GeoNetwork (see :ref:`harvesting_nodes` for a list). When a new harvester 
instance is created, its status is set to inactive. 
A call to the ``xml.harvesting.start`` service is
required to set the status to active and run the harvester at the scheduled
time.

Request
```````

The service requires an XML tree with all information about the harvesting node to be added. The common XML elements that must be in the tree are described at :ref:`harvesting_nodes`. Settings and example requests for each type of harvester in GeoNetwork are as follows:

- :ref:`geonetwork_harvesting`
- :ref:`webdav_harvesting`
- :ref:`csw_harvesting`
- :ref:`z3950_harvesting`
- :ref:`oaipmh_harvesting`
- :ref:`thredds_harvesting`
- :ref:`wfsfeatures_harvesting`
- :ref:`filesystem_harvesting`
- :ref:`arcsde_harvesting`
- :ref:`ogcwxs_harvesting`
- :ref:`geoportal_rest_harvesting`

Summary of features of the supported harvesting types
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

===============     ==============      ================    ============
Harvesting type     Authentication      Privileges          Categories
===============     ==============      ================    ============
GeoNetwork          native              through policies    yes
WebDAV              HTTP digest         yes                 yes
CSW                 HTTP Basic          yes                 yes
===============     ==============      ================    ============

Response
````````

If the request succeeds and the harvester instance is created, then HTTP status code 200 is returned along with an XML document containing the definition of the harvester as is described in the response section of the ``xml.harvesting.get`` service above.

If an error occurred then HTTP status code 500 is returned along with an XML document which contains details of what went wrong. An example of such an error response is:

::
 
 <error id="object-not-found">
   <message>Object not found</message>
   <class>ObjectNotFoundEx</class> 
   .....
 </error>

See :ref:`exception_handling` for more details.

.. index:: xml.harvesting.info

.. _xml_harvesting_info:

Get information for Harvester definition (xml.harvesting.info)
--------------------------------------------------------------

This service can be used to obtain information from the server that is relevant
to defining a harvester eg. harvester icons, stylesheets etc.

Request and Response
````````````````````

All requests must have a **type** parameter which defines the type of information required. The requests and responses for each value of the **type** parameter are:

.. _xml_harvesting_info&type=icons:

icons
^^^^^

Return the list of icons that can be used when creating a harvester instance. Icons are usually set in **site/icon** harvester setting.

POST Request Example::

 <request>
   <type>icons<type>
 </request>

 URL:
 http://localhost:8080/geonetwork/srv/eng/xml.harvesting.info

Response Example::
 
 <root>
   <icons>
     <icon>wfp.gif</icon>
     <icon>unep.gif</icon>
     <icon>webdav.gif</icon>
     <icon>gn20.gif</icon>
     <icon>thredds.gif</icon>
     <icon>wfs.gif</icon>
     <icon>csw.gif</icon>
     <icon>filesystem.gif</icon>
     <icon>fao.gif</icon>
     <icon>default.gif</icon>
     <icon>Z3950.gif</icon>
     <icon>oai-mhp.gif</icon>
     <icon>esri.gif</icon>
   </icons>
 </root> 

.. _xml_harvesting_info&type=importStylesheets:

importStylesheets
^^^^^^^^^^^^^^^^^
 
Return the list of stylesheets that can be used when creating a harvester instance. The ``id`` element in the response can be used in the **content/importxslt** harvester setting for those harvesters that support it.

POST Request Example::
 
 <request>
   <type>icons<type>
 </request>

 URL:
 http://localhost:8080/geonetwork/srv/eng/xml.harvesting.info

Response Example::
 
 <root>
   <stylesheets>
     <record>
       <id>ArcCatalog8_to_ISO19115.xsl</id>
       <name>ArcCatalog8_to_ISO19115</name>
     </record>
     <record>
       <id>CDMCoords-to-ISO19139Keywords.xsl</id>
       <name>CDMCoords-to-ISO19139Keywords</name>
     </record>
     .....
   </stylesheets>
 </root>
   

.. _xml_harvesting_info&type=oaiPmhServer:

oaiPmhServer
^^^^^^^^^^^^

Request information about the sets and prefixes of an OAIPMH server. This request requires an additional url attribute on the type parameter specifying the name of the OAIPMH server to query.

POST Request Example::
 
 <request>
   <type url="http://localhost:8080/geonetwork/srv/eng/oaipmh">oaiPmhServer</type>
 </request>

 URL:
 http://localhost:8080/geonetwork/srv/eng/xml.harvesting.info

Response Example::
 
 <root>
  <oaiPmhServer>
    <formats>
      <format>iso19115</format>
      <format>fgdc-std</format>
      <format>iso19139</format>
      <format>csw-record</format>
      <format>iso19110</format>
      <format>dublin-core</format>
      <format>oai_dc</format>
    </formats>
    <sets>
      <set>
        <name>maps</name>
        <label>Maps &amp; graphics</label>
      </set>
      <set>
        <name>datasets</name>
        <label>Datasets</label>
      </set>
      ......
    </sets>
  </oaiPmhServer>
 </root>

.. _xml_harvesting_info&type=wfsFragmentSchemas:

wfsFragmentSchemas
^^^^^^^^^^^^^^^^^^

Return list of schemas that have WFS Fragment conversion stylesheets. These stylesheets are stored in the ``WFSToFragments`` directory in the ``convert`` directory of a metadata schema. eg. for schema iso19139 this directory would be ``GEONETWORK_DATA_DIR/config/schema_plugins/iso19139/convert/WFSToFragments``.

POST Request Example::
 
 <request>
  <type>wfsFragmentSchemas</type>
 </request>

 URL:
 http://localhost:8080/geonetwork/srv/eng/xml.harvesting.info

Response Example::
 
 <root>
  <schemas>
    <record>
      <id>iso19139</id>
      <name>iso19139</name>
    </record>
  </schemas>
 </root>

.. _xml_harvesting_info&type=wfsFragmentStylesheets:

wfsFragmentStylesheets
^^^^^^^^^^^^^^^^^^^^^^

Return WFS Fragment conversion stylesheets for a schema previously returned by the request type ``wfsFragmentSchemas`` described above. These stylesheets are stored in the ``WFSToFragments`` directory in the ``convert`` directory of a metadata schema. eg. for schema iso19139 this directory would be ``GEONETWORK_DATA_DIR/config/schema_plugins/iso19139/convert/WFSToFragments``.

POST Request Example::
 
 <request>
   <schema>iso19139</schema>
   <type>wfsFragmentStylesheets</type>
 </request>

 URL:
 http://localhost:8080/geonetwork/srv/eng/xml.harvesting.info

Response Example::
 
 <root>
  <stylesheets>
    <record>
      <id>deegree22_philosopher_fragments.xsl</id>
      <name>deegree22_philosopher_fragments</name>
      <schema>iso19139</schema>
    </record>
    <record>
      <id>geoserver_boundary_fragments.xsl</id>
      <name>geoserver_boundary_fragments</name>
      <schema>iso19139</schema>
    </record>
  </stylesheets>
 </root>

.. _xml_harvesting_info&type=threddsFragmentSchemas:

threddsFragmentSchemas
^^^^^^^^^^^^^^^^^^^^^^

Return list of schemas that have THREDDS Fragment conversion stylesheets. These stylesheets are stored in the ``ThreddsToFragments`` directory in the ``convert`` directory of a metadata schema. eg. for schema iso19139 this directory would be ``GEONETWORK_DATA_DIR/config/schema_plugins/iso19139/convert/ThreddsToFragments``.

POST Request Example::
 
 <request>
  <type>threddsFragmentSchemas</type>
 </request>

 URL:
 http://localhost:8080/geonetwork/srv/eng/xml.harvesting.info

Response Example::
 
 <root>
  <schemas>
    <record>
      <id>iso19139</id>
      <name>iso19139</name>
    </record>
  </schemas>
 </root>


.. _xml_harvesting_info&type=threddsFragmentStylesheets:

threddsFragmentStylesheets
^^^^^^^^^^^^^^^^^^^^^^^^^^

Return WFS Fragment conversion stylesheets for a schema previously returned by the request type ``threddsFragmentSchemas`` described above. These stylesheets are stored in the ``ThreddsToFragments`` directory in the ``convert`` directory of a metadata schema. eg. for schema iso19139 this directory would be ``GEONETWORK_DATA_DIR/config/schema_plugins/iso19139/convert/ThreddsToFragments``.

POST Request Example::
 
 <request>
   <schema>iso19139</schema>
   <type>threddsFragmentStylesheets</type>
 </request>

 URL:
 http://localhost:8080/geonetwork/srv/eng/xml.harvesting.info

Response Example::
 
 <root>
  <stylesheets>
    <record>
      <id>netcdf-attributes.xsl</id>
      <name>netcdf-attributes</name>
      <schema>iso19139</schema>
    </record>
    <record>
      <id>thredds-metadata.xsl</id>
      <name>thredds-metadata</name>
      <schema>iso19139</schema>
    </record>
  </stylesheets>
 </root>

.. _xml_harvesting_info&type=ogcwxsOutputSchemas:

ogcwxsOutputSchemas
^^^^^^^^^^^^^^^^^^^

Return list of schemas that have GetCapabilities conversion stylesheets for a particular three letter OGC service type code. These stylesheets are stored in the ``OGCWxSGetCapabilitiesto19119`` directory in the ``convert`` directory of a metadata schema. eg. for schema iso19139: 

- the directory for these stylesheets would be ``GEONETWORK_DATA_DIR/config/schema_plugins/iso19139/convert/OGCWxSGetCapabilitiesto19119``
- if a conversion from the GetCapabilities statement of a particular OGC service to a metadata record of this schema exists, then a stylesheet for that serviceType will be present in the directory eg. for schema iso19139 and serviceType ``WFS``, the conversion stylesheet name would be ``OGCWFSGetCapabilities-to-ISO19119_ISO19139.xsl``

POST Request Example::
 
 <request>
  <type>ogcwxsOutputSchemas</type>
  <serviceType>WFS</serviceType>
 </request>

 URL:
 http://localhost:8080/geonetwork/srv/eng/xml.harvesting.info

Response Example::
 
 <root>
  <schemas>
    <record>
      <id>iso19139</id>
      <name>iso19139</name>
    </record>
  </schemas>
 </root>

Errors
``````

If an error occurred then HTTP status code 500 is returned along with an XML document which contains details of what went wrong. An example of such an error response is:

::
 
 <error id="bad-parameter">
   <message>type</message>
   <class>BadParameterEx</class> 
   .....
 </error>

See :ref:`exception_handling` for more details.

 
Update a Harvester Instance (xml.harvesting.update)
---------------------------------------------------

This service can be used to change the parameters of a harvester instance. 

.. note:: You cannot change the harvester type.

Request
```````

The simplest way to use this service is to:

#. use the ``xml.harvesting.get`` service to obtain the XML definition of the harvester that you want to update.
#. modify the parameters as required.
#. call this service with the modified XML definition of the harvester as the request.

The XML request is the same as that used in ``xml.harvesting.add``.

Response
````````

If the update succeeded then HTTP status code 200 is returned along with an XML document containing the harvester definition as supplied in the request.

If an error occurred then HTTP status code 500 is returned along with an XML document which contains details of what went wrong. An example of such an error response is:

::
 
 <error id="object-not-found">
   <message>Object not found</message>
   <class>ObjectNotFoundEx</class> 
   .....
 </error>

See :ref:`exception_handling` for more details.

.. index:: xml.harvesting.remove
.. index:: xml.harvesting.start
.. index:: xml.harvesting.stop
.. index:: xml.harvesting.run

Control or Remove a Harvester Instance (xml.harvesting.remove, xml.harvesting.start, xml.harvesting.stop, xml.harvesting.run)
-----------------------------------------------------------------------------------------------------------------------------

These services are described in on section because they share a common request
interface. Their purpose is to remove, start, stop or run a harvester:

#.  **remove**: Remove a harvester. Deletes the harvester instance.

#.  **start**: When created, a harvester is in the inactive state. This operation makes it active which means it will be run at the enxt scheduled time.

#.  **stop**: Makes a harvester inactive - it will no longer be executed at the scheduled time. Note this will *not* stop a harvester that is already performing a harvest.

#.  **run**: Start the harvester now. Used to test the harvesting.

Request
```````

A set of ids to operate on. Example::

    <request>
        <id>123</id>
        <id>456</id>
        <id>789</id>
    </request>

Response
````````

Similar to the request but every id has a status attribute indicating the
success or failure of the operation. For example, the response to the
previous request could be::

    <response>
        <id status="ok">123</id>
        <id status="not-found">456</id>
        <id status="inactive">789</id>
    </response>

The table below summarises, for each service, the
possible status values.


================    ======  =====   ====    ====
Status value        remove  start   stop    run
================    ======  =====   ====    ====
ok                    X       X       X       X 
not-found             X       X       X       X 
inactive                                      X 
already-inactive                      X     
already-active                X             
already-running                               X 
================    ======  =====   ====    ====

If the request has no id parameters, an empty response is returned.

Most errors relating to a harvester specified in the request (eg. harvester id not found) are returned as status attributes in the response. However, exceptions can still occur, in which case HTTP status code 500 is returned along with an XML document which contains details of what went wrong. An example of such an error response is:

::
 
 <error id="service-not-allowed">
   <message>Service not allowed</message>
   <class>ServiceNotAllowedEx</class> 
   .....
 </error>

See :ref:`exception_handling` for more details.

.. index:: xml.harvesting.history

.. _xml.harvesting.history:

Retrieve Harvesting History (xml.harvesting.history)
----------------------------------------------------

This service can be used to retrive the history of harvest sessions for a 
specified harvester instance or all harvester instances. The harvester history
information is stored in the GeoNetwork database in the HarvestHistory table.

Request
```````

Called without an **id** parameter, this service returns the harvest history of all harvesters. The response can be sorted by harvest *date* or by harvester *type*. The sort order is specified in the parameter **sort**. Example::

    <request>
      <sort>date</sort>
    </request>

Otherwise, an **id** parameter can be specified to request the harvest history of a specific harvester instance. In this case the sort order is by *date* of harvest::

    <request>
      <id>123</id>
    </request>


Response
````````

If the update succeeded then HTTP status code 200 is returned along with an XML document containing the harvest history. The response for both types of requests is the same except that the response to a request for the history of a specific harvester will only have history details for that harvester. An example of the response is::

 <response>
  <response>
    <record>
      <id>1</id>
      <harvestdate>2013-01-01T19:24:54</harvestdate>
      <harvesteruuid>b6a11fc3-3f6f-494b-a8f3-35eaadced575</harvesteruuid>
      <harvestername>test plaja</harvestername>
      <harvestertype>geonetwork</harvestertype>
      <deleted>n</deleted>
      <info>
        <result>
          <total>5</total>
          <added>5</added>
          <updated>0</updated>
          <unchanged>0</unchanged>
          <unknownSchema>0</unknownSchema>
          <removed>0</removed>
          <unretrievable>0</unretrievable>
          <doesNotValidate>0</doesNotValidate>
        </result>
      </info>
      <params>	 
        .....
      </params>
    </record>
  </response>
  <nodes>
    <node id="955" type="geonetwork">
      .....
    </node>
    .....
  </nodes>
  <sort>date</sort>
 </response> 
 
Each **record** element in the embedded **response** element contains the details of a harvest session. The elements are:

- **id** - harvest history record id in harvesthistory table
- **harvestdate** - date of harvest
- **harvesteruuid** - uuid of harvester that ran
- **harvestername** - name of harvester (Site/Name parameter) that ran
- **harvestertype** - type of harvester that ran
- **deleted** - has the harvester that ran been deleted? 'y' - yes, 'n' - no
- **info** - results of the harvest. May contain one of the following elements:

 - **result** - details of the successful harvest (a harvester dependent list of results from the harvest)
 - **error** - an exception from an unsuccessful harvest - see :ref:`exception_handling` for content details of this element

- **params** - the parameters that the harvester had been configured with for the harvest


After the embedded **response** element, the currently configured harvesters are returned as **node** children of a **nodes** element - see :ref:`xml.harvesting.add` for references to each of the harvester types that can be returned here.

If an error occurred then HTTP status code 500 is returned along with an XML document which contains details of what went wrong. An example of such an error response is:

::
 
 <error id="object-not-found">
   <message>Object not found</message>
   <class>ObjectNotFoundEx</class> 
   .....
 </error>

See :ref:`exception_handling` for more details.

.. index:: xml.harvesting.history.delete

Delete Harvesting History Entries (xml.harvesting.history.delete)
-----------------------------------------------------------------

This service can be used to delete harvester history entries from the harvesthistory table in the GeoNetwork database.

Request
```````

One or more **id** parameters can be specified to request deletion of the harvest history entries in the harvesthistory table. The **id** element values can be obtained from :ref:`xml.harvesting.history`::

    <request>
      <id>1</id>
      <id>2</id>
    </request>

Response
````````

If successful then HTTP status code 200 is returned along with an XML document with details of how many harvest history records were successfully deleted. An example of this response is::

 <response>2</response>

.. note:: If records with the id specified in the parameters are not present, they will be quietly ignored.

If an error occurred then HTTP status code 500 is returned along with an XML document which contains details of what went wrong. An example of such an error response is:

::
 
 <error id="service-not-allowed">
   <message>Service not allowed</message>
   <class>ServiceNotAllowedEx</class> 
   .....
 </error>

See :ref:`exception_handling` for more details.
