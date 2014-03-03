<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gts="http://www.isotc211.org/2005/gts"
  xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmx="http://www.isotc211.org/2005/gmx"
  xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:gn-fn-core="http://geonetwork-opensource.org/xsl/functions/core"
  xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
  xmlns:gn-fn-iso19115="http://geonetwork-opensource.org/xsl/functions/profiles/iso19115"
  xmlns:exslt="http://exslt.org/common" exclude-result-prefixes="#all">

  <xsl:include href="utility-fn.xsl"/>
  <xsl:include href="utility-tpl.xsl"/>

  <!-- Ignore all gn element -->
  <xsl:template mode="mode-iso19115" match="gn:*|@gn:*" priority="1000"/>


  <!-- Template to display non existing element ie. geonet:child element
	of the metadocument. Display in editing mode only and if 
  the editor mode is not flat mode. -->
  <xsl:template mode="mode-iso19115" match="gn:child" priority="2000">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>

    <!-- TODO: this should be common to all schemas -->
    <xsl:if test="$isEditing and 
      not($isFlatMode)">
      <xsl:call-template name="render-element-to-add">
        <xsl:with-param name="label" select="gn-fn-metadata:getLabel($schema, @name, $labels)/label"/>
        <xsl:with-param name="childEditInfo" select="."/>
        <xsl:with-param name="parentEditInfo" select="../gn:element"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>


  <!-- Visit all XML tree recursively -->
  <xsl:template mode="mode-iso19115" match="*">
    <xsl:apply-templates mode="mode-iso19115" select="*|@*"/> 
  </xsl:template>

  <!-- Boxed element -->
  <xsl:template mode="mode-iso19115" priority="200"
    match="mdContact|dataIdInfo|distInfo|graphOver|descKeys|spatRepInfo|idPoC|
        onLineSrc|dqInfo|refSysInfo|equScale|projection|ellipsoid|dataExt|geoBox|distributor|
		*[namespace-uri(.) != $gnUri and $isFlatMode = false() and *]">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>


    <xsl:call-template name="render-boxed-element">
      <xsl:with-param name="label"
        select="gn-fn-metadata:getLabel($schema, name(), $labels)/label"/>
      <xsl:with-param name="editInfo" select="gn:element"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="subTreeSnippet">
        <xsl:apply-templates mode="mode-iso19115" select="*"/>
      </xsl:with-param>
    </xsl:call-template>

  </xsl:template>



  <!-- Render simple element which usually match a form field -->
  <xsl:template mode="mode-iso19115" priority="200" match="*[text() != '']">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
    <xsl:variable name="labelConfig"
      select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)"/>
    <xsl:variable name="helper" select="gn-fn-metadata:getHelper($labelConfig/helper, .)"/>
    
    <xsl:message>## match node</xsl:message>
    <xsl:message><xsl:copy-of select="."/></xsl:message>
    <xsl:message><xsl:copy-of select="*/@value"/></xsl:message>
    

    <xsl:call-template name="render-element">
      <xsl:with-param name="label" select="$labelConfig/label"/>
      <xsl:with-param name="value" select="text()"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="type" select="'text'"/>
      <xsl:with-param name="name" select="if ($isEditing) then gn:element/@ref else ''"/>
      <xsl:with-param name="editInfo" select="gn:element"/>
    </xsl:call-template>

  </xsl:template>


  <!-- Match codelist values.
  
  -->
  <xsl:template mode="mode-iso19115" priority="400"
    match="*[count(*[ends-with(name(), 'Cd')]) = 1]">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>
    <xsl:param name="codelists" select="$codelists" required="no"/>
    
    <xsl:message>## match codelist</xsl:message>
    <xsl:message><xsl:copy-of select="."/></xsl:message>
    <xsl:message><xsl:copy-of select="*/@value"/></xsl:message>


    <xsl:call-template name="render-element">
      <xsl:with-param name="label"
        select="gn-fn-metadata:getLabel($schema, name(), $labels)/label"/>
      <xsl:with-param name="value" select="*/@value"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="type" select="'select'"/>
      <xsl:with-param name="name"
        select="if ($isEditing) then concat(*/gn:element/@ref, '_value') else ''"/>
      <xsl:with-param name="editInfo" select="*/gn:element"/>
      <xsl:with-param name="listOfValues"
        select="gn-fn-metadata:getCodeListValues($schema, name(*[@codeListValue]), $codelists)"/>
    </xsl:call-template>
  </xsl:template>


  <!-- Readonly elements -->
  <xsl:template mode="mode-iso19139" priority="200" match="mdDateSt|mdStanName|mdStanVer|mdFileID">

    <xsl:call-template name="render-element">
      <xsl:with-param name="label" select="gn-fn-metadata:getLabel($schema, name(), $labels)/label"/>
      <xsl:with-param name="value" select="*"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="gn-fn-metadata:getXPath(.)"/>
      <xsl:with-param name="type" select="'text'"/>
      <xsl:with-param name="name" select="''"/>
      <xsl:with-param name="editInfo" select="*/gn:element"/>
      <xsl:with-param name="parentEditInfo" select="gn:element"/>
      <xsl:with-param name="isDisabled" select="true()"/>
    </xsl:call-template>

  </xsl:template>



</xsl:stylesheet>
