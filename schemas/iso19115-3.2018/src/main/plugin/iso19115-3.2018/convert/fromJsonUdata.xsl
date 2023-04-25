<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gcoold="http://www.isotc211.org/2005/gco"
                xmlns:gmi="http://www.isotc211.org/2005/gmi"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gsr="http://www.isotc211.org/2005/gsr"
                xmlns:gss="http://www.isotc211.org/2005/gss"
                xmlns:gts="http://www.isotc211.org/2005/gts"
                xmlns:srvold="http://www.isotc211.org/2005/srv"
                xmlns:gml30="http://www.opengis.net/gml"
                xmlns:cat="http://standards.iso.org/iso/19115/-3/cat/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
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
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/1.0"
                xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/2.0"
                xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0"
                xmlns:mpc="http://standards.iso.org/iso/19115/-3/mpc/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/1.0"
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
                xmlns:java-xsl-util="java:org.fao.geonet.util.XslUtil"
                exclude-result-prefixes="#all">

  <xsl:import href="protocol-mapping.xsl"></xsl:import>
  <xsl:import href="udata-licenses.xsl"></xsl:import>

  <xsl:output method="xml" indent="yes"/>
  <xsl:strip-space elements="*"/>

  <xsl:template match="/record">
    <xsl:variable name="cataloglang" select="'fre'"></xsl:variable>

    <mdb:MD_Metadata>
      <mdb:metadataIdentifier>
        <mcc:MD_Identifier>
          <mcc:code>
            <gco:CharacterString>
              <xsl:value-of select="id"/>
            </gco:CharacterString>
          </mcc:code>
        </mcc:MD_Identifier>
      </mdb:metadataIdentifier>
      <mdb:defaultLocale>
        <lan:PT_Locale>
          <lan:language>
            <lan:LanguageCode codeList="codeListLocation#LanguageCode" codeListValue="{$cataloglang}"/>
          </lan:language>
          <lan:characterEncoding>
            <lan:MD_CharacterSetCode codeList="codeListLocation#MD_CharacterSetCode"
                                     codeListValue="utf8"/>
          </lan:characterEncoding>
        </lan:PT_Locale>
      </mdb:defaultLocale>
      <mdb:metadataScope>
        <mdb:MD_MetadataScope>
          <mdb:resourceScope>
            <mcc:MD_ScopeCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_ScopeCode" codeListValue="dataset"/>
          </mdb:resourceScope>
        </mdb:MD_MetadataScope>
      </mdb:metadataScope>

      <mdb:contact>
        <cit:CI_Responsibility>
          <cit:role>
            <cit:CI_RoleCode codeList="codeListLocation#CI_RoleCode" codeListValue="author"/>
          </cit:role>
          <cit:party>
            <cit:CI_Organisation>
              <cit:name>
                <gco:CharacterString>
                  <xsl:value-of select="organization/name"/>
                </gco:CharacterString>
              </cit:name>
              <cit:contactInfo>
                <cit:CI_Contact>
                  <cit:onlineResource>
                    <cit:CI_OnlineResource>
                      <cit:linkage>
                        <xsl:value-of select="organization/uri"/>
                      </cit:linkage>
                    </cit:CI_OnlineResource>
                  </cit:onlineResource>
                </cit:CI_Contact>
              </cit:contactInfo>
              <cit:logo>
                <mcc:MD_BrowseGraphic>
                  <mcc:fileName>
                    <gco:CharacterString>
                      <xsl:value-of select="organization/logo"/>
                    </gco:CharacterString>
                  </mcc:fileName>
                </mcc:MD_BrowseGraphic>
              </cit:logo>
            </cit:CI_Organisation>
          </cit:party>
        </cit:CI_Responsibility>
      </mdb:contact>

      <mdb:dateInfo>
        <cit:CI_Date>
          <cit:date>
            <gco:DateTime><xsl:value-of select="created_at"/></gco:DateTime>
          </cit:date>
          <cit:dateType>
            <cit:CI_DateTypeCode codeList="codeListLocation#CI_DateTypeCode" codeListValue="creation"/>
          </cit:dateType>
        </cit:CI_Date>
      </mdb:dateInfo>
      <mdb:dateInfo>
        <cit:CI_Date>
          <cit:date>
            <gco:DateTime><xsl:value-of select="last_modified"/></gco:DateTime>
          </cit:date>
          <cit:dateType>
            <cit:CI_DateTypeCode codeList="codeListLocation#CI_DateTypeCode" codeListValue="revision"/>
          </cit:dateType>
        </cit:CI_Date>
      </mdb:dateInfo>

      <mdb:metadataStandard>
        <cit:CI_Citation>
          <cit:title>
            <gco:CharacterString>ISO 19115-3</gco:CharacterString>
          </cit:title>
        </cit:CI_Citation>
      </mdb:metadataStandard>

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
                    <gco:DateTime><xsl:value-of select="created_at"/></gco:DateTime>
                  </cit:date>
                  <cit:dateType>
                    <cit:CI_DateTypeCode codeList="codeListLocation#CI_DateTypeCode" codeListValue="creation"/>
                  </cit:dateType>
                </cit:CI_Date>
              </cit:date>
            </cit:CI_Citation>
          </mri:citation>
          <mri:abstract>
            <gco:CharacterString>
              <xsl:value-of select="description"/>
            </gco:CharacterString>
          </mri:abstract>

          <mri:descriptiveKeywords>
            <mri:MD_Keywords>
              <xsl:for-each select="tags">
                <mri:keyword>
                  <gco:CharacterString>
                    <xsl:value-of select="." /> <!-- this can contain HTML entities -->
                  </gco:CharacterString>
                </mri:keyword>
              </xsl:for-each>
            </mri:MD_Keywords>
          </mri:descriptiveKeywords>

          <mri:resourceConstraints>
            <xsl:variable name="licenseId" select="license"></xsl:variable>
            <xsl:variable name="licenseFound" select="count($udataLicenses/license[id=$licenseId]) > 0"></xsl:variable>

            <mco:MD_LegalConstraints>
              <mco:reference>
                <cit:CI_Citation>
                  <xsl:choose>
                    <xsl:when test="$licenseFound">
                      <xsl:variable name="license" select="$udataLicenses/license[id=$licenseId]"></xsl:variable>
                      <cit:title>
                        <gco:CharacterString>
                          <xsl:value-of select="$license/title"/>
                        </gco:CharacterString>
                      </cit:title>
                      <cit:onlineResource>
                        <cit:CI_OnlineResource>
                          <cit:linkage>
                            <gco:CharacterString>
                              <xsl:value-of select="$license/url"/> <!-- TODO: map codes to actual licenses; use https://www.data.gouv.fr/api/1/datasets/licenses/ -->
                            </gco:CharacterString>
                          </cit:linkage>
                        </cit:CI_OnlineResource>
                      </cit:onlineResource>
                    </xsl:when>
                    <xsl:otherwise>
                      <cit:title>
                        <gco:CharacterString>
                          <xsl:value-of select="$licenseId"/>
                        </gco:CharacterString>
                      </cit:title>
                    </xsl:otherwise>
                  </xsl:choose>
                </cit:CI_Citation>
              </mco:reference>
            </mco:MD_LegalConstraints>
          </mri:resourceConstraints>

          <mri:defaultLocale>
            <lan:PT_Locale>
              <lan:language>
                <lan:LanguageCode codeList="codeListLocation#LanguageCode" codeListValue="{$cataloglang}"/>
              </lan:language>
              <lan:characterEncoding>
                <lan:MD_CharacterSetCode codeList="codeListLocation#MD_CharacterSetCode"
                                         codeListValue="utf8"/>
              </lan:characterEncoding>
            </lan:PT_Locale>
          </mri:defaultLocale>

          <xsl:variable name="temporalExtent" select="temporal_coverage"></xsl:variable>
          <xsl:if test="$temporalExtent/end != '' or $temporalExtent/start != ''">
            <mri:extent>
              <gex:EX_Extent>
                <gex:temporalElement>
                  <gex:EX_TemporalExtent>
                    <gex:extent>
                      <gml:TimePeriod>
                        <gml:beginPosition>
                          <xsl:value-of select="$temporalExtent/start"></xsl:value-of>
                        </gml:beginPosition>
                        <gml:endPosition>
                          <xsl:value-of select="$temporalExtent/end"></xsl:value-of>
                        </gml:endPosition>
                      </gml:TimePeriod>
                    </gex:extent>
                  </gex:EX_TemporalExtent>
                </gex:temporalElement>
              </gex:EX_Extent>
            </mri:extent>
          </xsl:if>

          <!-- TODO: spatial extent -->
          <!--<mri:extent>
            <gex:EX_Extent>
              <gex:geographicElement>
                <gex:EX_GeographicBoundingBox>
                  <gex:westBoundLongitude>
                    <gco:Decimal>-180</gco:Decimal>
                  </gex:westBoundLongitude>
                  <gex:eastBoundLongitude>
                    <gco:Decimal>180</gco:Decimal>
                  </gex:eastBoundLongitude>
                  <gex:southBoundLatitude>
                    <gco:Decimal>-90</gco:Decimal>
                  </gex:southBoundLatitude>
                  <gex:northBoundLatitude>
                    <gco:Decimal>90</gco:Decimal>
                  </gex:northBoundLatitude>
                </gex:EX_GeographicBoundingBox>
              </gex:geographicElement>
            </gex:EX_Extent>
          </mri:extent>-->
        </mri:MD_DataIdentification>
      </mdb:identificationInfo>

      <mdb:distributionInfo>
        <mrd:MD_Distribution>
          <xsl:for-each-group select="resources/format" group-by=".">
            <mrd:distributionFormat>
              <mrd:MD_Format>
                <mrd:formatSpecificationCitation>
                  <cit:CI_Citation>
                    <cit:title>
                      <gco:CharacterString>
                        <xsl:value-of select="."/>
                      </gco:CharacterString>
                    </cit:title>
                  </cit:CI_Citation>
                </mrd:formatSpecificationCitation>
              </mrd:MD_Format>
            </mrd:distributionFormat>
          </xsl:for-each-group>

          <mrd:transferOptions>
            <xsl:for-each select="resources">
              <mrd:MD_DigitalTransferOptions>
                <xsl:variable name="format" select="format"/>
                <xsl:variable name="protocolMatch" select="$format-protocol-mapping/entry[format=lower-case($format)]/protocol"></xsl:variable>
                <mrd:transferSize>
                  <gco:Real>
                    <xsl:value-of select="filesize"/>
                  </gco:Real>
                </mrd:transferSize>
                <mrd:onLine>
                  <cit:CI_OnlineResource>
                    <cit:linkage>
                      <gco:CharacterString>
                        <xsl:value-of select="url"></xsl:value-of>
                      </gco:CharacterString>
                    </cit:linkage>
                    <cit:protocol>
                      <gco:CharacterString>
                        <xsl:choose>
                          <xsl:when test="$protocolMatch != ''">
                            <xsl:value-of select="$protocolMatch"></xsl:value-of>
                          </xsl:when>
                          <xsl:otherwise>
                            WWW:DOWNLOAD:<xsl:value-of select="mime"></xsl:value-of>
                          </xsl:otherwise>
                        </xsl:choose>
                      </gco:CharacterString>
                    </cit:protocol>
                    <cit:name>
                      <gco:CharacterString>
                        <xsl:value-of select="title"/>
                      </gco:CharacterString>
                    </cit:name>
                    <xsl:if test="description != '' and description != 'null'">
                      <cit:description>
                        <gco:CharacterString>
                          <xsl:value-of select="description"></xsl:value-of>
                        </gco:CharacterString>
                      </cit:description>
                    </xsl:if>
                  </cit:CI_OnlineResource>
                </mrd:onLine>
              </mrd:MD_DigitalTransferOptions>
            </xsl:for-each>
          </mrd:transferOptions>
          <mrd:transferOptions>
            <mrd:MD_DigitalTransferOptions>
              <mrd:onLine>
                <cit:CI_OnlineResource>
                  <cit:linkage>
                    <gco:CharacterString>
                      <xsl:value-of select="page"></xsl:value-of>
                    </gco:CharacterString>
                  </cit:linkage>
                  <cit:protocol>
                    <gco:CharacterString>
                      WWW:LINK:LANDING_PAGE
                    </gco:CharacterString>
                  </cit:protocol>
                  <cit:name>
                    <gco:CharacterString>
                      Landing Page
                    </gco:CharacterString>
                  </cit:name>
                  <cit:description>
                    <gco:CharacterString>
                    </gco:CharacterString>
                  </cit:description>
                </cit:CI_OnlineResource>
              </mrd:onLine>
            </mrd:MD_DigitalTransferOptions>
          </mrd:transferOptions>
        </mrd:MD_Distribution>
      </mdb:distributionInfo>
    </mdb:MD_Metadata>
  </xsl:template>

</xsl:stylesheet>
