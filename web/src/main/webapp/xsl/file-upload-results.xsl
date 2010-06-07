<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="/">
	<html>
		<head>
			<script type="text/javascript" src="{/root/gui/url}/scripts/prototype.js"/>
		</head>
		<body>
		<center>
			<h2><xsl:value-of select="/root/gui/strings/fileUploadSuccessful"/></h2>
			<p/>
			<table>
				<tr>
					<th><xsl:value-of select="concat(/root/gui/strings/file,': ')"/></th>
					<td><span id="filename_uploaded" title="{/root/response/fname}"><xsl:value-of select="/root/response/fname"/></span></td>
				</tr>
				<tr>
					<th><xsl:value-of select="concat(/root/gui/strings/sizeBytes,': ')"/></th>
					<td align="right"><span id="filesize_uploaded" title="{/root/response/fsize}"><xsl:value-of select="/root/response/fsize"/></span></td>
				</tr>
			</table>
		</center>
		</body>
	</html>
	</xsl:template>
</xsl:stylesheet>
