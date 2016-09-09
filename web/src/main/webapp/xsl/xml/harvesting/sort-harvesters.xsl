<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:saxon="http://saxon.sf.net/"
                version="2.0">

  <xsl:param name="sortField" select="'site[1]/name[1]'"/>

  <xsl:template match="/nodes">
    <xsl:copy>
      <xsl:apply-templates select="node">
        <xsl:sort select="descendant-or-self::*[contains(saxon:path(),$sortField)]"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
