<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
  xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
  xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
  xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
  xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
  xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
  xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
  xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
  xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
  xmlns:gml="http://www.opengis.net/gml/3.2"
  xmlns:java-xsl-util="java:org.fao.geonet.util.XslUtil"
  exclude-result-prefixes="#all">

  <xsl:import href="ISO19139/utility/create19115-3Namespaces.xsl" />
  <xsl:import href="STAC/utility/add-stac-contact.xsl" />
  <xsl:import href="STAC/utility/add-stac-provider.xsl" />
  <xsl:output method="xml" indent="yes" />
  <xsl:strip-space elements="*" />

  <xsl:template match="/record|*">
    <mdb:MD_Metadata>
      <xsl:call-template name="add-iso19115-3.2018-namespaces" />
      <mdb:metadataIdentifier>
        <mcc:MD_Identifier>
          <mcc:code>
            <gco:CharacterString>
              <xsl:value-of select="(id)[1]" />
            </gco:CharacterString>
          </mcc:code>
        </mcc:MD_Identifier>
      </mdb:metadataIdentifier>
      <mdb:defaultLocale>
        <lan:PT_Locale>
          <lan:language>
            <lan:LanguageCode codeList="codeListLocation#LanguageCode"
              codeListValue="{java-xsl-util:threeCharLangCode(
                                (language)[1])}" />
          </lan:language>
          <lan:characterEncoding>
            <lan:MD_CharacterSetCode codeList="codeListLocation#MD_CharacterSetCode"
              codeListValue="utf8" />
          </lan:characterEncoding>
        </lan:PT_Locale>
      </mdb:defaultLocale>

      <mdb:metadataScope>
        <mdb:MD_MetadataScope>
          <mdb:resourceScope>
            <mcc:MD_ScopeCode
              codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_ScopeCode"
              codeListValue="series" />
          </mdb:resourceScope>
          <mdb:name>
            <gco:CharacterString>Data collection</gco:CharacterString>
          </mdb:name>
        </mdb:MD_MetadataScope>
      </mdb:metadataScope>
      <xsl:choose>
        <xsl:when test="contacts or contact or providers">
          <xsl:for-each select="contacts">
            <xsl:call-template name="map-stac-contact">
              <xsl:with-param name="contactNode" select="." />
            </xsl:call-template>
          </xsl:for-each>

          <xsl:if
            test="contact">
            <xsl:call-template name="map-stac-contact">
              <xsl:with-param name="contactNode" select="contact" />
            </xsl:call-template>
          </xsl:if>

          <xsl:if
            test="not(contacts) and not(contact) and count(providers) > 0">
            <xsl:for-each select="providers[1]">
              <mdb:contact>
                <cit:CI_Responsibility>
                  <cit:role>
                    <cit:CI_RoleCode codeList="codeListLocation#CI_RoleCode"
                      codeListValue="pointOfContact" />
                  </cit:role>
                  <cit:party>
                    <cit:CI_Organisation>
                      <cit:name>
                        <gco:CharacterString>
                          <xsl:value-of select="name" />
                        </gco:CharacterString>
                      </cit:name>
                      <xsl:if test="url">
                        <cit:contactInfo>
                          <cit:CI_Contact>
                            <cit:onlineResource>
                              <cit:CI_OnlineResource>
                                <cit:linkage>
                                  <gco:CharacterString>
                                    <xsl:value-of select="url" />
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
              </mdb:contact>
            </xsl:for-each>
          </xsl:if>
        </xsl:when>
      </xsl:choose>

      <mdb:dateInfo>
        <cit:CI_Date>
          <cit:date>
            <gco:DateTime>
              <xsl:value-of select="current-dateTime()" />
            </gco:DateTime>
          </cit:date>
          <cit:dateType>
            <cit:CI_DateTypeCode
              codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_DateTypeCode"
              codeListValue="creation" />
          </cit:dateType>
        </cit:CI_Date>
      </mdb:dateInfo>

      <mdb:identificationInfo>
        <mri:MD_DataIdentification>
          <mri:citation>
            <cit:CI_Citation>
              <cit:title>
                <gco:CharacterString>
                  <xsl:value-of
                    select="if ((title)[1] and string-length((title)[1]) > 0) then (title)[1]
                                        else if ((id)[1] and string-length((id)[1]) > 0) then concat('STAC Collection: ', (id)[1])
                                        else 'STAC Collection'" />
                </gco:CharacterString>
              </cit:title>
            </cit:CI_Citation>
          </mri:citation>
          <mri:abstract>
            <gco:CharacterString>
              <xsl:value-of
                select="if ((description)[1] and string-length((description)[1]) > 0)
                                    then (description)[1]
                                    else 'STAC Collection harvested via STAC API'" />
            </gco:CharacterString>
          </mri:abstract>
          <xsl:variable name="thumbnailAssets" select="assets/*[roles[contains(., 'thumbnail')]]" />
          <mri:extent>
            <gex:EX_Extent>
              <xsl:choose>
                <xsl:when test="extent/spatial/bbox">
                  <xsl:for-each select="extent/spatial/bbox">
                    <!-- Only create bounding box if all components are valid -->
                    <xsl:if
                      test="*[1] and *[1] != 'null' and
                                  *[2] and *[2] != 'null' and
                                  *[3] and *[3] != 'null' and
                                  *[4] and *[4] != 'null' ">
                      <gex:geographicElement>
                        <gex:EX_GeographicBoundingBox>
                          <gex:westBoundLongitude>
                            <gco:Decimal>
                              <xsl:value-of select="*[1]" />
                            </gco:Decimal>
                          </gex:westBoundLongitude>
                          <gex:eastBoundLongitude>
                            <gco:Decimal>
                              <xsl:value-of select="*[3]" />
                            </gco:Decimal>
                          </gex:eastBoundLongitude>
                          <gex:southBoundLatitude>
                            <gco:Decimal>
                              <xsl:value-of select="*[2]" />
                            </gco:Decimal>
                          </gex:southBoundLatitude>
                          <gex:northBoundLatitude>
                            <gco:Decimal>
                              <xsl:value-of select="*[4]" />
                            </gco:Decimal>
                          </gex:northBoundLatitude>
                        </gex:EX_GeographicBoundingBox>
                      </gex:geographicElement>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:when>
              </xsl:choose>

              <xsl:if test="extent/temporal/interval">
                <xsl:for-each select="extent/temporal/interval">
                  <xsl:if
                    test="(*[1] and *[1] != 'null' and *[1] != '') or (*[2] and *[2] != 'null' and *[2] != '')">
                    <gex:temporalElement>
                      <gex:EX_TemporalExtent>
                        <gex:extent>
                          <gml:TimePeriod>
                            <xsl:if test="*[1] and *[1] != 'null' and *[1] != ''">
                              <gml:beginPosition>
                                <xsl:value-of select="*[1]" />
                              </gml:beginPosition>
                            </xsl:if>
                            <xsl:if test="*[2] and *[2] != 'null' and *[2] != ''">
                              <gml:endPosition>
                                <xsl:value-of select="*[2]" />
                              </gml:endPosition>
                            </xsl:if>
                          </gml:TimePeriod>
                        </gex:extent>
                      </gex:EX_TemporalExtent>
                    </gex:temporalElement>
                  </xsl:if>
                </xsl:for-each>
              </xsl:if>
            </gex:EX_Extent>
          </mri:extent>
          <xsl:if test="count($thumbnailAssets) > 0">
            <mri:graphicOverview>
              <mcc:MD_BrowseGraphic>
                <mcc:fileName>
                  <gco:CharacterString>
                    <xsl:value-of select="$thumbnailAssets[1]/href" />
                  </gco:CharacterString>
                </mcc:fileName>
                <mcc:fileDescription>
                  <gco:CharacterString>
                    <xsl:choose>
                      <xsl:when test="$thumbnailAssets[1]/title and $thumbnailAssets[1]/description">
                        <xsl:value-of
                          select="concat($thumbnailAssets[1]/title, ' - ', $thumbnailAssets[1]/description)" />
                      </xsl:when>
                      <xsl:when test="$thumbnailAssets[1]/title">
                        <xsl:value-of select="$thumbnailAssets[1]/title" />
                      </xsl:when>
                      <xsl:when test="$thumbnailAssets[1]/description">
                        <xsl:value-of select="$thumbnailAssets[1]/description" />
                      </xsl:when>
                      <xsl:otherwise>Preview image</xsl:otherwise>
                    </xsl:choose>
                  </gco:CharacterString>
                </mcc:fileDescription>
                <mcc:fileType>
                  <gco:CharacterString>
                    <xsl:value-of select="$thumbnailAssets[1]/type" />
                  </gco:CharacterString>
                </mcc:fileType>
              </mcc:MD_BrowseGraphic>
            </mri:graphicOverview>
          </xsl:if>
          <xsl:variable name="keywords"
            select="keywords" />
          <xsl:if test="$keywords">
            <mri:descriptiveKeywords>
              <mri:MD_Keywords>
                <xsl:for-each select="$keywords">
                  <mri:keyword>
                    <gco:CharacterString>
                      <xsl:value-of select="." />
                    </gco:CharacterString>
                  </mri:keyword>
                </xsl:for-each>
              </mri:MD_Keywords>
            </mri:descriptiveKeywords>
          </xsl:if>
          <mri:resourceConstraints>
            <mco:MD_LegalConstraints>
              <mco:accessConstraints>
                <mco:MD_RestrictionCode
                  codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_RestrictionCode"
                  codeListValue="license" />
              </mco:accessConstraints>
              <mco:accessConstraints>
                <mco:MD_RestrictionCode
                  codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_RestrictionCode"
                  codeListValue="otherRestrictions" />
              </mco:accessConstraints>
              <mco:otherConstraints>
                <gco:CharacterString>
                  <xsl:value-of select="license" />
                </gco:CharacterString>
              </mco:otherConstraints>
            </mco:MD_LegalConstraints>
          </mri:resourceConstraints>

          <mri:defaultLocale>
            <lan:PT_Locale>
              <lan:language>
                <lan:LanguageCode codeList="codeListLocation#LanguageCode"
                  codeListValue="{java-xsl-util:threeCharLangCode(
                                    (language)[1])}" />
              </lan:language>
              <lan:characterEncoding>
                <lan:MD_CharacterSetCode codeList="codeListLocation#MD_CharacterSetCode"
                  codeListValue="utf8" />
              </lan:characterEncoding>
            </lan:PT_Locale>
          </mri:defaultLocale>
        </mri:MD_DataIdentification>
      </mdb:identificationInfo>

      <mdb:distributionInfo>
        <mrd:MD_Distribution>
          <mrd:transferOptions>
            <mrd:MD_DigitalTransferOptions>
              <xsl:for-each select="links[rel = 'self']">
                <mrd:onLine>
                  <cit:CI_OnlineResource>
                    <cit:linkage>
                      <gco:CharacterString>
                        <xsl:value-of select="href" />
                      </gco:CharacterString>
                    </cit:linkage>
                    <cit:protocol>
                      <gco:CharacterString>STAC</gco:CharacterString>
                    </cit:protocol>
                    <cit:name>
                      <gco:CharacterString>
                        <xsl:value-of select="title" />
                      </gco:CharacterString>
                    </cit:name>
                    <cit:description>
                      <gco:CharacterString>STAC Collection</gco:CharacterString>
                    </cit:description>
                  </cit:CI_OnlineResource>
                </mrd:onLine>
              </xsl:for-each>
            </mrd:MD_DigitalTransferOptions>
          </mrd:transferOptions>
        </mrd:MD_Distribution>
      </mdb:distributionInfo>
    </mdb:MD_Metadata>
  </xsl:template>
</xsl:stylesheet>