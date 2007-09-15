<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >
	<xsl:output method="html"/>
	
	<xsl:template match="/">

<!--		<root>
			<info>
				<xsl:copy-of  select="//minimap" />	
				<xsl:copy-of  select="//bigmap" />	
			</info>
			<html>
-->

		<div id="intermap_root"> <!-- class will be set to current tool -->
						
			<table class="padded_content">
				<tr height="30px">
					<!-- TOOLBAR -->
					<td>
						<table id="im_toolbar" class="padded_content">
							<tr id="im_toolSelector"> <!--class="im_tool"-->
								<td class="im_tool" id="im_tool_fullextent"    	onClick="javascript:im_bm_fullExtent()"><img src="{/root/gui/url}/images/zoomfull.png" title="Zoom to full map extent"/></td>
								<td class="im_tool" id="im_tool_zoomin"	onClick="javascript:setTool('zoomin');" ><img src="{/root/gui/url}/images/zoomin.png" title="Zoom in"/></td>
								<td class="im_tool" id="im_tool_zoomout" 	onClick="javascript:setTool('zoomout');"><img  src="{/root/gui/url}/images/zoomout.png" title="Zoom out"/></td>
								<td class="im_tool" id="im_tool_pan"		onClick="javascript:setTool('pan');"><img src="{/root/gui/url}/images/pan.png" title="Pan"/></td>															
<!--								<td class="im_tool" id="im_tool_zoomsel"	onClick="javascript:imc_zoomToLayer(activeLayerId)"><img src="{/root/gui/url}/images/zoomsel.png" title="Zoom to selected layer extent"/></td> -->
<!--								<td class="im_tool" id="im_tool_aoi"		onClick="javascript:setTool('aoi')"><img src="{/root/gui/url}/images/im_aoi16x16.png" title="Select an Area Of Interest"/></td> --> 
<!--								<td class="im_tool" id="im_tool_identify"	onClick="javascript:setTool('identify');">Identify</td> -->
								<td width="100%" style="border-top:0px;"/> <!-- spacer -->
								<td class="im_tool" id="im_tool_refresh" onClick="javascript:refreshNeeded()"><img src="{/root/gui/url}/images/reload.png" title="Refresh map"/></td>
<!--								<td class="im_tool"  				onClick="javascript:im_bm_toggleImageSize()">+/- map</td>-->
								<td class="im_tool" id="im_tool_reset" onClick="javascript:im_reset();"><img src="{/root/gui/url}/images/reset.png" title="Reset map"/></td>
							</tr>							
						</table>						
					</td>
					
					<!--  LAYERS -->
					<!-- This is only a placeholder structure: layers will be inserted dinamically. -->
					<td rowspan="3">
						<div id="im_layers" >
							
							<div id="im_layersHeader">
								<h3>
									<xsl:value-of select="/root/gui/strings/layers"/>
								</h3>
							</div>
							
							<!-- Layers -->
							<div id="im_layersDiv">
								<ul id="im_layerList" />
							</div>
							
<!--							<table id="im_refresh">
								<tr>
									<td>
										<button id="im_refreshButton" class="im_disabled">
											<xsl:value-of select="/root/gui/strings/refresh" />
										</button>
									</td>
								</tr>
							</table>
-->														
							<!-- layers toolbar -->
							<!--<div id="im_layersToolbar"/>-->
														
						</div>
					</td>					
				</tr>
				
				<tr>
					<td id="im_mapContainer" style="position:relative;width:370px;height:278px;">
						<div id="im_map" style="position: absolute;">
							<img id="im_mapImg" src="{/root/gui/url}/images/default_bigmap.gif" />
							<!--<img id="im_mapImg" src="{//mapRoot/response/url}" />-->
							<img id="im_resize"
								src="{/root/gui/url}/images/transpcorner.png" 
								style="z-index:1000; position:absolute; bottom:0px; right:0px; cursor:se-resize" 
								alt="resize"/>
							<div id="im_pleaseWait" style="position: absolute; display:none; ">Loading map...</div>							
						</div>		
						<div id="im_scale" style="position: absolute;" >
							1:?
<!--							<xsl:variable name="scale">
								<xsl:value-of select="round(//mapRoot/response/services/distScale)" />
							</xsl:variable>
							1:<xsl:value-of select="format-number($scale, '###,###')" />
-->						</div>
					</td>					
				</tr>
				
				<tr height="20px">
					<td>
						<table id="im_subtoolbar" class="padded_content">
							<tr>
								<td style="padding:2px"><a onClick="im_addLayer();"><img src="{/root/gui/url}/images/im_addLayer.png" title="Add a layer to this map"/></a></td>
								<!-- <td style="padding:2px" onClick="im_sendMail();" ><a><img src="{/root/gui/url}/images/im_mail.png" title="Send this map via e-mail"/></a></td> -->
								<td style="padding:2px" onClick="im_openPDFform();" ><a><img src="{/root/gui/url}/images/acroread.png" title="Export this map as a PDF"/></a></td>
								<!-- <td style="padding:2px" onClick="im_openPictureForm();" ><a><img src="{/root/gui/url}/images/im_exportPic.png" title="Export this map as an image"/></a></td> -->
<!--								<td style="padding:2px" onClick="im_openWMCform();" ><a><img src="{/root/gui/url}/images/im_exportPic.png" title="View context"/></a></td>-->
								<td width="100%" style="border-top:0px;"/> <!-- spacer -->								
								<td class="im_tool" id="im_tool_scale">
									<select name="im_setscale" id="im_setscale" onchange="javascript:im_bm_setScale();">
										<option id="im_currentscale" value="">1:?</option>										
										<option value="50000000">1:50.000.000</option>
										<option value="10000000">1:10.000.000</option>
										<option value="5000000">1:5.000.000</option>
										<option value="1000000">1:1.000.000</option>
										<option value="500000">1:500.000</option>
										<option value="100000">1:100.000</option>
										<option value="50000">1:50.000</option>
										<option value="10000">1:10.000</option>
										<option value="5000">1:5.000</option>
										<option value="1000">1:1.000</option>										
									</select>
								</td>
								
							</tr>
						</table>						
					</td>					
				</tr>	
				
				<tr>
					<td colspan="2">
						<div id="im_whiteboard" style="position:relative;"/>
					</td>
				</tr>
			</table>
							
<!--			<div id="im_inspector" style="display:none;">
				<div id="im_transparencySlider" style="display:none;">
					<div id="im_transparencyHandle" style="display:none;"/> 
				</div>
				<div id="im_transparencyValue" style="display:none;"/>
				<div id="im_legendButton" style="display:none;"/>
			</div>
								
			<div id="im_addLayers" />
				
			<div id="im_debug" />
			<div id="im_geonetRecords" style="display:none" />  
-->
			
		</div> <!-- END OF IM CONTAINER -->
<!--			</html>
		</root>-->
<!--			</body>
		</html>-->
	</xsl:template>
	
	
</xsl:stylesheet>
