
.. _metadata_xml_validation:

Metadata Validation services
============================

These services are for validating metadata against the XML schema documents (XSDs) and schematrons specified as part of a GeoNetwork Metadata Schema. See :ref:`schemaPlugins` for more details.

.. index:: xml.metadata.validation

Validate a metadata record (xml.metadata.validation)
----------------------------------------------------

This service can be used to validate a metadata record supplied as an XML parameter. The metadata record is first passed through the GeoNetwork schema detection rules (see :ref:`schemaPlugins`). After successful schema detection the metadata record is validated against the XML schema documents and schematrons (if any) specified in that schema.

Authentication required: No

Request
```````

Parameters:

- **data**: Metadata record.

Example with an ISO19115/19139 metadata record::

  Url:
  http://localhost:8080/geonetwork/srv/eng/xml.metadata.validation

  Mime-type:
  application/xml

  Post request:
  <?xml version="1.0" encoding="UTF-8"?>
  <request>
    <data><![CDATA[
      <gmd:MD_Metadata ....>
        .....
      </gmd:MD_Metadata>
    ]]></data>
  </request>

Response
````````

If the validation is successful an HTTP 200 response code is returned along with 
an XML document giving details of the GeoNetwork metadata schema that the record
matched and was successfully validated against.

Example::

  <?xml version="1.0" encoding="UTF-8"?>
  <response>
    <valid>y</valid>
    <schema>iso19139</schema>
  </response>

If the validation was not successful then an HTTP 500 error response code is returned along with an XML document describing the validation problems. An example is::
 
  <?xml version="1.0" encoding="UTF-8"?>
  <error id="xsd-validation-error">
    <message>XSD Validation error(s):
      .....
    </message>
    <stack>
      .....
    </stack>
    <object>
     <xsderrors>
      <error>
        <typeOfError>WARNING</typeOfError>
        <errorNumber>1</errorNumber>
        <message>....</message>
        <xpath>.</xpath>
      </error>
     </xsderrors>
    </object>
    <request>
      <language>eng</language>
      <service>xml.metadata.validation</service>
    </request>
   </error>

.. note:: XML parseable description of the validation problems is in the object container.

Validation may also fail when schematrons are applied to the metadata record. An HTTP error response code is returned along with an XML document describing the validation problems. An example is::
  
  <?xml version="1.0" encoding="UTF-8"?>
  <error id="schematron-validation-error">^M
    <message>Schematron errors detected
      .....
    </message>
    <stack>
      .....
    </stack>
    <object>
    <geonet:schematronerrors xmlns:geonet="http://www.fao.org/geonetwork">
      <geonet:report geonet:rule="schematron-rules-iso">
        <svrl:schematron-output xmlns:svrl="http://purl.oclc.org/dsdl/svrl" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:gml="http://www.opengis.net/gml" xmlns:schold="http://www.ascc.net/xml/schematron" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:iso="http://purl.oclc.org/dsdl/schematron" xmlns:xhtml="http://www.w3.org/1999/xhtml" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:gmd="http://www.isotc211.org/2005/gmd" title="Schematron validation for ISO&#xA;&#x9;&#x9;19115(19139)" schemaVersion="">
          <svrl:ns-prefix-in-attribute-values uri="http://www.opengis.net/gml" prefix="gml" />
          <svrl:ns-prefix-in-attribute-values uri="http://www.isotc211.org/2005/gmd" prefix="gmd" />
          <svrl:ns-prefix-in-attribute-values uri="http://www.isotc211.org/2005/srv" prefix="srv" />
          <svrl:ns-prefix-in-attribute-values uri="http://www.isotc211.org/2005/gco" prefix="gco" />
          <svrl:ns-prefix-in-attribute-values uri="http://www.fao.org/geonetwork" prefix="geonet" />
          <svrl:ns-prefix-in-attribute-values uri="http://www.w3.org/1999/xlink" prefix="xlink" />
          <svrl:active-pattern document="" name="CharacterString must have content or its parent must have a valid nilReason attribute." />
          <svrl:fired-rule context="*[gco:CharacterString]" />
          <svrl:fired-rule context="*[gco:CharacterString]" />
          <svrl:fired-rule context="*[gco:CharacterString]" />
          <svrl:fired-rule context="*[gco:CharacterString]" />
          <svrl:fired-rule context="*[gco:CharacterString]" />
          <svrl:fired-rule context="*[gco:CharacterString]" />
          <svrl:fired-rule context="*[gco:CharacterString]" />
          <svrl:fired-rule context="*[gco:CharacterString]" />
          <svrl:fired-rule context="*[gco:CharacterString]" />
          <svrl:fired-rule context="*[gco:CharacterString]" />
          <svrl:active-pattern document="" name="CRS attributes constraints" />
          <svrl:active-pattern document="" name="[ISOFTDS19139:2005-TableA1-Row24] - A name is required for contact" />
          <svrl:fired-rule context="//*[gmd:CI_ResponsibleParty]" />
          <svrl:failed-assert ref="#_31" test="$count &gt; 0" location="/*[local-name()='MD_Metadata']/*[local-name()='identificationInfo']/*[local-name()='MD_DataIdentification']/*[local-name()='citation']/*[local-name()='CI_Citation']/*[local-name()='citedResponsibleParty']">
            <svrl:text>
              <alert.M8>
                <div>
                  You must specify one or more of individualName, organisationName or positionName.
                  <span class="validationReportSuggestion">Suggestions: Check contact information for metadata contact or cited responsable party in identification section for example.</span>
                </div>
              </alert.M8>
            </svrl:text>
          </svrl:failed-assert>
          <svrl:active-pattern document="" name="[ISOFTDS19139:2005-TableA1-Row07] - OtherConstraints required if otherRestrictions" />
          <svrl:active-pattern document="" name="[ISOFTDS19139:2005-TableA1-Row16] - Units required for values" />
          <svrl:active-pattern document="" name="[ISOFTDS19139:2005-TableA1-Row13] - Description required if no sourceExtent" />
          <svrl:active-pattern document="" name="[ISOFTDS19139:2005-TableA1-Row10] - Content mandatory for dataset or series" />
          <svrl:active-pattern document="" name="[ISOFTDS19139:2005-TableA1-Row11 Row12] - Lineage" />
          <svrl:active-pattern document="" name="[ISOFTDS19139:2005-TableA1-Row08] - Dataset must have report or lineage" />
          <svrl:active-pattern document="" name="[ISOFTDS19139:2005-TableA1-Row09] - LevelDescription needed unless dataset or series" />
          <svrl:active-pattern document="" name="[ISOFTDS19139:2005-TableA1-Row17] - Units required for density values" />
          <svrl:active-pattern document="" name="[ISOFTDS19139:2005-TableA1-Row18] - Distribution format required" />
          <svrl:active-pattern document="" name="[ISOFTDS19139:2005-TableA1-Row23] - Extent element required" />
          <svrl:active-pattern document="" name="[ISOFTDS19139:2005-TableA1-Row04] - Dataset must have extent" />
          <svrl:fired-rule context="//gmd:MD_DataIdentification|//*[@gco:isoType='gmd:MD_DataIdentification']" />
          <svrl:successful-report ref="#_20" test="$extent = false()" location="/*[local-name()='MD_Metadata']/*[local-name()='identificationInfo']/*[local-name()='MD_DataIdentification']">
            <svrl:text>
              <report.M21>Extent defined for dataset.</report.M21>
            </svrl:text>
          </svrl:successful-report>
          <svrl:active-pattern document="" name="[ISOFTDS19139:2005-TableA1-Row05] - Dataset or series must have a topic category" />
          <svrl:fired-rule context="//gmd:MD_DataIdentification|//*[@gco:isoType='gmd:MD_DataIdentification']" />
          <svrl:successful-report ref="#_20" test="$topic = false" location="/*[local-name()='MD_Metadata']/*[local-name()='identificationInfo']/*[local-name()='MD_DataIdentification']">
            <svrl:text>
              <report.M6>Topic category is:</report.M6>
              ""
            </svrl:text>
          </svrl:successful-report>
          <svrl:active-pattern document="" name="[ISOFTDS19139:2005-TableA1-Row06] - Either aggregateDataSetName or aggregateDataSetIdentifier must be documented" />
          <svrl:active-pattern document="" name="[ISOFTDS19139:2005-TableA1-Row02] - Character set indication" />
          <svrl:fired-rule context="//gmd:MD_Metadata|//*[@gco:isoType='gmd:MD_Metadata']" />
          <svrl:active-pattern document="" name="[ISOFTDS19139:2005-TableA1-Row19] - Detail required unless simple term" />
          <svrl:active-pattern document="" name="[ISOFTDS19139:2005-TableA1-Row20] - Condition" />
          <svrl:active-pattern document="" name="[ISOFTDS19139:2005-TableA1-Row21] - DomainCode" />
          <svrl:active-pattern document="" name="[ISOFTDS19139:2005-TableA1-Row22] - ShortName" />
          <svrl:active-pattern document="" name="[ISOFTDS19139:2005-TableA1-Row15] - Check point description required if available" />
          <svrl:active-pattern document="" name="[ISOFTDS19139:2005-TableA1] - HierarchyLevelName must be documented if hierarchyLevel does not contain &quot;dataset&quot;" />
          <svrl:fired-rule context="//gmd:MD_Metadata/gmd:hierarchyLevel|//*[@gco:isoType='gmd:MD_Metadata']/gmd:hierarchyLevel" />
          <svrl:successful-report ref="#_6" test="$hl = false()" location="/*[local-name()='MD_Metadata']/*[local-name()='hierarchyLevel']">
            <svrl:text>
              <report.M61>Hierarchy level name is:</report.M61>
              "collectionSession"
            </svrl:text>
          </svrl:successful-report>
        </svrl:schematron-output>
      </geonet:report>
      <geonet:report geonet:rule="schematron-rules-geonetwork">
        <svrl:schematron-output xmlns:svrl="http://purl.oclc.org/dsdl/svrl" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:gml="http://www.opengis.net/gml" xmlns:schold="http://www.ascc.net/xml/schematron" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:iso="http://purl.oclc.org/dsdl/schematron" xmlns:xhtml="http://www.w3.org/1999/xhtml" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:gmd="http://www.isotc211.org/2005/gmd" title="Schematron validation / GeoNetwork recommendations" schemaVersion="">
          <svrl:ns-prefix-in-attribute-values uri="http://www.opengis.net/gml" prefix="gml" />
          <svrl:ns-prefix-in-attribute-values uri="http://www.isotc211.org/2005/gmd" prefix="gmd" />
          <svrl:ns-prefix-in-attribute-values uri="http://www.isotc211.org/2005/srv" prefix="srv" />
          <svrl:ns-prefix-in-attribute-values uri="http://www.isotc211.org/2005/gco" prefix="gco" />
          <svrl:ns-prefix-in-attribute-values uri="http://www.fao.org/geonetwork" prefix="geonet" />
          <svrl:ns-prefix-in-attribute-values uri="http://www.w3.org/1999/xlink" prefix="xlink" />
          <svrl:active-pattern document="" name="[Language] - Metadata language is not defined and other language are declared and Main metadata language MUST NOT be defined in other language section." />
          <svrl:fired-rule context="//gmd:MD_Metadata|//*[@gco:isoType='gmd:MD_Metadata']" />
          <svrl:successful-report ref="#_1" test="$localeAndNoLanguage" location="/*[local-name()='MD_Metadata']">
            <svrl:text>
              <report.M500>Main metadata language is:</report.M500>
              "eng"
            </svrl:text>
          </svrl:successful-report>
          <svrl:successful-report ref="#_1" test="$duplicateLanguage" location="/*[local-name()='MD_Metadata']">
            <svrl:text>
              <report.M501>No duplicate languages found.</report.M501>
            </svrl:text>
          </svrl:successful-report>
        </svrl:schematron-output>
      </geonet:report>
    </geonet:schematronerrors>
    </object>
  </error>

.. note:: XML parseable description of the schematron validation problems is in the object container. You should be looking for elements such as svrl:failed-assert.

See :ref:`exception_handling` for more details.

Errors
``````

- **bad-parameter XXXX**, when a
  mandatory parameter is empty. Returns 500 HTTP code

- **XSD Validation Error (error id:
  xsd-validation-error)**, when validation against XSDs fails.
  Returns 500 HTTP code

- **Schematron Validation Error (error id:
  schematron-validation-error)**, when validation against schematrons fails.
  Returns 500 HTTP code

- **No Schema Matches (error id:
  no-schema-matches)**, when a matching GeoNetwork metadata schema cannot be 
  found for the supplied metadata record.
  Returns 500 HTTP code

