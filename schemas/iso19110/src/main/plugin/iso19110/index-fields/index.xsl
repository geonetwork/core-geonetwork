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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gfc="http://www.isotc211.org/2005/gfc"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                version="2.0">

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

  <xsl:template match="/">
    <doc>
      <documentType>metadata</documentType>
      <documentStandard>iso19139</documentStandard>

      <resourceTitle>
        <xsl:value-of select="/gfc:FC_FeatureCatalogue/gmx:name/gco:CharacterString|
        /gfc:FC_FeatureCatalogue/gfc:name/gco:CharacterString|
        /gfc:FC_FeatureType/gfc:typeName/gco:LocalName"/>
      </resourceTitle>

      <resourceAbstract>
        <xsl:value-of select="/gfc:FC_FeatureCatalogue/gmx:scope/gco:CharacterString|
        /gfc:FC_FeatureCatalogue/gfc:scope/gco:CharacterString|
        /gfc:FC_FeatureType/gfc:definition/gco:CharacterString"/>
      </resourceAbstract>

      <uuid>
        <xsl:value-of select="/gfc:FC_FeatureCatalogue/@uuid|/gfc:FC_FeatureType/@uuid"/>
      </uuid>

      <xsl:for-each select="/gfc:FC_FeatureCatalogue/gmx:versionDate/gco:Date|
        /gfc:FC_FeatureCatalogue/gfc:versionDate/gco:Date">
        <Field name="revisionDate" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <!--<xsl:variable name="attributes"
                    select=".//gfc:carrierOfCharacteristics"/>
      <xsl:if test="count($attributes) > 0">
        <xsl:variable name="jsonAttributeTable">
          [
          <xsl:for-each select="$attributes">
            {"name": "<xsl:value-of select="*/gfc:memberName/*/text()"/>",
            "definition": "<xsl:value-of select="*/gfc:definition/*/text()"/>",
            "code": "<xsl:value-of select="*/gfc:code/*/text()"/>",
            "link": "<xsl:value-of select="*/gfc:code/*/@xlink:href"/>",
            "type": "<xsl:value-of select="*/gfc:valueType/gco:TypeName/gco:aName/*/text()"/>"
            <xsl:if test="*/gfc:listedValue">
              ,"values": [
              <xsl:for-each select="*/gfc:listedValue">{
                "label": "<xsl:value-of select="*/gfc:label/*/text()"/>",
                "code": "<xsl:value-of select="*/gfc:code/*/text()"/>",
                "definition": "<xsl:value-of select="*/gfc:definition/*/text()"/>"}
                <xsl:if test="position() != last()">,</xsl:if>
              </xsl:for-each>
              ]
            </xsl:if>
            }
            <xsl:if test="position() != last()">,</xsl:if>
          </xsl:for-each>
          ]
        </xsl:variable>
        <Field name="attributeTable" index="true" store="true"
               string="{$jsonAttributeTable}"/>
      </xsl:if>

      <xsl:for-each
        select="/gfc:FC_FeatureCatalogue/gfc:producer/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString">
        <xsl:variable name="role" select="../../gmd:role/*/@codeListValue"/>
        <xsl:variable name="logo" select="descendant::*/gmx:FileName/@src"/>

        <Field name="orgName" string="{string(.)}" store="false" index="true"/>
        <Field name="responsibleParty" string="{concat($role, '|metadata|', ., '|', $logo)}"
               store="true" index="false"/>
      </xsl:for-each>

      <Field name="any" store="false" index="true">
        <xsl:attribute name="string">
          <xsl:value-of
            select="normalize-space(string(/gfc:FC_FeatureCatalogue|/gfc:FC_FeatureType))"/>
          <xsl:text> </xsl:text>
          <xsl:for-each select="//@codeListValue">
            <xsl:value-of select="concat(., ' ')"/>
          </xsl:for-each>
        </xsl:attribute>
      </Field>-->

    </doc>
  </xsl:template>
</xsl:stylesheet>
