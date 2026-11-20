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
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                exclude-result-prefixes="#all">
  <!--
  Convert OGC API Processes metadata to ISO 19115-3.2018 XML format.
  -->

  <xsl:import href="ISO19139/utility/create19115-3Namespaces.xsl"/>

  <xsl:output method="xml" indent="yes"/>

  <xsl:strip-space elements="*"/>

  <!-- Date used for the metadata revision date. Default to the conversion date.
  The process description does not provide any date, so no resource date is set. -->
  <xsl:param name="date"
             select="format-date(current-date(), '[Y0001]-[M01]-[D01]')"/>

  <xsl:variable name="executeRel"
                select="'http://www.opengis.net/def/rel/ogc/1.0/execute'"/>

  <xsl:template match="/record">
    <mdb:MD_Metadata>
      <xsl:call-template name="add-iso19115-3.2018-namespaces"/>
      <mdb:metadataIdentifier>
        <mcc:MD_Identifier>
          <mcc:code>
            <gco:CharacterString>
              <!-- uuid is the record identifier set by the harvester (see recordIdPath),
               fallback to the process id. -->
              <xsl:value-of select="(uuid[. != ''], id)[1]"/>
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
        <xsl:with-param name="value" select="$date"/>
        <xsl:with-param name="type" select="'revision'"/>
      </xsl:call-template>

      <mdb:metadataStandard>
        <cit:CI_Citation>
          <cit:title>
            <gco:CharacterString>ISO 19115-3</gco:CharacterString>
          </cit:title>
        </cit:CI_Citation>
      </mdb:metadataStandard>

      <mdb:identificationInfo>
        <srv:SV_ServiceIdentification>
          <mri:citation>
            <cit:CI_Citation>
              <cit:title>
                <gco:CharacterString>
                  <xsl:value-of select="title"/>
                </gco:CharacterString>
              </cit:title>

              <cit:edition>
                <gco:CharacterString>
                  <xsl:value-of select="version"/>
                </gco:CharacterString>
              </cit:edition>
              <cit:identifier>
                <mcc:MD_Identifier>
                  <mcc:code>
                    <gcx:Anchor xlink:href="{apiUrl}">
                      <xsl:value-of select="id"/>
                    </gcx:Anchor>
                  </mcc:code>
                </mcc:MD_Identifier>
              </cit:identifier>
            </cit:CI_Citation>
          </mri:citation>

          <!-- Execution modes and example are appended to the abstract
          so that they are displayed in the default record view. -->
          <xsl:variable name="usage">
            <xsl:call-template name="build-usage-description"/>
          </xsl:variable>
          <mri:abstract>
            <gco:CharacterString>
              <xsl:value-of select="string-join((normalize-space(description)[. != ''],
                                                 replace($usage, '\s+$', '')[. != '']),
                                                '&#10;&#10;')"/>
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


          <srv:serviceType>
            <gco:ScopedName>OGC API Processes</gco:ScopedName>
          </srv:serviceType>
          <srv:serviceStandard>
            <cit:CI_Citation>
              <cit:title>
                <gco:CharacterString>OGC API - Processes - Part 1: Core</gco:CharacterString>
              </cit:title>
              <cit:identifier>
                <mcc:MD_Identifier>
                  <mcc:code>
                    <gcx:Anchor xlink:href="http://www.opengis.net/doc/IS/ogcapi-processes-1/1.0">OGC 18-062r2</gcx:Anchor>
                  </mcc:code>
                </mcc:MD_Identifier>
              </cit:identifier>
            </cit:CI_Citation>
          </srv:serviceStandard>

          <!-- The execute endpoint of the process.
          Default to the standard /execution path if no link is provided. -->
          <xsl:variable name="executeUrl"
                        select="(links[rel = $executeRel]/href[. != ''],
                                 if (normalize-space(apiUrl) != '')
                                 then concat(replace(normalize-space(apiUrl), '/$', ''), '/execution')
                                 else ())[1]"/>
          <xsl:if test="$executeUrl">
            <srv:containsOperations>
              <srv:SV_OperationMetadata>
                <srv:operationName>
                  <gco:CharacterString>execute</gco:CharacterString>
                </srv:operationName>
                <srv:distributedComputingPlatform>
                  <srv:DCPList codeList="codeListLocation#DCPList" codeListValue="WebServices"/>
                </srv:distributedComputingPlatform>
                <srv:invocationName>
                  <gco:CharacterString>
                    <xsl:value-of select="id"/>
                  </gco:CharacterString>
                </srv:invocationName>
                <srv:connectPoint>
                  <cit:CI_OnlineResource>
                    <cit:linkage>
                      <gco:CharacterString>
                        <xsl:value-of select="$executeUrl"/>
                      </gco:CharacterString>
                    </cit:linkage>
                    <cit:protocol>
                      <gco:CharacterString>OGC API Processes</gco:CharacterString>
                    </cit:protocol>
                  </cit:CI_OnlineResource>
                </srv:connectPoint>
              </srv:SV_OperationMetadata>
            </srv:containsOperations>
          </xsl:if>
        </srv:SV_ServiceIdentification>
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
                                <xsl:variable name="min"
                                              select="if (normalize-space(minOccurs) != '')
                                                      then normalize-space(minOccurs) else '1'"/>
                                <xsl:variable name="max"
                                              select="if (normalize-space(maxOccurs) = 'unbounded') then '*'
                                                      else if (normalize-space(maxOccurs) != '')
                                                      then normalize-space(maxOccurs) else '1'"/>
                                <xsl:value-of select="concat($min, '..', $max)"/>
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
              <!-- Links are often repeated in process descriptions, keep one per href and rel.
              Job list is a server endpoint not specific to the process, skip it. -->
              <xsl:for-each-group select="links[normalize-space(href) != ''
                                                and not(ends-with(rel, '/job-list'))]"
                                  group-by="concat(normalize-space(href), '|', rel)">
                <xsl:variable name="isExecute"
                              select="rel = $executeRel"/>
                <mrd:onLine>
                  <cit:CI_OnlineResource>
                    <cit:linkage>
                      <gco:CharacterString>
                        <xsl:value-of select="normalize-space(href)"/>
                      </gco:CharacterString>
                    </cit:linkage>
                    <cit:protocol>
                      <gco:CharacterString>
                        <xsl:value-of select="if ($isExecute) then 'OGC API Processes' else 'WWW:LINK'"/>
                      </gco:CharacterString>
                    </cit:protocol>
                    <xsl:if test="title != '' or type != ''">
                      <cit:name>
                        <xsl:choose>
                          <xsl:when test="type != ''">
                            <gcx:MimeFileType type="{type}">
                              <xsl:value-of select="title"/>
                            </gcx:MimeFileType>
                          </xsl:when>
                          <xsl:otherwise>
                            <gco:CharacterString>
                              <xsl:value-of select="title"/>
                            </gco:CharacterString>
                          </xsl:otherwise>
                        </xsl:choose>
                      </cit:name>
                    </xsl:if>
                    <xsl:if test="description != ''">
                      <cit:description>
                        <gco:CharacterString>
                          <xsl:value-of select="description"/>
                        </gco:CharacterString>
                      </cit:description>
                    </xsl:if>
                    <!-- No ISO function code matches the execute endpoint, so leave it out. -->
                    <xsl:if test="not($isExecute)">
                      <cit:function>
                        <cit:CI_OnLineFunctionCode
                          codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_OnLineFunctionCode"
                          codeListValue="{if (rel = 'self') then 'completeMetadata' else 'information'}"/>
                      </cit:function>
                    </xsl:if>
                  </cit:CI_OnlineResource>
                </mrd:onLine>
              </xsl:for-each-group>
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
    <xsl:param name="type" select="'revision'"/>
    <xsl:element name="{$tag}">
      <cit:CI_Date>
        <cit:date>
          <gco:Date>
            <xsl:value-of select="$value"/>
          </gco:Date>
        </cit:date>
        <cit:dateType>
          <cit:CI_DateTypeCode codeList="codeListLocation#CI_DateTypeCode" codeListValue="{$type}"/>
        </cit:dateType>
      </cit:CI_Date>
    </xsl:element>
  </xsl:template>

  <!-- Plain text description of execution modes and example request.
  The record view displays the abstract with line breaks but does not render Markdown.
  Only use xsl:text so that the stylesheet indentation is not in the output. -->
  <xsl:template name="build-usage-description">
    <xsl:if test="jobControlOptions">
      <xsl:text>Execution modes: </xsl:text>
      <xsl:value-of select="string-join(jobControlOptions/normalize-space(.), ', ')"/>
      <xsl:text>&#10;</xsl:text>
    </xsl:if>
    <xsl:for-each select="example/*">
      <xsl:text>&#10;Example </xsl:text>
      <xsl:value-of select="name()"/>
      <xsl:text>:&#10;</xsl:text>
      <xsl:for-each select="*">
        <xsl:text>- </xsl:text>
        <xsl:value-of select="name()"/>
        <xsl:text>: </xsl:text>
        <xsl:value-of select="normalize-space(.)"/>
        <xsl:text>&#10;</xsl:text>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
