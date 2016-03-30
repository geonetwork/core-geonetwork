<?xml version="1.0" encoding="UTF-8"?>
<!-- 
  The main entry point for all user interface generated
  from XSLT. 
-->
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="#all">



<xsl:template name="header">

<div class="navbar navbar-default gn-top-bar ng-scope" role="navigation">
<div class="container-fluid ng-scope">
  <div class="navbar-header">
    <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
      <span class="sr-only">Toggle navigation</span>
      <span class="icon-bar"></span>
      <span class="icon-bar"></span>
      <span class="icon-bar"></span>
    </button>
  </div>
  <div id="navbar" class="navbar-collapse collapse">
    <ul class="nav navbar-nav">
      <li class="active">
        <a href=".">
          <img class="gn-logo"  
          src="../../images/harvesting/GN3.png"/>
          <span class="visible-lg ng-binding">GeoNetwork</span>
        </a>
      </li>
      <li>
        <a title="Search" href="catalog.search.nojs">
          <span class="visible-lg ng-scope" >Search</span>
        </a>
      </li>
    </ul>
  </div>
</div>
</div>
</xsl:template>

<xsl:template name="footer">
<!--<footer class="navbar">
<div class="navbar navbar-default gn-bottom-bar ng-scope">
  <ul class="nav navbar-nav">
    <li><a href="http://geonetwork-opensource.org/">
      <i class="fa fa-fw"></i>
      <span  class="ng-scope">About</span></a>
    </li>
   
    <li>
      <a href="rss.search?sortBy=changeDate&amp;georss=simplepoint" title="Latest news">
        <i class="fa fa-rss"></i>
      </a>
    </li>
  </ul>
</div>
</footer>-->
</xsl:template>


</xsl:stylesheet>
