<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gn-fn-render="http://geonetwork-opensource.org/xsl/functions/render"
                xmlns:gn="http://www.fao.org/geonetwork"
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
          <xsl:with-param name="header" select="$catalogueHeader != 'false'"/>
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
    <div class="container-fluid gn-metadata-view gn-view-{$view} gn-schema-{$schema}">

      <xsl:choose>
        <!-- This mode will be deprecated once the v5 is dropped. -->
        <xsl:when test="$css = 'sextant'">
          <link rel="stylesheet" type="text/css" href="../../apps/js/ext/resources/css/ext-all.css"/>
          <link rel="stylesheet" type="text/css" href="../../apps/sextant/css/gndefault.css"/>
          <link rel="stylesheet" type="text/css" href="../../apps/sextant/css/gnmetadatadefault.css"/>
          <link rel="stylesheet" type="text/css" href="../../apps/sextant/css/metadata-view.css"/>
        </xsl:when>
        <!-- Avoid script injection. Only allows letters, digits and '-', '_' in API id-->
        <xsl:when test="matches($css, '^[A-Za-z0-9-_]+$')">
          <link rel="stylesheet" type="text/css" href="{$nodeUrl}../static/api-{$css}.css"/>
        </xsl:when>
      </xsl:choose>

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

        <!-- Custom header for http://portal.emodnet-bathymetry.eu/
        Taken from https://sextant-test.ifremer.fr/www/API-Sextant/emodnet_bathymetry/12_RegionForTest_CTC.html#/map
        -->
        <xsl:if test="$css = 'emodnet-bathymetry-portal'">
          <div style="height: 8rem; width: 100%; background-image: url(/geonetwork/images/emodnetheader.jpg); background-size: auto 8rem; line-height: 8rem; user-select: none; margin-bottom: 10px;">
            <a class=""
               style="vertical-align: top; background-image: url(/geonetwork/images/emodnet.png); background-size: auto 7rem; background-repeat: no-repeat; background-position: center; text-indent: -9999px; width: 12rem; height: 8rem; display: inline-block; font-size: 1em;"
               href="http://www.emodnet-bathymetry.eu"
               title="EMODnet Bathymetry"
               target="_blank">&amp;nbsp;</a>
            <div style="display: inline-block; font-weight: bold; line-height: normal; margin: 1.2em 0; border-left: .3em solid #F8BC00; padding: 0 0 0 2em;">
              <h1 style="margin: 0px; padding: 0px; color: #222 !important; font-size: 2.4em; text-transform: uppercase;">Bathymetry</h1>
              <p style="font-size: 0.8em; font-style: italic; margin: 0px; padding: 0px; color: #888;">Understanding the topography of the European seas</p>
              <h2 style="margin: 0px; padding: 0px; font-weight: bold; font-size: 0.8em;">Composite DTMs Discovery Service</h2>
            </div>
          </div>
        </xsl:if>


        <div class="row">
          <div class="col-md-9">

            <header>
              <xsl:if test="$header != 'false'">
                <h1 itemprop="name"
                    itemscope="itemscope"
                    itemtype="http://schema.org/name">
                  <i class="fa gn-icon-{$type}">&#160;</i>
                  <xsl:value-of select="$title"/>
                </h1>
              </xsl:if>


              <xsl:if test="$view != 'emodnetHydrography' and $view != 'sdn'">
                <xsl:apply-templates mode="getMetadataHeader" select="$metadata"/>

                <div gn-related="md"
                    data-user="user"
                    data-types="onlines">&#160;</div>
              </xsl:if>

              <!--<xsl:apply-templates mode="render-toc" select="$viewConfig"/>-->
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
          <div class="gn-md-side gn-md-side-advanced col-md-3">

            <section class="links"
                     ng-show="downloads.length > 0 || links.length > 0 ||layers.length > 0">
              <h3 translate="">accessData</h3>
              <sxt-links-btn>&#160;</sxt-links-btn>
            </section>

            <br/>

            <xsl:apply-templates mode="getOverviews" select="$metadata"/>

            <br/>

            <xsl:if test="$css != 'checkpoint' and
                          $view != 'emodnetHydrography' and $view != 'sdn'">
              <section>
                <h4>
                  <i class="fa fa-fw fa-cog">&#160;</i>&#160;
                  <span><xsl:value-of select="$schemaStrings/providedBy"/></span>
                </h4>
                <img class="gn-source-logo"
                     src="{$nodeUrl}../images/logos/{$source}.png">&#160;</img>
              </section>

              <br/>

              <section>
                <h4>
                  <i class="fa fa-fw fa-share-square-o">&#160;</i>&#160;
                  <span><xsl:value-of select="$schemaStrings/shareOnSocialSite"/></span>
                </h4>
                <a href="https://twitter.com/share?url={encode-for-uri($nodeUrl)}api%2Frecords%2F{$metadataUuid}"
                   target="_blank" class="btn btn-default">
                  <i class="fa fa-fw fa-twitter">&#160;</i>&#160;
                </a>
                <a href="https://plus.google.com/share?url={encode-for-uri($nodeUrl)}api%2Frecords%2F{$metadataUuid}"
                   target="_blank" class="btn btn-default">
                  <i class="fa fa-fw fa-google-plus">&#160;</i>&#160;
                </a>
                <a href="https://www.facebook.com/sharer.php?u={encode-for-uri($nodeUrl)}api%2Frecords%2F{$metadataUuid}"
                   target="_blank" class="btn btn-default">
                  <i class="fa fa-fw fa-facebook">&#160;</i>&#160;
                </a>
                <a href="http://www.linkedin.com/shareArticle?mini=true&amp;summary=Hydrological Basins in Africa (Sample record, please remove!)&amp;url={encode-for-uri($nodeUrl)}api%2Frecords%2F{$metadataUuid}"
                   target="_blank" class="btn btn-default">
                  <i class="fa fa-fw fa-linkedin">&#160;</i>&#160;
                </a>
                <a href="mailto:?subject={$title}&amp;body={encode-for-uri($nodeUrl)}api%2Frecords%2F{$metadataUuid}"
                   target="_blank" class="btn btn-default">
                  <i class="fa fa-fw fa-envelope-o">&#160;</i>&#160;
                </a>
              </section>

              <br/>

              <!-- Display link to portal and other view only
              when in pure HTML mode. -->
              <xsl:if test="$root != 'div'">
                <section>
                  <h4>
                    <i class="fa fa-fw fa-eye">&#160;</i>&#160;
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
              </xsl:if>

              <br/>

              <div class="well text-center">
                <a class="btn btn-block btn-primary"
                   href="{if ($portalLink != '')
                          then replace($portalLink, '\$\{uuid\}', $metadataUuid)
                          else concat($nodeUrl, $language, '/catalog.search#/metadata/', $metadataUuid)}">
                  <i class="fa fa-link">&#160;</i>
                  <xsl:value-of select="$schemaStrings/linkToPortal"/>
                </a>
                <xsl:value-of select="$schemaStrings/linkToPortal-help"/>
              </div>


              <section>
                <h4>
                  <i class="fa fa-fw fa-link">&#160;</i>&#160;
                  <span><xsl:value-of select="$schemaStrings/associatedResources"/></span>
                </h4>
                <div gn-related="md"
                     data-user="user"
                     data-types="parent|children|services|datasets|hassources|sources|fcats|siblings|associated">
                  Not available
                </div>
              </section>
            </xsl:if>
          </div>
        </div>

        <!--
        TODO: scrollspy or tabs on header ?
        <div class="gn-scroll-spy"
             data-gn-scroll-spy="gn-metadata-view-{$metadataId}"
             data-watch=""
             data-filter="div > h3"/>-->
        <footer>
          <xsl:if test="$view = 'emodnetHydrography'">
            <div gn-related="md"
                data-user="user"
                data-types="onlines">&#160;</div>
          </xsl:if>
        </footer>
      </article>

      <xsl:if test="$css = 'sextant'">
        <!-- Avoid self closing tag. -->
        <script src="../../static/lib.js">;</script>
        <script src="../../static/gn_search_sextant.js">;</script>
        <script type="text/javascript">
        var module = angular.module('gn_search');
        </script>
      </xsl:if>
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
        <xsl:if test="count(../tab) != 1">
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

  <!-- Quality tab for Checkpoint is a custom layout
  made on client side based on an Angular directive. -->
  <xsl:template mode="render-view"
                priority="200"
                match="tab[@id = 'checkpoint-tdp-qm' or
                           @id = 'checkpoint-dps-dq' or
                           @id = 'checkpoint-ud-qm']">

    <xsl:variable name="content">
      <xsl:apply-templates mode="render-field"
                           select="$metadata//*:dataQualityInfo"/>
    </xsl:variable>

    <!-- Display a tab only if it contains something -->
    <xsl:if test="$content/*">
      <xsl:variable name="title"
                    select="gn-fn-render:get-schema-strings($schemaStrings, @id)"/>

      <div id="gn-tab-{@id}" class="tab-pane">
        <h3 class="view-header">
          <xsl:value-of select="$title"/>
        </h3>
        <div id="gn-view-{generate-id()}" class="gn-tab-content">
          <xsl:copy-of select="$content"/>
        </div>
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
                           select="section|field|xsl"/>&#160;
    </div>
  </xsl:template>


  <xsl:template mode="render-view"
                match="xsl">
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
                           select="@xpath"/>&#160;
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
    <xsl:variable name="depth"
                  select="3 + 1 + count(ancestor-or-self::*[name(.) = 'section'])"/>
    <xsl:variable name="fieldName"
                  select="@name"/>
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

      <xsl:if test="$fieldName">
        <xsl:variable name="title"
                      select="gn-fn-render:get-schema-strings($schemaStrings, $fieldName)"/>

        <xsl:element name="h{$depth}">
          <xsl:attribute name="class" select="'view-header'"/>
          <xsl:value-of select="replace($title, '\*', '')"/>
        </xsl:element>
      </xsl:if>

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
            <xsl:with-param name="fieldName" select="replace($fieldName, '\*', '')"/>
            <!-- In EMODNet contact, an extra hyperlink is added
            to the contact using a uuid attribute.
            <gmd:CI_ResponsibleParty uuid="http://seadatanet.maris2.nl/v_edmo/print.asp?n_code=2467">
              <gmd:organisationName>
            There is no super generic way to achieve that, as we're
            here in the default standard template looping over template fields.

            For now, adding an extra parameter to set the field content with an hyperlink when we are in an organisation name.
            -->
            <xsl:with-param name="link"
                            select="if (name(*[1]) = 'gmd:organisationName') then $element/*/@uuid else ''"/>
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
