<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/">

  <!-- ================================================================= -->

  <xsl:template match="/root">
    <xsl:apply-templates select="simpledc"/>
  </xsl:template>

  <!-- ================================================================= -->

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- ================================================================= -->

  <xsl:template match="simpledc">
    <simpledc xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/">
      <xsl:apply-templates select="dc:*[name(.)!='dc:identifier']"/>
      <xsl:apply-templates select="dct:*[name(.)!='dct:modified']"/>
      <xsl:choose>
        <xsl:when test="/root/env/changeDate">
          <dct:modified>
            <xsl:value-of select="/root/env/changeDate"/>
          </dct:modified>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="dct:modified"/>
        </xsl:otherwise>
      </xsl:choose>
      <dc:identifier>
        <xsl:value-of select="/root/env/uuid"/>
      </dc:identifier>
    </simpledc>
  </xsl:template>

</xsl:stylesheet>
