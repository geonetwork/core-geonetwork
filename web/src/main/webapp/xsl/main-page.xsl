<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:exslt="http://exslt.org/common" xmlns:geonet="http://www.fao.org/geonetwork"
	exclude-result-prefixes="xsl exslt geonet">

	<xsl:include href="main.xsl"/>
	<xsl:include href="metadata.xsl"/>
	<xsl:include href="searchform_simple_template.xsl"/>
	<xsl:include href="searchform_advanced.xsl"/>
	
	<xsl:template mode="css" match="/">
		<xsl:call-template name="geoCssHeader"/>
		<xsl:call-template name="ext-ux-css"/>
	</xsl:template>
	
	<!--
		additional scripts
	-->
	<xsl:template mode="script" match="/">
	
		<!-- To avoid an interaction with prototype and ExtJs.Tooltip, should be loadded before ExtJs -->
		<xsl:choose>
			<xsl:when test="/root/request/debug">
				<script type="text/javascript" src="{/root/gui/url}/scripts/prototype.js"/><xsl:text>&#10;</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<script type="text/javascript" src="{/root/gui/url}/scripts/lib/gn.libs.js"/><xsl:text>&#10;</xsl:text>
			</xsl:otherwise>
		</xsl:choose>

		<xsl:call-template name="geoHeader"/>

		<!-- Required by keyword selection panel -->
		<xsl:if test="/root/gui/config/search/keyword-selection-panel">
			<xsl:call-template name="ext-ux"/>
		</xsl:if>
		
		<xsl:choose>
			<xsl:when test="/root/request/debug">         	
				<script type="text/javascript" src="{/root/gui/url}/scripts/geonetwork.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/scriptaculous/scriptaculous.js?load=slider,effects,controls"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/modalbox.js"/><xsl:text>&#10;</xsl:text>

				<script type="text/javascript" src="{/root/gui/url}/scripts/gn_search.js"/><xsl:text>&#10;</xsl:text>

				<!--link rel="stylesheet" type="text/css" href="{/root/gui/url}/scripts/ext/resources/css/ext-all.css" />
				<link rel="stylesheet" type="text/css" href="{/root/gui/url}/scripts/ext/resources/css/file-upload.css"/>

				<link rel="stylesheet" type="text/css" href="{/root/gui/url}/scripts/openlayers/theme/default/style.css"/>
				<link rel="stylesheet" type="text/css" href="{/root/gui/url}/geonetwork_map.css" /-->

				<script type="text/javascript" src="{/root/gui/url}/scripts/ext/adapter/ext/ext-base.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/ext/ext-all.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/ext/form/FileUploadField.js"/><xsl:text>&#10;</xsl:text>

				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/lib/OpenLayers.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/addins/LoadingPanel.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/addins/ScaleBar.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/geo/proj4js-compressed.js"/><xsl:text>&#10;</xsl:text>

				<script type="text/javascript" src="{/root/gui/url}/scripts/geoext/lib/GeoExt.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/mapfish/MapFish.js"/><xsl:text>&#10;</xsl:text>

				<script type="text/javascript" src="{/root/gui/url}/scripts/map/core/OGCUtil.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/core/MapStateManager.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/core/CatalogueInterface.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/core/WMCManager.js"/><xsl:text>&#10;</xsl:text>

				<script type="text/javascript" src="{/root/gui/url}/scripts/map/Control/ExtentBox.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/Control/ZoomWheel.js"/><xsl:text>&#10;</xsl:text>

				<script type="text/javascript" src="{/root/gui/url}/scripts/map/lang/de.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/lang/en.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/lang/es.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/lang/fr.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/lang/nl.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/lang/no.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/lang/it.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/lang/ca.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/lang/tr.js"/><xsl:text>&#10;</xsl:text>

				<script type="text/javascript" src="{/root/gui/url}/scripts/map/Ext.ux/form/DateTime.js"/><xsl:text>&#10;</xsl:text>

				<script type="text/javascript" src="{/root/gui/url}/scripts/map/widgets/tree/WMSListGenerator.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/widgets/tree/WMSTreeGenerator.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/widgets/wms/BrowserPanel.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/widgets/wms/LayerInfoPanel.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/widgets/wms/LayerStylesPanel.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/widgets/wms/PreviewPanel.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/widgets/wms/WMSLayerInfo.js"/><xsl:text>&#10;</xsl:text>

				<script type="text/javascript" src="{/root/gui/url}/scripts/map/widgets/FeatureInfoPanel.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/widgets/LegendPanel.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/widgets/OpacitySlider.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/widgets/PrintAction.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/widgets/ProjectionSelector.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/widgets/TimeSelector.js"/><xsl:text>&#10;</xsl:text>

				<script type="text/javascript" src="{/root/gui/url}/scripts/map/windows/BaseWindow.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/windows/SingletonWindowManager.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/windows/AddWMS.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/windows/FeatureInfo.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/windows/Opacity.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/windows/LoadWmc.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/windows/WMSTime.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/windows/LayerStyles.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/windows/WmsLayerMetadata.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/map/windows/Disclaimer.js"/><xsl:text>&#10;</xsl:text>

				<script type="text/javascript" src="{/root/gui/url}/scripts/ol_settings.js"/><xsl:text>&#10;</xsl:text>	
				<script type="text/javascript" src="{/root/gui/url}/scripts/ol_minimap.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/ol_map.js"/><xsl:text>&#10;</xsl:text>

				<script type="text/javascript" src="{/root/gui/url}/scripts/editor/tooltip.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/editor/tooltip-manager.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/editor/simpletooltip.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/editor/metadata-show.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/editor/metadata-editor.js"/><xsl:text>&#10;</xsl:text>
			</xsl:when>
			<xsl:otherwise>             
				<script type="text/javascript" src="{/root/gui/url}/scripts/lib/gn.libs.scriptaculous.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/lib/gn.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/lib/gn.search.js"/><xsl:text>&#10;</xsl:text>

				<!-- Editor JS is still required here at least for batch operation -->
				<script type="text/javascript" src="{/root/gui/url}/scripts/lib/gn.editor.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/lib/gn.libs.map.js"/><xsl:text>&#10;</xsl:text>
			</xsl:otherwise>
		</xsl:choose>

		<script type="text/javascript" src="{/root/gui/url}/scripts/core/kernel/kernel.js"/><xsl:text>&#10;</xsl:text>

		<xsl:variable name="wmc"><xsl:copy-of select="/root/request/wmc"/></xsl:variable>
		<xsl:variable name="uuid"><xsl:copy-of select="/root/request/uuid"/></xsl:variable>
		<xsl:variable name="id"><xsl:copy-of select="/root/request/id"/></xsl:variable>
		<xsl:variable name="urlWMS"><xsl:copy-of select="/root/request/url"/></xsl:variable>
		<xsl:variable name="typeWMS"><xsl:copy-of select="/root/request/type"/></xsl:variable>
		<xsl:variable name="tab"><xsl:copy-of select="/root/request/tab"/></xsl:variable>
		<xsl:variable name="search"><xsl:copy-of select="/root/request/search"/></xsl:variable>

		<script type="text/javascript">

			function init() {};


			var getIMServiceURL = function(service){
				// FIXME: the "/intermap/" context should be parametrized
				return "/intermap/srv/"+Env.lang+"/"+service;
			};

			function doSubmit(){
				if (checkSubmit())
					document.search.submit();
			}

			Ext.onReady(function(){
				$("loading").hide();

				var GNCookie = new Ext.state.CookieProvider({
					expires: new Date(new Date().getTime()+(1000*60*60*24*365))
										//1 year from now
					});

				Ext.state.Manager.setProvider(GNCookie);

				GeoNetwork.MapStateManager.loadMapState();

				initMapViewer();
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

				var searchTabs = new Ext.TabPanel({
					renderTo: 'search_tabs',
					activeTab : 0,
					deferredRender: false,
					border: false,
					bodyBorder: false,
					items: [ {
								itemId: 'default',
								bodyStyle: 'padding:15px',
								autoScroll: true,
								title: '<xsl:value-of select="/root/gui/strings/simpleSearch"/>',
								contentEl: 'simple_search_pnl'
							 }
							,{
								itemId: 'advanced',
								bodyStyle: 'padding:15px',
								autoScroll: true,
								title: '<xsl:value-of select="/root/gui/strings/advancedSearch"/>',
								contentEl: 'advanced_search_pnl'
							}
							<xsl:if test="/root/gui/config/search/show-remote-search">
								,{
									itemId: 'remote',
									bodyStyle: 'padding:15px',
									autoScroll: true,
									title: '<xsl:value-of select="/root/gui/strings/remoteSearch"/>',
									contentEl: 'remote_search_pnl'
								}
							</xsl:if>
								
							]
				});

				searchTabs.on('tabchange', function() {
					GNCookie.set('search',{searchTab: this.getActiveTab().itemId}); 
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
								layout: 'fit',
								border:false,
								width: 400,
								minSize: 300,
								maxSize: 450,
								autoScroll: true,
								collapsible: true,
								collapseMode: "mini",
								split: 'true',
								useSplitTips:true,
								collapsibleSplitTip: 'Drag to rezise the search panel. Double click to show/hide it',
								items: [searchTabs] 
								},
								{region:'center', 
								id: 'main-viewport',
								border:false,
								layout: 'border',
								items: [
									{region:'north',
									id: 'north-map-panel',
									title: '<xsl:value-of select="/root/gui/strings/mapViewer"/>',
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
								}]
							}]
						}]
					}]
				});

				mainViewport = new Ext.Viewport({
							layout:'border',
							border:false,
							autoScroll: true,
							items:[viewport]
				});

				// Initialize small maps for search
				initMapsSearch();

				var requestTab="<xsl:value-of select="$tab"/>";
				var search="<xsl:value-of select="$search"/>";

				var currentSearch = '';
				var cookie = GNCookie.get('search');
				if (cookie) currentSearch = cookie.searchTab;
				<!-- show tab requested otherwise show last tab selected -->
				if (requestTab == 'simple') {
					searchTabs.setActiveTab(requestTab);
					showSimpleSearch(search);

					// Init of advanced tab, otherwise when selected doesn't show "When" fields as not initialized
					initAdvancedSearch();

				} else if (requestTab == 'advanced') {
					searchTabs.setActiveTab(requestTab);
					showAdvancedSearch(search);
				} else if (requestTab == 'remote') {
					searchTabs.setActiveTab(requestTab);
					showRemoteSearch(search);

					//  Init of advanced tab, otherwise when selected doesn't show "When" fields as not initialized
						initAdvancedSearch();

				} else if (currentSearch == 'advanced') {
					searchTabs.setActiveTab(currentSearch);
					showAdvancedSearch(search);
				} else if (currentSearch == 'remote') {
					searchTabs.setActiveTab(currentSearch);
					showRemoteSearch(search);

					// Init of advanced tab, otherwise when selected doesn't show "When" fields as not initialized
					initAdvancedSearch();

				} else {
					searchTabs.setActiveTab('default');
					showSimpleSearch(search);

					// Init of advanced tab, otherwise when selected doesn't show "When" fields as not initialized
					initAdvancedSearch();
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

				<!-- If a WMS server & layername(s) are passed, it will be opened 
					 in the map viewer the large map viewer will also be opened -->
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
			});

			function initMapViewer() {
				var mapOptions = <xsl:value-of select='/root/gui/config/mapViewer/@options'/>;

				// Load layers defined in config file
				var layers = [];

				<xsl:for-each select="/root/gui/config/mapViewer/layers/layer">
					layers.push(["<xsl:value-of select='@tocName'/>","<xsl:value-of select='@server'/>",<xsl:value-of select='@params'/>, <xsl:value-of select='@options'/>]);                           
				</xsl:for-each>

				// Init projection list
				<xsl:for-each select="/root/gui/config/mapViewer/proj/crs">
				GeoNetwork.ProjectionList.push(["<xsl:value-of select='@code'/>","<xsl:value-of select='@name'/>"]);                
				</xsl:for-each>

				// Init WMS server list
				<xsl:for-each select="/root/gui/config/mapViewer/servers/server">
				GeoNetwork.WMSList.push(["<xsl:value-of select='@name'/>","<xsl:value-of select='@url'/>"]);                
				</xsl:for-each>

 				var scales = <xsl:value-of select='/root/gui/config/mapViewer/scales/@values'/>;

				// Initialize map viewer
				GeoNetwork.mapViewer.init(backgroundLayers, mapOptions, scales);
				}

			function initMapsSearch() {
				var mapOptions1 = <xsl:value-of select='/root/gui/config/mapSearch/@options'/>;
				var mapOptions2 = <xsl:value-of select='/root/gui/config/mapSearch/@options'/>;
				var mapOptions3 = <xsl:value-of select='/root/gui/config/mapSearch/@options'/>;

				var simpleAoiIds = {
						eastBL: 'eastBL_simple',
						westBL: 'westBL_simple',
						northBL: 'northBL_simple',
						southBL: 'southBL_simple'
				};

				var advancedAoiIds = {
						eastBL: 'eastBL',
						westBL: 'westBL',
						northBL: 'northBL',
						southBL: 'southBL'
				};

				var remoteAoiIds = {
						eastBL: 'eastBL_remote',
						westBL: 'westBL_remote',
						northBL: 'northBL_remote',
						southBL: 'southBL_remote'
				};

                // Initialize minimaps
                GeoNetwork.minimapSimpleSearch.init("ol_minimap1", "region_simple", backgroundLayersMapSearch, mapOptions1, simpleAoiIds);
                GeoNetwork.minimapAdvancedSearch.init("ol_minimap2", "region", backgroundLayersMapSearch, mapOptions2, advancedAoiIds);
                GeoNetwork.minimapRemoteSearch.init("ol_minimap3", "region_remote", backgroundLayersMapSearch, mapOptions3, remoteAoiIds);


                GeoNetwork.minimapSimpleSearch.setSynchMinimap(GeoNetwork.minimapAdvancedSearch);
                GeoNetwork.minimapSimpleSearch.setSynchMinimap(GeoNetwork.minimapRemoteSearch);
                GeoNetwork.minimapAdvancedSearch.setSynchMinimap(GeoNetwork.minimapSimpleSearch);
                GeoNetwork.minimapAdvancedSearch.setSynchMinimap(GeoNetwork.minimapRemoteSearch);
                GeoNetwork.minimapRemoteSearch.setSynchMinimap(GeoNetwork.minimapSimpleSearch);
                GeoNetwork.minimapRemoteSearch.setSynchMinimap(GeoNetwork.minimapAdvancedSearch);
                GeoNetwork.CatalogueInterface.init(GeoNetwork.mapViewer.getMap());
                GeoNetwork.MapStateManager.applyMapState(GeoNetwork.mapViewer.getMap());
            }
            
			function collapseMap(pnl) {
				Ext.getCmp('main-viewport').layout.north.getCollapsedEl().titleEl.dom.innerHTML = '<xsl:value-of select="/root/gui/strings/showMap"/>';
			}
			
			function expandMap(pnl) {
				Ext.getCmp('main-viewport').layout.north.getCollapsedEl().titleEl.dom.innerHTML = '<xsl:value-of select="/root/gui/strings/mapViewer"/>';
			}

			
			
		</script>
	</xsl:template>


	<xsl:variable name="lang" select="/root/gui/language"/>

	<xsl:template name="content">
		<!-- Page content - Search Results etc -->
		<div id="content" >
			<xsl:call-template name="pageContent"/>
		</div>
		
		<!-- Map panel -->
		<div id="map_container" style="overflow:hidden; clear: both;">	
			<div id="form_wmc" style="display:none"></div>
			<div id="ol_map"></div>
		</div>
		
		<!-- Search forms -->
		<div id="search_tabs">
			<div id="simple_search_pnl" class="x-hide-display" title="{/root/gui/strings/simpleSearch}">
				<xsl:call-template name="simple_search_panel"/>
				<xsl:call-template name="categories_latestupdates"/>
			</div>
			<div id="advanced_search_pnl" class="x-hide-display" title="{/root/gui/strings/advancedSearch}">
				<xsl:call-template name="advanced_search_panel">
					<xsl:with-param name="remote" select="false()"/>
				</xsl:call-template>
			</div>
			<div id="remote_search_pnl" class="x-hide-display" title="{/root/gui/strings/remoteSearch}">
				<xsl:call-template name="advanced_search_panel">
					<xsl:with-param name="remote" select="true()"/>
				</xsl:call-template>
			</div>
		</div>
	</xsl:template>

	<xsl:template name="categories_latestupdates">
		<xsl:call-template name="categories"/>
		<div id="latest_updates">
			<xsl:call-template name="latestUpdates"/>
		</div>
	</xsl:template>	

	<!--
	page content - search results etc
	-->
	<xsl:template name="pageContent">
		<h1 class="padded-content" style="margin-bottom: 0px">
			<xsl:value-of select="/root/gui/strings/mainpageTitle"/>
		</h1>
		
		<xsl:comment>MAIN CONTENT</xsl:comment>
		<div class="geosearchmain">
			<h1 id="loadingMD" style="text-align: center; display: none; width:100%"> <img src="{/root/gui/url}/images/loading.gif" width="20" height="21" /><xsl:value-of select="/root/gui/strings/searching"/></h1>
								
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

	<!--
	loading indicator	
	-->
	<xsl:template mode="loading" match="/" priority="2">
		<div id="loading">
			<div class="loading-indicator">
				<img src="{/root/gui/url}/images/spinner.gif" width="32" height="32"/>GeoNetwork opensource catalogue<br />
				<span id="loading-msg"><xsl:value-of select="/root/gui/strings/loading"/></span>
			</div>
		</div>
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

</xsl:stylesheet>
