<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
  xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
  xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
  xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
  xmlns:gn-fn-iso19115-3.2018="http://geonetwork-opensource.org/xsl/functions/profiles/iso19115-3.2018"
  xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
  exclude-result-prefixes="#all">


  <!-- Get language id attribute defined in
  the metadata PT_Locale section matching the lang
  parameter. If not found, return the lang parameter
  prefixed by #.
        -->
  <xsl:function name="gn-fn-iso19115-3.2018:getLangId" as="xs:string">
    <xsl:param name="md"/>
    <xsl:param name="lang"/>

    <xsl:variable name="languageIdentifier"
                  select="$md/lan:locale/lan:PT_Locale[
          lan:languageCode/lan:LanguageCode/@codeListValue = $lang]/@id"/>
    <xsl:choose>
      <xsl:when
        test="$languageIdentifier">
        <xsl:value-of
          select="concat('#', $languageIdentifier)"
        />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="concat('#', upper-case($lang))"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>


  <xsl:function name="gn-fn-iso19115-3.2018:getCodeListType" as="xs:string">
    <xsl:param name="name" as="xs:string"/>
    <xsl:param name="editorConfig" as="node()"/>

    <xsl:variable name="configType"
                  select="$editorConfig/editor/fields/for[@name = $name]/@use"/>

    <xsl:value-of select="if ($configType) then $configType else 'select'"/>
  </xsl:function>


  <xsl:function name="gn-fn-iso19115-3.2018:isNotMultilingualField" as="xs:boolean">
    <xsl:param name="element" as="node()"/>
    <xsl:param name="editorConfig" as="node()"/>

    <xsl:variable name="elementName" select="name($element)"/>

    <xsl:variable name="exclusionMatchesParent" as="xs:boolean">
      <xsl:variable name="parentName"
                    select="name($element/..)"/>

      <xsl:value-of select="count($editorConfig/editor/multilingualFields/exclude/
                                  name[. = $elementName]/@parent[. = $parentName]) > 0"/>
    </xsl:variable>


    <xsl:variable name="exclusionMatchesAncestor" as="xs:boolean">
      <xsl:variable name="ancestorNames"
                    select="$element/ancestor::*/name()"/>

      <xsl:value-of select="count($editorConfig/editor/multilingualFields/exclude/
                                  name[. = $elementName]/@ancestor[. = $ancestorNames]) > 0"/>
    </xsl:variable>


    <xsl:variable name="exclusionMatchesChild" as="xs:boolean">
      <xsl:variable name="childName"
                    select="name($element/*[1])"/>

      <xsl:value-of select="count($editorConfig/editor/multilingualFields/exclude/
                                  name[. = $elementName]/@child[. = $childName]) > 0"/>
    </xsl:variable>



    <xsl:variable name="excluded"
                  as="xs:boolean"
                  select="
                    count($editorConfig/editor/multilingualFields/exclude/name[. = $elementName and not(@*)]) > 0 or
                      $exclusionMatchesAncestor = true() or
                      $exclusionMatchesParent = true() or
                      $exclusionMatchesChild = true() or
                      count($element/gco:Boolean) > 0"/>

    <!--
     <xsl:message>===== elementName <xsl:copy-of select="$elementName"/></xsl:message>
     <xsl:message>= <xsl:copy-of select="$exclusionMatchesParent"/></xsl:message>
     <xsl:message>= <xsl:copy-of select="$exclusionMatchesAncestor"/></xsl:message>
     <xsl:message>= <xsl:copy-of select="$exclusionMatchesChild"/></xsl:message>
     <xsl:message>= excluded<xsl:copy-of select="$excluded"/></xsl:message>-->

    <xsl:value-of select="$excluded"/>
  </xsl:function>

  <!--
   Create a multilingual element depending on the metadata record.
   eg. eng#Basin of Africa|FR#Bassin versant d'Afrique
   -->
  <xsl:function name="gn-fn-iso19115-3.2018:fillTextElement" as="node()*">
    <xsl:param name="string" as="xs:string"/>
    <xsl:param name="mainLanguage" as="xs:string"/>
    <xsl:param name="useOnlyPTFreeText" as="xs:boolean"/>

    <xsl:copy-of select="gn-fn-iso19115-3.2018:fillTextElement($string, '\|', '#', $mainLanguage, $useOnlyPTFreeText)"/>
  </xsl:function>

  <xsl:function name="gn-fn-iso19115-3.2018:fillTextElement" as="node()*">
    <xsl:param name="string" as="xs:string"/>
    <xsl:param name="translationSeparator" as="xs:string"/>
    <xsl:param name="valueSeparator" as="xs:string"/>
    <xsl:param name="mainLanguage" as="xs:string"/>
    <xsl:param name="useOnlyPTFreeText" as="xs:boolean"/>

    <xsl:choose>
      <xsl:when test="matches($string, concat('.*', $translationSeparator, '.*'))">
        <xsl:for-each select="tokenize($string, $translationSeparator)">
          <xsl:variable name="descLang"
                        select="substring-before(., $valueSeparator)"/>
          <xsl:variable name="descValue"
                        select="substring-after(., $valueSeparator)"/>
          <xsl:if test="$useOnlyPTFreeText = false() and $descLang = $mainLanguage">
            <gco:CharacterString>
              <xsl:value-of select="$descValue"/>
            </gco:CharacterString>
          </xsl:if>
        </xsl:for-each>

        <lan:PT_FreeText>
          <xsl:for-each select="tokenize($string, $translationSeparator)">
            <xsl:variable name="descLang"
                          select="substring-before(., $valueSeparator)"/>
            <xsl:variable name="descValue"
                          select="substring-after(., $valueSeparator)"/>
            <xsl:if test="$useOnlyPTFreeText or $descLang != $mainLanguage">
              <lan:textGroup>
                <lan:LocalisedCharacterString locale="{concat('#', $descLang)}">
                  <xsl:value-of select="$descValue" />
                </lan:LocalisedCharacterString>
              </lan:textGroup>
            </xsl:if>
          </xsl:for-each>
        </lan:PT_FreeText>
      </xsl:when>
      <xsl:otherwise>
        <gco:CharacterString>
          <xsl:value-of select="if (contains($string, $valueSeparator))
                                then substring-after($string, $valueSeparator)
                                else $string"/>
        </gco:CharacterString>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
</xsl:stylesheet>
