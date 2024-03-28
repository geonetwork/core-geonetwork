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
                xmlns:keycloakUtil="java:org.fao.geonet.kernel.security.keycloak.KeycloakXslUtil"
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
    <link href="{/root/gui/url}/static/gn_fonts.css?v={$buildNumber}&amp;{$minimizedParam}" rel="stylesheet"
          media="all"/>

    <link href="{/root/gui/url}/static/{$customFilename}.css?v={$buildNumber}&amp;{$minimizedParam}" rel="stylesheet"
          media="all"/>

    <link href="{/root/gui/url}/static/gn_print_default.css?v={$buildNumber}&amp;{$minimizedParam}" rel="stylesheet"
          media="print"/>

    <link href="{/root/gui/url}/static/bootstrap-table.min.css?v={$buildNumber}" rel="stylesheet"
          media="all"></link>

    <link href="{/root/gui/url}/static/gn_pickers.css?v={$buildNumber}&amp;{$minimizedParam}" rel="stylesheet"
          media="screen"/>

    <link href="{/root/gui/url}/static/gn_inspire.css?v={$buildNumber}&amp;{$minimizedParam}" rel="stylesheet"
          media="all"/>

    <xsl:if test="$withD3">
      <link href="{/root/gui/url}/static/nv.d3.css?v={$buildNumber}&amp;{$minimizedParam}" rel="stylesheet"
            media="screen"/>
    </xsl:if>

    <link href="{/root/gui/url}/static/ng-skos.css?v={$buildNumber}" rel="stylesheet" media="screen"></link>
    <link href="{/root/gui/url}/static/{/root/gui/nodeId}_custom_style.css?v={$buildNumber}&amp;{$minimizedParam}"
          rel="stylesheet" media="all"/>
  </xsl:template>

  <xsl:template name="css-load-nojs">
    <link href="{/root/gui/url}/static/{$customFilename}.css?v={$buildNumber}&amp;{$minimizedParam}" rel="stylesheet"
          media="screen"/>

    <link href="{/root/gui/url}/static/gn_metadata_pdf.css?v={$buildNumber}&amp;{$minimizedParam}" rel="stylesheet"
          media="print"/>
  </xsl:template>


  <xsl:template name="javascript-load">


    <xsl:if test="$is3DModeAllowed">
      <script>var CESIUM_BASE_URL = '<xsl:value-of select="$uiResourcesPath"/>lib/olcesium/Cesium/';
      </script>
    </xsl:if>


    <!-- Load recaptcha api if recaptcha is enabled:
          - in the new account service.
          - in the search application if metadaat user feedback is enabled
    -->
    <xsl:choose>
      <xsl:when test="$isRecaptchaEnabled and ($service = 'new.account' or ($angularApp = 'gn_search' and $metadataUserFeedbackEnabled))">
        <script src="https://www.google.com/recaptcha/api.js"></script>
      </xsl:when>
      <xsl:otherwise>
        <!-- Add dummy object to prevent angularjs-recaptcha to load recaptcha api.js file in other cases.
             If angularjs-recaptcha doesn't find the grecaptcha object with the function render, request the api.js file
             adding some extra cookies that can cause issues with EU directive.
        -->
        <script>var grecaptcha = {render: function() {}};
        </script>
      </xsl:otherwise>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="$isDebugMode">
        <script src="{$uiResourcesPath}lib/modernizr.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/closure/base.js?v={$buildNumber}"></script>

        <script src="{$uiResourcesPath}lib/base64.js?v={$buildNumber}"></script>
        <!--<script src="{$uiResourcesPath}lib/jquery-2.2.4.js?v={$buildNumber}"></script>-->
        <script src="{$uiResourcesPath}lib/jquery-3.7.1.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/jquery-migrate-3.4.1.js?v={$buildNumber}"></script>

        <script src="{$uiResourcesPath}lib/moment-with-locales.min.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/moment-timezone-with-data-1970-2030.min.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/franc-min/franc-min.js?v={$buildNumber}"></script>

        <script src="{$uiResourcesPath}lib/angular/angular.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/angular/angular-resource.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/angular/angular-route.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/angular/angular-sanitize.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/angular/angular-cookies.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/angular/angular-messages.js?v={$buildNumber}"></script>
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
        <!--<script src="{$uiResourcesPath}lib/jquery.ext/jquery-ui-slider.min.js?v={$buildNumber}"></script>-->
        <script src="{$uiResourcesPath}lib/jquery.ext/ jquery-ui-slider-1.13.2.min.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/proj4js-compressed.js?v={$buildNumber}"></script>

        <xsl:choose>
          <xsl:when test="$is3DModeAllowed">
            <script src="{$uiResourcesPath}lib/openlayers/ol.js?v={$buildNumber}"></script>
            <script src="{$uiResourcesPath}lib/olcesium/Cesium/Cesium.js?v={$buildNumber}"></script>
            <script src="{$uiResourcesPath}lib/olcesium/olcesium.js?v={$buildNumber}"></script>
          </xsl:when>
          <xsl:otherwise>
            <script src="{$uiResourcesPath}lib/openlayers/ol.js?v={$buildNumber}"></script>
          </xsl:otherwise>
        </xsl:choose>

        <script src="{$uiResourcesPath}lib/FileSaver/FileSaver.min.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/tableExport/tableExport.min.js?v={$buildNumber}"></script>
        <xsl:if test="$withD3">
          <script src="{$uiResourcesPath}lib/d3.v3.js?v={$buildNumber}"></script>
          <script src="{$uiResourcesPath}lib/nv.d3.js?v={$buildNumber}"></script>
        </xsl:if>

        <script src="{$uiResourcesPath}lib/zip/zip.js?v={$buildNumber}"></script>
        <!-- Jsonix resources (OWS Context) -->
        <script src="{$uiResourcesPath}lib/jsonix/jsonix/Jsonix-all.js?v={$buildNumber}"></script>
        <script type="text/javascript">
          zip.workerScriptsPath = "../../catalog/lib/zip/";
        </script>


        <!--<xsl:if test="$isEditing">-->
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.ui.widget.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.iframe-transport.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.fileupload.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.fileupload-process.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.fileupload-angular.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.floatThead-slim.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/bootstrap.ext/typeahead.js/typeahead.bundle.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/bootstrap.ext/tagsinput/bootstrap-tagsinput.js?v={$buildNumber}"></script>
        <script
          src="{$uiResourcesPath}lib/bootstrap.ext/datepicker/bootstrap-datepicker.js?v={$buildNumber}"></script>
        <script
          src="{$uiResourcesPath}lib/bootstrap.ext/datepicker/bootstrap-datepicker.fr.js?v={$buildNumber}"></script>
        <script
          src="{$uiResourcesPath}lib/bootstrap.ext/datepicker/bootstrap-datepicker.nl.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/bootstrap-table/dist/bootstrap-table.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/bootstrap-table-angular.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/bootstrap-table/src/extensions/export/bootstrap-table-export.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/bootstrap-table/dist/bootstrap-table-locale-all.min.js"></script>
        <script src="{$uiResourcesPath}lib/bootstrap-table/dist/extensions/filter-control/bootstrap-table-filter-control.min.js"></script>

        <!--</xsl:if>-->

        <script src="{$uiResourcesPath}lib/lodash/lodash.min.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/recaptcha/angular-recaptcha.min.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/geohash.js?v={$buildNumber}"></script>

        <script src="{$uiResourcesPath}lib/xml2json/xml2json.min.js?v={$buildNumber}"></script>
        <script src="{$uiResourcesPath}lib/dom-to-image/dom-to-image.min.js?v={$buildNumber}"></script>
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
            <script src="{$uiResourcesPath}lib/olcesium/Cesium/Cesium.js?v={$buildNumber}"></script>
            <script src="{/root/gui/url}/static/lib3d.js?v={$buildNumber}"></script>
          </xsl:when>
          <xsl:otherwise>
            <script src="{/root/gui/url}/static/lib.js?v={$buildNumber}"></script>
          </xsl:otherwise>
        </xsl:choose>
        <script src="{/root/gui/url}/static/{$angularModule}.js?v={$buildNumber}&amp;{$minimizedParam}"></script>
      </xsl:otherwise>
    </xsl:choose>

    <xsl:if test="$isVegaEnabled or $angularApp = ('gn_editor', 'gn_admin')">
      <script src="{$uiResourcesPath}lib/vega/vega.js"></script>
    </xsl:if>

    <script src="{$uiResourcesPath}lib/d3_timeseries/d3.min.js?v={$buildNumber}"></script>
    <script src="{$uiResourcesPath}lib/timeline/timeline-zoomable.js?v={$buildNumber}"></script>
    <link rel="stylesheet" href="{$uiResourcesPath}lib/timeline/timeline.css"/>
    <link rel="stylesheet" href="{$uiResourcesPath}lib/d3_timeseries/nv.d3.min.css"/>

    <xsl:variable name="appConfig"
                  select="util:getUiConfiguration(/root/request/ui)"/>

    <script type="text/javascript">
      var module = angular.module('<xsl:value-of select="$angularApp"/>');
    </script>

    <xsl:if test="$angularApp = 'gn_search' or $angularApp = 'gn_login' or $angularApp = 'gn_admin'">
      <script type="text/javascript">
        module.config(['gnGlobalSettings',
        function(gnGlobalSettings) {
        gnGlobalSettings.isDisableLoginForm = <xsl:value-of select="$isDisableLoginForm"/>;
        gnGlobalSettings.isShowLoginAsLink = <xsl:value-of select="$isShowLoginAsLink"/>;
        gnGlobalSettings.isUserProfileUpdateEnabled = <xsl:value-of select="$isUserProfileUpdateEnabled"/>;
        gnGlobalSettings.isUserGroupUpdateEnabled = <xsl:value-of select="$isUserGroupUpdateEnabled"/>;
        }]);
      </script>

      <!-- For keycloak we have to add some extra scripts -->
      <xsl:if test="util:getSecurityProvider() =  'KEYCLOAK' and keycloakUtil:getClientId()">
        <xsl:variable name="authServerBaseUrl"  select="keycloakUtil:getAuthServerBaseUrl()"/>
        <script src="{$authServerBaseUrl}/js/keycloak.js"></script>
        <script type="text/javascript">
          var sessionModule = angular.module('gn_session_service');
          var keycloak = new Keycloak({
          "realm" : "<xsl:value-of select="keycloakUtil:getRealm()"/>",
          "url" : "<xsl:value-of select="keycloakUtil:getAuthServerBaseUrl()"/>",
          "clientId" : "<xsl:value-of select="keycloakUtil:getClientId()"/>"
          })

          keycloak.init({ onLoad: '<xsl:value-of select="keycloakUtil:getInitOnLoad()"/>',
          checkLoginIframe: false }).success(function(authenticated) {
             $(window).load(function() {
              if (authenticated) {
                if ($("#signinLink").length) {
                   window.location.href = $("#signinLink").href;
                }
              }
            })
          });

        </script>
      </xsl:if>
    </xsl:if>

    <!-- XML highlighter JS dependency. -->
    <xsl:if test="$angularApp = 'gn_editor' or $angularApp = 'gn_admin'">
      <script type="text/javascript" src="{$uiResourcesPath}lib/ace/ace.js?v={$buildNumber}"></script>
      <script type="text/javascript" src="{$uiResourcesPath}lib/ace/snippets/gn.js?v={$buildNumber}"></script>
      <script type="text/javascript" src="{$uiResourcesPath}lib/ace/ext-language_tools.js?v={$buildNumber}"></script>
      <script type="text/javascript" src="{$uiResourcesPath}lib/angular.ext/ui-ace.js?v={$buildNumber}"></script>
    </xsl:if>

    <script type="text/javascript">
      // Init GN config which is a dependency of gn
      // in order to be initialized quite early
      var cfgModule = angular.module('gn_config', []);
      cfgModule.config(['gnViewerSettings', 'gnSearchSettings', 'gnGlobalSettings',
      function(gnViewerSettings, gnSearchSettings, gnGlobalSettings) {
      gnGlobalSettings.init(
      <xsl:value-of select="if ($appConfig != '') then $appConfig else '{}'"/>,
      // Relative path is safer as even if settings are wrong, the client app works.
      null,
      <xsl:value-of select="if ($nodeUrl != '') then concat('&quot;', $nodeUrl, '&quot;') else 'null'"/>,
      gnViewerSettings, gnSearchSettings);
      }]);
    </script>
  </xsl:template>
</xsl:stylesheet>
