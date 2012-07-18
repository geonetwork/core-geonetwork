Ext.namespace('GeoNetwork');
Ext.namespace('cat');

var catalogue;
var app;
var cookie;

cat.app = function() {

	var geonetworkUrl;
	var searching = false;

	/**
	 * Application parameters are :
	 *  * any search form ids (eg. any) * mode=1 for visualization * advanced:
	 * to open advanced search form by default * search: to trigger the search *
	 * uuid: to display a metadata record based on its uuid * extent: to set
	 * custom map extent
	 */
	var urlParameters = {};

	/**
	 * Catalogue manager
	 */
	var catalogue;

	/**
	 * An interactive map panel for data visualization
	 */
	var iMap, searchForm, resultsPanel, metadataResultsView, tBar, bBar, mainTagCloudViewPanel, tagCloudViewPanel, infoPanel, visualizationModeInitialized = false;

	function search() {
		searching = true;
		catalogue.search('searchForm', app.loadResults, null,
				catalogue.startRecord, true);
	}

	function createLanguageSwitcher(lang){
        return new Ext.form.FormPanel({
            renderTo: 'lang-form',
            width: 95,
            border: false,
            layout: 'hbox',
            hidden:  GeoNetwork.Util.locales.length === 1 ? true : false,
            items: [new Ext.form.ComboBox({
                mode: 'local',
                triggerAction: 'all',
                width: 95,
                store: new Ext.data.ArrayStore({
                    idIndex: 2,
                    fields: ['id', 'name', 'id2'],
                    data: GeoNetwork.Util.locales
                }),
                valueField: 'id2',
                displayField: 'name',
                value: lang,
                listeners: {
                    select: function(cb, record, idx){
                        window.location.replace('?hl=' + cb.getValue());
                    }
                }
            })]
        });
    }
	
	function createLoginForm() {
		var loginForm = new GeoNetwork.LoginForm({
			renderTo : 'login-form',
			catalogue : catalogue,
			layout : 'hbox',
			hideLoginLabels : GeoNetwork.hideLoginLabels
		});

		// Store user info in cookie to be displayed if user reload the page
		// Register events to set cookie values
		catalogue.on('afterLogin', function() {
			cookie.set('user', catalogue.identifiedUser);
		});
		catalogue.on('afterLogout', function() {
			cookie.set('user', undefined);
		});

		// Refresh login form if needed
		var user = cookie.get('user');
		if (user) {
			catalogue.identifiedUser = user;
			loginForm.login(catalogue, true);
		}
	}
	
	function edit(metadataId, create, group, child) {

		if (!this.editorWindow) {
			this.editorPanel = new GeoNetwork.editor.EditorPanel({
				defaultViewMode : GeoNetwork.Settings.editor.defaultViewMode,
				catalogue : catalogue,
				xlinkOptions : {
					CONTACT : true
				}
			});

			this.editorWindow = new Ext.Window(
					{
						tools : [ {
							id : 'newwindow',
							qtip : OpenLayers.i18n('newWindow'),
							handler : function(e, toolEl, panel, tc) {
								window
										.open(GeoNetwork.Util
												.getBaseUrl(location.href)
												+ "#edit="
												+ panel
														.getComponent('editorPanel').metadataId);
								panel.hide();
							},
							scope : this
						} ],
						title : OpenLayers.i18n('mdEditor'),
						id : 'editorWindow',
						layout : 'fit',
						modal : false,
						items : this.editorPanel,
						closeAction : 'hide',
						collapsible : true,
						collapsed : false,
						// Unsuported. Needs some kind of component to store
						// minimized windows
						maximizable : false,
						maximized : true,
						resizable : true,
						// constrain: true,
						width : 980,
						height : 800
					});
			this.editorPanel.setContainer(this.editorWindow);
			this.editorPanel.on('editorClosed', function() {
				Ext.getCmp('searchForm').fireEvent('search');
			});
		}

		if (metadataId) {
			this.editorWindow.show();
			this.editorPanel.init(metadataId, create, group, child);
		}
	}

	function initPanels() {
		var infoPanel = Ext.getCmp('infoPanel'), resultsPanel = Ext
				.getCmp('resultsPanel');

		if (infoPanel.isVisible()) {
			infoPanel.hide();
		}
		if (!resultsPanel.isVisible()) {
			resultsPanel.show();
		}

		// Init map on first search to prevent error
		// when user add WMS layer without initializing
		// Visualization mode
		// if (GeoNetwork.MapModule && !visualizationModeInitialized) {
		// initMap();
		// }
	}

	function createInfoPanel() {
		var box = new Ext.BoxComponent({
			autoEl : {
				tag : 'img',
				src : 'images/logo-sextant-big.png',
				cls : 'bg_logo'
			}
		});
		return new Ext.Panel({
			id : 'infoPanel',
			cls : 'bg_logo_container',
			autoWidth : true,
			border : false,
			items : box
		});
	}

	function createToolBar() {

		var previousAction = new Ext.Action({
			id : 'previousBt',
			text : '&lt;&lt;',
			handler : function() {
				var from = catalogue.startRecord
						- parseInt(Ext.getCmp('E_hitsperpage').getValue(), 10);
				if (from > 0) {
					catalogue.startRecord = from;
					search();
				}
			},
			scope : this
		});

		var nextAction = new Ext.Action({
			id : 'nextBt',
			text : '&gt;&gt;',
			handler : function() {
				catalogue.startRecord += parseInt(Ext.getCmp('E_hitsperpage')
						.getValue(), 10);
				search();
			},
			scope : this
		});

		return new Ext.Toolbar({
			items : [ previousAction, '|', nextAction, '|', {
				xtype : 'tbtext',
				text : '',
				id : 'info'
			} ]
		});

	}

	function createResultsPanel(permalinkProvider) {

		var previousAction = new Ext.Action({
			id : 'previousBt',
			text : '&lt;&lt;',
			handler : function() {
				var from = catalogue.startRecord
						- parseInt(Ext.getCmp('E_hitsperpage').getValue(), 10);
				if (from > 0) {
					catalogue.startRecord = from;
					search();
				}
			},
			scope : this
		});

		var nextAction = new Ext.Action({
			id : 'nextBt',
			text : '&gt;&gt;',
			handler : function() {
				catalogue.startRecord += parseInt(Ext.getCmp('E_hitsperpage')
						.getValue(), 10);
				search();
			},
			scope : this
		});

		metadataResultsView = new cat.MetadataResultsView({
			catalogue : catalogue,
			displaySerieMembers : true,
			autoScroll : true,
			templates : {
	            SIMPLE: GeoNetwork.Templates.SIMPLE,
	            THUMBNAIL: GeoNetwork.Templates.THUMBNAIL,
	            FULL: cat.list.getTemplate()
	        },
	        tpl: cat.list.getTemplate(),
			featurecolor : GeoNetwork.Settings.results.featurecolor,
			colormap : GeoNetwork.Settings.results.colormap,
			featurecolorCSS : GeoNetwork.Settings.results.featurecolorCSS
		});

		catalogue.resultsView = metadataResultsView;

		tBar = new GeoNetwork.MetadataResultsToolbar({
			catalogue : catalogue,
			searchFormCmp : Ext.getCmp('searchForm'),
			sortByCmp : Ext.getCmp('E_sortBy'),
			metadataResultsView : metadataResultsView,
			config : {
				selectAction : false,
				sortByAction : true,
				templateView : true,
				otherActions : false
			},
			items : [ previousAction, '|', nextAction, '|', {
				xtype : 'tbtext',
				text : '',
				id : 'info'
			} ]
		});

		// bBar = createToolBar();

		resultPanel = new Ext.Panel({
			id : 'resultsPanel',
			border : false,
			hidden : true,
			bodyCssClass : 'md-view',
			cls : 'result-panel',
			autoWidth : true,
			layout : 'fit',
			tbar : tBar,
			items : metadataResultsView
		});
		return resultPanel;
	}

	function createSearchForm() {

		var services = catalogue.services;
		var whereForm = cat.where.createCmp();
		var whatForm = cat.what.createCmp(services);
		var whenForm = cat.when.createCmp();
		var whoForm = cat.who.createCmp(services);

		// Add hidden fields to be use by quick metadata links from the admin
		// panel (eg. my metadata).
		var ownerField = new Ext.form.TextField({
			name : 'E__owner',
			hidden : true
		});
		var isHarvestedField = new Ext.form.TextField({
			name : 'E__isHarvested',
			hidden : true
		});

		var formItems = [];
		formItems.push(whereForm, whatForm, whenForm, whoForm,
				GeoNetwork.util.SearchFormTools.getOptions(catalogue.services,
						undefined));

		// Add advanced mode criteria to simple form - end
		var advandcedField = [];
		advandcedField.push(whenForm, whoForm);
		advandcedField = advandcedField.concat(cat.what.getAdvancedFields());
		Ext.each(advandcedField, function(item) {
			item.setVisible(false);
		});

		var searchForm = new GeoNetwork.SearchFormPanel({
			id : 'searchForm',
			stateId : 's',
			border : false,
			bodyCssClass : 'search-panel-body',
			headerCssClass : 'search-panel-header',
			cls : 'search-panel',
			buttonAlign : 'left',
			title : '<span id="searchFormHeaderTitle" class="mainheader">'
					+ OpenLayers.i18n('search-header-criteria-simple')
					+ '</span>' + '<a id="searchFormHeaderLink" href="#">'
					+ OpenLayers.i18n('search-header-advanced') + '</a>',
			searchBt : new Ext.Button({
				text : OpenLayers.i18n('search'),
				cls : 'search-btn'
			}),
			resetCb : function() {
				this.getForm().reset();
				var elt = this.findByType('gn_categorytree', true);
				elt[0].reset();
			},
			resetBt : new Ext.Button({
				text : OpenLayers.i18n('reset'),
				cls : 'search-btn'
			}),
			searchCb : function() {
				if (metadataResultsView && Ext.getCmp('geometryMap')) {
					metadataResultsView.addMap(Ext.getCmp('geometryMap').map, true);
				}
				var any = Ext.get('E_any');
				if (any) {
					if (any.getValue() === OpenLayers.i18n('fullTextSearch')) {
						any.setValue('');
					}
				}
				catalogue.startRecord = 1; // Reset start record
				search();
			},
			padding : 5,
			defaults : {
				padding : 15,
				width : 180,
				cls : 'search_panel',
				margins : '10 0 0 0',
				frame : false,
				cls : 'search-form-panel',
				collapsedCls : 'search-form-panel-collapsed',
				bodyStyle : 'background-color:white',
				border : false
			},
			items : formItems
		});

		// Manage header click event to toggle advanced or simple search
		// criteria mode
		searchForm.on('afterrender',function(cpt) {
			cpt.advanced = false;
			cpt.addEvents('advancedmode', 'simplemode');
			cpt.header.on('click', function() {
				if (this.advanced) {
					this.fireEvent('simplemode', this);
					this.advanced = false;
				} else {
					this.fireEvent('advancedmode', this);
					this.advanced = true;
				}
			}, cpt);

			cpt.on('advancedmode',function(cpt) {
				Ext.each(advandcedField,function(item) {
					item.setVisible(true);
					whatForm.body.removeClass('hidden');
				});
				cpt.header.child('#searchFormHeaderTitle').dom.innerHTML = OpenLayers.i18n('search-header-criteria-advanced');
				cpt.header.child('#searchFormHeaderLink').dom.innerHTML = OpenLayers.i18n('search-header-simple');
			});
			cpt.on('simplemode',function() {
				Ext.each(advandcedField,function(item) {
					item.setVisible(false);
					whatForm.body.addClass('hidden');
				});
				cpt.header.child('#searchFormHeaderTitle').dom.innerHTML = OpenLayers.i18n('search-header-criteria-simple');
				cpt.header.child('#searchFormHeaderLink').dom.innerHTML = OpenLayers.i18n('search-header-advanced');
			});
		});
		return searchForm;
	}

	return {
		init : function() {

			geonetworkUrl = GeoNetwork.URL
					|| window.location.href.match(/(http.*\/.*)\/apps\/sextant.*/,
							'')[1];

			urlParameters = GeoNetwork.Util.getParameters(location.href);
			var lang = urlParameters.hl || GeoNetwork.Util.defaultLocale;
			if (urlParameters.extent) {
				urlParameters.bounds = new OpenLayers.Bounds(
						urlParameters.extent[0], urlParameters.extent[1],
						urlParameters.extent[2], urlParameters.extent[3]);
			}

			// Init cookie
			cookie = new Ext.state.CookieProvider({
				expires : new Date(new Date().getTime()
						+ (1000 * 60 * 60 * 24 * 365))
			});
			
			// Create connexion to the catalogue
			catalogue = new GeoNetwork.Catalogue({
				statusBarId : 'info',
				lang : lang,
				hostUrl : geonetworkUrl,
				mdOverlayedCmpId : 'resultsPanel',
				adminAppUrl : geonetworkUrl + '/srv/' + lang + '/admin',
				metadataStore : GeoNetwork.Settings.mdStore ? GeoNetwork.Settings.mdStore()	: GeoNetwork.data.MetadataResultsStore(), metadataCSWStore : GeoNetwork.data.MetadataCSWResultsStore(),
				summaryStore : GeoNetwork.data.MetadataSummaryStore(),
				editMode : 2,
				metadataEditFn: edit
			});

			// Extra stuffs
			infoPanel = createInfoPanel();
			// helpPanel = createHelpPanel();
			// tagCloudViewPanel = createTagCloud();

			// set a permalink provider
			permalinkProvider = new GeoExt.state.PermalinkProvider({
				encodeType : false
			});
			Ext.state.Manager.setProvider(permalinkProvider);
			//            
			// createHeader();

			// Search form
			searchForm = createSearchForm();
			createLoginForm();
			createLanguageSwitcher(lang);

			edit();
			// Search result
			resultsPanel = createResultsPanel(permalinkProvider);

			var viewport = new Ext.Viewport({
				layout : 'border',
				id : 'vp',
				cls : 'cat_layout',
				items : [ {
					region : 'west',
					id : 'west',
					split : true,
					border : true,
					frame : false,
					minWidth : 300,
					width : 400,
					maxWidth : 500,
					autoScroll : true,
					collapsible : true,
					hideCollapseTool : true,
					collapseMode : 'mini',
					margins : '40 0 20 30',
					layoutConfig : {
						animate : true
					},
					items : searchForm
				}, {
					region : 'center',
					id : 'center',
					split : true,
					margins : '40 30 20 0',
					items : [ infoPanel, resultsPanel ]
				} ]
			});

			if (GeoNetwork.searchDefault.activeMapControlExtent) {
				Ext.getCmp('geometryMap').setExtent();
			}
			if (urlParameters.bounds) {
				Ext.getCmp('geometryMap').map
						.zoomToExtent(urlParameters.bounds);
			}

			resultPanel.setHeight(Ext.getCmp('center').getHeight());

			var events = [ 'afterDelete', 'afterRating', 'afterLogout',
					'afterLogin' ];
			Ext.each(events, function(e) {
				catalogue.on(e, function() {
					if (searching === true) {
						searchForm.fireEvent('search');
					}
				});
			});

			// Hack to run search after all app is rendered within a sec ...
			// It could have been better to trigger event in
			// SearchFormPanel#applyState
			// FIXME
			if (urlParameters.s_search !== undefined) {
				setTimeout(function() {
					searchForm.fireEvent('search');
				}, 500);
			}
		},

		getCatalogue : function() {
			return catalogue;
		},

		loadResults : function(response) {

			initPanels();

			// FIXME : result panel need to update layout in case of slider
			// Ext.getCmp('resultsPanel').syncSize();
			Ext.getCmp('previousBt').setDisabled(catalogue.startRecord === 1);
			Ext.getCmp('nextBt').setDisabled(
					catalogue.startRecord
							+ parseInt(Ext.getCmp('E_hitsperpage').getValue(),
									10) > catalogue.metadataStore.totalLength);
			if (Ext.getCmp('E_sortBy').getValue()) {
				Ext.getCmp('sortByToolBar').setValue(
						Ext.getCmp('E_sortBy').getValue() + "#"
								+ Ext.getCmp('sortOrder').getValue());

			} else {
				Ext.getCmp('sortByToolBar').setValue(
						Ext.getCmp('E_sortBy').getValue());
			}

			resultsPanel.syncSize();
			resultsPanel.setHeight(Ext.getCmp('center').getHeight());

			Ext.getCmp('west').syncSize();
			Ext.getCmp('center').syncSize();
			Ext.ux.Lightbox.register('a[rel^=lightbox]');

		},
	}
}

Ext.onReady(function() {
	var lang = /hl=([a-z]{3})/.exec(location.href);
	GeoNetwork.Util.setLang(lang && lang[1], '..');

	Ext.QuickTips.init();
	setTimeout(function() {
		Ext.get('loading').remove();
		Ext.get('loading-mask').fadeOut({
			remove : true
		});
	}, 250);

	app = new cat.app();
	app.init();
	catalogue = app.getCatalogue();

	/* Focus on full text search field */
	Ext.getDom('E_any').focus(true);

});
