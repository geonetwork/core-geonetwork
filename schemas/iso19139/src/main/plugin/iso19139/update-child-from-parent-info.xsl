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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                version="1.0"
                exclude-result-prefixes="gmd srv">


  <!-- Parameters -->
  <xsl:param name="updateMode" select="'replace'"/>

  <xsl:param name="gmd-contact"/>
  <xsl:param name="gmd-spatialRepresentationInfo"/>
  <xsl:param name="gmd-referenceSystemInfo"/>
  <xsl:param name="gmd-metadataExtensionInfo"/>
  <xsl:param name="gmd-pointOfContact"/>
  <xsl:param name="gmd-descriptiveKeywords"/>
  <xsl:param name="gmd-extent"/>
  <xsl:param name="gmd-contentInfo"/>
  <xsl:param name="gmd-distributionInfo"/>
  <xsl:param name="gmd-dataQualityInfo"/>
  <xsl:param name="gmd-portrayalCatalogueInfo"/>
  <xsl:param name="gmd-metadataConstraints"/>
  <xsl:param name="gmd-applicationSchemaInfo"/>
  <xsl:param name="gmd-metadataMaintenance"/>

  <!-- ================================================================= -->

  <xsl:template match="/">
    <xsl:apply-templates select="/root/update/parent/gmd:MD_Metadata"/>
  </xsl:template>

  <!-- ================================================================= -->

  <xsl:template match="/root/update/parent/gmd:MD_Metadata">
    <xsl:copy>
      <xsl:copy-of select="/root/child/gmd:MD_Metadata/gmd:fileIdentifier"/>
      <xsl:copy-of select="/root/child/gmd:MD_Metadata/gmd:language|gmd:characterSet"/>
      <xsl:copy-of select="/root/child/gmd:MD_Metadata/gmd:parentIdentifier"/>
      <xsl:copy-of select="/root/child/gmd:MD_Metadata/gmd:hierarchyLevel"/>
      <xsl:copy-of select="/root/child/gmd:MD_Metadata/gmd:hierarchyLevelName"/>

      <xsl:call-template name="process">
        <xsl:with-param name="update" select="$gmd-contact"/>
        <xsl:with-param name="name" select="gmd:contact"/>
        <xsl:with-param name="mode" select="$updateMode"/>
      </xsl:call-template>

      <xsl:copy-of select="gmd:dateStamp"/>
      <xsl:copy-of select="gmd:metadataStandardName"/>
      <xsl:copy-of select="gmd:metadataStandardVersion"/>

      <xsl:copy-of select="/root/child/gmd:MD_Metadata/gmd:dataSetURI"/>
      <xsl:copy-of select="gmd:locale"/>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:call-template name="process">
        <xsl:with-param name="update" select="$gmd-spatialRepresentationInfo"/>
        <xsl:with-param name="name" select="gmd:spatialRepresentationInfo"/>
        <xsl:with-param name="mode" select="$updateMode"/>
      </xsl:call-template>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:call-template name="process">
        <xsl:with-param name="update" select="$gmd-referenceSystemInfo"/>
        <xsl:with-param name="name" select="gmd:referenceSystemInfo"/>
        <xsl:with-param name="mode" select="$updateMode"/>
      </xsl:call-template>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:call-template name="process">
        <xsl:with-param name="update" select="$gmd-metadataExtensionInfo"/>
        <xsl:with-param name="name" select="gmd:metadataExtensionInfo"/>
        <xsl:with-param name="mode" select="$updateMode"/>
      </xsl:call-template>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
      <!-- Identification -->
      <gmd:identificationInfo>
        <xsl:for-each select="/root/child/gmd:MD_Metadata/gmd:identificationInfo/*">
          <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:copy-of select="gmd:citation"/>
            <xsl:copy-of select="gmd:abstract"/>

            <!-- FIXME / TO BE DISCUSS following sections are preserved -->
            <xsl:copy-of select="gmd:purpose"/>
            <xsl:copy-of select="gmd:credit"/>
            <xsl:copy-of select="gmd:status"/>

            <xsl:call-template name="process">
              <xsl:with-param name="update" select="$gmd-pointOfContact"/>
              <xsl:with-param name="name"
                              select="/root/update/parent/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:pointOfContact"/>
              <xsl:with-param name="mode" select="$updateMode"/>
              <xsl:with-param name="subLevel" select="true()"/>
            </xsl:call-template>

            <xsl:copy-of
              select="/root/update/parent/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:resourceMaintenance"/>
            <xsl:copy-of select="gmd:graphicOverview"/>
            <xsl:copy-of
              select="/root/update/parent/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:resourceFormat"/>

            <xsl:call-template name="process">
              <xsl:with-param name="update" select="$gmd-descriptiveKeywords"/>
              <xsl:with-param name="name"
                              select="/root/update/parent/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:descriptiveKeywords"/>
              <xsl:with-param name="mode" select="$updateMode"/>
              <xsl:with-param name="subLevel" select="true()"/>
            </xsl:call-template>

            <!-- FIXME / TO BE DISCUSS following sections are replaced (excepts AggregationInfo preserved). -->
            <xsl:copy-of
              select="/root/update/parent/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:resourceSpecificUsage"/>
            <xsl:copy-of
              select="/root/update/parent/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:resourceConstraints"/>
            <xsl:copy-of select="gmd:aggregationInfo"/>
            <xsl:copy-of
              select="/root/update/parent/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:spatialRepresentationType"/>
            <xsl:copy-of
              select="/root/update/parent/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:spatialResolution"/>
            <xsl:copy-of
              select="/root/update/parent/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:langage"/>
            <xsl:copy-of
              select="/root/update/parent/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:characterSet"/>
            <xsl:copy-of
              select="/root/update/parent/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:topicCategory"/>
            <xsl:copy-of
              select="/root/update/parent/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:environmentDescription"/>

            <xsl:call-template name="process">
              <xsl:with-param name="update" select="$gmd-extent"/>
              <xsl:with-param name="name"
                              select="/root/update/parent/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:extent"/>
              <xsl:with-param name="mode" select="$updateMode"/>
              <xsl:with-param name="subLevel" select="true()"/>
            </xsl:call-template>

            <!-- FIXME / TO BE DISCUSS following sections are replaced/preserved  -->
            <xsl:copy-of
              select="/root/update/parent/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:supplementalInformation"/>
            <xsl:copy-of select="srv:*"/>

            <!-- Note: When applying this stylesheet
                      to an ISO profil having a new substitute for
                      MD_Identification, profil specific element copy.
                  -->
            <xsl:for-each
              select="*[namespace-uri()!='http://www.isotc211.org/2005/gmd' and namespace-uri()!='http://www.isotc211.org/2005/srv']">
              <xsl:copy-of select="."/>
            </xsl:for-each>
          </xsl:copy>
        </xsl:for-each>
      </gmd:identificationInfo>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:call-template name="process">
        <xsl:with-param name="update" select="$gmd-contentInfo"/>
        <xsl:with-param name="name" select="gmd:contentInfo"/>
        <xsl:with-param name="mode" select="$updateMode"/>
      </xsl:call-template>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
      <!-- Distribution -->

      <xsl:call-template name="process">
        <xsl:with-param name="update" select="$gmd-distributionInfo"/>
        <xsl:with-param name="name" select="gmd:distributionInfo"/>
        <!-- Force mode to replace element due to schema cardinality -->
        <xsl:with-param name="mode" select="'replace'"/>
      </xsl:call-template>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
      <!-- Quality -->

      <xsl:call-template name="process">
        <xsl:with-param name="update" select="$gmd-dataQualityInfo"/>
        <xsl:with-param name="name" select="gmd:dataQualityInfo"/>
        <xsl:with-param name="mode" select="$updateMode"/>
      </xsl:call-template>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:call-template name="process">
        <xsl:with-param name="update" select="$gmd-portrayalCatalogueInfo"/>
        <xsl:with-param name="name" select="gmd:portrayalCatalogueInfo"/>
        <xsl:with-param name="mode" select="$updateMode"/>
      </xsl:call-template>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:call-template name="process">
        <xsl:with-param name="update" select="$gmd-metadataConstraints"/>
        <xsl:with-param name="name" select="gmd:metadataConstraints"/>
        <xsl:with-param name="mode" select="$updateMode"/>
      </xsl:call-template>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:call-template name="process">
        <xsl:with-param name="update" select="$gmd-applicationSchemaInfo"/>
        <xsl:with-param name="name" select="gmd:applicationSchemaInfo"/>
        <xsl:with-param name="mode" select="$updateMode"/>
      </xsl:call-template>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:call-template name="process">
        <xsl:with-param name="update" select="$gmd-metadataMaintenance"/>
        <xsl:with-param name="name" select="gmd:metadataMaintenance"/>
        <!-- Force mode to replace element due to schema cardinality -->
        <xsl:with-param name="mode" select="'replace'"/>
      </xsl:call-template>

      <!-- FIXME / TO BE DISCUSS following sections are preserved -->
      <xsl:copy-of select="/root/child/gmd:MD_Metadata/gmd:series"/>
      <xsl:copy-of select="/root/child/gmd:MD_Metadata/gmd:describes"/>
      <xsl:copy-of select="/root/child/gmd:MD_Metadata/gmd:propertyType"/>
      <xsl:copy-of select="/root/child/gmd:MD_Metadata/gmd:featureType"/>
      <xsl:copy-of select="/root/child/gmd:MD_Metadata/gmd:featureAttribute"/>

    </xsl:copy>
  </xsl:template>


  <!-- Generic template for children update -->
  <!-- Depending on the choosen strategy to be applied on each main sections (mode) -->
  <xsl:template name="process">
    <xsl:param name="update" select="false()"/>
    <xsl:param name="name"/>
    <xsl:param name="mode"/>
    <xsl:param name="subLevel" select="false()"></xsl:param>

    <xsl:variable name="childElement">
      <xsl:choose>
        <xsl:when test="$subLevel=true()">
          <xsl:copy-of
            select="/root/child/gmd:MD_Metadata/gmd:identificationInfo/*/*[name(.)=name($name)]"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="/root/child/gmd:MD_Metadata/*[name(.)=name($name)]"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:choose>
      <!-- Replacing elements from parent -->
      <xsl:when test="$mode='replace' and $update='true'">
        <xsl:copy-of select="$name"/>
      </xsl:when>
      <!-- Adding elements -->
      <xsl:when test="$mode='add' and $update='true'">
        <xsl:copy-of select="$childElement"/>
        <xsl:copy-of select="$name"/>
      </xsl:when>
      <!-- Elements preserved from child-->
      <xsl:otherwise>
        <xsl:copy-of select="$childElement"/>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>


</xsl:stylesheet>
