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
  <xsl:import href="common/utility-tpl.xsl"/>

  <xsl:import href="render-variables.xsl"/>
  <xsl:import href="render-functions.xsl"/>
  <xsl:import href="render-layout-fields.xsl"/>

  <xsl:output method="html"/>

  <!-- Those templates should be overriden in the schema plugin - start -->
  <xsl:template mode="getMetadataTitle" match="*"/>
  <xsl:template mode="getMetadataAbstract" match="*"/>
  <xsl:template mode="getMetadataHierarchyLevel" match="*"/>
  <xsl:template mode="getOverviews" match="*"/>
  <xsl:template mode="getMetadataThumbnail" match="*"/>
  <xsl:template mode="getMetadataHeader" match="*"/>
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
          <xsl:with-param name="type">
            <xsl:apply-templates mode="getMetadataHierarchyLevel" select="$metadata"/>
          </xsl:with-param>
          <xsl:with-param name="thumbnail">
            <xsl:apply-templates mode="getMetadataThumbnail" select="$metadata"/>
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

      <xsl:variable name="title">
        <xsl:apply-templates mode="getMetadataTitle" select="$metadata"/>
      </xsl:variable>


      <article id="gn-metadata-view-{$metadataId}"
               class="gn-md-view gn-metadata-display"
               itemscope="itemscope"
               itemtype="{gn-fn-core:get-schema-org-class($type)}">



        <div class="row">
          <div class="col-md-8">

            <header>
              <h1 itemprop="name"
                  itemscope="itemscope"
                  itemtype="http://schema.org/name">
                <i class="fa gn-icon-{$type}">&#160;</i>
                <xsl:value-of select="$title"/>
              </h1>

              <xsl:apply-templates mode="getMetadataHeader" select="$metadata"/>

              <div gn-related="md"
                   data-user="user"
                   data-types="onlines">&#160;</div>
            </header>

            <div>
              <xsl:apply-templates mode="render-toc" select="$viewConfig"/>
              <!-- Tab panes -->
              <div>
                <xsl:if test="$tabs = 'true'">
                  <xsl:attribute name="class" select="'tab-content'"/>
                </xsl:if>
                <xsl:for-each select="$viewConfig/*">
                  <xsl:sort select="@formatter-order"
                            data-type="number"/>
                  <xsl:apply-templates mode="render-view"
                                       select="."/>
                </xsl:for-each>
              </div>
            </div>
          </div>
          <div class="gn-md-side gn-md-side-advanced col-md-4">
            <xsl:apply-templates mode="getOverviews" select="$metadata"/>

            <section class="gn-md-side-providedby">
              <h4>
                <i class="fa fa-fw fa-cog">&#160;</i>
                <span><xsl:value-of select="$schemaStrings/providedBy"/></span>
              </h4>
              <img class="gn-source-logo"
                   src="{$nodeUrl}../images/logos/{$source}.png" />
            </section>

            <xsl:if test="$isSocialbarEnabled">
              <section class="gn-md-side-social">
                <h4>
                  <i class="fa fa-fw fa-share-square-o">&#160;</i>
                  <span><xsl:value-of select="$schemaStrings/shareOnSocialSite"/></span>
                </h4>
                <a href="https://twitter.com/share?url={encode-for-uri($nodeUrl)}api%2Frecords%2F{$metadataUuid}"
                  target="_blank" class="btn btn-default">
                  <i class="fa fa-fw fa-twitter">&#160;</i>
                </a>
                <a href="https://plus.google.com/share?url={encode-for-uri($nodeUrl)}api%2Frecords%2F{$metadataUuid}"
                  target="_blank" class="btn btn-default">
                  <i class="fa fa-fw fa-google-plus">&#160;</i>
                </a>
                <a href="https://www.facebook.com/sharer.php?u={encode-for-uri($nodeUrl)}api%2Frecords%2F{$metadataUuid}"
                  target="_blank" class="btn btn-default">
                  <i class="fa fa-fw fa-facebook">&#160;</i>
                </a>
                <a href="http://www.linkedin.com/shareArticle?mini=true&amp;summary=&amp;url={encode-for-uri($nodeUrl)}api%2Frecords%2F{$metadataUuid}"
                  target="_blank" class="btn btn-default">
                  <i class="fa fa-fw fa-linkedin">&#160;</i>
                </a>
                <a href="mailto:?subject={$title}&amp;body={encode-for-uri($nodeUrl)}api%2Frecords%2F{$metadataUuid}"
                  target="_blank" class="btn btn-default">
                  <i class="fa fa-fw fa-envelope-o">&#160;</i>
                </a>
              </section>
            </xsl:if>

            <!-- Display link to portal and other view only
            when in pure HTML mode. -->
            <xsl:if test="$root != 'div'">
              <section class="gn-md-side-viewmode">
                <h4>
                  <i class="fa fa-fw fa-eye">&#160;</i>
                  <span><xsl:value-of select="$schemaStrings/viewMode"/></span>
                </h4>
                <xsl:for-each select="$configuration/editor/views/view[not(@disabled)]">
                  <ul>
                    <li>
                      <a>
                        <xsl:attribute name="href">
                          <xsl:choose>
                            <xsl:when test="@name = 'xml'">
                              <xsl:value-of select="concat($nodeUrl, 'api/records/', $metadataUuid, '/formatters/xml')"/>
                            </xsl:when>
                            <xsl:otherwise>
                              <xsl:value-of select="concat($nodeUrl, 'api/records/', $metadataUuid, '/formatters/xsl-view?view=', @name, '&amp;portalLink=', $portalLink)"/>
                            </xsl:otherwise>
                          </xsl:choose>
                        </xsl:attribute>
                        <xsl:variable name="name" select="@name"/>
                        <xsl:value-of select="$schemaStrings/*[name(.) = $name]"/>
                      </a>
                    </li>
                  </ul>
                </xsl:for-each>
              </section>

              <section class="gn-md-side-access">
                <div class="well text-center">
                  <span itemprop="identifier"
                      itemscope="itemscope"
                      itemtype="http://schema.org/identifier"
                      class="hidden">
                    <xsl:value-of select="$metadataUuid"/>
                  </span>
                  <a itemprop="url"
                     itemscope="itemscope"
                     itemtype="http://schema.org/url"
                     class="btn btn-block btn-primary"
                     href="{if ($portalLink != '')
                            then replace($portalLink, '\$\{uuid\}', $metadataUuid)
                            else concat($nodeUrl, $language, '/catalog.search#/metadata/', $metadataUuid)}">
                    <i class="fa fa-fw fa-link">&#160;</i>
                    <xsl:value-of select="$schemaStrings/linkToPortal"/>
                  </a>
                  <xsl:value-of select="$schemaStrings/linkToPortal-help"/>
                </div>
              </section>
            </xsl:if>

            <section class="gn-md-side-associated">
              <h4>
                <i class="fa fa-fw fa-link">&#160;</i>
                <span><xsl:value-of select="$schemaStrings/associatedResources"/></span>
              </h4>
              <div gn-related="md"
                   data-user="user"
                   data-types="parent|children|services|datasets|hassources|sources|fcats|siblings|associated">
                Not available
              </div>
            </section>
          </div>
        </div>

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
    <xsl:if test="$tabs = 'true' and count(tab) > 1">
      <ul class="view-outline nav nav-tabs nav-tabs-advanced">
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
        <xsl:if test="count(following-sibling::tab) > 0">
          <xsl:attribute name="class" select="'tab-pane'"/>
        </xsl:if>
        <h1 class="view-header">
          <xsl:value-of select="$title"/>
        </h1>
        <xsl:choose>
          <xsl:when test="normalize-space($content) = ''">
            No information
          </xsl:when>
          <xsl:otherwise>
            <xsl:copy-of select="$content"/>&#160;
          </xsl:otherwise>
        </xsl:choose>
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
    <div id="gn-view-{generate-id()}" class="gn-tab-content">
      <xsl:apply-templates mode="render-view" select="@xpath"/>
    </div>
  </xsl:template>


  <xsl:template mode="render-view"
                match="section[not(@xpath)]">
    <div id="gn-section-{generate-id()}" class="gn-tab-content">
      <xsl:if test="@name">
        <xsl:variable name="title"
                      select="gn-fn-render:get-schema-strings($schemaStrings, @name)"/>

        <xsl:element name="h{3 + count(ancestor-or-self::*[name(.) = 'section'])}">
          <xsl:attribute name="class" select="'view-header'"/>
          <xsl:value-of select="$title"/>
        </xsl:element>
      </xsl:if>
      <xsl:apply-templates mode="render-view"
                           select="section|field"/>&#160;
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
