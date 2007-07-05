<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:include href="banner.xsl"/>
	<xsl:include href="layers.xsl"/>
<!--	<xsl:include href="map.xsl"/>
-->	
	<xsl:template match="/">
		<html>
			<head>
				<title>
					<xsl:value-of select="/root/gui/strings/title" />
				</title>
				<link rel="stylesheet" type="text/css" href="../../intermap.css?" />
				
				<script language="javascript" src="../../scripts/prototype.js" />
				<script language="javascript" src="../../scripts/scriptaculous/scriptaculous.js?load=slider,effects,dragdrop" />
				<script language="javascript" src="../../scripts/util.js" />
				<script language="javascript" src="../../scripts/gui.js?" />
				<script language="javascript" src="../../scripts/connectors/intermap.js?" />
				
				<script language="JavaScript" type="text/javascript">
					var minLayersDivWidth = 236;
				
					// Initialize
					function init()
					{
						initDivs();
						
						window.name = "InterMap";
						window.focus();
												
						//Event.observe(document, 'mousedown', resizeLayersDivStart);
						new Draggable('im_resizeBar', {constraint:'horizontal',change:resizeLayersDiv});
						
						Event.observe('im_map', 'mousedown', mousedownEventListener);						
						
						setTool('zoomin'); // set the default tool
						
						imc_reloadLayers(); // append layers to list
						setLayersDivWidth(minLayersDivWidth);
						
						Event.observe('im_geonetRecords', 'mouseover', function(){ $('im_geonetRecords').className = 'opaque'; });
						Event.observe('im_geonetRecords', 'mouseout', function(){ $('im_geonetRecords').className = ''; });
						
						// keyboard events
						Event.observe(document, 'keypress', function(e) { keyPressed(e) });
					}
					
					function initDivs()
					{
						$('im_map').style.height = <xsl:value-of select="/root/response/mapRoot/response/imageHeight" /> + 'px';
						$('im_map').style.width = <xsl:value-of select="/root/response/mapRoot/response/imageWidth" /> + 'px';
						
						updateDivs();
					}
					
					function updateDivs()
					{
						var height = getWindowSize()[1] - Element.getHeight('im_banner');
						
						$('im_layers').style.height = height + 'px';
						$('im_resizeBar').style.height = height + 'px';
					}
				</script>
			</head>
			<body onLoad="javascript:init();" onResize="javascript:updateDivs();">
				<div id="im_banner">
					<xsl:call-template name="banner" />
				</div>
								
					
				<div id="im_layers" >
					<xsl:apply-templates select="/root/response/layersRoot" />
				</div>
				
				<div id="im_resizeBar" />
				
				<div id="im_mapAndToolbar">
					<div id="im_toolbar">
						<!--						<td class="banner-menu" colspan="2">
							<table width="850" border="0" cellpadding="0" cellspacing="0" id="nav">						
						-->
						<ul>
							<li class="im_tool" id="im_tool_fullextent"><a id="im_fullExtentButton" href="javascript:fullExtentButtonListener()">Full</a></li>
							<li class="im_tool" id="im_tool_aoi">         <a id="im_oaiButton" href="javascript:setTool('aoi')">AOI</a></li> 
							<li class="im_tool" id="im_tool_zoomin">   <a href="javascript:setTool('zoomin');"><img src="/intermap/images/zoomin.png" title="Zoom in"/>ZZZ</a></li>
							<li class="im_tool" id="im_tool_zoomout"> <a href="javascript:setTool('zoomout');"><img src="/intermap/images/zoomout.png" title="Zoom out"/>ZZZ</a></li>
							<li class="im_tool" id="im_tool_zoomsel"> <a href="javascript:imc_zoomToLayer(activeLayerId)"><img src="/intermap/images/zoomsel.png" title="Zoom to selected"/>ZZZ</a></li>
							<li class="im_tool" id="im_tool_pan">        <a href="javascript:setTool('pan');"><img src="/intermap/images/pan.png" title="Pan"/>ZZZ</a></li>
							<li class="im_tool" id="im_tool_identify">   <a href="javascript:setTool('identify');">Identify</a></li>
							<li class="im_tool" >                                <a href="javascript:print();"><img src="/intermap/images/print.png" title="Print"/></a></li>
							<li class="im_tool" >                                 <a href="javascript:resizeImage()">+/- map</a></li>
							<li class="im_tool" >                                 <a href="{/root/gui/locService}/mapServers.listServers">Add layer</a></li>
							<li class="im_tool" id="im_tool_reset">      <a href="{/root/gui/locService}/map.reset">Reset</a></li>
							<!--						<li class="fullextent"><a id="fullExtentButton" href="javascript:fullExtentButtonListener()">Full extent</a></li>
								<li class="aoi"><a id="oaiButton" href="javascript:setTool('aoi')">Area of interest</a></li> 
								<li class="zoomin"><a href="javascript:setTool('zoomin');"><img src="/intermap/images/zoomin.png" title="Zoom in"/>Zoom in</a></li>
								<li class="zoomout"><a href="javascript:setTool('zoomout');"><img src="/intermap/images/zoomout.png" title="Zoom out"/>Zoom out</a></li>
								<li class="zoomToSelected"><a href="javascript:imc_zoomToLayer(activeLayerId)"><img src="/intermap/images/zoomsel.png" title="Zoom to selected"/>Zoom to selected</a></li>
								<li class="pan"><a href="javascript:setTool('pan');"><img src="/intermap/images/pan.png" title="Pan"/>Pan</a></li>
								<li class="identify"><a href="javascript:setTool('identify');">Identify</a></li>
								<li><a href="javascript:print();"><img src="/intermap/images/print.png" title="Print"/>Print</a></li>
								<li><a href="javascript:resizeImage()">Bigger/Smaller map</a></li>
								<li><a href="{/root/gui/locService}/mapServers.listServers">Add layer</a></li>
								<li class="reset"><a href="{/root/gui/locService}/map.reset">Reset</a></li>
							-->					</ul>
						<!--
							</table>
							</td>
							</tr>
						-->					
					</div>

					<xsl:apply-templates select="/root/response/mapRoot" />
				</div>
				
				
				<div id="im_inspector">
<!--				<span id="inspectorCloseButton">close</span><br/> -->
					transparency:
					<div id="im_transparencySlider">
						<div id="im_transparencyHandle"> </div>
					</div>
					<div id="im_transparencyValue" />
					<div id="im_legendButton">Legend</div>
					<!--
					<div id="getFeatures">Get features</div>
					-->
				</div>
				
				<script language="javascript">
					// legend button
					Event.observe('im_legendButton', 'click', function(e){ showLegend(); });
					
					// transparency slider
					new Control.Slider( 'im_transparencyHandle', 'im_transparencySlider',
						{
							values:[0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100], range:$R(0,100),
							onSlide:function(v) { $('transparencyValue').innerHTML = v },
							onChange:function(v) { $('transparencyValue').innerHTML = v; transparencySliderMoved(v) }
						}
					);
				</script>
				
				<div id="im_addLayers" />
				
				<div id="im_debug" />
				
				<div id="im_geonetRecords" class="im_hidden">
				<!--
					<input type="text" id="liveSearch" />
				-->
				</div>
			</body>
		</html>
	</xsl:template>
	
	
	<!-- Main template -->
	<xsl:template match="/root/response/mapRoot">
		<div id="im_scale"><xsl:call-template name="scale" /></div>
		<div id="im_map">
			<img id="im_mapImg" src="{response/url}" />
		</div>		
		<div id="im_pleaseWait">Loading map...</div>
	</xsl:template>
	
	<xsl:template name="scale">
		<xsl:variable name="scale"><xsl:value-of select="round(response/services/distScale)" /></xsl:variable>
		1:<xsl:value-of select="format-number($scale, '###,###')" />
	</xsl:template>
	
</xsl:stylesheet>
