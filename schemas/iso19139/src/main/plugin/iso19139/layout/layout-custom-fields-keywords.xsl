<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:xslutil="java:org.fao.geonet.util.XslUtil"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                exclude-result-prefixes="#all">

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
    <xsl:variable name="thesaurusTitleEl" select="gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title"/>
    <xsl:variable name="thesaurusTitle">
      <xsl:choose>
        <xsl:when test="normalize-space($thesaurusTitleEl/gco:CharacterString) != ''">
          <xsl:value-of select="normalize-space($thesaurusTitleEl/gco:CharacterString)"/>
        </xsl:when>
        <xsl:when test="normalize-space($thesaurusTitleEl/gmd:PT_FreeText/
                          gmd:textGroup/gmd:LocalisedCharacterString[
                            @locale = concat('#', upper-case(xslutil:twoCharLangCode($lang)))][1]) != ''">
          <xsl:value-of select="$thesaurusTitleEl/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale = concat('#', upper-case(xslutil:twoCharLangCode($lang)))][1]"/>
        </xsl:when>
        <xsl:when test="$thesaurusTitleEl/gmd:PT_FreeText/
                          gmd:textGroup/gmd:LocalisedCharacterString[
                            normalize-space(text()) != ''][1]">
          <xsl:value-of select="$thesaurusTitleEl/gmd:PT_FreeText/gmd:textGroup/
                                  gmd:LocalisedCharacterString[normalize-space(text()) != ''][1]"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="gmd:MD_Keywords/gmd:thesaurusName/
                                  gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
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


    <xsl:variable name="thesaurusIdentifier"
                  select="gmd:thesaurusName/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code"/>

    <xsl:variable name="thesaurusTitle"
        select="gmd:thesaurusName/gmd:CI_Citation/gmd:title/(gco:CharacterString|gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString)"/>


    <xsl:variable name="thesaurusConfig"
                  as="element()?"
                  select="($listOfThesaurus/thesaurus[title=$thesaurusTitle]|
                  $listOfThesaurus/thesaurus[key=substring-after($thesaurusIdentifier, 'geonetwork.thesaurus.')])[1]"/>

    <xsl:choose>
      <xsl:when test="$thesaurusConfig">

        <!-- The thesaurus key may be contained in the MD_Identifier field or 
          get it from the list of thesaurus based on its title.
          -->
        <xsl:variable name="thesaurusInternalKey"
          select="if ($thesaurusIdentifier)
          then $thesaurusIdentifier
          else thesaurusConfig/key"/>
        <xsl:variable name="thesaurusKey"
                      select="if (starts-with($thesaurusInternalKey, 'geonetwork.thesaurus.'))
                      then substring-after($thesaurusInternalKey, 'geonetwork.thesaurus.')
                      else $thesaurusInternalKey"/>

        <!-- if gui lang eng > #EN -->
        <xsl:variable name="guiLangId"
                      select="
                      if (count($metadata/gmd:locale/gmd:PT_Locale[gmd:languageCode/gmd:LanguageCode/@codeListValue = $lang]) = 1)
                        then $metadata/gmd:locale/gmd:PT_Locale[gmd:languageCode/gmd:LanguageCode/@codeListValue = $lang]/@id
                        else $metadata/gmd:locale/gmd:PT_Locale[gmd:languageCode/gmd:LanguageCode/@codeListValue = $metadataLanguage]/@id"/>

        <!--
        get keyword in gui lang
        in default language
        -->
        <xsl:variable name="keywords" select="string-join(
                  if ($guiLangId and gmd:keyword//*[@locale = concat('#', $guiLangId)]) then
                    gmd:keyword//*[@locale = concat('#', $guiLangId)]/replace(text(), ',', ',,')
                  else gmd:keyword/*[1]/replace(text(), ',', ',,'), ',')"/>

        <!-- Define the list of transformation mode available. -->
        <xsl:variable name="transformations"
                      as="xs:string"
                      select="if ($thesaurusConfig/@transformations != '')
                              then $thesaurusConfig/@transformations
                              else 'to-iso19139-keyword,to-iso19139-keyword-with-anchor,to-iso19139-keyword-as-xlink'"/>

        <!-- Get current transformation mode based on XML fragment analysis -->
        <xsl:variable name="transformation"
          select="if (parent::node()/@xlink:href) then 'to-iso19139-keyword-as-xlink'
          else if (count(gmd:keyword/gmx:Anchor) > 0)
          then 'to-iso19139-keyword-with-anchor' 
          else 'to-iso19139-keyword'"/>

        <xsl:variable name="parentName" select="name(..)"/>

        <!-- Create custom widget: 
              * '' for item selector, 
              * 'tagsinput' for tags
              * 'tagsinput' and maxTags = 1 for only one tag
              * 'multiplelist' for multiple selection list
        -->
        <xsl:variable name="widgetMode" select="'tagsinput'"/>
        <xsl:variable name="maxTags"
                      as="xs:string"
                      select="if ($thesaurusConfig/@maxtags)
                              then $thesaurusConfig/@maxtags
                              else ''"/>
        <!--
          Example: to restrict number of keyword to 1 for INSPIRE
          <xsl:variable name="maxTags" 
          select="if ($thesaurusKey = 'external.theme.inspire-theme') then '1' else ''"/>
        -->
        <!-- Create a div with the directive configuration
            * elementRef: the element ref to edit
            * elementName: the element name
            * thesaurusName: the thesaurus title to use
            * thesaurusKey: the thesaurus identifier
            * keywords: list of keywords in the element
            * transformations: list of transformations
            * transformation: current transformation
          -->

        <xsl:variable name="allLanguages" select="concat($metadataLanguage, ',', $metadataOtherLanguages)"></xsl:variable>
        <div data-gn-keyword-selector="{$widgetMode}"
          data-metadata-id="{$metadataId}"
          data-element-ref="{concat('_X', ../gn:element/@ref, '_replace')}"
          data-thesaurus-title="{$thesaurusTitle}"
          data-thesaurus-key="{$thesaurusKey}"
          data-keywords="{$keywords}" data-transformations="{$transformations}"
          data-current-transformation="{$transformation}"
          data-max-tags="{$maxTags}"
          data-lang="{$metadataOtherLanguagesAsJson}"
          data-textgroup-only="false">
        </div>

        <xsl:variable name="isTypePlace" select="count(gmd:type/gmd:MD_KeywordTypeCode[@codeListValue='place']) > 0"/>
        <xsl:if test="$isTypePlace">
          <xsl:call-template name="render-batch-process-button">
            <xsl:with-param name="process-name" select="'add-extent-from-geokeywords'"/>
            <xsl:with-param name="process-params">{"replace": true}</xsl:with-param>
          </xsl:call-template>
        </xsl:if>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="mode-iso19139" select="*"/>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

</xsl:stylesheet>
