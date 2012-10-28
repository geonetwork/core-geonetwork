<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:include href="geo/utils.xsl"/>

	<!--
	main html header
	-->
	<xsl:template name="header">
		<meta http-equiv="X-UA-Compatible" content="IE=9"/>

		<!-- title -->
		<title><xsl:value-of select="/root/gui/strings/title"/></title>
		<link rel="shortcut icon" type="image/x-icon" href="{/root/gui/url}/images/logos/favicon.gif"/>
		<link rel="icon" type="image/x-icon" href="{/root/gui/url}/images/logos/favicon.gif"/>

		<!-- Recent updates newsfeed -->
		<link href="{/root/gui/locService}/rss.latest?georss=gml" rel="alternate" type="application/rss+xml" title="GeoNetwork opensource GeoRSS | {/root/gui/strings/recentAdditions}" />
		<link href="{/root/gui/locService}/portal.opensearch" rel="search" type="application/opensearchdescription+xml">
		<xsl:attribute name="title"><xsl:value-of select="//site/name"/> (GeoNetwork)</xsl:attribute>
		</link>

		<!-- meta tags -->
		<xsl:copy-of select="/root/gui/strings/header_meta/meta"/>
		<META HTTP-EQUIV="Pragma"  CONTENT="no-cache"/>
		<META HTTP-EQUIV="Expires" CONTENT="-1"/>
		
		<script language="JavaScript" type="text/javascript">
			var Env = new Object();

			Env.host = "<xsl:value-of select="/root/gui/env/server/protocol"/>://<xsl:value-of select="/root/gui/env/server/host"/>:<xsl:value-of select="/root/gui/env/server/port"/>";
			Env.locService= "<xsl:value-of select="/root/gui/locService"/>";
			Env.locUrl    = "<xsl:value-of select="/root/gui/locUrl"/>";
			Env.url       = "<xsl:value-of select="/root/gui/url"/>";
			Env.lang      = "<xsl:value-of select="/root/gui/language"/>";
			Env.proxy     = "<xsl:value-of select="/root/gui/config/proxy-url"/>";

			window.javascriptsLocation = "<xsl:value-of select="/root/gui/url"/>/scripts/";

			<xsl:if test="//service/@name = 'main.home'">
			document.onkeyup = alertkey;

			function alertkey(e) {
				if (!e) {
					if (window.event) {
						e = window.event;
					} else {
						return;
					}
				}

				if (e.keyCode == 13) {
					<xsl:if test="string(/root/gui/session/userId)=''">
					if ($('username').value != '') { // login action
						goSubmit('login')
						return;
						}
					</xsl:if>
					if (document.cookie.indexOf("search=advanced")!=-1)
						runAdvancedSearch();
					else
						runSimpleSearch();
					}
				};
			</xsl:if>
		</script><xsl:text>&#10;</xsl:text>

		<!-- stylesheet -->
		<link rel="stylesheet" type="text/css" href="{/root/gui/url}/geonetwork.css"/>
		<link rel="stylesheet" type="text/css" href="{/root/gui/url}/modalbox.css"/>
		<xsl:apply-templates mode="css" select="/"/>

		<!-- JS -->
		<xsl:call-template name="jsHeader"/>
		
	</xsl:template>
	
	<!--
		All element from localisation files having an attribute named js
		(eg. <key js="true">value</key>) is added to a global JS table. 
		The content of the value could be accessed in JS using the translate 
		function (ie. translate('key');).
	-->
	<xsl:template match="*" mode="js-translations">
		"<xsl:value-of select="name(.)"/>":"<xsl:value-of select="normalize-space(translate(.,'&quot;', '`'))"/>"
		<xsl:if test="position()!=last()">,</xsl:if>
	</xsl:template>
</xsl:stylesheet>
