/** copied from metadata-show.js for massive updates */

function setAll(id) {
    var list = Ext.get(id).select('input').elements;

    for ( var i = 0; i < list.length; i++) {
        list[i].checked = true;
    }
};

function clearAll(id) {
    var list = Ext.get(id).select('input').elements;

    for ( var i = 0; i < list.length; i++) {
        list[i].checked = false;
    }
};

function checkBoxModalUpdate(div, service, modalbox, title) {
    var boxes = Ext.get(div).select('input[type="checkbox"]').elements;
    var pars = "?id=" + $('metadataid').value;
    
    for ( var i = 0; i < boxes.length; i++) {
        var s = boxes[i];
        if (s.checked) {
            pars += "&" + s.name + "=on";
        }
    }
    
    OpenLayers.Request.GET({
        url: service+pars,
        failure: function(req) {
            alert(translate("error") + service + " / status " + req.status
                    + " text: " + req.statusText + " - "
                    + translate("tryAgain"));
        },
        success: function() {}
    });
    
    geocat.massiveOpWindow.close();
    Ext.get("actionOnSelection").dom.selectedIndex = 0;
};




/**
 * Geocat specific stuff
 */
function permlink(url) {
    Ext.MessageBox.show({
        title: translate("permlink"),
        msg: '<a href="'+url+'" target="_newtab">'+url+'</a>',
        animEl: 'mb7'
    });
}

var geocatConf = {
    header : {
        region : 'north',
        contentEl : 'header',
        border : false
    },
    loadingElemId : "loading"
};

var geocat = {
    session: {
        userId: "",
        profile: "",
        groups: []
    },
    map: null,
    mapMinWidth: 310,
    mapMinHeight: 220,
    mapMaxWidth: 500,
    mapInitWidth: 500,
    mapInitHeight: 250,
    mapMaxHeight: 405,
    metadataPopupWidth: 950,
    metadataPopupHeight: 500,
    vectorLayer: null,
    selectionHighlightLayer: null,
    selectionFeature: null,
    baseUrl: null,
    authentified: true,
    expandRefineOnSearch: false,
    expandRefineOnStartup: true,
    /**
     * Default result mode
     */
    resultMode: 'normal',
    resultModes: ['normal'],
//    resultModes: ['normal', 'medium', 'condensed'],
    exportResultModes: ['pdf', 'txt'],
//    exportResultModes: ['pdf', 'txt', 'kml', 'rss'],
    zoomToResults: false,
    maxRecords: [10, 20, 50, 100],
    maxAbstract: 350,
    labelWidth: 100,
    layerTreePopup: null,
    advancedSearchMode: false,
    layersAdded: [],
    formDefaults: {
        anchor: '100%',
        itemCls: 'simpleSearchFormItem'
        /*
        listeners: {
            render: function(field) {
                field.getEl().set({qtip: "tip"});
            }
        }
        */
    },
    contours: null,
    sortBy: 'relevance',
    metadataWindow: null,

    kantoneCombo: null, // singleton
    gemeindeCombo: null, // singleton

    nbResultPerPage: 10,

    drawFeature: null,

    defaultStyleColor: "#FFFFFF",
    defaultStyle: {
        fillColor: "#0000FF",
        fillOpacity: 0.0,
        strokeColor: "#000000",
        strokeOpacity: 1,
        strokeWidth: 1,
        pointRadius: 4,
        pointerEvents: "visiblePainted"
    },
    highlightStyleColor: "#dfe8f6",
    highlightStyle: {
        fillColor: this.highlightStyleColor,
        fillOpacity: 0.4,
        strokeColor: this.highlightStyleColor,
        strokeOpacity: 1,
        strokeWidth: 2,
        pointRadius: 4,
        pointerEvents: "visiblePainted"
    },
    selectionStyle: {
        fillColor: "#FFFF00",
        fillOpacity: 0,
        strokeColor: "#FFFF00",
        strokeOpacity: 1,
        strokeWidth: 3
    },

    queryStyle: {
        fillColor: "#0000FF",
        fillOpacity: 0.2,
        strokeColor: "#0000FF",
        strokeOpacity: 1,
        strokeWidth: 1,
        pointRadius: 4,
        pointerEvents: "visiblePainted"
    },

    refineWidget: {
        refinements: {}
    },

    initialize: function(baseUrl, geoserverUrl, authentified) {
        Ext.QuickTips.init();
        Ext.apply(Ext.QuickTips.getQuickTip(), {
            autoWidth: true,
            trackMouse: true
        });

        Ext.getBody().createChild({id:'map'});
//        var map = geocat.map = createMap('map', false);
        var mapCmp = new MapComponent(null, {drawPanel: false, displayLayertree: false});
        var map = geocat.map = mapCmp.map;

        geocat.baseUrl = baseUrl;
        geocat.geoserverUrl = geoserverUrl;
        geocat.authentified = authentified;

        geocat.selectionHighlightLayer = new OpenLayers.Layer.WMS(
                "Highlight",
                geocat.geoserverUrl + "wms", {
            styles: 'Selection',
            layers: ['chtopo:kantoneBB'],
            format: Ext.isIE6 ? 'image/png8' : 'image/png',
            transparent: true
        }, {
            singleTile: true,
            ratio: 1.0,
            isBaseLayer: false,
            displayInLayerSwitcher: false,
            visibility: false,
            opacity: 1
        });
        map.addLayer(geocat.selectionHighlightLayer);

        geocat.vectorLayer = new OpenLayers.Layer.Vector("Search", {
            displayOutsideMaxExtent: true,
            alwaysInRange: true,
            displayInLayerSwitcher: false,
            styleMap: new OpenLayers.StyleMap({
                'default': geocat.defaultStyle,
                temporary: geocat.selectionStyle,
                highlight: geocat.highlightStyle,
                query: geocat.queryStyle,
                selection: geocat.selectionStyle
            })
        });
        map.addLayer(geocat.vectorLayer);

        geocat.drawFeature = new OpenLayers.Control.DrawFeature(geocat.vectorLayer, OpenLayers.Handler.Polygon, {
            drawFeature: geocat.wherePolygonAdded
        });
        map.addControl(geocat.drawFeature);

        var toolbar = mapCmp.getToolbar();

        this.viewPort = new Ext.Viewport({
            layout: 'border',
            items: [geocatConf.header,{
                region: 'west',
                id: 'westRegion',
                split: true,
                minWidth: geocat.mapMinWidth,
                maxWidth: geocat.mapMaxWidth,
                width: geocat.mapInitWidth,
                border: false,
                layout: 'border',
                items: [
                    {
                        region: 'center',
                        layout: 'border',
                        defaults: {border: false},
                        items: [
                            geocat.createSearchForm.call(geocat),
                            {
                                region: 'south',
                                id: 'searchSwitch',
                                html: '<div id="switchSearchMode">' + geocat.getSimpleSearchSwitch() + '</div>'
                            }
                        ]
                    }, new GeoExt.MapPanel({
                        region: 'south',
                        split: true,
                        height: geocat.mapInitHeight,
                        minHeight: geocat.mapMinHeight,
                        maxHeight: geocat.mapMaxHeight,
                        id: 'mapRegion',
                        layout: 'fit',
                        tbar: mapCmp.getToolbar(), // TODO: add items from fillSearchToolbar?
                        border: false,
                        map: map
                    })
                ]
            },{
                region: 'center',
                id: 'searchResultsPanel',
                autoScroll: true,
                contentEl: 'searchResults',
                bodyStyle: "padding:10px"
            },{
                region: 'east',
                id: 'refineWidget',
                split: true,
                title: translate('refineSearch.title'),
                collapsible: true,
                collapsed: !geocat.expandRefineOnStartup,
                autoScroll: true,
                border: false,
                html: '<div id="refineRemove" class="arrowList"> </div><div id="refineAdd" class="arrowList"> </div>',
                width: 250,
                minWidth: 100,
                maxWidth: 500,
                bodyStyle: "padding:10px"
            }],
            listeners: {
                render: function() {
                    //get rid of the loading mask
                    Ext.get(geocatConf.loadingElemId).remove();

                    Ext.get("searchResults").show(); //search results where with "display:none" to avoid having the loading screen with a scrollbar
                }
            }
        });

        geocat.fixLayout();

        Ext.getCmp('mapRegion').on('resize', function() {
            this.map.updateSize();
        }, geocat);
        map.setCenter(map.getMaxExtent().getCenterLonLat(), 0);
        this.map.updateSize();
        Ext.getCmp('anyField').focus();

        if (geocat.expandRefineOnStartup) {
            geocat.refinementWithNoResultsDisplay();
        }

        mapCmp.zoomToFullExtent(); // Has to be done after layout setup

        // Shows up metadata window if uuid url param supplied
        var urlParams = Ext.urlDecode(location.search.replace('?', ''));
        if (urlParams.uuid) {
            this.openMetadataWindow(urlParams.uuid);
        }

        // Ensure layertree instance is created (c.f. print module)
        //this.createLayerTree();
        // disabled because createLayerTree use mapfish
    },

    createSearchForm: function() {
        var labelWidth = this.labelWidth;
        return {
            region: 'center',
            xtype: 'form',
            autoScroll: true,
            id: 'searchForm',
            defaultType: 'textfield',
            labelWidth: labelWidth,
            bodyStyle:'padding:10px 5px 2px 5px',
            layoutConfig: {
                labelSeparator: ''
            },
            defaults: this.formDefaults,
            items: geocat.getSimpleFormContent.call(geocat),
            listeners: {
                resize: function(component) {
                    geocat.updateComboSizes(component, component.body.dom.clientWidth);
                }
            },
            buttons: [
                //geocat.baseUrl + "srv/" + translate('language') + "/main.search.embedded"
                searchTools.createSearchButton('searchForm', geocat.baseUrl + "srv/" + geocat.language + "/csw",
                        geocat.startSearch,
                        geocat.processSearchResults,
                        geocat.failedSearch,
                        geocat.refineWidget,
                        geocat.addGeographicFilter)
            ]
        };
    },

    startSearch: function() {
        var target = Ext.get("searchResults");

        target.dom.innerHTML = translate("searching");
        target.addClass("searching");
        OpenLayers.Util.getElement("refineRemove").innerHTML="";
        OpenLayers.Util.getElement("refineAdd").innerHTML="";
        // Resizes map
        var mapExtent = geocat.map.getExtent();
        Ext.getCmp('mapRegion').setHeight(geocat.mapMinHeight);
        Ext.getCmp('westRegion').setWidth(geocat.mapMinWidth);
        geocat.viewPort.doLayout();
        geocat.map.zoomToExtent(mapExtent, true);
    },

    failedSearch: function(result) {
        var target = Ext.get("searchResults");
        target.dom.innerHTML = "Search failed: <br />" + result.responseText;
        target.removeClass("searching");
    },

    processSearchResults: function(result, getQuery) {
        var target = Ext.get("searchResults");
        target.removeClass("searching");
        
        geocat.showContours(result.responseXML);
        searchTools.transformXML(result.responseText, geocat.getResultsTemplate(getQuery), target.dom);
        geocat.transformSortBy();
        geocat.transformURIButtons(target.dom);
        geocat.metadataSelectInfo('selected=status');
        geocat.processRefinement(result);

        if (geocat.expandRefineOnSearch)
            Ext.getCmp('refineWidget').expand(true);
    },

    processRefinement: function(result, getQuery) {
        var refineRemove = OpenLayers.Util.getElement("refineRemove");
        refineRemove.innerHTML = "";

        var data = "";
        for (var key in geocat.refineWidget.refinements) {
            var refinementList = geocat.createRemoveCategory(key);
            if (refinementList != "") {
                data += '<tr><td class="refineCategory">' + key + '</td></tr>' +
                        refinementList + '';
            }
        }
        if (data != "") {
            var removeAll = '<tr><td class="removeAll">' + translate('refinements') +
                        '</td><td><a class="removeAll" href="javascript:geocat.clearRefinements()">' + translate('clear') + '</td></tr>';

            refineRemove.innerHTML = '<table class="refineRemove">' + removeAll + data + '</table>';
        }
        var refineAdd = OpenLayers.Util.getElement("refineAdd");
        searchTools.transformXML(result.responseText, geocat.getRefinementTemplate(), refineAdd);
    },

    createRemoveCategory: function(catkey) {
        var category = geocat.refineWidget.refinements[catkey];
        var data = "";
        var index = 0;
        for (var i=0; i<category.length; ++i) {
            var filterMap = category[i];
            for (var filterkey in filterMap) {
                var value = filterMap[filterkey];
                if (value != "") {
                    data += '<tr><td class="refineRemove"><ul class="refineRemove"><li>' + value + '</li></ul></td><td><a class="refineRemove" href="javascript:geocat.removeRefinement(\'' + catkey + '\',' + index + ',\'' + filterkey + '\')">' + translate('remove') + '</a></td></tr>';
                }
            }
            index++;
        }
        return data;
    },

    appendElement: function(parent, html) {
        var newElem = document.createElement('elem');
        newElem.innerHTML = html;
        parent.appendChild(newElem);
    },

    clearRefinements: function() {
        geocat.refineWidget.refinements = {};
        geocat.gotoPage(1);
    },

    refinementWithNoResultsDisplay: function() {
        // Search
        searchTools.doCSWQueryFromForm('searchForm',
                geocat.baseUrl + "srv/" + geocat.language + "/csw", 1,
                geocat.processRefinement,   //success
                function() {                //failure
                    alert ("Search failed.");
                },
                geocat.addGeographicFilter);
        Ext.getCmp('refineWidget').expand(true);
    },

    removeRefinement: function(category, index, key) {
        geocat.refineWidget.refinements[category][index][key] = "";
        geocat.gotoPage(1);
    },

    refine: function(category, kind, value) {
        var refinements = geocat.refineWidget.refinements;
        var array = refinements[category];
        if (array == null) {
            array = [];
            refinements[category] = array;
        }
        var filter = new Object;
        filter[kind] = value;
        array.push(filter);
        geocat.gotoPage(1);
    },

    transformSortBy: function() {
        var target = Ext.get('sortBy');
        if (!target) return;
        new Ext.form.ComboBox({
            renderTo: target,
            store: translate('sortByTypes'),
            mode: 'local',
            id:'sortByCombo',
            width: 140, // does not work for some reason
            forceSelection: true,
            triggerAction: 'all',
            editable: false,
            value: geocat.sortBy,
            listeners: {
                select: function(combo) {
                    var val = combo.getValue();
                    if (val != geocat.sortBy) {
                        geocat.sortBy = val;
                        geocat.gotoPage(1);
                    }
                }
            }
        });
    },

    setLinkIcon: function(proto, link) {
        if (proto == "WMS") {
            link.icon = '../../images/' + 'service.gif';
            link.tooltip = translate("OGC-WMS");
        } else if (proto == "KML") {
            link.icon = '../../images/' + 'google_earth_link_s.png';
            link.tooltip = translate("Google KML");
        } else if (proto.match("^_") || proto == "show") {
        } else {
            link.icon = '../../images/' + 'www.gif';
            link.tooltip = translate("Website");
        }
        link.buttonClass = link.icon ? "x-btn-text-icon" : null;
    },

    createLinkButton: function(curProto, byProto, id) {
        var cur;

        if (byProto.length == 0) {
            return ;
        }
        // Button with drop down list
        if (byProto[0].url != null && ((byProto[0].url.indexOf('metadata.edit')==-1
                && byProto[0].url.indexOf('metadata.delete')==-1
                && byProto[0].url.indexOf('metadata.duplicate.form')==-1
                && byProto[0].url.indexOf('metadata.show')==-1)
                || geocat.resultMode=='condensed')    ) { // TODO : Adding an option for button and icon could be better.
            var items = [];
            var first = byProto[0];

            for (var k = 0; k < byProto.length; ++k) {
                cur = byProto[k];
                geocat.setLinkIcon(curProto, cur);
                items.push({
                    text: cur.title || cur.name || cur.url,
                    handler: function() {
                        geocat.openLink(id, curProto, this/*cur*/);
                    },
                    scope: cur
                });
            }
            var name = '';
            var tooltip = (geocat.resultMode=='condensed' ?
                            '':items.length + " " + translate("links"));
            if (curProto == "WMS") {
                tooltip += " (" + translate("OGC-WMS") + ")";
            } else if (curProto == "KML") {
                tooltip += " (" + translate("Google KML") + ")";
            }

            if(items.length > 0) {
                new Ext.Button({
                    renderTo: id,
                    text: name,
                    tooltip: tooltip,
                    icon: first.icon,
                    cls: first.buttonClass,
                    menu: {
                        shadow:'drop',
                        items: items,
                        listeners: {
                            beforeshow: function(menu) {
                                menu.el.addClass("uriButtonMenu")
                            }
                        }
                    }
                });
            }
        } else {
            cur = byProto[0];

            geocat.setLinkIcon(curProto, cur);
            new Ext.Button({
                renderTo: id,
                text: cur.title || cur.name || cur.url,
                icon: cur.icon,
                cls: cur.buttonClass,
                tooltip: cur.tooltip,
                handler: function() {
                    geocat.openLink(id, curProto, this/*cur*/);
                },
                scope: cur
            });
        }
    },

    transformURIButtons: function(target) {
        var q = Ext.DomQuery;
        var lists = q.select("ul[class=URIButtons]", target);
        var idCount = 0;
        for (var i = 0; i < lists.length; ++i) {
            var uriList = lists[i];
            var uuid = uriList.attributes.getNamedItem("uuid") ? uriList.attributes.getNamedItem("uuid").value : null;
            var uris = q.select("li", uriList);
            var byProtos = {};
            for (var j = 0; j < uris.length; ++j) {
                var uri = uris[j];
                var proto = uri.attributes.getNamedItem("proto").value;
                var rawproto = proto;
                
                if (proto.toLowerCase().contains('wms')) proto = "WMS";
                else if(proto.toLowerCase() != 'show' &&
                		proto.toLowerCase() != '_edit' &&
                		proto.toLowerCase() != '_delete_' &&
                		proto.toLowerCase() != '_duplify') {
                	proto = "";
                }
                var array = byProtos[proto] || [];
                var url = uri.firstChild ? uri.firstChild.nodeValue : null;
                var name = uri.attributes.getNamedItem("name") ? uri.attributes.getNamedItem("name").value : url;
                name = name.trim();
                if(!name || name === "") {
                	name = url;
                }
                var title = uri.attributes.getNamedItem("title") ? uri.attributes.getNamedItem("title").value : name;
                title = title.trim();
                if(!title || title === "") {
                	title = name;
                }
                if(proto === 'WMS') {
                	title = title + " ("+proto+")";
                }
                var cur = {
                    title: title,
                    name: name,
                    url: url,
                    uuid: uuid
                };
                if(cur.url != null && cur.url.trim().length > 0) {
                    array.push(cur);
                }
                byProtos[proto] = array;
            }
            uriList.innerHTML = "";
            for (var curProto in byProtos) {
                var byProto = byProtos[curProto];

                var id = 'uriButton' + (idCount++);
                if (geocat.authentified || !curProto.match("^_")) {
                    Ext.DomHelper.append(uriList, {
                        tag: 'li', id: id
                    });
                    geocat.createLinkButton(curProto, byProto, id);
                }

                if (curProto == "WMS") {
                    id = 'uriButton' + (idCount++);
                    Ext.DomHelper.append(uriList, {
                        tag: 'li', id: id
                    });
                    geocat.createLinkButton("KML", byProto, id);
                }
            }
        }
    },

    updateComboSizes: function(component, width) {
        if (!component.items) return;
        component.items.each(function(item) {
            if (item.isXType('boxselect')) {
                item.setWidth(width - component.labelWidth - 10 - 1 - 5);
            } else if (item.isXType('combo')) {
                // I really hate that but have no other solution at this point.
                // dcorpataux, please explain what those 10, 1 and 5 numbers
                // correspond to
                // [elemoine]
                var IEShift = Ext.isIE ? -3 : -2;

                item.setWidth(width - component.labelWidth - 10 - 5 - 5 - IEShift);
            } else if (item.isXType('fieldset')) {
                item.setWidth(width - 15);
                geocat.updateComboSizes(item, width - 22);
            } else if (item.isXType('panel')) {
                geocat.updateComboSizes(item, width);
            } else if (item.isXType('container') && item.items) {
                geocat.updateComboSizes(item, width - 22);
                //geocat.updateComboSizes(item, item.getEl().getWidth()); doesn't work under IE and FF3
            } else if (item.isXType('radiogroup')) {
                if (item.id == 'fuzzyRadiogroup') return;
                item.setWidth(width - 22);
            }
            // DCX: add checkbox sets
        });
    },

    getTypeCombo: function() {
        return {
            xtype: 'combo',
            fieldLabel: translate('type'),
            name: 'E1.0_type',
            store: translate('dataTypes'),
            mode: 'local',
            //            displayField: 'label',
            //            valueField: 'name',
            value: '',
            emptyText: translate('any'),
            hideTrigger: true,
            forceSelection: true,
            editable: false,
            triggerAction: 'all',
            selectOnFocus: true,
            anchor: '100%'
        };
    },

    /**
     * Method: createKantoneCombo
     *
     * Parameters:
     * - {Boolean} If true, destroys and recreates singleton
     *
     * Returns:
     * An Class Ext.form.ComboBox
     *
     */
    getKantoneCombo: function(createNew) {
        if (createNew && this.kantoneCombo) {
            this.kantoneCombo.combo.destroy();
            delete this.kantoneCombo;
        }
        if (this.kantoneCombo) return this.kantoneCombo; // returns singleton if created
        this.kantoneCombo = geocat.createSearchWFS(false, 'chtopo', 'kantoneBB', ['KUERZEL', 'KANTONSNR', 'BOUNDING'], {
            id: 'kantoneComboBox',
            fieldLabel: translate('kantone'),
            displayField: 'KUERZEL',
            valueField: 'KANTONSNR',
            name: 'kantone',
            triggerAction: 'all',
            minChars: 1,
            anchor: '100%'
        });

        //handle the selection of one kanton: restrict the gemeinden combo
        this.kantoneCombo.combo.on("change", function(combo) {
            var records = combo.getRecords();
            if (records.length > 0) {
                this.getGemeindenCombo().combo.kantonFilter = records;
            } else {
                delete this.getGemeindenCombo().combo.kantonFilter;
            }
            this.getGemeindenCombo().combo.setValue("");
            delete this.getGemeindenCombo().combo.lastQuery;
            geocat.highlightGeographicFilter();
        }, this);

        return this.kantoneCombo;
    },

    /**
     * Method: getGemeindenCombo
     *
     * Parameters:
     * - {Boolean} If true, destroys and recreates singleton
     *
     * Returns:
     * An Class Ext.form.ComboBox
     *
     */
    getGemeindenCombo: function(createNew) {
        if (createNew && this.gemeindenCombo) {
            this.gemeindenCombo.combo.destroy();
            delete this.gemeindenCombo;
        }
        if (this.gemeindenCombo) return this.gemeindenCombo; // returns singleton if created
        this.gemeindenCombo = geocat.createSearchWFS(false, 'chtopo', 'gemeindenBB', ['GEMNAME_L', 'GEMNAME', 'OBJECTVAL', 'KANTONSNR', 'BOUNDING'], {
            id: 'gemeindenComboBox',
            fieldLabel: translate('city'),
            searchField: 'GEMNAME_L',
            displayField: 'GEMNAME',
            valueField: 'OBJECTVAL',
            listWidth: 200,
            name: 'gemeinden',
            loadingText: 'Searching...',
            triggerAction: 'all',
            minChars: 1,
            anchor: '100%',
            updateFilter: function(filter) {
                if (this.kantonFilter) {
                    var kantons = new OpenLayers.Filter.Logical({
                        type: OpenLayers.Filter.Logical.OR,
                        filters: []
                    });
                    for (var i = 0; i < this.kantonFilter.length; ++i) {
                        kantons.filters.push(new OpenLayers.Filter.Comparison({
                            type: OpenLayers.Filter.Comparison.EQUAL_TO,
                            property: "KANTONSNR",
                            value: this.kantonFilter[i].get("KANTONSNR")
                        }));
                    }
                    return new OpenLayers.Filter.Logical({
                        type: OpenLayers.Filter.Logical.AND,
                        filters: [
                            filter,
                            kantons
                        ]
                    });
                } else {
                    return filter;
                }
            }
        }, {
            // conversion function to extract out the correct translated name 
            // from the GEMNAME
            GEMNAME: function (value,record) {
                if (!value) return ""
                
                var firstQuote = value.indexOf('"')
                var lastQuote = value.lastIndexOf('"')
                var firstXBrace = value.indexOf("<")
                var lastXBrace = value.lastIndexOf(">")
                
                if(firstXBrace > -1) 
                {
                    var xmlString = "<data>"+value.substring(firstXBrace, lastXBrace+1)+"</data>";
                    var xml = searchTools.loadXMLString(xmlString);

                    var xpath = geocat.language.substring(0,2).toUpperCase();
                    var de = Ext.DomQuery.selectValue("//DE",xml,value)
                    var en = Ext.DomQuery.selectValue("//EN",xml,de)
                    var fr = Ext.DomQuery.selectValue("//FR",xml,en)
                    var it = Ext.DomQuery.selectValue("//IT",xml,fr)
                    var parsedValue = Ext.DomQuery.selectValue("//"+xpath,xml, it);
                    
                    return parsedValue;
                } 
                else if(firstQuote > -1) 
                {
                    return value.substring(firstQuote, lastQuote+1);
                } else
                {
                    return value; 
                }
            }
        });

        //handle the clearing of the gemeinden combobox: show the Kanton if any is selected
        this.gemeindenCombo.combo.on("change", function(combo) {
            if (combo.getRecords().length == 0) {
                var kantons = this.getKantoneCombo().combo.getValue();
                if (kantons) {
                    this.getKantoneCombo().refreshContour();
                }
            }
            geocat.highlightGeographicFilter();
        }, this);

        return this.gemeindenCombo;
    },


    getSimpleFormContent: function() {
        // the fields are wrapped in a fielset (invisible)
        // for updateComboSizes to do its job properly.
        return [{
            xtype: 'fieldset',
            autoHeight: true,
            defaultType: 'textfield',
            labelWidth: this.labelWidth,
            layout: 'form',
            layoutConfig: {
                labelSeparator: ''
            },
            cls: 'simpleFormFieldset',
            items: [{
		            xtype: 'hidden',
		            name: 'E_similarity',
		            value: searchTools.DEFAULT_SIMILARITY
		        },{
                fieldLabel: translate('searchText'),
                anchor: '100%',
                id: 'anyField',
                name: 'T_AnyText'
            },
                geocat.getTypeCombo(),
                geocat.getKantoneCombo(true).combo,
            {
                // see getAdvancedFormContent
                xtype: 'hidden',
                name: 'boundingRelation',
                value: OpenLayers.Filter.Spatial.WITHIN
            }]
        }];
    },

    getAdvancedFormContent: function() {
        var what = [{
            fieldLabel: translate('searchText'),
            id: 'anyField',
            anchor: '100%',
            name: 'T_AnyText'
        },{
            fieldLabel: translate('rtitle'),
            name: 'T_title',
            anchor: '100%',
            id: 'TitleField'
        },{
            fieldLabel: translate('abstract'),
            name: 'T_abstract',
            anchor: '100%',
            id: 'AbstractField'
        },{
            xtype: 'boxselect',
            fieldLabel: translate('keyword'),
            id: 'keywordsCombo',
            name: '[E1.0_keyword',
            store: geocat.createKeywordsStore(),
            mode: 'local',
            displayField: 'name',
            valueField: 'value',
            forceSelection: false,
            triggerAction: 'all',
            selectOnFocus: true,
            anchor: '100%'
        },{
            xtype: 'boxselect',
            fieldLabel: translate("theme"),
            name: '[E1.0_topicCat',
            id: 'topicCat',
            //store: translate("topicCat"),
            store: new Ext.data.SimpleStore({
                data: translate("topicCat"),
                fields: ["name", "label"],
                sortInfo: { field: "label", direction: "ASC" }
            }),
            mode: 'local',
            displayField: 'label',
            valueField: 'name',
            typeAhead: true,
            forceSelection: true,
            triggerAction: 'all',
            selectOnFocus: true,
            anchor: '100%'
        },{
            fieldLabel: translate('contact'),
            anchor: '100%',
            name: 'T_creator'
        },{
            fieldLabel: translate('organisationName'),
            anchor: '100%',
            name: 'T_orgName'
        }];

        if (geocat.authentified) {
            what = what.concat([{
                xtype: 'combo',
                fieldLabel: translate('template'),
                anchor: '100%',
                name: 'E__isTemplate',
                value: 'n',
                store: [
                        ["n", translate("no")],
                        ["y", translate("yes")]
                ],
                mode: 'local',
                displayField: 'name',
                valueField: 'value',
                hideTrigger: true,
                forceSelection: true,
                editable: false,
                triggerAction: 'all',
                selectOnFocus: true
            },{
                fieldLabel: translate('identifier'),
                anchor: '100%',
                name: 'S_basicgeodataid'
            },{
                xtype: 'boxselect',
                id: 'formatCombo',
                fieldLabel: translate("formatTxt"),
                name: 'E1.0_format',
                store: geocat.sortArryAsc(translate("formats")),
                mode: 'local',
                displayField: 'label',
                valueField: 'name',
                emptyText: translate('any'),
                typeAhead: true,
                forceSelection: true,
                triggerAction: 'all',
                selectOnFocus: true,
                anchor: '100%'
            }]);
        }

        what.push({
            xtype: 'hidden',
            name: 'E_similarity',
            value: searchTools.DEFAULT_SIMILARITY
        });

        var content = [{
            xtype: 'fieldset',
            title: translate('what'),
            autoHeight: true,
            defaultType: 'textfield',
            labelWidth: this.labelWidth,
            layout: 'form',
            layoutConfig: {
                labelSeparator: ''
            },
            cls: 'compressedFieldSet',
            items: what
        }];

        var types = [
            geocat.getTypeCombo()
        ];

        if (geocat.authentified) {
            types = types.concat([{
                xtype: 'combo',
                fieldLabel: translate('valid'),
                anchor: '100%',
                name: 'E__valid',
                store: [
                    ["",  translate('any')],
                    ["1", translate("yes")],
                    ["0", translate("no")],
                    ["-1", translate("unChecked")]
                ],
                mode: 'local',
                displayField: 'name',
                valueField: 'value',
                emptyText: translate('any'),
                hideTrigger: true,
                forceSelection: true,
                editable: false,
                triggerAction: 'all',
                selectOnFocus: true
            },{
                xtype: 'checkbox',
                fieldLabel: translate('toEdit'),
                id: 'toEdit',
                name: 'B_toEdit'
            },{
                xtype: 'checkbox',
                fieldLabel: translate('toPublish'),
                id: 'toPublish',
                name: 'B_toPublish'
            }]);
        }

        content.push({
            xtype: 'fieldset',
            labelWidth: this.labelWidth,
            title: translate('type')+'?',
            autoHeight: true,
            defaultType: 'textfield',
            cls: 'compressedFieldSet',
            layout: 'form',
            layoutConfig: {
                labelSeparator: ''
            },
            items: types
        });

        var countryCombo = new Ext.form.ComboBox({
            fieldLabel: translate('country'),
            name: 'country',
            store: geocat.createCountryStore(),
            mode: 'local',
            displayField: 'name',
            valueField: 'value',
            emptyText: translate('any'),
            forceSelection: true,
            triggerAction: 'all',
            //hidden: true,
            selectOnFocus: true,
            anchor: '100%'
        });

        var kantoneCombo = geocat.getKantoneCombo(true);
        //kantoneCombo.combo.hidden = true;

        var gemeindenCombo = geocat.getGemeindenCombo(true);
//        gemeindenCombo.combo.hidden = true;

        countryCombo.on("select", function(combo, record) {
            this.getKantoneCombo().combo.setValue("");
            delete this.getKantoneCombo().combo.lastQuery;
            this.getGemeindenCombo().combo.setValue("");
            delete this.getGemeindenCombo().combo.lastQuery;

            if (record && record.get("bbox")) {
                geocat.map.zoomToExtent(record.get("bbox"));
            }

            var disableOthers = record && record.get('name') == 'LI';
            this.getKantoneCombo().combo.setDisabled(disableOthers);
            this.getGemeindenCombo().combo.setDisabled(disableOthers);
            geocat.highlightGeographicFilter();
        }, this);

        content.push({
            xtype: 'fieldset',
            labelWidth: this.labelWidth,
            id: 'searchWhere',
            title: translate('where'),
            autoHeight: true,
            defaultType: 'textfield',
            cls: 'compressedFieldSet',
            layout: 'form',
            layoutConfig: {
                labelSeparator: ''
            },
            items: [
                {
                    xtype: 'radiogroup',
                    hideLabel: true,
                    //width: 30,
                    vertical: true,
                    columns: 1,
                    defaults: {
                        name: 'whereType',
                        boxLabel: '',
                        itemCls: 'compressedFormItem'
                    },
                    items: [
                        {
                            inputValue: 'none',
                            boxLabel: translate('wherenone'),
                            checked: true,
                            listeners: {
                                check: geocat.updateWhereForm('none')
                            }
                        }, {
                            inputValue: 'bbox',
                            boxLabel: translate('bbox'),
                            listeners: {
                                check: geocat.updateWhereForm('bbox')
                            }
                        },{
                            inputValue: 'gg25',
                            boxLabel: translate('adminUnit'),
                            listeners: {
                                check:  geocat.updateWhereForm('gg25')
                            }
                        },{
                            inputValue: 'polygon',
                            boxLabel: translate('drawOnMap'),
                            listeners: {
                                check:  geocat.updateWhereForm('polygon')
                            }
                        }
                    ]
                }, {
                    xtype: 'panel',
                    id: 'adminBorders',
                    border: false,
                    layout: 'form',
                    hidden: true,
                    layoutConfig: {
                        labelSeparator: ''
                    },
                    items: [
                        countryCombo,
                        kantoneCombo.combo,
                        gemeindenCombo.combo
                    ]
                }, {
                    xtype: 'panel',
                    id: 'drawPolygon',
                    border: false,
                    hidden: true,
                    html: '<span id="drawPolygonSpan"><a href="javascript:geocat.drawWherePolygon()">'+translate('startNewPolygon')+'</a></span>'
                },{
                    xtype: 'combo',
                    store: [
                        [OpenLayers.Filter.Spatial.WITHIN, translate('withinGeo')],
                        [OpenLayers.Filter.Spatial.INTERSECTS, translate('intersectGeo')],
                        [OpenLayers.Filter.Spatial.CONTAINS, translate('containsGeo')]
                    ],
                    hideTrigger: true,
                    forceSelection: true,
                    editable: false,
                    triggerAction: 'all',
                    selectOnFocus: true,
                    fieldLabel: translate('type'),
                    name: 'boundingRelation',
                    value: OpenLayers.Filter.Spatial.WITHIN
                }
            ],
            listeners: {
                expand: function() {
                    geocat.updateWhereForm('bbox')(null, true);
                },

                collapse: function() {
                }
            }
        });

        content.push({
            xtype: 'fieldset',
            title: translate('when'),
            autoHeight: true,
            defaultType: 'textfield',
            cls: 'compressedFieldSet',
            layout: 'form',
            labelWidth: this.labelWidth,
            layoutConfig: {
                labelSeparator: ''
            },
            items: [{
                xtype: 'datefield',
                fieldLabel: translate('from'),
                format: 'd/m/Y',
                postfix: 'T00:00:00',
                name: '>=_TempExtent_end'  //TODO: don't know how to respect the spec and search in "data des données" if "temporal_extent" is not defined
            },{
                xtype: 'datefield',
                fieldLabel: translate('to'),
                format: 'd/m/Y',
                postfix: 'T23:59:59',
                name: '<=_TempExtent_begin'
            }]
        });

        content.push({
            xtype: 'fieldset',
            labelWidth: this.labelWidth,
            title: translate('source'),
            autoHeight: true,
            defaultType: 'textfield',
            cls: 'compressedFieldSet',
            layout: 'form',
            layoutConfig: {
                labelSeparator: ''
            },
            items: [{
                xtype: 'boxselect',
                fieldLabel: translate("catalog"),
                name: '[V_',
                store: translate("sources_groups"),
                mode: 'local',
                displayField: 'label',
                valueField: 'name',
                typeAhead: true,
                forceSelection: true,
                triggerAction: 'all',
                selectOnFocus: true,
                anchor: '100%'
            }]
        });

        return content;
    },

    updateWhereForm: function(mode) {
        return function(self, checked) {
            var adminBorders = Ext.getCmp('adminBorders');
            var drawPolygon = Ext.getCmp('drawPolygon');
            geocat.drawFeature.deactivate();
            if (checked) {
                switch(mode) {
                    case 'bbox':
                        adminBorders.setVisible(false);
                        drawPolygon.setVisible(false);
                        geocat.map.events.register('moveend', null, geocat.highlightGeographicFilter);
                        break;
                    case 'gg25':
                        adminBorders.setVisible(true);
                        drawPolygon.setVisible(false);
                        geocat.map.events.unregister('moveend', null, geocat.highlightGeographicFilter);
                        break;
                    case 'polygon':
                        adminBorders.setVisible(false);
                        drawPolygon.setVisible(true);
                        geocat.map.events.unregister('moveend', null, geocat.highlightGeographicFilter);
                        if (geocat.selectionFeature) {
                            geocat.vectorLayer.destroyFeatures(geocat.selectionFeature);
                            geocat.selectionFeature = null;
                        }
                        geocat.drawWherePolygon();
                        break;
                }
                geocat.highlightGeographicFilter(null, mode);
                geocat.fixLayout();
            }
        };
    },

    drawWherePolygon: function() {
        geocat.drawFeature.activate();
        if (geocat.selectionFeature) {
            geocat.vectorLayer.destroyFeatures(geocat.selectionFeature);
            geocat.selectionFeature = null;
        }
        var span = Ext.get("drawPolygonSpan");
        Ext.DomHelper.overwrite(span, '<span id="drawPolygonSpan">'+translate('startNewPolygonHelp')+'</span>');
        geocat.fixLayout();
    },

    wherePolygonAdded: function(geometry) {
        geocat.drawFeature.deactivate();
        if (geocat.selectionFeature) {
            geocat.vectorLayer.destroyFeatures(geocat.selectionFeature);
        }
        geocat.selectionFeature = new OpenLayers.Feature.Vector(geometry, {}, geocat.selectionStyle);
        geocat.vectorLayer.addFeatures(geocat.selectionFeature);
        var span = Ext.get("drawPolygonSpan");
        Ext.DomHelper.overwrite(span, '<span id="drawPolygonSpan"><a href="javascript:geocat.drawWherePolygon()">'+translate('startNewPolygon')+'</a></span>');
        geocat.fixLayout();
    },

    fixLayout: function() {
        var form = Ext.getCmp("searchForm");
        form.doLayout();
        form.doLayout();
        geocat.updateComboSizes.defer(0, this, [form, form.body.dom.clientWidth]);
    },

    createCountryStore: function() {
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
                    {name: translate('any'), value: '', bbox: null},
                    {name: 'CH', value: "0", bbox: new OpenLayers.Bounds(485000, 73000, 836000, 297000)},
                    {name: 'LI', value: "1", bbox: new OpenLayers.Bounds(754500, 213000, 767000, 237500)}
                ]
            }
        });
    },

    createKeywordsStore: function() {
        var Keyword = Ext.data.Record.create([
            {name: 'name', mapping:'@name', sortDir: "ASC"},
            {name: 'value', mapping:'@name'}
        ]);
        var keywordStore = new Ext.data.Store({
            reader: new Ext.data.XmlReader({
                record: 'keyword',
                id: '@name'
            }, Keyword),
            proxy: new Ext.data.HttpProxy({
                url: geocat.baseUrl + "srv/" + geocat.language + "/geocat.keywords.list",
                method:'GET',
                disableCaching: false
            })
        });
        keywordStore.add(new Keyword({name: translate('any'), value: ''}));
        keywordStore.load({add: true});

        return keywordStore;
    },

    getSimpleSearchSwitch: function() {
        return '<center><a href="javascript:geocat.switchSearchMode(true)">' + translate("extended") + '</a></center>';
    },

    getAdvancedSearchSwitch: function() {
        return '<center><a href="javascript:geocat.switchSearchMode(false)">' + translate("hideAdvancedOptions") + '</a></center>';
    },

    switchSearchMode: function(advanced) {
        if (advanced == geocat.advancedSearchMode) return;
        var form = Ext.getCmp("searchForm");
        form.items.each(function(item) {
            form.remove(item);
        });

        var items;
        var button = OpenLayers.Util.getElement("switchSearchMode");
        geocat.advancedSearchMode = advanced;
        if (advanced) {
            form.defaults = {};
            button.innerHTML = geocat.getAdvancedSearchSwitch();
            items = geocat.getAdvancedFormContent.call(geocat);
        } else {
            form.defaults = this.formDefaults;
            button.innerHTML = geocat.getSimpleSearchSwitch();
            items = geocat.getSimpleFormContent.call(geocat);
        }
        for (var i = 0; i < items.length; ++i) {
            form.add(items[i]);
        }
        form.doLayout();
        geocat.updateComboSizes(form, form.body.dom.clientWidth);
        Ext.getCmp('anyField').focus();
        geocat.highlightGeographicFilter();
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
    },

    fillSearchToolbar: function(toolbar) {

    },
    
    showContours: function(cswResponse) {
        if (geocat.contours) geocat.vectorLayer.destroyFeatures(geocat.contours);
        geocat.contours = [];
        var wgs84 = new OpenLayers.Projection('EPSG:4326');

        var q = Ext.DomQuery;
        var records = q.select("Record", cswResponse);
        var resultBbox = null;
        for (var i = 0; i < records.length; ++i) {
            var record = records[i];
            var id = q.selectValue("identifier", record);
            var bboxes = q.select("BoundingBox", record);
            for (var j = 0; j < bboxes.length; ++j) {
                var bbox = bboxes[j];
                var ll = q.selectValue("LowerCorner", bbox).split(" ");
                var ur = q.selectValue("UpperCorner", bbox).split(" ");
                var bounds = new OpenLayers.Bounds(ll[0], ll[1], ur[0], ur[1]).transform(wgs84, geocat.map.getProjectionObject());
                var feature = new OpenLayers.Feature.Vector(bounds.toGeometry(), {id: id}, geocat.defaultStyle);
                geocat.contours.push(feature);
                if (resultBbox) {
                    resultBbox.extend(bounds);
                } else {
                    resultBbox = bounds.clone();
                }
            }
        }
        geocat.vectorLayer.addFeatures(geocat.contours);
        if (resultBbox && geocat.zoomToResults) geocat.map.zoomToExtent(resultBbox);
    },

    prevHighlighted: null,
    highlightMapAndDiv: function(id, div) {
        if (geocat.metadataWindow) {
            //the highlight feature is disabled when the metadata window is shown
            return;
        }
        if (id != geocat.prevHighlighted) {
            var features = geocat.contours;
            for (var i = 0; i < features.length; ++i) {
                var feature = features[i];
                if (feature.attributes.id == id) {
                    feature.style = geocat.highlightStyle;
                    geocat.vectorLayer.drawFeature(feature);

                } else {
                    if (feature.style != geocat.defaultStyle) {
                        feature.style = geocat.defaultStyle;
                        geocat.vectorLayer.drawFeature(feature);
                    }
                }
            }
            div.style.background = geocat.highlightStyleColor;
            geocat.prevHighlighted = id;
        }
        if (id == null)
            div.style.background = geocat.defaultStyleColor;
    },

    addGeographicFilter: function(values, filters) {
        var wgs84 = new OpenLayers.Projection('EPSG:4326');

        if (values.whereType == "none") {
            return;
        }

        if (values.whereType=='gg25' || !geocat.advancedSearchMode) {
            var ids = null;
            var idPrefix = null;
            if (values.gemeinden) {
                ids = values.gemeinden;
                idPrefix = 'gemeinden:';
            } else if (values.kantone) {
                ids = values.kantone;
                idPrefix = 'kantone:';
            } else if (values.country) {
                ids = values.country;
                idPrefix = 'country:';
            }
            if (ids) {
                ids = ids.split(",");

                var geoFilter = new OpenLayers.Filter.Logical({
                    type: OpenLayers.Filter.Logical.OR,
                    filters: []
                });
                //TODO: do not use OR, but put all the IDs into a single Spatial, separated by ','
                var geomId = '';
                for (var i = 0; i < ids.length; ++i) {
                    var id = ids[i];
                    if( id ){
                        if( geomId.length > 0){
                            geomId+=',';
                        }

                        geomId += idPrefix + id;
                    }
                }
                if (geomId.length > 0) {
                    var geometry = new searchTools.MultiPolygonReference(geomId);
                    geoFilter.filters.push(new OpenLayers.Filter.Spatial({
                        type: values.boundingRelation,
                        value: geometry,
                        property: 'ows:BoundingBox'
                    }));
                }
                if (geoFilter.filters.length == 1) {
                    filters.push(geoFilter.filters[0]);
                } else {
                    filters.push(geoFilter);
                }

            }

        } else if (values.whereType=='bbox') {
            var extent = geocat.map.getExtent().transform(geocat.map.getProjectionObject(), wgs84);
            filters.push(new OpenLayers.Filter.Spatial({
                type: values.boundingRelation,
                value: extent,
                property: 'ows:BoundingBox'
            }));

        } else if (values.whereType=='polygon' && geocat.selectionFeature) {
            var transformed = geocat.selectionFeature.geometry.clone().transform(geocat.map.getProjectionObject(), wgs84);
            filters.push(new OpenLayers.Filter.Spatial({
                type: values.boundingRelation,
                value: transformed,
                property: 'ows:BoundingBox'
            }));
        }
    },

    /**
     * Update the hightlight layer to show the geographic filters.
     */
    highlightGeographicFilter: function(event, mode) {
        var selLayer = geocat.selectionHighlightLayer;
        var values = searchTools.getFormValues(Ext.getCmp('searchForm'));

        if (mode == null) {
            mode = values.whereType;
        }

        if (mode != 'polygon' && geocat.selectionFeature) {
            geocat.vectorLayer.destroyFeatures(geocat.selectionFeature);
            geocat.selectionFeature = null;
        }

        if (mode == 'gg25' || !geocat.advancedSearchMode) {
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
                    type: OpenLayers.Filter.Logical.OR,
                    filters: []
                });
                for (var i = 0; i < ids.length; ++i) {
                    var id = ids[i];
                    if (id) {
                        wmsFilter.filters.push(new OpenLayers.Filter.Comparison({
                            type: OpenLayers.Filter.Comparison.EQUAL_TO,
                            property: layerFilter,
                            value: id
                        }));
                    }
                }

                selLayer.params.LAYERS = layer;
                selLayer.params.FILTER = new OpenLayers.Format.XML().write(new OpenLayers.Format.Filter().write(wmsFilter));
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
            geocat.selectionFeature = new OpenLayers.Feature.Vector(geocat.map.getExtent().toGeometry(), {}, geocat.selectionStyle);
            geocat.vectorLayer.addFeatures(geocat.selectionFeature);

        } else if (mode == 'polygon') {
            selLayer.setVisibility(false);

        } else {
            selLayer.setVisibility(false);
        }
    },

    zoomTo: function(id) {
        var features = geocat.contours;
        for (var i = 0; i < features.length; ++i) {
            var feature = features[i];
            if (feature.attributes.id == id) {
                geocat.map.zoomToExtent(feature.geometry.getBounds());
            }
        }

    },

    gotoPage: function(recordNum) {
        geocat.startSearch();
        var url = geocat.baseUrl + "srv/" + geocat.language + "/csw";
        searchTools.doCSWQueryFromForm('searchForm', url, recordNum,
                geocat.processSearchResults,
                geocat.failedSearch,
                function(values, resultingFilters) {
                    geocat.addGeographicFilter(values, resultingFilters);
                    for (var key in geocat.refineWidget.refinements) {
                        var refinement = geocat.refineWidget.refinements[key];
                        for(var i=0; i<refinement.length; ++i) {
                            var filter = refinement[i];
                            searchTools.addFiltersFromPropertyMap(filter, resultingFilters);
                        }
                    }
                });
    },

    openLink: function(id, protocol, link) {
        if (protocol == "WMS") {
            // two solutions at this step : 
            // either the user wants to add the layer to the map, 
            // or he wants a clean map
            if (geocat.layersAdded.length == 0) {
                geocat.layersAdded.push(
                    geocat.addLayer(protocol, link.url, link.name, link.title, link.uuid)
                );
            } else {
                // TODO: translations
                Ext.Msg.confirm(translate("addLayerConfirmTitle"), translate("addLayerConfirmText"), function(resp){
                    if (resp == 'yes') {
                        Ext.each(geocat.layersAdded, function(layer) {
                            geocat.map.removeLayer(layer);
                        });
                        geocat.layersAdded = [];
                    } 
                    geocat.layersAdded.push(
                        geocat.addLayer(protocol, link.url, link.name, link.title, link.uuid)
                    );
                });
            }
            
        } else if (protocol == "KML") {
            window.open(geocat.baseUrl + "srv/" + geocat.language + "/google.kml?uuid=" + link.uuid + "&layers=" + link.name, "_blank");
        } else if (protocol.contains("show")) {
            Ext.MessageBox.show({
                title: translate("show"),
                progressText: translate('loading'),
                width:300,
                wait:true,
                waitConfig: {interval:200},
                animEl: 'mb7'
                });


            OpenLayers.Request.GET({
                url: (protocol=="show"?link.url:link),
                success: function(response) {
                    Ext.MessageBox.hide();
                    if (!geocat.metadataWindow) {
                        geocat.metadataWindow = new Ext.Window({
                            title: translate("show"),
                            maximizable: true,
                            html: response.responseText,
                            autoScroll: true,
                            constrain: true,
                            width: geocat.metadataPopupWidth,
                            height: geocat.metadataPopupHeight,
                            onEsc : function(){
                                geocat.metadataWindow.hide();
                            },
                            listeners: {
                                close: function() {
                                    geocat.metadataWindow = null;
                                },
                                show: function(){
                                    // user geocat map handler
                                    searchTools.initMapDiv();
                                }
                            }
                        });
                    } else {
                        geocat.metadataWindow.body.dom.innerHTML = response.responseText;
                    }
                    geocat.metadataWindow.show();
                },
                failure: function() {
                    Ext.MessageBox.hide();
                }
            });
        } else {
            if (protocol.match("_$")) {
                //link to be confirmed
                Ext.Msg.confirm("Confirm", translate('confirmDelete'), function(button) {
                    if (button == "yes") {
                        window.open(link.url, "_blank");
                    }
                });
            } else {
                //standard links
                window.open(link.url, "_blank");
            }
        }
    },

    openMetadataWindow: function(uuid) {
        this.openLink(0, 'showFromUrl', 'metadata.show.embedded?uuid=' + uuid + '&currTab=simple&geocat');
    },

    addLayer: function(protocol, url, name, title, uuid) {
        var bbox = null, layer;
        for (var i = 0; i < geocat.contours.length; ++i) {
            var contour = geocat.contours[i];
            if (contour.attributes.id == uuid) {
                if (bbox) {
                    bbox.extend(contour.geometry.getBounds());
                } else {
                    bbox = contour.geometry.getBounds().clone();
                }
            }
        }
        if (!bbox) bbox = geocat.map.getMaxExtent();

        while (geocat.map.getLayersByName(title).length > 0) {
            title += "'";
        }
        layer = new OpenLayers.Layer.WMS(title, url, {
            layers: [name],
            transparent: true
        }, {
            format: 'image/png',
            isBaseLayer: false,
            singleTile: true,
            buffer: 0,
            ratio: 1.0,
            maxExtent: bbox
        });
        geocat.map.addLayer(layer);
        if (geocat.layerTreePopup) {
            /* disabled because createLayerTree use mapfish
            geocat.layerTreePopup.remove(geocat.layerTreePopup.getComponent(0));
            geocat.layerTreePopup.add(geocat.createLayerTree());
            geocat.layerTreePopup.doLayout();
            */
        }
        return layer;
    },


    /*
     *Check and uncheck metadata in current result page.
     */
    selectAllMetadata: function(on) {
        var checks = document.getElementsByName('metadataSelector');
        var checksLength = checks.length;
        for (var i = 0; i < checksLength; i++) {
            checks[i].checked = on;
        }
    },

    metadataSelectInfo: function(param) {
        OpenLayers.Request.GET({
             url: 'metadata.select?' + param,
             success: function(response) {
                // Get the number of selected items in current session
                var xmlString = response.responseText;
                var xmlobject = searchTools.loadXMLString(xmlString);
                var root = xmlobject.getElementsByTagName('response')[0];
                var nbSelected = root.getElementsByTagName('Selected')[0].firstChild.nodeValue;
                var item = document.getElementById('nbselected');
                if (item != null) {
                    item.innerHTML = nbSelected;
                    if (nbSelected == 0)
                        document.getElementById('actionOnSelection').disabled = true;
                    else
                        document.getElementById('actionOnSelection').disabled = false;
                }
            },
            onFailure: function(response) {
                alert('Error metadata.select ');
            }
        });
    },

    /**
     * Select a metadata record in current user session.
     */
    metadataSelect: function(id, selected) {
            if (selected===true)
                selected='add';
            else if (selected===false)
                selected='remove';

            var param = 'id=' + id + '&selected='+selected;
            geocat.metadataSelectInfo(param);
            if (selected=='remove-all') {
                geocat.selectAllMetadata(false);
            };
            if (selected=='add-all') {
                geocat.selectAllMetadata(true);
            };
    },

    metadataGroupAction: function(select) {
        var action = select.options[select.selectedIndex].value;
        switch (action) {
        case 'EXP':
            document.location.href = 'mef.export?format=full&version=2';
            select.selectedIndex=0;
            break;
        case 'DEL':
            if(!confirm(translate('confirmMassiveDelete')))
                return;
            document.location.href = 'metadata.batch.delete';
            break;
        case 'CATEGORIES':
            geocat.openMassiveOp(translate('updateCategories'),"metadata.batch.category.form");
            break;
        case 'PRIVILEGES':
            geocat.openMassiveOp(translate('updatePrivileges'),"metadata.batch.admin.form");
            break;
        }
    },
    openMassiveOp: function(title, request) {
        OpenLayers.Request.GET({
            url: request,
            failure: function() {alert ("Unable to perform operation");},
            success: function(response) {
                geocat.massiveOpWindow = new Ext.Window({
                    modal: true,
                    title: title,
                    maximizable: false,
                    html: response.responseText,
                    autoScroll: true,
                    constrain: true,
//                    width: geocat.metadataPopupWidth,
//                    height: geocat.metadataPopupHeight,
                    onEsc: function(){
                        geocat.massiveOpWindow.close();
                    },
                    listeners: {
                        close: function() {
                            geocat.massiveOpWindow = null;
                        }
                    }
                });
                geocat.massiveOpWindow.show();
            }
        });
    },
    openMassiveDialog:function(html) {
        
    },
    metadataSelectionAction: function() {
        // TODO : switch to ext.js ?
        var list = '<select id="actionOnSelection" onchange="geocat.metadataGroupAction (this);">' +
                '<option value=""></option><option value="EXP">'+translate('export')+'</option>';
        if (geocat.authentified){
            list += '<option value="DEL">'+translate('delete')+'</option>';
            list += '<option value="PRIVILEGES">'+translate('updatePrivileges')+'</option>';
        }
        list += '</select>';
        return list;
    },

    /**
     * Switch metadata result panel view mode using button ids.
     * Button id MUST be equal to result templates.
     */
    switchResultsMode: function(mode){
        if (geocat.resultMode == mode)
            return;

        geocat.resultMode = mode;
        Ext.getCmp('searchBt').handler();
    },

    /**
     * Display results mode option if more than one mode.
     */
    resultsModeToolBar: function(){
        if (geocat.resultModes.length==1)
            return '';

        var tb = '<span>'+translate('view')+':';
        for (var i=0; i<geocat.resultModes.length; i++) {
            tb += '<img onclick="geocat.switchResultsMode(\'' +
                            geocat.resultModes[i] +
                        '\');" src="../../images/mv-' +
                            geocat.resultModes[i] +
                        '.gif"/>&#160;';
        }
        tb += '</span>';

        return tb;
    },
    /**
     * Return combobox to define the number of max records per page.
     */
    maxRecordsCombo: function(){
        if (geocat.maxRecords.length==1)
            return '';

        var tb = '<span>' + translate('hitsPerPage') + ': <select onchange="geocat.maxRecordsChange(this);">';
        for (var i=0; i<geocat.maxRecords.length; i++) {
            tb += '<option value="' + geocat.maxRecords[i] + '"';

            if (geocat.maxRecords[i] == geocat.nbResultPerPage)
                tb += ' selected="selected"';

            tb += '>' + geocat.maxRecords[i] + '</option>';
        }
        tb += '</select></span>';

        return tb;
    },
    /**
     * Update max records variable and launch a new search.
     */
    maxRecordsChange: function(select){
        var nb = select.options[select.selectedIndex].value;
        if (nb != geocat.nbResultPerPage) {
            geocat.nbResultPerPage = nb;
            Ext.getCmp('searchBt').handler();
        }
    },
    getResultsTemplate: function(getQuery) {
        var newQuery = OpenLayers.Util.applyDefaults({
            maxRecords: 1000,
            startPosition: 1
        }, getQuery);
        var q = OpenLayers.Util.getParameterString(newQuery);
        q = q.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
        var exportLinks = '';

        for (var i=0; i<geocat.exportResultModes.length; i++) {
               var link = '';
               if (geocat.exportResultModes[i]=='txt')
                   link = 'csw.csv';
               else
                   link = 'csw.' + geocat.exportResultModes[i];

               exportLinks += '<a href="' + link + '?' +
                                   (geocat.exportResultModes[i]!='pdf'?'outputSchema=own&amp;':'') +    // Only pdf is based on a common format, others are schemas based.
                                   q + '" target="export" class="export-link"><img height="16px" src="../../images/' +
                                   geocat.exportResultModes[i] + '.gif" ext:qtip="Export '+geocat.exportResultModes[i].toUpperCase()+'"/></a>';
        }


        var xslhead = '<?xml version="1.0" encoding="UTF-8"?>\n' +
               '<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:ext="http://www.sencha.com/products/extjs/" xmlns:geonet="http://www.fao.org/geonetwork" exclude-result-prefixes="dc dct csw ext">\n' +
               '  <xsl:output method="html"/>\n'+
               '  <xsl:template match="/">\n' +
               '    <div>\n'+
               '      <xsl:apply-templates/>\n' +
               '    </div>\n'+
               '  </xsl:template>\n '+
               '  <xsl:template match="csw:SearchResults">\n' +
               '    <div class="resultsHeader">\n'+
               '      <xsl:if test="@numberOfRecordsMatched &gt; 0">\n' +
               '        <div style="float: right;">\n' +
               exportLinks +
               '        </div>\n' +
               '      </xsl:if>' +
               '      <xsl:if test="@numberOfRecordsMatched &gt; 1">\n' +
               '        <div id="sortBy" style="float: right;"></div><div style="float: right">' + translate('sortBy') + ':&#160;</div>\n' +
               '      </xsl:if>\n' +
               '      <xsl:choose>\n' +
               '        <xsl:when test="@numberOfRecordsMatched=0">\n' +
               '          '+translate('nothingFound')+'\n' +
               '        </xsl:when>\n' +
               '        <xsl:when test="@nextRecord!=0">\n' +
               '          ' + translate('resultsMatching') + ' <xsl:value-of select="@nextRecord - @numberOfRecordsReturned"/>-<xsl:value-of select="@nextRecord - 1"/>\n' +
               '          / <xsl:value-of select="@numberOfRecordsMatched"/>\n' +
               '        </xsl:when>\n' +
               '        <xsl:otherwise>\n' +
               '          ' + translate('resultsMatching') + ' 1-<xsl:value-of select="@numberOfRecordsReturned"/>\n' +
               '          / <xsl:value-of select="@numberOfRecordsMatched"/>\n' +
               '        </xsl:otherwise>\n' +
               '      </xsl:choose>\n' +
               '      <br />' +
               '      <xsl:if test="1=2 and @nextRecord - @numberOfRecordsReturned > 1">&#160;<a href="javascript:geocat.gotoPage({@nextRecord - @numberOfRecordsReturned - ' + geocat.nbResultPerPage + '})">&lt; ' + translate('geocatPrevious') + '</a> |&#160;</xsl:if>\n' + //TODO: add anti-XSS protections
               '      <xsl:if test="@nextRecord &gt; 0 and @nextRecord &lt; @numberOfRecordsMatched + 1"><a id="gotoNextPageButton" href="javascript:geocat.gotoPage({@nextRecord})">' + translate('geocatNext') + ' &gt;</a></xsl:if>\n' + //TODO: add anti-XSS protections
               '      <xsl:if test="@numberOfRecordsMatched &gt; 0">\n' +
               '&#160;|&#160;<span id="nbselected"></span> ' + translate("selected") +
               '&#160;|&#160;' + translate("select") + ' ' + '<a href="javascript:geocat.metadataSelect(0, \'add-all\')" title="' + translate('selectAll') + '" alt="' + translate('selectAll') + '">' + translate('all') + '</a>,' +
               '        <a href="javascript:geocat.metadataSelect(0, \'remove-all\')" title="' + translate('selectNone') + '" alt="' + translate('selectNone') + '">' + translate('none') + '</a> ' +
               '        <br /><br />' +
               '<table style="width:100%"><tr><td align="left">'+
               translate('selectedElementsAction') + ':&#160;' +
               '        ' + geocat.metadataSelectionAction() + '&#160;' +
               '</td><td align="right">'+
               geocat.resultsModeToolBar() +
               geocat.maxRecordsCombo() +               
               '</td></tr></table>'+
               '      </xsl:if>\n' +
               '    </div>\n' +

               '    <div style="clear: both" id="records">\n' +
               '      <xsl:apply-templates select="*"/>\n' +
               '    </div>\n' +

               '      <hr/>\n' +
               '      <p align="center">\n' +
               '        <xsl:if test="@nextRecord - @numberOfRecordsReturned > 1">&#160;<a href="javascript:geocat.gotoPage({@nextRecord - @numberOfRecordsReturned - ' + geocat.nbResultPerPage + '})">' + translate('geocatPrevious') + '</a></xsl:if>\n' + //TODO: add anti-XSS protections
               '        <xsl:if test="@nextRecord &gt; 0 and @nextRecord &lt; @numberOfRecordsMatched">&#160;<a href="javascript:geocat.gotoPage({@nextRecord})">' + translate('geocatNext') + '</a></xsl:if>\n' + //TODO: add anti-XSS protections
               '      </p>\n' +
               '  </xsl:template>\n';


           var xslfooter =
           '  <xsl:template match="dc:subject">\n' +
           '      <xsl:if test="position()&gt;1">, </xsl:if>\n' +
           '      <xsl:value-of select="."/>\n' +
           '  </xsl:template>\n' +

           '  <xsl:template match="text()"/>\n' +
           '</xsl:stylesheet>\n';

        // Logo XSL: either group logo (with/without group website link) or harvester (source) logo
        // Be sure to keep in sync with logo XSL in metadata-utils.xsl.
        var xsllogo =
            '<xsl:variable name="source" select="string(geonet:info/source)"/>' +
            '<xsl:variable name="groupLogoUuid" select="string(geonet:info/groupLogoUuid)"/>' +
            '<xsl:variable name="groupWebsite" select="string(geonet:info/groupWebsite)"/>' +
            '<xsl:variable name="groupLabel">' +
            '  <xsl:choose> ' +
            '    <xsl:when test="string(geonet:info/groupLabel)!=\'\'"><xsl:value-of select="string(geonet:info/groupLabel)"/></xsl:when>' +
            '    <xsl:otherwise><xsl:value-of select="string(geonet:info/groupName)"/></xsl:otherwise>' +
            '  </xsl:choose>' +
            '</xsl:variable>' +
            ' <xsl:choose>' +
            '    <xsl:when test="$groupWebsite != \'\' and $groupLogoUuid != \'\'">' +
            '        <a href="{$groupWebsite}" target="_blank">' +
            '            <img src="' + geocat.baseUrl + 'images/logos/{$groupLogoUuid}.png" width="40" title="{$groupLabel}"/>' +
            '       </a>' +
            '   </xsl:when>' +
            '   <xsl:when test="$groupLogoUuid != \'\'">' +
            '       <img src="' + geocat.baseUrl + 'images/logos/{$groupLogoUuid}.png" width="40" title="{$groupLabel}"/>' +
            '   </xsl:when>' +
            '   <xsl:otherwise>' +
            '       <a style="cursor:pointer;cursor:hand" onclick="geocat.openSource(\'{$source}\')" target="_blank">' +
            '           <img src="' + geocat.baseUrl + 'images/logos/{$source}.gif" width="40" title="{$groupLabel}"/>' +
            '       </a>' +
            '   </xsl:otherwise>' +
            '</xsl:choose>';

        if (geocat.resultMode=='condensed') {
            return xslhead +
                '  <xsl:template match="csw:Record">\n' +
                '      <div class="condensed" style="" ondblclick="geocat.zoomTo(\'{dc:identifier}\')" onmouseover="geocat.highlightMapAndDiv(\'{dc:identifier}\', this)" onmouseout="geocat.highlightMapAndDiv(null, this)">\n' +
                '        <xsl:variable name="isSelected" select="geonet:info/selected" />\n' +
                '          <h2><input class="content" type="checkbox" id="chk{geonet:info/id}" name="metadataSelector" onclick="geocat.metadataSelect(\'{geonet:info/uuid}\', this.checked)">' +
                '            <xsl:if test="$isSelected=\'true\'">\n' +
                '              <xsl:attribute name="checked">true</xsl:attribute>\n' +
                '            </xsl:if>\n' +
                '          </input>\n' +
                '           <a href="javascript:geocat.openLink(0, \'showFromUrl\', \'metadata.show.embedded?id={geonet:info/id}&amp;currTab=simple\');"><xsl:value-of select="dc:title"/></a></h2>' +
                xsllogo +
                '        <xsl:if test="dc:URI">\n' +
                '            <xsl:for-each select="dc:URI[contains(@protocol,\'image/\')]">\n' +
                '            <img class="thumbnail" src="{.}"/>\n' +
                '          </xsl:for-each>\n' +
                '         </xsl:if>\n' +
                '        <p>\n' +
                '          <xsl:if test="dc:URI">\n' +
                '           <div class="bt">' +
                '            <ul class="URIButtons" uuid="{dc:identifier}">\n' +
                '              <xsl:for-each select="dc:URI[not(contains(@protocol,\'image/\'))]">\n' +
                '                <li proto="{@protocol}" name="{@name}" title="{@title}"><xsl:value-of select="."/></li>\n' +
                '              </xsl:for-each>\n' +
//                '              <li proto="_edit" title="' + translate("edit") + '">' + geocat.baseUrl + 'srv/' + geocat.language + '/metadata.edit?id=<xsl:value-of select="geonet:info/id"/></li>\n' +
//                '              <li proto="_delete" title="' + translate("delete") + '">' + geocat.baseUrl + 'srv/' + geocat.language + '/metadata.delete?id=<xsl:value-of select="geonet:info/id"/></li>\n' +
//                '              <li proto="_duplicate" title="' + translate("create") + '">' + geocat.baseUrl + 'srv/' + geocat.language + '/metadata.duplicate.form?id=<xsl:value-of select="geonet:info/id"/></li>\n' +
                '            </ul>\n' +
                '           </div>' +
                '          </xsl:if>\n' +
                '        </p>\n' +
                '      </div>\n' +
                '  </xsl:template>\n' +
                xslfooter;
        } else if (geocat.resultMode=='medium') {
            return xslhead +
            '  <xsl:template match="csw:Record">\n' +
            '      <table width="100%" ondblclick="geocat.zoomTo(\'{dc:identifier}\')" onmouseover="geocat.highlightMapAndDiv(\'{dc:identifier}\', this)" onmouseout="geocat.highlightMapAndDiv(null, this)">\n' +
            '        <tr><td style="width:50px;padding:4px;text-align:center;vertical-align:top;">\n' +
                   xsllogo +
            '        </td><td>\n' +
            '          <xsl:variable name="isSelected" select="geonet:info/selected" />\n' +
            '          <h2><input class="content" type="checkbox" id="chk{geonet:info/id}" name="metadataSelector" onclick="geocat.metadataSelect(\'{geonet:info/uuid}\', this.checked)">' +
            '            <xsl:if test="$isSelected=\'true\'">\n' +
            '              <xsl:attribute name="checked">true</xsl:attribute>\n' +
            '            </xsl:if>\n' +
            '          </input>\n' +
            '           <a href="javascript:geocat.openLink(0, \'showFromUrl\', \'metadata.show.embedded?id={geonet:info/id}&amp;currTab=simple\');"><xsl:value-of select="dc:title"/></a></h2>' +
            '          <br/><i>' + translate('abstract') + ': </i>' +
            '           <xsl:choose>' +
            '              <xsl:when test="string-length(dct:abstract) &gt; ' + geocat.maxAbstract + '"><xsl:value-of select="substring(dct:abstract, 0, ' + geocat.maxAbstract + ')"/> ...</xsl:when>' +
            '              <xsl:otherwise><xsl:value-of select="dct:abstract"/></xsl:otherwise>' +
            '           </xsl:choose>' +
            //'           <br/><i>' + translate('keywords') + ': </i><xsl:apply-templates select="dc:subject"/>\n' +
            '          <xsl:if test="dct:modified">\n' +
            '            <br/><i>(' + translate('modified') + ': <xsl:value-of select="dct:modified"/>)</i>\n' +
            '          </xsl:if>\n' +
            '          <br/>\n' +
            '          <ul class="URIButtons" uuid="{dc:identifier}">\n' +
//            Turn off metadata button, as a link on title is available '            <li proto="show" title="' + translate("show") + '">' + geocat.baseUrl + 'srv/' + geocat.language + '/metadata.show.embedded?id=<xsl:value-of select="geonet:info/id"/>&amp;currTab=simple</li>\n' +
            '          <xsl:for-each select="dc:URI">\n' +
            '            <li proto="{@protocol}" name="{@name}" title="{@title}"><xsl:value-of select="."/></li>\n' +
            '          </xsl:for-each>\n' +
            '          <xsl:if test="geonet:info/edit=\'true\' and not(geonet:info/isHarvested=\'y\')">\n' +
            '              <li proto="_edit" title="' + translate("edit") + '">' + geocat.baseUrl + 'srv/' + geocat.language + '/metadata.edit?id=<xsl:value-of select="geonet:info/id"/></li>\n' +
            '              <li proto="_delete_" title="' + translate("delete") + '">' + geocat.baseUrl + 'srv/' + geocat.language + '/metadata.delete?id=<xsl:value-of select="geonet:info/id"/></li>\n' +
            '              <li proto="_duplify" title="' + translate("create") + '">' + geocat.baseUrl + 'srv/' + geocat.language + '/metadata.duplicate.form?id=<xsl:value-of select="geonet:info/id"/></li>\n' +
            '          </xsl:if>\n' +
            '          </ul><br/><br/>\n' +
            '         </td><td>' +
            '        <xsl:if test="dc:URI">\n' +
            '            <xsl:for-each select="dc:URI[contains(@protocol,\'image/\')]">\n' +
            '            <img class="thumbnail" src="{.}"/>\n' +
            '          </xsl:for-each>\n' +
            '         </xsl:if>\n' +
            '        </td></tr>\n' +
            '      </table>\n' +
            '  </xsl:template>\n' +
            xslfooter;
        } else {
            return xslhead +
            '  <xsl:template match="csw:Record">\n' +
            '      <table width="100%" ondblclick="geocat.zoomTo(\'{dc:identifier}\')" onmouseover="geocat.highlightMapAndDiv(\'{dc:identifier}\', this)" onmouseout="geocat.highlightMapAndDiv(null, this)">\n' +
            '        <tr><td style="width:50px;padding:4px;text-align:left;vertical-align:top;">\n' +
                   xsllogo +
            '        </td><td stlye="padding:4px">\n' +
            '          <xsl:variable name="isSelected" select="geonet:info/selected" />\n' +
            '          <h2 style="padding-bottom: 4px"><input style="border:none" class="content" type="checkbox" id="chk{geonet:info/id}" name="metadataSelector" onclick="geocat.metadataSelect(\'{geonet:info/uuid}\', this.checked)">' +
            '            <xsl:if test="$isSelected=\'true\'">\n' +
            '              <xsl:attribute name="checked">true</xsl:attribute>\n' +
            '            </xsl:if>\n' +
            '          </input>&#160;\n' +
            '           <a href="javascript:geocat.openLink(0, \'showFromUrl\', \'metadata.show.embedded?id={geonet:info/id}&amp;currTab=simple\');"><xsl:value-of select="dc:title"/></a>\n' +
            //    Add icon for dataset and service metadata only.
            '          <xsl:if test="dc:type and (dc:type=\'dataset\' or dc:type=\'service\')">&#160;<img src="../../images/{dc:type}.gif" alt="{dc:type}" title="{dc:type}"/></xsl:if>' +
            '          </h2>' +
            '          <br/><i>' + translate('abstract') + ': </i>' +
            '           <xsl:choose>' +
            '              <xsl:when test="string-length(dct:abstract) &gt; ' + geocat.maxAbstract + '"><xsl:value-of select="substring(dct:abstract, 0, ' + geocat.maxAbstract + ')"/> (...)</xsl:when>' +
            '              <xsl:otherwise><xsl:value-of select="dct:abstract"/></xsl:otherwise>' +
            '           </xsl:choose>' +
            //'           <br/><i>' + translate('keywords') + ': </i><xsl:apply-templates select="dc:subject"/>\n' +
            '          <xsl:if test="dct:modified">\n' +
            '            <br/><i>(' + translate('modified') + ': <xsl:value-of select="dct:modified"/>)</i>\n' +
            '          </xsl:if>\n' +
            '          <br/>\n' +
            '          <ul style="padding-top:4px" class="URIButtons" uuid="{dc:identifier}">\n' +
            '            <li proto="show" title="' + translate("show") + '">' + geocat.baseUrl + 'srv/' + geocat.language + '/metadata.show.embedded?id=<xsl:value-of select="geonet:info/id"/>&amp;currTab=simple</li>\n' +
            '          <xsl:for-each select="dc:URI">\n' +
            '              <li proto="{@protocol}" name="{@name}" title="{@title}"><xsl:value-of select="."/></li>\n' +
            '          </xsl:for-each>\n' +
            '          <xsl:if test="geonet:info/edit=\'true\' and not(geonet:info/isHarvested=\'y\')">\n' +
            '              <li proto="_edit" title="' + translate("edit") + '">' + geocat.baseUrl + 'srv/' + geocat.language + '/metadata.edit?id=<xsl:value-of select="geonet:info/id"/></li>\n' +
            '              <li proto="_delete_" title="' + translate("delete") + '">' + geocat.baseUrl + 'srv/' + geocat.language + '/metadata.delete?id=<xsl:value-of select="geonet:info/id"/></li>\n' +
            '              <li proto="_duplify" title="' + translate("create") + '">' + geocat.baseUrl + 'srv/' + geocat.language + '/metadata.duplicate.form?id=<xsl:value-of select="geonet:info/id"/></li>\n' +
            '          </xsl:if>\n' +
            '          </ul><br/>\n' +
            '        </td></tr>\n' +
            '      </table>\n' +
            //'      <hr style="border:0; width:90%; background-color:#ccc"/>' +
            '      <div style="padding:4px">&#160;</div>' +
            '  </xsl:template>\n' +
            xslfooter;
        }
    },

    getRefinementTemplate: function() {
        var xslt = '<?xml version="1.0" encoding="UTF-8"?>' +
               '<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:geonet="http://www.fao.org/geonetwork" exclude-result-prefixes="xsl csw geonet">';

        if( Ext.isIE6 || Ext.isIE7 || Ext.isIE8 ){
            xslt += '  <xsl:output method="xml" encoding="UTF-8" indent="yes"/>';
        }else{
            xslt += '  <xsl:output method="html" encoding="UTF-8" indent="yes"/>';
        }
        xslt += '<xsl:template name="unquote">'+
                '  <xsl:param name="val"/>'+
                '  <xsl:choose>'+
                '    <xsl:when test="contains($val, \'&quot;\')">'+
                '      <xsl:variable name="start" select="substring-before($val,\'&quot;\')"/>'+
                '      <xsl:variable name="end">'+
                '        <xsl:call-template name="unquote">'+
                '          <xsl:with-param name="val" select="substring-after($val,\'&quot;\')"/>'+
                '        </xsl:call-template>'+
                '      </xsl:variable>'+
                '      <xsl:value-of select="concat($start,\'\\&quot;\', $end)"/>'+
                '    </xsl:when>'+
                '    <xsl:otherwise>'+
                '      <xsl:value-of select="$val"/>'+
                '    </xsl:otherwise>'+
                '  </xsl:choose>'+
                '</xsl:template>';
        xslt += '  <xsl:template match="/">' +
               '    <table>'+
               '      <xsl:apply-templates/>' +
               '    </table>'+
               '  </xsl:template> '+
               geocat.getDefaultRefinementListTemplate('titles','titles','title','E1.0__title') +
               geocat.getDefaultRefinementListTemplate('keyword','keywords','keyword','E1.0_keyword') +
               geocat.getRefinementListTemplate('resolution','denominators','denominator','N_denominator', geocat.denominatorLabelFormat) +
               geocat.getDefaultRefinementListTemplate('category','categories','category','E1.0_topicCat') +
               geocat.getDefaultRefinementListTemplate('spatialRepresentation','spatialRepresentations','spatialRepresentation','E1.0_spatialRepresentation') +
               geocat.getDefaultRefinementListTemplate('organisationName','organisationNames','organisationName','E1.0__orgName') +
               geocat.getDefaultRefinementListTemplate('serviceType','serviceTypes','serviceType','E1.0_serviceType') +
               geocat.getDefaultRefinementListTemplate('type','types','type','E1.0_type') +
                '  <xsl:template match="text()"/>' +
               '</xsl:stylesheet>';
        return xslt;
    },

    getDefaultRefinementListTemplate: function(categoryLabel, category, elem, queryTerm) {
        return geocat.getRefinementListTemplate(categoryLabel, category, elem, queryTerm,
                geocat.basicLabelFormat);
    },

    getRefinementListTemplate: function(categoryLabel, category, elem, queryTerm, labelFormat) {
        return '  <xsl:template match="' + category + '">' +
               '    <xsl:if test="' + elem + '[/csw:GetRecordsResponse/geonet:Summary/@count != @count ]"><tr class="refineAdd"><td  class="refineAdd">' +
               translate('refine.'+categoryLabel) +
               '        <ul class="refineAdd">' +
               '            <xsl:apply-templates mode="' + elem + '" select="' + elem + '"/>' +
               '        </ul></td></tr>' +
               '    </xsl:if>' +
               '  </xsl:template>' +
               '  <xsl:template match="' + elem + '[/csw:GetRecordsResponse/geonet:Summary/@count != @count ]" mode="' + elem + '">' +
               '    <xsl:variable name="xname"><xsl:call-template name="unquote"><xsl:with-param name="val" select="@name"/></xsl:call-template></xsl:variable>'+
               '    <li><a href=\'javascript:geocat.refine("' + translate('refine.'+categoryLabel) + '","' + queryTerm + '", "{$xname}")\'>' + labelFormat + '</a></li>' +
               '  </xsl:template>';
    },

    basicLabelFormat: '<xsl:value-of select="@title" /> (<xsl:value-of select="@count" />)',
    denominatorLabelFormat: '1/<xsl:value-of select="@title" /> (<xsl:value-of select="@count" />)',
    dateLabelFormat: '<xsl:value-of select="@title" /> (<xsl:value-of select="@count" />)',
    sortArryAsc: function(array) {
        var end = array.slice(1).sort(function(a,b){
            var al = a[0].toLowerCase()+a[1].toLowerCase();
            var bl = b[0].toLowerCase()+b[1].toLowerCase();
           if(al < bl) {
               return -1;
           } else if (al == bl) {
               return 0;
           } else {
               return 1;
           }
        });
        
        end.splice(0,0,array[0]);
        return end;
    },
    openSource: function(sourceId) {
        var source = sources[sourceId];
        if(source) {
            window.open(source.url,"_blank");
        }
    }

};
