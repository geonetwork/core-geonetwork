<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xalan="http://xml.apache.org/xalan" xmlns:geonet="http://www.fao.org/geonetwork"
	exclude-result-prefixes="xsl xalan geonet">

	<xsl:variable name="lang" select="/root/gui/language"/>

	<xsl:template match="/">
	<form name="advsearch" id="advsearch" onsubmit="javascript:runAdvancedSearch();">
		<table class="advsearchfields">
			<xsl:comment>ADVANCED SEARCH</xsl:comment>					
			<tr class="advsearchfields">
				<xsl:comment>ADV SEARCH: WHAT?</xsl:comment>
				<td width="16px" height="236px" style="padding-right: 3px; padding-top:5px;">
					<img width="13px" height="233px" src="/geonetwork/images/arrow-down.gif"/>
				</td>
				<td style="margin-left:5px;">
					<xsl:call-template name="adv_what"></xsl:call-template>
				</td>
				<xsl:comment>ADV SEARCH: WHERE?</xsl:comment>					
				<td width="16px" height="236px" style="padding-right: 3px; padding-top:5px;">
					<img width="13px" height="233px" src="/geonetwork/images/arrow-down.gif"/>
				</td>
				<td>
					<xsl:call-template name="adv_where"></xsl:call-template>
				</td>
				<xsl:comment>ADV SEARCH: WHEN?</xsl:comment>					
				<td width="16px" height="236px" style="padding-right: 3px; padding-top:5px;">
					<img width="13px" height="233px" src="/geonetwork/images/arrow-down.gif"/>
				</td>
				<td>
					<xsl:call-template name="adv_when"></xsl:call-template>
				</td>
			</tr>
			
			<tr>
				<td colspan="6">
					<table class="advsearchfields" width="100%" border="0" cellspacing="0" cellpadding="0">
						<tr >
							<td width="5px"></td>
							<td style="background: url(/geonetwork/images/arrow-bg.gif) repeat-x;" height="29px" width="80%">
								
							</td>
							<td width="36px" style="background: url(/geonetwork/images/arrow-right.gif) no-repeat;" > </td>
							<td width="13px" style="background: url(/geonetwork/images/search-left.gif) no-repeat;" > </td>
							<td align="center" width="40px" style="background: url(/geonetwork/images/search-bg.gif) repeat-x; width: auto; white-space: nowrap; padding-bottom: 8px; vertical-align: bottom; cursor:hand;  cursor:pointer;" onclick="runAdvancedSearch()" >
								<font color="#FFFFFF"><strong><xsl:value-of select="/root/gui/strings/search"/></strong></font>
							</td>
							<td width="12px" style="background: url(/geonetwork/images/search-right.gif) no-repeat;" > </td>
						</tr>
					</table>
				</td>
			</tr>
			<tr>
				<td colspan="6" align="right">
					<a onClick="resetAdvancedSearch();" style="cursor:pointer; padding-right:10px; padding-left:10px;" title="{/root/gui/strings/resetSearch}" alt="{/root/gui/strings/resetSearch}"><xsl:value-of select="/root/gui/strings/reset"/></a>
					<a onClick="showSimpleSearch();" style="cursor:pointer; padding-right:10px; padding-left:10px;"><xsl:value-of select="/root/gui/strings/hideAdvancedOptions"/></a>		
					
				</td>
			</tr>
		</table>
	</form>					
</xsl:template>
	
	<!-- ============================================================ 
		WHAT
	======================================= ===================== -->
	
<xsl:template name="adv_what">
	<h1><xsl:value-of select="/root/gui/strings/what"/></h1>
	<table heigth="100%	">
		<tr>
			<th class="padded">
				<xsl:value-of select="/root/gui/strings/searchText"/>
			</th>
			<td style="padding-bottom:10px;">
				<input name="any" id="any" class="content"  size="20" value="{/root/gui/searchDefaults/any}"/>
			</td>
		</tr>
		<tr>
			<th class="padded">
				<xsl:value-of select="/root/gui/strings/rtitle"/>
			</th>
			<td style="padding-bottom:5px;">
				<input name="title" id="title" class="content"  size="20" value="{/root/gui/searchDefaults/title}"/>
			</td>
		</tr>
		<tr>
			<th class="padded">
				<xsl:value-of select="/root/gui/strings/abstract"/>
			</th>
			<td style="padding-bottom:5px;">
				<input name="abstract" id="abstract" class="content"  size="20" value="{/root/gui/searchDefaults/abstract}"/>
			</td>
		</tr>
		<tr>
			<th class="padded">
				<xsl:value-of select="/root/gui/strings/keywords"/>
			</th>
			<td style="padding-bottom:5px;">
				<input id="themekey" name="themekey" class="content" size="20" value="{/root/gui/searchDefaults/themekey}"/>
				<a href="#">
					<img src="{/root/gui/url}/images/gdict.png" align="absmiddle"
						onclick="keywordSelector();"/>
				</a>
				
				<div id="keywordSelectorFrame" class="keywordSelectorFrame" style="display:none;z-index:1000;">
					<div id="keywordSelector" class="keywordSelector"/>
				</div>
				
				<div id="keywordList" class="keywordList"/>
				
			</td>
		</tr>
		<tr >
			<td colspan="2" style="padding-top:30px; white-space: nowrap;">
				<fieldset style="margin: 10px 5px 10px;" title="{/root/gui/strings/fuzzySearch}">
					<legend><xsl:value-of select="/root/gui/strings/fuzzy"/></legend>
					<xsl:value-of select="/root/gui/strings/fuzzyPrecise"/> <input type="radio" id="similarity" name="similarity" value="1" >
						<xsl:if test="/root/gui/searchDefaults/similarity='1'">
								<xsl:attribute name="checked"/>
						</xsl:if>
					</input>
					<input type="radio" id="similarity" name="similarity" value=".8">
						<xsl:if test="/root/gui/searchDefaults/similarity='.8'">
								<xsl:attribute name="checked"/>
						</xsl:if>
					</input>
					<input type="radio" id="similarity" name="similarity" value=".6" >
						<xsl:if test="/root/gui/searchDefaults/similarity='.6'">
								<xsl:attribute name="checked"/>
						</xsl:if>
					</input>

					<input type="radio" id="similarity" name="similarity" value=".4" >
						<xsl:if test="/root/gui/searchDefaults/similarity='.4'">
								<xsl:attribute name="checked"/>
						</xsl:if>
					</input> 
					<input type="radio" id="similarity" name="similarity" value=".2" >
						<xsl:if test="/root/gui/searchDefaults/similarity='.2'">
								<xsl:attribute name="checked"/>
						</xsl:if>
					</input><xsl:value-of select="/root/gui/strings/fuzzyImprecise"/>
				</fieldset>		
			</td>	
		</tr>
		
		
				
	</table>

</xsl:template>
	
	
	<!-- ============================================================ 
		WHERE
	======================================= ===================== -->
	
<xsl:template name="adv_where">

	<h1><xsl:value-of select="/root/gui/strings/where"/></h1>
	
	<xsl:comment>MINIMAP</xsl:comment>
	
	<table align="center"> <!--  minimap & coords -->
		<tr>
			<td align="center" colspan="3">
				
				<table id="minimap_root" width="340px">
					<tr>
						<td colspan="4" align="center" style="padding: 3px;">
							<xsl:value-of select="/root/gui/strings/latMin"/> <input type="text" class="content" id="northBL" name="northBL"  size="5"
								value="{/root/gui/searchDefaults/northBL}" onChange="javascript:AoIrefresh();"/>
						</td>
					</tr>
					<tr height="102px" style="position:relative;">
						<td width="52px" style="padding-top: 25px; align: center;">
							<small><xsl:value-of select="/root/gui/strings/longMin"/></small>
							<br />
							<input type="text" class="content" id="westBL" name="westBL" size="5"
								value="{/root/gui/searchDefaults/westBL}" onChange="javascript:AoIrefresh();"/>
						</td>
						<td width="16px">
							<table width="16px">
								<xsl:comment>MINIMAP TOOLBAR</xsl:comment>						
								<tr  id="im_mm_toolbar"> <!-- This element's class is set at runtime -->
									<td class="im_mmtool" id="im_mmtool_fullextent"  	onClick="javascript:im_mm_fullExtent()"><img src="/intermap/images/im_zoomfull16x16.png" title="Zoom to full map extent"/></td>
								</tr>
								<tr  id="im_mm_toolbar"> <!-- This element's class is set at runtime -->
									<td class="im_mmtool" id="im_mmtool_zoomin"	onClick="javascript:im_mm_setTool('zoomin');" ><img src="/intermap/images/zoomin.png" title="Zoom in"/></td>
								</tr>
								<tr  id="im_mm_toolbar"> <!-- This element's class is set at runtime -->
									<td class="im_mmtool" id="im_mmtool_zoomout"   	onClick="javascript:im_mm_setTool('zoomout');"><img  src="/intermap/images/zoomout.png" title="Zoom out"/></td>
								</tr>
								<tr  id="im_mm_toolbar"> <!-- This element's class is set at runtime -->
									<td class="im_mmtool" id="im_mmtool_pan"		onClick="javascript:im_mm_setTool('pan');"><img src="/intermap/images/im_pan16x16.png" title="Pan"/></td>
								</tr>
								<tr  id="im_mm_toolbar"> <!-- This element's class is set at runtime -->
									<!--							<td class="im_mmtool" id="im_mmtool_zoomsel"	onClick="javascript:im_mm_zoomToAoi()"><img src="/intermap/images/zoomsel.png" title="Zoom to selected layer extent"/></td> -->
									<td class="im_mmtool" id="im_mmtool_aoi"		onClick="javascript:im_mm_setTool('aoi')"><img src="/intermap/images/im_aoi16x16.png" title="Select an Area Of Interest"/></td> 
								</tr>
							</table>
						</td>
						<td id="im_mm_mapContainer" style="position:relative;width:202px;height:102px;" >
							<div id="im_mm_map" style="position: absolute;width:202px;height:102px;overflow: hidden;">
								<img id="im_mm_image" width="200px" height="100px" src="/intermap/images/map0.jpg"/>
								<div id="im_mm_aoibox" style="left:0px;top:0px;width:200px;height:100px;position:absolute;border: 1px dashed #f00;visibility: visible;overflow: hidden;"/>
							</div>
							<div id="im_mm_wait" style="position: relative; z-index:999; left:59px; top:45px;">
								<img id="im_mm_waitimage" style="position: absolute; z-index:1000;" src="/intermap/images/waiting.gif" />
							</div>
						</td>
						<td width="52px" style="padding-top: 25px; align: center;">
							<small><xsl:value-of select="/root/gui/strings/longMax"/></small>
							<br />
							<input type="text" class="content" id="eastBL" name="eastBL" size="5"
								value="{/root/gui/searchDefaults/eastBL}" onChange="javascript:AoIrefresh();"/>
						</td>
					</tr>
					<tr>
						<td />
						<td colspan="2" align="center" style="padding: 3px;">
							<small><xsl:value-of select="/root/gui/strings/latMax"/></small> <input type="text" class="content" id="southBL" name="southBL" size="5"
								value="{/root/gui/searchDefaults/southBL}" onChange="javascript:AoIrefresh();"/>
						</td>
						<td>
							<img src="/intermap/images/update.png" id="updateBB" name="updateBB" style="visibility:hidden;border:2px solid red;" title="Update Area Of Interest" onClick="javascript:updateAoIFromForm();"/>
						</td>
					</tr>
				</table>
			</td>
		</tr>
		<tr>
			<th class="padded" colspan="2" nowrap="nowrap">
				<xsl:value-of select="/root/gui/strings/type"/>
			</th>
			<td class="padded" align="right">
				<select class="content" name="relation" id="relation">
					<xsl:for-each select="/root/gui/strings/boundingRelation">
						<option value="{@value}">
							<xsl:if
								test="@value=/root/gui/searchDefaults/relation">
								<xsl:attribute name="selected"/>
							</xsl:if>
							<xsl:value-of select="."/>
						</option>
					</xsl:for-each>
				</select>
			</td>
		</tr>
		<tr>
			<!-- regions combobox -->	
			<th class="padded">
				<xsl:value-of select="/root/gui/strings/region"/>
			</th>
			<td class="padded" colspan="2" align="right">
				<select class="content" name="region" id="region" onchange="javascript:doRegionSearch();">
					<option value="">
						<xsl:if test="/root/gui/searchDefaults/theme='_any_'">
							<xsl:attribute name="selected"/>
						</xsl:if>
						<xsl:value-of select="/root/gui/strings/any"/>
					</option>
					
					<xsl:for-each select="/root/gui/regions/record">
						<xsl:sort select="label/child::*[name() = $lang]" order="ascending"/>
						<option value="{id}">
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
	</table>
	
</xsl:template>

	<!-- ============================================================ 
		WHEN
	======================================= ===================== -->

<xsl:template name="adv_when">

	<h1><xsl:value-of select="/root/gui/strings/when"/></h1>


	<table border="0" cellpadding="0" cellspacing="0">
		<tr>
			<td colspan="2">
				<input onclick="setDates(0);" value="" name="radfrom" id="radfrom0" type="radio">
					<xsl:if test="string(/root/gui/searchDefaults/datefrom)='' and string(/root/gui/searchDefaults/dateto)=''"> 
						<xsl:attribute name="checked" />
 					</xsl:if>
					<xsl:value-of select="/root/gui/strings/anytime"/>
				</input>
			</td>
		</tr>
		
		<tr>		
			<td align="left" nowrap="nowrap">
				<input value="" name="radfrom" id="radfrom1" type="radio" disabled="disabled">
					<xsl:if test="string(/root/gui/searchDefaults/datefrom)!='' and string(/root/gui/searchDefaults/dateto)!=''">
						<xsl:attribute name="checked" />
					</xsl:if>
					<xsl:value-of select="/root/gui/strings/from"/>
					<input style="width: 90px;" readonly="1" id="datefrom" value="{/root/gui/searchDefaults/datefrom}" name="datefrom" class="inpBnds" type="text" 
						onchange="$('radfrom1').checked=true;$('radfrom1').disabled='';"/>
					<img title="FROM date selector" style="cursor: pointer; margin-bottom: 6px; margin-right:10px;" id="from_trigger_c" 
						src="/geonetwork/scripts/calendar/img.gif" alt="select FROM date" align="middle" hspace="1"/>
							
					<xsl:value-of select="/root/gui/strings/to"/>
					<input  style="width: 90px;" readonly="1" id="dateto" value="{/root/gui/searchDefaults/dateto}" name="dateto" class="inpBnds" type="text"
						onchange="$('radfrom1').checked=true;$('radfrom1').disabled='';" />
					<img title="TO date selector" style="cursor: pointer; margin-bottom: 6px;" id="to_trigger_c" 
						src="/geonetwork/scripts/calendar/img.gif" alt="select TO date" align="middle" hspace="1"/>								
				</input>
<!--				<div onclick="JavaScript:$('datefrom').value ='';$('dateto').value ='';" style="cursor: pointer;"><xsl:value-of select="/root/gui/strings/clear"/></div> -->
				<img title="{/root/gui/strings/clear}" style="cursor: pointer; margin-bottom: 6px;" id="clearDates" 
					src="/geonetwork/images/clear_left.png" alt="{/root/gui/strings/clear}" align="middle" 
					hspace="1" onclick="JavaScript:$('datefrom').value ='';$('dateto').value ='';$('radfrom0').checked=true;$('radfrom1').disabled='disabled';"/>
			</td>
		</tr>
	</table>

	<fieldset style="margin: 10px 5px 10px;">
		<legend><xsl:value-of select="/root/gui/strings/restrictTo"/></legend>

	<table style="margin-left:10px;">

		<!-- Source -->
		<tr>
			<th class="padded">
				<xsl:value-of select="/root/gui/strings/porCatInfoTab"/>
			</th>
			<td class="padded">
				<select class="content" name="siteId" id="siteId">
					<option value="">
						<xsl:if test="/root/gui/searchDefaults/siteId=''">
							<xsl:attribute name="selected"/>
						</xsl:if>
						<xsl:value-of select="/root/gui/strings/any"/>
					</option>
					<xsl:for-each select="/root/gui/sources/record">
						<!--
							<xsl:sort order="ascending" select="name"/>
						-->
						<xsl:variable name="source" select="siteid/text()"/>
						<xsl:variable name="sourceName" select="name/text()"/>
						<option value="{$source}">
							<xsl:if test="$source=/root/gui/searchDefaults/siteId">
								<xsl:attribute name="selected"/>
							</xsl:if>
							<xsl:value-of select="$sourceName"/>
						</option>
					</xsl:for-each>
				</select>
			</td>
		</tr>

		<!-- Group -->		
		<xsl:if	test="string(/root/gui/session/userId)!=''">
		<tr>		
			<th class="padded">
				<xsl:value-of select="/root/gui/strings/group"/>
			</th>
			<td class="padded">
				<select class="content" name="group" id="group">
					<option value="">
						<xsl:if test="/root/gui/searchDefaults/group=''">
							<xsl:attribute name="selected"/>
						</xsl:if>
						<xsl:value-of select="/root/gui/strings/any"/>
					</option>
					<xsl:for-each select="/root/gui/groups/record">
						<xsl:sort order="ascending" select="name"/>
						<option value="{id}">
							<xsl:if test="id=/root/gui/searchDefaults/group">
								<xsl:attribute name="selected"/>
							</xsl:if>
							<xsl:value-of select="name"/>
						</option>
					</xsl:for-each>
				</select>
			</td>		
		</tr>
		</xsl:if>
		
		<!-- Template -->
		<xsl:if test="string(/root/gui/session/userId)!='' and /root/gui/services/service[@name='metadata.edit']">
			<tr>
				<th class="padded">
					<xsl:value-of select="/root/gui/strings/kind"/>
				</th>
				<td>
					<select class="content" id="template" name="template" size="1">
						<option value="n">
							<xsl:if test="/root/gui/searchDefaults/template='n'">
								<xsl:attribute name="selected">true</xsl:attribute>
							</xsl:if>
							<xsl:value-of select="/root/gui/strings/metadata"/>
						</option>
						<option value="y">
							<xsl:if test="/root/gui/searchDefaults/template='y'">
								<xsl:attribute name="selected">true</xsl:attribute>
							</xsl:if>
							<xsl:value-of select="/root/gui/strings/template"/>
						</option>
						<!-- <option value="s">
							<xsl:if test="/root/gui/searchDefaults/template='s'">
								<xsl:attribute name="selected">true</xsl:attribute>
							</xsl:if>
							<xsl:value-of select="/root/gui/strings/subtemplate"/>
						</option> -->
					</select>
				</td>
			</tr>
		</xsl:if>
		
		<!-- Category -->
		
		<tr>
			<th class="padded">
				<xsl:value-of select="/root/gui/strings/category"/>
			</th>
			<td class="padded">
				<select class="content" name="category" id="category">
					<option value="">
						<xsl:if test="/root/gui/searchDefaults/category=''">
							<xsl:attribute name="selected"/>
						</xsl:if>
						<xsl:value-of select="/root/gui/strings/any"/>
					</option>
					
					<xsl:for-each select="/root/gui/categories/record">
						<xsl:sort select="label/child::*[name() = $lang]" order="ascending"/>
						
						<option value="{name}">
							<xsl:if test="name = /root/gui/searchDefaults/category">
								<xsl:attribute name="selected"/>
							</xsl:if>
							<xsl:value-of select="label/child::*[name() = $lang]"/>
						</option>
					</xsl:for-each>
				</select>
			</td>
		</tr>
		
		<!-- Map type -->

		<tr>
			<th class="padded">
				<xsl:value-of select="/root/gui/strings/mapType"/>
			</th>
			<td>
				<input name="digital" id="digital" type="checkbox" value="on">
					<xsl:if test="/root/gui/searchDefaults/digital='on'">
						<xsl:attribute name="checked"/>
					</xsl:if>
					<xsl:value-of select="/root/gui/strings/digital"/>
				</input>
				&#xA0;&#xA0; <input name="paper" id="paper" type="checkbox" value="on">
					<xsl:if test="/root/gui/searchDefaults/paper='on'">
						<xsl:attribute name="checked"/>
					</xsl:if>
					<xsl:value-of select="/root/gui/strings/paper"/>
				</input>
			</td>
		</tr>
		
		<!-- hits per page -->
		<tr>
			<th class="padded">
				<xsl:value-of select="/root/gui/strings/hitsPerPage"/>
			</th>
			<td class="padded">
				<select class="content" id="hitsPerPage" name="hitsPerPage" onchange="profileSelected()">
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
					
	</table>
	</fieldset>

</xsl:template>
	

</xsl:stylesheet>
