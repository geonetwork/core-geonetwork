<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:saxon="http://saxon.sf.net/"
  extension-element-prefixes="saxon"
  version="2.0">
  
  
  <!-- Register here the list of process for the schema-->
  <xsl:include href="process/keywords-comma-exploder.xsl"/>
  <xsl:include href="process/scale-denominator-formatter.xsl"/>
  <xsl:include href="process/add-extent-from-geokeywords.xsl"/>
  <xsl:include href="process/add-info-from-wms.xsl"/>
  <xsl:include href="process/add-service-info-from-wxs.xsl"/>
  <xsl:include href="process/linked-data-checker.xsl"/>
  <xsl:include href="process/related-metadata-checker.xsl"/>
  <xsl:include href="process/add-resource-id.xsl"/>
<!--  Disabled by default because related to INSPIRE only
  <xsl:include href="process/inspire-add-conformity.xsl"/>
  <xsl:include href="process/inspire-add-dq-toporeport.xsl"/>
  <xsl:include href="process/inspire-themes-and-topiccategory.xsl"/>
-->
  
  <xsl:variable name="processes">
    <p>keywords-comma-exploder</p>
    <p>scale-denominator-formatter</p>
    <p>add-extent-from-geokeywords</p>
    <p>add-info-from-wms</p>
    <p>add-service-info-from-wxs</p>
    <p>linked-data-checker</p>
    <p>related-metadata-checker</p>
    <p>add-resource-id</p>
<!--  Disabled by default because related to INSPIRE only
    <p>inspire-add-conformity</p>
    <p>inspire-add-dq-toporeport</p>
    <p>inspire-themes-and-topiccategory</p>
-->
  </xsl:variable>
  
  <xsl:param name="action" select="'list'"/>
  <xsl:param name="process" select="''"/>
  
  <!-- Analyze or process -->
  <xsl:template match="/">
    <xsl:choose>
      <xsl:when test="$action= 'list' or $action = 'analyze'">
        <xsl:variable name="root" select="/"/>
        
        <suggestions>
          <!-- Filter process if user ask for a specific one. If not loop over all. -->
          <xsl:for-each select="if ($process='') then $processes/p else $processes/p[.=$process]">
            <xsl:variable name="tplName" select="concat($action, '-',.)"/>
            <saxon:call-template name="{$tplName}">
              <xsl:with-param name="root" select="$root"/>
              <xsl:fallback>
                <xsl:message>Fall back as no saxon:call-template exists</xsl:message>
              </xsl:fallback>
            </saxon:call-template>
          </xsl:for-each>
        </suggestions>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
</xsl:stylesheet>