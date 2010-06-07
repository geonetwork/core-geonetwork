Ext.namespace("app");

/**
 * Class: app.FeatureSelectionPanel
 *
 * Provides an Ext.Panel with 2 regions:
 *  - a mappanel
 *  - a FeatureSelectionForm (see below)
 */

app.FeatureSelectionPanel = Ext.extend(Ext.Panel, {
    border: false,

    /**
     * APIProperty: mappanel
     * The mappanel item (has a valid value as soon as the mappanel is rendered
     */
    mappanel: null,

    /**
     * APIProperty: serviceUrl
     * The URL of the server
     * It is intended to expose WFS and WMS services
     */
    serviceUrl: null,

    /**
     * APIProperty: featureTypes
     * {Array} the list of featureTypes we should be able to send a getFeature
     *     on, if not provided, should be populated with the list of
     *     featureTypes given by a WFS GetCapabilities request
     */
    featureTypes: null,

    /**
     * APIProperty: overlayLayer
     */
    overlayLayer: null,

    /**
     * APIProperty: featureTypeStore
     * {<Ext.data.JsonStore>} the list of featureTypes. Should be populated
     *      with a WFS getCapabilities request
     */
    featureTypeStore: new Ext.data.JsonStore({
        root: 'featureTypes',
        fields: ['name', 'title']
    }),

    /**
     * Property: eventProtocol
     * {<mapfish.Protocol.TriggerEventDecorator>}
     */
    eventProtocol: null,

    /*
     * {<OpenLayers.Protocol.WFS>} WFS protocol shared between the eventProtocol
     *     (TriggerEventDecorator for the mapSearcher) and the form searcher
     */
    wfsProtocol: null,

    /**
     * APIProperty: mapSearcher
     * {<mapfish.Searcher.Map>}
     */
    mapSearcher: null,

    /**
     * APIProperty: searchCombo
     * {<Ext.form.Combobox>} the autocomplete text searching combobox
     */
    searchCombo: null,

    /**
     * APIProperty: popup
     * {<GeoExt.Popup>}
     */
    popup: null,

    /**
     * APIProperty: popupCounterTpl
     */
    popupCounterTpl: new Ext.XTemplate('{index} sur {total}'),

    /**
     * Property: statusbar
     */
    statusbar: null,

    defaults: {
        border: false
    },

    initComponent: function() {
        this.layout = 'border';

        this.items = [
            this.createMapPanel(),
            this.createForm()
        ];

        this.addEvents(
            /**
             * triggered when the user has selected a feature
             * using the selectFeature button
             */
            'featureselected'
        );

        this.bbar = this.statusbar = new Ext.StatusBar({});

        app.FeatureSelectionPanel.superclass.initComponent.call(this);

        this.getFeatureTypes();
    },

    createMap: function() {

        this.map = new OpenLayers.Map(null, options);
        this.map.addLayers([metacarta, this.overlayLayer]);
    },

    /**
     * APIMethod: createMapPanel
     * Return the map panel
     */
    createMapPanel: function() {

//        this.createMap();

//        var layer = new OpenLayers.Layer.WMS( "OpenLayers WMS",
//            "http://labs.metacarta.com/wms/vmap0",
//            {layers: 'basic,priroad,secroad,rail,tunnel,bridge,trail,ctylabel'},
//            {
//                opacity: 0.5
//            }
//        );
        var layer = new OpenLayers.Layer.WMS(
                "Blue Marble",
                "http://sigma.openplans.org/geoserver/wms?",
                {layers: "bluemarble"}
            );
        
        this.overlayLayer = new OpenLayers.Layer.WMS("overlay",
            this.serviceUrl,
            {
                layers: [],
                transparent: true
            },{
                singleTile: true,
                ratio: 1.5
            }
        );

        var options = {
//            maxExtent: new OpenLayers.Bounds(
//                1.41449,47.789154,3.562317,49.593658
//            ),
//            maxResolution: 0.010986328125,
//            numZoomLevels: 7,
            allOverlays: false
//            controls: [
//                    new OpenLayers.Control.Navigation(),
//                    new OpenLayers.Control.PanZoomBar()
//            ]
        };

        var config = {
            xtype: "gx_mappanel",
            id: 'mappanel_' + this.id,
            region: 'center',
            map: options,
            layers: [layer, this.overlayLayer],
            //extent: "1.41449,47.789154,3.562317,49.593658",
            listeners: {
                render: function(panel) {
                    this.mappanel = panel;
                },
                scope: this
            }
        };

        return config;
    },

    getFeatureTypes: function() {
        var success = function(request) {
            var parser = new OpenLayers.Format.WFSCapabilities();
            var capabilities = parser.read(request.responseText);

            var data = capabilities.featureTypeList;

            this.featureTypeStore.loadData(data);
        };

        var failure = function(request) {/* */};

        Ext.Ajax.request({
            url: this.serviceUrl,
            success: success,
            failure: failure,
            method: 'GET',
            params: { service: 'WFS', request: 'getCapabilities' },
            scope: this
        });
    },

    createForm: function() {
        this.featureTypesCombo = new Ext.form.ComboBox({
            fieldLabel: 'Sélectionnez une couche', // TODO i18n
            store: this.featureTypeStore,
            valueField: 'name',
            displayField: 'title',
            mode: 'local',
            triggerAction: 'all',
            editable: false,
            anchor: '100%',
            listeners: {
                select: function(combo, record) {
                    var featureType = record.get('name');
                    this.createProtocol(featureType);
                    this.setMapSearcher(featureType);
                    this.setSearchComboConfig(featureType);
                    this.searchCombo.enable();
                    this.overlayLayer.params['LAYERS'] = featureType;
                    this.overlayLayer.redraw(true);

                },
                scope: this
            }
        });

        this.createSearchCombo();

        return {
            xtype: 'form',
            region: 'east',
            width: 200,
            labelAlign: 'top',
            bodyStyle: 'padding:4px;',
            items: [
                this.featureTypesCombo,
                this.searchCombo
            ]
        }
    },

    createProtocol: function(featureType) {
        if (this.eventProtocol) {
            return this.eventProtocol;
        }

        this.wfsProtocol = new OpenLayers.Protocol.WFS({
            url: this.serviceUrl,
            featureType: featureType,
            featurePrefix: 'ms', // it seems like this prefix is MapServer specfic
            srsName: this.mappanel.map.getProjectionObject().getCode(),
            version: "1.1.0"
        });

        this.eventProtocol = new mapfish.Protocol.TriggerEventDecorator({
            protocol: this.wfsProtocol
        });

        this.eventProtocol.events.on({
            "crudtriggered": function() {
                if (this.popup) {
                    this.popup.close();
                }
                this.statusbar.showBusy();
                this.statusbar.setStatus({text: "Recherche"});	// TODO i18n
            },
            scope: this
        });
    },

    createSearchCombo: function() {
        this.searchCombo = new Ext.form.ComboBox({
            //fieldLabel: this.fieldLabel,
            //name: this.displayField,
            mode: 'remote',
            minChars: 2,
            typeAhead: true,
            forceSelection: true,
            hideTrigger: true,
            displayField: 'nom', //this.displayField,
            fieldLabel: 'Saisissez un nom ci-dessous ou cliquez sur la carte pour sélectionner une entité', // TODO i18n
            anchor: '100%',
            disabled: true,
            listeners: {
                select : function(combo, record, index) {
                    this.displayPopup([record.data.feature]);
                },
                scope: this
            }
        });

    },

    setSearchComboConfig: function(featureType) {
        var store = new GeoExt.data.FeatureStore({
            fields: [
                'nom'  // this should be dynamic
            ],
            proxy: new GeoExt.data.ProtocolProxy({
                protocol: this.wfsProtocol
            })
        });

        // add a filter to the options passed to proxy.load, proxy.load
        // itself passes these options to protocol.read
        store.on({
            beforeload: function(store, options) {
                options.filter = new OpenLayers.Filter.Comparison({
                    type: OpenLayers.Filter.Comparison.LIKE,
                    property: 'nom',
                    value: '*' + this.searchCombo.getValue()  +'*'
                    /*,
                    matchCase: false // this is currently unsupported in OL/Filter/Comparison.js
                    */
                });
                delete store.baseParams.query;
            },
            scope: this
        });

        // change the combo store (bindStore is not API however it seems
        // to be exactly what I'm looking for
        // http://extjs.com/forum/showthread.php?p=187660#post187660
        this.searchCombo.bindStore(null);
        this.searchCombo.bindStore(store, true);

    },

    setMapSearcher: function(featureType) {
        if (!this.mapSearcher) {
            this.mapSearcher = new mapfish.Searcher.Map({
                mode: mapfish.Searcher.Map.CLICK,
                protocol: this.eventProtocol ,
                displayDefaultPopup: true,
                displayPopup: OpenLayers.Function.bind(
                    function(response) {
                        var features = response.features;
                        this.displayPopup(features)

                    },
                    this
                ),
                searchTolerance:5,
                pointAsBBOX: true
            });
            this.mappanel.map.addControl(this.mapSearcher);
            this.mapSearcher.activate();
        } else {
            // only update the protocol options
            this.wfsProtocol.format.featureType = featureType;
            this.wfsProtocol.options.featureType = featureType;
        }
    },

    displayPopup: function(features) {
        this.statusbar.clearStatus();

        if (features && features.length > 0) {
            var nbFeatures = features.length;

            var items = [];

            for (var i = 0; i < nbFeatures; i++) {
                var feature = features[i];
                if (!feature.fid) {
                    // a fid is required here
                    // check the WFS server configuration if possible
                    // in MapServer, a valid "gml_featureid" should be given
                    OpenLayers.Console.error("no fid provided for the feature");
                    continue;
                }

                var typename = feature.fid.split(".")[0];
                //var title = typename.toUpperCase();
                var store = this.featureTypeStore;
                var title = store.getAt(store.find('name', typename)).get('title');

                var templateString = '<h3 class="popup_title">' + title + '</h3>';

                for (var k in feature.attributes) {
                    if (k != 'boundedBy' && k != 'msGeometry') {
                        templateString += '<div>' +
                                          '<b>' + k.toUpperCase() + ' : </b>' +
                                          '{' + k + '}' +
                                          '</div>'
                    }
                }

                var tpl = new Ext.Template(templateString);

                var handler =function(feature) {
                    this.fireEvent('featureselected', this, feature);
                };

                items.push({
                    id: 'popup-card-' + i,
                    border: false,
                    bodyStyle: 'padding:0.5em',
                    html: tpl.apply(feature.data),
                    buttons: [{
                        text: translate('selectFeature'),
                        handler: handler.createDelegate(this, [feature])
                    }]
                });
            }

            var bbar = false;

            // counter for the popup bbar (ie. "2 of 12")
            var counter = new Ext.Toolbar.TextItem(
                [
                    '<span id="',
                    this.getId() +  '_popup_counter',
                    '">',
                    this.popupCounterTpl.apply({index: 1, total: nbFeatures}),
                    '</span>'
                ].join('')
            );

            if (nbFeatures > 1) {
                var bbar = [
                    '->',
                    counter,
                    {
                        id: this.getId() + '_popup_move_prev',
                        iconCls: "x-tbar-page-prev",
                        handler: this.popupNavHandler.createDelegate(this, [-1, nbFeatures]),
                        disabled: true,
                        listeners: {
                            click: function(button, e) {
                                e.stopEvent();
                            }
                        }
                    },
                    {
                        id: this.getId() + '_popup_move_next',
                        iconCls: "x-tbar-page-next",
                        handler: this.popupNavHandler.createDelegate(this, [1, nbFeatures]),
                        listeners: {
                            click: function(button, e) {
                                e.stopEvent();
                            }
                        }
                    }
                ]
            }

            this.popup = new GeoExt.Popup({
                feature: features[0],
                /*
                lonlat: this.mapSearcher.popupLonLat,
                */
                closable: true,
                header: false,
                unpinnable: false,
                border: false,
                width:230,
                layout: 'card',
                activeItem: 0,
                items: items,
                bbar: bbar,
                resizable: false
            });

            this.mappanel.add(this.popup);

            // TODO clean those events managers
            Ext.EventManager.on(this.popup.getEl(), 'click', function(e) {
                e.stopEvent();
            });
            Ext.EventManager.on(this.popup.getEl(), 'dblclick', function(e) {
                e.stopEvent();
            });

            // closes the popup if user clicks anywhere
            // this doesn't work for the map
            Ext.getDoc().on('mousedown', this.closePopupIf, this);

            // this works for the map but not for the panZoomBarControls
            //Ext.getDoc().on('click', this.closePopupIf, this);

        } else {
            this.statusbar.setStatus({
                text: "Aucun résultat trouvé"
            });
        }
    },

    /**
     * Method: closePopupIf
     * Closes the popup if passed argument (event) doesn't concern the popup itself
     * Parameters:
     * {<Ext.Event>}
     */
    closePopupIf: function(e) {
        if(this.popup && !e.within(this.popup)){
            this.popup.close();
        }
    },

    /*
     * Method: popupNavHandler
     * Allows to navigate between features (next, previous) in the popup
     */
    popupNavHandler: function(direction, total) {
        var lay = this.popup.getLayout();
        var i = lay.activeItem.id.split('popup-card-')[1];
        var next = parseInt(i) + direction;
        lay.setActiveItem(next);
        this.popupCounterTpl.overwrite(Ext.get(this.getId() + '_popup_counter'), {
            index: next + 1, total: total
        });
        Ext.getCmp(this.getId() + '_popup_move_prev').setDisabled(next==0);
        Ext.getCmp(this.getId() + '_popup_move_next').setDisabled(next==total - 1);

        this.popup.position();
    }
});