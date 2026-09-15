<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:ogc="http://www.opengis.net/ogc"
    exclude-result-prefixes="#all"
    version="2.0">

    <xsl:template match="filters">
      <xsl:variable name="operators"
                    select="distinct-values(filter/condition[. != ''])"/>
      <xsl:variable name="isUsingOneLogicalOperator"
                    select="count($operators) = 1"/>

      <ogc:Filter>
        <xsl:choose>
          <xsl:when test="$isUsingOneLogicalOperator">
            <xsl:variable name="condition">
              <xsl:call-template name="getCondition">
                <xsl:with-param name="condition" select="$operators[1]" />
              </xsl:call-template>
            </xsl:variable>
            <xsl:element name="{$condition}" namespace="http://www.opengis.net/ogc">
              <xsl:for-each select="filter">
                <xsl:call-template name="processFilter">
                  <xsl:with-param name="filter" select="current()" />
                </xsl:call-template>
              </xsl:for-each>
            </xsl:element>

          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="processFilters">
              <xsl:with-param name="filters" select="." />
              <xsl:with-param name="position" select="count(./filter)" />
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </ogc:Filter>
    </xsl:template>

    <xsl:template name="getCondition">
        <xsl:param name="condition" />

        <xsl:choose>
            <xsl:when test="$condition = 'AND'">ogc:And</xsl:when>
            <xsl:when test="$condition = 'OR'">ogc:Or</xsl:when>
            <xsl:otherwise></xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="processFilters">
        <xsl:param name="filters" />
        <xsl:param name="position" />

        <xsl:if test="$position > 0">
            <xsl:variable name="filter" select="$filters/filter[$position]"/>

            <xsl:variable name="condition">
                <xsl:call-template name="getCondition">
                    <xsl:with-param name="condition" select="$filter/condition" />
                </xsl:call-template>
            </xsl:variable>

            <xsl:choose>
                <xsl:when test="string($condition)">
                    <xsl:element name="{$condition}" namespace="http://www.opengis.net/ogc">
                        <xsl:call-template name="processFilter">
                            <xsl:with-param name="filter" select="$filter" />
                        </xsl:call-template>

                        <xsl:call-template name="processFilters">
                            <xsl:with-param name="filters" select="." />
                            <xsl:with-param name="position" select="$position - 1" />
                        </xsl:call-template>
                    </xsl:element>
                </xsl:when>

                <xsl:otherwise>
                    <xsl:call-template name="processFilter">
                        <xsl:with-param name="filter" select="$filter" />
                    </xsl:call-template>

                    <xsl:call-template name="processFilters">
                        <xsl:with-param name="filters" select="." />
                        <xsl:with-param name="position" select="$position - 1" />
                    </xsl:call-template>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

  <xsl:template name="processFilter">
    <xsl:param name="filter" />

    <xsl:choose>
      <xsl:when test="$filter/operator = 'LIKE'">
          <ogc:PropertyIsLike wildCard="%" singleChar="_" escapeChar="\">
              <ogc:PropertyName><xsl:value-of select="$filter/field"/></ogc:PropertyName>
              <ogc:Literal><xsl:value-of select="$filter/value"/></ogc:Literal>
          </ogc:PropertyIsLike>
      </xsl:when>
      <xsl:when test="$filter/operator = 'EQUAL'">
        <ogc:PropertyIsEqualTo>
          <ogc:PropertyName><xsl:value-of select="$filter/field"/></ogc:PropertyName>
          <ogc:Literal><xsl:value-of select="$filter/value"/></ogc:Literal>
        </ogc:PropertyIsEqualTo>
      </xsl:when>
      <xsl:when test="$filter/operator = 'NOTLIKE'">
        <ogc:Not>
          <ogc:PropertyIsLike wildCard="%" singleChar="_" escapeChar="\">
            <ogc:PropertyName><xsl:value-of select="$filter/field"/></ogc:PropertyName>
            <ogc:Literal><xsl:value-of select="$filter/value"/></ogc:Literal>
          </ogc:PropertyIsLike>
        </ogc:Not>
      </xsl:when>
      <xsl:when test="$filter/operator = 'NOTEQUAL'">
        <ogc:Not>
          <ogc:PropertyIsEqualTo>
            <ogc:PropertyName><xsl:value-of select="$filter/field"/></ogc:PropertyName>
            <ogc:Literal><xsl:value-of select="$filter/value"/></ogc:Literal>
          </ogc:PropertyIsEqualTo>
        </ogc:Not>
      </xsl:when>
      <xsl:when test="$filter/operator = 'LESSTHAN'">
        <ogc:PropertyIsLessThan>
          <ogc:PropertyName><xsl:value-of select="$filter/field"/></ogc:PropertyName>
          <ogc:Literal><xsl:value-of select="$filter/value"/></ogc:Literal>
        </ogc:PropertyIsLessThan>
      </xsl:when>
      <xsl:when test="$filter/operator = 'LESSTHANOREQUALTO'">
        <ogc:PropertyIsLessThanEqualTo>
          <ogc:PropertyName><xsl:value-of select="$filter/field"/></ogc:PropertyName>
          <ogc:Literal><xsl:value-of select="$filter/value"/></ogc:Literal>
        </ogc:PropertyIsLessThanEqualTo>
      </xsl:when>
      <xsl:when test="$filter/operator = 'GREATERTHAN'">
        <ogc:PropertyIsGreaterThan>
          <ogc:PropertyName><xsl:value-of select="$filter/field"/></ogc:PropertyName>
          <ogc:Literal><xsl:value-of select="$filter/value"/></ogc:Literal>
        </ogc:PropertyIsGreaterThan>
      </xsl:when>
      <xsl:when test="$filter/operator = 'GREATERTHANOREQUALTO'">
        <ogc:PropertyIsGreaterThanEqualTo>
          <ogc:PropertyName><xsl:value-of select="$filter/field"/></ogc:PropertyName>
          <ogc:Literal><xsl:value-of select="$filter/value"/></ogc:Literal>
        </ogc:PropertyIsGreaterThanEqualTo>
      </xsl:when>
      <xsl:when test="$filter/operator = 'ISNULL'">
        <ogc:PropertyIsNull>
          <ogc:PropertyName><xsl:value-of select="$filter/field"/></ogc:PropertyName>
        </ogc:PropertyIsNull>
      </xsl:when>
      <xsl:when test="$filter/operator = 'ISNOTNULL'">
        <ogc:Not>
          <ogc:PropertyIsNull>
            <ogc:PropertyName><xsl:value-of select="$filter/field"/></ogc:PropertyName>
          </ogc:PropertyIsNull>
        </ogc:Not>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
