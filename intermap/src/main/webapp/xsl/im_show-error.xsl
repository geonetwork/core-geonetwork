<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!--===========================================================================
Layer transparency form
============================================================================-->

<xsl:template match="/">
	<html>
		<head>
			<title>
				<xsl:value-of select="/root/strings/title" />
			</title>
			<link rel="stylesheet" type="text/css" href="../../intermap.css" />
		</head>
		<body style="margin:10px;" onLoad="javascript:window.focus();">
			<xsl:value-of select="root/response/errors/layer/@message" />
		</body>
	</html>
</xsl:template>
</xsl:stylesheet>

