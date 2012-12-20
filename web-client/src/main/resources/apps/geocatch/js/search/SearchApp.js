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

            this.advSearchForm = new GeoNetwork.SearchFormPanel({
                id : 'advanced-search-options-content-form',
                renderTo : 'advanced-search-options-content',
                width : 250,
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
                items : this.generateAdvancedSearchForm(),
            });

            hideAdvancedSearch();

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
                // TODO: Check why if not set explicit width takes size much
                // bigger than panel
                /* anchor : '100%', */
                width : 180,
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
                emptyText : OpenLayers.i18n('any2'),
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

        /**
         * api:method[generateAdvancedSearchForm]
         * 
         * Creates advanced search form
         */
        generateAdvancedSearchForm : function() {
            var f = [ {
                fieldLabel : OpenLayers.i18n("searchText"),
                id : "anyField",
                anchor : "100%",
                name : "T_AnyText"
            }, {
                fieldLabel : OpenLayers.i18n("rtitle"),
                name : "T_title",
                anchor : "100%",
                id : "TitleField"
            }, {
                fieldLabel : OpenLayers.i18n("abstract"),
                name : "T_abstract",
                anchor : "100%",
                id : "AbstractField"
            }, new Ext.ux.form.SuperBoxSelect({
                fieldLabel : OpenLayers.i18n("keyword"),
                id : "keywordsCombo",
                name : "[E1.0_keyword",
                store : this.createKeywordsStore(),
                mode : "local",
                displayField : "name",
                valueField : "value",
                forceSelection : false,
                triggerAction : "all",
                selectOnFocus : true,
                anchor : "100%"
            }), new Ext.ux.form.SuperBoxSelect({
                fieldLabel : OpenLayers.i18n("theme"),
                name : "[E1.0_topicCat",
                id : "topicCat",
                store : new Ext.data.SimpleStore({
                    data : OpenLayers.i18n("topicCat"),
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
                anchor : "100%"
            }), {
                fieldLabel : OpenLayers.i18n("contact"),
                anchor : "100%",
                name : "T_creator"
            }, {
                fieldLabel : OpenLayers.i18n("organisationName"),
                anchor : "100%",
                name : "T_orgName"
            } ];

            f = f.concat([
                    {
                        xtype : "combo",
                        fieldLabel : OpenLayers.i18n("template"),
                        anchor : "100%",
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
                        hidden: !catalogue.isIdentified()
                    }, {
                        fieldLabel : OpenLayers.i18n("identifier"),
                        anchor : "100%",
                        name : "S_basicgeodataid",
                        hidden: !catalogue.isIdentified()
                    }, new Ext.ux.form.SuperBoxSelect({
                        id : "formatCombo",
                        fieldLabel : OpenLayers.i18n("formatTxt"),
                        name : "E1.0_format",
                        store : OpenLayers.i18n("formats"),
                        mode : "local",
                        displayField : "label",
                        valueField : "name",
                        emptyText : OpenLayers.i18n("any"),
                        typeAhead : true,
                        forceSelection : true,
                        triggerAction : "all",
                        selectOnFocus : true,
                        anchor : "100%",
                        hidden: !catalogue.isIdentified()
                    }) ])

            f.push({
                xtype : "hidden",
                name : "E_similarity",
            // FIXME
            // value : searchTools.DEFAULT_SIMILARITY
            });
            var d = [ {
                xtype : "fieldset",
                title : OpenLayers.i18n("what"),
                autoHeight : true,
                defaultType : "textfield",
                labelWidth : this.labelWidth,
                layout : "form",
                layoutConfig : {
                    labelSeparator : ""
                },
                cls : "compressedFieldSet",
                items : f
            } ];
            var c = [ this.getTypeCombo() ];
            c = c.concat([
                    {
                        xtype : "combo",
                        fieldLabel : OpenLayers.i18n("valid"),
                        anchor : "100%",
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
                        hidden: !catalogue.isIdentified()
                    }, /*{
                        xtype : "checkbox",
                        fieldLabel : OpenLayers.i18n("toEdit"),
                        id : "toEdit",
                        name : "B_toEdit",
                        hidden: !catalogue.isIdentified()
                    }, {
                        xtype : "checkbox",
                        fieldLabel : OpenLayers.i18n("toPublish"),
                        id : "toPublish",
                        name : "B_toPublish",
                        hidden: !catalogue.isIdentified()
                    }*/ ]);

            d.push({
                xtype : "fieldset",
                labelWidth : this.labelWidth,
                title : OpenLayers.i18n("type") + "?",
                autoHeight : true,
                defaultType : "textfield",
                cls : "compressedFieldSet",
                layout : "form",
                layoutConfig : {
                    labelSeparator : ""
                },
                items : c
            });
            var b = new Ext.form.ComboBox({
                fieldLabel : OpenLayers.i18n("country"),
                name : "country",
                store : this.createCountryStore(),
                mode : "local",
                displayField : "name",
                valueField : "value",
                emptyText : OpenLayers.i18n("any"),
                forceSelection : true,
                triggerAction : "all",
                selectOnFocus : true,
                anchor : "100%"
            });
            var a = this.getKantoneCombo(true);
            var e = this.getGemeindenCombo(true);
            b.on("select", function(h, g) {
                this.getKantoneCombo().combo.setValue("");
                delete this.getKantoneCombo().combo.lastQuery;
                this.getGemeindenCombo().combo.setValue("");
                delete this.getGemeindenCombo().combo.lastQuery;
                if (g && g.get("bbox")) {
                    app.mapApp.getMap().zoomToExtent(g.get("bbox"))
                }
                var i = g && g.get("name") == "LI";
                this.getKantoneCombo().combo.setDisabled(i);
                this.getGemeindenCombo().combo.setDisabled(i);
                highlightGeographicFilter()
            }, this);
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
                        layoutConfig : {
                            labelSeparator : ""
                        },
                        items : [
                                {
                                    xtype : "radiogroup",
                                    hideLabel : true,
                                    vertical : true,
                                    columns : 1,
                                    defaults : {
                                        name : "whereType",
                                        boxLabel : "",
                                        itemCls : "compressedFormItem"
                                    },
                                    items : [
                                            {
                                                inputValue : "none",
                                                boxLabel : OpenLayers.i18n("wherenone"),
                                                checked : true,
                                                listeners : {
                                                    check : app.searchApp
                                                            .updateWhereForm("none")
                                                }
                                            },
                                            {
                                                inputValue : "bbox",
                                                boxLabel : OpenLayers.i18n("bbox"),
                                                listeners : {
                                                    check : app.searchApp
                                                            .updateWhereForm("bbox")
                                                }
                                            },
                                            {
                                                inputValue : "gg25",
                                                boxLabel : OpenLayers.i18n("adminUnit"),
                                                listeners : {
                                                    check : app.searchApp
                                                            .updateWhereForm("gg25")
                                                }
                                            },
                                            {
                                                inputValue : "polygon",
                                                boxLabel : OpenLayers.i18n("drawOnMap"),
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
                                    items : [ b, a.combo, e.combo ]
                                },
                                {
                                    xtype : "panel",
                                    id : "drawPolygon",
                                    border : false,
                                    hidden : true,
                                    html : '<span id="drawPolygonSpan"><a href="javascript:geocat.drawWherePolygon()">'
                                            + OpenLayers.i18n("startNewPolygon")
                                            + "</a></span>"
                                },
                                {
                                    xtype : "combo",
                                    store : [
                                            [ OpenLayers.Filter.Spatial.WITHIN,
                                                    OpenLayers.i18n("withinGeo") ],
                                            [
                                                    OpenLayers.Filter.Spatial.INTERSECTS,
                                                    OpenLayers.i18n("intersectGeo") ],
                                            [
                                                    OpenLayers.Filter.Spatial.CONTAINS,
                                                    OpenLayers.i18n("containsGeo") ] ],
                                    hideTrigger : true,
                                    forceSelection : true,
                                    editable : false,
                                    triggerAction : "all",
                                    selectOnFocus : true,
                                    fieldLabel : OpenLayers.i18n("type"),
                                    name : "boundingRelation",
                                    value : OpenLayers.Filter.Spatial.WITHIN
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
                layout : "form",
                labelWidth : this.labelWidth,
                layoutConfig : {
                    labelSeparator : ""
                },
                items : [ {
                    xtype : "datefield",
                    fieldLabel : OpenLayers.i18n("from"),
                    format : "d/m/Y",
                    postfix : "T00:00:00",
                    name : ">=_TempExtent_end"
                }, {
                    xtype : "datefield",
                    fieldLabel : OpenLayers.i18n("to"),
                    format : "d/m/Y",
                    postfix : "T23:59:59",
                    name : "<=_TempExtent_begin"
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
                items : [ new Ext.ux.form.SuperBoxSelect({
                    fieldLabel : OpenLayers.i18n("catalog"),
                    name : "[V_",
                    store : OpenLayers.i18n("sources_groups"),
                    mode : "local",
                    displayField : "label",
                    valueField : "name",
                    typeAhead : true,
                    forceSelection : true,
                    triggerAction : "all",
                    selectOnFocus : true,
                    anchor : "100%"
                }) ]
            });
            return d;

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
                anchor : "100%"
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
                        geocat.map.events.register("moveend", null,
                                geocat.highlightGeographicFilter);
                        break;
                    case "gg25":
                        d.setVisible(true);
                        e.setVisible(false);
                        geocat.map.events.unregister("moveend", null,
                                geocat.highlightGeographicFilter);
                        break;
                    case "polygon":
                        d.setVisible(false);
                        e.setVisible(true);
                        geocat.map.events.unregister("moveend", null,
                                geocat.highlightGeographicFilter);
                        if (geocat.selectionFeature) {
                            geocat.vectorLayer
                                    .destroyFeatures(geocat.selectionFeature);
                            geocat.selectionFeature = null
                        }
                        geocat.drawWherePolygon();
                        break
                    }
                    geocat.highlightGeographicFilter(null, a);
                    geocat.fixLayout()
                }
            }
        },
        createSearchWFS : function(n, m, k, g, a, d) {
            var h = [];
            var j = "";
            for ( var e = 0; e < g.length; ++e) {
                var b = g[e];
                if (d != undefined && d[b] != undefined) {
                    h.push({
                        name : b,
                        mapping : b,
                        convert : d[b]
                    })
                } else {
                    h.push({
                        name : b,
                        mapping : b
                    })
                }
                j += "    <ogc:PropertyName>" + m + ":" + b
                        + "</ogc:PropertyName>"
            }
            var l = Ext.data.Record.create(h);
            var f = new Ext.data.XmlReader({
                record : k,
                id : "@fid"
            }, l);
            var c;
            if (n) {
                c = new Ext.data.Store({
                    reader : f,
                    sortInfo : {
                        field : a.displayField,
                        direction : "ASC"
                    }
                });
                searchTools.readWFS(geocat.geoserverUrl + "/wfs", m, k, g,
                        null, {
                            success : function(i) {
                                c.loadData(i.responseXML);
                                c.add(new l({}))
                            }
                        })
            } else {
                c = new Ext.data.Store({
                    reader : f,
                    sortInfo : {
                        field : a.displayField,
                        direction : "ASC"
                    },
                    load : function(i) {
                        i = i || {};
                        if (this.fireEvent("beforeload", this, i) !== false) {
                            this.storeOptions(i);
                            var r = this.baseParams[p.queryParam];
                            var q = new OpenLayers.Filter.Comparison({
                                type : OpenLayers.Filter.Comparison.LIKE,
                                property : a.searchField || a.displayField,
                                value : r.toLowerCase() + ".*"
                            });
                            if (a.updateFilter) {
                                q = a.updateFilter.call(p, q)
                            }
                            searchTools.readWFS(geocat.geoserverUrl + "/wfs",
                                    m, k, g, q, {
                                        success : function(s) {
                                            c.loadData(s.responseXML);
                                            c.add(new l({}))
                                        }
                                    });
                            return true
                        } else {
                            return false
                        }
                    }
                })
            }
            OpenLayers.Util.applyDefaults(a, {
                store : c,
                loadingText : "Searching...",
                mode : n ? "local" : "remote",
                hideTrigger : false,
                typeAhead : true,
                anchor : "100%",
                selectOnFocus : true
            });
            var p = new Ext.ux.form.SuperBoxSelect(a);
            var o = function(w) {
                var r = w.getRecords();
                if (r.length == 0) {
                    return;
                }
                var v = new OpenLayers.Format.WKT();
                var x = null;
                for ( var t = 0; t < r.length; ++t) {
                    var q = r[t];
                    if (q.get("BOUNDING")) {
                        var s = v.read(q.get("BOUNDING"));
                        if (x) {
                            x.extend(s.geometry.getBounds())
                        } else {
                            x = s.geometry.getBounds()
                        }
                    }
                }
                try {
                    if (x) {
                        app.mapApp.getMap().zoomToExtent(x)
                    }
                } catch (u) {
                }
            };
            p.on("change", o);
            return {
                combo : p,
                store : c,
                refreshContour : function() {
                    o(p)
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
            this.gemeindenCombo = this
                    .createSearchWFS(
                            false,
                            "chtopo",
                            "gemeindenBB",
                            [ "GEMNAME_L", "GEMNAME", "OBJECTVAL", "KANTONSNR",
                                    "BOUNDING" ],
                            {
                                id : "gemeindenComboBox",
                                fieldLabel : OpenLayers.i18n("city"),
                                searchField : "GEMNAME_L",
                                displayField : "GEMNAME",
                                valueField : "OBJECTVAL",
                                listWidth : 200,
                                name : "gemeinden",
                                loadingText : "Searching...",
                                triggerAction : "all",
                                minChars : 1,
                                anchor : "100%",
                                updateFilter : function(d) {
                                    if (this.kantonFilter) {
                                        var b = new OpenLayers.Filter.Logical(
                                                {
                                                    type : OpenLayers.Filter.Logical.OR,
                                                    filters : []
                                                });
                                        for ( var c = 0; c < this.kantonFilter.length; ++c) {
                                            b.filters
                                                    .push(new OpenLayers.Filter.Comparison(
                                                            {
                                                                type : OpenLayers.Filter.Comparison.EQUAL_TO,
                                                                property : "KANTONSNR",
                                                                value : this.kantonFilter[c]
                                                                        .get("KANTONSNR")
                                                            }))
                                        }
                                        return new OpenLayers.Filter.Logical(
                                                {
                                                    type : OpenLayers.Filter.Logical.AND,
                                                    filters : [ d, b ]
                                                })
                                    } else {
                                        return d
                                    }
                                }
                            }, {
                                GEMNAME : function(n, g) {
                                    if (!n) {
                                        return ""
                                    }
                                    var j = n.indexOf('"');
                                    var o = n.lastIndexOf('"');
                                    var l = n.indexOf("<");
                                    var d = n.lastIndexOf(">");
                                    if (l > -1) {
                                        var k = "<data>"
                                                + n.substring(l, d + 1)
                                                + "</data>";
                                        var f = searchTools.loadXMLString(k);
                                        var i = geocat.language.substring(0, 2)
                                                .toUpperCase();
                                        var m = Ext.DomQuery.selectValue(
                                                "//DE", f, n);
                                        var b = Ext.DomQuery.selectValue(
                                                "//EN", f, m);
                                        var h = Ext.DomQuery.selectValue(
                                                "//FR", f, b);
                                        var e = Ext.DomQuery.selectValue(
                                                "//IT", f, h);
                                        var c = Ext.DomQuery.selectValue("//"
                                                + i, f, e);
                                        return c
                                    } else {
                                        if (j > -1) {
                                            return n.substring(j, o + 1)
                                        } else {
                                            return n
                                        }
                                    }
                                }
                            });
            this.gemeindenCombo.combo.on("change", function(c) {
                if (c.getRecords().length == 0) {
                    var b = this.getKantoneCombo().combo.getValue();
                    if (b) {
                        this.getKantoneCombo().refreshContour()
                    }
                }
                highlightGeographicFilter()
            }, this);
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
                fieldLabel : OpenLayers.i18n('kantone'),
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
                        app.mapApp.getMap().zoomToExtent(bbox);
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
                anchor: "100% 100%",
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
                                catalogue.startRecord + 10 > catalogue.metadataStore.totalLength);
            }

            if (Ext.getCmp('nextBt_up')) {
                Ext
                        .getCmp('nextBt_up')
                        .setDisabled(
                                catalogue.startRecord + 10 > catalogue.metadataStore.totalLength);
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
                Ext.each(Ext.DomQuery.select('.md-action-menu'), function(el) {
                    hide(el);
                });
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

            var facetsPanel = new GeoNetwork.FacetsPanel(
                    {
                        id : 'facets-panel',
                        renderTo : 'facets-panel-div',
                        //renderTo: 'search-filter',
                        searchForm : Ext
                                .getCmp('advanced-search-options-content-form'),
                        breadcrumb : breadcrumb,
                        maxDisplayedItems : GeoNetwork.Settings.facetMaxItems || 7,
                        facetListConfig : GeoNetwork.Settings.facetListConfig
                                || []
                    });
        }
    };

};
