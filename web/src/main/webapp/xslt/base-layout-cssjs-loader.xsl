<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <!-- Template to load CSS and Javascript -->


  <xsl:import href="common/base-variables.xsl"/>

  <xsl:variable name="minimizedParam">
      <xsl:choose>
          <xsl:when test="/root/request/debug">?minimize=false</xsl:when>
          <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
  </xsl:variable>

  <xsl:template name="css-load">
    <!--
            TODO : less compilation
            <link href="style/app.css" rel="stylesheet" media="screen" />
-->
    <xsl:if test="$withD3">
      <link href="{/root/gui/url}/static/nv.d3.css{$minimizedParam}" rel="stylesheet" media="screen" />
    </xsl:if>

    <link rel="shortcut icon" type="image/x-icon" href="../../images/logos/favicon.ico" />

    <xsl:choose>
      <xsl:when test="$angularApp = 'gn_search'">
        <link href="{/root/gui/url}/static/{$angularModule}.css{$minimizedParam}" rel="stylesheet" media="screen" />
        <!--<link href="{/root/gui/url}/catalog/tmp/{$searchView}.css" rel="stylesheet" media="screen" />-->
    </xsl:when>
    <xsl:otherwise>
      <link href="{/root/gui/url}/static/{$angularApp}.css{$minimizedParam}" rel="stylesheet" media="screen" />
    </xsl:otherwise>
    </xsl:choose>

    <link href="{/root/gui/url}/static/{/root/gui/nodeId}_custom_style.css{$minimizedParam}" rel="stylesheet" media="screen" />
  </xsl:template>


  <xsl:template name="javascript-load">

    <script>var geonet={provide:function(s){},require:function(s){}}</script>
    <xsl:choose>
      <xsl:when test="$isDebugMode">

        <script src="{$uiResourcesPath}lib/modernizr.js"></script>
        <script src="{$uiResourcesPath}lib/closure/base.js"></script>

        <script src="{$uiResourcesPath}lib/base64.js"></script>

        <script src="{$uiResourcesPath}lib/jquery-2.0.3.js"></script>

        <script src="{$uiResourcesPath}lib/moment+langs.min.js"></script>

        <script src="{$uiResourcesPath}lib/angular/angular.js"></script>
        <script src="{$uiResourcesPath}lib/angular/angular-resource.js"></script>
        <script src="{$uiResourcesPath}lib/angular/angular-route.js"></script>
        <script src="{$uiResourcesPath}lib/angular/angular-sanitize.js"></script>

        <script src="{$uiResourcesPath}lib/angular-translate.js"></script>
        <script src="{$uiResourcesPath}lib/angular-md5.js"></script>
        <script src="{$uiResourcesPath}lib/angular-filter.min.js"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/hotkeys/hotkeys.js"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/datetimepicker.js"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/buttons.js"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/typeahead.js"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/position.js"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/bindHtml.js"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/tabs.js"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/slider.js"></script>

        <script src="{$uiResourcesPath}lib/style/bootstrap/dist/js/bootstrap.js"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery-ui-slider.min.js"></script>
        <script src="{$uiResourcesPath}lib/proj4js-compressed.js"></script>
        <script src="{$uiResourcesPath}lib/ngeo/ngeo-whitespace.js"></script>
        <xsl:if test="$withD3">
          <script src="{$uiResourcesPath}lib/d3.v3.js"></script>
          <script src="{$uiResourcesPath}lib/nv.d3.js"></script>
        </xsl:if>

        <xsl:if test="$angularApp = 'gn_search'">
          <script src="{$uiResourcesPath}lib/zip/zip.js"></script>
          <script type="text/javascript">
            zip.workerScriptsPath = "../../catalog/lib/zip/";
          </script>
        </xsl:if>

        <!-- Jsonix resources (OWS Context) -->
        <script src="{$uiResourcesPath}lib/jsonix/jsonix/Jsonix-min.js"></script>
        <script src="{$uiResourcesPath}lib/jsonix/w3c-schemas/XLink_1_0.js"></script>
        <script src="{$uiResourcesPath}lib/jsonix/ogc-schemas/OWS_1_0_0.js"></script>
        <script src="{$uiResourcesPath}lib/jsonix/ogc-schemas/Filter_1_0_0.js"></script>
        <script src="{$uiResourcesPath}lib/jsonix/ogc-schemas/GML_2_1_2.js"></script>
        <script src="{$uiResourcesPath}lib/jsonix/ogc-schemas/SLD_1_0_0.js"></script>
        <script src="{$uiResourcesPath}lib/jsonix/ogc-schemas/OWC_0_3_1.js"></script>


        <!--<xsl:if test="$isEditing">-->
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.ui.widget.js"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.iframe-transport.js"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.fileupload.js"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.fileupload-process.js"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.fileupload-angular.js"></script>
        <script src="{$uiResourcesPath}lib/bootstrap.ext/typeahead.js/typeahead.bundle.js"></script>
        <script src="{$uiResourcesPath}lib/bootstrap.ext/typeahead.js/handlebars-v2.0.0.js"></script>
        <script src="{$uiResourcesPath}lib/bootstrap.ext/tagsinput/bootstrap-tagsinput.js"></script>
        <script src="{$uiResourcesPath}lib/bootstrap.ext/datepicker/bootstrap-datepicker.js"></script>
        <!--</xsl:if>-->

      </xsl:when>
      <xsl:otherwise>
      </xsl:otherwise>
    </xsl:choose>

    <xsl:choose>
        <xsl:when test="/root/request/debug">
            <!-- Use Closure to load the application scripts -->
            <script src="{/root/gui/url}/static/closure_deps.js"></script>
            <script>
                 goog.require('<xsl:value-of select="$angularModule"/>');
            </script>
        </xsl:when>
        <xsl:otherwise>
            <script src="{/root/gui/url}/static/lib.js"></script>
            <script src="{/root/gui/url}/static/{$angularModule}.js{$minimizedParam}"></script>
        </xsl:otherwise>
    </xsl:choose>
    <xsl:if test="$owsContext">
      <script type="text/javascript">
        var module = angular.module('gn_search');
        module.config(['gnViewerSettings', function(gnViewerSettings) {
          gnViewerSettings.owsContext = '<xsl:value-of select="$owsContext"/>';
        }]);
      </script>
    </xsl:if>
    <xsl:if test="$wmsUrl and $layerName">
      <script type="text/javascript">
        var module = angular.module('gn_search');
        module.config(['gnViewerSettings', function(gnViewerSettings) {
          gnViewerSettings.wmsUrl = '<xsl:value-of select="$wmsUrl"/>';
          gnViewerSettings.layerName = '<xsl:value-of select="$layerName"/>';
        }]);
      </script>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
