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
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xslutil="java:org.fao.geonet.util.XslUtil"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                exclude-result-prefixes="#all">

  <!-- Custom rendering of keyword section
    * mri:descriptiveKeywords is boxed element and the title
    of the fieldset is the thesaurus title
    * if the thesaurus is available in the catalog, display
    the advanced editor which provides easy selection of
    keywords.
  -->
  <xsl:template mode="mode-iso19115-3.2018" priority="2000" match="
    mri:descriptiveKeywords">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>
    <xsl:param name="overrideLabel" select="''" required="no"/>

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
    <xsl:variable name="thesaurusTitleEl"
                  select="mri:MD_Keywords/mri:thesaurusName/*/cit:title"/>

    <!--TODO Add all Thesaurus as first block of keywords-->


    <xsl:variable name="thesaurusTitle">
      <xsl:choose>
        <xsl:when test="normalize-space($thesaurusTitleEl/gco:CharacterString) != ''">
          <xsl:value-of select="if ($overrideLabel != '')
              then $overrideLabel
              else concat(
                      $strings/keywordFrom,
                      normalize-space($thesaurusTitleEl/gco:CharacterString))"/>
        </xsl:when>
        <xsl:when test="normalize-space($thesaurusTitleEl/lan:PT_FreeText/
                          lan:textGroup/lan:LocalisedCharacterString[
                            @locale = concat('#', upper-case(xslutil:twoCharLangCode($lang)))][1]) != ''">
          <xsl:value-of
            select="$thesaurusTitleEl/lan:PT_FreeText/lan:textGroup/lan:LocalisedCharacterString[@locale = concat('#', upper-case(xslutil:twoCharLangCode($lang)))][1]"/>
        </xsl:when>
        <xsl:when test="$thesaurusTitleEl/lan:PT_FreeText/
                          lan:textGroup/lan:LocalisedCharacterString[
                            normalize-space(text()) != ''][1]">
          <xsl:value-of select="$thesaurusTitleEl/lan:PT_FreeText/lan:textGroup/
                                  lan:LocalisedCharacterString[normalize-space(text()) != ''][1]"/>
        </xsl:when>
        <xsl:when test="normalize-space($thesaurusTitleEl/gcx:Anchor) != ''">
          <xsl:value-of select="if ($overrideLabel != '')
              then $overrideLabel
              else concat(
                      $strings/keywordFrom,
                      normalize-space($thesaurusTitleEl/gcx:Anchor))"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="mri:MD_Keywords/mri:thesaurusName/*/cit:identifier/*/mcc:code"/>
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


    <xsl:variable name="thesaurusIdentifier"
                  select="normalize-space(*/mri:thesaurusName/*/cit:identifier/*/mcc:code/*/text())"/>

    <!-- Editor configuration can define to display thesaurus with or without fieldset -->
    <xsl:variable name="thesaurusConfig"
                  as="element()?"
                  select="if ($thesaurusList/thesaurus[@key = substring-after($thesaurusIdentifier, 'geonetwork.thesaurus.')])
                          then $thesaurusList/thesaurus[@key = substring-after($thesaurusIdentifier, 'geonetwork.thesaurus.')]
                          else $listOfThesaurus/thesaurus[title = $thesaurusTitle]"/>


    <xsl:choose>
      <xsl:when test="($isFlatMode and not($thesaurusConfig/@fieldset)) or $thesaurusConfig/@fieldset = 'false'">
        <xsl:apply-templates mode="mode-iso19115-3.2018" select="*">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="labels" select="$labels"/>
          <xsl:with-param name="overrideLabel" select="$overrideLabel"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="render-boxed-element">
          <xsl:with-param name="label"
            select="if ($thesaurusTitle != '')
                    then $thesaurusTitle
                    else gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)/label"/>
          <xsl:with-param name="editInfo" select="gn:element"/>
          <xsl:with-param name="cls" select="local-name()"/>
          <xsl:with-param name="xpath" select="$xpath"/>
          <xsl:with-param name="attributesSnippet" select="$attributes"/>
          <xsl:with-param name="subTreeSnippet">
            <xsl:apply-templates mode="mode-iso19115-3.2018" select="*">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="labels" select="$labels"/>
              <xsl:with-param name="overrideLabel" select="$overrideLabel"/>
            </xsl:apply-templates>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>


  <xsl:template mode="mode-iso19115-3.2018" match="mri:MD_Keywords" priority="2000">
    <xsl:param name="overrideLabel" select="''" required="no"/>

    <xsl:variable name="thesaurusIdentifier"
                  select="normalize-space(mri:thesaurusName/*/cit:identifier/*/mcc:code/*/text())"/>

    <xsl:variable name="thesaurusTitle"
                  select="if ($overrideLabel != '')
                          then $overrideLabel
                          else (mri:thesaurusName/*/cit:title/(gco:CharacterString|lan:PT_FreeText/lan:textGroup/lan:LocalisedCharacterString|gcx:Anchor))[1]"/>

    <!-- Check if thesaurus is defined in editor config or is available in the catalogue -->
    <xsl:variable name="thesaurusKey"
                  select="substring-after($thesaurusIdentifier, 'geonetwork.thesaurus.')"/>
    <xsl:variable name="thesaurusConfig"
                  as="element()?"
                  select="if ($thesaurusList/thesaurus[@key = $thesaurusKey])
                          then $thesaurusList/thesaurus[@key = $thesaurusKey]
                          else if ($listOfThesaurus/thesaurus[key = $thesaurusKey])
                          then $listOfThesaurus/thesaurus[key = $thesaurusKey]
                          else if ($listOfThesaurus/thesaurus[multilingualTitles/multilingualTitle/title = $thesaurusTitle])
                          then $listOfThesaurus/thesaurus[multilingualTitles/multilingualTitle/title = $thesaurusTitle]
                          else $listOfThesaurus/thesaurus[title = $thesaurusTitle]"/>
    <xsl:choose>
      <xsl:when test="$thesaurusConfig">

        <!-- The thesaurus key may be contained in the MD_Identifier field or
          get it from the list of thesaurus based on its title.
          -->
        <xsl:variable name="thesaurusInternalKey"
          select="if ($thesaurusIdentifier)
                  then $thesaurusIdentifier
                  else $thesaurusConfig/key"/>
        <xsl:variable name="thesaurusKey"
                      select="if (starts-with($thesaurusInternalKey, 'geonetwork.thesaurus.'))
                      then substring-after($thesaurusInternalKey, 'geonetwork.thesaurus.')
                      else $thesaurusInternalKey"/>

        <!-- if gui lang eng > #EN -->
        <xsl:variable name="guiLangId"
                      select="
                      if (count($metadata/mdb:otherLocale/lan:PT_Locale[lan:language/lan:LanguageCode/@codeListValue = $lang]) = 1)
                        then ($metadata/mdb:otherLocale/lan:PT_Locale[lan:language/lan:LanguageCode/@codeListValue = $lang]/@id)[1]
                        else ($metadata/mdb:otherLocale/lan:PT_Locale[lan:language/lan:LanguageCode/@codeListValue = $metadataLanguage]/@id)[1]"/>
        <!--
        get keyword in gui lang
        in default language
        -->
        <xsl:variable name="keywords" select="string-join(
                  if ($guiLangId and mri:keyword//*[@locale = concat('#', $guiLangId)])
                  then mri:keyword//*[@locale = concat('#', $guiLangId)][. != '']/replace(text(), ',', ',,')
                  else mri:keyword/*[1][. != '']/replace(text(), ',', ',,'), ',')"/>

        <!-- Define the list of transformation mode available. -->
        <xsl:variable name="transformations"
                      as="xs:string"
                      select="if ($thesaurusConfig/@transformations != '')
                              then $thesaurusConfig/@transformations
                              else 'to-iso19115-3.2018-keyword,to-iso19115-3.2018-keyword-with-anchor,to-iso19115-3.2018-keyword-as-xlink'"/>

        <!-- Get current transformation mode based on XML fragement analysis -->
        <xsl:variable name="transformation"
          select="if (parent::node()/@xlink:href)
                  then 'to-iso19115-3.2018-keyword-as-xlink'
                  else if (count(mri:keyword/gcx:Anchor) > 0)
                  then 'to-iso19115-3.2018-keyword-with-anchor'
                  else 'to-iso19115-3.2018-keyword'"/>

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
        <xsl:variable name="orderById"
                      as="xs:string"
                      select="if ($thesaurusConfig/@orderById)
                              then $thesaurusConfig/@orderById
                              else 'false'"/>
        <!--
          Example: to restrict number of keyword to 1 for INSPIRE
          <xsl:variable name="maxTags"
          select="if ($thesaurusKey = 'external.theme.httpinspireeceuropaeutheme-theme') then '1' else ''"/>
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
        <xsl:variable name="allLanguages"
                      select="concat($metadataLanguage, ',', $metadataOtherLanguages)"/>

        <div data-gn-keyword-selector="{$widgetMode}"
             data-metadata-id="{$metadataId}"
             data-element-ref="{concat('_X', ../gn:element/@ref, '_replace')}"
             data-thesaurus-title="{$thesaurusTitle}"
             data-thesaurus-key="{$thesaurusKey}"
             data-keywords="{$keywords}"
             data-transformations="{$transformations}"
             data-current-transformation="{$transformation}"
             data-max-tags="{$maxTags}"
             data-browsable="{not($thesaurusConfig/@browsable)
                              or $thesaurusConfig/@browsable != 'false'}"
             data-order-by-id="{$orderById}"
             data-lang="{$metadataOtherLanguagesAsJson}"
             data-textgroup-only="false">
        </div>

        <xsl:variable name="isTypePlace" select="count(mri:type/mri:MD_KeywordTypeCode[@codeListValue='place']) > 0"/>
        <xsl:if test="$isTypePlace">
          <xsl:call-template name="render-batch-process-button">
            <xsl:with-param name="process-label-key" select="'add-extent-from-geokeywords'"/>
            <xsl:with-param name="process-name" select="'add-extent-from-geokeywords'"/>
            <xsl:with-param name="process-params">{"replace": "true"}</xsl:with-param>
          </xsl:call-template>
          <xsl:call-template name="render-batch-process-button">
            <xsl:with-param name="process-label-key" select="'add-one-extent-from-geokeywords'"/>
            <xsl:with-param name="process-name" select="'add-extent-from-geokeywords'"/>
            <xsl:with-param name="process-params">{"replace": "true", "boundingAll": "true"}</xsl:with-param>
          </xsl:call-template>
        </xsl:if>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="mode-iso19115-3.2018" select="*"/>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

</xsl:stylesheet>
