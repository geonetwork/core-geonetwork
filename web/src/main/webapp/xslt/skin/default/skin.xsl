<?xml version="1.0" encoding="UTF-8"?>
<!--
  The main entry point for all user interface generated
  from XSLT.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:template name="header">
    <div class="navbar navbar-default gn-top-bar">
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
          <ul class="nav navbar-nav">
            <li>
              <a href="{/root/gui/nodeUrl}{$lang}/catalog.search#/home">
                <img class="gn-logo"
                     src="{/root/gui/url}/images/logos/{$env//system/site/siteId}.png"></img>
                <span class="hidden-xs">
                  <xsl:value-of select="$env//system/site/name"/>
                </span>
              </a>
            </li>
            <li>
              <div style="padding-top: 4px;">
                <form action="{$nodeUrl}search"
                      class="form-inline">
                  <div class="input-group gn-form-any">
                    <input type="text"
                           name="any"
                           id="fldAny"
                           placeholder="{$t/anyPlaceHolder}"
                           value="{/root/request/any}"
                           class="form-control"
                           autofocus=""/>
                    <div class="input-group-btn">
                      <button type="submit"
                              class="btn btn-primary">
                        &#160;&#160;
                        <i class="fa fa-search">&#160;</i>
                        &#160;&#160;
                      </button>
                      <a href="{$nodeUrl}search"
                         class="btn btn-link">
                        <i class="fa fa-times">&#160;</i>
                      </a>
                    </div>
                  </div>
                  <input type="hidden" name="fast" value="index"/>
                </form>
              </div>
            </li>
          </ul>


          <ul class="nav navbar-nav navbar-right">
            <li>
              <a href="{/root/gui/nodeUrl}{lang}/signin"
                 title="{$t/signIn}">
                <i class="fa fa-sign-in">&#160;</i>&#160;
                <xsl:value-of select="$t/signIn"/>
              </a>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </xsl:template>

  <xsl:template name="footer">

    <div class="navbar navbar-default gn-bottom-bar">
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
          <a href="{/root/gui/url}/doc/api" title="{$t/learnTheApi}">API</a>
        </li>
      </ul>
    </div>
  </xsl:template>


</xsl:stylesheet>
