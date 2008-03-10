<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:include href="main.xsl"/>

	<!--
	page content
	-->
	<xsl:template name="content">
		<div id="error">
			<h2><xsl:value-of select="/root/gui/strings/mdUpdateError"/></h2>
			<p id="error"><xsl:value-of select="/root/gui/strings/messageMdUpdateError"/></p>
			<p id="stacktrace"><xsl:value-of select="/root/error/class"/> : <xsl:value-of select="/root/error/exception/message"/></p>
			<p>
				<button class="content" onclick="load('{/root/gui/locService}/metadata.edit?id={/root/error/id}')">
					<xsl:value-of select="/root/gui/strings/backToEditor"/>
				</button>
			</p>
		</div>
	</xsl:template>
	
</xsl:stylesheet>
