<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
                exclude-result-prefixes="#all">

  <!-- Vacuum utility. Remove empty elements from a metadata record:
  * All empty text element
  * All elements having no child text (ie. normalize-space return '') and
  have only empty attribute (or gco:nilReason=missing).

  The main function call the vacuum-iso19139 mode.
  -->
  <xsl:function name="gn-fn-iso19139:vacuum" as="node()">
    <xsl:param name="metadata" as="node()"/>
    <xsl:for-each select="$metadata/*">
      <xsl:apply-templates mode="vacuum-iso19139"
                           select="."/>
    </xsl:for-each>
  </xsl:function>


  <xsl:function name="gn-fn-iso19139:isElementOrChildEmpty"
                as="xs:boolean">
    <xsl:param name="element"/>

    <xsl:choose>
      <xsl:when test="$element">
        <!--<xsl:message>&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;</xsl:message>
        <xsl:message><xsl:copy-of select="$element"/></xsl:message>
        <xsl:message><xsl:copy-of select="normalize-space($element)"/></xsl:message>
        <xsl:message><xsl:copy-of select="count($element//@*[. != '' and . != 'missing'])"/></xsl:message>-->
        <xsl:value-of select="if (normalize-space($element) = '' and
                                  count($element//@*[. != '' and . != 'missing']) = 0)
                              then true() else false()"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="false()"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template mode="vacuum-iso19139"
                match="@*|node()">
    <xsl:variable name="isElementEmpty"
                  select="gn-fn-iso19139:isElementOrChildEmpty(.)"/>

    <xsl:choose>
      <xsl:when test="$isElementEmpty = true()">
    <!--    <empty>
          <xsl:copy-of select="."/>
        </empty>-->
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates mode="vacuum-iso19139"
                               select="@*|node()"/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
  This will not work as it will only remove elements and its parent.
  It will not handle complex empty element.
  <xsl:template mode="vacuum-iso19139"
                match="@*[. = '']|
                       *[gco:CharacterString/normalize-space(text()) = '']|
                       *[text() = '' and count(@*) = 0 and count(*) = 0]"
                priority="2"><empty/></xsl:template>-->

  <!-- Always remove gn:* elements. -->
  <xsl:template mode="vacuum-iso19139"
                match="gn:*" priority="2"/>

</xsl:stylesheet>
