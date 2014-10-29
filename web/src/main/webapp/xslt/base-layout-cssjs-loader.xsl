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
    
    <link href="{/root/gui/url}/static/{$angularApp}.css{$minimizedParam}" rel="stylesheet" media="screen" />
    <link href="{/root/gui/url}/static/{/root/gui/nodeId}_custom_style.css{$minimizedParam}" rel="stylesheet" media="screen" />
  </xsl:template>
  
  
  <xsl:template name="javascript-load">
    
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
        <script src="{$uiResourcesPath}lib/angular-md5.js"></script>
        <script src="{$uiResourcesPath}lib/angular.ext/datetimepicker.js"></script>
        
        <script src="{$uiResourcesPath}lib/bootstrap-3.0.1.js"></script>
        <script src="{$uiResourcesPath}lib/proj4js-compressed.js"></script>
        <script src="{$uiResourcesPath}lib/ol-debug.js"></script>
        <xsl:if test="$withD3">
          <script src="{$uiResourcesPath}lib/d3.v3.js"></script>
          <script src="{$uiResourcesPath}lib/nv.d3.js"></script>
        </xsl:if>
        
        
        <!--<xsl:if test="$isEditing">-->
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.ui.widget.js"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.iframe-transport.js"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.fileupload.js"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.fileupload-process.js"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.fileupload-angular.js"></script>
      
        <script src="{$uiResourcesPath}lib/bootstrap.ext/typeahead.js/bloodhound.js"></script>
        <script src="{$uiResourcesPath}lib/bootstrap.ext/typeahead.js/typeahead.bundle.js"></script>
        <script src="{$uiResourcesPath}lib/bootstrap.ext/tagsinput/bootstrap-tagsinput.js"></script>
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
                goog.require('<xsl:value-of select="$angularApp"/>');
            </script>
        </xsl:when>
        <xsl:otherwise>
            <script src="{/root/gui/url}/static/lib.js"></script>
            <script src="{/root/gui/url}/static/{$angularApp}.js{$minimizedParam}"></script>
        </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
