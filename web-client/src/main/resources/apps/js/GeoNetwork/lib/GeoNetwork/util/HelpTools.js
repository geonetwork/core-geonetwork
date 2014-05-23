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
Ext.namespace("GeoNetwork.util");

/** api: (define) 
 *  module = GeoNetwork.util 
 *  class = HelpTools
 */
/** api: HelpTools help to interact with xml.schema.info services
 *  which retrieves help for schema elements.
 */
GeoNetwork.util.HelpTools = {
    fields: [{
            name: 'name',
            mapping: '@name'
        },{
            name: 'id',
            mapping: '@id'
        },{
            name: 'label'
        },{
            name: 'description'
        },{
            name: 'help',
            convert: function (v, record){
                // Extract all descriptions and p elements in description 
                var descs = [], i, j;
                var n = record.getElementsByTagName('help');
                for (i = 0; i < n.length; i++) {
                    var p = n[i].getElementsByTagName('p');
                    if (p.length !== 0) {
                        for (j = 0; j < n.length; j++) {
                            descs.push(p[j].firstChild.nodeValue);
                        }
                    } else {
                        if (n[i].firstChild) {
                            descs.push(n[i].firstChild.nodeValue);
                        }
                    }
                }
                return descs;
            }
        },{
            name: 'example'
        },{
            name: 'helper',
            convert: function (v, record){
                var helpers = [], 
                    title,
                    i;
                var nodes = record.getElementsByTagName('helper');
                if (nodes.length === 1) {
                    var n = nodes[0].getElementsByTagName('option');
                    for (i = 0; i < n.length; i++) {
                        helpers.push({
                            title: n[i].getAttribute('title'),
                            label: n[i].firstChild.nodeValue,
                            value: n[i].getAttribute('value')
                        });
                    }
                }
                return helpers;
            }
        }],
    /** api: property[Templates] 
     *  Default templates to display help information
     */
    Templates: {
        /** api: property[Templates.SIMPLE] 
         *  Display field description and example
         */
        SIMPLE: ['<div class="help">{description}', 
            '<tpl if="values.example != \'\'">',
              '<div><span>' + OpenLayers.i18n('example') + '</span> {example}</div>',
            '</tpl></div>'],
        /** api: property[Templates.COMPLETE] 
         *  Display all field information (label, description, help, example, tag name and helper).
         */
        COMPLETE: ['<div class="help"><span class="title">{label}</span>',
                     '<div>{description}</div>',
                     '<tpl for="help">',
                       '<div>{.}</div>',
                     '</tpl>',
                     '<tpl if="values.example != \'\'">',
                       '<div><span>' + OpenLayers.i18n('example') + '</span> {example}</div>',
                     '</tpl>',
                     '<tpl if="values.helper != \'\'">',
                       '<div><span>' + OpenLayers.i18n('helper') + '</span><ul>',
                       '<tpl for="helper">',
                         '<li title="{values.title}">{values.label}</li>',
                     '</tpl>',
                     '</ul></div>',
                   '</tpl>',
                   '<div>' + OpenLayers.i18n('tagName') + ' {name}</div>', 
                   '</div>']
    },
    /** api:method[get]
    *
    *  :param helpId: ``String``  help identifier (eg. stip.iso19139|gmd:identificationInfo|gmd:MD_Metadata|gmd:MD_Metadata/gmd:identificationInfo||d91874e39351)
    *  :param schema: ``String``    Metadata schema (eg. iso19139)
    *  :param url: ``String``  Usually schema.info service
    *  :param cb: ``Function``  Optional function to trigger in case of success
    *  
     * Load help information for the element and pass the record to the callback provided
     * 
     */
    get: function (helpId, schema, url, cb) {
    	var info = helpId.split('|');
    	schema = schema || info[0].split('.')[1] || 'iso19139'; // Fallback to iso19139 if schema undefined
    	var requestBody = '<request><element schema="' + schema + 
                                '" name="' + info[1] + 
                                '" context="' + info[2] + 
                                '" fullContext="' + info[3] + 
                                '" isoType="' + info[4] + '" /></request>';
        OpenLayers.Request.POST({
            url: url,
            data: requestBody,
            success: function(response){
                var el = response.responseXML.getElementsByTagName('element');
                if (el[0] && el[0].getAttribute('error') === 'not-found') {
                    return;
                }
                var pReader = new Ext.data.XmlReader({
                    record: 'element',
                        fields: GeoNetwork.util.HelpTools.fields
                });
                var r = pReader.readRecords(response.responseXML);

                if(cb) {
                    cb(r);
                }
            }
        });
    },    
    /** Show advanced tooltip **/
    showtt: function(r) {
    	if(r == null || r.records == null || r.records.length == 0)
    		return;
    	
    	var data = r.records[0].data;
    	
        var tipTpl = new Ext.XTemplate(GeoNetwork.util.HelpTools.Templates.COMPLETE);
        var msg =  panel.tipTpl.apply(r.records[0].data);
   	
    	msg = ['<div class="msg">',
                   '<div class="x-box-tl"><div class="x-box-tr"><div class="x-box-tc"></div></div></div>',
                   '<div class="x-box-ml"><div class="x-box-mr"><div class="x-box-mc">', msg, '</div></div></div>',
                   '<div class="x-box-bl"><div class="x-box-br"><div class="x-box-bc"></div></div></div>',
                   '</div>'].join('');
    	
    	//TODO instead of this, look for the label container
    	var container = Ext.get("msg-div");
    	
    	if(container == null) {
        	container = Ext.DomHelper.insertFirst(document.body, {id: "msg-div", "class": "msg"}, true);
    	}
    	
    	
    	var  msgCt = Ext.DomHelper.insertFirst(container, {id: "message-container-" + data.id}, true);
    	msgCt.alignTo(document, 't-t');
        var m = Ext.DomHelper.append(msgCt, {html:msg}, true);
        m.slideIn('t').pause(20).ghost("t", {remove:true});
        m.on("click", function(){m.remove();});
    }
};
