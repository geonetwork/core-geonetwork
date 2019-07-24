<?xml version="1.0" encoding="UTF-8"?>
<!--  Mapping between Thredds Catalog (version 1.0.1) to ISO19139 -->
<xsl:stylesheet xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
                xmlns:util="java:java.util.UUID"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:wms="http://www.opengis.net/wms"
                version="2.0"
                exclude-result-prefixes="wms xsl util">

  <!-- ============================================================================= -->

  <xsl:param name="lang">eng</xsl:param>
  <xsl:param name="topic"></xsl:param>
  <xsl:param name="uuid" select="util:toString(util:randomUUID())"/>
  <xsl:param name="url"></xsl:param>
  <xsl:param name="name"></xsl:param>
  <xsl:param name="desc"></xsl:param>
  <xsl:param name="bbox"></xsl:param>
  <xsl:param name="textent"></xsl:param>

  <!-- ============================================================================= -->

  <xsl:include href="resp-party.xsl"/>
  <xsl:include href="ref-system.xsl"/>
  <xsl:include href="identification.xsl"/>

  <!-- ============================================================================= -->

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

  <!-- ============================================================================= -->

  <xsl:template match="*">

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <gmd:MD_Metadata xsi:schemaLocation="http://www.isotc211.org/2005/gmd http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd">

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
        <gmd:MD_CharacterSetCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_CharacterSetCode"
                                 codeListValue="utf8"/>
      </gmd:characterSet>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <gmd:hierarchyLevel>
        <gmd:MD_ScopeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_ScopeCode" codeListValue="dataset"/>
      </gmd:hierarchyLevel>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:for-each select="wms:Service/wms:ContactInformation">
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
        <gco:CharacterString>ISO 19115:2003/19139</gco:CharacterString>
      </gmd:metadataStandardName>

      <gmd:metadataStandardVersion>
        <gco:CharacterString>1.0</gco:CharacterString>
      </gmd:metadataStandardVersion>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <gmd:identificationInfo>
        <gmd:MD_DataIdentification>
          <xsl:apply-templates select="." mode="DataIdentification">
            <xsl:with-param name="topic" select="$topic"/>
            <xsl:with-param name="name" select="$name"/>
            <xsl:with-param name="desc" select="$desc"/>
            <xsl:with-param name="bbox" select="$bbox"/>
            <xsl:with-param name="textent" select="$textent"/>
            <xsl:with-param name="modificationdate" select="format-dateTime(current-dateTime(),$df)"/>
          </xsl:apply-templates>
        </gmd:MD_DataIdentification>
      </gmd:identificationInfo>

      <gmd:distributionInfo>
        <gmd:MD_Distribution>
          <gmd:distributionFormat>
            <gmd:MD_Format>
              <gmd:name>
                <gco:CharacterString>netcdf/cf</gco:CharacterString>
              </gmd:name>
              <gmd:version>
                <gco:CharacterString>4.0</gco:CharacterString>
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
                  	<gco:CharacterString>WWW:LINK-1.0-http--threddscatalog</gco:CharacterString>
                	</gmd:protocol>
                  <gmd:function>
                  	<gmd:CI_OnLineFunctionCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_ScopeCode" codeListValue="service"/>
                	</gmd:function>
                </gmd:CI_OnlineResource>
              </gmd:onLine>
              <gmd:onLine>
                <gmd:CI_OnlineResource>
                  <gmd:linkage>
                    <gmd:URL>
                      <xsl:value-of select="replace($url,'.xml','.html')"/>
                    </gmd:URL>
                  </gmd:linkage>
                  <gmd:protocol>
                  	<gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                	</gmd:protocol>
                  <gmd:description>
          					<gco:CharacterString>Human Readable Link To Thredds Catalog</gco:CharacterString>
        					</gmd:description>
                  <gmd:function>
                  	<gmd:CI_OnLineFunctionCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_ScopeCode" codeListValue="information"/>
                	</gmd:function>
                </gmd:CI_OnlineResource>
              </gmd:onLine>
            </gmd:MD_DigitalTransferOptions>
          </gmd:transferOptions>
        </gmd:MD_Distribution>
      </gmd:distributionInfo>

      <gmd:dateStamp>
        <gco:DateTime>
          <xsl:value-of select="format-dateTime(current-dateTime(),$df)"/>
        </gco:DateTime>
      </gmd:dateStamp>


    </gmd:MD_Metadata>
  </xsl:template>

  <!-- ============================================================================= -->

</xsl:stylesheet>
