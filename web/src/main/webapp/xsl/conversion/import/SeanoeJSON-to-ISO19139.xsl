<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                exclude-result-prefixes="#all">

  <xsl:output method="xml" indent="yes"/>

  <xsl:strip-space elements="*"/>

  <xsl:template match="/record">
    <gmd:MD_Metadata>
      <xsl:namespace name="xsi" select="'http://www.w3.org/2001/XMLSchema-instance'"/>
      <xsl:namespace name="gco" select="'http://www.isotc211.org/2005/gco'"/>
      <xsl:namespace name="gmd" select="'http://www.isotc211.org/2005/gmd'"/>
      <xsl:namespace name="srv" select="'http://www.isotc211.org/2005/srv'"/>
      <xsl:namespace name="gmx" select="'http://www.isotc211.org/2005/gmx'"/>
      <xsl:namespace name="gml" select="'http://www.opengis.net/gml/3.2'"/>
      <xsl:namespace name="xlink" select="'http://www.w3.org/1999/xlink'"/>
      <gmd:fileIdentifier>
        <gco:CharacterString>
          <xsl:value-of select="concat('seanoe:', uuid)"/>
        </gco:CharacterString>
      </gmd:fileIdentifier>

      <gmd:language>
        <gmd:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/" codeListValue="eng"/>
      </gmd:language>

      <gmd:characterSet>
        <gmd:MD_CharacterSetCode codeListValue="utf8"
                                 codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_CharacterSetCode"
        />
      </gmd:characterSet>

      <gmd:hierarchyLevel>
        <!-- TODO: Maybe map all possible types of Seanoe to valid ISO types. Currently lowercasing only eg. Dataset > dataset -->
        <gmd:MD_ScopeCode
          codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_ScopeCode"
          codeListValue="{lower-case(type)}"/>
      </gmd:hierarchyLevel>

      <xsl:for-each select="publisher">
        <gmd:contact>
          <gmd:CI_ResponsibleParty>
            <gmd:organisationName>
              <gco:CharacterString>
                <xsl:value-of select="name"/>
              </gco:CharacterString>
            </gmd:organisationName>
            <gmd:contactInfo>
              <gmd:CI_Contact>
                <xsl:for-each select="url">
                  <gmd:onlineResource>
                    <gmd:CI_OnlineResource>
                      <gmd:linkage>
                        <gmd:URL>
                          <xsl:value-of select="."/>
                        </gmd:URL>
                      </gmd:linkage>
                    </gmd:CI_OnlineResource>
                  </gmd:onlineResource>
                </xsl:for-each>
              </gmd:CI_Contact>
            </gmd:contactInfo>
            <gmd:role>
              <gmd:CI_RoleCode codeListValue="publisher"
                               codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_RoleCode"
              />
            </gmd:role>
          </gmd:CI_ResponsibleParty>
        </gmd:contact>
      </xsl:for-each>

      <gmd:dateStamp>
        <gco:Date>
          <xsl:value-of select="date_update"/>
        </gco:Date>
      </gmd:dateStamp>

      <gmd:metadataStandardName>
        <gco:CharacterString>ISO 19115:2003/19139</gco:CharacterString>
      </gmd:metadataStandardName>

      <gmd:metadataStandardVersion>
        <gco:CharacterString>1.0</gco:CharacterString>
      </gmd:metadataStandardVersion>

      <gmd:identificationInfo>
        <gmd:MD_DataIdentification>
          <gmd:citation>
            <gmd:CI_Citation>
              <gmd:title>
                <gco:CharacterString>
                  <xsl:value-of select="title"/>
                </gco:CharacterString>
              </gmd:title>
              <xsl:for-each select="date_publication">
                <gmd:date>
                  <gmd:CI_Date>
                    <gmd:date>
                      <gco:Date>
                        <xsl:value-of select="."/>
                      </gco:Date>
                    </gmd:date>
                    <gmd:dateType>
                      <gmd:CI_DateTypeCode
                        codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_DateTypeCode"
                        codeListValue="publication"/>
                    </gmd:dateType>
                  </gmd:CI_Date>
                </gmd:date>
              </xsl:for-each>
              <xsl:for-each select="date_update">
                <gmd:date>
                  <gmd:CI_Date>
                    <gmd:date>
                      <gco:Date>
                        <xsl:value-of select="."/>
                      </gco:Date>
                    </gmd:date>
                    <gmd:dateType>
                      <gmd:CI_DateTypeCode
                        codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_DateTypeCode"
                        codeListValue="revision"/>
                    </gmd:dateType>
                  </gmd:CI_Date>
                </gmd:date>
              </xsl:for-each>

              <xsl:for-each select="how_to_cite">
                <gmd:otherCitationDetails>
                  <gco:CharacterString>
                    <xsl:value-of select="."/>
                  </gco:CharacterString>
                </gmd:otherCitationDetails>
              </xsl:for-each>
            </gmd:CI_Citation>
          </gmd:citation>

          <gmd:abstract>
            <gco:CharacterString>
              <xsl:value-of select="abstract"/>
            </gco:CharacterString>
          </gmd:abstract>

          <xsl:for-each select="acknowledgments">
            <gmd:credit>
              <gco:CharacterString>
                <xsl:value-of select="."/>
              </gco:CharacterString>
            </gmd:credit>
          </xsl:for-each>

          <xsl:for-each select="contributors">
            <xsl:call-template name="build-contact">
              <xsl:with-param name="role" select="'custodian'"/>
            </xsl:call-template>
          </xsl:for-each>

          <xsl:for-each select="authors">
            <xsl:variable name="orgIndex" select="Indice_organisme"/>
            <xsl:variable name="authorOrg" select="/record/organismes[./Indice = $orgIndex]"/>

            <xsl:call-template name="build-contact">
              <xsl:with-param name="role" select="'author'"/>
              <xsl:with-param name="org" select="$authorOrg/Name"/>
            </xsl:call-template>
          </xsl:for-each>


          <xsl:for-each select="illustration_image_URL[. != '']">
            <gmd:graphicOverview>
              <gmd:MD_BrowseGraphic>
                <gmd:fileName>
                  <gco:CharacterString>
                    <xsl:value-of select="."/>
                  </gco:CharacterString>
                </gmd:fileName>
                <xsl:if test="../illustration_image_caption != ''">
                  <gmd:fileDescription>
                    <gco:CharacterString>
                      <xsl:value-of select="../illustration_image_caption"/>
                    </gco:CharacterString>
                  </gmd:fileDescription>
                </xsl:if>
              </gmd:MD_BrowseGraphic>
            </gmd:graphicOverview>
          </xsl:for-each>

          <gmd:descriptiveKeywords>
            <gmd:MD_Keywords>
              <xsl:for-each select="thematique|keywords">
                <gmd:keyword>
                  <gco:CharacterString>
                    <xsl:value-of select="."/>
                  </gco:CharacterString>
                </gmd:keyword>
              </xsl:for-each>
              <gmd:type>
                <gmd:MD_KeywordTypeCode codeListValue="theme"
                                        codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_KeywordTypeCode"/>
              </gmd:type>
            </gmd:MD_Keywords>
          </gmd:descriptiveKeywords>

          <xsl:if test="oceanographic_cruise">
            <gmd:descriptiveKeywords>
              <gmd:MD_Keywords>
                <xsl:for-each select="oceanographic_cruise">
                  <gmd:keyword>
                    <xsl:choose>
                      <xsl:when test="doi">
                        <gmx:Anchor xlink:href="https://doi.org/{doi}">
                          <xsl:value-of select="name"/>
                        </gmx:Anchor>
                      </xsl:when>
                      <xsl:otherwise>
                        <gco:CharacterString>
                          <xsl:value-of select="name"/>
                        </gco:CharacterString>
                      </xsl:otherwise>
                    </xsl:choose>
                  </gmd:keyword>
                </xsl:for-each>
                <gmd:type>
                  <gmd:MD_KeywordTypeCode codeListValue="cruise"
                                          codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_KeywordTypeCode"/>
                </gmd:type>
              </gmd:MD_Keywords>
            </gmd:descriptiveKeywords>
          </xsl:if>


          <xsl:if test="european_project">
            <gmd:descriptiveKeywords>
              <gmd:MD_Keywords>
                <xsl:for-each select="european_project">
                  <gmd:keyword>
                    <gco:CharacterString>
                      <xsl:value-of select="concat(program, ' ', name, ' (Agreement: ', grant_agreement_id, ')')"/>
                    </gco:CharacterString>
                  </gmd:keyword>
                </xsl:for-each>
                <gmd:type>
                  <gmd:MD_KeywordTypeCode codeListValue="project"
                                          codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_KeywordTypeCode"/>
                </gmd:type>
              </gmd:MD_Keywords>
            </gmd:descriptiveKeywords>
          </xsl:if>

          <xsl:variable name="licences" as="node()*">
            <licence seanoe="CC0" link="https://creativecommons.org/publicdomain/zero/1.0/deed"
                     label="CC0 (Creative Commons - Transfer into public domain)"/>
            <licence seanoe="CC-BY" link="https://creativecommons.org/licenses/by/4.0/"
                     label="CC-BY (Creative Commons - Attribution)"/>
            <licence seanoe="CC-BY-NC" link="https://creativecommons.org/licenses/by-nc/4.0/"
                     label="CC-BY-NC (Creative Commons - Attribution, No commercial usage)"/>
            <licence seanoe="CC-BY-SA" link="https://creativecommons.org/licenses/by-sa/4.0/"
                     label="CC-BY-SA (Creative Commons - Attribution, Sharing under the same conditions)"/>
            <licence seanoe="CC-BY-ND" link="https://creativecommons.org/licenses/by-nd/4.0/"
                     label="CC-BY-ND (Creative Commons - Attribution, No modification)"/>
            <licence seanoe="CC-BY-NC-ND" link="https://creativecommons.org/licenses/by-nc-nd/4.0/"
                     label="CC-BY-NC-ND (Creative Commons - Attribution, No commercial usage, No modification)"/>
            <licence seanoe="CC-BY-NC-SA" link="https://creativecommons.org/licenses/by-nc-sa/4.0/"
                     label="CC-BY-NC-SA (Creative Commons - Attribution, No commercial usage, Sharing under the same conditions)"/>
          </xsl:variable>

          <gmd:resourceConstraints>
            <gmd:MD_LegalConstraints>
              <gmd:useLimitation>
                <xsl:variable name="seanoeLicence"
                              select="licence_creative_commons"/>
                <xsl:variable name="l"
                              select="$licences[@seanoe = $seanoeLicence]"/>
                <xsl:choose>
                  <xsl:when test="$l/@link != ''">
                    <gmx:Anchor xlink:href="{$l/@link}">
                      <xsl:value-of select="$l/@label"/>
                    </gmx:Anchor>
                  </xsl:when>
                  <xsl:otherwise>
                    <gco:CharacterString>
                      <xsl:value-of select="$seanoeLicence"/>
                    </gco:CharacterString>
                  </xsl:otherwise>
                </xsl:choose>
              </gmd:useLimitation>
              <gmd:useConstraints>
                <gmd:MD_RestrictionCode
                  codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_RestrictionCode"
                  codeListValue="otherRestrictions"/>
              </gmd:useConstraints>
              <gmd:otherConstraints>
                <gco:CharacterString>
                  <xsl:value-of select="disclaimer"/>
                </gco:CharacterString>
              </gmd:otherConstraints>
            </gmd:MD_LegalConstraints>
          </gmd:resourceConstraints>

          <xsl:for-each select="linked_dataset[doi]|linked_document[doi]">
            <gmd:aggregationInfo>
              <gmd:MD_AggregateInformation>
                <gmd:aggregateDataSetIdentifier>
                  <gmd:MD_Identifier>
                    <gmd:code>
                      <gmx:Anchor xlink:href="https://doi.org/{doi}"
                        xlink:title="{title}">
                        <xsl:value-of select="doi"/>
                      </gmx:Anchor>
                    </gmd:code>
                  </gmd:MD_Identifier>
                </gmd:aggregateDataSetIdentifier>
                <!-- TODO: Citation + view mode -->
                <gmd:associationType>
                  <gmd:DS_AssociationTypeCode codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#DS_AssociationTypeCode"
                                              codeListValue="crossReference"/>
                </gmd:associationType>
                <gmd:initiativeType>
                  <gmd:DS_InitiativeTypeCode codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#DS_InitiativeTypeCode"
                                             codeListValue="{if (name() = 'linked_dataset') then 'dataset' else 'document'}"/>
                </gmd:initiativeType>
              </gmd:MD_AggregateInformation>
            </gmd:aggregationInfo>
          </xsl:for-each>

          <gmd:language>
            <gmd:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/" codeListValue="eng"/>
          </gmd:language>

          <gmd:topicCategory>
            <gmd:MD_TopicCategoryCode>oceans</gmd:MD_TopicCategoryCode>
          </gmd:topicCategory>

          <xsl:for-each select="geographical_bounding_boxes">
            <gmd:extent>
              <gmd:EX_Extent>
                <gmd:geographicElement>
                  <gmd:EX_GeographicBoundingBox>
                    <gmd:westBoundLongitude>
                      <gco:Decimal>
                        <xsl:value-of select="west"/>
                      </gco:Decimal>
                    </gmd:westBoundLongitude>
                    <gmd:eastBoundLongitude>
                      <gco:Decimal>
                        <xsl:value-of select="east"/>
                      </gco:Decimal>
                    </gmd:eastBoundLongitude>
                    <gmd:southBoundLatitude>
                      <gco:Decimal>
                        <xsl:value-of select="south"/>
                      </gco:Decimal>
                    </gmd:southBoundLatitude>
                    <gmd:northBoundLatitude>
                      <gco:Decimal>
                        <xsl:value-of select="north"/>
                      </gco:Decimal>
                    </gmd:northBoundLatitude>
                  </gmd:EX_GeographicBoundingBox>
                </gmd:geographicElement>
              </gmd:EX_Extent>
            </gmd:extent>
          </xsl:for-each>


          <xsl:for-each select="Temporal_extend[count(*) > 0]">
            <gmd:extent>
              <gmd:EX_Extent>
                <gmd:temporalElement>
                  <gmd:EX_TemporalExtent>
                    <gmd:extent>
                      <gml:TimePeriod gml:id="d{generate-id()}">
                        <gml:beginPosition>
                          <xsl:value-of select="begin"/>
                        </gml:beginPosition>
                        <gml:endPosition>
                          <xsl:value-of select="end"/>
                        </gml:endPosition>
                      </gml:TimePeriod>
                    </gmd:extent>
                  </gmd:EX_TemporalExtent>
                </gmd:temporalElement>
              </gmd:EX_Extent>
            </gmd:extent>
          </xsl:for-each>

        </gmd:MD_DataIdentification>
      </gmd:identificationInfo>

      <gmd:distributionInfo>
        <gmd:MD_Distribution>
          <xsl:for-each select="distinct-values(data_files/Format)">
            <gmd:distributionFormat>
              <gmd:MD_Format>
                <gmd:name>
                  <gco:CharacterString>
                    <xsl:value-of select="."/>
                  </gco:CharacterString>
                </gmd:name>
                <gmd:version gco:nilReason="missing">
                  <gco:CharacterString/>
                </gmd:version>
              </gmd:MD_Format>
            </gmd:distributionFormat>
          </xsl:for-each>

          <gmd:transferOptions>
            <gmd:MD_DigitalTransferOptions>
              <xsl:for-each select="data_files">
                <gmd:onLine>
                  <gmd:CI_OnlineResource>
                    <gmd:linkage>
                      <gmd:URL>
                        <xsl:value-of select="url"/>
                      </gmd:URL>
                    </gmd:linkage>
                    <gmd:protocol>
                      <gco:CharacterString>
                        WWW:DOWNLOAD-1.0-link--download
                        <!--
                        The following would be more consistent with Geonetwork
                        <xsl:value-of select="if(Format != '')
                                              then concat('WWW:DOWNLOAD:', Format)
                                               else 'WWW:DOWNLOAD'"/>

                                               -->
                      </gco:CharacterString>
                    </gmd:protocol>
                    <gmd:name>
                      <gco:CharacterString>
                        <xsl:value-of select="Description_EN"/>
                      </gco:CharacterString>
                    </gmd:name>
                    <xsl:if test="traitement != ''">
                      <gmd:description>
                        <gco:CharacterString>
                          <xsl:value-of select="traitement"/>
                        </gco:CharacterString>
                      </gmd:description>
                    </xsl:if>
                  </gmd:CI_OnlineResource>
                </gmd:onLine>
              </xsl:for-each>


              <xsl:for-each select="associated_URL">
                <gmd:onLine>
                  <gmd:CI_OnlineResource>
                    <gmd:linkage>
                      <gmd:URL>
                        <xsl:value-of select="url"/>
                      </gmd:URL>
                    </gmd:linkage>
                    <gmd:protocol>
                      <gco:CharacterString>
                        WWW:LINK
                      </gco:CharacterString>
                    </gmd:protocol>
                    <gmd:name>
                      <gco:CharacterString>
                        <xsl:value-of select="description"/>
                      </gco:CharacterString>
                    </gmd:name>
                  </gmd:CI_OnlineResource>
                </gmd:onLine>
              </xsl:for-each>
            </gmd:MD_DigitalTransferOptions>
          </gmd:transferOptions>

          <gmd:transferOptions>
            <gmd:MD_DigitalTransferOptions>
              <gmd:onLine>
                <gmd:CI_OnlineResource>
                  <gmd:linkage>
                    <gmd:URL>
                      <xsl:value-of select="url"/>
                    </gmd:URL>
                  </gmd:linkage>
                  <gmd:protocol>
                    <gco:CharacterString>
                      WWW:LINK-1.0-http--metadata-URL
                    </gco:CharacterString>
                  </gmd:protocol>
                  <gmd:name>
                    <gco:CharacterString>
                      Landing Page
                    </gco:CharacterString>
                  </gmd:name>
                </gmd:CI_OnlineResource>
              </gmd:onLine>
            </gmd:MD_DigitalTransferOptions>
          </gmd:transferOptions>
        </gmd:MD_Distribution>
      </gmd:distributionInfo>

      <gmd:lineage>
        <gmd:LI_Lineage>
          <gmd:scope>
            <gmd:DQ_Scope>
              <gmd:level>
                <gmd:MD_ScopeCode codeListValue="dataset"
                                  codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_ScopeCode"/>
              </gmd:level>
            </gmd:DQ_Scope>
          </gmd:scope>
          <gmd:statement gco:nilReason="missing">
            <gco:CharacterString/>
          </gmd:statement>
        </gmd:LI_Lineage>
      </gmd:lineage>
    </gmd:MD_Metadata>
  </xsl:template>

  <xsl:template name="build-contact">
    <xsl:param name="role" select="'pointOfContact'"/>
    <xsl:param name="org" select="''"/>

    <gmd:pointOfContact>
      <gmd:CI_ResponsibleParty>
        <xsl:if test="Orcid">
          <xsl:attribute name="uuid" select="Orcid"/>
        </xsl:if>
        <xsl:variable name="name"
                      select="if (collectivity_author) then collectivity_author
                              else concat(LastName, ' ', FirstName)"/>
        <xsl:if test="$name != ''">
          <gmd:individualName>
            <gco:CharacterString>
              <xsl:value-of select="$name"/>
            </gco:CharacterString>
          </gmd:individualName>
        </xsl:if>
        <xsl:if test="$org != ''">
          <gmd:organisationName>
            <gco:CharacterString>
              <xsl:value-of select="$org"/>
            </gco:CharacterString>
          </gmd:organisationName>
        </xsl:if>
        <xsl:if test="Email">
          <gmd:contactInfo>
            <gmd:CI_Contact>
              <gmd:address>
                <gmd:CI_Address>
                  <gmd:electronicMailAddress>
                    <gco:CharacterString>
                      <xsl:value-of select="Email"/>
                    </gco:CharacterString>
                  </gmd:electronicMailAddress>
                </gmd:CI_Address>
              </gmd:address>
            </gmd:CI_Contact>
          </gmd:contactInfo>
        </xsl:if>
        <gmd:role>
          <gmd:CI_RoleCode codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_RoleCode"
                           codeListValue="{$role}">
          </gmd:CI_RoleCode>
        </gmd:role>
      </gmd:CI_ResponsibleParty>
    </gmd:pointOfContact>
  </xsl:template>

</xsl:stylesheet>
