<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:template match="/">

		<root>
			<info>
				<xsl:copy-of  select="//minimap" />	
				<xsl:copy-of  select="//bigmap" />	
			</info>
			<html>


		<div id="intermap_root"> <!-- class will be set to current tool -->
<!--				<link rel="stylesheet" type="text/css" href="/intermap/intermap-embedded.css?" />-->
				
<!--				<script language="javascript" src="/intermap/scripts/prototype.js" />
				<script language="javascript" src="/intermap/scripts/scriptaculous/scriptaculous.js?load=slider,effects,dragdrop" />-->
<!--				<script language="javascript" src="/intermap/scripts/util.js" />
				<script language="javascript" src="/intermap/scripts/gui.js?" />
				<script language="javascript" src="/intermap/scripts/connectors/intermap.js?" />
-->				
			
			<div id="im_resizeBar" style="display:none;"/> <!-- DUMMY -->
			
			<table class="padded_content">
				<tr>
					<!-- TOOLBAR -->
					<td>
						<table id="im_toolbar" class="padded_content">
							<tr id="im_toolSelector"> <!--class="im_tool"-->
								<td class="im_tool" id="im_tool_fullextent"    	onClick="javascript:fullExtentButtonListener()"><img src="/intermap/images/zoomfull.png" title="Zoom to full map extent"/></td>
								<td class="im_tool" id="im_tool_zoomin"	onClick="javascript:setTool('zoomin');" ><img src="/intermap/images/zoomin.png" title="Zoom in"/></td>
								<td class="im_tool" id="im_tool_zoomout" 	onClick="javascript:setTool('zoomout');"><img  src="/intermap/images/zoomout.png" title="Zoom out"/></td>
								<td class="im_tool" id="im_tool_pan"		onClick="javascript:setTool('pan');"><img src="/intermap/images/pan.png" title="Pan"/></td>
								<td class="im_tool" id="im_tool_zoomsel"	onClick="javascript:imc_zoomToLayer(activeLayerId)"><img src="/intermap/images/zoomsel.png" title="Zoom to selected layer extent"/></td>
								<td class="im_tool" id="im_tool_aoi"		onClick="javascript:setTool('aoi')"><img src="/intermap/images/im_aoi16x16.png" title="Select an Area Of Interest"/></td> 
<!--								<td class="im_tool" id="im_tool_identify"	onClick="javascript:setTool('identify');">Identify</td> -->
								<td width="100%"/> <!-- spacer -->
								<td class="im_tool"  				onClick="javascript:resizeImage()">+/- map</td>
								<td class="im_tool" id="im_tool_reset"	onClick="javascript:im_reset();">Reset</td>
								
							</tr>
							
						</table>
						
					</td>
					
					<!--  LAYERS -->
					<td rowspan="4">
						<div id="im_layers" >
							<xsl:apply-templates select="/root/response/layersRoot" />
						</div>
					</td>					
				</tr>
				
				<tr>
					<td id="im_mapContainer" style="position:relative;width:370px;height:278px;">
						<div id="im_map" style="position: absolute;">
							<img id="im_mapImg" src="{//mapRoot/response/url}" />
						</div>		
						<div id="im_scale" style="position: absolute;" >
							<xsl:variable name="scale"><xsl:value-of select="round(//mapRoot/response/services/distScale)" /></xsl:variable>
							1:<xsl:value-of select="format-number($scale, '###,###')" />
						</div>
						<div id="im_pleaseWait" style="position: absolute; display:none;">Loading map...</div>
					</td>					
				</tr>
				
				<tr>
					<td>
						<table id="im_subtoolbar" class="padded_content">
							<tr>
								<td style="padding:2px"><a onClick="im_addLayer();"><img src="/intermap/images/im_addLayer.png" title="Add a layer to this map"/></a></td>
<!--								<td style="padding:2px">Mail map</td>-->
								<td style="padding:2px" onClick="im_sendMail();" ><a><img src="/intermap/images/im_mail.png" title="Send this map via e-mail"/></a></td>
<!--								<td style="padding:2px" onClick="javascript:print();" ><a><img src="/intermap/images/print.png" title="Print"/></a></td>-->
<!--								<td style="padding:2px">Print map</td>-->
								<td style="padding:2px" onClick="im_createPDF();" ><a><img src="/intermap/images/acroread.png" title="Export this map as a PDF"/></a></td>
<!--								<td style="padding:2px">Export PDF</td>-->
								<td style="padding:2px" onClick="im_createPic();" ><a><img src="/intermap/images/im_exportPic.png" title="Export this map as an image"/></a></td>
<!--								<td style="padding:2px">Export TIFF</td>-->
							</tr>
						</table>
						
					</td>					
				</tr>	
				
				<tr>
					<td>
						<div id="im_whiteboard" style="position:relative;">
							
						</div>
					</td>
				</tr>
			</table>
			
				
			<div id="im_inspector" style="display:none;">
				<div id="im_transparencySlider" style="display:none;">
					<div id="im_transparencyHandle" style="display:none;"/> 
				</div>
				<div id="im_transparencyValue" style="display:none;"/>
				<div id="im_legendButton" style="display:none;"/>
			</div>
								
			<div id="im_addLayers" />
				
			<div id="im_debug" />
			<div id="im_geonetRecords" style="display:none" /> <!--  DUMMY -->
			
		</div> <!-- END OF IM CONTAINER -->
			</html>
		</root>
<!--			</body>
		</html>-->
	</xsl:template>
	
	



	<xsl:template match="/root/response/layersRoot">
		
		<div id="im_layersHeader">
			<h3>
				<xsl:value-of select="/root/gui/strings/layers"/>
			</h3>
		</div>
		
		<!-- Layers -->
		<div id="im_layersDiv">
			<ul id="im_layerList" />
		</div>
		
		<table id="im_refresh">
			<tr>
				<td>
					<button id="im_refreshButton" class="im_disabled">
						<xsl:value-of select="/root/gui/strings/refresh" />
					</button>
				</td>
			</tr>
		</table>

		
		<!-- layers toolbar -->
		<div id="im_layersToolbar">
		</div>
	</xsl:template>

</xsl:stylesheet>
