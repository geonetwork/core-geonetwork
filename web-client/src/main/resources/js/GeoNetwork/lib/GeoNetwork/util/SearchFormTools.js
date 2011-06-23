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
 *  class = SearchFormTools
 */
/** api: example
 *  SearchFormTools help to quickly create simple or advanced form
 *  or any GeoNetwork default search fields.
 *
 *
 *  .. code-block:: javascript
 *
 *      searchForm = new Ext.FormPanel({
 *                items : GeoNetwork.SearchFormTools.getAdvancedFormFields(catalogue.services),
 *               ...
 *
 *
 *
 *  TODO : Add INSPIRE fields
 *  TODO : Add distributed search
 *
 */
GeoNetwork.util.SearchFormTools = {

    /** api:method[getSimpleFormFields]
     *  :param services: Optional GeoNetwork services URL used for OpenSearch suggestion URL. If not defined, no suggestion fields.
     *  :return: A GeoNetwork simple form
     *
     *  Create a simple form
     */
    getSimpleFormFields: function(services, layers, mapOptions, withTypes, activeMapControlExtent){
        var fields = [];
        if (services) {
            fields.push(new GeoNetwork.form.OpenSearchSuggestionTextField({
                hideLabel: true,
                width: 285,
                minChars: 2,
                loadingText: '...',
                hideTrigger: true,
                url: services.opensearchSuggest
            }));
        } else {
            fields.push(GeoNetwork.util.SearchFormTools.getFullTextField());
        }
        
        if (withTypes) {
            fields.push(GeoNetwork.util.SearchFormTools.getTypesField());
        }
        
        if (layers) {
            fields.push(GeoNetwork.util.SearchFormTools.getSimpleMap(layers, mapOptions, activeMapControlExtent));
        }
        
        fields.push(GeoNetwork.util.SearchFormTools.getOptions());
        return fields;
    },
    /** api:method[getAdvancedFormFields]
     *  :param services: Optional GeoNetwork services URL used for OpenSearch suggestion URL. If not defined, no suggestion fields.
     *  :return: A GeoNetwork advanced form
     *
     *  Create a advanced form
     */
    getAdvancedFormFields: function(services, layers, mapOptions){
        var fields = [], fullTextField, themekeyField, orgNameField;
        if (services) {
            fullTextField = new GeoNetwork.form.OpenSearchSuggestionTextField({
                hideLabel: true,
                width: 285, // FIXME
                minChars: 2,
                loadingText: '...',
                hideTrigger: true,
                url: services.opensearchSuggest
            });
            orgNameField = new GeoNetwork.form.OpenSearchSuggestionTextField({
                hideLabel: false,
                minChars: 1,
                hideTrigger: false,
                url: services.opensearchSuggest,
                field: 'orgName', 
                name: 'E_orgName', 
                fieldLabel: OpenLayers.i18n('org')
            });
            themekeyField = new GeoNetwork.form.OpenSearchSuggestionTextField({
                hideLabel: false,
                minChars: 1,
                hideTrigger: false,
                url: services.opensearchSuggest,
                field: 'keyword', 
                name: 'E_themekey', 
                fieldLabel: OpenLayers.i18n('keyword')
            });
        } else {
            fullTextField = GeoNetwork.util.SearchFormTools.getFullTextField();
            themekeyField = GeoNetwork.util.SearchFormTools.getKeywordsField();
            orgNameField = GeoNetwork.util.SearchFormTools.getOrgNameField();
        }
        
        var advancedTextField = GeoNetwork.util.SearchFormTools.getAdvancedTextFields();
        var titleField = GeoNetwork.util.SearchFormTools.getTitleField();
        var abstractField = GeoNetwork.util.SearchFormTools.getAbstractField();
        
        var types = GeoNetwork.util.SearchFormTools.getTypesField();
        
        var mapTypes = {
            xtype: 'fieldset',
            title: OpenLayers.i18n('mapTypes'),
            autoHeight: true,
            autoWidth: true,
            collapsible: true,
            collapsed: true,
            defaultType: 'checkbox',
            defaults: {
                width: 160
            },
            items: GeoNetwork.util.SearchFormTools.getMapTypesField()
        };
        
        var accuracySettings = {
            xtype: 'fieldset',
            title: OpenLayers.i18n('similarity'),
            autoHeight: true,
            autoWidth: true,
            layout: 'column',
            collapsible: true,
            collapsed: true,
            items: [GeoNetwork.util.SearchFormTools.getSimilarityField()]
        };
        
        var geomField = new Ext.form.TextField({
            name: 'E_geometry',
            id: 'geometry',
            fieldLabel: OpenLayers.i18n('wkt'),
            // toolTip : '(eg. POLYGON((-180 -90,180 -90,180 90,-180 90,-180
            // -90)) or POINT(6 10))',
            hideLabel: false
        });
        
        
        var mapLayers = [], i;
        for (i = 0; i < layers.length; i++) {
            //mapLayers.push(new OpenLayers.Layer.WMS(layers[i][0], layers[i][1], layers[i][2], layers[i][3]));
            mapLayers.push(layers[i].clone());
        }
        var geomWithMapField = {
            xtype: 'gn_geometrymapfield',
            geometryFieldId: 'geometry',
            //width : 300,
            id: 'geometryMap', // FIXME : hardcoded id required to get map ref 
            layers: mapLayers,
            mapOptions: mapOptions
        };
        
        var nearYouButton = GeoNetwork.util.SearchFormTools.getNearYouButton('geometry');
        
        
        // Default relation is intersection. Geometry field could be used alone.
        var geoRelationField = GeoNetwork.util.SearchFormTools.getRelationField();
        var denominatorField = GeoNetwork.util.SearchFormTools.getScaleDenominatorField(true);
        
        
        var geoFields = new Ext.form.FieldSet({
            title: OpenLayers.i18n('spatialSearch'),
            autoWidth: true,
            collapsible: true,
            collapsed: true,
            items: [geomField, nearYouButton, geomWithMapField, geoRelationField]
        });
        
        
        var when = new Ext.form.FieldSet({
            title: OpenLayers.i18n('when'),
            autoWidth: true,
            //layout: 'row',
            defaultType: 'datefield',
            collapsible: true,
            collapsed: true,
            items: GeoNetwork.util.SearchFormTools.getWhen()
        });
        
        
        // Admin option
        var catalogueField = GeoNetwork.util.SearchFormTools.getCatalogueField(services.getSources, services.logoUrl);
        var groupField = GeoNetwork.util.SearchFormTools.getGroupField(services.getGroups);
        var metadataTypeField = GeoNetwork.util.SearchFormTools.getMetadataTypeField();
        var categoryField = GeoNetwork.util.SearchFormTools.getCategoryField(services.getCategories);
        
        var options = GeoNetwork.util.SearchFormTools.getOptions();
        
        fields.push(fullTextField, advancedTextField, titleField, abstractField, themekeyField, orgNameField, 
                        geoFields, types, mapTypes, denominatorField, when, 
                        catalogueField, groupField, metadataTypeField, categoryField, options, accuracySettings);
        
        return fields;
    },
    /** api:method[getSimpleMap]
     *  :return: An array of component with a hidden geometry field
     *  and a simple map.
     *
     *  Create simple map search field.
     *
     *  TODO : Add more options ? See GeometryMapField
     */
    getSimpleMap: function(layers, mapOptions, activeMapControlExtent){
        var fields = [], mapLayers = [], i;
        
        var geomField = new Ext.form.TextField({
            name: 'E_geometry',
            id: 'geometry',
            inputType: 'hidden'
        });
        
        for (i = 0; i < layers.length; i++) {
            mapLayers.push(layers[i].clone());
            //mapLayers.push(new OpenLayers.Layer.WMS(layers[i][0], layers[i][1], layers[i][2], layers[i][3]));
        }
        var geomWithMapField = {
            xtype: 'gn_geometrymapfield',
            geometryFieldId: 'geometry',
            id: 'geometryMap',
            width : 290,
            layers: mapLayers,
            mapOptions: mapOptions,
            activated: activeMapControlExtent
            // restrictToMapExtent: true
        };
        fields.push(geomField, geomWithMapField);
        
        return fields;
    },
    /** api:method[getOptions]
     *  :param hitsPerPageOptions: ``Array(String)``    List of options for hits per page field
     *  :return: An options fieldset
     *
     *  Create option fields
     */
    getOptions: function(hitsPerPageOptions){
        var hitsPerPage = hitsPerPageOptions ||
        [['10'], ['20'], ['50'], ['100']];
        
        /* Extra option */
        var sortByFields = GeoNetwork.util.SearchFormTools.getSortByCombo();
        
        var hitsPerPageField = new Ext.form.ComboBox({
            id: 'E_hitsperpage',
            name: 'E_hitsperpage',
            mode: 'local',
            triggerAction: 'all',
            fieldLabel: OpenLayers.i18n('hitsPerPage'),
            value: hitsPerPage[1], // Set arbitrarily the second value of the
            // array as the default one.
            store: new Ext.data.ArrayStore({
                id: 0,
                fields: ['id'],
                data: hitsPerPage
            }),
            valueField: 'id',
            displayField: 'id'
        });
        var options = new Ext.form.FieldSet({
            title: 'Options',
            autoWidth: true,
            collapsible: true,
            collapsed: true,
            defaults: {
                width: 160
            },
            items: [sortByFields, hitsPerPageField]
        });
        
        return options;
    },
    getSortByCombo: function(){
        var sortByField = new Ext.form.TextField({
            name: 'E_sortBy',
            id: 'E_sortBy',
            inputType: 'hidden'
        });
        var sortOrderField = new Ext.form.TextField({
            name: 'E_sortOrder',
            id: 'sortOrder',
            inputType: 'hidden'
        });
        
        var combo = new Ext.form.ComboBox({
            mode: 'local',
            fieldLabel: OpenLayers.i18n('sortBy'),
            triggerAction: 'all',
            value: 'relevance',
            store: GeoNetwork.util.SearchFormTools.getSortByStore(),
            valueField: 'id',
            displayField: 'name',
            listeners: {
                change: function(cb, newValue, oldValue){
                    /* Adapt sort order according to sort field */
                   var tokens = newValue.split('#');
                   sortByField.setValue(tokens[0]);
                   sortOrderField.setValue(tokens[1]);
                }
            }
        });
        return [sortByField, sortOrderField, combo];
    },
    getSortByStore: function(){
        return new Ext.data.ArrayStore({
            id: 0,
            fields: ['id', 'name'],
            data: [['relevance#', OpenLayers.i18n('relevance')], 
                    ['title#reverse', OpenLayers.i18n('title')], 
                    ['changeDate#', OpenLayers.i18n('changeDate')], 
                    ['rating#', OpenLayers.i18n('rating')], 
                    ['popularity#', OpenLayers.i18n('popularity')], 
                    ['denominator#', OpenLayers.i18n('scaleDesc')], 
                    ['denominator#revers', OpenLayers.i18n('scaleAsc')]]
        });
    },
    /** api:method[getFullTextField]
     *  :return: A full text search text field
     *
     *  Create full text search field
     */
    getFullTextField: function(){
        return new Ext.form.TextField({
            name: 'E_any',
            id: 'E_any',
            fieldLabel: OpenLayers.i18n("fullTextSearch"),
            hideLabel: false
        });
    },
    /** api:method[getTitleField]
     *  :return: A title field
     *
     *  Create a title search field
     */
    getTitleField: function(){
        return GeoNetwork.util.SearchFormTools.getTextField('E_title', OpenLayers.i18n('title'));
    },
    /** api:method[getAbstractField]
     *  :return: An abstract field
     *
     *  Create an abstract field
     */
    getAbstractField: function(){
        return GeoNetwork.util.SearchFormTools.getTextField('E_abstract', OpenLayers.i18n('abstract'));
    },
    /** api:method[getKeywordsField]
     *  :return: A keyword field
     *
     *  Create keyword field
     */
    getKeywordsField: function(){
        return GeoNetwork.util.SearchFormTools.getTextField('E_themekey', OpenLayers.i18n('keyword'));
    },
    /** api:method[getOrgNameField]
     *  :return: A organisation name search field
     *
     *  Create organisation name search field
     */
    getOrgNameField: function(){
        return GeoNetwork.util.SearchFormTools.getTextField('E_orgName', OpenLayers.i18n('org'));
    },
    /** api:method[getTextField]
     *  :param name: ``String``    Text field name
     *  :param fieldLabel: ``String``    Text field label
     *  :return: a text field
     *
     *  Create text field
     */
    getTextField: function(name, fieldLabel){
        return new Ext.form.TextField({
            name: name,
            fieldLabel: fieldLabel,
            hideLabel: false
        });
    },
    /** api:method[getCatalogueField]
     *  :return:
     *
     *  Create catalogue combo
     *  DataView
     *  TODO : list from server
     */
    getCatalogueField: function(url, logoUrl){
        // TODO : make multiple selection
        
        var catStore = GeoNetwork.data.CatalogueSourceStore(url);
        catStore.load();
        
        return new Ext.form.ComboBox({
            name: 'E_siteId',
            mode: 'local',
            triggerAction: 'all',
            fieldLabel: OpenLayers.i18n('catalogue'),
            store: catStore,
            valueField: 'id',
            displayField: 'name',
            tpl: '<tpl for="."><div class="x-combo-list-item logo"><img src="' + logoUrl + '{id}.gif"/>{name}</div></tpl>'
        });
    },
    /** api:method[getGroupField]
     *  :return: A group combo
     *
     *  Create a list of group
     *
     *  TODO : list from server
     */
    getGroupField: function(url){
        var groupStore = GeoNetwork.data.GroupStore(url);
        groupStore.load();
        
        return new Ext.form.ComboBox({
            name: 'E_group',
            mode: 'local',
            triggerAction: 'all',
            fieldLabel: OpenLayers.i18n('group'),
            store: groupStore,
            valueField: 'id',
            displayField: 'name',
            tpl: '<tpl for="."><div class="x-combo-list-item">{[values.label.' + OpenLayers.Lang.getCode() + ']}</div></tpl>'
        });
    },
    /** api:method[getMetadataTypeField]
     *  :return: A metadata type combo
     *
     *  Create a combo with template or metadata options
     */
    getMetadataTypeField: function(){
        return new Ext.form.ComboBox({
            name: 'E_template',
            mode: 'local',
            triggerAction: 'all',
            fieldLabel: OpenLayers.i18n('kind'),
            store: new Ext.data.ArrayStore({
                id: 0,
                fields: ['id', 'name'],
                data: [['n', OpenLayers.i18n('md')], ['y', OpenLayers.i18n('tpl')]]
            }),
            valueField: 'id',
            displayField: 'name'
        });
    },
    /** api:method[getCategoryField]
     *  :return: A category combobox
     *
     *  Create category combo
     *
     *  TODO : retrieve from server
     */
    getCategoryField: function(url, imgUrl){
        var store = GeoNetwork.data.CategoryStore(url);
        
        store.load();
        
        return new Ext.form.ComboBox({
            name: 'E_category',
            mode: 'local',
            fieldLabel: OpenLayers.i18n('category'),
            triggerAction: 'all',
            store: store,
            valueField: 'name',
            displayField: 'name',
            tpl: (imgUrl ?
                '<tpl for="."><div class="x-combo-list-item"><img src="' + imgUrl + '{name}.png"/>{[values.label.' + OpenLayers.Lang.getCode() + ']}</div></tpl>':
                '<tpl for="."><div class="x-combo-list-item">{[values.label.' + OpenLayers.Lang.getCode() + ']}</div></tpl>')
        });
    },
    /** api:method[getAdvancedTextFields]
     *  :return: A fieldset with advanced text search
     *
     *  Create fieldset with extra search on text (or, exact, all, without searches)
     */
    getAdvancedTextFields: function(){
        return {
            xtype: 'fieldset',
            title: OpenLayers.i18n('advTextSearch'),
            autoHeight: true,
            autoWidth: true,
            collapsible: true,
            collapsed: true,
            defaultType: 'textfield',
            items: [{
                name: 'E_or',
                fieldLabel: OpenLayers.i18n('eitherWords'),
                hideLabel: false
            }, {
                name: 'E_phrase',
                fieldLabel: OpenLayers.i18n('exactPhrase'),
                hideLabel: false
            }, {
                name: 'E_all',
                fieldLabel: OpenLayers.i18n('allWords'),
                hideLabel: false
            }, {
                name: 'E_without',
                fieldLabel: OpenLayers.i18n('withoutWords') ,
                hideLabel: false
            }]
        };
        
    },
    /** api:method[getSimilarityField]
     *  :return: A similarity radio group
     *
     *  Create similarity field
     */
    getSimilarityField: function(){
        return {
            xtype: 'radiogroup',
            items: [{
                xtype: 'label',
                text: OpenLayers.i18n('precise')
            }, {
                name: 'E_similarity',
                inputValue: 1
            }, {
                name: 'E_similarity',
                inputValue: 0.8,
                checked: true
            }, {
                name: 'E_similarity',
                inputValue: 0.6
            }, {
                name: 'E_similarity',
                inputValue: 0.4
            }, {
                name: 'E_similarity',
                inputValue: 0.2
            }, {
                xtype: 'label',
                text: OpenLayers.i18n('imprecise')
            }]
        };
    },
    /** api:method[getRelationField]
     *  :return: A combo with geom relations
     *
     *  Create geometry relation field
     */
    getRelationField: function(){
        return new Ext.form.ComboBox({
            name: 'E_relation',
            mode: 'local',
            width: 150,
            triggerAction: 'all',
            fieldLabel: OpenLayers.i18n('relationType'),
            store: new Ext.data.ArrayStore({
                id: 0,
                fields: ['relation', 'label'],
                data: [['', ''],  
                        ['intersection', OpenLayers.i18n('intersection')], 
                        ['overlaps', OpenLayers.i18n('overlaps')], 
                        ['encloses', OpenLayers.i18n('encloses')], 
                        ['fullyOutsideOf', OpenLayers.i18n('fullyOutsideOf')], 
                        ['crosses', OpenLayers.i18n('crosses')], 
                        ['touches', OpenLayers.i18n('touches')], 
                        ['within', OpenLayers.i18n('within')]]
            }),
            valueField: 'relation',
            displayField: 'label'
        });
    },
    /** api:method[getNearYouButton]
     *  :param geomFieldId: ``String``   Field id to update on click
     *  :return: A button
     *
     *  Create a button which update the associated text field with
     *  the coordinate compute by the browser.
     */
    getNearYouButton: function(geomFieldId){
        return new Ext.Button({
            text:  OpenLayers.i18n('nearYou'),
            iconCls: 'md-mn mn-user-location',
            iconAlign: 'right',
            //width : 100,
            listeners: {
                click: function(){
                    if (navigator.geolocation) {
                        navigator.geolocation.getCurrentPosition(function(position){
                            Ext.getCmp(geomFieldId).setValue('POINT(' + position.coords.latitude + 
                                                                  ' ' + position.coords.longitude + ')');
                        });
                    }
                }
            }
        });
        
    },
    /** api:method[registerDateVtype]
     *  :return: ``Boolean``
     *
     *  Register a new VTypes for date range in order to
     *  link a start and end date Date field and control
     *  value range.
     */
    registerDateVtype: function(){
        if (Ext.form.VTypes.daterange) {
            return;
        }
        
        Ext.apply(Ext.form.VTypes, {
            daterange: function(val, field){
                var date = field.parseDate(val);
                
                if (!date) {
                    return false;
                }
                if (field.startDateField &&
                (!this.dateRangeMax || (date.getTime() !== this.dateRangeMax.getTime()))) {
                    var start = Ext.getCmp(field.startDateField);
                    start.setMaxValue(date);
                    start.validate();
                    this.dateRangeMax = date;
                } else if (field.endDateField &&
                (!this.dateRangeMin || (date.getTime() !== this.dateRangeMin.getTime()))) {
                    var end = Ext.getCmp(field.endDateField);
                    end.setMinValue(date);
                    end.validate();
                    this.dateRangeMin = date;
                }
                /*
                 * Always return true since we're only using this vtype to set
                 * the min/max allowed values (these are tested for after the
                 * vtype test)
                 */
                return true;
            }
        });
        
    },
    getWhen: function(){
        var anyTime = new Ext.form.Checkbox({
                name: 'timeType',
                checked: true,
                fieldLabel: OpenLayers.i18n('anyTime'),
                handler: function(ck, checked){
                    // clean children with calendar type
                    
                    ck.ownerCt.items.each(function(item, index, length){
                        if (checked && item.getXType() === 'datefield') {
                            item.setValue('');
                        }
                    });
                }
            });
        
        var items = [anyTime, {
                xtype: 'label',
                text: OpenLayers.i18n('mdChangeDate')
            }];
        items.push(GeoNetwork.util.SearchFormTools.getMetadataDateField(anyTime));
        items.push({
                xtype: 'label',
                text: OpenLayers.i18n('tempExtent')
            });
        items.push(GeoNetwork.util.SearchFormTools.getTemporalExtentField(anyTime));

        return items;
        
    },
    /** api:method[getMetadataDateField]
     *  :return: Metadata date extent fields
     *
     *  Create metadata date extent fields and register DateVtype if needed.
     */
    getMetadataDateField: function(anyTime){
        GeoNetwork.util.SearchFormTools.registerDateVtype();
        return GeoNetwork.util.SearchFormTools.getDateRangeFields(
            'E_dateFrom', 'from', 'dateFrom', 
            'E_dateTo', 'to', 'dateTo', 
            anyTime
        );
    },
    /** api:method[getTemporalExtentField]
     *  :return: Temporal extent fields
     *
     *  Create temporal extent fields and register DateVtype if needed.
     */
    getTemporalExtentField: function(anyTime){
        GeoNetwork.util.SearchFormTools.registerDateVtype();
        return GeoNetwork.util.SearchFormTools.getDateRangeFields(
            'E_extFrom', 'from', 'extFrom', 
            'E_extTo', 'to', 'extTo', 
            anyTime
        );
    },
    getDateRangeFields: function(nameFrom, labelFrom, idFrom, nameTo, labelTo, idTo, anyTime){
        GeoNetwork.util.SearchFormTools.registerDateVtype();
        var changeCb = function(field, newValue, oldValue){
                    if (this && newValue !== '') {
                        this.setValue(false);
                    }
                };
        return [{
            fieldLabel: OpenLayers.i18n(labelFrom),
            name: nameFrom,
            id: idFrom,
            width : 120,
            vtype: 'daterange',
            endDateField: idTo,
            format: 'd/m/Y',
            listeners: {
                change: changeCb,
                scope: anyTime
            }
        }, {
            fieldLabel: OpenLayers.i18n(labelTo),
            name: nameTo,
            id: idTo,
            width : 120,
            vtype: 'daterange',
            startDateField: idFrom,
            format: 'd/m/Y',
            listeners: {
                change: changeCb,
                scope: anyTime
            }
        }];
    },
    /** api:method[getScaleDenominatorField]
     *  :return: a multi slider fields to search on scale
     *  :param disabled: ``boolean`` to enable or disable the field by default.
     *  
     *  Create a multi slider fields to search on scale using
     *  denominatorFrom and denominatorTo form parameter.
     *  
     */
    getScaleDenominatorField: function(disabled, min, max, increment, width){
        min = min || 1000;
        max = max || 1000000;
        increment = increment || 1000;  // TODO : exp increment
        width = width || 230;
        
        var tip = new Ext.slider.Tip({
            getText: function(thumb){
                return String.format('<b>' + OpenLayers.i18n('scaleNominator') + '{0}</b>', thumb.value);
            }
        });
        
        var denominatorFrom = new Ext.form.Field({
           name: 'E_denominatorFrom',
           value: '',
           hidden: true
        });
        var denominatorTo = new Ext.form.Field({
           name: 'E_denominatorTo',
           value: '',
           hidden: true
        });
        
        var slider = new Ext.slider.MultiSlider({
            disabled: disabled ? true : false,
            width: width,
            minValue: min,
            maxValue: max,
            increment: increment,
            values: [min, max],
            formFields: [denominatorFrom, denominatorTo],
            plugins: tip,
            listeners: {
                'change': function(sliders, newValue, thumb){
                    sliders.formFields[thumb.index].setValue(newValue);
                }
            }
        });
        
        var scaleCk = new Ext.form.Checkbox({
            fieldLabel: OpenLayers.i18n('scale'),
            value: disabled ? true : false,
            handler: function (ch, checked){
                slider.setDisabled(!checked);
                if (!checked) {
                    denominatorFrom.setValue('');
                    denominatorTo.setValue('');
                }
            }
        });
        return [denominatorFrom, denominatorTo, scaleCk, slider];
    },
    /** api:method[getMapTypesField]
     *  :return: A map type options
     *
     */
    getMapTypesField: function(){
        var spatialTypes = GeoNetwork.util.SearchFormTools.getSpatialRepresentationTypeField();
        
        return [{
            hideLabel: true,
            boxLabel: OpenLayers.i18n('digital'),
            name: 'B_digital'
        }, {
            hideLabel: true,
            boxLabel: OpenLayers.i18n('hardCopy'),
            name: 'B_paper'
        }, {
            hideLabel: true,
            boxLabel: OpenLayers.i18n('download'),
            name: 'B_download'
        }, {
            hideLabel: true,
            boxLabel: OpenLayers.i18n('interactive'),
            name: 'B_dynamic'
        }, spatialTypes];
        
    },
    /** api:method[getValidField]
     *  :return: Validation status field
     *
     */
    getValidField: function(){
        var metadataType = new Ext.form.ComboBox({
            name: 'E__valid',
            mode: 'local',
            autoSelect: false,
            triggerAction: 'all',
            fieldLabel: OpenLayers.i18n('validationStatus'),
            store: new Ext.data.ArrayStore({
                id: 0,
                fields: ['id', 'name'],
                data: [['1', OpenLayers.i18n('valid')], 
                        ['0', OpenLayers.i18n('notValid')],
                        ['-1', OpenLayers.i18n('notDetermined')]]
            }),
            valueField: 'id',
            displayField: 'name'
        });
        return metadataType;
        
    },
    /** api:method[getTypesField]
     *  :return: Type selection using combo box based
     *  on hierarchy level values.
     *
     *  // TODO : Add other options
     */
    getTypesField: function(){
        var metadataType = new Ext.form.ComboBox({
            name: 'E_type',
            mode: 'local',
            autoSelect: false,
            triggerAction: 'all',
            fieldLabel: OpenLayers.i18n('resourceType'),
            store: new Ext.data.ArrayStore({
                id: 0,
                fields: ['id', 'name'],
                data: [['dataset', OpenLayers.i18n('dataset')], 
                        ['series', OpenLayers.i18n('series')],
                        ['service', OpenLayers.i18n('service')]]
            }),
            valueField: 'id',
            displayField: 'name'
        });
        return metadataType;
        
    },
    /** api:method[getSpatialRepresentationTypeField]
     *  :param codeList: ``Array`` of values 
     *  :return: Spatial representation types combo box
     * 
     *  Spatial representation type is based on 
     *  gmd:spatialRepresentationType/gmd:MD_SpatialRepresentationTypeCode/@codeListValue
     *  ISO19115 codelist. Applying such a filter will not return non ISO records. 
     */
    getSpatialRepresentationTypeField: function(codeList){
        var defaultCodeList = [['grid', OpenLayers.i18n('grid')], 
                       ['stereoModel', OpenLayers.i18n('stereoModel')], 
                       ['tin', OpenLayers.i18n('tin')], 
                       ['textTabled', OpenLayers.i18n('textTable')], 
                       ['vector', OpenLayers.i18n('vector')], 
                       ['video', OpenLayers.i18n('video')]];
        
        codeList = codeList || defaultCodeList;
        
         var spatialRepresentationType = new Ext.form.ComboBox({
            name: 'E_spatialRepresentationType',
            mode: 'local',
            autoSelect: false,
            triggerAction: 'all',
            fieldLabel: OpenLayers.i18n('spatialRepType'),
            store: new Ext.data.ArrayStore({
                id: 0,
                fields: ['id', 'name'],
                data: codeList
            }),
            valueField: 'id',
            displayField: 'name'
        });
        
        return spatialRepresentationType;
        
    }
};
