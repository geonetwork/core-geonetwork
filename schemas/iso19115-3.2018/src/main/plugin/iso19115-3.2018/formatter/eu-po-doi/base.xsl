<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2001-2023 Food and Agriculture Organization of the
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
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:gn-doi="java:org.fao.geonet.doi.client.DoiBuilder"
                xmlns:eu="http://ra.publications.europa.eu/schema/doidata/1.0"
                xmlns:grant_id="http://www.crossref.org/grant_id/0.1.1"
                xmlns:datacite="http://datacite.org/schema/kernel-4"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:variable name="df"
                select="'[Y0001][M01][D01]'"/>

  <xsl:param name="now" select="format-date(current-date(), $df)"/>


  <xsl:template name="eu-po-doi-message">
    <xsl:param name="dataciteResource"
               as="node()"/>

    <xsl:param name="nodeUrl"
               select="util:getSettingValue('nodeUrl')"/>

    <eu:DOIRegistrationMessage
      xmlns:eu="http://ra.publications.europa.eu/schema/doidata/1.0"
      xmlns:datacite4="http://datacite.org/schema/kernel-4"
      xmlns:grant_id="http://www.crossref.org/grant_id/0.1.1"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://ra.publications.europa.eu/schema/doidata/1.0 http://ra.publications.europa.eu/schema/OP/DOIMetadata/1.0/OP_DOIMetadata_1.0.xsd">
      <eu:Header>
        <eu:FromCompany>
          <xsl:value-of select="util:getSettingValue(
                        'system/site/organization')"/>
        </eu:FromCompany>
        <!-- <eu:FromPerson></eu:FromPerson>-->
        <eu:FromEmail>
          <xsl:value-of select="util:getSettingValue(
                        'system/feedback/email')"/>
        </eu:FromEmail>
        <eu:ToCompany>Publication Office</eu:ToCompany>
        <!-- <eu:MessageNumber></eu:MessageNumber>-->
        <!-- <eu:MessageRepeat></eu:MessageRepeat>-->
        <eu:SentDate><xsl:value-of select="$now"/></eu:SentDate>
        <!-- <eu:MessageNote></eu:MessageNote>-->
        <!-- <eu:NotificationResponse></eu:NotificationResponse>-->
      </eu:Header>
      <eu:DOIData>
        <eu:DOI><xsl:value-of select="gn-doi:createDoi($metadataUuid)"/></eu:DOI>
        <eu:DOIWebsiteLink>
          <xsl:value-of select="util:getPermalink($metadataUuid, util:getLanguage())"/>
        </eu:DOIWebsiteLink>
        <eu:Metadata>
          <xsl:copy-of select="$dataciteResource"/>
          <!-- TODO: grant_id:grant -->
        </eu:Metadata>
      </eu:DOIData>
    </eu:DOIRegistrationMessage>
  </xsl:template>
</xsl:stylesheet>
