<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml"/>

    <xsl:template match="/">
                <table border="1">
                    <xsl:choose>
                        <xsl:when test="not(node())">
	                       <tr>
	                       	<td colspan="2"><i><xsl:value-of select="/root/gui/strings/stat.noValue"/></i></td>
	                       </tr>
	                       </xsl:when>
	                   <xsl:otherwise>
							<tr>
							    <th><b><xsl:value-of select="/root/gui/strings/stat.searchedTerm"/></b></th>
							    <th><b><xsl:value-of select="/root/gui/strings/stat.count"/></b></th>
							</tr>
		                    <xsl:for-each select="root/gui/mostSearchedKeyword/record">
		                        <tr>
		                            <td>
		                                <xsl:value-of select="termtext"/>
		                            </td>
		                            <td align="center">
		                                <xsl:value-of select="cnt"/>
		                            </td>
		                        </tr>
		                    </xsl:for-each>
	                   </xsl:otherwise>
                </xsl:choose>
                </table>
    </xsl:template>
</xsl:stylesheet>
