/*
 * Copyright (C) 2012 GeoNetwork
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
 * All search related stuff. Helps App.js to control and do searches.
 */
GeoNetwork.searchApp = function() {

    // Public Space
    return {
        init : function() {

            this.generateSimpleSearchForm();
            this.advSearchForm = this.generateAdvancedSearchForm();
            this.createResultsPanel(null);
        },

        /**
         * api:method[generateSimpleSearchForm]
         * 
         * Creates simple search form
         */
        generateSimpleSearchForm : function() {
            var formItems = [];

            var fieldAny = new GeoNetwork.form.OpenSearchSuggestionTextField({
                fieldLabel : 'Text search',
                hideLabel : false,
                id : 'E_any',
                anchor : '100%',
                minChars : 2,
                loadingText : '...',
                hideTrigger : true,
                url : catalogue.services.opensearchSuggest,
                listeners : {
                    // Updating the hidden search field in the
                    // search form which is going to be submitted
                    change : function() {
                        Ext.getCmp('E_trueany').setValue(this.getValue());
                    },
                    keyup : function() {
                        Ext.getCmp('E_trueany').setValue(this.getValue());
                    }
                }
            });

            var fieldType = new Ext.form.ComboBox({
                fieldLabel : 'Type',
                name : 'E1.0_type',
                store : this.getTypeStore(),
                mode : 'local',
                displayField : 'name',
                valueField : 'id',
                value : '',
                emptyText : 'Any', // translate('any'),
                hideTrigger : true,
                forceSelection : true,
                editable : false,
                triggerAction : 'all',
                selectOnFocus : true,
                anchor : '100%'
            });

            var fieldKantone = this.getKantoneCombo().combo;

            formItems.push([ fieldAny, fieldType, fieldKantone ]);

            return new GeoNetwork.SearchFormPanel({
                id : 'simple-search-options-content-form',
                renderTo : 'simple-search-options-content',
                stateId : 's',
                autoHeight : true,
                border : false,
                searchCb : function() {

                    var any = Ext.get('E_any');
                    if (any) {
                        if (any.getValue() === OpenLayers
                                .i18n('fullTextSearch')) {
                            any.setValue('');
                        }
                    }

                    catalogue.startRecord = 1; // Reset start record
                    searching = true;
                    catalogue.search('simple-search-options-content-form',
                            app.searchApp.loadResults, null,
                            catalogue.startRecord, true);
                    showSearch();
                },
                listeners : {
                    onreset : function() {
                        Ext.getCmp('facets-panel').reset();
                        this.fireEvent('search');

                        GeoNetwork.Util.updateHeadInfo({
                            title : catalogue.getInfo().name
                        });
                    }
                },
                forceLayout : true,
                padding : 5,
                items : formItems
            });
        },
        generateAdvancedSearchForm : function() {
            var advancedCriteria = [];

            // This is the full text search field populated when the
            // one from the simple search form is updated.
            // This field is hidden
            var any = new Ext.form.TextField({
                name : 'E_any_OR_geokeyword',
                id : 'E_trueany',
                hidden : true
            });

            // Multi select keyword
            var themekeyStore = new GeoNetwork.data.OpenSearchSuggestionStore({
                url : catalogue.services.opensearchSuggest,
                rootId : 1,
                baseParams : {
                    field : 'keyword'
                }
            });

            var themekeyField = new Ext.ux.form.SuperBoxSelect({
                hideLabel : false,
                minChars : 0,
                queryParam : 'q',
                hideTrigger : false,
                id : 'E_themekey',
                name : 'E_themekey',
                store : themekeyStore,
                valueField : 'value',
                displayField : 'value',
                valueDelimiter : ' or ',
                // tpl: tpl,
                fieldLabel : OpenLayers.i18n('keyword')
            });

            var orgNameStore = new GeoNetwork.data.OpenSearchSuggestionStore({
                url : catalogue.services.opensearchSuggest,
                rootId : 1,
                baseParams : {
                    field : 'orgName'
                }
            });

            var orgNameField = new Ext.ux.form.SuperBoxSelect({
                hideLabel : false,
                minChars : 0,
                queryParam : 'q',
                hideTrigger : false,
                id : 'E_orgName',
                name : 'E_orgName',
                store : orgNameStore,
                valueField : 'value',
                displayField : 'value',
                valueDelimiter : ' or ',
                // tpl: tpl,
                fieldLabel : OpenLayers.i18n('org')
            });
            // var denominatorField = GeoNetwork.util.SearchFormTools
            // .getScaleDenominatorField(true);

            // var groupField = GeoNetwork.util.SearchFormTools.getGroupField(
            // catalogue.services.getGroups, true);
            var metadataTypeField = GeoNetwork.util.SearchFormTools
                    .getMetadataTypeField(true);
            // var validField = GeoNetwork.util.SearchFormTools
            // .getValidField(true);

            // Add hidden fields to be use by quick metadata links from the
            // admin panel (eg. my metadata).
            var ownerField = new Ext.form.TextField({
                name : 'E__owner',
                hidden : true
            });
            var isHarvestedField = new Ext.form.TextField({
                name : 'E__isHarvested',
                hidden : true
            });
            var siteId = new Ext.form.TextField({
                name : 'E_siteId',
                hidden : true
            });
            var serviceTypeField = GeoNetwork.util.INSPIRESearchFormTools
                    .getServiceTypeField(true);
            advancedCriteria.push(themekeyField, orgNameField,
                    metadataTypeField, ownerField, isHarvestedField, siteId);

            var optionFields = GeoNetwork.util.SearchFormTools
                    .getOptions(catalogue.services);

            var what = {
                title : OpenLayers.i18n('What'),
                id : 'what_adv_search',
                margins : '0 5 0 5',
                layout : 'form',
                forceLayout : true,
                items : [ any, advancedCriteria, optionFields ]
            };

            var mapLayers = [];
            for ( var i = 0; i < GeoNetwork.map.BACKGROUND_LAYERS.length; i++) {
                mapLayers.push(GeoNetwork.map.BACKGROUND_LAYERS[i].clone());
            }
            var geomWithMapField = {
                xtype : 'gn_geometrymapfield',
                id : 'geometryMap',
                layers : mapLayers,
                width : '100%',
                height : '220',
                mapOptions : GeoNetwork.map.MAP_OPTIONS,
                activated : false
            // restrictToMapExtent: true
            };
            var where = {
                title : OpenLayers.i18n('Where'),
                id : 'where_adv_search',
                margins : '0 5 0 5',
                bodyStyle : 'padding:0px',
                layout : 'form',
                items : [ geomWithMapField ]
            };

            var when = {
                title : OpenLayers.i18n('When'),
                id : 'when_adv_search',
                margins : '0 5 0 5',
                forceLayout : true,
                defaultType : 'datefield',
                layout : 'form',
                defaults : {
                    anchor : '100%'
                },
                items : GeoNetwork.util.SearchFormTools.getWhen()
            };

            var inspireFields = GeoNetwork.util.INSPIRESearchFormTools
                    .getINSPIREFields(catalogue.services, true, {
                        withAnnex : true,
                        withTheme : true
                    });
            inspireFields.push(serviceTypeField);

            var inspire = {
                title : 'INSPIRE',
                id : 'inspire_adv_search',
                margins : '0 5 0 5',
                defaultType : 'datefield',
                layout : 'form',
                defaults : {
                    anchor : '100%'
                },
                items : inspireFields
            };

            var formItems = [];

            formItems.push({
                id : 'advSearchTabs',
                layout : {
                    type : 'column'
                },
                plain : true,
                forceLayout : false,
                border : false,
                deferredRender : false,
                defaults : {
                    columnWidth : 0.25
                },
                items : [ what, where, when, inspire ]
            });

            this.setAdminFieldsCallback([ metadataTypeField ]);

            return new GeoNetwork.SearchFormPanel({
                id : 'advanced-search-options-content-form',
                renderTo : 'advanced-search-options-content',
                stateId : 's',
                layout : 'form',
                defaults : {
                    anchor : '32%'
                },
                autoHeight : true,
                border : false,
                searchCb : function() {

                    var any = Ext.get('E_any');
                    if (any) {
                        if (any.getValue() === OpenLayers
                                .i18n('fullTextSearch')) {
                            any.setValue('');
                        }
                    }

                    catalogue.startRecord = 1; // Reset start record
                    searching = true;
                    catalogue.search('advanced-search-options-content-form',
                            app.searchApp.loadResults, null,
                            catalogue.startRecord, true);
                    app.searchApp.firstSearch = true;
                    showSearch();
                },
                listeners : {
                    onreset : function() {
                        if (Ext.getCmp('facets-panel')) {
                            Ext.getCmp('facets-panel').reset();
                        }
                        this.fireEvent('search');

                        GeoNetwork.Util.updateHeadInfo({
                            title : catalogue.getInfo().name
                        });
                    }
                },
                forceLayout : true,
                padding : 2,
                items : formItems
            });
        },

        /**
         * Set event in order to display some search criteria only when user is
         * logged in.
         */
        setAdminFieldsCallback : function(adminFields) {
            // Hide or show extra fields after login event
            Ext.each(adminFields, function(item) {
                item.setVisible(catalogue.identifiedUser
                        && catalogue.identifiedUser.role === "Administrator");
            });
            catalogue
                    .on(
                            'afterLogin',
                            function() {
                                Ext
                                        .each(
                                                adminFields,
                                                function(item) {
                                                    item
                                                            .setVisible(catalogue.identifiedUser
                                                                    && catalogue.identifiedUser.role === "Administrator");
                                                });
                                GeoNetwork.util.SearchFormTools
                                        .refreshGroupFieldValues();
                            });
            catalogue.on('afterLogout', function() {
                Ext.each(adminFields, function(item) {
                    item.setVisible(!(catalogue.identifiedUser === undefined));
                });
                GeoNetwork.util.SearchFormTools.refreshGroupFieldValues();
            });

        },

        /**
         * api:method[getTypeStore]
         * 
         * Return an ArrayStore of type options
         */
        getTypeStore : function(defaultValue) {
            return new Ext.data.ArrayStore(
                    {
                        id : 0,
                        fields : [ 'id', 'name' ],
                        data : [
                                [ '', OpenLayers.i18n('any') ],
                                [ 'dataset', OpenLayers.i18n('dataset') ],
                                [ 'basicgeodata',
                                        OpenLayers.i18n('basicgeodata') ],
                                [ 'basicgeodata-federal',
                                        OpenLayers.i18n('basicgeodata-federal') ],
                                [
                                        'basicgeodata-cantonal',
                                        OpenLayers
                                                .i18n('basicgeodata-cantonal') ],
                                [
                                        'basicgeodata-communal',
                                        OpenLayers
                                                .i18n('basicgeodata-communal') ],
                                [ 'service', OpenLayers.i18n('service') ],
                                [ 'service-OGC:WMS',
                                        OpenLayers.i18n('service-OGC:WMS') ],
                                [ 'service-OGC:WFS',
                                        OpenLayers.i18n('service-OGC:WFS') ] ]
                    });
        },

        /**
         * api:method[getCountryStore]
         * 
         * Return an ArrayStore of country options
         */
        getCountryStore : function() {
            var Country = Ext.data.Record.create([ {
                name : 'name',
                mapping : 'name'
            }, {
                name : 'value',
                mapping : 'value'
            }, {
                name : 'bbox',
                mapping : 'bbox'
            } ]);
            return new Ext.data.Store({
                reader : new Ext.data.JsonReader({
                    root : 'root',
                    id : 'value'
                }, Country),
                data : {
                    root : [
                            {
                                name : OpenLayers.i18n('any'),
                                value : '',
                                bbox : null
                            },
                            {
                                name : 'CH',
                                value : "0",
                                bbox : new OpenLayers.Bounds(485000, 73000,
                                        836000, 297000)
                            },
                            {
                                name : 'LI',
                                value : "1",
                                bbox : new OpenLayers.Bounds(754500, 213000,
                                        767000, 237500)
                            } ]
                }
            });
        },

        /**
         * api:method[getSpatialRelationStore]
         * 
         * Return an ArrayStore of spatial relations options
         */
        getSpatialRelationStore : function() {
            return new Ext.data.ArrayStore({
                id : 0,
                fields : [ 'id', 'name' ],
                data : [
                        [ OpenLayers.Filter.Spatial.WITHIN,
                                OpenLayers.i18n('withinGeo') ],
                        [ OpenLayers.Filter.Spatial.INTERSECTS,
                                OpenLayers.i18n('intersectGeo') ],
                        [ OpenLayers.Filter.Spatial.CONTAINS,
                                OpenLayers.i18n('containsGeo') ] ]
            });
        },

        /**
         * Method: createKantoneCombo
         * 
         * Parameters: - {Boolean} If true, destroys and recreates singleton
         * 
         * Returns: An Class Ext.form.ComboBox
         * 
         */
        getKantoneCombo : function(createNew) {
            return this.createSearchWFS(false, 'chtopo', 'kantoneBB', [
                    'KUERZEL', 'KANTONSNR', 'BOUNDING' ], {
                id : 'kantoneComboBox',
                fieldLabel : translate('kantone'),
                displayField : 'KUERZEL',
                valueField : 'KANTONSNR',
                name : 'kantone',
                triggerAction : 'all',
                minChars : 1,
                anchor : '100%'
            });
        },

        createSearchWFS : function(local, ns, type, fields, opts, conversions) {
            var recordFields = [];
            var properties = "";
            for ( var i = 0; i < fields.length; ++i) {
                var name = fields[i];
                if (conversions != undefined && conversions[name] != undefined) {
                    recordFields.push({
                        name : name,
                        mapping : name,
                        convert : conversions[name]
                    });
                } else {
                    recordFields.push({
                        name : name,
                        mapping : name
                    });
                }
                properties += '    <ogc:PropertyName>' + ns + ':' + name
                        + '</ogc:PropertyName>';
            }

            var Record = Ext.data.Record.create(recordFields);

            var reader = new Ext.data.XmlReader({
                record : type,
                id : '@fid'
            }, Record);

            var ds;
            if (local) {
                ds = new Ext.data.Store({
                    reader : reader,
                    sortInfo : {
                        field : opts.displayField,
                        direction : "ASC"
                    }
                });

                app.searchApp.readWFS(
                        GeoNetwork.Settings.GeoserverUrl + "/wfs", ns, type,
                        fields, null, {
                            success : function(response) {
                                ds.loadData(response.responseXML);
                                ds.add(new Record({}));
                            }
                        });
            } else {
                ds = new Ext.data.Store(
                        {
                            reader : reader,
                            sortInfo : {
                                field : opts.displayField,
                                direction : "ASC"
                            },
                            load : function(options) {
                                options = options || {};
                                if (this.fireEvent("beforeload", this, options) !== false) {
                                    this.storeOptions(options);
                                    var query = this.baseParams[search.queryParam];
                                    var filter = new OpenLayers.Filter.Comparison(
                                            {
                                                type : OpenLayers.Filter.Comparison.LIKE,
                                                property : opts.searchField
                                                        || opts.displayField,
                                                value : query.toLowerCase()
                                                        + ".*"
                                            });
                                    if (opts.updateFilter) {
                                        filter = opts.updateFilter.call(search,
                                                filter);
                                    }

                                    app.searchApp
                                            .readWFS(
                                                    GeoNetwork.Settings.GeoserverUrl
                                                            + "/wfs",
                                                    ns,
                                                    type,
                                                    fields,
                                                    filter,
                                                    {
                                                        success : function(
                                                                response) {
                                                            ds
                                                                    .loadData(response.responseXML);
                                                            ds.add(new Record(
                                                                    {}));
                                                        }
                                                    });
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                        });
            }
            OpenLayers.Util.applyDefaults(opts, {
                store : ds,
                loadingText : 'Searching...',
                mode : local ? 'local' : 'remote',
                hideTrigger : false,
                typeAhead : true,
                anchor : '100%',
                selectOnFocus : true
            });
            var search = new Ext.ux.form.SuperBoxSelect(opts);

            var refreshTheContour = function(combo) {
                var records = combo.getRecords();

                if (records.length == 0)
                    return;

                var format = new OpenLayers.Format.WKT();
                var bbox = null;
                for ( var i = 0; i < records.length; ++i) {
                    var record = records[i];
                    if (record.get("BOUNDING")) {
                        var feature = format.read(record.get("BOUNDING"));
                        if (bbox) {
                            bbox.extend(feature.geometry.getBounds());
                        } else {
                            bbox = feature.geometry.getBounds();
                        }
                    }
                }
                try {
                    if (bbox)
                        geocat.map.zoomToExtent(bbox);
                } catch (e) {
                }
            };
            search.on('change', refreshTheContour);

            return {
                combo : search,
                store : ds,
                refreshContour : function() {
                    refreshTheContour(search);
                }
            };
        },

        readWFS : function(url, ns, type, properties, filter, queryOpts) {
            var queryPrefix = '<?xml version="1.0" ?>\n'
                    + '<wfs:GetFeature service="WFS" version="1.0.0"\n'
                    + '                xmlns:wfs="http://www.opengis.net/wfs"\n'
                    + '                xmlns:ogc="http://www.opengis.net/ogc"\n'
                    + '                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"\n'
                    + '                xsi:schemaLocation="http://www.opengis.net/wfs ../wfs/1.0.0/WFS-basic.xsd">\n'
                    + '  <wfs:Query typeName="' + ns + ':' + type + '">\n';
            var queryPostfix = '  </wfs:Query>\n' + '</wfs:GetFeature>';
            var property = '';
            for ( var i = 0; i < properties.length; ++i) {
                property += '    <ogc:PropertyName>' + ns + ':' + properties[i]
                        + '</ogc:PropertyName>\n';
            }

            var filters = "";
            if (filter) {
                filters = new OpenLayers.Format.XML()
                        .write(new OpenLayers.Format.Filter().write(filter));
            }

            var opts = {
                url : url,
                data : queryPrefix + property + filters + queryPostfix
            };
            OpenLayers.Util.applyDefaults(opts, queryOpts);
            return OpenLayers.Request.POST(opts);
        },
        /**
         * Bottom bar
         * 
         * @return
         */
        createBBar : function() {

            var previousAction = new Ext.Action({
                id : 'previousBt',
                text : '&lt;&lt;',
                handler : function() {
                    var from = catalogue.startRecord
                            - parseInt(Ext.getCmp('E_hitsperpage').getValue(),
                                    10);
                    if (from > 0) {
                        catalogue.startRecord = from;
                        catalogue.search(
                                'advanced-search-options-content-form',
                                app.searchApp.loadResults, null,
                                catalogue.startRecord, true);
                    }
                },
                scope : this
            });

            var nextAction = new Ext.Action({
                id : 'nextBt',
                text : '&gt;&gt;',
                handler : function() {
                    catalogue.startRecord += parseInt(Ext.getCmp(
                            'E_hitsperpage').getValue(), 10);
                    catalogue.search('advanced-search-options-content-form',
                            app.searchApp.loadResults, null,
                            catalogue.startRecord, true);
                },
                scope : this
            });

            return new Ext.Toolbar({
                items : [ previousAction, '|', nextAction, '|', {
                    xtype : 'tbtext',
                    text : '',
                    id : 'info'
                } ]
            });

        },

        /**
         * Results panel layout with top, bottom bar and DataView
         * 
         * @return
         */
        createResultsPanel : function(permalinkProvider) {
            var metadataResultsView = new GeoNetwork.MetadataResultsView({
                catalogue : catalogue,
                displaySerieMembers : true,
                autoScroll : true,
                autoWidth : false,
                tpl : GeoNetwork.Templates.FULL,
                templates : {
                    SIMPLE : GeoNetwork.Templates.SIMPLE,
                    THUMBNAIL : GeoNetwork.Templates.THUMBNAIL,
                    FULL : GeoNetwork.Templates.FULL
                },
                featurecolor : GeoNetwork.Settings.results.featurecolor,
                colormap : GeoNetwork.Settings.results.colormap,
                featurecolorCSS : GeoNetwork.Settings.results.featurecolorCSS
            });

            catalogue.resultsView = metadataResultsView;

            // Add results to map
//            Ext.each(app.mapApp.maps, function(map) {
//                catalogue.resultsView.addMap(map, true);
//            });
//TODO add map
            
            var tBar = new GeoNetwork.MetadataResultsToolbar({
                catalogue : catalogue,
                searchFormCmp : Ext
                        .getCmp('advanced-search-options-content-form'),
                sortByCmp : Ext.getCmp('E_sortBy'),
                metadataResultsView : metadataResultsView
            });

            var bBar = this.createBBar();

            var resultPanel = new Ext.Panel({
                id : 'resultsPanel',
                border : false,
                hidden : true,
                bodyCssClass : 'md-view',
                autoWidth : true,
                layout : 'fit',
                tbar : tBar,
                items : metadataResultsView,
                renderTo : 'result-panel',
                // paging bar on the bottom
                bbar : bBar
            });
            return resultPanel;
        },
        loadResults : function(response, query) {
            // Ext.state.Manager.getProvider().updateLastSearch(query);
            // Show "List results" panel
            var facetPanel = Ext.getCmp('facets-panel');
            // Init facet panel on first search
            if (!facetPanel) {
                app.searchApp.generateFacetedSearchPanel();
            }
            Ext.getCmp('facets-panel').refresh(response);

            if (Ext.getCmp('previousBt')) {
                Ext.getCmp('previousBt').setDisabled(
                        catalogue.startRecord === 1);
            }
            if (Ext.getCmp('previousBt_up')) {
                Ext.getCmp('previousBt_up').setDisabled(
                        catalogue.startRecord === 1);
            }

            if (Ext.getCmp('nextBt')) {
                Ext
                        .getCmp('nextBt')
                        .setDisabled(
                                catalogue.startRecord
                                        + parseInt(Ext.getCmp('E_hitsperpage')
                                                .getValue(), 10) > catalogue.metadataStore.totalLength);
            }

            if (Ext.getCmp('nextBt_up')) {
                Ext
                        .getCmp('nextBt_up')
                        .setDisabled(
                                catalogue.startRecord
                                        + parseInt(Ext.getCmp('E_hitsperpage')
                                                .getValue(), 10) > catalogue.metadataStore.totalLength);
            }

            if (Ext.getCmp('E_sortBy').getValue()) {
                Ext.getCmp('sortByToolBar').setValue(
                        Ext.getCmp('E_sortBy').getValue() + "#"
                                + Ext.getCmp('sortOrder').getValue());
            } else {
                if (Ext.getCmp('sortByToolBar')) {
                    Ext.getCmp('sortByToolBar').setValue(
                            Ext.getCmp('E_sortBy').getValue());
                }
            }

            if (Ext.getCmp("info_up") && Ext.getCmp("info")) {
                Ext.getCmp("info_up").update(
                        Ext.getCmp("info").el.dom.textContent);
            }

            // Fix for width sortBy combo in toolbar
            // See this:
            // http://www.sencha.com/forum/showthread.php?122454-TabPanel-deferred-render-false-nested-toolbar-layout-problem

            if (Ext.getCmp('sortByToolBar')) {
                Ext.getCmp('sortByToolBar').syncSize();
                Ext.getCmp('sortByToolBar').setWidth(130);
            }
            // Update page title based on search results and params
            var formParams = GeoNetwork.util.SearchTools
                    .getFormValues(app.searchApp.advSearchForm);
            var criteria = '', excludedSearchParam = {
                E_hitsperpage : null,
                timeType : null,
                sortOrder : null,
                sortBy : null
            };
            for ( var item in formParams) {
                if (formParams.hasOwnProperty(item)
                        && excludedSearchParam[item] === undefined) {
                    var value = formParams[item];
                    if (value !== '') {
                        fieldName = item.split('_');
                        criteria += OpenLayers.i18n(fieldName[1] || item)
                                + ': ' + value + ' - ';
                    }
                }
            }
            var title = (catalogue.metadataStore.totalLength || 0)
                    + OpenLayers.i18n('recordsFound')
                    + (criteria !== '' ? ' | ' + criteria : '');

            GeoNetwork.Util.updateHeadInfo({
                title : catalogue.getInfo().name + ' | ' + title
            });

            if (!cookie || !cookie.get('user') || !cookie.get('user').username) {
                Ext.each(Ext.DomQuery.select('.md-action-menu'), function(el) {
                    hide(el);
                });
            }
        },
        generateFacetedSearchPanel : function() {
            var facetsPanel = new GeoNetwork.FacetsPanel(
                    {
                        id : 'facets-panel',
                        renderTo : 'search-filter',
                        searchForm : Ext
                                .getCmp('advanced-search-options-content-form'),
                        maxDisplayedItems : GeoNetwork.Settings.facetMaxItems || 7,
                        facetListConfig : GeoNetwork.Settings.facetListConfig
                                || []
                    });
        }
    };

};