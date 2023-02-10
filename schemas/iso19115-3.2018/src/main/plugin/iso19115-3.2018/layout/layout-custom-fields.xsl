<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
  xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
  xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
  xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
  xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
  xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
  xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
  xmlns:gts="http://www.isotc211.org/2005/gts"
  xmlns:gml="http://www.opengis.net/gml/3.2"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
  xmlns:java-xsl-util="java:org.fao.geonet.util.XslUtil"
  xmlns:saxon="http://saxon.sf.net/"
  extension-element-prefixes="saxon"
  exclude-result-prefixes="#all">


  <!-- Readonly elements
  [parent::mds:metadataIdentifier and
                        mcc:codeSpace/gco:CharacterString='urn:uuid']|
                      mds:dateInfo/cit:CI_Date[cit:dateType/cit:CI_DateTypeCode
                        /@codeListValue='revision']
-->
  <xsl:template mode="mode-iso19115-3.2018"
                match="mdb:metadataIdentifier/mcc:MD_Identifier/mcc:code|
                       mdb:metadataIdentifier/mcc:MD_Identifier/mcc:codeSpace|
                       mdb:metadataIdentifier/mcc:MD_Identifier/mcc:description|
                       mdb:dateInfo/cit:CI_Date[cit:dateType/cit:CI_DateTypeCode = 'revision']/cit:date|
                       mdb:dateInfo/cit:CI_Date[cit:dateType/cit:CI_DateTypeCode = 'revision']/cit:dateType"
                priority="2000">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>
    <xsl:param name="overrideLabel" select="''" required="no"/>

    <xsl:variable name="xpath"
                  select="gn-fn-metadata:getXPath(.)"/>

    <xsl:variable name="isoType"
                  select="if (../@gco:isoType) then ../@gco:isoType else ''"/>

    <xsl:variable name="labelConfig"
                  select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)"/>

    <xsl:variable name="labelCfg">
      <xsl:choose>
        <xsl:when test="$overrideLabel != ''">
          <element>
            <label><xsl:value-of select="$overrideLabel"/></label>
          </element>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="$labelConfig"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>


    <xsl:call-template name="render-element">
      <xsl:with-param name="label" select="$labelCfg/*"/>
      <xsl:with-param name="value" select="*"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="gn-fn-metadata:getXPath(.)"/>
      <xsl:with-param name="type" select="gn-fn-metadata:getFieldType($editorConfig, name(), '', $xpath)"/>
      <xsl:with-param name="name" select="''"/>
      <xsl:with-param name="editInfo" select="*/gn:element"/>
      <xsl:with-param name="parentEditInfo" select="gn:element"/>
      <xsl:with-param name="isDisabled" select="true()"/>
    </xsl:call-template>

  </xsl:template>



  <!-- Set CRS system type before the CRS -->
  <xsl:template mode="mode-iso19115-3.2018"
                priority="2000"
                match="mrs:MD_ReferenceSystem">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>

    <xsl:variable name="attributes">
      <!-- Create form for all existing attribute (not in gn namespace)
      and all non existing attributes not already present. -->
      <xsl:apply-templates mode="render-for-field-for-attribute"
                           select="
        @*|
        gn:attribute[not(@name = parent::node()/@*/name())]">
        <xsl:with-param name="ref" select="gn:element/@ref"/>
        <xsl:with-param name="insertRef" select="gn:element/@ref"/>
      </xsl:apply-templates>
    </xsl:variable>

    <xsl:variable name="errors">
      <xsl:if test="$showValidationErrors">
        <xsl:call-template name="get-errors"/>
      </xsl:if>
    </xsl:variable>

    <xsl:call-template name="render-boxed-element">
      <xsl:with-param name="label"
                      select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)/label"/>
      <xsl:with-param name="editInfo" select="gn:element"/>
      <xsl:with-param name="errors" select="$errors"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="attributesSnippet" select="$attributes"/>
      <xsl:with-param name="subTreeSnippet">

        <xsl:apply-templates mode="mode-iso19115-3.2018"
                             select="mrs:referenceSystemType"/>

        <xsl:apply-templates mode="mode-iso19115-3.2018"
                             select="gn:*[@name = 'referenceSystemType']"/>

        <xsl:apply-templates mode="mode-iso19115-3.2018"
                             select="mrs:referenceSystemIdentifier"/>

        <xsl:apply-templates mode="mode-iso19115-3.2018"
                             select="gn:*[@name = 'referenceSystemIdentifier']"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>



  <!-- Measure elements, gco:Distance, gco:Angle, gco:Scale, gco:Length, ... -->
  <xsl:template mode="mode-iso19115-3.2018" priority="2000" match="mri:*[gco:*/@uom]">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>
    <xsl:param name="refToDelete" select="''" required="no"/>
    <xsl:param name="overrideLabel" select="''" required="no"/>

    <!--Avoid CHOICEELEMENT level for parent name -->
    <xsl:variable name="labelConfig"
                  select="gn-fn-metadata:getLabel($schema, name(), $labels,
                              name(ancestor::mri:*[not(contains(name(), 'CHOICE_ELEMENT'))][1]),
                              '', '')"/>
    <xsl:variable name="labelMeasureType"
                  select="gn-fn-metadata:getLabel($schema, name(gco:*), $labels, name(), '', '')"/>
    <xsl:variable name="isRequired" select="gn:element/@min = 1 and gn:element/@max = 1"/>

    <xsl:variable name="parentEditInfo" select="if ($refToDelete) then $refToDelete else gn:element"/>

    <div class="form-group gn-field gn-title {if ($isRequired) then 'gn-required' else ''}"
         id="gn-el-{*/gn:element/@ref}"
         data-gn-field-highlight="">
      <label class="col-sm-2 control-label">
        <xsl:value-of select="if ($overrideLabel) then $overrideLabel else $labelConfig/label"/>
        <xsl:if test="$overrideLabel = '' and
                      $labelMeasureType != '' and
                      $labelMeasureType/label != $labelConfig/label">&#10;
          (<xsl:value-of select="$labelMeasureType/label"/>)
        </xsl:if>
      </label>
      <div class="col-sm-9 gn-value">
        <xsl:variable name="elementRef"
                      select="gco:*/gn:element/@ref"/>
        <xsl:variable name="helper"
                      select="gn-fn-metadata:getHelper($labelConfig/helper, .)"/>
        <div data-gn-measure="{gco:*/text()}"
             data-uom="{gco:*/@uom}"
             data-ref="{concat('_', $elementRef)}">
        </div>

        <textarea id="_{$elementRef}_config" class="hidden">
          <xsl:copy-of select="java-xsl-util:xmlToJson(
              saxon:serialize($helper, 'default-serialize-mode'))"/></textarea>
      </div>
      <div class="col-sm-1 gn-control">
        <xsl:call-template name="render-form-field-control-remove">
          <xsl:with-param name="editInfo" select="*/gn:element"/>
          <xsl:with-param name="parentEditInfo" select="$parentEditInfo"/>
        </xsl:call-template>
      </div>
    </div>
  </xsl:template>

 <!-- <xsl:template mode="mode-iso19115-3.2018"
                match="cit:CI_Date"
                priority="2000">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>

    <div class="row">
      <div class="col-md-8">
        <xsl:apply-templates mode="mode-iso19115-3.2018" select="cit:date">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="labels" select="$labels"/>
        </xsl:apply-templates>
      </div>
      <div class="col-md-4">
        <xsl:apply-templates mode="mode-iso19115-3.2018" select="cit:dateType">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="labels" select="$labels"/>
        </xsl:apply-templates>
      </div>
    </div>
  </xsl:template>-->



  <!-- ===================================================================== -->
  <!-- gml:TimePeriod (format = %Y-%m-%dThh:mm:ss) -->
  <!-- ===================================================================== -->

  <xsl:template mode="mode-iso19115-3.2018" match="gml:beginPosition|gml:endPosition|gml:timePosition"
                priority="200">


    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="value" select="normalize-space(text()[1])"/>


    <xsl:variable name="attributes">
      <xsl:if test="$isEditing">
        <!-- Create form for all existing attribute (not in gn namespace)
        and all non existing attributes not already present. -->
        <xsl:apply-templates mode="render-for-field-for-attribute"
                             select="             @*|           gn:attribute[not(@name = parent::node()/@*/name())]">
          <xsl:with-param name="ref" select="gn:element/@ref"/>
          <xsl:with-param name="insertRef" select="gn:element/@ref"/>
        </xsl:apply-templates>
      </xsl:if>
    </xsl:variable>


    <xsl:call-template name="render-element">
      <xsl:with-param name="label"
                      select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), '', $xpath)/label"/>
      <xsl:with-param name="name" select="gn:element/@ref"/>
      <xsl:with-param name="value" select="text()"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="$xpath"/>
      <!--
          Default field type is Date.

          TODO : Add the capability to edit those elements as:
           * xs:time
           * xs:dateTime
           * xs:anyURI
           * xs:decimal
           * gml:CalDate
          See http://trac.osgeo.org/geonetwork/ticket/661
        -->
      <xsl:with-param name="type"
                      select="if (string-length($value) = 10 or $value = '') then 'date' else 'datetime'"/>
      <xsl:with-param name="editInfo" select="gn:element"/>
      <xsl:with-param name="attributesSnippet" select="$attributes"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="mode-iso19115-3.2018"
                match="gex:EX_GeographicBoundingBox"
                priority="2000">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>

    <xsl:call-template name="render-boxed-element">
      <xsl:with-param name="label"
        select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)/label"/>
      <xsl:with-param name="editInfo" select="gn:element"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="subTreeSnippet">
        <div gn-draw-bbox="" data-hleft="{gex:westBoundLongitude/gco:Decimal}"
          data-hright="{gex:eastBoundLongitude/gco:Decimal}" data-hbottom="{gex:southBoundLatitude/gco:Decimal}"
          data-htop="{gex:northBoundLatitude/gco:Decimal}" data-hleft-ref="_{gex:westBoundLongitude/gco:Decimal/gn:element/@ref}"
          data-hright-ref="_{gex:eastBoundLongitude/gco:Decimal/gn:element/@ref}"
          data-hbottom-ref="_{gex:southBoundLatitude/gco:Decimal/gn:element/@ref}"
          data-htop-ref="_{gex:northBoundLatitude/gco:Decimal/gn:element/@ref}"
          data-lang="lang"></div>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="mode-iso19115-3.2018"
                match="gex:EX_BoundingPolygon" priority="2000">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
    <xsl:variable name="labelConfig" select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)"/>

    <xsl:variable name="readonly" select="ancestor-or-self::node()[@xlink:href] != ''"/>

    <xsl:for-each select="gex:polygon">
      <xsl:call-template name="render-boxed-element">
        <xsl:with-param name="label"
                        select="$labelConfig/label"/>
        <xsl:with-param name="editInfo" select="../../gn:element"/>
        <xsl:with-param name="cls" select="local-name()"/>
        <xsl:with-param name="subTreeSnippet">

          <xsl:variable name="geometry">
            <xsl:apply-templates select="gml:MultiSurface|gml:LineString|gml:Point|gml:Polygon"
                                 mode="gn-element-cleaner"/>
          </xsl:variable>

          <xsl:variable name="identifier"
                        select="concat('_X', ./gn:element/@ref, '_replace')"/>

          <br />
          <gn-bounding-polygon polygon-xml="{saxon:serialize($geometry, 'default-serialize-mode')}"
                               identifier="{$identifier}"
                               read-only="{$readonly}">
          </gn-bounding-polygon>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <!-- Those elements MUST be ignored in the editor layout -->
  <xsl:template mode="mode-iso19115-3.2018"
                match="*[contains(name(), 'GROUP_ELEMENT')]"
                priority="2000">
    <xsl:apply-templates mode="mode-iso19115-3.2018" select="*"/>
  </xsl:template>


  <!--
    Display contact as table when mode is flat (eg. simple view) or if using xsl mode
    Match first node (or added one)
  -->
  <xsl:template mode="mode-iso19115-3.2018"
                match="*[
                        *[1]/name() = $editorConfig/editor/tableFields/table/@for and
                        (1 or @gn:addedObj = 'true') and
                        $isFlatMode]"
                priority="2000">
    <xsl:call-template name="iso19115-3.2018-table"/>
  </xsl:template>

  <!-- Ignore the following -->
  <xsl:template mode="mode-iso19115-3.2018"
                match="*[
                        *[1]/name() = $editorConfig/editor/tableFields/table/@for and
                        preceding-sibling::*[1]/name() = name() and
                        not(@gn:addedObj) and
                        $isFlatMode]"
                priority="2000"/>

  <!-- Define table layout -->
  <xsl:template name="iso19115-3.2018-table">
    <xsl:variable name="name" select="name()"/>
    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>

    <xsl:variable name="childName"
                  select="*[1]/name()"/>

    <xsl:variable name="isEmbeddedMode"
                  select="@gn:addedObj = 'true'"/>
    <xsl:variable name="isFirstOfItsKind"
                  select="preceding-sibling::*[1]/name() != $name"/>

    <xsl:variable name="tableConfig"
                  select="$editorConfig/editor/tableFields/table[@for = $childName]"/>

    <xsl:variable name="values">
      <xsl:if test="not($isEmbeddedMode) or ($isEmbeddedMode and $isFirstOfItsKind)">
        <header>
          <xsl:for-each select="$tableConfig/header/col">
            <col>
              <xsl:copy-of select="@*"/>
              <xsl:if test="@label">
                <!-- TODO: column names may comes from strings.xml -->
                <xsl:value-of select="gn-fn-metadata:getLabel($schema, @label, $labels, '', $isoType, $xpath)/label"/>
              </xsl:if>
            </col>
          </xsl:for-each>
        </header>
      </xsl:if>
      <xsl:for-each select="(.|following-sibling::*[name() = $name])/*[name() = $childName]">

        <xsl:variable name="base"
                      select="."/>
        <xsl:for-each select="$tableConfig/row">
          <row>
            <xsl:for-each select="col">
              <col>
                <xsl:if test="@use != ''">
                  <xsl:copy-of select="@use|directiveAttributes"/>
                </xsl:if>
                <xsl:if test="@del != ''">
                  <xsl:attribute name="remove" select="'true'"/>

                  <saxon:call-template name="{concat('evaluate-', $schema)}">
                    <xsl:with-param name="base" select="$base"/>
                    <xsl:with-param name="in"
                                    select="concat('/', @del, '/gn:element')"/>
                  </saxon:call-template>
                </xsl:if>

                <xsl:if test="@xpath != ''">
                  <saxon:call-template name="{concat('evaluate-', $schema)}">
                    <xsl:with-param name="base" select="$base"/>
                    <xsl:with-param name="in"
                                    select="concat('/', @xpath)"/>
                  </saxon:call-template>
                </xsl:if>
              </col>
            </xsl:for-each>
          </row>

          <xsl:for-each select="section[@xpath]">
            <row>
              <col colspan="{count(../col)}" type="form" withLabel="true">
                <xsl:apply-templates mode="form-builder" select=".">
                  <xsl:with-param name="base" select="$base"/>
                </xsl:apply-templates>
                <!--<xsl:variable name="nodes">

                <saxon:call-template name="{concat('evaluate-', $schema)}">
                  <xsl:with-param name="base" select="$base"/>
                  <xsl:with-param name="in"
                                  select="concat('/', @xpath)"/>
                </saxon:call-template>
                </xsl:variable>

                <xsl:for-each select="$nodes">
                  <saxon:call-template name="{concat('dispatch-', $schema)}">
                    <xsl:with-param name="base" select="."/>
                  </saxon:call-template>
                </xsl:for-each>

                <xsl:if test="@or and @in">
                  <saxon:call-template name="{concat('evaluate-', $schema)}">
                    <xsl:with-param name="base" select="$base"/>
                    <xsl:with-param name="in"
                                    select="concat('/../', @in, '/gn:child[@name=''', @or, ''']')"/>
                  </saxon:call-template>
                </xsl:if>-->
              </col>
            </row>
          </xsl:for-each>
        </xsl:for-each>
      </xsl:for-each>
    </xsl:variable>


    <!-- Return only the new row in embed mode. -->
    <xsl:choose>
      <xsl:when test="$tableConfig/@fieldset = 'false' or ($isEmbeddedMode and not($isFirstOfItsKind))">
        <xsl:call-template name="render-table">
          <xsl:with-param name="values" select="$values"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>

        <xsl:variable name="tableTitle" select="if (($tableConfig/@label) and (string($strings/*[name() = $tableConfig/@label])))
              then $strings/*[name() = $tableConfig/@label]
              else gn-fn-metadata:getLabel($schema, $name, $labels, name(..), $isoType, $xpath)/label" />

        <xsl:call-template name="render-boxed-element">
          <xsl:with-param name="label"
                          select="$tableTitle"/>
          <xsl:with-param name="cls" select="local-name()"/>
          <xsl:with-param name="subTreeSnippet">

            <xsl:call-template name="render-table">
              <xsl:with-param name="values" select="$values"/>
            </xsl:call-template>

          </xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
