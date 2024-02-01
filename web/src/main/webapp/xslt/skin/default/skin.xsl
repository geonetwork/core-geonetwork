<?xml version="1.0" encoding="UTF-8"?>
<!--
  The main entry point for all user interface generated
  from XSLT.

  Note: When using getUiConfigurationJsonProperty, /root/request/ui may be empty or the parameter may not be defined.
        If this is the case then use defaults specified from CatController.js - defaultConfig

-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:param name="output" as="xs:string" select="'not-pdf'"/>

  <xsl:function name="geonet:updateUrlPlaceholder" as="xs:string">
    <xsl:param name="url" as="xs:string"/>
    <xsl:param name="node" as="xs:string"/>
    <xsl:param name="lang" as="xs:string"/>
    <xsl:variable name="iso2CharLang" select="util:twoCharLangCode($lang)"/>
    <xsl:value-of select="replace(replace(replace($url, '\{\{lang\}\}', $lang), '\{\{isoLang\}\}', $iso2CharLang), '\{\{node\}\}', $node)"/>
  </xsl:function>

  <xsl:template name="header">
    <xsl:variable name="lang"
                  select="/root/gui/language"/>

    <xsl:variable name="isHeaderEnabled"
                  select="if (util:getUiConfigurationJsonProperty(/root/request/ui, 'mods.header.enabled') = 'false')
                        then false()
                        else true()"/>

    <xsl:if test="$isHeaderEnabled">
      <div class="navbar navbar-default gn-top-bar" role="navigation">
        <div class="container">
          <div class="navbar-header">
            <button type="button"
                    class="navbar-toggle collapsed"
                    data-toggle="collapse"
                    data-target="#navbar"
                    title="{$i18n/toggleNavigation}"
                    aria-expanded="false"
                    aria-controls="navbar">
              <span class="sr-only">
                <xsl:value-of select="$i18n/toggleNavigation"/>
              </span>
              <span class="icon-bar"></span>
              <span class="icon-bar"></span>
              <span class="icon-bar"></span>
            </button>
          </div>
          <div id="navbar" class="navbar-collapse collapse">
            <ul class="nav navbar-nav" id="topbar-left">
              <xsl:variable name="isHomeEnabled"
                            select="if (util:getUiConfigurationJsonProperty(/root/request/ui, 'mods.home.enabled') = 'false')
                                    then false()
                                    else true()"/>
              <xsl:if test="$isHomeEnabled">
                <xsl:variable name="isLogoInHeader"
                              select="if (util:getUiConfigurationJsonProperty(/root/request/ui, 'mods.header.isLogoInHeader') = 'true')
                                    then true()
                                    else false()"/>
                <xsl:variable name="isShowGNName"
                              select="if (util:getUiConfigurationJsonProperty(/root/request/ui, 'mods.header.showGNName') = 'false')
                                    then false()
                                    else true()"/>
                <xsl:if test="not($isLogoInHeader) or $isShowGNName">
                  <xsl:variable name="appUrl"
                                select="if(output != 'pdf' and util:getUiConfigurationJsonProperty(/root/request/ui, 'mods.home.appUrl'))
                                    then geonet:updateUrlPlaceholder(util:getUiConfigurationJsonProperty(/root/request/ui, 'mods.home.appUrl'), /root/gui/nodeId, $lang)
                                    else /root/gui/nodeUrl"/>
                  <li>
                    <a href="{$appUrl}">
                      <xsl:if test="not($isLogoInHeader)">
                        <img class="gn-logo"
                             alt="{$i18n/siteLogo}"
                             src="{/root/gui/nodeUrl}../images/logos/{$env//system/site/siteId}.png"/>
                      </xsl:if>
                      <xsl:if test="$isShowGNName">
                        <xsl:value-of select="$env//system/site/name"/>
                      </xsl:if>
                    </a>
                  </li>
                </xsl:if>
              </xsl:if>

              <xsl:if test="$output != 'pdf'">
                <xsl:variable name="isSearchEnabled"
                              select="if (util:getUiConfigurationJsonProperty(/root/request/ui, 'mods.search.enabled') = 'false')
                                    then false()
                                    else true()"/>
                <xsl:if test="$isSearchEnabled">
                  <xsl:variable name="searchUrl"
                                select="if(util:getUiConfigurationJsonProperty(/root/request/ui, 'mods.search.appUrl'))
                                    then geonet:updateUrlPlaceholder(util:getUiConfigurationJsonProperty(/root/request/ui, 'mods.search.appUrl'), /root/gui/nodeId, $lang)
                                    else concat(/root/gui/nodeUrl, $lang, '/catalog.search#/search')"/>
                  <li>
                    <a title="{$t/search}" href="{$searchUrl}" onclick="location.href=('{$searchUrl}');return false;">
                      <i class="fa fa-fw fa-search hidden-sm">&#160;</i>
                      <span><xsl:value-of select="$t/search"/></span>
                    </a>
                  </li>
                </xsl:if>

                <xsl:variable name="isMapEnabled"
                              select="if (util:getUiConfigurationJsonProperty(/root/request/ui, 'mods.map.enabled') = 'false')
                                    then false()
                                    else true()"/>
                <xsl:if test="$isMapEnabled">
                  <xsl:variable name="mapUrl"
                                select="if(util:getUiConfigurationJsonProperty(/root/request/ui, 'mods.map.appUrl'))
                                    then geonet:updateUrlPlaceholder(util:getUiConfigurationJsonProperty(/root/request/ui, 'mods.map.appUrl'), /root/gui/nodeId, $lang)
                                    else concat(/root/gui/nodeUrl, $lang , '/catalog.search#/map')"/>
                  <li id="map-menu" class="hidden-nojs">
                    <a title="{$t/map}"
                       href="{$mapUrl}">
                      <i class="fa fa-fw fa-globe hidden-sm">&#160;</i>
                      <span><xsl:value-of select="$t/map"/></span></a>
                  </li>
                </xsl:if>
              </xsl:if>
            </ul>

            <xsl:if test="$output != 'pdf'">
              <xsl:variable name="isAuthenticated"
                            select="util:isAuthenticated()"/>
              <xsl:variable name="isSigninEnabled"
                            select="if (util:getUiConfigurationJsonProperty(/root/request/ui, 'mods.authentication.enabled') = 'false')
                                    then false()
                                    else true()"/>
              <xsl:if test="$isSigninEnabled and not($isAuthenticated) and not($isDisableLoginForm)">
                <xsl:variable name="signinUrl"
                              select="if(util:getUiConfigurationJsonProperty(/root/request/ui, 'mods.signin.appUrl'))
                                    then geonet:updateUrlPlaceholder(util:getUiConfigurationJsonProperty(/root/request/ui, 'mods.signin.appUrl'), /root/gui/nodeId, $lang)
                                    else concat(/root/gui/nodeUrl, $lang , '/catalog.signin')"/>
                <ul class="nav navbar-nav navbar-right">
                  <li>
                    <a href="{$signinUrl}"
                       title="{$t/signIn}">
                      <i class="fa fa-sign-in fa-fw">&#160;</i>
                      <xsl:value-of select="$t/signIn"/>
                    </a>
                  </li>
                </ul>
              </xsl:if>
            </xsl:if>
          </div>
        </div>
      </div>

      <xsl:if test="/root/search/response">
        <form action="{$nodeUrl}search"
              class="form-horizontal" role="form">
          <div class="row gn-top-search" style="margin:20px">
            <div class="col-md-offset-3 col-md-1 relative"><b><xsl:value-of select="$t/search"/></b></div>
            <div class="col-md-5 relative">
              <div class="gn-form-any input-group input-group-lg">
                <input type="text"
                       name="any"
                       id="gn-any-field"
                       aria-label="{$t/anyPlaceHolder}"
                       placeholder="{$t/anyPlaceHolder}"
                       value="{/root/request/any}"
                       class="form-control"
                       autofocus=""/>
                <div class="input-group-btn">
                  <button type="submit"
                          class="btn btn-default"
                          title="{$t/search}">
                    <i class="fa fa-search">&#160;</i>
                  </button>
                  <a href="{$nodeUrl}search"
                     class="btn btn-default"
                     title="{$t/reset}">
                    <i class="fa fa-times">&#160;</i>
                  </a>
                </div>
              </div>
              <input type="hidden" name="fast" value="index"/>
            </div>
          </div>
        </form>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <xsl:template name="footer">
    <xsl:if test="$output != 'pdf'">
      <xsl:variable name="isFooterEnabled"
                    select="if (util:getUiConfigurationJsonProperty(/root/request/ui, 'mods.footer.enabled') = 'false')
                          then false()
                          else true()"/>
      <xsl:if test="$isFooterEnabled">
        <div class="navbar navbar-default gn-bottom-bar" role="navigation">
          <ul class="nav navbar-nav">
            <li class="gn-footer-text">

            </li>
            <li>
              <a href="http://geonetwork-opensource.org/">
                <i class="fa fa-fw">&#160;</i>
                <span><xsl:value-of select="$t/about"/></span>
              </a>
            </li>
            <li class="hidden-sm">
              <a href="https://github.com/geonetwork/core-geonetwork">
                <i class="fa fa-github">&#160;</i>
                <span><xsl:value-of select="$t/github"/></span>
              </a>
            </li>
            <li>
              <a href="{/root/gui/url}/doc/api" title="{$t/learnTheApi}"><xsl:value-of select="$t/API"/>&#160;</a>
            </li>
          </ul>
        </div>
      </xsl:if>
    </xsl:if>
  </xsl:template>


</xsl:stylesheet>
