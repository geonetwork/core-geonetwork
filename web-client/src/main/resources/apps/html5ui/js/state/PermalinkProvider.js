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
 * Extends the PermalinkProvider of GeoExt
 * 
 * Helps App.js
 */

if (!GeoNetwork.state) {
    GeoNetwork.state = {};
}

GeoNetwork.state.PermalinkProvider = function(config) {
    GeoExt.state.PermalinkProvider.superclass.constructor
            .apply(this, arguments);

    config = config || {};

    var url = config.url;
    delete config.url;

    if (!Ext.isIE) { // FIXME
        GeoNetwork.state.History.on('change', function() {
            Ext.state.Manager.getProvider().restore();
            return false;
        });
    }

    Ext.apply(this, config);
};

Ext
        .extend(
                GeoNetwork.state.PermalinkProvider,
                GeoExt.state.PermalinkProvider,
                {
                    restoring : false,
                    lastSearch : '',
                    metadata_separator : '|',
                    /**
                     * Update the last search to show it on the url
                     * 
                     * @param s
                     */
                    updateLastSearch : function(s) {
                        if (s == null)
                            s = '';

                        this.lastSearch = s;

                        if (!this.restoring) {
                            GeoNetwork.state.History.add(s);
                        }
                    },
                    /**
                     * private: method[readURL] :param url: ``String`` The URL
                     * to get the state from. :return: ``Object`` The state
                     * object.
                     * 
                     * Create a state object from a URL.
                     */
                    restore : function(url) {

                        if (GeoNetwork.state.History.eventsSuspended)
                            return;

                        this.restoring = true;

                        var i = window.location.href.indexOf("#");
                        if (i < 0) {
                            i = window.location.href.length;
                        }
                        this
                                .restoreFields(window.location.href.substring(
                                        0, i));

                        var end = location.hash.indexOf("?");
                        if (end <= 0) {
                            end = location.hash.length;
                        }
                        var from = location.hash.indexOf("|");
                        if (from < 0) {
                            from = end;
                        }

                        var restoreSearch = true;

                        if (from > 1) {
                            var search_string = location.hash
                                    .substring(1, from);

                            var url_md = 'xml.metadata.get?id=';
                            var open_file = "<gmd:fileIdentifier>";
                            var close_file = "</gmd:fileIdentifier>";
                            var open_char = "<gco:CharacterString>";
                            var close_char = "</gco:CharacterString>";

                            if (search_string.indexOf("id=") == 0) {
                                var id = search_string.substring(3);
                                Ext.Ajax.request({
                                    url : url_md + id,
                                    success : function(response, opts) {
                                        var res = response.responseText;

                                        var uuid = res.substring(res
                                                .indexOf(open_file) + 21, res
                                                .indexOf(close_file));
                                        uuid = uuid.substring(uuid
                                                .indexOf(open_char) + 21, uuid
                                                .indexOf(close_char));
                                        app.searchApp.addMetadata(uuid, true);
                                    }
                                });
                                restoreSearch = false;
                            } else if (search_string.indexOf("edit=") == 0) {
                                var id = search_string.substring(5);
                                Ext.Ajax.request({
                                    url : url_md + id,
                                    success : function(response, opts) {
                                        var res = response.responseText;

                                        var uuid = res.substring(res
                                                .indexOf(open_file) + 21, res
                                                .indexOf(close_file));
                                        uuid = uuid.substring(uuid
                                                .indexOf(open_char) + 21, uuid
                                                .indexOf(close_char));
                                        app.searchApp.addMetadata(uuid, true,
                                                id);
                                    }
                                });
                                restoreSearch = false;
                            } else if (search_string.indexOf("create") == 0) {
                                Ext.getCmp("resultsPanel").getTopToolbar().createMetadataAction
                                        .fireEvent('click');
                            } else if (!Ext.isEmpty(search_string)) {
                                GeoNetwork.util.SearchTools.doQuery(
                                        search_string, catalogue,
                                        catalogue.startRecord,
                                        app.searchApp.loadResults, null, true,
                                        catalogue.metadataStore,
                                        catalogue.summaryStore, true);

                                app.searchApp.firstSearch = true;
                                showSearch();

                                restoreSearch = false;
                            }

                        }
                        from = location.hash.indexOf("|");
                        if (from < 0) {
                            from = 0;
                        } else {
                            restoreSearch = false;
                        }
                        Ext.each(location.hash.substring(from, end).split("|"),
                                function(uuid) {
                                    if (app && app.searchApp
                                            && uuid.indexOf("#") < 0
                                            && !Ext.isEmpty(uuid)) {
                                        app.searchApp.addMetadata(uuid, true);
                                        restoreSearch = false;
                                    }
                                });

                        if (OpenLayers.Util.getParameters(url).hasOwnProperty(
                                "s_search")
                                && restoreSearch) {
                            Ext.getCmp('advanced-search-options-content-form')
                                    .fireEvent('search');
                            Ext.getCmp('advanced-search-options-content-form')
                                    .getForm().reset();

                        } else {
                            this.restoreFields(window.location.href);

                            Ext.state.Manager.getProvider().restoring = false;
                        }

                    },
                    restoreFields : function(url) {

                        this.readURL(url);
                        // Continue with GeoExt stuff

                        var all = {};

                        Ext.each(Ext.ComponentMgr.all.items, function(el) {
                            if (el.stateId) {
                                all[el.stateId] = el;
                            }
                        });

                        for (prop in this.state) {
                            var obj = this.state[prop];

                            var el = all[prop];

                            if (!el) {
                                el = Ext.getCmp(prop);
                            }
                            if (!el) {
                                Ext.get(prop);
                            }
                            if (el) {
                                if (el.map) {
                                    this.restoreMap(obj, el.map);
                                } else if (obj.zoom && obj.x && obj.y) {

                                    Ext.each(el.initialConfig.items, function(
                                            item) {
                                        Ext.each(item.items, function(it) {
                                            if (it.map) {
                                                map = it.map;
                                            }
                                        });
                                    });

                                    this.restoreMap(obj, map);
                                } else if (obj.collapsed && el.collapse) {
                                    el.collapse();
                                } else {
                                    for (key in obj) {
                                        var el_ = Ext.DomQuery
                                                .select("*[name='" + key + "']");

                                        if (el_ && el_.length && el_.length > 0) {
                                            el_ = el_[0];
                                        }

                                        if (el_) {
                                            el_ = Ext.get(el_);
                                            if (el_.setValue) {
                                                el_.setValue(obj[key]);
                                            } else if (el_.dom) {
                                                el_.dom.value = obj[key];
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    },
                    readURL : function(url) {
                        this.state = {};

                        var params = OpenLayers.Util.getParameters(url
                                .substring(url.indexOf("?")));
                        var k, split, stateId;
                        for (k in params) {
                            if (params.hasOwnProperty(k)) {
                                split = k.split("_");
                                if (split.length > 1) {
                                    stateId = split[0];
                                    this.state[stateId] = this.state[stateId]
                                            || {};
                                    this.state[stateId][split.slice(1)
                                            .join("_")] = this.encodeType ? this
                                            .decodeValue(params[k])
                                            : params[k];
                                }
                            }
                        }
                        return this.state;
                    },

                    /** Pretty link for sharing */
                    getPrettyLink : function(base) {
                        var link = this.getLink(base);
                        if (link.indexOf("?") >= 0) {
                            link = link.substring(0, link.indexOf("?"));
                        }

                        link = link.substring(link.indexOf("|") + 1);

                        // Get base url and language
                        var urlParts = window.location.href.match(/(http.*\/.*)\/srv\/(.*)\/.*/, '');

                        return urlParts[1]
                          + '/srv/' + urlParts[2] + '/search' + "?uuid=" + link;
                    },
                    /**
                     * api: method[getLink] :param base: ``String`` The base
                     * URL, optional. :return: ``String`` The permalink.
                     * 
                     * Return the permalink corresponding to the current state.
                     */
                    getLink : function(base) {
                        base = base || document.location.href;

                        // Before doing GeoExt stuff, add tabs (metadata)

                        /*
                        var tabs = GeoNetwork.state.History.getToken();

                        if (!tabs || tabs == 'null')
                            tabs = '';

                        var qMark = tabs.indexOf("?");
                        if (qMark > 0) {
                            tabs = tabs.substring(0, qMark);
                        }
                        qMark = tabs.indexOf("#");
                        if (qMark > 0) {
                            tabs = tabs.substring(qMark);
                        }

                        base = tabs;
                        */

                        // Now we continue with GeoExt stuff
                        var params = {};

                        var id, k, state = this.state;
                        for (id in state) {
                            if (state.hasOwnProperty(id)) {
                                for (k in state[id]) {
                                    params[id + "_" + k] = this.encodeType ? unescape(this
                                            .encodeValue(state[id][k]))
                                            : state[id][k];
                                }
                            }
                        }

                        // merge params in the URL into the state params
                        OpenLayers.Util.applyDefaults(params, OpenLayers.Util
                                .getParameters(base));

                        var paramsStr = OpenLayers.Util
                                .getParameterString(params);

                        return Ext.urlAppend(base, paramsStr);
                    },
                    restoreMap : function(state, map) {

                        // if we get strings for state.x, state.y or state.zoom
                        // OpenLayers will take care of converting them to the
                        // appropriate types so we don't bother with that
                        map.setCenter(new OpenLayers.LonLat(state.x, state.y),
                                state.zoom, false, false);

                        // set layer visibility and opacity
                        var i, l, layer, layerId, visibility, opacity;
                        Ext.each(map.layers, function(layer) {
                            layerId = layer.name;
                            visibility = state["visibility_" + layerId];
                            if (visibility !== undefined) {
                                // convert to boolean
                                visibility = (/^true$/i).test(visibility);
                                if (layer.isBaseLayer) {
                                    if (visibility) {
                                        map.setBaseLayer(layer);
                                    }
                                } else {
                                    layer.setVisibility(visibility);
                                }
                            }
                            opacity = state["opacity_" + layerId];
                            if (opacity !== undefined) {
                                layer.setOpacity(opacity);
                            }
                        });
                    }
                });
