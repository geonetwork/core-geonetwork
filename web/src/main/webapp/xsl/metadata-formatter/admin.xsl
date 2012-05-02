<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:include href="../main.xsl"/>

	<xsl:template mode="css" match="/">
		<xsl:call-template name="geoCssHeader"/>
		<xsl:call-template name="ext-ux-css"/>
	</xsl:template>

	<xsl:template mode="script" match="/">
		<script type="text/javascript" src="../../scripts/ext/adapter/ext/ext-base.js"></script>
		<xsl:choose>
			<xsl:when test="/root/request/debug">
				<script type="text/javascript" src="../../scripts/ext/ext-all-debug.js"></script>
			</xsl:when>
			<xsl:otherwise>
				<script type="text/javascript" src="../../scripts/ext/ext-all.js"></script>
			</xsl:otherwise>
		</xsl:choose>
		<script type="text/javascript" src="../../scripts/ext/form/FileUploadField.js"></script>
		<script type="text/javascript" src="../../scripts/ext-ux/XmlTreeLoader.js"></script>
		<script type="text/javascript" src="../../scripts/UserXsl.js"></script>
		<script type="text/javascript" language="JavaScript">
			 Ext.onReady(function(){
				new GeoNetwork.UserXsl('xslManager');
			});
		</script>
	</xsl:template>

	<xsl:template name="content">
	  <xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="'Administer Metadata Formatter XSL'"/>
			<xsl:with-param name="content">
				<div id="xslManager" style="width:100%;height:600px;"/>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<button class="content" onclick="load('{/root/gui/locService}/admin')">
					<xsl:value-of select="/root/gui/strings/back"/>
				</button>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

</xsl:stylesheet>