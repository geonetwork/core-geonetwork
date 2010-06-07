<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!--
	html page with large gif
	-->
	<xsl:template match="/">
		<html>
			<head>
				<title><xsl:value-of select="/root/response/fname"/></title>
			</head>
			<body>
				<img src="{/root/gui/locService}/resources.get?access=public&amp;id={/root/response/id}&amp;fname={/root/response/fname}" border="0"/>
			</body>
		</html>
	</xsl:template>
	
</xsl:stylesheet>
