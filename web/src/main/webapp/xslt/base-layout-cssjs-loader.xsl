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
      <link href="{/root/gui/url}/static/nv.d3.css{$minimizedParam}" rel="stylesheet"
            media="screen"/>
    </xsl:if>

    <link href="{/root/gui/url}/static/{$customFilename}.css{$minimizedParam}" rel="stylesheet"
          media="screen"/>

    <link href="{/root/gui/url}/static/bootstrap-table.min.css" rel="stylesheet"
          media="screen"></link>
    <link href="{/root/gui/url}/static/ng-skos.css" rel="stylesheet" media="screen"></link>
    <link href="{/root/gui/url}/static/{/root/gui/nodeId}_custom_style.css{$minimizedParam}"
          rel="stylesheet" media="screen"/>
  </xsl:template>


  <xsl:template name="javascript-load">


    <xsl:if test="$is3DModeAllowed">
      <script>var CESIUM_BASE_URL = '<xsl:value-of select="$uiResourcesPath"/>lib/ol3cesium/Cesium/';
      </script>
    </xsl:if>

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
        <script src="{$uiResourcesPath}lib/angular/angular-gettext.min.js"/>
        <script src="{$uiResourcesPath}lib/angular/angular-cookies.js"></script>
        <script src="{$uiResourcesPath}lib/angular-translate.js"></script>
        <script src="{$uiResourcesPath}lib/angular-md5.js"></script>
        <script src="{$uiResourcesPath}lib/angular-filter.min.js"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/hotkeys/hotkeys.js"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/datetimepicker.js"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/buttons.js"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/rating.js"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/typeahead.js"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/position.js"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/bindHtml.js"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/tabs.js"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/slider.js"></script>
        <script
          src="{$uiResourcesPath}lib/angular.ext/colorpicker/angularjs-color-picker.js"></script>
        <script src="{$uiResourcesPath}lib/tinycolor.js"></script>

        <script src="{$uiResourcesPath}lib/style/bootstrap/dist/js/bootstrap.js"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery-ui-slider.min.js"></script>
        <script src="{$uiResourcesPath}lib/proj4js-compressed.js"></script>

        <xsl:choose>
          <xsl:when test="$is3DModeAllowed">
            <script src="{$uiResourcesPath}lib/ol3cesium/Cesium/Cesium.js"></script>
            <script src="{$uiResourcesPath}lib/ol3cesium/ngeool3cesium-debug.js"></script>
          </xsl:when>
          <xsl:otherwise>
            <script src="{$uiResourcesPath}lib/ngeo/ngeo-debug.js"></script>
          </xsl:otherwise>
        </xsl:choose>

        <script src="{$uiResourcesPath}lib/FileSaver/FileSaver.min.js"></script>
        <script src="{$uiResourcesPath}lib/tableExport/tableExport.min.js"></script>
        <xsl:if test="$withD3">
          <script src="{$uiResourcesPath}lib/d3.v3.js"></script>
          <script src="{$uiResourcesPath}lib/nv.d3.js"></script>
        </xsl:if>

        <xsl:if test="$angularApp = 'gn_search' or
                      $angularApp = 'gn_editor' or
                      $angularApp = 'gn_admin'">
          <script src="{$uiResourcesPath}lib/zip/zip.js"></script>
          <!-- Jsonix resources (OWS Context) -->
          <script src="{$uiResourcesPath}lib/jsonix/jsonix/Jsonix-min.js"></script>
          <script type="text/javascript">
            zip.workerScriptsPath = "../../catalog/lib/zip/";
          </script>
        </xsl:if>


        <!--<xsl:if test="$isEditing">-->
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.ui.widget.js"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.iframe-transport.js"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.fileupload.js"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.fileupload-process.js"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.fileupload-angular.js"></script>
        <script src="{$uiResourcesPath}lib/bootstrap.ext/typeahead.js/typeahead.bundle.js"></script>
        <script
          src="{$uiResourcesPath}lib/bootstrap.ext/typeahead.js/handlebars-v2.0.0.js"></script>
        <script src="{$uiResourcesPath}lib/bootstrap.ext/tagsinput/bootstrap-tagsinput.js"></script>
        <script
          src="{$uiResourcesPath}lib/bootstrap.ext/datepicker/bootstrap-datepicker.js"></script>
        <script src="{$uiResourcesPath}lib/bootstrap-table/dist/bootstrap-table.js"></script>
        <script src="{$uiResourcesPath}lib/bootstrap-table/src/extensions/export/bootstrap-table-export.js"></script>
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

        <xsl:choose>
          <xsl:when test="$is3DModeAllowed">
            <script src="{$uiResourcesPath}lib/ol3cesium/Cesium/Cesium.js"></script>
            <script src="{/root/gui/url}/static/lib3d.js"></script>
          </xsl:when>
          <xsl:otherwise>
            <script src="{/root/gui/url}/static/lib.js"></script>
          </xsl:otherwise>
        </xsl:choose>
        <script src="{/root/gui/url}/static/{$angularModule}.js{$minimizedParam}"></script>
      </xsl:otherwise>
    </xsl:choose>

    <xsl:variable name="appConfig"
                  select="util:getSettingValue('ui/config')"/>

    <!-- XML highlighter JS dependency. -->
    <xsl:if test="$angularApp = 'gn_editor'">
      <script type="text/javascript" src="{$uiResourcesPath}lib/ace/ace.js"></script>
      <script type="text/javascript" src="{$uiResourcesPath}lib/angular.ext/ui-ace.js"></script>
    </xsl:if>

    <script type="text/javascript">
      var module = angular.module('<xsl:value-of select="$angularApp"/>');
      module.config(['gnViewerSettings', 'gnSearchSettings', 'gnGlobalSettings',
      function(gnViewerSettings, gnSearchSettings, gnGlobalSettings) {
      gnGlobalSettings.init(
      <xsl:value-of select="if ($appConfig != '') then $appConfig else '{}'"/>,
      null, gnViewerSettings, gnSearchSettings);
      }]);
    </script>
  </xsl:template>
</xsl:stylesheet>
