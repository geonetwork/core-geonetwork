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

  <xsl:output method="xml" indent="yes"/>
  <xsl:strip-space elements="*"/>

  <xsl:template match="/record">
    <xsl:variable name="cataloglang" select="'fre'"></xsl:variable>

    <mdb:MD_Metadata>
      <mdb:metadataIdentifier>
        <mcc:MD_Identifier>
          <mcc:code>
            <gco:CharacterString>
              <xsl:value-of select="uuid"/>
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
            <cit:CI_RoleCode codeList="codeListLocation#CI_RoleCode" codeListValue="publisher"/>
          </cit:role>
          <cit:party>
            <cit:CI_Organisation>
              <cit:name>
                <gco:CharacterString>
                  <xsl:value-of select="maintainer"/>
                </gco:CharacterString>
              </cit:name>
              <cit:contactInfo>
                <cit:CI_Contact>
                  <cit:address>
                    <cit:CI_Address>
                      <cit:electronicMailAddress>
                        <gco:CharacterString>
                          <xsl:value-of select="maintainer_email"/>
                        </gco:CharacterString>
                      </cit:electronicMailAddress>
                    </cit:CI_Address>
                  </cit:address>
                </cit:CI_Contact>
              </cit:contactInfo>
            </cit:CI_Organisation>
          </cit:party>
        </cit:CI_Responsibility>
      </mdb:contact>

      <mdb:dateInfo>
        <cit:CI_Date>
          <cit:date>
            <gco:DateTime>
              <xsl:call-template name="date">
                <xsl:with-param name="value">
                  <xsl:value-of select="metadata_created"></xsl:value-of>
                </xsl:with-param>
              </xsl:call-template>
            </gco:DateTime>
          </cit:date>
          <cit:dateType>
            <cit:CI_DateTypeCode codeList="codeListLocation#CI_DateTypeCode" codeListValue="creation"/>
          </cit:dateType>
        </cit:CI_Date>
      </mdb:dateInfo>
      <mdb:dateInfo>
        <cit:CI_Date>
          <cit:date>
            <gco:DateTime>
              <xsl:call-template name="date">
                <xsl:with-param name="value">
                  <xsl:value-of select="metadata_modified"></xsl:value-of>
                </xsl:with-param>
              </xsl:call-template>
            </gco:DateTime>
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
                    <gco:DateTime/>
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
              <xsl:value-of select="notes"/>
            </gco:CharacterString>
          </mri:abstract>
          <mri:status>
            <mcc:MD_ProgressCode codeListValue="{state}"/>
          </mri:status>

          <mri:pointOfContact>
            <cit:CI_Responsibility>
              <cit:role>
                <cit:CI_RoleCode codeList="codeListLocation#CI_RoleCode" codeListValue="originator">originator</cit:CI_RoleCode>
              </cit:role>
              <cit:party>
                <cit:CI_Organisation>
                  <cit:contactInfo>
                    <cit:CI_Contact>
                      <cit:name>
                        <gco:CharacterString>
                          <xsl:value-of select="author"/>
                        </gco:CharacterString>
                      </cit:name>
                      <cit:address>
                        <cit:CI_Address>
                          <cit:electronicMailAddress>
                            <gco:CharacterString>
                              <xsl:value-of select="author_email"/>
                            </gco:CharacterString>
                          </cit:electronicMailAddress>
                        </cit:CI_Address>
                      </cit:address>
                    </cit:CI_Contact>
                  </cit:contactInfo>
                </cit:CI_Organisation>
              </cit:party>
            </cit:CI_Responsibility>
          </mri:pointOfContact>

          <mri:topicCategory>
            <mri:MD_TopicCategoryCode></mri:MD_TopicCategoryCode>
          </mri:topicCategory>

          <mri:descriptiveKeywords>
            <mri:MD_Keywords>
              <xsl:for-each select="tags">
                <mri:keyword>
                  <gco:CharacterString>
                    <xsl:value-of select="java-xsl-util:html2textNormalized(name)" /> <!-- this can contain HTML entities -->
                  </gco:CharacterString>
                </mri:keyword>
              </xsl:for-each>
            </mri:MD_Keywords>
          </mri:descriptiveKeywords>

          <mri:resourceConstraints>
            <mco:MD_LegalConstraints>
              <mco:reference>
                <cit:CI_Citation>
                  <cit:onlineResource>
                    <cit:CI_OnlineResource>
                      <cit:linkage>
                        <gco:CharacterString>
                          <xsl:value-of select="license_title"/> <!-- actually contains an url -->
                        </gco:CharacterString>
                      </cit:linkage>
                    </cit:CI_OnlineResource>
                  </cit:onlineResource>
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
                <mrd:transferSize>
                  <gco:Real>
                    <xsl:value-of select="size"/>
                  </gco:Real>
                </mrd:transferSize>
                <mrd:onLine>
                  <cit:CI_OnlineResource>
                    <cit:linkage>
                      <gco:CharacterString>
                        <xsl:value-of select="java-xsl-util:html2textNormalized(url)"></xsl:value-of>
                      </gco:CharacterString>
                    </cit:linkage>
                    <cit:protocol>
                      <gco:CharacterString>
                        <xsl:value-of select="$format-protocol-mapping/entry[format=lower-case($format)]/protocol"/>
                      </gco:CharacterString>
                    </cit:protocol>
                    <cit:name>
                      <gco:CharacterString>
                        <xsl:value-of select="name"/>
                      </gco:CharacterString>
                    </cit:name>
                    <cit:description>
                      <gco:CharacterString>
                        <xsl:value-of select="java-xsl-util:html2textNormalized(description)"></xsl:value-of>
                      </gco:CharacterString>
                    </cit:description>
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
                      <xsl:value-of select="java-xsl-util:html2textNormalized(url)"></xsl:value-of>
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

      <mdb:resourceLineage>
        <mrl:LI_Lineage>
          <mrl:statement>
            <gco:CharacterString/>
          </mrl:statement>
          <mrl:scope>
            <mcc:MD_Scope>
              <mcc:level>
                <mcc:MD_ScopeCode codeList="codeListLocation#MD_ScopeCode" codeListValue="dataset"/>
              </mcc:level>
            </mcc:MD_Scope>
          </mrl:scope>
        </mrl:LI_Lineage>
      </mdb:resourceLineage>
    </mdb:MD_Metadata>
  </xsl:template>

  <xsl:template name="date">
    <xsl:param name="value"></xsl:param>

    <!-- Regexps for parsing expected date formats -->
    <!-- examples of localized date: "jeu, 24/12/2020 - 03:00", "24-12-2020 03:00" -->
    <xsl:variable name="regExLoc">^.*([0-9]{2})[-/]([0-9]{2})[-/]([0-9]{4})[-\s]+([0-9]{2}:[0-9]{2}).*$</xsl:variable>
    <!-- example of internationalized date: "2020-12-24 03:00:00" -->
    <xsl:variable name="regExInt">^([0-9]{4})-([0-9]{2})-([0-9]{2}) ([0-9]{2}:[0-9]{2}:[0-9]{2})$</xsl:variable>

    <xsl:variable name="dateFormatted">
      <xsl:choose>
        <xsl:when test="matches($value, $regExLoc)">
          <xsl:value-of select="replace($value, $regExLoc, '$3-$2-$1T$4:00')"></xsl:value-of>
        </xsl:when>
        <xsl:when test="matches($value, $regExInt)">
          <xsl:value-of select="replace($value, $regExInt, '$1-$2-$3T$4')"></xsl:value-of>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsl:value-of select="xs:dateTime($dateFormatted)"></xsl:value-of>
  </xsl:template>
</xsl:stylesheet>
