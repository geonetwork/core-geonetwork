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
 *  class = HarvesterStore
 */
/** api: method[HarvesterStore]
 *  A pre-configured `Ext.data.JsonStore <http://extjs.com/deploy/dev/docs/?class=Ext.data.JsonStore>`_
 *  for GeoNetwork harvesters.
 *
 *  See :class:`GeoNetwork.admin.HarvesterPanel`
 *
 *  :param url: ``String`` Usually the xml.harvester.get service URL.
 *    See `service description <../../../developers/xml_services/services_harvesting.html#xml-harvesting-get>`_
 *    for mode information.
 */
GeoNetwork.data.HarvesterStore = function(url){
    /**
     * Return an array of categories
     */
    function getCategories(v, record){
        var categories = [], 
            i, 
            catNode = record.getElementsByTagName('categories');
        if (catNode.length === 1) {
            var cats = catNode[0].getElementsByTagName('category');
            for (i = 0; i < cats.length; i++) {
                categories.push(cats[i].getAttribute('id'));
            }
        }
        return categories;
    }
    
    /**
     * Return an array of operation
     */
    function getOperation(v, record){
        var operation = [],
            i,
            ops = record.getElementsByTagName('operation');
        for (i = 0; i < ops.length; i++) {
            var op = ops[i];
            operation.push(op.getAttribute('name'));
        }
        return operation;
    }
    
    /**
     * Read privileges node
     */
    function getPrivileges(v, record){
    
        var Privilege = Ext.data.Record.create([{
            name: 'group_id',
            mapping: '@id'
        }, {
            name: 'operation',
            convert: getOperation
        }]);
        
        // Maybe the use of a reader here is not so useful
        var pReader = new Ext.data.XmlReader({
            record: "group"
        }, Privilege);
        
        var r = pReader.readRecords(record);
        return r;
    }
    
    return new Ext.data.XmlStore({
        autoDestroy: true,
        url: url,
        // reader configs
        record: 'node', // records will have an "Item" tag
        idPath: '@id',
        fields: [{
            name: 'id',
            mapping: '@id'
        }, {
            name: 'type',
            mapping: '@type'
        }, {
            name: 'site_name',
            mapping: 'site/name'
        }, {
            name: 'site_uuid',
            mapping: 'site/uuid'
        }, /* GeoNetwork */ {
            name: 'site_host',
            mapping: 'site/host'
        }, {
            name: 'site_servlet',
            mapping: 'site/servlet'
        }, {
            name: 'site_port',
            mapping: 'site/port'
        }, /* OGC WxS */ {
            name: 'site_url',
            mapping: 'site/url'
        }, {
            name: 'site_icon',
            mapping: 'site/icon'
        }, {
            name: 'site_ogctype',
            mapping: 'site/ogctype'
        }, {
            name: 'options_lang',
            mapping: 'options/lang'
        }, {
            name: 'options_topic',
            mapping: 'options/topic'
        }, {
            name: 'options_createthumbnails',
            mapping: 'options/createthumbnails'
        }, {
            name: 'options_uselayer',
            mapping: 'options/uselayer'
        }, {
            name: 'options_uselayermd',
            mapping: 'options/uselayermd'
        }, {
            name: 'options_datasetcategory',
            mapping: 'options/datasetcategory'
        }, {
            name: 'privileges',
            convert: getPrivileges
        }, {
            name: 'categories',
            convert: getCategories
        }, {
            name: 'site_account_use',
            mapping: 'site/account/use'
        }, {
            name: 'site_account_username',
            mapping: 'site/account/username'
        }, {
            name: 'site_account_password',
            mapping: 'site/account/password'
        }, {
            name: 'options_every',
            mapping: 'options/every'
        }, {
            name: 'options_onerunonly',
            mapping: 'options/onerunonly'
        }, {
            name: 'options_status',
            mapping: 'options/status'
        }, {
            name: 'info_lastrun',
            mapping: 'info/lastRun'
        }, {
            name: 'info_running',
            mapping: 'info/running'
        }, {
            name: 'info_result_total',
            mapping: 'info/result/total'
        }, {
            name: 'info_result_added',
            mapping: 'info/result/added'
        }, {
            name: 'info_result_layer',
            mapping: 'info/result/layer'
        }, {
            name: 'info_result_layerUuidExist',
            mapping: 'info/result/layerUuidExist'
        }, {
            name: 'info_result_layerUsingMdUrl',
            mapping: 'info/result/layerUsingMdUrl'
        }, {
            name: 'info_result_unknownSchema',
            mapping: 'info/result/unknownSchema'
        }, {
            name: 'info_result_removed',
            mapping: 'info/result/removed'
        }, {
            name: 'info_result_unretrievable',
            mapping: 'info/result/unretrievable'
        }, {
            name: 'info_result_badFormat',
            mapping: 'info/result/badFormat'
        }, {
            name: 'info_result_doesNotValidate',
            mapping: 'info/result/doesNotValidate'
        }, {
            name: 'info_result_thumbnails',
            mapping: 'info/result/thumbnails'
        }, {
            name: 'info_result_thumbnailsFailed',
            mapping: 'info/result/thumbnailsFailed'
        }, {
            name: 'error_message',
            mapping: 'error/message'
        }, {
            name: 'error_class',
            mapping: 'error/class'
        }, {
            name: 'error_stack'
            // TODO convert stack to text or object mapping : 'error/message'
        }]
    });
};