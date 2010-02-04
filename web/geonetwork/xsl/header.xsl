<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:include href="geo/utils.xsl"/>
	
	<!--
	main html header
	-->
	<xsl:template name="header">
		
		<!-- title -->
		<title><xsl:value-of select="/root/gui/strings/title"/></title>
		<link href="{/root/gui/url}/favicon.ico" rel="shortcut icon" type="image/x-icon" />
		<link href="{/root/gui/url}/favicon.ico" rel="icon" type="image/x-icon" />

		<!-- stylesheet -->
		<link rel="stylesheet" type="text/css" href="{/root/gui/url}/geonetwork.css"/>
		<link rel="stylesheet" type="text/css" href="{/root/gui/url}/modalbox.css"/>
		
		<!-- Recent updates newsfeed -->
		<link href="{/root/gui/locService}/rss.latest?georss=gml" rel="alternate" type="application/rss+xml" title="GeoNetwork opensource GeoRSS | {/root/gui/strings/recentAdditions}" />
		<link href="{/root/gui/locService}/portal.opensearch" rel="search" type="application/opensearchdescription+xml">

		<xsl:attribute name="title">GeoNetwork|<xsl:value-of select="//site/organization"/>|<xsl:value-of select="//site/name"/></xsl:attribute>

		</link>

		
		<!-- meta tags -->
		<xsl:copy-of select="/root/gui/strings/header_meta/meta"/>
		
		<META HTTP-EQUIV="Pragma"  CONTENT="no-cache"/>
		<META HTTP-EQUIV="Expires" CONTENT="-1"/>

		<!-- javascript -->
		<!-- Add geo JS (ie. OpenLayers, GeoExt) only when needed, ie. metadata.edit metadata.show, ... -->
		<xsl:if test="/root/gui/reqService = 'main.home' 
					or /root/gui/reqService = 'metadata.show' 
					or /root/gui/reqService = 'metadata.edit'">
			<xsl:call-template name="geoHeader"/>
		</xsl:if>
		<!-- Add Ext selection panel JS only when needed -->
		<xsl:if test="/root/gui/reqService = 'metadata.edit'">
			<xsl:call-template name="selectionPanel"/>
		</xsl:if>
		
		<script type="text/javascript" src="{/root/gui/url}/scripts/prototype.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/geonetwork.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/scriptaculous/scriptaculous.js?load=slider,effects,controls"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/modalbox.js"></script>
		<script type="text/javascript" src="{/root/gui/url}/scripts/form_check.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/tablednd.js"></script>
		
	
		<script language="JavaScript" type="text/javascript">
			var Env = new Object();

			Env.locService= "<xsl:value-of select="/root/gui/locService"/>";
			Env.locUrl    = "<xsl:value-of select="/root/gui/locUrl"/>";
			Env.url       = "<xsl:value-of select="/root/gui/url"/>";
			Env.lang      = "<xsl:value-of select="/root/gui/language"/>";
            Env.proxy     = "<xsl:value-of select="/root/gui/config/proxy-url"/>";
			var on        = "<xsl:value-of select="/root/gui/url"/>/images/plus.gif";
            var off       = "<xsl:value-of select="/root/gui/url"/>/images/minus.png";
			
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
			
            var translations = {
				<xsl:apply-templates select="/root/gui/strings/*[@js='true' and not(*) and not(@id)]" mode="js-translations"/>
			};

			function translate(text) {
				return translations[text] || text;
			}
		</script>
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
