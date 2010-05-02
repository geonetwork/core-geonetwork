<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html"/>

<xsl:template match="/meta">
       <html>
               <head>
                       <title>HTML representation of <xsl:value-of select="title"/></title>
               </head>
               <body>
                       <table>
                               <tr><td>Title</td><td><xsl:value-of select="title"/></td></tr>
                               <tr><td>Creator</td><td><xsl:value-of select="creator"/></td></tr>
                               <tr><td>Originator</td><td><xsl:value-of select="originator"/></td></tr>
                               <tr><td>Subject</td><td><xsl:value-of select="subject"/></td></tr>
                               <tr><td>Coverage</td><td><xsl:value-of select="coverage"/></td></tr>
                       </table>
               </body>
       </html>
</xsl:template>

</xsl:stylesheet>
