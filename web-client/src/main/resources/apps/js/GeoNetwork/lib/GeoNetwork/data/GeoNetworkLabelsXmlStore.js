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

Ext.namespace('GeoNetwork.data');

/** api: (define)
 *  module = GeoNetwork.data
 *  class = GeoNetworkLabelsXmlStore
 */
/** api: method[GeoNetworkLabelsXmlStore]
 *  A (base) pre-configured `Ext.data.XmlStore <http://extjs.com/deploy/dev/docs/?class=Ext.data.XmlStore>`_
 *   with a sort method for GeoNetwork data with labels in several languages.
 *
 *   Other stores like GroupStore extends this class instead of `Ext.data.XmlStore <http://extjs.com/deploy/dev/docs/?class=Ext.data.XmlStore>`_
 *
 */
GeoNetwork.data.GeoNetworkLabelsXmlStore = Ext.extend(Ext.data.XmlStore, {
    /**
     * Sorts the store using the labels for the provided language
     */
    sortByLang: function(lang) {
        var fn = function(r1, r2) {
            var result;

            var v1 = r1.data.label[lang];
            var v2 = r2.data.label[lang];

            result = (v1 > v2) ? 1 : ((v1 < v2) ? -1 : 0);

            return result;
        };

        this.data.sort('ASC', fn);
        if(this.snapshot && this.snapshot != this.data){
            this.snapshot.sort('ASC', fn);
        }
        this.fireEvent("datachanged", this);
    }
});
Ext.reg('GeoNetworkLabelsXmlStore', GeoNetwork.data.GeoNetworkLabelsXmlStore);
