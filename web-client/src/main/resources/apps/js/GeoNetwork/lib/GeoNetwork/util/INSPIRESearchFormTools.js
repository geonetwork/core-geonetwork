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
    /** api:method[getServiceTypeField]
     *  :param multi: Create fields with multiselection combobox.
     *
     *  :return: service types combo box
     *
     */
    getServiceTypeField : function (multi) {
        var serviceTypes = [ [ 'discovery', OpenLayers.i18n('serviceType_discovery')  ], 
                             [ 'view', OpenLayers.i18n('serviceType_view')  ],
                             [ 'download', OpenLayers.i18n('serviceType_download')  ],
                             [ 'transformation', OpenLayers.i18n('serviceType_transformation') ],
                             [ 'invoke', OpenLayers.i18n('serviceType_invoke') ],[ 'other', OpenLayers.i18n('serviceType_other') ] ];

        config = {
                id : 'serviceType',
                name : 'E_serviceType',
                mode : 'local',
                triggerAction : 'all',
                fieldLabel : OpenLayers.i18n('serviceType'),
                // value: annexes[1], // Set arbitrarily the second value of the
                // array as the default one.
                store : new Ext.data.ArrayStore({
                    id : 0,
                    fields : [ 'id', 'label' ],
                    data : serviceTypes
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

    /** api:method[getAnnexField] 
     *  :param multi: Create fields with multiselection combobox.
     *  
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
     *    INSPIRE
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
     * 
     *  :param services: Catalogue service URLs (eg. catalogue.services).
     *  
     *  :return: An INSPIRE themes text field with autocompletion (based on Lucene field content - not a thesaurus)
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
     *  :param services: Catalogue service URLs (eg. catalogue.services).
     *  :param multi: Create fields with multiselection combobox.
     *  
     *  :return: An INSPIRE themes combo box
     * 
     *  Use xml.search.keywords service to retrieve the list of all INSPIRE themes
     *  in current GUI language.
     * 
     *  TODO : Improve support of multilingual search for INSPIRE themes
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
    /** api:method[getClassificationDataServicesField]
     *  :param services: Catalogue service URLs (eg. catalogue.services).
     *  :param multi: Create fields with multiselection combobox.
     *
     *  :return: An INSPIRE classification data services combo box
     *
     *  Use xml.search.keywords service to retrieve the list of all INSPIRE themes
     *  in current GUI language.
     *
     */
    getClassificationDataServicesField : function (services, multi) {
        var Keyword, classificationDataServicesStore;

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
        classificationDataServicesStore = new Ext.data.Store({
            proxy : new Ext.data.HttpProxy({
                url : services.searchKeyword,
                method : 'GET'
            }),
            baseParams : {
                pNewSearch : true,
                pTypeSearch : 1,
                pKeyword: '*',
                pThesauri : 'external.theme.inspire-service-taxonomy',
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

        classificationDataServicesStore.load();
        var config = {
            id : 'keyword',
            name : 'E_keyword',
            mode : 'local',
            triggerAction : 'all',
            fieldLabel : OpenLayers.i18n('inspireClassificationDataServices'),
            store : classificationDataServicesStore,
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
     *  :param services: Catalogue service URLs (eg. catalogue.services).
     *  :param multi: Create fields with multiselection combobox.
     *  :param config: Configure fields to be displayed (withAnnex, withServiceType, 
     *  withDataService, withTheme, withRelated).
     *  :return: An INSPIRE form
     * 
     *  Create an INSPIRE form with annexes, themes and related checkbox fields.
     */
    getINSPIREFields : function (services, multi, config) {
        if (!config){
            return [this.getAnnexField(multi),
                        this.getServiceTypeField(multi),
                        this.getClassificationDataServicesField(services, multi),
                        this.getThemesField(services, multi), 
                        this.getRelatedField()];
        } else {
            var f = [];
            config.withAnnex && f.push(this.getAnnexField(multi));
            config.withServiceType && f.push(this.getServiceTypeField(multi));
            config.withDataService && f.push(this.getClassificationDataServicesField(services, multi));
            config.withTheme && f.push(this.getThemesField(services, multi));
            config.withRelated && f.push(this.getRelatedField());
            return f;
        }
    }
};
