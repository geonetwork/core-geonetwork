<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:template match="/">
<!--		<html>
			<head>
				<title>
					<xsl:value-of select="/root/gui/strings/title" />
				</title>
-->				
		<div id="intermap_root">
				<link rel="stylesheet" type="text/css" href="/intermap/intermap.css?" />
				
<!--				<script language="javascript" src="/intermap/scripts/prototype.js" />
				<script language="javascript" src="/intermap/scripts/scriptaculous/scriptaculous.js?load=slider,effects,dragdrop" />-->
				<script language="javascript" src="/intermap/scripts/util.js" />
				<script language="javascript" src="/intermap/scripts/gui.js?" />
				<script language="javascript" src="/intermap/scripts/connectors/intermap.js?" />
				
				<script language="JavaScript" type="text/javascript">
					var minLayersDivWidth = 236;
				
					// Initialize
					function im_init()
					{
						initDivs();
						
						window.name = "InterMap";
						window.focus();
												
						//Event.observe(document, 'mousedown', resizeLayersDivStart);
						new Draggable('resizeBar', {constraint:'horizontal',change:resizeLayersDiv});
						
						Event.observe('map', 'mousedown', mousedownEventListener);						
						
						setTool('zoomin'); // set the default tool
						
						appendLayers(); // append layers to list
						setLayersDivWidth(minLayersDivWidth);
						
						Event.observe('geonetRecords', 'mouseover', function(){ $('geonetRecords').className = 'opaque'; });
						Event.observe('geonetRecords', 'mouseout', function(){ $('geonetRecords').className = ''; });
						
						// keyboard events
						Event.observe(document, 'keypress', function(e) { keyPressed(e) });
					}
					
					function initDivs()
					{
						$('map').style.height = <xsl:value-of select="/root/response/mapRoot/response/imageHeight" /> + 'px';
						$('map').style.width = <xsl:value-of select="/root/response/mapRoot/response/imageWidth" /> + 'px';
						
						updateDivs();
					}
					
					function updateDivs()
					{
						var height = getWindowSize()[1] - Element.getHeight('banner');
						
						$('layers').style.height = height + 'px';
						$('resizeBar').style.height = height + 'px';
					}
				</script>
		
<!--			</head>
			
			<body onLoad="javascript:init();" onResize="javascript:updateDivs();">
						
-->					
				<div id="layers" >
					<xsl:apply-templates select="/root/response/layersRoot" />
				</div>
				
				<div id="resizeBar" />
				
				<div id="mapAndToolbar">
					<div id="im_toolbar">
						<!--						<td class="banner-menu" colspan="2">
							<table width="850" border="0" cellpadding="0" cellspacing="0" id="nav">						
						-->
						<ul>
							<li class="im_tool" id="im_tool_fullextent"><a id="fullExtentButton" href="javascript:fullExtentButtonListener()">Full</a></li>
							<li class="im_tool" id="im_tool_aoi">         <a id="oaiButton" href="javascript:setTool('aoi')">AOI</a></li> 
							<li class="im_tool" id="im_tool_zoomin">   <a href="javascript:setTool('zoomin');"><img src="/intermap/images/zoomin.png" title="Zoom in"/>ZZZ</a></li>
							<li class="im_tool" id="im_tool_zoomout"> <a href="javascript:setTool('zoomout');"><img src="/intermap/images/zoomout.png" title="Zoom out"/>ZZZ</a></li>
							<li class="im_tool" id="im_tool_zoomsel"> <a href="javascript:imc_zoomToLayer(activeLayerId)"><img src="/intermap/images/zoomsel.png" title="Zoom to selected"/>ZZZ</a></li>
							<li class="im_tool" id="im_tool_pan">        <a href="javascript:setTool('pan');"><img src="/intermap/images/pan.png" title="Pan"/>ZZZ</a></li>
							<li class="im_tool" id="im_tool_identify">   <a href="javascript:setTool('identify');">Identify</a></li>
							<li class="im_tool" >                                <a href="javascript:print();"><img src="/intermap/images/print.png" title="Print"/></a></li>
							<li class="im_tool" >                                 <a href="javascript:resizeImage()">+/- map</a></li>
							<li class="im_tool" >                                 <a href="{/root/gui/locService}/mapServers.listServers">Add layer</a></li>
							<li class="im_tool" id="im_tool_reset">      <a href="{/root/gui/locService}/map.reset">Reset</a></li>
						</ul>
					</div>

					<xsl:apply-templates select="/root/response/mapRoot" />
				</div>
				
				
				<div id="inspector">
<!--				<span id="inspectorCloseButton">close</span><br/> -->
					transparency:
					<div id="transparencySlider">
						<div id="transparencyHandle"> </div>
					</div>
					<div id="transparencyValue" />
					<div id="legendButton">Legend</div>
					<!--
					<div id="getFeatures">Get features</div>
					-->
				</div>
				
				<script language="javascript">
					// legend button
					Event.observe('legendButton', 'click', function(e){ showLegend(); });
					
					// transparency slider
					new Control.Slider( 'transparencyHandle', 'transparencySlider',
						{
							values:[0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100], range:$R(0,100),
							onSlide:function(v) { $('transparencyValue').innerHTML = v },
							onChange:function(v) { $('transparencyValue').innerHTML = v; transparencySliderMoved(v) }
						}
					);
				</script>
				
				<div id="addLayers" />
				
				<div id="debug" />
				
				<div id="geonetRecords" class="hidden">
				<!--
					<input type="text" id="liveSearch" />
				-->
				</div>
			</div> <!-- END OF IM CONTAINER -->
<!--			</body>
		</html>-->
	</xsl:template>
	
	
	<!-- Main template -->
	<xsl:template match="/root/response/mapRoot">
		<div id="scale"><xsl:call-template name="scale" /></div>
		<div id="map">
			<img id="mapImg" src="{response/url}" />
		</div>		
		<div id="pleaseWait">Loading map...</div>
	</xsl:template>


	<xsl:template name="scale">
		<xsl:variable name="scale"><xsl:value-of select="round(response/services/distScale)" /></xsl:variable>
		1:<xsl:value-of select="format-number($scale, '###,###')" />
	</xsl:template>


	<xsl:template match="/root/response/layersRoot">
		<div id="layersHeader">
			<h3>
				<xsl:value-of select="/root/gui/strings/layers"/>
			</h3>
			<!-- Layers -->
			<div id="layersDiv">
				<ul id="layerList" />
			</div>
			<table id="refresh">
				<tr>
					<td>
						<button id="refreshButton" class="disabled">
							<xsl:value-of select="/root/gui/strings/refresh" />
						</button>
					</td>
				</tr>
			</table>
		</div>
		
		<!-- layers toolbar -->
		<div id="layersToolbar">
			<!-- delete layer button -->
			<span id="deleteLayerButton">delete</span>
			<xsl:text> </xsl:text>
			<!-- add layer button -->
			<!-- 
				<span id="addLayersButton">add</span>
			-->
			<a href="{/root/gui/locService}/mapServers.listServers">add</a>
			<!-- event listeners -->
			<script language="javascript">
				// <![CDATA[
				// Event.observe('addLayersButton', 'click', function(e){ addButtonPushed(); });
				Event.observe('deleteLayerButton', 'click', function(e){ deleteButtonListener(); });
				Event.observe('refreshButton', 'click', function(e){ refreshButtonListener(); });
			// ]]>
			</script>
		</div>
	</xsl:template>

</xsl:stylesheet>
