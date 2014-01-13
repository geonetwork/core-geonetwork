<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:geonet="http://www.fao.org/geonetwork" xmlns:exslt="http://exslt.org/common"
	exclude-result-prefixes="exslt geonet">
	<xsl:include href="metadata/common.xsl"/>
	<xsl:output omit-xml-declaration="no" method="html" doctype-public="html" indent="yes"
		encoding="UTF-8"/>
	<xsl:variable name="hostUrl"
		select="concat(/root/gui/env/server/protocol, '://', /root/gui/env/server/host, ':', /root/gui/env/server/port)"/>
	<xsl:variable name="baseUrl" select="/root/gui/url"/>
	<xsl:variable name="serviceUrl" select="concat($hostUrl, /root/gui/locService)"/>
	<xsl:variable name="rssUrl" select="concat($serviceUrl, '/rss.search?sortBy=changeDate')"/>
	<xsl:variable name="siteName" select="/root/gui/env/site/name"/>

	<!-- main page -->
	<xsl:template match="/">
		<html xmlns="http://www.w3.org/1999/xhtml">
			<head>
				<title/>
				<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
				<meta http-equiv="X-UA-Compatible" content="IE=9"/>
				<link rel="icon" type="image/gif" href="../../images/logos/favicon.gif"/>
				<link href="../../srv/en/rss.latest?georss=gml" rel="alternate"
					type="application/rss+xml" title="GeoNetwork opensource GeoRSS | Recent Changes"/>
				<link href="../../srv/en/portal.opensearch" rel="search"
					type="application/opensearchdescription+xml" title="GeoNetwork"/>

				<link rel="stylesheet" type="text/css"
					href="../../apps/js/ext/resources/css/ext-all.css"/>
				<link rel="stylesheet" type="text/css"
					href="../../apps/js/ext/resources/css/xtheme-gray.css"/>
				<link rel="stylesheet" type="text/css"
					href="../../apps/js/OpenLayers/theme/default/style.css"/>
			    <xsl:choose>
			        <xsl:when test="/root/request/debug">
			            <link rel="stylesheet" type="text/css" href="../../static/geonetwork-client-ext-ux_css.css?minimize=false"/>
			            <link rel="stylesheet" type="text/css" href="../../static/geonetwork-client_css.css?minimize=false"/>
			        </xsl:when>
			        <xsl:otherwise>
			            <link rel="stylesheet" type="text/css" href="../../static/geonetwork-client-ext-ux_css.css"/>
			            <link rel="stylesheet" type="text/css" href="../../static/geonetwork-client_css.css"/>
			        </xsl:otherwise>
			    </xsl:choose>
			</head>
			<body>
				<div id="loading-mask"></div>
				<div id="loading">
					<div class="loading-indicator">Loading ...</div>
				</div>
				
				<div id="header">
					<div id="title" class="mn-main"></div>
				</div>
				<div id="mn-top">
					<div class="pipe"><a href="#" onclick="Ext.getDom('shortcut').style.display='block';">?</a></div>
					<div class="pipe" id="lang-form"></div>
					<div class="pipe" id="login-form"></div>
					<div class="pipe first" id="mode-form"></div>
				</div>
				
				
				<div id="infoContent">
					
				</div>
				<div id="tag">
					
				</div>
				<div id="shortcut" style="display:none;">
					
				</div>
				
				<xsl:choose>
					<xsl:when test="/root/request/debug">
						
						<script type="text/javascript" src="../../apps/js/ext/adapter/ext/ext-base.js"></script>
						<script type="text/javascript" src="../../apps/js/ext/ext-all-debug.js"></script>
						<script type="text/javascript" src="../../apps/js/ext-ux/Rating/RatingItem.js"></script>
						<script type="text/javascript" src="../../apps/js/ext-ux/FileUploadField/FileUploadField.js"></script>
						<script type="text/javascript" src="../../apps/js/ext-ux/TwinTriggerComboBox/TwinTriggerComboBox.js"></script>
						<script type="text/javascript" src="../../apps/js/ext-ux/DateTime/DateTime.js"></script>
						<script type="text/javascript" src="../../apps/js/ext-ux/RowExpander/RowExpander.js"></script>
						<script type="text/javascript" src="../../apps/js/ext-ux/MultiselectItemSelector-3.0/DDView.js"></script>
						<script type="text/javascript" src="../../apps/js/ext-ux/MultiselectItemSelector-3.0/Multiselect.js"></script>
						<script type="text/javascript" src="../../apps/js/ext-ux/SuperBoxSelect/SuperBoxSelect.js"></script>
						<script type="text/javascript" src="../../apps/js/ext-ux/LightBox/lightbox.js"></script>
						<script type="text/javascript" src="../../apps/js/ext-ux/CheckColumn.js"></script>
						
						
						<script type="text/javascript" src="../../apps/js/proj4js-compressed.js"></script>
						<script type="text/javascript" src="../../apps/js/OpenLayers/lib/OpenLayers.js"></script>
						<script type="text/javascript" src="../../apps/js/GeoExt/lib/overrides/override-ext-ajax.js"></script>
						<script type="text/javascript" src="../../apps/js/GeoExt/lib/GeoExt.js"></script>
						<script type="text/javascript" src="../../apps/js/GeoExt-ux/LayerOpacitySliderPlugin/LayerOpacitySliderPlugin.js"></script>
						
						<script type="text/javascript" src="../../apps/js/GeoNetwork/lib/GeoNetwork.js"></script>
						
						<script type="text/javascript" src="../../apps/search/js/Settings.js"></script>
						<script type="text/javascript" src="../../apps/search/js/Shortcuts.js"></script>
						<script type="text/javascript" src="../../apps/search/js/map/Settings.js"></script>
						<script type="text/javascript" src="../../apps/search/js/map/MapApp.js"></script>
						<script type="text/javascript" src="../../apps/search/js/App.js"></script>
						
					</xsl:when>
					<xsl:otherwise>
					    <script type="text/javascript" src="../../apps/js/ext/adapter/ext/ext-base.js"></script>
					    <script type="text/javascript" src="../../apps/js/ext/ext-all.js"></script>
					    <script type="text/javascript" src="../../static/geonetwork-client-mini-nomap.js"></script>
					    <script type="text/javascript" src="../../static/geonetwork-client-mini.js"></script>
					    <script type="text/javascript" src="../../static/geonetwork-client-search-app.js"></script>
					</xsl:otherwise>
				</xsl:choose>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
