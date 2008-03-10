<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<html>
			<head>
				<title>
					<xsl:value-of select="/root/gui/strings/title" />
				</title>
				<link rel="stylesheet" type="text/css" href="../../intermap.css" />
				<script language="JavaScript1.2" type="text/javascript"> function init() {
					window.name="InterMap"; window.focus(); } </script>
			</head>
			<frameset onload="javascript:init();" rows="110,*" border="0" frameborder="0" framespacing="0">
				<frame name="banner" bordercolor="#fff" frameborder="0" framespacing="0" marginheight="0"
					marginwidth="0" noresize="noresize" scrolling="no" src="{/root/gui/locService}/banner"/>
				<frame bordercolor="#fff" frameborder="0" framespacing="0" name="main" marginheight="0" marginwidth="0" 
					noresize="noresize" scrolling="yes" src="{/root/gui/locService}/mapServers.getServices?mapserver={/root/response/mapserver}&amp;url={/root/response/url}"/>
			</frameset>
		</html>
	</xsl:template>
</xsl:stylesheet>
