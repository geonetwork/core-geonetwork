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
 *  class = ThesaurusFeedStore
 */
/** api: method[ThesaurusFeedStore]
 *  A pre-configured `Ext.data.XmlStore <http://extjs.com/deploy/dev/docs/?class=Ext.data.XmlStore>`_
 *  for GeoNetwork thesaurus available in a repository.
 *
 *  :param url: ``String`` Atom document. Default values is `GeoNetwork thesaurus repository <http://geonetwork.svn.sourceforge.net/svnroot/geonetwork/utilities/repository/thesaurus.xml>`_
 * 
 */
GeoNetwork.data.ThesaurusFeedStore = function(feed){
    var defaultFeed = 'https://raw.github.com/geonetwork/util-repository/master/thesaurus.xml';
    var store = new Ext.data.XmlStore({
        autoDestroy: true,
        autoLoad: true,
        proxy: new Ext.data.HttpProxy({
            method: 'GET',
            url: feed || defaultFeed,
            disableCaching: false
        }),
        record: 'entry',
        idPath: 'id',
        fields: [{
            name: 'id'
        }, {
            name: 'title'
        }, {
            name: 'link',
            mapping: 'link/@href'
        }, {
            name: 'updated'
        }, {
            name: 'summary'
        }, {
            name: 'category'
        }]
    });
    return store;
};