<?xml version="1.0" encoding="UTF-8"?>
<!--  Mapping between Thredds Catalog (version 1.0.1) to ISO19119 -->
<xsl:stylesheet xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:util="java:java.util.UUID"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0"
                xmlns="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0"
                exclude-result-prefixes="util">

  <!-- ============================================================================= -->

  <xsl:param name="uuid" select="util:toString(util:randomUUID())"/>
  <xsl:param name="lang">eng</xsl:param>
  <xsl:param name="topic"></xsl:param>
  <xsl:param name="url"></xsl:param>
  <xsl:param name="name"></xsl:param>
  <xsl:param name="type"></xsl:param>
  <xsl:param name="desc"></xsl:param>
  <xsl:param name="version"></xsl:param>
  <xsl:param name="props"></xsl:param>
  <xsl:param name="serverops"></xsl:param>

  <!-- ============================================================================= -->

  <xsl:include href="resp-party.xsl"/>
  <xsl:include href="ref-system.xsl"/>
  <xsl:include href="identification.xsl"/>

  <!-- ============================================================================= -->

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

  <!-- ============================================================================= -->

  <xsl:template match="*">

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <gmd:MD_Metadata>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <gmd:fileIdentifier>
        <gco:CharacterString>
          <xsl:value-of select="$uuid"/>
        </gco:CharacterString>
      </gmd:fileIdentifier>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <gmd:language>
        <gco:CharacterString>
          <xsl:value-of select="$lang"/>
        </gco:CharacterString>
        <!-- English is default -->
      </gmd:language>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <gmd:characterSet>
        <gmd:MD_CharacterSetCode codeList="./resources/codeList.xml#MD_CharacterSetCode"
                                 codeListValue="utf8"/>
      </gmd:characterSet>

      <gmd:hierarchyLevel>
        <gmd:MD_ScopeCode codeList="./resources/codeList.xml#MD_ScopeCode" codeListValue="service"/>
      </gmd:hierarchyLevel>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:for-each select="Service/ContactInformation">
        <gmd:contact>
          <gmd:CI_ResponsibleParty>
            <xsl:apply-templates select="." mode="RespParty"/>
          </gmd:CI_ResponsibleParty>
        </gmd:contact>
      </xsl:for-each>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
      <xsl:variable name="df">[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]</xsl:variable>
      <gmd:dateStamp>
        <gco:DateTime>
          <xsl:value-of select="format-dateTime(current-dateTime(),$df)"/>
        </gco:DateTime>
      </gmd:dateStamp>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <gmd:metadataStandardName>
        <gco:CharacterString>AS/NZS 19119/2005: Geographic Information - Services
        </gco:CharacterString>
      </gmd:metadataStandardName>

      <gmd:metadataStandardVersion>
        <gco:CharacterString>1.0 - 2005</gco:CharacterString>
      </gmd:metadataStandardVersion>

      <!-- spatRepInfo-->
      <!-- TODO - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:for-each select="refSysInfo">
        <gmd:referenceSystemInfo>
          <gmd:MD_ReferenceSystem>
            <xsl:apply-templates select="." mode="RefSystemTypes"/>
          </gmd:MD_ReferenceSystem>
        </gmd:referenceSystemInfo>
      </xsl:for-each>

      <!--mdExtInfo-->
      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <gmd:identificationInfo>
        <srv:SV_ServiceIdentification>
          <xsl:apply-templates select="." mode="SrvDataIdentification">
            <xsl:with-param name="topic" select="$topic"/>
            <xsl:with-param name="name" select="$name"/>
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="desc" select="$desc"/>
            <xsl:with-param name="props" select="$props"/>
            <xsl:with-param name="version" select="$version"/>
            <xsl:with-param name="serverops" select="$serverops"/>
          </xsl:apply-templates>
        </srv:SV_ServiceIdentification>
      </gmd:identificationInfo>

      <!--contInfo-->
      <!--distInfo -->
      <gmd:distributionInfo>
        <gmd:MD_Distribution>
          <gmd:distributionFormat>
            <gmd:MD_Format>
              <gmd:name gco:nilReason="missing">
                <gco:CharacterString/>
              </gmd:name>
              <gmd:version gco:nilReason="missing">
                <gco:CharacterString/>
              </gmd:version>
            </gmd:MD_Format>
          </gmd:distributionFormat>
          <gmd:transferOptions>
            <gmd:MD_DigitalTransferOptions>
              <gmd:onLine>
                <gmd:CI_OnlineResource>
                  <gmd:linkage>
                    <gmd:URL>
                      <xsl:value-of select="$url"/>
                    </gmd:URL>
                  </gmd:linkage>
                  <gmd:protocol>
                    <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                  </gmd:protocol>
                  <gmd:description>
                    <gco:CharacterString>
                      <xsl:value-of select="$name"/>
                    </gco:CharacterString>
                  </gmd:description>
                </gmd:CI_OnlineResource>
              </gmd:onLine>
            </gmd:MD_DigitalTransferOptions>
          </gmd:transferOptions>
        </gmd:MD_Distribution>
      </gmd:distributionInfo>

      <!--dqInfo-->
      <gmd:dataQualityInfo>
        <gmd:DQ_DataQuality>
          <gmd:scope>
            <gmd:DQ_Scope>
              <gmd:level>
                <gmd:MD_ScopeCode codeListValue="service"
                                  codeList="./resources/codeList.xml#MD_ScopeCode"/>
              </gmd:level>
              <gmd:levelDescription>
                <gmd:MD_ScopeDescription>
                  <gmd:attributes/>
                </gmd:MD_ScopeDescription>
              </gmd:levelDescription>
            </gmd:DQ_Scope>
          </gmd:scope>
          <gmd:lineage>
            <gmd:LI_Lineage>
              <gmd:statement gco:nilReason="missing">
                <gco:CharacterString/>
              </gmd:statement>
            </gmd:LI_Lineage>
          </gmd:lineage>
        </gmd:DQ_DataQuality>
      </gmd:dataQualityInfo>
      <!--mdConst -->
      <!--mdMaint-->

    </gmd:MD_Metadata>
  </xsl:template>

  <!-- ============================================================================= -->

</xsl:stylesheet>
