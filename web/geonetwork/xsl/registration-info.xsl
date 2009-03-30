<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    
	<xsl:template match="/">
    <xsl:choose>
        <xsl:when test="/root/response/result='errorEmailToAddressFailed'">
            <p class="error"><xsl:copy-of select="/root/gui/info/errorEmailToAddressFailed"/></p>
        </xsl:when>
        <xsl:when test="/root/response/result='errorEmailAddressAlreadyRegistered'">
            <p class="error"><xsl:copy-of select="/root/gui/info/errorEmailAddressAlreadyRegistered"/></p>
        </xsl:when>
        <xsl:otherwise>
            <p><xsl:value-of select="/root/gui/info/message"/></p>
        </xsl:otherwise>
    </xsl:choose>
		<p><xsl:value-of select="/root/gui/strings/registrationDetails"/><b><xsl:value-of select="concat(/root/response/@name,' ',/root/response/@surname,' (',/root/response/@email,')')"/></b></p>
	</xsl:template>
    
</xsl:stylesheet>
