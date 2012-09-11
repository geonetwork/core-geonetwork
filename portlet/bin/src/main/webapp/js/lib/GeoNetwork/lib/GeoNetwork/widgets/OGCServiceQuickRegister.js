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
Ext.namespace('GeoNetwork');

/** api: (define) 
 *  module = GeoNetwork
 *  class = OGCServiceQuickRegister
 *  base_link = `Ext.menu.Menu <http://extjs.com/deploy/dev/docs/?class=Ext.menu.Menu>`_
 *
 */
/** api: constructor 
 *  .. class:: OGCServiceQuickRegister(config)
 *
 *     Create a menu bar with a simple form for configuration
 *     of an OGC WxS harvester. This widget use the GetCapabilities
 *     URL to retrieve the name and version of the service. Then
 *     the configuration is sent to the catalogue.
 *
 *
 *     This widget require specific privileges
 *     for xml.harvesting.add and xml.harvesting.run
 *     services. Login first or change user privileges to accept public
 *     service calls.
 *
 */
GeoNetwork.OGCServiceQuickRegister = Ext.extend(Ext.menu.Menu, {
    /** api: config[catalogue] 
     *  ``GeoNetwork.Catalogue`` Catalogue to use
     */
    catalogue: undefined,
    
    /** api: config[catalogue] 
     *  ``GeoNetwork.data.HarvesterStore`` Optional harvester store to refresh after a
     *  new harvester registration
     */
    harvesterStore: undefined,
    
    /** api: config[textInfo] 
     *  ``String`` Text information to display
     */
    textInfo: undefined,
    
    /** api: config[textExample] 
     *  ``String`` Example URL
     */
    textExample: 'eg. http://services.sandre.eaufrance.fr/geo/ouvrage?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetCapabilities',
    
    /** api: config[textValidButton] 
     *  ``String`` Validation button text
     */
    textValidButton: 'Check & Add',
    
    /** api: config[urlFieldWidth] 
     *  ``Number`` Width of the URL field. Default 250.
     */
    urlFieldWidth: 250,
    
    /** api: property[urlField] 
     *  ``Ext.form.TextField`` URL field for GetCapabilities
     */
    urlField: undefined,
    
    /** private: method[initComponent] 
     *  Initializes the menu
     */
    initComponent: function(){
        this.textInfo = OpenLayers.i18n('enterGetCapURL'); 
        var cmp = [];
        cmp.push({
            text: this.textInfo,
            canActivate: false
        });
        
        this.urlField = {
            xtype: 'textfield',
            width: this.urlFieldWidth,
            margins: '0 0 0 30',
            id: 'OGCServiceQuickRegisterUrl', // Could not be unique
            value: '',
            iconCls: 'no-icon'
        };
        
        cmp.push(this.urlField);
        cmp.push({
            text: this.textExample,
            canActivate: false
        });
        cmp.push({
            xtype: 'buttongroup',
            autoWidth: true,
            border: false,
            frame: false,
            columns: 2,
            iconCls: 'no-icon',
            defaults: {
                xtype: 'button',
                iconAlign: 'right'
            },
            items: [{
                text: this.textValidButton,
                iconCls: 'md-mn md-mn-badd',
                handler: this.getCapabilities
            }]
        });
        
        Ext.apply(this, {
            defaultType: 'menuitem',
            items: cmp
        });
        
        GeoNetwork.OGCServiceQuickRegister.superclass.initComponent.call(this);
        
    },
    /** private: method[getCapabilities] 
     *  Retrieve capabilities document and get name and version in
     *  order to configure the harvester.
     */
    getCapabilities: function(){
        var url = Ext.getCmp('OGCServiceQuickRegisterUrl').getValue();
        var menu = this.ownerCt.ownerCt;
        OpenLayers.Request.GET({
            url: url,
            success: function(response){
                if (response.responseText.indexOf('Some unexpected') !== -1) { // FIXME : better error handling
                    Ext.Msg.alert('Failed to check service url.', response.responseText);
                    return;
                }
                var xml = response.responseXML;
                if (!xml) {
                    Ext.Msg.alert('GetCapabilities failed', 'Null response returned - check proxy or service configuration.'); // TODO i18n
                    return;
                }
                
                var format = new OpenLayers.Format.WMSCapabilities();
                var obj = format.read(xml);
                menu.addHarvester(obj.service.title, url, obj.version);
            },
            failure: function(response){
                Ext.Msg.alert('Failed to check service url.', response.responseText); // TODO : i18n
            }
        });
    },
    
    /** private: method[addHarvester] 
     *  Create an XML configuration for the harvester
     *  before sending it to the catalogue for registration
     */
    addHarvester: function(title, url, version){
        var data = new Ext.data.Record({
            id: '',
            type: 'ogcwxs',
            site_name: title,
            site_url: url.replace(/&/g, '&amp;'),
            site_ogctype: 'WMS' + version,
            site_icon: 'default.gif',
            site_account_use: 'false',
            options_every: '90',
            options_topic: '',
            options_onerunonly: 'true',
            options_lang: 'en',
            options_createthumbnails: 'true',
            options_uselayer: 'true',
            options_uselayermd: 'false'
        });
        var xmlTpl = new GeoNetwork.Templates().getHarvesterTemplate();
        var xml = xmlTpl.apply(data.data);
        this.saveHarvester(xml);
    },
    
    /** private: method[saveHarvester] 
     *  Save the harvester in the catalogue for registration
     */
    saveHarvester: function(xml){
        var menu = this;
        var opts = {
            url: this.catalogue.services.harvestingAdd,
            data: xml
        };
        OpenLayers.Util.applyDefaults(opts, {
            success: function(response){
                var xml = response.responseXML;
                var id = xml.firstChild.getAttribute('id');
                menu.runHarvester(this.catalogue.services.harvestingRun, id);
            },
            failure: function(response){
                Ext.Msg.alert('Failed', response.responseText);
            }
        });
        OpenLayers.Request.POST(opts);
        
    },
    
    /** private: method[runHarvester] 
     *  Send the query and the id. Use to activate and start the harvester.
     */
    runHarvester: function(url, id){
        var menu = this;
        OpenLayers.Request.GET({
            url: url + '?id=' + id,
            success: function(response){
                if (response.responseText.indexOf('nexpected') !== -1) { // FIXME : better error handling
                    Ext.Msg.alert('Activation or start failed.', response.responseText);
                    return;
                }
                Ext.Msg.alert('Successfull registration', 'Wait a few minutes before having all layers and services registered in the catalogue.');
                // Refresh list of harvester
                if (menu.harvesterStore) {
                    menu.harvesterStore.reload();
                }
            },
            failure: function(response){
                Ext.Msg.alert('Registration failed', response.responseText);
            }
        });
    }
});

/** api: xtype = gn_ogcservicequickregister */
Ext.reg('gn_ogcservicequickregister', GeoNetwork.OGCServiceQuickRegister);
