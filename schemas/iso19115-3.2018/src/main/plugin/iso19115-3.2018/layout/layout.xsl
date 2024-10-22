<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0"
  xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/2.0"
  xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
  xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
  xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
  xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
  xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
  xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
  xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/2.0"
  xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
  xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
  xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
  xmlns:dqm="http://standards.iso.org/iso/19157/-2/dqm/1.0"
  xmlns:mrc="http://standards.iso.org/iso/19157/-2/mrc/1.0"
  xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
  xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
  xmlns:gfc="http://standards.iso.org/iso/19110/gfc/1.1"
  xmlns:gmx="http://standards.iso.org/iso/19115/-3/gmx"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:gml="http://www.opengis.net/gml/3.2"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:gn-fn-core="http://geonetwork-opensource.org/xsl/functions/core"
  xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
  xmlns:gn-fn-iso19115-3.2018="http://geonetwork-opensource.org/xsl/functions/profiles/iso19115-3.2018"
  exclude-result-prefixes="#all">

  <xsl:include href="utility-fn.xsl"/>
  <xsl:include href="utility-tpl.xsl"/>
  <xsl:include href="layout-custom-fields.xsl"/>
  <xsl:include href="layout-custom-fields-date.xsl"/>
  <xsl:include href="layout-custom-fields-contact.xsl"/>
  <xsl:include href="layout-custom-fields-keywords.xsl"/>
  <xsl:include href="layout-custom-feature-catalogue.xsl"/>

  <!-- Visit all XML tree recursively -->
  <xsl:template mode="mode-iso19115-3.2018"
                match="mds:*|mcc:*|mri:*|mrs:*|mrc:*|mrd:*|mco:*|msr:*|lan:*|
                       gcx:*|gex:*|dqm:*|mdq:*|cit:*|srv:*|gml:*|gfc:*"
                priority="2">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>
    <xsl:param name="refToDelete" required="no"/>

    <xsl:apply-templates mode="mode-iso19115-3.2018" select="*|@*">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="labels" select="$labels"/>
      <xsl:with-param name="refToDelete" select="$refToDelete"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- Ignore all gn element -->
  <xsl:template mode="mode-iso19115-3.2018" match="gn:*|@gn:*|@*" priority="1000"/>


  <!-- Template to display non existing element ie. geonet:child element
	of the metadocument. Display in editing mode only and if
  the editor mode is not flat mode. -->
  <xsl:template mode="mode-iso19115-3.2018" match="gn:child" priority="2000">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>

    <xsl:variable name="name" select="concat(@prefix, ':', @name)"/>
    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(..)"/>
    <xsl:variable name="flatModeException"
                  select="gn-fn-metadata:isFieldFlatModeException($viewConfig, $name, name(..), $xpath)"/>

    <xsl:if test="$isEditing and
                  (not($isFlatMode) or $flatModeException)">

      <xsl:variable name="directive"
                    select="gn-fn-metadata:getFieldAddDirective($editorConfig, $name)"/>
      <xsl:variable name="label"
                    select="gn-fn-metadata:getLabel($schema, $name, $labels, name(..), '', '')"/>

      <xsl:choose>
        <!-- Specifc case when adding a new keyword using the gn-thesaurus-selector
        in a view where descriptiveKeyword is a flat mode exception. In this case
        the "Add keyword" button will add a new descriptiveKeyword block if none exists
        and it will insert a keyword in the first descriptiveKeyword block (not referencing thesaurus)
        ie. free text keyword block.
        The goal here is to avoid to have multiple free text descriptiveKeyword sections.
        -->
        <xsl:when test="$flatModeException and $name = 'mri:descriptiveKeywords'">
          <xsl:variable name="freeTextKeywordBlocks"
                        select="../mri:descriptiveKeywords[not(*/mri:thesaurusName)]"/>
          <xsl:variable name="isFreeTextKeywordBlockExist"
                        select="count($freeTextKeywordBlocks) > 0"/>
          <xsl:variable name="freeTextKeywordTarget"
                        select="if ($isFreeTextKeywordBlockExist) then $freeTextKeywordBlocks[1]/*/gn:child[@name = 'keyword'] else ."/>

          <xsl:variable name="directive" as="node()?">
            <xsl:for-each select="$directive">
              <xsl:copy>
                <xsl:copy-of select="@*"/>
                <directiveAttributes data-freekeyword-element-ref="{$freeTextKeywordTarget/../gn:element/@ref}"
                                     data-freekeyword-element-name="{concat($freeTextKeywordTarget/@prefix, ':', $freeTextKeywordTarget/@name)}">
                  <xsl:copy-of select="directiveAttributes/@*"/>
                </directiveAttributes>
              </xsl:copy>
            </xsl:for-each>
          </xsl:variable>

          <xsl:call-template name="render-element-to-add">
            <xsl:with-param name="label" select="$label/label"/>
            <xsl:with-param name="class" select="if ($label/class) then $label/class else ''"/>
            <xsl:with-param name="btnLabel" select="if ($label/btnLabel) then $label/btnLabel else ''"/>
            <xsl:with-param name="btnClass" select="if ($label/btnClass) then $label/btnClass else ''"/>
            <xsl:with-param name="directive" select="$directive"/>
            <xsl:with-param name="childEditInfo" select="."/>
            <xsl:with-param name="parentEditInfo" select="../gn:element"/>
            <xsl:with-param name="isFirst" select="count(preceding-sibling::*[name() = $name]) = 0"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="render-element-to-add">
            <xsl:with-param name="label" select="$label/label"/>
            <xsl:with-param name="class" select="if ($label/class) then $label/class else ''"/>
            <xsl:with-param name="btnLabel" select="if ($label/btnLabel) then $label/btnLabel else ''"/>
            <xsl:with-param name="btnClass" select="if ($label/btnClass) then $label/btnClass else ''"/>
            <xsl:with-param name="directive" select="$directive"/>
            <xsl:with-param name="childEditInfo" select="."/>
            <xsl:with-param name="parentEditInfo" select="../gn:element"/>
            <xsl:with-param name="isFirst" select="count(preceding-sibling::*[name() = $name]) = 0"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <!-- Boxed element

      Details about the last line :
      * namespace-uri(.) != $gnUri: Only take into account profile's element
      * and $isFlatMode = false(): In flat mode, don't box any
      * and gmd:*: Match all elements having gmd child elements
      * and not(gco:CharacterString): Don't take into account those having gco:CharacterString (eg. multilingual elements)
  -->
  <xsl:template mode="mode-iso19115-3.2018" priority="200"
                name="mode-iso19115-3.2018-fieldset"
                match="*[name() = $editorConfig/editor/fieldsWithFieldset/name
                          or @gco:isoType = $editorConfig/editor/fieldsWithFieldset/name]|
                        *[namespace-uri(.) != $gnUri and $isFlatMode = false() and
                        not(gco:CharacterString)]">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>
    <xsl:param name="config" required="no"/>
    <xsl:param name="overrideLabel" select="''" required="no"/>

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
                      select="if ($overrideLabel != '')
                              then $overrideLabel
                              else gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)/label"/>
      <xsl:with-param name="editInfo" select="gn:element"/>
      <xsl:with-param name="errors" select="$errors"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="attributesSnippet" select="$attributes"/>
      <xsl:with-param name="subTreeSnippet">
        <!-- Process child of those element. Propagate schema
        and labels to all subchilds (eg. needed like iso19110 elements
        contains gmd:* child. -->
        <xsl:apply-templates mode="mode-iso19115-3.2018" select="*">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="labels" select="$labels"/>
        </xsl:apply-templates>
      </xsl:with-param>
      <xsl:with-param name="collapsible"
                      select="if ($config and $config/@collapsible != '')
                              then xs:boolean($config/@collapsible) else true()"/>
      <xsl:with-param name="collapsed"
                      select="if ($config and $config/@collapsed != '')
                              then xs:boolean($config/@collapsed) else false()"/>
    </xsl:call-template>
  </xsl:template>

  <!-- Render simple element which usually match a form field -->
  <xsl:template mode="mode-iso19115-3.2018" priority="200"
                match="*[gco:CharacterString|gcx:Anchor|gco:Integer|gco:UnlimitedInteger|gco:Decimal|
       gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|gco:Angle|gmx:FileName|
       gco:Scale|gco:RecordType|gcx:MimeFileType|gco:LocalName|gco:ScopedName|gco:RecordType|
       gco:Record|lan:PT_FreeText|mcc:URI|gco:TM_PeriodDuration|gco:UomIdentifier]">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>
    <xsl:param name="overrideLabel" select="''" required="no"/>
    <xsl:param name="isDisabled" required="no"/>
    <xsl:param name="refToDelete" select="''" required="no"/>
    <xsl:param name="config" required="no"/>


    <xsl:variable name="elementName" select="name()"/>

    <xsl:variable name="exclusionMatchesParent">
      <xsl:variable name="parent">
        <xsl:value-of separator="," select="$editorConfig/editor/multilingualFields/exclude/name[. = $elementName]/@parent" />
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="string-length($parent) > 0">
          <xsl:value-of select="contains($parent, ../name())" />
        </xsl:when>
        <xsl:otherwise><xsl:value-of select="false()"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="exclusionMatchesAncestor">
      <xsl:variable name="ancestor">
        <xsl:value-of separator="," select="$editorConfig/editor/multilingualFields/exclude/name[. = $elementName]/@ancestor" />
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="string-length($ancestor) > 0 and count(ancestor::*[contains($ancestor, name())]) != 0">
          <xsl:value-of select="true()" />
        </xsl:when>
        <xsl:otherwise><xsl:value-of select="false()"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="exclusionMatchesChild">
      <xsl:variable name="child">
        <xsl:value-of separator="," select="$editorConfig/editor/multilingualFields/exclude/name[. = $elementName]/@child" />
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="string-length($child) > 0 and count(*[contains($child, name())]) != 0">
          <xsl:value-of select="true()" />
        </xsl:when>
        <xsl:otherwise><xsl:value-of select="false()"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="excluded"
                  select="gn-fn-iso19115-3.2018:isNotMultilingualField(., $editorConfig)"/>


    <xsl:variable name="hasPTFreeText" select="count(lan:PT_FreeText) > 0"/>
    <xsl:variable name="hasOnlyPTFreeText"
                  select="count(lan:PT_FreeText) > 0 and count(gco:CharacterString|gcx:Anchor) = 0"/>


    <xsl:variable name="isMultilingualElement"
                  select="$metadataIsMultilingual and $excluded = false()"/>
    <xsl:variable name="isMultilingualElementExpanded"
                  select="$isMultilingualElement and count($editorConfig/editor/multilingualFields/expanded[name = $elementName]) > 0"/>

    <!-- For some fields, always display attributes.
    TODO: move to editor config ? -->
    <xsl:variable name="forceDisplayAttributes" select="count(gcx:Anchor) > 0"/>

    <xsl:variable name="monoLingualValue"
                  select="gco:CharacterString|gcx:Anchor|gco:Integer|gco:UnlimitedInteger|gco:Decimal|
                          gco:Boolean|gco:Real|gco:Measure|gco:Length|
                          gco:Distance|gco:Angle|gmx:FileName|
                          gco:Scale|gco:RecordType|gcx:MimeFileType|
                          gco:LocalName|gco:ScopedName|gco:RecordType|
                          gco:Record|mcc:URI|gco:TM_PeriodDuration|gco:UomIdentifier"/>
    <xsl:variable name="theElement"
                  select="if ($isMultilingualElement and $hasOnlyPTFreeText or not($monoLingualValue))
                          then lan:PT_FreeText
                          else $monoLingualValue"/>

    <!--
      This may not work if node context is lost eg. when an element is rendered
      after a selection with copy-of.
      <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>-->
    <xsl:variable name="xpath" select="gn-fn-metadata:getXPathByRef(gn:element/@ref, $metadata, false())"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
    <xsl:variable name="labelConfig"
                  select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)"/>
    <xsl:variable name="helper" select="gn-fn-metadata:getHelper($labelConfig/helper, .)"/>

    <xsl:variable name="attributes">

      <!-- Create form for all existing attribute (not in gn namespace)
      and all non existing attributes not already present for the
      current element and its children (eg. @uom in gco:Distance).
      A list of exception is defined in form-builder.xsl#render-for-field-for-attribute. -->
      <xsl:apply-templates mode="render-for-field-for-attribute"
                           select="
            @*|
            gn:attribute[not(@name = parent::node()/@*/name())]">
        <xsl:with-param name="ref" select="gn:element/@ref"/>
        <xsl:with-param name="insertRef" select="$theElement/gn:element/@ref"/>
      </xsl:apply-templates>
      <xsl:apply-templates mode="render-for-field-for-attribute"
                           select="
        */@*|
        */gn:attribute[not(@name = parent::node()/@*/name())]">
        <xsl:with-param name="ref" select="$theElement/gn:element/@ref"/>
        <xsl:with-param name="insertRef" select="$theElement/gn:element/@ref"/>
      </xsl:apply-templates>
    </xsl:variable>

    <xsl:variable name="errors">
      <xsl:if test="$showValidationErrors">
        <xsl:call-template name="get-errors">
          <xsl:with-param name="theElement" select="$theElement"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:variable>

    <xsl:variable name="values">
      <xsl:if test="$isMultilingualElement">
        <xsl:variable name="text"
                      select="normalize-space(gco:CharacterString|gcx:Anchor)"/>
        <values>
          <!--
          CharacterString is not edited anymore, but it's PT_FreeText
          counterpart is.
          Or the PT_FreeText element matching the main language
          <xsl:if test="gco:CharacterString">
            <value ref="{$theElement/gn:element/@ref}" lang="{$metadataLanguage}"><xsl:value-of select="gco:CharacterString"/></value>
          </xsl:if>-->


          <!-- the existing translation -->
          <xsl:for-each select="lan:PT_FreeText/lan:textGroup/lan:LocalisedCharacterString">
            <value ref="{gn:element/@ref}"
                   lang="{substring-after(@locale, '#')}"><xsl:value-of select="."/></value>
          </xsl:for-each>

          <!-- and create field for none translated language -->
          <xsl:for-each select="$metadataOtherLanguages/lang">
            <xsl:variable name="code" select="@code"/>
            <xsl:variable name="currentLanguageId" select="@id"/>
            <xsl:variable name="ptFreeElementDoesNotExist"
                          select="count($theElement/parent::node()/
                                        lan:PT_FreeText/*/
                                        lan:LocalisedCharacterString[
                                          @locale = concat('#', $currentLanguageId)]) = 0"/>
            <xsl:choose>
              <xsl:when test="$ptFreeElementDoesNotExist and
                              $text != '' and
                              $code = $metadataLanguage">
                <value ref="lang_{@id}_{$theElement/parent::node()/gn:element/@ref}"
                       lang="{@id}">
                  <xsl:value-of select="$text"/>
                </value>
              </xsl:when>
              <xsl:when test="$ptFreeElementDoesNotExist">
                <value ref="lang_{@id}_{$theElement/parent::node()/gn:element/@ref}"
                       lang="{@id}"></value>
              </xsl:when>
            </xsl:choose>
          </xsl:for-each>
        </values>
      </xsl:if>
    </xsl:variable>

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
      <xsl:with-param name="value" select="if ($isMultilingualElement) then $values else *[namespace-uri(.) != $gnUri]"/>
      <xsl:with-param name="errors" select="$errors"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <!--<xsl:with-param name="widget"/>
        <xsl:with-param name="widgetParams"/>-->
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="attributesSnippet" select="$attributes"/>
      <xsl:with-param name="type"
                      select="if ($config and $config/@use != '')
                              then $config/@use
                              else gn-fn-metadata:getFieldType($editorConfig, name(),
                                                               name($theElement), $xpath)"/>
      <xsl:with-param name="directiveAttributes">
        <xsl:choose>
          <xsl:when test="$config and $config/@use != ''">
            <xsl:element name="directive">
              <xsl:attribute name="data-directive-name" select="$config/@use"/>
              <xsl:copy-of select="$config/directiveAttributes/@*"/>
            </xsl:element>
          </xsl:when>
          <xsl:otherwise>
            <xsl:copy-of select="gn-fn-metadata:getFieldDirective($editorConfig, name(), name($theElement), $xpath)"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:with-param>
      <xsl:with-param name="name" select="if ($isEditing) then $theElement/gn:element/@ref else ''"/>
      <xsl:with-param name="editInfo" select="$theElement/gn:element"/>
      <xsl:with-param name="parentEditInfo" select="if ($refToDelete) then $refToDelete else gn:element"/>
      <xsl:with-param name="listOfValues" select="$helper"/>
      <xsl:with-param name="toggleLang" select="$isMultilingualElementExpanded"/>
      <xsl:with-param name="forceDisplayAttributes" select="$forceDisplayAttributes"/>
      <xsl:with-param name="isFirst" select="count(preceding-sibling::*[name() = $elementName]) = 0"/>
      <xsl:with-param name="isDisabled" select="$isDisabled"/>
    </xsl:call-template>
  </xsl:template>



  <xsl:template mode="mode-iso19115-3.2018"
                match="gml:beginPosition|gml:endPosition|gml:timePosition"
                priority="400">
    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="value" select="normalize-space(text())"/>

    <xsl:variable name="attributes">
      <!-- Create form for all existing attribute (not in gn namespace)
      and all non existing attributes not already present. -->
      <xsl:apply-templates mode="render-for-field-for-attribute"
                           select="@*|
                            gn:attribute[not(@name = parent::node()/@*/name())]">
        <xsl:with-param name="ref" select="gn:element/@ref"/>
        <xsl:with-param name="insertRef" select="gn:element/@ref"/>
      </xsl:apply-templates>
    </xsl:variable>

    <xsl:call-template name="render-element">
      <xsl:with-param name="label"
                      select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), '', $xpath)"/>
      <xsl:with-param name="name" select="gn:element/@ref"/>
      <xsl:with-param name="value" select="text()"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="type"
                      select="if (string-length($value) = 10 or $value = '') then 'date' else 'datetime'"/>
      <xsl:with-param name="editInfo" select="gn:element"/>
      <xsl:with-param name="attributesSnippet" select="$attributes"/>
    </xsl:call-template>
  </xsl:template>


  <xsl:template mode="mode-iso19115-3.2018" priority="200"
                match="gfc:memberName|gfc:typeName|gfc:aliases">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="value" select="normalize-space(text())"/>

    <xsl:variable name="attributes">
      <!-- Create form for all existing attribute (not in gn namespace)
      and all non existing attributes not already present. -->
      <xsl:apply-templates mode="render-for-field-for-attribute"
                           select="@*|
                            gn:attribute[not(@name = parent::node()/@*/name())]">
        <xsl:with-param name="ref" select="gn:element/@ref"/>
        <xsl:with-param name="insertRef" select="gn:element/@ref"/>
      </xsl:apply-templates>
    </xsl:variable>


    <xsl:call-template name="render-element">
      <xsl:with-param name="label"
                      select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), '', $xpath)"/>
      <xsl:with-param name="name" select="gn:element/@ref"/>
      <xsl:with-param name="value" select="text()"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="type"
                      select="gn-fn-metadata:getFieldType($editorConfig, name(), '', $xpath)"/>
      <xsl:with-param name="editInfo" select="gn:element"/>
      <xsl:with-param name="attributesSnippet" select="$attributes"/>
    </xsl:call-template>

  </xsl:template>

  <!--
  <xsl:template mode="mode-iso19115-3.2018" match="*|@*" priority="0"/>
-->
  <!-- Codelists -->
  <xsl:template mode="mode-iso19115-3.2018" priority="200"
                match="*[*/@codeList]">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>
    <xsl:param name="codelists" select="$codelists" required="no"/>
    <xsl:param name="overrideLabel" select="''" required="no"/>

    <xsl:variable name="elementName" select="name()"/>
    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>

    <xsl:variable name="labelConfig">
      <xsl:choose>
        <xsl:when test="$overrideLabel != ''">
          <element>
            <label><xsl:value-of select="$overrideLabel"/></label>
          </element>
	</xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:call-template name="render-element">
      <xsl:with-param name="label"
                      select="$labelConfig/*"/>
      <xsl:with-param name="value" select="*/@codeListValue"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="type" select="gn-fn-iso19115-3.2018:getCodeListType(name(), $editorConfig)"/>
      <xsl:with-param name="name"
                      select="concat(*/gn:element/@ref, '_codeListValue')"/>
      <xsl:with-param name="editInfo" select="*/gn:element"/>
      <xsl:with-param name="parentEditInfo" select="gn:element"/>
      <xsl:with-param name="listOfValues"
                      select="gn-fn-metadata:getCodeListValues($schema, name(*[@codeListValue]), $codelists, .)"/>
      <xsl:with-param name="isFirst" select="count(preceding-sibling::*[name() = $elementName]) = 0"/>
    </xsl:call-template>
  </xsl:template>


  <!--
    Take care of enumerations.

    In the metadocument an enumeration provide the list of possible values:
  <gmd:topicCategory>
    <gmd:MD_TopicCategoryCode>
    <geonet:element ref="69" parent="68" uuid="gmd:MD_TopicCategoryCode_0073afa8-bc8f-4c52-94f3-28d3aa686772" min="1" max="1">
      <geonet:text value="farming"/>
      <geonet:text value="biota"/>
      <geonet:text value="boundaries"/
  -->

  <xsl:template mode="mode-iso19115-3.2018"
                match="mri:topicCategory[1]"
                priority="2100">

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
    <xsl:variable name="labelConfig" select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)"/>


    <xsl:variable name="elementName" select="name()" />

    <xsl:variable name="topicCategories">
      <xsl:for-each select="../*[name() = $elementName]">
        <xsl:value-of select="mri:MD_TopicCategoryCode/text()" />
        <xsl:if test="position() != last()">,</xsl:if>
      </xsl:for-each>
    </xsl:variable>

    <xsl:call-template name="render-element">
      <xsl:with-param name="label"
                      select="$labelConfig"/>
      <xsl:with-param name="value" select="$topicCategories"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="type" select="'data-gn-topiccategory-selector-div'"/>
      <xsl:with-param name="editInfo" select="gn:element"/>
      <xsl:with-param name="parentEditInfo" select="../gn:element"/>
    </xsl:call-template>
  </xsl:template>

  <!-- Ignore the following topic categories and the gn:child to add new ones -->
  <xsl:template mode="mode-iso19115-3.2018"
                match="mri:topicCategory[preceding-sibling::*[1]/name() = name()]"
                priority="2100"/>
  <xsl:template mode="mode-iso19115-3.2018"
                match="gn:child[@name = 'topicCategory' and count(../mri:topicCategory) > 0]"
                priority="21000"/>


  <xsl:template mode="mode-iso19115-3.2018"
                match="*[gn:element/gn:text]"
                priority="2000">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>
    <xsl:param name="codelists" select="$codelists" required="no"/>
    <xsl:param name="refToDelete" required="no"/>

    <xsl:call-template name="render-element">
      <xsl:with-param name="label"
                      select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), '', '')"/>
      <xsl:with-param name="value" select="text()"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="type" select="gn-fn-iso19115-3.2018:getCodeListType(name(), $editorConfig)"/>
      <xsl:with-param name="name" select="gn:element/@ref"/>
      <xsl:with-param name="editInfo" select="if ($refToDelete) then $refToDelete else */gn:element"/>
      <xsl:with-param name="listOfValues"
                      select="gn-fn-metadata:getCodeListValues($schema, name(), $codelists, .)"/>
    </xsl:call-template>
  </xsl:template>


  <!-- Some element to ignore which are matched by the
  next template -->
  <xsl:template mode="mode-iso19115-3.2018" priority="400" match="gml:TimeInstantType"/>

  <!-- the gml element having no child eg. gml:name. -->
  <xsl:template mode="mode-iso19115-3.2018" priority="300" match="gml:*[count(.//gn:element) = 1]">
    <xsl:variable name="name" select="name(.)"/>

    <xsl:variable name="labelConfig" select="gn-fn-metadata:getLabel($schema, $name, $labels)"/>
    <xsl:variable name="helper" select="gn-fn-metadata:getHelper($labelConfig/helper, .)"/>
    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>

    <xsl:variable name="added" select="parent::node()/parent::node()/@gn:addedObj"/>
    <xsl:call-template name="render-element">
      <xsl:with-param name="label" select="$labelConfig"/>
      <xsl:with-param name="value" select="."/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="gn-fn-metadata:getXPath(.)"/>
      <xsl:with-param name="type" select="gn-fn-metadata:getFieldType($editorConfig, name(), '', $xpath)"/>
      <xsl:with-param name="name" select="if ($isEditing) then gn:element/@ref else ''"/>
      <xsl:with-param name="editInfo"
                      select="gn:element"/>
      <xsl:with-param name="listOfValues" select="$helper"/>
    </xsl:call-template>
  </xsl:template>
</xsl:stylesheet>
