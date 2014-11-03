<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:srv="http://www.isotc211.org/2005/srv">

    <xsl:param name="atomProtocol"/>

    <xsl:template match="gmd:MD_Metadata">
        <!--<feeds>
        <xsl:for-each  select="gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint[ends-with(gmd:CI_OnlineResource/gmd:linkage/gmd:URL, 'xml')]">
            <atomfeed><xsl:value-of select="gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/></atomfeed>
        </xsl:for-each>
        </feeds>-->


        <xsl:variable name="lower">abcdefghijklmnopqrstuvwxyz</xsl:variable>
        <xsl:variable name="upper">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>

        <!-- Get first element. TODO: Check if can be several -->
        <atomfeed><xsl:value-of select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource[translate(gmd:protocol/gco:CharacterString, $upper, $lower) = translate($atomProtocol, $upper, $lower)]/gmd:linkage/gmd:URL"/></atomfeed>

    </xsl:template>
</xsl:stylesheet>