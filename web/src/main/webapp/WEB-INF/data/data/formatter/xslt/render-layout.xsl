<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gn-fn-render="http://geonetwork-opensource.org/xsl/functions/render"
                xmlns:saxon="http://saxon.sf.net/"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all"
                version="2.0">

  <xsl:import href="render-variables.xsl"/>
  <xsl:import href="render-functions.xsl"/>
  <xsl:import href="render-layout-fields.xsl"/>

  <!-- Those templates should be overriden in the schema plugin - start -->
  <xsl:template mode="getMetadataTitle" match="undefined"/>
  <xsl:template mode="getMetadataAbstract" match="undefined"/>
  <!-- Those templates should be overriden in the schema plugin - end -->

  <!-- Starting point -->
  <xsl:template match="/">
    <div class="container gn-metadata-view">
      <!--<xsl:message>#Render tab: <xsl:value-of select="$tab"/></xsl:message>-->
      <article>
        <header>
          <h1><xsl:apply-templates mode="getMetadataTitle" select="$metadata"/></h1>
          <p><xsl:apply-templates mode="getMetadataAbstract" select="$metadata"/></p>
          <!-- TODO : Add thumbnail to header -->
          <xsl:apply-templates mode="render-toc" select="$viewConfig"/>
        </header>
        <xsl:apply-templates mode="render-view" select="$viewConfig/*"/>

        <footer>

        </footer>
      </article>
    </div>
  </xsl:template>



  <!-- Render list of tabs in the current view -->
  <xsl:template mode="render-toc" match="view">
    <xsl:if test="count(tab) > 1">
      <!-- TODO: Hide tabs which does not contains anything -->
      <ul class="view-outline nav nav-pills">
        <xsl:for-each select="tab">
          <li><a href="#gn-tab-{@id}">
            <xsl:value-of select="gn-fn-render:get-schema-strings($schemaStrings, @id)"/>
          </a></li>
        </xsl:for-each>
      </ul>
    </xsl:if>
  </xsl:template>



  <!-- Render a tab. Tab id is defined in strings
  and set the title of the section -->
  <xsl:template mode="render-view" match="tab">
    <xsl:variable name="content">
      <xsl:apply-templates mode="render-view" select="*|@*"/>
    </xsl:variable>

    <!-- Display a tab only if it contains something -->
    <xsl:if test="$content != ''">
      <xsl:variable name="title"
                    select="gn-fn-render:get-schema-strings($schemaStrings, @id)"/>

      <div id="gn-tab-{@id}">
        <h3 class="view-header">
          <xsl:value-of select="$title"/>
        </h3>
        <xsl:copy-of select="$content"/>
      </div>
    </xsl:if>
  </xsl:template>



  <!-- Render sections. 2 types of sections could be
  defined:
  1) section with xpath matching an element
  2) section with name create a set of fields
  -->
  <xsl:template mode="render-view" match="section[@xpath]">
    <div id="gn-view-{generate-id()}">
      <xsl:apply-templates mode="render-view" select="@xpath"/>
    </div>
  </xsl:template>

  <xsl:template mode="render-view" match="section[@name]">
    <xsl:variable name="title"
            select="gn-fn-render:get-schema-strings($schemaStrings, @name)"/>

    <div id="gn-section-{generate-id()}">
      <h3 class="view-header">
        <xsl:value-of select="$title"/>
      </h3>
      <xsl:apply-templates mode="render-view" select="*|@*"/>
    </div>
  </xsl:template>



  <!-- Render fields. -->
  <xsl:template mode="render-view" match="field[@xpath]">
    <xsl:apply-templates mode="render-view" select="@xpath"/>
  </xsl:template>


  <!-- Render metadata elements defined by XPath -->
  <xsl:template mode="render-view" match="@xpath">
    <xsl:param name="base" select="$metadata"/>

    <!-- Matching nodes -->
    <xsl:variable name="nodes">
      <saxon:call-template name="{concat('evaluate-', $schema)}">
        <xsl:with-param name="base" select="$base"/>
        <xsl:with-param name="in" select="concat('/../', .)"/>
      </saxon:call-template>
    </xsl:variable>

    <xsl:variable name="fieldName">
      <xsl:if test="../@name">
        <xsl:value-of select="gn-fn-render:get-schema-strings($schemaStrings, ../@name)"/>
      </xsl:if>
    </xsl:variable>

    <!-- A field may be based on a template -->
    <!--<xsl:variable name="subFields" select="../template/values/key"/>-->

    <xsl:for-each select="$nodes">
      <xsl:apply-templates mode="render-field">
        <xsl:with-param name="fieldName" select="$fieldName"/>
      </xsl:apply-templates>

      <!--<xsl:choose>
        <xsl:when test="$subFields">
          <xsl:variable name="currentNode" select="."/>
          <xsl:for-each select="$subFields">
            <xsl:apply-templates mode="render-view" select="@xpath">
              <xsl:with-param name="base" select="$currentNode"/>
            </xsl:apply-templates>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
        </xsl:otherwise>
      </xsl:choose>-->
    </xsl:for-each>
  </xsl:template>


  <!-- Forgot all none matching elements -->
  <xsl:template mode="render-view" match="*|@*"/>

</xsl:stylesheet>