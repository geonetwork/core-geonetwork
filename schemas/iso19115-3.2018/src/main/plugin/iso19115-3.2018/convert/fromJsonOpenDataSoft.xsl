<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/1.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/2.0"
                xmlns:gfc="http://standards.iso.org/iso/19110/gfc/1.1"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:java-xsl-util="java:org.fao.geonet.util.XslUtil"
                exclude-result-prefixes="#all">

  <!--
  Import from OpenDataSoft (Huwise) metadata formats.

  Resources can be dataset or other type of resources.
  The conversion handle v1, v2.0 and v2.1 of the API.

  eg.
  * dataset
   https://www.pndb.fr/api/explore/v2.0/catalog/datasets (main change is datasetid renamed to dataset_id and removal of interop_metas)
   https://www.pndb.fr/api/explore/v2.1/catalog/datasets (main change is that the dataset tag is removed)
  * custom asset
  https://www.pndb.fr/api/explore/v2.1/catalog/assets/galaxy-e-plateforme-danalyses


  -->

  <xsl:import href="protocol-mapping.xsl"/>
  <xsl:import href="odstheme-mapping.xsl"/>
  <xsl:import href="ISO19139/utility/create19115-3Namespaces.xsl"/>

  <xsl:output method="xml" indent="yes"/>

  <xsl:strip-space elements="*"/>

  <xsl:variable name="isAsset"
                select="exists(/record/asset_type)" as="xs:boolean"/>

  <xsl:template match="/record">

    <!--
    v1 use basic_metas
    v2.0 use dataset/metas
    v2.1 use metas at root level
    -->
    <xsl:variable name="base"
                  select="if (basic_metas) then basic_metas
                               else if (dataset/metas) then dataset/metas
                               else if (metas) then metas
                               else ."/>

    <!--
    v1 use datasetid
    v2.x use dataset_id
    for other asset, rely on slug
    -->
    <xsl:variable name="uuid"
                  select="if ($isAsset) then slug else (datasetid|dataset/dataset_id|dataset_id)[1]"/>

    <xsl:variable name="contactName"
                  select="if (contact_name) then contact_name else $base/(publisher|default/publisher)"/>
    <xsl:variable name="contactMail"
                  select="if (contact_email) then contact_email else author_email"/>

    <mdb:MD_Metadata>
      <xsl:call-template name="add-iso19115-3.2018-namespaces"/>
      <mdb:metadataIdentifier>
        <mcc:MD_Identifier>
          <mcc:code>
            <gco:CharacterString>
              <xsl:value-of select="$uuid"/>
            </gco:CharacterString>
          </mcc:code>
        </mcc:MD_Identifier>
      </mdb:metadataIdentifier>
      <mdb:defaultLocale>
        <lan:PT_Locale>
          <lan:language>
            <lan:LanguageCode codeList="codeListLocation#LanguageCode"
                              codeListValue="{java-xsl-util:threeCharLangCode(
                                  if ($isAsset) then metadata_languages[1]
                                  else $base/(language|default/metadata_languages)[1])}"/>
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
              codeListValue="{if (@type) then @type else 'dataset'}"/>
          </mdb:resourceScope>

          <xsl:if test="$isAsset">
            <mdb:name>
              <gco:CharacterString><xsl:value-of select="asset_type"/></gco:CharacterString>
            </mdb:name>
          </xsl:if>
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
                  <xsl:value-of select="$contactName"/>
                </gco:CharacterString>
              </cit:name>
              <cit:contactInfo>
                <cit:CI_Contact>
                  <cit:address>
                    <cit:CI_Address>
                      <cit:electronicMailAddress>
                        <gco:CharacterString>
                          <xsl:value-of select="$contactMail"/>
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


      <xsl:call-template name="build-date">
        <xsl:with-param name="tag" select="'mdb:dateInfo'"/>
        <xsl:with-param name="base" select="$base"/>
      </xsl:call-template>

      <mdb:metadataStandard>
        <cit:CI_Citation>
          <cit:title>
            <gco:CharacterString>ISO 19115-3</gco:CharacterString>
          </cit:title>
        </cit:CI_Citation>
      </mdb:metadataStandard>

      <xsl:apply-templates select="$base/default/records_count"
                           mode="ods-to-iso"/>

      <mdb:identificationInfo>
        <mri:MD_DataIdentification>
          <xsl:variable name="title"
                        select="if (title) then title
                                     else $base/default/title"/>
          <xsl:variable name="description"
                        select="if (description) then description
                                     else $base/default/description"/>

          <mri:citation>
            <cit:CI_Citation>
              <cit:title>
                <gco:CharacterString>
                  <xsl:value-of select="$title"/>
                </gco:CharacterString>
              </cit:title>

              <xsl:call-template name="build-date">
                <xsl:with-param name="tag" select="'cit:date'"/>
                <xsl:with-param name="base" select="$base"/>
              </xsl:call-template>

              <xsl:apply-templates select="dataset/dataset_id|dataset_id"
                                   mode="ods-to-iso"/>
            </cit:CI_Citation>
          </mri:citation>
          <mri:abstract>
            <gco:CharacterString>
              <xsl:value-of select=" java-xsl-util:html2text($description, true())"/>
            </gco:CharacterString>
          </mri:abstract>

          <xsl:for-each select="$base/default/attributions[. != 'null']">
            <mri:credit>
              <gco:CharacterString>
                <xsl:value-of select="."/>
              </gco:CharacterString>
            </mri:credit>
          </xsl:for-each>

          <!-- TODO: Check state definition-->
          <!--<mri:status>
            <mcc:MD_ProgressCode codeList="codeListLocation#MD_ProgressCode" codeListValue="{state}"/>
          </mri:status>-->

          <!-- add publisher to resource organisation as well-->
          <xsl:if test="not(organization)">
            <mri:pointOfContact>
              <cit:CI_Responsibility>
                <cit:role>
                  <cit:CI_RoleCode codeList="codeListLocation#CI_RoleCode" codeListValue="originator"/>
                </cit:role>
                <cit:party>
                  <cit:CI_Organisation>
                    <cit:name>
                      <gco:CharacterString>
                        <xsl:value-of select="$contactName"/>
                      </gco:CharacterString>
                    </cit:name>
                    <xsl:if test="$contactMail">
                      <cit:contactInfo>
                        <cit:CI_Contact>
                          <cit:address>
                            <cit:CI_Address>
                              <cit:electronicMailAddress>
                                <gco:CharacterString>
                                  <xsl:value-of select="$contactMail"/>
                                </gco:CharacterString>
                              </cit:electronicMailAddress>
                            </cit:CI_Address>
                          </cit:address>
                        </cit:CI_Contact>
                      </cit:contactInfo>
                    </xsl:if>
                  </cit:CI_Organisation>
                </cit:party>
              </cit:CI_Responsibility>
            </mri:pointOfContact>
          </xsl:if>

          <xsl:for-each select="organization">
            <mri:pointOfContact>
              <cit:CI_Responsibility>
                <cit:role>
                  <cit:CI_RoleCode codeList="codeListLocation#CI_RoleCode" codeListValue="originator"/>
                </cit:role>
                <cit:party>
                  <cit:CI_Organisation>
                    <cit:name>
                      <gco:CharacterString>
                        <xsl:value-of select="$base/interop_metas/dcat/creator|$base/dcat/creator"/>
                      </gco:CharacterString>
                    </cit:name>
                    <cit:contactInfo>
                      <cit:CI_Contact>
                        <cit:address>
                          <cit:CI_Address>
                            <cit:electronicMailAddress>
                              <gco:CharacterString>
                                <xsl:value-of select="$base/interop_metas/dcat/contact_email|$base/dcat/contact_email"/>
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

          <xsl:apply-templates select="$base/dcat/creator"
                               mode="ods-to-iso"/>

          <xsl:variable name="odsThemes"
                        select="if ($isAsset) then themes else $base/default/theme"/>

          <xsl:if test="count($odsThemes) > 0">
            <xsl:for-each select="distinct-values($odsThemeToIsoTopic[theme = $odsThemes]/name())">
              <mri:topicCategory>
                <mri:MD_TopicCategoryCode>
                  <xsl:value-of select="."/>
                </mri:MD_TopicCategoryCode>
              </mri:topicCategory>
            </xsl:for-each>
          </xsl:if>

          <mri:extent>
            <gex:EX_Extent>
              <xsl:for-each select="$base/default/bbox">
                <gex:geographicElement>
                  <gex:EX_GeographicBoundingBox>
                    <gex:westBoundLongitude>
                      <gco:Decimal>
                        <xsl:value-of select="min(geometry/coordinates/array[position() mod 2 != 0])"/>
                      </gco:Decimal>
                    </gex:westBoundLongitude>
                    <gex:eastBoundLongitude>
                      <gco:Decimal>
                        <xsl:value-of select="max(geometry/coordinates/array[position() mod 2 != 0])"/>
                      </gco:Decimal>
                    </gex:eastBoundLongitude>
                    <gex:southBoundLatitude>
                      <gco:Decimal>
                        <xsl:value-of select="min(geometry/coordinates/array[position() mod 2 = 0])"/>
                      </gco:Decimal>
                    </gex:southBoundLatitude>
                    <gex:northBoundLatitude>
                      <gco:Decimal>
                        <xsl:value-of select="max(geometry/coordinates/array[position() mod 2 = 0])"/>
                      </gco:Decimal>
                    </gex:northBoundLatitude>
                  </gex:EX_GeographicBoundingBox>
                </gex:geographicElement>
              </xsl:for-each>

              <xsl:for-each select="$base/default/geographic_reference">
                <gex:geographicElement>
                  <gex:EX_GeographicDescription>
                    <gex:geographicIdentifier>
                      <mcc:MD_Identifier>
                        <mcc:code>
                          <gco:CharacterString>
                            <xsl:value-of select="."/>
                          </gco:CharacterString>
                        </mcc:code>
                      </mcc:MD_Identifier>
                    </gex:geographicIdentifier>
                  </gex:EX_GeographicDescription>
                </gex:geographicElement>
              </xsl:for-each>

              <xsl:apply-templates select="$base/dcat/temporal_coverage_start"
                                   mode="ods-to-iso"/>
            </gex:EX_Extent>
          </mri:extent>


          <xsl:for-each select="thumbnail">
            <mri:graphicOverview>
              <mcc:MD_BrowseGraphic>
                <mcc:fileName>
                  <gco:CharacterString><xsl:value-of select="url"/></gco:CharacterString>
                </mcc:fileName>
              </mcc:MD_BrowseGraphic>
            </mri:graphicOverview>
          </xsl:for-each>

          <xsl:apply-templates select="$base/dcat/accrualperiodicity"
                               mode="ods-to-iso"/>

          <xsl:apply-templates select="$base/dcat/temporal"
                               mode="ods-to-iso"/>

          <xsl:apply-templates select="$base/default/territory"
                               mode="ods-to-iso"/>

          <xsl:if test="count($odsThemes) > 0">
            <mri:descriptiveKeywords>
              <mri:MD_Keywords>
                <xsl:for-each select="$odsThemes">
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

          <!-- ODS keywords copied without type -->
          <xsl:variable name="keywords"
                        select="if ($isAsset) then (keywords|category) else ($base/default/keyword)"/>
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
                <mri:type>
                  <mri:MD_KeywordTypeCode codeListValue="theme"
                                          codeList="./resources/codeList.xml#MD_KeywordTypeCode"/>
                </mri:type>
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
                    <xsl:variable name="licenseUrl"
                                  select="$base/default/license_url[. != 'null']"/>
                    <xsl:choose>
                      <xsl:when test="$licenseUrl != ''">
                        <gcx:Anchor xlink:href="{$licenseUrl}">
                          <xsl:value-of select="$base/default/license"/>
                        </gcx:Anchor>
                      </xsl:when>
                      <xsl:otherwise>
                        <gco:CharacterString>
                          <xsl:value-of select="$base/default/license"/>
                        </gco:CharacterString>
                      </xsl:otherwise>
                    </xsl:choose>
                  </cit:title>
                  <cit:onlineResource>
                    <cit:CI_OnlineResource>
                      <cit:linkage>
                        <gco:CharacterString>
                          <xsl:value-of select="$base/default/license_url"/>
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
                  <xsl:value-of select="$base/default/license"/>
                </gco:CharacterString>
              </mco:otherConstraints>
            </mco:MD_LegalConstraints>
          </mri:resourceConstraints>


          <mri:defaultLocale>
            <lan:PT_Locale>
              <lan:language>
                <lan:LanguageCode codeList="codeListLocation#LanguageCode"
                                  codeListValue="{java-xsl-util:threeCharLangCode($base/default/language)}"/>
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
                      <xsl:value-of select="($base/default/title)[1]"/>
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
                              <xsl:if test="description[. != 'null']">
                                -
                                <xsl:value-of select="description"/>
                              </xsl:if>
                            </gco:CharacterString>
                          </gfc:definition>
                          <gfc:cardinality>1</gfc:cardinality>
                          <gfc:valueType>
                            <gco:TypeName>
                              <gco:aName>
                                <gco:CharacterString>
                                  <xsl:value-of select="type"/>
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

          <xsl:variable name="count"
                        select="$base/default/records_count"/>

          <xsl:if test="$count > 0">
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
                            select="$base/default/records_count"/>
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
                      <xsl:with-param name="format">shp</xsl:with-param>
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
                                          '/explore/',
                                          if ($isAsset) then 'assets' else 'dataset',
                                           '/',
                                          $uuid,
                                          if ($isAsset) then '' else '/information/')"/>
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

              <xsl:for-each select="$base/default/references[. != 'null']">
                <mrd:onLine>
                  <cit:CI_OnlineResource>
                    <cit:linkage>
                      <gco:CharacterString>
                        <xsl:value-of select="."/>
                      </gco:CharacterString>
                    </cit:linkage>
                    <cit:protocol>
                      <gco:CharacterString>
                        WWW:LINK
                      </gco:CharacterString>
                    </cit:protocol>
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
              <xsl:value-of select="$base/dcat/dataquality[. != 'null']"/>
            </gco:CharacterString>
          </mrl:statement>
          <mrl:scope>
            <mcc:MD_Scope>
              <mcc:level>
                <mcc:MD_ScopeCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_ScopeCode"
                                  codeListValue="dataset"/>
              </mcc:level>
            </mcc:MD_Scope>
          </mrl:scope>
        </mrl:LI_Lineage>
      </mdb:resourceLineage>
    </mdb:MD_Metadata>
  </xsl:template>

  <xsl:template name="dataLink">
    <xsl:param name="format"/>

    <mrd:onLine>
      <cit:CI_OnlineResource>
        <cit:linkage>
          <gco:CharacterString>
            <xsl:value-of select="concat(nodeUrl,
                                   '/api/explore/v2.1/catalog/datasets/',
                                   (datasetid|dataset/dataset_id|dataset_id)[1],
                                   '/exports/', $format, '?use_labels=true')"/>
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
          <cit:CI_OnLineFunctionCode
            codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_OnLineFunctionCode"
            codeListValue="download"/>
        </cit:function>
      </cit:CI_OnlineResource>
    </mrd:onLine>
  </xsl:template>

  <xsl:template name="dataFormat">
    <xsl:param name="format"/>
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


  <xsl:variable name="dateTagNameToIsoType" as="node()*">
    <entry key="modified">publication</entry>
    <entry key="data_processed">revision</entry>
    <entry key="created_at">creation</entry>
    <entry key="updated_at">revision</entry>
  </xsl:variable>

  <xsl:template name="build-date">
    <xsl:param name="tag"/>
    <xsl:param name="base"/>
    <xsl:for-each select="$base/default/modified[. != 'null']|
                                        $base/default/data_processed[. != 'null']|
                                        $base/created_at|
                                        $base/updated_at">

      <xsl:variable name="type"
                    select="$dateTagNameToIsoType[@key = current()/local-name()]"/>
      <xsl:element name="{$tag}">
        <cit:CI_Date>
          <cit:date>
            <gco:DateTime>
              <xsl:value-of select="."/>
            </gco:DateTime>
          </cit:date>
          <cit:dateType>
            <cit:CI_DateTypeCode codeList="codeListLocation#CI_DateTypeCode" codeListValue="{if ($type) then $type else 'publication'}"/>
          </cit:dateType>
        </cit:CI_Date>
      </xsl:element>
    </xsl:for-each>
  </xsl:template>


  <xsl:template match="dcat/accrualperiodicity"
                mode="ods-to-iso">
    <mri:resourceMaintenance>
      <mmi:MD_MaintenanceInformation>
        <mmi:maintenanceAndUpdateFrequency>
          <mmi:MD_MaintenanceFrequencyCode
            codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_MaintenanceFrequencyCode"
            codeListValue="{.}"/>
        </mmi:maintenanceAndUpdateFrequency>
      </mmi:MD_MaintenanceInformation>
    </mri:resourceMaintenance>
  </xsl:template>

  <xsl:template match="dcat/creator"
                mode="ods-to-iso">
    <mri:pointOfContact>
      <cit:CI_Responsibility>
        <cit:role>
          <cit:CI_RoleCode codeList="codeListLocation#CI_RoleCode" codeListValue="author"/>
        </cit:role>
        <cit:party>
          <cit:CI_Organisation>
            <cit:name>
              <gco:CharacterString>
                <!-- TODO: Clarify meaning of publisher/creator/contributor
                and to which contact contact_name/contact_email is attached to. -->
                <xsl:value-of select="if (. != 'null') then . else ../../default/publisher"/>
              </gco:CharacterString>
            </cit:name>
            <xsl:if test="../contact_email[. != 'null']">
              <cit:contactInfo>
                <cit:CI_Contact>
                  <cit:address>
                    <cit:CI_Address>
                      <cit:electronicMailAddress>
                        <gco:CharacterString>
                          <xsl:value-of select="../contact_email"/>
                        </gco:CharacterString>
                      </cit:electronicMailAddress>
                    </cit:CI_Address>
                  </cit:address>
                </cit:CI_Contact>
              </cit:contactInfo>
            </xsl:if>
            <xsl:if test="../contact_name[. != 'null']">
              <cit:individual>
                <cit:CI_Individual>
                  <cit:name>
                    <gco:CharacterString>
                      <xsl:value-of select="../contact_name"/>
                    </gco:CharacterString>
                  </cit:name>
                </cit:CI_Individual>
              </cit:individual>
            </xsl:if>
          </cit:CI_Organisation>
        </cit:party>
      </cit:CI_Responsibility>
    </mri:pointOfContact>
  </xsl:template>


  <!--
        "territory": [
          "Région wallonne",
          "Région de Bruxelles-Capitale"
        ],

        "metas": {
          "dcat": {...
          "accrualperiodicity": "daily",
-->
  <xsl:template match="dcat/temporal[. != 'null']
                                    |default/territory[. != 'null']"
                mode="ods-to-iso">
    <xsl:variable name="type"
                  select="if(name() = 'temporal') then 'temporal' else 'place'"/>
    <mri:descriptiveKeywords>
      <mri:MD_Keywords>
        <mri:keyword>
          <gco:CharacterString>
            <xsl:value-of select="."/>
          </gco:CharacterString>
        </mri:keyword>
        <mri:type>
          <mri:MD_KeywordTypeCode codeList="" codeListValue="{$type}"/>
        </mri:type>
      </mri:MD_Keywords>
    </mri:descriptiveKeywords>
  </xsl:template>


  <!--
        "metas": {
          "dcat": {...
            "temporal_coverage_start": "2018-12-30T23:00:00+00:00",
            "temporal_coverage_end": "2020-12-30T23:00:00+00:00",
  -->
  <xsl:template match="dcat/temporal_coverage_start[. != 'null']"
                mode="ods-to-iso">
    <gex:temporalElement>
      <gex:EX_TemporalExtent>
        <gex:extent>
          <gml:TimePeriod>
            <gml:beginPosition>
              <xsl:value-of select="."/>
            </gml:beginPosition>
            <gml:endPosition>
              <xsl:value-of select="../temporal_coverage_end[. != 'null']"/>
            </gml:endPosition>
          </gml:TimePeriod>
        </gex:extent>
      </gex:EX_TemporalExtent>
    </gex:temporalElement>
  </xsl:template>


  <xsl:template match="dataset_id"
                mode="ods-to-iso">
    <cit:identifier>
      <mcc:MD_Identifier>
        <mcc:code>
          <gco:CharacterString>
            <xsl:value-of select="."/>
          </gco:CharacterString>
        </mcc:code>
      </mcc:MD_Identifier>
    </cit:identifier>
  </xsl:template>

  <xsl:template match="default/records_count"
                mode="ods-to-iso">
    <mdb:spatialRepresentationInfo>
      <msr:MD_VectorSpatialRepresentation>
        <msr:geometricObjects>
          <msr:MD_GeometricObjects>
            <xsl:for-each select="../geometry_types[1]">
              <msr:geometricObjectType>
                <msr:MD_GeometricObjectTypeCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_GeometricObjectTypeCode"
                                                codeListValue="{.}"/>
              </msr:geometricObjectType>
            </xsl:for-each>
            <msr:geometricObjectCount>
              <gco:Integer><xsl:value-of select="."/></gco:Integer>
            </msr:geometricObjectCount>
          </msr:MD_GeometricObjects>
        </msr:geometricObjects>
      </msr:MD_VectorSpatialRepresentation>
    </mdb:spatialRepresentationInfo>
  </xsl:template>

  <xsl:template match="*" mode="ods-to-iso"/>
</xsl:stylesheet>
