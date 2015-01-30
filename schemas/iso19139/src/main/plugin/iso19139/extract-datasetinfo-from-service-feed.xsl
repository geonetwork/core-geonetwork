<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:atom="http://www.w3.org/2005/Atom"
                xmlns:georss="http://www.georss.org/georss" xmlns:inspire_dls="http://inspire.ec.europa.eu/schemas/inspire_dls/1.0"
        >
    <xsl:template match="atom:feed">
        <datasets>
            <xsl:for-each  select="atom:entry[inspire_dls:spatial_dataset_identifier_code]">
                <dataset>
                    <identifier><xsl:value-of select="inspire_dls:spatial_dataset_identifier_code"/></identifier>
                    <namespace><xsl:value-of select="inspire_dls:spatial_dataset_identifier_namespace"/></namespace>
                </dataset>
            </xsl:for-each>
        </datasets>
    </xsl:template>
</xsl:stylesheet>