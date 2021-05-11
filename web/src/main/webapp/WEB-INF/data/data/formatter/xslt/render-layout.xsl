<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gn-fn-render="http://geonetwork-opensource.org/xsl/functions/render"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:gn-fn-core="http://geonetwork-opensource.org/xsl/functions/core"
                xmlns:utils="java:org.fao.geonet.util.XslUtil"
                xmlns:tr="java:org.fao.geonet.api.records.formatters.SchemaLocalizations"
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
  <xsl:template mode="getExtent" match="*"/>
  <xsl:template mode="getLicense" match="*"/>
  <xsl:template mode="getTags" match="*"/>
  <xsl:template mode="getMetadataThumbnail" match="*"/>
  <xsl:template mode="getMetadataHeader" match="*"/>
  <xsl:template mode="getMetadataCitation" match="*"/>
  <xsl:template mode="getJsonLD" match="*"/>
  <!-- Those templates should be overridden in the schema plugin - end -->

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
            <xsl:variable name="title">
              <xsl:apply-templates mode="getMetadataTitle" select="$metadata"/>
            </xsl:variable>
            <xsl:copy-of select="if ($title/div) then $title/div[1] else $title"/>
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
          <xsl:with-param name="meta">
            <xsl:call-template name="render-language-meta"/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
  https://support.google.com/webmasters/answer/189077
  -->
  <xsl:template name="render-language-meta">
    <xsl:variable name="metadataOtherLanguages">
      <saxon:call-template name="{concat('get-', $schema, '-other-languages')}"/>
    </xsl:variable>

    <xsl:variable name="defaultLanguage"
                  select="$metadataOtherLanguages/*[position() = last()]/@code"/>

    <xsl:for-each select="$metadataOtherLanguages/*">
      <link rel="alternate"
            hreflang="{utils:twoCharLangCode(@code)}"
            href="{$nodeUrl}api/records/{$metadataUuid}?language={@code}" />
    </xsl:for-each>
    <xsl:if test="count($metadataOtherLanguages/*) > 1">
      <link rel="alternate"
            hreflang="x-default"
            href="{$nodeUrl}api/records/{$metadataUuid}?language=all" />
    </xsl:if>
  </xsl:template>


  <xsl:template name="render-language-switcher">
    <xsl:if test="$language = 'all'">
      <xsl:variable name="metadataOtherLanguages">
        <saxon:call-template name="{concat('get-', $schema, '-other-languages')}"/>
      </xsl:variable>
      <xsl:variable name="metadataMainLanguages">
        <saxon:call-template name="{concat('get-', $schema, '-language')}"/>
      </xsl:variable>
      <xsl:variable name="defaultLanguage"
                    select="$metadataOtherLanguages/*[position() = last()]/@code"/>

      <div class="gn-multilingual-field">
        <ul class="nav nav-pills">
          <script src="{$nodeUrl}../catalog/js/GnLandingPageLib.js?v={$buildNumber}">&amp;nbsp;</script>
          <script type="text/javascript">
            window.onload = function() {
              document.getElementById('gn-default-lang-link').click();
            };
          </script>
          <xsl:for-each select="($metadataOtherLanguages/*[@default], $metadataOtherLanguages/*[not(@default)])">
            <li>
              <a id="{if (@default) then 'gn-default-lang-link' else ''}"
                 onclick="gnLandingPage.displayLanguage('{@code}', this);">
                <xsl:variable name="label"
                              select="utils:getIsoLanguageLabel(@code, @code)"/>
                <xsl:value-of select="if ($label != '') then $label else @code"/><xsl:text> </xsl:text>
              </a>
            </li>
          </xsl:for-each>

          <xsl:if test="count($metadataOtherLanguages/*) = 0 and $metadataMainLanguages">
            <li class="active">
              <a id="gn-default-lang-link"
                 onclick="gnLandingPage.displayLanguage('{$metadataMainLanguages}', this);">
                <xsl:variable name="label"
                              select="utils:getIsoLanguageLabel($metadataMainLanguages, $metadataMainLanguages)"/>
                <xsl:value-of select="if ($label != '') then $label else @code"/><xsl:text> </xsl:text>
              </a>
            </li>
          </xsl:if>

          <xsl:if test="count($metadataOtherLanguages/*) > 1">
            <li class="active">
              <a onclick="gnLandingPage.displayLanguage('', this);">
                <xsl:value-of select="'All'"/>
              </a>
            </li>
          </xsl:if>
        </ul>
      </div>
      <br/>

      <xsl:variable name="defaultUrlConfig">
        <url>
          <fre>https://sextant.ifremer.fr/Donnees/Catalogue#/metadata/${uuid}</fre>
          <eng>https://sextant.ifremer.fr/eng/Data/Catalogue#/metadata/${uuid}</eng>
        </url>
      </xsl:variable>

      <xsl:variable name="portalLinkConfig">
        <xsl:choose>
          <xsl:when test="$portalLink = 'group'">
            <xsl:variable name="groupInfo"
                          select="utils:getGroupDetails($groupOwner)"/>
            <xsl:variable name="groupUrlConfig"
                          select="$groupInfo/record/website/text()"/>
            <xsl:choose>
              <xsl:when test="starts-with($groupUrlConfig, '&lt;')">
                <xsl:copy-of select="saxon:parse($groupUrlConfig)"/>
              </xsl:when>
              <xsl:when test="starts-with($groupUrlConfig, 'http')">
                <url>
                  <eng><xsl:value-of select="$groupUrlConfig"/></eng>
                  <fre><xsl:value-of select="$groupUrlConfig"/></fre>
                </url>
              </xsl:when>
              <xsl:otherwise>
                <xsl:copy-of select="$defaultUrlConfig"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <xsl:copy-of select="$defaultUrlConfig"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>


      <section class="gn-md-side-access">
        <div class="well text-center">
          <xsl:for-each select="distinct-values(($metadataMainLanguages, $metadataOtherLanguages/*[@default]/@code, $metadataOtherLanguages/*[not(@default)]/@code))">

            <xsl:variable name="portalLinkForLanguage"
                          select="if ($portalLinkConfig/url/*[name() = current()])
                                  then $portalLinkConfig/url/*[name() = current()]/text()
                                  else 'https://sextant.ifremer.fr/Donnees/Catalogue#/metadata/${uuid}'"/>
            <a class="btn btn-block btn-primary"
               xml:lang="{current()}"
               href="{replace($portalLinkForLanguage, '\$\{uuid\}', $metadataUuid)}">
              <i class="fa fa-fw fa-link"><xsl:comment select="'icon'"/></i>

              <xsl:call-template name="landingpage-label">
                <xsl:with-param name="key" select="'linkToPortal'"/>
              </xsl:call-template>
            </a>
          </xsl:for-each>

          <xsl:call-template name="landingpage-label">
            <xsl:with-param name="key" select="'linkToPortal-help'"/>
          </xsl:call-template>
        </div>
      </section>
      <br/>


    </xsl:if>
  </xsl:template>


  <xsl:template name="render-record">

    <xsl:if test="$portalLink != ''">
      <link rel="stylesheet" type="text/css"
            href="{$nodeUrl}../catalog/views/sextant/landing-pages/default/styles.css"/>
      <link rel="stylesheet" type="text/css"
            href="{$nodeUrl}../catalog/views/sextant/landing-pages/{$groupOwner}/styles.css"/>
      <!-- Default head -->
      <div class="gn-landing-page-header gn-landing-page-header-default">
        <div class="col-md-1">
          <img src="{$nodeUrl}../catalog/views/sextant/landing-pages/default/Logo-Sextant.png"/>
        </div>
        <div class="col-md-10">
          <h1>Sextant</h1>
          <div class="subtitle" xml:lang="fre">Infrastructure de données géographiques marines et littorales</div>
          <div class="subtitle" xml:lang="eng">Spatial Data Infrastructure for Marine Environments</div>
        </div>
        <div class="col-md-1">
          <img src="{$nodeUrl}../catalog/views/sextant/landing-pages/default/logo.png"/>
        </div>
      </div>

      <div class="gn-landing-page-header">&#160;</div>
    </xsl:if>

    <div class="container gn-metadata-view gn-view-{$view} gn-schema-{$schema}">

      <xsl:choose>
        <!-- Avoid script injection. Only allows letters, digits and '-', '_' in API id-->
        <xsl:when test="$css != sextant and matches($css, '^[A-Za-z0-9-_]+$')">
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
               class="gn-md-view gn-metadata-display">

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
                <h1>
                  <i class="fa gn-icon-{$type}"><xsl:comment select="'icon'"/></i>
                  <xsl:copy-of select="$title"/>
                </h1>
              </xsl:if>


              <div>
                <xsl:apply-templates mode="getMetadataHeader" select="$metadata"/>
              </div>
              <xsl:if test="$related != ''">
                <div gn-related="md"
                     data-user="user"
                     data-types="{$related}">&#160;</div>
              </xsl:if>

            </header>
            <div>
              <xsl:choose>
                <xsl:when test="$template != ''">
                  <saxon:call-template name="{$template}"/>
                </xsl:when>
                <xsl:otherwise>
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
                </xsl:otherwise>
              </xsl:choose>

              <xsl:if test="$citation = 'true'">
                <xsl:apply-templates mode="getMetadataCitation" select="$metadata"/>
              </xsl:if>
            </div>
          </div>
          <div class="gn-md-side gn-md-side-advanced col-md-3">
            <xsl:call-template name="render-language-switcher"/>

            <xsl:if test="$language != all and $portalLink != ''">
              <xsl:variable name="defaultUrl"
                            select="concat('https://sextant.ifremer.fr/Donnees/Catalogue#/metadata/', $metadataUuid)"/>
              <!--<xsl:variable name="defaultUrl"
                            select="concat($nodeUrl,
                                      if($language = 'all') then 'eng' else $language,
                                      '/catalog.search#/metadata/', $metadataUuid)"/>-->
              <xsl:variable name="portalUrl">
                <xsl:choose>
                  <xsl:when test="$portalLink = 'default'">
                    <xsl:value-of select="$defaultUrl"/>
                  </xsl:when>
                  <xsl:when test="$portalLink = 'group'">
                    <xsl:variable name="groupInfo"
                                  select="utils:getGroupDetails($groupOwner)"/>
                    <xsl:value-of select="if ($groupInfo/record/website/text() != '')
                                          then $groupInfo/record/website/text()
                                          else $defaultUrl"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="$portalLink"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:variable>

              <section class="gn-md-side-access">
                <div class="well text-center">
                  <a class="btn btn-block btn-primary"
                     href="{replace($portalUrl, '\$\{uuid\}', $metadataUuid)}">
                    <i class="fa fa-fw fa-link"><xsl:comment select="'icon'"/></i>

                    <xsl:call-template name="landingpage-label">
                      <xsl:with-param name="key" select="'linkToPortal'"/>
                    </xsl:call-template>
                  </a>

                  <xsl:call-template name="landingpage-label">
                    <xsl:with-param name="key" select="'linkToPortal-help'"/>
                  </xsl:call-template>
                  <!--<a href="http://www.linkedin.com/shareArticle?mini=true&amp;summary=&amp;url={encode-for-uri($nodeUrl)}api%2Frecords%2F{$metadataUuid}"
                     target="_blank" class="btn btn-default">
                    <i class="fa fa-fw fa-linkedin">&#160;</i>&#160;
                  </a>
                  <a href="mailto:?subject={$title}&amp;body={encode-for-uri($nodeUrl)}api%2Frecords%2F{$metadataUuid}"
                     target="_blank" class="btn btn-default">
                    <i class="fa fa-fw fa-envelope-o">&#160;</i>&#160;
                  </a>-->
                </div>
              </section>
              <br/>
            </xsl:if>

            <section class="links"
                     ng-show="downloads.length > 0 || links.length > 0 ||layers.length > 0">
              <h3 translate="">accessData</h3>
              <sxt-links-btn>&#160;</sxt-links-btn>
              <!--<br/>
              <xsl:apply-templates mode="getLicense" select="$metadata"/>-->
            </section>

            <br/>

            <xsl:apply-templates mode="getOverviews" select="$metadata"/>
            <xsl:apply-templates mode="getExtent" select="$metadata"/>

            <xsl:if test="$view != 'sdn' and $view != 'emodnetHydrography'">
              <!-- https://gitlab.ifremer.fr/sextant/geonetwork/issues/112 -->
              <xsl:apply-templates mode="getTags" select="$metadata">
                <xsl:with-param name="byThesaurus" select="true()"/>
              </xsl:apply-templates>
            </xsl:if>


            <br/>
            <xsl:if test="$css != 'checkpoint' and
                          $view != 'emodnetHydrography' and
                          $view != 'earthObservation' and
                          $view != 'sdn'">

              <!--<section class="gn-md-side-providedby">
                <h2>
                  <i class="fa fa-fw fa-cog"><xsl:comment select="'icon'"/></i>
                  <span><xsl:value-of select="$schemaStrings/providedBy"/></span>
                </h2>
                <img class="gn-source-logo"
                     alt="{$schemaStrings/logo}"
                     src="{$nodeUrl}../images/logos/{$source}.png" />
              </section>-->

              <xsl:if test="$isSocialbarEnabled and $output != 'pdf'">
                <section class="gn-md-side-social">
                  <h3>
<!--                    <i class="fa fa-fw fa-share-square-o"><xsl:comment select="'icon'"/></i>-->
                    <span>
                      <xsl:call-template name="landingpage-label">
                        <xsl:with-param name="key" select="'shareOnSocialSite'"/>
                      </xsl:call-template>
                    </span>
                  </h3>

                  <!-- href="{$nodeUrl}api/records/{$metadataUuid}" -->
                  <a data-ng-click="getSextantPermalink(md)"
                     class="btn btn-default">
                    <i class="fa fa-fw fa-link">&#160;</i>
                  </a>
                  <a href="https://twitter.com/share?url={encode-for-uri($nodeUrl)}api%2Frecords%2F{$metadataUuid}"
                     target="_blank"
                     aria-label="Twitter"
                     class="btn btn-default">
                    <i class="fa fa-fw fa-twitter"><xsl:comment select="'icon'"/></i>
                  </a>
                  <a href="https://www.facebook.com/sharer.php?u={encode-for-uri($nodeUrl)}api%2Frecords%2F{$metadataUuid}"
                     target="_blank"
                     aria-label="Facebook"
                     class="btn btn-default">
                    <i class="fa fa-fw fa-facebook"><xsl:comment select="'icon'"/></i>
                  </a>
                  <a href="http://www.linkedin.com/shareArticle?mini=true&amp;summary=&amp;url={encode-for-uri($nodeUrl)}api%2Frecords%2F{$metadataUuid}"
                     target="_blank"
                     aria-label="LinkedIn"
                     class="btn btn-default">
                    <i class="fa fa-fw fa-linkedin"><xsl:comment select="'icon'"/></i>
                  </a>
                  <a href="mailto:?subject={$title}&amp;body={encode-for-uri($nodeUrl)}api%2Frecords%2F{$metadataUuid}"
                     target="_blank"
                     aria-label="Email"
                     class="btn btn-default">
                    <i class="fa fa-fw fa-envelope-o"><xsl:comment select="'icon'"/></i>
                  </a>
                </section>
              </xsl:if>
            </xsl:if>

            <!-- Display link to portal and other view only
            when in pure HTML mode. -->
            <xsl:if test="$viewMenu = 'true'">
              <section class="gn-md-side-viewmode">
                <h2>
                  <i class="fa fa-fw fa-eye"><xsl:comment select="'icon'"/></i>
                  <span><xsl:value-of select="$schemaStrings/viewMode"/></span>
                </h2>
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

            <!--<xsl:if test="$sideRelated != ''">
              <section class="gn-md-side-associated">
                <h2>
                  <i class="fa fa-fw fa-link"><xsl:comment select="'icon'"/></i>
                  <span><xsl:value-of select="$schemaStrings/associatedResources"/></span>
                </h2>
                <div gn-related="md"
                     data-user="user"
                     data-types="{$sideRelated}">
                  Not available
                </div>
              </section>
            </xsl:if>-->
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

      <xsl:if test="$css = 'sextant' and $loadJS = 'true'">
        <!-- Avoid self closing tag. -->
        <script src="{$nodeUrl}../static/lib.js">;</script>
        <script src="{$nodeUrl}../static/gn_search_sextant.js">;</script>
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
          <!-- If in tab mode, do not repeat the tab name as header
          as it is already displayed in the tab itself. -->
          <xsl:if test="$tabs = 'true'">
            <xsl:attribute name="class" select="'hidden'"/>
          </xsl:if>
          <xsl:value-of select="$title"/>
        </h1>
        <xsl:if test="normalize-space($content) != ''">
          <xsl:choose>
            <!-- In tab mode, the top level container is not needed for styling
            as it is contained by tab-pane. Also if the tab contains only one child
            do not build a block. -->
            <xsl:when test="$tabs = 'true' or count(*) = 1">
              <xsl:copy-of select="$content"/>&#160;
            </xsl:when>
            <xsl:otherwise>
              <div class="entry name">
                <xsl:copy-of select="$content"/>&#160;
              </div>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
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
        <h1 class="view-header">
          <xsl:value-of select="$title"/>
        </h1>
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
      <xsl:comment select="'icon'"/>
    </div>
  </xsl:template>


  <xsl:template mode="render-view"
                match="section[not(@xpath)]">

    <xsl:variable name="content">
      <xsl:apply-templates mode="render-view"
                           select="section|field|xsl"/>&#160;
    </xsl:variable>

    <xsl:if test="count($content/*) > 0">
      <div id="gn-section-{generate-id()}" class="gn-tab-content">
        <xsl:if test="@name">
          <xsl:variable name="title"
                        select="gn-fn-render:get-schema-strings($schemaStrings, @name)"/>

          <xsl:element name="h{1 + count(ancestor-or-self::*[name(.) = 'section'])}">
            <xsl:value-of select="$title"/>
          </xsl:element>
        </xsl:if>
        <xsl:copy-of select="$content"/>
      </div>
    </xsl:if>
  </xsl:template>


  <xsl:template mode="render-view"
                match="xsl">
    <div id="gn-section-{generate-id()}">
      <xsl:if test="@name">
        <xsl:variable name="title"
                      select="gn-fn-render:get-schema-strings($schemaStrings, @name)"/>

        <xsl:element name="h{3 + count(ancestor-or-self::*[name(.) = 'section'])}">
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

    <div class="entry name gn-field-template">
      <xsl:if test="@name">
        <xsl:variable name="title"
                      select="gn-fn-render:get-schema-strings($schemaStrings, @name)"/>

        <xsl:element name="h{3 + 1 + count(ancestor-or-self::*[name(.) = 'section'])}">
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
        <div class="target">
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
          &#160;
        </div>
      </xsl:for-each>
    </div>
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



  <xsl:template mode="render-field"
                match="*[*/name() = $configuration/editor/tableFields/table/@for and
                         $isFlatMode = true() and
                         $view != 'sextant']"
                priority="2001">
    <xsl:variable name="isFirstOfItsKind"
                  select="count(preceding-sibling::*[name() = current()/name()]) = 0"/>
    <xsl:if test="$isFirstOfItsKind">
      <xsl:variable name="tableConfig"
                    select="$configuration/editor/tableFields/table[@for = current()/*/name()]"/>
      <dl class="gn-table">
        <dt>
          <xsl:value-of select="tr:nodeLabel(tr:create($schema), name(), null)"/>
        </dt>
        <dd>
          <table class="table">
            <thead>
              <tr>
                <xsl:for-each select="$tableConfig/header/col[@label]">
                  <th>
                    <xsl:value-of select="tr:nodeLabel(tr:create($schema), @label, null)"/>
                  </th>
                </xsl:for-each>
              </tr>
            </thead>
            <tbody>
              <tr>
                <xsl:call-template name="render-table-row">
                  <xsl:with-param name="tableConfig" select="$tableConfig"/>
                </xsl:call-template>
              </tr>
              <xsl:for-each select="following-sibling::*[name() = current()/name()]">
                <tr>
                  <xsl:call-template name="render-table-row">
                    <xsl:with-param name="tableConfig" select="$tableConfig"/>
                  </xsl:call-template>
                </tr>
              </xsl:for-each>
            </tbody>
          </table>
        </dd>
      </dl>
    </xsl:if>
  </xsl:template>

  <xsl:template name="render-table-row">
    <xsl:param name="tableConfig"/>

    <xsl:variable name="root"
                  select="."/>
    <xsl:for-each select="$tableConfig/row/col[@xpath]">
      <xsl:variable name="node">
        <saxon:call-template name="{concat('evaluate-', $schema)}">
          <xsl:with-param name="base" select="$root/*"/>
          <xsl:with-param name="in" select="concat('/', @xpath)"/>
        </saxon:call-template>
      </xsl:variable>
      <td><xsl:apply-templates mode="render-value"
                               select="if ($node//@codeListValue) then $node//@codeListValue else $node"/></td>
    </xsl:for-each>
  </xsl:template>


  <!-- Forgot all none matching elements -->
  <xsl:template mode="render-view" match="*|@*"/>
  <xsl:template mode="render-field" match="*|@*|text()"/>

</xsl:stylesheet>
