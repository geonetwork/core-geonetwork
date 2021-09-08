<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:mac="http://standards.iso.org/iso/19115/-3/mac/2.0"
                xmlns:dqm="http://standards.iso.org/iso/19157/-2/dqm/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:reg="http://standards.iso.org/iso/19115/-3/reg/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:mex="http://standards.iso.org/iso/19115/-3/mex/1.0"
                xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/2.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:gfc="http://standards.iso.org/iso/19110/gfc/1.1"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:tr="java:org.fao.geonet.api.records.formatters.SchemaLocalizations"
                xmlns:gn-fn-render="http://geonetwork-opensource.org/xsl/functions/render"
                xmlns:gn-fn-iso19115-3.2018="http://geonetwork-opensource.org/xsl/functions/profiles/iso19115-3.2018"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:saxon="http://saxon.sf.net/"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">
  <!-- This formatter render an ISO19115-3 record based on the
  editor configuration file.

  The layout is made in 2 modes:
  * render-field taking care of elements (eg. sections, label)
  * render-value taking care of element values (eg. characterString, URL)

  3 levels of priority are defined: 100, 50, none
  -->


  <xsl:variable name="dateFormatRegex"
                select="'(\d{4}-[01]\d-[0-3]\d.*)'"/>

  <!-- Load the editor configuration to be able
  to render the different views -->
  <xsl:variable name="configuration"
                select="document('../../layout/config-editor.xml')"/>

 <!-- Required for utility-fn.xsl -->
  <xsl:variable name="editorConfig"
                select="document('../../layout/config-editor.xml')"/>

  <!-- Some utility -->
  <xsl:include href="../../layout/evaluate.xsl"/>
  <xsl:include href="../../layout/utility-tpl-multilingual.xsl"/>
  <xsl:include href="../../layout/utility-fn.xsl"/>
  <xsl:include href="../../formatter/jsonld/iso19115-3.2018-to-jsonld.xsl"/>

  <!-- The core formatter XSL layout based on the editor configuration -->
  <xsl:include href="sharedFormatterDir/xslt/render-layout.xsl"/>
  <!--<xsl:include href="../../../../../data/formatter/xslt/render-layout.xsl"/>-->

  <!-- Define the metadata to be loaded for this schema plugin-->
  <xsl:variable name="metadata"
                select="/root/mdb:MD_Metadata"/>

  <xsl:variable name="langId" select="gn-fn-iso19115-3.2018:getLangId($metadata, $language)"/>

  <xsl:variable name="allLanguages">
    <xsl:call-template name="get-iso19115-3.2018-other-languages"/>
  </xsl:variable>

  <!-- Ignore some fields displayed in header or in right column -->
  <xsl:template mode="render-field"
                match="mri:graphicOverview|mri:abstract|mdb:identificationInfo/*/mri:citation/*/cit:title"
                priority="2000"/>


  <!-- Specific schema rendering -->
  <xsl:template mode="getMetadataTitle" match="mdb:MD_Metadata">
    <xsl:for-each select="mdb:identificationInfo/*/mri:citation/*/cit:title">
      <xsl:call-template name="get-iso19115-3.2018-localised">
        <xsl:with-param name="langId" select="$langId"/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <xsl:template mode="getMetadataAbstract" match="mdb:MD_Metadata"/>


  <xsl:template mode="getTags" match="mdb:MD_Metadata"/>

  <xsl:template mode="getMetadataHierarchyLevel" match="mdb:MD_Metadata">
    <xsl:value-of select="mdb:metadataScope/*/mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue"/>
  </xsl:template>

  <xsl:template mode="getMetadataThumbnail" match="mdb:MD_Metadata"/>

  <xsl:template mode="getExtent" match="mdb:MD_Metadata"/>

  <xsl:template mode="getOverviews" match="mdb:MD_Metadata"/>

  <xsl:template mode="getMetadataHeader" match="mdb:MD_Metadata"/>

  <xsl:template mode="getMetadataCitation" match="mdb:MD_Metadata"/>

  <xsl:template name="cersat-summary-view">
<!--    <h1><xsl:apply-templates mode="getMetadataTitle" select="$metadata"/></h1>-->

    <div class="row">
      <div class="col-md-8">
        <div class="row">
          <div class="col-md-12 cersat-head-badge">
            <xsl:variable name="version"
                          select="$metadata/mdb:resourceLineage/*/mrl:processStep/*/mrl:stepDateTime/*"/>

            <xsl:for-each select="$version">
              <xsl:sort select="gml:timePosition" order="descending"/>
              <xsl:if test="position() = 1">
                <div class="cersat-bg cersat-bg-lightgreen">
                  <div>
                    <xsl:value-of select="$schemaStrings/cersat-version"/>
                  </div>
                  <div class="cersat-bg-white"><xsl:value-of select="gml:identifier"/></div>
                </div>
              </xsl:if>
            </xsl:for-each>


            <xsl:variable name="doiUrl"
                          select="$metadata/mdb:identificationInfo/*/mri:citation/*/cit:identifier/*/mcc:code/*[contains(lower-case(text()), 'doi')]"/>
            <xsl:if test="$doiUrl != ''">
              <div class="cersat-bg cersat-bg-lightgreen">
                <div>
                  DOI
                </div>
                <div class="cersat-bg-white">
                  <a href="{$doiUrl}">
                    <xsl:value-of select="$doiUrl"/>
                  </a>
                </div>
              </div>
            </xsl:if>


            <xsl:variable name="status"
                          select="$metadata/mdb:identificationInfo/*/mri:status/*/@codeListValue"/>
            <xsl:if test="$status != ''">
              <div class="cersat-bg cersat-bg-lightgreen">
                <div>
                  <xsl:value-of select="$schemaStrings/cersat-opdataset"/>
                </div>
                <div class="cersat-bg-white">
                  <xsl:value-of select="tr:codelist-value-label(
                                          tr:create($schema),
                                          'MD_ProgressCode',
                                          $status)"/>
                </div>
              </div>
            </xsl:if>
          </div>
        </div>


        <div class="row">
          <div class="col-md-12">
            <xsl:for-each select="$metadata/mdb:identificationInfo/*/mri:graphicOverview/*">
              <img data-gn-img-modal="md"
                   class="gn-img-thumbnail pull-left"
                   alt="{$schemaStrings/overview}"
                   src="{mcc:fileName/*}"/>
            </xsl:for-each>

            <xsl:for-each select="$metadata/mdb:identificationInfo/*/mri:abstract">
              <xsl:variable name="txt">
                <xsl:call-template name="get-iso19115-3.2018-localised">
                  <xsl:with-param name="langId" select="$langId"/>
                </xsl:call-template>
              </xsl:variable>
              <xsl:call-template name="addLineBreaksAndHyperlinks">
                <xsl:with-param name="txt" select="$txt"/>
              </xsl:call-template>
            </xsl:for-each>

            <xsl:variable name="authors"
                          select="$metadata/mdb:identificationInfo/*/mri:pointOfContact[.//cit:role/*/@codeListValue = 'principalInvestigator']"/>
            <xsl:if test="count($authors) > 0">
              <div class="">
                <strong>
                  <xsl:value-of select="$schemaStrings/eo-cersat-author"/>
                </strong>
                <xsl:for-each select="$authors">
                  <xsl:value-of select=".//cit:party/*/cit:name"/>
                  <xsl:for-each select=".//cit:electronicMailAddress">
                    <a href="mailto:{.}">
                      <i class="fa fa-envelope"></i>
                    </a>
                  </xsl:for-each>
                  <xsl:if test="position() != last()">,</xsl:if>
                </xsl:for-each>
              </div>
            </xsl:if>

            <xsl:variable name="gcmd-keyword"
                          select="$metadata/mdb:identificationInfo/*/mri:descriptiveKeywords
                                    [contains(*/mri:thesaurusName/*/cit:title/(gcx:Anchor|gco:CharacterString),
                                    'Cersat - GCMD parameter')]/*/mri:keyword"/>
            <xsl:if test="count($gcmd-keyword) > 0">
              <div class="row cersat-gcmd">
                <xsl:for-each select="$gcmd-keyword">
                  <div class="badge">
                    <xsl:call-template name="get-iso19115-3.2018-localised">
                      <xsl:with-param name="langId" select="$langId"/>
                    </xsl:call-template>
                  </div>
                </xsl:for-each>
              </div>
            </xsl:if>
          </div>
        </div>


        <div class="row">
          <div class="col-md-12">
            <h2>
              <xsl:value-of select="$schemaStrings/cersat-dataaccess"/>
            </h2>

            <!--<div class="panel panel-default">
              <div class="panel-heading">
                <xsl:value-of select="$schemaStrings/cersat-dataaccess"/>
              </div>
              <div class="panel-body">-->
                <div class="">

                  <xsl:variable name="accessPolicy"
                                select="$metadata/mdb:identificationInfo/*
                                        /mri:resourceConstraints/mco:MD_LegalConstraints
                                        /mco:accessConstraints/*/@codeListValue"/>

                  <xsl:if test="$accessPolicy != ''">
                    <strong>
                      <xsl:value-of select="$schemaStrings/cersat-accesspolicy"/>
                    </strong>

                    <xsl:variable name="codelistTranslation"
                                  select="tr:codelist-value-label(
                                              tr:create($schema),
                                              'MD_RestrictionCode',
                                              $accessPolicy)"/>
                    <xsl:value-of select="$codelistTranslation"/>
                  </xsl:if>
                </div>

                <xsl:variable name="formats"
                              select="$metadata//mrd:distributionFormat/*/mrd:formatSpecificationCitation/*[cit:title/*/text() != '']"/>
                <xsl:if test="count($formats) > 0">
                  <div class="">
                    <strong>
                      <xsl:value-of select="$schemaStrings/cersat-formats"/>
                    </strong>
                    <xsl:for-each select="$formats">
                      <xsl:value-of select="concat(cit:title, ' ', cit:edition)"/>
                      <xsl:if test="position() &lt; last()">, </xsl:if>
                    </xsl:for-each>
                  </div>
                  <br/>
                </xsl:if>

                <xsl:variable name="online"
                              select="$metadata//mrd:onLine/*[cit:function/*/@codeListValue != 'information']"/>
                <xsl:if test="count($online) > 0">
                  <xsl:for-each select="$online">
                    <div>
                      <strong>
                        <xsl:value-of select="if (cit:description/*/text() != '')
                                              then cit:description/*/text()
                                              else cit:protocol/*/text()"/>
                      </strong>
                      <xsl:choose>
                        <xsl:when test="cit:protocol/*/text() = 'Local'">
                          <xsl:value-of select="cit:linkage/*/text()"/>
                        </xsl:when>
                        <xsl:otherwise>
                          <a title="{cit:protocol/*/text()}"
                             href="{cit:linkage/*/text()}">
                            <xsl:value-of select="if (cit:name/*/text() != '')
                                                then cit:name/*/text()
                                                else cit:linkage/*/text()"/>
                          </a>
                        </xsl:otherwise>
                      </xsl:choose>
                    </div>
                  </xsl:for-each>
                  <br/>
                </xsl:if>
              <!--</div>
            </div>-->

            <xsl:variable name="citation"
                          select="$metadata/mdb:identificationInfo/*/mri:resourceConstraints/
                                mco:MD_LegalConstraints/mco:otherConstraints[2]"/>
            <xsl:for-each select="$citation">
              <div class="cersat-bg cersat-bg-green">
                <div>
                  <xsl:value-of select="$schemaStrings/cersat-citation"/>
                </div>
                <div class="cersat-bg-white">
                  <xsl:call-template name="get-iso19115-3.2018-localised">
                    <xsl:with-param name="langId" select="$langId"/>
                  </xsl:call-template>
                </div>
              </div>
            </xsl:for-each>
          </div>
        </div>


        <div class="row">
          <div class="col-md-12">
            <xsl:variable name="resources"
                        select="$metadata//mrd:onLine/*[cit:function/*/@codeListValue = 'information']"/>

            <xsl:if test="count($resources) &gt; 0">
              <h2>
                <xsl:value-of select="$schemaStrings/cersat-resources"/>
              </h2>

              <xsl:for-each select="$resources">
                <div>
                  <strong>
                    <xsl:value-of select="if (cit:description/*/text() != '')
                                            then cit:description/*/text()
                                            else cit:protocol/*/text()"/>
                  </strong>
                  <xsl:choose>
                    <xsl:when test="cit:protocol/*/text() = 'Local'">
                      <xsl:value-of select="cit:linkage/*/text()"/>
                    </xsl:when>
                    <xsl:otherwise>
                      <a title="{cit:protocol/*/text()}"
                         href="{cit:linkage/*/text()}">
                        <xsl:value-of select="if (cit:name/*/text() != '')
                                              then cit:name/*/text()
                                              else cit:linkage/*/text()"/>
                      </a>
                    </xsl:otherwise>
                  </xsl:choose>
                </div>
              </xsl:for-each>
            </xsl:if>
          </div>
        </div>
      </div>
      <div class="col-md-4">
        <div class="panel panel-default">
          <div class="panel-heading">ID: <xsl:value-of select="$metadata/mdb:metadataIdentifier/*/mcc:code/*/text()"/></div>
          <div class="panel-body">

            <xsl:variable name="projects"
                          select="$metadata/mdb:identificationInfo/*/mri:descriptiveKeywords
            [contains(*/mri:thesaurusName/*/cit:title/(gcx:Anchor|gco:CharacterString),
            'Cersat - Project')]/*/mri:keyword"/>
            <xsl:if test="count($projects) > 0">
              <div class="row cersat-bg cersat-bg-orange">
                <div>
                  <xsl:value-of select="$schemaStrings/cersat-projects"/>
                </div>
                <div class="cersat-bg-white">
                  <xsl:for-each select="$projects">
                    <xsl:call-template name="get-iso19115-3.2018-localised">
                      <xsl:with-param name="langId" select="$langId"/>
                    </xsl:call-template>
                    <xsl:if test="position() &lt; last()">, </xsl:if>
                  </xsl:for-each>
                </div>
              </div>
            </xsl:if>


            <h2>
              <xsl:value-of select="$schemaStrings/cersat-product"/>
            </h2>
            <xsl:for-each select="$metadata/mdb:contentInfo/*/mrc:processingLevelCode/*/mcc:code[*/text() != '']">
              <div>
                <strong>
                  <xsl:call-template name="landingpage-label">
                    <xsl:with-param name="key" select="'eo-level'"/>
                  </xsl:call-template>
                </strong>
                <xsl:value-of select="*/text()"/>
              </div>
            </xsl:for-each>

            <xsl:for-each select="$metadata/mdb:contentInfo/*/mrc:attributeDescription[*/text() != '']">
              <div>
                <strong>
                  <xsl:call-template name="landingpage-label">
                    <xsl:with-param name="key" select="'eo-acquisition-pattern'"/>
                  </xsl:call-template>
                </strong>
                <xsl:value-of select="*/text()"/>
              </div>
            </xsl:for-each>

            <xsl:for-each select="$metadata/mdb:contentInfo/*/mrc:processingLevelCode/*/mcc:description[*/text() != '']">
              <div>
                <strong>
                  <xsl:call-template name="landingpage-label">
                    <xsl:with-param name="key" select="'eo-compositing'"/>
                  </xsl:call-template>
                </strong>
                <xsl:value-of select="*/text()"/>
              </div>
            </xsl:for-each>

            <xsl:for-each select="$metadata/mdb:identificationInfo/*/mri:descriptiveKeywords
                            [contains(*/mri:thesaurusName/*/cit:title/(gcx:Anchor|gco:CharacterString),
                            'Cersat - Latency')]/*/mri:keyword[*/text() != '']">
              <div>
                <strong>
                  <xsl:call-template name="landingpage-label">
                    <xsl:with-param name="key" select="'eo-latency'"/>
                  </xsl:call-template>
                </strong>
                <xsl:value-of select="*/text()"/>
              </div>
            </xsl:for-each>


            <h2>
              <xsl:value-of select="$schemaStrings/cersat-obssource"/>
            </h2>

            <xsl:variable name="instruments"
                          select="$metadata/mdb:acquisitionInformation/*/mac:instrument/*"/>
            <xsl:for-each select="$instruments">
              <xsl:value-of select="mac:mountedOn/*/mac:identifier/*/mcc:code/*/text()"/> /
              <xsl:value-of select="mac:identifier/*/mcc:code/*/text()"/>
              <xsl:if test="position() &lt; last()">, </xsl:if>
            </xsl:for-each>



            <h2>
              <xsl:value-of select="$schemaStrings/cersat-temporal"/>
            </h2>

            <xsl:for-each select="$metadata/mdb:identificationInfo/*/mri:extent/*/gex:temporalElement//gml:TimePeriod">
              <div>
                <strong>
                  <xsl:call-template name="landingpage-label">
                    <xsl:with-param name="key" select="'eo-temporal-coverage'"/>
                  </xsl:call-template>
                </strong>


                <xsl:variable name="indeterminatePositionLabel"
                              select="gml:beginPosition/@indeterminatePosition"/>

                <xsl:if test="gml:beginPosition != '' or normalize-space($indeterminatePositionLabel) != ''">
                  <xsl:value-of select="concat((normalize-space($indeterminatePositionLabel), $schemaStrings/cersat-view-temporal-from)[1], ' ')"/>
                  <xsl:value-of select="if (matches(gml:beginPosition, $dateFormatRegex)) then format-date(xs:date(tokenize(gml:beginPosition, 'T')[1]), '[D01]-[M01]-[Y0001]') else gml:beginPosition"/>
                  <i class="fa fa-fw fa-arrow-right">&#160;</i>
                </xsl:if>


              <xsl:variable name="indeterminatePositionLabel"
                            select="gml:endPosition/@indeterminatePosition"/>

                <xsl:if test="gml:endPosition != '' or normalize-space($indeterminatePositionLabel) != ''">
                  <xsl:value-of select="concat((normalize-space($indeterminatePositionLabel), $schemaStrings/cersat-view-temporal-to)[1], ' ')"/>
                  <xsl:value-of select="if (matches(gml:endPosition, $dateFormatRegex)) then format-date(xs:date(tokenize(gml:endPosition, 'T')[1]), '[D01]-[M01]-[Y0001]') else gml:endPosition"/>
                </xsl:if>

                <xsl:if test="gml:timePosition != ''">
                  <xsl:value-of select="concat ($schemaStrings/cersat-view-temporal-at, ' ', gml:timePosition)"/>
                </xsl:if>
              </div>
            </xsl:for-each>

            <xsl:for-each select="$metadata/mdb:identificationInfo/*/mri:temporalResolution[gco:TM_PeriodDuration/text() != '']">
              <div class="flex-row">
                <div>
                  <strong>
                    <xsl:call-template name="landingpage-label">
                      <xsl:with-param name="key" select="'eo-resolution'"/>
                    </xsl:call-template>
                  </strong>
                </div>&#160;
                <div data-gn-field-duration-div="{gco:TM_PeriodDuration}"><xsl:value-of select="gco:TM_PeriodDuration"/></div>
              </div>
            </xsl:for-each>



            <h2>
              <xsl:value-of select="$schemaStrings/cersat-spatial"/>
            </h2>

            <xsl:for-each select="$metadata/mdb:identificationInfo/*/mri:extent/*/gex:geographicElement/gex:EX_GeographicDescription/gex:geographicIdentifier/*/mcc:code[*/text() != '']">
              <div>
                <strong>
                  <xsl:call-template name="landingpage-label">
                    <xsl:with-param name="key" select="'eo-boundaries-code'"/>
                  </xsl:call-template>
                </strong>
                <xsl:value-of select="*/text()"/>
              </div>
            </xsl:for-each>

            <xsl:for-each select="$metadata/mdb:identificationInfo/*/mri:spatialResolution/*//mri:distance[*/text() != '']">
              <div>
                <strong>
                  <xsl:call-template name="landingpage-label">
                    <xsl:with-param name="key" select="'eo-resolution'"/>
                  </xsl:call-template>
                </strong>
                <xsl:value-of select="*/text()"/>
              </div>
            </xsl:for-each>

            <xsl:for-each select="$metadata/mdb:referenceSystemInfo/*/
                          mrs:referenceSystemIdentifier/*/mcc:description[*/text() != '']">
              <div>
                <strong>
                  <xsl:call-template name="landingpage-label">
                    <xsl:with-param name="key" select="'eo-projection'"/>
                  </xsl:call-template>
                </strong>
                <xsl:value-of select="*/text()"/>
              </div>
            </xsl:for-each>

            <xsl:for-each select="$metadata/mdb:identificationInfo/*/mri:extent/*/gex:geographicElement/gex:EX_GeographicBoundingBox">

              <xsl:variable name="numberFormat" select="'0.00'"/>

              <div>
                <strong>
                  <xsl:call-template name="landingpage-label">
                    <xsl:with-param name="key" select="'eo-bbox'"/>
                  </xsl:call-template>
                </strong>
                Latitude <xsl:value-of select="format-number(gex:southBoundLatitude, $numberFormat)"/> to
                <xsl:value-of select="format-number(gex:northBoundLatitude, $numberFormat)"/>,
                Longitude <xsl:value-of select="format-number(gex:westBoundLongitude, $numberFormat)"/> to
                <xsl:value-of select="format-number(gex:eastBoundLongitude, $numberFormat)"/>
              </div>
            </xsl:for-each>

            <xsl:copy-of select="gn-fn-render:extent($metadataUuid)"/>

          </div>
        </div>

        <div class="panel panel-default">
          <div class="panel-heading">
            <xsl:value-of select="$schemaStrings/cersat-contacts"/>
          </div>
          <div class="panel-body">
            <xsl:variable name="help-desk"
                          select="$metadata/mdb:identificationInfo/*/mri:pointOfContact[.//cit:role/*/@codeListValue = 'pointOfContact']/*/cit:party/cit:CI_Organisation/cit:contactInfo/*/cit:address/*/cit:electronicMailAddress"/>
            <xsl:if test="$help-desk != ''">
              <div class="row cersat-bg cersat-bg-orange">
                <div>
                  <xsl:value-of select="$schemaStrings/cersat-helpdesk"/>
                </div>
                <div class="cersat-bg-white">
                  <xsl:value-of select="$help-desk"/>
                </div>
              </div>
            </xsl:if>

            <xsl:for-each-group
              select="$metadata/mdb:identificationInfo/*/mri:pointOfContact/*[
                        .//cit:role/*/@codeListValue != 'pointOfContact'
                        and .//cit:role/*/@codeListValue !=  'principalInvestigator']"
              group-by="cit:role/*/@codeListValue">
              <div class="row">
                <xsl:variable name="codelistTranslation"
                              select="tr:codelist-value-label(
                                        tr:create($schema),
                                        'CI_RoleCode',
                                        current-grouping-key())"/>

                <strong><xsl:value-of select="$codelistTranslation"/></strong>
                <xsl:for-each select="current-group()">
                  <xsl:value-of select=".//cit:party/*/cit:name"/>
                  <xsl:for-each select=".//cit:electronicMailAddress">
                    <a href="mailto:{.}">
                      <i class="fa fa-envelope"></i>
                    </a>
                  </xsl:for-each>
                </xsl:for-each>
              </div>
            </xsl:for-each-group>
          </div>
        </div>
      </div>
    </div>

    <div class="row">
      <div class="col-md-12">
        <h2>
          <xsl:value-of select="$schemaStrings/cersat-publications"/>
        </h2>
      </div>
    </div>

  </xsl:template>

  <xsl:template mode="render-field"
                match="@*"/>

  <xsl:template mode="render-value"
                match="@*"/>

</xsl:stylesheet>
