<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:include href="index-embedded.xsl"/>
	
	<xsl:template match="/">
		<html>
			<head>
				<title>
					<xsl:value-of select="/root/gui/strings/title" />
				</title>
				<link rel="stylesheet" type="text/css" href="../../intermap-embedded.css?" />

				<script language="JavaScript" type="text/javascript">
					var Env = new Object();
					
					Env.locService= "<xsl:value-of select="/root/gui/locService"/>";
					Env.locUrl    = "<xsl:value-of select="/root/gui/locUrl"/>";
					Env.url       = "<xsl:value-of select="/root/gui/url"/>";
					Env.lang      = "<xsl:value-of select="/root/gui/language"/>";
					
					var getIMServiceURL = function(service)
					{
					   return "<xsl:value-of select="/root/gui/locService"/>/"+service;
					};
				</script>
				
				<script type="text/javascript" src="{/root/gui/url}/scripts/prototype.js"/>
				
				<!-- scriptaculous -->
				<script type="text/javascript" src="{/root/gui/url}/scripts/scriptaculous/slider.js"/>
				<script type="text/javascript" src="{/root/gui/url}/scripts/scriptaculous/effects.js"/>
				<script type="text/javascript" src="{/root/gui/url}/scripts/scriptaculous/controls.js"/>
				<script type="text/javascript" src="{/root/gui/url}/scripts/scriptaculous/dragdrop.js"/>
				
				<script type="text/javascript" src="../../scripts/util.js?" />
				<script type="text/javascript" src="../../scripts/im_extras.js?"/>
				<script type="text/javascript" src="../../scripts/im_ajax.js?" />
				<script type="text/javascript" src="../../scripts/im_layers.js?" />
				
				<script type="text/javascript" src="../../scripts/im_class.js?"/>
<!--				<script type="text/javascript" src="/intermap/scripts/im_minimap.js?"/>-->
				<script type="text/javascript" src="../../scripts/im_bigmap.js?" />
				<script type="text/javascript" src="../../scripts/im_standalone.js?"/>				
				<script type="text/javascript" src="{/root/gui/url}/scripts/im_markers.js?"/>			
				
<!--				<script type="text/javascript" src="{/root/gui/url}/scripts/core/kernel/kernel.js?"/>
				<script type="text/javascript" src="{/root/gui/url}/scripts/editor/tooltip-manager.js?"/>
-->					
				
				<script type="text/javascript">
					
										
				</script>
			</head>
			
			<body onload="im_boot();">
				<xsl:call-template name="bigmap"/>
			</body>					
		</html>
		
	</xsl:template>
		
</xsl:stylesheet>
