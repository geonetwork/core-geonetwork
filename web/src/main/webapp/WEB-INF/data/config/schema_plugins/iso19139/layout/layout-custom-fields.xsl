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


  <!-- Readonly elements -->
  <xsl:template mode="mode-iso19139" priority="200" match="gmd:fileIdentifier|gmd:dateStamp">
    
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
      <xsl:with-param name="label"
        select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)/label"/>
      <xsl:with-param name="value" select="gmd:*/text()"/>
      <!-- should only match on child -->
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="type" select="gn-fn-iso19139:getCodeListType(name())"/>
      <xsl:with-param name="editInfo" select="*/gn:element"/>
      <xsl:with-param name="listOfValues"
        select="gn-fn-metadata:getCodeListValues($schema, name(gmd:*), $codelists)"/>
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
    </xsl:call-template>

  </xsl:template>



  <xsl:template mode="mode-iso19139" match="gmd:parentIdentifier" priority="200">

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>

    <xsl:call-template name="render-element">
      <xsl:with-param name="label"
        select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)/label"/>
      <xsl:with-param name="value" select="."/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="directive" select="'gn-field-cherry-pick-uuid'"/>
      <xsl:with-param name="editInfo" select="gco:CharacterString/gn:element"/>
    </xsl:call-template>

  </xsl:template>

  <!-- ===================================================================== -->
  <!-- gml:TimePeriod (format = %Y-%m-%dThh:mm:ss) -->
  <!-- ===================================================================== -->
  
  <xsl:template mode="mode-iso19139" match="gml:beginPosition|gml:endPosition|gml:timePosition" priority="200">
    
    
      <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
      <xsl:variable name="value" select="normalize-space(text())"/>
      
      
      <xsl:variable name="attributes">
        <xsl:if test="$isEditing">
          <!-- Create form for all existing attribute (not in gn namespace)
        and all non existing attributes not already present. -->
          <xsl:apply-templates mode="render-for-field-for-attribute" 
            select="
            @*|
            gn:attribute[not(@name = parent::node()/@*/name())]">
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
        <xsl:with-param name="type" select="if (string-length($value) = 10 or $value = '') then 'date' else 'datetime'"/>
        <xsl:with-param name="editInfo" select="gn:element"/>
        <xsl:with-param name="attributesSnippet" select="$attributes"/>
      </xsl:call-template>
  </xsl:template>
  
  <xsl:template mode="mode-iso19139" match="gmd:MD_Keywords" priority="2000">
    <!-- TODO: never provide widget editor if thesaurus not available in the catalog -->
    
    <!-- Create a div which contains the JSON configuration 
    * thesaurus: thesaurus to use
    * keywords: list of keywords in the element
    * transformations: list of transformations
    * transformation: current transformation
    -->
    
    <!-- Single quote are escaped inside keyword. 
    TODO: support multilingual editing of keywords
    -->
    <xsl:variable name="keywords" select="string-join(gmd:keyword/*[1], ',')"/>
    
    <!-- Define the list of transformation mode available. -->
    <xsl:variable name="transformations">'to-iso19139-keyword', 'to-iso19139-keyword-with-anchor', 'to-iso19139-keyword-as-xlink'</xsl:variable>
    
    <!-- Get current transformation mode based on XML fragement analysis -->
    <xsl:variable name="transformation" select="if (count(gmd:keyword/gmx:Anchor) > 0) 
      then 'to-iso19139-keyword-with-anchor' 
      else if (@xlink:href) then 'to-iso19139-keyword-as-xlink' 
      else 'to-iso19139-keyword'"/>
    
    <xsl:variable name="parentName" select="name(..)"/>
    
    <!-- Create custom widget: 
      * '' for item selector, 
      * 'combo' for simple combo, 
      * 'list' for selection list, 
      * 'multiplelist' for multiple selection list
      -->
    <xsl:variable name="widgetMode" select="''"/>
    
    
    
    <xsl:variable name="thesaurusName" select="gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
    
    <!-- The thesaurus key may be contained in the MD_Identifier field or 
    get it from the list of thesaurus based on its title.
    -->
    <xsl:variable name="thesaurusKey" select="if (gmd:thesaurusName/gmd:CI_Citation/
      gmd:identifier/gmd:MD_Identifier/gmd:code) 
      then gmd:thesaurusName/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code 
      else /root/gui/thesaurus/thesauri/thesaurus[title=$thesaurusName]/key"/>
    
    <div data-gn-keyword-selector="{$widgetMode}"
      data-element-ref="{concat('_X', ../gn:element/@ref)}"
      data-thesaurus-name="{$thesaurusName}"
      data-thesaurus-key="{substring-after($thesaurusKey, 'geonetwork.thesaurus.')}"
      data-keywords="{$keywords}"
      data-transformations="{$transformations}"
      data-current-transformation="{$transformation}"
      ></div>
  </xsl:template>
  
</xsl:stylesheet>
