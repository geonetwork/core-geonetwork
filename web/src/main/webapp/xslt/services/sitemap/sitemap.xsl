<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:geonet="http://www.fao.org/geonetwork" exclude-result-prefixes="#all">
  
  <xsl:include href="../../common/base-variables.xsl"/>
  
  <xsl:variable name="format" select="/root/request/format"/>
  
  
  <xsl:template match="/root">
      
      <xsl:choose>
        <xsl:when test="$format='rdf'">
          <xsl:call-template name="rdf"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="xml"/>
        </xsl:otherwise>
      </xsl:choose>
  </xsl:template>
  
  <xsl:template name="xml">
    <urlset
      xmlns="http://www.sitemaps.org/schemas/sitemap/0.9"
      xmlns:geo="http://www.google.com/geo/schemas/sitemap/1.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd">
      <xsl:for-each select="response/record">
        <xsl:variable name="uuid" select="uuid"/>
        <xsl:variable name="schemaid" select="schemaid"/>
        <xsl:variable name="changedate" select="changedate"/>
        
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
        <xsl:for-each select="response/record">
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
