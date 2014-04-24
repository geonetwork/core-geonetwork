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

  <!-- ================================================================= -->

  <xsl:template match="dct:references">
    <xsl:copy>
      <xsl:variable name="value" select="normalize-space(.)"/>

      <xsl:choose>
        <!-- convention: use upload@ prefix in dct:references to identify an uploaded file from metadata editor
                         dct:refereces stores then the URL to download the uploaded file.
        -->
        <xsl:when test="starts-with($value, 'upload@')">
          <xsl:variable name="valueFixed" select="replace(.,'upload@', '')"/>
          <xsl:choose>
            <xsl:when test="/root/env/config/downloadservice/simple='true'">
              <xsl:value-of select="concat(/root/env/siteURL,'/resources.get?uuid=',/root/env/uuid,'&amp;fname=',$valueFixed,'&amp;access=private')"/>
            </xsl:when>
            <xsl:when test="/root/env/config/downloadservice/withdisclaimer='true'">
              <xsl:value-of select="concat(/root/env/siteURL,'/file.disclaimer?uuid=',/root/env/uuid,'&amp;fname=',$valueFixed,'&amp;access=private')"/>
            </xsl:when>
            <xsl:otherwise> <!-- /root/env/config/downloadservice/leave='true' -->
              <xsl:value-of select="."/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <!-- if not an uploaded file, keep the value -->
        <xsl:otherwise> <!-- /root/env/config/downloadservice/leave='true' -->
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <!-- ================================================================= -->

</xsl:stylesheet>
