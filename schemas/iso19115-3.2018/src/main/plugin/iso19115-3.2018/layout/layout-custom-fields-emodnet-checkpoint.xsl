<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/2.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/2.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:dqm="http://standards.iso.org/iso/19157/-2/dqm/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19157/-2/mrc/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gfc="http://standards.iso.org/iso/19110/gfc/1.1"
                xmlns:gmx="http://standards.iso.org/iso/19115/-3/gmx"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                exclude-result-prefixes="#all">


  <!--
   This is an example template to display quality measures
   as a simple table view. In this table, only values can be edited.
  -->
  <xsl:template name="iso19115-3.2018-qm">
    <xsl:param name="config" as="node()?"/>

    <xsl:variable name="format" select="'#0'"></xsl:variable>

    <xsl:variable name="isDps"
                  select="count(
                            $metadata/mdb:metadataStandard/*/cit:title/*[text() =
                              'ISO 19115-3 - Emodnet Checkpoint - Data Product Specification']
                          ) = 1"/>

    <xsl:variable name="isTdp"
                  select="count(
                            $metadata/mdb:metadataStandard/*/cit:title/*[text() =
                              'ISO 19115-3 - Emodnet Checkpoint - Targeted Data Product']
                          ) = 1"/>

    <xsl:variable name="isUd"
                  select="count(
                            $metadata/mdb:metadataStandard/*/cit:title/*[text() =
                              'ISO 19115-3 - Emodnet Checkpoint - Upstream Data']
                          ) = 1"/>
    <xsl:variable name="challenge"
                  select="$metadata/mdb:identificationInfo/*/
                            mri:descriptiveKeywords[
                              contains(*/mri:thesaurusName/*/cit:title/gco:CharacterString,
                              'Used by challenges')
                            ]/*/mri:keyword/gco:CharacterString/text()"/>

    <!-- Component is in a section -->
    <xsl:variable name="cptId" select="*/@uuid[contains(., '/CP')]"/>

    <xsl:if test="matches($cptId, '.*/CP[0-9]*(/.*|$)') and not(ends-with($cptId, '#QE'))">
      <xsl:call-template name="render-boxed-element">
        <xsl:with-param name="label"
                        select="concat($strings/checkpoint-dps-component, ' ')"/>
        <xsl:with-param name="editInfo" select="gn:element"/>
        <xsl:with-param name="cls" select="local-name()"/>
        <!--<xsl:with-param name="attributesSnippet" select="$attributes"/>-->
        <xsl:with-param name="subTreeSnippet">


          <!-- TODO: List all related DPS/TDP/UD with link in editor mode -->


          <xsl:variable name="isMedsea"
                        select="starts-with(*/mcc:levelDescription[1]//mcc:other, 'MEDSEA')"/>


          <!-- Component description -->
          <xsl:for-each select="*/mdq:scope">
            <!-- TODO: In TDP do no display/or readonly component details ? -->

            <!-- Think to bypass choice element MD_ScopeDescription_TypeCHOICE_ELEMENT0 using // -->
            <xsl:apply-templates mode="mode-iso19115-3.2018"
                                 select="*/mcc:levelDescription[1]//mcc:other">
              <xsl:with-param name="overrideLabel" select="$strings/checkpoint-dps-component-name"/>
              <xsl:with-param name="isDisabled" select="$isTdp"/>
            </xsl:apply-templates>

            <xsl:apply-templates mode="mode-iso19115-3.2018"
                                 select="*/mcc:levelDescription[2]//mcc:other">
              <xsl:with-param name="overrideLabel" select="$strings/checkpoint-dps-component-description"/>
              <!--<xsl:with-param name="isDisabled" select="$isTdp"/>-->
            </xsl:apply-templates>

            <xsl:if test="$isDps or $isTdp or $isUd">
              <!--<xsl:apply-templates mode="mode-iso19115-3.2018"
                                   select="*/mcc:levelDescription[3]//mcc:other">
                <xsl:with-param name="overrideLabel" select="$strings/checkpoint-dps-component-lineage"/>
                <xsl:with-param name="isDisabled" select="$isTdp"/>
              </xsl:apply-templates>-->

              <xsl:for-each select="*/mcc:levelDescription[position() > 2]//mcc:other">

                <xsl:variable name="name"
                              select="./*/gn:element/@ref"/>
                <xsl:variable name="parent"
                              select="ancestor::mcc:levelDescription/gn:element"/>

                <div class="form-group gn-field gn-other"
                     id="gn-el-{$name}"
                     data-gn-field-highlight="">
                  <label for="gn-field-{$name}"
                         class="col-sm-2 control-label">
                    <xsl:value-of select="$strings/checkpoint-dps-component-lineage"/>
                  </label>
                  <div class="col-sm-9 gn-value">
                    <input class="" id="gn-field-{$name}" name="_{$name}"
                           type="hidden"
                           value="{./*}"/>

                    <div data-gn-checkpoint-lineage=""
                         data-ref="_{$name}">
                    </div>

                    <div data-ref="_{$name}"
                         data-type="text"
                         gn-field-suggestions=""
                         data-field="checkpointUdLineageDesc"
                         data-fq="{if ($challenge != '') then $challenge else ''}">
                    </div>
                  </div>
                  <div class="col-sm-1 gn-control">
                    <a class="btn pull-right"
                       data-gn-click-and-spin="remove({$parent/@ref}, {$parent/@parent}, {$name})"
                       data-gn-field-highlight-remove="{$name}"
                       data-toggle="tooltip"
                       data-placement="top"
                       title="Delete this field">
                      <i class="fa fa-times text-danger gn-control"></i>
                    </a>
                  </div>
                </div>


                <!--<xsl:call-template name="render-element">
                  <xsl:with-param name="label" select="$strings/checkpoint-dps-component-lineage"/>
                  <xsl:with-param name="value" select="./*"/>
                  <xsl:with-param name="cls" select="local-name(.)"/>
                  <xsl:with-param name="type" select="'text'"/>
                  <xsl:with-param name="name" select="./*/gn:element/@ref"/>
                  <xsl:with-param name="editInfo" select="./*/gn:element"/>
                  <xsl:with-param name="isReadOnly" select="$isTdp"/>
                  <xsl:with-param name="parentEditInfo" select="ancestor::mcc:levelDescription/gn:element"/>
                  <xsl:with-param name="listOfValues">
                    <xsl:if test="$isDps">
                      <directive name="gn-field-suggestions"
                                 data-field="checkpointUdLineageDesc"
                                 data-fq="{if ($challenge != '') then $challenge else ''}"/>
                    </xsl:if>
                  </xsl:with-param>
                </xsl:call-template>-->


                <!-- Add link to upstream data -->
                <xsl:if test="$isTdp">
                  <xsl:variable name="labelKey" select="'checkpoint-linkToUpstreamData'"/>
                  <xsl:variable name="label" select="$strings/*[name() = $labelKey]"/>
                  <xsl:call-template name="render-associated-resource-button">
                    <xsl:with-param name="type" select="'sibling'"/>
                    <xsl:with-param name="options">{"associationType": "crossReference", "initiativeType": "upstreamData"}</xsl:with-param>
                    <xsl:with-param name="label" select="if ($label != '') then $label else $labelKey"/>
                  </xsl:call-template>
                </xsl:if>
              </xsl:for-each>


              <!-- TODO: Add the capability to populate manually the value -->
              <xsl:if test="$isDps">
                <xsl:variable name="ref" select="*/gn:element/@ref"/>
                <div class="form-group gn-field gn-lineage gn-extra-field gn-add-field"
                     data-gn-field-highlight="">
                  <label class="col-sm-2 control-label">
                  </label>
                  <div class="col-sm-9">
                    <div>
                      <button class="btn btn-default ng-isolate-scope"
                              data-gn-template-field-add-button="_X{$ref}_mccCOLONlevelDescription">
                        <i class="fa fa-plus"></i>
                      </button>
                      <div class="hidden">
                        <textarea class="form-control gn-debug ng-isolate-scope"
                                  name="_X{$ref}_mccCOLONlevelDescription">
                          <![CDATA[<mcc:levelDescription
                      xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                      xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0">
                      <mcc:MD_ScopeDescription>
                        <mcc:other>
                          <gco:CharacterString></gco:CharacterString>
                        </mcc:other>
                      </mcc:MD_ScopeDescription>
                    </mcc:levelDescription>]]>
                        </textarea>
                      </div>
                    </div>
                  </div>
                </div>
              </xsl:if>
            </xsl:if>

            <xsl:apply-templates mode="mode-iso19115-3.2018"
                                 select="*/mcc:extent"/>

          </xsl:for-each>

          <!-- Checkbox if component covered or not -->
          <xsl:variable name="sqr"
                        select="*/mdq:standaloneQualityReport/*"/>
          <xsl:variable name="sqrId"
                        select="concat('gn-sqr-', generate-id($sqr/mdq:reportReference))"/>
          <xsl:variable name="isCovered"
                        select="normalize-space($sqr/mdq:reportReference/*/cit:title) = ''"/>
          <xsl:variable name="isNotCovered"
                        select="contains($sqr/mdq:reportReference/*/cit:title, 'Component is not covered')"/>

          <!-- In TDP define if component is covered or not and explain why.
           When declared not covered, the measure table below is hidden by
           the directive. -->
          <xsl:if test="$isTdp or $isUd">
            <div data-gn-checkpoint-cpt-covered="{$isCovered}"
                 data-id="{$sqrId}"
                 data-title-id="{$sqr/mdq:reportReference/*/cit:title/gco:CharacterString/gn:element/@ref}"
                 data-abstract-id="{$sqr/mdq:abstract/gco:CharacterString/gn:element/@ref}"
            />
            <!-- If not display textearea to populate explanation
            in a standalone quality report.

            Component can not be covered
            -->
            <div id="{$sqrId}">
              <xsl:apply-templates mode="mode-iso19115-3.2018"
                                   select="$sqr/mdq:reportReference/*/cit:title"/>
              <xsl:apply-templates mode="mode-iso19115-3.2018"
                                   select="$sqr/mdq:abstract"/>
            </div>
          </xsl:if>




          <xsl:choose>
            <xsl:when test="false()"></xsl:when>
            <!--<xsl:when test="$isUd and $isNotCovered">-->
            <!-- In an UD when component is not covered, display only the reason why.
            No quality table.
            <div>
              <xsl:apply-templates mode="mode-iso19115-3.2018"
                                   select="$sqr/mdq:reportReference/*/cit:title"/>
              <xsl:apply-templates mode="mode-iso19115-3.2018"
                                   select="$sqr/mdq:abstract"/>
            </div>
          </xsl:when>-->
            <xsl:otherwise>


              <!-- If covered, display table -->
              <div id="{$sqrId}-table">

                <!-- Component QMs -->
                <xsl:variable name="values">
                  <header>
                    <col>
                      <xsl:value-of select="gn-fn-metadata:getLabel($schema, 'mdq:measureIdentification', $labels,'', '', '')/label"/>
                    </col>
                    <col>
                      <xsl:value-of select="gn-fn-metadata:getLabel($schema, 'mdq:nameOfMeasure', $labels,'', '', '')/label"/>
                    </col>
                    <col class="gn-table-min-width">
                      <xsl:value-of select="gn-fn-metadata:getLabel($schema, 'mdq:value', $labels,'', '', '')/label"/>
                    </col>
                    <col>
                      <xsl:value-of select="gn-fn-metadata:getLabel($schema, 'mdq:valueUnit', $labels,'', '', '')/label"/>
                    </col>
                    <xsl:choose>
                      <xsl:when test="$isTdp">
                        <col>
                          <xsl:value-of select="$strings/checkpoint-qe"/>
                        </col>
                        <col>
                          <xsl:value-of select="$strings/checkpoint-fu"/>
                        </col>
                        <col>
                          <xsl:value-of select="$strings/checkpoint-dps-value"/>
                        </col>
                      </xsl:when>
                      <xsl:when test="$isUd">
                        <col>
                          <xsl:value-of select="$strings/checkpoint-qe"/>
                        </col>
                        <col>
                          <xsl:value-of select="$strings/checkpoint-fu"/>
                        </col>
                        <col>
                          <xsl:value-of select="$strings/checkpoint-dps-value"/>
                        </col>
                        <col>
                          <xsl:value-of select="$strings/checkpoint-tdp-value"/>
                        </col>
                      </xsl:when>
                    </xsl:choose>
                  </header>
                  <xsl:for-each select="*/mdq:report/mdq:*">
                    <xsl:variable name="measureId"
                                  select="mdq:measure/*/mdq:measureIdentification/*/mcc:code/*"/>
                    <xsl:variable name="measureName"
                                  select="mdq:measure/*/mdq:nameOfMeasure/*"/>
                    <xsl:variable name="measureDesc"
                                  select="mdq:measure/*/mdq:measureDescription/*"/>

                    <xsl:choose>
                      <xsl:when test="$isDps and $measureId = 'AP.5.1'">
                        <!-- not displayed -->
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:for-each select="mdq:result">
                          <xsl:variable name="unit"
                                        select="*/mdq:valueUnit/*/gml:identifier"/>

                          <!-- TODO: Add group by date -->
                          <row title="{$measureDesc}">
                            <xsl:choose>
                              <!-- Quantitative results with units -->
                              <xsl:when test="mdq:DQ_QuantitativeResult">
                                <col readonly="">
                                  <!-- Add DPS. or TDP. or UD prefix. -->
                                  <xsl:value-of select="if ($isUd) then 'UD.' else if ($isTdp) then 'TDP.' else if ($isDps) then 'DPS.' else ''"/>
                                  <xsl:value-of select="$measureId"/>
                                </col>
                                <col readonly="">
                                  <xsl:value-of select="$measureName"/>
                                </col>
                                <!--<col type="{*/mdq:valueRecordType/*/text()}"
                                     min="0">-->
                                <xsl:choose>
                                  <xsl:when test="$measureName = 'Usability'">
                                    <col type="select">
                                      <xsl:copy-of select="*/mdq:value/gco:*[1]"/>
                                      <options>
                                        <option value="Excellent">Excellent</option>
                                        <option value="Very good">Very good</option>
                                        <option value="Good">Good</option>
                                        <option value="Limited">Limited</option>
                                        <option value="Inadequate">Inadequate</option>
                                      </options>
                                    </col>
                                  </xsl:when>
                                  <xsl:otherwise>
                                    <col type="{*/mdq:valueRecordType/*/text()}">
                                      <xsl:if test="$isUd and $measureName = 'Number of Characteristics'">
                                        <xsl:attribute name="readonly"/>
                                      </xsl:if>
                                      <xsl:copy-of select="*/mdq:value/gco:*[1]"/>
                                    </col>
                                  </xsl:otherwise>
                                </xsl:choose>


                                <col readonly="">
                                  <xsl:value-of select="if ($unit/text() != '')
                                            then $unit/text()
                                            else */mdq:valueRecordType/*/normalize-space()"/>
                                </col>
                                <xsl:choose>
                                  <xsl:when test="$isTdp">
                                    <xsl:variable name="qeId"
                                                  select="concat('P.', replace($measureId/text(), 'AP', 'APE'))"/>
                                    <xsl:variable name="tdpQe"
                                                  select="$metadata/mdb:dataQualityInfo/*[starts-with(@uuid, $cptId)]
                                                      /mdq:report/*[
                                                        mdq:measure/*/mdq:measureIdentification/*/mcc:code/*/text() = $qeId
                                                      ]"/>

                                    <col readonly="" title="{$tdpQe/mdq:measure/*/mdq:measureDescription/*/text()}">
                                      <xsl:value-of select="format-number($tdpQe/mdq:result/*/mdq:value/*/text(), $format)"/>
                                    </col>

                                    <xsl:call-template name="checkpoint-render-indicator">
                                      <xsl:with-param name="metadata" select="$metadata"/>
                                      <xsl:with-param name="measureId" select="$measureId"/>
                                      <xsl:with-param name="cptId" select="$cptId"/>
                                      <xsl:with-param name="isMedsea" select="$isMedsea"/>
                                      <xsl:with-param name="format" select="$format"/>
                                    </xsl:call-template>

                                    <col readonly="">
                                      <span data-gn-qm-value="{concat($cptId, '|', $measureId/text())}"/>
                                    </col>
                                  </xsl:when>
                                  <xsl:when test="$isUd">
                                    <xsl:variable name="qeId"
                                                  select="concat('UD.', replace($measureId/text(), 'AP', 'APE'))"/>
                                    <xsl:variable name="udQe"
                                                  select="$metadata/mdb:dataQualityInfo/*[starts-with(@uuid, $cptId)]
                                                      /mdq:report/*[
                                                        mdq:measure/*/mdq:measureIdentification/*/mcc:code/*/text() = $qeId
                                                      ]"/>
                                    <col readonly="" title="{$udQe/mdq:measure/*/mdq:measureDescription/*/text()}">
                                      <xsl:variable name="v"
                                                    select="$udQe/mdq:result/*/mdq:value/*/text()"/>

                                      <xsl:value-of select="if (matches($v, '^-?\d+(,\d+)*(\.\d+(e\d+)?)?$'))
                                      then format-number($v, $format)
                                      else $v"/>
                                    </col>

                                    <xsl:call-template name="checkpoint-render-indicator">
                                      <xsl:with-param name="metadata" select="$metadata"/>
                                      <xsl:with-param name="measureId" select="$measureId"/>
                                      <xsl:with-param name="cptId" select="$cptId"/>
                                      <xsl:with-param name="isMedsea" select="$isMedsea"/>
                                      <xsl:with-param name="format" select="$format"/>
                                    </xsl:call-template>

                                    <col readonly="">
                                      <span data-gn-qm-value="{concat($cptId, '|', $measureId/text())}"/>
                                    </col>
                                    <col readonly="">
                                      <span data-gn-qm-value="{concat($cptId, '|', $measureId/text())}" data-tdp="true"/>
                                    </col>
                                  </xsl:when>
                                </xsl:choose>



                                <!-- Measures can only be removed in the DPS. ie. once defined
                                 in component in a spec, the related TDP and UDs MUST encode
                                 the same list of values. -->
                                <xsl:if test="$isDps">
                                  <col remove="">
                                    <xsl:copy-of select="ancestor::mdq:report/gn:element"/>
                                  </col>
                                </xsl:if>
                              </xsl:when>
                              <!-- Descriptive results -->
                              <xsl:when test="mdq:DQ_DescriptiveResult">
                                <col/>
                                <col readonly="">
                                  <xsl:value-of select="$measureName"/>
                                  (<xsl:value-of select="gn-fn-metadata:getLabel($schema, 'mdq:DQ_DescriptiveResult', $labels,'', '', '')/label"/>)
                                </col>
                                <col type="textarea" colspan="2">
                                  <xsl:copy-of select="*/mdq:statement/gco:*"/>
                                </col>
                                <col/>
                                <xsl:if test="$isUd or $isTdp">
                                  <col/>
                                  <!-- DPS & TDP descriptive result for the measure -->
                                  <col readonly="">
                                    <span data-gn-qm-value="{concat($cptId, '|', $measureId/text())}" data-descriptive-result="true">test</span>
                                  </col>
                                  <xsl:if test="$isUd">
                                    <col readonly="">
                                      <span data-gn-qm-value="{concat($cptId, '|', $measureId/text())}" data-tdp="true" data-descriptive-result="true"/>
                                    </col>
                                  </xsl:if>
                                </xsl:if>
                              </xsl:when>
                              <xsl:otherwise>
                                <!-- Not supported -->
                              </xsl:otherwise>
                            </xsl:choose>
                          </row>
                          <!-- TODO: Add all unique date of measure-->
                        </xsl:for-each>
                      </xsl:otherwise>
                    </xsl:choose>

                  </xsl:for-each>
                  <row>
                    <col readonly="" colspan="4">
                      <xsl:value-of select="$strings/checkpoint-measure-date"/>&#160;
                      <xsl:value-of select="string-join(
                                          distinct-values(*/mdq:report/mdq:*/
                                            mdq:result/*/mdq:dateTime/gco:Date), ', ')"/>
                    </col>
                  </row>
                </xsl:variable>

                <xsl:call-template name="render-table">
                  <xsl:with-param name="values" select="$values"/>
                  <xsl:with-param name="addControl">
                    <xsl:if test="$config/@or">
                      <xsl:apply-templates select="*/gn:child[@name = $config/@or]"
                                           mode="mode-iso19115-3.2018"/>
                    </xsl:if>
                  </xsl:with-param>
                </xsl:call-template>
              </div>
            </xsl:otherwise>
          </xsl:choose>


        </xsl:with-param>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>


  <xsl:template name="checkpoint-render-indicator">
    <xsl:param name="metadata"/>
    <xsl:param name="measureId"/>
    <xsl:param name="cptId"/>
    <xsl:param name="isMedsea"/>
    <xsl:param name="format"/>


    <xsl:variable name="fuId"
                  select="concat('UD.', replace($measureId/text(), 'AP', 'FU'))"/>
    <xsl:variable name="udFu"
                  select="$metadata/mdb:dataQualityInfo/*[starts-with(@uuid, $cptId)]
                                                      /mdq:report/*[
                                                        mdq:measure/*/mdq:measureIdentification/*/mcc:code/*/text() = $fuId
                                                      ]"/>
    <!--
        Red: [-100 et -10[
        Yellow: [-10 et + 10]
        Green: ]10 à 100%]
    -->
    <xsl:variable name="v" select="$udFu/mdq:result/*/mdq:value/*/text()"/>

    <xsl:choose>
      <xsl:when test="$isMedsea">
        <col readonly=""
             class="{if (string($v) = 'NaN' or $v = '') then ''
                                            else if ($v &lt; -10) then 'gn-class-red'
                                            else if ($v &gt;= -10 and $v &lt;= 10) then 'gn-class-green'
                                            else if ($v &gt; 10) then 'gn-class-green' else ''}"
             title="{$udFu/mdq:measure/*/mdq:measureDescription/*/text()}">
          <xsl:value-of select="format-number($v, $format)"/>
        </col>
      </xsl:when>
      <xsl:otherwise>
        <col readonly=""
             class="{if (string($v) = 'NaN' or $v = '') then ''
                                        else if ($v &lt; -10) then 'gn-class-red'
                                        else if ($v &gt;= -10) then 'gn-class-green'
                                        else ''}"
             title="{$udFu/mdq:measure/*/mdq:measureDescription/*/text()}">
          <xsl:value-of select="format-number($v, $format)"/>
        </col>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>







  <!-- MEDSEA / Use thesaurus local.theme.emodnet-checkpoint.challenges
  to populate the field gmd:hierarchyLevelName.
                            -->
  <xsl:template mode="mode-iso19115-3.2018" priority="20000"
                match="mdb:metadataScope/mdb:MD_MetadataScope/mdb:name[
                            contains($metadata/mdb:metadataStandard/*/cit:title/gco:CharacterString,
                                     'Emodnet Checkpoint') or
                            contains($metadata/mdb:metadataStandard/*/cit:title/gco:CharacterString,
                                     'Emodnet Checkpoint - Targeted Product')]">
    <div class="form-group gn-field"
         id="gn-el-11">
      <label for="gn-field-11" class="col-sm-2 control-label">
        <xsl:value-of select="$strings/challenge"/>
      </label>
      <div class="col-sm-9 gn-value">
        <input class="form-control" value="{gco:CharacterString}"
               name="_{gco:CharacterString/gn:element/@ref}"
               data-gn-keyword-picker=""
               data-thesaurus-key="local.theme.emodnet-checkpoint.challenges"
               data-gn-field-tooltip="iso19115-3|mdb:metadataScope||/mdb:MD_Metadata/mdb:metadataScope"
               type="text"/>
      </div>
    </div>
  </xsl:template>


  <!-- Display challenge expert score with a helper first
  and then the challenge expert opinion. -->
  <xsl:template match="mri:resourceConstraints/mco:MD_Constraints"
                name="checkpoint-expert-score">

    <xsl:apply-templates select="mco:useLimitation[1]"
                         mode="mode-iso19115-3.2018"/>

    <xsl:for-each select="mco:useLimitation[2]">
      <div class="form-group gn-field"
           id="gn-el-11">
        <label for="gn-field-11" class="col-sm-2 control-label">
          <xsl:value-of select="$strings/challengeExpertOpinion"/>
        </label>
        <div class="col-sm-9 gn-value">
          <textarea class="form-control"
                 name="_{gco:CharacterString/gn:element/@ref}">
            <xsl:value-of select="gco:CharacterString"/>
          </textarea>
        </div>
      </div>
    </xsl:for-each>

  </xsl:template>
</xsl:stylesheet>
