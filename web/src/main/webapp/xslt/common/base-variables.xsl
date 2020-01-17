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

<xsl:stylesheet xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0">
  <!-- Global XSL variables about the catalog and user session -->


  <xsl:output name="default-serialize-mode" indent="no"
              omit-xml-declaration="yes"/>
  <xsl:output name="default-indent-mode" indent="yes"
              omit-xml-declaration="yes"/>

  <!--
  -->
  <xsl:variable name="gnUri" select="'http://www.fao.org/geonetwork'"/>

  <xsl:variable name="buildNumber"
                select="util:getBuildNumber()"/>

  <xsl:variable name="uiResourcesPath" select="'../../catalog/'"/>

  <!-- The current service name -->
  <xsl:variable name="service" select="/root/gui/reqService"/>

  <xsl:variable name="i18n" select="/root/gui/i18n"/>
  <!-- Used by SearchApi loading translation from JSON locale files. -->
  <xsl:variable name="t" select="/root/translations"/>
  <xsl:variable name="lang" select="/root/gui/language"/>
  <xsl:variable name="lang2chars" select="/root/gui/lang2chars"/>
  <xsl:variable name="requestParameters" select="/root/request"/>

  <!-- XSL using this variable should be refactored to not rely on the
  old i18n files. FIXME eg. metadata-fop.xsl -->
  <xsl:variable name="oldGuiStrings" select="/root/gui/strings"/>

  <xsl:variable name="isDebugMode" select="/root/request/debug"/>
  <xsl:variable name="isReadOnly" select="/root/gui/env/readonly = 'true'"/>
  <xsl:variable name="withD3" select="$service = 'admin.console'"/>

  <xsl:variable name="searchView"
                select="if (/root/request/view) then /root/request/view else if(util:getSettingValue('system/ui/defaultView')) then util:getSettingValue('system/ui/defaultView') else 'default'"></xsl:variable>
  <xsl:variable name="angularModule"
                select="if ($angularApp = 'gn_search') then concat('gn_search_', $searchView) else $angularApp"></xsl:variable>

  <xsl:variable name="shibbolethOn"
                select="util:existsBean('shibbolethConfiguration')"/>

  <!-- Define which JS module to load using Closure -->
  <xsl:variable name="angularApp" select="
    if ($service = 'admin.console') then 'gn_admin'
    else if ($service = 'catalog.signin' or
              $service = 'new.account' or
              $service = 'new.password' or
              $service = 'error' or
              $service = 'service-not-allowed' or
              $service = 'node-change-warning') then 'gn_login'
    else if ($service = 'contact.us') then 'gn_contact_us'
    else if ($service = 'catalog.edit') then 'gn_editor'
    else if ($service = 'catalog.viewer') then 'gn_viewer'
    else if ($service = 'catalog.search'
      or $service = 'search'
      or $service = 'md.format.html') then 'gn_search'
    else if ($service = 'display') then 'gn_formatter_viewer'
    else 'gn'"/>

  <xsl:variable name="customFilename" select="concat($angularApp, '_', $searchView)"></xsl:variable>

  <!-- Catalog settings -->
  <xsl:variable name="env" select="/root/gui/systemConfig"/>

  <!-- Only system settings (use for backward compatibility replacing
  /root/gui/env by $envSystem is equivalent). New reference to setting
  should use $env.
  -->
  <xsl:variable name="envSystem" select="/root/gui/systemConfig/system"/>

  <!-- URL for services - may not be defined FIXME or use fullURL instead -->
  <xsl:variable name="siteURL" select="/root/gui/siteURL"/>

  <xsl:variable name="nodeUrl"
                select="util:getSettingValue('nodeUrl')"/>

  <!-- URL for webapp root -->
  <xsl:variable name="baseURL" select="/root/gui/baseUrl"/>
  <!-- Full URL with protocol, host and port -->
  <xsl:variable name="fullURL" select="/root/gui/serverUrl"/>
  <!-- Full URL for services -->
  <xsl:variable name="fullURLForService" select="concat($fullURL, /root/gui/locService)"/>
  <xsl:variable name="fullURLForWebapp" select="concat($fullURL, /root/gui/url)"/>

  <xsl:variable name="isMailEnabled" select="$env/feedback/emailServer/host != ''"/>

  <xsl:variable name="serviceInfo" select="/root/gui"/>
  <xsl:variable name="session" select="/root/gui/session"/>
  <xsl:variable name="isLoggedIn" select="$session/userId != ''"/>

  <xsl:variable name="isJsEnabled" select="not(ends-with($service, '-nojs'))"/>

  <!-- TODO: Can be improved.
  If there is no config in the database then this is always false.
  If CatController default config was set it to true, 3D JS dependencies will not be loaded.
  CatController default config is false and if user wants to enable 3D
  he/she has to create a config from the admin.
   -->
  <xsl:variable name="is3DModeAllowed"
                select="if ($service = 'catalog.search' and
                            (util:getUiConfigurationJsonProperty(/root/request/ui, 'mods.map.is3DModeAllowed') = 'true' or /root/request/with3d))
                        then true()
                        else false()"/>

  <xsl:variable name="isSocialbarEnabled"
                select="if (util:getUiConfigurationJsonProperty(/root/request/ui, 'mods.recordview.isSocialbarEnabled') = 'true')
                        then true()
                        else false()"/>

  <xsl:variable name="isRecaptchaEnabled"
                select="$env/system/userSelfRegistration/recaptcha/enable = 'true'" />

  <!-- TODO: retrieve from settings -->
  <xsl:variable name="geopublishMatchingPattern"
                select="'^WWW:DOWNLOAD.*|^FILE:GEO|FILE:RASTER|^DB:POSTGIS'"/>
  <xsl:variable name="layerMatchingPattern"
                select="'^OGC:WMS.*'"/>
</xsl:stylesheet>
