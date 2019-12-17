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
    <xsl:call-template name="iso19139-table"/>
  </xsl:template>

  <!-- Ignore the following -->
  <xsl:template mode="mode-iso19139"
                match="*[
                        *[1]/name() = $editorConfig/editor/tableFields/table/@for and
                        preceding-sibling::*[1]/name() = name() and
                        not(@gn:addedObj) and
                        $isFlatMode]"
                priority="2000"/>

  <!-- Define table layout -->
  <xsl:template name="iso19139-table">
    <xsl:variable name="name" select="name()"/>
    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>

    <xsl:variable name="childName"
                  select="*[1]/name()"/>

    <xsl:variable name="isEmbeddedMode"
                  select="@gn:addedObj = 'true'"/>
    <xsl:variable name="isFirstOfItsKind"
                  select="preceding-sibling::*[1]/name() != $name"/>

    <xsl:variable name="tableConfig"
                  select="$editorConfig/editor/tableFields/table[@for = $childName]"/>

    <xsl:variable name="values">
      <xsl:if test="not($isEmbeddedMode) or ($isEmbeddedMode and $isFirstOfItsKind)">
        <header>
          <xsl:for-each select="$tableConfig/header/col">
            <col>
              <xsl:copy-of select="@*"/>
              <xsl:if test="@label">
                <!-- TODO: column names may comes from strings.xml -->
                <xsl:value-of select="gn-fn-metadata:getLabel($schema, @label, $labels, '', $isoType, $xpath)/label"/>
              </xsl:if>
            </col>
          </xsl:for-each>
        </header>
      </xsl:if>
      <xsl:for-each select="(.|following-sibling::*[name() = $name])/*[name() = $childName]">

        <xsl:variable name="base"
                      select="."/>
        <xsl:for-each select="$tableConfig/row">
          <row>
            <xsl:for-each select="col">
              <col>
                <xsl:if test="@use != ''">
                  <xsl:copy-of select="@use|directiveAttributes"/>
                </xsl:if>
                <xsl:if test="@del != ''">
                  <xsl:attribute name="remove" select="'true'"/>

                  <saxon:call-template name="{concat('evaluate-', $schema)}">
                    <xsl:with-param name="base" select="$base"/>
                    <xsl:with-param name="in"
                                    select="concat('/', @del, '/gn:element')"/>
                  </saxon:call-template>
                </xsl:if>

                <xsl:if test="@xpath != ''">
                  <saxon:call-template name="{concat('evaluate-', $schema)}">
                    <xsl:with-param name="base" select="$base"/>
                    <xsl:with-param name="in"
                                    select="concat('/', @xpath)"/>
                  </saxon:call-template>
                </xsl:if>
              </col>
            </xsl:for-each>
          </row>

          <xsl:for-each select="section[@xpath]">
            <row>
              <col colspan="{count(../col)}" type="form" withLabel="true">
                <xsl:apply-templates mode="form-builder" select=".">
                  <xsl:with-param name="base" select="$base"/>
                </xsl:apply-templates>
                <!--<xsl:variable name="nodes">

                <saxon:call-template name="{concat('evaluate-', $schema)}">
                  <xsl:with-param name="base" select="$base"/>
                  <xsl:with-param name="in"
                                  select="concat('/', @xpath)"/>
                </saxon:call-template>
                </xsl:variable>

                <xsl:for-each select="$nodes">
                  <saxon:call-template name="{concat('dispatch-', $schema)}">
                    <xsl:with-param name="base" select="."/>
                  </saxon:call-template>
                </xsl:for-each>

                <xsl:if test="@or and @in">
                  <saxon:call-template name="{concat('evaluate-', $schema)}">
                    <xsl:with-param name="base" select="$base"/>
                    <xsl:with-param name="in"
                                    select="concat('/../', @in, '/gn:child[@name=''', @or, ''']')"/>
                  </saxon:call-template>
                </xsl:if>-->
              </col>
            </row>
          </xsl:for-each>
        </xsl:for-each>
      </xsl:for-each>
    </xsl:variable>


    <!-- Return only the new row in embed mode. -->
    <xsl:choose>
      <xsl:when test="$tableConfig/@fieldset = 'false' or ($isEmbeddedMode and not($isFirstOfItsKind))">
        <xsl:call-template name="render-table">
          <xsl:with-param name="values" select="$values"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>

        <xsl:variable name="tableTitle" select="if (($tableConfig/@label) and (string($strings/*[name() = $tableConfig/@label])))
              then $strings/*[name() = $tableConfig/@label]
              else gn-fn-metadata:getLabel($schema, $name, $labels, name(..), $isoType, $xpath)/label" />

        <xsl:call-template name="render-boxed-element">
          <xsl:with-param name="label"
                          select="$tableTitle"/>
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
