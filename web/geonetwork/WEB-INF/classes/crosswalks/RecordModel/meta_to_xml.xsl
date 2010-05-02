<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml"/>

<xsl:template match="/meta">
       <XmlRecord>
               <title><xsl:value-of select="title"/></title>
               <creator><xsl:value-of select="creator"/></creator>
               <originator><xsl:value-of select="originator"/></originator>
               <subject><xsl:value-of select="subject"/></subject>
               <coverage><xsl:value-of select="coverage"/></coverage>
               <Additional>
                       <Extra>This tag added by the record syntax conversion step</Extra>
                       <Extra>And this</Extra>
               </Additional>
       </XmlRecord>
</xsl:template>

</xsl:stylesheet>
