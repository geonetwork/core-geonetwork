<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gts="http://www.isotc211.org/2005/gts"
  xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmx="http://www.isotc211.org/2005/gmx"
  xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
  xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
  xmlns:exslt="http://exslt.org/common" exclude-result-prefixes="#all">

  <xsl:include href="utility-fn.xsl"/>
  <xsl:include href="utility-tpl.xsl"/>

  <!-- Ignore all gn element -->
  <xsl:template mode="mode-iso19139" match="gn:*|@gn:*" priority="1000"/>

  <!-- Template to display non existing element ie. geonet:child element
	of the metadocument. -->
  <xsl:template mode="mode-iso19139" match="gn:child" priority="2000">
    <!-- TODO: this should be common to all schemas -->
    <xsl:if test="$isEditing and not($isFlatMode)">
      <xsl:call-template name="render-element-to-add">
        <xsl:with-param name="label" select="gn-fn-metadata:getLabel($schema, concat(@prefix, ':', @name), $labels)/label"/>
        <xsl:with-param name="childEditInfo" select="."/>
        <xsl:with-param name="parentEditInfo" select="../gn:element"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>


  <!-- Visit all XML tree recursively -->
  <xsl:template mode="mode-iso19139" match="gmd:*|gmx:*|gml:*|srv:*|gts:*">
    <xsl:apply-templates mode="mode-iso19139" select="*|@*"/>
  </xsl:template>

  <!-- Readonly elements -->
  <xsl:template mode="mode-iso19139" priority="200"
    match="gmd:fileIdentifier|gmd:dateStamp">

      <xsl:call-template name="render-element">
      <xsl:with-param name="label" select="gn-fn-metadata:getLabel($schema, name(), $labels)/label"/>
      <xsl:with-param name="value" select="*"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="gn-fn-metadata:getXPath(.)"/>
      <xsl:with-param name="type" select="gn-fn-iso19139:getFieldType(name(), '')"/>
      <xsl:with-param name="name" select="''"/>
      <xsl:with-param name="editInfo" select="*/gn:element"/>
      <xsl:with-param name="parentEditInfo" select="gn:element"/>
      <xsl:with-param name="isDisabled" select="true()"/>
    </xsl:call-template>
    
  </xsl:template>
    
  <!-- Boxed element -->
  <xsl:template mode="mode-iso19139" priority="200"
    match="gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']|
		gmd:identificationInfo|
		gmd:distributionInfo|
		gmd:portrayalCatalogueInfo|
		gmd:portrayalCatalogueCitation|
		gmd:descriptiveKeywords|
		gmd:thesaurusName|
		*[name(..)='gmd:resourceConstraints']|
		gmd:spatialRepresentationInfo|
		gmd:pointOfContact|
		gmd:contact|
		gmd:dataQualityInfo|
		gmd:contentInfo|
		gmd:distributionFormat|
		gmd:referenceSystemInfo|
		gmd:spatialResolution|
		gmd:offLine|
		gmd:onLine|
		gmd:address|
		gmd:projection|
		gmd:ellipsoid|
		gmd:extent[name(..)!='gmd:EX_TemporalExtent']|
		gmd:attributes|
		gmd:verticalCRS|
		gmd:geographicBox|
		gmd:EX_TemporalExtent|
		gmd:MD_Distributor|
		srv:containsOperations|
		srv:SV_CoupledResource|
		gmd:metadataConstraints|
		gmd:aggregationInfo|
		gmd:report/*|
		gmd:result/*|
		gmd:processStep|
		gmd:lineage">


    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
    
    <xsl:call-template name="render-boxed-element">
      <xsl:with-param name="label" select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)/label"/>
      <xsl:with-param name="editInfo" select="gn:element"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="subTreeSnippet">
        <!-- Process child of those element -->
        <xsl:apply-templates mode="mode-iso19139" select="*"/>
      </xsl:with-param>
    </xsl:call-template>

  </xsl:template>



  <!-- Render simple element which usually match a form field -->
  <xsl:template mode="mode-iso19139" priority="100"
    match="*[gco:CharacterString|gco:Date|gco:DateTime|gco:Integer|gco:Decimal|
		gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|gco:Angle|
		gco:Scale|gco:RecordType|gmx:MimeFileType|gmd:URL]">

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
    <xsl:variable name="labelConfig" select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)"/>

      <xsl:call-template name="render-element">
      <xsl:with-param name="label" select="$labelConfig/label"/>
      <xsl:with-param name="value" select="*"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <!--<xsl:with-param name="widget"/>
			<xsl:with-param name="widgetParams"/>-->
      <xsl:with-param name="xpath" select="$xpath"/>
      <!--<xsl:with-param name="attributesSnippet" as="node()"/>-->
        <xsl:with-param name="type" select="gn-fn-iso19139:getFieldType(name(), 
            name(gco:CharacterString|gco:Date|gco:DateTime|gco:Integer|gco:Decimal|
                gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|gco:Angle|
                gco:Scale|gco:RecordType|gmx:MimeFileType|gmd:URL))"/>
      <xsl:with-param name="name" select="if ($isEditing) then */gn:element/@ref else ''"/>
      <xsl:with-param name="editInfo" select="*/gn:element"/>
      <xsl:with-param name="parentEditInfo" select="gn:element"/>
      <!-- TODO: Handle conditional helper -->
      <xsl:with-param name="listOfValues" select="$labelConfig/helper"/>
    </xsl:call-template>

  </xsl:template>



  <!-- Match codelist values.
  
  eg. 
  <gmd:CI_RoleCode codeList="./resources/codeList.xml#CI_RoleCode" codeListValue="pointOfContact">
    <geonet:element ref="42" parent="41" uuid="gmd:CI_RoleCode_e75c8ec6-b994-4e98-b7c8-ecb48bda3725" min="1" max="1"/>
    <geonet:attribute name="codeList"/>
    <geonet:attribute name="codeListValue"/>
    <geonet:attribute name="codeSpace" add="true"/>
  
  -->
  <xsl:template mode="mode-iso19139" priority="100" match="gmd:*[*/@codeList]|srv:*[*/@codeList]">

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>

      <xsl:call-template name="render-element">
       <xsl:with-param name="label" select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)/label"/>
      <xsl:with-param name="value" select="*/@codeListValue"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <!--<xsl:with-param name="widget"/>
            <xsl:with-param name="widgetParams"/>-->
      <xsl:with-param name="xpath" select="$xpath"/>
      <!--<xsl:with-param name="attributesSnippet" as="node()"/>-->
      <xsl:with-param name="type" select="gn-fn-iso19139:getCodeListType(name())"/>
      <xsl:with-param name="name" select="if ($isEditing) then concat(*/gn:element/@ref, '_codeListValue') else ''"/>
      <xsl:with-param name="editInfo" select="*/gn:element"/>
      <xsl:with-param name="listOfValues" select="gn-fn-metadata:getCodeListValues($schema, name(*[@codeListValue]), $codelists)"/>
    </xsl:call-template>

  </xsl:template>

  <!-- Match enumeration
  
  eg. <gmd:topicCategory xmlns:gmd="http://www.isotc211.org/2005/gmd"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xmlns:gml="http://www.opengis.net/gml"
                   xmlns:gts="http://www.isotc211.org/2005/gts"
                   xmlns:gco="http://www.isotc211.org/2005/gco"
                   xmlns:geonet="http://www.fao.org/geonetwork"
                   geonet:addedObj="true"><gmd:MD_TopicCategoryCode><geonet:element ref="312" parent="311"
                      uuid="gmd:MD_TopicCategoryCode_ddf9f4dc-daac-49fc-b61f-53f1c13f1f3c"
                      min="1"
                      max="1"><geonet:text value="farming"/><geonet:text value="biota"/>
  -->
    <xsl:template mode="mode-iso19139" priority="100" match="gmd:topicCategory">
        
        <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
        <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
        
        <xsl:call-template name="render-element">
            <xsl:with-param name="label" select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)/label"/>
            <xsl:with-param name="value" select="gmd:*/text()"/><!-- should only match on child -->
            <xsl:with-param name="cls" select="local-name()"/>
            <xsl:with-param name="xpath" select="$xpath"/>
            <xsl:with-param name="type" select="gn-fn-iso19139:getCodeListType(name())"/>
            <xsl:with-param name="editInfo" select="*/gn:element"/>
            <xsl:with-param name="listOfValues" select="gn-fn-metadata:getCodeListValues($schema, name(gmd:*), $codelists)"/>
        </xsl:call-template>
        
    </xsl:template>

</xsl:stylesheet>
