<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gml="http://www.opengis.net/gml" >
<xsl:template match="/">
	<html>
		<script>
			var closeTimerID;
		</script>
		<body onunload="clearTimeout(closeTimerID);">
			<script>
				function closeWindow() {
					clearTimeout(closeTimerID);
					window.close();
				}
				closeTimerID = window.setTimeout("closeWindow()",7000);
				window.location='<xsl:value-of select="/root/response/url" />';
			</script>
		</body>
	</html>
</xsl:template>
</xsl:stylesheet>
