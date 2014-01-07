<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:geonet="http://www.fao.org/geonetwork" 
	xmlns:exslt="http://exslt.org/common"
	xmlns:dc = "http://purl.org/dc/elements/1.1/" 
	xmlns:gmd="http://www.isotc211.org/2005/gmd" 
	xmlns:gco="http://www.isotc211.org/2005/gco"
	exclude-result-prefixes="gco gmd dc exslt geonet"
	>

	<!--
	show metadata form
	-->
	
	<xsl:include href="main.xsl"/>
	<xsl:include href="metadata.xsl"/>

	<xsl:variable name="protocol" select="/root/gui/env/server/protocol" />
	<xsl:variable name="host" select="/root/gui/env/server/host" />
	<xsl:variable name="port" select="/root/gui/env/server/port" />
	<xsl:variable name="baseURL" select="concat($protocol,'://',$host,':',$port,/root/gui/url)" />
	<xsl:variable name="serverUrl" select="concat($protocol,'://',$host,':',$port,/root/gui/locService)" />
	<xsl:variable name="showMap" select="/root/gui/config/metadata-show/@showMapPanel" />

	<xsl:template mode="css" match="/">
		<xsl:if test="$currTab!='xml'">
			<xsl:call-template name="geoCssHeader"/>
			<xsl:call-template name="ext-ux-css"/>
		</xsl:if>
	</xsl:template>

	<!--
	additional scripts
	-->
	<xsl:template mode="script" match="/">

		<xsl:call-template name="geoHeader"/>
		<xsl:call-template name="ext-ux"/>

        <xsl:variable name="minimize">
            <xsl:choose>
                <xsl:when test="/root/request/debug">?minimize=false</xsl:when>
                <xsl:otherwise></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <script type="text/javascript" src="{/root/gui/url}/static/gn.search.js{$minimize}"/><xsl:text>&#10;</xsl:text>

        <!-- Editor JS is still required here at least for batch operation -->
        <script type="text/javascript" src="{/root/gui/url}/static/gn.editor.js{$minimize}"/><xsl:text>&#10;</xsl:text>
        <script type="text/javascript" src="{/root/gui/url}/static/gn.libs.map.js{$minimize}"/><xsl:text>&#10;</xsl:text>
        <script type="text/javascript" src="{/root/gui/url}/static/kernel.js{$minimize}"/><xsl:text>&#10;</xsl:text>

		<xsl:variable name="urlWMS"><xsl:copy-of select="/root/request/url"/></xsl:variable>
		<xsl:variable name="typeWMS"><xsl:copy-of select="/root/request/type"/></xsl:variable>


		<xsl:if test="normalize-space($showMap)!='false'">
		<script type="text/javascript">

			function init() {};


			var getIMServiceURL = function(service){
				// FIXME: the "/intermap/" context should be parametrized
				return "/intermap/srv/"+Env.lang+"/"+service;
			};


			Ext.onReady(function(){

				var GNCookie = new Ext.state.CookieProvider({
					expires: new Date(new Date().getTime()+(1000*60*60*24*365))
										//1 year from now
					});

				Ext.state.Manager.setProvider(GNCookie);

				GeoNetwork.MapStateManager.loadMapState();

				initMapViewer();
				var mapViewport =  GeoNetwork.mapViewer.getViewport();

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
								[{region:'center',
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
		</xsl:if>
	</xsl:template>

	<xsl:template name="content">
		<!-- Page content -->
		<div id="content" >
			<xsl:call-template name="pageContent"/>
		</div>
		<xsl:if test="$currTab!='xml'">
			<!-- Map panel -->
			<div id="map_container" style="overflow:hidden; clear: both;">
				<div id="form_wmc" style="display:none"></div>
				<div id="ol_map"></div>
			</div>
		</xsl:if>
	</xsl:template>

	<!--
	page content
	-->
	<xsl:template name="pageContent">
		<xsl:param name="schema">
			<xsl:apply-templates mode="schema" select="."/>
		</xsl:param>

		<table  width="100%" height="100%">
			<xsl:for-each select="/root/*[name(.)!='gui' and name(.)!='request']"> <!-- just one -->
				<tr height="100%">
					<td class="blue-content" width="150" valign="top">
						<xsl:call-template name="tab">
							<xsl:with-param name="tabLink" select="concat(/root/gui/locService,'/metadata.show')"/>
						</xsl:call-template>
					</td>
					<td class="content" valign="top">
						
						<xsl:variable name="md">
							<xsl:apply-templates mode="brief" select="."/>
						</xsl:variable>
						<xsl:variable name="metadata" select="exslt:node-set($md)/*[1]"/>
						<xsl:variable name="mdURL" select="normalize-space(concat($baseURL, '?uuid=', geonet:info/uuid))"/>
						
						<xsl:call-template name="socialBookmarks">
							<xsl:with-param name="baseURL" select="$baseURL" /> <!-- The base URL of the local GeoNetwork site -->
							<xsl:with-param name="mdURL" select="$mdURL" /> <!-- The URL of the metadata using the UUID -->
							<xsl:with-param name="title" select="$metadata/title" />
							<xsl:with-param name="abstract" select="$metadata/abstract" />
						</xsl:call-template>
						
						<table width="100%">
						
							<xsl:variable name="buttons">
								<tr><td class="padded-content" height="100%" align="center" valign="top">
									<xsl:call-template name="buttons">
										<xsl:with-param name="metadata" select="$metadata"/>
										<xsl:with-param name="buttonBarId" select="1"/>
									</xsl:call-template>
								</td></tr>
							</xsl:variable>
							<xsl:if test="$buttons!=''">
								<xsl:copy-of select="$buttons"/>
							</xsl:if>
							<tr>
								<td align="center" valign="left" class="padded-content">
									<table width="100%">
										<tr>
											<td align="left" valign="middle" class="padded-content" height="40">
												<xsl:variable name="source" select="string(geonet:info/source)"/>
												<xsl:choose>
													<!-- //FIXME does not point to baseURL yet-->
													<xsl:when test="/root/gui/sources/record[string(siteid)=$source]">
														<a href="{/root/gui/sources/record[string(siteid)=$source]/baseURL}" target="_blank">
															<img src="{/root/gui/url}/images/logos/{$source}.gif" width="40"/>
														</a>
													</xsl:when>
													<xsl:otherwise>
														<img src="{/root/gui/url}/images/logos/{$source}.gif" width="40"/>
													</xsl:otherwise>
												</xsl:choose>
											</td>
											<td class="padded" width="90%">
												<h1 align="left">
													<xsl:value-of select="$metadata/title"/>
												</h1>
											</td>
											
											<!-- Export links (XML, PDF, ...) -->
											<xsl:if test="(string(geonet:info/isTemplate)!='s')">
										  	<td align="right" class="padded-content" height="16" nowrap="nowrap">
													<xsl:call-template name="showMetadataExportIcons"/>
										  	</td>
											</xsl:if>

										</tr>
									</table>
								</td>
							</tr>
							<!-- subtemplate title button -->
							<xsl:if test="(string(geonet:info/isTemplate)='s')">
								<tr><td class="padded-content" height="100%" align="center" valign="top">
									<b><xsl:value-of select="geonet:info/title"/></b>
								</td></tr>
							</xsl:if>

							<tr><td class="padded-content">
								<table class="md" width="100%">
									<form name="mainForm" accept-charset="UTF-8" method="POST" action="{/root/gui/locService}/metadata.edit">
										<input type="hidden" name="id" value="{geonet:info/id}"/>
										<input type="hidden" name="currTab" value="{/root/gui/currTab}"/>
										
										<xsl:choose>
											<xsl:when test="$currTab='xml'">
												<xsl:apply-templates mode="xmlDocument" select="."/>
											</xsl:when>
											<xsl:otherwise>
												<xsl:apply-templates mode="elementEP" select="."/>
											</xsl:otherwise>
										</xsl:choose>
										
									</form>
								</table>
							</td></tr>
							
							<xsl:if test="$buttons!=''">
								<tr><td class="padded-content" height="100%" align="center" valign="top">
									<xsl:call-template name="buttons">
										<xsl:with-param name="metadata" select="$metadata"/>
										<xsl:with-param name="buttonBarId" select="2"/>
									</xsl:call-template>
								</td></tr>
							</xsl:if>
							
						</table>
					</td>
				</tr>
			</xsl:for-each>
			<tr><td class="blue-content" colspan="3"/></tr>
		</table>
	</xsl:template>

</xsl:stylesheet>
