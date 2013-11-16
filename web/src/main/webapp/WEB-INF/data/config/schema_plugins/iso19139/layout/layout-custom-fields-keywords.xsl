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

  <!-- Custom rendering of keyword section 
    * gmd:descriptiveKeywords is boxed element and the title 
    of the fieldset is the thesaurus title
    * if the thesaurus is available in the catalog, display
    the advanced editor which provides easy selection of 
    keywords.
  
  -->


  <xsl:template mode="mode-iso19139" priority="2000" match="
    gmd:descriptiveKeywords">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
    <xsl:variable name="thesaurusTitle"
      select="gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString"/>


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




    <xsl:call-template name="render-boxed-element">
      <xsl:with-param name="label"
        select="if ($thesaurusTitle) 
                then $thesaurusTitle 
                else gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)/label"/>
      <xsl:with-param name="editInfo" select="gn:element"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="attributesSnippet" select="$attributes"/>
      <xsl:with-param name="subTreeSnippet">
        <xsl:apply-templates mode="mode-iso19139" select="*">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="labels" select="$labels"/>
        </xsl:apply-templates>
      </xsl:with-param>
    </xsl:call-template>

  </xsl:template>






  <xsl:template mode="mode-iso19139" match="gmd:MD_Keywords" priority="2000">


    <xsl:variable name="thesaurusTitle"
      select="gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString"/>

    <xsl:variable name="isTheaurusAvailable"
      select="count($listOfThesaurus/thesaurus[title=$thesaurusTitle]) > 0"/>
    <xsl:choose>
      <xsl:when test="$isTheaurusAvailable">

        <!-- The thesaurus key may be contained in the MD_Identifier field or 
          get it from the list of thesaurus based on its title.
          -->
        <xsl:variable name="thesaurusKey"
          select="if (gmd:thesaurusName/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code) 
          then gmd:thesaurusName/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code 
          else $listOfThesaurus/thesaurus[title=$thesaurusTitle]/key"/>



        <!-- Single quote are escaped inside keyword. 
          TODO: support multilingual editing of keywords
          -->
        <xsl:variable name="keywords" select="string-join(gmd:keyword/*[1], ',')"/>

        <!-- Define the list of transformation mode available. -->
        <xsl:variable name="transformations">'to-iso19139-keyword',
          'to-iso19139-keyword-with-anchor', 'to-iso19139-keyword-as-xlink'</xsl:variable>

        <!-- Get current transformation mode based on XML fragement analysis -->
        <xsl:variable name="transformation"
          select="if (count(gmd:keyword/gmx:Anchor) > 0) 
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

        <!-- Create a div with the directive configuration
            * widgetMod: the layout to use
            * elementRef: the element ref to edit
            * elementName: the element name
            * thesaurusName: the thesaurus title to use
            * thesaurusKey: the thesaurus identifier
            * keywords: list of keywords in the element
            * transformations: list of transformations
            * transformation: current transformation
          -->
        <div data-gn-keyword-selector="{$widgetMode}"
          data-element-ref="{concat('_X', ../gn:element/@ref)}"
          data-thesaurus-title="{$thesaurusTitle}"
          data-thesaurus-key="{substring-after($thesaurusKey, 'geonetwork.thesaurus.')}"
          data-keywords="{$keywords}" data-transformations="{$transformations}"
          data-current-transformation="{$transformation}">
        </div>

      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="mode-iso19139" select="*"/>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

</xsl:stylesheet>
