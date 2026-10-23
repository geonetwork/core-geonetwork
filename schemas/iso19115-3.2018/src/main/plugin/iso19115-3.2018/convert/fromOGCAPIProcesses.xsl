<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:gfc="http://standards.iso.org/iso/19110/gfc/1.1"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:cat="http://standards.iso.org/iso/19115/-3/cat/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                exclude-result-prefixes="#all">
  <!--
  Convert OGC API Processes metadata to ISO 19115-3.2018 XML format.
  -->

  <xsl:import href="ISO19139/utility/create19115-3Namespaces.xsl"/>

  <xsl:output method="xml" indent="yes"/>

  <xsl:strip-space elements="*"/>

  <xsl:template match="/record">
    <mdb:MD_Metadata>
      <xsl:call-template name="add-iso19115-3.2018-namespaces"/>
      <mdb:metadataIdentifier>
        <mcc:MD_Identifier>
          <mcc:code>
            <gco:CharacterString>
              <xsl:value-of select="util:md5Hex(apiUrl)"/>
            </gco:CharacterString>
          </mcc:code>
        </mcc:MD_Identifier>
      </mdb:metadataIdentifier>
      <mdb:defaultLocale>
        <lan:PT_Locale>
          <lan:language>
            <lan:LanguageCode codeList="codeListLocation#LanguageCode"
                              codeListValue="eng"/>
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
            <mcc:MD_ScopeCode
              codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_ScopeCode"
              codeListValue="service"/>
          </mdb:resourceScope>
          <mdb:name>
            <gco:CharacterString>OGC API Process</gco:CharacterString>
          </mdb:name>
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
                  <xsl:value-of select="nodeUrl"/>
                </gco:CharacterString>
              </cit:name>
            </cit:CI_Organisation>
          </cit:party>
        </cit:CI_Responsibility>
      </mdb:contact>


      <xsl:call-template name="build-date">
        <xsl:with-param name="tag" select="'mdb:dateInfo'"/>
        <xsl:with-param name="value" select="format-date(current-date(), '[Y0001]-[M01]-[D01]')"/>
      </xsl:call-template>

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

              <xsl:call-template name="build-date">
                <xsl:with-param name="tag" select="'cit:date'"/>
                <xsl:with-param name="value" select="format-date(current-date(), '[Y0001]-[M01]-[D01]')"/>
              </xsl:call-template>

              <cit:edition>
                <gco:CharacterString>
                  <xsl:value-of select="version"/>
                </gco:CharacterString>
              </cit:edition>
              <cit:identifier>
                <mcc:MD_Identifier>
                  <mcc:code gco:nilReason="missing">
                    <gcx:Anchor xlink:href="{apiUrl}">
                      <xsl:value-of select="id"/>
                    </gcx:Anchor>
                  </mcc:code>
                </mcc:MD_Identifier>
              </cit:identifier>
            </cit:CI_Citation>
          </mri:citation>

          <mri:abstract>
            <gco:CharacterString>
              <xsl:value-of select="description"/>
            </gco:CharacterString>
          </mri:abstract>

          <xsl:if test="nodeUrl">
            <mri:pointOfContact>
              <cit:CI_Responsibility>
                <cit:role>
                  <cit:CI_RoleCode codeList="codeListLocation#CI_RoleCode" codeListValue="resourceProvider"/>
                </cit:role>
                <cit:party>
                  <cit:CI_Organisation>
                    <cit:name>
                      <gco:CharacterString>
                        <xsl:value-of select="nodeUrl"/>
                      </gco:CharacterString>
                    </cit:name>
                  </cit:CI_Organisation>
                </cit:party>
              </cit:CI_Responsibility>
            </mri:pointOfContact>
          </xsl:if>

          <xsl:variable name="keywords"
                        select=".//keywords|use_case"/>
          <xsl:if test="count($keywords) > 0">
            <mri:descriptiveKeywords>
              <mri:MD_Keywords>
                <xsl:for-each select="$keywords">
                  <mri:keyword>
                    <gco:CharacterString>
                      <xsl:value-of select="."/>
                    </gco:CharacterString>
                  </mri:keyword>
                </xsl:for-each>
                <mri:type>
                  <mri:MD_KeywordTypeCode codeListValue="theme"
                                          codeList="./resources/codeList.xml#MD_KeywordTypeCode"/>
                </mri:type>
              </mri:MD_Keywords>
            </mri:descriptiveKeywords>
          </xsl:if>


          <mri:defaultLocale>
            <lan:PT_Locale>
              <lan:language>
                <lan:LanguageCode codeList="codeListLocation#LanguageCode"
                                  codeListValue="eng"/>
              </lan:language>
              <lan:characterEncoding>
                <lan:MD_CharacterSetCode codeList="codeListLocation#MD_CharacterSetCode"
                                         codeListValue="utf8"/>
              </lan:characterEncoding>
            </lan:PT_Locale>
          </mri:defaultLocale>

          <xsl:if test="example or jobControlOptions">
            <mri:supplementalInformation>
              <gco:CharacterString>
                <xsl:if test="example">
                  # Example:
                  <xsl:for-each select="example/*">
                    ##
                    <xsl:value-of select="name()"/>

                    <xsl:for-each select="*">
                      * <xsl:value-of select="name()"/>:
                      <xsl:value-of select="."/>
                    </xsl:for-each>
                  </xsl:for-each>
                </xsl:if>

                <xsl:text>


                </xsl:text>

                <xsl:if test="jobControlOptions">
                  # Execution modes:
                  <xsl:for-each select="jobControlOptions">
                    *
                    <xsl:value-of select="."/>
                  </xsl:for-each>
                </xsl:if>
              </gco:CharacterString>
            </mri:supplementalInformation>
          </xsl:if>
        </mri:MD_DataIdentification>
      </mdb:identificationInfo>

      <xsl:if test="inputs or outputs">
        <mdb:contentInfo>
          <mrc:MD_FeatureCatalogue>
            <mrc:featureCatalogue>
              <gfc:FC_FeatureCatalogue>
                <cat:name>
                  <gco:CharacterString>Process parameters</gco:CharacterString>
                </cat:name>
                <cat:versionNumber>
                  <gco:CharacterString>
                    <xsl:value-of select="version"/>
                  </gco:CharacterString>
                </cat:versionNumber>

                <xsl:for-each select="inputs|outputs">
                  <gfc:featureType>
                    <gfc:FC_FeatureType>
                      <gfc:typeName>
                        <xsl:value-of select="name()"/>
                      </gfc:typeName>
                      <xsl:for-each select="*">
                        <gfc:carrierOfCharacteristics>
                          <gfc:FC_FeatureAttribute>
                            <gfc:memberName>
                              <xsl:value-of select="title"/>
                            </gfc:memberName>
                            <gfc:definition>
                              <gco:CharacterString>
                                <xsl:value-of select="description"/>
                              </gco:CharacterString>
                            </gfc:definition>
                            <gfc:cardinality>
                              <gco:CharacterString>
                                <xsl:if test="minOccurs">
                                  <xsl:value-of select="concat(minOccurs, '..', maxOccurs)"/>
                                </xsl:if>
                              </gco:CharacterString>
                            </gfc:cardinality>
                            <gfc:code>
                              <gco:CharacterString>
                                <xsl:value-of select="name()"/>
                              </gco:CharacterString>
                            </gfc:code>
                            <gfc:valueType>
                              <gco:TypeName>
                                <gco:aName>
                                  <gco:CharacterString>
                                    <xsl:value-of select="schema/type"/>
                                    <xsl:if test="schema/contentMediaType">
                                      <xsl:value-of select="concat(' (', schema/contentMediaType, ')')"/>
                                    </xsl:if>
                                  </gco:CharacterString>
                                </gco:aName>
                              </gco:TypeName>
                            </gfc:valueType>
                          </gfc:FC_FeatureAttribute>
                        </gfc:carrierOfCharacteristics>
                      </xsl:for-each>
                      <gfc:featureCatalogue/>
                    </gfc:FC_FeatureType>
                  </gfc:featureType>
                </xsl:for-each>
              </gfc:FC_FeatureCatalogue>
            </mrc:featureCatalogue>
          </mrc:MD_FeatureCatalogue>
        </mdb:contentInfo>
      </xsl:if>

      <mdb:distributionInfo>
        <mrd:MD_Distribution>
          <mrd:distributionFormat>
            <mrd:MD_Format>
              <mrd:formatSpecificationCitation>
                <cit:CI_Citation>
                  <cit:title>
                    <gco:CharacterString>
                      OGC API Processes
                    </gco:CharacterString>
                  </cit:title>
                </cit:CI_Citation>
              </mrd:formatSpecificationCitation>
            </mrd:MD_Format>
          </mrd:distributionFormat>

          <mrd:transferOptions>
            <mrd:MD_DigitalTransferOptions>
              <!--
                <links>
                  <hreflang>en-US</hreflang>
                  <rel>http://www.opengis.net/def/rel/ogc/1.0/execute</rel>
                  <href>https://aquainfra.ogc.igb-berlin.de/pygeoapi/processes/heat4advanced/execution?f=json</href>
                  <type>application/json</type>
                  <title>Execution for this process as JSON</title>
                </links>
              -->
              <xsl:for-each select="links">
                <mrd:onLine>
                  <cit:CI_OnlineResource>
                    <cit:linkage>
                      <gco:CharacterString>
                        <xsl:value-of select="href"/>
                      </gco:CharacterString>
                    </cit:linkage>
                    <cit:protocol>
                      <gco:CharacterString>
                        <xsl:value-of select="type"/>
                      </gco:CharacterString>
                    </cit:protocol>
                    <cit:name>
                      <gco:CharacterString>
                        <xsl:value-of select="title"/>
                      </gco:CharacterString>
                    </cit:name>
                    <cit:description>
                      <gco:CharacterString>
                        <xsl:value-of select="description"/>
                      </gco:CharacterString>
                    </cit:description>
                    <cit:function>
                      <cit:CI_OnLineFunctionCode
                        codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_OnLineFunctionCode"
                        codeListValue="information"/>
                    </cit:function>
                  </cit:CI_OnlineResource>
                </mrd:onLine>
              </xsl:for-each>
            </mrd:MD_DigitalTransferOptions>
          </mrd:transferOptions>
        </mrd:MD_Distribution>
      </mdb:distributionInfo>

      <mdb:resourceLineage>
        <mrl:LI_Lineage>
          <mrl:statement>
            <gco:CharacterString>

            </gco:CharacterString>
          </mrl:statement>
          <mrl:scope>
            <mcc:MD_Scope>
              <mcc:level>
                <mcc:MD_ScopeCode
                  codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_ScopeCode"
                  codeListValue="service"/>
              </mcc:level>
            </mcc:MD_Scope>
          </mrl:scope>
        </mrl:LI_Lineage>
      </mdb:resourceLineage>
    </mdb:MD_Metadata>
  </xsl:template>

  <xsl:template name="build-date">
    <xsl:param name="tag"/>
    <xsl:param name="value"/>

    <xsl:variable name="type"
                  select="'publication'"/>
    <xsl:element name="{$tag}">
      <cit:CI_Date>
        <cit:date>
          <gco:DateTime>
            <xsl:value-of select="$value"/>
          </gco:DateTime>
        </cit:date>
        <cit:dateType>
          <cit:CI_DateTypeCode codeList="codeListLocation#CI_DateTypeCode" codeListValue="{$type}"/>
        </cit:dateType>
      </cit:CI_Date>
    </xsl:element>
  </xsl:template>
</xsl:stylesheet>
