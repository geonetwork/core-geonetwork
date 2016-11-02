<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
				xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:srv="http://www.isotc211.org/2005/srv">
    <xsl:template match="gmd:MD_Metadata">
        <fileIdentifiers>
            <xsl:for-each  select="gmd:identificationInfo/srv:SV_ServiceIdentification/srv:operatesOn[@xlink:href!='' and @uuidref!='']">
				<xsl:variable name="fileIdentifier">
					<xsl:call-template name="getParamFromUrl">
				       	<xsl:with-param name="url" select="@xlink:href"/>
						<xsl:with-param name="paramName" select="'id'"/>
					</xsl:call-template>
           		</xsl:variable>
           		<fileIdentifier><xsl:value-of select="normalize-space($fileIdentifier)"/></fileIdentifier>                    	
			</xsl:for-each>
        </fileIdentifiers>
    </xsl:template>

    <xsl:template name="getParamFromUrl">
       	<xsl:param name="url" />
		<xsl:param name="paramName" />
		<xsl:variable name="paramValue"><xsl:choose><xsl:when test="contains($url,concat('&amp;amp;',$paramName,'='))"><xsl:value-of select="substring-after($url,concat('&amp;amp;',$paramName,'='))"/></xsl:when><xsl:when test="contains($url,concat('&amp;',$paramName,'='))"><xsl:value-of select="substring-after($url,concat('&amp;',$paramName,'='))"/></xsl:when></xsl:choose></xsl:variable>
		<xsl:choose>
			<xsl:when test="contains($paramValue,'&amp;')"><xsl:value-of select="substring-before($paramValue,'&amp;')"/></xsl:when>
			<xsl:when test="contains($paramValue,'&amp;amp;')"><xsl:value-of select="substring-before($paramValue,'&amp;amp;')"/></xsl:when>
			<xsl:otherwise><xsl:value-of select="$paramValue"/></xsl:otherwise>
		</xsl:choose>
    </xsl:template>
</xsl:stylesheet>