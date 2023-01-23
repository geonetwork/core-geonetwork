<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0"
                xmlns="http://www.isotc211.org/2005/gmd">

  <!-- ============================================================================= -->

  <xsl:template match="*" mode="RespParty">

    <xsl:for-each select="rpIndName">
      <individualName>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </individualName>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="rpOrgName">
      <organisationName>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </organisationName>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="rpPosName">
      <positionName>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </positionName>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="rpCntInfo">
      <contactInfo>
        <CI_Contact>
          <xsl:apply-templates select="." mode="Contact"/>
        </CI_Contact>
      </contactInfo>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <role>
      <CI_RoleCode codeList="./resources/codeList.xml#CI_RoleCode"
                   codeListValue="{role/RoleCd/@value}"/>
    </role>

  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template match="*" mode="Contact">

    <xsl:for-each select="cntPhone">
      <phone>
        <CI_Telephone>
          <xsl:apply-templates select="." mode="Telephone"/>
        </CI_Telephone>
      </phone>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="cntAddress">
      <address>
        <CI_Address>
          <xsl:apply-templates select="." mode="Address"/>
        </CI_Address>
      </address>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="cntOnLineRes">
      <onlineResource>
        <CI_OnlineResource>
          <xsl:apply-templates select="." mode="OnLineRes"/>
        </CI_OnlineResource>
      </onlineResource>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="cntHours">
      <hoursOfService>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </hoursOfService>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="cntInstr">
      <contactInstructions>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </contactInstructions>
    </xsl:for-each>

  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template match="*" mode="Telephone">

    <xsl:for-each select="voiceNum">
      <voice>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </voice>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="faxNum">
      <facsimile>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </facsimile>
    </xsl:for-each>

  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template match="*" mode="Address">

    <xsl:for-each select="delPoint">
      <deliveryPoint>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </deliveryPoint>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="city">
      <city>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </city>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="adminArea">
      <administrativeArea>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </administrativeArea>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="postCode">
      <postalCode>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </postalCode>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="country">
      <country>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </country>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="eMailAdd">
      <electronicMailAddress>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </electronicMailAddress>
    </xsl:for-each>

  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template match="*" mode="OnLineRes">

    <linkage>
      <URL>
        <xsl:value-of select="linkage"/>
      </URL>
    </linkage>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="protocol">
      <protocol>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </protocol>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="appProfile">
      <applicationProfile>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </applicationProfile>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="orName">
      <name>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </name>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="orDesc">
      <description>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </description>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="orFunct">
      <function>
        <CI_OnLineFunctionCode codeList="./resources/codeList.xml#CI_OnLineFunctionCode"
                               codeListValue="{OnFunctCd/@value}"/>
      </function>
    </xsl:for-each>

  </xsl:template>

  <!-- ============================================================================= -->

</xsl:stylesheet>
