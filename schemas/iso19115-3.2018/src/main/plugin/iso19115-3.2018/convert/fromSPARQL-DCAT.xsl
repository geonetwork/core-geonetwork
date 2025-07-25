<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:sr="http://www.w3.org/2005/sparql-results#"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gn-fn-sparql="http://geonetwork-opensource.org/xsl/functions/sparql"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:import href="ISO19139/utility/create19115-3Namespaces.xsl"/>
  <xsl:import href="common/functions-sparql.xsl"/>

  <xsl:output method="xml" indent="yes" encoding="UTF-8"/>

  <xsl:param name="uuid" as="xs:string?"/>

  <xsl:variable name="root"
                select="/sr:sparql/sr:results"/>

  <xsl:template match="/">
    <xsl:variable name="catalogURIs"
                  select="gn-fn-sparql:getSubject($root,
                    'http://www.w3.org/1999/02/22-rdf-syntax-ns#type',
                    'http://www.w3.org/ns/dcat#Catalog')"/>

    <xsl:variable name="recordURIs"
                  select="gn-fn-sparql:getSubject($root,
                    'http://www.w3.org/1999/02/22-rdf-syntax-ns#type',
                    'http://www.w3.org/ns/dcat#CatalogRecord')"/>

    <xsl:variable name="datasetURIs"
                  select="gn-fn-sparql:getSubject($root,
                    'http://www.w3.org/1999/02/22-rdf-syntax-ns#type',
                    'http://www.w3.org/ns/dcat#Dataset')"/>

    <xsl:variable name="serviceURIs"
                  select="gn-fn-sparql:getSubject($root,
                    'http://www.w3.org/1999/02/22-rdf-syntax-ns#type',
                    'http://www.w3.org/ns/dcat#DataService')"/>

    <xsl:for-each select="$recordURIs">

      <!-- We expect only one CatalogRecord containing on Dataset or DataService -->
      <xsl:variable name="resource"
                    select="($datasetURIs|$serviceURIs)"/>

      <xsl:variable name="isService"
                    select="ends-with($resource/binding[@name = 'object']/uri, 'DataService')"
                    as="xs:boolean"/>

      <xsl:variable name="recordUri"
                    select="sr:uri"/>


      <mdb:MD_Metadata>
        <xsl:call-template name="add-iso19115-3.2018-namespaces"/>


        <xsl:variable name="uuid"
                      select="if ($uuid != '') then $uuid
                              else gn-fn-sparql:getObject($root,
                                'http://purl.org/dc/terms/identifier',
                                $recordUri)/sr:literal"/>

        <mdb:metadataIdentifier>
          <mcc:MD_Identifier>
            <mcc:code>
              <gco:CharacterString>
                <xsl:value-of select="$uuid"/>
              </gco:CharacterString>
            </mcc:code>
          </mcc:MD_Identifier>
        </mdb:metadataIdentifier>

        <xsl:variable name="languages"
                      select="gn-fn-sparql:getObject($root,
                                'http://purl.org/dc/terms/language',
                                $recordUri)/sr:uri"/>
        <xsl:for-each select="$languages">
          <xsl:call-template name="build-language">
            <xsl:with-param name="element" select="'mdb:defaultLocale'"/>
            <xsl:with-param name="languageUri" select="."/>
          </xsl:call-template>
        </xsl:for-each>

        <mdb:metadataScope>
          <mdb:MD_MetadataScope>
            <mdb:resourceScope>
              <mcc:MD_ScopeCode codeList=""
                                codeListValue="{if ($isService)
                                                then 'service'
                                                else 'dataset'}"/>
            </mdb:resourceScope>
          </mdb:MD_MetadataScope>
        </mdb:metadataScope>

        <mdb:contact>
          <cit:CI_Responsibility>
            <cit:role>
              <cit:CI_RoleCode codeList="codeListLocation#CI_RoleCode" codeListValue="pointOfContact">pointOfContact
              </cit:CI_RoleCode>
            </cit:role>
            <cit:party>
              <cit:CI_Organisation>
                <cit:name>
                  <gco:CharacterString/>
                </cit:name>
                <cit:contactInfo>
                  <cit:CI_Contact>
                    <cit:address>
                      <cit:CI_Address>
                        <cit:electronicMailAddress>
                          <gco:CharacterString/>
                        </cit:electronicMailAddress>
                      </cit:CI_Address>
                    </cit:address>
                  </cit:CI_Contact>
                </cit:contactInfo>
                <cit:individual>
                  <cit:CI_Individual>
                    <cit:name>
                      <gco:CharacterString/>
                    </cit:name>
                    <cit:positionName>
                      <gco:CharacterString/>
                    </cit:positionName>
                  </cit:CI_Individual>
                </cit:individual>
              </cit:CI_Organisation>
            </cit:party>
          </cit:CI_Responsibility>
        </mdb:contact>

        <xsl:variable name="dateTypes" as="node()*">
          <type dcatType="created" isoType="creation"/>
          <type dcatType="modified" isoType="revision"/>
          <type dcatType="issued" isoType="publication"/>
        </xsl:variable>
        <xsl:for-each select="$dateTypes">
          <xsl:variable name="dates"
                        select="gn-fn-sparql:getObject($root,
                                                  concat('http://purl.org/dc/terms/', @dcatType),
                                                  $recordUri)/sr:literal"/>
          <xsl:variable name="isoType" select="@isoType"/>
          <xsl:for-each select="$dates">
            <xsl:call-template name="build-date">
              <xsl:with-param name="element" select="'mdb:dateInfo'"/>
              <xsl:with-param name="date" select="."/>
              <xsl:with-param name="dateType" select="$isoType"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:for-each>


        <mdb:metadataStandard>
          <cit:CI_Citation>
            <cit:title>
              <gco:CharacterString>ISO 19115-3</gco:CharacterString>
            </cit:title>
          </cit:CI_Citation>
        </mdb:metadataStandard>


        <mdb:metadataLinkage>
          <cit:CI_OnlineResource>
            <cit:linkage>
              <gco:CharacterString>
                <xsl:value-of select="$recordUri"/>
              </gco:CharacterString>
            </cit:linkage>
            <cit:function>
              <cit:CI_OnLineFunctionCode
                codeList="http://standards.iso.org/iso/19139/resources/codelist/gmxCodelists.xml#CI_OnLineFunctionCode"
                codeListValue="completeMetadata"/>
            </cit:function>
          </cit:CI_OnlineResource>
        </mdb:metadataLinkage>


        <xsl:for-each select="$resource">
          <xsl:variable name="resourceUri"
                        select="sr:uri"/>

          <mdb:identificationInfo>
            <xsl:choose>
              <xsl:when test="$isService"></xsl:when>
              <xsl:otherwise>
                <mri:MD_DataIdentification>
                  <mri:citation>
                    <cit:CI_Citation>
                      <cit:title>
                        <gco:CharacterString>
                          <xsl:value-of select="gn-fn-sparql:getObject($root,
                                                  'http://purl.org/dc/terms/title',
                                                  $resourceUri)/sr:literal"/>
                        </gco:CharacterString>
                      </cit:title>

                      <xsl:for-each select="$dateTypes">
                        <xsl:variable name="dates"
                                      select="gn-fn-sparql:getObject($root,
                                                  concat('http://purl.org/dc/terms/', @dcatType),
                                                  $resourceUri)/sr:literal"/>
                        <xsl:variable name="isoType" select="@isoType"/>
                        <xsl:for-each select="$dates">
                          <xsl:call-template name="build-date">
                            <xsl:with-param name="element" select="'cit:date'"/>
                            <xsl:with-param name="date" select="."/>
                            <xsl:with-param name="dateType" select="$isoType"/>
                          </xsl:call-template>
                        </xsl:for-each>
                      </xsl:for-each>


                      <xsl:variable name="identifier"
                                    select="gn-fn-sparql:getObject($root,
                                                  'http://purl.org/dc/terms/identifier',
                                                  $resourceUri)/sr:literal"/>
                      <xsl:if test="$identifier != ''">
                        <cit:identifier>
                          <mcc:MD_Identifier>
                            <mcc:code>
                              <gco:CharacterString>
                                <xsl:value-of select="$identifier"/>
                              </gco:CharacterString>
                            </mcc:code>
                          </mcc:MD_Identifier>
                        </cit:identifier>
                      </xsl:if>
                    </cit:CI_Citation>
                  </mri:citation>
                  <mri:abstract>
                    <gco:CharacterString>
                      <xsl:value-of select="gn-fn-sparql:getObject($root,
                                                  'http://purl.org/dc/terms/description',
                                                  $resourceUri)/sr:literal"/>
                    </gco:CharacterString>
                  </mri:abstract>

                  <xsl:for-each select="gn-fn-sparql:getObject($root,
                                                  'http://www.w3.org/ns/dcat#contactPoint',
                                                  $resourceUri)/sr:bnode">
                    <xsl:call-template name="build-contact">
                      <xsl:with-param name="contactUri" select="."/>
                    </xsl:call-template>
                  </xsl:for-each>

                  <!--
                  <dct:creator rdf:nodeID="autos1"/>
                  -->
                  <xsl:for-each select="gn-fn-sparql:getObject($root,
                                                  'http://purl.org/dc/terms/creator',
                                                  $resourceUri)/sr:bnode">
                    <xsl:call-template name="build-contact">
                      <xsl:with-param name="contactUri" select="."/>
                      <xsl:with-param name="contactRole" select="'creator'"/>
                    </xsl:call-template>
                  </xsl:for-each>

                  <!--
                      <dct:publisher rdf:resource="http://publications.europa.eu/resource/authority/corporate-body/JRC"/>
                  -->
                  <xsl:for-each select="gn-fn-sparql:getObject($root,
                                                  'http://purl.org/dc/terms/publisher',
                                                  $resourceUri)/sr:uri">
                    <xsl:call-template name="build-contact">
                      <xsl:with-param name="contactUri" select="."/>
                      <xsl:with-param name="contactRole" select="'publisher'"/>
                    </xsl:call-template>
                  </xsl:for-each>


                  <!--
                   <dc:creator>Pigaieire, Sergitista, Filipe</dc:creator>
                  -->
                  <xsl:for-each select="gn-fn-sparql:getObject($root,
                                                  'http://purl.org/dc/elements/1.1/creator',
                                                  $resourceUri)/sr:literal[. != '']">
                    <xsl:call-template name="build-contact">
                      <xsl:with-param name="contactName" select="."/>
                      <xsl:with-param name="contactRole" select="'creator'"/>
                    </xsl:call-template>
                  </xsl:for-each>


                  <!--
                  <dcat:spatialResolutionInMeters>25000</dcat:spatialResolutionInMeters>
                  -->
                  <xsl:for-each select="gn-fn-sparql:getObject($root,
                                                  'http://www.w3.org/ns/dcat#spatialResolutionInMeters',
                                                  $resourceUri)/sr:literal">
                    <mri:spatialResolution>
                      <mri:MD_Resolution>
                        <mri:equivalentScale>
                          <mri:MD_RepresentativeFraction>
                            <mri:denominator>
                              <gco:Integer>
                                <xsl:value-of select="."/>
                              </gco:Integer>
                            </mri:denominator>
                          </mri:MD_RepresentativeFraction>
                        </mri:equivalentScale>
                      </mri:MD_Resolution>
                    </mri:spatialResolution>
                  </xsl:for-each>

                  <!--
                  <mri:topicCategory>
                    <mri:MD_TopicCategoryCode>inlandWaters</mri:MD_TopicCategoryCode>
                  </mri:topicCategory>
                  -->

                  <!--
                    <dct:spatial>
                        <dct:Location>
                            <locn:geometry>{"coordinates":[[[6.755991,45.788744],[10.541824,45.788744],[10.541824,47.517566],[6.755991,47.517566],[6.755991,45.788744]]],"type":"Polygon"}</locn:geometry>
                        </dct:Location>
                    </dct:spatial>
                   -->
                  <xsl:for-each select="gn-fn-sparql:getObject($root,
                                                  'http://purl.org/dc/terms/spatial',
                                                  $resourceUri)/sr:bnode[. != '']">
                    <xsl:for-each select="gn-fn-sparql:getObject($root,
                                                  'http://www.w3.org/ns/locn#geometry',
                                                  .)/sr:literal">
                      <xsl:variable name="coordByPipe"
                                    select="util:geoJsonGeomToBbox(string(.))"/>
                      <xsl:if test="$coordByPipe != ''">
                        <xsl:variable name="coords"
                                      select="tokenize($coordByPipe, '\|')"/>
                        <mri:extent>
                          <gex:EX_Extent>
                            <gex:geographicElement>
                              <gex:EX_GeographicBoundingBox>
                                <gex:westBoundLongitude>
                                  <gco:Decimal>
                                    <xsl:value-of select="$coords[1]"/>
                                  </gco:Decimal>
                                </gex:westBoundLongitude>
                                <gex:eastBoundLongitude>
                                  <gco:Decimal>
                                    <xsl:value-of select="$coords[3]"/>
                                  </gco:Decimal>
                                </gex:eastBoundLongitude>
                                <gex:southBoundLatitude>
                                  <gco:Decimal>
                                    <xsl:value-of select="$coords[2]"/>
                                  </gco:Decimal>
                                </gex:southBoundLatitude>
                                <gex:northBoundLatitude>
                                  <gco:Decimal>
                                    <xsl:value-of select="$coords[4]"/>
                                  </gco:Decimal>
                                </gex:northBoundLatitude>
                              </gex:EX_GeographicBoundingBox>
                            </gex:geographicElement>
                          </gex:EX_Extent>
                        </mri:extent>
                      </xsl:if>
                    </xsl:for-each>
                  </xsl:for-each>

                  <!--
                  <dct:PeriodOfTime rdf:nodeID="autos4">
                    <schema:endDate rdf:datatype="http://www.w3.org/2001/XMLSchema#date">2021-12-31</schema:endDate>
                    <schema:startDate rdf:datatype="http://www.w3.org/2001/XMLSchema#date">2021-01-01</schema:startDate>
                  </dct:PeriodOfTime>
                  -->
                  <xsl:for-each select="gn-fn-sparql:getObject($root,
                                                  'http://purl.org/dc/terms/temporal',
                                                  $resourceUri)/sr:bnode[. != '']">
                    <xsl:variable name="periodOfTimeUri" select="."/>

                    <xsl:variable name="startDate"
                                  select="gn-fn-sparql:getObject($root,
                                                  'http://schema.org/startDate',
                                                  $periodOfTimeUri)"/>
                    <xsl:variable name="endDate"
                                  select="gn-fn-sparql:getObject($root,
                                                  'http://schema.org/endDate',
                                                  $periodOfTimeUri)/sr:literal"/>

                    <mri:extent>
                      <gex:EX_Extent>
                        <gex:temporalElement>
                          <gex:EX_TemporalExtent>
                            <gex:extent>
                              <gml:TimePeriod gml:id="{generate-id()}">
                                <gml:beginPosition>
                                  <xsl:value-of select="$startDate"/>
                                </gml:beginPosition>
                                <gml:endPosition>
                                  <xsl:value-of select="$endDate"/>
                                </gml:endPosition>
                              </gml:TimePeriod>
                            </gex:extent>
                          </gex:EX_TemporalExtent>
                        </gex:temporalElement>
                      </gex:EX_Extent>
                    </mri:extent>
                  </xsl:for-each>


                  <!--
                         <dct:spatial rdf:resource="http://publications.europa.eu/resource/authority/country/EUR"/>
                  -->
                  <xsl:for-each select="gn-fn-sparql:getObject($root,
                                                      'http://purl.org/dc/terms/spatial',
                                                      $resourceUri)/sr:uri">
                    <mri:extent>
                      <gex:EX_Extent>
                        <gex:geographicElement>
                          <gex:EX_GeographicDescription>
                            <gex:geographicIdentifier>
                              <mcc:MD_Identifier>
                                <mcc:code>
                                  <gcx:Anchor xlink:href="{current()}">
                                    <xsl:value-of select="current()"/>
                                  </gcx:Anchor>
                                </mcc:code>
                              </mcc:MD_Identifier>
                            </gex:geographicIdentifier>
                          </gex:EX_GeographicDescription>
                        </gex:geographicElement>
                      </gex:EX_Extent>
                    </mri:extent>
                  </xsl:for-each>


                  <xsl:for-each select="gn-fn-sparql:getObject($root,
                                                  'http://purl.org/dc/terms/accrualPeriodicity',
                                                  $resourceUri)/sr:uri">

                    <xsl:variable name="euPrefix"
                                  select="'http://publications.europa.eu/resource/authority/frequency/'"/>
                    <xsl:variable name="frequency"
                                  select="if(starts-with(., $euPrefix))
                                          then substring-after(., $euPrefix)
                                          else ."/>
                    <mri:resourceMaintenance>
                      <mmi:MD_MaintenanceInformation>
                        <mmi:maintenanceAndUpdateFrequency>
                          <mmi:MD_MaintenanceFrequencyCode codeListValue="{$frequency}"
                                                           codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_MaintenanceFrequencyCode"/>
                        </mmi:maintenanceAndUpdateFrequency>
                      </mmi:MD_MaintenanceInformation>
                    </mri:resourceMaintenance>
                  </xsl:for-each>

                  <!--
                  <mri:graphicOverview>
                    <mcc:MD_BrowseGraphic>
                       <mcc:fileName>
                          <gco:CharacterString>https://metawal.wallonie.be/geonetwork/srv/api/records/b795de68-726c-4bdf-a62a-a42686aa5b6f/attachments/picc_vdiff_1.png</gco:CharacterString>
                       </mcc:fileName>
                       <mcc:fileDescription>
                          <gco:CharacterString>picc_vdiff_1</gco:CharacterString>
                       </mcc:fileDescription>
                       <mcc:fileType>
                          <gco:CharacterString>png</gco:CharacterString>
                       </mcc:fileType>
                    </mcc:MD_BrowseGraphic>
                  </mri:graphicOverview>
                  -->

                  <!--
                    <dcat:theme>
                        <skos:Concept>
                            <skos:prefLabel>Umweltüberwachung</skos:prefLabel>
                        </skos:Concept>
                    </dcat:theme>
                  -->
                  <xsl:variable name="dcatThemes"
                                select="gn-fn-sparql:getObject($root,
                                                      ('http://www.w3.org/ns/dcat#theme'),
                                                      $resourceUri)/sr:bnode"/>
                  <xsl:if test="exists($dcatThemes)">
                    <mri:descriptiveKeywords>
                      <mri:MD_Keywords>
                        <xsl:for-each select="$dcatThemes">
                          <xsl:variable name="label"
                                        select="gn-fn-sparql:getObject($root,
                                                    'http://www.w3.org/2004/02/skos/core#prefLabel',
                                                    .)/sr:literal"/>
                          <mri:keyword>
                            <gco:CharacterString>
                              <xsl:value-of select="$label"/>
                            </gco:CharacterString>
                          </mri:keyword>
                        </xsl:for-each>
                        <mri:type>
                          <mri:MD_KeywordTypeCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_KeywordTypeCode" codeListValue="theme"/>
                        </mri:type>
                        <mri:thesaurusName>
                          <cit:CI_Citation>
                            <cit:title>
                              <gcx:Anchor xlink:href="http://publications.europa.eu/resource/authority/data-theme">Data theme</gcx:Anchor>
                            </cit:title>
                            <cit:date>
                              <cit:CI_Date>
                                <cit:date>
                                  <gco:Date>2024-10-10</gco:Date>
                                </cit:date>
                                <cit:dateType>
                                  <cit:CI_DateTypeCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_DateTypeCode" codeListValue="publication"/>
                                </cit:dateType>
                              </cit:CI_Date>
                            </cit:date>
                            <cit:identifier>
                              <mcc:MD_Identifier>
                                <mcc:code>
                                  <gcx:Anchor xlink:href="https://localhost/geonetwork/srv/api/registries/vocabularies/external.theme.data-theme-skos">geonetwork.thesaurus.external.theme.data-theme-skos</gcx:Anchor>
                                </mcc:code>
                              </mcc:MD_Identifier>
                            </cit:identifier>
                          </cit:CI_Citation>
                        </mri:thesaurusName>
                      </mri:MD_Keywords>
                    </mri:descriptiveKeywords>
                  </xsl:if>


                  <!--
                      <dcat:keyword xml:lang="en">census</dcat:keyword>
                  -->
                  <mri:descriptiveKeywords>
                    <mri:MD_Keywords>
                      <xsl:for-each select="gn-fn-sparql:getObject($root,
                                                      ('http://www.w3.org/ns/dcat#keyword'),
                                                      $resourceUri)/sr:literal">
                        <mri:keyword>
                          <gco:CharacterString>
                            <xsl:value-of select="."/>
                          </gco:CharacterString>
                        </mri:keyword>
                      </xsl:for-each>
                    </mri:MD_Keywords>
                  </mri:descriptiveKeywords>


                  <!--
                      <dct:accessRights rdf:resource="http://data.jrc.ec.europa.eu/access-rights/no-limitations"/>
                      <dct:license rdf:resource="http://publications.europa.eu/resource/authority/licence/COM_REUSE"/>
                      May be attached to the dataset or the distribution in DCAT.
                  -->
                  <xsl:for-each select="($resourceUri, gn-fn-sparql:getObject($root,
                                                  'http://www.w3.org/ns/dcat#distribution',
                                                  $resourceUri)/sr:bnode[. != ''])">
                    <xsl:for-each select="gn-fn-sparql:getObject($root,
                                                          'http://purl.org/dc/terms/accessRights',
                                                          current())/sr:uri[. != '']">

                      <xsl:variable name="accessConstraints" as="node()*">
                        <entry key="http://data.jrc.ec.europa.eu/access-rights/no-limitations">unrestricted</entry>
                        <entry
                          key="http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/noLimitations">
                          unrestricted
                        </entry>
                      </xsl:variable>

                      <mri:resourceConstraints>
                        <mco:MD_LegalConstraints>
                          <mco:accessConstraints>
                            <mco:MD_RestrictionCode
                              codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_RestrictionCode"
                              codeListValue="{($accessConstraints[@key = current()]/text(), 'licence')[1]}"/>
                          </mco:accessConstraints>
                          <mco:otherConstraints>
                            <gcx:Anchor xlink:href="{current()}">
                              <xsl:value-of select="current()"/>
                            </gcx:Anchor>
                          </mco:otherConstraints>
                        </mco:MD_LegalConstraints>
                      </mri:resourceConstraints>
                    </xsl:for-each>

                    <xsl:for-each select="gn-fn-sparql:getObject($root,
                                                          'http://purl.org/dc/terms/license',
                                                          current())/sr:uri[. != '']">
                      <mri:resourceConstraints>
                        <mco:MD_LegalConstraints>
                          <mco:useConstraints>
                            <mco:MD_RestrictionCode
                              codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_RestrictionCode"
                              codeListValue="licence'"/>
                          </mco:useConstraints>
                          <mco:otherConstraints>
                            <gcx:Anchor xlink:href="{current()}">
                              <xsl:value-of select="current()"/>
                            </gcx:Anchor>
                          </mco:otherConstraints>
                        </mco:MD_LegalConstraints>
                      </mri:resourceConstraints>
                    </xsl:for-each>
                  </xsl:for-each>


                  <!--
                      <dct:isPartOf rdf:resource="https://data.jrc.ec.europa.eu/collection/id-00433"/>

                      See mapping of relation in
                      https://github.com/geonetwork/core-geonetwork/blob/main/schemas/iso19115-3.2018/src/main/plugin/iso19115-3.2018/formatter/dcat/dcat-core-associated.xsl#L17-L28
                  -->
                  <xsl:variable name="isoAssociatedTypesToDcatCommonNames"
                                as="node()*">
                    <entry associationType="partOfSeamlessDatabase">http://purl.org/dc/terms/isPartOf</entry>
                    <entry associationType="crossReference">http://purl.org/dc/terms/references</entry>
                    <entry associationType="isComposedOf">http://purl.org/dc/terms/hasPart</entry>
                    <entry associationType="revisionOf">http://purl.org/pav/previousVersion</entry>
                  </xsl:variable>

                  <xsl:for-each select="$isoAssociatedTypesToDcatCommonNames">
                    <xsl:variable name="association" select="current()"/>
                    <xsl:for-each select="gn-fn-sparql:getObject($root,
                                                        $association/text(),
                                                        $resourceUri)/sr:uri[. != '']">
                      <mri:associatedResource>
                        <mri:MD_AssociatedResource>
                          <mri:associationType>
                            <mri:DS_AssociationTypeCode
                              codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#DS_AssociationTypeCode"
                              codeListValue="{$association/@associationType}"/>
                          </mri:associationType>
                          <mri:metadataReference xlink:href="{current()}"/>
                        </mri:MD_AssociatedResource>
                      </mri:associatedResource>
                    </xsl:for-each>
                  </xsl:for-each>


                  <xsl:variable name="resourceLanguages"
                                select="gn-fn-sparql:getObject($root,
                                    'http://purl.org/dc/terms/language',
                                    $resourceUri)/sr:uri"/>
                  <xsl:for-each select="$resourceLanguages">
                    <xsl:call-template name="build-language">
                      <xsl:with-param name="element" select="'mri:defaultLocale'"/>
                      <xsl:with-param name="languageUri" select="."/>
                    </xsl:call-template>
                  </xsl:for-each>

                </mri:MD_DataIdentification>
              </xsl:otherwise>
            </xsl:choose>
          </mdb:identificationInfo>


          <xsl:variable name="lineage"
                        select="gn-fn-sparql:getObject($root,
                                    ('http://purl.org/dc/terms/provenance', 'http://www.w3.org/2000/01/rdf-schema#label'),
                                    $resourceUri)/sr:literal"/>

          <xsl:if test="$lineage != ''">
            <mdb:resourceLineage>
              <mrl:LI_Lineage>
                <mrl:statement>
                  <gco:CharacterString>
                    <xsl:value-of select="$lineage"/>
                  </gco:CharacterString>
                </mrl:statement>
                <mrl:scope>
                  <mcc:MD_Scope>
                    <mcc:level>
                      <mcc:MD_ScopeCode
                        codeList="https://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_ScopeCode"
                        codeListValue="dataset">dataset
                      </mcc:MD_ScopeCode>
                    </mcc:level>
                  </mcc:MD_Scope>
                </mrl:scope>
              </mrl:LI_Lineage>
            </mdb:resourceLineage>
          </xsl:if>


          <xsl:variable name="distributions"
                        select="gn-fn-sparql:getObject($root,
                                                  'http://www.w3.org/ns/dcat#distribution',
                                                  $resourceUri)/sr:bnode[. != '']"/>

          <xsl:if test="$distributions">
            <mdb:distributionInfo>
              <mrd:MD_Distribution>
                <!--
                    <dct:format rdf:resource="http://publications.europa.eu/resource/authority/file-type/TIFF"/>
                -->

                <xsl:for-each select="$distributions">
                  <xsl:variable name="distributionUri"
                                select="."/>
                  <xsl:for-each select="gn-fn-sparql:getObject($root,
                                                    'http://purl.org/dc/terms/format',
                                                    $distributionUri)/sr:uri[. != '']">
                    <mrd:distributionFormat>
                      <mrd:MD_Format>
                        <mrd:formatSpecificationCitation>
                          <cit:CI_Citation>
                            <cit:title>
                              <gcx:Anchor xlink:href="{current()}">
                                <xsl:value-of select="current()"/>
                              </gcx:Anchor>
                            </cit:title>
                          </cit:CI_Citation>
                        </mrd:formatSpecificationCitation>
                      </mrd:MD_Format>
                    </mrd:distributionFormat>
                  </xsl:for-each>
                </xsl:for-each>

                <mrd:transferOptions>
                  <mrd:MD_DigitalTransferOptions>
                    <xsl:for-each select="$distributions">
                      <xsl:variable name="distributionUri"
                                    select="."/>
                      <xsl:variable name="accessUrl"
                                    select="gn-fn-sparql:getObject($root,
                                                        'http://www.w3.org/ns/dcat#accessURL',
                                                        $distributionUri)/sr:uri"/>
                      <xsl:variable name="downloadURL"
                                    select="gn-fn-sparql:getObject($root,
                                                        'http://www.w3.org/ns/dcat#downloadURL',
                                                        $distributionUri)/sr:uri"/>

                      <xsl:for-each select="($accessUrl, $downloadURL)">
                        <mrd:onLine>
                          <cit:CI_OnlineResource>
                            <cit:linkage>
                              <gco:CharacterString>
                                <xsl:value-of select="current()"/>
                              </gco:CharacterString>
                            </cit:linkage>


                            <xsl:for-each select="gn-fn-sparql:getObject($root,
                                                          'http://www.w3.org/ns/adms#representationTechnique',
                                                          $distributionUri)/sr:bnode[. != '']">
                              <xsl:for-each select="gn-fn-sparql:getObject($root,
                                                            'http://www.w3.org/2004/02/skos/core#prefLabel',
                                                            .)/sr:literal[. != '']">
                                <cit:protocol>
                                  <gco:CharacterString>
                                    <xsl:value-of select="."/>
                                  </gco:CharacterString>
                                </cit:protocol>
                              </xsl:for-each>
                            </xsl:for-each>

                            <!--
                            <dct:format rdf:resource="http://publications.europa.eu/resource/authority/file-type/TIFF"/>
                            -->
                            <xsl:for-each select="gn-fn-sparql:getObject($root,
                                                          'http://purl.org/dc/terms/format',
                                                          $distributionUri)/sr:uri">
                              <cit:protocol>
                                <gcx:Anchor xlink:href="{.}">
                                  <xsl:value-of select="."/>
                                </gcx:Anchor>
                              </cit:protocol>
                            </xsl:for-each>

                            <xsl:for-each select="gn-fn-sparql:getObject($root,
                                                          'http://purl.org/dc/terms/title',
                                                          $distributionUri)/sr:literal">
                              <cit:name>
                                <gco:CharacterString>
                                  <xsl:value-of select="."/>
                                </gco:CharacterString>
                              </cit:name>
                            </xsl:for-each>

                            <xsl:for-each select="gn-fn-sparql:getObject($root,
                                                          'http://purl.org/dc/terms/description',
                                                          $distributionUri)/sr:literal">
                              <cit:description>
                                <gco:CharacterString>
                                  <xsl:value-of select="."/>
                                </gco:CharacterString>
                              </cit:description>
                            </xsl:for-each>


                            <xsl:for-each select="gn-fn-sparql:getObject($root,
                                  'http://purl.org/dc/terms/type',
                                  $distributionUri)/sr:uri">
                              <cit:function>
                                <cit:CI_OnLineFunctionCode codeList="" codeListValue="{.}"/>
                              </cit:function>
                            </xsl:for-each>

                          </cit:CI_OnlineResource>
                        </mrd:onLine>
                      </xsl:for-each>
                    </xsl:for-each>
                  </mrd:MD_DigitalTransferOptions>
                </mrd:transferOptions>
              </mrd:MD_Distribution>
            </mdb:distributionInfo>
          </xsl:if>
        </xsl:for-each>
      </mdb:MD_Metadata>
    </xsl:for-each>
  </xsl:template>


  <xsl:template name="build-date">
    <xsl:param name="element" as="xs:string"/>
    <xsl:param name="date" as="xs:string"/>
    <xsl:param name="dateType" as="xs:string"/>

    <xsl:element name="{$element}">
      <cit:CI_Date>
        <cit:date>
          <gco:DateTime>
            <xsl:value-of select="$date"/>
          </gco:DateTime>
        </cit:date>
        <cit:dateType>
          <cit:CI_DateTypeCode
            codeList="https://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_DateTypeCode"
            codeListValue="{$dateType}"></cit:CI_DateTypeCode>
        </cit:dateType>
      </cit:CI_Date>
    </xsl:element>
  </xsl:template>


  <xsl:template name="build-language">
    <xsl:param name="element" as="xs:string"/>
    <xsl:param name="languageUri" as="xs:string"/>

    <xsl:variable name="euPrefix"
                  select="'(http://publications\.europa\.eu/resource/authority/language/|http://lexvo\.org/id/iso639-3/)([A-Za-z]+)'"/>

    <xsl:element name="{$element}">
      <lan:PT_Locale>
        <lan:language>
          <lan:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/" codeListValue="{if (matches($languageUri, $euPrefix))
                                then lower-case(replace($languageUri, $euPrefix, '$2'))
                                else $languageUri}"/>
        </lan:language>
        <lan:characterEncoding>
          <lan:MD_CharacterSetCode
            codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_CharacterSetCode"
            codeListValue="utf8"/>
        </lan:characterEncoding>
      </lan:PT_Locale>
    </xsl:element>
  </xsl:template>


  <!--
  <dcat:contactPoint>
      <vcard:Kind>
          <vcard:title>Bundesamt für Raumentwicklung</vcard:title>
          <vcard:role>pointOfContact</vcard:role>
          <vcard:hasEmail>rolf.giezendanner@are.admin.ch</vcard:hasEmail>
      </vcard:Kind>
  </dcat:contactPoint>

  <foaf:Person rdf:nodeID="autos1">
    <owl:sameAs rdf:resource="http://orcid.org/1234"/>
    <foaf:familyName xml:lang="en">Bat</foaf:familyName>
    <foaf:givenName xml:lang="en">Fie</foaf:givenName>
    <foaf:mbox rdf:resource="mailto:filipa@ec.eupa.eu"/>
    <foaf:name xml:lang="en">Batlipe</foaf:name>
  </foaf:Person>

  <foaf:Organization rdf:about="http://publications.europa.eu/resource/authority/corporate-body/JRC">
    <foaf:homepage rdf:resource="https://ec.europa.eu/info/departments/joint-research-centre"/>
    <foaf:name xml:lang="en">European Commission, Joint Research Centre</foaf:name>
  </foaf:Organization>
  -->
  <xsl:template name="build-contact">
    <xsl:param name="element" as="xs:string?" select="'mri:pointOfContact'"/>
    <xsl:param name="contactUri" as="xs:string?"/>
    <xsl:param name="contactName" as="xs:string?"/>
    <xsl:param name="organisationName" as="xs:string?"/>
    <xsl:param name="contactRole" as="xs:string?"/>

    <xsl:variable name="role"
                  select="if ($contactRole != '')
                               then $contactRole
                               else if ($contactUri != '') then gn-fn-sparql:getObject($root,
                                    'http://www.w3.org/2006/vcard/ns#role',
                                    $contactUri)/sr:literal
                                else ''"/>
    <xsl:variable name="organisationName"
                  select="if ($organisationName != '')
                               then $organisationName
                               else if ($contactUri != '') then (gn-fn-sparql:getObject($root,
                                    'http://www.w3.org/2006/vcard/ns#title',
                                    $contactUri)
                                    |gn-fn-sparql:getObject($root,
                                    'http://xmlns.com/foaf/0.1/name',
                                    $contactUri))/sr:literal[. != '']
                                else ''"/>
    <xsl:variable name="individualName"
                  select="if ($contactName != '')
                               then $contactName
                               else if ($contactUri != '') then gn-fn-sparql:getObject($root,
                                    'http://xmlns.com/foaf/0.1/name',
                                    $contactUri)/sr:literal
                                else ''"/>
    <xsl:variable name="orcId"
                  select="if ($contactUri != '') then gn-fn-sparql:getObject($root,
                                    'http://www.w3.org/2002/07/owl#sameAs',
                                    $contactUri)/sr:uri[contains(., '://orcid.org/')]
                                    else ''"/>
    <xsl:variable name="website"
                  select="if ($contactUri != '') then gn-fn-sparql:getObject($root,
                                    'http://xmlns.com/foaf/0.1/homepage',
                                    $contactUri)/sr:uri
                                    else ''"/>
    <xsl:variable name="email"
                  select="if ($contactUri != '') then (gn-fn-sparql:getObject($root,
                                    'http://www.w3.org/2006/vcard/ns#hasEmail',
                                    $contactUri)|gn-fn-sparql:getObject($root,
                                    'http://xmlns.com/foaf/0.1/mbox',
                                    $contactUri))//(sr:literal|sr:uri) else ''"/>

    <xsl:element name="{$element}">
      <cit:CI_Responsibility>
        <cit:role>
          <cit:CI_RoleCode
            codeList="https://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_RoleCode"
            codeListValue="{$role}"></cit:CI_RoleCode>
        </cit:role>
        <cit:party>
          <cit:CI_Organisation>
            <cit:name>
              <gco:CharacterString>
                <xsl:value-of select="$organisationName"/>
              </gco:CharacterString>
            </cit:name>
            <xsl:if test="$email != '' or $website != ''">
              <cit:contactInfo>
                <cit:CI_Contact>
                  <xsl:if test="$email != ''">
                    <cit:address>
                      <cit:CI_Address>
                        <cit:electronicMailAddress>
                          <gco:CharacterString>
                            <xsl:value-of select="replace($email, 'mailto:', '')"/>
                          </gco:CharacterString>
                        </cit:electronicMailAddress>
                      </cit:CI_Address>
                    </cit:address>
                  </xsl:if>
                  <xsl:if test="$website != ''">
                    <cit:onlineResource>
                      <cit:CI_OnlineResource>
                        <cit:linkage>
                          <gco:CharacterString>
                            <xsl:value-of select="$website"/>
                          </gco:CharacterString>
                        </cit:linkage>
                      </cit:CI_OnlineResource>
                    </cit:onlineResource>
                  </xsl:if>
                </cit:CI_Contact>
              </cit:contactInfo>
            </xsl:if>
            <xsl:if test="$individualName != ''">
              <cit:individual>
                <cit:CI_Individual>
                  <cit:name>
                    <gco:CharacterString>
                      <xsl:value-of select="$individualName"/>
                    </gco:CharacterString>
                  </cit:name>
                  <xsl:if test="$orcId != ''">
                    <cit:partyIdentifier>
                      <mcc:MD_Identifier>
                        <mcc:code>
                          <gco:CharacterString>
                            <xsl:value-of select="$orcId"/>
                          </gco:CharacterString>
                        </mcc:code>
                        <mcc:codeSpace>
                          <gco:CharacterString>ORCID</gco:CharacterString>
                        </mcc:codeSpace>
                      </mcc:MD_Identifier>
                    </cit:partyIdentifier>
                  </xsl:if>
                </cit:CI_Individual>
              </cit:individual>
            </xsl:if>
          </cit:CI_Organisation>
        </cit:party>
      </cit:CI_Responsibility>
    </xsl:element>
  </xsl:template>
</xsl:stylesheet>
