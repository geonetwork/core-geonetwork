<?xml version="1.0" encoding="UTF-8"?>

<!--

  Copyright 2015-2023 EUROPEAN UNION
  Licensed under the EUPL, Version 1.2 or - as soon they will be approved by
  the European Commission - subsequent versions of the EUPL (the "Licence");
  You may not use this work except in compliance with the Licence.
  You may obtain a copy of the Licence at:

  https://joinup.ec.europa.eu/collection/eupl

  Unless required by applicable law or agreed to in writing, software
  distributed under the Licence is distributed on an "AS IS" basis,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the Licence for the specific language governing permissions and
  limitations under the Licence.

  Contributors: GeoDCAT-AP Working Group (https://github.com/SEMICeu/geodcat-ap)

  This work was originally supported by the EU Interoperability Solutions for
  European Public Administrations Programme (http://ec.europa.eu/isa)
  through Action 1.17: Re-usable INSPIRE Reference Platform
  (http://ec.europa.eu/isa/actions/01-trusted-information-exchange/1-17action_en.htm).

  Source code:  https://github.com/SEMICeu/iso-19139-to-dcat-ap

-->

<!--

  PURPOSE AND USAGE

  This XSLT is a proof of concept for the implementation of the latest version
  of the geospatial profile of DCAT-AP (GeoDCAT-AP):

    https://semiceu.github.io/GeoDCAT-AP/releases/

  As such, this XSLT must be considered as unstable, and can be updated any
  time based on the revisions to the GeoDCAT-AP specifications and
  related work in the framework of INSPIRE and the EU ISA² Programme.

  The official distributions of this XSLT are published in the dedicated GitHub
  repository:

    https://github.com/SEMICeu/iso-19139-to-dcat-ap

  Comments and inquiries should be sent via the corresponding issue tracker:

    https://github.com/SEMICeu/iso-19139-to-dcat-ap/issues

-->

<xsl:transform
        xmlns:adms   = "http://www.w3.org/ns/adms#"
        xmlns:cnt    = "http://www.w3.org/2011/content#"
        xmlns:dc     = "http://purl.org/dc/elements/1.1/"
        xmlns:dcat   = "http://www.w3.org/ns/dcat#"
        xmlns:dct    = "http://purl.org/dc/terms/"
        xmlns:dctype = "http://purl.org/dc/dcmitype/"
        xmlns:dqv    = "http://www.w3.org/ns/dqv#"
        xmlns:earl   = "http://www.w3.org/ns/earl#"
        xmlns:foaf   = "http://xmlns.com/foaf/0.1/"
        xmlns:gco    = "http://www.isotc211.org/2005/gco"
        xmlns:geodcatap = "http://data.europa.eu/930/"
        xmlns:gmd    = "http://www.isotc211.org/2005/gmd"
        xmlns:gml    = "http://www.opengis.net/gml"
        xmlns:gmx    = "http://www.isotc211.org/2005/gmx"
        xmlns:gsp    = "http://www.opengis.net/ont/geosparql#"
        xmlns:i      = "http://inspire.ec.europa.eu/schemas/common/1.0"
        xmlns:i-gp   = "http://inspire.ec.europa.eu/schemas/geoportal/1.0"
        xmlns:locn   = "http://www.w3.org/ns/locn#"
        xmlns:owl    = "http://www.w3.org/2002/07/owl#"
        xmlns:org    = "http://www.w3.org/ns/org#"
        xmlns:prov   = "http://www.w3.org/ns/prov#"
        xmlns:rdf    = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:rdfs   = "http://www.w3.org/2000/01/rdf-schema#"
        xmlns:schema = "http://schema.org/"
        xmlns:sdmx-attribute = "http://purl.org/linked-data/sdmx/2009/attribute#"
        xmlns:skos   = "http://www.w3.org/2004/02/skos/core#"
        xmlns:srv    = "http://www.isotc211.org/2005/srv"
        xmlns:vcard  = "http://www.w3.org/2006/vcard/ns#"
        xmlns:wdrs   = "http://www.w3.org/2007/05/powder-s#"
        xmlns:xlink  = "http://www.w3.org/1999/xlink"
        xmlns:xsi    = "http://www.w3.org/2001/XMLSchema-instance"
        xmlns:xsl    = "http://www.w3.org/1999/XSL/Transform"
        xmlns:xs="http://www.w3.org/2001/XMLSchema"
        exclude-result-prefixes="earl gco gmd gml gmx i i-gp srv xlink xsi xsl wdrs"
        version="2.0">

  <xsl:output method="xml"
              indent="yes"
              encoding="utf-8"
              cdata-section-elements="locn:geometry dcat:bbox" />

  <!--

    Global variables
    ================

  -->

  <!-- Variables $core and $extended. -->
  <!--

    These variables are meant to be placeholders for the IDs used for the core and extended profiles of GeoDCAT-AP.

  -->
  <!--
    <xsl:variable name="core">core</xsl:variable>
    <xsl:variable name="extended">extended</xsl:variable>
    <xsl:variable name="core">http://data.europa.eu/r5r/</xsl:variable>
    <xsl:variable name="extended">http://data.europa.eu/930/</xsl:variable>
  -->
  <xsl:variable name="profile-core-code">core</xsl:variable>
  <xsl:variable name="profile-extended-code">extended</xsl:variable>
  <xsl:variable name="profile-core-uri">http://data.europa.eu/r5r/</xsl:variable>
  <xsl:variable name="profile-extended-uri">http://data.europa.eu/930/</xsl:variable>

  <!--

    Mapping parameters
    ==================

    This section includes mapping parameters by the XSLT processor used, or, possibly, manually.

  -->

  <!-- Parameter $profile -->
  <!--

    This parameter specifies the GeoDCAT-AP profile to be used:
    - value "core": the GeoDCAT-AP Core profile, which includes only the INSPIRE and ISO 19115 core metadata elements supported in DCAT-AP
    - value "extended": the GeoDCAT-AP Extended profile, which defines mappings for all the INSPIRE and ISO 19115 core metadata elements

    The current specifications for the core and extended GeoDCAT-AP profiles are available on the Joinup collaboration platform:

      https://joinup.ec.europa.eu/solution/geodcat-ap

  -->

  <!-- Uncomment to use GeoDCAT-AP Core as default profile -->
  <!--
    <xsl:variable name="default-profile" select="$profile-core-uri"/>
  -->
  <!-- Uncomment to use GeoDCAT-AP Extended as default profile -->
  <xsl:variable name="default-profile" select="$profile-extended-uri"/>

  <xsl:param name="profile" select="$default-profile"/>

  <xsl:variable name="selected-profile">
    <xsl:choose>
      <xsl:when test="$profile = $profile-core-code">
        <xsl:value-of select="$profile-core-uri"/>
      </xsl:when>
      <xsl:when test="$profile = $profile-core-uri">
        <xsl:value-of select="$profile"/>
      </xsl:when>
      <xsl:when test="$profile = $profile-extended-code">
        <xsl:value-of select="$profile-extended-uri"/>
      </xsl:when>
      <xsl:when test="$profile = $profile-extended-uri">
        <xsl:value-of select="$profile"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$default-profile"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="core">
    <xsl:choose>
      <xsl:when test="$profile = $profile-core-code">
        <xsl:value-of select="$profile-core-code"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$profile-core-uri"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="extended">
    <xsl:choose>
      <xsl:when test="$profile = $profile-extended-code">
        <xsl:value-of select="$profile-extended-code"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$profile-extended-uri"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <!-- Parameter $include-deprecated -->
  <!--
    This parameter specifies whether deprecated mappings must ("yes") or must not
    ("no") be included in the output.
  -->

  <!-- Uncomment to include deprecated mappings from the output -->

  <xsl:param name="include-deprecated">yes</xsl:param>

  <!-- Uncomment to exclude deprecated mappings from the output -->
  <!--
    <xsl:param name="include-deprecated">no</xsl:param>
  -->

  <!-- Parameter $CoupledResourceLookUp -->
  <!--

    This parameter specifies whether the coupled resource, referenced via @xlink:href,
    should be looked up to fetch the resource's  unique resource identifier (i.e., code
    and code space). More precisely:
    - value "enabled": The coupled resource is looked up
    - value "disabled": The coupled resource is not looked up

    The default value is "enabled" for GeoDCAT-AP Extended, and "disabled" otherwise.

    CAVEAT: Using this feature may cause the transformation to hang, in case the URL in
    @xlink:href is broken, the request hangs indefinitely, or does not return the
    expected resource (e.g., and HTML page, instead of an XML-encoded ISO 19139 record).
    It is strongly recommended that this issue is dealt with by using appropriate
    configuration parameters and error handling (e.g., by specifying a timeout on HTTP
    calls and by setting the HTTP Accept header to "application/xml").

  -->

  <xsl:param name="CoupledResourceLookUp">
    <xsl:choose>
      <xsl:when test="$profile = $extended">
        <xsl:text>enabled</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>disabled</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:param>

  <!--

    Other global parameters
    =======================

  -->

  <!-- Variables to be used to convert strings into lower/uppercase by using the translate() function. -->

  <xsl:variable name="lowercase">abcdefghijklmnopqrstuvwxyz</xsl:variable>
  <xsl:variable name="uppercase">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>

  <!-- URIs, URNs and names for spatial reference system registers. -->

  <xsl:param name="EpsgSrsBaseUri">http://www.opengis.net/def/crs/EPSG/0</xsl:param>
  <xsl:param name="EpsgSrsBaseUrn">urn:ogc:def:crs:EPSG</xsl:param>
  <xsl:param name="EpsgSrsName">EPSG Coordinate Reference Systems</xsl:param>
  <xsl:param name="OgcSrsBaseUri">http://www.opengis.net/def/crs/OGC</xsl:param>
  <xsl:param name="OgcSrsBaseUrn">urn:ogc:def:crs:OGC</xsl:param>
  <xsl:param name="OgcSrsName">OGC Coordinate Reference Systems</xsl:param>

  <!-- URI and URN for CRS84. -->

  <xsl:param name="Crs84Uri" select="concat($OgcSrsBaseUri,'/1.3/CRS84')"/>
  <xsl:param name="Crs84Urn" select="concat($OgcSrsBaseUrn,':1.3:CRS84')"/>

  <!-- URI and URN for ETRS89. -->

  <xsl:param name="Etrs89Uri" select="concat($EpsgSrsBaseUri,'/4258')"/>
  <xsl:param name="Etrs89Urn" select="concat($EpsgSrsBaseUrn,'::4258')"/>

  <!-- URI and URN of the spatial reference system (SRS) used in the bounding box.
       The default SRS is CRS84. If a different SRS is used, also parameter
       $SrsAxisOrder must be specified. -->

  <!-- The SRS URI is used in the WKT and GML encodings of the bounding box. -->
  <xsl:param name="SrsUri" select="$Crs84Uri"/>
  <!-- The SRS URN is used in the GeoJSON encoding of the bounding box. -->
  <xsl:param name="SrsUrn" select="$Crs84Urn"/>

  <!-- Axis order for the reference SRS:
       - "LonLat": longitude / latitude
       - "LatLon": latitude / longitude.
       The axis order must be specified only if the reference SRS is different from CRS84.
       If the reference SRS is CRS84, this parameter is ignored. -->

  <xsl:param name="SrsAxisOrder">LonLat</xsl:param>

  <!-- Namespaces -->

  <!-- Currently not used.
    <xsl:param name="timeUri">http://placetime.com/</xsl:param>
    <xsl:param name="timeInstantUri" select="concat($timeUri,'instant/gregorian/')"/>
    <xsl:param name="timeIntervalUri" select="concat($timeUri,'interval/gregorian/')"/>
  -->
  <xsl:param name="dcat">http://www.w3.org/ns/dcat#</xsl:param>
  <xsl:param name="dct">http://purl.org/dc/terms/</xsl:param>
  <xsl:param name="dctype">http://purl.org/dc/dcmitype/</xsl:param>
  <xsl:param name="foaf">http://xmlns.com/foaf/0.1/</xsl:param>
  <xsl:param name="geodcatap">http://data.europa.eu/930/</xsl:param>
  <xsl:param name="gsp">http://www.opengis.net/ont/geosparql#</xsl:param>
  <xsl:param name="prov">http://www.w3.org/ns/prov#</xsl:param>
  <xsl:param name="skos">http://www.w3.org/2004/02/skos/core#</xsl:param>
  <xsl:param name="vcard">http://www.w3.org/2006/vcard/ns#</xsl:param>
  <xsl:param name="xsd">http://www.w3.org/2001/XMLSchema#</xsl:param>

  <!-- Old params used for the SRS
    <xsl:param name="ogcCrsBaseUri">http://www.opengis.net/def/EPSG/0/</xsl:param>
    <xsl:param name="ogcCrsBaseUrn">urn:ogc:def:EPSG::</xsl:param>
  -->

  <!-- Currently not used.
    <xsl:param name="inspire">http://inspire.ec.europa.eu/schemas/md/</xsl:param>
  -->

  <!-- Currently not used.
    <xsl:param name="kos">http://ec.europa.eu/open-data/kos/</xsl:param>
    <xsl:param name="kosil" select="concat($kos,'interoperability-level/')"/>
    <xsl:param name="kosdst" select="concat($kos,'dataset-type/')"/>
    <xsl:param name="kosdss" select="concat($kos,'dataset-status/Completed')"/>
    <xsl:param name="kosdoct" select="concat($kos,'documentation-type/')"/>
    <xsl:param name="koslic" select="concat($kos,'licence/EuropeanCommission')"/>
  -->

  <!-- OP's NALs base URI -->

  <xsl:param name="op">http://publications.europa.eu/resource/authority/</xsl:param>

  <!-- OP's NALs URIs -->

  <xsl:param name="opcb" select="concat($op,'corporate-body/')"/>
  <xsl:param name="opcountry" select="concat($op,'country/')"/>
  <xsl:param name="opfq" select="concat($op,'frequency/')"/>
  <xsl:param name="opft" select="concat($op,'file-type/')"/>
  <xsl:param name="oplang" select="concat($op,'language/')"/>

  <!-- Currently not used.
    <xsl:param name="cldFrequency">http://purl.org/cld/freq/</xsl:param>
  -->

  <!-- IANA base URI -->

  <xsl:param name="iana">https://www.iana.org/assignments/</xsl:param>

  <!-- IANA registers URIs -->

  <xsl:param name="iana-mt" select="concat($iana,'media-types/')"/>

  <!-- DEPRECATED: Parameter kept for backward compatibility with GeoDCAT-AP v1.* -->
  <!-- This is used as the datatype for the GeoJSON-based encoding of the bounding box. -->
  <xsl:param name="geojsonMediaTypeUri" select="concat($iana-mt,'application/vnd.geo+json')"/>

  <!-- QUDT Units Vocabulary -->
  <xsl:param name="qudt-unit">http://www.qudt.org/vocab/unit</xsl:param>

  <!-- Ontology for units of measure (OM) -->
  <xsl:param name="om18">http://www.wurvoc.org/vocabularies/om-1.8</xsl:param>
  <xsl:param name="om2">http://www.ontology-of-units-of-measure.org/resource/om-2</xsl:param>
  <xsl:param name="om" select="$om18"/>

  <!-- Units of measure -->
  <xsl:param name="uom-m" select="concat($qudt-unit, '/', 'M')"/>
  <xsl:param name="uom-km" select="concat($qudt-unit, '/', 'KiloM')"/>
  <xsl:param name="uom-ft" select="concat($qudt-unit, '/', 'FT')"/>
  <xsl:param name="uom-deg" select="concat($qudt-unit, '/', 'DEG')"/>
  <!--
    <xsl:param name="uom-m" select="concat($om, '/', 'metre')"/>
    <xsl:param name="uom-km" select="concat($om, '/', 'kilometre')"/>
    <xsl:param name="uom-ft">
      <xsl:choose>
        <xsl:when test="$om = $om18">
          <xsl:value-of select="concat($om, '/', 'foot-international')"/>
        </xsl:when>
        <xsl:when test="$om = $om2">
          <xsl:value-of select="concat($om, '/', 'foot-International')"/>
        </xsl:when>
      </xsl:choose>
    </xsl:param>
    <xsl:param name="uom-deg" select="concat($om, '/', 'degree')"/>
  -->

  <!-- INSPIRE base URI -->

  <xsl:param name="inspire">http://inspire.ec.europa.eu/</xsl:param>

  <!-- INSPIRE metadata code list URIs -->

  <xsl:param name="INSPIRECodelistUri" select="concat($inspire,'metadata-codelist/')"/>
  <xsl:param name="SpatialDataServiceCategoryCodelistUri" select="concat($INSPIRECodelistUri,'SpatialDataServiceCategory')"/>
  <xsl:param name="DegreeOfConformityCodelistUri" select="concat($INSPIRECodelistUri,'DegreeOfConformity')"/>
  <xsl:param name="ResourceTypeCodelistUri" select="concat($INSPIRECodelistUri,'ResourceType')"/>
  <xsl:param name="ResponsiblePartyRoleCodelistUri" select="concat($INSPIRECodelistUri,'ResponsiblePartyRole')"/>
  <xsl:param name="SpatialDataServiceTypeCodelistUri" select="concat($INSPIRECodelistUri,'SpatialDataServiceType')"/>
  <xsl:param name="TopicCategoryCodelistUri" select="concat($INSPIRECodelistUri,'TopicCategory')"/>

  <!-- INSPIRE code list URIs (not yet supported; the URI pattern is tentative) -->
  <!-- DEPRECATED following the publication of the relevant code list in the INSPIRE Registry
    <xsl:param name="SpatialRepresentationTypeCodelistUri" select="concat($INSPIRECodelistUri,'SpatialRepresentationTypeCode')"/>
    <xsl:param name="MaintenanceFrequencyCodelistUri" select="concat($INSPIRECodelistUri,'MaintenanceFrequencyCode')"/>
  -->
  <xsl:param name="SpatialRepresentationTypeCodelistUri" select="concat($INSPIRECodelistUri,'SpatialRepresentationType')"/>
  <xsl:param name="MaintenanceFrequencyCodelistUri" select="concat($INSPIRECodelistUri,'MaintenanceFrequency')"/>

  <!-- INSPIRE glossary URI -->

  <xsl:param name="INSPIREGlossaryUri" select="concat($inspire,'glossary/')"/>

  <!-- INSPIRE glossary URI -->

  <xsl:param name="inspire-mt" select="concat($inspire,'media-types/')"/>

  <!--

    Master template
    ===============

  -->

  <xsl:template match="/">
    <rdf:RDF>
      <xsl:apply-templates mode="iso19139-to-dcatap"
                           select="gmd:MD_Metadata|//gmd:MD_Metadata"/>
    </rdf:RDF>
  </xsl:template>

  <!--

    Metadata template
    =================

  -->

  <xsl:template mode="iso19139-to-dcatap"
                match="gmd:MD_Metadata|//gmd:MD_Metadata"
                priority="10">

    <!--

      Parameters to create HTTP URIs for the resource and the corresponding metadata record
      =====================================================================================

      These parameters must be customised depending on the strategy used to assign HTTP URIs.

      The default rule implies that HTTP URIs are specified for the metadata file identifier
      (metadata URI) and the resource identifier (resource URI).

      Resource URI can be an http or https URI based on:
      * codeSpace + code value
      * code value
      The first URI found is used.
    -->

    <xsl:param name="ResourceUri">
      <xsl:variable name="identifiers"
                    select="gmd:identificationInfo/*/gmd:citation/*/
                              gmd:identifier/*"/>

      <xsl:variable name="uriIdentifiers"
                    as="node()*">
        <xsl:for-each select="$identifiers">
          <xsl:variable name="rURI"
                        select="if (gmd:codeSpace)
                                then concat(
                                      gmd:codeSpace/(gco:CharacterString
                                                     |gmx:Anchor/@xlink:href),
                                      gmd:code/(gco:CharacterString
                                                |gmx:Anchor/@xlink:href))
                                else gmd:code/(gco:CharacterString/text()
                                               |gmx:Anchor/@xlink:href)"/>
          <xsl:if test="matches($rURI, '^https?://')">
            <uri><xsl:value-of select="$rURI"/></uri>
          </xsl:if>
        </xsl:for-each>
      </xsl:variable>

      <xsl:if test="count($uriIdentifiers) > 0">
        <xsl:value-of select="$uriIdentifiers[1]"/>
        <xsl:if test="count($uriIdentifiers) > 1">
          <xsl:message>ResourceUri is <xsl:value-of select="$uriIdentifiers[1]"/>. Ignored: <xsl:value-of select="string-join($uriIdentifiers[position() > 1], ',')"/>. </xsl:message>
        </xsl:if>
      </xsl:if>
    </xsl:param>

    <xsl:param name="MetadataUri">
      <xsl:variable name="mURI" select="gmd:fileIdentifier/gco:CharacterString"/>
      <xsl:if test="$mURI != '' and ( starts-with($mURI, 'http://') or starts-with($mURI, 'https://') )">
        <xsl:value-of select="$mURI"/>
      </xsl:if>
    </xsl:param>

    <!--

      Other parameters
      ================

    -->

    <!-- Metadata language: corresponding Alpha-2 codes -->

    <xsl:param name="ormlang">
      <xsl:choose>
        <xsl:when test="gmd:language/gmd:LanguageCode/@codeListValue != ''">
          <xsl:value-of select="translate(gmd:language/gmd:LanguageCode/@codeListValue,$uppercase,$lowercase)"/>
        </xsl:when>
        <xsl:when test="gmd:language/gmd:LanguageCode != ''">
          <xsl:value-of select="translate(gmd:language/gmd:LanguageCode,$uppercase,$lowercase)"/>
        </xsl:when>
        <xsl:when test="gmd:language/gco:CharacterString != ''">
          <xsl:value-of select="translate(gmd:language/gco:CharacterString,$uppercase,$lowercase)"/>
        </xsl:when>
      </xsl:choose>
    </xsl:param>

    <xsl:param name="MetadataLanguage">
      <xsl:choose>
        <xsl:when test="$ormlang = 'bul'">
          <xsl:text>bg</xsl:text>
        </xsl:when>
        <xsl:when test="$ormlang = 'cze'">
          <xsl:text>cs</xsl:text>
        </xsl:when>
        <xsl:when test="$ormlang = 'dan'">
          <xsl:text>da</xsl:text>
        </xsl:when>
        <xsl:when test="$ormlang = 'ger'">
          <xsl:text>de</xsl:text>
        </xsl:when>
        <xsl:when test="$ormlang = 'gre'">
          <xsl:text>el</xsl:text>
        </xsl:when>
        <xsl:when test="$ormlang = 'eng'">
          <xsl:text>en</xsl:text>
        </xsl:when>
        <xsl:when test="$ormlang = 'spa'">
          <xsl:text>es</xsl:text>
        </xsl:when>
        <xsl:when test="$ormlang = 'est'">
          <xsl:text>et</xsl:text>
        </xsl:when>
        <xsl:when test="$ormlang = 'fin'">
          <xsl:text>fi</xsl:text>
        </xsl:when>
        <xsl:when test="$ormlang = 'fre'">
          <xsl:text>fr</xsl:text>
        </xsl:when>
        <xsl:when test="$ormlang = 'gle'">
          <xsl:text>ga</xsl:text>
        </xsl:when>
        <xsl:when test="$ormlang = 'hrv'">
          <xsl:text>hr</xsl:text>
        </xsl:when>
        <xsl:when test="$ormlang = 'ita'">
          <xsl:text>it</xsl:text>
        </xsl:when>
        <xsl:when test="$ormlang = 'lav'">
          <xsl:text>lv</xsl:text>
        </xsl:when>
        <xsl:when test="$ormlang = 'lit'">
          <xsl:text>lt</xsl:text>
        </xsl:when>
        <xsl:when test="$ormlang = 'hun'">
          <xsl:text>hu</xsl:text>
        </xsl:when>
        <xsl:when test="$ormlang = 'mlt'">
          <xsl:text>mt</xsl:text>
        </xsl:when>
        <xsl:when test="$ormlang = 'dut'">
          <xsl:text>nl</xsl:text>
        </xsl:when>
        <xsl:when test="$ormlang = 'pol'">
          <xsl:text>pl</xsl:text>
        </xsl:when>
        <xsl:when test="$ormlang = 'por'">
          <xsl:text>pt</xsl:text>
        </xsl:when>
        <xsl:when test="$ormlang = 'rum'">
          <xsl:text>ru</xsl:text>
        </xsl:when>
        <xsl:when test="$ormlang = 'slo'">
          <xsl:text>sk</xsl:text>
        </xsl:when>
        <xsl:when test="$ormlang = 'slv'">
          <xsl:text>sl</xsl:text>
        </xsl:when>
        <xsl:when test="$ormlang = 'swe'">
          <xsl:text>sv</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$ormlang"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:param>

    <!-- Resource language: corresponding Alpha-2 codes -->

    <xsl:param name="orrlang">
      <xsl:choose>
        <xsl:when test="gmd:identificationInfo/*/gmd:language/gmd:LanguageCode/@codeListValue != ''">
          <xsl:value-of select="translate(gmd:identificationInfo/*/gmd:language/gmd:LanguageCode/@codeListValue,$uppercase,$lowercase)"/>
        </xsl:when>
        <xsl:when test="gmd:identificationInfo/*/gmd:language/gmd:LanguageCode != ''">
          <xsl:value-of select="translate(gmd:identificationInfo/*/gmd:language/gmd:LanguageCode,$uppercase,$lowercase)"/>
        </xsl:when>
        <xsl:when test="gmd:identificationInfo/*/gmd:language/gco:CharacterString != ''">
          <xsl:value-of select="translate(gmd:identificationInfo/*/gmd:language/gco:CharacterString,$uppercase,$lowercase)"/>
        </xsl:when>
      </xsl:choose>
    </xsl:param>

    <xsl:param name="ResourceLanguage">
      <xsl:choose>
        <xsl:when test="$orrlang = 'bul'">
          <xsl:text>bg</xsl:text>
        </xsl:when>
        <xsl:when test="$orrlang = 'cze'">
          <xsl:text>cs</xsl:text>
        </xsl:when>
        <xsl:when test="$orrlang = 'dan'">
          <xsl:text>da</xsl:text>
        </xsl:when>
        <xsl:when test="$orrlang = 'ger'">
          <xsl:text>de</xsl:text>
        </xsl:when>
        <xsl:when test="$orrlang = 'gre'">
          <xsl:text>el</xsl:text>
        </xsl:when>
        <xsl:when test="$orrlang = 'eng'">
          <xsl:text>en</xsl:text>
        </xsl:when>
        <xsl:when test="$orrlang = 'spa'">
          <xsl:text>es</xsl:text>
        </xsl:when>
        <xsl:when test="$orrlang = 'est'">
          <xsl:text>et</xsl:text>
        </xsl:when>
        <xsl:when test="$orrlang = 'fin'">
          <xsl:text>fi</xsl:text>
        </xsl:when>
        <xsl:when test="$orrlang = 'fre'">
          <xsl:text>fr</xsl:text>
        </xsl:when>
        <xsl:when test="$orrlang = 'gle'">
          <xsl:text>ga</xsl:text>
        </xsl:when>
        <xsl:when test="$orrlang = 'hrv'">
          <xsl:text>hr</xsl:text>
        </xsl:when>
        <xsl:when test="$orrlang = 'ita'">
          <xsl:text>it</xsl:text>
        </xsl:when>
        <xsl:when test="$orrlang = 'lav'">
          <xsl:text>lv</xsl:text>
        </xsl:when>
        <xsl:when test="$orrlang = 'lit'">
          <xsl:text>lt</xsl:text>
        </xsl:when>
        <xsl:when test="$orrlang = 'hun'">
          <xsl:text>hu</xsl:text>
        </xsl:when>
        <xsl:when test="$orrlang = 'mlt'">
          <xsl:text>mt</xsl:text>
        </xsl:when>
        <xsl:when test="$orrlang = 'dut'">
          <xsl:text>nl</xsl:text>
        </xsl:when>
        <xsl:when test="$orrlang = 'pol'">
          <xsl:text>pl</xsl:text>
        </xsl:when>
        <xsl:when test="$orrlang = 'por'">
          <xsl:text>pt</xsl:text>
        </xsl:when>
        <xsl:when test="$orrlang = 'rum'">
          <xsl:text>ru</xsl:text>
        </xsl:when>
        <xsl:when test="$orrlang = 'slo'">
          <xsl:text>sk</xsl:text>
        </xsl:when>
        <xsl:when test="$orrlang = 'slv'">
          <xsl:text>sl</xsl:text>
        </xsl:when>
        <xsl:when test="$orrlang = 'swe'">
          <xsl:text>sv</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$orrlang"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:param>

    <xsl:param name="IsoScopeCode">
      <xsl:value-of select="normalize-space(gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue)"/>
    </xsl:param>

    <xsl:param name="InspireResourceType">
      <xsl:if test="$IsoScopeCode = 'dataset' or $IsoScopeCode = 'series' or $IsoScopeCode = 'service'">
        <xsl:value-of select="$IsoScopeCode"/>
      </xsl:if>
    </xsl:param>

    <xsl:param name="ResourceType">
      <xsl:choose>
        <xsl:when test="$IsoScopeCode = 'dataset' or $IsoScopeCode = 'nonGeographicDataset'">
          <xsl:text>dataset</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$IsoScopeCode"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:param>

    <xsl:param name="ServiceType">
      <xsl:value-of select="gmd:identificationInfo/*/srv:serviceType/gco:LocalName"/>
    </xsl:param>
    <!--
        <xsl:param name="ResourceTitle">
          <xsl:value-of select="gmd:identificationInfo[1]/*/gmd:citation/*/gmd:title/gco:CharacterString"/>
        </xsl:param>
    -->
    <xsl:param name="ResourceTitle">
      <xsl:for-each select="gmd:identificationInfo[1]/*/gmd:citation/*/gmd:title">
        <dct:title xml:lang="{$MetadataLanguage}">
          <xsl:value-of select="normalize-space(gco:CharacterString)"/>
        </dct:title>
        <xsl:call-template name="LocalisedString">
          <xsl:with-param name="term">dct:title</xsl:with-param>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:param>
    <!--
        <xsl:param name="ResourceAbstract">
          <xsl:value-of select="gmd:identificationInfo[1]/*/gmd:abstract/gco:CharacterString"/>
        </xsl:param>
    -->
    <xsl:param name="ResourceAbstract">
      <xsl:for-each select="gmd:identificationInfo[1]/*/gmd:abstract">
        <dct:description xml:lang="{$MetadataLanguage}">
          <xsl:value-of select="normalize-space(gco:CharacterString)"/>
        </dct:description>
        <xsl:call-template name="LocalisedString">
          <xsl:with-param name="term">dct:description</xsl:with-param>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:param>
    <!--
        <xsl:param name="Lineage">
          <xsl:value-of select="gmd:dataQualityInfo/*/gmd:lineage/*/gmd:statement/gco:CharacterString"/>
        </xsl:param>
    -->
    <xsl:param name="Lineage">
      <xsl:for-each select="gmd:dataQualityInfo/*/gmd:lineage/*/gmd:statement">
        <dct:provenance>
          <dct:ProvenanceStatement>
            <dct:description xml:lang="{$MetadataLanguage}"><xsl:value-of select="normalize-space(gco:CharacterString)"/></dct:description>
            <xsl:call-template name="LocalisedString">
              <xsl:with-param name="term">dct:description</xsl:with-param>
            </xsl:call-template>
          </dct:ProvenanceStatement>
        </dct:provenance>
      </xsl:for-each>
    </xsl:param>

    <xsl:param name="MetadataDate">
      <xsl:choose>
        <xsl:when test="gmd:dateStamp/gco:Date">
          <xsl:value-of select="gmd:dateStamp/gco:Date"/>
        </xsl:when>
        <xsl:when test="gmd:dateStamp/gco:DateTime">
          <!--
                    <xsl:value-of select="substring(gmd:dateStamp/gco:DateTime/text(),1,10)"/>
          -->
          <xsl:value-of select="normalize-space(gmd:dateStamp/gco:DateTime/text())"/>
        </xsl:when>
      </xsl:choose>
    </xsl:param>

    <xsl:param name="UniqueResourceIdentifier">
      <xsl:for-each select="gmd:identificationInfo[1]/*/gmd:citation/*/gmd:identifier/*">
        <xsl:choose>
          <xsl:when test="gmd:codeSpace/gco:CharacterString/text() != ''">
            <xsl:value-of select="concat(gmd:codeSpace/gco:CharacterString/text(),gmd:code/gco:CharacterString/text())"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="gmd:code/gco:CharacterString/text()"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </xsl:param>

    <xsl:param name="ConstraintsRelatedToAccessAndUse">
      <xsl:apply-templates mode="iso19139-to-dcatap"
                           select="gmd:identificationInfo[1]/*/gmd:resourceConstraints/*">
        <xsl:with-param name="MetadataLanguage" select="$MetadataLanguage"/>
      </xsl:apply-templates>
    </xsl:param>

    <!-- Conformity, expressed by using an earl:Assertion (only for the extended profile) -->
    <!--
        <xsl:param name="Conformity">
          <xsl:for-each select="gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation">
            <xsl:variable name="specinfo">
              <dct:title xml:lang="{$MetadataLanguage}">
                <xsl:value-of select="gmd:title/gco:CharacterString"/>
              </dct:title>
              <xsl:apply-templates select="gmd:date/gmd:CI_Date"/>
            </xsl:variable>
            <xsl:variable name="degree">
              <xsl:choose>
                <xsl:when test="../../gmd:pass/gco:Boolean = 'true'">
                  <xsl:value-of select="concat($DegreeOfConformityCodelistUri,'/conformant')"/>
                </xsl:when>
                <xsl:when test="../../gmd:pass/gco:Boolean = 'false'">
                  <xsl:value-of select="concat($DegreeOfConformityCodelistUri,'/notConformant')"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="concat($DegreeOfConformityCodelistUri,'/notEvaluated')"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <xsl:variable name="explanation">
              <xsl:value-of select="../../gmd:explanation/gco:CharacterString"/>
            </xsl:variable>
            <earl:Assertion>
              <xsl:if test="$ResourceUri != ''">
                <earl:subject rdf:resource="{$ResourceUri}"/>
              </xsl:if>
              <xsl:choose>
                <xsl:when test="../@xlink:href and ../@xlink:href != ''">
                  <earl:test>
                    <rdf:Description rdf:about="{../@xlink:href}">
                      <xsl:copy-of select="$specinfo"/>
                    </rdf:Description>
                  </earl:test>
                </xsl:when>
                <xsl:otherwise>
                  <earl:test rdf:parseType="Resource">
                    <xsl:copy-of select="$specinfo"/>
                  </earl:test>
                </xsl:otherwise>
              </xsl:choose>
              <earl:result>
                <earl:TestResult>
                  <earl:outcome rdf:resource="{$degree}"/>
                  <xsl:if test="$explanation and $explanation != ''">
                    <earl:info xml:lang="{$MetadataLanguage}"><xsl:value-of select="$explanation"/></earl:info>
                  </xsl:if>
                </earl:TestResult>
              </earl:result>
            </earl:Assertion>
          </xsl:for-each>
        </xsl:param>
    -->
    <!-- Conformity, expressed by using a prov:Activity (only for the extended profile) -->

    <xsl:param name="Conformity">
      <xsl:for-each select="gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation">
        <xsl:variable name="specUri" select="normalize-space(gmd:title/gmx:Anchor/@xlink:href)"/>
        <xsl:variable name="specTitle">
          <xsl:for-each select="gmd:title">
            <!--
                        <dct:title xml:lang="{$MetadataLanguage}"><xsl:value-of select="normalize-space(gco:CharacterString)"/></dct:title>
            -->
            <dct:title xml:lang="{$MetadataLanguage}">
              <xsl:value-of select="normalize-space(*[self::gco:CharacterString|self::gmx:Anchor])"/>
            </dct:title>
            <xsl:call-template name="LocalisedString">
              <xsl:with-param name="term">dct:title</xsl:with-param>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="specinfo">
          <!--
                    <dct:title xml:lang="{$MetadataLanguage}">
                      <xsl:value-of select="gmd:title/gco:CharacterString"/>
                    </dct:title>
          -->
          <xsl:copy-of select="$specTitle"/>
          <xsl:apply-templates mode="iso19139-to-dcatap"
                               select="gmd:date/gmd:CI_Date"/>
        </xsl:variable>
        <xsl:variable name="degree">
          <xsl:choose>
            <xsl:when test="../../gmd:pass/gco:Boolean = 'true'">
              <xsl:value-of select="concat($DegreeOfConformityCodelistUri,'/conformant')"/>
            </xsl:when>
            <xsl:when test="../../gmd:pass/gco:Boolean = 'false'">
              <xsl:value-of select="concat($DegreeOfConformityCodelistUri,'/notConformant')"/>
            </xsl:when>
            <xsl:otherwise>
              <!--
                          <xsl:when test="../../gmd:pass/gco:Boolean = ''">
              -->
              <xsl:value-of select="concat($DegreeOfConformityCodelistUri,'/notEvaluated')"/>
              <!--
                          </xsl:when>
              -->
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <!--
                <xsl:variable name="explanation">
                  <xsl:value-of select="../../gmd:explanation/gco:CharacterString"/>
                </xsl:variable>
        -->
        <xsl:variable name="explanation">
          <xsl:for-each select="../../gmd:explanation">
            <dct:description xml:lang="{$MetadataLanguage}">
              <xsl:value-of select="normalize-space(gco:CharacterString)"/>
            </dct:description>
            <xsl:call-template name="LocalisedString">
              <xsl:with-param name="term">dct:description</xsl:with-param>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="Activity">
          <prov:Activity>
            <xsl:if test="$ResourceUri != ''">
              <prov:used>
                <prov:Entity rdf:about="{$ResourceUri}"/>
              </prov:used>
            </xsl:if>
            <prov:qualifiedAssociation rdf:parseType="Resource">
              <rdf:type rdf:resource="{$prov}Association"/>
              <prov:hadPlan rdf:parseType="Resource">
                <rdf:type rdf:resource="{$prov}Plan"/>
                <xsl:choose>
                  <xsl:when test="$specUri != ''">
                    <prov:wasDerivedFrom>
                      <prov:Entity rdf:about="{$specUri}"/>
                    </prov:wasDerivedFrom>
                    <!--
                                        <prov:wasDerivedFrom>
                                          <rdf:Description rdf:about="{$specUri}">
                                            <xsl:copy-of select="$specinfo"/>
                                          </rdf:Description>
                                        </prov:wasDerivedFrom>
                    -->
                  </xsl:when>
                  <xsl:when test="../@xlink:href and ../@xlink:href != ''">
                    <prov:wasDerivedFrom>
                      <prov:Entity rdf:about="{../@xlink:href}"/>
                    </prov:wasDerivedFrom>
                    <!--
                                        <prov:wasDerivedFrom>
                                          <rdf:Description rdf:about="{../@xlink:href}">
                                            <xsl:copy-of select="$specinfo"/>
                                          </rdf:Description>
                                        </prov:wasDerivedFrom>
                    -->
                  </xsl:when>
                  <xsl:otherwise>
                    <prov:wasDerivedFrom rdf:parseType="Resource">
                      <rdf:type rdf:resource="{$prov}Entity"/>
                      <xsl:copy-of select="$specinfo"/>
                    </prov:wasDerivedFrom>
                  </xsl:otherwise>
                </xsl:choose>
              </prov:hadPlan>
            </prov:qualifiedAssociation>
            <prov:generated rdf:parseType="Resource">
              <rdf:type rdf:resource="{$prov}Entity"/>
              <dct:type rdf:resource="{$degree}"/>
              <!--
                            <xsl:if test="$explanation and $explanation != ''">
                              <dct:description xml:lang="{$MetadataLanguage}"><xsl:value-of select="$explanation"/></dct:description>
                            </xsl:if>
              -->
              <xsl:copy-of select="$explanation"/>
            </prov:generated>
          </prov:Activity>
        </xsl:variable>
        <!--
                <xsl:choose>
                  <xsl:when test="$ResourceUri != ''">
                    <xsl:copy-of select="$Activity"/>
                  </xsl:when>
                  <xsl:otherwise>
        -->
        <prov:wasUsedBy>
          <xsl:copy-of select="$Activity"/>
        </prov:wasUsedBy>
        <!--
                  </xsl:otherwise>
                </xsl:choose>
        -->
      </xsl:for-each>
    </xsl:param>

    <!-- Metadata character encoding (only for the extended profile) -->

    <xsl:param name="MetadataCharacterEncoding">
      <xsl:apply-templates mode="iso19139-to-dcatap"
                           select="gmd:characterSet/gmd:MD_CharacterSetCode"/>
    </xsl:param>

    <xsl:param name="ResourceCharacterEncoding">
      <xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification">
        <xsl:apply-templates mode="iso19139-to-dcatap"
                             select="gmd:characterSet/gmd:MD_CharacterSetCode"/>
      </xsl:for-each>
    </xsl:param>

    <!-- Metadata description (metadata on metadata) -->

    <xsl:param name="MetadataDescription">
      <rdf:type rdf:resource="{$dcat}CatalogRecord"/>
      <xsl:if test="$ResourceUri != ''">
        <foaf:primaryTopic rdf:resource="{$ResourceUri}"/>
      </xsl:if>
      <!-- Metadata standard -->
      <dct:conformsTo>
        <dct:Standard rdf:about="{$selected-profile}"/>
      </dct:conformsTo>
      <!-- Metadata language -->
      <xsl:if test="$ormlang != ''">
        <dct:language>
          <dct:LinguisticSystem rdf:about="{concat($oplang,translate($ormlang,$lowercase,$uppercase))}"/>
        </dct:language>
      </xsl:if>
      <!-- Metadata date -->
      <xsl:if test="$MetadataDate != ''">
        <xsl:variable name="data-type">
          <xsl:call-template name="DateDataType">
            <xsl:with-param name="date" select="$MetadataDate"/>
          </xsl:call-template>
        </xsl:variable>
        <dct:modified rdf:datatype="{$xsd}{$data-type}">
          <xsl:value-of select="$MetadataDate"/>
        </dct:modified>
      </xsl:if>
      <!-- Metadata point of contact: only for the extended profile -->
      <xsl:if test="$profile = $extended">
        <xsl:for-each select="gmd:contact">
          <xsl:apply-templates mode="iso19139-to-dcatap"
                               select="gmd:CI_ResponsibleParty">
            <xsl:with-param name="MetadataLanguage" select="$MetadataLanguage"/>
            <xsl:with-param name="ResourceType" select="$ResourceType"/>
          </xsl:apply-templates>
        </xsl:for-each>
        <!-- Old version
                <xsl:apply-templates select="gmd:contact/gmd:CI_ResponsibleParty">
                  <xsl:with-param name="MetadataLanguage" select="$MetadataLanguage"/>
                </xsl:apply-templates>
        -->
      </xsl:if>
      <!-- Metadata file identifier (tentative): only for the extended profile -->
      <xsl:if test="$profile = $extended">
        <xsl:for-each select="gmd:fileIdentifier/gco:CharacterString">
          <dct:identifier rdf:datatype="{$xsd}string"><xsl:value-of select="."/></dct:identifier>
        </xsl:for-each>
      </xsl:if>
      <!-- Metadata standard (tentative): only for the extended profile -->
      <!-- Mapping moved to core profile for compliance with DCAT-AP 2 -->
      <!--
            <xsl:if test="$profile = $extended">
      -->
      <xsl:variable name="MetadataStandardURI" select="gmd:metadataStandardName/gmx:Anchor/@xlink:href"/>
      <!--
            <xsl:variable name="MetadataStandardName" select="gmd:metadataStandardName/*[self::gco:CharacterString|self::gmx:Anchor]"/>
      -->
      <xsl:variable name="MetadataStandardName">
        <xsl:for-each select="gmd:metadataStandardName">
          <dct:title xml:lang="{$MetadataLanguage}">
            <xsl:value-of select="normalize-space(*[self::gco:CharacterString|self::gmx:Anchor])"/>
          </dct:title>
          <xsl:call-template name="LocalisedString">
            <xsl:with-param name="term">dct:title</xsl:with-param>
          </xsl:call-template>
        </xsl:for-each>
      </xsl:variable>
      <xsl:variable name="MetadataStandardVersion" select="gmd:metadataStandardVersion/gco:CharacterString"/>
      <xsl:if test="$MetadataCharacterEncoding != '' or $MetadataStandardURI != '' or $MetadataStandardName != ''">
        <dct:source rdf:parseType="Resource">
          <rdf:type rdf:resource="{$dcat}CatalogRecord"/>
          <!-- Metadata date -->
          <xsl:if test="$MetadataDate != ''">
            <dct:modified rdf:datatype="{$xsd}date">
              <xsl:value-of select="$MetadataDate"/>
            </dct:modified>
          </xsl:if>
          <xsl:if test="$MetadataCharacterEncoding != ''">
            <!-- Metadata character encoding (tentative): only for the extended profile -->
            <xsl:copy-of select="$MetadataCharacterEncoding"/>
          </xsl:if>
          <xsl:choose>
            <xsl:when test="$MetadataStandardURI != ''">
              <!-- Metadata standard, denoted by a URI -->
              <dct:conformsTo>
                <dct:Standard rdf:about="{$MetadataStandardURI}"/>
              </dct:conformsTo>
            </xsl:when>
            <xsl:when test="$MetadataStandardName != ''">
              <dct:conformsTo rdf:parseType="Resource">
                <rdf:type rdf:resource="{$dct}Standard"/>
                <!-- Metadata standard name -->
                <!--
                                  <dct:title xml:lang="{$MetadataLanguage}"><xsl:value-of select="$MetadataStandardName"/></dct:title>
                -->
                <xsl:copy-of select="$MetadataStandardName"/>
                <xsl:if test="$MetadataStandardVersion != ''">
                  <!-- Metadata standard version -->
                  <owl:versionInfo xml:lang="{$MetadataLanguage}"><xsl:value-of select="$MetadataStandardVersion"/></owl:versionInfo>
                </xsl:if>
              </dct:conformsTo>
            </xsl:when>
          </xsl:choose>
        </dct:source>
      </xsl:if>
      <!-- Old version:
              <xsl:for-each select="gmd:metadataStandardName/gco:CharacterString">
                <xsl:if test="text() != '' or ../../gmd:metadataStandardVersion/gco:CharacterString/text() != ''">
                  <dct:source rdf:parseType="Resource">
                    <xsl:if test="$MetadataCharacterEncoding != ''">
                      <xsl:copy-of select="$MetadataCharacterEncoding"/>
                    </xsl:if>
                    <dct:conformsTo rdf:parseType="Resource">
                      <xsl:if test="text() != ''">
                        <dct:title xml:lang="{$MetadataLanguage}"><xsl:value-of select="."/></dct:title>
                      </xsl:if>
                      <xsl:if test="../../gmd:metadataStandardName/gco:CharacterString/text() != ''">
                        <owl:versionInfo xml:lang="{$MetadataLanguage}"><xsl:value-of select="../../gmd:metadataStandardVersion/gco:CharacterString"/></owl:versionInfo>
                      </xsl:if>
                    </dct:conformsTo>
                  </dct:source>
                </xsl:if>
              </xsl:for-each>
      -->
      <!--
            </xsl:if>
      -->
    </xsl:param>

    <!-- Resource description (resource metadata) -->

    <xsl:param name="ResourceDescription">
      <xsl:choose>
        <xsl:when test="$ResourceType = 'dataset'">
          <rdf:type rdf:resource="{$dcat}Dataset"/>
        </xsl:when>
        <xsl:when test="$ResourceType = 'series'">
          <!-- DEPRECATED: Mapping kept for backward compatibility with GeoDCAT-AP v2.* and earlier versions -->
          <xsl:if test="$include-deprecated = 'yes'">
            <rdf:type rdf:resource="{$dcat}Dataset"/>
          </xsl:if>
          <!-- Mapping added for compliance with DCAT-AP 3 -->
          <rdf:type rdf:resource="{$dcat}DatasetSeries"/>
        </xsl:when>
        <xsl:when test="$ResourceType = 'service'">
          <!-- Mapping moved to core profile for compliance with DCAT-AP 2 -->
          <!--
                    <xsl:if test="$profile = $extended">
          -->
          <!-- DEPRECATED: Mapping kept for backward compatibility with GeoDCAT-AP v1.* -->
          <xsl:if test="$include-deprecated = 'yes'">
            <rdf:type rdf:resource="{$dctype}Service"/>
          </xsl:if>
          <!-- Mapping added for compliance with DCAT-AP 2 -->
          <rdf:type rdf:resource="{$dcat}DataService"/>
          <!--
                    </xsl:if>
          -->
          <xsl:if test="$ServiceType = 'discovery'">
            <!-- DEPRECATED: Mapping kept for backward compatibility with GeoDCAT-AP v1.* -->
            <xsl:if test="$include-deprecated = 'yes'">
              <rdf:type rdf:resource="{$dcat}Catalog"/>
            </xsl:if>
            <!-- Mapping added for compliance with DCAT-AP 2 -->
            <!--
                        <rdf:type rdf:resource="{$dcat}DataService"/>
            -->
          </xsl:if>
        </xsl:when>
      </xsl:choose>
      <!-- Mapping moved to core profile for compliance with DCAT-AP 2 -->
      <!--
            <xsl:if test="$profile = $extended">
      -->
      <xsl:if test="$InspireResourceType != ''">
        <geodcatap:resourceType rdf:resource="{$ResourceTypeCodelistUri}/{$ResourceType}"/>
      </xsl:if>
      <!--
            </xsl:if>
      -->
      <!--
            <dct:title xml:lang="{$MetadataLanguage}"><xsl:value-of select="$ResourceTitle"/></dct:title>
      -->
      <xsl:copy-of select="$ResourceTitle"/>
      <!--
            <dct:description xml:lang="{$MetadataLanguage}">
              <xsl:value-of select="normalize-space($ResourceAbstract)"/>
            </dct:description>
      -->
      <xsl:copy-of select="$ResourceAbstract"/>
      <!-- Maintenance information (tentative) -->
      <xsl:for-each select="gmd:identificationInfo/*/gmd:resourceMaintenance">
        <xsl:apply-templates mode="iso19139-to-dcatap"
                             select="gmd:MD_MaintenanceInformation/gmd:maintenanceAndUpdateFrequency/gmd:MD_MaintenanceFrequencyCode"/>
      </xsl:for-each>
      <!-- Topic category -->
      <xsl:if test="$profile = $extended">
        <xsl:apply-templates mode="iso19139-to-dcatap"
                             select="gmd:identificationInfo/*/gmd:topicCategory">
          <xsl:with-param name="MetadataLanguage" select="$MetadataLanguage"/>
        </xsl:apply-templates>
      </xsl:if>
      <!-- Keyword -->
      <xsl:apply-templates mode="iso19139-to-dcatap"
                           select="gmd:identificationInfo/*/gmd:descriptiveKeywords/gmd:MD_Keywords">
        <xsl:with-param name="MetadataLanguage" select="$MetadataLanguage"/>
        <xsl:with-param name="ResourceType" select="$ResourceType"/>
        <xsl:with-param name="ServiceType" select="$ServiceType"/>
      </xsl:apply-templates>
      <!-- Identifier, 0..1 -->
      <!--
            <xsl:apply-templates select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/*">
              <xsl:with-param name="MetadataLanguage" select="$MetadataLanguage"/>
            </xsl:apply-templates>
      -->
      <!-- Resource locators -->
      <!--
            <xsl:apply-templates select="gmd:distributionInfo/*/gmd:transferOptions/*/gmd:onLine/*/gmd:linkage">
              <xsl:with-param name="ResourceType" select="$ResourceType"/>
              <xsl:with-param name="MetadataLanguage" select="$MetadataLanguage"/>
            </xsl:apply-templates>
      -->
      <!-- Unique Resource Identifier -->
      <xsl:apply-templates mode="iso19139-to-dcatap"
                           select="gmd:identificationInfo/*/gmd:citation/*/gmd:identifier/*"/>
      <!-- Coupled resources -->
      <xsl:apply-templates mode="iso19139-to-dcatap"
                           select="gmd:identificationInfo[1]/*/srv:operatesOn">
        <xsl:with-param name="ResourceType" select="$ResourceType"/>
        <xsl:with-param name="MetadataLanguage" select="$MetadataLanguage"/>
      </xsl:apply-templates>
      <!-- Resource Language -->
      <xsl:if test="$ResourceType = 'dataset' or $ResourceType = 'series'">
        <xsl:choose>
          <xsl:when test="$orrlang != ''">
            <dct:language>
              <dct:LinguisticSystem rdf:about="{concat($oplang,translate($orrlang,$lowercase,$uppercase))}"/>
            </dct:language>
          </xsl:when>
          <xsl:otherwise>
            <!-- To be decided (when the resource language is not specified, it defaults to the metadata language): -->
            <!--
                         <xsl:if test="$ormlang != ''">
                           <dct:language rdf:resource="{concat($oplang,translate($ormlang,$lowercase,$uppercase))}"/>
                         </xsl:if>
            -->
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
      <!-- Spatial service type -->
      <!-- Mapping added to the core profile for compliance with DCAT-AP 2 -->
      <!--
            <xsl:if test="$ResourceType = 'service' and $profile = $extended">
      -->
      <xsl:if test="$ResourceType = 'service'">
        <!-- Replaced by param $ServiceType -->
        <!--
                <xsl:apply-templates select="gmd:identificationInfo/*/srv:serviceType">
                  <xsl:with-param name="MetadataLanguage" select="$MetadataLanguage"/>
                </xsl:apply-templates>
        -->
        <geodcatap:serviceType rdf:resource="{$SpatialDataServiceTypeCodelistUri}/{$ServiceType}"/>
      </xsl:if>
      <!-- Spatial extent -->
      <!--
            <xsl:apply-templates select="gmd:identificationInfo[1]/*/*[self::gmd:extent|self::srv:extent]/*/gmd:geographicElement/gmd:EX_GeographicBoundingBox"/>
      -->
      <xsl:apply-templates mode="iso19139-to-dcatap"
                           select="gmd:identificationInfo[1]/*/*[self::gmd:extent|self::srv:extent]/*/gmd:geographicElement">
        <xsl:with-param name="MetadataLanguage" select="$MetadataLanguage"/>
      </xsl:apply-templates>
      <!-- Temporal extent -->
      <xsl:apply-templates mode="iso19139-to-dcatap"
                           select="gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent"/>
      <!-- Creation date, publication date, date of last revision -->
      <xsl:apply-templates mode="iso19139-to-dcatap"
                           select="gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation"/>
      <!-- Lineage -->
      <xsl:if test="$ResourceType != 'service' and $Lineage != ''">
        <!--
                <dct:provenance>
                  <dct:ProvenanceStatement>
                    <rdfs:label xml:lang="{$MetadataLanguage}">
                      <xsl:value-of select="normalize-space($Lineage)"/>
                    </rdfs:label>
                  </dct:ProvenanceStatement>
                </dct:provenance>
        -->
        <xsl:copy-of select="$Lineage"/>
      </xsl:if>
      <!-- Coordinate and temporal reference systems (tentative) -->
      <!-- Mapping added to the core profile for compliance with DCAT-AP 2 -->
      <!--
            <xsl:if test="$profile = $extended">
      -->
      <xsl:apply-templates mode="iso19139-to-dcatap"
                           select="gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier">
        <xsl:with-param name="MetadataLanguage" select="$MetadataLanguage"/>
      </xsl:apply-templates>
      <!--
            </xsl:if>
      -->
      <!-- Spatial resolution -->
      <!-- Mapping moved to core profile for compliance with DCAT-AP 2 -->
      <!--
            <xsl:if test="$profile = $extended">
      -->
      <xsl:apply-templates mode="iso19139-to-dcatap"
                           select="gmd:identificationInfo/*/gmd:spatialResolution/gmd:MD_Resolution"/>
      <!--
            </xsl:if>
      -->
      <!-- Constraints related to access and use for services -->
      <!-- Mapping moved to the core profile for compliance with DCAT-AP 2 -->
      <!--
            <xsl:if test="$ResourceType = 'service' and ($ServiceType = 'discovery' or $profile = $extended)">
      -->
      <xsl:if test="$ResourceType = 'service'">
        <xsl:copy-of select="$ConstraintsRelatedToAccessAndUse"/>
      </xsl:if>

      <!-- Conformity -->
      <xsl:apply-templates mode="iso19139-to-dcatap"
                           select="gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation">
        <xsl:with-param name="ResourceUri" select="$ResourceUri"/>
        <xsl:with-param name="MetadataLanguage" select="$MetadataLanguage"/>
        <xsl:with-param name="Conformity" select="$Conformity"/>
      </xsl:apply-templates>

      <!-- Spatial representation type -->
      <xsl:variable name="SpatialRepresentationType">
        <xsl:apply-templates mode="iso19139-to-dcatap"
                             select="gmd:identificationInfo/*/gmd:spatialRepresentationType/gmd:MD_SpatialRepresentationTypeCode"/>
      </xsl:variable>

      <xsl:for-each select="gmd:distributionInfo/gmd:MD_Distribution">
        <!-- Encoding -->
        <xsl:variable name="Encoding">
          <xsl:apply-templates mode="iso19139-to-dcatap"
                               select="gmd:distributionFormat/gmd:MD_Format/gmd:name/*"/>
        </xsl:variable>
        <!-- Resource locators (access / download URLs) -->
        <xsl:for-each select="gmd:transferOptions/*/gmd:onLine/*">

          <xsl:variable name="url" select="gmd:linkage/gmd:URL"/>

          <xsl:variable name="protocol" select="gmd:protocol/*"/>

          <xsl:variable name="protocol-url" select="gmd:protocol/gmx:Anchor/@xlink:href"/>

          <xsl:variable name="function" select="gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue"/>

          <xsl:variable name="Title">
            <xsl:for-each select="gmd:name">
              <dct:title xml:lang="{$MetadataLanguage}">
                <xsl:value-of select="normalize-space(*)"/>
              </dct:title>
              <xsl:call-template name="LocalisedString">
                <xsl:with-param name="term">dct:title</xsl:with-param>
              </xsl:call-template>
            </xsl:for-each>
          </xsl:variable>

          <xsl:variable name="Description">
            <xsl:for-each select="gmd:description">
              <dct:description xml:lang="{$MetadataLanguage}">
                <xsl:value-of select="normalize-space(*)"/>
              </dct:description>
              <xsl:call-template name="LocalisedString">
                <xsl:with-param name="term">dct:description</xsl:with-param>
              </xsl:call-template>
            </xsl:for-each>
          </xsl:variable>

          <xsl:variable name="TitleAndDescription">
            <!--
                        <xsl:for-each select="gmd:name/gco:CharacterString">
                          <dct:title xml:lang="{$MetadataLanguage}"><xsl:value-of select="."/></dct:title>
                        </xsl:for-each>
                        <xsl:for-each select="gmd:description/gco:CharacterString">
                          <dct:description xml:lang="{$MetadataLanguage}"><xsl:value-of select="."/></dct:description>
                         </xsl:for-each>
            -->
            <xsl:copy-of select="$Title"/>
            <xsl:copy-of select="$Description"/>
          </xsl:variable>

          <xsl:variable name="TitleOrDescriptionOrPlaceholder">
            <xsl:choose>
              <xsl:when test="normalize-space(gmd:name/*) != ''">
                <xsl:value-of select="normalize-space(gmd:name/*)"/>
              </xsl:when>
              <xsl:when test="normalize-space(gmd:description/*) != ''">
                <xsl:value-of select="normalize-space(gmd:description)"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>N/A</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>

          <xsl:choose>
            <!-- Mapping added to the core profile for compliance with DCAT-AP 2 -->
            <xsl:when test="$ResourceType = 'service'">
              <xsl:call-template name="service-endpoint">
                <xsl:with-param name="function" select="$function"/>
                <xsl:with-param name="protocol" select="$protocol"/>
                <xsl:with-param name="url" select="$url"/>
              </xsl:call-template>
              <xsl:if test="$profile = $extended">
                <xsl:call-template name="service-protocol">
                  <xsl:with-param name="function" select="$function"/>
                  <xsl:with-param name="protocol" select="$protocol"/>
                  <xsl:with-param name="url" select="$url"/>
                </xsl:call-template>
              </xsl:if>
            </xsl:when>
            <!-- Distributions -->
            <xsl:when test="$ResourceType = 'dataset' or $ResourceType = 'series'">
              <xsl:variable name="points-to-service">
                <xsl:call-template name="detect-service">
                  <xsl:with-param name="function" select="$function"/>
                  <xsl:with-param name="protocol" select="$protocol"/>
                  <xsl:with-param name="url" select="$url"/>
                </xsl:call-template>
              </xsl:variable>
              <xsl:choose>
                <xsl:when test="$points-to-service = 'yes' or $function = 'download' or $function = 'offlineAccess' or $function = 'order'">
                  <dcat:distribution>
                    <dcat:Distribution>
                      <!-- Title and description -->
                      <xsl:copy-of select="$TitleAndDescription"/>
                      <!-- Access URL -->
                      <!--
                                            <xsl:for-each select="gmd:linkage/gmd:URL">
                                              <dcat:accessURL rdf:resource="{.}"/>
                                            </xsl:for-each>
                      -->
                      <xsl:choose>
                        <xsl:when test="$points-to-service = 'yes'">
                          <dcat:accessService rdf:parseType="Resource">
                            <rdf:type rdf:resource="{$dcat}DataService"/>
                            <dct:title xml:lang="{$MetadataLanguage}">
                              <xsl:value-of select="$TitleOrDescriptionOrPlaceholder"/>
                            </dct:title>
                            <xsl:call-template name="service-endpoint">
                              <xsl:with-param name="function" select="$function"/>
                              <xsl:with-param name="protocol" select="$protocol"/>
                              <xsl:with-param name="url" select="$url"/>
                            </xsl:call-template>
                            <xsl:if test="$profile = $extended">
                              <xsl:call-template name="service-protocol">
                                <xsl:with-param name="function" select="$function"/>
                                <xsl:with-param name="protocol" select="$protocol"/>
                                <xsl:with-param name="url" select="$url"/>
                              </xsl:call-template>
                            </xsl:if>
                          </dcat:accessService>
                        </xsl:when>
                        <xsl:otherwise>
                        </xsl:otherwise>
                      </xsl:choose>
                      <dcat:accessURL rdf:resource="{$url}"/>
                      <!-- Constraints related to access and use -->
                      <xsl:copy-of select="$ConstraintsRelatedToAccessAndUse"/>
                      <!-- Spatial representation type (tentative) -->
                      <xsl:if test="$profile = $extended">
                        <xsl:copy-of select="$SpatialRepresentationType"/>
                      </xsl:if>
                      <!-- Encoding -->
                      <xsl:copy-of select="$Encoding"/>
                      <!-- Resource character encoding -->
                      <xsl:if test="$profile = $extended">
                        <xsl:copy-of select="$ResourceCharacterEncoding"/>
                      </xsl:if>
                    </dcat:Distribution>
                  </dcat:distribution>
                </xsl:when>
                <xsl:when test="$function = 'information' or $function = 'search'">
                  <!-- ?? Should foaf:page be detailed with title, description, etc.? -->
                  <xsl:for-each select="gmd:linkage/gmd:URL">
                    <foaf:page>
                      <foaf:Document rdf:about="{.}">
                        <xsl:copy-of select="$TitleAndDescription"/>
                      </foaf:Document>
                    </foaf:page>
                  </xsl:for-each>
                </xsl:when>
                <!-- ?? Should dcat:landingPage be detailed with title, description, etc.? -->
                <xsl:otherwise>
                  <xsl:for-each select="gmd:linkage/gmd:URL">
                    <dcat:landingPage>
                      <foaf:Document rdf:about="{.}">
                        <xsl:copy-of select="$TitleAndDescription"/>
                      </foaf:Document>
                    </dcat:landingPage>
                  </xsl:for-each>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:when>
          </xsl:choose>
        </xsl:for-each>
      </xsl:for-each>
      <!-- Responsible organisation -->
      <xsl:for-each select="gmd:identificationInfo/*/gmd:pointOfContact">
        <xsl:apply-templates mode="iso19139-to-dcatap"
                             select="gmd:CI_ResponsibleParty">
          <xsl:with-param name="MetadataLanguage" select="$MetadataLanguage"/>
          <xsl:with-param name="ResourceType" select="$ResourceType"/>
        </xsl:apply-templates>
      </xsl:for-each>
      <!--
            <xsl:apply-templates select="gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty">
              <xsl:with-param name="MetadataLanguage" select="$MetadataLanguage"/>
              <xsl:with-param name="ResourceType" select="$ResourceType"/>
            </xsl:apply-templates>
      -->
    </xsl:param>

    <xsl:param name="Metadata">
      <xsl:if test="$MetadataUri != '' or normalize-space($MetadataDescription)">
        <foaf:isPrimaryTopicOf>
          <xsl:choose>
            <xsl:when test="$MetadataUri != ''">
              <rdf:Description rdf:about="{$MetadataUri}">
                <xsl:copy-of select="$MetadataDescription"/>
              </rdf:Description>
            </xsl:when>
            <xsl:otherwise>
              <xsl:if test="normalize-space($MetadataDescription)">
                <rdf:Description>
                  <xsl:copy-of select="$MetadataDescription"/>
                </rdf:Description>
              </xsl:if>
            </xsl:otherwise>
          </xsl:choose>
        </foaf:isPrimaryTopicOf>
      </xsl:if>
    </xsl:param>

    <xsl:choose>
      <xsl:when test="$ResourceUri != ''">
        <rdf:Description rdf:about="{$ResourceUri}">
          <xsl:copy-of select="$Metadata"/>
          <xsl:copy-of select="$ResourceDescription"/>
        </rdf:Description>
      </xsl:when>
      <xsl:otherwise>
        <rdf:Description>
          <xsl:copy-of select="$Metadata"/>
          <xsl:copy-of select="$ResourceDescription"/>
        </rdf:Description>
      </xsl:otherwise>
    </xsl:choose>

    <!-- Mapping added to core profile for compliance with DCAT-AP 2 -->
    <!--
        <xsl:if test="$profile = $extended and $ResourceUri != '' and $Conformity != ''">
    -->
    <!--
        <xsl:if test="$ResourceUri != '' and $Conformity != ''">
          <xsl:copy-of select="$Conformity"/>
        </xsl:if>
    -->


  </xsl:template>

  <!--

    Templates for specific metadata elements
    ========================================

  -->

  <!-- Unique Resource Identifier -->

  <xsl:template mode="iso19139-to-dcatap" name="UniqueResourceIdentifier" match="gmd:identificationInfo/*/gmd:citation/*/gmd:identifier/*">
    <xsl:param name="ns">
      <xsl:value-of select="gmd:codeSpace/gco:CharacterString"/>
    </xsl:param>
    <xsl:param name="code">
      <xsl:value-of select="gmd:code/gco:CharacterString"/>
    </xsl:param>
    <xsl:param name="id">
      <xsl:choose>
        <xsl:when test="$ns != ''">
          <xsl:choose>
            <xsl:when test="substring($ns,string-length($ns),1) = '/'">
              <xsl:value-of select="concat($ns,$code)"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="concat($ns,'/',$code)"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$code"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <xsl:param name="idDatatypeURI">
      <xsl:choose>
        <xsl:when test="starts-with($id, 'http://') or starts-with($id, 'https://') or starts-with($id, 'urn:')">
          <xsl:value-of select="concat($xsd,'anyURI')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="concat($xsd,'string')"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <dct:identifier rdf:datatype="{$idDatatypeURI}"><xsl:value-of select="$id"/></dct:identifier>
  </xsl:template>

  <!-- Responsible Organisation -->
  <!--
    <xsl:template name="ResponsibleOrganisation" match="gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty">
  -->
  <xsl:template mode="iso19139-to-dcatap" name="ResponsibleOrganisation" match="gmd:CI_ResponsibleParty">
    <xsl:param name="MetadataLanguage"/>
    <xsl:param name="ResourceType"/>

    <xsl:param name="role">
      <xsl:value-of select="gmd:role/gmd:CI_RoleCode/@codeListValue"/>
    </xsl:param>

    <xsl:param name="ResponsiblePartyRole">
      <xsl:value-of select="concat($ResponsiblePartyRoleCodelistUri,'/',$role)"/>
    </xsl:param>

    <xsl:param name="IndividualURI">
      <xsl:value-of select="normalize-space(gmd:individualName/*/@xlink:href)"/>
    </xsl:param>

    <xsl:param name="IndividualName">
      <xsl:value-of select="normalize-space(gmd:individualName/*)"/>
    </xsl:param>

    <xsl:param name="IndividualName-FOAF">
      <xsl:for-each select="gmd:individualName">
        <foaf:name xml:lang="{$MetadataLanguage}"><xsl:value-of select="normalize-space(*[self::gco:CharacterString|gmx:Anchor])"/></foaf:name>
        <xsl:call-template name="LocalisedString">
          <xsl:with-param name="term">foaf:name</xsl:with-param>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:param>

    <xsl:param name="IndividualName-vCard">
      <xsl:for-each select="gmd:individualName">
        <vcard:fn xml:lang="{$MetadataLanguage}"><xsl:value-of select="normalize-space(*[self::gco:CharacterString|gmx:Anchor])"/></vcard:fn>
        <xsl:call-template name="LocalisedString">
          <xsl:with-param name="term">vcard:fn</xsl:with-param>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:param>

    <xsl:param name="OrganisationURI">
      <xsl:value-of select="normalize-space(gmd:organisationName/*/@xlink:href)"/>
    </xsl:param>

    <xsl:param name="URI">
      <xsl:choose>
        <xsl:when test="$IndividualURI != ''">
          <xsl:value-of select="$IndividualURI"/>
        </xsl:when>
        <xsl:when test="$OrganisationURI != ''">
          <xsl:value-of select="$OrganisationURI"/>
        </xsl:when>
      </xsl:choose>
    </xsl:param>

    <xsl:param name="OrganisationName">
      <xsl:value-of select="string-join(gmd:organisationName/*[self::gco:CharacterString|gmx:Anchor], '')"/>
    </xsl:param>
    <xsl:param name="OrganisationName-FOAF">
      <xsl:for-each select="gmd:organisationName">
        <foaf:name xml:lang="{$MetadataLanguage}"><xsl:value-of select="normalize-space(*[self::gco:CharacterString|gmx:Anchor])"/></foaf:name>
        <xsl:call-template name="LocalisedString">
          <xsl:with-param name="term">foaf:name</xsl:with-param>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:param>

    <xsl:param name="OrganisationName-vCard">
      <xsl:for-each select="gmd:organisationName">
        <vcard:organization-name xml:lang="{$MetadataLanguage}"><xsl:value-of select="normalize-space(*[self::gco:CharacterString|gmx:Anchor])"/></vcard:organization-name>
        <xsl:call-template name="LocalisedString">
          <xsl:with-param name="term">vcard:organization-name</xsl:with-param>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:param>

    <xsl:param name="OrganisationNameAsIndividualName-vCard">
      <xsl:for-each select="gmd:organisationName">
        <vcard:fn xml:lang="{$MetadataLanguage}"><xsl:value-of select="normalize-space(*[self::gco:CharacterString|gmx:Anchor])"/></vcard:fn>
        <xsl:call-template name="LocalisedString">
          <xsl:with-param name="term">vcard:fn</xsl:with-param>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:param>

    <xsl:param name="Email">
      <xsl:for-each select="gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/*[normalize-space() != '']">
        <foaf:mbox rdf:resource="mailto:{normalize-space(.)}"/>
      </xsl:for-each>
    </xsl:param>

    <xsl:param name="Email-vCard">
      <xsl:for-each select="gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/*[normalize-space() != '']">
        <vcard:hasEmail rdf:resource="mailto:{normalize-space(.)}"/>
      </xsl:for-each>
    </xsl:param>

    <xsl:param name="URL">
      <xsl:for-each select="gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/gmd:URL[normalize-space() != '']">
        <foaf:workplaceHomepage rdf:resource="{normalize-space(.)}"/>
      </xsl:for-each>
    </xsl:param>

    <xsl:param name="URL-vCard">
      <xsl:for-each select="gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/gmd:URL[normalize-space() != '']">
        <vcard:hasURL rdf:resource="{normalize-space(.)}"/>
      </xsl:for-each>
    </xsl:param>
    <xsl:param name="Telephone">
      <xsl:for-each select="gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:voice/*[normalize-space() != '']">
        <foaf:phone rdf:resource="tel:+{translate(translate(translate(translate(translate(normalize-space(.),' ',''),'(',''),')',''),'+',''),'.','')}"/>
      </xsl:for-each>
    </xsl:param>

    <xsl:param name="Telephone-vCard">
      <xsl:for-each select="gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:voice/*[normalize-space() != '']">
        <vcard:hasTelephone rdf:resource="tel:+{translate(translate(translate(translate(translate(normalize-space(.),' ',''),'(',''),')',''),'+',''),'.','')}"/>
      </xsl:for-each>
    </xsl:param>

    <xsl:param name="Address">
      <xsl:for-each select="gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address">
        <xsl:variable name="deliveryPoint" select="normalize-space(string-join(gmd:deliveryPoint/*, ' '))"/>
        <xsl:variable name="city" select="normalize-space(gmd:city/*)"/>
        <xsl:variable name="administrativeArea" select="normalize-space(gmd:administrativeArea/*)"/>
        <xsl:variable name="postalCode" select="normalize-space(gmd:postalCode/*)"/>
        <xsl:variable name="country" select="normalize-space(gmd:country/*)"/>
        <xsl:if test="$deliveryPoint != '' or $city != '' or $administrativeArea != '' or $postalCode != '' or $country != ''">
          <locn:address>
            <locn:Address>
              <xsl:if test="$deliveryPoint != ''">
                <locn:thoroughfare><xsl:value-of select="$deliveryPoint"/></locn:thoroughfare>
              </xsl:if>
              <xsl:if test="$city != ''">
                <locn:postName><xsl:value-of select="$city"/></locn:postName>
              </xsl:if>
              <xsl:if test="$administrativeArea != ''">
                <locn:adminUnitL2><xsl:value-of select="$administrativeArea"/></locn:adminUnitL2>
              </xsl:if>
              <xsl:if test="$postalCode != ''">
                <locn:postCode><xsl:value-of select="$postalCode"/></locn:postCode>
              </xsl:if>
              <xsl:if test="$country != ''">
                <locn:adminUnitL1><xsl:value-of select="$country"/></locn:adminUnitL1>
              </xsl:if>
            </locn:Address>
          </locn:address>
        </xsl:if>
      </xsl:for-each>
    </xsl:param>

    <xsl:param name="Address-vCard">
      <xsl:for-each select="gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address">
        <xsl:variable name="deliveryPoint" select="normalize-space(string-join(gmd:deliveryPoint/*, ' '))"/>
        <xsl:variable name="city" select="normalize-space(gmd:city/*)"/>
        <xsl:variable name="administrativeArea" select="normalize-space(gmd:administrativeArea/*)"/>
        <xsl:variable name="postalCode" select="normalize-space(gmd:postalCode/*)"/>
        <xsl:variable name="country" select="normalize-space(gmd:country/*)"/>
        <xsl:if test="$deliveryPoint != '' or $city != '' or $administrativeArea != '' or $postalCode != '' or $country != ''">
          <vcard:hasAddress>
            <vcard:Address>
              <xsl:if test="$deliveryPoint != ''">
                <vcard:street-address><xsl:value-of select="$deliveryPoint"/></vcard:street-address>
              </xsl:if>
              <xsl:if test="$city != ''">
                <vcard:locality><xsl:value-of select="$city"/></vcard:locality>
              </xsl:if>
              <xsl:if test="$administrativeArea != ''">
                <vcard:region><xsl:value-of select="$administrativeArea"/></vcard:region>
              </xsl:if>
              <xsl:if test="$postalCode != ''">
                <vcard:postal-code><xsl:value-of select="$postalCode"/></vcard:postal-code>
              </xsl:if>
              <xsl:if test="$country != ''">
                <vcard:country-name><xsl:value-of select="$country"/></vcard:country-name>
              </xsl:if>
            </vcard:Address>
          </vcard:hasAddress>
        </xsl:if>
      </xsl:for-each>
    </xsl:param>

    <xsl:param name="ROInfo">
      <xsl:variable name="info">
        <xsl:choose>
          <xsl:when test="$IndividualName != ''">
            <rdf:type rdf:resource="{$foaf}Person"/>
          </xsl:when>
          <xsl:when test="$OrganisationName != ''">
            <rdf:type rdf:resource="{$foaf}Organization"/>
          </xsl:when>
          <xsl:otherwise>
            <rdf:type rdf:resource="{$foaf}Agent"/>
          </xsl:otherwise>
        </xsl:choose>

        <!-- CAVEAT: The following xsl:if ensures that the agent is typed also as prov:Agent for its
             use with the PROV-based approach. The conditions MUST be aligned with the one used later
             just before the statement with predicate prov:agent. -->

        <xsl:if test="($ResourceType != 'service' and $role != 'pointOfContact' and $role != 'publisher' and $role != 'author') or $profile = $extended">
          <rdf:type rdf:resource="{$prov}Agent"/>
        </xsl:if>

        <xsl:if test="$IndividualName != ''">
          <!--
                    <foaf:name xml:lang="{$MetadataLanguage}">
                      <xsl:value-of select="$IndividualName"/>
                    </foaf:name>
          -->
          <xsl:copy-of select="$IndividualName-FOAF"/>
          <xsl:if test="$OrganisationName != ''">
            <org:memberOf>
              <xsl:choose>
                <xsl:when test="$OrganisationURI != ''">
                  <foaf:Organization rdf:about="{$OrganisationURI}">
                    <!--
                                        <foaf:name xml:lang="{$MetadataLanguage}"><xsl:value-of select="$OrganisationName"/></foaf:name>
                    -->
                    <xsl:copy-of select="$OrganisationName-FOAF"/>
                  </foaf:Organization>
                </xsl:when>
                <xsl:otherwise>
                  <foaf:Organization>
                    <!--
                                        <foaf:name xml:lang="{$MetadataLanguage}"><xsl:value-of select="$OrganisationName"/></foaf:name>
                    -->
                    <xsl:copy-of select="$OrganisationName-FOAF"/>
                  </foaf:Organization>
                </xsl:otherwise>
              </xsl:choose>
            </org:memberOf>
          </xsl:if>
        </xsl:if>
        <xsl:if test="$IndividualName = '' and $OrganisationName != ''">
          <!--
                    <foaf:name xml:lang="{$MetadataLanguage}">
                      <xsl:value-of select="$OrganisationName"/>
                    </foaf:name>
          -->
          <xsl:copy-of select="$OrganisationName-FOAF"/>
        </xsl:if>
        <xsl:copy-of select="$Telephone"/>
        <xsl:copy-of select="$Email"/>
        <xsl:copy-of select="$URL"/>
        <xsl:copy-of select="$Address"/>
        <!--
              <xsl:for-each select="gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString">
                <foaf:mbox rdf:resource="mailto:{.}"/>
              </xsl:for-each>
              <xsl:for-each select="gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/gmd:URL">
        -->
        <!-- ?? Should another property be used instead? E.g., foaf:homepage? -->
        <!--
                <foaf:workplaceHomepage rdf:resource="{.}"/>
              </xsl:for-each>
        -->
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="$IndividualURI != ''">
          <rdf:Description rdf:about="{$IndividualURI}">
            <xsl:copy-of select="$info"/>
          </rdf:Description>
        </xsl:when>
        <xsl:when test="$OrganisationURI != ''">
          <rdf:Description rdf:about="{$OrganisationURI}">
            <xsl:copy-of select="$info"/>
          </rdf:Description>
        </xsl:when>
        <xsl:otherwise>
          <rdf:Description>
            <xsl:copy-of select="$info"/>
          </rdf:Description>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:param>

    <xsl:param name="ResponsibleParty">
      <xsl:variable name="info">
        <xsl:choose>
          <xsl:when test="$IndividualName != ''">
            <rdf:type rdf:resource="{$vcard}Individual"/>
          </xsl:when>
          <xsl:when test="$OrganisationName != ''">
            <rdf:type rdf:resource="{$vcard}Organization"/>
          </xsl:when>
          <xsl:otherwise>
            <rdf:type rdf:resource="{$vcard}Kind"/>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="$IndividualName != ''">
          <!--
                    <vcard:fn xml:lang="{$MetadataLanguage}">
                      <xsl:value-of select="$IndividualName"/>
                    </vcard:fn>
          -->
          <xsl:copy-of select="$IndividualName-vCard"/>
        </xsl:if>
        <xsl:if test="$IndividualName != '' and $OrganisationName != ''">
          <!--
                    <vcard:organization-name xml:lang="{$MetadataLanguage}">
                      <xsl:value-of select="$OrganisationName"/>
                    </vcard:organization-name>
          -->
          <xsl:copy-of select="$OrganisationName-vCard"/>
        </xsl:if>
        <xsl:if test="$IndividualName = '' and $OrganisationName != ''">
          <!--
                    <vcard:fn xml:lang="{$MetadataLanguage}">
                      <xsl:value-of select="$OrganisationName"/>
                    </vcard:fn>
          -->
          <xsl:copy-of select="$OrganisationNameAsIndividualName-vCard"/>
        </xsl:if>
        <xsl:copy-of select="$Telephone-vCard"/>
        <xsl:copy-of select="$Email-vCard"/>
        <xsl:copy-of select="$URL-vCard"/>
        <xsl:copy-of select="$Address-vCard"/>
        <!--
              <xsl:for-each select="gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString">
                <vcard:hasEmail rdf:resource="mailto:{.}"/>
              </xsl:for-each>
              <xsl:for-each select="gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/gmd:URL">
                <vcard:hasURL rdf:resource="{.}"/>
              </xsl:for-each>
        -->
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="$IndividualURI != ''">
          <rdf:Description rdf:about="{$IndividualURI}">
            <xsl:copy-of select="$info"/>
          </rdf:Description>
        </xsl:when>
        <xsl:when test="$OrganisationURI != ''">
          <rdf:Description rdf:about="{$OrganisationURI}">
            <xsl:copy-of select="$info"/>
          </rdf:Description>
        </xsl:when>
        <xsl:otherwise>
          <rdf:Description>
            <xsl:copy-of select="$info"/>
          </rdf:Description>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:param>

    <xsl:choose>
      <!-- Mapping added for compliance with GeoDCAT-AP 2 -->
      <xsl:when test="$role = 'resourceProvider' and $profile = $extended">
        <geodcatap:resourceProvider>
          <xsl:copy-of select="$ROInfo"/>
        </geodcatap:resourceProvider>
      </xsl:when>
      <!-- Mapping added for compliance with GeoDCAT-AP 2 -->
      <xsl:when test="$role = 'custodian' and $profile = $extended">
        <geodcatap:custodian>
          <xsl:copy-of select="$ROInfo"/>
        </geodcatap:custodian>
      </xsl:when>
      <xsl:when test="$role = 'owner' and $profile = $extended">
        <dct:rightsHolder>
          <xsl:copy-of select="$ROInfo"/>
        </dct:rightsHolder>
      </xsl:when>
      <!-- Mapping added for compliance with GeoDCAT-AP 2 -->
      <xsl:when test="$role = 'user' and $profile = $extended">
        <geodcatap:user>
          <xsl:copy-of select="$ROInfo"/>
        </geodcatap:user>
      </xsl:when>
      <!--
            <xsl:when test="$role = 'user'">
              <prov:wasUsedBy>
                <prov:Activity>
                  <prov:wasAssociatedWith>
                    <xsl:copy-of select="$ROInfo"/>
                  </prov:wasAssociatedWith>
                </prov:Activity>
              </prov:wasUsedBy>
            </xsl:when>
      -->
      <!-- Mapping added for compliance with GeoDCAT-AP 2 -->
      <xsl:when test="$role = 'distributor' and $profile = $extended">
        <geodcatap:distributor>
          <xsl:copy-of select="$ROInfo"/>
        </geodcatap:distributor>
      </xsl:when>
      <!-- Mapping added for compliance with GeoDCAT-AP 2 -->
      <xsl:when test="$role = 'originator' and $profile = $extended">
        <geodcatap:originator>
          <xsl:copy-of select="$ROInfo"/>
        </geodcatap:originator>
      </xsl:when>
      <xsl:when test="$role = 'pointOfContact' and ($ResourceType != 'service' or $profile = $extended)">
        <dcat:contactPoint>
          <xsl:copy-of select="$ResponsibleParty"/>
        </dcat:contactPoint>
      </xsl:when>
      <!--
            <xsl:when test="$role = 'pointOfContact' and $ResourceType != 'service'">
              <dcat:contactPoint>
                <xsl:copy-of select="$ResponsibleParty"/>
              </dcat:contactPoint>
            </xsl:when>
      -->
      <!-- Mapping added for compliance with GeoDCAT-AP 2 -->
      <xsl:when test="$role = 'principalInvestigator' and $profile = $extended">
        <geodcatap:principalInvestigator>
          <xsl:copy-of select="$ROInfo"/>
        </geodcatap:principalInvestigator>
      </xsl:when>
      <!-- Mapping added for compliance with GeoDCAT-AP 2 -->
      <xsl:when test="$role = 'processor' and $profile = $extended">
        <geodcatap:processor>
          <xsl:copy-of select="$ROInfo"/>
        </geodcatap:processor>
      </xsl:when>
      <!--
            <xsl:when test="$role = 'processor'">
              <prov:entityOfInfluence>
                <prov:Derivation>
                  <prov:hadActivity>
                    <prov:Activity>
                      <prov:wasAssociatedWith>
                        <xsl:copy-of select="$ROInfo"/>
                      </prov:wasAssociatedWith>
                    </prov:Activity>
                  </prov:hadActivity>
                </prov:Derivation>
              </prov:entityOfInfluence>
            </xsl:when>
      -->
      <xsl:when test="$role = 'publisher' and ($ResourceType != 'service' or $profile = $extended)">
        <dct:publisher>
          <xsl:copy-of select="$ROInfo"/>
        </dct:publisher>
      </xsl:when>
      <!-- Mapping moved to core profile for compliance with DCAT-AP 2 -->
      <xsl:when test="$role = 'author' and ($ResourceType != 'service' or $profile = $extended)">
        <dct:creator>
          <xsl:copy-of select="$ROInfo"/>
        </dct:creator>
      </xsl:when>
      <!--
            <xsl:when test="$role = 'author' and $profile = $extended">
              <dct:creator>
                <xsl:copy-of select="$ROInfo"/>
              </dct:creator>
            </xsl:when>
      -->
    </xsl:choose>
    <!-- Mapping moved to core profile for compliance with DCAT-AP 2 -->
    <xsl:if test="($ResourceType != 'service' and $role != 'pointOfContact' and $role != 'publisher' and $role != 'author') or $profile = $extended">
      <prov:qualifiedAttribution>
        <prov:Attribution>
          <prov:agent>
            <!--
                        <xsl:copy-of select="$ResponsibleParty"/>
            -->
            <xsl:copy-of select="$ROInfo"/>
          </prov:agent>
          <!-- DEPRECATED: Mapping kept for backward compatibility with GeoDCAT-AP v1.* -->
          <xsl:if test="$profile = $extended">
            <xsl:if test="$include-deprecated = 'yes'">
              <dct:type rdf:resource="{$ResponsiblePartyRole}"/>
            </xsl:if>
          </xsl:if>
          <!-- Mapping added for compliance with DCAT-AP 2 -->
          <dcat:hadRole>
            <dcat:Role rdf:about="{$ResponsiblePartyRole}"/>
          </dcat:hadRole>
        </prov:Attribution>
      </prov:qualifiedAttribution>
    </xsl:if>

  </xsl:template>

  <!-- Metadata point of contact -->
  <!--
    <xsl:template name="MetadataPointOfContact" match="gmd:contact/gmd:CI_ResponsibleParty">
      <xsl:param name="MetadataLanguage"/>
      <xsl:param name="ResponsiblePartyRole">
        <xsl:value-of select="concat($ResponsiblePartyRoleCodelistUri,'/','pointOfContact')"/>
      </xsl:param>
      <xsl:param name="OrganisationName">
        <xsl:value-of select="gmd:organisationName/gco:CharacterString"/>
      </xsl:param>
      <xsl:param name="ResponsibleParty">
        <vcard:Kind>
          <vcard:organization-name xml:lang="{$MetadataLanguage}">
            <xsl:value-of select="$OrganisationName"/>
          </vcard:organization-name>
          <xsl:for-each select="gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString">
            <vcard:hasEmail rdf:resource="mailto:{.}"/>
          </xsl:for-each>
        </vcard:Kind>
      </xsl:param>
      <dcat:contactPoint>
        <xsl:copy-of select="$ResponsibleParty"/>
      </dcat:contactPoint>
      <xsl:if test="$profile = $extended">
        <prov:qualifiedAttribution>
          <prov:Attribution>
            <prov:agent>
              <xsl:copy-of select="$ResponsibleParty"/>
            </prov:agent>
            <dct:type rdf:resource="{$ResponsiblePartyRole}"/>
          </prov:Attribution>
        </prov:qualifiedAttribution>
      </xsl:if>
    </xsl:template>
  -->
  <!-- Resource locator -->
  <!-- Old version, applied to the resource (not to the resource distribution)
    <xsl:template name="ResourceLocator" match="gmd:distributionInfo/*/gmd:transferOptions/*/gmd:onLine/*/gmd:linkage">
      <xsl:param name="ResourceType"/>
      <xsl:choose>
        <xsl:when test="$ResourceType = 'dataset' or $ResourceType = 'series'">
          <dcat:landingPage rdf:resource="{gmd:URL}"/>
        </xsl:when>
        <xsl:when test="$ResourceType = 'service'">
          <foaf:homepage rdf:resource="{gmd:URL}"/>
        </xsl:when>
      </xsl:choose>
    </xsl:template>
  -->
  <xsl:template mode="iso19139-to-dcatap" name="ResourceLocator" match="gmd:transferOptions/*/gmd:onLine/*/gmd:linkage">
    <xsl:param name="MetadataLanguage"/>
    <xsl:param name="ResourceType"/>
    <xsl:choose>
      <xsl:when test="$ResourceType = 'dataset' or $ResourceType = 'series'">
        <dct:title xml:lang="{$MetadataLanguage}">
          <xsl:value-of select="../gmd:description/gco:CharacterString"/>
        </dct:title>
        <dcat:accessURL rdf:resource="{gmd:URL}"/>
      </xsl:when>
      <!--
            <xsl:when test="$ResourceType = 'service'">
              <foaf:homepage rdf:resource="{gmd:URL}"/>
            </xsl:when>
      -->
    </xsl:choose>
  </xsl:template>

  <!-- Coupled resource -->

  <xsl:template mode="iso19139-to-dcatap" name="CoupledResource" match="gmd:identificationInfo[1]/*/srv:operatesOn">
    <xsl:param name="href" select="@xlink:href"/>
    <xsl:param name="code">
      <xsl:choose>
        <xsl:when test="$CoupledResourceLookUp = 'enabled' and $href != '' and (starts-with($href, 'http://') or starts-with($href, 'https://'))">
          <xsl:value-of select="document($href)//gmd:identificationInfo/*/gmd:citation/*/gmd:identifier/*/gmd:code/gco:CharacterString"/>
        </xsl:when>
        <xsl:when test="*/gmd:citation/*/gmd:identifier/*/gmd:code/gco:CharacterString != ''">
          <xsl:value-of select="*/gmd:citation/*/gmd:identifier/*/gmd:code/gco:CharacterString"/>
        </xsl:when>
        <xsl:when test="@uuidref != ''">
          <xsl:value-of select="@uuidref"/>
        </xsl:when>
      </xsl:choose>
    </xsl:param>
    <xsl:param name="codespace">
      <xsl:choose>
        <xsl:when test="$CoupledResourceLookUp = 'enabled' and $href != '' and (starts-with($href, 'http://') or starts-with($href, 'https://'))">
          <xsl:value-of select="document($href)//gmd:identificationInfo/*/gmd:citation/*/gmd:identifier/*/gmd:codeSpace/gco:CharacterString"/>
        </xsl:when>
        <xsl:when test="*/gmd:citation/*/gmd:identifier/*/gmd:codeSpace/gco:CharacterString != ''">
          <xsl:value-of select="*/gmd:citation/*/gmd:identifier/*/gmd:codeSpace/gco:CharacterString"/>
        </xsl:when>
      </xsl:choose>
    </xsl:param>
    <xsl:param name="resID" select="concat($codespace, $code)"/>
    <xsl:param name="uriref" select="@uriref"/>
    <xsl:choose>
      <!-- The use of @uriref is still under discussion by the INSPIRE MIG. -->
      <xsl:when test="$uriref != ''">
        <!-- DEPRECATED: Mapping kept for backward compatibility with GeoDCAT-AP v1.* -->
        <xsl:if test="$profile = $extended">
          <xsl:if test="$include-deprecated = 'yes'">
            <dct:hasPart>
              <dcat:Dataset rdf:about="{@uriref}"/>
            </dct:hasPart>
          </xsl:if>
        </xsl:if>
        <!-- Mapping added for compliance with DCAT-AP 2 -->
        <dcat:servesDataset>
          <dcat:Dataset rdf:about="{@uriref}"/>
        </dcat:servesDataset>
      </xsl:when>
      <xsl:when test="$code != ''">
        <xsl:choose>
          <xsl:when test="starts-with($code, 'http://') or starts-with($code, 'https://')">
            <!-- DEPRECATED: Mapping kept for backward compatibility with GeoDCAT-AP v1.* -->
            <xsl:if test="$profile = $extended">
              <xsl:if test="$include-deprecated = 'yes'">
                <dct:hasPart>
                  <dcat:Dataset rdf:about="{$code}"/>
                </dct:hasPart>
              </xsl:if>
            </xsl:if>
            <!-- Mapping added for compliance with DCAT-AP 2 -->
            <dcat:servesDataset>
              <dcat:Dataset rdf:about="{$code}"/>
            </dcat:servesDataset>
          </xsl:when>
          <xsl:otherwise>
            <!-- DEPRECATED: Mapping kept for backward compatibility with GeoDCAT-AP v1.* -->
            <xsl:if test="$profile = $extended">
              <xsl:if test="$include-deprecated = 'yes'">
                <dct:hasPart rdf:parseType="Resource">
                  <rdf:type rdf:resource="{$dcat}Dataset"/>
                  <xsl:choose>
                    <xsl:when test="starts-with($resID, 'http://') or starts-with($resID, 'https://')">
                      <dct:identifier rdf:datatype="{$xsd}anyURI"><xsl:value-of select="$resID"/></dct:identifier>
                    </xsl:when>
                    <xsl:otherwise>
                      <dct:identifier rdf:datatype="{$xsd}string"><xsl:value-of select="$resID"/></dct:identifier>
                    </xsl:otherwise>
                  </xsl:choose>
                  <xsl:if test="$href != '' and $href != '' and (starts-with($href, 'http://') or starts-with($href, 'https://'))">
                    <foaf:isPrimaryTopicOf>
                      <dcat:CatalogRecord rdf:about="{$href}"/>
                    </foaf:isPrimaryTopicOf>
                  </xsl:if>
                </dct:hasPart>
              </xsl:if>
            </xsl:if>
            <!-- Mapping added for compliance with DCAT-AP 2 -->
            <dcat:servesDataset rdf:parseType="Resource">
              <rdf:type rdf:resource="{$dcat}Dataset"/>
              <xsl:choose>
                <xsl:when test="starts-with($resID, 'http://') or starts-with($resID, 'https://')">
                  <dct:identifier rdf:datatype="{$xsd}anyURI"><xsl:value-of select="$resID"/></dct:identifier>
                </xsl:when>
                <xsl:otherwise>
                  <dct:identifier rdf:datatype="{$xsd}string"><xsl:value-of select="$resID"/></dct:identifier>
                </xsl:otherwise>
              </xsl:choose>
              <xsl:if test="$href != '' and $href != '' and (starts-with($href, 'http://') or starts-with($href, 'https://'))">
                <foaf:isPrimaryTopicOf>
                  <dcat:CatalogRecord rdf:about="{$href}"/>
                </foaf:isPrimaryTopicOf>
              </xsl:if>
            </dcat:servesDataset>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <!-- Spatial data service type -->
  <!-- Replaced by param $ServiceType -->
  <!--
    <xsl:template match="gmd:identificationInfo/*/srv:serviceType">
      <dct:type rdf:resource="{$SpatialDataServiceTypeCodelistUri}/{gco:LocalName}"/>
    </xsl:template>
  -->
  <!-- Conformity -->
  <xsl:template mode="iso19139-to-dcatap" name="Conformity" match="gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation">
    <xsl:param name="ResourceUri"/>
    <xsl:param name="MetadataLanguage"/>
    <xsl:param name="Conformity"/>

    <xsl:variable name="specUri" select="normalize-space(gmd:title/gmx:Anchor/@xlink:href)"/>
    <xsl:variable name="specTitle">
      <xsl:for-each select="gmd:title">
        <!--
                <dct:title xml:lang="{$MetadataLanguage}"><xsl:value-of select="normalize-space(gco:CharacterString)"/></dct:title>
        -->
        <dct:title xml:lang="{$MetadataLanguage}">
          <xsl:value-of select="normalize-space(*[self::gco:CharacterString|self::gmx:Anchor])"/>
        </dct:title>
        <xsl:call-template name="LocalisedString">
          <xsl:with-param name="term">dct:title</xsl:with-param>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:variable>
    <xsl:variable name="specinfo">
      <!--
            <dct:title xml:lang="{$MetadataLanguage}">
              <xsl:value-of select="gmd:title/gco:CharacterString"/>
            </dct:title>
      -->
      <xsl:copy-of select="$specTitle"/>
      <xsl:apply-templates mode="iso19139-to-dcatap"
                           select="gmd:date/gmd:CI_Date"/>
    </xsl:variable>
    <!--
        <xsl:variable name="specinfo">
          <dct:title xml:lang="{$MetadataLanguage}">
            <xsl:value-of select="gmd:title/gco:CharacterString"/>
          </dct:title>
          <xsl:apply-templates select="gmd:date/gmd:CI_Date"/>
        </xsl:variable>
    -->
    <!--
        <xsl:variable name="degree">
          <xsl:choose>
            <xsl:when test="../../gmd:pass/gco:Boolean = 'true'">
              <xsl:value-of select="concat($DegreeOfConformityCodelistUri,'/conformant')"/>
            </xsl:when>
            <xsl:when test="../../gmd:pass/gco:Boolean = 'false'">
              <xsl:value-of select="concat($DegreeOfConformityCodelistUri,'/notConformant')"/>
            </xsl:when>
            <xsl:otherwise>

            <xsl:when test="../../gmd:pass/gco:Boolean = ''">

              <xsl:value-of select="concat($DegreeOfConformityCodelistUri,'/notEvaluated')"/>

            </xsl:when>

            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
    -->
    <xsl:if test="../../gmd:pass/gco:Boolean = 'true'">
      <xsl:choose>
        <xsl:when test="$specUri != ''">
          <dct:conformsTo>
            <dct:Standard rdf:about="{$specUri}"/>
          </dct:conformsTo>
          <!--
                    <dct:conformsTo>
                      <rdf:Description rdf:about="{$specUri}">
                        <xsl:copy-of select="$specinfo"/>
                      </rdf:Description>
                    </dct:conformsTo>
          -->
        </xsl:when>
        <xsl:when test="../@xlink:href and ../@xlink:href != ''">
          <dct:conformsTo>
            <dct:Standard rdf:about="{../@xlink:href}"/>
          </dct:conformsTo>
          <!--
                    <dct:conformsTo>
                      <rdf:Description rdf:about="{../@xlink:href}">
                        <xsl:copy-of select="$specinfo"/>
                      </rdf:Description>
                    </dct:conformsTo>
          -->
        </xsl:when>
        <xsl:otherwise>
          <dct:conformsTo rdf:parseType="Resource">
            <rdf:type rdf:resource="{$dct}Standard"/>
            <xsl:copy-of select="$specinfo"/>
          </dct:conformsTo>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
    <!-- Mapping moved to core profile for compliance with DCAT-AP 2 -->
    <!--
        <xsl:if test="$profile = $extended">
    -->
    <!--
          <xsl:if test="$Conformity != '' and $ResourceUri = ''">
    -->
    <xsl:copy-of select="$Conformity"/>
    <!--
          </xsl:if>
    -->
    <!--
          <xsl:choose>
            <xsl:when test="../@xlink:href and ../@xlink:href != ''">
              <wdrs:describedby>
                <earl:Assertion>
                  <earl:test>
                    <rdf:Description rdf:about="{../@xlink:href}">
                      <xsl:copy-of select="$specinfo"/>
                    </rdf:Description>
                  </earl:test>
                  <earl:result>
                    <earl:TestResult>
                      <earl:outcome rdf:resource="{$degree}"/>
                    </earl:TestResult>
                  </earl:result>
                </earl:Assertion>
              </wdrs:describedby>
            </xsl:when>
            <xsl:otherwise>
              <wdrs:describedby>
                <earl:Assertion>
                  <earl:test rdf:parseType="Resource">
                    <xsl:copy-of select="$specinfo"/>
                  </earl:test>
                  <earl:result>
                    <earl:TestResult>
                      <earl:outcome rdf:resource="{$degree}"/>
                    </earl:TestResult>
                  </earl:result>
                </earl:Assertion>
              </wdrs:describedby>
            </xsl:otherwise>
          </xsl:choose>
    -->
    <!--
        </xsl:if>
    -->
  </xsl:template>

  <!-- Geographic extent -->

  <xsl:template mode="iso19139-to-dcatap" name="GeographicExtent" match="gmd:identificationInfo[1]/*/*[self::gmd:extent|self::srv:extent]/*/gmd:geographicElement">
    <xsl:param name="MetadataLanguage"/>
    <!--

          <xsl:otherwise>
            <dct:spatial>
              <dct:Location>
                <xsl:for-each select="gmd:description">
                  <rdfs:label xml:lang="{$MetadataLanguage}"><xsl:value-of select="gco:CharacterString"/></rdfs:label>
                </xsl:for-each>
    -->
    <xsl:apply-templates mode="iso19139-to-dcatap"
                         select="gmd:EX_GeographicDescription/gmd:geographicIdentifier/*">
      <xsl:with-param name="MetadataLanguage" select="$MetadataLanguage"/>
    </xsl:apply-templates>
    <xsl:apply-templates mode="iso19139-to-dcatap"
                         select="gmd:EX_GeographicBoundingBox"/>
    <!--
              </dct:Location>
            </dct:spatial>
          </xsl:otherwise>
        </xsl:choose>
    -->
  </xsl:template>

  <!-- Geographic identifier -->

  <xsl:template mode="iso19139-to-dcatap" name="GeographicIdentifier" match="gmd:EX_GeographicDescription/gmd:geographicIdentifier/*">
    <xsl:param name="MetadataLanguage"/>
    <!--
        <xsl:param name="GeoCode" select="gmd:code/*[self::gco:CharacterString|self::gmx:Anchor/@xlink:href]"/>
        <xsl:param name="GeoURI" select="gmd:code/gmx:Anchor/@xlink:href"/>
    -->
    <xsl:param name="GeoCode">
      <xsl:choose>
        <xsl:when test="gmd:code/gco:CharacterString">
          <xsl:value-of select="gmd:code/gco:CharacterString"/>
        </xsl:when>
        <xsl:when test="gmd:code/gmx:Anchor">
          <xsl:value-of select="gmd:code/gmx:Anchor/@xlink:href"/>
        </xsl:when>
      </xsl:choose>
    </xsl:param>

    <xsl:param name="GeoURI">
      <xsl:if test="starts-with($GeoCode,'http://') or starts-with($GeoCode,'https://')">
        <xsl:value-of select="$GeoCode"/>
      </xsl:if>
    </xsl:param>
    <xsl:param name="GeoURN">
      <xsl:if test="starts-with($GeoCode,'urn:')">
        <xsl:value-of select="$GeoCode"/>
      </xsl:if>
    </xsl:param>

    <xsl:choose>
      <xsl:when test="$GeoURI != ''">
        <!--
                <xsl:choose>
                  <xsl:when test="gmd:EX_GeographicBoundingBox">
                    <dct:spatial>
                      <dct:Location rdf:about="{$GeoURI}">
                        <xsl:if test="$GeoCode != ''">
                          <rdfs:label xml:lang="{$MetadataLanguage}"><xsl:value-of select="$GeoCode"/></rdfs:label>
                        </xsl:if>
                        <xsl:apply-templates select="gmd:EX_GeographicBoundingBox"/>
                      </dct:Location>
                    </dct:spatial>
                  </xsl:when>
                  <xsl:otherwise>
        -->
        <dct:spatial>
          <dct:Location rdf:about="{$GeoURI}"/>
        </dct:spatial>
        <!--
                  </xsl:otherwise>
                </xsl:choose>
        -->
      </xsl:when>
      <xsl:when test="$GeoCode != ''">
        <dct:spatial rdf:parseType="Resource">
          <rdf:type rdf:resource="{$dct}Location"/>
          <!--
                    <rdfs:seeAlso rdf:parseType="Resource">
          -->
          <xsl:choose>
            <xsl:when test="$GeoURN != ''">
              <dct:identifier rdf:datatype="{$xsd}anyURI"><xsl:value-of select="$GeoURN"/></dct:identifier>
            </xsl:when>
            <xsl:otherwise>
              <skos:prefLabel xml:lang="{$MetadataLanguage}">
                <xsl:value-of select="$GeoCode"/>
              </skos:prefLabel>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:for-each select="gmd:authority/gmd:CI_Citation">
            <skos:inScheme>
              <skos:ConceptScheme>
                <dct:title xml:lang="{$MetadataLanguage}">
                  <xsl:value-of select="gmd:title/*[self::gco:CharacterString|self::gmx:Anchor]"/>
                </dct:title>
                <xsl:apply-templates mode="iso19139-to-dcatap"
                                     select="gmd:date/gmd:CI_Date"/>
              </skos:ConceptScheme>
            </skos:inScheme>
          </xsl:for-each>
          <!--
                    </rdfs:seeAlso>
          -->
        </dct:spatial>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <!-- Geographic bounding box -->

  <!--
    <xsl:template name="GeographicBoundingBox" match="gmd:identificationInfo[1]/*/*[self::gmd:extent|self::srv:extent]/*/gmd:geographicElement/gmd:EX_GeographicBoundingBox">
  -->
  <xsl:template mode="iso19139-to-dcatap" name="GeographicBoundingBox" match="gmd:EX_GeographicBoundingBox">
    <xsl:param name="north" select="gmd:northBoundLatitude/gco:Decimal"/>
    <xsl:param name="east"  select="gmd:eastBoundLongitude/gco:Decimal"/>
    <xsl:param name="south" select="gmd:southBoundLatitude/gco:Decimal"/>
    <xsl:param name="west"  select="gmd:westBoundLongitude/gco:Decimal"/>

    <!-- Bbox as a dct:Box -->
    <!-- Need to check whether this is correct - in particular, the "projection" parameter -->
    <!--
        <xsl:param name="DCTBox">northlimit=<xsl:value-of select="$north"/>; eastlimit=<xsl:value-of select="$east"/>; southlimit=<xsl:value-of select="$south"/>; westlimit=<xsl:value-of select="$west"/>; projection=EPSG:<xsl:value-of select="$srid"/></xsl:param>
    -->

    <!-- Bbox as GML (GeoSPARQL) -->

    <xsl:param name="GMLLiteral">
      <xsl:choose>
        <xsl:when test="$SrsUri = $Crs84Uri">&lt;gml:Envelope srsName="<xsl:value-of select="$SrsUri"/>"&gt;&lt;gml:lowerCorner&gt;<xsl:value-of select="$west"/><xsl:text> </xsl:text><xsl:value-of select="$south"/>&lt;/gml:lowerCorner&gt;&lt;gml:upperCorner&gt;<xsl:value-of select="$east"/><xsl:text> </xsl:text><xsl:value-of select="$north"/>&lt;/gml:upperCorner&gt;&lt;/gml:Envelope&gt;</xsl:when>
        <xsl:when test="$SrsAxisOrder = 'LonLat'">&lt;gml:Envelope srsName="<xsl:value-of select="$SrsUri"/>"&gt;&lt;gml:lowerCorner&gt;<xsl:value-of select="$west"/><xsl:text> </xsl:text><xsl:value-of select="$south"/>&lt;/gml:lowerCorner&gt;&lt;gml:upperCorner&gt;<xsl:value-of select="$east"/><xsl:text> </xsl:text><xsl:value-of select="$north"/>&lt;/gml:upperCorner&gt;&lt;/gml:Envelope&gt;</xsl:when>
        <xsl:when test="$SrsAxisOrder = 'LatLon'">&lt;gml:Envelope srsName="<xsl:value-of select="$SrsUri"/>"&gt;&lt;gml:lowerCorner&gt;<xsl:value-of select="$south"/><xsl:text> </xsl:text><xsl:value-of select="$west"/>&lt;/gml:lowerCorner&gt;&lt;gml:upperCorner&gt;<xsl:value-of select="$north"/><xsl:text> </xsl:text><xsl:value-of select="$east"/>&lt;/gml:upperCorner&gt;&lt;/gml:Envelope&gt;</xsl:when>
      </xsl:choose>
    </xsl:param>

    <!-- Bbox as WKT (GeoSPARQL) -->

    <xsl:param name="WKTLiteral">
      <xsl:choose>
        <xsl:when test="$SrsUri = $Crs84Uri">POLYGON((<xsl:value-of select="$west"/><xsl:text> </xsl:text><xsl:value-of select="$north"/>,<xsl:value-of select="$east"/><xsl:text> </xsl:text><xsl:value-of select="$north"/>,<xsl:value-of select="$east"/><xsl:text> </xsl:text><xsl:value-of select="$south"/>,<xsl:value-of select="$west"/><xsl:text> </xsl:text><xsl:value-of select="$south"/>,<xsl:value-of select="$west"/><xsl:text> </xsl:text><xsl:value-of select="$north"/>))</xsl:when>
        <xsl:when test="$SrsAxisOrder = 'LonLat'">&lt;<xsl:value-of select="$SrsUri"/>&gt; POLYGON((<xsl:value-of select="$west"/><xsl:text> </xsl:text><xsl:value-of select="$north"/>,<xsl:value-of select="$east"/><xsl:text> </xsl:text><xsl:value-of select="$north"/>,<xsl:value-of select="$east"/><xsl:text> </xsl:text><xsl:value-of select="$south"/>,<xsl:value-of select="$west"/><xsl:text> </xsl:text><xsl:value-of select="$south"/>,<xsl:value-of select="$west"/><xsl:text> </xsl:text><xsl:value-of select="$north"/>))</xsl:when>
        <xsl:when test="$SrsAxisOrder = 'LatLon'">&lt;<xsl:value-of select="$SrsUri"/>&gt; POLYGON((<xsl:value-of select="$north"/><xsl:text> </xsl:text><xsl:value-of select="$west"/>,<xsl:value-of select="$north"/><xsl:text> </xsl:text><xsl:value-of select="$east"/>,<xsl:value-of select="$south"/><xsl:text> </xsl:text><xsl:value-of select="$east"/>,<xsl:value-of select="$south"/><xsl:text> </xsl:text><xsl:value-of select="$west"/>,<xsl:value-of select="$north"/><xsl:text> </xsl:text><xsl:value-of select="$west"/>))</xsl:when>
      </xsl:choose>
    </xsl:param>

    <!-- Bbox as GeoJSON -->

    <!-- Revised to ensure compliance with RFC 7946 -->
    <xsl:param name="GeoJSONLiteral">
      <xsl:choose>
        <!-- GeoJSON compliant with RFC 7946 -->
        <xsl:when test="$SrsUri = $Crs84Uri">{"type":"Polygon","coordinates":[[[<xsl:value-of select="$west"/><xsl:text>,</xsl:text><xsl:value-of select="$north"/>],[<xsl:value-of select="$east"/><xsl:text>,</xsl:text><xsl:value-of select="$north"/>],[<xsl:value-of select="$east"/><xsl:text>,</xsl:text><xsl:value-of select="$south"/>],[<xsl:value-of select="$west"/><xsl:text>,</xsl:text><xsl:value-of select="$south"/>],[<xsl:value-of select="$west"/><xsl:text>,</xsl:text><xsl:value-of select="$north"/>]]]}</xsl:when>
        <!-- GeoJSON compliant with original specification, where CRSs different from CRS84 were supported -->
        <xsl:otherwise>{"type":"Polygon","crs":{"type":"name","properties":{"name":"<xsl:value-of select="$SrsUrn"/>"}},"coordinates":[[[<xsl:value-of select="$west"/><xsl:text>,</xsl:text><xsl:value-of select="$north"/>],[<xsl:value-of select="$east"/><xsl:text>,</xsl:text><xsl:value-of select="$north"/>],[<xsl:value-of select="$east"/><xsl:text>,</xsl:text><xsl:value-of select="$south"/>],[<xsl:value-of select="$west"/><xsl:text>,</xsl:text><xsl:value-of select="$south"/>],[<xsl:value-of select="$west"/><xsl:text>,</xsl:text><xsl:value-of select="$north"/>]]]}</xsl:otherwise>
      </xsl:choose>
    </xsl:param>

    <dct:spatial rdf:parseType="Resource">
      <rdf:type rdf:resource="{$dct}Location"/>
      <!-- DEPRECATED: Mapping kept for backward compatibility with GeoDCAT-AP v1.* -->
      <xsl:if test="$include-deprecated = 'yes'">
        <!-- Recommended geometry encodings -->
        <locn:geometry rdf:datatype="{$gsp}wktLiteral"><xsl:value-of select="$WKTLiteral"/></locn:geometry>
        <locn:geometry rdf:datatype="{$gsp}gmlLiteral"><xsl:value-of select="$GMLLiteral"/></locn:geometry>
        <!-- Additional geometry encodings -->
        <!-- DEPRECATED: Mapping kept for backward compatibility with GeoDCAT-AP v1.* -->
        <locn:geometry rdf:datatype="{$geojsonMediaTypeUri}"><xsl:value-of select="$GeoJSONLiteral"/></locn:geometry>
        <!-- Mapping added for compliance with GeoDCAT-AP 2 -->
        <locn:geometry rdf:datatype="{$gsp}geoJSONLiteral"><xsl:value-of select="$GeoJSONLiteral"/></locn:geometry>
        <!--
                <locn:geometry rdf:datatype="{$dct}Box"><xsl:value-of select="$DCTBox"/></locn:geometry>
        -->
      </xsl:if>
      <!-- Mapping added for compliance with DCAT-AP 2 -->
      <!-- Recommended geometry encodings -->
      <dcat:bbox rdf:datatype="{$gsp}wktLiteral"><xsl:value-of select="$WKTLiteral"/></dcat:bbox>
      <dcat:bbox rdf:datatype="{$gsp}gmlLiteral"><xsl:value-of select="$GMLLiteral"/></dcat:bbox>
      <!-- Additional geometry encodings -->
      <!-- DEPRECATED: Mapping kept for backward compatibility with GeoDCAT-AP v1.* -->
      <xsl:if test="$include-deprecated = 'yes'">
        <dcat:bbox rdf:datatype="{$geojsonMediaTypeUri}"><xsl:value-of select="$GeoJSONLiteral"/></dcat:bbox>
      </xsl:if>
      <!-- Mapping added for compliance with GeoDCAT-AP 2 -->
      <dcat:bbox rdf:datatype="{$gsp}geoJSONLiteral"><xsl:value-of select="$GeoJSONLiteral"/></dcat:bbox>
    </dct:spatial>

  </xsl:template>

  <!-- Temporal extent -->

  <xsl:template mode="iso19139-to-dcatap" name="TemporalExtent" match="gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent">
    <!--
        <xsl:for-each select="gmd:extent/gml:TimeInstant|gmd:extent/gml:TimePeriod">
          <xsl:if test="local-name(.) = 'TimeInstant' or ( local-name(.) = 'TimePeriod' and gml:beginPosition and gml:endPosition )">
    -->
    <xsl:for-each select="gmd:extent/*[local-name() = 'TimeInstant']|gmd:extent/*[local-name() = 'TimePeriod']">
      <xsl:if test="local-name(.) = 'TimeInstant' or ( local-name(.) = 'TimePeriod' and *[local-name() = 'beginPosition'] and *[local-name() = 'endPosition'] )">
        <!--
                <xsl:variable name="dctperiod">
                  <xsl:choose>
                    <xsl:when test="local-name(.) = 'TimeInstant'">start=<xsl:value-of select="gml:timePosition"/>; end=<xsl:value-of select="gml:timePosition"/></xsl:when>
                    <xsl:otherwise>start=<xsl:value-of select="gml:beginPosition"/>; end=<xsl:value-of select="gml:endPosition"/></xsl:otherwise>
                  </xsl:choose>
                </xsl:variable>
        -->
        <xsl:variable name="dateStart">
          <xsl:choose>
            <!--
                        <xsl:when test="local-name(.) = 'TimeInstant'"><xsl:value-of select="gml:timePosition"/></xsl:when>
                        <xsl:otherwise><xsl:value-of select="gml:beginPosition"/></xsl:otherwise>
            -->
            <xsl:when test="local-name(.) = 'TimeInstant'"><xsl:value-of select="normalize-space(*[local-name() = 'timePosition'])"/></xsl:when>
            <xsl:otherwise><xsl:value-of select="normalize-space(*[local-name() = 'beginPosition'])"/></xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="dateStart-data-type">
          <xsl:call-template name="DateDataType">
            <xsl:with-param name="date" select="$dateStart"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="dateEnd">
          <xsl:choose>
            <!--
                        <xsl:when test="local-name(.) = 'TimeInstant'"><xsl:value-of select="gml:timePosition"/></xsl:when>
                        <xsl:otherwise><xsl:value-of select="gml:endPosition"/></xsl:otherwise>
            -->
            <xsl:when test="local-name(.) = 'TimeInstant'"><xsl:value-of select="normalize-space(*[local-name() = 'timePosition'])"/></xsl:when>
            <xsl:otherwise><xsl:value-of select="normalize-space(*[local-name() = 'endPosition'])"/></xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="dateEnd-data-type">
          <xsl:call-template name="DateDataType">
            <xsl:with-param name="date" select="$dateEnd"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:if test="$dateStart != '' or $dateEnd != ''">
          <dct:temporal>
            <dct:PeriodOfTime>
              <!-- DEPRECATED: Mapping kept for backward compatibility with GeoDCAT-AP v1.* -->
              <xsl:if test="$include-deprecated = 'yes'">
                <xsl:if test="$dateStart != ''">
                  <schema:startDate rdf:datatype="{$xsd}{$dateStart-data-type}"><xsl:value-of select="$dateStart"/></schema:startDate>
                </xsl:if>
                <xsl:if test="$dateEnd != ''">
                  <schema:endDate rdf:datatype="{$xsd}{$dateStart-data-type}"><xsl:value-of select="$dateEnd"/></schema:endDate>
                </xsl:if>
              </xsl:if>
              <!-- Mapping added for compliance with DCAT-AP 2 -->
              <xsl:if test="$dateStart != ''">
                <dcat:startDate rdf:datatype="{$xsd}{$dateEnd-data-type}"><xsl:value-of select="$dateStart"/></dcat:startDate>
              </xsl:if>
              <xsl:if test="$dateEnd != ''">
                <dcat:endDate rdf:datatype="{$xsd}{$dateEnd-data-type}"><xsl:value-of select="$dateEnd"/></dcat:endDate>
              </xsl:if>
            </dct:PeriodOfTime>
          </dct:temporal>
        </xsl:if>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <!-- Dates of publication, last revision, creation -->

  <xsl:template mode="iso19139-to-dcatap" name="ResourceDates" match="gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation">
    <xsl:apply-templates mode="iso19139-to-dcatap"
                         select="gmd:date/gmd:CI_Date"/>
  </xsl:template>

  <!-- Generic date template -->

  <xsl:template mode="iso19139-to-dcatap" name="Dates" match="gmd:date/gmd:CI_Date">
    <xsl:param name="date">
      <xsl:choose>
        <xsl:when test="gmd:date/gco:Date">
      <xsl:value-of select="normalize-space(gmd:date/gco:Date)"/>
        </xsl:when>
        <xsl:when test="gmd:date/gco:DateTime">
          <xsl:value-of select="normalize-space(gmd:date/gco:DateTime)"/>
        </xsl:when>
      </xsl:choose>
    </xsl:param>
    <xsl:param name="type">
      <xsl:value-of select="gmd:dateType/gmd:CI_DateTypeCode/@codeListValue"/>
    </xsl:param>
    <xsl:param name="data-type">
      <xsl:call-template name="DateDataType">
        <xsl:with-param name="date" select="$date"/>
      </xsl:call-template>
    </xsl:param>
    <xsl:if test="$date != ''">
      <xsl:choose>
        <xsl:when test="$type = 'publication'">
          <dct:issued rdf:datatype="{$xsd}{$data-type}">
            <xsl:value-of select="$date"/>
          </dct:issued>
        </xsl:when>
        <xsl:when test="$type = 'revision'">
          <dct:modified rdf:datatype="{$xsd}{$data-type}">
            <xsl:value-of select="$date"/>
          </dct:modified>
        </xsl:when>
        <xsl:when test="$type = 'creation' and $profile = $extended">
          <dct:created rdf:datatype="{$xsd}{$data-type}">
            <xsl:value-of select="$date"/>
          </dct:created>
        </xsl:when>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <!-- Generic date data type template -->

  <xsl:template name="DateDataType">
    <xsl:param name="date"/>
    <xsl:choose>
      <xsl:when test="string-length($date) = 4">
        <xsl:text>gYear</xsl:text>
      </xsl:when>
      <xsl:when test="string-length($date) = 10">
        <xsl:text>date</xsl:text>
      </xsl:when>
      <xsl:when test="string-length($date) &gt; 10">
        <xsl:text>dateTime</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>date</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Constraints related to access and use -->

  <xsl:template mode="iso19139-to-dcatap" name="ConstraintsRelatedToAccessAndUse" match="gmd:identificationInfo[1]/*/gmd:resourceConstraints/*">
    <xsl:param name="MetadataLanguage"/>
    <xsl:param name="LimitationsOnPublicAccess">
      <xsl:value-of select="gmd:MD_LegalConstraints/gmd:otherConstraints/*"/>
    </xsl:param>
    <xsl:param name="LimitationsOnPublicAccessCode">
      <xsl:value-of select="gmd:MD_LegalConstraints/gmd:otherConstraints/*/@codeListValue"/>
    </xsl:param>
    <xsl:param name="LimitationsOnPublicAccessURL">
      <xsl:value-of select="gmd:MD_LegalConstraints/gmd:otherConstraints/*/@xlink:href"/>
    </xsl:param>

    <!-- DEPRECATED: This mapping is not compliant with the 2017 edition of the INSPIRE Metadata
                     Technical Guidelines, where use conditions are specified instead by element
                     gmd:useConstraints (the use of gmd:useLimitation for this purpose has been
                     recognised as an error, as this element is rather about "fit for purpose").

                     The mapping has been however kept active for backward compatibility, waiting
                     for being revised (e.g., mapped to a usage note) or dropped.
    -->

    <xsl:if test="$include-deprecated = 'yes'">

      <xsl:for-each select="gmd:useLimitation">
        <xsl:choose>
          <!-- In case the rights/licence URL IS NOT provided -->
          <xsl:when test="normalize-space(gco:CharacterString) != ''">
            <dct:rights>
              <dct:RightsStatement>
                <dct:description xml:lang="{$MetadataLanguage}"><xsl:value-of select="normalize-space(gco:CharacterString)"/></dct:description>
                <xsl:call-template name="LocalisedString">
                  <xsl:with-param name="term">dct:description</xsl:with-param>
                </xsl:call-template>
              </dct:RightsStatement>
            </dct:rights>
            <!--
                      <dct:rights>
                        <dct:RightsStatement>
                          <rdfs:label xml:lang="{$MetadataLanguage}"><xsl:value-of select="normalize-space(gco:CharacterString)"/></rdfs:label>
                        </dct:RightsStatement>
                      </dct:rights>
            -->
          </xsl:when>
          <xsl:when test="gmd:MD_RestrictionCode">
            <xsl:variable name="use-limitation-code" select="normalize-space(@codeListValue)"/>
            <xsl:variable name="use-limitation-text">
              <xsl:choose>
                <xsl:when test="normalize-space(.) != ''">
                  <xsl:value-of select="normalize-space(.)"/>
                </xsl:when>
                <xsl:when test="$use-limitation-code != ''">
                  <xsl:value-of select="$use-limitation-code"/>
                </xsl:when>
              </xsl:choose>
            </xsl:variable>
            <dct:rights>
              <dct:RightsStatement>
                <xsl:if test="$use-limitation-code != ''">
                  <dct:identifier rdf:datatype="{$xsd}string"><xsl:value-of select="$use-limitation-code"/></dct:identifier>
                </xsl:if>
                <dct:description xml:lang="{$MetadataLanguage}"><xsl:value-of select="$use-limitation-text"/></dct:description>
                <xsl:call-template name="LocalisedString">
                  <xsl:with-param name="term">dct:description</xsl:with-param>
                </xsl:call-template>
              </dct:RightsStatement>
            </dct:rights>
            <!--
                      <dct:rights>
                        <dct:RightsStatement>
                          <rdfs:label xml:lang="{$MetadataLanguage}"><xsl:value-of select="normalize-space(gco:CharacterString)"/></rdfs:label>
                        </dct:RightsStatement>
                      </dct:rights>
            -->
          </xsl:when>
          <!-- In case the rights/licence URL IS provided -->
          <xsl:when test="gmx:Anchor/@xlink:href">
            <dct:license>
              <dct:LicenseDocument rdf:about="{gmx:Anchor/@xlink:href}"/>
            </dct:license>
            <!--
                      <dct:license>
                        <dct:LicenseDocument rdf:about="{gmx:Anchor/@xlink:href}">
                          <rdfs:label xml:lang="{$MetadataLanguage}"><xsl:value-of select="normalize-space(gmx:Anchor)"/></rdfs:label>
                        </dct:LicenseDocument>
                      </dct:license>
            -->
          </xsl:when>
        </xsl:choose>
      </xsl:for-each>

    </xsl:if>

    <!-- Mapping added for compliance with the 2017 edition of the INSPIRE Metadata Technical Guidelines -->
    <xsl:for-each select="gmd:otherConstraints[../gmd:useConstraints]">
      <xsl:choose>
        <!-- In case the rights/licence URL IS NOT provided -->
        <xsl:when test="normalize-space(gco:CharacterString) != ''">
          <dct:rights>
            <dct:RightsStatement>
              <dct:description xml:lang="{$MetadataLanguage}"><xsl:value-of select="normalize-space(gco:CharacterString)"/></dct:description>
              <xsl:call-template name="LocalisedString">
                <xsl:with-param name="term">dct:description</xsl:with-param>
              </xsl:call-template>
            </dct:RightsStatement>
          </dct:rights>
        </xsl:when>
        <xsl:when test="gmd:MD_RestrictionCode">
          <xsl:variable name="use-constraints-code" select="normalize-space(@codeListValue)"/>
          <xsl:variable name="use-constraints-text">
            <xsl:choose>
              <xsl:when test="normalize-space(.) != ''">
                <xsl:value-of select="normalize-space(.)"/>
              </xsl:when>
              <xsl:when test="$use-constraints-code != ''">
                <xsl:value-of select="$use-constraints-code"/>
              </xsl:when>
            </xsl:choose>
          </xsl:variable>
          <dct:rights>
            <dct:RightsStatement>
              <xsl:if test="$use-constraints-code != ''">
                <dct:identifier rdf:datatype="{$xsd}string"><xsl:value-of select="$use-constraints-code"/></dct:identifier>
              </xsl:if>
              <dct:description xml:lang="{$MetadataLanguage}"><xsl:value-of select="$use-constraints-text"/></dct:description>
              <xsl:call-template name="LocalisedString">
                <xsl:with-param name="term">dct:description</xsl:with-param>
              </xsl:call-template>
            </dct:RightsStatement>
          </dct:rights>
        </xsl:when>
        <!-- In case the rights/licence URL IS provided -->
        <xsl:when test="gmx:Anchor/@xlink:href">
          <dct:license>
            <dct:LicenseDocument rdf:about="{gmx:Anchor/@xlink:href}"/>
          </dct:license>
        </xsl:when>
      </xsl:choose>
    </xsl:for-each>

    <!-- Mapping revised for compliance with the 2017 edition of the INSPIRE Metadata Technical Guidelines -->
    <xsl:for-each select="gmd:otherConstraints[../gmd:accessConstraints]">
      <!-- Mapping moved to core profile for compliance with DCAT-AP 2 -->
      <!--
            <xsl:if test="$profile = $extended">
      -->
      <xsl:choose>
        <!-- In case the rights/licence URL IS NOT provided -->
        <xsl:when test="normalize-space(gco:CharacterString) != ''">
          <dct:rights>
            <dct:RightsStatement>
              <dct:description xml:lang="{$MetadataLanguage}"><xsl:value-of select="normalize-space(gco:CharacterString)"/></dct:description>
              <xsl:call-template name="LocalisedString">
                <xsl:with-param name="term">dct:description</xsl:with-param>
              </xsl:call-template>
            </dct:RightsStatement>
          </dct:rights>
        </xsl:when>
        <xsl:when test="gmd:MD_RestrictionCode">
          <xsl:variable name="access-constraints-code" select="normalize-space(@codeListValue)"/>
          <xsl:variable name="access-constraints-text">
            <xsl:choose>
              <xsl:when test="normalize-space(.) != ''">
                <xsl:value-of select="normalize-space(.)"/>
              </xsl:when>
              <xsl:when test="$access-constraints-code != ''">
                <xsl:value-of select="$access-constraints-code"/>
              </xsl:when>
            </xsl:choose>
          </xsl:variable>
          <dct:rights>
            <dct:RightsStatement>
              <xsl:if test="$access-constraints-code != ''">
                <dct:identifier rdf:datatype="{$xsd}string"><xsl:value-of select="$access-constraints-code"/></dct:identifier>
              </xsl:if>
              <dct:description xml:lang="{$MetadataLanguage}"><xsl:value-of select="$access-constraints-text"/></dct:description>
              <xsl:call-template name="LocalisedString">
                <xsl:with-param name="term">dct:description</xsl:with-param>
              </xsl:call-template>
            </dct:RightsStatement>
          </dct:rights>
        </xsl:when>
        <!-- In case the rights/licence URL IS provided -->
        <xsl:when test="gmx:Anchor/@xlink:href">
          <dct:accessRights>
            <dct:RightsStatement rdf:about="{gmx:Anchor/@xlink:href}"/>
          </dct:accessRights>
        </xsl:when>
      </xsl:choose>
      <!--
            </xsl:if>
      -->
    </xsl:for-each>
    <!--
        <xsl:for-each select="gmd:accessConstraints">
          <dct:accessRights rdf:resource="{$MD_RestrictionCode}_{gmd:MD_RestrictionCode/@codeListValue}"/>
        </xsl:for-each>
        <xsl:for-each select="gmd:classification">
          <dct:accessRights rdf:resource="{$MD_ClassificationCode}_{gmd:MD_ClassificationCode/@codeListValue}"/>
        </xsl:for-each>
    -->
  </xsl:template>

  <!-- Keyword -->

  <xsl:template mode="iso19139-to-dcatap" name="Keyword" match="gmd:identificationInfo/*/gmd:descriptiveKeywords/gmd:MD_Keywords">
    <xsl:param name="MetadataLanguage"/>
    <xsl:param name="ResourceType"/>
    <xsl:param name="ServiceType"/>
    <xsl:param name="OriginatingControlledVocabularyURI" select="normalize-space(gmd:thesaurusName/gmd:CI_Citation/gmd:title/gmx:Anchor/@xlink:href)"/>
    <xsl:param name="OriginatingControlledVocabulary">
      <!--
            <xsl:for-each select="gmd:thesaurusName/gmd:CI_Citation">
              <dct:title xml:lang="{$MetadataLanguage}">
                <xsl:value-of select="gmd:title/gco:CharacterString"/>
              </dct:title>
              <xsl:apply-templates select="gmd:date/gmd:CI_Date"/>
            </xsl:for-each>
      -->
      <xsl:for-each select="gmd:thesaurusName/gmd:CI_Citation">
        <xsl:for-each select="gmd:title">
          <dct:title xml:lang="{$MetadataLanguage}">
            <xsl:value-of select="normalize-space(*[self::gco:CharacterString|self::gmx:Anchor])"/>
          </dct:title>
          <xsl:call-template name="LocalisedString">
            <xsl:with-param name="term">dct:title</xsl:with-param>
          </xsl:call-template>
        </xsl:for-each>
        <xsl:if test="$profile = $extended">
          <xsl:apply-templates mode="iso19139-to-dcatap"
                               select="gmd:date/gmd:CI_Date"/>
        </xsl:if>
      </xsl:for-each>
    </xsl:param>
    <xsl:param name="ConceptScheme">
      <xsl:choose>
        <xsl:when test="$OriginatingControlledVocabularyURI != ''">
          <skos:ConceptScheme rdf:about="{$OriginatingControlledVocabularyURI}">
            <xsl:copy-of select="$OriginatingControlledVocabulary"/>
          </skos:ConceptScheme>
        </xsl:when>
        <xsl:otherwise>
          <skos:ConceptScheme>
            <xsl:copy-of select="$OriginatingControlledVocabulary"/>
          </skos:ConceptScheme>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <xsl:param name="inScheme">
      <xsl:choose>
        <xsl:when test="$OriginatingControlledVocabularyURI != ''">
          <skos:inScheme rdf:resource="{$OriginatingControlledVocabularyURI}"/>
        </xsl:when>
        <xsl:otherwise>
          <skos:inScheme>
            <xsl:copy-of select="$ConceptScheme"/>
          </skos:inScheme>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <xsl:for-each select="gmd:keyword[normalize-space(gco:CharacterString) != '' or normalize-space(gmx:Anchor/@xlink:href) != '']">
      <xsl:variable name="lckw" select="translate(*[self::gco:CharacterString|self::gmx:Anchor],$uppercase,$lowercase)"/>
      <xsl:choose>
        <xsl:when test="normalize-space($OriginatingControlledVocabulary) = '' and not( gmx:Anchor/@xlink:href and ( starts-with(gmx:Anchor/@xlink:href, 'http://') or starts-with(gmx:Anchor/@xlink:href, 'https://') ) )">
          <xsl:choose>
            <xsl:when test="$ResourceType = 'service'">
              <!-- Mapping moved to core profile for compliance with DCAT-AP 2 -->
              <!-- Mapping added for compliance with DCAT-AP 2 -->
              <dcat:keyword xml:lang="{$MetadataLanguage}">
                <xsl:value-of select="normalize-space(*[self::gco:CharacterString|self::gmx:Anchor])"/>
              </dcat:keyword>
              <xsl:call-template name="LocalisedString">
                <xsl:with-param name="term">dcat:keyword</xsl:with-param>
              </xsl:call-template>
              <xsl:if test="$profile = $extended">
                <!-- DEPRECATED: Mapping kept for backward compatibility with GeoDCAT-AP v1.* -->
                <xsl:if test="$include-deprecated = 'yes'">
                  <dc:subject xml:lang="{$MetadataLanguage}">
                    <xsl:value-of select="normalize-space(*[self::gco:CharacterString|self::gmx:Anchor])"/>
                  </dc:subject>
                  <xsl:call-template name="LocalisedString">
                    <xsl:with-param name="term">dc:subject</xsl:with-param>
                  </xsl:call-template>
                </xsl:if>
              </xsl:if>
            </xsl:when>
            <xsl:otherwise>
              <dcat:keyword xml:lang="{$MetadataLanguage}">
                <xsl:value-of select="normalize-space(*[self::gco:CharacterString|self::gmx:Anchor])"/>
              </dcat:keyword>
              <xsl:call-template name="LocalisedString">
                <xsl:with-param name="term">dcat:keyword</xsl:with-param>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <xsl:choose>
            <!-- In case the concept's URI is NOT provided -->
            <xsl:when test="gco:CharacterString">
              <xsl:choose>
                <xsl:when test="$ResourceType != 'service'">
                  <dcat:theme rdf:parseType="Resource">
                    <rdf:type rdf:resource="{$skos}Concept"/>
                    <skos:prefLabel xml:lang="{$MetadataLanguage}">
                      <xsl:value-of select="normalize-space(gco:CharacterString)"/>
                    </skos:prefLabel>
                    <xsl:call-template name="LocalisedString">
                      <xsl:with-param name="term">skos:prefLabel</xsl:with-param>
                    </xsl:call-template>
                    <xsl:copy-of select="$inScheme"/>
                    <!--
                                        <skos:inScheme>
                                          <skos:ConceptScheme>
                                            <xsl:copy-of select="$OriginatingControlledVocabulary"/>
                                          </skos:ConceptScheme>
                                        </skos:inScheme>
                    -->
                  </dcat:theme>
                </xsl:when>
                <xsl:otherwise>
                  <!-- Mapping moved to core profile for compliance with DCAT-AP 2 -->
                  <!-- Mapping added for compliance with DCAT-AP 2 -->
                  <dcat:theme rdf:parseType="Resource">
                    <rdf:type rdf:resource="{$skos}Concept"/>
                    <skos:prefLabel xml:lang="{$MetadataLanguage}">
                      <xsl:value-of select="normalize-space(gco:CharacterString)"/>
                    </skos:prefLabel>
                    <xsl:call-template name="LocalisedString">
                      <xsl:with-param name="term">skos:prefLabel</xsl:with-param>
                    </xsl:call-template>
                    <xsl:copy-of select="$inScheme"/>
                    <!--
                                        <skos:inScheme>
                                          <skos:ConceptScheme>
                                            <xsl:copy-of select="$OriginatingControlledVocabulary"/>
                                          </skos:ConceptScheme>
                                        </skos:inScheme>
                    -->
                  </dcat:theme>
                  <xsl:if test="$profile = $extended">
                    <!-- DEPRECATED: Mapping kept for backward compatibility with GeoDCAT-AP v1.* -->
                    <xsl:if test="$include-deprecated = 'yes'">
                      <dct:subject rdf:parseType="Resource">
                        <rdf:type rdf:resource="{$skos}Concept"/>
                        <skos:prefLabel xml:lang="{$MetadataLanguage}">
                          <xsl:value-of select="normalize-space(gco:CharacterString)"/>
                        </skos:prefLabel>
                        <xsl:call-template name="LocalisedString">
                          <xsl:with-param name="term">skos:prefLabel</xsl:with-param>
                        </xsl:call-template>
                        <xsl:copy-of select="$inScheme"/>
                        <!--
                                                <skos:inScheme>
                                                  <skos:ConceptScheme>
                                                    <xsl:copy-of select="$OriginatingControlledVocabulary"/>
                                                  </skos:ConceptScheme>
                                                </skos:inScheme>
                        -->
                      </dct:subject>
                    </xsl:if>
                  </xsl:if>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:when>
            <!-- In case the concept's URI is provided -->
            <xsl:when test="gmx:Anchor/@xlink:href">
              <xsl:choose>
                <xsl:when test="$ResourceType != 'service'">
                  <dcat:theme rdf:resource="{gmx:Anchor/@xlink:href}"/>
                  <!--
                                    <skos:Concept rdf:about="{gmx:Anchor/@xlink:href}">
                                      <skos:prefLabel xml:lang="{$MetadataLanguage}">
                                        <xsl:value-of select="gmx:Anchor"/>
                                      </skos:prefLabel>
                                      <skos:inScheme>
                                        <skos:ConceptScheme>
                                          <xsl:copy-of select="$OriginatingControlledVocabulary"/>
                                        </skos:ConceptScheme>
                                      </skos:inScheme>
                                    </skos:Concept>
                  -->
                </xsl:when>
                <xsl:otherwise>
                  <!-- Mapping moved to core profile for compliance with DCAT-AP 2 -->
                  <!-- Mapping added for compliance with DCAT-AP 2 -->
                  <dcat:theme rdf:resource="{gmx:Anchor/@xlink:href}"/>
                  <xsl:if test="$profile = $extended">
                    <!-- DEPRECATED: Mapping kept for backward compatibility with GeoDCAT-AP v1.* -->
                    <xsl:if test="$include-deprecated = 'yes'">
                      <dct:subject rdf:resource="{gmx:Anchor/@xlink:href}"/>
                    </xsl:if>
                  </xsl:if>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:when>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <!-- Topic category -->

  <xsl:template mode="iso19139-to-dcatap" name="TopicCategory" match="gmd:identificationInfo/*/gmd:topicCategory">
    <xsl:param name="TopicCategory"><xsl:value-of select="normalize-space(gmd:MD_TopicCategoryCode)"/></xsl:param>
    <xsl:if test="$TopicCategory != ''">
      <geodcatap:topicCategory rdf:resource="{$TopicCategoryCodelistUri}/{$TopicCategory}"/>
    </xsl:if>
  </xsl:template>

  <!-- Spatial resolution (unstable - to be replaced with a standard-based solution, when available) -->

  <xsl:template mode="iso19139-to-dcatap" name="SpatialResolution" match="gmd:identificationInfo/*/gmd:spatialResolution/gmd:MD_Resolution">
    <!-- dcat:granularity is deprecated -->
    <!--
        <xsl:for-each select="gmd:distance/gco:Distance">
          <dcat:granularity rdf:datatype="{$xsd}string"><xsl:value-of select="."/> <xsl:value-of select="@uom"/></dcat:granularity>
        </xsl:for-each>
        <xsl:for-each select="gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator">
          <dcat:granularity rdf:datatype="{$xsd}string">1/<xsl:value-of select="gco:Integer"/></dcat:granularity>
        </xsl:for-each>
    -->
    <xsl:for-each select="gmd:distance/gco:Distance">
      <xsl:variable name="UoM">
        <xsl:choose>
          <xsl:when test="@uom = 'EPSG::9001' or @uom = 'urn:ogc:def:uom:EPSG::9001' or @uom = 'urn:ogc:def:uom:UCUM::m' or @uom = 'urn:ogc:def:uom:OGC::m'">
            <xsl:value-of select="concat('m',' (',@uom,')')"/>
          </xsl:when>
          <xsl:when test="@uom = 'EPSG::9002' or @uom = 'urn:ogc:def:uom:EPSG::9002' or @uom = 'urn:ogc:def:uom:UCUM::[ft_i]' or @uom = 'urn:ogc:def:uom:OGC::[ft_i]'">
            <xsl:value-of select="concat('ft',' (',@uom,')')"/>
          </xsl:when>
          <xsl:when test="starts-with(@uom, 'http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/uom/ML_gmxUom.xml#')">
            <xsl:value-of select="concat(substring-after(@uom, 'http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/uom/ML_gmxUom.xml#'),' (',@uom,')')"/>
          </xsl:when>
          <!-- To be completed -->
          <xsl:otherwise>
            <xsl:value-of select="normalize-space(@uom)"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <!--
            <xsl:if test="$profile = $extended">
              <rdfs:comment xml:lang="en">Spatial resolution (distance): <xsl:value-of select="."/>&#160;<xsl:value-of select="$UoM"/></rdfs:comment>
            </xsl:if>
      -->
      <!-- Mapping moved to core profile for compliance with DCAT-AP 2 -->
      <!-- Mapping added for compliance with DCAT-AP 2 -->
      <xsl:choose>
        <xsl:when test="($UoM = 'm' or starts-with($UoM, 'm ')) and number(.) = number(.)">
          <dcat:spatialResolutionInMeters rdf:datatype="{$xsd}decimal">
            <xsl:value-of select="."/>
          </dcat:spatialResolutionInMeters>
          <!-- Mapping added for compliance with GeoDCAT-AP 2 -->
          <xsl:if test="$profile = 'extended'">
            <dqv:hasQualityMeasurement>
              <dqv:QualityMeasurement>
                <dqv:isMeasurementOf>
                  <dqv:Metric rdf:about="{$geodcatap}spatialResolutionAsDistance"/>
                </dqv:isMeasurementOf>
                <dqv:value rdf:datatype="{$xsd}decimal"><xsl:value-of select="."/></dqv:value>
                <sdmx-attribute:unitMeasure rdf:resource="{$uom-m}"/>
              </dqv:QualityMeasurement>
            </dqv:hasQualityMeasurement>
          </xsl:if>
        </xsl:when>
        <xsl:when test="($UoM = 'km' or starts-with($UoM, 'km ')) and number(.) = number(.)">
          <dcat:spatialResolutionInMeters rdf:datatype="{$xsd}decimal">
            <xsl:value-of select="(. * 1000)"/>
          </dcat:spatialResolutionInMeters>
          <!-- Mapping added for compliance with GeoDCAT-AP 2 -->
          <xsl:if test="$profile = 'extended'">
            <dqv:hasQualityMeasurement>
              <dqv:QualityMeasurement>
                <dqv:isMeasurementOf>
                  <dqv:Metric rdf:about="{$geodcatap}spatialResolutionAsDistance"/>
                </dqv:isMeasurementOf>
                <dqv:value rdf:datatype="{$xsd}decimal"><xsl:value-of select="."/></dqv:value>
                <sdmx-attribute:unitMeasure rdf:resource="{$uom-km}"/>
              </dqv:QualityMeasurement>
            </dqv:hasQualityMeasurement>
          </xsl:if>
        </xsl:when>
        <xsl:when test="($UoM = 'ft' or starts-with($UoM, 'ft ')) and number(.) = number(.)">
          <dcat:spatialResolutionInMeters rdf:datatype="{$xsd}decimal">
            <xsl:value-of select="(. * 0.3048)"/>
          </dcat:spatialResolutionInMeters>
          <!-- Mapping added for compliance with GeoDCAT-AP 2 -->
          <xsl:if test="$profile = 'extended'">
            <dqv:hasQualityMeasurement>
              <dqv:QualityMeasurement>
                <dqv:isMeasurementOf>
                  <dqv:Metric rdf:about="{$geodcatap}spatialResolutionAsDistance"/>
                </dqv:isMeasurementOf>
                <dqv:value rdf:datatype="{$xsd}decimal"><xsl:value-of select="."/></dqv:value>
                <sdmx-attribute:unitMeasure rdf:resource="{$uom-ft}"/>
              </dqv:QualityMeasurement>
            </dqv:hasQualityMeasurement>
          </xsl:if>
        </xsl:when>
        <xsl:otherwise>
          <xsl:if test="$profile = $extended">
            <rdfs:comment xml:lang="en">
              Spatial resolution (distance): <xsl:value-of select="."/>&#160;<xsl:value-of select="$UoM"/>
            </rdfs:comment>
          </xsl:if>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
    <xsl:if test="$profile = $extended">
      <xsl:for-each select="gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator">
        <!--
                <rdfs:comment xml:lang="en">Spatial resolution (equivalent scale): 1:<xsl:value-of select="gco:Integer"/></rdfs:comment>
        -->
        <xsl:choose>
          <!-- Mapping added for compliance with GeoDCAT-AP 2 -->
          <xsl:when test="number(gco:Integer) = number(gco:Integer)">
            <dqv:hasQualityMeasurement>
              <dqv:QualityMeasurement>
                <dqv:isMeasurementOf>
                  <dqv:Metric rdf:about="{$geodcatap}spatialResolutionAsScale"/>
                </dqv:isMeasurementOf>
                <dqv:value rdf:datatype="{$xsd}decimal">
                  <xsl:value-of select="format-number((1 div gco:Integer), '0.########################')"/>
                </dqv:value>
              </dqv:QualityMeasurement>
            </dqv:hasQualityMeasurement>
          </xsl:when>
          <xsl:otherwise>
            <rdfs:comment xml:lang="en">
              Spatial resolution (equivalent scale): <xsl:value-of select="gco:Integer"/>
            </rdfs:comment>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>

  <!-- Character encoding -->

  <xsl:template mode="iso19139-to-dcatap" name="CharacterEncoding" match="gmd:characterSet/gmd:MD_CharacterSetCode">
    <xsl:variable name="CharSetCode">
      <xsl:choose>
        <xsl:when test="@codeListValue = 'ucs2'">
          <xsl:text>ISO-10646-UCS-2</xsl:text>
        </xsl:when>
        <xsl:when test="@codeListValue = 'ucs4'">
          <xsl:text>ISO-10646-UCS-4</xsl:text>
        </xsl:when>
        <xsl:when test="@codeListValue = 'utf7'">
          <xsl:text>UTF-7</xsl:text>
        </xsl:when>
        <xsl:when test="@codeListValue = 'utf8'">
          <xsl:text>UTF-8</xsl:text>
        </xsl:when>
        <xsl:when test="@codeListValue = 'utf16'">
          <xsl:text>UTF-16</xsl:text>
        </xsl:when>
        <xsl:when test="@codeListValue = '8859part1'">
          <xsl:text>ISO-8859-1</xsl:text>
        </xsl:when>
        <xsl:when test="@codeListValue = '8859part2'">
          <xsl:text>ISO-8859-2</xsl:text>
        </xsl:when>
        <xsl:when test="@codeListValue = '8859part3'">
          <xsl:text>ISO-8859-3</xsl:text>
        </xsl:when>
        <xsl:when test="@codeListValue = '8859part4'">
          <xsl:text>ISO-8859-4</xsl:text>
        </xsl:when>
        <xsl:when test="@codeListValue = '8859part5'">
          <xsl:text>ISO-8859-5</xsl:text>
        </xsl:when>
        <xsl:when test="@codeListValue = '8859part6'">
          <xsl:text>ISO-8859-6</xsl:text>
        </xsl:when>
        <xsl:when test="@codeListValue = '8859part7'">
          <xsl:text>ISO-8859-7</xsl:text>
        </xsl:when>
        <xsl:when test="@codeListValue = '8859part8'">
          <xsl:text>ISO-8859-8</xsl:text>
        </xsl:when>
        <xsl:when test="@codeListValue = '8859part9'">
          <xsl:text>ISO-8859-9</xsl:text>
        </xsl:when>
        <xsl:when test="@codeListValue = '8859part10'">
          <xsl:text>ISO-8859-10</xsl:text>
        </xsl:when>
        <xsl:when test="@codeListValue = '8859part11'">
          <xsl:text>ISO-8859-11</xsl:text>
        </xsl:when>
        <xsl:when test="@codeListValue = '8859part12'">
          <xsl:text>ISO-8859-12</xsl:text>
        </xsl:when>
        <xsl:when test="@codeListValue = '8859part13'">
          <xsl:text>ISO-8859-13</xsl:text>
        </xsl:when>
        <xsl:when test="@codeListValue = '8859part14'">
          <xsl:text>ISO-8859-14</xsl:text>
        </xsl:when>
        <xsl:when test="@codeListValue = '8859part15'">
          <xsl:text>ISO-8859-15</xsl:text>
        </xsl:when>
        <xsl:when test="@codeListValue = '8859part16'">
          <xsl:text>ISO-8859-16</xsl:text>
        </xsl:when>
        <!-- Mapping to be verified: multiple candidates are available in the IANA register for jis -->
        <xsl:when test="@codeListValue = 'jis'">
          <xsl:text>JIS_Encoding</xsl:text>
        </xsl:when>
        <xsl:when test="@codeListValue = 'shiftJIS'">
          <xsl:text>Shift_JIS</xsl:text>
        </xsl:when>
        <xsl:when test="@codeListValue = 'eucJP'">
          <xsl:text>EUC-JP</xsl:text>
        </xsl:when>
        <xsl:when test="@codeListValue = 'usAscii'">
          <xsl:text>US-ASCII</xsl:text>
        </xsl:when>
        <!-- Mapping to be verified: multiple candidates are available in the IANA register ebcdic  -->
        <xsl:when test="@codeListValue = 'ebcdic'">
          <xsl:text>IBM037</xsl:text>
        </xsl:when>
        <xsl:when test="@codeListValue = 'eucKR'">
          <xsl:text>EUC-KR</xsl:text>
        </xsl:when>
        <xsl:when test="@codeListValue = 'big5'">
          <xsl:text>Big5</xsl:text>
        </xsl:when>
        <xsl:when test="@codeListValue = 'GB2312'">
          <xsl:text>GB2312</xsl:text>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>
    <cnt:characterEncoding rdf:datatype="{$xsd}string"><xsl:value-of select="$CharSetCode"/></cnt:characterEncoding>
    <!--
        <cnt:characterEncoding rdf:datatype="{$xsd}string"><xsl:value-of select="@codeListValue"/></cnt:characterEncoding>
    -->
  </xsl:template>

  <!-- Encoding -->

  <xsl:template mode="iso19139-to-dcatap" name="Encoding" match="gmd:distributionFormat/gmd:MD_Format/gmd:name/*">
    <xsl:param name="format-label">
      <xsl:value-of select="normalize-space(.)"/>
    </xsl:param>
    <xsl:param name="format-uri">
      <xsl:choose>
        <xsl:when test="@xlink:href and @xlink:href != ''">
          <xsl:value-of select="@xlink:href"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="EncodingLabelToUri">
            <xsl:with-param name="label" select="normalize-space(.)"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <xsl:param name="media-type">
      <xsl:choose>
        <xsl:when test="$format-uri != ''">
          <dct:MediaType rdf:about="{$format-uri}"/>
        </xsl:when>
        <xsl:when test="$format-label != ''">
          <dct:MediaType>
            <rdfs:label><xsl:value-of select="$format-label"/></rdfs:label>
          </dct:MediaType>
        </xsl:when>
      </xsl:choose>
    </xsl:param>
    <xsl:choose>
      <xsl:when test="starts-with($format-uri,$iana-mt)">
        <dcat:mediaType>
          <xsl:copy-of select="$media-type"/>
        </dcat:mediaType>
      </xsl:when>
      <xsl:otherwise>
        <dct:format>
          <xsl:copy-of select="$media-type"/>
        </dct:format>
      </xsl:otherwise>
    </xsl:choose>
    <!--
        <dct:format>
          <rdf:Description rdf:about="{$format-uri}">
            <rdf:type rdf:resource="{$dct}MediaType"/>
            <xsl:if test="$format-uri = concat($opft,'OP_DATPRO') and $format-label != ''">
              <rdfs:label><xsl:value-of select="."/></rdfs:label>
            </xsl:if>
          </rdf:Description>
        </dct:format>
    -->
    <!--
        <xsl:choose>
          <xsl:when test="@xlink:href and @xlink:href != ''">
            <dct:format>
              <dct:MediaType rdf:about="{@xlink:href}"/>
            </dct:format>
          </xsl:when>
          <xsl:otherwise>
            <dct:format rdf:parseType="Resource">
              <rdf:type rdf:resource="{$dct}MediaType"/>
              <rdfs:label><xsl:value-of select="."/></rdfs:label>
            </dct:format>
          </xsl:otherwise>
        </xsl:choose>
    -->
  </xsl:template>

  <!-- Encoding: Label to URI -->

  <!-- CAVEAT: This template attempts to map textual description
       of a distribution encoding, based on the most frequently
       used labels from the European Data Portal. -->

  <xsl:template name="EncodingLabelToUri">
    <xsl:param name="label"/>
    <xsl:param name="norm-label">
      <xsl:choose>
        <xsl:when test="starts-with(normalize-space($label),$opft)">
          <xsl:value-of select="translate(substring-after(normalize-space($label),$opft),$uppercase,$lowercase)"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="translate(normalize-space($label),$uppercase,$lowercase)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <xsl:choose>
      <xsl:when test="$norm-label = 'aaigrid'">
        <xsl:value-of select="concat($opft,'GRID_ASCII')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'aig'">
        <xsl:value-of select="concat($opft,'GRID')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'atom'">
        <xsl:value-of select="concat($opft,'ATOM')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'csv'">
        <xsl:value-of select="concat($opft,'CSV')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'csw'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($opft,'XML')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'dbf'">
        <xsl:value-of select="concat($opft,'DBF')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'dgn'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($opft,'BIN')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'djvu'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($iana-mt,'image/vn.djvu')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'doc'">
        <xsl:value-of select="concat($opft,'DOC')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'docx'">
        <xsl:value-of select="concat($opft,'DOCX')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'dxf'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($iana-mt,'image/vn.dxf')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'dwg'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($iana-mt,'image/vn.dwg')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'ecw'">
        <xsl:value-of select="concat($opft,'ECW')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'ecwp'">
        <xsl:value-of select="concat($opft,'ECW')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'elp'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($opft,'EXE')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'epub'">
        <xsl:value-of select="concat($opft,'EPUB')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'fgeo'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($opft,'GDB')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'gdb'">
        <xsl:value-of select="concat($opft,'GDB')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'geojson'">
        <xsl:value-of select="concat($opft,'GEOJSON')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'geopackage'">
        <xsl:value-of select="concat($opft,'GPKG')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'georss'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($opft,'RSS')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'geotiff'">
        <xsl:value-of select="concat($opft,'TIFF')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'gif'">
        <xsl:value-of select="concat($opft,'GIF')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'gml'">
        <xsl:value-of select="concat($opft,'GML')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'gmz'">
        <xsl:value-of select="concat($opft,'GMZ')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'gpkg'">
        <xsl:value-of select="concat($opft,'GPKG')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'gpx'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($opft,'XML')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'grid'">
        <xsl:value-of select="concat($opft,'GRID')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'grid_ascii'">
        <xsl:value-of select="concat($opft,'GRID_ASCII')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'gtfs'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($opft,'CSV')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'gtiff'">
        <xsl:value-of select="concat($opft,'TIFF')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'gzip'">
        <xsl:value-of select="concat($opft,'GZIP')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'html'">
        <xsl:value-of select="concat($opft,'HTML')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'jpeg'">
        <xsl:value-of select="concat($opft,'JPEG')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'jpg'">
        <xsl:value-of select="concat($opft,'JPEG')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'json'">
        <xsl:value-of select="concat($opft,'JSON')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'json-ld'">
        <xsl:value-of select="concat($opft,'JSON_LD')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'json_ld'">
        <xsl:value-of select="concat($opft,'JSON_LD')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'jsonld'">
        <xsl:value-of select="concat($opft,'JSON_LD')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'kml'">
        <xsl:value-of select="concat($opft,'KML')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'kmz'">
        <xsl:value-of select="concat($opft,'KMZ')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'las'">
        <xsl:value-of select="concat($opft,'LAS')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'laz'">
        <xsl:value-of select="concat($opft,'LAZ')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'marc'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($iana-mt,'application/marc')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'mdb'">
        <xsl:value-of select="concat($opft,'MDB')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'mxd'">
        <xsl:value-of select="concat($opft,'MXD')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'n-triples'">
        <xsl:value-of select="concat($opft,'RDF_N_TRIPLES')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'n3'">
        <xsl:value-of select="concat($opft,'N3')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'netcdf'">
        <xsl:value-of select="concat($opft,'NETCDF')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'ods'">
        <xsl:value-of select="concat($opft,'ODS')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'odt'">
        <xsl:value-of select="concat($opft,'ODT')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'ogc:csw'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($opft,'XML')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'ogc:sos'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($opft,'XML')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'ogc:wcs'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($opft,'TIFF')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'ogc:wfs'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($opft,'GML')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'ogc:wfs-g'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($opft,'GML')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'ogc:wmc'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($opft,'XML')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'ogc:wms'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($opft,'PNG')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'ogc:wmts'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($opft,'PNG')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'ogc:wps'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($opft,'GML')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'oracledump'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($inspire-mt,'application/x-oracledump')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'pc-axis'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($opft,'TXT')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'pdf'">
        <xsl:value-of select="concat($opft,'PDF')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'pgeo'">
        <xsl:value-of select="concat($opft,'MDB')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'png'">
        <xsl:value-of select="concat($opft,'PNG')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'rar'">
        <xsl:value-of select="concat($opft,'RAR')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'rdf/xml'">
        <xsl:value-of select="concat($opft,'RDF_XML')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'rdf-n3'">
        <xsl:value-of select="concat($opft,'N3')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'rdf-turtle'">
        <xsl:value-of select="concat($opft,'RDF_TURTLE')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'rdf-xml'">
        <xsl:value-of select="concat($opft,'RDF_XML')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'rdf_n_triples'">
        <xsl:value-of select="concat($opft,'RDF_N_TRIPLES')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'rdf_n3'">
        <xsl:value-of select="concat($opft,'N3')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'rdf_turtle'">
        <xsl:value-of select="concat($opft,'RDF_TURTLE')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'rdf_xml'">
        <xsl:value-of select="concat($opft,'RDF_XML')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'rss'">
        <xsl:value-of select="concat($opft,'RSS')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'rtf'">
        <xsl:value-of select="concat($opft,'RTF')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'scorm'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($opft,'ZIP')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'shp'">
        <xsl:value-of select="concat($opft,'SHP')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'sos'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($opft,'XML')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'spatialite'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($iana-mt,'application/vnd.sqlite3')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'sqlite'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($iana-mt,'application/vnd.sqlite3')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'sqlite3'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($iana-mt,'application/vnd.sqlite3')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'svg'">
        <xsl:value-of select="concat($opft,'SVG')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'text'">
        <xsl:value-of select="concat($opft,'TXT')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'tiff'">
        <xsl:value-of select="concat($opft,'TIFF')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'tmx'">
        <xsl:value-of select="concat($opft,'TMX')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'tsv'">
        <xsl:value-of select="concat($opft,'TSV')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'ttl'">
        <xsl:value-of select="concat($opft,'RDF_TURTLE')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'turtle'">
        <xsl:value-of select="concat($opft,'RDF_TURTLE')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'txt'">
        <xsl:value-of select="concat($opft,'TXT')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'vcard-json'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($opft,'JSON')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'vcard-xml'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($opft,'XML')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'xbrl'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($opft,'XML')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'xhtml'">
        <xsl:value-of select="concat($opft,'XHTML')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'xls'">
        <xsl:value-of select="concat($opft,'XLS')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'xlsx'">
        <xsl:value-of select="concat($opft,'XLSX')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'xml'">
        <xsl:value-of select="concat($opft,'XML')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'wcs'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($opft,'TIFF')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'wfs'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($opft,'GML')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'wfs-g'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($opft,'GML')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'wmc'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($opft,'XML')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'wms'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($opft,'PNG')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'wmts'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($opft,'PNG')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'wps'">
        <!-- PROVISIONAL -->
        <xsl:value-of select="concat($opft,'GML')"/>
      </xsl:when>
      <xsl:when test="$norm-label = 'zip'">
        <xsl:value-of select="concat($opft,'ZIP')"/>
      </xsl:when>
      <xsl:otherwise>
        <!--
                <xsl:value-of select="concat($opft,'OP_DATPRO')"/>
        -->
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Maintenance information -->

  <xsl:template mode="iso19139-to-dcatap" name="MaintenanceInformation" match="gmd:MD_MaintenanceInformation/gmd:maintenanceAndUpdateFrequency/gmd:MD_MaintenanceFrequencyCode">
    <!-- The following parameter maps frequency codes used in ISO 19139 metadata to the corresponding ones of the Dublin Core Collection Description Frequency Vocabulary (when available). -->
    <xsl:param name="FrequencyCodeURI">
      <xsl:if test="@codeListValue != ''">
        <xsl:choose>
          <xsl:when test="@codeListValue = 'continual'">
            <!--  DC Freq voc
                         <xsl:value-of select="concat($cldFrequency,'continuous')"/>
            -->
            <xsl:value-of select="concat($opfq,'CONT')"/>
          </xsl:when>
          <xsl:when test="@codeListValue = 'daily'">
            <!--  DC Freq voc
                        <xsl:value-of select="concat($cldFrequency,'daily')"/>
            -->
            <xsl:value-of select="concat($opfq,'DAILY')"/>
          </xsl:when>
          <xsl:when test="@codeListValue = 'weekly'">
            <!--  DC Freq voc
                        <xsl:value-of select="concat($cldFrequency,'weekly')"/>
            -->
            <xsl:value-of select="concat($opfq,'WEEKLY')"/>
          </xsl:when>
          <xsl:when test="@codeListValue = 'fortnightly'">
            <!--  DC Freq voc
                        <xsl:value-of select="concat($cldFrequency,'biweekly')"/>
            -->
            <xsl:value-of select="concat($opfq,'BIWEEKLY')"/>
          </xsl:when>
          <xsl:when test="@codeListValue = 'monthly'">
            <!--  DC Freq voc
                        <xsl:value-of select="concat($cldFrequency,'monthly')"/>
            -->
            <xsl:value-of select="concat($opfq,'MONTHLY')"/>
          </xsl:when>
          <xsl:when test="@codeListValue = 'quarterly'">
            <!--  DC Freq voc
                        <xsl:value-of select="concat($cldFrequency,'quarterly')"/>
            -->
            <xsl:value-of select="concat($opfq,'QUARTERLY')"/>
          </xsl:when>
          <xsl:when test="@codeListValue = 'biannually'">
            <!--  DC Freq voc
                        <xsl:value-of select="concat($cldFrequency,'semiannual')"/>
            -->
            <xsl:value-of select="concat($opfq,'ANNUAL_2')"/>
          </xsl:when>
          <xsl:when test="@codeListValue = 'annually'">
            <!--  DC Freq voc
                        <xsl:value-of select="concat($cldFrequency,'annual')"/>
            -->
            <xsl:value-of select="concat($opfq,'ANNUAL')"/>
          </xsl:when>
          <xsl:when test="@codeListValue = 'asNeeded'">
            <!--  A mapping is missing in Dublin Core -->
            <xsl:value-of select="concat($opfq,'AS_NEEDED')"/>
          </xsl:when>
          <xsl:when test="@codeListValue = 'irregular'">
            <!--  DC Freq voc
                        <xsl:value-of select="concat($cldFrequency,'irregular')"/>
            -->
            <xsl:value-of select="concat($opfq,'IRREG')"/>
          </xsl:when>
          <xsl:when test="@codeListValue = 'notPlanned'">
            <!--  A mapping is missing in Dublin Core -->
            <xsl:value-of select="concat($opfq,'NOT_PLANNED')"/>
          </xsl:when>
          <xsl:when test="@codeListValue = 'unknown'">
            <!--  A mapping is missing in Dublin Core -->
            <!--  INSPIRE Freq code list (not yet available)
                        <xsl:value-of select="concat($MaintenanceFrequencyCodelistUri,'/',@codeListValue)"/>
            -->
            <xsl:value-of select="concat($opfq,'UNKNOWN')"/>
          </xsl:when>
        </xsl:choose>
      </xsl:if>
    </xsl:param>
    <xsl:if test="$FrequencyCodeURI != ''">
      <dct:accrualPeriodicity>
        <dct:Frequency rdf:about="{$FrequencyCodeURI}"/>
      </dct:accrualPeriodicity>
    </xsl:if>
  </xsl:template>

  <!-- Coordinate and temporal reference system (tentative) -->

  <xsl:template mode="iso19139-to-dcatap" name="ReferenceSystem" match="gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier">
    <xsl:param name="MetadataLanguage"/>
    <xsl:param name="code" select="gmd:code/*[self::gco:CharacterString|gmx:Anchor]"/>
    <xsl:param name="link" select="gmd:code/gmx:Anchor/@xlink:href"/>
    <xsl:param name="codespace" select="gmd:codeSpace/*[self::gco:CharacterString|gmx:Anchor]"/>
    <xsl:param name="version" select="gmd:version/*[self::gco:CharacterString|gmx:Anchor]"/>
    <xsl:param name="version-statement">
      <xsl:if test="$profile = $extended">
        <xsl:if test="$version != ''">
          <owl:versionInfo xml:lang="{$MetadataLanguage}"><xsl:value-of select="$version"/></owl:versionInfo>
        </xsl:if>
      </xsl:if>
    </xsl:param>

    <xsl:choose>
      <xsl:when test="starts-with($link, 'http://') or starts-with($link, 'https://')">
        <geodcatap:referenceSystem>
          <rdf:Description rdf:about="{$link}">
            <rdf:type rdf:resource="{$dct}Standard"/>
            <dct:type rdf:resource="{$INSPIREGlossaryUri}SpatialReferenceSystem"/>
          </rdf:Description>
        </geodcatap:referenceSystem>
      </xsl:when>
      <xsl:when test="starts-with($code, 'http://') or starts-with($code, 'https://')">
        <geodcatap:referenceSystem>
          <rdf:Description rdf:about="{$code}">
            <rdf:type rdf:resource="{$dct}Standard"/>
            <dct:type rdf:resource="{$INSPIREGlossaryUri}SpatialReferenceSystem"/>
          </rdf:Description>
        </geodcatap:referenceSystem>
      </xsl:when>
      <xsl:when test="starts-with($code, 'urn:')">
        <xsl:variable name="srid">
          <xsl:if test="starts-with(translate($code,$uppercase,$lowercase), translate($EpsgSrsBaseUrn,$uppercase,$lowercase))">
            <xsl:value-of select="substring-after(substring-after(substring-after(substring-after(substring-after(substring-after($code,':'),':'),':'),':'),':'),':')"/>
          </xsl:if>
        </xsl:variable>
        <xsl:variable name="sridVersion" select="substring-before(substring-after(substring-after(substring-after(substring-after(substring-after($code,':'),':'),':'),':'),':'),':')"/>
        <xsl:choose>
          <xsl:when test="$srid != '' and number($srid) = number($srid)">
            <geodcatap:referenceSystem>
              <rdf:Description rdf:about="{$EpsgSrsBaseUri}/{$srid}">
                <rdf:type rdf:resource="{$dct}Standard"/>
                <rdf:type rdf:resource="{$skos}Concept"/>
                <dct:type rdf:resource="{$INSPIREGlossaryUri}SpatialReferenceSystem"/>
                <dct:identifier rdf:datatype="{$xsd}anyURI"><xsl:value-of select="$code"/></dct:identifier>
                <skos:notation rdf:datatype="{$xsd}anyURI"><xsl:value-of select="$code"/></skos:notation>
                <skos:inScheme>
                  <skos:ConceptScheme rdf:about="{$EpsgSrsBaseUri}">
                    <dct:title xml:lang="en">
                      <xsl:value-of select="$EpsgSrsName"/>
                    </dct:title>
                  </skos:ConceptScheme>
                </skos:inScheme>
                <xsl:copy-of select="$version-statement"/>
              </rdf:Description>
            </geodcatap:referenceSystem>
          </xsl:when>
          <xsl:otherwise>
            <geodcatap:referenceSystem rdf:parseType="Resource">
              <rdf:type rdf:resource="{$dct}Standard"/>
              <dct:type rdf:resource="{$INSPIREGlossaryUri}SpatialReferenceSystem"/>
              <dct:identifier rdf:datatype="{$xsd}anyURI"><xsl:value-of select="$code"/></dct:identifier>
              <xsl:if test="$codespace != ''">
                <rdf:type rdf:resource="{$skos}Concept"/>
                <skos:notation rdf:datatype="{$xsd}anyURI"><xsl:value-of select="$code"/></skos:notation>
                <skos:inScheme>
                  <skos:ConceptScheme>
                    <dct:title xml:lang="{$MetadataLanguage}">
                      <xsl:value-of select="$codespace"/>
                    </dct:title>
                  </skos:ConceptScheme>
                </skos:inScheme>
              </xsl:if>
              <xsl:copy-of select="$version-statement"/>
            </geodcatap:referenceSystem>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="$code castable as xs:double and $code = number($code) and (translate($codespace,$uppercase,$lowercase) = 'epsg' or starts-with(translate($codespace,$uppercase,$lowercase),translate($EpsgSrsBaseUrn,$uppercase,$lowercase)))">
            <geodcatap:referenceSystem>
              <rdf:Description rdf:about="{$EpsgSrsBaseUri}/{$code}">
                <rdf:type rdf:resource="{$dct}Standard"/>
                <rdf:type rdf:resource="{$skos}Concept"/>
                <dct:type rdf:resource="{$INSPIREGlossaryUri}SpatialReferenceSystem"/>
                <dct:identifier rdf:datatype="{$xsd}anyURI"><xsl:value-of select="concat($EpsgSrsBaseUrn,':',$version,':',$code)"/></dct:identifier>
                <skos:notation rdf:datatype="{$xsd}anyURI"><xsl:value-of select="concat($EpsgSrsBaseUrn,':',$version,':',$code)"/></skos:notation>
                <skos:inScheme>
                  <skos:ConceptScheme rdf:about="{$EpsgSrsBaseUri}">
                    <dct:title xml:lang="en">
                      <xsl:value-of select="$EpsgSrsName"/>
                    </dct:title>
                  </skos:ConceptScheme>
                </skos:inScheme>
                <xsl:copy-of select="$version-statement"/>
              </rdf:Description>
            </geodcatap:referenceSystem>
          </xsl:when>
          <xsl:when test="translate(normalize-space(translate($code,$uppercase,$lowercase)),': ','') = 'etrs89'">
            <geodcatap:referenceSystem>
              <rdf:Description rdf:about="{$Etrs89Uri}">
                <rdf:type rdf:resource="{$dct}Standard"/>
                <rdf:type rdf:resource="{$skos}Concept"/>
                <dct:type rdf:resource="{$INSPIREGlossaryUri}SpatialReferenceSystem"/>
                <dct:identifier rdf:datatype="{$xsd}anyURI"><xsl:value-of select="$Etrs89Urn"/></dct:identifier>
                <skos:notation rdf:datatype="{$xsd}anyURI"><xsl:value-of select="$Etrs89Urn"/></skos:notation>
                <dct:title xml:lang="en">ETRS89 - European Terrestrial Reference System 1989</dct:title>
                <skos:prefLabel xml:lang="en">ETRS89 - European Terrestrial Reference System 1989</skos:prefLabel>
                <skos:inScheme>
                  <skos:ConceptScheme rdf:about="{$EpsgSrsBaseUri}">
                    <dct:title xml:lang="en">
                      <xsl:value-of select="$EpsgSrsName"/>
                    </dct:title>
                  </skos:ConceptScheme>
                </skos:inScheme>
                <xsl:copy-of select="$version-statement"/>
              </rdf:Description>
            </geodcatap:referenceSystem>
          </xsl:when>
          <xsl:when test="translate(normalize-space(translate($code,$uppercase,$lowercase)),': ','') = 'crs84'">
            <geodcatap:referenceSystem>
              <rdf:Description rdf:about="{$Crs84Uri}">
                <rdf:type rdf:resource="{$dct}Standard"/>
                <rdf:type rdf:resource="{$skos}Concept"/>
                <dct:type rdf:resource="{$INSPIREGlossaryUri}SpatialReferenceSystem"/>
                <dct:identifier rdf:datatype="{$xsd}anyURI"><xsl:value-of select="$Crs84Urn"/></dct:identifier>
                <skos:notation rdf:datatype="{$xsd}anyURI"><xsl:value-of select="$Crs84Urn"/></skos:notation>
                <dct:title xml:lang="en">CRS84</dct:title>
                <skos:prefLabel xml:lang="en">CRS84</skos:prefLabel>
                <skos:inScheme>
                  <skos:ConceptScheme rdf:about="{$OgcSrsBaseUri}">
                    <dct:title xml:lang="en">
                      <xsl:value-of select="$OgcSrsName"/>
                    </dct:title>
                  </skos:ConceptScheme>
                </skos:inScheme>
                <xsl:copy-of select="$version-statement"/>
              </rdf:Description>
            </geodcatap:referenceSystem>
          </xsl:when>
          <xsl:otherwise>
            <geodcatap:referenceSystem rdf:parseType="Resource">
              <rdf:type rdf:resource="{$dct}Standard"/>
              <rdf:type rdf:resource="{$skos}Concept"/>
              <dct:type rdf:resource="{$INSPIREGlossaryUri}SpatialReferenceSystem"/>
              <dct:title xml:lang="{$MetadataLanguage}">
                <xsl:value-of select="$code"/>
              </dct:title>
              <xsl:if test="$codespace != ''">
                <skos:prefLabel xml:lang="{$MetadataLanguage}">
                  <xsl:value-of select="$code"/>
                </skos:prefLabel>
                <skos:inScheme>
                  <skos:ConceptScheme>
                    <dct:title xml:lang="{$MetadataLanguage}">
                      <xsl:value-of select="$codespace"/>
                    </dct:title>
                  </skos:ConceptScheme>
                </skos:inScheme>
              </xsl:if>
              <xsl:copy-of select="$version-statement"/>
            </geodcatap:referenceSystem>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Spatial representation type (tentative) -->

  <xsl:template mode="iso19139-to-dcatap" name="SpatialRepresentationType" match="gmd:identificationInfo/*/gmd:spatialRepresentationType/gmd:MD_SpatialRepresentationTypeCode">
    <adms:representationTechnique rdf:resource="{$SpatialRepresentationTypeCodelistUri}/{@codeListValue}"/>
  </xsl:template>

  <!-- Multilingual text -->

  <xsl:template name="LocalisedString">
    <xsl:param name="term"/>
    <xsl:for-each select="gmd:PT_FreeText/*/gmd:LocalisedCharacterString">
      <xsl:variable name="value" select="normalize-space(.)"/>
      <xsl:variable name="langs">
        <xsl:call-template name="Alpha3-to-Alpha2">
          <xsl:with-param name="lang" select="translate(translate(@locale, $uppercase, $lowercase), '#', '')"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:if test="$value != ''">
        <xsl:element name="{$term}">
          <xsl:attribute name="xml:lang"><xsl:value-of select="$langs"/></xsl:attribute>
          <xsl:value-of select="$value"/>
        </xsl:element>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="Alpha3-to-Alpha2">
    <xsl:param name="lang"/>
    <xsl:choose>
      <xsl:when test="$lang = 'bul'">
        <xsl:text>bg</xsl:text>
      </xsl:when>
      <xsl:when test="$lang = 'cze'">
        <xsl:text>cs</xsl:text>
      </xsl:when>
      <xsl:when test="$lang = 'dan'">
        <xsl:text>da</xsl:text>
      </xsl:when>
      <xsl:when test="$lang = 'ger'">
        <xsl:text>de</xsl:text>
      </xsl:when>
      <xsl:when test="$lang = 'gre'">
        <xsl:text>el</xsl:text>
      </xsl:when>
      <xsl:when test="$lang = 'eng'">
        <xsl:text>en</xsl:text>
      </xsl:when>
      <xsl:when test="$lang = 'spa'">
        <xsl:text>es</xsl:text>
      </xsl:when>
      <xsl:when test="$lang = 'est'">
        <xsl:text>et</xsl:text>
      </xsl:when>
      <xsl:when test="$lang = 'fin'">
        <xsl:text>fi</xsl:text>
      </xsl:when>
      <xsl:when test="$lang = 'fre'">
        <xsl:text>fr</xsl:text>
      </xsl:when>
      <xsl:when test="$lang = 'gle'">
        <xsl:text>ga</xsl:text>
      </xsl:when>
      <xsl:when test="$lang = 'hrv'">
        <xsl:text>hr</xsl:text>
      </xsl:when>
      <xsl:when test="$lang = 'ita'">
        <xsl:text>it</xsl:text>
      </xsl:when>
      <xsl:when test="$lang = 'lav'">
        <xsl:text>lv</xsl:text>
      </xsl:when>
      <xsl:when test="$lang = 'lit'">
        <xsl:text>lt</xsl:text>
      </xsl:when>
      <xsl:when test="$lang = 'hun'">
        <xsl:text>hu</xsl:text>
      </xsl:when>
      <xsl:when test="$lang = 'mlt'">
        <xsl:text>mt</xsl:text>
      </xsl:when>
      <xsl:when test="$lang = 'dut'">
        <xsl:text>nl</xsl:text>
      </xsl:when>
      <xsl:when test="$lang = 'pol'">
        <xsl:text>pl</xsl:text>
      </xsl:when>
      <xsl:when test="$lang = 'por'">
        <xsl:text>pt</xsl:text>
      </xsl:when>
      <xsl:when test="$lang = 'rum'">
        <xsl:text>ru</xsl:text>
      </xsl:when>
      <xsl:when test="$lang = 'slo'">
        <xsl:text>sk</xsl:text>
      </xsl:when>
      <xsl:when test="$lang = 'slv'">
        <xsl:text>sl</xsl:text>
      </xsl:when>
      <xsl:when test="$lang = 'swe'">
        <xsl:text>sv</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$lang"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Templates for services and distributions pointing to services -->

  <xsl:template name="detect-service">
    <xsl:param name="function"/>
    <xsl:param name="protocol"/>
    <xsl:param name="url"/>
    <xsl:choose>
      <xsl:when test="contains(substring-after(translate($url, $uppercase, $lowercase), '?'), 'request=getcapabilities')">
        <xsl:text>yes</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>no</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="service-protocol-code">
    <xsl:param name="function"/>
    <xsl:param name="protocol"/>
    <xsl:param name="url"/>
    <xsl:choose>
      <xsl:when test="contains(substring-after(translate($url, $uppercase, $lowercase), '?'), 'service=csw')">
        <xsl:text>csw</xsl:text>
      </xsl:when>
      <xsl:when test="translate($protocol, $uppercase, $lowercase) = 'ogc:csw'">
        <xsl:text>csw</xsl:text>
      </xsl:when>
      <xsl:when test="contains(substring-after(translate($url, $uppercase, $lowercase), '?'), 'service=sos')">
        <xsl:text>sos</xsl:text>
      </xsl:when>
      <xsl:when test="translate($protocol, $uppercase, $lowercase) = 'ogc:sos'">
        <xsl:text>sos</xsl:text>
      </xsl:when>
      <xsl:when test="contains(substring-after(translate($url, $uppercase, $lowercase), '?'), 'service=sps')">
        <xsl:text>sps</xsl:text>
      </xsl:when>
      <xsl:when test="translate($protocol, $uppercase, $lowercase) = 'ogc:sps'">
        <xsl:text>sps</xsl:text>
      </xsl:when>
      <xsl:when test="contains(substring-after(translate($url, $uppercase, $lowercase), '?'), 'service=wcs')">
        <xsl:text>wcs</xsl:text>
      </xsl:when>
      <xsl:when test="translate($protocol, $uppercase, $lowercase) = 'ogc:wcs'">
        <xsl:text>wcs</xsl:text>
      </xsl:when>
      <xsl:when test="contains(substring-after(translate($url, $uppercase, $lowercase), '?'), 'service=wfs')">
        <xsl:text>wfs</xsl:text>
      </xsl:when>
      <xsl:when test="translate($protocol, $uppercase, $lowercase) = 'ogc:wfs'">
        <xsl:text>wfs</xsl:text>
      </xsl:when>
      <xsl:when test="contains(substring-after(translate($url, $uppercase, $lowercase), '?'), 'service=wms')">
        <xsl:text>wms</xsl:text>
      </xsl:when>
      <xsl:when test="translate($protocol, $uppercase, $lowercase) = 'ogc:wms'">
        <xsl:text>wms</xsl:text>
      </xsl:when>
      <xsl:when test="contains(substring-after(translate($url, $uppercase, $lowercase), '?'), 'service=wmts')">
        <xsl:text>wmts</xsl:text>
      </xsl:when>
      <xsl:when test="translate($protocol, $uppercase, $lowercase) = 'ogc:wmts'">
        <xsl:text>wmts</xsl:text>
      </xsl:when>
      <xsl:when test="contains(substring-after(translate($url, $uppercase, $lowercase), '?'), 'service=wps')">
        <xsl:text>wps</xsl:text>
      </xsl:when>
      <xsl:when test="translate($protocol, $uppercase, $lowercase) = 'ogc:wps'">
        <xsl:text>wps</xsl:text>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="service-protocol-url">
    <xsl:param name="protocol"/>
    <xsl:choose>
      <xsl:when test="$protocol = 'csw'">
        <xsl:text>http://www.opengeospatial.org/standards/cat</xsl:text>
      </xsl:when>
      <xsl:when test="$protocol = 'sos'">
        <xsl:text>http://www.opengeospatial.org/standards/sos</xsl:text>
      </xsl:when>
      <xsl:when test="$protocol = 'sps'">
        <xsl:text>http://www.opengeospatial.org/standards/sps</xsl:text>
      </xsl:when>
      <xsl:when test="$protocol = 'wcs'">
        <xsl:text>http://www.opengeospatial.org/standards/wcs</xsl:text>
      </xsl:when>
      <xsl:when test="$protocol = 'wfs'">
        <xsl:text>http://www.opengeospatial.org/standards/wfs</xsl:text>
      </xsl:when>
      <xsl:when test="$protocol = 'wms'">
        <xsl:text>http://www.opengeospatial.org/standards/wms</xsl:text>
      </xsl:when>
      <xsl:when test="$protocol = 'wmts'">
        <xsl:text>http://www.opengeospatial.org/standards/wmts</xsl:text>
      </xsl:when>
      <xsl:when test="$protocol = 'wps'">
        <xsl:text>http://www.opengeospatial.org/standards/wps</xsl:text>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="service-protocol">
    <xsl:param name="function"/>
    <xsl:param name="protocol"/>
    <xsl:param name="url"/>
    <xsl:param name="protocol-url">
      <xsl:call-template name="service-protocol-url">
        <xsl:with-param name="protocol">
          <xsl:call-template name="service-protocol-code">
            <xsl:with-param name="function" select="$function"/>
            <xsl:with-param name="protocol" select="$protocol"/>
            <xsl:with-param name="url" select="$url"/>
          </xsl:call-template>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:param>
    <xsl:if test="$protocol-url != ''">
      <geodcatap:serviceProtocol>
        <dct:Standard rdf:about="{$protocol-url}"/>
      </geodcatap:serviceProtocol>
    </xsl:if>
  </xsl:template>

  <xsl:template name="service-endpoint">
    <xsl:param name="function"/>
    <xsl:param name="protocol"/>
    <xsl:param name="url"/>
    <xsl:param name="endpoint-url">
      <xsl:choose>
        <xsl:when test="contains($url, '?')">
          <xsl:value-of select="substring-before($url, '?')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$url"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <xsl:param name="endpoint-description">
      <xsl:choose>
        <xsl:when test="contains(substring-after(translate($url, $uppercase, $lowercase), '?'), 'request=getcapabilities')">
          <xsl:value-of select="$url"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$url"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <xsl:param name="service-type">
    </xsl:param>
    <xsl:if test="$endpoint-url != ''">
      <dcat:endpointURL rdf:resource="{$endpoint-url}"/>
    </xsl:if>
    <xsl:if test="$endpoint-description != ''">
      <dcat:endpointDescription rdf:resource="{$endpoint-description}"/>
    </xsl:if>
  </xsl:template>

</xsl:transform>
