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

<xsl:stylesheet xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:wfs="http://www.opengis.net/wfs"
                xmlns:ows="http://www.opengis.net/ows"
                xmlns:owsg="http://www.opengeospatial.net/ows"
                xmlns:ows11="http://www.opengis.net/ows/1.1"
                xmlns:wms="http://www.opengis.net/wms"
                xmlns:wcs="http://www.opengis.net/wcs"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0"
                xmlns="http://www.isotc211.org/2005/gmd"
                extension-element-prefixes="wcs ows wfs owsg ows11">

  <!-- ============================================================================= -->

  <xsl:template match="*" mode="RespParty">

    <xsl:for-each
      select="ContactPersonPrimary/ContactPerson|wms:ContactPersonPrimary/wms:ContactPerson|wcs:individualName|ows:ServiceContact/ows:IndividualName|ows11:ServiceContact/ows11:IndividualName">
      <individualName>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </individualName>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each
      select="ContactPersonPrimary/ContactOrganization|wms:ContactPersonPrimary/wms:ContactOrganization|wcs:organisationName|ows:ProviderName|ows11:ProviderName">
      <organisationName>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </organisationName>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each
      select="ContactPosition|wms:ContactPosition|wcs:positionName|ows:ServiceContact/ows:PositionName|ows11:ServiceContact/ows11:PositionName">
      <positionName>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </positionName>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <contactInfo>
      <CI_Contact>
        <xsl:apply-templates select="." mode="Contact"/>
      </CI_Contact>
    </contactInfo>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <role>
      <CI_RoleCode codeList="./resources/codeList.xml#CI_RoleCode" codeListValue="pointOfContact"/>
    </role>

  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template match="*" mode="Contact">

    <phone>
      <CI_Telephone>
        <xsl:for-each select="ContactVoiceTelephone|wms:ContactVoiceTelephone|
            ows:ServiceContact/ows:ContactInfo/ows:Phone/ows:Voice|
            ows11:ServiceContact/ows11:ContactInfo/ows11:Phone/ows11:Voice">
          <voice>
            <gco:CharacterString>
              <xsl:value-of select="."/>
            </gco:CharacterString>
          </voice>
        </xsl:for-each>

        <xsl:for-each select="ContactFacsimileTelephone|wms:ContactFacsimileTelephone|
            ows:ServiceContact/ows:ContactInfo/ows:Phone/ows:Facsimile|
            ows11:ServiceContact/ows11:ContactInfo/ows11:Phone/ows11:Facsimile">
          <facsimile>
            <gco:CharacterString>
              <xsl:value-of select="."/>
            </gco:CharacterString>
          </facsimile>
        </xsl:for-each>
      </CI_Telephone>
    </phone>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
    <xsl:for-each select="ContactAddress|../wms:ContactInformation|wms:ContactAddress|
              wcs:contactInfo|
              ows:ServiceContact/ows:ContactInfo/ows:Address|
              ows11:ServiceContact/ows11:ContactInfo/ows11:Address">

      <address>
        <CI_Address>
          <xsl:apply-templates select="." mode="Address"/>
        </CI_Address>
      </address>
    </xsl:for-each>

    <!--cntOnLineRes-->
    <!--cntHours -->
    <!--cntInstr -->
    <xsl:variable name="url" select="//Service/OnlineResource/@xlink:href|
      //wms:Service/wms:OnlineResource/@xlink:href|
      ows:ProviderSite/@xlink:href|
      ows11:ProviderSite/@xlink:href"/>
    <xsl:if test="$url != ''">
      <onlineResource>
        <CI_OnlineResource>
          <linkage>
            <URL>
              <xsl:value-of select="$url"/>
            </URL>
          </linkage>
        </CI_OnlineResource>
      </onlineResource>
    </xsl:if>
  </xsl:template>


  <!-- ============================================================================= -->

  <xsl:template match="*" mode="Address">
    <xsl:for-each select="Address|wms:Address|ows:DeliveryPoint|ows11:DeliveryPoint">
      <deliveryPoint>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </deliveryPoint>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="City|wms:City|wcs:address/wcs:city|ows:City|ows11:City">
      <city>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </city>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each
      select="StateOrProvince|wms:StateOrProvince|ows:AdministrativeArea|ows11:AdministrativeArea">
      <administrativeArea>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </administrativeArea>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="PostCode|wms:PostCode|ows:PostalCode|ows11:PostalCode">
      <postalCode>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </postalCode>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="Country|wms:Country|wcs:address/wcs:country|ows:Country|ows11:Country">
      <country>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </country>
    </xsl:for-each>

    <!-- TODO - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each
      select="ContactElectronicMailAddress|wms:ContactElectronicMailAddress|wcs:address/wcs:electronicMailAddress|ows:ElectronicMailAddress|ows11:ElectronicMailAddress">
      <electronicMailAddress>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </electronicMailAddress>
    </xsl:for-each>

  </xsl:template>

  <!-- ============================================================================= -->

</xsl:stylesheet>
