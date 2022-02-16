<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                exclude-result-prefixes="#all">

    <xsl:output method="xml" indent="yes"/>

    <xsl:strip-space elements="*"/>

    <xsl:template match="/record">

        <gmd:MD_Metadata>
            <gmd:fileIdentifier>
                <gco:CharacterString>
                    <xsl:value-of select="uuid"/>
                </gco:CharacterString>
            </gmd:fileIdentifier>

            <gmd:language>
                <gco:CharacterString>eng</gco:CharacterString>
            </gmd:language>

            <gmd:characterSet>
                <gmd:MD_CharacterSetCode codeListValue="utf8"
                                         codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#MD_CharacterSetCode"/>
            </gmd:characterSet>

            <gmd:hierarchyLevel>
                <gmd:MD_ScopeCode codeList="" codeListValue="{type}"/>
            </gmd:hierarchyLevel>

            <gmd:citedResponsibleParty>
                <gmd:CI_ResponsibleParty>
                    <gmd:organisationName>
                        <gco:CharacterString>
                            <xsl:value-of select="publisher"/>
                        </gco:CharacterString>
                    </gmd:organisationName>
                    <gmd:role>
                        <gmd:CI_RoleCode
                                codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#CI_RoleCode"
                                codeListValue="publisher"/>
                    </gmd:role>
                </gmd:CI_ResponsibleParty>
            </gmd:citedResponsibleParty>

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
                            <gmd:date>
                                <gmd:CI_Date>
                                    <gmd:date>
                                        <gco:Date>
                                            <xsl:value-of select="date_publication"/>
                                        </gco:Date>
                                    </gmd:date>
                                    <gmd:dateType>
                                        <gmd:CI_DateTypeCode
                                                codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#CI_DateTypeCode"
                                                codeListValue="publication"/>
                                    </gmd:dateType>
                                </gmd:CI_Date>
                            </gmd:date>
                        </gmd:CI_Citation>
                    </gmd:citation>

                    <gmd:abstract>
                        <gco:CharacterString>
                            <xsl:value-of select="abstract"/>
                        </gco:CharacterString>
                    </gmd:abstract>

                    <xsl:for-each select="/record/authors">
                        <xsl:variable name="orgIndex" select="Indice_organisme"/>
                        <xsl:variable name="authorOrg" select="/record/organismes[./Indice=$orgIndex]"/>
                        <gmd:pointOfContact>
                            <gmd:CI_ResponsibleParty>
                                <gmd:role>
                                    <gmd:CI_RoleCode codeList="./resources/codeList.xml#CI_RoleCode"
                                                     codeListValue="author">author
                                    </gmd:CI_RoleCode>
                                </gmd:role>

                                <gmd:organisationName>
                                    <gco:CharacterString>
                                        <xsl:value-of select="$authorOrg/Name"/>
                                    </gco:CharacterString>
                                </gmd:organisationName>

                                <gmd:individualName>
                                    <gco:CharacterString>
                                        <xsl:value-of select="FirstName"/>,
                                        <xsl:value-of select="LastName"/>
                                    </gco:CharacterString>
                                </gmd:individualName>
                            </gmd:CI_ResponsibleParty>
                        </gmd:pointOfContact>
                    </xsl:for-each>

                    <!--<mri:spatialRepresentationType>
                      <mcc:MD_SpatialRepresentationTypeCode codeList="" codeListValue=""/>
                    </mri:spatialRepresentationType>
                    <mri:spatialResolution>
                      <mri:MD_Resolution>
                        <mri:equivalentScale>
                          <mri:MD_RepresentativeFraction>
                            <mri:denominator>
                              <gco:Integer/>
                            </mri:denominator>
                          </mri:MD_RepresentativeFraction>
                        </mri:equivalentScale>
                      </mri:MD_Resolution>
                    </mri:spatialResolution>-->

                    <gmd:topicCategory>
                        <gmd:MD_TopicCategoryCode></gmd:MD_TopicCategoryCode>
                    </gmd:topicCategory>

                    <!--<mri:extent>
                      <gex:EX_Extent>
                        <gex:temporalElement>
                          <gex:EX_TemporalExtent>
                            <gex:extent>
                              <gml:TimePeriod gml:id="A1234">
                                <gml:beginPosition>
                                </gml:beginPosition>
                                <gml:endPosition>
                                </gml:endPosition>
                              </gml:TimePeriod>
                            </gex:extent>
                          </gex:EX_TemporalExtent>
                        </gex:temporalElement>
                      </gex:EX_Extent>
                    </mri:extent>-->
                    <!--
                    geographic_area: {
                      type: "Polygon",
                      coordinates: [
                      [
                      [
                      2.3642042812,
                      48.816398324

                    <mri:extent>
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

                    <!--                    <mri:resourceMaintenance>-->
                    <!--                        <mmi:MD_MaintenanceInformation>-->
                    <!--                            <mmi:maintenanceDate>-->
                    <!--                                <cit:CI_Date>-->
                    <!--                                    <cit:date>-->
                    <!--                                        <gco:DateTime><xsl:value-of select="date_update" /></gco:DateTime>-->
                    <!--                                    </cit:date>-->
                    <!--                                    <cit:dateType>-->
                    <!--                                        <cit:CI_DateTypeCode codeList="codeListLocation#CI_DateTypeCode" codeListValue="update">update</cit:CI_DateTypeCode>-->
                    <!--                                    </cit:dateType>-->
                    <!--                                </cit:CI_Date>-->
                    <!--                            </mmi:maintenanceDate>-->
                    <!--                        </mmi:MD_MaintenanceInformation>-->
                    <!--                    </mri:resourceMaintenance>-->

                    <gmd:descriptiveKeywords>
                        <gmd:MD_Keywords>
                            <xsl:for-each select="thematique">
                                <gmd:keyword>
                                    <gco:CharacterString>
                                        <xsl:value-of select="."/>
                                    </gco:CharacterString>
                                </gmd:keyword>
                            </xsl:for-each>
                            <gmd:type>
                                <gmd:MD_KeywordTypeCode codeListValue="theme"
                                                        codeList="./resources/codeList.xml#MD_KeywordTypeCode"/>
                            </gmd:type>
                        </gmd:MD_Keywords>
                    </gmd:descriptiveKeywords>


                    <gmd:resourceConstraints>
                        <gmd:MD_LegalConstraints>
                            <gmd:useLimitation>
                                <gco:CharacterString>
                                    <xsl:value-of select="licence_creative_commons"/>
                                </gco:CharacterString>
                            </gmd:useLimitation>
                            <gmd:accessConstraints>
                                <gmd:MD_RestrictionCode codeListValue="otherRestrictions"
                                                        codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_RestrictionCode"/>
                            </gmd:accessConstraints>
                            <gmd:useConstraints>
                                <gmd:MD_RestrictionCode codeListValue="otherRestrictions"
                                                        codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_RestrictionCode"/>
                            </gmd:useConstraints>
                            <gmd:otherConstraints>
                                <gco:CharacterString>
                                    <xsl:value-of select="disclaimer"/>
                                </gco:CharacterString>
                            </gmd:otherConstraints>
                        </gmd:MD_LegalConstraints>
                    </gmd:resourceConstraints>

                    <gmd:graphicOverview>
                        <gmd:MD_BrowseGraphic>
                            <gmd:fileName>
                                <gco:CharacterString>
                                    <xsl:value-of select="illustration_image_URL"/>
                                </gco:CharacterString>
                            </gmd:fileName>
                            <xsl:if test="illustration_image_caption != ''">
                                <gmd:fileDescription>
                                    <gco:CharacterString>
                                        <xsl:value-of select="illustration_image_caption"/>
                                    </gco:CharacterString>
                                </gmd:fileDescription>
                            </xsl:if>
                        </gmd:MD_BrowseGraphic>
                    </gmd:graphicOverview>
                </gmd:MD_DataIdentification>
            </gmd:identificationInfo>

            <gmd:distributionInfo>
                <gmd:MD_Distribution>
                    <xsl:for-each-group select="data_files/Format" group-by=".">
                        <gmd:distributionFormat>
                            <gmd:MD_Format>
                                <gmd:name>
                                    <gco:CharacterString>
                                        <xsl:value-of select="."/>
                                    </gco:CharacterString>
                                </gmd:name>
                            </gmd:MD_Format>
                        </gmd:distributionFormat>
                    </xsl:for-each-group>

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
                                                WWW:DOWNLOAD
                                            </gco:CharacterString>
                                        </gmd:protocol>
                                        <gmd:name>
                                            <gco:CharacterString>
                                                <xsl:value-of select="traitement"/>
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
                                            DOI
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
                    <gmd:statement>
                        <gco:CharacterString/>
                    </gmd:statement>
                </gmd:LI_Lineage>
            </gmd:lineage>
        </gmd:MD_Metadata>
    </xsl:template>

</xsl:stylesheet>
