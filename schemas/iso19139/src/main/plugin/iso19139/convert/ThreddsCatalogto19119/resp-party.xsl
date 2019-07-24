<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:wfs="http://www.opengis.net/wfs"
                xmlns:ows="http://www.opengis.net/ows"
                xmlns:owsg="http://www.opengeospatial.net/ows"
                xmlns:ows11="http://www.opengis.net/ows/1.1"
                xmlns:wcs="http://www.opengis.net/wcs"
                xmlns:wms="http://www.opengis.net/wms"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0"
                exclude-result-prefixes="wms wcs ows wfs owsg ows11 xsl">

  <!-- ============================================================================= -->

  <xsl:template match="*" mode="RespParty">

    <xsl:for-each
      select="wms:ContactPersonPrimary/wms:ContactPerson|wcs:individualName|ows:ServiceContact/ows:IndividualName|ows11:ServiceContact/ows11:IndividualName">
      <gmd:individualName>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </gmd:individualName>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each
      select="wms:ContactPersonPrimary/wms:ContactOrganization|wcs:organisationName|ows:ProviderName|ows11:ProviderName">
      <gmd:organisationName>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </gmd:organisationName>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each
      select="wms:ContactPosition|wcs:positionName|ows:ServiceContact/ows:PositionName|ows11:ServiceContact/ows11:PositionName">
      <gmd:positionName>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </gmd:positionName>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <gmd:contactInfo>
      <gmd:CI_Contact>
        <xsl:apply-templates select="." mode="Contact"/>
      </gmd:CI_Contact>
    </gmd:contactInfo>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <gmd:role>
      <gmd:CI_RoleCode codeList="./resources/codeList.xml#CI_RoleCode"
                       codeListValue="pointOfContact"/>
    </gmd:role>

  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template match="*" mode="Contact">

    <gmd:phone>
      <gmd:CI_Telephone>
        <xsl:for-each select="wms:ContactVoiceTelephone|
            ows:ServiceContact/ows:ContactInfo/ows:Phone/ows:Voice|
            ows11:ServiceContact/ows11:ContactInfo/ows11:Phone/ows11:Voice">
          <gmd:voice>
            <gco:CharacterString>
              <xsl:value-of select="."/>
            </gco:CharacterString>
          </gmd:voice>
        </xsl:for-each>

        <xsl:for-each select="wms:ContactFacsimileTelephone|
            ows:ServiceContact/ows:ContactInfo/ows:Phone/ows:Facsimile|
            ows11:ServiceContact/ows11:ContactInfo/ows11:Phone/ows11:Facsimile">
          <gmd:facsimile>
            <gco:CharacterString>
              <xsl:value-of select="."/>
            </gco:CharacterString>
          </gmd:facsimile>
        </xsl:for-each>
      </gmd:CI_Telephone>
    </gmd:phone>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="wms:ContactAddress|
              wcs:contactInfo|
              ows:ServiceContact/ows:ContactInfo/ows:Address|
              ows11:ServiceContact/ows11:ContactInfo/ows11:Address">
      <gmd:address>
        <gmd:CI_Address>
          <xsl:apply-templates select="." mode="Address"/>
        </gmd:CI_Address>
      </gmd:address>
    </xsl:for-each>

    <xsl:for-each select="wms:ContactElectronicMailAddress">
      <gmd:address>
        <gmd:CI_Address>
          <gmd:electronicMailAddress>
            <gco:CharacterString>
              <xsl:value-of select="."/>
            </gco:CharacterString>
          </gmd:electronicMailAddress>
        </gmd:CI_Address>
      </gmd:address>
    </xsl:for-each>

    <!--cntOnLineRes-->
    <!--cntHours -->
    <!--cntInstr -->
    <gmd:onlineResource>
      <gmd:CI_OnlineResource>
        <gmd:linkage>
          <gmd:URL>
            <xsl:value-of select="//wms:Service/wms:OnlineResource/@xlink:href|
                           ows:ProviderSite/@xlink:href|
                           ows11:ProviderSite/@xlink:href"/>
          </gmd:URL>
        </gmd:linkage>
      </gmd:CI_OnlineResource>
    </gmd:onlineResource>
  </xsl:template>


  <!-- ============================================================================= -->

  <xsl:template match="*" mode="Address">

    <xsl:for-each select="wms:Address|ows:DeliveryPoint|ows11:DeliveryPoint">
      <gmd:deliveryPoint>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </gmd:deliveryPoint>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="wms:City|wcs:address/wcs:city|ows:City|ows11:City">
      <gmd:city>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </gmd:city>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="wms:StateOrProvince|ows:AdministrativeArea|ows11:AdministrativeArea">
      <gmd:administrativeArea>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </gmd:administrativeArea>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="wms:PostCode|ows:PostalCode|ows11:PostalCode">
      <gmd:postalCode>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </gmd:postalCode>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="wms:Country|wcs:address/wcs:country|ows:Country|ows11:Country">
      <gmd:country>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </gmd:country>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each
      select="wms:ContactElectronicMailAddress|wcs:address/wcs:electronicMailAddress|ows:ElectronicMailAddress|ows11:ElectronicMailAddress">
      <gmd:electronicMailAddress>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </gmd:electronicMailAddress>
    </xsl:for-each>

  </xsl:template>

  <!-- ============================================================================= -->

</xsl:stylesheet>
