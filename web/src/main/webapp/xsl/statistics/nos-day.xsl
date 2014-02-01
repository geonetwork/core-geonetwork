<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml"/>

    <xsl:template match="/">
    <!--  a super smart way to guess how to present result, by parsing the service name :-o -->
                <table border="1">
                    <tr>
                        <td><xsl:value-of select="/root/gui/strings/stat.day"/></td>
                        <td><xsl:value-of select="/root/gui/strings/stat.numberOfSearch"/></td>
                    </tr>
                    <xsl:for-each select="root/gui/search/record">
                        <tr>
                            <th>
                                <xsl:value-of select="day"/>
                            </th>
                            <th align="right">
                                <xsl:value-of select="number"/>
                            </th>
                        </tr>
                    </xsl:for-each>
                </table>
    </xsl:template>
</xsl:stylesheet>
