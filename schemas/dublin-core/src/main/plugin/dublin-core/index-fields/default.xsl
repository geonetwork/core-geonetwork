<?xml version="1.0" encoding="UTF-8" ?>
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
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:java="java:org.fao.geonet.util.XslUtil"
                xmlns:dct="http://purl.org/dc/terms/"
                version="2.0">

  <!-- This file defines what parts of the metadata are indexed by Lucene
    Searches can be conducted on indexes defined here.
    The Field@name attribute defines the name of the search variable.
    If a variable has to be maintained in the user session, it needs to be
    added to the GeoNetwork constants in the Java source code.
    Please keep indexes consistent among metadata standards if they should
    work accross different metadata resources -->
  <!-- ========================================================================================= -->

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

  <!-- ========================================================================================= -->

  <xsl:template match="/">

    <xsl:variable name="langCode"
                  select="if (normalize-space(/simpledc/dc:language) != '')
                          then string(/simpledc/dc:language) else 'eng'"/>

    <Document locale="{$langCode}">

      <!-- locale information -->
      <Field name="_locale" string="{$langCode}" store="true" index="true"/>
      <Field name="_docLocale" string="{$langCode}" store="true" index="true"/>

      <!-- For multilingual docs it is good to have a title in the default locale.  In this type of metadata we don't have one but in the general case we do so we need to add it to all -->
      <Field name="_defaultTitle" string="{string(/simpledc/dc:title)}" store="true" index="true"/>

      <xsl:for-each select="/simpledc/dc:language">
        <Field name="mdLanguage" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="/simpledc/dc:identifier">
        <Field name="identifier" string="{string(.)}" store="false" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="/simpledc/dct:abstract|/simpledc/dc:description">
        <Field name="abstract" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="/simpledc/dc:date">
        <Field name="createDate" string="{string(.)}" store="true" index="true"/>
        <Field name="createDateYear" string="{substring(., 0, 5)}" store="true" index="true"/>
      </xsl:for-each>


      <xsl:for-each select="/simpledc/dct:modified">
        <Field name="changeDate" string="{string(.)}" store="true" index="true"/>
        <!--<Field name="createDateYear" string="{substring(., 0, 5)}" store="true" index="true"/>-->
      </xsl:for-each>

      <xsl:for-each select="/simpledc/dc:format">
        <Field name="format" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="/simpledc/dc:type">
        <Field name="type" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="/simpledc/dc:source">
        <Field name="lineage" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="/simpledc/dc:relation">
        <Field name="relation" string="{string(.)}" store="false" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="/simpledc/dct:accessRights">
        <Field name="MD_ConstraintsUseLimitation" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>
      <xsl:for-each select="/simpledc/dc:rights">
        <Field name="MD_LegalConstraintsUseLimitation" string="{string(.)}" store="true"
               index="true"/>
      </xsl:for-each>

      <xsl:for-each select="/simpledc/dct:spatial">
        <Field name="spatial" string="{string(.)}" store="false" index="true"/>
      </xsl:for-each>

      <!-- This is needed by the CITE test script to look for strings like 'a b*'
          strings that contain spaces -->

      <xsl:for-each select="/simpledc/dc:title">
        <Field name="title" string="{string(.)}" store="true" index="true"/>
        <!-- not tokenized title for sorting -->
        <Field name="_title" string="{string(.)}" store="false" index="true"/>
      </xsl:for-each>


      <xsl:for-each select="/simpledc/(dct:references|dc:relation)[normalize-space(.) != '']">
        <xsl:variable name="name" select="tokenize(., '/')[last()]"/>
        <!-- Index link where last token after the last / is the link name. -->
        <Field name="link"
               string="{concat($name, '||', ., '|WWW-LINK|WWW:LINK|0')}"
               store="true"
               index="false"/>
      </xsl:for-each>
      <xsl:for-each select="/simpledc/(dct:references|dc:relation)[normalize-space(.) != ''
                              and matches(., '.*(.gif|.png.|.jpeg|.jpg)$', 'i')]">
        <xsl:variable name="thumbnailType"
                      select="if (position() = 1) then 'thumbnail' else 'overview'"/>
        <!-- First thumbnail is flagged as thumbnail and could be considered the main one -->
        <Field name="image"
               string="{concat($thumbnailType, '|', ., '|')}"
               store="true" index="false"/>
      </xsl:for-each>


      <!-- This index for "coverage" requires significant expansion to
         work well for spatial searches. It now only works for very
         strictly formatted content -->
      <xsl:for-each select="/simpledc/dc:coverage">
        <xsl:variable name="coverage" select="."/>

        <!-- North 46.3, South 42.51, East 3.88, West -1.84 -->
        <xsl:choose>
          <xsl:when test="starts-with(., 'North')">
            <xsl:variable name="n" select="substring-after($coverage,'North ')"/>
            <xsl:variable name="north" select="substring-before($n, ',')"/>
            <xsl:variable name="s" select="substring-after($coverage,'South ')"/>
            <xsl:variable name="south" select="substring-before($s, ',')"/>
            <xsl:variable name="e" select="substring-after($coverage,'East ')"/>
            <xsl:variable name="east" select="substring-before($e, ',')"/>
            <xsl:variable name="w" select="substring-after($coverage,'West ')"/>
            <xsl:variable name="west"
                          select="if (contains($w, '. ')) then substring-before($w, '. ') else $w"/>
            <xsl:variable name="p" select="substring-after($coverage,'(')"/>
            <xsl:variable name="place" select="substring-before($p,')')"/>

            <Field name="westBL" string="{$west}" store="false" index="true"/>
            <Field name="eastBL" string="{$east}" store="false" index="true"/>
            <Field name="southBL" string="{$south}" store="false" index="true"/>
            <Field name="northBL" string="{$north}" store="false" index="true"/>
            <Field name="geoBox" string="{concat($west, '|',
                                                  $south, '|',
                                                  $east, '|',
                                                  $north
                                                  )}" store="true" index="false"/>

            <Field name="keyword" string="{$place}" store="true" index="true"/>
          </xsl:when>

           <!-- Latitude 4.817 to 88.225; Longitude -179.950 to 180.000 -->
          <xsl:when test="starts-with(., 'Latitude')">
            <xsl:variable name="s" select="substring-after($coverage,'Latitude')"/>
            <xsl:variable name="south" select="normalize-space(substring-before($s, 'to'))"/>
            <xsl:variable name="n" select="substring-after($s,'to')"/>
            <xsl:variable name="north" select="normalize-space(substring-before($n, ';'))"/>
            <xsl:variable name="w" select="substring-after($n,'Longitude')"/>
            <xsl:variable name="west" select="normalize-space(substring-before($w, 'to'))"/>
            <xsl:variable name="e" select="substring-after($w,'to')"/>
            <xsl:variable name="east" select="normalize-space($e)"/>

            <Field name="westBL"  string="{$west}" store="false" index="true"/>
            <Field name="eastBL"  string="{$east}" store="false" index="true"/>
            <Field name="southBL" string="{$south}" store="false" index="true"/>
            <Field name="northBL" string="{$north}" store="false" index="true"/>
            <Field name="geoBox" string="{concat($west, '|',
              $south, '|',
              $east, '|',
              $north
              )}" store="true" index="false"/>
          </xsl:when>

          <!-- 4.817,-179.95 / 88.225,180 (min, max Latitude / min, max Longitude) -->
          <xsl:when test="ends-with(., '(min, max Latitude / min, max Longitude)')">
            <xsl:variable name="s" select="$coverage"/>
            <xsl:variable name="south" select="normalize-space(substring-before($s, ','))"/>
            <xsl:variable name="w" select="substring-after($s,',')"/>
            <xsl:variable name="west" select="normalize-space(substring-before($w, '/'))"/>
            <xsl:variable name="n" select="substring-after($w,'/')"/>
            <xsl:variable name="north" select="normalize-space(substring-before($n, ','))"/>
            <xsl:variable name="e" select="substring-after($n,',')"/>
            <xsl:variable name="east" select="normalize-space(substring-before($e, '(min, max Latitude / min, max Longitude)'))"/>

            <Field name="westBL"  string="{$west}" store="false" index="true"/>
            <Field name="eastBL"  string="{$east}" store="false" index="true"/>
            <Field name="southBL" string="{$south}" store="false" index="true"/>
            <Field name="northBL" string="{$north}" store="false" index="true"/>
            <Field name="geoBox" string="{concat($west, '|',
              $south, '|',
              $east, '|',
              $north
              )}" store="true" index="false"/>
          </xsl:when>

          <xsl:otherwise>
            <Field name="keyword" string="{.}" store="true" index="true"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>


      <xsl:apply-templates select="/simpledc/dc:subject">
        <xsl:with-param name="name" select="'keyword'"/>
        <xsl:with-param name="store" select="'true'"/>
      </xsl:apply-templates>

      <xsl:variable name="listOfKeywords">{
        <xsl:variable name="keywordWithNoThesaurus"
                      select="/simpledc/dc:subject[text() != '']"/>
        <xsl:if test="count($keywordWithNoThesaurus) > 0">
          'otherKeywords': [
          <xsl:for-each select="$keywordWithNoThesaurus">
            {'value': <xsl:value-of select="concat('''', replace(., '''', '\\'''), '''')"/>, 'link': ''}
            <xsl:if test="position() != last()">,</xsl:if>
          </xsl:for-each>
          ]
        </xsl:if>
        }
      </xsl:variable>

      <Field name="keywordGroup"
             string="{normalize-space($listOfKeywords)}"
             store="true"
             index="false"/>


      <xsl:for-each select="/simpledc/dct:isPartOf">
        <Field name="parentUuid" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <Field name="any" store="false" index="true">
        <xsl:attribute name="string">
          <xsl:value-of select="normalize-space(string(/simpledc))"/>
          <xsl:text> </xsl:text>
          <xsl:for-each select="//*/@*">
            <xsl:value-of select="concat(., ' ')"/>
          </xsl:for-each>
        </xsl:attribute>
      </Field>

      <!-- locally searchable fields -->

      <!-- defaults to true -->
      <Field name="digital" string="true" store="false" index="true"/>

      <xsl:for-each select="/simpledc/dc:publisher">
        <xsl:variable name="role"
                      select="java:getCodelistTranslation('gmd:CI_RoleCode',
                                                 'pointOfContact',
                                                 string($langCode))"/>
        <Field name="responsibleParty"
               string="{concat($role, '|resource|', ., '|')}" store="true" index="false"/>
      </xsl:for-each>

      <xsl:for-each select="/simpledc/dc:creator">
        <xsl:variable name="role"
                      select="java:getCodelistTranslation('gmd:CI_RoleCode',
                                                 'author',
                                                 string($langCode))"/>
        <Field name="responsibleParty"
               string="{concat($role, '|metadata|', ., '|')}" store="true" index="false"/>
      </xsl:for-each>

      <xsl:choose>
        <xsl:when test="/simpledc/dct:accrualPeriodicity">
          <xsl:for-each select="/simpledc/dct:accrualPeriodicity">
            <Field name="updateFrequency" string="{string(.)}" store="true" index="true"/>
            <Field name="cl_maintenanceAndUpdateFrequency_text"
                   string="{java:getCodelistTranslation('gmd:MD_MaintenanceFrequencyCode',
                                                 string(.),
                                                 string($langCode))}"
                   store="true" index="true"/>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <Field name="updateFrequency"
                 string="unknown"
                 store="true" index="true"/>
          <Field name="cl_maintenanceAndUpdateFrequency_text"
                 string="{java:getCodelistTranslation('gmd:MD_MaintenanceFrequencyCode',
                                                 'unknown',
                                                 string($langCode))}"
                 store="true" index="true"/>
        </xsl:otherwise>
      </xsl:choose>
    </Document>
  </xsl:template>

  <!-- ========================================================================================= -->

  <!-- text element, by default indexed, not stored, tokenized -->
  <xsl:template match="*">
    <xsl:param name="name" select="name(.)"/>
    <xsl:param name="store" select="'false'"/>
    <xsl:param name="index" select="'true'"/>

    <Field name="{$name}" string="{string(.)}" store="{$store}" index="{$index}"/>
  </xsl:template>

</xsl:stylesheet>
