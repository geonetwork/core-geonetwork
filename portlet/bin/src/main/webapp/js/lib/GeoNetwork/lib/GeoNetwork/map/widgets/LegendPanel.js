/*
 * Copyright (C) 2009 GeoNetwork
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
 * Class: GeoNetwork.LegendPanel
 * Override the default GeoExt LegendPanel so that LegendURLs from the 
 * GetCapabilities interface get used. The current GeoNetwork js code
 * already sets a property on the OL Layer object called legendURL, which
 * we can use, and we can set this on the layer store record so that it
 * gets used.
 *
 * Inherits from:
 *  - {GeoExt.LegendPanel}
 */
GeoNetwork.LegendPanel = Ext.extend(GeoExt.LegendPanel, {

    initComponent: function() {
        GeoNetwork.LegendPanel.superclass.initComponent.call(this);
    },
    
    onStoreAdd: function(store, records, index) {
        GeoNetwork.LegendPanel.superclass.onStoreAdd.apply(this, arguments);
        for (var i=0, len=records.length; i<len; i++) {
            var record = records[i];
            if (record.get('layer').legendURL !== undefined) {
                record.set('legendURL', record.get('layer').legendURL);
            }
        }
    }

});

Ext.reg('gn_legendpanel', GeoNetwork.LegendPanel); 
