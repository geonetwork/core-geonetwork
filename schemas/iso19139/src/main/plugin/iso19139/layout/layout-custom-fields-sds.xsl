<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
                version="2.0"
                exclude-result-prefixes="#all">


  <!-- SDS: Category. Render an anchor as select box populating it with a codelist -->
  <xsl:template mode="mode-iso19139" priority="2000"
                match="gmd:dataQualityInfo/*/gmd:report/gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult/gmd:specification/gmd:CI_Citation/gmd:title/gmx:Anchor[$tab='inspire_sds']">
    <xsl:variable name="labelConfig">
      <label><xsl:value-of select="$strings/sds-category"/></label>
    </xsl:variable>
    <xsl:call-template name="render-element">
      <xsl:with-param name="label" select="$labelConfig"/>
      <xsl:with-param name="value" select="@xlink:href"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="gn-fn-metadata:getXPath(.)"/>
      <xsl:with-param name="type" select="'select'"/>
      <xsl:with-param name="listOfValues"
                      select="gn-fn-metadata:getCodeListValues($schema, 'SDS_category', $codelists, .)"/>
      <xsl:with-param name="name" select="concat(gn:element/@ref,'_xlinkCOLONhref')"/>
      <xsl:with-param name="editInfo" select="*/gn:element"/>
      <xsl:with-param name="parentEditInfo" select="gn:element"/>
      <xsl:with-param name="isDisabled" select="false()"/>
    </xsl:call-template>

    <script>
      $( document ).ready(function(){
      var id = <xsl:value-of select="gn:element/@ref"/>;
      var gnselect = "_" + id + "_xlinkCOLONhref";
      var gnhidden = "#_" + id;
      if(!$(gnhidden).value){
      getHiddenFiekdContent(gnselect, gnhidden);
      }
      $("select[name=" + gnselect + "]").on('change', function() {
      getHiddenFiekdContent(gnselect, gnhidden);
      })
      });
      function getHiddenFiekdContent(gnselect, gnhidden){
      var value = $("select[name=" + gnselect + "]").val();
      var lastindexof = value.lastIndexOf("/")+1;
      $(gnhidden).val(value.substring(lastindexof));
      }
    </script>

    <input type="hidden">
      <xsl:attribute name="name" select="concat('_',gn:element/@ref)"/>
      <xsl:attribute name="id" select="concat('_',gn:element/@ref)"/>
    </input>

  </xsl:template>

  <!-- SDS: CRS -->
  <xsl:template mode="mode-iso19139" priority="2002"
                match="gmd:MD_Metadata/gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code/gmx:Anchor[$tab='inspire_sds']">
    <xsl:variable name="labelConfig">
      <label></label>
    </xsl:variable>
    <xsl:call-template name="render-element">
      <xsl:with-param name="label" select="$labelConfig"/>
      <xsl:with-param name="value" select="@xlink:title"/>
      <xsl:with-param name="name" select="gn:element/@ref"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="editInfo" select="../../../gn:element"/>
      <xsl:with-param name="isDisabled" select="false()"/>
    </xsl:call-template>
  </xsl:template>

  <!-- SDS: Quality of Service-->
  <xsl:template mode="mode-iso19139" priority="2002"
                match="gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_ConceptualConsistency[/root/gui/currTab/text()='inspire_sds']">
    <xsl:call-template name="render-boxed-element">
      <xsl:with-param name="label"
                      select="concat($strings/qos_measure, gmd:nameOfMeasure/gmx:Anchor/text())"/>
      <xsl:with-param name="editInfo" select="gn:element"/>
      <xsl:with-param name="subTreeSnippet">
        <xsl:if test="gmd:result/gmd:DQ_QuantitativeResult/gmd:valueUnit/@xlink:href != ''">
          <xsl:variable name="labelConfig">
            <label><xsl:value-of select="$strings/qos_uom"/></label>
          </xsl:variable>
          <xsl:call-template name="render-element">
            <xsl:with-param name="label" select="$labelConfig"/>
            <xsl:with-param name="value"
                            select="gmd:result/gmd:DQ_QuantitativeResult/gmd:valueUnit/@xlink:href"/>
            <xsl:with-param name="cls" select="local-name()"/>
            <xsl:with-param name="editInfo" select="gn:element"/>
            <xsl:with-param name="isDisabled" select="true()"/>
          </xsl:call-template>
        </xsl:if>

        <xsl:variable name="labelConfig">
          <label><xsl:value-of select="$strings/qos_value"/></label>
        </xsl:variable>

        <xsl:call-template name="render-element">
          <xsl:with-param name="label" select="$labelConfig"/>
          <xsl:with-param name="value"
                          select="gmd:result/gmd:DQ_QuantitativeResult/gmd:value/gco:Record/text()"/>
          <xsl:with-param name="name"
                          select="gmd:result/gmd:DQ_QuantitativeResult/gmd:value/gco:Record/gn:element/@ref"/>
          <xsl:with-param name="cls" select="local-name()"/>
          <xsl:with-param name="editInfo" select="gn:element"/>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- SDS: Render Constraints anchors -->
  <xsl:template mode="mode-iso19139"
                match="srv:SV_ServiceIdentification[$tab='inspire_sds']/gmd:resourceConstraints[gmd:MD_LegalConstraints/gmd:otherConstraints/gmx:Anchor]"
                priority="2000">

    <xsl:variable name="anchor" select="./gmd:MD_LegalConstraints/gmd:otherConstraints/gmx:Anchor"/>
    <xsl:variable name="accessCode"
                  select="substring-after($anchor/@xlink:href, 'ConditionsApplyingToAccessAndUse/')"/>

    <xsl:variable name="labelConfig">
      <label><xsl:value-of select="$strings/sds-limitation"/></label>
    </xsl:variable>
    <xsl:call-template name="render-element">
      <xsl:with-param name="label" select="$labelConfig"/>
      <xsl:with-param name="value" select="$strings/sds/*[name()=$accessCode]"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="gn-fn-metadata:getXPath(.)"/>
      <xsl:with-param name="name" select="concat(gn:element/@ref,'_xlinkCOLONhref')"/>
      <xsl:with-param name="editInfo" select="*/gn:element"/>
      <xsl:with-param name="parentEditInfo" select="gn:element"/>
      <xsl:with-param name="isDisabled" select="false()"/>
      <xsl:with-param name="isReadOnly" select="true()"/>
    </xsl:call-template>
  </xsl:template>

  <!-- SDS: DCP codelist -->

  <!--<xsl:template mode="mode-iso19139" priority="200" match="*[*/@codeList]">  -->
  <xsl:template mode="mode-iso19139" priority="201"
                match="srv:SV_OperationMetadata/srv:DCP[$tab='inspire_sds']">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>
    <xsl:param name="codelists" select="$iso19139codelists" required="no"/>
    <xsl:param name="overrideLabel" select="''" required="no"/>

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
    <xsl:variable name="elementName" select="name()"/>
    <xsl:variable name="labelConfig">
      <label><xsl:value-of select="if ($overrideLabel != '') then $overrideLabel else gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)/label"/></label>
    </xsl:variable>
    <xsl:call-template name="render-element">
      <xsl:with-param name="label"
                      select="$labelConfig"/>
      <xsl:with-param name="value" select="*/@codeListValue"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="type" select="gn-fn-iso19139:getCodeListType(name())"/>
      <xsl:with-param name="name"
                      select="if ($isEditing) then concat(*/gn:element/@ref, '_codeListValue') else ''"/>
      <xsl:with-param name="editInfo" select="*/gn:element"/>
      <xsl:with-param name="parentEditInfo" select="gn:element"/>
      <xsl:with-param name="listOfValues"
                      select="gn-fn-metadata:getCodeListValues($schema, 'SDS_DCP', $codelists, .)"/>
      <xsl:with-param name="isFirst"
                      select="count(preceding-sibling::*[name() = $elementName]) = 0"/>
    </xsl:call-template>

  </xsl:template>


</xsl:stylesheet>
