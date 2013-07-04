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
 *  class = MetadataSummaryStore
 */
/** api: method[MetadataSummaryStore] 
 *
 *   return a pre-configured `Ext.data.JsonStore *      <http://extjs.com/deploy/dev/docs/?class=Ext.data.JsonStore>`_
 *   for GeoNetwork summary.
 *
 *   
 *   .. code-block:: xml
 *   
 *    <summary count="9" type="local" hitsusedforsummary="9">
 *       <spatialRepresentations/>
 *       <keywords>
 *         <keyword count="2" name="Global"/>
 *         <keyword count="2" name="watersheds"/>
 *         <keyword count="1" name="Biology"/>
 *         <keyword count="1" name="water resources"/>
 *         <keyword count="1" name="LAND-Ownership"/>
 *         <keyword count="1" name="endangered plant species"/>
 *         <keyword count="1" name="Africa"/>
 *         <keyword count="1" name="Eurasia"/>
 *         <keyword count="1" name="endangered animal species"/>
 *         <keyword count="1" name="boundaries"/>
 *       </keywords>
 *       <denominators>
 *         <denominator count="2" name="5000000"/>
 *       </denominators>
 *       <organizationNames>
 *         <organizationName count="1" name="Department of Sustainability and Environment (DSE)"/>
 *         <organizationName 
 *   
 */
GeoNetwork.data.MetadataSummaryStore = function(root){
    
    return new Ext.data.JsonStore({
        totalProperty: 'count',
        root: root || 'keywords.keyword',
        listeners: {
            load: function(store, records, opt){
                var valueField = 'count';
                var normalizedField = 'class';
                var typeField = 'type';
                var params = {};
                var maxBound = 6;
                
                
                // Normalize value between 0 and 6 for tag cloud use mainly
                store.each(function(v){
                    v.set(typeField, store.root);
                    var type = v.get(typeField);
                    if (typeof params.type !== 'object') {
                        params.type = {
                            maxValue: 0,
                            minValue: 1
                        };
                    }
                    params.type.maxValue = Math.max(v.get(valueField), params.type.maxValue);
                    params.type.minValue = Math.min(v.get(valueField), params.type.minValue);
                });
                store.each(function(v){
                    var type = v.get(typeField);
                    var normalizedValue = 0;
                    if (params.type.maxValue !== params.type.minValue) {
                        normalizedValue = (v.get(valueField) - params.type.minValue) /
                        (params.type.maxValue - params.type.minValue) *
                        maxBound;
                    }
                    v.set(normalizedField, Math.round(normalizedValue));
                });
            }
        },
        sortInfo: { // Sort by value 
            field: 'value',
            direction: 'ASC'
        },
        fields: [{
            name: 'type',
            defaultValue: null
        }, {
            name: 'value',
            mapping: 'name',
            defaultValue: ''
        }, {
            name: 'count',
            mapping: 'count',
            defaultValue: ''
        }, {
            name: 'class',
            defaultValue: '0'
        }]
    });
};