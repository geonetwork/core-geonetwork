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
<xsl:stylesheet xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0"
>
  <xsl:param name="protocol" select="'OGC:WMS-1.1.1-http-get-map'"/>
  <xsl:param name="url"/>
  <xsl:param name="desc"/>
  <xsl:param name="scopedName"/>

  <!-- ============================================================================= -->

  <xsl:template match="gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:copy-of select="gmd:fileIdentifier|
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
          gmd:identificationInfo|
          gmd:contentInfo"/>

      <!-- TODO we could check if online resource already exists before adding information -->
      <gmd:distributionInfo>
        <gmd:MD_Distribution>
          <xsl:copy-of select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat"/>
          <xsl:copy-of select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor"/>
          <gmd:transferOptions>
            <gmd:MD_DigitalTransferOptions>
              <xsl:copy-of
                select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions[1]/gmd:MD_DigitalTransferOptions/gmd:unitsOfDistribution"/>
              <xsl:copy-of
                select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions[1]/gmd:MD_DigitalTransferOptions/gmd:transferSize"/>
              <xsl:copy-of
                select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions[1]/gmd:MD_DigitalTransferOptions/gmd:onLine"/>
              <gmd:onLine>
                <gmd:CI_OnlineResource>
                  <gmd:linkage>
                    <gmd:URL>
                      <xsl:value-of select="$url"/>
                    </gmd:URL>
                  </gmd:linkage>
                  <gmd:protocol>
                    <gco:CharacterString>
                      <xsl:value-of select="$protocol"/>
                    </gco:CharacterString>
                  </gmd:protocol>
                  <gmd:name>
                    <gco:CharacterString>
                      <xsl:value-of select="$scopedName"/>
                    </gco:CharacterString>
                  </gmd:name>
                  <gmd:description>
                    <gco:CharacterString>
                      <xsl:value-of select="$desc"/>
                    </gco:CharacterString>
                  </gmd:description>
                </gmd:CI_OnlineResource>
              </gmd:onLine>
              <xsl:copy-of
                select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions[1]/gmd:MD_DigitalTransferOptions/gmd:offLine"/>
            </gmd:MD_DigitalTransferOptions>
          </gmd:transferOptions>
          <xsl:copy-of
            select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions[position() > 1]"/>
        </gmd:MD_Distribution>

      </gmd:distributionInfo>

      <xsl:copy-of select="gmd:dataQualityInfo|
        gmd:portrayalCatalogueInfo|
        gmd:metadataConstraints|
        gmd:applicationSchemaInfo|
        gmd:metadataMaintenance|
        gmd:series|
        gmd:describes|
        gmd:propertyType|
        gmd:featureType|
        gmd:featureAttribute"/>

    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
