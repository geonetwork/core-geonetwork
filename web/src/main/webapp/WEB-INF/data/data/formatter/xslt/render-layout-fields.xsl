<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="#all"
                version="2.0">


  <!-- This template should be overriden in the
  schema plugin for other types of layouts. -->
  <xsl:template mode="render-field" match="*|@*">
    <xsl:param name="fieldName" select="''" as="xs:string"/>
    <dl>
      <dt>
        <xsl:value-of select="if ($fieldName != '')
                              then $fieldName
                              else name(.)"/>
      </dt>
      <dd>
        <xsl:apply-templates mode="render-value"/>
      </dd>
    </dl>
  </xsl:template>

  <xsl:template mode="render-value" match="*|@*">
    <xsl:value-of select="."/>
  </xsl:template>
</xsl:stylesheet>