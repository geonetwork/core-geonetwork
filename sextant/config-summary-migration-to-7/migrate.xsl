<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ns="http://www.fao.org/geonetwork/spring"
                xmlns:bean="http://www.springframework.org/schema/beans"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                version="1.1">

  <xsl:output method="text"/>

  <xsl:variable name="indexFieldMapping" select="document('field-mapping.xml')"/>
  <xsl:variable name="configSummary" select="/"/>

  <xsl:template match="/">
    <xsl:text>var facetMapping = {</xsl:text>
    <xsl:message>Converting config-summary to JS facet config for v4.</xsl:message>
    <xsl:for-each select="$configSummary//ns:facet/@name">
      <xsl:message>Processing <xsl:value-of select="current()"/> </xsl:message>
      <xsl:variable name="field"
                    select="$indexFieldMapping//field[@v3 = current()]"/>
      <xsl:choose>
        <xsl:when test="not($field) or $field/@v4 = ''">
          <xsl:message>WARNING: No mapping available for <xsl:value-of select="current()"/> </xsl:message>
        </xsl:when>
        <xsl:otherwise>
          "<xsl:value-of select="$field/@v3"/>": {<xsl:choose>
          <xsl:when test="$field/text() != ''">
            <xsl:copy-of select="$field/text()"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:variable name="size">
              <xsl:choose>
                <xsl:when test="$configSummary//ns:item[@facet = current()]/@max">
                  <xsl:value-of select="$configSummary//ns:item[@facet = current()]/@max"/>
                </xsl:when>
                <xsl:otherwise>10</xsl:otherwise>
              </xsl:choose>
            </xsl:variable>

            <xsl:variable name="order">
              <xsl:choose>
                <xsl:when test="$configSummary//ns:item[@facet = current()][@sortBy = 'value' and @sortOrder = 'desc']">
                  ,"order" : { "_key" : "desc" }
                </xsl:when>
                <xsl:when test="$configSummary//ns:item[@facet = current()][@sortBy = 'value']">
                  ,"order" : { "_key" : "asc" }
                </xsl:when>
                <xsl:otherwise></xsl:otherwise>
              </xsl:choose>
            </xsl:variable>

            <!-- A default term facet -->
            "<xsl:value-of select="$field/@v4"/>":
            {
            "terms": {
              "field": "<xsl:value-of select="$field/@v4"/>",
              "size": <xsl:value-of select="$size"/><xsl:value-of select="$order"/>
              }
            }
          </xsl:otherwise>
        </xsl:choose>}<xsl:if test="position() != last()">,</xsl:if>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
    <xsl:text>}</xsl:text>
  </xsl:template>
</xsl:stylesheet>
