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
                exclude-result-prefixes="#all">

    <xsl:output method="xml" indent="yes"/>

    <xsl:strip-space elements="*"/>

    <xsl:template match="/record">

      <mdb:MD_Metadata xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                       xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                       xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                       xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                       xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                       xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                       xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                       xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0"
                       xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                       xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                       xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                       xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                       xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                       xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                       xmlns:gfc="http://standards.iso.org/iso/19110/gfc/1.1"
                       xmlns:gml="http://www.opengis.net/gml/3.2">
        <mdb:metadataIdentifier>
          <mcc:MD_Identifier>
            <mcc:code>
              <gco:CharacterString>
                <xsl:value-of select="datasetid"/>
              </gco:CharacterString>
            </mcc:code>
          </mcc:MD_Identifier>
        </mdb:metadataIdentifier>
        <mdb:defaultLocale>
          <lan:PT_Locale>
            <lan:language>
              <lan:LanguageCode codeList="codeListLocation#LanguageCode" codeListValue="{metas/language}"/>
            </lan:language>
            <lan:characterEncoding>
              <lan:MD_CharacterSetCode codeList="codeListLocation#MD_CharacterSetCode"
                                       codeListValue=""/>
            </lan:characterEncoding>
          </lan:PT_Locale>
        </mdb:defaultLocale>
        <mdb:metadataScope>
          <mdb:MD_MetadataScope>
            <mdb:resourceScope>
              <mcc:MD_ScopeCode codeList="" codeListValue="{@type}"/>
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
                    <xsl:value-of select="metas/publisher"/>
                  </gco:CharacterString>
                </cit:name>
                <cit:contactInfo>
                  <cit:CI_Contact>
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
        </mdb:contact>

        <mdb:dateInfo>
          <cit:CI_Date>
            <cit:date>
              <gco:DateTime><xsl:value-of select="metas/modified"/></gco:DateTime>
            </cit:date>
            <cit:dateType>
              <cit:CI_DateTypeCode codeList="codeListLocation#CI_DateTypeCode" codeListValue="creation"/>
            </cit:dateType>
          </cit:CI_Date>
        </mdb:dateInfo>
        <mdb:dateInfo>
          <cit:CI_Date>
            <cit:date>
              <gco:DateTime><xsl:value-of select="metas/modified"/></gco:DateTime>
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
        <!--<mdb:referenceSystemInfo>
          <mrs:MD_ReferenceSystem>
            <mrs:referenceSystemIdentifier>
              <mcc:MD_Identifier>
                <mcc:code>
                  <gco:CharacterString>WGS 1984</gco:CharacterString>
                </mcc:code>
              </mcc:MD_Identifier>
            </mrs:referenceSystemIdentifier>
          </mrs:MD_ReferenceSystem>
        </mdb:referenceSystemInfo>-->
        <mdb:identificationInfo>
          <mri:MD_DataIdentification>
            <mri:citation>
              <cit:CI_Citation>
                <cit:title>
                  <gco:CharacterString>
                    <xsl:value-of select="metas/title"/>
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
                <xsl:value-of select="metas/description"/>
              </gco:CharacterString>
            </mri:abstract>
            <!-- TODO: Check state definition-->
            <!--<mri:status>
              <mcc:MD_ProgressCode codeList="codeListLocation#MD_ProgressCode" codeListValue="{state}"/>
            </mri:status>-->

            <xsl:for-each select="organization">
              <mri:pointOfContact>
                <cit:CI_Responsibility>
                  <cit:role>
                    <cit:CI_RoleCode codeList="codeListLocation#CI_RoleCode" codeListValue="originator">originator</cit:CI_RoleCode>
                  </cit:role>
                  <cit:party>
                    <cit:CI_Organisation>
                      <cit:name>
                        <gco:CharacterString>
                          <xsl:value-of select="interop_metas/dcat/creator"/>
                        </gco:CharacterString>
                      </cit:name>
                      <cit:contactInfo>
                        <cit:CI_Contact>
                          <cit:address>
                            <cit:CI_Address>
                              <cit:electronicMailAddress>
                                <gco:CharacterString>
                                  <xsl:value-of select="interop_metas/dcat/contact_email"/>
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
            <mri:topicCategory>
              <mri:MD_TopicCategoryCode></mri:MD_TopicCategoryCode>
            </mri:topicCategory>
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


            <!--<mri:resourceMaintenance>
              <mmi:MD_MaintenanceInformation>
                <mmi:maintenanceAndUpdateFrequency>
                  <mmi:MD_MaintenanceFrequencyCode codeListValue="asNeeded"
                                                   codeList="./resources/codeList.xml#MD_MaintenanceFrequencyCode"/>
                </mmi:maintenanceAndUpdateFrequency>
              </mmi:MD_MaintenanceInformation>
            </mri:resourceMaintenance>-->

            <!--
            keyword: [
              "CASVP",
              "Famille",
              "handicap",
              "Enfant handicapé",
              "Aide Sociale",
              "Aide famille"
              ],....-->
            <mri:descriptiveKeywords>
              <mri:MD_Keywords>
                <xsl:for-each select="metas/keyword|metas/theme">
                  <mri:keyword>
                    <gco:CharacterString>
                      <xsl:value-of select="name"/>
                    </gco:CharacterString>
                  </mri:keyword>
                </xsl:for-each>
                <mri:type>
                  <mri:MD_KeywordTypeCode codeListValue="theme"
                                          codeList="./resources/codeList.xml#MD_KeywordTypeCode"/>
                </mri:type>
              </mri:MD_Keywords>
            </mri:descriptiveKeywords>

            <!--
            license_url: "http://opendatacommons.org/licenses/odbl/",
            -->
            <mri:resourceConstraints>
              <mco:MD_LegalConstraints>
                <mco:reference>
                  <cit:CI_Citation>
                    <cit:title>
                      <gco:CharacterString>
                        <xsl:value-of select="metas/license"/>
                      </gco:CharacterString>
                    </cit:title>
                    <cit:onlineResource>
                      <cit:CI_OnlineResource>
                        <cit:linkage>
                          <gco:CharacterString>
                            <xsl:value-of select="metas/license_url"/>
                          </gco:CharacterString>
                        </cit:linkage>
                      </cit:CI_OnlineResource>
                    </cit:onlineResource>
                  </cit:CI_Citation>
                </mco:reference>
                <mco:accessConstraints>
                  <mco:MD_RestrictionCode codeListValue="otherRestrictions"
                                          codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_RestrictionCode"/>
                </mco:accessConstraints>
                <mco:useConstraints>
                  <mco:MD_RestrictionCode codeListValue="otherRestrictions"
                                          codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_RestrictionCode"/>
                </mco:useConstraints>
                <mco:otherConstraints>
                  <gco:CharacterString>
                    <xsl:value-of select="metas/license"/>
                  </gco:CharacterString>
                </mco:otherConstraints>
              </mco:MD_LegalConstraints>
            </mri:resourceConstraints>


            <mri:defaultLocale>
              <lan:PT_Locale>
                <lan:language>
                  <lan:LanguageCode codeList="codeListLocation#LanguageCode" codeListValue="{metas/language}"/>
                </lan:language>
                <lan:characterEncoding>
                  <lan:MD_CharacterSetCode codeList="codeListLocation#MD_CharacterSetCode"
                                           codeListValue="utf8"/>
                </lan:characterEncoding>
              </lan:PT_Locale>
            </mri:defaultLocale>
          </mri:MD_DataIdentification>
        </mdb:identificationInfo>


        <!--
        fields: [
          {
          label: "N_SQ_FIL",
          type: "double",
          description: "Numéro unique du filet de hauteur",
          name: "n_sq_fil"
          },
        -->
        <xsl:if test="count(fields) > 0">
          <mdb:contentInfo>
            <mrc:MD_FeatureCatalogue>
              <mrc:featureCatalogue>
                <gfc:FC_FeatureCatalogue>
                  <gfc:producer></gfc:producer>
                  <gfc:featureType>
                    <gfc:FC_FeatureType>
                      <gfc:typeName><xsl:value-of select="metas/title"/></gfc:typeName>
                      <gfc:isAbstract>
                        <gco:Boolean>false</gco:Boolean>
                      </gfc:isAbstract>
                      <xsl:for-each select="fields">
                        <gfc:carrierOfCharacteristics>
                          <gfc:FC_FeatureAttribute>
                            <gfc:memberName>
                              <xsl:value-of select="name"/>
                            </gfc:memberName>
                            <gfc:definition>
                              <gco:CharacterString>
                                <xsl:value-of select="label"/>:
                                <xsl:value-of select="description"/>
                              </gco:CharacterString>
                            </gfc:definition>
                            <gfc:cardinality></gfc:cardinality>
                            <gfc:valueType>
                              <gco:TypeName>
                                <gco:aName>
                                  <gco:CharacterString><xsl:value-of select="type"/></gco:CharacterString>
                                </gco:aName>
                              </gco:TypeName>
                            </gfc:valueType>
                          </gfc:FC_FeatureAttribute>
                        </gfc:carrierOfCharacteristics>
                      </xsl:for-each>
                      <gfc:featureCatalogue/>
                    </gfc:FC_FeatureType>
                  </gfc:featureType>
                </gfc:FC_FeatureCatalogue>
              </mrc:featureCatalogue>
            </mrc:MD_FeatureCatalogue>
          </mdb:contentInfo>
        </xsl:if>

        <!--
        attachments: [
        {
        mimetype: "application/pdf",
        url: "odsfile://plu_filets_hauteur0.pdf",
        id: "plu_filets_hauteur_pdf",
        title: "PLU_FILETS_HAUTEUR.pdf"
        }
        ],
-->
        <mdb:distributionInfo>
          <mrd:MD_Distribution>
            <xsl:for-each-group select="attachments/mimetype" group-by=".">
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
              <mrd:MD_DigitalTransferOptions>
                <xsl:for-each select="attachments">
                  <mrd:onLine>
                    <cit:CI_OnlineResource>
                      <cit:linkage>
                        <gco:CharacterString>
                          <xsl:value-of select="url"/>
                        </gco:CharacterString>
                      </cit:linkage>
                      <cit:protocol>
                        <gco:CharacterString>
                          <xsl:value-of select="mimetype"/>
                        </gco:CharacterString>
                      </cit:protocol>
                      <cit:name>
                        <gco:CharacterString>
                          <xsl:value-of select="id"/>
                        </gco:CharacterString>
                      </cit:name>
                      <cit:description>
                        <gco:CharacterString>
                          <xsl:value-of select="title"/>
                        </gco:CharacterString>
                      </cit:description>
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
</xsl:stylesheet>
