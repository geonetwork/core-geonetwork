.. _processing:

Processing
==========

GeoNetwork can batch process metadata records by applying an XSLT. The processing XSLTs are schema dependent and must be stored in the process folder of each metadata schema. For example, the process folder for the iso19139 metadata schema can be found in ``GEONETWORK_DATA_DIR/config/schema_plugins/iso19139/process``.

Some examples of batch processing are:

 * Filtering harvested records from another GeoNetwork node (See :ref:`harvesting_gn` in the Harvesting section of this manual)
 * Suggesting content for metadata elements (editor suggestion mechanism)
 * Applying an XSLT to a selected set of metadata records by using the xml.batch.processing service (this service does not have a user interface, it is intended to be used with an http submitter such as curl).

Process available
-----------------

Anonymizer
~~~~~~~~~~

 * schema: ISO19139
 * usage: Harvester

Anonymiser is an XSL transformation provided for ISO19139 records which removes
all resource contacts except point of contact. In addition, it has three custom options to replace email addresses, remove keywords and remove internal online resources. These options are controlled by the following parameters:
 
 * protocol: Protocol of the online resources that must be removed
 
 * email: Generic email to use for all email addresses in a particular domain (ie. after @domain.org).
 
 * thesaurus: Portion of thesaurus name for which keywords should be removed
 
It could be used in the GeoNetwork harvesting XSL filter configuration using::

  anonymizer?protocol=DB:&email=gis-service@myorganisation.org&thesaurus=MYINTERNALTHESAURUS


Scale denominator formatter
~~~~~~~~~~~~~~~~~~~~~~~~~~~

 * schema: ISO19139
 * usage: Suggestion

Format scale which contains " ", "/" or ":" characters.

Add extent form geographic keywords
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 * schema: ISO19139
 * usage: Suggestion

Compute extent based on keyword of type place using installed thesaurus.

WMS synchronizer
~~~~~~~~~~~~~~~~

 * schema: ISO19139
 * usage: Suggestion

If an OGC WMS server is defined in distribution section, suggest that the user add extent, CRS and graphic overview based on that WMS.


Add INSPIRE conformity
~~~~~~~~~~~~~~~~~~~~~~

 * schema: ISO19139
 * usage: Suggestion

If INSPIRE themes are found, suggest that the user add an INSPIRE conformity section.


Add INSPIRE data quality report
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 * schema: ISO19139
 * usage: Suggestion

Suggest the creation of a default topological consistency report
when INSPIRE theme is set to Hydrography, Transport Networks or Utility and governmental services

Keywords comma exploder
~~~~~~~~~~~~~~~~~~~~~~~

 * schema: ISO19139
 * usage: Suggestion

Suggest that comma separated keywords be expanded to remove the commas (which is better for indexing and searching).

Keywords mapper
~~~~~~~~~~~~~~~

 * schema: ISO19139
 * usage: Batch process
 
Process records and map keyword define in a mapping table (to be defined manually in the process).


Linked data checker
~~~~~~~~~~~~~~~~~~~

 * schema: ISO19139
 * usage: Suggestion

Check URL status and suggest to remove the link on error.


Thumbnail linker
~~~~~~~~~~~~~~~~

 * schema: ISO19139
 * usage: Batch process

This batch process creates a browse graphic or thumbnail for all metadata records.

Process parameters:

 * prefix: thumbnail URL prefix (mandatory)
 
 * thumbnail_name: Name of the element to use in the metadata for the thumbnail file name (without extension). This element should be unique in a record. Default is gmd:fileIdentifier (optional).
 
 * thumbnail_desc: Thumbnail description (optional).

 * thumbnail_type: Thumbnail type (optional).
 
 * suffix: Thumbnail file extension. Default is .png (optional).


Inserted fragment is::

    <gmd:graphicOverview>
        <gmd:MD_BrowseGraphic>
          <gmd:fileName>
            <gco:CharacterString>$prefix + $thumbnail_name + $suffix</gco:CharacterString>
          </gmd:fileName>
          <gmd:fileDescription>
            <gco:CharacterString>$thumbnail_desc</gco:CharacterString>
          </gmd:fileDescription>
          <gmd:fileType>
            <gco:CharacterString>$thumbnail_type</gco:CharacterString>
          </gmd:fileType>
        </gmd:MD_BrowseGraphic>
    </gmd:graphicOverview>

