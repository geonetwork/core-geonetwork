<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:srv="http://www.isotc211.org/2005/srv">
    <xsl:template match="gmd:MD_Metadata">
        <datasets>
            <xsl:for-each  select="gmd:identificationInfo/srv:SV_ServiceIdentification/srv:operatesOn">
                <dataset><xsl:value-of select="@uuidref"/></dataset>
            </xsl:for-each>
        </datasets>
    </xsl:template>
</xsl:stylesheet>