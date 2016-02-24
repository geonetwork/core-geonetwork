<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gts="http://www.isotc211.org/2005/gts"
  xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmx="http://www.isotc211.org/2005/gmx"
  xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:gn-fn-core="http://geonetwork-opensource.org/xsl/functions/core"
  xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
  xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
  xmlns:exslt="http://exslt.org/common" exclude-result-prefixes="#all">

  <xsl:include href="layout-custom-fields-keywords.xsl"/>
  
  <xsl:template mode="mode-iso19139" priority="2002" match="gmd:MD_Metadata/gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code/gmx:Anchor[/root/gui/currTab/text()='inspire_sds']">
    <xsl:call-template name="render-element">
      <xsl:with-param name="label" select="''" />
      <xsl:with-param name="value" select="@xlink:title" />
      <xsl:with-param name="name" select="gn:element/@ref" />
      <xsl:with-param name="cls" select="local-name()" />
      <xsl:with-param name="editInfo" select="../../../gn:element" />
      <xsl:with-param name="isDisabled" select="false()" />
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template mode="mode-iso19139" priority="2002" match="gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_ConceptualConsistency[/root/gui/currTab/text()='inspire_sds']">
    <xsl:call-template name="render-boxed-element">
      <xsl:with-param name="label" select="gmd:nameOfMeasure/gmx:Anchor/text()" />
      <xsl:with-param name="editInfo" select="gn:element" />
      <xsl:with-param name="subTreeSnippet">
        <xsl:call-template name="render-element">
          <xsl:with-param name="label" select="/root/gui/schemas/iso19139/strings/qos_description" />
          <xsl:with-param name="value" select="gmd:measureIdentification/gmd:MD_Identifier/gmd:code/gco:CharacterString/text()" />
          <xsl:with-param name="name" select="gmd:measureIdentification/gmd:MD_Identifier/gmd:code/gco:CharacterString/gn:element/@ref" />
          <xsl:with-param name="cls" select="local-name()" />
          <xsl:with-param name="editInfo" select="gmd:measureIdentification/gmd:MD_Identifier/gmd:code/gco:CharacterString/gn:element" />
        </xsl:call-template>
        <xsl:if test="gmd:result/gmd:DQ_QuantitativeResult/gmd:valueUnit/@xlink:href != ''">
          <xsl:call-template name="render-element">
            <xsl:with-param name="label" select="/root/gui/schemas/iso19139/strings/qos_uom" />
            <xsl:with-param name="value" select="gmd:result/gmd:DQ_QuantitativeResult/gmd:valueUnit/@xlink:href" />
            <xsl:with-param name="cls" select="local-name()" />
            <xsl:with-param name="editInfo" select="gn:element" />
            <xsl:with-param name="isDisabled" select="true()" />
          </xsl:call-template>
        </xsl:if>
        <xsl:call-template name="render-element">
          <xsl:with-param name="label" select="/root/gui/schemas/iso19139/strings/qos_value" />
          <xsl:with-param name="value" select="gmd:result/gmd:DQ_QuantitativeResult/gmd:value/gco:Record/text()" />
          <xsl:with-param name="name" select="gmd:result/gmd:DQ_QuantitativeResult/gmd:value/gco:Record/gn:element/@ref" />
          <xsl:with-param name="cls" select="local-name()" />
          <xsl:with-param name="editInfo" select="gn:element" />
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- SDS tool: Render an anchor as select box populating it with a codelist -->
  <xsl:template mode="mode-iso19139" priority="2000" match="gmd:dataQualityInfo/*/gmd:report/gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult/gmd:specification/gmd:CI_Citation/gmd:title/gmx:Anchor[/root/gui/currTab/text()='inspire_sds']">
    <xsl:call-template name="render-element">
      <xsl:with-param name="label" select="/root/gui/strings/category"/>
      <xsl:with-param name="value" select="@xlink:href"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="gn-fn-metadata:getXPath(.)"/>
      <xsl:with-param name="type" select="'select'"/>
      <xsl:with-param name="listOfValues" select="/root/gui/schemas/iso19139/codelists/codelist[@name='INSPIRE_category']"/>
      <xsl:with-param name="name" select="concat(gn:element/@ref,'_xlinkCOLONhref')"/>
      <xsl:with-param name="editInfo" select="*/gn:element"/>
      <xsl:with-param name="parentEditInfo" select="gn:element"/>
      <xsl:with-param name="isDisabled" select="false()"/>
    </xsl:call-template>
    
    <script>
        $( document ).ready(function(){
	        var id = <xsl:value-of select="gn:element/@ref" />;
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

  <!-- SDS: Render Constraints anchors -->

  <xsl:template mode="mode-iso19139"
                match="srv:SV_ServiceIdentification[/root/gui/currTab/text()='inspire_sds']/gmd:resourceConstraints[gmd:MD_LegalConstraints/gmd:otherConstraints/gmx:Anchor]"
                priority="2000">

     <xsl:variable name="anchor" select="./gmd:MD_LegalConstraints/gmd:otherConstraints/gmx:Anchor"/>
     <xsl:variable name="accessCode" select="substring-after($anchor/@xlink:href, 'ConditionsApplyingToAccessAndUse/')"/>

    <xsl:call-template name="render-element">
      <xsl:with-param name="label" select="/root/gui/schemas/iso19139/strings/sds-limitation"/>
      <xsl:with-param name="value" select="/root/gui/schemas/iso19139/strings/sds/*[name()=$accessCode]"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="gn-fn-metadata:getXPath(.)"/>
      <xsl:with-param name="name" select="concat(gn:element/@ref,'_xlinkCOLONhref')"/>
      <xsl:with-param name="editInfo" select="*/gn:element"/>
      <xsl:with-param name="parentEditInfo" select="gn:element"/>
      <xsl:with-param name="isDisabled" select="false()"/>
      <xsl:with-param name="isReadOnly" select="true()"/>
    </xsl:call-template>
  </xsl:template>


  <!-- Readonly elements -->
  <xsl:template mode="mode-iso19139" priority="2000" match="gmd:fileIdentifier|gmd:dateStamp">

    <xsl:call-template name="render-element">
      <xsl:with-param name="label" select="gn-fn-metadata:getLabel($schema, name(), $labels)/label"/>
      <xsl:with-param name="value" select="*"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="gn-fn-metadata:getXPath(.)"/>
      <xsl:with-param name="type" select="gn-fn-metadata:getFieldType($editorConfig, name(), '')"/>
      <xsl:with-param name="name" select="''"/>
      <xsl:with-param name="editInfo" select="*/gn:element"/>
      <xsl:with-param name="parentEditInfo" select="gn:element"/>
      <xsl:with-param name="isDisabled" select="true()"/>
    </xsl:call-template>

  </xsl:template>

  <!-- Duration
      
       xsd:duration elements use the following format:
       
       Format: PnYnMnDTnHnMnS
       
       *  P indicates the period (required)
       * nY indicates the number of years
       * nM indicates the number of months
       * nD indicates the number of days
       * T indicates the start of a time section (required if you are going to specify hours, minutes, or seconds)
       * nH indicates the number of hours
       * nM indicates the number of minutes
       * nS indicates the number of seconds
       
       A custom directive is created.
  -->
  <xsl:template mode="mode-iso19139" match="gts:TM_PeriodDuration|gml:duration" priority="200">

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>

    <xsl:call-template name="render-element">
      <xsl:with-param name="label"
        select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)/label"/>
      <xsl:with-param name="value" select="."/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="directive" select="'gn-field-duration'"/>
      <xsl:with-param name="editInfo" select="gn:element"/>
      <xsl:with-param name="parentEditInfo" select="../gn:element"/>
    </xsl:call-template>

  </xsl:template>

  <!-- ===================================================================== -->
  <!-- gml:TimePeriod (format = %Y-%m-%dThh:mm:ss) -->
  <!-- ===================================================================== -->

  <xsl:template mode="mode-iso19139" match="gml:beginPosition|gml:endPosition|gml:timePosition"
    priority="200">


    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="value" select="normalize-space(text())"/>


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
  
  <xsl:template mode="mode-iso19139" match="gmd:EX_GeographicBoundingBox" priority="2000">
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
        <div gn-draw-bbox="" data-hleft="{gmd:westBoundLongitude/gco:Decimal}"
          data-hright="{gmd:eastBoundLongitude/gco:Decimal}" data-hbottom="{gmd:southBoundLatitude/gco:Decimal}"
          data-htop="{gmd:northBoundLatitude/gco:Decimal}" data-hleft-ref="_{gmd:westBoundLongitude/gco:Decimal/gn:element/@ref}"
          data-hright-ref="_{gmd:eastBoundLongitude/gco:Decimal/gn:element/@ref}"
          data-hbottom-ref="_{gmd:southBoundLatitude/gco:Decimal/gn:element/@ref}"
          data-htop-ref="_{gmd:northBoundLatitude/gco:Decimal/gn:element/@ref}"
          data-lang="lang"></div>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
</xsl:stylesheet>
