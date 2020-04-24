<?xml version="1.0" encoding="UTF-8"?>
<!--
  The main entry point for all user interface generated
  from XSLT.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:template name="header">
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
            <li>
              <a href="{/root/gui/nodeUrl}">
                <img class="gn-logo"
                     alt="{$i18n/siteLogo}"
                     src="{/root/gui/url}/images/logos/{$env//system/site/siteId}.png"></img>
                <xsl:value-of select="$env//system/site/name"/>
              </a>
            </li>
            <li>
              <a title="{$t/search}" href="{/root/gui/nodeUrl}search" onclick="location.href=('{/root/gui/nodeUrl}{$lang}/catalog.search#/search');return false;">
                <i class="fa fa-fw fa-search hidden-sm">&#160;</i>
                <span><xsl:value-of select="$t/search"/></span>
              </a>
            </li>
            <li id="map-menu" class="hidden-nojs">
              <a  title="{$t/map}"
                  href="{/root/gui/nodeUrl}{$lang}/catalog.search#/map">
                <i class="fa fa-fw fa-globe hidden-sm">&#160;</i>
                <span><xsl:value-of select="$t/map"/></span></a>
            </li>
          </ul>

          <ul class="nav navbar-nav navbar-right">
            <li>
              <a href="{/root/gui/nodeUrl}{$lang}/catalog.signin"
                 title="{$t/signIn}">
                <i class="fa fa-sign-in fa-fw">&#160;</i>
                <xsl:value-of select="$t/signIn"/>
              </a>
            </li>
          </ul>
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
  </xsl:template>

  <xsl:template name="footer">

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
          <a href="{/root/gui/url}/doc/api" title="{$t/learnTheApi}"><xsl:value-of select="$t/api"/>&#160;</a>
        </li>
      </ul>
    </div>
  </xsl:template>


</xsl:stylesheet>
