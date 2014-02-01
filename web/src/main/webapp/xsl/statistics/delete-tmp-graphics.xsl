<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml"/>

    <xsl:template match="/">
                <table>
                    <tr>
                        <td colspan="3"><xsl:value-of select="/root/gui/deleteTmpGraphics/message"/><xsl:value-of select="/root/gui/strings/stat.filesDel"/></td>
                    </tr>
                </table>
    </xsl:template>
</xsl:stylesheet>
