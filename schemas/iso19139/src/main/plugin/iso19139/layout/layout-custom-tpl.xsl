<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:saxon="http://saxon.sf.net/"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
                version="2.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <!--
    Display contact as table when mode is flat (eg. simple view) or if using xsl mode
    Match first node (or added one)
  -->
  <xsl:template mode="mode-iso19139"
                match="*[
                        *[1]/name() = $editorConfig/editor/tableFields/table/@for and
                        (1 or @gn:addedObj = 'true') and
                        $isFlatMode]"
                priority="2000">
    <xsl:call-template name="build-table">
      <xsl:with-param name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
    </xsl:call-template>
  </xsl:template>

  <!-- Ignore the following -->
  <xsl:template mode="mode-iso19139"
                match="*[
                        *[1]/name() = $editorConfig/editor/tableFields/table/@for and
                        preceding-sibling::*[1]/name() = name() and
                        not(@gn:addedObj) and
                        $isFlatMode]"
                priority="2000"/>



  <xsl:template mode="mode-iso19139"
                match="srv:operatesOn[(1 or @gn:addedObj = 'true') and $isFlatMode]"
                priority="4000">

    <xsl:variable name="name"
                  select="name()"/>
    <xsl:variable name="xpath"
                  select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType"
                  select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
    <xsl:variable name="isEmbeddedMode"
                  select="@gn:addedObj = 'true'"/>
    <xsl:variable name="isFirstOfItsKind"
                  select="preceding-sibling::*[1]/name() != $name"/>
    <xsl:variable name="values">
      <xsl:if test="not($isEmbeddedMode) or ($isEmbeddedMode and $isFirstOfItsKind)">
        <header>
          <col>
            <xsl:value-of select="gn-fn-metadata:getLabel($schema, 'uuidref', $labels, '', $isoType, $xpath)/label"/>
          </col>
          <col>
            <xsl:value-of select="gn-fn-metadata:getLabel($schema, 'xlink:href', $labels, '', $isoType, $xpath)/label"/>
          </col>
        </header>
      </xsl:if>
      <xsl:for-each select="(.|following-sibling::*[name() = $name])">
        <row>
          <col type="text" name="{concat('_', gn:element/@ref, '_uuidref')}">
            <value><xsl:value-of select="@uuidref"/></value>
          </col>
          <col type="text" name="{concat('_', gn:element/@ref, '_xlinkCOLONhref')}">
            <value><xsl:value-of select="@xlink:href"/></value>
          </col>
          <col remove="true">
            <xsl:copy-of select="gn:element"/>
          </col>
        </row>
      </xsl:for-each>
    </xsl:variable>

    <!-- Return only the new row in embed mode. -->
    <xsl:choose>
      <xsl:when test="$isEmbeddedMode and not($isFirstOfItsKind)">
        <xsl:call-template name="render-table">
          <xsl:with-param name="values" select="$values"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="render-boxed-element">
          <xsl:with-param name="label"
                          select="gn-fn-metadata:getLabel($schema, $name, $labels, $name, $isoType, $xpath)/label"/>
          <xsl:with-param name="cls" select="local-name()"/>
          <xsl:with-param name="subTreeSnippet">

            <xsl:call-template name="render-table">
              <xsl:with-param name="values" select="$values"/>
            </xsl:call-template>

          </xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="mode-iso19139"
                match="srv:operatesOn[
                        preceding-sibling::*[1]/name() = name() and
                        not(@gn:addedObj) and
                        $isFlatMode]"
                priority="4000"/>

</xsl:stylesheet>
