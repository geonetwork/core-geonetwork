<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet 	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
						xmlns:geonet="http://www.fao.org/geonetwork" 
						xmlns:exslt= "http://exslt.org/common"
						xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
						exclude-result-prefixes="xsl exslt geonet">
    <xsl:output method="xml"/>
    <xsl:template match="/">
        <table>
            <tr>
                <td colspan="2">
                    <b><xsl:value-of select="/root/gui/strings/stat.clickToDownload"/>:&#160;
                    <i>
                        <a>
                            <xsl:attribute name="href">
                                <xsl:value-of select="root/gui/statCSV/fileURL"/>
                            </xsl:attribute>
                            <xsl:value-of select="root/gui/statCSV/exportedTable"/>
                        </a>
                    </i>
                    </b>
                </td>
            </tr>
        </table>
    </xsl:template>
</xsl:stylesheet>
