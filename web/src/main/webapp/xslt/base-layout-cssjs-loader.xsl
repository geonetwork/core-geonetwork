<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2001-2016 Food and Agriculture Organization of the
  ~ United Nations (FAO-UN), United Nations World Food Programme (WFP)
  ~ and United Nations Environment Programme (UNEP)
  ~
  ~ This program is free software; you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation; either version 2 of the License, or (at
  ~ your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful, but
  ~ WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  ~ General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
  ~
  ~ Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
  ~ Rome - Italy. email: geonetwork@osgeo.org
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                version="2.0"
                exclude-result-prefixes="#all">
  <!-- Template to load CSS and Javascript -->


  <xsl:import href="common/base-variables.xsl"/>

  <xsl:variable name="minimizedParam">
    <xsl:choose>
      <xsl:when test="/root/request/debug">minimize=false</xsl:when>
      <xsl:otherwise></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:template name="css-load">
    <!--
            TODO : less compilation
            <link href="style/app.css" rel="stylesheet" media="screen" />
-->
    <xsl:if test="$withD3">
      <link href="{/root/gui/url}/static/nv.d3.css?v={$buildNumber}&amp;{$minimizedParam}" rel="stylesheet"
            media="screen"/>
    </xsl:if>

    <link href="{/root/gui/url}/static/{$customFilename}.css?v={$buildNumber}&amp;{$minimizedParam}" rel="stylesheet"
          media="screen"/>

    <link href="{/root/gui/url}/static/bootstrap-table.min.css?v={$buildNumber}" rel="stylesheet"
          media="screen"></link>
    <link href="{/root/gui/url}/static/ng-skos.css?v={$buildNumber}" rel="stylesheet" media="screen"></link>
    <link href="{/root/gui/url}/static/{/root/gui/nodeId}_custom_style.css?v={$buildNumber}&amp;{$minimizedParam}"
          rel="stylesheet" media="screen"/>
  </xsl:template>


  <xsl:template name="javascript-load">


    <xsl:if test="$is3DModeAllowed">
      <script>var CESIUM_BASE_URL = '<xsl:value-of select="$uiResourcesPath"/>lib/ol3cesium/Cesium/';
      </script>
    </xsl:if>


    <xsl:if test="$isRecaptchaEnabled and $service = 'new.account'">
      <script src="https://www.google.com/recaptcha/api.js"></script>
    </xsl:if>

    <xsl:choose>
      <xsl:when test="$isDebugMode">
        <script src="{$uiResourcesPath}lib/modernizr.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/closure/base.js?v={$buildNumber}"></script>

        <script src="{$uiResourcesPath}lib/base64.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/jquery-2.2.4.js?v={$buildNumber}"></script>

        <script src="{$uiResourcesPath}lib/moment+langs.min.js?v={$buildNumber}"></script>

        <script src="{$uiResourcesPath}lib/angular/angular.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/angular/angular-resource.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/angular/angular-route.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/angular/angular-sanitize.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/angular/angular-gettext.min.js?v={$buildNumber}"/>
        <script src="{$uiResourcesPath}lib/angular/angular-cookies.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/angular-translate.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/angular-md5.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/angular-filter.min.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/hotkeys/hotkeys.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/datetimepicker.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/modal.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/buttons.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/rating.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/typeahead.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/position.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/bindHtml.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/tabs.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/slider.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/date.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/angular-floatThead.js?v={$buildNumber}"></script>
        <script
          src="{$uiResourcesPath}lib/angular.ext/colorpicker/angularjs-color-picker.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/tinycolor.js?v={$buildNumber}"></script>

        <script src="{$uiResourcesPath}lib/style/bootstrap/dist/js/bootstrap.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery-ui-slider.min.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/proj4js-compressed.js?v={$buildNumber}"></script>

        <xsl:choose>
          <xsl:when test="$is3DModeAllowed">
            <script src="{$uiResourcesPath}lib/ol3cesium/Cesium/Cesium.js?v={$buildNumber}"></script>
            <script src="{$uiResourcesPath}lib/ol3cesium/ngeool3cesium-debug.js?v={$buildNumber}"></script>
          </xsl:when>
          <xsl:otherwise>
            <script src="{$uiResourcesPath}lib/ngeo/ngeo-debug.js?v={$buildNumber}"></script>
          </xsl:otherwise>
        </xsl:choose>

        <script src="{$uiResourcesPath}lib/FileSaver/FileSaver.min.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/tableExport/tableExport.min.js?v={$buildNumber}"></script>
        <xsl:if test="$withD3">
          <script src="{$uiResourcesPath}lib/d3.v3.js?v={$buildNumber}"></script>
          <script src="{$uiResourcesPath}lib/nv.d3.js?v={$buildNumber}"></script>
        </xsl:if>

        <xsl:if test="$angularApp = 'gn_search' or
                      $angularApp = 'gn_editor' or
                      $angularApp = 'gn_formatter_viewer' or
                      $angularApp = 'gn_admin'">
          <script src="{$uiResourcesPath}lib/zip/zip.js?v={$buildNumber}"></script>
          <!-- Jsonix resources (OWS Context) -->
          <script src="{$uiResourcesPath}lib/jsonix/jsonix/Jsonix-all.js?v={$buildNumber}"></script>
          <script type="text/javascript">
            zip.workerScriptsPath = "../../catalog/lib/zip/";
          </script>
        </xsl:if>


        <!--<xsl:if test="$isEditing">-->
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.ui.widget.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.iframe-transport.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.fileupload.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.fileupload-process.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.fileupload-angular.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.floatThead-slim.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/bootstrap.ext/typeahead.js/typeahead.bundle.js?v={$buildNumber}"></script>
        <script
          src="{$uiResourcesPath}lib/bootstrap.ext/typeahead.js/handlebars-v2.0.0.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/bootstrap.ext/tagsinput/bootstrap-tagsinput.js?v={$buildNumber}"></script>
        <script
          src="{$uiResourcesPath}lib/bootstrap.ext/datepicker/bootstrap-datepicker.js?v={$buildNumber}"></script>
        <script
          src="{$uiResourcesPath}lib/bootstrap.ext/datepicker/bootstrap-datepicker.fr.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/bootstrap-table/dist/bootstrap-table.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/bootstrap-table/src/extensions/export/bootstrap-table-export.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/bootstrap-table/dist/bootstrap-table-locale-all.min.js"></script>
        <!--</xsl:if>-->

        <script src="{$uiResourcesPath}lib/underscore/underscore-min.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/recaptcha/angular-recaptcha.min.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/geohash.js?v={$buildNumber}"></script>
      </xsl:when>
      <xsl:otherwise>
      </xsl:otherwise>
    </xsl:choose>


    <xsl:choose>
      <xsl:when test="/root/request/debug">
        <!-- Use Closure to load the application scripts -->
        <script src="{/root/gui/url}/static/closure_deps.js?v={$buildNumber}"></script>
        <script>
          goog.require('<xsl:value-of select="$angularModule"/>');
        </script>
      </xsl:when>
      <xsl:otherwise>

        <xsl:choose>
          <xsl:when test="$is3DModeAllowed">
            <script src="{$uiResourcesPath}lib/ol3cesium/Cesium/Cesium.js?v={$buildNumber}"></script>
            <script src="{/root/gui/url}/static/lib3d.js?v={$buildNumber}"></script>
          </xsl:when>
          <xsl:otherwise>
            <script src="{/root/gui/url}/static/lib.js?v={$buildNumber}"></script>
          </xsl:otherwise>
        </xsl:choose>
        <script src="{/root/gui/url}/static/{$angularModule}.js?v={$buildNumber}&amp;{$minimizedParam}"></script>
      </xsl:otherwise>
    </xsl:choose>

    <xsl:variable name="appConfig"
                  select="util:getSettingValue('ui/config')"/>

    <xsl:if test="$angularApp = 'gn_search'">
      <script src="{$uiResourcesPath}lib/d3_timeseries/d3.min.js?v={$buildNumber}"></script>
      <script src="{$uiResourcesPath}lib/timeline/timeline-zoomable.js?v={$buildNumber}"></script>
      <link rel="stylesheet" href="{$uiResourcesPath}lib/timeline/timeline.css"/>
      <link rel="stylesheet" href="{$uiResourcesPath}lib/d3_timeseries/nv.d3.min.css"/>
      <script type="text/javascript">
        var module = angular.module('gn_search');
        module.config(['gnGlobalSettings',
        function(gnGlobalSettings) {
        gnGlobalSettings.shibbolethEnabled = <xsl:value-of select="$shibbolethOn"/>;
        }]);
      </script>
    </xsl:if>

    <xsl:if test="$angularApp = 'gn_login'">
      <script type="text/javascript">
        var module = angular.module('gn_login');
        module.config(['gnGlobalSettings',
        function(gnGlobalSettings) {
        gnGlobalSettings.shibbolethEnabled = <xsl:value-of select="$shibbolethOn"/>;
        }]);
      </script>
    </xsl:if>

    <!-- XML highlighter JS dependency. -->
    <xsl:if test="$angularApp = 'gn_editor'">
      <script type="text/javascript" src="{$uiResourcesPath}lib/ace/ace.js?v={$buildNumber}"></script>
      <script type="text/javascript" src="{$uiResourcesPath}lib/angular.ext/ui-ace.js?v={$buildNumber}"></script>
    </xsl:if>


    <script type="text/javascript">
      var module = angular.module('<xsl:value-of select="$angularApp"/>');

      // Init GN config which is a dependency of gn
      // in order to be initialized quite early
      var cfgModule = angular.module('gn_config', []);
      cfgModule.config(['gnViewerSettings', 'gnSearchSettings', 'gnGlobalSettings',
      function(gnViewerSettings, gnSearchSettings, gnGlobalSettings) {
      gnGlobalSettings.init(
      <xsl:value-of select="if ($appConfig != '') then $appConfig else '{}'"/>,
      null, gnViewerSettings, gnSearchSettings);
      }]);
    </script>
  </xsl:template>
</xsl:stylesheet>
