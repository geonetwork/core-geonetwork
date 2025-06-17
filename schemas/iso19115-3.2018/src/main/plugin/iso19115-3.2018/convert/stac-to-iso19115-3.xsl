<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:cat="http://standards.iso.org/iso/19115/-3/cat/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0"
                xmlns:mac="http://standards.iso.org/iso/19115/-3/mac/2.0"
                xmlns:mas="http://standards.iso.org/iso/19115/-3/mas/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mda="http://standards.iso.org/iso/19115/-3/mda/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mdt="http://standards.iso.org/iso/19115/-3/mdt/1.0"
                xmlns:mex="http://standards.iso.org/iso/19115/-3/mex/1.0"
                xmlns:mic="http://standards.iso.org/iso/19115/-3/mic/1.0"
                xmlns:mil="http://standards.iso.org/iso/19115/-3/mil/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/2.0"
                xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0"
                xmlns:mpc="http://standards.iso.org/iso/19115/-3/mpc/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/2.0"
                xmlns:mai="http://standards.iso.org/iso/19115/-3/mai/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
                exclude-result-prefixes="#all">

  <xsl:output method="xml" indent="yes"/>
  <xsl:strip-space elements="*"/>

  <!-- Main template for STAC collection -->
  <xsl:template match="/record">
    <xsl:apply-templates select="//collections" mode="collection-list"/>
  </xsl:template>
  
  <xsl:template match="collections" mode="collection-list">
    <xsl:for-each select="*">
      <xsl:call-template name="process-collection"/>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="process-collection">
    <mdb:MD_Metadata>
      <!-- Metadata identifier -->
      <mdb:metadataIdentifier>
        <mcc:MD_Identifier>
          <mcc:code>
            <gco:CharacterString>
              <xsl:value-of select="id"/>
            </gco:CharacterString>
          </mcc:code>
          <mcc:codeSpace>
            <gco:CharacterString>
              <xsl:choose>
                <xsl:when test="links[rel='self']">
                  <xsl:value-of select="links[rel='self']/href"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="concat('https://stacapi-cdos.apps.okd.crocc.meso.umontpellier.fr/collections/', id)"/>
                </xsl:otherwise>
              </xsl:choose>
            </gco:CharacterString>
          </mcc:codeSpace>
        </mcc:MD_Identifier>
      </mdb:metadataIdentifier>

      <!-- Default locale -->
      <mdb:defaultLocale>
        <lan:PT_Locale>
          <lan:language>
            <lan:LanguageCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#LanguageCode" codeListValue="eng"/>
          </lan:language>
          <lan:characterEncoding>
            <lan:MD_CharacterSetCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_CharacterSetCode" codeListValue="utf8"/>
          </lan:characterEncoding>
        </lan:PT_Locale>
      </mdb:defaultLocale>

      <!-- Metadata scope -->
      <mdb:metadataScope>
        <mdb:MD_MetadataScope>
          <mdb:resourceScope>
            <mcc:MD_ScopeCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_ScopeCode" codeListValue="collection"/>
          </mdb:resourceScope>
        </mdb:MD_MetadataScope>
      </mdb:metadataScope>

      <!-- Contact -->
      <xsl:if test="providers or contacts">
        <mdb:contact>
          <xsl:choose>
            <xsl:when test="providers">
              <xsl:for-each select="providers">
                <cit:CI_Responsibility>
                  <cit:role>
                    <cit:CI_RoleCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_RoleCode" codeListValue="pointOfContact"/>
                  </cit:role>
                  <cit:party>
                    <cit:CI_Organisation>
                      <cit:name>
                        <gco:CharacterString>
                          <xsl:value-of select="name"/>
                        </gco:CharacterString>
                      </cit:name>
                      <xsl:if test="description">
                        <cit:description>
                          <gco:CharacterString>
                            <xsl:value-of select="description"/>
                          </gco:CharacterString>
                        </cit:description>
                      </xsl:if>
                      <xsl:if test="url">
                        <cit:contactInfo>
                          <cit:CI_Contact>
                            <cit:onlineResource>
                              <cit:CI_OnlineResource>
                                <cit:linkage>
                                  <gco:CharacterString>
                                    <xsl:value-of select="url"/>
                                  </gco:CharacterString>
                                </cit:linkage>
                              </cit:CI_OnlineResource>
                            </cit:onlineResource>
                          </cit:CI_Contact>
                        </cit:contactInfo>
                      </xsl:if>
                    </cit:CI_Organisation>
                  </cit:party>
                </cit:CI_Responsibility>
              </xsl:for-each>
            </xsl:when>
            <xsl:when test="contacts">
              <xsl:for-each select="contacts">
                <cit:CI_Responsibility>
                  <cit:role>
                    <cit:CI_RoleCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_RoleCode" codeListValue="pointOfContact"/>
                  </cit:role>
                  <cit:party>
                    <cit:CI_Organisation>
                      <cit:name>
                        <gco:CharacterString>
                          <xsl:value-of select="organization"/>
                        </gco:CharacterString>
                      </cit:name>
                      <cit:contactInfo>
                        <cit:CI_Contact>
                          <cit:individualName>
                            <gco:CharacterString>
                              <xsl:value-of select="name"/>
                            </gco:CharacterString>
                          </cit:individualName>
                        </cit:CI_Contact>
                      </cit:contactInfo>
                    </cit:CI_Organisation>
                  </cit:party>
                </cit:CI_Responsibility>
              </xsl:for-each>
            </xsl:when>
          </xsl:choose>
        </mdb:contact>
      </xsl:if>

      <!-- Date info -->
      <mdb:dateInfo>
        <cit:CI_Date>
          <cit:date>
            <gco:DateTime>
              <xsl:choose>
                <xsl:when test="created">
                  <xsl:value-of select="created"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="current-dateTime()"/>
                </xsl:otherwise>
              </xsl:choose>
            </gco:DateTime>
          </cit:date>
          <cit:dateType>
            <cit:CI_DateTypeCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_DateTypeCode" codeListValue="creation"/>
          </cit:dateType>
        </cit:CI_Date>
      </mdb:dateInfo>
      
      <xsl:if test="updated">
        <mdb:dateInfo>
          <cit:CI_Date>
            <cit:date>
              <gco:DateTime>
                <xsl:value-of select="updated"/>
              </gco:DateTime>
            </cit:date>
            <cit:dateType>
              <cit:CI_DateTypeCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_DateTypeCode" codeListValue="revision"/>
            </cit:dateType>
          </cit:CI_Date>
        </mdb:dateInfo>
      </xsl:if>

      <!-- Resource identification -->
      <mdb:identificationInfo>
        <mri:MD_DataIdentification>
          <mri:citation>
            <cit:CI_Citation>
              <cit:title>
                <gco:CharacterString>
                  <xsl:value-of select="title"/>
                </gco:CharacterString>
              </cit:title>
              <cit:date>
                <cit:CI_Date>
                  <cit:date>
                    <gco:DateTime>
                      <xsl:choose>
                        <xsl:when test="created">
                          <xsl:value-of select="created"/>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:value-of select="current-dateTime()"/>
                        </xsl:otherwise>
                      </xsl:choose>
                    </gco:DateTime>
                  </cit:date>
                  <cit:dateType>
                    <cit:CI_DateTypeCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_DateTypeCode" codeListValue="creation"/>
                  </cit:dateType>
                </cit:CI_Date>
              </cit:date>
              <xsl:if test="updated">
                <cit:date>
                  <cit:CI_Date>
                    <cit:date>
                      <gco:DateTime>
                        <xsl:value-of select="updated"/>
                      </gco:DateTime>
                    </cit:date>
                    <cit:dateType>
                      <cit:CI_DateTypeCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_DateTypeCode" codeListValue="revision"/>
                    </cit:dateType>
                  </cit:CI_Date>
                </cit:date>
              </xsl:if>
              <cit:identifier>
                <mcc:MD_Identifier>
                  <mcc:code>
                    <gco:CharacterString>
                      <xsl:value-of select="id"/>
                    </gco:CharacterString>
                  </mcc:code>
                </mcc:MD_Identifier>
              </cit:identifier>
            </cit:CI_Citation>
          </mri:citation>

          <!-- Abstract -->
          <mri:abstract>
            <gco:CharacterString>
              <xsl:value-of select="description"/>
            </gco:CharacterString>
          </mri:abstract>

          <!-- Purpose from additional STAC fields -->
          <xsl:if test="summaries/purpose or summaries/mission">
            <mri:purpose>
              <gco:CharacterString>
                <xsl:choose>
                  <xsl:when test="summaries/purpose">
                    <xsl:value-of select="summaries/purpose"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="summaries/mission"/>
                  </xsl:otherwise>
                </xsl:choose>
              </gco:CharacterString>
            </mri:purpose>
          </xsl:if>

          <!-- Status -->
          <xsl:if test="summaries/status">
            <mri:status>
              <mcc:MD_ProgressCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_ProgressCode">
                <xsl:attribute name="codeListValue">
                  <xsl:choose>
                    <xsl:when test="summaries/status = 'ongoing'">onGoing</xsl:when>
                    <xsl:when test="summaries/status = 'completed'">completed</xsl:when>
                    <xsl:when test="summaries/status = 'planned'">planned</xsl:when>
                    <xsl:otherwise>
                      <xsl:value-of select="summaries/status"/>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:attribute>
              </mcc:MD_ProgressCode>
            </mri:status>
          </xsl:if>

          <!-- Keywords from STAC extensions -->
          <xsl:if test="keywords or stac_extensions or stac_version">
            <mri:descriptiveKeywords>
              <mri:MD_Keywords>
                <xsl:if test="keywords">
                  <xsl:for-each select="keywords">
                    <mri:keyword>
                      <gco:CharacterString>
                        <xsl:value-of select="."/>
                      </gco:CharacterString>
                    </mri:keyword>
                  </xsl:for-each>
                </xsl:if>
                <xsl:if test="stac_extensions">
                  <xsl:for-each select="stac_extensions">
                    <mri:keyword>
                      <gco:CharacterString>
                        <xsl:value-of select="."/>
                      </gco:CharacterString>
                    </mri:keyword>
                  </xsl:for-each>
                </xsl:if>
                <xsl:if test="stac_version">
                  <mri:keyword>
                    <gco:CharacterString>
                      <xsl:value-of select="concat('STAC version: ', stac_version)"/>
                    </gco:CharacterString>
                  </mri:keyword>
                </xsl:if>
                <mri:type>
                  <mri:MD_KeywordTypeCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_KeywordTypeCode" codeListValue="theme"/>
                </mri:type>
              </mri:MD_Keywords>
            </mri:descriptiveKeywords>
          </xsl:if>

          <!-- Resource constraints - License info -->
          <xsl:if test="license">
            <mri:resourceConstraints>
              <mco:MD_LegalConstraints>
                <mco:useConstraints>
                  <mco:MD_RestrictionCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_RestrictionCode" codeListValue="license"/>
                </mco:useConstraints>
                <mco:otherConstraints>
                  <gco:CharacterString>
                    <xsl:value-of select="license"/>
                  </gco:CharacterString>
                </mco:otherConstraints>
              </mco:MD_LegalConstraints>
            </mri:resourceConstraints>
          </xsl:if>

          <!-- Extent - Spatial -->
          <xsl:if test="extent">
            <mri:extent>
              <gex:EX_Extent>
                <xsl:if test="extent/spatial">
                  <gex:geographicElement>
                    <gex:EX_GeographicBoundingBox>
                      <gex:westBoundLongitude>
                        <gco:Decimal>
                          <xsl:value-of select="extent/spatial/bbox[1]"/>
                        </gco:Decimal>
                      </gex:westBoundLongitude>
                      <gex:eastBoundLongitude>
                        <gco:Decimal>
                          <xsl:value-of select="extent/spatial/bbox[3]"/>
                        </gco:Decimal>
                      </gex:eastBoundLongitude>
                      <gex:southBoundLatitude>
                        <gco:Decimal>
                          <xsl:value-of select="extent/spatial/bbox[2]"/>
                        </gco:Decimal>
                      </gex:southBoundLatitude>
                      <gex:northBoundLatitude>
                        <gco:Decimal>
                          <xsl:value-of select="extent/spatial/bbox[4]"/>
                        </gco:Decimal>
                      </gex:northBoundLatitude>
                    </gex:EX_GeographicBoundingBox>
                  </gex:geographicElement>
                </xsl:if>
                
                <!-- Temporal extent -->
                <xsl:if test="extent/temporal/interval">
                  <gex:temporalElement>
                    <gex:EX_TemporalExtent>
                      <gex:extent>
                        <gml:TimePeriod gml:id="{generate-id()}">
                          <gml:beginPosition>
                            <xsl:value-of select="extent/temporal/interval[1]"/>
                          </gml:beginPosition>
                          <gml:endPosition>
                            <xsl:value-of select="extent/temporal/interval[2]"/>
                          </gml:endPosition>
                        </gml:TimePeriod>
                      </gex:extent>
                    </gex:EX_TemporalExtent>
                  </gex:temporalElement>
                </xsl:if>
              </gex:EX_Extent>
            </mri:extent>
          </xsl:if>

          <!-- Additional STAC-specific information -->
          <mri:supplementalInformation>
            <gco:CharacterString>
              <xsl:text>STAC Collection ID: </xsl:text>
              <xsl:value-of select="id"/>
              <xsl:if test="stac_version">
                <xsl:text>&#10;STAC Version: </xsl:text>
                <xsl:value-of select="stac_version"/>
              </xsl:if>
              <xsl:if test="stac_extensions">
                <xsl:text>&#10;STAC Extensions: </xsl:text>
                <xsl:for-each select="stac_extensions">
                  <xsl:value-of select="."/>
                  <xsl:if test="position() != last()">
                    <xsl:text>, </xsl:text>
                  </xsl:if>
                </xsl:for-each>
              </xsl:if>
              <xsl:if test="summaries">
                <xsl:text>&#10;STAC Collection Summaries: Available</xsl:text>
              </xsl:if>
            </gco:CharacterString>
          </mri:supplementalInformation>
        </mri:MD_DataIdentification>
      </mdb:identificationInfo>

      <!-- Distribution information -->
      <mdb:distributionInfo>
        <mrd:MD_Distribution>
          <!-- Links section -->
          <xsl:if test="links">
            <mrd:transferOptions>
              <mrd:MD_DigitalTransferOptions>
                <xsl:for-each select="links">
                  <mrd:onLine>
                    <cit:CI_OnlineResource>
                      <cit:linkage>
                        <gco:CharacterString>
                          <xsl:value-of select="href"/>
                        </gco:CharacterString>
                      </cit:linkage>
                      <xsl:if test="rel">
                        <cit:protocol>
                          <gco:CharacterString>
                            <xsl:value-of select="rel"/>
                          </gco:CharacterString>
                        </cit:protocol>
                      </xsl:if>
                      <xsl:if test="title">
                        <cit:name>
                          <gco:CharacterString>
                            <xsl:value-of select="title"/>
                          </gco:CharacterString>
                        </cit:name>
                      </xsl:if>
                      <xsl:if test="type">
                        <cit:description>
                          <gco:CharacterString>
                            <xsl:value-of select="type"/>
                          </gco:CharacterString>
                        </cit:description>
                      </xsl:if>
                    </cit:CI_OnlineResource>
                  </mrd:onLine>
                </xsl:for-each>
              </mrd:MD_DigitalTransferOptions>
            </mrd:transferOptions>
          </xsl:if>
        </mrd:MD_Distribution>
      </mdb:distributionInfo>

      <!-- Metadata about the metadata -->
      <mdb:metadataStandard>
        <cit:CI_Citation>
          <cit:title>
            <gco:CharacterString>ISO 19115-3</gco:CharacterString>
          </cit:title>
        </cit:CI_Citation>
      </mdb:metadataStandard>

    </mdb:MD_Metadata>
  </xsl:template>
</xsl:stylesheet>
