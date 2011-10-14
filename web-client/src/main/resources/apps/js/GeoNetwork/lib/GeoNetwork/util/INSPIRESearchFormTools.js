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
 *  class = INSPIRESearchFormTools
 */
/**
 * api: example INSPIRESearchFormTools help to quickly create simple INSPIRE
 * form
 * 
 */
GeoNetwork.util.INSPIRESearchFormTools = {
    /** api:method[getAnnexField] 
     *  :return: Annex I, II, III combo box
     * 
     */
    getAnnexField : function (multi) {
        var annexes = [ [ 'i', 'I' ], [ 'ii', 'II' ], [ 'iii', 'III' ] ], 
            config = {
                    id : 'inspireannex',
                    name : 'E_inspireannex',
                    mode : 'local',
                    triggerAction : 'all',
                    fieldLabel : OpenLayers.i18n('inspireannex'),
                    // value: annexes[1], // Set arbitrarily the second value of the
                    // array as the default one.
                    store : new Ext.data.ArrayStore({
                        id : 0,
                        fields : [ 'id', 'label' ],
                        data : annexes
                    }),
                    valueField : 'id',
                    displayField : 'label'
                };
        if (multi) {
            Ext.apply(config, {
                valueDelimiter: ' or '
                });
            return new Ext.ux.form.SuperBoxSelect(config);
        } else {
            return new Ext.form.ComboBox(config);
        }
    },
    /** api:method[getRelatedField] 
     *  :return: Checkbox for dataset related to
     * INSPIRE
     * 
     */
    getRelatedField : function () {
        var inspirerelated = new Ext.form.Checkbox({
            hideLabel : true,
            boxLabel : OpenLayers.i18n('inspirerelated'),
            tabTip : OpenLayers.i18n('inspirerelatedtt'),
            name : 'O_inspirerelated'
        });
        return inspirerelated;
    },
    /** api:method[getThemesFieldWithSuggestion] 
     * :return:
     * 
     */
    getThemesFieldWithSuggestion : function (services) {
        var inspiretheme = new GeoNetwork.form.OpenSearchSuggestionTextField({
            hideLabel : false,
            minChars : 1,
            hideTrigger : false,
            url : services.opensearchSuggest,
            field : 'inspiretheme',
            name : 'E_inspiretheme',
            fieldLabel : OpenLayers.i18n('inspiretheme')
        });
        return inspiretheme;
    },
    /** api:method[getThemesFieldWithSuggestion] 
     *  :return:
     * 
     * Use xml.search.keywords service to retrieve the list of all INSPIRE themes
     * in current GUI language.
     * 
     * TODO : Improve support of multilingual search for INSPIRE themes
     */
    getThemesField : function (services, multi) {
        var Keyword, themesStore, inspiretheme;
        
        Keyword = Ext.data.Record.create([ {
            name : 'id'
        }, {
            name : 'value'
        }, {
            name : 'definition'
        }, {
            name : 'uri'
        }]);

        // Keyword store
        themesStore = new Ext.data.Store({
            proxy : new Ext.data.HttpProxy({
                url : services.searchKeyword,
                method : 'GET'
            }),
            baseParams : {
                pNewSearch : true,
                pTypeSearch : 1,
                pKeyword: '*',
                pThesauri : 'external.theme.inspire-theme',
                pMode : 'searchBox',
                maxResults : '35'
            },
            reader : new Ext.data.XmlReader({
                record : 'keyword',
                id : 'id'
            }, Keyword),
            fields : [ "id", "value", "definition", "uri" ],
            sortInfo : {
                field : "value"
            }
        });

        themesStore.load();
        var config = {
                id : 'inspiretheme',
                name : 'E_inspiretheme',
                mode : 'local',
                triggerAction : 'all',
                fieldLabel : OpenLayers.i18n('inspiretheme'),
                store : themesStore,
                valueField : 'value',
                displayField : 'value'
            };
        if (multi) {
            Ext.apply(config, {
                valueDelimiter: ' or ',
                stackItems: true
                });
            return new Ext.ux.form.SuperBoxSelect(config);
        } else {
             return new Ext.form.ComboBox(config);
        }
    },
    /** api:method[getINSPIREFields] 
     *  :return: An INSPIRE form
     * 
     * Create an INSPIRE form
     */
    getINSPIREFields : function (services, multi) {
        return [this.getAnnexField(multi), 
                        this.getThemesField(services, multi), 
                        this.getRelatedField()];
    }
};
