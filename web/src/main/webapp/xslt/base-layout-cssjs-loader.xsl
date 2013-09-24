<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <!-- Template to load CSS and Javascript -->
  
  
  <xsl:import href="common/base-variables.xsl"/>
  
  
  <xsl:template name="css-load">
    <!--
            TODO : less compilation 
            <link href="style/app.css" rel="stylesheet" media="screen" />
-->
    <xsl:if test="$withD3">
      <link href="{$uiResourcesPath}style/nv.d3.css" rel="stylesheet" media="screen" />
    </xsl:if>
    
    <link href="{$uiResourcesPath}lib/jquery.ext/jquery.fileupload-ui.css" rel="stylesheet"/>
    
    <link rel="shortcut icon" type="image/x-icon" href="../../images/logos/favicon.ico" />
    
    <xsl:choose>
      <xsl:when test="$isDebugMode">
        <link href="{$uiResourcesPath}style/{$angularApp}.less" rel="stylesheet/less" media="screen" />
      </xsl:when>
      <xsl:otherwise>
        <link href="{$uiResourcesPath}style/{$angularApp}.css" rel="stylesheet" media="screen" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  
  <xsl:template name="javascript-load">
    
    
    <xsl:choose>
      <xsl:when test="$isDebugMode">
        <script src="{$uiResourcesPath}lib/less-1.4.1.min.js"></script>
        
        <script src="{$uiResourcesPath}lib/closure/base.js"></script>
        
        <script src="{$uiResourcesPath}lib/jquery-2.0.2.js"></script>
        
        <script src="{$uiResourcesPath}lib/moment+langs.min.js"></script>
        
        <script src="{$uiResourcesPath}lib/angular/angular.js"></script>
        <script src="{$uiResourcesPath}lib/angular/angular-resource.js"></script>
        <script src="{$uiResourcesPath}lib/angular/angular-route.js"></script>
        
        <script src="{$uiResourcesPath}lib/angular-translate.js"></script>
        
        <script src="{$uiResourcesPath}lib/bootstrap-3.0.0.js"></script>
        
        <xsl:if test="$withD3">
          <script src="{$uiResourcesPath}lib/d3.v3.js"></script>
          <script src="{$uiResourcesPath}lib/d3.ext/gauge.js"></script>
          <script src="{$uiResourcesPath}lib/nv.d3.js"></script>
        </xsl:if>
        
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.ui.widget.js"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.iframe-transport.js"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.fileupload.js"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.fileupload-process.js"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.fileupload-angular.js"></script>
        
        <!-- Use Closure to load the application scripts -->
        <script>
          window.CLOSURE_NO_DEPS = true;
        </script>
        
        <script>
          goog.require('<xsl:value-of select="$angularApp"/>');
        </script>
      </xsl:when>
      <xsl:otherwise>
        <script src="{$uiResourcesPath}lib/jquery-2.0.2.min.js"></script>
          
        <script src="{$uiResourcesPath}lib/moment+langs.min.js"></script>
        
        <script src="{$uiResourcesPath}lib/angular/angular.min.js"></script>
        <script src="{$uiResourcesPath}lib/angular/angular-resource.min.js"></script>
        <script src="{$uiResourcesPath}lib/angular/angular-route.min.js"></script>
          
        <script src="{$uiResourcesPath}lib/angular-translate.min.js"></script>
        <script src="{$uiResourcesPath}lib/bootstrap-3.0.0.min.js"></script>
        
        <xsl:if test="$withD3">
          <script src="{$uiResourcesPath}lib/d3.v3.min.js"></script>
          <!-- TODO: minify -->
          <script src="{$uiResourcesPath}lib/d3.ext/gauge.js"></script>
          <script src="{$uiResourcesPath}lib/nv.d3.min.js"></script>
        </xsl:if>
        
        
        <!-- TODO: minify -->
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.ui.widget.js"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.iframe-transport.js"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.fileupload.js"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.fileupload-process.js"></script>
        <script src="{$uiResourcesPath}lib/jquery.ext/jquery.fileupload-angular.js"></script>
        
        <script src="{$uiResourcesPath}lib/{$angularApp}.min.js"></script>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
