<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0">
  <xsl:output method="text"/>

  <xsl:template match="/meta">
    A Sutrs record for the item with title <xsl:value-of select="title"/>, the
    Coverage attribute is
    <xsl:value-of select="coverage"/> and the creator
    is <xsl:value-of select="creator"/>. The item was originated by
    <xsl:value-of select="originator"/> and our poor representation of a
    subject heading is <xsl:value-of select="coverage"/>.
  </xsl:template>

</xsl:stylesheet>
