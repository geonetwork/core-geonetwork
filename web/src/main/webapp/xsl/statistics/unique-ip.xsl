<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml"/>

    <xsl:template match="/">
                <table border="1">
                    <tr>
                        <th><b><xsl:value-of select="/root/gui/strings/stat.ip"/></b></th>
                        <th><b><xsl:value-of select="/root/gui/strings/stat.numberOfSearch"/></b></th>
                    </tr>
                    <xsl:for-each select="root/gui/searchUniqueIP/record">
                        <tr>
                            <td>
                                <xsl:value-of select="ip"/>
                            </td>
                            <td align="right">
                                <xsl:value-of select="sumhit"/>
                            </td>
                        </tr>
                    </xsl:for-each>
                </table>
    </xsl:template>
</xsl:stylesheet>
