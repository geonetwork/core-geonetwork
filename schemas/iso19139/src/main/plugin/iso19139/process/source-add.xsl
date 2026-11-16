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

<!--
Stylesheet used to update metadata adding a reference to a source record.
-->
<xsl:stylesheet xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                version="2.0"
                exclude-result-prefixes="#all">

  <!-- Catalogue base URL. Used to build the link to the source record
     when sourceUrl is empty. -->
  <xsl:param name="siteUrl"/>

  <!-- Parameters describing a single source record.
       Only used when relatedRecords is empty. -->
  <!-- UUID of the source record to link.
       Set as the uuidref attribute. -->
  <xsl:param name="sourceUuid"/>
  <!-- Link to the source record. Optional: when empty a CSW
       GetRecordById URL is built from siteUrl and sourceUuid. -->
  <xsl:param name="sourceUrl" select="''"/>
  <!-- Title of the source record. Optional: when empty no
       xlink:title attribute is added. -->
  <xsl:param name="sourceTitle" select="''"/>

  <!-- A list of source records as a comma separated list of
       uuid#title#url tokens. Used when linking more than one source.
       When set, it takes precedence over the parameters above. -->
  <xsl:param name="relatedRecords" select="''"/>

  <!-- Decode the separators escaped by the client in the relatedRecords
       parameter.
       '%25' is decoded last so that a value which already contained a
       percent escape (eg. a title with '%2C' in it) is restored as is. -->
  <xsl:function name="geonet:unescape">
    <xsl:param name="value"/>
    <xsl:value-of select="replace(replace(replace($value,
                            '%23', '#'),
                            '%2C', ','),
                            '%25', '%')"/>
  </xsl:function>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="gmd:MD_Metadata | *[contains(@gco:isoType, 'MD_Metadata')]" priority="2">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="
        gmd:fileIdentifier | gmd:language | gmd:characterSet | gmd:parentIdentifier | gmd:hierarchyLevel |
        gmd:hierarchyLevelName | gmd:contact | gmd:dateStamp | gmd:metadataStandardName | gmd:metadataStandardVersion |
        gmd:dataSetURI | gmd:locale | gmd:spatialRepresentationInfo | gmd:referenceSystemInfo | gmd:metadataExtensionInfo |
        gmd:identificationInfo | gmd:contentInfo | gmd:distributionInfo"/>
      <xsl:choose>
        <xsl:when test="gmd:dataQualityInfo">
          <xsl:apply-templates select="gmd:dataQualityInfo"/>
        </xsl:when>
        <xsl:otherwise>
          <gmd:dataQualityInfo>
            <gmd:DQ_DataQuality>
              <gmd:lineage>
                <gmd:LI_Lineage>
                  <xsl:call-template name="make-source-link"/>
                </gmd:LI_Lineage>
              </gmd:lineage>
            </gmd:DQ_DataQuality>
          </gmd:dataQualityInfo>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="
        gmd:portrayalCatalogueInfo | gmd:metadataConstraints | gmd:applicationSchemaInfo | gmd:metadataMaintenance |
        gmd:series | gmd:describes | gmd:propertyType | gmd:featureType | gmd:featureAttribute"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="gmd:dataQualityInfo/*[not(gmd:lineage)]" priority="2">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="*[name() != 'gmd:lineage']"/>
      <gmd:lineage>
        <gmd:LI_Lineage>
          <xsl:call-template name="make-source-link"/>
        </gmd:LI_Lineage>
      </gmd:lineage>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="gmd:LI_Lineage|*[contains(@gco:isoType, 'LI_Lineage')]" priority="2">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:copy-of select="gmd:statement|gmd:processStep|gmd:source"/>

      <xsl:call-template name="make-source-link"/>
    </xsl:copy>
  </xsl:template>

  <!-- Add one source per record, either from the single record
       parameters or from the relatedRecords list. -->
  <xsl:template name="make-source-link">
    <xsl:choose>
      <xsl:when test="$relatedRecords != ''">
        <xsl:for-each select="tokenize($relatedRecords, ',')">
          <xsl:variable name="token" select="tokenize(., '#')"/>
          <xsl:call-template name="make-one-source-link">
            <xsl:with-param name="uuid" select="$token[1]"/>
            <xsl:with-param name="title" select="geonet:unescape(($token[2], '')[1])"/>
            <xsl:with-param name="url" select="geonet:unescape(($token[3], '')[1])"/>
          </xsl:call-template>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="make-one-source-link">
          <xsl:with-param name="uuid" select="$sourceUuid"/>
          <xsl:with-param name="title" select="$sourceTitle"/>
          <xsl:with-param name="url" select="$sourceUrl"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="make-one-source-link">
    <xsl:param name="uuid"/>
    <xsl:param name="title" select="''"/>
    <xsl:param name="url" select="''"/>

    <gmd:source uuidref="{$uuid}">
      <xsl:if test="$title != ''">
        <xsl:attribute name="xlink:title" select="$title"/>
      </xsl:if>
      <xsl:choose>
        <xsl:when test="$url != ''">
          <xsl:attribute name="xlink:href" select="$url"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="xlink:href"
                         select="concat($siteUrl, 'csw?service=CSW&amp;request=GetRecordById&amp;version=2.0.2&amp;outputSchema=http://www.isotc211.org/2005/gmd&amp;elementSetName=full&amp;id=', $uuid)"/>
        </xsl:otherwise>
      </xsl:choose>
    </gmd:source>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>
</xsl:stylesheet>
