<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:ogc="http://www.opengis.net/ogc"
    exclude-result-prefixes="#all"
    version="2.0">

    <xsl:template match="filters">
      <xsl:variable name="cql">
         <xsl:call-template name="processFilters">
            <xsl:with-param name="filters" select="." />
            <xsl:with-param name="position" select="1" />
        </xsl:call-template>
      </xsl:variable>

      <cql>
        <xsl:value-of select="normalize-space($cql)" />
      </cql>
    </xsl:template>

    <xsl:template name="getCondition">
        <xsl:param name="condition" />

        <xsl:choose>
            <xsl:when test="$condition = 'AND'">And</xsl:when>
            <xsl:when test="$condition = 'OR'">Or</xsl:when>
            <xsl:otherwise></xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="processFilters">
        <xsl:param name="filters" />
        <xsl:param name="position" />

        <xsl:if test="$position &lt;= count($filters/filter)">
            <xsl:variable name="filter" select="$filters/filter[$position]"/>

            <xsl:variable name="condition">
                <xsl:call-template name="getCondition">
                    <xsl:with-param name="condition" select="$filter/condition" />
                </xsl:call-template>
            </xsl:variable>

          <xsl:if test="string($condition)"><xsl:value-of select="$condition" /><xsl:text> </xsl:text></xsl:if>

            <xsl:call-template name="processFilter">
              <xsl:with-param name="filter" select="$filter" />
            </xsl:call-template>

            <xsl:call-template name="processFilters">
              <xsl:with-param name="filters" select="." />
              <xsl:with-param name="position" select="$position + 1" />
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

  <xsl:template name="processFilter">
    <xsl:param name="filter" />

    <xsl:choose>
      <xsl:when test="$filter/operator = 'LIKE'">
        <xsl:value-of select="$filter/field"/> like '<xsl:value-of select="$filter/value"/>'
      </xsl:when>
      <xsl:when test="$filter/operator = 'EQUAL'">
        <xsl:value-of select="$filter/field"/> = '<xsl:value-of select="$filter/value"/>'
      </xsl:when>
      <xsl:when test="$filter/operator = 'NOTLIKE'">
        <xsl:value-of select="$filter/field"/> &lt;&gt; '<xsl:value-of select="$filter/value"/>'
      </xsl:when>
      <xsl:when test="$filter/operator = 'NOTEQUAL'">
        <xsl:value-of select="$filter/field"/> &lt;&gt; '<xsl:value-of select="$filter/value"/>'
      </xsl:when>
      <xsl:when test="$filter/operator = 'LESSTHAN'">
        <xsl:value-of select="$filter/field"/> &lt; '<xsl:value-of select="$filter/value"/>'
      </xsl:when>
      <xsl:when test="$filter/operator = 'LESSTHANOREQUALTO'">
        <xsl:value-of select="$filter/field"/> &lt;= '<xsl:value-of select="$filter/value"/>'
      </xsl:when>
      <xsl:when test="$filter/operator = 'GREATERTHAN'">
        <xsl:value-of select="$filter/field"/> &gt; '<xsl:value-of select="$filter/value"/>'
      </xsl:when>
      <xsl:when test="$filter/operator = 'GREATERTHANOREQUALTO'">
        <xsl:value-of select="$filter/field"/> &gt;= '<xsl:value-of select="$filter/value"/>'
      </xsl:when>
      <xsl:when test="$filter/operator = 'ISNULL'">
        <xsl:value-of select="$filter/field"/> IS NULL
      </xsl:when>
      <xsl:when test="$filter/operator = 'ISNOTNULL'">
        <xsl:value-of select="$filter/field"/> IS NOT NULL
      </xsl:when>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
