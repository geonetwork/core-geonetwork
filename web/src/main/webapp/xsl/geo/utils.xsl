<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
				xmlns:xs="http://www.w3.org/2001/XMLSchema"
				exclude-result-prefixes="xs"
				version="2.0">

	<xsl:template name="jsHeader">
		<xsl:param name="small" select="true()"/>

		<script language="JavaScript" type="text/javascript">
			var translations = {
				<xsl:apply-templates select="/root/gui/strings/*[@js='true' and not(*) and not(@id)]" mode="js-translations"/>
			};
		</script>

		<xsl:choose>
			<xsl:when test="/root/request/debug">
				<script type="text/javascript" src="{/root/gui/url}/scripts/prototype.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/geonetwork.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/scriptaculous/scriptaculous.js?load=slider,effects,controls"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/modalbox.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/form_check.js"/><xsl:text>&#10;</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<script type="text/javascript" src="{/root/gui/url}/scripts/lib/gn.libs.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/lib/gn.libs.scriptaculous.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/lib/gn.js"/><xsl:text>&#10;</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="geoCssHeader">
		<link rel="stylesheet" type="text/css" href="{/root/gui/url}/scripts/ext/resources/css/ext-all.css"/>
		<link rel="stylesheet" type="text/css" href="{/root/gui/url}/scripts/ext/resources/css/file-upload.css"/>
		<link rel="stylesheet" type="text/css" href="{/root/gui/url}/scripts/openlayers/theme/geonetwork/style.css"/>
		<link rel="stylesheet" type="text/css" href="{/root/gui/url}/scripts/openlayers/theme/default/style.css"/>
	</xsl:template>

	<!-- Insert all required JS and CSS files:
	* Ext
	* Openlayers
	* GeoExt
	* GeoNetwork specific JS

	If debugging, you should add a debug parameter to your URL in order to load non compressed JS files.
	If changes are made to JS files, jsbuild tool need to be run in order to update JS libs. 
	JS files are compressed using jsbuild tool (see jsbuild directory).
	-->
	<xsl:template name="geoHeader">
		<script type="text/javascript" src="{/root/gui/url}/scripts/ext/adapter/ext/ext-base.js"/><xsl:text>&#10;</xsl:text>
		<script type="text/javascript" src="{/root/gui/url}/scripts/geo/proj4js-compressed.js"/><xsl:text>&#10;</xsl:text>
		<xsl:if test="count(/root/gui/config/map/proj/crs) &gt; 1">
		</xsl:if>
		
		<xsl:choose>
			<xsl:when test="/root/request/debug">
				<link rel="stylesheet" type="text/css" href="{/root/gui/url}/scripts/geoext/resources/css/geoext-all-debug.css"/>
				<script type="text/javascript" src="{/root/gui/url}/scripts/ext/ext-all-debug.js" /><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/ext/form/FileUploadField.js" /><xsl:text>&#10;</xsl:text>
				
				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/OpenLayers.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/lib/OpenLayers/Format/CSWGetRecords.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/lib/OpenLayers/Format/CSWGetRecords/v2_0_2.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/lib/OpenLayers/Lang/nl.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/lib/OpenLayers/Lang/de.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/lib/OpenLayers/Lang/en.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/lib/OpenLayers/Lang/fr.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/lib/OpenLayers/Lang/es.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/lib/OpenLayers/Lang/no.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/lib/OpenLayers/Lang/fi.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/lib/OpenLayers/Lang/it.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/lib/OpenLayers/Lang/tr.js"/><xsl:text>&#10;</xsl:text>
				
				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/addins/LoadingPanel.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/addins/ScaleBar.js"/><xsl:text>&#10;</xsl:text>

				<script type="text/javascript" src="{/root/gui/url}/scripts/geoext/lib/GeoExt.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/mapfish/MapFish.js"/><xsl:text>&#10;</xsl:text>
			</xsl:when>
			<xsl:otherwise>     
				<script type="text/javascript" src="{/root/gui/url}/scripts/ext/ext-all.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/ext/form/FileUploadField.js"/><xsl:text>&#10;</xsl:text>

				<!-- For now using standard OpenLayers.js and GeoExt.js compressed files.  TODO: Change to use gn.geo.libs.js -->  
				<!--script type="text/javascript" src="{/root/gui/url}/scripts/lib/gn.geo.libs.js"></script--> 

				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/OpenLayers.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/lib/OpenLayers/Format/CSWGetRecords.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/lib/OpenLayers/Format/CSWGetRecords/v2_0_2.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/lib/OpenLayers/Lang/nl.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/lib/OpenLayers/Lang/de.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/lib/OpenLayers/Lang/en.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/lib/OpenLayers/Lang/fr.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/lib/OpenLayers/Lang/es.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/lib/OpenLayers/Lang/no.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/lib/OpenLayers/Lang/fi.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/lib/OpenLayers/Lang/it.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/lib/OpenLayers/Lang/tr.js"/><xsl:text>&#10;</xsl:text>

				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/addins/LoadingPanel.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/openlayers/addins/ScaleBar.js"/><xsl:text>&#10;</xsl:text>

				<script type="text/javascript" src="{/root/gui/url}/scripts/geoext/GeoExt.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/mapfish/MapFish.js"/><xsl:text>&#10;</xsl:text>
			</xsl:otherwise>
		</xsl:choose>

		<script type="text/javascript" language="JavaScript1.2">
		// Load layers defined in config file
		var backgroundLayers = [];

		// Map viewer options to use in main map viewer and in editor map viewer
		var mapOptions =  <xsl:value-of select='/root/gui/config/mapViewer/@options'/>;

		<xsl:for-each select="/root/gui/config/mapViewer/layers/layer">
		backgroundLayers.push(["<xsl:value-of select='@tocName'/>","<xsl:value-of select='@server'/>",<xsl:value-of select='@params'/>, <xsl:value-of select='@options'/>]);                           
		</xsl:for-each>

		var backgroundLayersMapSearch = [];
		<xsl:for-each select="/root/gui/config/mapSearch/layers/layer">
		backgroundLayersMapSearch.push(["<xsl:value-of select='@tocName'/>","<xsl:value-of select='@server'/>",<xsl:value-of select='@params'/>, <xsl:value-of select='@options'/>]);
		</xsl:for-each>

		// If no layers defined for search map, use the layers defined for map viewer
		if (backgroundLayersMapSearch.length == 0) backgroundLayersMapSearch = backgroundLayers;
		</script><xsl:text>&#10;</xsl:text>

		<script type="text/javascript" src="{/root/gui/url}/scripts/geo/extentMap.js"/><xsl:text>&#10;</xsl:text>

		<xsl:apply-templates mode="proj4init" select="/root/gui/config/map/proj"/>

		<xsl:call-template name="extentViewerJavascriptInit"/>

		<xsl:call-template name="css"/>
	</xsl:template>

	<xsl:template name="ext-ux-css">
		<link rel="stylesheet" type="text/css" href="{/root/gui/url}/scripts/ext-ux/MultiselectItemSelector-3.0/Multiselect.css"/>
	</xsl:template>

	<xsl:template name="ext-ux">
		<script type="text/javascript" src="{/root/gui/url}/scripts/ext-ux/MultiselectItemSelector-3.0/Multiselect.js"/><xsl:text>&#10;</xsl:text>
		<script type="text/javascript" src="{/root/gui/url}/scripts/ext-ux/MultiselectItemSelector-3.0/DDView.js"/><xsl:text>&#10;</xsl:text>
		<script type="text/javascript" src="{/root/gui/url}/scripts/ext-ux/TwinTriggerComboBox/TwinTriggerComboBox.js"/><xsl:text>&#10;</xsl:text>
		<script type="text/javascript" src="{/root/gui/url}/scripts/map/Ext.ux/form/DateTime.js"/><xsl:text>&#10;</xsl:text>
	</xsl:template>

	<!-- Insert required JS and CSS for Ext selection panel (ie KeywordSelectionPanel) -->
	<xsl:template name="edit-header">
		<xsl:call-template name="ext-ux"/>

		<!-- Load javascript needed for editor in debug mode.
			 If not, they are part of gn.editor.js -->
		<xsl:choose>
			<xsl:when test="/root/request/debug">
				<!-- <script type="text/javascript" src="{/root/gui/url}/scripts/editor/metadata-editor.js"/><xsl:text>&#10;</xsl:text> -->
				<script type="text/javascript" src="{/root/gui/url}/scripts/editor/csw.SearchTools.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/editor/app.SearchField.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/editor/app.KeywordSelectionPanel.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/editor/app.CRSSelectionPanel.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/editor/LogoSelectionPanel.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/editor/app.LinkedMetadataSelectionPanel.js"/><xsl:text>&#10;</xsl:text>
				<script type="text/javascript" src="{/root/gui/url}/scripts/editor/app.GeoPublisherPanel.js"/><xsl:text>&#10;</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<!-- 
					editor libs is already loaded in all page due to lots of dependencies
				<script type="text/javascript" src="{/root/gui/url}/scripts/lib/gn.editor.js"/><xsl:text>&#10;</xsl:text>
				-->
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>

	<xsl:template name="css">
		<link rel="stylesheet" type="text/css" href="{/root/gui/url}/geonetwork_map.css" />

		<style type="text/css">
			.styler {
			background-image:url(<xsl:value-of select="/root/gui/url"/>/images/map/layerstyles.png) !important;
			}
			.drawPolygon {
			background-image:url(<xsl:value-of select="/root/gui/url"/>/images/draw_polygon_off.png) !important;
			}
			.drawCircle {
			background-image:url(<xsl:value-of select="/root/gui/url"/>/images/draw_circle_off.png) !important;
			}
			.drawRectangle {
			background-image:url(<xsl:value-of select="/root/gui/url"/>/images/draw_rectangle_off.png) !important;
			}
			.clearPolygon {
			background-image:url(<xsl:value-of select="/root/gui/url"/>/images/draw_polygon_clear_off.png) !important;
			}
		</style>
	</xsl:template>

	<!-- Create Javascript projection definition. -->
	<xsl:template mode="proj4init" match="proj">
		<script language="JavaScript1.2" type="text/javascript">
			<xsl:for-each select="crs[@def!='']">
				Proj4js.defs["<xsl:value-of select="@code"/>"] = "<xsl:value-of select="@def"/>";                
			</xsl:for-each>
		</script><xsl:text>&#10;</xsl:text>
	</xsl:template>

	<!-- Init all maps. -->
	<xsl:template name="extentViewerJavascriptInit">
		<script language="JavaScript1.2" type="text/javascript">
			if (Ext) {
				Ext.onReady(extentMap.initMapDiv);
			} else {
				Event.observe(window,'load',extentMap.initMapDiv);
			}
		</script>
	</xsl:template>
</xsl:stylesheet>
