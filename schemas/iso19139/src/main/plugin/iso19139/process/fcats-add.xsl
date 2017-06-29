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

  <xsl:param name="uuidref"></xsl:param>
  <xsl:param name="siteUrl"></xsl:param>

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


      <xsl:choose>
        <!-- Check if featureCatalogueCitation for uuidref -->
        <xsl:when
          test="gmd:contentInfo/gmd:MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation[@uuidref = $uuidref]">
          <gmd:contentInfo>
            <gmd:MD_FeatureCatalogueDescription>
              <xsl:copy-of select="gmd:contentInfo/gmd:MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation[@uuidref = $uuidref]/../gmd:complianceCode|
                                   gmd:contentInfo/gmd:MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation[@uuidref = $uuidref]/../gmd:language|
                                   gmd:contentInfo/gmd:MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation[@uuidref = $uuidref]/../gmd:includedWithDataset|
                                   gmd:contentInfo/gmd:MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation[@uuidref = $uuidref]/../gmd:featureTypes"/>

              <!-- Add xlink:href featureCatalogueCitation -->
              <gmd:featureCatalogueCitation uuidref="{$uuidref}"
                                            xlink:href="{$siteUrl}csw?service=CSW&amp;request=GetRecordById&amp;version=2.0.2&amp;outputSchema=http://www.isotc211.org/2005/gmd&amp;elementSetName=full&amp;id={$uuidref}">
                <xsl:copy-of
                  select="gmd:contentInfo/gmd:MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation[@uuidref = $uuidref]/gmd:CI_Citation"/>
              </gmd:featureCatalogueCitation>

            </gmd:MD_FeatureCatalogueDescription>
          </gmd:contentInfo>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="gmd:contentInfo"/>
          <gmd:contentInfo>
            <gmd:MD_FeatureCatalogueDescription>
              <gmd:includedWithDataset/>
              <gmd:featureCatalogueCitation uuidref="{$uuidref}"
                                            xlink:href="{$siteUrl}csw?service=CSW&amp;request=GetRecordById&amp;version=2.0.2&amp;outputSchema=http://www.isotc211.org/2005/gmd&amp;elementSetName=full&amp;id={$uuidref}"/>
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


  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Always remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

</xsl:stylesheet>
