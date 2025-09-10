<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2001-2025 Food and Agriculture Organization of the
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
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gn-fn-index="http://geonetwork-opensource.org/xsl/functions/index"
                xmlns:daobs="http://daobs.org"
                xmlns:saxon="http://saxon.sf.net/"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:date-util="java:org.fao.geonet.utils.DateUtil"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all"
                version="2.0">

  <xsl:import href="common/index-utils.xsl"/>

  <xsl:output method="xml" indent="yes"/>

  <xsl:output name="default-serialize-mode"
              indent="no"
              omit-xml-declaration="yes"
              encoding="utf-8"
              escape-uri-attributes="yes"/>

  <xsl:param name="metadataId"/>

  <xsl:variable name="dateFormat" as="xs:string"
                select="'[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01][ZN]'"/>

  <xsl:variable name="separator" as="xs:string"
                select="'|'"/>

  <!-- To avoid Document contains at least one immense term
  in field="resourceAbstract" (whose UTF8 encoding is longer
  than the max length 32766. -->
  <xsl:variable name="maxFieldLength" select="32000" as="xs:integer"/>

  <!-- Get resources attachment properties which will be used in online resources and overviews -->
  <xsl:variable name="attachmentPropertiesElements" select="util:jsonToXml(util:getIndexableAttachmentProperties($metadataId))"/>

  <xsl:template match="/">
    <xsl:apply-templates mode="index"/>
  </xsl:template>


  <xsl:template match="simpledc"
                mode="index">
    <!-- Main variables for the document -->

    <xsl:variable name="mainTitle" as="xs:string?"
                  select="dc:title[1]"/>

    <xsl:variable name="resourceTitleObject" as="xs:string"
                  select="concat('{',
                          $doubleQuote, 'default', $doubleQuote, ':',
                          $doubleQuote, util:escapeForJson($mainTitle) ,$doubleQuote,
                        '}')"/>

    <xsl:variable name="identifier" as="xs:string?"
                  select="dc:identifier[1]"/>

    <xsl:variable name="revisionDateType"
                  select="'revision'"/>

    <xsl:variable name="creationDateType"
                  select="'creation'"/>

    <!-- Create a first document representing the main record. -->
    <doc>
      <xsl:copy-of select="gn-fn-index:add-field('docType', 'metadata')"/>

      <!-- Index the metadata document as XML -->
      <document>
        <!--<xsl:value-of select="saxon:serialize(., 'default-serialize-mode')"/>-->
      </document>
      <xsl:copy-of select="gn-fn-index:add-field('metadataIdentifier', $identifier)"/>


      <!-- Since GN sets the timezone in system/server/timeZone setting as Java system default
        timezone we can rely on XSLT functions to get current date in the right timezone -->
      <harvestedDate>
        <xsl:value-of select="format-dateTime(current-dateTime(), $dateFormat)"/>
      </harvestedDate>


      <!-- For multilingual docs it is good to have a title in the default locale.
       In this type of metadata we don't have one but in the general
       case we do so we need to add it to all -->
      <xsl:for-each select="dc:title[1]">
        <xsl:copy-of select="gn-fn-index:add-object-field('resourceTitleObject', $resourceTitleObject)"/>
      </xsl:for-each>

      <xsl:for-each select="dc:language[1]">
        <xsl:copy-of select="gn-fn-index:add-field('resourceLanguage', current())"/>
      </xsl:for-each>

      <xsl:for-each select="dct:abstract|dc:description">
        <xsl:copy-of select="gn-fn-index:add-field('resourceAbstract', current())"/>
      </xsl:for-each>

      <xsl:for-each select="dct:created[. != '']">

        <xsl:variable name="creationDate"
                      select="date-util:convertToISOZuluDateTime(string(current()))"/>
        <xsl:element name="{$creationDateType}DateForResource">
          <xsl:value-of select="$creationDate"/>
        </xsl:element>
        <xsl:element name="{$creationDateType}YearForResource">
          <xsl:value-of select="substring($creationDate, 0, 5)"/>
        </xsl:element>
        <xsl:element name="{$creationDateType}MonthForResource">
          <xsl:value-of select="substring($creationDate, 0, 8)"/>
        </xsl:element>
        <!--creationDateForResource><xsl:value-of select="date-util:convertToISOZuluDateTime(string(.))"/></creationDateForResource-->
      </xsl:for-each>

      <xsl:for-each select="dct:modified[. != '']">
        <dateStamp><xsl:value-of select="date-util:convertToISOZuluDateTime(normalize-space(.))"/></dateStamp>

        <xsl:variable name="revisionDate"
                      select="date-util:convertToISOZuluDateTime(string(current()))"/>
        <xsl:element name="{$revisionDateType}DateForResource">
          <xsl:value-of select="$revisionDate"/>
        </xsl:element>
        <xsl:element name="{$revisionDateType}YearForResource">
          <xsl:value-of select="substring($revisionDate, 0, 5)"/>
        </xsl:element>
        <xsl:element name="{$revisionDateType}MonthForResource">
          <xsl:value-of select="substring($revisionDate, 0, 8)"/>
        </xsl:element>
        <!--revisionDateForResource><xsl:value-of select="date-util:convertToISOZuluDateTime(string(.))"/></revisionDateForResource-->
      </xsl:for-each>

      <xsl:for-each select="dc:format">
        <format><xsl:value-of select="util:escapeForJson(.)"/></format>
      </xsl:for-each>

      <xsl:for-each select="dc:type[. != '']">
        <resourceType><xsl:value-of select="util:escapeForJson(.)"/></resourceType>
      </xsl:for-each>

      <xsl:for-each select="dc:source">
        <lineage><xsl:value-of select="util:escapeForJson(.)"/></lineage>
      </xsl:for-each>

      <!-- TODO Change mapping of dc:relation -->
      <xsl:for-each select="dc:relation">
        <related><xsl:value-of select="util:escapeForJson(.)"/></related>
      </xsl:for-each>

      <!-- TODO Change mapping of dct:accessRights -->
      <xsl:for-each select="dct:accessRights">
        <useLimitation><xsl:value-of select="util:escapeForJson(.)"/></useLimitation>
      </xsl:for-each>

      <!-- TODO Change mapping of dct:rights -->
      <xsl:for-each select="dct:rights">
        <useLimitation><xsl:value-of select="util:escapeForJson(.)"/></useLimitation>
      </xsl:for-each>

      <xsl:variable name="allKeywords">
        <xsl:variable name="keywords"
                      select="dc:subject[. != '']"/>
        <xsl:if test="count($keywords) > 0">
          <thesaurus>
            <info type="theme" field="otherKeywords-theme"/>
            <keywords>
              <xsl:for-each select="$keywords">
                <keyword>
                  <values>
                    <value>
                      "default": <xsl:value-of select="concat($doubleQuote, util:escapeForJson(.), $doubleQuote)"/>
                    </value>
                  </values>
                </keyword>
              </xsl:for-each>
            </keywords>
          </thesaurus>
        </xsl:if>

        <xsl:variable name="geoDescription"
                      select="dct:spatial[. != '']"/>
        <xsl:if test="count($geoDescription) > 0">
          <thesaurus>
            <info type="place" field="otherKeywords-place"/>
            <keywords>
              <xsl:for-each select="$geoDescription">
                <keyword>
                  <values>
                    <value>
                      "default": <xsl:value-of select="concat($doubleQuote, util:escapeForJson(.), $doubleQuote)"/>
                    </value>
                  </values>
                </keyword>
              </xsl:for-each>
            </keywords>
          </thesaurus>
        </xsl:if>
      </xsl:variable>

      <xsl:call-template name="build-all-keyword-fields">
        <xsl:with-param name="allKeywords" select="$allKeywords"/>
      </xsl:call-template>



      <xsl:for-each select="(dct:references|dc:relation)[normalize-space(.) != '']">
        <xsl:variable name="name" select="tokenize(., '/')[last()]"/>
        <!-- Index link where last token after the last / is the link name. -->
        <link type="object">{
          "protocol":"<xsl:value-of select="'WWW:LINK'"/>",
          "urlObject":{"default": "<xsl:value-of select="util:escapeForJson(.)"/>"},
          "nameObject":{"default": "<xsl:value-of select="util:escapeForJson($name)"/>"},
          "descriptionObject":{"default": ""},
          "function": ""
          <!-- Include attachment properties -->
          <xsl:variable name="matchAttachmentProperties" select="$attachmentPropertiesElements/root/array[url=normalize-space(current())]"/>
          <xsl:if test="$matchAttachmentProperties">
            <xsl:variable name="attachmentPropertiesJson" select="util:xmlToJson($matchAttachmentProperties)"/>
            <xsl:if test="$attachmentPropertiesJson">,
              "attachmentProperties": <xsl:value-of select="$attachmentPropertiesJson"/>
            </xsl:if>
          </xsl:if>
          }</link>
      </xsl:for-each>

      <xsl:variable name="overviews"
                    select="(dct:references|dc:relation)[normalize-space(.) != ''
                              and matches(., '.*(.gif|.png|.jpeg|.jpg)$', 'i')]"/>

      <xsl:copy-of select="gn-fn-index:add-field('hasOverview',
                            if (count($overviews) > 0) then 'true' else 'false')"/>

      <xsl:for-each select="$overviews">
        <overview type="object">{
          "url":"<xsl:value-of select="current()"/>"
          <xsl:if test="$isStoringOverviewInIndex">
            <xsl:variable name="data"
                          select="util:buildDataUrl(., 140)"/>
            <xsl:if test="$data != ''">,
              "data": "<xsl:value-of select="$data"/>"
            </xsl:if>
          </xsl:if>
          <!-- Include attachment properties -->
          <xsl:variable name="matchAttachmentProperties" select="$attachmentPropertiesElements/root/array[url=current()]"/>
          <xsl:if test="$matchAttachmentProperties">
            <xsl:variable name="attachmentPropertiesJson" select="util:xmlToJson($matchAttachmentProperties)"/>
            <xsl:if test="$attachmentPropertiesJson">,
              "attachmentProperties": <xsl:value-of select="$attachmentPropertiesJson"/>
            </xsl:if>
          </xsl:if>
          }</overview>
      </xsl:for-each>


      <!-- This index for "coverage" requires significant expansion to
         work well for spatial searches. It now only works for very
         strictly formatted content
         North 46.3, South 42.51, East 3.88, West -1.84 -->
      <xsl:for-each select="/simpledc/dc:coverage">
        <xsl:variable name="coverage" select="."/>

        <xsl:choose>
          <xsl:when test="starts-with(., 'North')">
            <xsl:variable name="nt" select="substring-after($coverage,'North ')"/>
            <xsl:variable name="n" select="substring-before($nt, ',')"/>
            <xsl:variable name="st" select="substring-after($coverage,'South ')"/>
            <xsl:variable name="s" select="substring-before($st, ',')"/>
            <xsl:variable name="et" select="substring-after($coverage,'East ')"/>
            <xsl:variable name="e" select="substring-before($et, ',')"/>
            <xsl:variable name="wt" select="substring-after($coverage,'West ')"/>
            <xsl:variable name="w"
                          select="if (contains($wt, '. ')) then substring-before($wt, '. ') else $wt"/>
            <xsl:variable name="p" select="substring-after($coverage,'(')"/>
            <xsl:variable name="place" select="substring-before($p,')')"/>

            <xsl:choose>
              <xsl:when test="-180 &lt;= number($e) and number($e) &lt;= 180 and
                              -180 &lt;= number($w) and number($w) &lt;= 180 and
                              -90 &lt;= number($s) and number($s) &lt;= 90 and
                              -90 &lt;= number($n) and number($n) &lt;= 90">
                <xsl:choose>
                  <xsl:when test="$e = $w and $s = $n">
                    <location><xsl:value-of select="concat($s, ',', $w)"/></location>
                  </xsl:when>
                  <xsl:when
                    test="($e = $w and $s != $n) or ($e != $w and $s = $n)">
                    <!-- Probably an invalid bbox indexing a point only -->
                    <location><xsl:value-of select="concat($s, ',', $w)"/></location>
                  </xsl:when>
                  <xsl:otherwise>
                    <geom type="object">
                      <xsl:text>{"type": "Polygon",</xsl:text>
                      <xsl:text>"coordinates": [[</xsl:text>
                      <xsl:value-of select="concat('[', $w, ',', $s, ']')"/>
                      <xsl:text>,</xsl:text>
                      <xsl:value-of select="concat('[', $e, ',', $s, ']')"/>
                      <xsl:text>,</xsl:text>
                      <xsl:value-of select="concat('[', $e, ',', $n, ']')"/>
                      <xsl:text>,</xsl:text>
                      <xsl:value-of select="concat('[', $w, ',', $n, ']')"/>
                      <xsl:text>,</xsl:text>
                      <xsl:value-of select="concat('[', $w, ',', $s, ']')"/>
                      <xsl:text>]]}</xsl:text>
                    </geom>

                    <location><xsl:value-of select="concat(
                                              (number($s) + number($n)) div 2,
                                              ',',
                                              (number($w) + number($e)) div 2)"/></location>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
            </xsl:choose>
          </xsl:when>
        </xsl:choose>
      </xsl:for-each>
    </doc>
  </xsl:template>
</xsl:stylesheet>
