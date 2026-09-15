<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:import href="common.xsl"/>

  <xsl:template match="*" mode="site">
    <capabilitiesUrl>
      <xsl:value-of select="capabUrl/value"/>
    </capabilitiesUrl>
    <apiKeyHeader>
      <xsl:value-of select="apiKeyHeader/value"/>
    </apiKeyHeader>
    <apiKey>
      <xsl:value-of select="apiKey/value"/>
    </apiKey>
    <icon>
      <xsl:value-of select="icon/value"/>
    </icon>
    <rejectDuplicateResource>
      <xsl:value-of select="rejectDuplicateResource/value"/>
    </rejectDuplicateResource>
    <hopCount>
      <xsl:value-of select="hopCount/value"/>
    </hopCount>
    <xpathFilter>
      <xsl:value-of select="xpathFilter/value"/>
    </xpathFilter>
    <xslfilter>
      <xsl:value-of select="xslfilter/value"/>
    </xslfilter>
    <queryScope>
      <xsl:value-of select="queryScope/value"/>
    </queryScope>
    <outputSchema>
      <xsl:value-of select="outputSchema/value"/>
    </outputSchema>
    <sortBy>
      <xsl:value-of select="sortBy/value"/>
    </sortBy>
  </xsl:template>


  <xsl:template match="*" mode="options"/>


  <xsl:template match="*" mode="searches">
    <!-- Convert old search filter values to the new filter format -->
    <xsl:if test="count(children/filters/children) = 0 and count(children/search/children/*[string(.)]) > 0">
      <filters>
        <xsl:for-each select="children/search/children/*[string(.)]">
          <xsl:variable name="searchValue" select="normalize-space(.)" />

          <filter>
            <field>
              <xsl:value-of select="name()"/>
            </field>
            <operator>
              <xsl:choose>
                <xsl:when test="contains($searchValue, '%')">LIKE</xsl:when>
                <xsl:otherwise>EQUAL</xsl:otherwise>
              </xsl:choose>
            </operator>
            <value><xsl:value-of select="normalize-space(.)"/></value>
            <condition><xsl:if test="position() > 1">AND</xsl:if></condition>
          </filter>
        </xsl:for-each>
      </filters>
    </xsl:if>
  </xsl:template>

  <xsl:template match="*" mode="filters">
    <filters>
      <xsl:for-each select="filter/children">
        <xsl:sort select="position/value" data-type="number" />
        <filter>
          <field>
            <xsl:value-of select="field/value"/>
          </field>
          <operator>
            <xsl:value-of select="operator/value"/>
          </operator>
          <value>
            <xsl:value-of select="value/value"/>
          </value>
          <condition>
            <xsl:value-of select="condition/value"/>
          </condition>
        </filter>
      </xsl:for-each>
    </filters>
  </xsl:template>

  <xsl:template match="*" mode="bboxFilter">
    <bboxFilter>
      <bbox-xmin>
        <xsl:value-of select="bbox-xmin/value"/>
      </bbox-xmin>
      <bbox-ymin>
        <xsl:value-of select="bbox-ymin/value"/>
      </bbox-ymin>
      <bbox-xmax>
        <xsl:value-of select="bbox-xmax/value"/>
      </bbox-xmax>
      <bbox-ymax>
        <xsl:value-of select="bbox-ymax/value"/>
      </bbox-ymax>
    </bboxFilter>
  </xsl:template>

  <xsl:template match="children">
    <xsl:copy-of select="search/children/child::*"/>
  </xsl:template>
</xsl:stylesheet>
