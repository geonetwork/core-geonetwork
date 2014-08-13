<?xml version="1.0" encoding="UTF-8"?>
<!-- 
  The main entry point for all user interface generated
  from XSLT. 
-->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:java="java:org.fao.geonet.util.XslUtil">

    <xsl:output omit-xml-declaration="yes" method="html" doctype-public="html" indent="yes"
        encoding="UTF-8"/>

    <xsl:include href="../../common/base-variables.xsl"/>

    <xsl:include href="../../base-layout-cssjs-loader.xsl"/>

    <xsl:template match="/">
        <xsl:variable name="env" select="/root/gui/env"/>
        <html ng-app="{$angularApp}" lang="{$lang}" id="ng-app">
            <head>
                <title>
                    <xsl:value-of select="concat($env/site/name, ' - ', $env/site/organization)"
                        />
                </title>
                <meta charset="utf-8"/>
                <meta name="viewport" content="initial-scale=1.0, user-scalable=no"/>
                <meta name="apple-mobile-web-app-capable" content="yes"/>

                <meta name="description" content=""/>
                <meta name="keywords" content=""/>


                <link rel="icon" type="image/gif" href="../../images/logos/favicon.gif"/>
                <!--<link href="rss.search?sortBy=changeDate" rel="alternate" type="application/rss+xml"-->
                    <!--title="{{concat($env/system/site/name, ' - ', $env/system/site/organization)}}"/>-->
                <!--<link href="portal.opensearch" rel="search" type="application/opensearchdescription+xml"-->
                    <!--title="concat($env/system/site/name, ' - ', $env/system/site/organization)"/>-->

                <xsl:call-template name="css-load"/>
                <script type="text/javascript">
                    var translationJson = <xsl:value-of select="java:loadTranslationFile('/catalog/locales/%s-inspire.json', /root/gui/language)"/>
                </script>
            </head>


            <!-- The GnCatController takes care of
            loading site information, check user login state
            and a facet search to get main site information.
            -->
            <body data-ng-controller="GnInspireController">
                <!-- AngularJS application -->
                <div data-ng-cloak="" class="ng-cloak">
                    <div data-ng-include="'{$uiResourcesPath}templates/editor/inspire/metadata.html'"></div>

                    <xsl:if test="$isJsEnabled">
                        <xsl:call-template name="javascript-load-inspire"/>
                    </xsl:if>
                </div>
                <xsl:if test="$isJsEnabled">
                    <xsl:call-template name="no-js-alert"/>
                </xsl:if>
            </body>
        </html>
    </xsl:template>

    <xsl:template name="no-js-alert">
        <noscript>
            <div class="alert" data-ng-hide="">
                <strong>
                    <xsl:value-of select="$i18n/warning"/>
                </strong>
                <xsl:text> </xsl:text>
                <xsl:copy-of select="$i18n/nojs"/>
            </div>
        </noscript>
    </xsl:template>



    <xsl:template name="javascript-load-inspire">

        <script>var geonet={provide:function(s){},require:function(s){}}</script>
        <xsl:choose>
            <xsl:when test="$isDebugMode">

                <script src="{$uiResourcesPath}lib/modernizr.js"></script>
                <script src="{$uiResourcesPath}lib/closure/base.js"></script>

                <script src="{$uiResourcesPath}lib/jquery-2.0.3.js"></script>

                <script src="{$uiResourcesPath}lib/moment+langs.min.js"></script>

                <script src="{$uiResourcesPath}lib/angular/angular.js"></script>
                <script src="{$uiResourcesPath}lib/angular/angular-resource.js"></script>
                <script src="{$uiResourcesPath}lib/angular/angular-route.js"></script>

                <script src="{$uiResourcesPath}lib/angular-translate.js"></script>

                <script src="{$uiResourcesPath}lib/bootstrap-3.0.1.js"></script>
                <script src="{$uiResourcesPath}lib/ol-whitespace.js"></script>

            </xsl:when>
            <xsl:otherwise>
            </xsl:otherwise>
        </xsl:choose>

        <xsl:choose>
            <xsl:when test="/root/request/debug">
                <!-- Use Closure to load the application scripts -->
                <script src="{/root/gui/url}/static/closure_deps.js"></script>
                <script>
                    goog.require('<xsl:value-of select="$angularApp"/>');
                </script>
            </xsl:when>
            <xsl:otherwise>
                <script src="{/root/gui/url}/static/inspire-lib.js"></script>
                <script src="{/root/gui/url}/static/{$angularApp}.js{$minimizedParam}"></script>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
