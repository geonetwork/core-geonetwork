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
Ext.namespace('GeoNetwork.form');



/** api: (define)
 *  module = GeoNetwork.form
 *  class = SearchField
 *  base_link = `Ext.form.TwinTriggerField <http://extjs.com/deploy/dev/docs/?class=Ext.form.TextField>`_
 */
/** api: constructor 
 *  .. class:: SearchField()
 *
 *  Custom search field with a reset button once search is launched.
 */
GeoNetwork.form.SearchField = Ext.extend(Ext.form.TwinTriggerField, {
    initComponent: function(){
        if (!this.store.baseParams) {
            this.store.baseParams = {};
        }
        GeoNetwork.form.SearchField.superclass.initComponent.call(this);
        this.on('specialkey', function(f, e){
            if (e.getKey() === e.ENTER) {
                this.onTrigger2Click();
            }
        }, this);
    },
    
    validationEvent: false,
    validateOnBlur: false,
    trigger1Class: 'x-form-clear-trigger',
    trigger2Class: 'x-form-search-trigger',
    hideTrigger1: true,
    width: 180,
    hasSearch: false,
    paramName: 'query',
    
    onTrigger1Click: function(){
        if (this.hasSearch) {
            this.store.baseParams[this.paramName] = '';
            this.store.removeAll();
            this.el.dom.value = '';
            this.triggers[0].hide();
            this.hasSearch = false;
            this.focus();
        }
    },
    
    onTrigger2Click: function(){
        var v = this.getRawValue();
        if (v.length < 1) {
            this.store.baseParams[this.paramName] = '*';
        } else {
            this.store.baseParams[this.paramName] = v;
        }
        
        /**
         * If a triggerAction is defined run it. If not
         * reload associated store.
         */
        if (this.triggerAction) {
            this.triggerAction(this.scope, v);
        } else {
            this.store.reload();
        }
        this.hasSearch = true;
        this.triggers[0].show();
        this.focus();
    }
});

/** api: xtype = gn_searchfield*/
Ext.reg('gn_searchfield', GeoNetwork.form.SearchField);