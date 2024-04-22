<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:saxon="http://saxon.sf.net/"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                version="2.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <!--
    Display contact as table when mode is flat (eg. simple view) or if using xsl mode
    Match first node (or added one)
  -->
  <xsl:template mode="mode-dcat2"
                match="*[
                        *[1]/name() = $editorConfig/editor/tableFields/table/@for and
                        (1 or @gn:addedObj = 'true') and
                        $isFlatMode]"
                priority="2000">
    <xsl:call-template name="dcat2-table"/>
  </xsl:template>

  <!-- Ignore the following -->
  <xsl:template mode="mode-dcat2"
                match="*[
                        *[1]/name() = $editorConfig/editor/tableFields/table/@for and
                        preceding-sibling::*[1]/name() = name() and
                        not(@gn:addedObj) and
                        $isFlatMode]"
                priority="2000"/>

  <!-- Define table layout -->
  <xsl:template name="dcat2-table">
    <xsl:variable name="name" select="name()"/>
    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="''"/>

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
      <xsl:when test="$isEmbeddedMode and not($isFirstOfItsKind)">
        <xsl:call-template name="render-table">
          <xsl:with-param name="values" select="$values"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="render-boxed-element">
          <xsl:with-param name="label"
                          select="gn-fn-metadata:getLabel($schema, $name, $labels, name(..), $isoType, $xpath)/label"/>
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
</xsl:stylesheet>
