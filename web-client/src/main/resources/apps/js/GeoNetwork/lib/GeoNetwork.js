/*
 * Copyright (C) 2001-2011 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 * 
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */
/*
 * The code in this file is based on code taken from OpenLayers.
 *
 * Copyright (c) 2006-2007 MetaCarta, Inc., published under the Clear BSD
 * license.  See http://svn.openlayers.org/trunk/openlayers/license.txt for the
 * full text of the license.
 */
 
(function() {

    /**
     * Check to see if GeoNetwork.singleFile is true. It is true if the
     * GeoNetwork/SingleFile.js is included before this one, as it is
     * the case in single file builds.
     */
    var singleFile = (typeof GeoNetwork == "object" && GeoNetwork.singleFile);

    /**
     * The relative path of this script.
     */
    var scriptName = singleFile ? "GeoNetwork.js" : "lib/GeoNetwork.js";

    /**
     * Function returning the path of this script.
     */
    var getScriptLocation = function() {
        var scriptLocation = "";
        // If we load other scripts right before GeoNetwork using the same
        // mechanism to add script resources dynamically (e.g. OpenLayers), 
        // document.getElementsByTagName will not find the GeoNetwork script tag
        // in FF2. Using document.documentElement.getElementsByTagName instead
        // works around this issue.
        var scripts = document.documentElement.getElementsByTagName('script');
        for(var i=0, len=scripts.length; i<len; i++) {
            var src = scripts[i].getAttribute('src');
            if(src) {
                var index = src.lastIndexOf(scriptName); 
                // set path length for src up to a query string
                var pathLength = src.lastIndexOf('?');
                if(pathLength < 0) {
                    pathLength = src.length;
                }
                // is it found, at the end of the URL?
                if((index > -1) && (index + scriptName.length == pathLength)) {
                    scriptLocation = src.slice(0, pathLength - scriptName.length);
                    break;
                }
            }
        }
        return scriptLocation;
    };

    /**
     * If GeoNetwork.singleFile is false then the JavaScript files in the jsfiles
     * array are autoloaded.
     */
    if(!singleFile) {
        var jsfiles = new Array(
            "GeoNetwork/Util.js",
            "GeoNetwork/Message.js",
            "GeoNetwork/lang/ca.js",
            "GeoNetwork/lang/en.js",
            "GeoNetwork/lang/fr.js",
			"GeoNetwork/lang/nl.js",
			"GeoNetwork/lang/de.js",
			"GeoNetwork/lang/es.js",
			"GeoNetwork/lang/pl.js",
            "GeoNetwork/Catalogue.js",
            "GeoNetwork/util/Old.js",
            "GeoNetwork/util/SearchTools.js",
            "GeoNetwork/util/INSPIRESearchFormTools.js",
            "GeoNetwork/util/SearchFormTools.js",
            "GeoNetwork/util/CSWSearchTools.js",
            "GeoNetwork/data/SubTemplateTypeStore.js",
            "GeoNetwork/util/HelpTools.js",
            "GeoNetwork/util/LinkTools.js",
            "GeoNetwork/data/GroupStore.js",
            "GeoNetwork/data/UserStore.js",
            "GeoNetwork/data/StatusStore.js",
            "GeoNetwork/data/CategoryStore.js",
            "GeoNetwork/data/ValidationRuleStore.js",
            "GeoNetwork/data/LanguageStore.js",
            "GeoNetwork/data/SuggestionStore.js",
            "GeoNetwork/data/ThesaurusFeedStore.js",
            "GeoNetwork/data/RegionStore.js",
            "GeoNetwork/data/CodeListStore.js",
            "GeoNetwork/data/ThesaurusStore.js",
            "GeoNetwork/data/CatalogueSourceStore.js",
            "GeoNetwork/data/HarvesterStore.js",
            "GeoNetwork/data/MetadataThumbnailStore.js",
            "GeoNetwork/data/MetadataResultsStore.js",
            "GeoNetwork/data/MetadataResultsFastStore.js",
            "GeoNetwork/data/MetadataRelationStore.js",
            "GeoNetwork/data/MetadataCSWResultsStore.js",
            "GeoNetwork/data/MetadataSummaryStore.js",
            "GeoNetwork/data/OpenSearchSuggestionReader.js",
            "GeoNetwork/data/OpenSearchSuggestionStore.js",
            "GeoNetwork/form/SearchField.js",
            "GeoNetwork/form/GeometryMapField.js",
            "GeoNetwork/form/OpenSearchSuggestionTextField.js",
            "GeoNetwork/widgets/LoginForm.js",
            "GeoNetwork/widgets/Templates.js",
            "GeoNetwork/widgets/MetadataMenu.js",
            "GeoNetwork/widgets/SearchFormPanel.js",
            "GeoNetwork/widgets/FeedbackForm.js",
            "GeoNetwork/widgets/MetadataResultsView.js",
            "GeoNetwork/widgets/MetadataResultsToolbar.js",
            "GeoNetwork/widgets/FacetsPanel.js",
            "GeoNetwork/widgets/TagCloudView.js",
            "GeoNetwork/widgets/admin/AdminTools.js",
            "GeoNetwork/widgets/admin/MetadataInsertPanel.js",
            "GeoNetwork/widgets/admin/HarvesterPanel.js",
            "GeoNetwork/widgets/admin/SubTemplateManagerPanel.js",
            "GeoNetwork/widgets/admin/ThesaurusManagerPanel.js",
            "GeoNetwork/widgets/view/ViewWindow.js",
            "GeoNetwork/widgets/view/ViewPanel.js",
            "GeoNetwork/widgets/editor/KeywordSelectionPanel.js",
            "GeoNetwork/widgets/editor/ConceptSelectionPanel.js",
            "GeoNetwork/widgets/editor/CRSSelectionPanel.js",
            "GeoNetwork/widgets/editor/SubTemplateSelectionPanel.js",
            "GeoNetwork/widgets/editor/NewMetadataPanel.js",
            "GeoNetwork/widgets/editor/LogoSelectionPanel.js",
            "GeoNetwork/widgets/editor/LinkedMetadataSelectionPanel.js",
            "GeoNetwork/widgets/editor/EditorTools.js",
            "GeoNetwork/widgets/editor/SuggestionsPanel.js",
            "GeoNetwork/widgets/editor/HelpPanel.js",
            "GeoNetwork/widgets/editor/GeoPublisherPanel.js",
            "GeoNetwork/widgets/editor/ThumbnailPanel.js",
            "GeoNetwork/widgets/editor/LinkResourcesWindow.js",
            "GeoNetwork/widgets/editor/LinkedMetadataPanel.js",
            "GeoNetwork/widgets/editor/ValidationPanel.js",
            "GeoNetwork/widgets/editor/EditorPanel.js",
            "GeoNetwork/widgets/editor/EditorToolbar.js",
            "GeoNetwork/widgets/OGCServiceQuickRegister.js",
            "GeoNetwork/map/ExtentMap.js",
            "GeoNetwork/map/core/OGCUtil.js",
            "GeoNetwork/map/core/CatalogueInterface.js",
            "GeoNetwork/map/core/WMCManager.js",
            "GeoNetwork/map/Control/ExtentBox.js",
            "GeoNetwork/map/Control/ZoomWheel.js",
            "GeoNetwork/map/widgets/tree/WMSListGenerator.js",
            "GeoNetwork/map/widgets/tree/WMSTreeGenerator.js",
            "GeoNetwork/map/widgets/wms/BrowserPanel.js",
            "GeoNetwork/map/widgets/wms/LayerInfoPanel.js",
            "GeoNetwork/map/widgets/wms/LayerStylesPanel.js",
            "GeoNetwork/map/widgets/wms/PreviewPanel.js",
            "GeoNetwork/map/widgets/wms/WMSLayerInfo.js",
            "GeoNetwork/map/widgets/FeatureInfoPanel.js",
            "GeoNetwork/map/widgets/LegendPanel.js",
            "GeoNetwork/map/widgets/OpacitySlider.js",
            "GeoNetwork/map/widgets/ProjectionSelector.js",
            "GeoNetwork/map/widgets/TimeSelector.js",
            "GeoNetwork/map/widgets/WxSExtractor.js",
            "GeoNetwork/map/windows/BaseWindow.js",
            "GeoNetwork/map/windows/SingletonWindowManager.js",
            "GeoNetwork/map/windows/AddWMS.js",
            "GeoNetwork/map/windows/FeatureInfo.js",
            "GeoNetwork/map/windows/Opacity.js",
            "GeoNetwork/map/windows/LoadWmc.js",
            "GeoNetwork/map/windows/Disclaimer.js",
            "GeoNetwork/map/windows/LayerStyles.js",
            "GeoNetwork/map/windows/WmsLayerMetadata.js",
            "GeoNetwork/map/windows/WMSTime.js",
            "../../OpenLayers/addins/LoadingPanel.js",
            "../../OpenLayers/addins/ScaleBar.js",
            "../../OpenLayers/addins/Format/CSWGetRecords/v2_0_2_GeoNetwork.js",
            "../../OpenLayers/addins/Format/GeoNetworkRecords.js"
            //"GeoNetwork/map/widgets/PrintAction.js"
        );

        var allScriptTags = new Array(jsfiles.length);

        var host = getScriptLocation() + "lib/";
        for (var i=0, len=jsfiles.length; i<len; i++) {
                allScriptTags[i] = "<script src='" + host + jsfiles[i] +
                                   "'></script>";
        }
        document.write(allScriptTags.join(""));
    }
})();
