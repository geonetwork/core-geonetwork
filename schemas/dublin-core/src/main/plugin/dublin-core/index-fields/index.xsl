<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2001-2020 Food and Agriculture Organization of the
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


  <!-- List of keywords to search for to flag a record as opendata.
   Do not put accents or upper case letters here as comparison will not
   take them in account. -->
  <xsl:variable name="openDataKeywords"
                select="'opendata|open data|donnees ouvertes'"/>

  <xsl:variable name="dateFormat" as="xs:string"
                select="'[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01][ZN]'"/>

  <xsl:variable name="separator" as="xs:string"
                select="'|'"/>

  <!-- To avoid Document contains at least one immense term
  in field="resourceAbstract" (whose UTF8 encoding is longer
  than the max length 32766. -->
  <xsl:variable name="maxFieldLength" select="32000" as="xs:integer"/>

  <xsl:template match="/">
    <xsl:apply-templates mode="index"/>
  </xsl:template>


  <xsl:template match="simpledc"
                mode="index">
    <!-- Main variables for the document -->
    <xsl:variable name="identifier" as="xs:string?"
                  select="dc:identifier[1]"/>

    <!-- Create a first document representing the main record. -->
    <doc>
      <docType>metadata</docType>
      <documentStandard>dublin-core</documentStandard>

      <!-- Index the metadata document as XML -->
      <document>
        <!--<xsl:value-of select="saxon:serialize(., 'default-serialize-mode')"/>-->
      </document>
      <metadataIdentifier>
        <xsl:value-of select="$identifier"/>
      </metadataIdentifier>

      <!-- Since GN sets the timezone in system/server/timeZone setting as Java system default
        timezone we can rely on XSLT functions to get current date in the right timezone -->
      <harvestedDate>
        <xsl:value-of select="format-dateTime(current-dateTime(), $dateFormat)"/>
      </harvestedDate>


      <!-- For multilingual docs it is good to have a title in the default locale.  In this type of metadata we don't have one but in the general case we do so we need to add it to all -->
      <resourceTitle><xsl:value-of select="string(dc:title)"/></resourceTitle>

      <xsl:for-each select="dc:language">
        <mainLanguage><xsl:value-of select="string(.)"/></mainLanguage>
      </xsl:for-each>

      <xsl:for-each select="dct:abstract|dc:description">
        <resourceAbstract><xsl:value-of select="string(.)"/></resourceAbstract>
      </xsl:for-each>

      <xsl:for-each select="dc:date">
        <creationDateForResource><xsl:value-of select="date-util:convertToISOZuluDateTime(string(.))"/></creationDateForResource>
      </xsl:for-each>

      <xsl:for-each select="dct:modified">
        <revisionDateForResource><xsl:value-of select="date-util:convertToISOZuluDateTime(string(.))"/></revisionDateForResource>
      </xsl:for-each>

      <xsl:for-each select="dc:format">
        <format><xsl:value-of select="string(.)"/></format>
      </xsl:for-each>

      <xsl:for-each select="dc:type[. != '']">
        <resourceType><xsl:value-of select="string(.)"/></resourceType>
      </xsl:for-each>

      <xsl:for-each select="dc:source">
        <lineage><xsl:value-of select="string(.)"/></lineage>
      </xsl:for-each>

      <xsl:for-each select="dc:relation">
        <related><xsl:value-of select="string(.)"/></related>
      </xsl:for-each>

      <xsl:for-each select="dct:accessRights">
        <useLimitation><xsl:value-of select="string(.)"/></useLimitation>
      </xsl:for-each>

      <xsl:for-each select="dct:rights">
        <useLimitation><xsl:value-of select="string(.)"/></useLimitation>
      </xsl:for-each>

      <xsl:variable name="geoTags"
                    select="dct:spatial[. != '']"/>
      <xsl:if test="count($geoTags) > 0">
        <keywordType-place type="object">[
          <xsl:for-each select="$geoTags">
            {"default": <xsl:value-of select="concat($doubleQuote, gn-fn-index:json-escape(.), $doubleQuote)"/>}
            <xsl:if test="position() != last()">,</xsl:if>
          </xsl:for-each>
        ]</keywordType-place>
      </xsl:if>

      <xsl:variable name="tags"
                    select="dc:subject[. != '']"/>
      <xsl:if test="count($tags) > 0">
        <tag type="object">[
          <xsl:for-each select="$tags">
            {"default": <xsl:value-of select="concat($doubleQuote, gn-fn-index:json-escape(.), $doubleQuote)"/>}
            <xsl:if test="position() != last()">,</xsl:if>
          </xsl:for-each>
          ]</tag>
      </xsl:if>



      <xsl:for-each select="(dct:references|dc:relation)[normalize-space(.) != '']">
        <xsl:variable name="name" select="tokenize(., '/')[last()]"/>
        <!-- Index link where last token after the last / is the link name. -->
        <link type="object">{
          "protocol":"<xsl:value-of select="'WWW-LINK'"/>",
          "url":"<xsl:value-of select="gn-fn-index:json-escape(.)"/>",
          "name":"<xsl:value-of select="gn-fn-index:json-escape($name)"/>",
          "description":""
          }</link>
      </xsl:for-each>
      <xsl:for-each select="(dct:references|dc:relation)[normalize-space(.) != ''
                              and matches(., '.*(.gif|.png.|.jpeg|.jpg)$', 'i')]">
        <xsl:variable name="thumbnailType"
                      select="if (position() = 1) then 'thumbnail' else 'overview'"/>
        <!-- First thumbnail is flagged as thumbnail and could be considered the main one -->
        <overviewUrl><xsl:value-of select="concat($thumbnailType, '|', ., '|')"/></overviewUrl>
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
                    <geom>
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
