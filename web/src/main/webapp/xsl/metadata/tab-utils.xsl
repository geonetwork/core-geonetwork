<?xml version="1.0" encoding="UTF-8" ?>
<!-- 
  Metadata tabs layout utility.
  Tabs are defined using 2 levels :
   * level 1 is mode (eg. simple, advanced, inspire, ...)
   * level 2 is section or currTab. Simple mode as one sub level which is the default. 
   Id attribute indicate the default tab to open.
   
   Layout and behavior is set using CSS and JS.
  
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
  xmlns:geonet="http://www.fao.org/geonetwork" 
  xmlns:exslt="http://exslt.org/common"
  xmlns:saxon="http://saxon.sf.net/"
  extension-element-prefixes="saxon"
  exclude-result-prefixes="exslt geonet saxon">

  <!--
	editor left tab
	-->
  <xsl:template name="tab">
    <xsl:param name="schema"/>
    <xsl:param name="tabLink"/>
    <div id="tabs">

      <!-- Tab visibility is managed in config-gui.xml -->
      <!-- simple tab -->
      <xsl:if test="/root/gui/env/metadata/enableSimpleView = 'true'">
        <xsl:call-template name="mainTab">
          <xsl:with-param name="title" select="/root/gui/strings/simpleTab"/>
          <xsl:with-param name="default">simple</xsl:with-param>
          <xsl:with-param name="menu">
            <item label="simpleTab">simple</item>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:if>

      <!--  complete tab(s) -->
      <xsl:choose>
        <!-- Show complete tab with one main tab only for subtemplates to allow full editing -->
        <xsl:when test="geonet:info[isTemplate='s']">
          <xsl:call-template name="mainTab">
            <xsl:with-param name="title" select="/root/gui/strings/completeTab"/>
            <xsl:with-param name="default">advanced</xsl:with-param>
            <xsl:with-param name="menu">
              <item label="advTab">advanced</item>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <!-- metadata type-specific complete tab -->
          <xsl:variable name="tabTemplate" select="concat($schema,'CompleteTab')"/>
          <saxon:call-template name="{$tabTemplate}">
            <xsl:with-param name="tabLink" select="$tabLink"/>
            <xsl:with-param name="schema" select="$schema"/>
          </saxon:call-template>

        </xsl:otherwise>
      </xsl:choose>

      <!-- xml tab -->
      <xsl:if test="/root/gui/env/metadata/enableXmlView = 'true'">
        <xsl:call-template name="mainTab">
          <xsl:with-param name="title" select="/root/gui/strings/xmlTab"/>
          <xsl:with-param name="default">xml</xsl:with-param>
          <xsl:with-param name="menu">
            <item label="xmlTab">xml</item>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:if>
    </div>
  </xsl:template>

  <!--
	default complete tab template - schemas that don't provide a set of
	tabs can call this from their CompleteTab callback
	-->
  <xsl:template name="completeTab">
    <xsl:param name="tabLink"/>

    <xsl:call-template name="displayTab">
      <xsl:with-param name="tab" select="'metadata'"/>
      <xsl:with-param name="text" select="/root/gui/strings/completeTab"/>
      <xsl:with-param name="tabLink" select="$tabLink"/>
    </xsl:call-template>
  </xsl:template>


  <xsl:template name="mainTab">
    <xsl:param name="title"/>
    <xsl:param name="menu"/>
    <xsl:param name="default"/>
    <ul>
      <li>
        <span class="mode" id="{$default}"><xsl:value-of select="$title"/></span>
        <ul>
          <xsl:variable name="loc" select="/root/gui"/>
          <xsl:for-each select="$menu/*">
            <xsl:variable name="labelId" select="@label"/>
            <xsl:variable name="labelTextFromSchema"
                          select="$loc/schemas/*[name(.) = $schema]/strings/*[name(.) = $labelId]"/>
            <xsl:variable name="labelText"
                          select="if ($labelTextFromSchema != '') then $labelTextFromSchema else $loc/strings/*[name(.) = $labelId]"/>

            <xsl:call-template name="displayTab">
              <xsl:with-param name="tab">
                <xsl:value-of select="."/>
              </xsl:with-param>
              <xsl:with-param name="text" select="if ($labelText != '') then $labelText else $labelId"/>
              <xsl:with-param name="tabLink"/>
            </xsl:call-template>
          </xsl:for-each>
        </ul>
      </li>
    </ul>
  </xsl:template>

  <!--
	shows a tab
	-->
  <xsl:template name="displayTab">
    <xsl:param name="tab"/>
    <xsl:param name="text"/>
    <xsl:param name="tabLink"/>
    <li id="{$tab}">
      <xsl:attribute name="class">
        <xsl:if test="$currTab=$tab">active</xsl:if>
      </xsl:attribute>

      <xsl:choose>
        <!-- not active -->
        <xsl:when test="$currTab!=$tab">
          <a href="javascript:void(0);">
            <xsl:value-of select="$text"/>
          </a>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$text"/>
        </xsl:otherwise>
      </xsl:choose>
    </li>
  </xsl:template>

</xsl:stylesheet>
