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
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                exclude-result-prefixes="#all"
                version="2.0">
  <xsl:import href="../../../iso19139/formatter/eu-geodcat-ap-semiceu/view.xsl"/>
  <xsl:import href="../../convert/ISO19139/toISO19139.xsl"/>

  <xsl:output method="xml"
              indent="yes"/>

  <xsl:variable name="metadata"
                select="//mdb:MD_Metadata"/>

  <xsl:template match="/" priority="99">
    <xsl:variable name="iso19139" as="node()">
      <records>
        <xsl:for-each select="$metadata">
          <xsl:variable name="nameSpacePrefix">
            <xsl:call-template name="getNamespacePrefix"/>
          </xsl:variable>
          <xsl:element name="{concat($nameSpacePrefix,':',local-name(.))}">
            <xsl:call-template name="add-namespaces"/>

            <xsl:apply-templates select="mdb:metadataIdentifier"/>
            <xsl:apply-templates select="mdb:defaultLocale"/>
            <xsl:apply-templates select="mdb:parentMetadata"/>
            <xsl:apply-templates select="mdb:metadataScope"/>
            <xsl:apply-templates select="mdb:contact"/>
            <xsl:apply-templates select="mdb:dateInfo"/>
            <xsl:apply-templates select="mdb:metadataStandard"/>
            <xsl:apply-templates select="mdb:metadataProfile"/>
            <xsl:apply-templates select="mdb:alternativeMetadataReference"/>
            <xsl:apply-templates select="mdb:otherLocale"/>
            <xsl:apply-templates select="mdb:metadataLinkage"/>
            <xsl:apply-templates select="mdb:spatialRepresentationInfo"/>
            <xsl:apply-templates select="mdb:referenceSystemInfo"/>
            <xsl:apply-templates select="mdb:metadataExtensionInfo"/>
            <xsl:apply-templates select="mdb:identificationInfo"/>
            <xsl:apply-templates select="mdb:contentInfo"/>
            <xsl:apply-templates select="mdb:distributionInfo"/>
            <xsl:apply-templates select="mdb:dataQualityInfo"/>
            <xsl:apply-templates select="mdb:resourceLineage"/>
            <xsl:apply-templates select="mdb:portrayalCatalogueInfo"/>
            <xsl:apply-templates select="mdb:metadataConstraints"/>
            <xsl:apply-templates select="mdb:applicationSchemaInfo"/>
            <xsl:apply-templates select="mdb:metadataMaintenance"/>
          </xsl:element>
        </xsl:for-each>
      </records>
    </xsl:variable>

    <xsl:for-each select="$iso19139">
      <rdf:RDF>
        <xsl:apply-templates mode="iso19139-to-dcatap" select="*"/>
      </rdf:RDF>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
