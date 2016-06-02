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
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:ows="http://www.opengis.net/ows"
                xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
                version="2.0"
>

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
                  select="if (normalize-space(dc:language) != '')
                          then string(dc:language) else 'eng'"/>

    <Document locale="{$langCode}">
      <Field name="_locale" string="{$langCode}" store="true" index="true"/>
      <Field name="_docLocale" string="{$langCode}" store="true" index="true"/>
      <Field name="_defaultTitle" string="{string(/csw:Record/dc:title)}" store="true"
             index="true"/>
      <!-- This index for "coverage" requires significant expansion to
                 work well for spatial searches. It now only works for very
                 strictly formatted content -->
      <xsl:variable name="coverage" select="/csw:Record/ows:BoundingBox"/>
      <xsl:variable name="north" select="substring-before($coverage/ows:UpperCorner,' ')"/>
      <xsl:variable name="south" select="substring-before($coverage/ows:LowerCorner,' ')"/>
      <xsl:variable name="east" select="substring-after($coverage/ows:UpperCorner,' ')"/>
      <xsl:variable name="west" select="substring-after($coverage/ows:LowerCorner,' ')"/>

      <xsl:for-each select="/csw:Record/dc:identifier">
        <Field name="identifier" string="{string(.)}" store="false" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="/csw:Record/dct:abstract">
        <Field name="abstract" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="/csw:Record/dc:date">
        <Field name="createDate" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>


      <xsl:for-each select="/csw:Record/dct:modified">
        <Field name="changeDate" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="/csw:Record/dc:format">
        <Field name="format" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="/csw:Record/dc:type">
        <Field name="type" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="/csw:Record/dc:relation">
        <Field name="relation" string="{string(.)}" store="false" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="/csw:Record/dct:spatial">
        <Field name="spatial" string="{string(.)}" store="false" index="true"/>
      </xsl:for-each>

      <!-- This is needed by the CITE test script to look for strings like 'a b*'
                  strings that contain spaces -->

      <xsl:for-each select="/csw:Record/dc:title">
        <Field name="title" string="{string(.)}" store="true" index="true"/>
        <Field name="_title" string="{string(.)}" store="false" index="true"/>
      </xsl:for-each>

      <xsl:apply-templates select="/csw:Record/dc:description">
        <xsl:with-param name="name" select="'description'"/>
      </xsl:apply-templates>

      <Field name="westBL" string="{$west}" store="false" index="true"/>
      <Field name="eastBL" string="{$east}" store="false" index="true"/>
      <Field name="southBL" string="{$south}" store="false" index="true"/>
      <Field name="northBL" string="{$north}" store="false" index="true"/>
      <xsl:if test="$west and $south and $east and $north">
        <Field name="geoBox" string="{concat($west, '|',
          $south, '|',
          $east, '|',
          $north
          )}" store="true" index="false"/>
      </xsl:if>

      <xsl:for-each select="/csw:Record/dc:subject">
        <xsl:apply-templates select=".">
          <xsl:with-param name="name" select="'keyword'"/>
          <xsl:with-param name="store" select="'true'"/>
        </xsl:apply-templates>

        <xsl:apply-templates select=".">
          <xsl:with-param name="name" select="'subject'"/>
          <xsl:with-param name="store" select="'true'"/>
        </xsl:apply-templates>
      </xsl:for-each>

      <Field name="any" store="false" index="true">
        <xsl:attribute name="string">
          <xsl:value-of select="normalize-space(string(/csw:Record))"/>
          <xsl:text> </xsl:text>
          <xsl:for-each select="//*/@*">
            <xsl:value-of select="concat(., ' ')"/>
          </xsl:for-each>
        </xsl:attribute>
      </Field>

      <!-- locally searchable fields -->

      <!-- defaults to true -->
      <Field name="digital" string="true" store="false" index="true"/>

      <Field name="responsibleParty"
             string="{concat('creator', '|metadata|', /csw:Record/dc:creator, '|')}" store="true"
             index="false"/>

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

