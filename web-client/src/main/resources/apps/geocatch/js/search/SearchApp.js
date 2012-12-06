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
        },

        /** api:method[generateSimpleSearchForm]
         *  
         *  Creates simple search form
         */
        generateSimpleSearchForm : function() {
            var formItems = [];

            var fieldAny = new GeoNetwork.form.OpenSearchSuggestionTextField({ 
                fieldLabel: 'Text search',   
                hideLabel: false,            
                id : 'E_any',
               anchor: '100%',
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
                fieldLabel: 'Type',
                name: 'E1.0_type',
                store: this.getTypeStore(),
                mode: 'local',
                displayField: 'name',
                valueField: 'id',
                value: '',
                emptyText: 'Any', //translate('any'),
                hideTrigger: true,
                forceSelection: true,
                editable: false,
                triggerAction: 'all',
                selectOnFocus: true,
                anchor: '100%'
            });

            
            formItems.push([fieldAny, fieldType]);

           return new GeoNetwork.SearchFormPanel({
                id : 'simple-search-options-content-form',
                renderTo : 'simple-search-options-content',
                stateId : 's',
                autoHeight: true,
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

        /** api:method[getTypeStore]
         *  
         *  Return an ArrayStore of type options
         */
        getTypeStore: function(defaultValue){
            return new Ext.data.ArrayStore({
                id: 0,
                fields: ['id', 'name'],
                data: [
                        ['', OpenLayers.i18n('any')],
                        ['dataset', OpenLayers.i18n('dataset')], 
                        ['basicgeodata', OpenLayers.i18n('basicgeodata')], 
                        ['basicgeodata-federal', OpenLayers.i18n('basicgeodata-federal')], 
                        ['basicgeodata-cantonal', OpenLayers.i18n('basicgeodata-cantonal')], 
                        ['basicgeodata-communal', OpenLayers.i18n('basicgeodata-communal')], 
                        ['service', OpenLayers.i18n('service')], 
                        ['service-OGC:WMS', OpenLayers.i18n('service-OGC:WMS')],
                        ['service-OGC:WFS', OpenLayers.i18n('service-OGC:WFS')]]
            });
        },

        /** api:method[getCountryStore]
         *  
         *  Return an ArrayStore of country options
         */
        getCountryStore: function() {
            var Country = Ext.data.Record.create([
                {name: 'name', mapping: 'name'},
                {name: 'value', mapping: 'value'},
                {name: 'bbox', mapping: 'bbox'}
            ]);
            return new Ext.data.Store({
                reader: new Ext.data.JsonReader({
                    root: 'root',
                    id: 'value'
                }, Country),
                data: {
                    root: [
                        {name: OpenLayers.i18n('any'), value: '', bbox: null},
                        {name: 'CH', value: "0", bbox: new OpenLayers.Bounds(485000, 73000, 836000, 297000)},
                        {name: 'LI', value: "1", bbox: new OpenLayers.Bounds(754500, 213000, 767000, 237500)}
                    ]
                }
            });
        },

        createSearchWFS: function(local, ns, type, fields, opts, conversions) {
            var recordFields = [];
            var properties = "";
            for (var i = 0; i < fields.length; ++i) {
                var name = fields[i];
                if(conversions != undefined && conversions[name] != undefined) {
                    recordFields.push({name: name, mapping: name, convert: conversions[name]});                
                } else {
                    recordFields.push({name: name, mapping: name});
                }
                properties += '    <ogc:PropertyName>' + ns + ':' + name + '</ogc:PropertyName>';
            }

            var Record = Ext.data.Record.create(recordFields);

            var reader = new Ext.data.XmlReader({
                record: type,
                id: '@fid'
            }, Record);

            var ds;
            if (local) {
                ds = new Ext.data.Store({
                    reader: reader,
                    sortInfo: {field: opts.displayField,  direction:"ASC"}
                });
                searchTools.readWFS(geocat.geoserverUrl + "/wfs", ns, type, fields, null, {
                    success: function(response) {
                        ds.loadData(response.responseXML);
                        ds.add(new Record({}));
                    }
                });
            } else {
                ds = new Ext.data.Store({
                    reader: reader,
                    sortInfo: {field: opts.displayField,  direction:"ASC"},
                    load: function(options) {
                        options = options || {};
                        if (this.fireEvent("beforeload", this, options) !== false) {
                            this.storeOptions(options);
                            var query = this.baseParams[search.queryParam];
                            var filter = new OpenLayers.Filter.Comparison({
                                type: OpenLayers.Filter.Comparison.LIKE,
                                property: opts.searchField || opts.displayField,
                                value: query.toLowerCase() + ".*"
                            });
                            if (opts.updateFilter) {
                                filter = opts.updateFilter.call(search, filter);
                            }
                            searchTools.readWFS(geocat.geoserverUrl + "/wfs", ns, type, fields, filter, {
                                success: function(response) {
                                    ds.loadData(response.responseXML);
                                    ds.add(new Record({}));
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
                store: ds,
                loadingText: 'Searching...',
                mode: local ? 'local' : 'remote',
                hideTrigger:false,
                typeAhead: true,
                anchor: '100%',
                selectOnFocus: true
            });
            var search = new Ext.ux.BoxSelect(opts);

            var refreshTheContour = function(combo) {
                var records = combo.getRecords();

                if (records.length == 0) return;

                var format = new OpenLayers.Format.WKT();
                var bbox = null;
                for (var i = 0; i < records.length; ++i) {
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
                try {if (bbox) geocat.map.zoomToExtent(bbox);}catch(e){}
            };
            search.on('change', refreshTheContour);

            return {
                combo: search,
                store: ds,
                refreshContour: function() {
                    refreshTheContour(search);
                }
            };
        }
    };
};