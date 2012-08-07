Ext.namespace('cat.view');

cat.view.ViewPanel = Ext.extend(GeoNetwork.view.ViewPanel, {

	/** url for the metadata.formatter.html to custom MD format * */
	formatterServiceUrl : undefined,

	/** template use for custom formatter * */
	viewMode : undefined,

	afterMetadataLoad : function() {
		this.cache = {};
		this.tooltips = [];
		this.catalogue.extentMap.initMapDiv();
		this.registerTooltip();
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

		// this.tbar = [this.createViewMenu(), this.createActionMenu(), '->',
		// this.createPrintMenu(), this.createTooltipMenu()];

		GeoNetwork.view.ViewPanel.superclass.initComponent.call(this);
		this.metadataSchema = this.record ? this.record.get('schema') : '';

		if (this.formatterServiceUrl) {
			var formatterViewPanel = new Ext.Panel({
				autoLoad : {
					url : this.formatterServiceUrl,
					scripts : true,
					scope : this
				},
				cls : 'viewmd-panel',
				border : false,
				frame : false,
				autoScroll : true,
				autoHeight : true
			});
		}

		if (this.serviceUrl) {
			var showPanel = new Ext.Panel({
				autoLoad : {
					url : this.serviceUrl + '&currTab=' + 'simple',
					callback : function() {
						this.fireEvent('aftermetadataload', this);
					},
					scope : this
				},
				id : 'result-metadata-modal-tab-2-content',
				title : OpenLayers.i18n('complete'),
				border : false,
				frame : false,
				cls : 'viewmd-panel',
				autoHeight : true,
				autoScroll : true
			});
			if (this.formatterServiceUrl) {
				formatterViewPanel.setTitle(OpenLayers.i18n('essentielle'));
			}
		}

		if (showPanel && formatterViewPanel) {
			this.add(new Ext.TabPanel({
				items : [ formatterViewPanel, showPanel ],
				activeTab : 0,
				border : false,
				frame : false,
				autoScroll : true,
				title: 'titre',
				cls : 'mdshow-tabpanel',
				headerCfg: {
					children: [{
						id: 'md-print-btn',
						tag: 'div',
						html: '&nbsp;',
						cls: 'file-pdf',
						tip: 'PDF'
					},{
						id: 'md-xml-btn',
						tag: 'div',
						html: '&nbsp;',
						cls: 'file-xml',
						tip: 'ISO19139'
					}]
				},
				listeners: {
					afterrender: {
						fn: function(c) {
							Ext.get('md-print-btn').on('click', function(btn) {
								window.open('print.html?uuid=' + this.metadataUuid + '&currTab=' + 'simple' + "&hl=" + this.lang);
							}, this);
							Ext.get('md-xml-btn').on('click', function(btn) {
								this.catalogue.metadataXMLShow(this.metadataUuid, this.metadataSchema);
							}, this)
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

		// if (this.permalink) {
		// // TODO : Add viewpanel state (ie. size for window, tab)
		// var l = GeoNetwork.Util.getBaseUrl(location.href) + "?uuid=" +
		// this.metadataUuid;
		// this.getTopToolbar().add(GeoNetwork.Util.buildPermalinkMenu(l));
		// }

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
