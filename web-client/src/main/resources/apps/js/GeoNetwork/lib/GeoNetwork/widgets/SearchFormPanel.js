/*
 * Copyright (C) 2001-2012 Food and Agriculture Organization of the
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
 *  class = SearchFormPanel
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.FormPanel>`_
 */
/** api: constructor 
 *  .. class:: SearchFormPanel(config)
 *
 *     Create a search form panel.
 *     
 */
GeoNetwork.SearchFormPanel = Ext.extend(Ext.FormPanel, {
    searchTriggered: false,
    
    defaultConfig: {
        border: false,
        stateful: true,
        /** api: config[searchCb] 
         *  The search function
         */
        searchCb: undefined,
        resetCb: undefined
    },
    getSearchBt: function () {
        return this.searchBt;
    },
    /** private: property[stateEvents]
     *  ``Array(String)`` Array of state events
     */
    stateEvents: ["onsearch", "onreset"],
    
    /** private: method[applyState]
     *  :param state: ``Object`` The state to apply.
     *
     *  Apply the state provided as an argument.
     *  It does not trigger the search.
     */
    applyState: function (state) {
        this.on('afterrender', function () {
            // Force layout in order to render all form fields
            // If not, superboxselect field are not initialized
            this.doLayout(false, true);
            
            // Populate search form and create new field if they do not exist
            // If not available, those field probably come to a facet value filter
            GeoNetwork.util.SearchTools.populateFormFromParams(this, state, true);
           
            // We can't really trigger fire event yet
            // Add this to your app to trigger the search when ready - FIXME
//           if (urlParameters.s_search !== undefined) {
//               setTimeout(function(){searchForm.fireEvent('search');}, 500);
//           }
           //this.fireEvent("search");
        }, this);
    },

    /** private: method[getState]
     *  :return:  ``Object`` The state.
     *
     *  Returns the current state for the search form panel.
     */
    getState: function () {
        var state = {};
        if (this.searchTriggered) {
            state.search = '';
        }
        
        var parameters = GeoNetwork.util.SearchTools.getFormValues(this);
        Ext.apply(state, parameters);
        
        return state;
    },
    search: function () {
        this.searchTriggered = true;
        this.searchCb();
        this.fireEvent('onsearch');
    },
    reset: function (args) {
        this.searchTriggered = false;
        if (this.resetCb) this.resetCb(args);
        this.getForm().reset();
        this.fireEvent('onreset', args);
    },
    /** private: method[initComponent] 
     *  Initializes the search form panel.
     */
    initComponent: function () {
        Ext.applyIf(this, this.defaultConfig);
        Ext.applyIf(this, {
				/* Don't do this
            searchBt: new Ext.Button({
                text: OpenLayers.i18n('search'),
                iconCls : 'md-mn-find',
                ctCls: 'gn-bt-main',
                iconAlign: 'right'
            }),
				*/
            resetBt: new Ext.Button({
								text: OpenLayers.i18n('Reset'),
                tooltip: OpenLayers.i18n('resetSearchForm'),
                iconCls: 'md-mn-reset'
            })
        });
        
        GeoNetwork.SearchFormPanel.superclass.initComponent.call(this);
        
        if (this.resetBt) {
            this.addButton(this.resetBt, this.reset, this);
        }
        if (this.searchBt) {
            this.addButton(this.searchBt, this.search, this);
        }
        
        this.addEvents(
                /** private: event[search]
                 *  Fires search.
                 */
                'search',
                /** private: event[reset]
                 *  Fires search.
                 */
                'reset'
            );
        this.on({
            'search': this.search,
            scope: this
        });
        this.on({
            'reset': this.reset,
            scope: this
        });
    }
});

/** api: xtype = gn_searchformpanel */
Ext.reg('gn_searchformpanel', GeoNetwork.SearchFormPanel);
