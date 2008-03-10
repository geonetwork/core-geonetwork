<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- Main template -->
	<xsl:template match="/root/response/mapRoot">
		<div id="im_scale"><xsl:call-template name="scale" /></div>
		<div>
			<img id="im_bm_image" src="{response/url}" />
		</div>		
		<div id="im_bm_image_waitdiv">Loading map...</div>
	</xsl:template>
	
	<xsl:template name="scale">
		<xsl:variable name="scale"><xsl:value-of select="round(response/services/distScale)" /></xsl:variable>
		1:<xsl:value-of select="format-number($scale, '###,###')" />
	</xsl:template>
	
</xsl:stylesheet>
