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
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:gml320="http://www.opengis.net/gml"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:tr="java:org.fao.geonet.api.records.formatters.SchemaLocalizations"
                xmlns:gn-fn-render="http://geonetwork-opensource.org/xsl/functions/render"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
                xmlns:xslUtils="java:org.fao.geonet.util.XslUtil"
                xmlns:saxon="http://saxon.sf.net/"
                version="2.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <xsl:template name="sextant-summary-view">
    <table class="table">
      <tr>
        <td><xsl:value-of select="$schemaStrings/sxt-view-date"/></td>
        <td>
          <xsl:for-each select="$metadata/gmd:identificationInfo/*/gmd:citation/*/gmd:date/*">
            <xsl:apply-templates mode="render-value" select="gmd:date"/>
            - <xsl:apply-templates mode="render-value" select="gmd:dateType/*/@codeListValue"/><br/>
          </xsl:for-each>
        </td>
      </tr>
      <tr>
        <td><xsl:value-of select="$schemaStrings/sxt-view-author"/></td>
        <td>
          <xsl:apply-templates mode="render-field"
                               select="$metadata/gmd:identificationInfo/*/gmd:pointOfContact[*/gmd:role/*/@codeListValue = 'author']"/>
        </td>
      </tr>
      <tr>
        <td><xsl:value-of select="$schemaStrings/sxt-view-contact"/></td>
        <td>
          <xsl:apply-templates mode="render-field"
                               select="$metadata/gmd:identificationInfo/*/gmd:pointOfContact[*/gmd:role/*/@codeListValue != 'author']"/>
        </td>
      </tr>
      <tr>
        <td><xsl:value-of select="$schemaStrings/sxt-view-keyword"/></td>
        <td>
          <xsl:apply-templates mode="render-field"
                               select="$metadata/gmd:identificationInfo/*/gmd:descriptiveKeywords"/>
        </td>
      </tr>
      <tr>
        <td><xsl:value-of select="$schemaStrings/sxt-view-lineage"/></td>
        <td>
          <xsl:apply-templates mode="render-field"
                               select="$metadata/gmd:dataQualityInfo/*/gmd:lineage/*/gmd:statement"/>
        </td>
      </tr>
      <tr>
        <td><xsl:value-of select="$schemaStrings/sxt-view-constraint"/></td>
        <td>
          <xsl:apply-templates mode="render-field"
                               select="$metadata/gmd:identificationInfo/*/gmd:resourceConstraints/*/*"/>
        </td>
      </tr>
    </table>
  </xsl:template>
</xsl:stylesheet>
