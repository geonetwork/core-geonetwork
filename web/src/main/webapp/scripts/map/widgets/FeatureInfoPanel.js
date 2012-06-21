/*
 * Copyright (C) 2009 GeoNetwork
 *
 * This file is part of GeoNetwork
 *
 * GeoNetwork is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoNetwork is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoNetwork.  If not, see <http://www.gnu.org/licenses/>.
 */

Ext.namespace('GeoNetwork');

/**
 * Class: GeoNetwork.FeatureInfoPanel
 * FeatureInfoPanel is an Ext.Panel that displays a listview for all
 * the results returned by the FeatureInfo control. When the user clicks
 * on an item the attributes and values will be shown.
 *
 * Inherits from:
 *  - {Ext.Panel}
 */

/**
 * Constructor: GeoNetwork.FeatureInfoPanel
 * Create an instance of GeoNetwork.FeatureInfoPanel
 *
 * Parameters:
 * config - {Object} A config object used to set the featureinfo
 *     panel's properties.
 */
GeoNetwork.FeatureInfoPanel = function(config){
    Ext.apply(this, config);
    GeoNetwork.FeatureInfoPanel.superclass.constructor.call(this);
};

Ext.extend(GeoNetwork.FeatureInfoPanel, Ext.Panel, {

    /**
     * APIProperty: features
     * Array({<OpenLayers.Feature.Vector>}) the features
     *         returned from a GetFeatureInfo response.
     */
    features: null,

    /**
     * APIProperty: treePanel
     * {<Ext.tree.TreePanel>} shows a list of layers
     */
    treePanel: null,

    /**
     * APIProperty: infoPanel
     * {<Ext.Panel>} the panel used from showing the attributes of a feature
     */
    infoPanel: null,

    /**
     * Method: initComponent
     * Initialize this component.
     */
    initComponent: function() {

        GeoNetwork.FeatureInfoPanel.superclass.initComponent.call(this);

        this.layout = 'border';

        this.treePanel = new Ext.tree.TreePanel({rootVisible: true,
            autoScroll: true});

        var root = new Ext.tree.TreeNode({text: OpenLayers.i18n("featureInfoTitle"), 
            draggable:false, expanded: true, cls: 'folder'});
        this.treePanel.setRootNode(root);

        var center = {region: 'center', items: [this.treePanel], split: true,
            minWidth: 100};

        this.infoPanel = new Ext.Panel();
        this.infoPanel.on('render', function() {
            if (this.features) {
                this.showFeatures(this.features);
            }
        }, this);        

        var east = {region: 'east', items: [this.infoPanel], split: true,
            plain: true, cls: 'popup-variant1', width: 400, 
            autoScroll: true};

        this.add(center);
        this.add(east);

        this.doLayout();
    },

    /**
     * APIMethod: setMap
     * Set a reference to the {<OpenLayers.Map>} object
     */
    setMap: function(map) {
        this.map = map;
    },

    /**
     * Method: featureToHTML
     * Create the HTML structure for 1 feature and show this in the infoPanel
     *
     * Parameters:
     * feature - {<OpenLayers.Feature.Vector>}
     */
    featureToHTML: function(feature) {
        var tplstring = '<table class="olFeatureInfoTable" cellspacing="1" ' + 
            'cellpadding="1"><tbody>';
        for (var attr in feature.attributes) {
            if (attr) {
                tplstring += '<tr class="olFeatureInfoRow">' + 
                    '<td width="50%" class="olFeatureInfoColumn">' + attr + 
                    '</td><td width="50%" class="olFeatureInfoValue">' + 
                    feature.attributes[attr] + '</td></tr>';
            }
        }
        tplstring += '</tbody></table>';
        var tpl = new Ext.XTemplate(tplstring);
        tpl.overwrite(this.infoPanel.body, feature);
    },

    /**
     * Method: click
     * When a tree node is clicked, show the attributes of the associated
     * feature.
     *
     * Parameters:
     * node - {<Ext.tree.TreeNode>} the node which was clicked
     */
    click: function(node) {
        if (node.attributes.features.length === 0) {
            var html = '<table class="olFeatureInfoTable" cellpadding="1" ' +
                'cellspacing="1"><tbody>';
            html += '<tr class="olFeatureInfoRow"><td colspan="2" ' +
                'class="olFeatureInfoValue">' +
                OpenLayers.i18n("FeatureInfoNoInfo") +
                '</td></tr>';
            html += '</tbody></table>';
            Ext.DomHelper.overwrite(this.infoPanel.body, html);
        }
        for (var i=0, len = node.attributes.features.length; i<len; i++) {
            var feature = node.attributes.features[i];
            this.featureToHTML(feature);
        }
    },

    /**
     * Method: clearInfoPanel
     * Clear the contents of the info panel
     */
    clearInfoPanel: function() {
        if (this.infoPanel.body) {
            Ext.DomHelper.overwrite(this.infoPanel.body, '');
        }
    },

    /**
     * Function: getLayerTitle
     * Search for the layer title to which a certain featureinfo result belongs
     *
     * Parameters:
     * featureType - {String} The name of the layer / featuretype
     *
     * Returns:
     * {String} The title of the layer if found, otherwise a blank space in HTML
     */
    getLayerTitle: function(featureType) {
        if (featureType == null) return '&nbsp';
        var layers = this.map.getLayersByClass('OpenLayers.Layer.WMS');
        for (var i=0, len=layers.length; i<len; i++) {
            if (layers[i].params.LAYERS instanceof Array) {
                if (OpenLayers.Util.indexOf(layers[i].params.LAYERS, featureType) !== -1) {
                    return layers[i].name;
                }
            } else {
                if (layers[i].params.LAYERS.indexOf(featureType) !== -1) {
                    return layers[i].name;
                }
            }
        }
    },

    /**
     * APIMethod: showFeatures
     * Show the features in the feature info panel, clears any previous 
     *     features.
     *
     * Parameters:
     *     features - Array({<OpenLayers.Feature.Vector>} the features
     *         returned from a GetFeatureInfo response.
     */
    showFeatures: function(features) {
        this.clearInfoPanel();
        var root = this.treePanel.getRootNode();
        while(root.firstChild){
            root.removeChild(root.firstChild);
        }
        // group based on feature.type
        var i, len, featureList = [];
        for (i=0, len=features.length; i<len; i++) {
            var found = false;

            var featureType = "";
            if (features[i].type) {
                featureType = features[i].type;
            } else if (features[i].gml && features[i].gml.featureType) {
                featureType = features[i].gml.featureType;
            }

            for (var j=0; j<featureList.length; j++) {
                if (featureList[j].title === featureType) {
                    featureList[j].features.push(features[i]);
                    found = true;
                }
            }
            if (found === false) {
                featureList.push({title: this.getLayerTitle(featureType), features: [features[i]]});
            }
        }
        for (i=0, len = featureList.length; i<len; i++) {
            // get title based on feature.type
            var node = new Ext.tree.TreeNode({text: featureList[i].title,
                features: featureList[i].features});
            node.addListener("click", this.click, this);
            root.appendChild(node);
            if (i === 0) {
                root.expand();
                this.click(node);
                this.treePanel.getSelectionModel().select(node);
            }
        }
        root.expand();
    }
});

Ext.reg('gn_featureinfo', GeoNetwork.FeatureInfoPanel);
