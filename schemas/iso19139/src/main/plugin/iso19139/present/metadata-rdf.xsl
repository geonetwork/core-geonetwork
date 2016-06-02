<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2001-2016 Food and Agriculture Organization of the
  ~ United Nations (FAO-UN), United Nations World Food Programme (WFP)
  ~ and United Nations Environment Programme (UNEP)
  ~
  ~ This program is free software; you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation; either version 2 of the License, or (at
  ~ your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful, but
  ~ WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  ~ General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
  ~
  ~ Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
  ~ Rome - Italy. email: geonetwork@osgeo.org
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:saxon="http://saxon.sf.net/"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
                xmlns:foaf="http://xmlns.com/foaf/0.1/"
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:ogc="http://www.opengis.net/rdf#"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:iso19139="http://geonetwork-opensource.org/schemas/iso19139"
                version="2.0"
                extension-element-prefixes="saxon" exclude-result-prefixes="#all">


  <!-- TODO : add Multilingual metadata support
    See http://www.w3.org/TR/2004/REC-rdf-syntax-grammar-20040210/#section-Syntax-languages

    TODO : maybe some characters may be encoded / avoid in URIs
    See http://www.w3.org/TR/2004/REC-rdf-concepts-20040210/#dfn-URI-reference
  -->

  <!--
    Create reference block to metadata record and dataset to be added in dcat:Catalog usually.
  -->
  <!-- FIME : $url comes from a global variable. -->
  <xsl:template match="gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']" mode="record-reference">
    <!-- TODO : a metadata record may contains aggregate. In that case create one dataset per aggregate member. -->
    <dcat:dataset rdf:resource="{$url}/resource/{iso19139:getResourceCode(.)}"/>
    <dcat:record rdf:resource="{$url}/metadata/{gmd:fileIdentifier/gco:CharacterString}"/>
  </xsl:template>


  <!--
    Convert ISO record to DCAT
    -->
  <xsl:template match="gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']" mode="to-dcat">


    <!-- Catalogue records
      "A record in a data catalog, describing a single dataset."

      xpath: //gmd:MD_Metadata|//*[@gco:isoType='gmd:MD_Metadata']
    -->
    <dcat:CatalogRecord rdf:about="{$url}/metadata/{gmd:fileIdentifier/gco:CharacterString}">
      <!-- Link to a dcat:Dataset or a rdf:Description for services and feature catalogue. -->
      <foaf:primaryTopic rdf:resource="{$url}/resource/{iso19139:getResourceCode(.)}"/>

      <!-- Metadata change date.
      "The date is encoded as a literal in "YYYY-MM-DD" form (ISO 8601 Date and Time Formats)." -->
      <xsl:variable name="date" select="substring-before(gmd:dateStamp/gco:DateTime, 'T')"/>
      <dct:issued>
        <xsl:value-of select="$date"/>
      </dct:issued>
      <dct:modified>
        <xsl:value-of select="$date"/>
      </dct:modified>
      <!-- xpath: gmd:dateStamp/gco:DateTime -->

      <xsl:call-template name="add-reference">
        <xsl:with-param name="uuid" select="gmd:fileIdentifier/gco:CharacterString"/>
      </xsl:call-template>
    </dcat:CatalogRecord>

    <xsl:apply-templates select="gmd:identificationInfo/*" mode="to-dcat"/>

  </xsl:template>


  <!-- Add references for HTML and XML metadata record link -->
  <xsl:template name="add-reference">
    <xsl:param name="uuid"/>

    <dct:references>
      <rdf:Description rdf:about="{$url}/srv/eng/xml.metadata.get?uuid={$uuid}">
        <dct:format>
          <dct:IMT>
            <rdf:value>application/xml</rdf:value>
            <rdfs:label>XML</rdfs:label>
          </dct:IMT>
        </dct:format>
      </rdf:Description>
    </dct:references>

    <dct:references>
      <rdf:Description rdf:about="{$url}?uuid={$uuid}">
        <dct:format>
          <dct:IMT>
            <rdf:value>text/html</rdf:value>
            <rdfs:label>HTML</rdfs:label>
          </dct:IMT>
        </dct:format>
      </rdf:Description>
    </dct:references>
  </xsl:template>

  <!-- Create all references for ISO19139 record (if rdf.metadata.get) or records (if rdf.search) -->
  <xsl:template match="gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']" mode="references">

    <!-- Keywords -->
    <xsl:for-each-group
      select="//gmd:MD_Keywords[(gmd:thesaurusName)]/gmd:keyword/gco:CharacterString" group-by=".">
      <!-- FIXME maybe only do that, if keyword URI is available (when xlink is used ?) -->
      <skos:Concept
        rdf:about="{$url}/thesaurus/{iso19139:getThesaurusCode(../../gmd:thesaurusName)}/{encode-for-uri(.)}">
        <skos:inScheme
          rdf:resource="{$url}/thesaurus/{iso19139:getThesaurusCode(../../gmd:thesaurusName)}"/>
        <skos:prefLabel>
          <xsl:value-of select="."/>
        </skos:prefLabel>
      </skos:Concept>
    </xsl:for-each-group>


    <!-- Distribution
      "Represents a specific available form of a dataset. Each dataset might be available in different
      forms, these forms might represent different formats of the dataset, different endpoints,...
      Examples of Distribution include a downloadable CSV file, an XLS file representing the dataset,
      an RSS feed ..."

      Download, WebService, Feed

      xpath: //gmd:distributionInfo/*/gmd:transferOptions/*/gmd:onLine/gmd:CI_OnlineResource
    -->
    <xsl:for-each-group
      select="//gmd:distributionInfo/*/gmd:transferOptions/*/gmd:onLine/gmd:CI_OnlineResource"
      group-by="gmd:linkage/gmd:URL">
      <dcat:Distribution rdf:about="{gmd:linkage/gmd:URL}">
        <!--
          "points to the location of a distribution. This can be a direct download link, a link
          to an HTML page containing a link to the actual data, Feed, Web Service etc.
          the semantic is determined by its domain (Distribution, Feed, WebService, Download)."
        -->
        <dcat:accessURL>
          <xsl:value-of select="gmd:linkage/gmd:URL"/>
        </dcat:accessURL>
        <!-- xpath: gmd:linkage/gmd:URL -->

        <xsl:if test="gmd:name/gco:CharacterString!=''">
          <dct:title>
            <xsl:value-of select=" gmd:name/gco:CharacterString"/>
          </dct:title>
        </xsl:if>
        <!-- xpath: gmd:name/gco:CharacterString -->

        <!-- "The size of a distribution.":N/A
          <dcat:size></dcat:size>
        -->

        <xsl:if test="gmd:protocol/gco:CharacterString!=''">
          <dct:format>
            <!--
              "the file format of the distribution."

              "MIME type is used for values. A list of MIME types URLs can be found at IANA.
              However ESRI Shape files have no specific MIME type (A Shape distribution is actually
              a collection of files), currently this is still an open question?"

              In our case, Shapefile will be zipped !

              Mapping between protocol list and mime/type when needed
            -->
            <dct:IMT>
              <rdf:value>
                <xsl:value-of select="gmd:protocol/gco:CharacterString"/>
              </rdf:value>
              <rdfs:label>
                <xsl:value-of select="gmd:protocol/gco:CharacterString"/>
              </rdfs:label>
            </dct:IMT>
          </dct:format>
        </xsl:if>
        <!-- xpath: gmd:protocol/gco:CharacterString -->

      </dcat:Distribution>
    </xsl:for-each-group>


    <xsl:for-each-group
      select="//gmd:CI_ResponsibleParty[gmd:organisationName/gco:CharacterString!='']"
      group-by="gmd:organisationName/gco:CharacterString">
      <!-- Organization description.
        Organization could be linked to a catalogue, a catalogue record.

        xpath: //gmd:organisationName
      -->
      <foaf:Organization rdf:about="{$url}/organization/{encode-for-uri(current-grouping-key())}">
        <foaf:name>
          <xsl:value-of select="current-grouping-key()"/>
        </foaf:name>
        <!-- xpath: gmd:organisationName/gco:CharacterString -->
        <xsl:for-each-group
          select="//gmd:CI_ResponsibleParty[gmd:organisationName/gco:CharacterString=current-grouping-key()]"
          group-by="gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString">
          <foaf:member
            rdf:resource="{$url}/organization/{encode-for-uri(iso19139:getContactId(.))}"/>
        </xsl:for-each-group>
      </foaf:Organization>
    </xsl:for-each-group>


    <xsl:for-each-group select="//gmd:CI_ResponsibleParty"
                        group-by="gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString">
      <!-- Organization memeber

        xpath: //gmd:CI_ResponsibleParty-->

      <foaf:Agent rdf:about="{$url}/person/{encode-for-uri(iso19139:getContactId(.))}">
        <xsl:if test="gmd:individualName/gco:CharacterString">
          <foaf:name>
            <xsl:value-of select="gmd:individualName/gco:CharacterString"/>
          </foaf:name>
        </xsl:if>
        <!-- xpath: gmd:individualName/gco:CharacterString -->
        <xsl:if
          test="gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:voice/gco:CharacterString">
          <foaf:phone>
            <xsl:value-of
              select="gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:voice/gco:CharacterString"/>
          </foaf:phone>
        </xsl:if>
        <!-- xpath: gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:voice/gco:CharacterString -->
        <xsl:if
          test="gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString">
          <foaf:mbox
            rdf:resource="mailto:{gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString}"/>
        </xsl:if>
        <!-- xpath: gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString -->
      </foaf:Agent>
    </xsl:for-each-group>
  </xsl:template>


  <!-- Service
    Create a simple rdf:Description. To be improved.

    xpath: //srv:SV_ServiceIdentification||//*[contains(@gco:isoType, 'SV_ServiceIdentification')]
  -->
  <xsl:template
    match="srv:SV_ServiceIdentification|*[contains(@gco:isoType, 'SV_ServiceIdentification')]"
    mode="to-dcat">
    <rdf:Description rdf:about="{$url}/resource/{iso19139:getResourceCode(../../.)}">
      <xsl:call-template name="to-dcat"/>
    </rdf:Description>
  </xsl:template>


  <!-- Dataset
    "A collection of data, published or curated by a single source, and available for access or
    download in one or more formats."

    xpath: //gmd:MD_DataIdentification|//*[contains(@gco:isoType, 'MD_DataIdentification')]
  -->
  <xsl:template match="gmd:MD_DataIdentification|*[contains(@gco:isoType, 'MD_DataIdentification')]"
                mode="to-dcat">
    <dcat:Dataset rdf:about="{$url}/resource/{iso19139:getResourceCode(../../.)}">
      <xsl:call-template name="to-dcat"/>
    </dcat:Dataset>
  </xsl:template>


  <!-- Build a dcat record for a dataset or service -->
  <xsl:template name="to-dcat">
    <!-- "A unique identifier of the dataset." -->
    <dct:identifier>
      <xsl:value-of select="iso19139:getResourceCode(../../.)"/>
    </dct:identifier>
    <!-- xpath: gmd:identificationInfo/*/gmd:citation/*/gmd:identifier/*/gmd:code -->


    <dct:title>
      <xsl:value-of select="gmd:citation/*/gmd:title/gco:CharacterString"/>
    </dct:title>
    <!-- xpath: gmd:identificationInfo/*/gmd:citation/*/gmd:title/gco:CharacterString -->


    <dct:abstract>
      <xsl:value-of select="gmd:abstract/gco:CharacterString"/>
    </dct:abstract>
    <!-- xpath: gmd:identificationInfo/*/gmd:abstract/gco:CharacterString -->


    <!-- "A keyword or tag describing the dataset."
      Create dcat:keyword if no thesaurus name information available.
    -->
    <xsl:for-each
      select="gmd:descriptiveKeywords/gmd:MD_Keywords[not(gmd:thesaurusName)]/gmd:keyword/gco:CharacterString">
      <dcat:keyword>
        <xsl:value-of select="."/>
      </dcat:keyword>
    </xsl:for-each>
    <!-- xpath: gmd:identificationInfo/*/gmd:descriptiveKeywords/gmd:MD_Keywords[not(gmd:thesaurusName)]/gmd:keyword/gco:CharacterString -->


    <!-- "The main category of the dataset. A dataset can have multiple themes."
      Create dcat:theme if gmx:Anchor or GEMET concepts or INSPIRE themes
    -->
    <xsl:for-each
      select="gmd:descriptiveKeywords/gmd:MD_Keywords[(gmd:thesaurusName)]/gmd:keyword/gco:CharacterString">
      <!-- FIXME maybe only do that, if keyword URI is available (when xlink is used ?) -->
      <dcat:theme
        rdf:resource="{$url}/thesaurus/{iso19139:getThesaurusCode(../../gmd:thesaurusName)}/{.}"/>
    </xsl:for-each>
    <!-- xpath: gmd:identificationInfo/*/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gmx:Anchor -->
    <!-- xpath: gmd:identificationInfo/*/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharaceterString -->
    <!-- xpath: gmd:identificationInfo/*/gmd:topicCategory/gmd:MD_TopicCategoryCode -->
    <xsl:for-each select="gmd:topicCategory/gmd:MD_TopicCategoryCode[.!='']">
      <!-- FIXME Is there any public URI pointing to topicCategory enumeration ? -->
      <dcat:theme rdf:resource="{$url}/thesaurus/iso/topicCategory/{.}"/>
    </xsl:for-each>

    <!-- Thumbnail -->
    <xsl:for-each
      select="gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString">
      <foaf:thumbnail rdf:resource="{.}"/>
    </xsl:for-each>
    <!-- xpath: gmd:identificationInfo/*/gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString -->


    <!-- "Spatial coverage of the dataset." -->
    <xsl:for-each select="gmd:extent/*/gmd:geographicElement/gmd:EX_GeographicBoundingBox">
      <xsl:variable name="coords" select="
        concat(gmd:westBoundLongitude/gco:Decimal, ' ', gmd:southBoundLatitude/gco:Decimal),
        concat(gmd:westBoundLongitude/gco:Decimal, ' ', gmd:northBoundLatitude/gco:Decimal),
        concat(gmd:eastBoundLongitude/gco:Decimal, ' ', gmd:northBoundLatitude/gco:Decimal),
        concat(gmd:eastBoundLongitude/gco:Decimal, ' ', gmd:southBoundLatitude/gco:Decimal),
        concat(gmd:westBoundLongitude/gco:Decimal, ' ', gmd:southBoundLatitude/gco:Decimal)
        ">
      </xsl:variable>
      <dct:spatial>
        <ogc:Polygon>
          <ogc:asWKT rdf:datatype="http://www.opengis.net/rdf#WKTLiteral">
            &lt;http://www.opengis.net/def/crs/OGC/1.3/CRS84&gt;
            Polygon((<xsl:value-of select="string-join($coords, ', ')"/>))
          </ogc:asWKT>
        </ogc:Polygon>
      </dct:spatial>
    </xsl:for-each>
    <!-- xpath: gmd:identificationInfo/*/gmd:extent/*/gmd:geographicElement/gmd:EX_GeographicBoundingBox -->


    <!-- "The temporal period that the dataset covers." -->
    <!-- TODO could be improved-->
    <xsl:for-each
      select="gmd:extent/*/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod">
      <dct:temporal>
        <xsl:value-of select="gml:beginPosition"/>
        <xsl:if test="gml:endPosition">
          /
          <xsl:value-of select="gml:endPosition"/>
        </xsl:if>
      </dct:temporal>
    </xsl:for-each>
    <!-- xpath: gmd:identificationInfo/*/gmd:extent/*/gmd:temporalElement -->

    <xsl:for-each
      select="gmd:citation/*/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']">
      <dct:issued>
        <xsl:value-of select="gmd:date/gco:Date|gmd:date/gco:DateTime"/>
      </dct:issued>
    </xsl:for-each>
    <xsl:for-each
      select="gmd:citation/*/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']">
      <dct:updated>
        <xsl:value-of select="gmd:date/gco:Date|gmd:date/gco:DateTime"/>
      </dct:updated>
    </xsl:for-each>

    <!-- "An entity responsible for making the dataset available" -->
    <xsl:for-each select="gmd:pointOfContact/*/gmd:organisationName/gco:CharacterString[.!='']">
      <dct:publisher rdf:resource="{$url}/organization/{encode-for-uri(.)}"/>
    </xsl:for-each>
    <!-- xpath: gmd:identificationInfo/*/gmd:pointOfContact -->


    <!-- "The frequency with which dataset is published." See placetime.com intervals. -->
    <xsl:for-each
      select="gmd:resourceMaintenance/gmd:MD_MaintenanceInformation/gmd:maintenanceAndUpdateFrequency/gmd:MD_MaintenanceFrequencyCode">
      <dct:accrualPeriodicity>
        <xsl:value-of select="@codeListValue"/>
      </dct:accrualPeriodicity>
    </xsl:for-each>
    <!-- xpath: gmd:identificationInfo/*/gmd:resourceMaintenance/gmd:MD_MaintenanceInformation/gmd:maintenanceAndUpdateFrequency/gmd:MD_MaintenanceFrequencyCode/@codeListValue -->

    <!-- "This is usually geographical or temporal but can also be other dimension" ??? -->
    <xsl:for-each
      select="gmd:spatialResolution/gmd:MD_Resolution/gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer[.!='']">
      <dcat:granularity>
        <xsl:value-of select="."/>
      </dcat:granularity>
    </xsl:for-each>
    <!-- xpath: gmd:identificationInfo/*/gmd:spatialResolution/gmd:MD_Resolution/gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer -->


    <!--
      "The language of the dataset."
      "This overrides the value of the catalog language in case of conflict"
    -->
    <xsl:for-each select="gmd:language/gmd:LanguageCode/@codeListValue">
      <dct:language>
        <xsl:value-of select="."/>
      </dct:language>
    </xsl:for-each>
    <!-- xpath: gmd:identificationInfo/*/gmd:language/gmd:LanguageCode/@codeListValue -->


    <!-- "The license under which the dataset is published and can be reused." -->
    <xsl:for-each select="gmd:resourceConstraints/gmd:MD_LegalConstraints/*/gmd:MD_RestrictionCode">
      <dct:license>
        <xsl:value-of select="@codeListValue"/>
      </dct:license>
    </xsl:for-each>
    <xsl:for-each
      select="gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:otherConstraints/gco:CharacterString">
      <dct:license>
        <xsl:value-of select="."/>
      </dct:license>
    </xsl:for-each>
    <!-- xpath: gmd:identificationInfo/*/gmd:resourceConstraints/??? -->


    <xsl:for-each select="../../gmd:distributionInfo/*/gmd:transferOptions/*/gmd:onLine">
      <dcat:distribution rdf:resource="{gmd:CI_OnlineResource/gmd:linkage/gmd:URL}"/>
    </xsl:for-each>
    <!-- xpath: gmd:distributionInfo/*/gmd:transferOptions/*/gmd:onLine/gmd:CI_OnlineResource -->


    <!-- ISO19110 relation
      "This usually consisits of a table providing explanation of columns meaning, values interpretation and acronyms/codes used in the data."
    -->
    <xsl:for-each
      select="../../gmd:contentInfo/gmd:MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/@uuidref ">
      <dcat:dataDictionary rdf:resource="{$url}/metadata/{.}"/>
    </xsl:for-each>
    <!-- xpath: gmd:contentInfo/gmd:MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/@uuidref -->

    <!-- Dataset relation
    -->
    <xsl:for-each select="srv:operatesOn/@uuidref ">
      <dct:relation rdf:resource="{$url}/metadata/{.}"/>
    </xsl:for-each>


    <xsl:for-each select="gmd:aggregationInfo/gmd:MD_AggregateInformation">
      <dct:relation
        rdf:resource="{$url}/metadata/{gmd:aggregateDataSetIdentifier/gmd:MD_Identifier/gmd:code/gco:CharacterString}"/>
    </xsl:for-each>

    <!-- Source relation -->
    <xsl:for-each select="/root/gui/relation/sources/response/metadata">
      <dct:relation rdf:resource="{$url}/metadata/{geonet:info/uuid}"/>
    </xsl:for-each>


    <!-- Parent/child relation -->
    <xsl:for-each select="../../gmd:parentIdentifier/gco:CharacterString[.!='']">
      <dct:relation rdf:resource="{$url}/metadata/{.}"/>
    </xsl:for-each>
    <xsl:for-each select="/root/gui/relation/children/response/metadata">
      <dct:relation rdf:resource="{$url}/metadata/{geonet:info/uuid}"/>
    </xsl:for-each>

    <!-- Service relation -->
    <xsl:for-each select="/root/gui/relations/services/response/metadata">
      <dct:relation rdf:resource="{$url}/metadata/{geonet:info/uuid}"/>
    </xsl:for-each>


    <!--
      "A related document such as technical documentation, agency program page, citation, etc."

      TODO : only for URL ?
      <xsl:for-each select="gmd:citation/*/gmd:otherCitationDetails/gco:CharacterString">
      <dct:reference rdf:resource="url?"/>
      </xsl:for-each>
    -->
    <!-- xpath: gmd:identificationInfo/*/gmd:citation/*/gmd:otherCitationDetails/gco:CharacterString -->


    <!-- "describes the quality of data." -->
    <xsl:for-each
      select="../../gmd:dataQualityInfo/*/gmd:lineage/gmd:LI_Lineage/gmd:statement/gco:CharacterString">
      <dcat:dataQuality>
        <!-- rdfs:literal -->
        <xsl:value-of select="."/>
      </dcat:dataQuality>
    </xsl:for-each>
    <!-- xpath: gmd:dataQualityInfo/*/gmd:lineage/gmd:LI_Lineage/gmd:statement/gco:CharacterString -->


    <!-- FIXME ?
      <void:dataDump></void:dataDump>-->
  </xsl:template>


  <!--
    Get resource (dataset or service) identifier if set and return metadata UUID if not.
  -->
  <xsl:function name="iso19139:getResourceCode" as="xs:string">
    <xsl:param name="metadata" as="node()"/>

    <xsl:value-of select="if ($metadata/gmd:identificationInfo/*/gmd:citation/*/gmd:identifier/*/gmd:code/gco:CharacterString!='')
      then $metadata/gmd:identificationInfo/*/gmd:citation/*/gmd:identifier/*/gmd:code/gco:CharacterString
      else $metadata/gmd:fileIdentifier/gco:CharacterString"/>
  </xsl:function>


  <!--
    Get thesaurus identifier, otherCitationDetails value, citation @id or thesaurus title.
  -->
  <xsl:function name="iso19139:getThesaurusCode" as="xs:string">
    <xsl:param name="thesaurusName" as="node()"/>

    <xsl:value-of select="if ($thesaurusName/*/gmd:otherCitationDetails/*!='') then $thesaurusName/*/gmd:otherCitationDetails/*
      else if ($thesaurusName/gmd:CI_Citation/@id!='') then $thesaurusName/gmd:CI_Citation/@id!=''
      else encode-for-uri($thesaurusName/*/gmd:title/gco:CharacterString)"/>
  </xsl:function>

  <!--
    Get contact identifier (for the time being = email and node generated identifier if no email available)
  -->
  <xsl:function name="iso19139:getContactId" as="xs:string">
    <xsl:param name="responsibleParty" as="node()"/>

    <xsl:value-of select="if ($responsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString!='')
      then $responsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString
      else generate-id($responsibleParty)"/>
  </xsl:function>

</xsl:stylesheet>
