<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:exslt="http://exslt.org/common" xmlns:geonet="http://www.fao.org/geonetwork"
	exclude-result-prefixes="xsl exslt geonet">

	<xsl:include href="main.xsl"/>
	<xsl:include href="metadata.xsl"/>
	<xsl:include href="searchform_simple_template.xsl"/>
	<xsl:include href="searchform_advanced.xsl"/>
	
	<xsl:template mode="css" match="/">
		<!--  FIXME : hard coded intermap link.  -->
		<!--link rel="stylesheet" type="text/css" href="/intermap/intermap-embedded.css?" /-->
		<link rel="stylesheet" type="text/css" href="{/root/gui/url}/scripts/calendar/calendar-blue2.css"></link>
		<xsl:call-template name="geoCssHeader"/>
		<xsl:call-template name="ext-ux-css"/>
        <link rel="stylesheet" type="text/css" href="{/root/gui/url}/geonetwork_map.css" />
	</xsl:template>
	
	<!--
	additional scripts
	-->
	<xsl:template mode="script" match="/">
	
        <!-- To avoid an interaction with prototype and ExtJs.Tooltip, should be loadded before ExtJs -->
        <xsl:choose>
            <xsl:when test="/root/request/debug">
                <script type="text/javascript" src="{/root/gui/url}/scripts/prototype.js"></script>
            </xsl:when>
            <xsl:otherwise>
              <script type="text/javascript" src="{/root/gui/url}/scripts/lib/gn.libs.js"></script>      
            </xsl:otherwise>
        </xsl:choose>
    
		<xsl:call-template name="geoHeader"/>
		
		<!-- Required by keyword selection panel -->
		<xsl:if test="/root/gui/config/search/keyword-selection-panel">
			<xsl:call-template name="ext-ux"/>
		</xsl:if>
		
         <xsl:choose>
            <xsl:when test="/root/request/debug">         	
                <script type="text/javascript" src="{/root/gui/url}/scripts/geonetwork.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/scriptaculous/scriptaculous.js?load=slider,effects,controls"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/modalbox.js"></script>

                <script type="text/javascript" src="{/root/gui/url}/scripts/gn_search.js"></script>
                
                <!--link rel="stylesheet" type="text/css" href="{/root/gui/url}/scripts/ext/resources/css/ext-all.css" />
                <link rel="stylesheet" type="text/css" href="{/root/gui/url}/scripts/ext/resources/css/file-upload.css" />

                <link rel="stylesheet" type="text/css" href="{/root/gui/url}/scripts/openlayers/theme/default/style.css" />
                <link rel="stylesheet" type="text/css" href="{/root/gui/url}/geonetwork_map.css" /-->
         
                <script type="text/javascript" src="{/root/gui/url}/scripts/ext/adapter/ext/ext-base.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/ext/ext-all.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/ext/form/FileUploadField.js"></script>

                <script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/lib/OpenLayers.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/addins/LoadingPanel.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/addins/ScaleBar.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/geo/proj4js-compressed.js"></script>

                <script type="text/javascript" src="{/root/gui/url}/scripts/geoext/lib/GeoExt.js"></script>				
                <script type="text/javascript" src="{/root/gui/url}/scripts/mapfish/MapFish.js"></script>

                <script type="text/javascript" src="{/root/gui/url}/scripts/map/core/OGCUtil.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/core/CatalogueInterface.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/core/WMCManager.js"></script>

                <script type="text/javascript" src="{/root/gui/url}/scripts/map/Control/ExtentBox.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/Control/ZoomWheel.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/Control/WMSGetFeatureInfo.js"></script>

                <script type="text/javascript" src="{/root/gui/url}/scripts/map/Format/WMSCapabilities.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/Format/WMSCapabilities_1_1_1.js"></script>

                <script type="text/javascript" src="{/root/gui/url}/scripts/map/lang/en.js"></script>

                <script type="text/javascript" src="{/root/gui/url}/scripts/map/Ext.ux/form/DateTime.js"></script>

                <script type="text/javascript" src="{/root/gui/url}/scripts/map/widgets/tree/WMSListGenerator.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/widgets/tree/WMSTreeGenerator.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/widgets/wms/BrowserPanel.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/widgets/wms/LayerInfoPanel.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/widgets/wms/LayerStylesPanel.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/widgets/wms/PreviewPanel.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/widgets/wms/WMSLayerInfo.js"></script>

                <script type="text/javascript" src="{/root/gui/url}/scripts/map/widgets/FeatureInfoPanel.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/widgets/LegendPanel.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/widgets/OpacitySlider.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/widgets/PrintAction.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/widgets/ProjectionSelector.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/widgets/TimeSelector.js"></script>
                
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/windows/BaseWindow.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/windows/SingletonWindowManager.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/windows/AddWMS.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/windows/FeatureInfo.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/windows/Opacity.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/windows/LoadWmc.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/windows/WMSTime.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/windows/LayerStyles.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/windows/WmsLayerMetadata.js"></script>				

                <script type="text/javascript" src="{/root/gui/url}/scripts/ol_settings.js"></script>		
                <script type="text/javascript" src="{/root/gui/url}/scripts/ol_minimap.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/ol_map.js"></script>
                
                <script type="text/javascript" src="{/root/gui/url}/scripts/editor/tooltip.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/editor/tooltip-manager.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/editor/simpletooltip.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/editor/metadata-show.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/editor/metadata-editor.js"></script>
            </xsl:when>
            <xsl:otherwise>             
                <script type="text/javascript" src="{/root/gui/url}/scripts/lib/gn.libs.scriptaculous.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/lib/gn.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/lib/gn.search.js"></script>

                <!-- Editor JS is still required here at least for massive operation -->
        		<script type="text/javascript" src="{/root/gui/url}/scripts/lib/gn.editor.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/lib/gn.libs.map.js"></script>              
            </xsl:otherwise>
         </xsl:choose>
            
            
		<script type="text/javascript" src="{/root/gui/url}/scripts/calendar/calendar.js"></script>
		<script type="text/javascript" src="{/root/gui/url}/scripts/calendar/calendar-setup.js"></script>
		<script type="text/javascript" src="{/root/gui/url}/scripts/calendar/lang/calendar-{/root/gui/language}.js"></script>
		
		<script type="text/javascript" src="{/root/gui/url}/scripts/core/kernel/kernel.js"></script>
		
		<xsl:variable name="wmc"><xsl:copy-of select="/root/request/wmc"/></xsl:variable>
		<xsl:variable name="uuid"><xsl:copy-of select="/root/request/uuid"/></xsl:variable>
		<xsl:variable name="id"><xsl:copy-of select="/root/request/id"/></xsl:variable>
		<xsl:variable name="urlWMS"><xsl:copy-of select="/root/request/url"/></xsl:variable>
		<xsl:variable name="typeWMS"><xsl:copy-of select="/root/request/type"/></xsl:variable>
		
		<script type="text/javascript" language="JavaScript1.2">

			function init()
			{
				var currentSearch = get_cookie('search');
				if (currentSearch=='advanced') {
					showAdvancedSearch();
				} else {
					initSimpleSearch("<xsl:value-of select="$wmc"/>");
				}
				<!-- If a UUID is passed, it will be opened within the AJAX page -->
				var uuid="<xsl:value-of select="$uuid"/>";
				if (uuid!='') {
					gn_showSingleMetadataUUID(uuid);
				}

				var id="<xsl:value-of select="$id"/>";
				if (id!='') {
						gn_showSingleMetadata(id);
				}

				<!-- If a WMS server & layername(s) are passed, it will be opened in the map viewer
					the large map viewer will also be opened -->
				var urlWMS="<xsl:value-of select="$urlWMS"/>";
				var typeWMS="<xsl:value-of select="$typeWMS"/>";
				servicesWMS = new Array();
				<xsl:for-each select="/root/request/service">
					<xsl:text>servicesWMS.push("</xsl:text><xsl:value-of select="."/><xsl:text>");</xsl:text>
				</xsl:for-each>
				if (urlWMS!='') {
				if (servicesWMS.length!=null || servicesWMS.length>0) {
						if (typeWMS!='') {
							imc_addServices(urlWMS, servicesWMS, typeWMS, im_servicesAdded);
							openIntermap();
						}
					}
				}
			}
						
			var getIMServiceURL = function(service)
			{
				// FIXME: the "/intermap/" context should be parametrized
				return "/intermap/srv/"+Env.lang+"/"+service;
			};

			function checkSubmit()
			{
				if (document.search.remote.value == 'on')
				{
					if (isWhitespace(document.search.any.value) &amp;&amp;
						!(document.search.title    &amp;&amp; !isWhitespace(document.search.title.value)) &amp;&amp;
						!(document.search['abstract'] &amp;&amp; !isWhitespace(document.search['abstract'].value)) &amp;&amp;
						!(document.search.themekey &amp;&amp; !isWhitespace(document.search.themekey.value)))
					{
						alert("Please type some search criteria");	// TODO : i18n
						return false;
					}
					servers = 0;
					for (var i=0; i &lt; document.search.servers.length; i++)
						if (document.search.servers.options[i].selected) servers++;
					if (servers == 0)
					{
						alert("Please select a server");// TODO : i18n
						return false;
					}
				}
				return true;
			}
			
			function doSubmit()
			{
				if (checkSubmit())
					document.search.submit();
			}

			Ext.onReady(function(){
                $("loading").hide();

                Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
                
				GeoNetwork.mapViewer.init();
				var mapViewport =  GeoNetwork.mapViewer.getViewport();
				
				var categories = new Ext.Panel({
					region: 'south',
					layout:'fit',
					contentEl: 'categories_pnl',
					title:'Categories',
					bodyStyle: 'padding-left:15px',
					autoHeight: true,
					collapsible:true,
					border:false
				});
				
			    var recent = new Ext.Panel({
					region: 'south',
					layout:'fit',
					contentEl: 'recent_pnl',
					title:'Recent changes',
					bodyStyle: 'padding-left:15px',
					autoHeight: true,
					collapsible:true,
					border:false	
				});
				
			   var viewport = new Ext.Panel({
					region: 'center',
					layout:'border',
					border:false,
					autoScroll:true,
					items:[
						// North: header
						{
							region:'north',
							contentEl :'header',
							border:false
						},
						
						// Center: Content
						{
							region:'center',                
							layout:'border',
							border:false,
							layoutConfig:{
								animate:true
							}, 
							items:[
								{region:'center',   
								border:false,
								layout: 'border',
								items: 
									[{region:'west',
									xtype: 'panel',
									border:false,
									width: 370,
									minSize: 300,
									maxSize: 370,
									autoScroll: true,
									collapsible: true,
									collapseMode: "mini",
									split: 'true',
									useSplitTips:true,
									collapsibleSplitTip: 'Drag to rezise the search panel. Double click to show/hide it',
									bodyStyle: 'padding:15px',
									contentEl: 'search_pnl'
									},
									{region:'center', 
										id: 'main-viewport',
										border:false,
										layout: 'border',
										items: [
											{region:'north',
											id: 'north-map-panel',
											title: 'Map viewer',
											border:false,
											collapsible: true,
											collapsed: true,
											split: true,
											height: 450,
											minSize: 300,
											//maxSize: 500,
											layout: 'fit',
											listeners: {
												  collapse: collapseMap,
												  expand: expandMap
											   },
											items: [mapViewport]
											
											},
											{region:'center', 
											contentEl :'content',
											border:false,
											autoScroll: true
											}
										]
										
									}]
							}]
						}]});
						
						
						mainViewport = new Ext.Viewport({
							layout:'border',
							border:false,
							autoScroll: true,
							items:[viewport]
						});
						
						// Initialize minimaps 
						GeoNetwork.minimapSimpleSearch.init("ol_minimap1", "region_simple");
						GeoNetwork.minimapAdvancedSearch.init("ol_minimap2", "region");

                        GeoNetwork.minimapSimpleSearch.setSynchMinimap(GeoNetwork.minimapAdvancedSearch);
                        GeoNetwork.minimapAdvancedSearch.setSynchMinimap(GeoNetwork.minimapSimpleSearch);
                        GeoNetwork.CatalogueInterface.init(GeoNetwork.mapViewer.getMap());

                        
						showSimpleSearch();
			});
			
			function collapseMap(pnl) {
				Ext.getCmp('main-viewport').layout.north.getCollapsedEl().titleEl.dom.innerHTML = 'Show map';
			}
			
			function expandMap(pnl) {
				Ext.getCmp('main-viewport').layout.north.getCollapsedEl().titleEl.dom.innerHTML = 'Map viewer';
			}
		</script>
	</xsl:template>


	<xsl:variable name="lang" select="/root/gui/language"/>

	<xsl:template name="content">
		<!-- content -->
		<div id="content" >
			<xsl:call-template name="pageContent"/>
		</div>
		
		<!-- Map panel -->
		<div id="map_container" style="overflow:hidden; clear: both;">	
			<div id="form_wmc" style="display:none"></div>
			<div id="ol_map"></div>
		</div>
		
		<div id="search_pnl">
			<!-- Simple search panel -->
			<div id="simple_search_pnl">
				<xsl:call-template name="simple_search_content"/>
				<xsl:call-template name="categories_latestupdates"/>
			</div>
			
			<!-- Advanced search panel -->
			<div id="advanced_search_pnl">
				<xsl:call-template name="advanced_search_content"/>
			</div>
			
		</div>
	</xsl:template>
	
	<xsl:template name="categories_latestupdates">
		<xsl:call-template name="categories"/>
		<xsl:call-template name="latestUpdates"/>
	</xsl:template>
	
	<!--
	page content
	-->
	<xsl:template name="pageContent">
		<xsl:comment xml:space="preserve">
			page content
		</xsl:comment>
		<xsl:choose>
			<xsl:when test="/root/gui/searchDefaults/intermap='on'">
				<xsl:call-template name="mapcontent"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="normalcontent"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>


	<xsl:template name="simple_search_content">
		<div id="simplesearch">
			<xsl:call-template name="geofields"/>
		</div>
	</xsl:template>
	
	<xsl:template name="advanced_search_content">
		<xsl:call-template name="advanced_search_fields"/>
	</xsl:template>
		
	<xsl:template name="mapcontent">
		<h1 class="padded-content" style="margin-bottom: 0px">
			<xsl:value-of select="/root/gui/strings/mainpageTitle"/>
		</h1>
		
		<xsl:comment>FORM FIELDS</xsl:comment>
		<form name="search" action="{/root/gui/locService}/main.search" method="post"
			onsubmit="return checkSubmit()">
			<input name="extended" type="hidden" value="{/root/gui/searchDefaults/extended}"/>
			<input name="intermap" type="hidden" value="{/root/gui/searchDefaults/intermap}"/>
			<input name="remote" type="hidden" value="{/root/gui/searchDefaults/remote}"/>
			<input name="attrset" type="hidden" value="geo"/>
			<!-- FIXME: possibly replace with menu -->
			<input type="submit" style="display: none;"/>
		</form>

		<xsl:comment>MAIN CONTENT TABLE</xsl:comment>
		<div class="geosearchmain">
			<h1 id="loadingMD" style="text-align: center; display: none; width:100%"><xsl:value-of select="/root/gui/strings/searching"/></h1>
								
			<!-- This DIV contains a first-time message that will be removed when the first search will be run -->
			<div id="resultList">
				<div class="padded-content">
					<xsl:comment>MAINPAGE 1</xsl:comment>
					<xsl:copy-of select="/root/gui/strings/mainpage1/node()"/>
					<xsl:comment>MAINPAGE 2</xsl:comment>
					<xsl:copy-of select="/root/gui/strings/mainpage2/node()"/> <a href="mailto:{/root/gui/env/feedback/email}">
						<xsl:value-of select="/root/gui/env/feedback/email"/>
					</a>
				</div>
				
				<xsl:if test="/root/gui/featured/*">
					<div style="padding: 10px;">
							<xsl:comment>Featured Map</xsl:comment>
							<xsl:call-template name="featured"/>
					</div>
				</xsl:if>
			</div>
		</div>
	</xsl:template>

    <xsl:template mode="loading" match="/" priority="2">
        <div id="loading">
            <div class="loading-indicator">
            <img src="{/root/gui/url}/images/spinner.gif" width="32" height="32"/>GeoNetwork opensource catalogue<br />
            <span id="loading-msg"><xsl:value-of select="/root/gui/strings/loading"/></span>
            </div>
        </div>
    </xsl:template>
    
	<!-- FIXME : should we keep that template
	which was used for old (2.0.3) search interface ?
	This is UI is not fonctionnal anymore (JS error, search failed, ...)
	-->
	<xsl:template name="normalcontent">
		<table width="100%" height="100%">
			<tr height="100%">

				<!-- search and purpose -->
				<td class="padded-content" width="70%" height="100%">
					<table width="100%" height="100%">
						<tr>

							<!-- search -->
							<td>
								<h1>
									<xsl:value-of select="/root/gui/strings/mainpageTitle"/>
								</h1>
								<form name="search" action="{/root/gui/locService}/main.search" method="post" onsubmit="return checkSubmit()">
									<input name="extended" type="hidden" value="{/root/gui/searchDefaults/extended}"/>
									<input name="intermap" type="hidden" value="{/root/gui/searchDefaults/intermap}"/>
									<input name="remote" type="hidden" value="{/root/gui/searchDefaults/remote}"/>
									<input name="attrset" type="hidden" value="geo"/>
									<!-- FIXME: possibly replace with menu -->
									<input type="submit" style="display: none;"/>
									<table width="100%" height="100%">
										<tr>
											<td valign="top">
												<xsl:call-template name="fields"/>
											</td>
											<td width="200">
												<table height="100%">
													<tr>
														<td class="padded" align="right">
															<button class="content-small" type="button" onclick="goIntermap('on','{/root/gui/locService}/main.home')">
																<xsl:value-of select="/root/gui/strings/intermapSearch"/>
															</button>
														</td>
													</tr>
													<tr>
														<td class="padded" align="right">
															<xsl:choose>
																<xsl:when test="/root/gui/searchDefaults/extended='off'">
																	<button class="content-small" type="button" onclick="goExtended('on','{/root/gui/locService}/main.home')">
																		<xsl:value-of select="/root/gui/strings/extended"/>
																	</button>
																</xsl:when>
																<xsl:otherwise>
																	<button class="content-small" type="button" onclick="goExtended('off','{/root/gui/locService}/main.home')">
																		<xsl:value-of select="/root/gui/strings/simple"/>
																	</button>
																</xsl:otherwise>
															</xsl:choose>
														</td>
													</tr>
													<tr>
														<td class="padded" align="right">
															<xsl:choose>
																<xsl:when test="/root/gui/searchDefaults/remote='off'">
																	<button class="content-small" type="button" onclick="goRemote('on','{/root/gui/locService}/main.home')">
																		<xsl:value-of select="/root/gui/strings/remote"/>
																	</button>
																</xsl:when>
																<xsl:otherwise>
																	<button class="content-small" type="button" onclick="goRemote('off','{/root/gui/locService}/main.home')">
																		<xsl:value-of select="/root/gui/strings/local"/>
																	</button>
																</xsl:otherwise>
															</xsl:choose>
														</td>
													</tr>
													<tr height="100%">
														<td align="right" valign="baseline">
															<a onclick="doSubmit()">
																<img onmouseover="this.src='{/root/gui/locUrl}/images/search-white.gif'" onmouseout="this.src='{/root/gui/locUrl}/images/search-blue.gif'" style="cursor:hand;cursor:pointer" src="{/root/gui/locUrl}/images/search-blue.gif" alt="Search" title="{/root/gui/strings/search}" align="top"/>
															</a>
															
														</td>
													</tr>
												</table>
											</td>
										</tr>
									</table>
								</form>
							</td>
						</tr>
						<tr>
							<td class="dots"/>
						</tr>

						<tr height="100%">
							<!-- Info -->
							<td valign="top">
								<xsl:copy-of select="/root/gui/strings/mainpage1"/>
								<xsl:copy-of select="/root/gui/strings/mainpage2"/>
								<a href="mailto:{/root/gui/env/feedback/email}">
									<xsl:value-of select="/root/gui/env/feedback/email"/>
								</a>
							</td>
						</tr>
					</table>
				</td>

				<td class="separator"/>

				<!-- right -->
				<td class="padded-content" valign="top">
					<center>
						<img src="{/root/gui/url}/images/intermap.gif" alt="InterMap" align="top"/>
					</center>
					<xsl:copy-of select="/root/gui/strings/interMapInfo"/>
				</td>
			</tr>

			<tr>
				<td class="separator"/>
			</tr>

			<!-- types -->
			<tr>
				<td colspan="3">
					<table width="100%">
						<tr>

							<xsl:choose>
								<xsl:when test="/root/gui/featured/*">

									<!-- featured map -->
									<td class="footer" align="center" valign="top" width="33%">
										<xsl:call-template name="featured"/>
									</td>

									<td class="separator"/>

									<!-- latest updates -->
									<td class="footer" align="left" valign="top" width="33%">
										<xsl:call-template name="latestUpdates"/>
									</td>

								</xsl:when>
								<xsl:otherwise>

									<!-- latest updates -->
									<td class="footer" align="left" valign="top" width="50%">
										<xsl:call-template name="latestUpdates"/>
									</td>

								</xsl:otherwise>
							</xsl:choose>

							<!-- categories -->
							<xsl:call-template name="categories"/>

						</tr>
					</table>
				</td>
			</tr>
		</table>
	</xsl:template>

	<!--
	featured map
	-->
	<xsl:template name="featured">
		<fieldset id="featured">
			<legend><xsl:value-of select="/root/gui/strings/featuredMap"/></legend>
			<table>
				<xsl:for-each select="/root/gui/featured/*">
					<xsl:variable name="md">
						<xsl:apply-templates mode="brief" select="."/>
					</xsl:variable>
					<xsl:variable name="metadata" select="exslt:node-set($md)/*[1]"/>
					<tr>
						<td>
							
								<div class="arrow" onClick="gn_showSingleMetadata('{geonet:info/id}');" 
				style="cursor:hand;cursor:pointer">
									<h2><xsl:value-of select="$metadata/title"/></h2>
								</div>
							<p/>
							<xsl:variable name="abstract" select="$metadata/abstract"/>
							<xsl:choose>
								<xsl:when test="string-length($abstract) &gt; $maxAbstract">
									<xsl:value-of select="substring($abstract, 0, $maxAbstract)"/>
										<div class="arrow" onClick="gn_showSingleMetadata('{geonet:info/id}');" 
				style="cursor:hand;cursor:pointer">...<xsl:value-of select="/root/gui/strings/more"/>...</div>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="$abstract"/>
								</xsl:otherwise>
							</xsl:choose>
						</td>
						<td width="40%">
							<xsl:call-template name="thumbnail">
								<xsl:with-param name="metadata" select="$metadata"/>
							</xsl:call-template>
						</td>
					</tr>
				</xsl:for-each>
			</table>
		</fieldset>
	</xsl:template>

	<!--
	latest updates
	-->
	<xsl:template name="latestUpdates">
		<xsl:if test="/root/gui/latestUpdated/*">
			<div class="geosearchfields">
				<h1 align="left">
					<!--xsl:value-of select="/root/gui/strings/recentAdditions"/--> &#160;&#160;&#160; 
					<a href="{/root/gui/locService}/rss.latest?georss=simplepoint" target="_blank">
						<img style="cursor:hand;cursor:pointer" src="{/root/gui/url}/images/georss.png"
							alt="GeoRSS-GML" title="{/root/gui/strings/georss}" align="top"/>
					</a>
				</h1>
				<xsl:for-each select="/root/gui/latestUpdated/*">
					<xsl:variable name="md">
						<xsl:apply-templates mode="brief" select="."/>
					</xsl:variable>
					<xsl:variable name="metadata" select="exslt:node-set($md)/*[1]"/>
					<div class="arrow" onClick="gn_showSingleMetadataUUID('{geonet:info/uuid}');" 
						style="cursor:hand;cursor:pointer">
						<xsl:value-of select="$metadata/title"/>
						<br/>
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
	</xsl:template>

	<!--
	categories
	-->
	<xsl:template name="categories">
		<xsl:if test="/root/gui/categories/* and /root/gui/config/category/admin">
			<div class="geosearchfields" style="margin-top:10px">
				<xsl:for-each select="/root/gui/categories/*">
					<xsl:sort select="label/child::*[name() = $lang]" order="ascending"/>
					<xsl:variable name="categoryName" select="name"/>
					<xsl:variable name="categoryLabel" select="label/child::*[name() = $lang]"/>
					<div class="arrow" onClick="runCategorySearch('{$categoryName}');" style="cursor:hand;cursor:pointer">
						<xsl:if test="/root/gui/config/category/display-in-search">
							<img class="category" src="../../images/category/{$categoryName}.png"/>
						</xsl:if>
						<xsl:value-of select="$categoryLabel"/>
						<br/>
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
	</xsl:template>

	<!--
	search fields
	-->
	<xsl:template name="fields">
		<table>
			<!-- Title -->
			<xsl:if test="string(/root/gui/searchDefaults/extended)='on'">
				<tr>
					<th class="padded">
						<xsl:value-of select="/root/gui/strings/rtitle"/>
					</th>
					<td class="padded">
						<input class="content" name="title" size="30"
							value="{/root/gui/searchDefaults/title}"/>
					</td>
				</tr>
			</xsl:if>

			<!-- Abstract -->
			<xsl:if test="string(/root/gui/searchDefaults/extended)='on'">
				<tr>
					<th class="padded">
						<xsl:value-of select="/root/gui/strings/abstract"/>
					</th>
					<td class="padded">
						<input class="content" name="abstract" size="30"
							value="{/root/gui/searchDefaults/abstract}"/>
					</td>
				</tr>
			</xsl:if>

			<!-- Any (free text) -->
			<tr>
				<th class="padded">
					<xsl:value-of select="/root/gui/strings/searchText"/>
				</th>
				<td class="padded">
					<input class="content" name="any" size="30"
						value="{/root/gui/searchDefaults/any}"/>
					<br/>
				</td>
			</tr>

			<!-- Keywords -->
			<xsl:if test="string(/root/gui/searchDefaults/extended)='on'">
				<tr>
					<th class="padded">
						<xsl:value-of select="/root/gui/strings/keywords"/>
					</th>
					<td class="padded">
						<input class="content" id="themekey" name="themekey" size="30"
							value="{/root/gui/searchDefaults/themekey}"/>

						<a href="#">
							<img src="{/root/gui/url}/images/gdict.png" align="absmiddle"
								onclick="keywordSelector();"/>
						</a>

						<div id="keywordSelectorFrame" class="keywordSelectorFrame"
							style="display:none;">
							<div id="keywordSelector" class="keywordSelector"/>
						</div>

						<div id="keywordList" class="keywordList"/>
						<script type="text/javascript">
					  var keyordsSelected = false;

					  function addQuote (li){
					  $("themekey").value = '"'+li.innerHTML+'"';
					  }

					  function keywordSelector(){
						if ($("keywordSelectorFrame").style.display == 'none'){
							if (!keyordsSelected){
								new Ajax.Updater("keywordSelector","portal.search.keywords?mode=selector&amp;keyword="+$("themekey").value);
								keyordsSelected = true;
							}
							$("keywordSelectorFrame").style.display = 'block';
						}else{
							$("keywordSelectorFrame").style.display = 'none';
						}
					  }

					  function keywordCheck(k, check){
						k = '"'+ k + '"';
						//alert (k+"-"+check);
						if (check){	// add the keyword to the list
							if ($("themekey").value != '') // add the "or" keyword
								$("themekey").value += ' or '+ k;
							else
								$("themekey").value = k;
						}else{ // Remove that keyword
							$("themekey").value = $("themekey").value.replace(' or '+ k, '');
							$("themekey").value = $("themekey").value.replace(k, '');
							pos = $("themekey").value.indexOf(" or ");
							if (pos == 0){
								$("themekey").value = $("themekey").value.substring (4, $("themekey").value.length);
							}
						}
					  }

					  new Ajax.Autocompleter('themekey', 'keywordList', 'portal.search.keywords?',{paramName: 'keyword', updateElement : addQuote});
					</script>
					</td>
				</tr>
			</xsl:if>

			<!-- Fuzzy search similarity for text field only (ie. Keywords, Any, Abstract, Title) set to 80% by default -->
			<input class="content" id="similarity" name="similarity" type="hidden" value=".8"/>
			<xsl:if test="string(/root/gui/searchDefaults/extended)='on'">
				<tr>
					<th>
						<xsl:value-of select="/root/gui/strings/fuzzy"/>
					</th>
					<td>
						<table>
							<tr>
								<td width="20px" align="center">-</td>
								<td>
									<div class="track" id="similarityTrack"
										style="width:100px;height:5px;">
										<xsl:attribute name="alt">
											<xsl:value-of select="/root/gui/strings/fuzzySearch"/>
										</xsl:attribute>
										<xsl:attribute name="title">
											<xsl:value-of select="/root/gui/strings/fuzzySearch"/>
										</xsl:attribute>
										<div class="handle" id="similarityHandle"
											style="width:5px;height:10px;"> </div>
									</div>
								</td>
								<td width="20px" align="center">+</td>
								<td>
									<div id="similarityDebug" style="display:none;"/>
								</td>
							</tr>
						</table>
						<script type="text/javascript" language="JavaScript1.2">
						var similaritySlider = new Control.Slider(
													'similarityHandle',
													'similarityTrack'
													,{range:$R(0,10),
														values:[0,1,2,3,4,5,6,7,8,9,10]}
													);
						similaritySlider.options.onSlide = function(v){
							$('similarity').value = (v/10);
							$('similarityDebug').innerHTML = '('+(v/10)+')';
						};
						similaritySlider.options.onChange = function(v){
					        $('similarity').value = (v/10);
							$('similarityDebug').innerHTML = '('+(v/10)+')';
					    };
						similaritySlider.setValue($('similarity').value*10);
					</script>
					</td>
				</tr>
			</xsl:if>


			<!-- Area -->
			<xsl:if test="string(/root/gui/searchDefaults/extended)='on'">
				<tr>
					<th class="padded">
						<xsl:value-of select="/root/gui/strings/location"/>
					</th>
					<td class="padded">
						<table>
							<tr>
								<td>
									<select class="content" name="relation">
										<xsl:for-each select="/root/gui/strings/boundingRelation">
											<option>
												<xsl:if test="@value=/root/gui/searchDefaults/relation">
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
								<td>
									<xsl:text disable-output-escaping="yes">&amp;nbsp;&amp;nbsp;</xsl:text>
								</td>
								<td>

									<!-- regions combobox -->

									<select class="content" name="region" id="region">
										<option value="">
											<xsl:if test="/root/gui/searchDefaults/theme='_any_'">
												<xsl:attribute name="selected"/>
											</xsl:if>
											<xsl:value-of select="/root/gui/strings/any"/>
										</option>

										<xsl:for-each select="/root/gui/regions/record">
											<xsl:sort select="label/child::*[name() = $lang]"
												order="ascending"/>
											<option>
												<xsl:if test="id=/root/gui/searchDefaults/region">
												<xsl:attribute name="selected"/>
												</xsl:if>
												<xsl:attribute name="value">
												<xsl:value-of select="id"/>
												</xsl:attribute>
												<xsl:value-of
												select="label/child::*[name() = $lang]"/>
											</option>
										</xsl:for-each>
									</select>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</xsl:if>

			<!-- Group -->
			<xsl:if
				test="string(/root/gui/session/userId)!='' and string(/root/gui/searchDefaults/extended)='on' and string(/root/gui/searchDefaults/remote)='off'">
				<tr>
					<th class="padded">
						<xsl:value-of select="/root/gui/strings/group"/>
					</th>
					<td class="padded">
						<select class="content" name="group">
							<option value="">
								<xsl:if test="/root/gui/searchDefaults/group=''">
									<xsl:attribute name="selected"/>
								</xsl:if>
								<xsl:value-of select="/root/gui/strings/any"/>
							</option>
							<xsl:for-each select="/root/gui/groups/record">
								<xsl:sort order="ascending" select="name"/>
								<option>
									<xsl:if test="id=/root/gui/searchDefaults/group">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:attribute name="value">
										<xsl:value-of select="id"/>
									</xsl:attribute>
									<xsl:value-of select="name"/>
								</option>
							</xsl:for-each>
						</select>
					</td>
				</tr>
			</xsl:if>

			<!-- Category -->
			<xsl:if
				test="string(/root/gui/searchDefaults/extended)='on' and string(/root/gui/searchDefaults/remote)='off'">
				<tr>
					<th class="padded">
						<xsl:value-of select="/root/gui/strings/category"/>
					</th>
					<td class="padded">
						<select class="content" name="category">
							<option value="">
								<xsl:if test="/root/gui/searchDefaults/category=''">
									<xsl:attribute name="selected"/>
								</xsl:if>
								<xsl:value-of select="/root/gui/strings/any"/>
							</option>

							<xsl:for-each select="/root/gui/categories/record">
								<xsl:sort select="label/child::*[name() = $lang]" order="ascending"/>

								<option>
									<xsl:if test="name = /root/gui/searchDefaults/category">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:attribute name="value">
										<xsl:value-of select="name"/>
									</xsl:attribute>
									<xsl:value-of select="label/child::*[name() = $lang]"/>
								</option>
							</xsl:for-each>
						</select>
					</td>
				</tr>
			</xsl:if>

			<!-- Source -->
			<xsl:if
				test="string(/root/gui/searchDefaults/extended)='on' and string(/root/gui/searchDefaults/remote)='off'">
				<tr>
					<th class="padded">
						<xsl:value-of select="/root/gui/strings/site"/>
					</th>
					<td class="padded">
						<select class="content" name="siteId">
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
								<option>
									<xsl:if test="$source=/root/gui/searchDefaults/siteId">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:attribute name="value">
										<xsl:value-of select="$source"/>
									</xsl:attribute>
									<xsl:value-of select="$sourceName"/>
								</option>
							</xsl:for-each>
						</select>
					</td>
				</tr>
			</xsl:if>

			<!-- Map type -->
			<xsl:if test="string(/root/gui/searchDefaults/remote)='off'">
				<tr>
					<th class="padded">
						<xsl:value-of select="/root/gui/strings/mapType"/>
					</th>
					<td>
						<input name="digital" type="checkbox" value="on">
							<xsl:if test="/root/gui/searchDefaults/digital='on'">
								<xsl:attribute name="checked"/>
							</xsl:if>
							<xsl:value-of select="/root/gui/strings/digital"/>
						</input>
						<!--
						FIXME: disabled
						<xsl:if test="string(/root/gui/searchDefaults/extended)='on' and string(/root/gui/searchDefaults/remote)='off'">
							&#xA0;&#xA0;
							<input class="content" name="download" type="checkbox">
								<xsl:if test="/root/gui/searchDefaults/download='on'">
									<xsl:attribute name="checked"/>
								</xsl:if>
								<xsl:value-of select="/root/gui/strings/downloadData"/>
							</input>
							&#xA0;&#xA0;
							<input class="content" name="online" type="checkbox">
								<xsl:if test="/root/gui/searchDefaults/online='on'">
									<xsl:attribute name="checked"/>
								</xsl:if>
								<xsl:value-of select="/root/gui/strings/interactiveMap"/>
							</input>
							&#xA0;&#xA0;
							&#xA0;&#xA0;
						</xsl:if>
						-->
						&#xA0;&#xA0; <input name="paper" type="checkbox" value="on">
							<xsl:if test="/root/gui/searchDefaults/paper='on'">
								<xsl:attribute name="checked"/>
							</xsl:if>
							<xsl:value-of select="/root/gui/strings/paper"/>
						</input>
					</td>
				</tr>
			</xsl:if>

			<!-- Template -->
			<xsl:if
				test="string(/root/gui/session/userId)!='' and /root/gui/services/service[@name='metadata.edit'] and string(/root/gui/searchDefaults/remote)='off'">
				<tr>
					<th class="padded">
						<xsl:value-of select="/root/gui/strings/kind"/>
					</th>
					<td>
						<select class="content" name="template" size="1">
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
							<option value="s">
								<xsl:if test="/root/gui/searchDefaults/template='s'">
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								<xsl:value-of select="/root/gui/strings/subtemplate"/>
							</option>
						</select>
					</td>
				</tr>
			</xsl:if>

			<!-- remote search fields -->
			<xsl:if test="string(/root/gui/searchDefaults/remote)='on'">

				<tr>
					<td class="dots" colspan="2"/>
				</tr>

				<!-- Profiles and servers -->
				<tr>
					<th class="padded">
						<xsl:value-of select="/root/gui/strings/profile"/>
					</th>
					<td class="padded">
						<select class="content" name="profile" onchange="profileSelected()">
							<xsl:for-each select="/root/gui/searchProfiles/profile">
								<option>
									<xsl:if
										test="string(@value)=string(/root/gui/searchDefaults/profile)">
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

				<tr>
					<th class="padded">
						<xsl:value-of select="/root/gui/strings/server"/>
					</th>
					<td class="padded">
						<select class="content" name="servers" size="6" multiple="true"
							onchange="serverSelected()">
							<xsl:for-each select="/root/gui/repositories/Instance">
								<xsl:variable name="name" select="@instance_dn"/>
								<xsl:variable name="collection" select="@collection_dn"/>
								<xsl:variable name="description"
									select="/root/gui/repositories/Collection[@collection_dn=$collection]/@collection_name"/>
								<option>
									<xsl:if
										test="/root/gui/searchDefaults/servers/server[string(.)=$name]">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:attribute name="value">
										<xsl:value-of select="$name"/>
									</xsl:attribute>
									<xsl:value-of select="$description"/>
								</option>
							</xsl:for-each>
						</select>
					</td>
				</tr>

				<!-- timeout -->
				<tr>
					<th class="padded">
						<xsl:apply-templates select="/root/gui/strings/timeout" mode="caption"/>
					</th>
					<td class="padded">
						<select class="content" name="timeout">
							<xsl:for-each select="/root/gui/strings/timeoutChoice">
								<option>
									<xsl:if
										test="string(@value)=string(/root/gui/searchDefaults/timeout)">
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
			</xsl:if>

			<!-- other search options -->

			<tr>
				<td class="dots" colspan="2"/>
			</tr>

			<!-- hits per page -->
			<tr>
				<th class="padded">
					<xsl:value-of select="/root/gui/strings/hitsPerPage"/>
				</th>
				<td class="padded">
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

		</table>
	</xsl:template>

</xsl:stylesheet>
