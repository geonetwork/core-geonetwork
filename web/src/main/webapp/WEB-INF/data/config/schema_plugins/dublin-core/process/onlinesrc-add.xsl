<?xml version="1.0" encoding="UTF-8"?>
<!--
Stylesheet used to update metadata adding a reference to a parent record.
-->
<xsl:stylesheet version="2.0"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="url"/>


  <xsl:template match="/simpledc">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:copy-of
          select="dc:*|dct:*"/>

      <dct:references>
        <xsl:choose>
          <xsl:when test="not(starts-with($url, 'http'))">
            upload@<xsl:value-of select="$url"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$url"/>
          </xsl:otherwise>
        </xsl:choose>

      </dct:references>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>