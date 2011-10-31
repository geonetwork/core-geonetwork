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
Ext.namespace("GeoNetwork.data");

/** api: (define)
 *  module = GeoNetwork.data
 *  class = OpenSearchSuggestionStore
 *  base_link = `Ext.data.Store <http://extjs.com/deploy/dev/docs/?class=Ext.data.Store>`_
 */
Ext.namespace("GeoNetwork.data");

/** api: constructor
 *  .. class:: OpenSearchSuggestionStore(config)
 *
 *      Small helper class to make creating stores for remotely-loaded attributes
 *      data easier. OpenSearchSuggestionStore is pre-configured with a built-in
 *      ``Ext.data.HttpProxy`` and :class:`GeoNetwork.data.OpenSearchSuggestionReader`.
 *      The HttpProxy is configured to allow caching (disableCaching: false) and
 *      uses GET.
 */
GeoNetwork.data.OpenSearchSuggestionStore = function(c){
    c = c || {};
    GeoNetwork.data.OpenSearchSuggestionStore.superclass.constructor.call(this, Ext.apply(c, {
        proxy: c.proxy ||
            (!c.data ? new Ext.data.HttpProxy({
                url: c.url,
                disableCaching: false,
                method: "GET"
            }) : undefined),
        reader: new GeoNetwork.data.OpenSearchSuggestionReader(c, c.fields ||
        ["value"] // TODO : add extra field according to OpenSearch suggestion (not supported by GeoNetwork yet)
)
    }));
};
Ext.extend(GeoNetwork.data.OpenSearchSuggestionStore, Ext.data.Store);