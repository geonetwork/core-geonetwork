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
                <xsl:value-of select="(datasetid|dataset/dataset_id)[1]"/>
              </gco:CharacterString>
            </mcc:code>
          </mcc:MD_Identifier>
        </mdb:metadataIdentifier>
        <mdb:defaultLocale>
          <lan:PT_Locale>
            <lan:language>
              <lan:LanguageCode codeList="codeListLocation#LanguageCode"
                                codeListValue="{java-xsl-util:threeCharLangCode(
                                (metas/language|dataset/metas/default/metadata_languages)[1])}"/>
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
              <mcc:MD_ScopeCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_ScopeCode"
                                codeListValue="{if (@type) then @type else 'dataset'}"/>
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
                    <xsl:value-of select="(metas/publisher|dataset/metas/default/publisher)[1]"/>
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
              <gco:DateTime><xsl:value-of select="(metas/modified|dataset/metas/default/metadata_processed)[1]"/></gco:DateTime>
            </cit:date>
            <cit:dateType>
              <cit:CI_DateTypeCode codeList="codeListLocation#CI_DateTypeCode" codeListValue="publication"/>
            </cit:dateType>
          </cit:CI_Date>
        </mdb:dateInfo>
        <mdb:dateInfo>
          <cit:CI_Date>
            <cit:date>
              <gco:DateTime><xsl:value-of select="metas/metadata_processed"/></gco:DateTime>
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
                    <xsl:value-of select="(metas/title|dataset/metas/default/title)[1]"/>
                  </gco:CharacterString>
                </cit:title>
                <cit:date>
                  <cit:CI_Date>
                    <cit:date>
                      <gco:DateTime>
                        <xsl:value-of select="(metas/modified|dataset/metas/default/modified)[1]"/>
                      </gco:DateTime>
                    </cit:date>
                    <cit:dateType>
                      <cit:CI_DateTypeCode codeList="codeListLocation#CI_DateTypeCode" codeListValue="publication"/>
                    </cit:dateType>
                  </cit:CI_Date>
                </cit:date>
                <cit:date>
                  <cit:CI_Date>
                    <cit:date>
                      <gco:DateTime><xsl:value-of select="metas/data_processed"/></gco:DateTime>
                    </cit:date>
                    <cit:dateType>
                      <cit:CI_DateTypeCode codeList="codeListLocation#CI_DateTypeCode" codeListValue="revision"/>
                    </cit:dateType>
                  </cit:CI_Date>
                </cit:date>
              </cit:CI_Citation>
            </mri:citation>
            <mri:abstract>
              <gco:CharacterString>
                <xsl:value-of select="(metas/description|dataset/metas/default/description)[1]"/>
              </gco:CharacterString>
            </mri:abstract>
            <!-- TODO: Check state definition-->
            <!--<mri:status>
              <mcc:MD_ProgressCode codeList="codeListLocation#MD_ProgressCode" codeListValue="{state}"/>
            </mri:status>-->

            <!-- add publisher to resource organisation as well-->
            <xsl:if test="not(organization)">
              <mri:pointOfContact>
                <cit:CI_Responsibility>
                  <cit:role>
                    <cit:CI_RoleCode codeList="codeListLocation#CI_RoleCode" codeListValue="originator">publisher</cit:CI_RoleCode>
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
              </mri:pointOfContact>
            </xsl:if>
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

            <!-- ODS themes copied as topicCategory -->
            <xsl:if test="metas/theme">
                <xsl:for-each select="metas/theme">
                  <mri:topicCategory>
                    <mri:MD_TopicCategoryCode>
                      <xsl:value-of select="."/>
                    </mri:MD_TopicCategoryCode>
                  </mri:topicCategory>
                </xsl:for-each>
            </xsl:if>

            <!-- ODS keywords copied without type -->
            <xsl:variable name="keywords"
                          select="metas/keyword|dataset/metas/default/keyword"/>
            <xsl:if test="$keywords">
              <mri:descriptiveKeywords>
                <mri:MD_Keywords>
                  <xsl:for-each select="$keywords">
                    <mri:keyword>
                      <gco:CharacterString>
                        <xsl:value-of select="."/>
                      </gco:CharacterString>
                    </mri:keyword>
                  </xsl:for-each>
                </mri:MD_Keywords>
              </mri:descriptiveKeywords>
            </xsl:if>

            <!--
            license_url: "http://opendatacommons.org/licenses/odbl/",
            -->
            <mri:resourceConstraints>
              <mco:MD_LegalConstraints>
                <mco:reference>
                  <cit:CI_Citation>
                    <cit:title>
                      <gco:CharacterString>
                        <xsl:value-of select="metas/license|dataset/meta/default/license"/>
                      </gco:CharacterString>
                    </cit:title>
                    <cit:onlineResource>
                      <cit:CI_OnlineResource>
                        <cit:linkage>
                          <gco:CharacterString>
                            <xsl:value-of select="metas/license_url|dataset/meta/default/license_url"/>
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
                    <xsl:value-of select="metas/license|dataset/meta/default/license"/>
                  </gco:CharacterString>
                </mco:otherConstraints>
              </mco:MD_LegalConstraints>
            </mri:resourceConstraints>


            <mri:defaultLocale>
              <lan:PT_Locale>
                <lan:language>
                  <lan:LanguageCode codeList="codeListLocation#LanguageCode" codeListValue="{java-xsl-util:threeCharLangCode((metas/language|dataset/meta/default/language)[1])}"/>
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
        <xsl:if test="count(fields|dataset/fields) > 0">
          <mdb:contentInfo>
            <mrc:MD_FeatureCatalogue>
              <mrc:featureCatalogue>
                <gfc:FC_FeatureCatalogue>
                  <gfc:producer></gfc:producer>
                  <gfc:featureType>
                    <gfc:FC_FeatureType>
                      <gfc:typeName>
                        <xsl:value-of select="(metas/title|dataset/metas/default/title)[1]"/>
                      </gfc:typeName>
                      <gfc:isAbstract>
                        <gco:Boolean>false</gco:Boolean>
                      </gfc:isAbstract>
                      <xsl:for-each select="fields|dataset/fields">
                        <gfc:carrierOfCharacteristics>
                          <gfc:FC_FeatureAttribute>
                            <gfc:memberName>
                              <xsl:value-of select="name"/>
                            </gfc:memberName>
                            <gfc:definition>
                              <gco:CharacterString>
                                <xsl:value-of select="label"/>
                                <xsl:if test="description">
                                  - <xsl:value-of select="description"/>
                                </xsl:if>
                              </gco:CharacterString>
                            </gfc:definition>
                            <gfc:cardinality>1</gfc:cardinality>
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
            <xsl:if test="metas/records_count > 0">
              <xsl:call-template name="dataFormat">
                <xsl:with-param name="format">csv</xsl:with-param>
              </xsl:call-template>
              <xsl:call-template name="dataFormat">
                <xsl:with-param name="format">json</xsl:with-param>
              </xsl:call-template>
              <xsl:if test="count(features[. = 'geo']) > 0">
                <xsl:call-template name="dataFormat">
                  <xsl:with-param name="format">geojson</xsl:with-param>
                </xsl:call-template>
                <xsl:if test="metas/records_count &lt; 5000">
                  <xsl:call-template name="dataFormat">
                    <xsl:with-param name="format">shapefile</xsl:with-param>
                  </xsl:call-template>
                </xsl:if>
              </xsl:if>
            </xsl:if>


            <mrd:transferOptions>
              <mrd:MD_DigitalTransferOptions>
                <xsl:for-each select="attachments|dataset/attachments">
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

                <!-- Data download links are inferred from the record metadata -->
                <xsl:variable name="count"
                              select="dataset/metas/default/records_count"/>
                <xsl:if test="$count > 0">
                  <xsl:call-template name="dataLink">
                    <xsl:with-param name="format">csv</xsl:with-param>
                  </xsl:call-template>
                  <xsl:call-template name="dataLink">
                    <xsl:with-param name="format">json</xsl:with-param>
                  </xsl:call-template>
                  <xsl:if test="count(.//features[. = 'geo']) > 0">
                    <xsl:call-template name="dataLink">
                      <xsl:with-param name="format">geojson</xsl:with-param>
                    </xsl:call-template>
                    <xsl:if test="$count &lt; 5000">
                      <xsl:call-template name="dataLink">
                        <xsl:with-param name="format">shapefile</xsl:with-param>
                      </xsl:call-template>
                    </xsl:if>
                  </xsl:if>
                </xsl:if>

              </mrd:MD_DigitalTransferOptions>
            </mrd:transferOptions>
            <mrd:transferOptions>
              <mrd:MD_DigitalTransferOptions>
                <mrd:onLine>
                  <cit:CI_OnlineResource>
                    <cit:linkage>
                      <gco:CharacterString>
                        <xsl:value-of select="concat(nodeUrl,
                                          '/explore/dataset/',
                                          (datasetid|dataset/dataset_id)[1],
                                           '/information/')" />
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

    <xsl:template name="dataLink">
      <xsl:param name="format" />

      <mrd:onLine>
        <cit:CI_OnlineResource>
          <cit:linkage>
            <gco:CharacterString>
              <xsl:value-of select="concat(nodeUrl,
                                   '/explore/dataset/',
                                   (datasetid|dataset/dataset_id)[1],
                                   '/download?format=', $format,
                                   '&amp;timezone=Europe/Berlin&amp;use_labels_for_header=false')" />
            </gco:CharacterString>
          </cit:linkage>
          <cit:protocol>
            <gco:CharacterString>
              <xsl:value-of select="$format-protocol-mapping/entry[format=lower-case($format)]/protocol"/>
            </gco:CharacterString>
          </cit:protocol>
          <cit:name>
            <gco:CharacterString>
              <xsl:value-of select="$format"/>
            </gco:CharacterString>
          </cit:name>
          <cit:description>
            <gco:CharacterString>
              <xsl:value-of select="$format"/>
            </gco:CharacterString>
          </cit:description>
          <cit:function>
            <cit:CI_OnLineFunctionCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_OnLineFunctionCode"
                                       codeListValue="download"/>
          </cit:function>
        </cit:CI_OnlineResource>
      </mrd:onLine>
    </xsl:template>

    <xsl:template name="dataFormat">
      <xsl:param name="format" />
      <mrd:distributionFormat>
        <mrd:MD_Format>
          <mrd:formatSpecificationCitation>
            <cit:CI_Citation>
              <cit:title>
                <gco:CharacterString>
                  <xsl:value-of select="$format"/>
                </gco:CharacterString>
              </cit:title>
            </cit:CI_Citation>
          </mrd:formatSpecificationCitation>
        </mrd:MD_Format>
      </mrd:distributionFormat>
    </xsl:template>

</xsl:stylesheet>
