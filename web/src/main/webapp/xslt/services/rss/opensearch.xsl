<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:import href="../../common/base-variables.xsl"/>

  <xsl:template match="/">
    <OpenSearchDescription xmlns="http://a9.com/-/spec/opensearch/1.1/">
      <ShortName>
        <xsl:value-of select="$env/system/site/name"/>
      </ShortName>
      <LongName>
        <xsl:value-of
                select="concat($env/system/site/name, ' (', $env/system/site/organization, ')')"/>
      </LongName>
      <Description>
        <xsl:value-of select="/root/gui/strings/opensearch"/>
      </Description>
      <Tags>Catalogue Metadata ISO19115 ISO19139 DC</Tags>
      <Contact>
        <xsl:value-of select="//feedback/email"/>
      </Contact>
      <Url type="application/rss+xml">
        <xsl:attribute name="template">
          <xsl:value-of select="concat($fullURLForService, '/rss.search?')"/>
          <xsl:text>any={searchTerms}&amp;hitsPerPage={count?}&amp;bbox={geo:box?}&amp;geometry={geo:geometry?}&amp;name={geo:locationString?}</xsl:text>
        </xsl:attribute>
      </Url>
      <Url type="text/html">
        <xsl:attribute name="template">
          <xsl:value-of
                  select="concat($fullURLForService ,'/catalog.search#/search?')"/>
          <xsl:text>any={searchTerms}&amp;hitsPerPage={count?}&amp;bbox={geo:box?}&amp;geometry={geo:geometry?}&amp;name={geo:locationString?}</xsl:text>
        </xsl:attribute>
      </Url>
      <Url type="application/x-suggestions+json">
        <xsl:attribute name="template">
          <xsl:value-of
                  select="concat($fullURLForService ,'/suggest?field=anylight&amp;sortBy=STARTSWITHFIRST&amp;')"/>
          <xsl:text>q={searchTerms}</xsl:text>
        </xsl:attribute>
      </Url>
      <Image height="16" width="16" type="image/x-icon">
        <xsl:value-of select="concat($fullURLForWebapp, '/images/logos/favicon.gif')"/>
      </Image>
    </OpenSearchDescription>
  </xsl:template>
</xsl:stylesheet>
