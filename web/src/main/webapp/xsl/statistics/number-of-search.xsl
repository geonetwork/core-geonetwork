<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml"/>

    <xsl:template match="/">
    <!--  a super smart way to guess how to present result, by parsing the service name :-o -->
                <table border="1">
                    <tr>
                        <th><xsl:value-of select="/root/gui/strings/stat.month"/></th>
                        <th><xsl:value-of select="/root/gui/strings/stat.numberOfSearch"/></th>
                    </tr>
                    <xsl:for-each select="root/gui/search/record">
                        <tr>
                            <td>
                                <xsl:value-of select="month"/>
                            </td>
                            <td align="right">
                                <xsl:value-of select="number"/>
                            </td>
                        </tr>
                    </xsl:for-each>
                </table>
    </xsl:template>
</xsl:stylesheet>
