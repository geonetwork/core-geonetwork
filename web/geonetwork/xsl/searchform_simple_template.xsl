<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xalan="http://xml.apache.org/xalan" xmlns:geonet="http://www.fao.org/geonetwork"
	exclude-result-prefixes="xsl xalan geonet">


	<xsl:template name="geofields">
		<table class="geosearchfields" width="211px">
			<tr>
				<td rowspan="6" style="vertical-align: bottom;">
					<img width="9px" height="246px" src="/geonetwork/images/arrow_down_simple.gif"/>
				</td>
				<td colspan="2"/>
			</tr>

			<!-- Any (free text) -->
			<tr>
				<td>
					<xsl:value-of select="/root/gui/strings/searchText"/>
				</td>
				<td class="padded" align="right">
					<input name="any" id="any" class="content"  size="17"
						value="{/root/gui/searchDefaults/any}"/>
					<br/>
				</td>
			</tr>
			
			<tr>
				<td colspan="2">
					<p><xsl:value-of select="/root/gui/strings/location"/></p>
				</td>
			</tr>

			<xsl:comment>MINIMAP</xsl:comment>					
			<tr style="margin-bottom:5px;">
				<td colspan="2" width="202px" align="center" >
					<table id="minimap_root">
						<xsl:comment>MINIMAP TOOLBAR</xsl:comment>						
						<tr  id="im_mm_toolbar"> <!-- This element's class is set at runtime -->
							<td width="122px" />
							<td class="im_mmtool" id="im_mmtool_fullextent"  	onClick="javascript:im_mm_fullExtent()"><img src="/intermap/images/im_zoomfull16x16.png" title="Zoom to full map extent"/></td>
							<td class="im_mmtool" id="im_mmtool_zoomin"	onClick="javascript:im_mm_setTool('zoomin');" ><img src="/intermap/images/zoomin.png" title="Zoom in"/></td>
							<td class="im_mmtool" id="im_mmtool_zoomout"   	onClick="javascript:im_mm_setTool('zoomout');"><img  src="/intermap/images/zoomout.png" title="Zoom out"/></td>
							<td class="im_mmtool" id="im_mmtool_pan"		onClick="javascript:im_mm_setTool('pan');"><img src="/intermap/images/im_pan16x16.png" title="Pan"/></td>
<!--							<td class="im_mmtool" id="im_mmtool_zoomsel"	onClick="javascript:im_mm_zoomToAoi()"><img src="/intermap/images/zoomsel.png" title="Zoom to selected layer extent"/></td> -->
							<td class="im_mmtool" id="im_mmtool_aoi"		onClick="javascript:im_mm_setTool('aoi')"><img src="/intermap/images/im_aoi16x16.png" title="Select an Area Of Interest"/></td> 
						</tr>
						<tr height="102px" style="position:relative;">
							<td id="im_mm_mapContainer" style="position:relative;width:202px;height:102px;" colspan="6"  >
								<div id="im_mm_map" style="position: absolute;width:202px;height:102px;overflow: hidden;">
									<img id="im_mm_image" width="200px" height="100px" style="left:1px;"  src="/intermap/images/map0.jpg"/>
									<div id="im_mm_aoibox" style="left:0px;top:0px;width:200px;height:100px;position:absolute;border: 1px dashed #f00;visibility: visible;overflow: hidden;"/>
								</div>
								<div id="im_mm_wait" style="position: relative; z-index:999; left:59px; top:45px;">
									<img id="im_mm_waitimage" style="position: absolute; z-index:1000;" src="/intermap/images/waiting.gif" />
								</div>
							</td>
						</tr>
						<tr>
							<td align="right" colspan="7">
								<div id="openIMBtn" class="IMBtn" title="View Map" style="cursor:wait;">Open Map Viewer</div>
							</td>
						</tr>
						<tr>
							<td align="right" colspan="7" >
								<div id="loadIMBtn" class="IMBtn" style="display:none; cursor:default;">Loading Map Viewer...</div>
							</td>
						</tr>
						<tr>
							<td align="right" colspan="7">
								<div id="closeIMBtn" class="IMBtn" title="Close Map Viewer" style="display:none" onclick="closeIntermap();">Close Map Viewer</div>
							</td>
						</tr>
					</table>
					<br/>
				</td>
			</tr>

			<xsl:comment>COORDS</xsl:comment>
			<input type="hidden" class="content" id="northBL" name="northBL"  size="7"
				value="{/root/gui/searchDefaults/northBL}"/>
			<input type="hidden" class="content" id="westBL" name="westBL" size="7"
				value="{/root/gui/searchDefaults/westBL}"/>
			<input type="hidden" class="content" id="eastBL" name="eastBL" size="7"
				value="{/root/gui/searchDefaults/eastBL}"/>
			<input type="hidden" class="content" id="southBL" name="southBL" size="7"
				value="{/root/gui/searchDefaults/southBL}"/>
			<input type="hidden" class="content" id="relation" name="relation" size="7"
				value="overlaps"/>

			<!-- Area -->
			<tr>
				<td align="right" colspan="2">
					<!-- regions combobox -->
					<select class="content" name="region" id="region" onchange="javascript:doRegionSearch();">
							<option value="">
							<xsl:if test="/root/gui/searchDefaults/theme='_any_'">
								<xsl:attribute name="selected"/>
							</xsl:if>
							<xsl:value-of select="/root/gui/strings/any"/>
						</option>

						<xsl:for-each select="/root/gui/regions/record">
							<xsl:sort select="label/child::*[name() = $lang]" order="ascending"/>
							<option>
								<xsl:if test="id=/root/gui/searchDefaults/region">
									<xsl:attribute name="selected"/>
								</xsl:if>
								<xsl:attribute name="value">
									<xsl:value-of select="id"/>
								</xsl:attribute>
								<xsl:value-of select="label/child::*[name() = $lang]"/>
							</option>
						</xsl:for-each>
					</select>
				</td>
			</tr>


			<!-- other search options -->

			<!-- hits per page -->
<!--			<tr>
				<td class="padded">
					<xsl:value-of select="/root/gui/strings/hitsPerPage"/>
				</td>
				<td class="padded" align="right">
					<select class="content" name="hitsPerPage" onchange="profileSelected()">
						<xsl:for-each select="/root/gui/strings/hitsPerPageChoice">
							<option>
								<xsl:if
									test="string(@value)=string(/root/gui/searchDefaults/hitsPerPage)">
									<xsl:attribute name="selected"/>
								</xsl:if>
								<xsl:attribute name="value">
									<xsl:value-of select="@value"/>
								</xsl:attribute>
								<xsl:value-of select="."/>
							</option>
						</xsl:for-each>
					</select>
				</td>
			</tr>
-->
			<tr>
				<td colspan="2" style="align: center; padding-top: 7px;">
					<table class="advsearchfields" width="211px" border="0" cellspacing="0" cellpadding="0">
						<tr >
							<td style="background: url(/geonetwork/images/arrow-bg.gif) repeat-x;" height="29px" width="30%">
								
							</td>
<!--							<td width="36px" style="background: url(/geonetwork/images/arrow-right.gif) no-repeat;" > -->
							<td style="padding:0px; margin:0px;" width="36px">
								<img width="36px" style="padding:0px; margin:0px;"  src="/geonetwork/images/arrow-right.gif"/>
							</td>
							<td style="padding:0px; margin:0px;" width="13px">
								<img width="13px" style="padding:0px; margin:0px;"  src="/geonetwork/images/search-left.gif"/>
							</td>
							<td align="center" style="background: url(/geonetwork/images/search-bg.gif) repeat-x; width: auto; white-space: nowrap; padding-bottom: 8px; vertical-align: bottom; cursor:hand;  cursor:pointer;" onclick="runSimpleSearch();" >
								<font color="#FFFFFF"><strong><xsl:value-of select="/root/gui/strings/search"/></strong></font>
							</td>
							<td style="padding:0px; margin:0px;" width="12px">
								<img width="12px" style="padding:0px; margin:0px;"  src="/geonetwork/images/search-right.gif"/>
							</td>
						</tr>
					</table>
				</td>
			</tr>
			
			<script language="JavaScript" type="text/javascript">
				//Event.observe('searchBtn', 	'click', 		runSimpleSearch);
				Event.observe('any', 		'keypress',	gn_anyKeyObserver);
				//			Event.observe('openIMBtn', 'click',  function(){openIntermap()} ); // issued only when IM is loaded
				Event.observe('closeIMBtn', 	'click',  		closeIntermap	 );
			</script>

			<tr>			
				<td colspan="3">
					<table width="100%">
						<tr>
							<td style="padding-left:10px;padding-top:5px;" align="right"><a onClick="showAdvancedSearch()" style="cursor:pointer;">Advanced</a></td>							
						</tr>
					</table>
				</td>
			</tr>
		</table>
	</xsl:template>



</xsl:stylesheet>
