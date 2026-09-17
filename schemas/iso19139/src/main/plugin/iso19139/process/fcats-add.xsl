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
Stylesheet used to update metadata for a service and
attached it to the metadata for data.
-->
<xsl:stylesheet xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                version="2.0"
                exclude-result-prefixes="#all">

  <!-- Catalogue base URL. Used to build the link to the feature
     catalogue when fcatsUrl is empty. -->
  <xsl:param name="siteUrl"/>

  <!-- Parameters describing a single feature catalogue.
       Only used when relatedRecords is empty. -->
  <!-- UUID of the feature catalogue to link.
       Set as the uuidref attribute. -->
  <xsl:param name="uuidref"/>
  <!-- Link to the feature catalogue. Optional: when empty a CSW
       GetRecordById URL is built from siteUrl and uuidref. -->
  <xsl:param name="fcatsUrl" select="''"/>
  <!-- Title of the feature catalogue. Optional: when empty no
       xlink:title attribute is added. -->
  <xsl:param name="fcatsTitle" select="''"/>

  <!-- A list of feature catalogues as a comma separated list of
       uuid#title#url tokens. Used when linking more than one catalogue.
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

  <xsl:template match="/gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:copy-of
        select="gmd:fileIdentifier|
                gmd:language|
                gmd:characterSet|
                gmd:parentIdentifier|
                gmd:hierarchyLevel|
                gmd:hierarchyLevelName|
                gmd:contact|
                gmd:dateStamp|
                gmd:metadataStandardName|
                gmd:metadataStandardVersion|
                gmd:dataSetURI|
                gmd:locale|
                gmd:spatialRepresentationInfo|
                gmd:referenceSystemInfo|
                gmd:metadataExtensionInfo|
                gmd:identificationInfo"/>


      <xsl:variable name="citationWithRef"
                    select="if ($uuidref != '')
                            then gmd:contentInfo/*/gmd:featureCatalogueCitation[@uuidref = $uuidref]
                            else ()"/>
      <xsl:choose>
        <!-- Check if featureCatalogueCitation for uuidref -->
        <xsl:when
          test="$citationWithRef">
          <gmd:contentInfo>
            <gmd:MD_FeatureCatalogueDescription>
              <xsl:copy-of select="$citationWithRef/../gmd:complianceCode|
                                   $citationWithRef/../gmd:language|
                                   $citationWithRef/../gmd:includedWithDataset|
                                   $citationWithRef/../gmd:featureTypes"/>

              <xsl:call-template name="make-fcats-link"/>

            </gmd:MD_FeatureCatalogueDescription>
          </gmd:contentInfo>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="gmd:contentInfo"/>
          <gmd:contentInfo>
            <gmd:MD_FeatureCatalogueDescription>
              <gmd:includedWithDataset/>
              <xsl:call-template name="make-fcats-links"/>
            </gmd:MD_FeatureCatalogueDescription>
          </gmd:contentInfo>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:copy-of select="gmd:distributionInfo|
                            gmd:dataQualityInfo|
                            gmd:portrayalCatalogueInfo|
                            gmd:metadataConstraints|
                            gmd:applicationSchemaInfo|
                            gmd:metadataMaintenance|
                            gmd:series|
                            gmd:describes|
                            gmd:propertyType|
                            gmd:featureType|
                            gmd:featureAttribute"/>


      <xsl:apply-templates select="*[namespace-uri()!='http://www.isotc211.org/2005/gmd' and
                                     namespace-uri()!='http://www.isotc211.org/2005/srv']"/>
    </xsl:copy>
  </xsl:template>

  <!-- Add one citation per record, either from the single record
       parameters or from the relatedRecords list. -->
  <xsl:template name="make-fcats-links">
    <xsl:choose>
      <xsl:when test="$relatedRecords != ''">
        <xsl:for-each select="tokenize($relatedRecords, ',')">
          <xsl:variable name="token" select="tokenize(., '#')"/>
          <xsl:call-template name="make-fcats-link">
            <xsl:with-param name="uuid" select="$token[1]"/>
            <xsl:with-param name="title" select="geonet:unescape(($token[2], '')[1])"/>
            <xsl:with-param name="url" select="geonet:unescape(($token[3], '')[1])"/>
          </xsl:call-template>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="make-fcats-link"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="make-fcats-link">
    <xsl:param name="uuid" select="$uuidref"/>
    <xsl:param name="title" select="$fcatsTitle"/>
    <xsl:param name="url" select="$fcatsUrl"/>

    <gmd:featureCatalogueCitation uuidref="{$uuid}">
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
    </gmd:featureCatalogueCitation>
  </xsl:template>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Always remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

</xsl:stylesheet>
