<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
							  xmlns:fo="http://www.w3.org/1999/XSL/Format">


	<xsl:include href="../header.xsl"/>
	<xsl:include href="../banner.xsl"/>

	<xsl:variable name="widgetPath">../../apps</xsl:variable>
	<xsl:variable name="indent" select="100"/>

	<xsl:template mode="css" match="/" priority="2">
		<link rel="stylesheet" type="text/css" href="{$widgetPath}/js/ext/resources/css/ext-all.css"/>
		<link rel="stylesheet" type="text/css" href="{$widgetPath}/js/ext-ux/FileUploadField/file-upload.css"/>
		<link rel="stylesheet" type="text/css" href="{$widgetPath}/css/gnmapdefault.css"/>
		<link rel="stylesheet" type="text/css" href="{$widgetPath}/css/gnmetadatadefault.css"/>
		<link rel="stylesheet" type="text/css" href="{$widgetPath}/css/metadata-view.css"/>
	</xsl:template>

	<xsl:template mode="script" match="/" priority="2">

		<script type="text/javascript" src="{$widgetPath}/js/proj4js-compressed.js"/>
		<script type="text/javascript" src="{$widgetPath}/js/ext/adapter/ext/ext-base.js"/>
		<xsl:choose>
			<xsl:when test="/root/request/debug">
				<script type="text/javascript" src="{$widgetPath}/js/ext/ext-all-debug.js"/>
			</xsl:when>
			<xsl:otherwise>
				<script type="text/javascript" src="{$widgetPath}/js/ext/ext-all.js"/>
			</xsl:otherwise>
		</xsl:choose>
		<script type="text/javascript" src="{$widgetPath}/../static/geonetwork-client-mini.js"/>
		
		<xsl:if test="/root/request/debug">
            <script type="text/javascript" src="{$widgetPath}/js/GeoNetwork/lib/GeoNetwork/data/ThesaurusFeedStore.js" />
            <script type="text/javascript" src="{$widgetPath}/js/GeoNetwork/lib/GeoNetwork/data/ThesaurusStore.js" />
            <script type="text/javascript" src="{$widgetPath}/js/GeoNetwork/lib/GeoNetwork/widgets/admin/ThesaurusManagerPanel.js" />

		</xsl:if>
		<script type="text/javascript" language="JavaScript">
			var catalogue;

			OpenLayers.ProxyHostURL = '../../proxy?url=';

			OpenLayers.ProxyHost = function(url){
				/**
				 * Do not use proxy for local domain.
				 * This is required to keep the session activated.
				 */
				if (url &amp;&amp; url.indexOf(window.location.host) != -1) {
					return url;
				} else {
					
					return OpenLayers.ProxyHostURL + encodeURIComponent(url);
					
				}
			};
			Ext.onReady(function(){
				GeoNetwork.Util.setLang('<xsl:value-of select="/root/gui/language"/>');

				catalogue = new GeoNetwork.Catalogue({
					statusBarId : 'info',
					hostUrl: '../..',
					lang: '<xsl:value-of select="/root/gui/language"/>',
					mdOverlayedCmpId : 'resultsPanel'
				});

				var manager = new GeoNetwork.admin.ThesaurusManagerPanel({
					catalogue: catalogue,
					feed: '<xsl:value-of select="/root/gui/config/repository/thesaurus"/>',
					renderTo: 'manager',
					autoWidth : true,
					layout : 'border',
					height: 680
				});
			})
		</script>
	</xsl:template>

	<xsl:template match="/">
		<html>
			<head>
				<xsl:call-template name="header"/>
				<xsl:apply-templates mode="script" select="/"/>

				<style type="text/css">
					body {
						height:100%;
					}
				</style>
			</head>
			<body>
				<xsl:call-template name="banner"/>
				<div id="content_container">
					<xsl:call-template name="content"/>
				</div>
			</body>
		</html>
	</xsl:template>

	<xsl:template name="content">

		<table	width="100%" height="100%">
			<tr>
				<td class="padded-content" width="{$indent}"/>
				<td class="dots"/>
				<td class="padded-content" style="height:25px;">
					<h1><xsl:value-of select="/root/gui/strings/thesaurus/management"/></h1>
				</td>
			</tr>
			<tr>
				<td class="padded-content" width="{$indent}"/>
				<td class="dots"/>
				<td style="padding:5px;">
					<div id="manager" style="width:100%;" align="left"/>
				</td>
			</tr>
			<tr>
				<td class="padded-content" width="{$indent}" style="height:25px;"/>
				<td class="dots"/>
				<td class="padded-content" style="text-align:center;">
					<button class="content" onclick="load('{/root/gui/locService}/admin')">
						<xsl:value-of select="/root/gui/strings/back"/>
					</button>
				</td>
			</tr>
			<tr><td class="blue-content" colspan="3"/></tr>
		</table> 
	</xsl:template>

</xsl:stylesheet>
