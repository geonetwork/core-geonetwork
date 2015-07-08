<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:geonet="http://www.fao.org/geonetwork" exclude-result-prefixes="#all">
  
  <xsl:include href="../../common/base-variables.xsl"/>
  
  <xsl:variable name="format" select="/root/request/format"/>
  <xsl:variable name="indexDocs" select="/root/response/indexDocs"/>
  <xsl:variable name="changeDate" select="/root/response/changeDate"/>

  <xsl:template match="/root">
    <xsl:choose>
      <!-- Return index document -->
      <xsl:when test="string($indexDocs)">
        <xsl:call-template name="indexDoc"/>
      </xsl:when>
      <!-- Return results -->
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="$format='rdf'">
            <xsl:call-template name="rdf"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="xml"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="indexDoc">
    <sitemapindex
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xmlns="http://www.sitemaps.org/schemas/sitemap/0.9"
      xsi:schemaLocation="http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/siteindex.xsd">

      <xsl:call-template name="displayIndexDocs">
        <xsl:with-param name="pStart" select="1"/>
        <xsl:with-param name="pEnd" select="$indexDocs"/>
      </xsl:call-template>
    </sitemapindex>
  </xsl:template>


  <xsl:template name="displayIndexDocs">
    <xsl:param name="pStart"/>
    <xsl:param name="pEnd"/>

    <xsl:if test="not($pStart > $pEnd)">
      <xsl:choose>
        <xsl:when test="$pStart = $pEnd">
          <sitemap xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
            <xsl:variable name="formatParam">
              <xsl:if test="string($format)"><xsl:value-of select="$format" />/</xsl:if>
            </xsl:variable>
            <loc><xsl:value-of select="/root/gui/env/server/protocol"/>://<xsl:value-of select="/root/gui/env/server/host"/>:<xsl:value-of select="/root/gui/env/server/port"/><xsl:value-of select="/root/gui/url"/>/sitemap/<xsl:value-of select="$formatParam" /><xsl:value-of select="$pStart" />/<xsl:value-of select="/root/gui/language" /></loc>
            <lastmod><xsl:value-of select="$changeDate" /></lastmod>
          </sitemap>
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="vMid" select=
            "floor(($pStart + $pEnd) div 2)"/>
          <xsl:call-template name="displayIndexDocs">
            <xsl:with-param name="pStart" select="$pStart"/>
            <xsl:with-param name="pEnd" select="$vMid"/>
          </xsl:call-template>
          <xsl:call-template name="displayIndexDocs">
            <xsl:with-param name="pStart" select="$vMid+1"/>
            <xsl:with-param name="pEnd" select="$pEnd"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  
  <xsl:template name="xml">
    <urlset
      xmlns="http://www.sitemaps.org/schemas/sitemap/0.9"
      xmlns:geo="http://www.google.com/geo/schemas/sitemap/1.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd">
      <xsl:for-each select="metadata/record">
        <xsl:variable name="uuid" select="uuid"/>
        <xsl:variable name="schemaid" select="datainfo/schemaid"/>
        <xsl:variable name="changedate" select="datainfo/changedate"/>
        
        <url>
          <loc>
            <xsl:choose>
              <xsl:when test="$format='xml'">
                <xsl:variable name="metadataUrlValue">
                  <xsl:call-template name="metadataXmlDocUrl">
                    <xsl:with-param name="schemaid" select="$schemaid"/>
                    <xsl:with-param name="uuid" select="$uuid"/>
                  </xsl:call-template>
                </xsl:variable>
                <xsl:value-of select="$env/system/server/protocol"/>://<xsl:value-of select="$env/system/server/host"/>:<xsl:value-of select="$env/system/server/port"/><xsl:value-of select="/root/gui/locService"/>/<xsl:value-of select="$metadataUrlValue"/>
              </xsl:when>
              
              <xsl:otherwise>
                <xsl:value-of select="$env/system/server/protocol"/>://<xsl:value-of select="$env/system/server/host"/>:<xsl:value-of select="$env/system/server/port"/><xsl:value-of select="/root/gui/url"/>/?uuid=<xsl:value-of select="$uuid"/>
              </xsl:otherwise>
            </xsl:choose>
          </loc>
          <lastmod><xsl:value-of select="$changedate"/></lastmod>
          <geo:geo>
            <geo:format><xsl:value-of select="$schemaid"/></geo:format>
          </geo:geo>
        </url>
      </xsl:for-each>
    </urlset>
  </xsl:template>
    
  <xsl:template name="rdf">
    <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9"
      xmlns:sc="http://sw.deri.org/2007/07/sitemapextension/scschema.xsd">
      <sc:dataset>
        <sc:datasetLabel><xsl:value-of select="$env/system/site/name"/> content catalogue for Linked Data spiders (RDF)</sc:datasetLabel>
        <xsl:for-each select="metadata/record">
          <sc:dataDumpLocation><xsl:value-of select="$env/system/server/protocol"/>://<xsl:value-of select="$env/system/server/host"/>:<xsl:value-of select="$env/system/server/port"/><xsl:value-of select="/root/gui/url"/>/srv/eng/rdf.metadata.get?uuid=<xsl:value-of select="uuid"/></sc:dataDumpLocation>
        </xsl:for-each>
        <!--For 5 latests update:
        <sc:sampleURI>http://<server_host>:<server_port>/<catalogue>/metadata/<uuid>.rdf</sc:sampleURI>
        
        
        Link to a full dump using the search API
        <sc:dataDumpLocation>http://<server_host>:<server_port>/<catalogue>/search/rdf/</sc:dataDumpLocation>
        or provide for all catalogue record a link using
        <sc:dataDumpLocation>http://<server_host>:<server_port>/<catalogue>/metadata/<uuid>.rdf</sc:dataDumpLocation>-->
        <changefreq>daily</changefreq>
      </sc:dataset>
    </urlset>
  </xsl:template>
  
  
    <xsl:template name="metadataXmlDocUrl">
        <xsl:param name="schemaid" />
        <xsl:param name="uuid" />
        
        <xsl:choose>
          <xsl:when test="$schemaid='dublin-core'">xml_dublin-core?uuid=<xsl:value-of select="$uuid"/></xsl:when>
          <xsl:when test="$schemaid='fgdc-std'">xml_fgdc-std?uuid=<xsl:value-of select="$uuid"/></xsl:when>
          <xsl:when test="$schemaid='iso19115'">xml_iso19115to19139?uuid=<xsl:value-of select="$uuid"/></xsl:when>
          <xsl:when test="$schemaid='iso19110'">xml_iso19110?uuid=<xsl:value-of select="$uuid"/></xsl:when>
          <xsl:when test="$schemaid='iso19139'">xml_iso19139?uuid=<xsl:value-of select="$uuid"/></xsl:when>
          <xsl:otherwise>xml.metadata.get?uuid=<xsl:value-of select="$uuid"/></xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
