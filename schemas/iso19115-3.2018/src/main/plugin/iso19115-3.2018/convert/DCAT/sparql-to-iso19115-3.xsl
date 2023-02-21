<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:sr="http://www.w3.org/2005/sparql-results#"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:spdx="http://spdx.org/rdf/terms#"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:adms="http://www.w3.org/ns/adms#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:vcard="http://www.w3.org/2006/vcard/ns#"
                xmlns:foaf="http://xmlns.com/foaf/0.1/"
                xmlns:owl="http://www.w3.org/2002/07/owl#"
                xmlns:schema="http://schema.org/"
                xmlns:locn="http://www.w3.org/ns/locn#"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:mdcat="http://data.vlaanderen.be/ns/metadata-dcat#"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
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
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:gn-fn-sparql="http://geonetwork-opensource.org/xsl/functions/sparql"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:import href="../ISO19139/utility/create19115-3Namespaces.xsl"/>
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
              <cit:CI_RoleCode codeList="codeListLocation#CI_RoleCode" codeListValue="pointOfContact">pointOfContact</cit:CI_RoleCode>
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
              <gco:CharacterString><xsl:value-of select="$recordUri"/></gco:CharacterString>
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
                              <gco:Integer><xsl:value-of select="."/></gco:Integer>
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
                                  <gco:Decimal><xsl:value-of select="$coords[1]"/></gco:Decimal>
                                </gex:westBoundLongitude>
                                <gex:eastBoundLongitude>
                                  <gco:Decimal><xsl:value-of select="$coords[3]"/></gco:Decimal>
                                </gex:eastBoundLongitude>
                                <gex:southBoundLatitude>
                                  <gco:Decimal><xsl:value-of select="$coords[2]"/></gco:Decimal>
                                </gex:southBoundLatitude>
                                <gex:northBoundLatitude>
                                  <gco:Decimal><xsl:value-of select="$coords[4]"/></gco:Decimal>
                                </gex:northBoundLatitude>
                              </gex:EX_GeographicBoundingBox>
                            </gex:geographicElement>
                          </gex:EX_Extent>
                        </mri:extent>
                      </xsl:if>
                    </xsl:for-each>
                  </xsl:for-each>

                  <!--
                  <mri:extent>
                    <gex:EX_Extent>
                       <gex:temporalElement>
                          <gex:EX_TemporalExtent>
                             <gex:extent>
                                <gml:TimePeriod gml:id="d36068e416a1053983">
                                   <gml:beginPosition>1992-02-01</gml:beginPosition>
                                   <gml:endPosition>2023-01-14</gml:endPosition>
                                </gml:TimePeriod>
                             </gex:extent>
                          </gex:EX_TemporalExtent>
                       </gex:temporalElement>
                    </gex:EX_Extent>
                 </mri:extent>
                  -->

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
                  <mri:descriptiveKeywords>
                    <mri:MD_Keywords>
                      <xsl:for-each select="gn-fn-sparql:getObject($root,
                                                      ('http://www.w3.org/ns/dcat#theme'),
                                                      $resourceUri)/sr:bnode">
                        <xsl:variable name="label"
                                      select="gn-fn-sparql:getObject($root,
                                                  'http://www.w3.org/2004/02/skos/core#prefLabel',
                                                  .)/sr:literal"/>
                        <mri:keyword>
                          <gco:CharacterString><xsl:value-of select="$label"/></gco:CharacterString>
                        </mri:keyword>
                      </xsl:for-each>
                    </mri:MD_Keywords>
                  </mri:descriptiveKeywords>

                  <!--<mri:resourceConstraints xsi:schemaLocation="http://www.isotc211.org/2005/gmd http://schemas.opengis.net/iso/19139/20060504/gmd/gmd.xsd">
                    <mco:MD_LegalConstraints>
                      <mco:useConstraints>
                        <mco:MD_RestrictionCode codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_RestrictionCode"
                                                codeListValue="otherRestrictions"/>
                      </mco:useConstraints>
                      <mco:otherConstraints xsi:type="gmd:PT_FreeText_PropertyType">
                        <gco:CharacterString>• Le gestionnaire du jeu de données tel qu’il est défini plus haut possède les droits de propriété (y compris les droits de propriété intellectuelle) se rapportant aux fichiers. • Le gestionnaire accorde au client le droit d’utiliser les données pour son usage interne. • L’usage des données à des fins commerciales, sous quelque forme que ce soit, est formellement interdit. • Le nom du gestionnaire doit apparaître lors de chaque utilisation publique des données.</gco:CharacterString>
                      </mco:otherConstraints>
                    </mco:MD_LegalConstraints>
                  </mri:resourceConstraints>
                  <mri:resourceConstraints xsi:schemaLocation="http://www.isotc211.org/2005/srv http://schemas.opengis.net/iso/19139/20060504/srv/srv.xsd">
                    <mco:MD_LegalConstraints>
                      <mco:accessConstraints>
                        <mco:MD_RestrictionCode codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_RestrictionCode"
                                                codeListValue="otherRestrictions"/>
                      </mco:accessConstraints>
                      <mco:otherConstraints xsi:type="gmd:PT_FreeText_PropertyType">
                        <gcx:Anchor xlink:href="http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/noLimitations">Pas de restrictions concernant l'accès public</gcx:Anchor>
                      </mco:otherConstraints>
                    </mco:MD_LegalConstraints>
                  </mri:resourceConstraints>-->


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
                <mrl:statement xsi:type="lan:PT_FreeText_PropertyType">
                  <gco:CharacterString><xsl:value-of select="$lineage"/> </gco:CharacterString>
                </mrl:statement>
                <mrl:scope>
                  <mcc:MD_Scope>
                    <mcc:level>
                      <mcc:MD_ScopeCode codeList="https://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_ScopeCode"
                                        codeListValue="dataset">dataset</mcc:MD_ScopeCode>
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
                <mrd:distributionFormat>
                  <mrd:MD_Format>
                     <mrd:formatSpecificationCitation>
                        <cit:CI_Citation>
                           <cit:title>
                              <gcx:Anchor xlink:href="http://inspire.ec.europa.eu/media-types/application/x-shapefile">ESRI Shapefile (.shp)</gcx:Anchor>
                           </cit:title>
                           <cit:date gco:nilReason="unknown"/>
                           <cit:edition>
                              <gco:CharacterString>-</gco:CharacterString>
                           </cit:edition>
                        </cit:CI_Citation>
                     </mrd:formatSpecificationCitation>
                  </mrd:MD_Format>
               </mrd:distributionFormat>
                -->
                <mrd:transferOptions>
                  <mrd:MD_DigitalTransferOptions>
                    <xsl:for-each select="$distributions">
                      <xsl:variable name="accessUrl"
                                    select="gn-fn-sparql:getObject($root,
                                                        'http://www.w3.org/ns/dcat#accessURL',
                                                        .)/sr:uri"/>
                      <mrd:onLine>
                        <cit:CI_OnlineResource>
                          <cit:linkage>
                            <gco:CharacterString>
                              <xsl:value-of select="$accessUrl"/>
                            </gco:CharacterString>
                          </cit:linkage>

                          <xsl:for-each select="gn-fn-sparql:getObject($root,
                                                        'http://www.w3.org/ns/adms#representationTechnique',
                                                        .)/sr:bnode[. != '']">
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

                          <xsl:for-each select="gn-fn-sparql:getObject($root,
                                                        'http://purl.org/dc/terms/title',
                                                        .)/sr:literal">
                            <cit:name>
                              <gco:CharacterString>
                                <xsl:value-of select="."/>
                              </gco:CharacterString>
                            </cit:name>
                          </xsl:for-each>

                          <xsl:for-each select="gn-fn-sparql:getObject($root,
                                                        'http://purl.org/dc/terms/description',
                                                        .)/sr:literal">
                            <cit:description>
                              <gco:CharacterString>
                                <xsl:value-of select="."/>
                              </gco:CharacterString>
                            </cit:description>
                          </xsl:for-each>
                        </cit:CI_OnlineResource>
                      </mrd:onLine>
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
          <gco:DateTime><xsl:value-of select="$date"/></gco:DateTime>
        </cit:date>
        <cit:dateType>
          <cit:CI_DateTypeCode codeList="https://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_DateTypeCode"
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
          <lan:MD_CharacterSetCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_CharacterSetCode"
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
  -->
  <xsl:template name="build-contact">
    <xsl:param name="element" as="xs:string?" select="'mri:pointOfContact'"/>
    <xsl:param name="contactUri" as="xs:string"/>

    <xsl:variable name="role"
                  select="gn-fn-sparql:getObject($root,
                                    'http://www.w3.org/2006/vcard/ns#role',
                                    $contactUri)/sr:literal"/>
    <xsl:variable name="title"
                  select="gn-fn-sparql:getObject($root,
                                    'http://www.w3.org/2006/vcard/ns#title',
                                    $contactUri)/sr:literal"/>
    <xsl:variable name="email"
                  select="gn-fn-sparql:getObject($root,
                                    'http://www.w3.org/2006/vcard/ns#hasEmail',
                                    $contactUri)/sr:literal"/>


    <xsl:element name="{$element}">
      <cit:CI_Responsibility>
        <cit:role>
          <cit:CI_RoleCode codeList="https://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_RoleCode"
                           codeListValue="{$role}"></cit:CI_RoleCode>
        </cit:role>
        <cit:party>
          <cit:CI_Organisation>
            <cit:name>
              <gco:CharacterString>
                <xsl:value-of select="$title"/>
              </gco:CharacterString>
            </cit:name>
            <cit:contactInfo>
              <cit:CI_Contact>
                <cit:address>
                  <cit:CI_Address>
                    <cit:electronicMailAddress>
                      <gco:CharacterString><xsl:value-of select="$email"/></gco:CharacterString>
                    </cit:electronicMailAddress>
                  </cit:CI_Address>
                </cit:address>
              </cit:CI_Contact>
            </cit:contactInfo>
          </cit:CI_Organisation>
        </cit:party>
      </cit:CI_Responsibility>
    </xsl:element>
  </xsl:template>
</xsl:stylesheet>
