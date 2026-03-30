<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
  xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
  xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
  xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
  xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
  xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
  xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
  xmlns:gml="http://www.opengis.net/gml/3.2"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
  xmlns:java-xsl-util="java:org.fao.geonet.util.XslUtil"
  xmlns:saxon="http://saxon.sf.net/"
  extension-element-prefixes="saxon"
  exclude-result-prefixes="#all">


  <!-- Readonly elements -->
  <xsl:template mode="mode-iso19115-3.2018"
                match="mdb:metadataIdentifier/mcc:MD_Identifier/mcc:code|
                             mdb:metadataLinkage/*[cit:function/*/@codeListValue = 'completeMetadata']/cit:linkage|
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


  <xsl:template mode="mode-iso19115-3.2018"
                match="@uuidref" priority="2000">
    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>

    <xsl:call-template name="render-element">
      <xsl:with-param name="label"
                      select="gn-fn-metadata:getLabel($schema, name(..), $labels)"/>
      <xsl:with-param name="value" select="."/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="gn-fn-metadata:getXPath(.)"/>
      <xsl:with-param name="type" select="gn-fn-metadata:getFieldType($editorConfig, name(), '', $xpath)"/>
      <xsl:with-param name="name" select="''"/>
      <xsl:with-param name="editInfo" select="../gn:element"/>
      <xsl:with-param name="parentEditInfo" select="../gn:element"/>
      <xsl:with-param name="isDisabled" select="true()"/>
    </xsl:call-template>
  </xsl:template>



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
      <xsl:with-param name="editInfo" select="../gn:element"/>
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
    <xsl:call-template name="build-table">
      <xsl:with-param name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
    </xsl:call-template>
  </xsl:template>

  <!-- Ignore the following -->
  <xsl:template mode="mode-iso19115-3.2018"
                match="*[
                        *[1]/name() = $editorConfig/editor/tableFields/table/@for and
                        preceding-sibling::*[1]/name() = name() and
                        not(@gn:addedObj) and
                        $isFlatMode]"
                priority="2000"/>


</xsl:stylesheet>
