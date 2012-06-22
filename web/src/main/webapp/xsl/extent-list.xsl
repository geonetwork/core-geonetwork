<?xml version="1.0" encoding="UTF-8"?>
   

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink">

   <xsl:import  href="translate-widget.xsl"/>

	<xsl:output method='xml' encoding='UTF-8' indent='no' />
    <xsl:variable name="langCode">
    	<xsl:choose>
    		<xsl:when test="string(/root/gui/language) = 'ger'">DE</xsl:when>
    		<xsl:otherwise>
    			<xsl:value-of select="translate(substring(string(/root/gui/language),1,2),$LOWER,$UPPER)"/>
    		</xsl:otherwise>
    	</xsl:choose>
    </xsl:variable>

	<xsl:template match="wfs">
		<ul>
			<results style="display:none"><xsl:value-of select="count(/root/response/wfs/featureType/feature)"/></results>
			<xsl:for-each select="/root/response/wfs/featureType/feature">
				<li>
					<displayText>
	                     <xsl:variable name="tmp">
	                        <xsl:call-template name="translations" >
	                            <xsl:with-param name="root" select="desc" />
	                            <xsl:with-param name="langCode" select="$langCode"/>
	                        </xsl:call-template>
	                     </xsl:variable>
	                     <xsl:choose>
	                     	<xsl:when test="normalize-space($tmp) = ''">
	                     		<xsl:value-of select="normalize-space(desc)"/>
	                     	</xsl:when>
	                     	<xsl:otherwise>
			                    <xsl:value-of select="normalize-space($tmp)"/>
	                     	</xsl:otherwise>
	                     </xsl:choose>
					</displayText>
					<valid><xsl:value-of select="../@typename != 'gn:non_validated'"/></valid>
					<href><xsl:value-of select="@href"/></href>
				</li>
			</xsl:for-each>
		</ul>
	</xsl:template>


	<xsl:template match="text()" />

</xsl:stylesheet>