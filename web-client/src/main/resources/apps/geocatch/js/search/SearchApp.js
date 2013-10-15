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

        simpleSearchForm : undefined,
        advSearchForm : undefined,
        switcher : undefined,
        drawControl : undefined,

        init : function() {

            this.simpleSearchForm = this.generateSimpleSearchForm();

            this.advSearchForm = this.generateAdvancedSearchForm();

            this.switcher = this.generateSwitcher();

            hideAdvancedSearch();

            this.createResultsPanel(null);

            // Show initial facets filter
            app.searchApp.generateFacetedSearchPanel();
            // FIXME how many results should I load to
            // get all important facets?
            Ext.Ajax
                    .request({
                        url : catalogue.URL
                                + "/srv/"
                                + catalogue.LANG
                                + "/q?fast=index&from=1&to=30&sortBy=relevance&summaryOnly=1&ts="
                                + (new Date()).getTime(),
                        success : function(response) {
                            Ext.getCmp('facets-panel').refresh(response);
                        },
                        disableCaching : false

                    });

        },

        getSearchForm : function() {
            var searchForm = Ext.getCmp('advanced-search-options-content-form');

            if (!searchForm.isVisible()) {
                searchForm = Ext.getCmp('simple-search-options-content-form');
            }

            return searchForm;
        },

        fireSearch : function(keepUrlParams) {

            var searchForm = this.getSearchForm();
            if(keepUrlParams === undefined || !keepUrlParams) {
	            Ext.each(Ext.query("input[name=E__owner]"), function(input) {
	                Ext.getCmp(input.id).setValue("");
	            });
	            Ext.each(Ext.query("input[name=E_siteId]"), function(input) {
	            	Ext.getCmp(input.id).setValue("");
	            });
	            Ext.each(Ext.query("input[name=E__isHarvested]"), function(input) {
	                Ext.getCmp(input.id).setValue("");
	            });
            }
            searchForm.fireEvent('search');

            // Maximize Search Results Panel
            Ext.getCmp("west").setWidth(Ext.getCmp("west").minWidth);
            Ext.getCmp("vp").doLayout();
        },

        resetSearch : function() {

            var searchForm = this.getSearchForm();

            geocat.vectorLayer.removeAllFeatures();
            app.mapApp.getMap().zoomToMaxExtent();

            searchForm.fireEvent('reset');
        },

        /***********************************************************************
         * api:method[generateSwitcher]
         * 
         * Generate the switcher links between simple and advanced search
         */
        generateSwitcher : function() {

            return new Ext.Panel(
                    {
                        height : 66,
                        region : 'south',
                        border : false,
                        html : '<div style="padding:8px; display:block;text-align:center;background-color: #DFE8F6;">'
                                + '<input type="button" onclick="app.searchApp.fireSearch()"'
                                + ' id="search-submit" class="form-submit" value="'
                                + OpenLayers.i18n('Search')
                                + '"></input><input type="button" onclick="app.searchApp.resetSearch()"'
                                + ' id="search-reset" class="form-reset" value="'
                                + OpenLayers.i18n('Reset')
                                + '"></input></div>'
                                + '<a id="show-advanced" href="javascript:showAdvancedSearch()">'
                                + OpenLayers.i18n('showAdvancedOptions')
                                + '</a>'
                                + '<a id="hide-advanced" href="javascript:hideAdvancedSearch()" style="display:none">'
                                + OpenLayers.i18n('hideAdvancedOptions')
                                + '</a>'
                    });
        },

        /**
         * api:method[generateSimpleSearchForm]
         * 
         * Creates simple search form
         */
        generateSimpleSearchForm : function() {
            var formItems = [];

            var fieldAny = new GeoNetwork.form.OpenSearchSuggestionTextField({
                fieldLabel : OpenLayers.i18n('searchText'),
                hideLabel : false,
                id : 'E_any',
                // TODO: Check why if not set explicit width takes size much
                // bigger than panel
                anchor : '-10',
                minChars : 2,
                loadingText : '...',
                hideTrigger : true,
                url : catalogue.services.opensearchSuggest,
                listeners : {
                    // Updating the hidden search field in the
                    // search form which is going to be submitted
                    change : function() {
                        Ext.getCmp('anyField').setValue(this.getValue());
                    },
                    keyup : function(e, a) {
                        if (a.ENTER == a.keyCode) {
                            app.searchApp.fireSearch();
                        }
                        Ext.getCmp('anyField').setValue(this.getValue());

                    }
                }
            })

            var fieldType = new Ext.form.ComboBox({
                fieldLabel : OpenLayers.i18n('recordType'),
                name : 'E1.0_type',
                store : this.getTypeStore(),
                mode : 'local',
                displayField : 'name',
                valueField : 'id',
                value : '',
                emptyText : OpenLayers.i18n('any2'),
                hideTrigger : true,
                forceSelection : true,
                editable : false,
                triggerAction : 'all',
                selectOnFocus : true,
                anchor : '-10',
                listeners : {
                    keyup : function(e, a) {
                        if (a.ENTER == a.keyCode) {
                            app.searchApp.fireSearch();
                        }
                    }
                }
            });
            
            var fieldKantone = this.getKantoneCombo(true).combo;

            var hidden = new Ext.form.TextField({
                name : 'G_hidden',
                hidden : true,
                value : "gg25"
            });

            formItems.push([ fieldAny, fieldType, fieldKantone, hidden ]);

            return new GeoNetwork.SearchFormPanel({
                id : 'simple-search-options-content-form',
                autoHeight : true,
                labelWidth : 100,
                bodyStyle : 'padding:5px',
                region : 'north',
                border : false,
                resetBt : null,
                searchBt : null,
                searchCb : function() {

                    var any = Ext.get('E_any');
                    if (any) {
                        if (any.getValue() === OpenLayers
                                .i18n('fullTextSearch')) {
                            any.setValue('');
                        }
                    }

                    Ext.get("results-main").dom.style.display = 'none';

                    catalogue.startRecord = 1; // Reset start record
                    searching = true;
                    catalogue.search('simple-search-options-content-form',
                            app.searchApp.loadResults, null,
                            catalogue.startRecord, true);
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
                items : formItems
            });
        },

        /**
         * api:method[generateAdvancedSearchForm]
         * 
         * Creates advanced search form
         */
        generateAdvancedSearchForm : function() {

            var f = [ {
                fieldLabel : OpenLayers.i18n("searchText"),
                id : "anyField",
                enableKeyEvents : true,
                anchor : '-10',
                name : "T_any",
                listeners : {
                    keyup : function(e, a) {
                        if (a.ENTER == a.keyCode) {
                            app.searchApp.fireSearch();
                        }
                    }
                }
            }, {
                fieldLabel : OpenLayers.i18n("rtitle"),
                name : "T_title",
                anchor : '-10',
                id : "TitleField",
                enableKeyEvents : true,
                listeners : {
                    keyup : function(e, a) {
                        if (a.ENTER == a.keyCode) {
                            app.searchApp.fireSearch();
                        }
                    }
                }
            }, {
                fieldLabel : OpenLayers.i18n("abstract"),
                name : "T_abstract",
                anchor : '-10',
                id : "AbstractField",
                enableKeyEvents : true,
                listeners : {
                    keyup : function(e, a) {
                        if (a.ENTER == a.keyCode) {
                            app.searchApp.fireSearch();
                        }
                    }
                }
            }, new Ext.ux.form.SuperBoxSelect({
                fieldLabel : OpenLayers.i18n("keyword"),
                id : "keywordsCombo",
                name : "[V_keyword",
                store : this.createKeywordsStore(),
                mode : "local",
                displayField : "name",
                valueField : "value",
                forceSelection : false,
                triggerAction : "all",
                selectOnFocus : true,
                anchor : '-10'
            }), new Ext.ux.form.SuperBoxSelect({
                fieldLabel : OpenLayers.i18n("theme"),
                name : "[V_topicCat",
                id : "topicCat",
                store : new Ext.data.SimpleStore({
                    data : GeoNetwork.Settings.Stores['topicCat'],
                    fields : [ "name", "label" ],
                    sortInfo : {
                        field : "label",
                        direction : "ASC"
                    }
                }),
                mode : "local",
                displayField : "label",
                valueField : "name",
                typeAhead : true,
                forceSelection : true,
                triggerAction : "all",
                selectOnFocus : true,
                anchor : '-10'
            }), {
                fieldLabel : OpenLayers.i18n("contact"),
                anchor : '-10',
                name : "T_creator",
                enableKeyEvents : true,
                listeners : {
                    keyup : function(e, a) {
                        if (a.ENTER == a.keyCode) {
                            app.searchApp.fireSearch();
                        }
                    }
                }
            }, {
                fieldLabel : OpenLayers.i18n("organisationName"),
                anchor : '-10',
                name : "T_orgName",
                enableKeyEvents : true,
                listeners : {
                    keyup : function(e, a) {
                        if (a.ENTER == a.keyCode) {
                            app.searchApp.fireSearch();
                        }
                    }
                }
            } ];

            // Hidden fields (for links from the admin)
            f = f.concat([ {
                xtype : "hidden",
                name : "E__owner"
            }, {
                xtype : "hidden",
                name : "E_siteId"
            }, {
                xtype : "hidden",
                name : "E__isHarvested"
            } ]);

            f = f.concat([
                    {
                        xtype : "combo",
                        id : "isTemplate",
                        fieldLabel : OpenLayers.i18n("template"),
                        anchor : "-10",
                        name : "E__isTemplate",
                        value : "n",
                        store : [ [ "n", OpenLayers.i18n("no") ],
                                [ "y", OpenLayers.i18n("yes") ] ],
                        mode : "local",
                        displayField : "name",
                        valueField : "value",
                        hideTrigger : true,
                        forceSelection : true,
                        editable : false,
                        triggerAction : "all",
                        selectOnFocus : true,
                        hidden : !catalogue.isIdentified()
                    }, {
                        fieldLabel : OpenLayers.i18n("identifier"),
                        anchor : '-10',
                        id : "basicgeodataid",
                        name : "S_basicgeodataid",
                        enableKeyEvents : true,
                        listeners : {
                            keyup : function(e, a) {
                                if (a.ENTER == a.keyCode) {
                                    app.searchApp.fireSearch();
                                }
                            }
                        },
                        hidden : false//!catalogue.isIdentified()
                    }, new Ext.ux.form.SuperBoxSelect({
                        id : "formatCombo",
                        fieldLabel : OpenLayers.i18n("formatTxt"),
                        name : "E1.0_format",
                        store : new Ext.data.SimpleStore({
                            data : GeoNetwork.Settings.Stores['formats'],
                            fields : [ "name", "label" ],
                            sortInfo : {
                                field : "label",
                                direction : "ASC"
                            }
                        }),
                        mode : "local",
                        displayField : "label",
                        valueField : "name",
                        emptyText : OpenLayers.i18n("any"),
                        typeAhead : true,
                        forceSelection : true,
                        triggerAction : "all",
                        selectOnFocus : true,
                        anchor : '-10',
                        hidden : !catalogue.isIdentified()
                    }) ]);

            f.push([{
                    xtype : "hidden",
                    name : "E_similarity",
                    value : GeoNetwork.util.SearchTools.DEFAULT_SIMILARITY
                }, {
                   xtype : "hidden",
                   name : "customFilter",
                   value : ""
                }
            ]);
            var d = [ {
                xtype : "fieldset",
                id : 'what-container',
                title : OpenLayers.i18n("what"),
                autoHeight : true,
                defaultType : "textfield",
                labelWidth : this.labelWidth,
                layout : "form",
                anchor : '-10',
                layoutConfig : {
                    labelSeparator : ""
                },
                cls : "compressedFieldSet",
                items : f
            } ];
            
            var fieldArchivedGeoData = new Ext.form.ComboBox({
                fieldLabel : OpenLayers.i18n("geodata"),
                name : 'E1.0_historicalArchive',
                store : this.getArchivedStore(),
                mode : 'local',
                displayField : 'name',
                valueField : 'id',
                value : '',
                hideTrigger : true,
                forceSelection : true,
                editable : false,
                triggerAction : 'all',
                selectOnFocus : true,
                anchor : '-10',
                listeners : {
                    keyup : function(e, a) {
                        if (a.ENTER == a.keyCode) {
                            app.searchApp.fireSearch();
                        }
                    }
                }
            });
            var c = [ this.getTypeCombo() ];
            c = c.concat([
                    {
                        xtype : "combo",
                        fieldLabel : OpenLayers.i18n("valid"),
                        anchor : '-10',
                        id : "isValid",
                        name : "E__valid",
                        store : [ [ "", OpenLayers.i18n("any") ],
                                [ "1", OpenLayers.i18n("yes") ],
                                [ "0", OpenLayers.i18n("no") ],
                                [ "-1", OpenLayers.i18n("unChecked") ] ],
                        mode : "local",
                        displayField : "name",
                        valueField : "value",
                        emptyText : OpenLayers.i18n("any"),
                        hideTrigger : true,
                        forceSelection : true,
                        editable : false,
                        triggerAction : "all",
                        selectOnFocus : true,
                        hidden : !catalogue.isIdentified()
                    }, {
                        xtype : "checkbox",
                        fieldLabel : OpenLayers.i18n("toEdit"),
                        id : "toEdit",
                        name : "B_editable",
                        hidden : !catalogue.isIdentified()
                    }, {
                        xtype : "checkbox",
                        fieldLabel : OpenLayers.i18n("toPublish"),
                        id : "toPublish",
                        name : "B_toPublish",
                        hidden : !catalogue.isIdentified()
                    }, fieldArchivedGeoData ]);

            d.push({
                xtype : "fieldset",
                labelWidth : this.labelWidth,
                title : OpenLayers.i18n("type") + "?",
                autoHeight : true,
                defaultType : "textfield",
                cls : "compressedFieldSet",
                layout : "form",
                anchor : '-10',
                layoutConfig : {
                    labelSeparator : ""
                },
                items : c
            });
            var b = this.createSearchWFS("country", {
                id : "country",
                fieldLabel : OpenLayers.i18n("country"),
                searchField : "LABEL",
                displayField : "LABEL",
                valueField : "ID",
                listWidth : 200,
                emptyText : OpenLayers.i18n("any"),
                name : "country",
                loadingText : "Searching...",
                triggerAction : "all",
                minChars : 1,
                anchor : "-10"
            });
            var a = this.getKantoneCombo(true);
            var e = this.getGemeindenCombo(true);
            var onchangeCountry = function(h, g) {
                Ext.each(this.getKantoneCombo(), function(k) {
                    Ext.getCmp(k.id).setValue("");
                    delete Ext.getCmp(k.id).lastQuery;
                });

                this.getGemeindenCombo().combo.setValue("");
                delete this.getGemeindenCombo().combo.lastQuery;

                var i = Ext.getCmp("country").getValue() === "country:1";

                Ext.each(Ext.query("*[id^=kantoneComboBox]", Ext
                        .get("advanced-search-options-content-form").dom),
                        function(k) {
                            Ext.getCmp(k.id).setDisabled(i);
                        });
                this.getGemeindenCombo().combo.setDisabled(i);
                app.searchApp.highlightGeographicFilter()
            };
            b.combo.on("change", onchangeCountry, this);
            b.combo.on("removeItem", onchangeCountry, this);
            d
                    .push({
                        xtype : "fieldset",
                        labelWidth : this.labelWidth,
                        id : "searchWhere",
                        title : OpenLayers.i18n("where"),
                        autoHeight : true,
                        defaultType : "textfield",
                        cls : "compressedFieldSet",
                        layout : "form",
                        anchor : '-10',
                        layoutConfig : {
                            labelSeparator : ""
                        },
                        items : [
                                {
                                    xtype : "radiogroup",
                                    name : 'G_whereType',
                                    hideLabel : true,
                                    vertical : true,
                                    columns : 1,
                                    defaults : {
                                        name : "G_whereType",
                                        boxLabel : "",
                                        itemCls : "compressedFormItem"
                                    },
                                    items : [
                                            {
                                                id : "none_where",
                                                inputValue : "none",
                                                boxLabel : OpenLayers
                                                        .i18n("wherenone"),
                                                checked : true,
                                                listeners : {
                                                    check : app.searchApp
                                                            .updateWhereForm("none")
                                                }
                                            },
                                            {
                                                id : "bbox_where",
                                                inputValue : "bbox",
                                                boxLabel : OpenLayers
                                                        .i18n("bbox"),
                                                listeners : {
                                                    check : app.searchApp
                                                            .updateWhereForm("bbox")
                                                }
                                            },
                                            {
                                                id : "gg25_where",
                                                inputValue : "gg25",
                                                boxLabel : OpenLayers
                                                        .i18n("adminUnit"),
                                                listeners : {
                                                    check : app.searchApp
                                                            .updateWhereForm("gg25")
                                                }
                                            },
                                            {
                                                id : "polygon_where",
                                                inputValue : "polygon",
                                                boxLabel : OpenLayers
                                                        .i18n("drawOnMap"),
                                                listeners : {
                                                    check : app.searchApp
                                                            .updateWhereForm("polygon")
                                                }
                                            } ]
                                },
                                {
                                    xtype : "panel",
                                    id : "adminBorders",
                                    border : false,
                                    layout : "form",
                                    hidden : true,
                                    layoutConfig : {
                                        labelSeparator : ""
                                    },
                                    items : [ b.combo, a.combo, e.combo ]
                                },
                                {
                                    xtype : "panel",
                                    id : "drawPolygon",
                                    border : false,
                                    hidden : true,
                                    html : '<span id="drawPolygonSpan">'
                                            + '<a href="javascript:app.searchApp.drawWherePolygon()">'
                                            + OpenLayers
                                                    .i18n("startNewPolygon")
                                            + "</a></span>"
                                },
                                {
                                    xtype : "combo",
                                    store : [
                                            [
                                                    "within",
                                                    OpenLayers
                                                            .i18n("withinGeo") ],
                                            [
                                                    "intersection",
                                                    OpenLayers
                                                            .i18n("intersectGeo") ],
                                            [
                                                    "encloses",
                                                    OpenLayers
                                                            .i18n("containsGeo") ] ],
                                    hideTrigger : true,
                                    forceSelection : true,
                                    editable : false,
                                    triggerAction : "all",
                                    anchor : '-10',
                                    selectOnFocus : true,
                                    fieldLabel : OpenLayers.i18n("type"),
                                    name : "boundingRelation",
                                    id : "boundingRelation",
                                    value : "within"
                                } ],
                        listeners : {
                            expand : function() {
                                app.searchApp.updateWhereForm("bbox")(null,
                                        true)
                            },
                            collapse : function() {
                            }
                        }
                    });
            d.push({
                xtype : "fieldset",
                title : OpenLayers.i18n("when"),
                autoHeight : true,
                defaultType : "textfield",
                cls : "compressedFieldSet",
                anchor : '-10',
                layout : "form",
                labelWidth : this.labelWidth,
                layoutConfig : {
                    labelSeparator : ""
                },
                items : [ {
                    xtype : "datefield",
                    fieldLabel : OpenLayers.i18n("from"),
                    format : "d/m/Y",
                    anchor : '-10',
                    postfix : "T00:00:00",
                    enableKeyEvents : true,
                    name : "E_extFrom",
                    listeners : {
                        keyup : function(e, a) {
                            if (a.ENTER == a.keyCode) {
                                app.searchApp.fireSearch();
                            }
                        }
                    }
                }, {
                    xtype : "datefield",
                    fieldLabel : OpenLayers.i18n("to"),
                    format : "d/m/Y",
                    postfix : "T23:59:59",
                    enableKeyEvents : true,
                    anchor : '-10',
                    name : "E_extTo",
                    listeners : {
                        keyup : function(e, a) {
                            if (a.ENTER == a.keyCode) {
                                app.searchApp.fireSearch();
                            }
                        }
                    }
                } ]
            });
            d.push({
                xtype : "fieldset",
                labelWidth : this.labelWidth,
                title : OpenLayers.i18n("source"),
                autoHeight : true,
                defaultType : "textfield",
                cls : "compressedFieldSet",
                layout : "form",
                layoutConfig : {
                    labelSeparator : ""
                },
                enableKeyEvents : true,
                listeners : {
                    keyup : function(e, a) {
                        if (a.ENTER == a.keyCode) {
                            app.searchApp.fireSearch();
                        }
                    }
                },
                items : [ new Ext.ux.form.SuperBoxSelect({
                    fieldLabel : OpenLayers.i18n("catalog"),
                    name : "[V__catalog",
                    store : GeoNetwork.Settings.Stores['sources_groups'],
                    displayField : 'label',
                    valueField : 'name',
                    mode : "local",
                    typeAhead : true,
                    forceSelection : true,
                    triggerAction : "all",
                    selectOnFocus : true,
                    anchor : "-10"
                }) ]
            });
            return new GeoNetwork.SearchFormPanel({
                id : 'advanced-search-options-content-form',
                labelWidth : 100,
                resetBt : null,
                searchBt : null,
                forcelayout : true,
                bodyStyle : 'padding:5px',
                style : {
                    display : 'none'
                },
                searchCb : function() {

                    var any = Ext.get('E_any');
                    if (any) {
                        if (any.getValue() === OpenLayers
                                .i18n('fullTextSearch')) {
                            any.setValue('');
                        }
                    }

                    Ext.get("results-main").dom.style.display = 'none';

                    catalogue.startRecord = 1; // Reset start record
                    searching = true;
                    catalogue.search('advanced-search-options-content-form',
                            app.searchApp.loadResults, null,
                            catalogue.startRecord, true);
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
                items : d
            });

        },
        createKeywordsStore : function() {
            var a = Ext.data.Record.create([ {
                name : "name",
                mapping : "@name",
                sortDir : "ASC"
            }, {
                name : "value",
                mapping : "@name"
            } ]);
            var b = new Ext.data.Store({
                reader : new Ext.data.XmlReader({
                    record : "keyword",
                    id : "@name"
                }, a),
                proxy : new Ext.data.HttpProxy({
                    url : geocat.baseUrl + "srv/" + geocat.language
                            + "/geocat.keywords.list",
                    method : "GET",
                    disableCaching : false
                })
            });
            b.add(new a({
                name : OpenLayers.i18n("any"),
                value : ""
            }));
            b.load({
                add : true
            });
            return b
        },
        getTypeCombo : function() {
            return {
                xtype : "combo",
                fieldLabel : OpenLayers.i18n("type"),
                name : "E1.0_type",
                store : this.getTypeStore(),
                displayField : 'name',
                valueField : 'id',
                mode : "local",
                value : "",
                emptyText : OpenLayers.i18n("any"),
                hideTrigger : true,
                forceSelection : true,
                editable : false,
                triggerAction : "all",
                selectOnFocus : true,
                anchor : "-10"
            }
        },
        createCountryStore : function() {
            var a = Ext.data.Record.create([ {
                name : "name",
                mapping : "name"
            }, {
                name : "value",
                mapping : "value"
            }, {
                name : "bbox",
                mapping : "bbox"
            } ]);
            return new Ext.data.Store({
                reader : new Ext.data.JsonReader({
                    root : "root",
                    id : "value"
                }, a),
                data : {
                    root : [
                            {
                                name : OpenLayers.i18n("any"),
                                value : "",
                                bbox : null
                            },
                            {
                                name : "CH",
                                value : "0",
                                bbox : new OpenLayers.Bounds(485000, 73000,
                                        836000, 297000)
                            },
                            {
                                name : "LI",
                                value : "1",
                                bbox : new OpenLayers.Bounds(754500, 213000,
                                        767000, 237500)
                            } ]
                }
            })
        },
        updateWhereForm : function(a) {
            return function(b, c) {
                var d = Ext.getCmp("adminBorders");
                var e = Ext.getCmp("drawPolygon");
                geocat.drawFeature.deactivate();
                if (c) {
                    switch (a) {
                    case "bbox":
                        d.setVisible(false);
                        e.setVisible(false);
                        app.mapApp.getMap().events.register("moveend", null,
                                geocat.highlightGeographicFilter);
                        break;
                    case "gg25":
                        d.setVisible(true);
                        e.setVisible(false);
                        app.mapApp.getMap().events.unregister("moveend", null,
                                geocat.highlightGeographicFilter);
                        
                        var combos = Ext.query("input", Ext.get(adminBorders).dom);
                        
                        Ext.each(combos, function(combo){
                            var e = Ext.getCmp(combo.id);
                            if(e && e.getValueEx().length > 0){
                                e.fireEvent("change", e);
                            }
                        });
                        break;
                    case "polygon":
                        d.setVisible(false);
                        e.setVisible(true);
                        app.mapApp.getMap().events.unregister("moveend", null,
                                geocat.highlightGeographicFilter);
                        if (geocat.selectionFeature) {
                            geocat.vectorLayer
                                    .destroyFeatures(geocat.selectionFeature);
                            geocat.selectionFeature = null
                        }
                        app.searchApp.drawWherePolygon();
                        break;
                    default:
                        d.setVisible(false);
                        e.setVisible(false);
                        break;
                    }
                    app.searchApp.highlightGeographicFilter(null, a);
                }
            }
        },

        /**
         * Deprecated name. It uses new swisstopo APi for geographic instances
         */
        createSearchWFS : function(type, config) {

            var lang = /srv\/([a-z]{3})/.exec(window.location.href)[1];

            var c = new Ext.data.Store({
                id : config.id + "store",
                fields : [ "ID", "LABEL", "BOUNDING" ],
                sortInfo : {
                    id : "ID",
                    field : "LABEL",
                    value : "ID",
                    direction : "ASC"
                },
                reader : new Ext.data.JsonReader({
                    idProperty : 'ID',
                    fields : [ "ID", "LABEL", "BOUNDING" ]
                })
            });

            Ext.Ajax
                    .request({
                        url : "xml.regions.list?categoryId=" + type,
                        disableCaching : false,
                        success : function(r) {
                            var data = [];

                            var i = 0;

                            if (Ext.isIE) {
                                i++;
                            }

                            var children = r.responseXML.childNodes[i].childNodes;
                            Ext
                                    .each(
                                            children,
                                            function(e) {
                                                if (e.hasChildNodes()) {

                                                    var label_ = Ext.DomQuery
                                                            .select(lang, e)[0].textContent
                                                            || Ext.DomQuery
                                                                    .select(
                                                                            lang,
                                                                            e)[0].text;

                                                    if (label_
                                                            && label_.length > 0) {

                                                        if (Ext.DomQuery
                                                                .select(
                                                                        "north",
                                                                        e)[0].textContent) {

                                                            var bbox = "POLYGON ("
                                                                    + Ext.DomQuery
                                                                            .select(
                                                                                    "north",
                                                                                    e)[0].textContent
                                                                    + " "
                                                                    + Ext.DomQuery
                                                                            .select(
                                                                                    "east",
                                                                                    e)[0].textContent
                                                                    + ", "
                                                                    + Ext.DomQuery
                                                                            .select(
                                                                                    "south",
                                                                                    e)[0].textContent
                                                                    + " "
                                                                    + Ext.DomQuery
                                                                            .select(
                                                                                    "west",
                                                                                    e)[0].textContent
                                                                    + ")";
                                                        } else {

                                                            var bbox = "POLYGON ("
                                                                    + Ext.DomQuery
                                                                            .select(
                                                                                    "north",
                                                                                    e)[0].text
                                                                    + " "
                                                                    + Ext.DomQuery
                                                                            .select(
                                                                                    "east",
                                                                                    e)[0].text
                                                                    + ", "
                                                                    + Ext.DomQuery
                                                                            .select(
                                                                                    "south",
                                                                                    e)[0].text
                                                                    + " "
                                                                    + Ext.DomQuery
                                                                            .select(
                                                                                    "west",
                                                                                    e)[0].text
                                                                    + ")";
                                                        }

                                                        data
                                                                .push({
                                                                    "ID" : e
                                                                            .getAttribute("id"),
                                                                    "LABEL" : label_,
                                                                    "BOUNDING" : bbox
                                                                });
                                                    }
                                                }
                                            });

                            c.loadData(data);
                        }
                    });
            OpenLayers.Util.applyDefaults(config, {
                store : c,
                loadingText : OpenLayers.i18n("Searching..."),
                mode : "local",
                hideTrigger : false,
                typeAhead : true,
                anchor : "-10",
                selectOnFocus : true,
                listeners : {
                    keyup : function(e, a) {
                        if (a.ENTER == a.keyCode) {
                            app.searchApp.fireSearch();
                        }
                    }
                }
            });
            var p = new Ext.ux.form.SuperBoxSelect(config);
            var o = function(w) {
                geocat.vectorLayer.removeAllFeatures();
                app.mapApp.getMap().zoomToMaxExtent();
                var format = new OpenLayers.Format.WKT();

                Ext.each(w.usedRecords.items, function(q) {
                    Ext.Ajax.request({
                        url : "region.geom.wkt?id=" + q.get("ID") + "&srs="
                                + app.mapApp.getMap().getProjection(),
                        success : function(r) {
                            feature = format.read(r.responseText);
                            geocat.vectorLayer.addFeatures([ feature ]);

                            app.mapApp.getMap().zoomToExtent(
                                    geocat.vectorLayer.getDataExtent());
                        },
                        disableCaching : false
                    });
                });
            };
            p.on("change", o);
            p.on("removeitem", o);
            p.on("reset", o);
            return {
                combo : p,
                store : c,
                refreshContour : function() {
                    o(this.combo);
                }
            }
        },

        getGemeindenCombo : function(a) {
            if (a && this.gemeindenCombo) {
                this.gemeindenCombo.combo.destroy();
                delete this.gemeindenCombo
            }
            if (this.gemeindenCombo) {
                return this.gemeindenCombo
            }
            this.gemeindenCombo = this.createSearchWFS("gemeinden", {
                id : "gemeindenComboBox",
                fieldLabel : OpenLayers.i18n("city"),
                searchField : "LABEL",
                displayField : "LABEL",
                valueField : "ID",
                listWidth : 200,
                name : "gemeinden",
                loadingText : "Searching...",
                triggerAction : "all",
                minChars : 1,
                anchor : "-10"
            });

            return this.gemeindenCombo
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
        
        getArchivedStore : function(defaultValue) {
            return new Ext.data.ArrayStore(
                    {
                        id : 0,
                        fields : [ 'id', 'name' ],
                        data : [
                                [ '', OpenLayers.i18n('includearchived') ],
                                [ 'n',OpenLayers.i18n('excludearchived') ],
                                [ 'y',OpenLayers.i18n('onlyarchived') ] ]
                    });
        },

        drawWherePolygon : function() {

            if (!this.drawControl) {
                this.drawControl = new OpenLayers.Control.DrawFeature(
                        geocat.vectorLayer, OpenLayers.Handler.Polygon);

                var featureAdded = function(f) {
                    app.searchApp.drawControl.deactivate();

                    var span = Ext.get("drawPolygonSpan");
                    Ext.DomHelper.overwrite(span, '<span id="drawPolygonSpan">'
                            + '<a href="' + 'javascript:app.searchApp.'
                            + 'updateWhereForm(\'polygon\')(true, true)">'
                            + OpenLayers.i18n('deletePolygonHelp')
                            + '</a></span>');

                };
                this.drawControl.events.on({
                    'featureadded' : featureAdded
                });
                app.mapApp.getMap().addControl(this.drawControl);
            }
            this.drawControl.activate();
            geocat.vectorLayer.removeAllFeatures();
            if (geocat.selectionFeature) {
                geocat.vectorLayer.destroyFeatures(geocat.selectionFeature);
                geocat.selectionFeature = null;
            }
            var span = Ext.get("drawPolygonSpan");
            Ext.DomHelper.overwrite(span, '<span id="drawPolygonSpan">'
                    + OpenLayers.i18n('startNewPolygonHelp') + '</span>');
        },

        /**
         * Update the hightlight layer to show the geographic filters.
         */
        highlightGeographicFilter : function(event, mode) {
            var selLayer = geocat.selectionHighlightLayer;
            var values = GeoNetwork.util.SearchTools
                    .getFormValues(this.advSearchForm);

            if (mode == null) {
                mode = values.whereType;
            }

            if (mode != 'polygon') {
                geocat.vectorLayer.destroyFeatures();
                geocat.selectionFeature = null;

                if (this.drawControl) {
                    this.drawControl.deactivate();
                }
            }

            if (mode == 'gg25') {
                var ids = null;
                var layer = null;
                var layerFilter = null;
                if (values.gemeinden) {
                    ids = values.gemeinden;
                    layer = 'chtopo:gemeindenBB';
                    layerFilter = "OBJECTVAL";
                } else if (values.kantone) {
                    ids = values.kantone;
                    layer = 'chtopo:kantoneBB';
                    layerFilter = "KANTONSNR";
                } else if (values.country) {
                    ids = values.country;
                    layer = 'chtopo:countries';
                    layerFilter = "LAND";
                }
                if (ids) {
                    ids = ids.split(",");

                    var wmsFilter = new OpenLayers.Filter.Logical({
                        type : OpenLayers.Filter.Logical.OR,
                        filters : []
                    });
                    for ( var i = 0; i < ids.length; ++i) {
                        var id = ids[i];
                        if (id) {
                            wmsFilter.filters
                                    .push(new OpenLayers.Filter.Comparison(
                                            {
                                                type : OpenLayers.Filter.Comparison.EQUAL_TO,
                                                property : layerFilter,
                                                value : id
                                            }));
                        }
                    }

                    selLayer.params.LAYERS = layer;
                    selLayer.params.FILTER = new OpenLayers.Format.XML()
                            .write(new OpenLayers.Format.Filter()
                                    .write(wmsFilter));
                    if (selLayer.getVisibility()) {
                        selLayer.redraw();
                    } else {
                        selLayer.setVisibility(true);
                    }

                } else {
                    selLayer.setVisibility(false);
                }

            } else if (mode == 'bbox') {
                selLayer.setVisibility(false);
                geocat.selectionFeature = new OpenLayers.Feature.Vector(
                        app.mapApp.getMap().getExtent().toGeometry(), {},
                        geocat.selectionStyle);
                geocat.vectorLayer.addFeatures(geocat.selectionFeature);

            } else if (mode == 'polygon') {
                selLayer.setVisibility(false);

                this.drawControl.activate();

            } else {
                selLayer.setVisibility(false);
            }
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
                data : [ [ "within", OpenLayers.i18n('withinGeo') ],
                        [ "intersects", OpenLayers.i18n('intersectGeo') ],
                        [ "crosses", OpenLayers.i18n('containsGeo') ] ]
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

            var id = 'kantoneComboBox';

            if (Ext.getCmp(id)) {
                var d = new Date();
                var n = d.getMilliseconds();
                id = id + n;
            }

            if (createNew) {
                return this.createSearchWFS('kantone', {
                    id : id,
                    fieldLabel : OpenLayers.i18n('kantone'),
                    displayField : 'LABEL',
                    valueField : 'ID',
                    sortField : 'LABEL',
                    name : 'kantone',
                    triggerAction : 'all',
                    minChars : 1,
                    anchor : '-10'
                });
            } else {
                return Ext.query("*[id^=kantoneComboBox]");
            }
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
                	Ext.getCmp("previousBt_up").handler();},
                scope : this
            });

            var nextAction = new Ext.Action({
                id : 'nextBt',
                text : '&gt;&gt;',
                handler : function() {
                	Ext.getCmp("nextBt_up").handler();
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
                tpl : GeoNetwork.Geocatch.Templates.FULL,
                templates : {
                    SIMPLE : GeoNetwork.Templates.SIMPLE,
                    THUMBNAIL : GeoNetwork.Templates.THUMBNAIL,
                    FULL : GeoNetwork.Geocatch.Templates.FULL
                },
                featurecolor : GeoNetwork.Settings.results.featurecolor,
                colormap : GeoNetwork.Settings.results.colormap,
                featurecolorCSS : GeoNetwork.Settings.results.featurecolorCSS
            });

            catalogue.resultsView = metadataResultsView;

            // Add results to map
            // Ext.each(app.mapApp.maps, function(map) {
            // catalogue.resultsView.addMap(map, true);
            // });
            // TODO add map

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
                anchor : "100% 100%",
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

            showSearch();

            // Ext.state.Manager.getProvider().updateLastSearch(query);
            // Show "List results" panel
            var facetPanel = Ext.getCmp('facets-panel');
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

            if (Ext.getCmp('E_sortBy')) {
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
                // Swisstopo: show always the action menu in metadata results
                //Ext.each(Ext.DomQuery.select('.md-action-menu'), function(el) {
                //    hide(el);
                //});
            }
        },
        generateFacetedSearchPanel : function() {
            var breadcrumb = new Ext.Panel({
                id : 'bread-crumb',
                renderTo : 'bread-crumb-div',
                layout : 'table',
                cls : 'breadcrumb',
                defaultType : 'button',
                border : false,
                split : false,
                layoutConfig : {
                    columns : 1
                }
            });

            var facetsPanel = new GeoNetwork.FacetsPanel({
                id : 'facets-panel',
                renderTo : 'facets-panel-div',
                // renderTo: 'search-filter',
                breadcrumb : breadcrumb,
                maxDisplayedItems : GeoNetwork.Settings.facetMaxItems || 7,
                facetListConfig : GeoNetwork.Settings.facetListConfig || []
            });
        },
        indexSelectionAction : function () {
            var toText = function (xml) {
                if (!xml) {
                    return "";
                }
                if (xml.textContent === undefined) {
                    return xml.innerText;
                } else {
                    return xml.textContent;
                }
            };
            var getElementsByTagName = function (xml, name) {
                if (!xml) {
                    return [];
                }
                if (xml.getElementsByTagName === undefined) {
                    return [];
                } else {
                    return xml.getElementsByTagName(name);
                }
            };
            var url = catalogue.services.rootUrl + "metadata.selection.index";
            Ext.Ajax.request({
                url: url,
                method: "GET",
                success : function(response) {
                    var error = getElementsByTagName(response.responseXML, "error");

                    if (error.length > 0) {
                        Ext.MessageBox.alert(OpenLayers.i18n("error"), toText(error[0]));
                    } else {
                        var text = toText(response.responseXML.firstChild.attributes.item(0));
                        Ext.MessageBox.alert("", OpenLayers.i18n("indexSelectionRunning")+text);
                    }
                },
                failure : function(response) {
                    var msgs = getElementsByTagName(response.responseXML, "message");

                    var msg;
                    if (msgs.length > 0) {
                        msg = OpenLayers.i18n("indexSelectionError") + toText(msgs[0]);
                    } else {
                        msg = OpenLayers.i18n("indexSelectionError") + OpenLayers.i18n("error");
                    }
                    Ext.MessageBox.alert(OpenLayers.i18n("error"), msg);
                },
            });
        }
    };

};
