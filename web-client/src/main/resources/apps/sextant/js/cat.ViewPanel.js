Ext.namespace('cat.view');

cat.view.ViewPanel = Ext.extend(GeoNetwork.view.ViewPanel, {

	/** url for the metadata.formatter.html to custom MD format * */
	formatterServiceUrl : undefined,

	/** template use for custom formatter * */
	viewMode : undefined,
	
	/** mask the full panel before it shows **/
	mask: undefined,
	
	afterMetadataLoad : function() {
		this.cache = {};
		this.tooltips = [];
		this.catalogue.extentMap.initMapDiv();
		this.registerTooltip();
        this.getLinkedData();
	},
	
	getPermaLink : function() {
		return formatterUrl = this.catalogue.services.mdFormatter + '?id=' + encodeURI(this.metadataUuid) + '&xsl=mdviewer';
	},
	
	/**
	 * Show a textfield with the external link of the MD
	 */
	showPermaLinkField: function(value) {
		
		if(this.permalinkMenu) this.permalinkMenu.destroy();
		
		var menuElt = Ext.get('md-link-btn');
		this.permalinkMenu = new Ext.menu.Menu({
			cls: 'no-icon-menu permalink-menu',
			floating: true,
			showSeparator: false
		});
		
		var permalinkText = new Ext.menu.TextItem({
			text : '<input value="'+value+'"/><br/><a href="'+value+'" target="#">'+OpenLayers.i18n('link')+'</a>'
		});
		
		this.permalinkMenu.add(OpenLayers.i18n('permalinkInfo'), permalinkText);
		this.permalinkMenu.showAt([menuElt.getX(), menuElt.getY() + menuElt.getHeight()]);
	},

	/**
	 * private: method[initComponent] Initializes the metadata view window.
	 */
	initComponent : function() {
		Ext.applyIf(this, this.defaultConfig);

		this.tipTpl = new Ext.XTemplate(
				GeoNetwork.util.HelpTools.Templates.SIMPLE);
		this.relatedTpl = new Ext.XTemplate(this.relatedTpl
				|| GeoNetwork.Templates.Relation.SHORT);
		
		this.hidden=true;
		GeoNetwork.view.ViewPanel.superclass.initComponent.call(this);
		this.metadataSchema = this.record ? this.record.get('schema') : '';
		
		this.on('afterrender', function(p) {
			var el = this.ownerCt ? this.ownerCt.body : Ext.getBody();
			this.mask = new Ext.LoadMask(el , {
				msg:' ',
				removeMask: true,
				msgCls: 'mdviewer-mask-msg'
			});
			this.mask.show();
		}, this);
		

		if (this.formatterServiceUrl) {
			var formatterViewPanel = new Ext.Panel({
				autoLoad : {
					url : this.formatterServiceUrl,
					scripts : true,
					callback : function() {
						this.show();
						this.doLayout();
						
						var task = new Ext.util.DelayedTask(function(){
							this.mask.hide();
						}, this);
						
						task.delay(100);
					},
					scope : this,
					text:' '
				},
				id : 'result-metadata-modal-tab-1-content',
				cls : 'viewmd-panel',
				border : false,
				frame : false,
				autoScroll : false,
				autoHeight:true,
				autoWidth: true
			});
		}

		if (this.serviceUrl) {
			var showPanel = new Ext.Panel({
				autoLoad : {
					url : this.serviceUrl + '&currTab=' + 'simple',
					callback : function() {
						this.fireEvent('aftermetadataload', this);
					},
					scope : this,
					text: ' '
				},
				id : 'result-metadata-modal-tab-2-content',
				title : OpenLayers.i18n('complete'),
				border : false,
				frame : false,
				cls : 'viewmd-panel',
				autoScroll : false
			});
			if (this.formatterServiceUrl) {
				formatterViewPanel.setTitle(OpenLayers.i18n('essentielle'));
			}
		}
		
		Ext.QuickTips.init();
		if (showPanel && formatterViewPanel) {
			this.add(new Ext.TabPanel({
				items : [ formatterViewPanel, showPanel ],
				activeTab : 0,
				border : false,
				frame : false,
				autoScroll : true,
				deferredRender: true,
				id: 'gn-sxt-viewertabpanel',
				title: 'titre',
				cls : 'mdshow-tabpanel',
				headerCfg: {
					children: [{
						id: 'md-link-btn',
						tag: 'div',
						html: '&nbsp;',
						cls: 'file-link',
						'ext:qtip': OpenLayers.i18n('exportLink')
					},{
						id: 'md-print-btn',
						tag: 'div',
						html: '&nbsp;',
						cls: 'file-pdf',
						'ext:qtip': OpenLayers.i18n('exportPDF')
					},{
						id: 'md-xml-btn',
						tag: 'div',
						html: '&nbsp;',
						cls: 'file-xml',
						'ext:qtip': OpenLayers.i18n('exportXSML')
					}]
				},
				listeners: {
					afterrender: {
						fn: function(c) {
							Ext.get('md-print-btn').on('click', function(btn) {
								//window.open('print.html?uuid=' + this.metadataUuid + '&currTab=' + 'simple' + "&hl=" + this.lang);
								this.catalogue.metadataPrint(this.metadataUuid);
							}, this);
							Ext.get('md-xml-btn').on('click', function(btn) {
								this.catalogue.metadataXMLShow(this.metadataUuid, this.metadataSchema);
							}, this);
							Ext.get('md-link-btn').on('click', function(btn) {
								this.showPermaLinkField(this.getPermaLink());
							}, this);
						},
						scope: this
					}
				}
			}));
		} else {
			if (showPanel) {
				this.add(showPanel);
			} else {
				this.add(formatterViewPanel);
			}
		}

		this.addEvents(
		/**
		 * private: event[search] Fires search.
		 */
		"aftermetadataload");
		this.on({
			"aftermetadataload" : this.afterMetadataLoad,
			scope : this
		});
	}
});

/** api: xtype = gn_view_viewpanel */
Ext.reg('cat', GeoNetwork.view.ViewPanel);
