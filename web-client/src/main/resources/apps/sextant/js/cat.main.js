Ext.namespace('GeoNetwork');
Ext.namespace('cat');

var catalogue;
var app;
var cookie;

cat.app = function() {

	var geonetworkUrl;
	var searching = true;

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

//	// Option 1 for https://forge.ifremer.fr/mantis/view.php?id=14387 - fields are blinking
//	var fieldWithFilterApplied = [];
//	var appFilteredField = ['E_credit', 'E__groupPublished'];
//	/**
//	 * The application may provide custom restriction for 
//	 * search field using hidden input like configwhat.
//	 * 
//	 * For those field with restriction, if no selection is made
//	 * apply the list of filters defined by default to not allow 
//	 * search accross all catalog content
//	 */
//	function applyAppSearchFilter() {
//        Ext.each (appFilteredField, function(fieldId) {
//            var catalogueField = Ext.getCmp(fieldId);
//            if (catalogueField.mode === 'local' && catalogueField.getValue() === '') {
//                catalogueField.getStore().each(function (record) {
//                    catalogueField.addValue(record.get('value'));
//                });
//                fieldWithFilterApplied.push(catalogueField);
//            }
//	    });
//	}
//	function clearAppSearchFilter() {
//        // Restore state of each filters
//        Ext.each (fieldWithFilterApplied, function(field) {
//            field.setValue('');
//        });
//        fieldWithFilterApplied = [];
//	}
	
	function search() {
        searching = true;
        Ext.get('load-spinner').show();
        cookie.set('cat.search.page', catalogue.startRecord);

        if (metadataResultsView && Ext.getCmp('geometryMap')) {
            metadataResultsView.addMap(Ext.getCmp('geometryMap').map, true);
        }
        catalogue.search('searchForm', app.loadResults, null, catalogue.startRecord, true);
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

	    // Refresh login form if needed
        var user = cookie.get('user');
        if (user) {
            catalogue.identifiedUser = user;
        }
        
		var loginForm = new GeoNetwork.LoginForm({
			renderTo : 'login-form',
			catalogue : catalogue,
			layout : 'hbox',
			hidden:catalogue.casEnabled,
			hideLoginLabels : GeoNetwork.hideLoginLabels
		});
		
		catalogue.on('afterLogin', function() {
		    cookie.set('user', catalogue.identifiedUser);
		});
		catalogue.on('afterLogout', function() {
		    cookie.set('user', undefined);
		});
		
		loginForm.triggerClick();
	}
	
	function showMD(uuid, record, url, maximized, width, height) {
		
		var bd = Ext.getBody();
		//var style = urlParameters.style || 'sextant';
		var url = Ext.get(Ext.query('input[id*=configmdviewerurl]')[0]).getValue();
		var style = OpenLayers.Util.getParameters(url).style || 'sextant';
		
		var win = new cat.view.ViewWindow({
			layout:'fit',
            serviceUrl: style == 'sextant' ? this.services.mdView + '?uuid=' + encodeURI(uuid) : null,
            formatterServiceUrl: this.services.mdFormatter + '?uuid=' + encodeURI(uuid) + '&xsl=' + style,
            lang: this.lang,
            currTab: GeoNetwork.defaultViewMode || 'simple',
            printDefaultForTabs: GeoNetwork.printDefaultForTabs || false,
            catalogue: this,
            maximized: false,
            metadataUuid: uuid,
            record: record,
            viewMode: style, 
            resultsView: this.resultsView,
            modal: true,
            draggable: false,
            movable: false,
            resizable: false,
            width: Ext.getBody().getViewSize().width-400,
            height: Ext.getBody().getViewSize().height-250,
            cls: 'view-win',
            bodyStyle:'padding:10px'
            });
        win.show();
	}
	
	function edit(metadataId, create, group, child, isTemplate, schema) {
	    var record = catalogue.metadataStore.getAt(catalogue.metadataStore.find('id', metadataId));
	    
		if (!this.editorWindow) {
			this.editorPanel = new GeoNetwork.editor.EditorPanel({
				defaultEditMode : GeoNetwork.Settings.editor.defaultViewMode,
				editMode : GeoNetwork.Settings.editor.editMode,
				defaultViewMode : GeoNetwork.Settings.editor.defaultViewMode,
				selectionPanelImgPath: cat.libPath + '/ext-ux/images',
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
						cls: 'view-win',
						title : OpenLayers.i18n('mdEditor'),
						id : 'editorWindow',
						layout : 'fit',
						modal : false,
						closeAction: 'destroy',
						items : this.editorPanel,
						closeAction : 'hide',
						collapsible : true,
						collapsed : false,
						maximizable : false,
						maximized : true,
						resizable : true,
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
			var recordSchema = record && record.get('schema');
			this.editorPanel.init(metadataId, create, group, child, undefined, true, recordSchema || schema);
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
	}
	
	function resetResultPanels() {
		var infoPanel = Ext.getCmp('infoPanel'), resultsPanel = Ext
		.getCmp('resultsPanel');

		if (resultsPanel.isVisible()) {
			resultsPanel.hide();
		}
		if (!infoPanel.isVisible()) {
			infoPanel.show();
		}
	}

	function createInfoPanel() {
		var box = new Ext.BoxComponent({
			autoEl : {
				tag : 'img',
				src : cat.imgPath+'images/logo-sextant-big.png',
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
		
		var pdfAction = new Ext.Action({
			id: 'pdfPrintResults',
            iconCls: 'md-mn-pdf',
            tooltip: OpenLayers.i18n('printSel'),
            handler: function(){
                this.catalogue.metadataSelectAll(function(){
                    this.catalogue.pdfExport();
                });
            },
            scope: this
        });

		metadataResultsView = new cat.MetadataResultsView({
			catalogue : catalogue,
			displaySerieMembers : true,
			displayContextualMenu : false,
			autoHeight: true,
			autoScroll : false,
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
			permalinkProvider: permalinkProvider,
			config : {
				selectAction : false,
				sortByAction : true,
				templateView : true,
				otherActions : true
			},
			sortByStore : new Ext.data.ArrayStore({
				id: 0,
				fields: ['id', 'name'],
				data: [['relevance#', OpenLayers.i18n('relevance')], 
	                    ['title#reverse', OpenLayers.i18n('title')], 
	                    ['changeDate#', OpenLayers.i18n('changeDate')]]
	        }),
			items : [ previousAction, '|', nextAction, '|', {
					xtype : 'tbtext',
					text : '',
					id : 'info'
				}, ' ',' ', ' ', ' ', ' ', 
				pdfAction, 
				new Ext.Toolbar.TextItem({
					id: 'gn-sxt-restb-admin-btn',
					cls: 'admin-btn-tbar',
					text: '<div class="md-action-menu"><span class="icon">&nbsp;</span>'+ OpenLayers.i18n('administrer') + '<span class="list-icon">&nbsp;</span></div>'
				})
			],
			createOtherActionMenu : function() {
				this.actionMenu = new Ext.menu.Menu();
				this.createAdminMenu(!this.catalogue.isAdmin());
				
				this.on('afterrender', function() {
					var adminBtn = this.getComponent('gn-sxt-restb-admin-btn');
					adminBtn.on('afterrender', function(e){
						e.el.on('click', function(evt, elt) {
							var menuElt = e.el.child('.md-action-menu');
							this.actionMenu.showAt([menuElt.getX(), menuElt.getY()+menuElt.getHeight()]);
						}, this);
					}, this);
					this.actionOnSelectionMenu = adminBtn;
					tBar.changeMode(false);
					this.updatePrivileges(catalogue, catalogue.identifiedUser);
				}, this);
				
				return ' ';
			},
			changeMode: function(mode) {
				this.items.each(function(it){
					if( it.getId() != 'gn-sxt-restb-admin-btn') {
						it.setVisible(mode);
					}
				}, this);
			},
		    updatePrivileges: function(catalogue, user){

		        var nbVisible=0;
		        var editingActions = []; //new Md & insert MD actions
		        
		        if(this.createMetadataAction) editingActions.push(this.createMetadataAction);
		        if(this.mdImportAction) editingActions.push(this.mdImportAction);
		        
		        // Only administrator (SXT5_ALL_Administrator) will see 'Administration' button
		        var vis = user && user.role == 'Administrator';
		        this.adminAction.setVisible(vis);
		        
		        // Do not display editing action for registered users
		        Ext.each(editingActions, function(){
		            var vis = user && user.role !== 'RegisteredUser';
		            this.setVisible(vis);
		            if(vis)nbVisible++;
		        });
		        this.actionOnSelectionMenu && this.actionOnSelectionMenu.setVisible(nbVisible > 0 && catalogue.identifiedUser != undefined);
		    }
		});
		
		resultPanel = new Ext.Panel({
			id : 'resultsPanel',
			border : false,
			hidden : true,
			autoHeight: true,
			bodyCssClass : 'md-view',
			cls : 'result-panel',
			autoWidth : true,
			layout : 'fit',
			items : metadataResultsView
		});
		
		return resultPanel;
	}

	function createSearchForm() {
		
		var services = catalogue.services;
		cat.what.createCmp(catalogue);
		
		var whereForm = cat.where.createCmp();
		var whatForm = cat.what.getPanel();
		var whenForm = cat.when.createCmp();
		var whoForm = cat.who.createCmp(services, cat.what);

		// Add hidden fields to be use by quick metadata links from the admin
		// panel (eg. my metadata).
		var ownerField = new Ext.form.TextField({
			id: 'txtfield-E__owner',
			name : 'E__owner',
			hidden : true
		});
		var isHarvestedField = new Ext.form.TextField({
			id: 'txtfield-E__isHarvested',
			name : 'E__isHarvested',
			hidden : true
		});
		var catalogueField = new Ext.form.TextField({
			id: 'txtfield-E_siteId',
			name : 'E_siteId',
			hidden : true
		});
		var catalogueField = new Ext.form.TextField({
			id: 'txtfield-E_template',
			name : 'E_template',
			hidden : true
		});

		var formItems = [];
		var optionsPanel = GeoNetwork.util.SearchFormTools.getOptions(catalogue.services,
				undefined);
		optionsPanel.setVisible(false);
		formItems.push(whereForm, whatForm, whoForm, whenForm, optionsPanel, ownerField, isHarvestedField, catalogueField);

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
					+ '&nbsp;'
					+ '</span>' + '<a id="searchFormHeaderLink" href="#">'
					+ OpenLayers.i18n('search-header-advanced') + '</a>',
			searchBt : new Ext.Button({
				template: new Ext.Template(
						'<div class="search-btn">',
						'<div class="btnLeft">&nbsp;</div>',
						'<div class="btnText"></div>',
						'<div class="btnRight">&#160;</div>',
						'</div>'),
						buttonSelector: '.btnText',
				text : OpenLayers.i18n('search')
			}),
			resetCb : function() {
				this.getForm().reset();
				var elt = this.findByType('gn_categorytree', true);
				elt[0].reset();
				this.find('id', 'E__groupPublished')[0].getStore().load();
				catalogue.metadataStore.removeAll();
				resetResultPanels();
				cookie.set('cat.search.page', 0);
				GeoExt.MapPanel.guess().map.zoomToMaxExtent();
				GeoExt.MapPanel.guess().vectorLayer.destroyFeatures();
				tBar.changeMode(false);
			},
			resetBt : new Ext.Button({
				text : OpenLayers.i18n('reset'),
				template: new Ext.Template(
						'<div class="search-btn">',
						'<div class="btnLeft">&nbsp;</div>',
						'<div class="btnText"></div>',
						'<div class="btnRight">&#160;</div>',
						'</div>'),
						buttonSelector: '.btnText'
				}),
			searchCb : function() {

				
				
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
			autoScroll: true,
			defaults : {
				cls : 'search_panel',
				margins : '10 0 0 0',
				frame : false,
				cls : 'search-form-panel',
				collapsedCls : 'search-form-panel-collapsed',
				bodyStyle : 'background-color:white;padding:15px',
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
				cookie.set('cat.searchform.advanced', !this.advanced)
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
					item.setVisible(!item.disabled);
					if(!Ext.isIE) // temp
					    whatForm.body && whatForm.body.removeClass('hidden');
				});
				cpt.header.child('#searchFormHeaderLink').dom.innerHTML = OpenLayers.i18n('search-header-simple');
			});
			cpt.on('simplemode',function() {
				Ext.each(advandcedField,function(item) {
					item.setVisible(false);
					if(!Ext.isIE) //temp
					    whatForm.body && whatForm.body.addClass('hidden');
				});
				cpt.header.child('#searchFormHeaderLink').dom.innerHTML = OpenLayers.i18n('search-header-advanced');
			});
			if(cookie.get('cat.searchform.advanced')) {
				this.fireEvent('advancedmode', this);
				this.advanced = true;
			}
		});
		
		return searchForm;
	}
	
	function modalActionFn(title, url, cb){
        if (url) {
            var app = this, win, defaultCb = function(el, success, response, options) {
                if (!success){
                    app.showError('Catalogue error', title);
                    win.close();
                }
            };
            win = new Ext.Window({
            	id: 'gn-modalWindow',
                layout: 'fit',
                closeAction: 'destroy',
                maximized: false,
                border: false,
                modal: true,
                draggable: false,
                movable: false,
                resizable: false,
                width: Ext.getBody().getViewSize().width-400,
                height: Ext.getBody().getViewSize().height-250,
                cls: 'view-win',
                bodyStyle:'padding:10px',
                title: title,
                items: new Ext.Panel({
                	border:false,
                    autoLoad: {
                        url: url,
                        callback: cb || defaultCb,
                        scope: win
                    },
                    frame: false,
                    autoScroll: true
                })
            });
            win.show(this);
        }
    }
	
	/**
	 * Fix the main panel height to the browser size (or portlet-content if embeded in liferay)
	 * Called on startup and window.resize
	 * 
	 **/
	function fitHeightToBody(o) {
		var portletContainer = Ext.Element.select('#column-1 .portlet-body');
		var catalogDiv = Ext.Element.select('#catalogTab');
		var height=0;
		if(portletContainer.getCount()>0 && catalogDiv.getCount()==1) {
			var d = Ext.get('main-viewport');
			height=Ext.getBody().getViewSize().height -d.getY() - 20;
		}
		else {
			height=Ext.getBody().getViewSize().height-50;
		}
		o.setHeight(height);
		searchForm.setHeight(height);
		
		Ext.getBody().setHeight(Ext.getBody().getViewSize().height);
		o.doLayout();
	}
	
	// Get location of an url
	var getLocation = function(href) {
	    var l = document.createElement("a");
	    l.href = href;
	    return l;
	};

	return {
	    switchMode: function() {
	        // Not supported by this widget app
	    },
	    getIMap: function () {return this;},
	    addWMC: function(url) {
	           // Not supported by this widget app
        },
        addWMSLayer: function (args) {
            var layer = args[0];
            // Not supported by this widget app
            // TODO : should open the viewer ?
        },
		init : function() {
			
		    var cookiePath = '/';
		    
			var gnUrlElt = Ext.query('input[id*=configgeonetwork]');
			if(gnUrlElt && gnUrlElt.length==1) {
				GeoNetwork.URL=Ext.get(gnUrlElt[0]).getValue();
				
				var loc = getLocation(window.location.href);
				cookiePath = loc.pathname;
			}
			geonetworkUrl = GeoNetwork.URL || window.location.href.match(/((http).*\/.*)\/apps\/sextant.*/, '')[1];
			
			urlParameters = GeoNetwork.Util.getParameters(location.href);
			
			if (urlParameters.extent) {
				urlParameters.bounds = new OpenLayers.Bounds(
						urlParameters.extent[0], urlParameters.extent[1],
						urlParameters.extent[2], urlParameters.extent[3]);
			}

			// Init cookie
			cookie = new Ext.state.CookieProvider({
				expires : new Date(new Date().getTime()
						+ (1000 * 60 * 60 * 24 * 365)),
				path: cookiePath
			});
			Ext.state.Manager.setProvider(cookie);
			
			// Create connexion to the catalogue
			catalogue = new GeoNetwork.Catalogue({
				statusBarId : 'info',
				lang : cat.language,
				hostUrl : geonetworkUrl,
				mdOverlayedCmpId : 'resultsPanel',
				adminAppUrl : geonetworkUrl + '/srv/' + cat.language + '/admin',
				metadataStore : GeoNetwork.Settings.mdStore ? GeoNetwork.Settings.mdStore()	: GeoNetwork.data.MetadataResultsStore(), metadataCSWStore : GeoNetwork.data.MetadataCSWResultsStore(),
				summaryStore : GeoNetwork.data.MetadataSummaryStore(),
				editMode : 2,
				metadataEditFn: edit,
				metadataShowFn: showMD,
				modalAction:modalActionFn
			});
			
			catalogue.getInfo();
			
			// Extra stuffs
			infoPanel = createInfoPanel();
			searchForm = createSearchForm();
			//createLanguageSwitcher(cat.language);

			edit();
			resultsPanel = createResultsPanel();
			
			createLoginForm();
			
			var viewport = new Ext.Panel({
				renderTo: 'main-viewport',
				layout : 'border',
				border: false,
				id : 'vp',
				cls : 'cat_layout',
				items : [ {
					region : 'west',
					id : 'west',
					bodyCssClass: 'west-panel-body',
					cls: 'sxt-layout-border-white',
					split : true,
					border : false,
					frame : false,
					minWidth : 300,
					width : 442,
					maxWidth : 500,
					margins: '0 7 0 0',
					collapsible : true,
					hideCollapseTool : true,
					collapseMode : 'mini',
					layoutConfig : {
						animate : true
					},
					items : searchForm
				}, {
					region : 'center',
					id : 'center',
					split : true,
					autoScroll: true,
					cls: 'sxt-layout-border-white',
					margins: '0 0 0 4',
					tbar : tBar,
					items : [ infoPanel, resultsPanel,new Ext.BoxComponent({
						autoEl : {
							tag : 'img',
							src : cat.imgPath+'images/spinner-large.gif',
							cls : 'spinner',
							id : 'load-spinner'
						}
					})]
				} ],
				listeners: {
					afterrender: {
						fn: function(o){
						    function setHiddenField(name) {
								if(urlParameters['s_'+name]) {
									searchForm.getComponent('txtfield-'+name).setValue(urlParameters['s_'+name]);
								}
							}
							var searchPage = cookie.get('cat.search.page');
							if (urlParameters.s_search !== undefined) {
								searchForm.reset();
								setHiddenField('E__owner');
								setHiddenField('E__isHarvested');
								setHiddenField('E_siteId');
								setHiddenField('E_template');
								search();
							} else if(searchPage && searchPage > 0) {
								catalogue.startRecord = searchPage;
							}
							
							fitHeightToBody(o);
							
							var loadDiv = Ext.get('loading');
							if(loadDiv) {
								loadDiv.remove();
								Ext.get('loading-mask').fadeOut({
									remove : true
								});
							}
						}
					}
				}
			});
			
			window.onresize = function() {
				fitHeightToBody(viewport);
			}
			
			if (urlParameters.edit !== undefined && urlParameters.edit !== '') {
                catalogue.metadataEdit(urlParameters.edit);
            }
            if (urlParameters.create !== undefined) {
                Ext.getCmp('center').toolbars[0].createMetadataAction.fireEvent('click');
            }
            if (urlParameters.uuid !== undefined) {
                catalogue.metadataShow(urlParameters.uuid, true);
            } else if (urlParameters.id !== undefined) {
                catalogue.metadataShowById(urlParameters.id, true);
            }

			if (GeoNetwork.searchDefault.activeMapControlExtent) {
				Ext.getCmp('geometryMap').setExtent();
			}
			if (urlParameters.bounds) {
				Ext.getCmp('geometryMap').map
						.zoomToExtent(urlParameters.bounds);
			}

			resultPanel.setHeight(Ext.getCmp('center').getHeight());

			var events = [ 'afterDelete', 'afterRating', 'afterLogout', 'afterBadLogin',
					'afterLogin' ];

            var searchAfterLoggin = function() {
                if (searching === true) {
                    var searchPage = cookie.get('cat.search.page');
                    if (searchPage && searchPage > 0) {
                        catalogue.startRecord = searchPage;
                    }
                    search();
                }
            }

            Ext.each(events, function(e) {
                catalogue.on(e, function() {
                    cat.what.updateUserGroups(searchAfterLoggin);
                });
            });
            
		},

		getCatalogue : function() {
			return catalogue;
		},

		loadResults : function(response) {
//		    clearAppSearchFilter();
		    
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
			
			
			Ext.getCmp('pdfPrintResults').setDisabled(catalogue.metadataStore.totalLength > GeoNetwork.Settings.results.maxResultsInPDF);
			
			resultsPanel.syncSize();
			resultsPanel.setHeight(Ext.getCmp('center').getHeight());

			Ext.getCmp('west').syncSize();
			Ext.getCmp('center').syncSize();
			Ext.ux.Lightbox.register('a[rel^=lightbox]');
			
			tBar.changeMode(true);
			Ext.get('load-spinner').hide();
		}
	}
}

Ext.onReady(function() {
	
	var urlParameters = GeoNetwork.Util.getParameters(location.href);
	cat.language = cat.language || urlParameters.hl || GeoNetwork.Util.defaultLocale;
	
	if(cat.language == 'fr') cat.language = 'fre';
	else if(cat.language == 'en') cat.language = 'eng';
	
	cat.libPath = cat.imgPath?cat.imgPath+'js/lib':'../js';
	GeoNetwork.Util.setLang(cat.language, cat.libPath);

	Ext.QuickTips.init();
	cat.imgPath=cat.imgPath?cat.imgPath:'';
	
	app = new cat.app();
	app.init();
	catalogue = app.getCatalogue();

	Ext.Ajax.request({
		url: catalogue.services.getInspireInfo+'&type=harvester',
		success: function(res) {
			var enable = res.responseXML.getElementsByTagName("harvester")[0].getElementsByTagName("enable")[0];
			if (enable.textContent) {
				GeoNetwork.Settings.editor.editHarvested = enable.textContent;
			} else {
				GeoNetwork.Settings.editor.editHarvested = enable.innerText;
			}
		} 
	});
	
	/* Focus on full text search field */
	Ext.getDom('E_any').focus(true);
	Ext.get('E_any').setHeight(28);
	
	initShortcut();
});
