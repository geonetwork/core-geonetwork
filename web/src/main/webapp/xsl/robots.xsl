<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:geonet="http://www.fao.org/geonetwork">
  <xsl:output method="text"/>
  
  <xsl:variable name="port" select="/root/gui/env/server/port"/>
  <xsl:variable name="url" select="concat(/root/gui/env/server/protocol, '://', 
    /root/gui/env/server/host, 
    if ($port='80') then '' else concat(':', $port),
    /root/gui/url)"/>
  
  <xsl:template match="/">
    <xsl:text>sitemap: </xsl:text><xsl:value-of select="concat($url, '/srv/eng/portal.sitemap')"/>
    <xsl:text>
</xsl:text>
    <xsl:text>sitemap: </xsl:text><xsl:value-of select="concat($url, '/srv/eng/portal.sitemap?format=rdf')"/>
  </xsl:template>
</xsl:stylesheet>
