<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:wmc="http://www.opengis.net/context"
                xmlns:wmc11="http://www.opengeospatial.net/context"
                xmlns:ows="http://www.opengis.net/ows"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:java="java:org.fao.geonet.util.XslUtil"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:template match="*" mode="RespParty">

    <xsl:for-each select="wmc:ContactPersonPrimary/wmc:ContactPerson|
                          wmc11:ContactPersonPrimary/wmc11:ContactPerson|
                          ows:ServiceContact/ows:IndividualName">
      <gmd:individualName>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </gmd:individualName>
    </xsl:for-each>

    <xsl:for-each select="wmc:ContactPersonPrimary/wmc:ContactOrganization|
                          wmc11:ContactPersonPrimary/wmc11:ContactOrganization|
                          ../ows:ProviderName">">
      <gmd:organisationName>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </gmd:organisationName>
    </xsl:for-each>

    <xsl:for-each select="wmc:ContactPosition|
                          wmc11:ContactPosition|
                          ows:ServiceContact/ows:PositionName">
      <gmd:positionName>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </gmd:positionName>
    </xsl:for-each>

    <gmd:contactInfo>
      <gmd:CI_Contact>
        <xsl:apply-templates select="." mode="Contact"/>
      </gmd:CI_Contact>
    </gmd:contactInfo>

    <gmd:role>
      <gmd:CI_RoleCode
        codeList="./resources/codeList.xml#CI_RoleCode"
        codeListValue="{if (ows:ServiceContact/ows:Role) then ows:ServiceContact/ows:Role else 'pointOfContact'}"/>
    </gmd:role>
  </xsl:template>


  <xsl:template match="*" mode="Contact">

    <gmd:phone>
      <gmd:CI_Telephone>
        <xsl:for-each select="wmc:ContactVoiceTelephone|
                              wmc11:ContactVoiceTelephone|
                              ows:ContactInfo/ows:Phone">
          <gmd:voice>
            <gco:CharacterString>
              <xsl:value-of select="."/>
            </gco:CharacterString>
          </gmd:voice>
        </xsl:for-each>
      </gmd:CI_Telephone>
    </gmd:phone>

    <xsl:for-each select="wmc:ContactAddress|wmc11:ContactAddress|ows:ContactInfo/ows:Address">
      <gmd:address>
        <gmd:CI_Address>
          <xsl:apply-templates select="." mode="Address"/>
        </gmd:CI_Address>
      </gmd:address>
    </xsl:for-each>

  </xsl:template>

  <xsl:template match="*" mode="Address">

    <xsl:for-each select="*:Address">
      <gmd:deliveryPoint>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </gmd:deliveryPoint>
    </xsl:for-each>

    <xsl:for-each select="*:City">
      <gmd:city>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </gmd:city>
    </xsl:for-each>

    <xsl:for-each select="*:StateOrProvince">
      <gmd:administrativeArea>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </gmd:administrativeArea>
    </xsl:for-each>

    <xsl:for-each select="*:PostCode">
      <gmd:postalCode>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </gmd:postalCode>
    </xsl:for-each>

    <xsl:for-each select="*:Country">
      <gmd:country>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </gmd:country>
    </xsl:for-each>

    <xsl:for-each select="*:eMailAdd|ows:electronMailAddress">
      <gmd:electronicMailAddress>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </gmd:electronicMailAddress>
    </xsl:for-each>

  </xsl:template>
</xsl:stylesheet>
