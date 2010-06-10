<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml"/>

<xsl:template match="/meta">
       <iso2709>
               <tag245><subfield code="a"><xsl:value-of select="title"/></subfield></tag245>
       </iso2709>
</xsl:template>

</xsl:stylesheet>
