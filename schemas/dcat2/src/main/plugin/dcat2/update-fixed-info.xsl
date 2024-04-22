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
                xmlns:spdx="http://spdx.org/rdf/terms#"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:adms="http://www.w3.org/ns/adms#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:prov="http://www.w3.org/ns/prov#"
                xmlns:foaf="http://xmlns.com/foaf/0.1/"
                xmlns:owl="http://www.w3.org/2002/07/owl#"
                xmlns:schema="http://schema.org/"
                xmlns:locn="http://www.w3.org/ns/locn#"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:gn-fn-dcat2="http://geonetwork-opensource.org/xsl/functions/profiles/dcat2"
                xmlns:saxon="http://saxon.sf.net/"
                xmlns:java="java:org.fao.geonet.util.XslUtil"
                extension-element-prefixes="saxon"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:output method="xml" indent="yes" encoding="UTF-8"/>

  <xsl:output name="default-serialize-mode"
              indent="no"
              omit-xml-declaration="yes"/>

  <xsl:include href="layout/utility-fn.xsl"/>
  <xsl:include href="layout/utility-tpl-multilingual.xsl"/>

  <!--
    URL prefix for the identifier.

    Default is:
    <dcat:Dataset rdf:about="https://opendata.vlaanderen.be/geonetwork/srv/api/records/9fde14b3-4654-44f5-970e-be0a986cf4eb">
      <dct:identifier>9fde14b3-4654-44f5-970e-be0a986cf4eb</dct:identifier>

  -->
  <xsl:variable name="uuidUrlPrefix"
                select="concat(java:getSettingValue('nodeUrl'), 'api/records/')"/>

  <xsl:variable name="serviceUrl"
                select="/root/env/siteURL"/>

  <xsl:variable name="env"
                select="/root/env"/>

  <xsl:variable name="metadata"
                select="/root/rdf:RDF"/>

  <xsl:variable name="mainLanguage">
    <xsl:call-template name="get-dcat2-language"/>
  </xsl:variable>

  <xsl:variable name="locales">
    <xsl:call-template name="get-dcat2-other-languages"/>
  </xsl:variable>

  <xsl:variable name="isMultilingual"
                select="count($locales/*) > 1"/>

  <xsl:variable name="editorConfig"
                select="document('layout/config-editor.xml')"/>

  <xsl:variable name="nonMultilingualFields"
                select="$editorConfig/editor/multilingualFields/exclude"/>

  <xsl:variable name="iso2letterLanguageCode"
                select="lower-case(java:twoCharLangCode(/root/gui/language))"/>

  <xsl:variable name="resourcePrefix"
                select="$env/metadata/resourceIdentifierPrefix"/>


  <xsl:template match="/root">
    <xsl:apply-templates select="rdf:RDF"/>
  </xsl:template>


  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>


  <!-- Multilingual support -->
  <xsl:variable name="multilingualElements"
                select="('dct:title', 'dct:description')"/>

  <!-- Ignore element not in main language (they are handled in dcat2-translations-builder. -->
  <xsl:template match="*[name() = $multilingualElements
                         and $isMultilingual
                         and @xml:lang != $mainLanguage]"/>

  <!-- Expand element which may not contain xml:lang attribute
  eg. when clicking + -->
  <xsl:template match="*[name() = $multilingualElements
                         and $isMultilingual
                         and not(@xml:lang)]">
    <xsl:variable name="name"
                  select="name()"/>
    <xsl:variable name="value"
                  select="."/>
    <xsl:for-each select="$locales/lang/@code">
      <xsl:element name="{$name}">
        <xsl:attribute name="xml:lang"
                       select="current()"/>
        <xsl:value-of select="if (current() = $mainLanguage)
                              then $value else ''"/>
      </xsl:element>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="*[name() = $multilingualElements
                         and $isMultilingual
                         and @xml:lang = $mainLanguage]"
                priority="100">
    <!-- Then we copy translations of following siblings
    or create empty elements. -->
    <xsl:variable name="name"
                  select="name(.)"/>

    <xsl:variable name="excluded"
                  select="gn-fn-dcat2:isNotMultilingualField(., $editorConfig)"/>
    <xsl:variable name="isMultilingualElement"
                  select="$isMultilingual and $excluded = false()"/>

    <xsl:choose>
      <xsl:when test="$isMultilingualElement">
        <xsl:call-template name="dcat2-translations-builder"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- Copy existing translation
  and add empty elements for other languages -->
  <xsl:template name="dcat2-translations-builder">
    <xsl:variable name="name"
                  select="name(.)"/>
    <xsl:variable name="value"
                  select="text()"/>

    <xsl:variable name="followingSiblings"
                  select="following-sibling::*[name() = $name]"/>

    <!-- Select element with same name and different xml:lang attribute
    until the next one with the main language. -->
    <xsl:variable name="currentGroup"
                  select="$followingSiblings[
                    count($followingSiblings/*[@xml:lang = $mainLanguage]) = 0
                    or position() &lt; $followingSiblings/*[@xml:lang = $mainLanguage]/position()]"/>

    <xsl:for-each select="$locales/lang/@code">
      <xsl:element name="{$name}">
        <xsl:attribute name="xml:lang"
                       select="current()"/>
        <xsl:value-of select="if (current() = $mainLanguage)
                              then $value else $currentGroup[@xml:lang = current()]"/>
      </xsl:element>
    </xsl:for-each>
  </xsl:template>


  <xsl:template name="dcat2-build-identifier">
    <!-- If a local record, use API landing page -->
    <xsl:variable name="rdfAbout"
                  select="concat($uuidUrlPrefix,
                                   /root/env/uuid)"/>
    <xsl:attribute name="rdf:about" select="$rdfAbout"/>
    <dct:identifier>
      <xsl:value-of select="/root/env/uuid"/>
    </dct:identifier>

    <!--
          When duplicate, do not copy any dct:identifier otherwise copy all dct:identifier elements except the first.
          We could use position() to check the position of the next dct:identifier elements,
          but when schema change and dct:identifier is not the first element in the dcat:Dataset sequence anymore,
          the next will continue to work.
        -->
    <xsl:if test="/root/env/id != ''">
      <xsl:for-each select="dct:identifier">
        <xsl:variable name="previousIdentifierSiblingsCount"
                      select="count(preceding-sibling::*[name(.) = 'dct:identifier'])"/>
        <xsl:if test="$previousIdentifierSiblingsCount > 0">
          <xsl:apply-templates select="."/>
        </xsl:if>
      </xsl:for-each>
    </xsl:if>

  </xsl:template>


  <xsl:template name="add-namespaces">
    <xsl:namespace name="rdf" select="'http://www.w3.org/1999/02/22-rdf-syntax-ns#'"/>
    <xsl:namespace name="rdfs" select="'http://www.w3.org/2000/01/rdf-schema#'"/>
    <xsl:namespace name="dct" select="'http://purl.org/dc/terms/'"/>
    <xsl:namespace name="dcat" select="'http://www.w3.org/ns/dcat#'"/>
    <xsl:namespace name="skos" select="'http://www.w3.org/2004/02/skos/core#'"/>
    <xsl:namespace name="spdx" select="'http://spdx.org/rdf/terms#'"/>
    <xsl:namespace name="adms" select="'http://www.w3.org/ns/adms#'"/>
    <xsl:namespace name="prov" select="'http://www.w3.org/ns/prov#'"/>
    <xsl:namespace name="foaf" select="'http://xmlns.com/foaf/0.1/'"/>
    <xsl:namespace name="owl" select="'http://www.w3.org/2002/07/owl#'"/>
    <xsl:namespace name="schema" select="'http://schema.org/'"/>
    <xsl:namespace name="locn" select="'http://www.w3.org/ns/locn#'"/>
    <xsl:namespace name="gml" select="'http://www.opengis.net/gml/3.2'"/>
  </xsl:template>

  <xsl:template name="add-resource-type">
    <xsl:variable name="inScheme" select="'https://registry.geonetwork-opensource.org/dcat/type'"/>
    <xsl:variable name="typeURI">
      <xsl:choose>
        <xsl:when test="name() = 'dcat:Dataset'">
          <xsl:value-of select="concat($inScheme, '/', 'dataset')"/>
        </xsl:when>
        <xsl:when test="name() = 'dcat:DataService'">
          <xsl:value-of select="concat($inScheme, '/', 'service')"/>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="thesaurusKey" select="'external.theme.dcat-type'"/>

    <dct:type>
      <skos:Concept rdf:about="{$typeURI}">
        <xsl:for-each select="$locales/lang/@code">
          <skos:prefLabel xml:lang="{.}">
            <xsl:value-of select="java:getKeywordValueByUri($typeURI, $thesaurusKey, .)"/>
          </skos:prefLabel>
        </xsl:for-each>
        <skos:inScheme rdf:resource="{$inScheme}"/>
      </skos:Concept>
    </dct:type>
  </xsl:template>

  <xsl:template match="rdf:RDF" priority="10">
    <xsl:copy>
      <xsl:call-template name="add-namespaces"/>
      <xsl:apply-templates select="@*"/>

      <xsl:call-template name="dcat2-build-catalogrecord"/>

      <xsl:apply-templates select="dcat:Dataset|dcat:DataService"/>
    </xsl:copy>
  </xsl:template>

  <!-- Create CatalogRecord if missing -->
  <xsl:template name="dcat2-build-catalogrecord">
      <dcat:CatalogRecord>
        <xsl:apply-templates select="dcat:CatalogRecord/@*[not(name(.) = 'rdf:about')]"/>
        <xsl:call-template name="dcat2-build-identifier"/>

        <dct:issued><xsl:value-of select="/root/env/createDate"/></dct:issued>
        <dct:modified><xsl:value-of select="/root/env/changeDate"/></dct:modified>

        <xsl:variable name="rdfAbout"
                      select="concat($uuidUrlPrefix,
                                   /root/env/uuid)"/>
        <foaf:primaryTopic rdf:resource="{$rdfAbout}"></foaf:primaryTopic>

        <xsl:apply-templates select="dcat:CatalogRecord/*[not(name() = ('dct:identifier', 'dct:issued', 'dct:modified', 'foaf:primaryTopic'))]"/>
      </dcat:CatalogRecord>
  </xsl:template>



  <xsl:template match="dcat:Dataset|dcat:DataService" priority="10">
    <xsl:copy>
      <xsl:apply-templates select="@*[not(name(.) = 'rdf:about')]"/>
<!--      <xsl:call-template name="dcat-build-identifier"/>-->

      <!-- Fixed order of elements. -->
      <xsl:apply-templates select="dct:title"/>
      <xsl:apply-templates select="dct:description"/>

<!--      <xsl:message><xsl:copy-of select="."/></xsl:message>-->
      <xsl:apply-templates select="dcat:theme"/>
      <xsl:apply-templates select="dcat:keyword"/>

      <xsl:choose>
        <xsl:when test="dct:type">
          <xsl:apply-templates select="dct:type"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="add-resource-type"/>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:apply-templates select="dct:creator"/>
      <xsl:apply-templates select="dct:publisher"/>
      <xsl:apply-templates select="dcat:contactPoint"/>

      <xsl:apply-templates select="dct:issued"/>
      <xsl:apply-templates select="dct:modified"/>

      <xsl:apply-templates select="dct:spatial"/>
      <xsl:apply-templates select="dct:temporal"/>

      <xsl:apply-templates select="dct:accessRights"/>
      <xsl:apply-templates select="dct:conformsTo"/>
      <xsl:apply-templates select="foaf:page"/>
      <xsl:apply-templates select="dct:accrualPeriodicity"/>
      <xsl:apply-templates select="dct:hasVersion"/>
      <xsl:apply-templates select="dct:isVersionOf"/>
      <xsl:apply-templates select="dcat:landingPage"/>
      <xsl:apply-templates select="dct:language"/>
      <xsl:apply-templates select="dct:license" />
      <xsl:apply-templates select="adms:identifier"/>
      <xsl:apply-templates select="dct:provenance"/>
      <xsl:apply-templates select="dct:relation"/>
      <xsl:apply-templates select="dct:source"/>
      <xsl:apply-templates select="owl:versionInfo"/>
      <xsl:apply-templates select="adms:versionNotes"/>
      <xsl:apply-templates select="dcat:extension"/>
      <xsl:apply-templates select="dcat:distribution"/>
      <xsl:apply-templates select="dcat:spatialResolutionInMeters"/>
      <xsl:apply-templates select="dcat:temporalResolution"/>
      <xsl:apply-templates select="adms:sample"/>
      <xsl:apply-templates select="prov:qualifiedInvalidation"/>
      <xsl:apply-templates select="locn:address"/>

      <xsl:apply-templates select="dcat:endpointDescription"/>
      <xsl:apply-templates select="dcat:endpointURL"/>
      <xsl:apply-templates select="dcat:servesDataset"/>
    </xsl:copy>
  </xsl:template>


  <xsl:template match="foaf:Document[not(@rdf:about)]" priority="10">
    <xsl:copy>
      <xsl:attribute name="rdf:about"/>
      <xsl:apply-templates select="@*|*"/>
    </xsl:copy>
  </xsl:template>


  <!-- Fix value for attribute -->
  <xsl:template match="rdf:Statement/rdf:object" priority="10">
    <xsl:copy>
      <xsl:copy-of select="@*[not(name()='rdf:datatype')]"/>
      <xsl:attribute name="rdf:datatype">xs:dateTime</xsl:attribute>
    </xsl:copy>
  </xsl:template>

  <!-- Fill concepts with resourceType -->
  <xsl:template match="skos:Concept" priority="10">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:variable name="rdfType" select="gn-fn-dcat2:getRdfTypeByElementName(name(..), name(../..))"/>
      <xsl:if test="normalize-space($rdfType) != ''">
        <rdf:type rdf:resource="{$rdfType}"/>
      </xsl:if>
      <xsl:apply-templates select="*[not(name() = 'rdf:type')]"/>
    </xsl:copy>
  </xsl:template>

  <!-- Set data type for Date or DateTime -->
  <xsl:template match="dct:issued|dct:modified|schema:startDate|schema:endDate" priority="10">
    <xsl:copy>
      <xsl:copy-of select="@*[not(name()='rdf:datatype')]"/>
      <xsl:attribute name="rdf:datatype">
        <xsl:if test="not(contains(lower-case(.),'t'))">http://www.w3.org/2001/XMLSchema#date</xsl:if>
        <xsl:if test="contains(lower-case(.),'t')">http://www.w3.org/2001/XMLSchema#dateTime</xsl:if>
      </xsl:attribute>
      <xsl:value-of select="."/>
    </xsl:copy>
  </xsl:template>


  <!-- Convert DC extent to GML and WKT -->
  <xsl:template match="dct:Location" priority="10">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <!-- Store id in rdf:about -->
      <xsl:if test="not(@rdf:about)">
        <xsl:attribute name="rdf:about"/>
      </xsl:if>

      <xsl:variable name="coverage">
        <xsl:choose>
          <xsl:when test="count(locn:geometry[ends-with(@rdf:datatype,'#gmlLiteral')]) > 0">
            <xsl:value-of select="locn:geometry[ends-with(@rdf:datatype,'#gmlLiteral')][1]"/>
          </xsl:when>
          <xsl:when test="count(locn:geometry[ends-with(@rdf:datatype,'#wktLiteral')]) > 0">
            <xsl:value-of select="locn:geometry[ends-with(@rdf:datatype,'#wktLiteral')][1]"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="locn:geometry[1]"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="n" select="substring-after($coverage,'North ')"/>
      <xsl:if test="string-length($n) = 0">
        <xsl:copy-of select="node()"/>
      </xsl:if>
      <xsl:if test="string-length($n) > 0">
        <xsl:variable name="north" select="substring-before($n,',')"/>
        <xsl:variable name="s" select="substring-after($coverage,'South ')"/>
        <xsl:variable name="south" select="substring-before($s,',')"/>
        <xsl:variable name="e" select="substring-after($coverage,'East ')"/>
        <xsl:variable name="east" select="substring-before($e,',')"/>
        <xsl:variable name="w" select="substring-after($coverage,'West ')"/>
        <xsl:variable name="west" select="if (contains($w, '. '))
		                                      then substring-before($w,'. ') else $w"/>
        <xsl:variable name="place" select="substring-after($coverage,'. ')"/>
        <xsl:variable name="isValid" select="number($west) and number($east) and number($south) and number($north)"/>

        <xsl:choose>
          <xsl:when test="$isValid">
            <xsl:variable name="wktLiteral"
                          select="concat('POLYGON ((',$west,' ',$south,',',$west,' ',$north,',',$east,' ',$north,',', $east,' ', $south,',', $west,' ',$south,'))')"/>
            <xsl:variable name="gmlLiteral"
                          select="concat('&lt;gml:Polygon xmlns:gml=&quot;http://www.opengis.net/gml/3.2&quot;&gt;&lt;gml:exterior&gt;&lt;gml:LinearRing&gt;&lt;gml:posList&gt;',$south,' ',$west,' ',$north,' ', $west, ' ', $north, ' ', $east, ' ', $south, ' ', $east,' ', $south, ' ', $west, '&lt;/gml:posList&gt;&lt;/gml:LinearRing&gt;&lt;/gml:exterior&gt;&lt;/gml:Polygon&gt;')"/>
            <xsl:element name="locn:geometry">
              <xsl:attribute name="rdf:datatype">http://www.opengis.net/ont/geosparql#wktLiteral</xsl:attribute>
              <xsl:value-of select="$wktLiteral"/>
            </xsl:element>
            <xsl:element name="locn:geometry">
              <xsl:attribute name="rdf:datatype">http://www.opengis.net/ont/geosparql#gmlLiteral</xsl:attribute>
              <xsl:value-of select="$gmlLiteral"/>
            </xsl:element>
          </xsl:when>
          <xsl:otherwise>
            <xsl:element name="locn:geometry">
              <xsl:attribute name="rdf:datatype">http://www.opengis.net/ont/geosparql#wktLiteral</xsl:attribute>
            </xsl:element>
            <xsl:element name="locn:geometry">
              <xsl:attribute name="rdf:datatype">http://www.opengis.net/ont/geosparql#gmlLiteral</xsl:attribute>
            </xsl:element>
          </xsl:otherwise>
        </xsl:choose>


        <xsl:apply-templates select="node()[not(name(.) = 'locn:geometry')]"/>
        <!-- TODO: Store <skos:prefLabel xml:lang="nl">Vlaams Gewest</skos:prefLabel> ?-->
      </xsl:if>
    </xsl:copy>
  </xsl:template>

  <!-- Ignore all empty rdf:about -->
  <xsl:template match="@rdf:about[normalize-space() = '']
                       |@rdf:datatype[normalize-space() = '']"
                priority="10"/>

  <!-- Remove non numeric byteSize and format scientific notation to decimal -->
  <xsl:template match="dcat:byteSize" priority="10">
    <xsl:if test="string(number(.)) != 'NaN'">
      <xsl:copy>
        <xsl:attribute name="rdf:datatype">http://www.w3.org/2001/XMLSchema#decimal</xsl:attribute>
        <xsl:choose>
          <xsl:when test="matches(string(.), '^\-?[\d\.,]*[Ee][+\-]*\d*$')">
            <xsl:value-of select="format-number(number(.), '#0.#############')"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="."/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:copy>
    </xsl:if>
  </xsl:template>

  <!-- Fix value for attribute -->
  <xsl:template match="spdx:checksumValue" priority="10">
    <xsl:copy>
      <xsl:copy-of select="@*[not(name()='rdf:datatype')]"/>
      <xsl:attribute name="rdf:datatype">http://www.w3.org/2001/XMLSchema#hexBinary</xsl:attribute>
      <xsl:value-of select="."/>
    </xsl:copy>
  </xsl:template>

  <!-- Fix value for attribute -->
  <xsl:template match="spdx:algorithm" priority="10">
    <spdx:algorithm rdf:resource="http://spdx.org/rdf/terms#checksumAlgorithm_sha1"/>
  </xsl:template>

  <!-- foaf:Document always have a rdf about attribute to set the URL. -->
  <xsl:template match="foaf:Document[not(@rdf:about)]">
    <xsl:copy>
      <xsl:attribute name="rdf:about"/>
      <xsl:copy-of select="@*|*"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove some elements which are empty.
   TODO: Issue related to keyword picker ? -->
  <xsl:template match="dcat:keyword[count(@*) = 0 and count(*) = 0]
                       |dcat:theme[count(@*) = 0 and count(*) = 0]
                       |dct:type[count(@*) = 0 and count(*) = 0]
                       |dct:accessRights[count(@*) = 0 and count(*) = 0]
                       |dct:accrualPeriodicity[count(@*) = 0 and count(*) = 0]
                       |dct:creator[count(@*) = 0 and count(*) = 0]
                       |dct:publisher[count(@*) = 0 and count(*) = 0]
                       |dcat:contactPoint[count(@*) = 0 and count(*) = 0]
                       |dct:format[count(@*) = 0 and count(*) = 0]
                       |dcat:packageFormat[count(@*) = 0 and count(*) = 0]
                       |dcat:compressFormat[count(@*) = 0 and count(*) = 0]
                       |dcat:mediaType[count(@*) = 0 and count(*) = 0]"/>
</xsl:stylesheet>
