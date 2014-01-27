<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <!-- Global XSL variables about the catalog and user session -->

  <xsl:variable name="uiResourcesPath" select="'../../catalog/'"/>
  
  <!-- The current service name -->
  <xsl:variable name="service" select="/root/gui/reqService"/>
  
  <xsl:variable name="i18n" select="/root/gui/i18n"/>
  <xsl:variable name="lang" select="/root/gui/language"/>
  
  <xsl:variable name="isDebugMode" select="/root/request/debug"/>
  <xsl:variable name="isReadOnly" select="/root/gui/env/readonly = 'true'"/>
  <xsl:variable name="withD3" select="$service = 'admin.console'"/>
  
  <!-- Define which JS module to load using Closure -->
  <xsl:variable name="angularApp" select="
    if ($service = 'admin.console') then 'gn_admin'
    else if ($service = 'catalog.signin' or 
              $service = 'new.account' or 
              $service = 'new.password' or 
              $service = 'node-change-warning') then 'gn_login'
    else if ($service = 'catalog.edit') then 'gn_editor'
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
  
  <xsl:variable name="session" select="/root/gui/session"/>
  <xsl:variable name="isLoggedIn" select="$session/userId != ''"/>
  
  <xsl:variable name="isJsEnabled" select="not(ends-with($service, '-nojs'))"/>
  
</xsl:stylesheet>
