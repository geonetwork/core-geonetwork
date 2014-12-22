<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <!-- Global XSL variables about the catalog and user session -->
  
  
  <xsl:output name="default-serialize-mode" indent="no"
    omit-xml-declaration="yes" />
  <xsl:output name="default-indent-mode" indent="yes"
    omit-xml-declaration="yes" />
  
  <!--
  -->
  <xsl:variable name="gnUri" select="'http://www.fao.org/geonetwork'"/>
  
  
  <xsl:variable name="uiResourcesPath" select="'../../catalog/'"/>
  
  <!-- The current service name -->
  <xsl:variable name="service" select="/root/gui/reqService"/>
  
  <xsl:variable name="i18n" select="/root/gui/i18n"/>
  <xsl:variable name="lang" select="/root/gui/language"/>
  <xsl:variable name="requestParameters" select="/root/request"/>

  <!-- XSL using this variable should be refactored to not rely on the
  old i18n files. FIXME eg. metadata-fop.xsl -->
  <xsl:variable name="oldGuiStrings" select="/root/gui/strings"/>
  
  <xsl:variable name="isDebugMode" select="/root/request/debug"/>
  <xsl:variable name="isReadOnly" select="/root/gui/env/readonly = 'true'"/>
  <xsl:variable name="withD3" select="$service = 'admin.console'"/>
  <xsl:variable name="searchView" select="if (/root/request/view) then /root/request/view else 'default'"></xsl:variable>
  <xsl:variable name="owsContext" select="/root/request/owscontext" />
  <xsl:variable name="wmsUrl" select="/root/request/wmsurl" />
  <xsl:variable name="layerName" select="/root/request/layername" />
  <xsl:variable name="angularModule" select="if ($angularApp = 'gn_search') then concat('gn_search_', $searchView) else $angularApp"></xsl:variable>

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
    else if ($service = 'catalog.search') then 'gn_search'
    else 'gn'"/>
  
  <!-- Catalog settings -->
  <xsl:variable name="env" select="/root/gui/systemConfig"/>
  
  <!-- Only system settings (use for backward compatibility replacing
  /root/gui/env by $envSystem is equivalent). New reference to setting
  should use $env.
  -->
  <xsl:variable name="envSystem" select="/root/gui/systemConfig/system"/>
  
  <!-- URL for services - may not be defined FIXME or use fullURL instead -->
  <xsl:variable name="siteURL" select="/root/gui/siteURL"/>
  
  <!-- URL for webapp root -->
  <xsl:variable name="baseURL" select="substring-before($siteURL,'/srv/')"/>
  <!-- Full URL with protocol, host and port -->
  <xsl:variable name="fullURL" select="concat($env/system/server/protocol, '://',
    $env/system/server/host, ':',
    $env/system/server/port)"/>
  <!-- Full URL for services -->
  <xsl:variable name="fullURLForService" select="concat($fullURL, /root/gui/locService)"/>
  
  <xsl:variable name="isMailEnabled" select="$env/feedback/emailServer/host != ''"/>

  <xsl:variable name="serviceInfo" select="/root/gui"/>
  <xsl:variable name="session" select="/root/gui/session"/>
  <xsl:variable name="isLoggedIn" select="$session/userId != ''"/>
  
  <xsl:variable name="isJsEnabled" select="not(ends-with($service, '-nojs'))"/>
  
  <!-- TODO: retrieve from settings -->
  <xsl:variable name="geopublishMatchingPattern"
    select="'^WWW:DOWNLOAD.*|^FILE:GEO|FILE:RASTER|^DB:POSTGIS'"/>
  <xsl:variable name="layerMatchingPattern"
                select="'^OGC:WMS.*'"/>
</xsl:stylesheet>
