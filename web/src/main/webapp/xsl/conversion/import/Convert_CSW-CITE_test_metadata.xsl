<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:ows="http://www.opengis.net/ows"
                version="1.0">

  <xsl:output method="xml" indent="yes"/>

  <!-- ================================================================= -->

  <xsl:template match="/csw:Record">
    <simpledc>
      <xsl:apply-templates select="*"/>
    </simpledc>
  </xsl:template>

  <!-- ================================================================= -->

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- ================================================================= -->

  <xsl:template match="ows:BoundingBox">
    <xsl:variable name="south" select="substring-before(ows:LowerCorner, ' ')"/>
    <xsl:variable name="west" select="substring-after (ows:LowerCorner, ' ')"/>
    <xsl:variable name="north" select="substring-before(ows:UpperCorner, ' ')"/>
    <xsl:variable name="east" select="substring-after (ows:UpperCorner, ' ')"/>

    <dc:coverage>North <xsl:value-of select="$north"/>, South <xsl:value-of select="$south"/>, East
      <xsl:value-of select="$east"/>, West <xsl:value-of select="$west"/>. (Global)
    </dc:coverage>

    <xsl:copy-of select="."/>
  </xsl:template>

  <!-- ================================================================= -->

</xsl:stylesheet>
