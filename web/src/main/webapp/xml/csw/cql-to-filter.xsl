<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ogc="http://www.opengis.net/ogc"
                version="1.0">

  <xsl:template match="/">
    <ogc:Filter>
      <xsl:apply-templates select="*"/>
    </ogc:Filter>
  </xsl:template>

  <!-- ========================================================================== -->

  <xsl:template match="searchClause">
    <xsl:choose>
      <xsl:when test="relation/value = '='">
        <xsl:choose>
          <xsl:when test="contains(term, '%')">
            <ogc:PropertyIsLike wildCard="%" singleChar="_" escape="\">
              <ogc:PropertyName>
                <xsl:value-of select="index"/>
              </ogc:PropertyName>
              <ogc:Literal>
                <xsl:value-of select="term"/>
              </ogc:Literal>
            </ogc:PropertyIsLike>
          </xsl:when>

          <xsl:otherwise>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>
                <xsl:value-of select="index"/>
              </ogc:PropertyName>
              <ogc:Literal>
                <xsl:value-of select="term"/>
              </ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>

      <xsl:when test="relation/value = '&lt;&gt;'">
        <ogc:PropertyIsNotEqualTo>
          <ogc:PropertyName>
            <xsl:value-of select="index"/>
          </ogc:PropertyName>
          <ogc:Literal>
            <xsl:value-of select="term"/>
          </ogc:Literal>
        </ogc:PropertyIsNotEqualTo>
      </xsl:when>

      <xsl:when test="relation/value = '&lt;'">
        <ogc:PropertyIsLessThan>
          <ogc:PropertyName>
            <xsl:value-of select="index"/>
          </ogc:PropertyName>
          <ogc:Literal>
            <xsl:value-of select="term"/>
          </ogc:Literal>
        </ogc:PropertyIsLessThan>
      </xsl:when>

      <xsl:when test="relation/value = '&lt;='">
        <ogc:PropertyIsLessThanEqualTo>
          <ogc:PropertyName>
            <xsl:value-of select="index"/>
          </ogc:PropertyName>
          <ogc:Literal>
            <xsl:value-of select="term"/>
          </ogc:Literal>
        </ogc:PropertyIsLessThanEqualTo>
      </xsl:when>

      <xsl:when test="relation/value = '&gt;'">
        <ogc:PropertyIsGreaterThan>
          <ogc:PropertyName>
            <xsl:value-of select="index"/>
          </ogc:PropertyName>
          <ogc:Literal>
            <xsl:value-of select="term"/>
          </ogc:Literal>
        </ogc:PropertyIsGreaterThan>
      </xsl:when>

      <xsl:when test="relation/value = '&gt;='">
        <ogc:PropertyIsGreaterThanEqualTo>
          <ogc:PropertyName>
            <xsl:value-of select="index"/>
          </ogc:PropertyName>
          <ogc:Literal>
            <xsl:value-of select="term"/>
          </ogc:Literal>
        </ogc:PropertyIsGreaterThanEqualTo>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <!-- ========================================================================== -->

  <xsl:template match="triple">
    <xsl:choose>
      <xsl:when test="boolean/value = 'and'">
        <ogc:And>
          <xsl:apply-templates select="leftOperand/*"/>
          <xsl:apply-templates select="rightOperand/*"/>
        </ogc:And>
      </xsl:when>

      <xsl:when test="boolean/value = 'or'">
        <ogc:Or>
          <xsl:apply-templates select="leftOperand/*"/>
          <xsl:apply-templates select="rightOperand/*"/>
        </ogc:Or>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <!-- ========================================================================== -->

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
