<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html" indent="yes"/>
	<xsl:template match="/">
		<script type="text/javascript" src="{/root/gui/url}/scripts/prototype.js"/>
		<div id="error">
			<h2><xsl:value-of select="/root/gui/error/heading"/></h2>
			<p><xsl:value-of select="/root/gui/error/message"/></p>
			<p><xsl:value-of select="/root/error/class"/> : <xsl:value-of select="/root/error/message"/></p>
		</div>
	</xsl:template>
</xsl:stylesheet>
