/*
 * Copyright (C) 2001-2011 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
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

/** api: (define)
 *  module = GeoNetwork
 *  class = GeoPublisherPanel
 *  base_link = `Ext.FormPanel <http://extjs.com/deploy/dev/docs/?class=Ext.FormPanel>`_
 */

/** api: constructor 
 *  .. class:: GeoPublisherPanel(config)
 * 
 *  Panel for datasets publication in Geoserver nodes.
 *  Datasets could be 
 *  
 *  * a zip file with one ESRI Shapefiles (zip and shapefile file names must be equal)
 *  * a GeoTiff file
 *  * a zip file with a GeoTiff (zip and geotiff file names must be equal) 
 *  * a PostGis table reference eg. jdbc:postgresql://host:port/user:password@database#table
 *  
 *  
 */
GeoNetwork.GeoPublisherPanel = Ext
        .extend(
                Ext.FormPanel,
                {
                    /** api: property[border] 
                     * ``Boolean`` Border
                     */
                    border : false,

                    /** api: property[itemSelector] 
                     * ``String`` 
                     */
                    itemSelector : null,

                    /** api: property[geoserverStore] 
                     * ``XMLStore`` Store GeoServer registered node
                     */
                    geoserverStore : undefined,

                    /** api: property[loadingMask] 
                     * ``LoadingMask`` 
                     */
                    loadingMask : null,

                    /** api: property[metadataId] 
                     * ``Number`` Metadata identifier 
                     */
                    metadataId : null,

                    /** api: property[fileName] 
                     * ``String`` The resource name to publish (file or db url) 
                     */
                    fileName : null,

                    /** api: property[layerName] 
                     * ``String`` Name of the layer. File without extension or table name 
                     */
                    layerName : null,

                    /** api: property[accessStatus] 
                     * ``String`` private/publid 
                     */
                    accessStatus : null,

                    /** api: property[nodeId] 
                     * ``String`` Identifier of GeoServer selected node
                     */
                    nodeId : null,

                    /** api: property[geoPublicationMap] 
                     *  
                     */
                    geoPublicationMap : null,

                    /** api: property[geoPublicationMapPanel] 
                     * ``GeoExt.MapPanel`` Map Panel
                     */
                    geoPublicationMapPanel : null,

                    /** api: property[geoPublicationTb] 
                     */
                    geoPublicationTb : null,
                    /** api: config[layers] 
                     * ``Array`` List of layers to add to the map for background
                     */
                    layers : null,
                    /** api: config[extent] 
                     * ``Array`` Initial map extent
                     */
                    extent : null,
                    /** api: property[stylerBt] 
                     */
                    stylerBt : null,
                    /** api: property[publishBt] 
                     */
                    publishBt : null,
                    /** api: property[unpublishBt] 
                     */
                    unpublishBt : null,
                    /** api: property[checkBt] 
                     */
                    checkBt : null,
                    /** api: property[addOnLineSourceBt] 
                     */
                    addOnLineSourceBt : null,
                    /** api: property[enableStyler] 
                     */
                    enableStyler : false,
                    /** api: property[statusBar] 
                     */
                    statusBar : null,

                    /** api: property[layerPreviewName] 
                     * ``String`` Name of the preview layer
                     */
                    layerPreviewName : 'DatasetPreview',
                    /** private: method[constructor]
                     * 
                     * 
                     *  Initializes the panel
                     *  
                     *  
                     */
                    initComponent : function() {

                        var record = Ext.data.Record.create( [ {
                            name : 'id'
                        }, {
                            name : 'name'
                        }, {
                            name : 'adminUrl'
                        }, {
                            name : 'wmsUrl'
                        }, {
                            name : 'wfsUrl'
                        }, {
                            name : 'stylerUrl'
                        }, {
                            name : 'namespacePrefix'
                        } ]);

                        this.geoserverStore = new Ext.data.Store( {
                            proxy : new Ext.data.HttpProxy( {
                                url : 'geoserver.publisher?action=LIST',
                                method : 'GET'
                            }),
                            baseParams : {
                                action : 'LIST'
                            },
                            reader : new Ext.data.XmlReader( {
                                record : 'node',
                                id : 'id'
                            }, record),
                            sortInfo : {
                                field : 'name'
                            }
                        });

                        /**
                         * Publish current file in remote node.
                         */
                        this.publishBt = new Ext.Button(
                                {
                                    text : translate('publish'),
                                    tooltip : translate('publishTooltip'),
                                    iconCls : 'addVector',
                                    handler : function() {
                                        this.statusBar.setText('');
                                        this.loadingMask.show();

                                        Ext.Ajax
                                                .request( {
                                                    url : 'geoserver.publisher',
                                                    params : {
                                                        metadataId : this.metadataId,
                                                        nodeId : this.nodeId,
                                                        file : this.fileName,
                                                        access : this.accessStatus,
                                                        action : 'CREATE'
                                                    },
                                                    method : 'GET',
                                                    success : function(result,
                                                            request) {
                                                        // Check exceptions
                                                        //  * In case of GeoNetwork OutOfMemoryError (could happen on big file during copy)
                                                        if (result.responseText
                                                                .indexOf('OutOfMemoryError') != -1) {
                                                            this.statusBar
                                                                    .setText(translate('publishError')
                                                                            + translate('publishErrorCode')
                                                                            + ' OutOfMemoryError.');
                                                            this.loadingMask
                                                                    .hide();
                                                            return;
                                                        }

                                                        //	* In case of GeoServer REST error message						
                                                        var root = result.responseXML
                                                                .getElementsByTagName(
                                                                        'Exception')
                                                                .item(0);
                                                        if (root != null) {
                                                            this.statusBar
                                                                    .setText(translate('publishError')
                                                                            + translate('publishErrorCode')
                                                                            + root
                                                                                    .getAttribute('status'));
                                                            this.loadingMask
                                                                    .hide();
                                                            return;
                                                        }

                                                        this.statusBar
                                                                .setText(translate('publishSuccess'));
                                                        this
                                                                .updatePrivileges(1);
                                                        this
                                                                .addLayer(
                                                                        this.layerPreviewName,
                                                                        this.layerName);
                                                        this.statusBar
                                                                .setText(this.statusBar.text
                                                                        + translate('publishLayerAdded'));
                                                        this.loadingMask.hide();
                                                    },
                                                    failure : function(result,
                                                            request) {
                                                        this.statusBar
                                                                .setText(translate('publishError'));
                                                        this.loadingMask.hide();
                                                    },
                                                    scope : this
                                                });
                                    },
                                    scope : this
                                });

                        /**
                         * Unpublish current file.
                         */
                        this.unpublishBt = new Ext.Button(
                                {
                                    text : translate('unpublish'),
                                    tooltip : translate('unpublishTooltip'),
                                    iconCls : 'delVector',
                                    handler : function() {
                                        this.statusBar.setText('');
                                        this.cleanLayerPreview();

                                        Ext.Ajax
                                                .request( {
                                                    url : 'geoserver.publisher',
                                                    params : {
                                                        metadataId : this.metadataId,
                                                        nodeId : this.nodeId,
                                                        file : this.fileName,
                                                        access : this.accessStatus,
                                                        action : 'DELETE'
                                                    },
                                                    method : 'GET',
                                                    success : function(result,
                                                            request) {
                                                        this.statusBar
                                                                .setText(translate('unpublishSuccess'));
                                                        this
                                                                .updatePrivileges(0);
                                                    },
                                                    failure : function(result,
                                                            request) {
                                                        this.statusBar
                                                                .setText(translate('unpublishError'));
                                                    },
                                                    scope : this
                                                });
                                    },
                                    scope : this
                                });

                        /**
                         * Check current file is already published in remote node.
                         */
                        this.checkBt = new Ext.Button(
                                {
                                    text : translate('check'),
                                    iconCls : 'connect',
                                    listeners : {
                                        click : function() {
                                            this.statusBar.setText('');

                                            Ext.Ajax
                                                    .request( {
                                                        url : 'geoserver.publisher',
                                                        params : {
                                                            metadataId : this.metadataId,
                                                            nodeId : this.nodeId,
                                                            file : this.fileName,
                                                            access : this.accessStatus,
                                                            action : 'GET'
                                                        },
                                                        method : 'GET',
                                                        success : function(
                                                                result, request) {
                                                            // Return error message according to exception
                                                            var root = result.responseXML
                                                                    .getElementsByTagName(
                                                                            'Exception')
                                                                    .item(0);
                                                            if (root != null) {
                                                                var status = root
                                                                        .getAttribute('status');
                                                                this.statusBar
                                                                        .setText(translate('errorDatasetNotFound')
                                                                                + status);
                                                                if (status
                                                                        .indexOf('No vector layer') != -1
                                                                        || status
                                                                                .indexOf('404') != -1) {
                                                                    this
                                                                            .updatePrivileges(0);
                                                                } else {
                                                                    this
                                                                            .updatePrivileges();
                                                                }
                                                                return;
                                                            }

                                                            this.statusBar
                                                                    .setText(translate('datasetFound'));
                                                            this
                                                                    .updatePrivileges(1);
                                                            this
                                                                    .addLayer(
                                                                            this.layerPreviewName,
                                                                            this.layerName);
                                                        },
                                                        failure : function(
                                                                result, request) {
                                                            this.statusBar
                                                                    .setText(translate('checkFailure'));
                                                            this
                                                                    .updatePrivileges();
                                                        },
                                                        scope : this
                                                    });
                                        },
                                        scope : this
                                    }
                                });

                        /**
                         * Add online source information to current metadata record.
                         */
                        this.addOnLineSourceBt = new Ext.Button( {
                            text : translate('addOnlineSource'),
                            iconCls : 'processMetadata',
                            tooltip : '',
                            handler : function() {
                                var node = this.geoserverStore
                                        .getById(this.nodeId);
                                this.fireEvent('addOnLineSource', this, node
                                        .get('wmsUrl'), node
                                        .get('namespacePrefix')
                                        + ":" + this.layerName, '');
                            },
                            scope : this
                        });

                        /**
                         * Add online source information to current metadata record.
                         */
                        this.stylerBt = new Ext.Button(
                                {
                                    text : translate('Style'),
                                    iconCls : 'styler',
                                    id : 'stylerBt',
                                    tooltip : '',
                                    handler : function() {
                                        var node = this.geoserverStore
                                                .getById(this.nodeId);
                                        var url = node.get('stylerUrl')
                                                + '?namespace='
                                                + node.get('namespacePrefix')
                                                + '&layer='
                                                + node.get('namespacePrefix')
                                                + ':' + this.layerName;
                                        window
                                                .open(url, '',
                                                        'toolbar=no,menubar=no,width=600,height=500');
                                    },
                                    scope : this
                                });

                        this.geoPublicationTb = new Ext.Toolbar( {
                            items : [ this.getGeoserverCombo(), this.checkBt,
                                    this.publishBt, this.unpublishBt,
                                    this.addOnLineSourceBt, this.stylerBt ]
                        });

                        this.statusBar = new Ext.form.Label( {
                            id : 'statusBar',
                            html : translate('statusInformation')
                        });

                        var tb = new Ext.Toolbar( {
                            items : [ this.statusBar ]
                        });

                        this.geoserverStore.load( {
                            add : false
                        });
                        this.tbar = this.geoPublicationTb;
                        this.bbar = tb;
                        this.items = this.getGeoPublicationMapPanel();

                        /**
                         * triggered when the user has published a dataset
                         */
                        this.addEvents('addOnLineSource');
                        GeoNetwork.GeoPublisherPanel.superclass.initComponent
                                .call(this);

                        this.on('resize', function(el, w, h) {
                            el.geoPublicationMapPanel.setSize(w, h);
                        });

                        // add mask when action publish which could take some time
                        // according to zip file size.
                        if (!this.loadingMask) {
                            this.loadingMask = new Ext.LoadMask(Ext.getBody(), // TODO : maybe restrict loading mask to panel ? 
                                    {
                                        msg : translate('publishing')
                                    });
                        }
                    },

                    /** private: method[getGeoPublicationMapPanel]
                     *  
                     *  Create a map panel to have a quick preview of the published datasets
                     */
                    getGeoPublicationMapPanel : function() {

                        var map = new OpenLayers.Map();

                        var layers = [];
                        for ( var i = 0; i < backgroundLayers.length; i++) {
                            var layer = new OpenLayers.Layer.WMS(
                                    backgroundLayers[i][0],
                                    backgroundLayers[i][1],
                                    backgroundLayers[i][2],
                                    backgroundLayers[i][3])
                            map.addLayer(layer);
                        }

                        return this.geoPublicationMapPanel = new GeoExt.MapPanel(
                                {
                                    id : 'mapPanel',
                                    extent : this.extent,
                                    //title : translate('mapPreview'),
                                    map : map,
                                    width : this.width,
                                    height : this.height
                                //layers : layers
                                });
                    },

                    /** private: method[addLayer]
                     *  
                     *  Add a layer to the map panel
                     */
                    addLayer : function(title, name) {
                        // Try to display layer on map preview
                        var map = Ext.getCmp('mapPanel').map;
                        this.cleanLayerPreview();
                        var layer = new OpenLayers.Layer.WMS(title,
                                this.geoserverStore.getById(this.nodeId).get(
                                        'wmsUrl'), {
                                    transparent : 'true',
                                    layers : name
                                });

                        map.addLayer(layer);
                    },

                    /** private: method[cleanLayerPreview]
                     *  
                     *  Remove the preview layer from the map panel
                     */
                    cleanLayerPreview : function() {
                        var map = Ext.getCmp('mapPanel').map;
                        var currentLayers = map
                                .getLayersByName(this.layerPreviewName);
                        if (currentLayers.length > 0) {
                            currentLayers[0].destroy();
                        }
                    },

                    /** private: method[getGeoserverCombo]
                     * 
                     *  Create a combo with the list of geoserver node registerd.
                     *  
                     *  When selecting a node:
                     *  * check current server status (TODO).
                     *  * check current dataset is already published, assume
                     *  featuretype name = dataset name (which is the case when
                     *  dataset published using this interface).
                     */
                    getGeoserverCombo : function() {
                        return {
                            xtype : 'combo',
                            tpl : '<tpl for="."><div ext:qtip="{name}. {adminUrl}" class="x-combo-list-item">{name}</div></tpl>',
                            store : this.geoserverStore,
                            displayField : 'name',
                            autoSelect : true,
                            forceSelection : true,
                            mode : 'local',
                            triggerAction : 'all',
                            emptyText : translate('selectANode'),
                            id : 'geoserverNode',
                            listeners : {
                                select : function(combo, record, index) {
                                    this.nodeId = record.get('id');
                                    this.cleanLayerPreview();
                                    this.enableStyler = (record
                                            .get('stylerUrl') == '' ? false
                                            : true);
                                    this.checkBt.fireEvent('click');

                                    // TODO : check nodeId is up
                                },
                                scope : this
                            }
                        };
                    },

                    /** api: method[setRef]
                     * 
                     *  Set the element reference : metadata
                     *  identifier and file name.
                     */
                    setRef : function(id, fileName, accessStatus) {
                        this.metadataId = id;
                        this.fileName = fileName;
                        if (this.fileName.indexOf('jdbc') != -1) // In case of db
                            this.layerName = this.fileName.substr(this.fileName
                                    .indexOf('#') + 1, this.fileName.length);
                        else
                            this.layerName = this.fileName.substr(0,
                                    this.fileName.indexOf('.'));
                        this.accessStatus = accessStatus;
                        this.cleanLayerPreview();
                        this.updatePrivileges();
                        if (this.nodeId != null)
                            this.checkBt.fireEvent('click');
                    },
                    /** private: method[updatePrivileges]
                     * 
                     *  step=null Check only available.
                     *  step=0 File checked and not found, activate publish action only
                     *  step=1 File already published, all actions allowed
                     */
                    updatePrivileges : function(step) {
                        if (this.nodeId == null)
                            this.checkBt.disable();
                        else
                            this.checkBt.enable();
                        this.publishBt.disable();
                        this.unpublishBt.disable();
                        this.addOnLineSourceBt.disable();
                        this.stylerBt.disable();

                        switch (step) {
                        case 0:
                            this.publishBt.enable();
                            break;
                        case 1:
                            this.publishBt.enable();
                            this.unpublishBt.enable();
                            this.addOnLineSourceBt.enable();

                            if (this.fileName.indexOf('tif') != -1
                                    || !this.enableStyler)
                                this.stylerBt.disable();
                            else
                                this.stylerBt.enable();
                            break;
                        }
                    }

                });
/** api: xtype = gn_geopublihserpanel */
Ext.reg('gn_geopublihserpanel', GeoNetwork.GeoPublisherPanel);
