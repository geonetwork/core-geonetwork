<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml"/>

    <xsl:template match="/">
                <table border="1">
				<xsl:for-each select="root/gui/simpleAdvancedSearch/record">
				  	<tr>
					<xsl:choose>
					  <xsl:when test="simple = 0">
					    <td align="right"><xsl:value-of select="/root/gui/strings/stat.numAdvancedSearch"/></td>
					  </xsl:when>
					  <xsl:otherwise>
				  		<td align="right"><xsl:value-of select="/root/gui/strings/stat.numSimpleSearch"/></td>
					  </xsl:otherwise>
					</xsl:choose>                    
					    <td>&#160;<b><xsl:value-of select="cnt"/></b></td>
			    </tr>
			    </xsl:for-each>
                </table>
    </xsl:template>
</xsl:stylesheet>
<!--  select simple, count(*) as cnt from requests group by simple -->
