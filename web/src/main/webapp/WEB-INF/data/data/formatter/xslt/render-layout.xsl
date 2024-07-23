<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gn-fn-render="http://geonetwork-opensource.org/xsl/functions/render"
                xmlns:gn-fn-core="http://geonetwork-opensource.org/xsl/functions/core"
                xmlns:tr="java:org.fao.geonet.api.records.formatters.SchemaLocalizations"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:utils="java:org.fao.geonet.util.XslUtil"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:saxon="http://saxon.sf.net/"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all"
                version="2.0">

  <xsl:import href="common/render-html.xsl"/>
  <xsl:import href="common/functions-core.xsl"/>
  <xsl:import href="common/utility-tpl.xsl"/>
  <xsl:import href="common/menu-fn.xsl"/>

  <xsl:import href="render-variables.xsl"/>
  <xsl:import href="render-functions.xsl"/>
  <xsl:import href="render-layout-fields.xsl"/>

  <xsl:output omit-xml-declaration="yes" method="xhtml" doctype-system="html" indent="yes"
              encoding="UTF-8"/>

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
      <div class="gn-multilingual-field">
        <ul class="nav nav-pills">
          <script src="{$nodeUrl}../catalog/js/GnLandingPageLib.js?v={$buildNumber}">&amp;nbsp;</script>
          <script type="text/javascript">
            window.onload = function() {
              document.getElementById('gn-default-lang-link').click();
            };
          </script>

          <xsl:variable name="metadataOtherLanguages">
            <saxon:call-template name="{concat('get-', $schema, '-other-languages')}"/>
          </xsl:variable>

          <xsl:variable name="defaultLanguage"
                        select="$metadataOtherLanguages/*[position() = last()]/@code"/>

          <xsl:for-each select="($metadataOtherLanguages/*[@default], $metadataOtherLanguages/*[not(@default)])">
            <li class="">
              <a id="{if (@default) then 'gn-default-lang-link' else ''}"
                 onclick="gnLandingPage.displayLanguage('{@code}', this);">
                <xsl:variable name="label"
                              select="utils:getIsoLanguageLabel(@code, @code)"/>
                <xsl:value-of select="if ($label != '') then $label else @code"/><xsl:text> </xsl:text>
              </a>
            </li>
          </xsl:for-each>
          <xsl:if test="count($metadataOtherLanguages/*) > 1">
            <li class="active">
              <a onclick="gnLandingPage.displayLanguage('', this);">
                <xsl:value-of select="'All'"/>
              </a>
            </li>
          </xsl:if>
        </ul>
      </div>
    </xsl:if>
  </xsl:template>


  <xsl:template name="render-record">
    <div class="container-fluid gn-metadata-view gn-schema-{$schema}">
      <xsl:variable name="type">
        <xsl:apply-templates mode="getMetadataHierarchyLevel" select="$metadata"/>
      </xsl:variable>

      <xsl:variable name="title">
        <xsl:apply-templates mode="getMetadataTitle" select="$metadata"/>
      </xsl:variable>

      <article id="{$metadataUuid}"
               class="gn-md-view gn-metadata-display">

        <div class="row">
          <div class="col-md-8">

            <header>
              <h1>
                <i class="fa fa-fw gn-icon-{$type}"></i>
                <xsl:copy-of select="$title"/>
              </h1>

              <xsl:call-template name="render-language-switcher"/>

              <xsl:apply-templates mode="getMetadataHeader" select="$metadata"/>

              <xsl:if test="$related != ''">
                <div gn-related="md"
                     data-user="user"
                     data-layout="card"
                     data-types="{$related}"></div>
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
            </div>

            <xsl:if test="$citation = 'true'">
              <xsl:apply-templates mode="getMetadataCitation" select="$metadata"/>
            </xsl:if>
          </div>
          <div class="gn-md-side gn-md-side-advanced col-md-4">
            <xsl:apply-templates mode="getOverviews" select="$metadata"/>
            <xsl:apply-templates mode="getExtent" select="$metadata"/>

            <xsl:apply-templates mode="getTags" select="$metadata">
              <xsl:with-param name="byThesaurus" select="true()"/>
            </xsl:apply-templates>


            <br/>
            <section class="gn-md-side-providedby">
              <h2>
                <i class="fa fa-fw fa-cog"></i>
                <span><xsl:value-of select="$schemaStrings/providedBy"/></span>
              </h2>
              <img class="gn-source-logo"
                   alt="{$schemaStrings/logo}"
                   src="{$nodeUrl}../images/logos/{$source}.png" />
            </section>

            <xsl:if test="$isSocialbarEnabled">
              <section class="gn-md-side-social">
                <h2>
                  <i class="fa fa-fw fa-share-square-o"></i>
                  <span><xsl:value-of select="$schemaStrings/shareOnSocialSite"/></span>
                </h2>
                <a href="https://twitter.com/share?url={encode-for-uri($nodeUrl)}api%2Frecords%2F{$metadataUuid}"
                   target="_blank"
                   aria-label="Twitter"
                   class="btn btn-default btn-round">
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512"><path d="M389.2 48h70.6L305.6 224.2 487 464H345L233.7 318.6 106.5 464H35.8L200.7 275.5 26.8 48H172.4L272.9 180.9 389.2 48zM364.4 421.8h39.1L151.1 88h-42L364.4 421.8z"/></svg>
                </a>
                <a href="https://www.facebook.com/sharer.php?u={encode-for-uri($nodeUrl)}api%2Frecords%2F{$metadataUuid}"
                   target="_blank"
                   aria-label="Facebook"
                   class="btn btn-default btn-round">
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 320 512"><path d="M80 299.3V512H196V299.3h86.5l18-97.8H196V166.9c0-51.7 20.3-71.5 72.7-71.5c16.3 0 29.4 .4 37 1.2V7.9C291.4 4 256.4 0 236.2 0C129.3 0 80 50.5 80 159.4v42.1H14v97.8H80z"/></svg>
                </a>
                <a href="http://www.linkedin.com/shareArticle?mini=true&amp;summary=&amp;url={encode-for-uri($nodeUrl)}api%2Frecords%2F{$metadataUuid}"
                   target="_blank"
                   aria-label="LinkedIn"
                   class="btn btn-default btn-round">
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512"><path d="M100.3 448H7.4V148.9h92.9zM53.8 108.1C24.1 108.1 0 83.5 0 53.8a53.8 53.8 0 0 1 107.6 0c0 29.7-24.1 54.3-53.8 54.3zM447.9 448h-92.7V302.4c0-34.7-.7-79.2-48.3-79.2-48.3 0-55.7 37.7-55.7 76.7V448h-92.8V148.9h89.1v40.8h1.3c12.4-23.5 42.7-48.3 87.9-48.3 94 0 111.3 61.9 111.3 142.3V448z"/></svg>
                </a>
                <a href="mailto:?subject={$title}&amp;body={encode-for-uri($nodeUrl)}api%2Frecords%2F{$metadataUuid}"
                   target="_blank"
                   aria-label="Email"
                   class="btn btn-default btn-round">
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512"><path d="M64 112c-8.8 0-16 7.2-16 16l0 22.1L220.5 291.7c20.7 17 50.4 17 71.1 0L464 150.1l0-22.1c0-8.8-7.2-16-16-16L64 112zM48 212.2L48 384c0 8.8 7.2 16 16 16l384 0c8.8 0 16-7.2 16-16l0-171.8L322 328.8c-38.4 31.5-93.7 31.5-132 0L48 212.2zM0 128C0 92.7 28.7 64 64 64l384 0c35.3 0 64 28.7 64 64l0 256c0 35.3-28.7 64-64 64L64 448c-35.3 0-64-28.7-64-64L0 128z"/></svg>
                </a>
              </section>
            </xsl:if>

            <!-- Display link to portal and other view only
            when in pure HTML mode. -->
            <xsl:if test="$viewMenu = 'true'">
              <section class="gn-md-side-viewmode">
                <h2>
                  <i class="fa fa-fw fa-eye"></i>
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

            <section class="gn-md-side-access">
              <a class="btn btn-block btn-primary"
                 href="{if ($portalLink != '')
                        then replace($portalLink, '\$\{uuid\}', $metadataUuid)
                        else utils:getDefaultUrl($metadataUuid, $language)}">
                <i class="fa fa-fw fa-link"></i>
                <xsl:value-of select="$schemaStrings/linkToPortal"/>
              </a>
              <div class="hidden-xs hidden-sm">
                <xsl:value-of select="$schemaStrings/linkToPortal-help"/>
              </div>
            </section>

            <!-- Don't add the associated resources in the metadata static page, this page doesn't include JS libs -->
            <xsl:if test="$sideRelated != '' and $root != 'html'">
              <section class="gn-md-side-associated">
                <h2>
                  <i class="fa fa-fw fa-link"></i>
                  <span><xsl:value-of select="$schemaStrings/associatedResources"/></span>
                </h2>
                <div gn-related="md"
                     data-user="user"
                     data-layout="card"
                     data-types="{$sideRelated}">
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


  <!-- Render sections. 2 types of sections could be
  defined:
  1) section with xpath matching an element
  2) section with name create a set of fields
  -->
  <xsl:template mode="render-view"
                match="section[@xpath]">
    <xsl:variable name="isDisplayed"
                  as="xs:boolean"
                  select="gn-fn-metadata:check-elementandsession-visibility(
                  $schema, $metadata, $serviceInfo, @displayIfRecord, @displayIfServiceInfo)"/>

    <xsl:if test="$isDisplayed">
      <div id="gn-view-{generate-id()}" class="gn-tab-content">
        <xsl:apply-templates mode="render-view" select="@xpath"/>

      </div>
    </xsl:if>
  </xsl:template>


  <xsl:template mode="render-view"
                match="section[not(@xpath)]">
    <xsl:variable name="isDisplayed"
                  as="xs:boolean"
                  select="gn-fn-metadata:check-elementandsession-visibility(
                  $schema, $metadata, $serviceInfo, @displayIfRecord, @displayIfServiceInfo)"/>

    <xsl:if test="$isDisplayed">
      <xsl:variable name="content">
        <xsl:apply-templates mode="render-view"
                             select="section|field|xsl|list"/>&#160;
      </xsl:variable>

      <xsl:if test="count($content/*) > 0">
        <div id="gn-section-{generate-id()}" class="gn-tab-content">
          <xsl:if test="@name">
            <xsl:variable name="title"
                          select="
                          if (contains( @name, ':'))
                          then gn-fn-render:get-schema-labels($schemaLabels, @name)
                          else gn-fn-render:get-schema-strings($schemaStrings, @name) "/>
            <xsl:element name="h{1 + count(ancestor-or-self::*[name(.) = 'section'])}">
              <xsl:value-of select="$title"/>
            </xsl:element>
          </xsl:if>
          <xsl:copy-of select="$content"/>
        </div>
      </xsl:if>
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
                match="field[not(template)]|list[@xpath]">
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
                <xsl:with-param name="fieldName" select="$fieldName"/>
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
    <xsl:param name="collapsible" as="xs:boolean" select="true()" required="no"/>
    <xsl:param name="collapsed" as="xs:boolean" select="false()" required="no"/>

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
        <xsl:with-param name="collapsible" select="$collapsible"/>
        <xsl:with-param name="collapsed" select="$collapsed"/>
      </xsl:apply-templates>
    </xsl:for-each>
  </xsl:template>



  <xsl:template mode="render-field"
                match="*[*/name() = $configuration/editor/tableFields/table/@for and
                         $isFlatMode = true()]"
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

    <!-- Only if child context. eg. <gmd:pointOfContact xlink:href=
    may not have children if xlink fails to resolve. -->
    <xsl:if test="$root/*">
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
    </xsl:if>
  </xsl:template>


  <!-- Forgot all none matching elements -->
  <xsl:template mode="render-view" match="*|@*"/>
  <xsl:template mode="render-field" match="*|@*|text()"/>

</xsl:stylesheet>
