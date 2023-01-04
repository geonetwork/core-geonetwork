<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
  xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
  xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
  xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
  xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
  xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
  xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
  xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
  xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:gn-fn-iso19115-3.2018="http://geonetwork-opensource.org/xsl/functions/profiles/iso19115-3.2018"
  exclude-result-prefixes="#all">

  <xsl:import href="../layout/utility-fn.xsl"/>
  <!-- TODO: could be nice to define the target distributor -->

  <!-- Main properties for the link.
  Name and description may be multilingual eg. ENG#English name|FRE#Le français
  Name and description may be a list of layer/feature type names and titles comma separated. -->
  <xsl:param name="protocol" select="'OGC:WMS'"/>
  <xsl:param name="url"/>
  <xsl:param name="name"/>
  <xsl:param name="desc"/>
  <xsl:param name="function"/>
  <xsl:param name="applicationProfile"/>
  <xsl:param name="catalogUrl"/>
  <xsl:param name="mimeType"/>
  <xsl:param name="mimeTypeStrategy" select="'protocol'"/>

  <!-- Add an optional uuidref attribute to the onLine element created. -->
  <xsl:param name="uuidref"/>

  <!-- In this case an external metadata is available under the
  extra element and all online resource from this records are added
  in this one. -->
  <xsl:param name="extra_metadata_uuid"/>

  <!-- Target element to update. The key is based on the concatenation
  of URL+Protocol+Name -->
  <xsl:param name="updateKey"/>


  <xsl:variable name="mainLang"
                select="/mdb:MD_Metadata/mdb:defaultLocale/*/lan:language/*/@codeListValue"
                as="xs:string?"/>

  <xsl:variable name="useOnlyPTFreeText"
                select="count(//*[lan:PT_FreeText and not(gco:CharacterString)]) > 0"
                as="xs:boolean"/>

  <xsl:variable name="metadataIdentifier"
                select="/mdb:MD_Metadata/mdb:metadataIdentifier[position() = 1]/mcc:MD_Identifier/mcc:code/gco:CharacterString"/>

  <xsl:template match="/mdb:MD_Metadata|*[contains(@gco:isoType, 'mdb:MD_Metadata')]">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="mdb:metadataIdentifier"/>
      <xsl:apply-templates select="mdb:defaultLocale"/>
      <xsl:apply-templates select="mdb:parentMetadata"/>
      <xsl:apply-templates select="mdb:metadataScope"/>
      <xsl:apply-templates select="mdb:contact"/>
      <xsl:apply-templates select="mdb:dateInfo"/>
      <xsl:apply-templates select="mdb:metadataStandard"/>
      <xsl:apply-templates select="mdb:metadataProfile"/>
      <xsl:apply-templates select="mdb:alternativeMetadataReference"/>
      <xsl:apply-templates select="mdb:otherLocale"/>
      <xsl:apply-templates select="mdb:metadataLinkage"/>
      <xsl:apply-templates select="mdb:spatialRepresentationInfo"/>
      <xsl:apply-templates select="mdb:referenceSystemInfo"/>
      <xsl:apply-templates select="mdb:metadataExtensionInfo"/>
      <xsl:apply-templates select="mdb:identificationInfo"/>
      <xsl:apply-templates select="mdb:contentInfo"/>


      <xsl:choose>
        <xsl:when
          test="count(mdb:distributionInfo) = 0">
          <mdb:distributionInfo>
            <mrd:MD_Distribution>
              <mrd:transferOptions>
                <mrd:MD_DigitalTransferOptions>
                  <xsl:call-template name="createOnlineSrc"/>
                </mrd:MD_DigitalTransferOptions>
              </mrd:transferOptions>
            </mrd:MD_Distribution>
          </mdb:distributionInfo>
        </xsl:when>
        <xsl:otherwise>
          <xsl:for-each select="mdb:distributionInfo">
            <xsl:copy>
              <xsl:copy-of select="@*"/>
              <mrd:MD_Distribution>
                <xsl:apply-templates select="mrd:MD_Distribution/mrd:description"/>
                <xsl:apply-templates select="mrd:MD_Distribution/mrd:distributionFormat"/>
                <xsl:apply-templates select="mrd:MD_Distribution/mrd:distributor"/>
                <xsl:choose>
                  <xsl:when test="position() = 1">
                    <mrd:transferOptions>
                      <mrd:MD_DigitalTransferOptions>
                        <xsl:apply-templates select="mrd:MD_Distribution/mrd:transferOptions[1]/mrd:MD_DigitalTransferOptions/mrd:unitsOfDistribution"/>
                        <xsl:apply-templates select="mrd:MD_Distribution/mrd:transferOptions[1]/mrd:MD_DigitalTransferOptions/mrd:transferSize"/>
                        <xsl:apply-templates select="mrd:MD_Distribution/mrd:transferOptions[1]/mrd:MD_DigitalTransferOptions/mrd:onLine"/>



                        <xsl:if test="$updateKey = ''">
                          <xsl:call-template name="createOnlineSrc"/>
                        </xsl:if>

                        <xsl:apply-templates select="mrd:MD_Distribution/mrd:transferOptions[1]/mrd:MD_DigitalTransferOptions/mrd:offLine"/>
                        <xsl:apply-templates select="mrd:MD_Distribution/mrd:transferOptions[1]/mrd:MD_DigitalTransferOptions/mrd:transferFrequency"/>
                        <xsl:apply-templates select="mrd:MD_Distribution/mrd:transferOptions[1]/mrd:MD_DigitalTransferOptions/mrd:distributionFormat"/>
                      </mrd:MD_DigitalTransferOptions>
                   </mrd:transferOptions>

                   <xsl:apply-templates
                     select="mrd:MD_Distribution/mrd:transferOptions[position() > 1]"/>
                 </xsl:when>
                 <xsl:otherwise>
                   <xsl:apply-templates select="mrd:MD_Distribution/mrd:transferOptions"/>
                 </xsl:otherwise>
               </xsl:choose>
              </mrd:MD_Distribution>
            </xsl:copy>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:apply-templates select="mdb:dataQualityInfo"/>
      <xsl:apply-templates select="mdb:resourceLineage"/>
      <xsl:apply-templates select="mdb:portrayalCatalogueInfo"/>
      <xsl:apply-templates select="mdb:metadataConstraints"/>
      <xsl:apply-templates select="mdb:applicationSchemaInfo"/>
      <xsl:apply-templates select="mdb:metadataMaintenance"/>
      <xsl:apply-templates select="mdb:acquisitionInformation"/>

    </xsl:copy>
  </xsl:template>

    <!-- Updating the link matching the update key. -->
  <xsl:template match="mrd:onLine[$updateKey != '' and
                      normalize-space($updateKey) = concat(
                      cit:CI_OnlineResource/cit:linkage/gco:CharacterString,
                      cit:CI_OnlineResource/cit:protocol/gco:CharacterString,
                      cit:CI_OnlineResource/cit:name/gco:CharacterString)
                      ]">
      <xsl:call-template name="createOnlineSrc"/>
  </xsl:template>


  <xsl:template name="createOnlineSrc">
    <!-- Add all online source from the target metadata to the
                    current one -->
    <xsl:if test="//extra">
      <xsl:for-each select="//extra//mrd:onLine">
        <mrd:onLine>
          <xsl:if test="$extra_metadata_uuid">
            <xsl:attribute name="uuidref" select="$extra_metadata_uuid"/>
          </xsl:if>
          <xsl:copy-of select="*"/>
        </mrd:onLine>
      </xsl:for-each>
    </xsl:if>

    <!-- Add online source from URL -->
    <xsl:if test="$url">
      <!-- If a name is provided loop on all languages -->
      <xsl:choose>
        <xsl:when test="starts-with($protocol, 'OGC:') and contains($name, ',')">
          <xsl:for-each select="tokenize($name, ',')">
            <mrd:onLine>
              <cit:CI_OnlineResource>
                <cit:linkage>
                  <xsl:copy-of select="gn-fn-iso19115-3.2018:fillTextElement($url, '◿', '◿', $mainLang, $useOnlyPTFreeText)"/>
                </cit:linkage>

                <xsl:if test="$protocol != ''">
                  <cit:protocol>
                    <gco:CharacterString>
                      <xsl:value-of select="$protocol"/>
                    </gco:CharacterString>
                  </cit:protocol>
                </xsl:if>

                <xsl:if test="$applicationProfile != ''">
                  <cit:applicationProfile>
                    <gco:CharacterString>
                      <xsl:value-of select="$applicationProfile"/>
                    </gco:CharacterString>
                  </cit:applicationProfile>
                </xsl:if>

                <xsl:if test="normalize-space(.) != ''">
                  <cit:name>
                    <xsl:copy-of select="gn-fn-iso19115-3.2018:fillTextElement(., $mainLang, $useOnlyPTFreeText)"/>
                  </cit:name>
                </xsl:if>
                <xsl:variable name="pos" select="position()"/>
                <xsl:variable name="description" select="tokenize($desc, ',')[position() = $pos]"/>
                <xsl:if test="$description != ''">
                  <cit:description>
                    <xsl:copy-of select="gn-fn-iso19115-3.2018:fillTextElement($description, $mainLang, $useOnlyPTFreeText)"/>
                  </cit:description>
                </xsl:if>

                <xsl:if test="$function != ''">
                  <cit:function>
                    <cit:CI_OnLineFunctionCode codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_OnLineFunctionCode"
                                               codeListValue="{$function}"/>
                  </cit:function>
                </xsl:if>
              </cit:CI_OnlineResource>
            </mrd:onLine>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <mrd:onLine>
            <cit:CI_OnlineResource>
              <cit:linkage>
                <xsl:copy-of select="gn-fn-iso19115-3.2018:fillTextElement($url, '◿', '◿', $mainLang, $useOnlyPTFreeText)"/>
              </cit:linkage>

              <xsl:if test="$protocol != ''">
                <cit:protocol>
                  <xsl:call-template name="setProtocol"/>
                </cit:protocol>
              </xsl:if>

              <xsl:if test="$applicationProfile != ''">
                <cit:applicationProfile>
                  <gco:CharacterString>
                    <xsl:value-of select="$applicationProfile"/>
                  </gco:CharacterString>
                </cit:applicationProfile>
              </xsl:if>

              <xsl:if test="normalize-space($name) != ''">
                <cit:name>
                  <xsl:copy-of select="gn-fn-iso19115-3.2018:fillTextElement($name, $mainLang, $useOnlyPTFreeText)"/>
                </cit:name>
              </xsl:if>

              <xsl:if test="$desc != ''">
                <cit:description>
                  <xsl:copy-of select="gn-fn-iso19115-3.2018:fillTextElement($desc, $mainLang, $useOnlyPTFreeText)"/>
                </cit:description>
              </xsl:if>

              <xsl:if test="$function != ''">
                <cit:function>
                  <cit:CI_OnLineFunctionCode codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_OnLineFunctionCode"
                                             codeListValue="{$function}"/>
                </cit:function>
              </xsl:if>
            </cit:CI_OnlineResource>
          </mrd:onLine>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>


  <xsl:template name="setProtocol">
    <xsl:choose>
      <xsl:when test="$mimeTypeStrategy = 'mimeType'">
        <gcx:MimeFileType type="{$mimeType}">
          <xsl:value-of select="$protocol"/>
        </gcx:MimeFileType>
      </xsl:when>
      <xsl:when test="$mimeTypeStrategy = 'protocol' and $mimeType != ''">
        <gco:CharacterString>
          <xsl:value-of select="concat($protocol, ':', $mimeType)"/>
        </gco:CharacterString>
      </xsl:when>
      <xsl:otherwise>
        <gco:CharacterString>
          <xsl:value-of select="$protocol"/>
        </gco:CharacterString>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- Remove geonet:* elements. -->
  <xsl:template match="gn:*" priority="2"/>

  <!-- Copy everything. -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
