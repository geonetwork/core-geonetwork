<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:geonet="http://www.fao.org/geonetwork"
                version="1.0"
                exclude-result-prefixes="xalan"
>


  <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
  <!-- callbacks from schema templates -->
  <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

  <xsl:template mode="elementFop" match="*|@*">
    <xsl:param name="schema"/>

    <xsl:choose>
      <!-- Is a localized element -->
      <xsl:when test="contains($schema, 'iso19139') and gmd:PT_FreeText">
        <xsl:apply-templates mode="localizedElemFop" select=".">
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:apply-templates>
      </xsl:when>
      <!-- has children or attributes, existing or potential -->
      <xsl:when
        test="*[namespace-uri(.)!=$geonetUri]|*/@*|geonet:child|geonet:element/geonet:attribute">
        <!-- if it does not have children show it as a simple element -->
        <xsl:if
          test="not(*[namespace-uri(.)!=$geonetUri]|geonet:child|geonet:element/geonet:attribute)">
          <xsl:apply-templates mode="simpleElementFop" select=".">
            <xsl:with-param name="schema" select="$schema"/>
          </xsl:apply-templates>
        </xsl:if>
        <!-- existing attributes -->
        <xsl:apply-templates mode="simpleElementFop" select="*/@*">
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:apply-templates>

        <!-- existing and new children -->
        <xsl:apply-templates mode="elementEPFop"
                             select="*[namespace-uri(.)!=$geonetUri]|geonet:child">
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:apply-templates>
      </xsl:when>

      <!-- neither children nor attributes, just text -->
      <xsl:otherwise>
        <xsl:apply-templates mode="simpleElementFop" select=".">
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:apply-templates>
      </xsl:otherwise>

    </xsl:choose>
  </xsl:template>

  <xsl:template mode="localizedElemFop" match="*">
    <xsl:param name="schema"/>
    <xsl:variable name="title">
      <xsl:call-template name="getTitle">
        <xsl:with-param name="name" select="name(.)"/>
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="UPPER">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
    <xsl:variable name="LOWER">abcdefghijklmnopqrstuvwxyz</xsl:variable>
    <xsl:variable name="text">
      <xsl:call-template name="translatedString">
        <xsl:with-param name="schema" select="$schema"/>
        <xsl:with-param name="langId"
                        select="concat('#',translate(substring(/root/gui/language,1,2),$LOWER,$UPPER))"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:call-template name="info-rows">
      <xsl:with-param name="label" select="$title"/>
      <xsl:with-param name="value" select="$text"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="simpleElementFop" match="*">
    <xsl:param name="schema"/>

    <xsl:param name="title">
      <xsl:call-template name="getTitle">
        <xsl:with-param name="name">
          <xsl:choose>
            <xsl:when
              test="not(contains($schema, 'iso19139')) and not(contains($schema, 'iso19110')) and not(contains($schema, 'iso19135'))">
              <xsl:value-of select="name(.)"/>
            </xsl:when>
            <xsl:when test="@codeList">
              <xsl:value-of select="name(.)"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="name(..)"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:with-param>
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:call-template>
    </xsl:param>
    <xsl:param name="text">
      <xsl:call-template name="getElementText">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:call-template>
    </xsl:param>

    <xsl:call-template name="info-rows">
      <xsl:with-param name="label" select="$title"/>
      <xsl:with-param name="value" select="$text"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="simpleElementFop" match="@*">
    <xsl:param name="schema"/>
    <xsl:param name="title">
      <xsl:call-template name="getTitle">
        <xsl:with-param name="name" select="name(../..)"/>
        <!-- Usually codelist -->
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:call-template>
    </xsl:param>
    <xsl:param name="text">
      <xsl:call-template name="getAttributeText">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:call-template>
    </xsl:param>

    <xsl:call-template name="info-rows">
      <xsl:with-param name="label" select="$title"/>
      <xsl:with-param name="value" select="$text"/>
    </xsl:call-template>
  </xsl:template>


  <xsl:template mode="complexElement" match="*">
    <xsl:param name="schema"/>
    <xsl:param name="title">
      <xsl:call-template name="getTitle">
        <xsl:with-param name="name" select="name(.)"/>
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:call-template>
    </xsl:param>
    <xsl:param name="content">
      <xsl:call-template name="getContent">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:call-template>
    </xsl:param>


    <xsl:call-template name="complexElementFop">
      <xsl:with-param name="title" select="$title"/>
      <xsl:with-param name="text" select="text()"/>
      <xsl:with-param name="content" select="$content"/>
      <xsl:with-param name="schema" select="$schema"/>
    </xsl:call-template>
  </xsl:template>


  <!--
    prevent drawing of geonet:* elements
    -->
  <xsl:template mode="elementFop"
                match="geonet:element|geonet:info|geonet:attribute|geonet:schematronerrors"/>
  <xsl:template mode="simpleElementFop"
                match="geonet:element|geonet:info|geonet:attribute|geonet:schematronerrors|@codeList|*[@codeList]|@gco:nilReason|*[@gco:nilReason]"/>
  <xsl:template mode="complexElementFop"
                match="geonet:element|geonet:info|geonet:attribute|geonet:schematronerrors"/>

</xsl:stylesheet>
