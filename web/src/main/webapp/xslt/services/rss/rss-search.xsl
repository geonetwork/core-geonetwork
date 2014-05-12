<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
  xmlns:geonet="http://www.fao.org/geonetwork" xmlns:media="http://search.yahoo.com/mrss/"
  xmlns:georss="http://www.georss.org/georss" xmlns:gml="http://www.opengis.net/gml"
  exclude-result-prefixes="#all">

  <xsl:output method="xml" media-type="application/rss+xml"/>

  <xsl:strip-space elements="*"/>
  
  <xsl:include href="rss-utils.xsl"/>

  
  <xsl:template match="/root">

    <rss version="2.0" xmlns:media="http://search.yahoo.com/mrss/"
      xmlns:georss="http://www.georss.org/georss" xmlns:gml="http://www.opengis.net/gml">
      <channel>

        <title>
          <xsl:value-of select="concat($env/system/site/name, ' (', $env/system/site/organization, ')')"/>
        </title>
        <link>
          <xsl:value-of select="$baseURL"/>
        </link>
        <description>
          <!-- TODO : use CSW abstract here or a new setting -->
        </description>
        <language>
          <xsl:value-of select="$lang"/>
        </language>
        <copyright>
          <!-- TODO : use CSW access constraint here or a new setting -->
        </copyright>
        <category>Geographic metadata catalog</category>
        <generator>GeoNetwork opensource</generator>
        
        <!-- FIXME -->
        <ttl>30</ttl>
        
        <xsl:apply-templates mode="item" select="//rssItems/*[name() != 'summary']"/>
        
      </channel>
    </rss>
  </xsl:template>

</xsl:stylesheet>
