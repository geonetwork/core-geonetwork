<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <!--
    main mapfish includes
    -->
    <xsl:template name="mapfish_script_includes">
        <script type="text/javascript" src="{/root/gui/url}/scripts/lib/gn.libs.scriptaculous.js"></script>
        <script type="text/javascript" src="{/root/gui/url}/scripts/lib/gn.js"></script>
    
         <xsl:choose>
            <xsl:when test="/root/request/debug">           

                <script type="text/javascript" src="{/root/gui/url}/scripts/gn_search.js"></script>
                
                <!--link rel="stylesheet" type="text/css" href="{/root/gui/url}/scripts/ext/resources/css/ext-all.css" />
                <link rel="stylesheet" type="text/css" href="{/root/gui/url}/scripts/ext/resources/css/file-upload.css" />

                <link rel="stylesheet" type="text/css" href="{/root/gui/url}/scripts/openlayers/theme/default/style.css" />
                <link rel="stylesheet" type="text/css" href="{/root/gui/url}/geonetwork_map.css" /-->
         
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/core/OGCUtil.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/core/MapStateManager.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/core/CatalogueInterface.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/core/WMCManager.js"></script>

                <script type="text/javascript" src="{/root/gui/url}/scripts/map/Control/ExtentBox.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/Control/ZoomWheel.js"></script>

                <script type="text/javascript" src="{/root/gui/url}/scripts/map/lang/de.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/lang/en.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/lang/es.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/lang/fr.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/lang/nl.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/lang/no.js"></script>

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
                <script type="text/javascript" src="{/root/gui/url}/scripts/map/windows/Disclaimer.js"></script>

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
                <script type="text/javascript" src="{/root/gui/url}/scripts/lib/gn.search.js"></script>

                <!-- Editor JS is still required here at least for batch operation -->
                <script type="text/javascript" src="{/root/gui/url}/scripts/lib/gn.editor.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/lib/gn.libs.map.js"></script>              
            </xsl:otherwise>
         </xsl:choose>
            
            
        <script type="text/javascript" src="{/root/gui/url}/scripts/core/kernel/kernel.js"></script>
        <script type="text/javascript"
                src="{/root/gui/url}/scripts/mapfishIntegration/proj4js-compressed.js"/>
        <script type="text/javascript"
                src="{/root/gui/url}/scripts/mapfishIntegration/MapComponent.js"/>
        <script type="text/javascript"
                src="{/root/gui/url}/scripts/mapfishIntegration/MapDrawComponent.js"/>
        <script type="text/javascript"
                src="{/root/gui/url}/scripts/mapfishIntegration/Ext.ux.BoxSelect.js"/>
        <script type="text/javascript">
            OpenLayers.Lang.setCode('<xsl:value-of select="/root/gui/strings/language"/>');
            Proj4js.defs["EPSG:21781"] = "+title=CH1903 / LV03 +proj=somerc +lat_0=46.95240555555556 +lon_0=7.439583333333333 +x_0=600000 +y_0=200000 +ellps=bessel +towgs84=674.374,15.056,405.346,0,0,0,0 +units=m +no_defs";
        </script>
    
    </xsl:template>
    <xsl:template name="mapfish_css_includes">
        <link rel="stylesheet" type="text/css"
              href="{/root/gui/url}/scripts/mapfishIntegration/boxselect.css"/>
        <link href="{/root/gui/url}/print.css" type="text/css" rel="stylesheet" media="print"/>
        <link href="{/root/gui/url}/scripts/mapfish/mapfish.css" type="text/css" rel="stylesheet"/>
        <style type="text/css">
            .olControlAttribution {
              left: 5px !important;
              bottom: 5px !important;
            }
            .olControlAttribution a {
              padding: 2px;
            }

            .float-left {
              float: left;
            }
            .clear-left {
              clear: left;
            }
            .zoomin {
              background-image:url(<xsl:value-of select="/root/gui/url"/>/scripts/mapfish/img/icon_zoomin.png) !important;
              height:20px !important;
              width:20px !important;
            }
            .zoomout {
              background-image:url(<xsl:value-of select="/root/gui/url"/>/scripts/mapfish/img/icon_zoomout.png) !important;
              height:20px !important;
              width:20px !important;
            }
            .zoomfull {
              background-image:url(<xsl:value-of select="/root/gui/url"/>/scripts/mapfish/img/icon_zoomfull.png) !important;
              height:20px !important;
              width:20px !important;
            }
            .pan {
              background-image:url(<xsl:value-of select="/root/gui/url"/>/scripts/mapfish/img/icon_pan.png) !important;
              height:20px !important;
              width:20px !important;
            }
            .selectBbox {
              background-image:url(<xsl:value-of select="/root/gui/url"/>/scripts/mapfish/img/draw_polygon_off.png) !important;
              height:20px !important;
              width:20px !important;
            }
            .drawPolygon {
              background-image:url(<xsl:value-of select="/root/gui/url"/>/scripts/mapfish/img/draw_polygon_off.png) !important;
              height:20px !important;
              width:20px !important;
            }
            .drawRectangle {
              background-image:url(<xsl:value-of select="/root/gui/url"/>/images/draw_rectangle_off.png) !important;
              height:20px !important;
              width:20px !important;
            }
            .clearPolygon {
              background-image:url(<xsl:value-of select="/root/gui/url"/>/scripts/mapfish/img/draw_polygon_clear_off.png) !important;
              height:20px !important;
              width:20px !important;
            }
            .layerTreeButton {
              background-image:url(<xsl:value-of select="/root/gui/url"/>/images/layers.png) !important;
              height:20px !important;
              width:20px !important;
            }
            .compressedFieldSet {
              padding-top: 0;
              padding-bottom: 2px;
              margin-bottom: 2px;
            }
            .compressedFormItem {
              margin-bottom: 0;
            }
            .compressedFormItem div {
              padding-top: 0;
            }
            .compressedFormItem label {
              padding: 0;
            }
            .simpleFormFieldset {
                border-style: none;
            }
            .vCenteredColumn div.x-form-item {
              position: absolute;
              top: 60px;
            }
            .uriButtons li {
              display: block;
              float: left;
              list-style-type: none;
              padding-right: 20px;
            }
            fieldset#featured {
              margin: 5px;
              float: right;
            }
            fieldset#latestUpdates {
              margin-top: 2em;
              float: left;
            }

            .mf-email-pdf-action {
              background-image:url(<xsl:value-of select="/root/gui/url"/>/images/emailPDF.png) !important;
            }
        </style>
    </xsl:template>

</xsl:stylesheet>
