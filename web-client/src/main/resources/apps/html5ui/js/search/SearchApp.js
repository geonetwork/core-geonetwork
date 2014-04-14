/*
 * Copyright (C) 2012 GeoNetwork
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
 * All search related stuff. Helps App.js to control and do searches.
 */
GeoNetwork.searchApp = function() {

    // Public Space
    return {
        advSearchForm : null,
        permalinkProvider : null,
        init : function() {
            this.permalinkProvider = Ext.state.Manager.getProvider();

            this.generateSimpleSearchForm();
            this.advSearchForm = this.generateAdvancedSearchForm();
            this.createResultsPanel(this.permalinkProvider);
            resizeResultsPanel();
        },
        addMetadata : function(uuid, clean, edit) {
            // Check integrity of UUID
            if (uuid && uuid !== '' && uuid !== '#?') {
                catalogue.metadataShow(uuid, true);

                if (edit) {
                    // We need to edit the metadata (redirection)
                    app.edit(edit, false);
                }
            }
        },
        /**
         * Set event in order to display some search criteria only when user is
         * logged in.
         */
        setAdminFieldsCallback : function(adminFields) {
            // Hide or show extra fields after login event
            Ext.each(adminFields, function(item) {
                item.setVisible(catalogue.identifiedUser
                        && catalogue.identifiedUser.role === "Administrator");
            });
            catalogue
                    .on(
                            'afterLogin',
                            function() {
                                Ext
                                        .each(
                                                adminFields,
                                                function(item) {
                                                    item
                                                            .setVisible(catalogue.identifiedUser
                                                                    && catalogue.identifiedUser.role === "Administrator");
                                                });
                                GeoNetwork.util.SearchFormTools
                                        .refreshGroupFieldValues();
                            });
            catalogue.on('afterLogout', function() {
                Ext.each(adminFields, function(item) {
                    item.setVisible(!(catalogue.identifiedUser === undefined));
                });
                GeoNetwork.util.SearchFormTools.refreshGroupFieldValues();
            });

        },
        generateSimpleSearchForm : function() {
            var field = new GeoNetwork.form.OpenSearchSuggestionTextField({
                hideLabel : true,
                id : 'fullTextField',
                renderTo : 'fullTextField',
                width : 170,
                minChars : 2,
                loadingText : '...',
                hideTrigger : true,
                url : catalogue.services.opensearchSuggest,
                listeners : {
                    // Updating the hidden search field in the
                    // search form which is going to be submitted
                    change : function() {
                        if (this.getValue().length > 0)
                            Ext.getCmp('E_trueany').setValue(
                                    this.getValue() + "*");
                        else
                            Ext.getCmp('E_trueany').setValue(this.getValue());
                    },
                    keyup : function(e, a) {
                        if (this.getValue().length > 0)
                            Ext.getCmp('E_trueany').setValue(
                                    this.getValue() + "*");
                        else
                            Ext.getCmp('E_trueany').setValue(this.getValue());
                        if (a.ENTER == a.keyCode) {
                            e.list.hide();
                            Ext.getCmp('advanced-search-options-content-form')
                                    .fireEvent('search');
                        }
                    }
                }
            });

        },
        generateAdvancedSearchForm : function() {
            var advancedCriteria = [];

            // This is the full text search field populated when the
            // one from the simple search form is updated.
            // This field is hidden
            var any = new Ext.form.TextField({
                name : 'E_any_OR_geokeyword',
                id : 'E_trueany',
                hidden : true
            });

            // These are the basic search (dummy)
            var onlineData = new Ext.form.Checkbox({
                name : 'O_dynamic',
                id : 'E_dynamic',
                hidden : true
            });

            var onlineData_ = new Ext.form.Checkbox({
                name : 'O_dynamic',
                id : 'o_dynamic',
                boxLabel : OpenLayers.i18n('Online data'),
                renderTo : "ck1"
            });

            onlineData_.on("check", function(el) {
                Ext.getCmp('E_dynamic').setValue(this.getValue());
                Ext.getCmp('advanced-search-options-content-form').fireEvent(
                        'search');
            });

            var dataForDownload = new Ext.form.Checkbox({
                name : 'O_download',
                id : 'E_download',
                hidden : true
            });

            var dataForDownload_ = new Ext.form.Checkbox({
                name : 'O_download_',
                id : 'o_download',
                boxLabel : OpenLayers.i18n('Data for download'),
                renderTo : "ck2"
            });

            dataForDownload_.on("check", function(el) {
                Ext.getCmp('E_download').setValue(this.getValue());
                Ext.getCmp('advanced-search-options-content-form').fireEvent(
                        'search');
            });

            var noDirectDownload = new Ext.form.Checkbox({
                name : 'O_nodynamicdownload',
                id : 'E_nodynamicdownload',
                hidden : true
            });

            var noDirectDownload_ = new Ext.form.Checkbox({
                name : 'O_nodynamicdownload_',
                id : 'o_nodynamicdownload',
                boxLabel : OpenLayers.i18n('No direct download'),
                renderTo : "ck3"
            });

            noDirectDownload_.on("check", function(el) {
                Ext.getCmp('E_nodynamicdownload').setValue(this.getValue());
                Ext.getCmp('advanced-search-options-content-form').fireEvent(
                        'search');
            });

            // Multi select keyword
            var themekeyStore = new GeoNetwork.data.OpenSearchSuggestionStore({
                url : catalogue.services.opensearchSuggest,
                rootId : 1,
                baseParams : {
                    field : 'keyword'
                }
            });

            var themekeyField = new Ext.ux.form.SuperBoxSelect({
                hideLabel : false,
                minChars : 0,
                queryParam : 'q',
                hideTrigger : false,
                id : 'E_themekey',
                name : 'E_themekey',
                store : themekeyStore,
                valueField : 'value',
                displayField : 'value',
                valueDelimiter : ' or ',
                // tpl: tpl,
                fieldLabel : OpenLayers.i18n('keyword')
            });

            var orgNameStore = new GeoNetwork.data.OpenSearchSuggestionStore({
                url : catalogue.services.opensearchSuggest,
                rootId : 1,
                baseParams : {
                    field : 'orgName'
                }
            });

            var orgNameField = new Ext.ux.form.SuperBoxSelect({
                hideLabel : false,
                minChars : 0,
                queryParam : 'q',
                hideTrigger : false,
                id : 'E_orgName',
                name : 'E_orgName',
                store : orgNameStore,
                valueField : 'value',
                displayField : 'value',
                valueDelimiter : ' or ',
                // tpl: tpl,
                fieldLabel : OpenLayers.i18n('org')
            });
            // var denominatorField = GeoNetwork.util.SearchFormTools
            // .getScaleDenominatorField(true);

            // var groupField = GeoNetwork.util.SearchFormTools.getGroupField(
            // catalogue.services.getGroups, true);
            var metadataTypeField = GeoNetwork.util.SearchFormTools
                    .getMetadataTypeField(true);
            // var validField = GeoNetwork.util.SearchFormTools
            // .getValidField(true);

            // Add hidden fields to be use by quick metadata links from the
            // admin panel (eg. my metadata).
            var ownerField = new Ext.form.TextField({
                name : 'E__owner',
                hidden : true
            });
            var isHarvestedField = new Ext.form.TextField({
                name : 'E__isHarvested',
                hidden : true
            });
            var siteId = new Ext.form.TextField({
                name : 'E_siteId',
                hidden : true
            });
            var serviceTypeField = GeoNetwork.util.INSPIRESearchFormTools
                    .getServiceTypeField(true);
            advancedCriteria.push(themekeyField, orgNameField,
                    metadataTypeField, ownerField, isHarvestedField, siteId);

            var sortByCombo = new Ext.form.TextField({
                name : 'E_sortBy',
                id : 'E_sortBy',
                inputType : 'hidden'
            });

            var orderBy = new Ext.form.TextField({
                name : 'E_sortOrder',
                id : 'E_orderBy',
                inputType : 'hidden'
            });

            var what = {
                title : OpenLayers.i18n('What'),
                id : 'what_adv_search',
                bodyStyle : 'padding:4px',
                layout : 'form',
                defaults : {
                    anchor : '100%'
                },
                forceLayout : true,
                items : [ any, onlineData, dataForDownload, noDirectDownload,
                        sortByCombo, orderBy, advancedCriteria ]
            };

            var mapLayers = [];
            for ( var i = 0; i < GeoNetwork.map.BACKGROUND_LAYERS.length; i++) {
                mapLayers.push(GeoNetwork.map.BACKGROUND_LAYERS[i].clone());
            }
            var geomWithMapField = {
                xtype : 'gn_geometrymapfield',
                id : 'geometryMap',
                layers : mapLayers,
                width : '100%',
                height : '212',
                mapOptions : GeoNetwork.map.MAP_OPTIONS,
                activated : false
            // restrictToMapExtent: true
            };
            var where = {
                title : OpenLayers.i18n('Where'),
                id : 'where_adv_search',
                // bodyStyle : 'padding:4px',
                layout : 'form',
                defaults : {
                    anchor : '100%'
                },
                items : [ geomWithMapField ]
            };

            var when = {
                title : OpenLayers.i18n('When'),
                id : 'when_adv_search',
                bodyStyle : 'padding:4px',
                forceLayout : true,
                defaultType : 'datefield',
                layout : 'form',
                defaults : {
                    anchor : '100%'
                },
                items : GeoNetwork.util.SearchFormTools.getWhen()
            };

            var inspireFields = GeoNetwork.util.INSPIRESearchFormTools
                    .getINSPIREFields(catalogue.services, true, {
                        withAnnex : true,
                        withTheme : true
                    });
            inspireFields.push(serviceTypeField);

            var inspire = {
                title : 'INSPIRE',
                id : 'inspire_adv_search',
                bodyStyle : 'padding:4px',
                defaultType : 'datefield',
                layout : 'form',
                defaults : {
                    anchor : '100%'
                },

                items : inspireFields
            };

            var formItems = [];

            formItems.push({
                id : 'advSearchTabs',
                layout : {
                    type : 'hbox',
                    defaultMargins : '0 5 0 5',
                    pack : 'left',
                    align : 'top',
                    width : '100%'
                },
                border : false,
                items : [ {
                    id : 'what-inspire',
                    layout : 'form',
                    defaults : {
                        anchor : '100%'
                    },
                    border : false,
                    items : [ what, inspire ]
                }, where, when ]
            });

            this.setAdminFieldsCallback([ metadataTypeField ]);

            return new GeoNetwork.SearchFormPanel({
                id : 'advanced-search-options-content-form',
                renderTo : 'advanced-search-options-content',
                stateId : 's',
                autoHeight : true,
                width : '100%',
                border : false,
                searchCb : function() {

                    var any = Ext.get('E_any');
                    if (any) {
                        if (any.getValue() === OpenLayers
                                .i18n('fullTextSearch')) {
                            any.setValue('');
                        }
                    }

                    catalogue.startRecord = 1; // Reset start record
                    searching = true;
                    catalogue.search('advanced-search-options-content-form',
                            app.searchApp.loadResults, null,
                            catalogue.startRecord, true);
                    app.searchApp.firstSearch = true;
                    showSearch();
                },
                listeners : {
                    onreset : function() {
                        if (Ext.getCmp('facets-panel')) {
                            Ext.getCmp('facets-panel').reset();
                        }
                        this.fireEvent('search');

                        GeoNetwork.Util.updateHeadInfo({
                            title : catalogue.getInfo().name
                        });
                    }
                },
                padding : 2,
                items : formItems
            });
        },
        generateFacetedSearchPanel : function() {
            var breadcrumb = new Ext.Panel({
                id : 'bread-crumb',
                renderTo : 'bread-crumb-div',
                layout : 'table',
                cls : 'breadcrumb',
                defaultType : 'button',
                border : false,
                split : false,
                layoutConfig : {
                    columns : 150 //Never fall to a second row
                }
            });
            var facetsPanel = new GeoNetwork.FacetsPanel(
                    {
                        id : 'facets-panel',
                        renderTo : 'facets-panel-div',
                        searchForm : Ext
                                .getCmp('advanced-search-options-content-form'),
                        breadcrumb : breadcrumb,
                        switchFacetsMenu : false,
                        maxDisplayedItems : GeoNetwork.Settings.facetMaxItems || 7,
                        facetListConfig : GeoNetwork.Settings.facetListConfig
                                || []
                    });
        },
        /**
         * Bottom bar
         * 
         * @return
         */
        createBBar : function() {

            var previousAction = new Ext.Action({
                id : 'previousBtFooter',
                text : '&lt;&lt;',
                handler : function() {
                    var from = catalogue.startRecord - 50;
                    if (from > 0) {
                        catalogue.startRecord = from;
                        catalogue.search(
                                'advanced-search-options-content-form',
                                app.searchApp.loadResults, null,
                                catalogue.startRecord, true);
                    }
                },
                scope : this
            });

            var nextAction = new Ext.Action({
                id : 'nextBtFooter',
                text : '&gt;&gt;',
                handler : function() {
                    catalogue.startRecord += 50;
                    catalogue.search('advanced-search-options-content-form',
                            app.searchApp.loadResults, null,
                            catalogue.startRecord, true);
                },
                scope : this
            });

            return new Ext.Toolbar({
                items : [ previousAction, {
                    xtype : 'tbtext',
                    text : '',
                    id : 'infoFooter'
                }, nextAction  ]
            });

        },

        /**
         * Results panel layout with top, bottom bar and DataView
         * 
         * @return
         */
        createResultsPanel : function(permalinkProvider) {
            var metadataResultsView = new GeoNetwork.MetadataResultsView({
                catalogue : catalogue,
                displaySerieMembers : true,
                autoScroll : true,
                autoWidth : false,
                tpl : GeoNetwork.HTML5UI.Templates.FULL,
                templates : {
                    SIMPLE : GeoNetwork.HTML5UI.Templates.SIMPLE,
                    THUMBNAIL : GeoNetwork.HTML5UI.Templates.THUMBNAIL,
                    FULL : GeoNetwork.HTML5UI.Templates.FULL
                },
                featurecolor : GeoNetwork.Settings.results.featurecolor,
                colormap : GeoNetwork.Settings.results.colormap,
                featurecolorCSS : GeoNetwork.Settings.results.featurecolorCSS
            });

            catalogue.resultsView = metadataResultsView;

            // Add results to map
            Ext.each(app.mapApp.maps, function(map) {
                catalogue.resultsView.addMap(map, true);
            });

            var tBar = new GeoNetwork.MetadataResultsToolbar({
                catalogue : catalogue,
                withPaging: true,
                searchFormCmp : Ext
                        .getCmp('advanced-search-options-content-form'),
                sortByCmp : Ext.getCmp('E_sortBy'),
                metadataResultsView : metadataResultsView
            // Permalink provider is broken due to cookie state probably
            // so remove it from the NGR GUI FIXME
            // permalinkProvider : permalinkProvider
            });

            var bBar = this.createBBar();

            // Add handlers for the pagination buttons in the top bar
            Ext.getCmp('previousBt').on('click', function() {
                var from = catalogue.startRecord - 50;
                if (from > 0) {
                    catalogue.startRecord = from;
                    catalogue.search(
                        'advanced-search-options-content-form',
                        app.searchApp.loadResults, null,
                        catalogue.startRecord, true);
                }
            });

            Ext.getCmp('nextBt').on('click', function() {
                catalogue.startRecord += 50;
                catalogue.search('advanced-search-options-content-form',
                    app.searchApp.loadResults, null,
                    catalogue.startRecord, true);
            });

            var resultPanel = new Ext.Panel({
                id : 'resultsPanel',
                border : false,
                hidden : true,
                bodyCssClass : 'md-view',
                autoWidth : true,
                layout : 'fit',
                tbar : tBar,
                items : metadataResultsView,
                renderTo : 'result-panel',
                // paging bar on the bottom
                bbar : bBar
            });
            return resultPanel;
        },
        loadResults : function(response, query) {
            Ext.state.Manager.getProvider().updateLastSearch(query);
            // Show "List results" panel
            var facetPanel = Ext.getCmp('facets-panel');
            // Init facet panel on first search
            if (!facetPanel) {
                app.searchApp.generateFacetedSearchPanel();
            }
            Ext.getCmp('facets-panel').refresh(response);

            Ext.getCmp('previousBt').setDisabled(catalogue.startRecord === 1);
            Ext.getCmp('nextBt')
                    .setDisabled(
                            catalogue.startRecord + 50 > catalogue.metadataStore.totalLength);

            Ext.getCmp('previousBtFooter').setDisabled(catalogue.startRecord === 1);
            Ext.getCmp('nextBtFooter')
                .setDisabled(
                    catalogue.startRecord + 50 > catalogue.metadataStore.totalLength);

            Ext.getCmp("infoFooter").update(Ext.getCmp("info").el.dom.textContent
                || Ext.getCmp("info").el.dom.innerText);

            // Fix for width sortBy combo in toolbar
            // See this:
            // http://www.sencha.com/forum/showthread.php?122454-TabPanel-deferred-render-false-nested-toolbar-layout-problem
            Ext.getCmp('sortByToolBar').syncSize();
            Ext.getCmp('sortByToolBar').setWidth(130);

            // Update page title based on search results and params
            var formParams = GeoNetwork.util.SearchTools
                    .getFormValues(app.searchApp.advSearchForm);
            var criteria = '', excludedSearchParam = {
                E_hitsperpage : null,
                timeType : null,
                sortOrder : null,
                sortBy : null
            };
            for ( var item in formParams) {
                if (formParams.hasOwnProperty(item)
                        && excludedSearchParam[item] === undefined) {
                    var value = formParams[item];
                    if (value !== '') {
                        fieldName = item.split('_');
                        criteria += OpenLayers.i18n(fieldName[1] || item)
                                + ': ' + value + ' - ';
                    }
                }
            }
            var title = (catalogue.metadataStore.totalLength || 0)
                    + OpenLayers.i18n('recordsFound')
                    + (criteria !== '' ? ' | ' + criteria : '');

            GeoNetwork.Util.updateHeadInfo({
                title : catalogue.getInfo().name + ' | ' + title
            });

            if (!cookie || !cookie.get('user') || !cookie.get('user').username) {
                Ext.each(Ext.DomQuery.select('.md-action-menu'), function(el) {
                    hide(el);
                });
            }

            Ext.state.Manager.getProvider().restoring = false;
        }
    };
};
