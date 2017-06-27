<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gn-fn-render="http://geonetwork-opensource.org/xsl/functions/render"
                xmlns:gn-fn-core="http://geonetwork-opensource.org/xsl/functions/core"
                xmlns:saxon="http://saxon.sf.net/"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all"
                version="2.0">

  <xsl:import href="common/render-html.xsl"/>
  <xsl:import href="common/functions-core.xsl"/>
  <xsl:import href="render-variables.xsl"/>
  <xsl:import href="render-functions.xsl"/>
  <xsl:import href="render-layout-fields.xsl"/>

  <!-- Those templates should be overriden in the schema plugin - start -->
  <xsl:template mode="getMetadataTitle" match="undefined"/>
  <xsl:template mode="getMetadataAbstract" match="undefined"/>
  <xsl:template mode="getMetadataHierarchyLevel" match="undefined"/>
  <xsl:template mode="getMetadataHeader" match="undefined"/>
  <!-- Those templates should be overriden in the schema plugin - end -->

  <!-- Starting point -->
  <xsl:template match="/">
    <xsl:choose>
      <xsl:when test="$root = 'div'">
        <!-- Render only a DIV with the record details -->
        <xsl:call-template name="render-record"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- Render complete HTML page -->
        <xsl:call-template name="render-html">
          <xsl:with-param name="content">
            <xsl:call-template name="render-record"/>
          </xsl:with-param>
          <xsl:with-param name="title">
            <xsl:apply-templates mode="getMetadataTitle" select="$metadata"/>
          </xsl:with-param>
          <xsl:with-param name="description">
            <xsl:apply-templates mode="getMetadataAbstract" select="$metadata"/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="render-record">
    <div class="container-fluid gn-metadata-view gn-schema-{$schema}">

      <xsl:variable name="type">
        <xsl:apply-templates mode="getMetadataHierarchyLevel" select="$metadata"/>
      </xsl:variable>
      <article id="gn-metadata-view-{$metadataId}"
               itemscope="itemscope"
               itemtype="{gn-fn-core:get-schema-org-class($type)}">
        <header>
          <h1>
            <xsl:apply-templates mode="getMetadataTitle" select="$metadata"/>
          </h1>
          <!--<p><xsl:apply-templates mode="getMetadataAbstract" select="$metadata"/></p>-->
          <!-- TODO : Add thumbnail to header -->

          <xsl:apply-templates mode="getMetadataHeader" select="$metadata"/>
          <!--<xsl:apply-templates mode="render-toc" select="$viewConfig"/>-->
        </header>
        <xsl:apply-templates mode="render-view" select="$viewConfig/*"/>
        <!--
        TODO: scrollspy or tabs on header ?
        <div class="gn-scroll-spy"
             data-gn-scroll-spy="gn-metadata-view-{$metadataId}"
             data-watch=""
             data-filter="div > h3"/>-->
        <footer>

        </footer>
      </article>
      <br/>
      <br/>
    </div>
  </xsl:template>




  <!-- Render list of tabs in the current view -->
  <xsl:template mode="render-toc" match="view">
    <xsl:if test="count(tab) > 1">
      <!-- TODO: Hide tabs which does not contains anything -->
      <ul class="view-outline nav nav-pills">
        <xsl:for-each select="tab">
          <li>
            <a href="#gn-tab-{@id}">
              <xsl:value-of select="gn-fn-render:get-schema-strings($schemaStrings, @id)"/>
            </a>
          </li>
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
  <xsl:template mode="render-view"
                match="section[@xpath]">
    <div id="gn-view-{generate-id()}">
      <xsl:apply-templates mode="render-view" select="@xpath"/>
    </div>
  </xsl:template>


  <xsl:template mode="render-view"
                match="section[not(@xpath)]">
    <div id="gn-section-{generate-id()}">
      <xsl:if test="@name">
        <xsl:variable name="title"
                      select="gn-fn-render:get-schema-strings($schemaStrings, @name)"/>

        <xsl:element name="h{3 + count(ancestor-or-self::*[name(.) = 'section'])}">
          <xsl:attribute name="class" select="'view-header'"/>
          <xsl:value-of select="$title"/>
        </xsl:element>
      </xsl:if>
      <xsl:apply-templates mode="render-view"
                           select="section|field"/>
    </div>
  </xsl:template>


  <!-- Render metadata elements defined by XPath -->
  <xsl:template mode="render-view"
                match="field[not(template)]">
    <xsl:param name="base" select="$metadata"/>

    <!-- Matching nodes -->
    <xsl:variable name="nodes">
      <saxon:call-template name="{concat('evaluate-', $schema)}">
        <xsl:with-param name="base" select="$base"/>
        <xsl:with-param name="in" select="concat('/../', @xpath)"/>
      </saxon:call-template>
    </xsl:variable>

    <xsl:variable name="fieldName">
      <xsl:if test="@name">
        <xsl:value-of select="gn-fn-render:get-schema-strings($schemaStrings, @name)"/>
      </xsl:if>
    </xsl:variable>

    <xsl:for-each select="$nodes">
      <xsl:apply-templates mode="render-field">
        <xsl:with-param name="fieldName" select="$fieldName"/>
      </xsl:apply-templates>
    </xsl:for-each>
  </xsl:template>


  <xsl:template mode="render-view"
                match="field[template]"
                priority="2">
    <xsl:param name="base" select="$metadata"/>
    <xsl:if test="@name">
      <xsl:variable name="title"
                    select="gn-fn-render:get-schema-strings($schemaStrings, @name)"/>

      <xsl:element name="h{3 + 1 + count(ancestor-or-self::*[name(.) = 'section'])}">
        <xsl:attribute name="class" select="'view-header'"/>
        <xsl:value-of select="$title"/>
      </xsl:element>
    </xsl:if>

    <xsl:variable name="fieldXpath"
                  select="@xpath"/>
    <xsl:variable name="fields" select="template/values/key"/>

    <xsl:variable name="elements">
      <saxon:call-template name="{concat('evaluate-', $schema)}">
        <xsl:with-param name="base" select="$base"/>
        <xsl:with-param name="in"
                        select="concat('/../', $fieldXpath)"/>
      </saxon:call-template>
    </xsl:variable>

    <!-- Loop on each element matching current field -->
    <xsl:for-each select="$elements/*">
      <xsl:variable name="element" select="."/>

      <!-- Loop on each fields -->
      <xsl:for-each select="$fields">
        <xsl:variable name="nodes">
          <saxon:call-template name="{concat('evaluate-', $schema)}">
            <xsl:with-param name="base" select="$element"/>
            <xsl:with-param name="in"
                            select="concat('/./',
                           replace(@xpath, '/gco:CharacterString', ''))"/>
          </saxon:call-template>
        </xsl:variable>

        <xsl:variable name="fieldName">
          <xsl:if test="@label">
            <xsl:value-of select="gn-fn-render:get-schema-strings($schemaStrings, @label)"/>
          </xsl:if>
        </xsl:variable>

        <xsl:for-each select="$nodes">
          <xsl:apply-templates mode="render-field">
            <xsl:with-param name="fieldName" select="$fieldName"/>
          </xsl:apply-templates>
        </xsl:for-each>
      </xsl:for-each>
    </xsl:for-each>
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

    <xsl:for-each select="$nodes">
      <xsl:apply-templates mode="render-field">
        <xsl:with-param name="fieldName" select="$fieldName"/>
      </xsl:apply-templates>
    </xsl:for-each>
  </xsl:template>


  <!-- Forgot all none matching elements -->
  <xsl:template mode="render-view" match="*|@*"/>
  <xsl:template mode="render-field" match="*|@*|text()"/>

</xsl:stylesheet>
