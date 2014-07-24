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
 *  class = GroupStore
 */
/** api: method[GroupStore]
 *  A pre-configured `Ext.data.JsonStore <http://extjs.com/deploy/dev/docs/?class=Ext.data.JsonStore>`_
 *  for GeoNetwork groups. Groups visibility depends on user privileges.
 *
 *  :param url: ``String`` Usually the xml.info service URL.
 *   See `xml.info service description <../../../developers/xml_services/services_general.html#xml-info>`_
 *   for mode information.
 */
GeoNetwork.data.GroupStore = function(url){
    /**
     * Create a list of label // TODO : merge with other stores
     */
    function getLabel(v, record){
        var labels = {};
        var i;
        var label = record.getElementsByTagName('label');
        if (label.length === 1) {
            var children = label[0].childNodes;
            var child;
            for (i = 0, len = children.length; i < len; ++i) {
                child = children[i];
                if (child.nodeType === 1) {
                    labels[child.nodeName] = child.firstChild.nodeValue;
                }
            }
        }
        return labels;
    }
    function getLabelForCurrentLang(v, record){
        var labels = {};
        var i;
        var label = record.getElementsByTagName('label');
        if (label.length === 1) {
            var children = label[0].childNodes;
            var child;
            for (i = 0, len = children.length; i < len; ++i) {
                child = children[i];
                if (child.nodeType === 1 && child.nodeName == catalogue.lang) {
                    label = child.firstChild.nodeValue;
                }
            }
        }
        return label;
    }
    
    return new Ext.data.XmlStore({
        autoDestroy: true,
        proxy: new Ext.data.HttpProxy({
            method: 'GET',
            url: url,
            disableCaching: false
        }),
        record: 'group/group',
        idPath: 'id',
        fields: [{
            name: 'id',
            mapping: '@id'
        }, {
            name: 'name'
        }, {
            name: 'description'
        }, {
            name: 'email'
        }, {
            name: 'referrer'
        }, {
            name: 'label',
            convert: getLabel
        }, {
            name: 'labelInLang',
            convert: getLabelForCurrentLang
        }]
    });
};