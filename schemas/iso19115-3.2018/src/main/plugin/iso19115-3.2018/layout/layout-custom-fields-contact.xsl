<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                exclude-result-prefixes="#all">

  <!--
  ```
   <cit:phone>
      <cit:CI_Telephone>
         <cit:number>
            <gco:CharacterString>PHONE</gco:CharacterString>
         </cit:number>
         <cit:numberType>
            <cit:CI_TelephoneTypeCode codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_TelephoneTypeCode"
                                      codeListValue=""/>
         </cit:numberType>
      </cit:CI_Telephone>
   </cit:phone>
  ```

  -->
  <xsl:template mode="mode-iso19115-3.2018" match="*[cit:CI_Telephone]" priority="2000">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>

    <xsl:apply-templates mode="mode-iso19115-3.2018" select="*/cit:*">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="labels" select="$labels"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- Number type is handled in next template -->
  <xsl:template mode="mode-iso19115-3.2018" match="cit:numberType" priority="2000"/>

  <!-- Rendering number type as a dropdown -->
  <xsl:template mode="mode-iso19115-3.2018"
                priority="2000"
                match="cit:number[parent::node()/name() = 'cit:CI_Telephone']">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>

    <xsl:variable name="labelConfig"
                  select="gn-fn-metadata:getLabel($schema, name(), $labels)"/>

    <xsl:variable name="dateTypeElementRef"
                  select="../gn:element/@ref"/>

    <div class="form-group gn-field gn-title gn-required"
         id="gn-el-{$dateTypeElementRef}"
         data-gn-field-highlight="">
      <label class="col-sm-2 control-label">
        <xsl:value-of select="$labelConfig/label"/>
      </label>
      <div class="col-sm-3 gn-value">
        <xsl:variable name="codelist"
                      select="gn-fn-metadata:getCodeListValues($schema,
                                  'cit:CI_TelephoneTypeCode',
                                  $codelists,
                                  .)"/>
        <xsl:call-template name="render-codelist-as-select">
          <xsl:with-param name="listOfValues" select="$codelist"/>
          <xsl:with-param name="lang" select="$lang"/>
          <xsl:with-param name="isDisabled" select="ancestor-or-self::node()[@xlink:href]"/>
          <xsl:with-param name="elementRef" select="../cit:numberType/cit:CI_TelephoneTypeCode/gn:element/@ref"/>
          <xsl:with-param name="isRequired" select="true()"/>
          <xsl:with-param name="hidden" select="false()"/>
          <xsl:with-param name="valueToEdit" select="../cit:numberType/cit:CI_TelephoneTypeCode/@codeListValue"/>
          <xsl:with-param name="name" select="concat(../cit:numberType/cit:CI_TelephoneTypeCode/gn:element/@ref, '_codeListValue')"/>
        </xsl:call-template>


        <xsl:call-template name="render-form-field-control-move">
          <xsl:with-param name="elementEditInfo" select="../../gn:element"/>
          <xsl:with-param name="domeElementToMoveRef" select="$dateTypeElementRef"/>
        </xsl:call-template>
      </div>
      <div class="col-sm-6 gn-value">
        <!-- Phone number is not multilingual so display the input -->
        <input class="form-control"
               type="tel"
               name="{concat('_', gco:CharacterString/gn:element/@ref)}"
               value="{normalize-space(gco:CharacterString)}">
          <xsl:if test="ancestor-or-self::node()[@xlink:href]">
            <xsl:attribute name="disabled" select="'disabled'"/>
          </xsl:if>
        </input>


        <!-- Create form for all existing attribute (not in gn namespace)
         and all non existing attributes not already present. -->
        <div class="well well-sm gn-attr {if ($isDisplayingAttributes) then '' else 'hidden'}">
          <xsl:apply-templates mode="render-for-field-for-attribute"
                               select="
            ../../@*|
            ../../gn:attribute[not(@name = parent::node()/@*/name())]">
            <xsl:with-param name="ref" select="../../gn:element/@ref"/>
            <xsl:with-param name="insertRef" select="../gn:element/@ref"/>
          </xsl:apply-templates>
        </div>
      </div>
      <div class="col-sm-1 gn-control">
        <xsl:call-template name="render-form-field-control-remove">
          <xsl:with-param name="editInfo" select="../gn:element"/>
          <xsl:with-param name="parentEditInfo" select="../../gn:element"/>
        </xsl:call-template>
      </div>
    </div>
  </xsl:template>

</xsl:stylesheet>
