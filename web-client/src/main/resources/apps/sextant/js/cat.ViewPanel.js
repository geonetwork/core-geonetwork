Ext.namespace('cat.view');

cat.view.ViewPanel = Ext.extend(GeoNetwork.view.ViewPanel, {
    
	/** url for the metadata.formatter.html to custom MD format **/
	formatterServiceUrl: undefined,
	
	/** template use for custom formatter **/
	viewMode: undefined,
	
    /** private: method[initComponent] 
     *  Initializes the metadata view window.
     */
    initComponent: function(){
        Ext.applyIf(this, this.defaultConfig);
        
        this.tipTpl = new Ext.XTemplate(GeoNetwork.util.HelpTools.Templates.SIMPLE);
        this.relatedTpl = new Ext.XTemplate(this.relatedTpl || GeoNetwork.Templates.Relation.SHORT);
        
        //this.tbar = [this.createViewMenu(), this.createActionMenu(), '->', this.createPrintMenu(), this.createTooltipMenu()];
        
        GeoNetwork.view.ViewPanel.superclass.initComponent.call(this);
        this.metadataSchema = this.record ? this.record.get('schema') : '';
        
        
        if(this.formatterServiceUrl) {
        	var formatterViewPanel = new Ext.Panel({
                autoLoad: {
                    url: this.formatterServiceUrl,
                    scripts: true,
                    scope: this
                },
                title: 'Complete',
                cls: 'viewmd-panel', 
                border: false,
                frame: false,
                autoScroll: true
            });
        }
        
        if(this.serviceUrl) {
        	var showPanel = new Ext.Panel({
                autoLoad: {
                    url: this.serviceUrl + '&currTab=' + 'simple',
                    scripts: true,
                    callback: function() {
                        this.fireEvent('aftermetadataload', this);
                    },
                    scope: this
                },
                id: 'result-metadata-modal-tab-2-content',
                title: 'Essentielle',
                border: false,
                frame: false,
                autoScroll: true
            });
        }
        
        if(showPanel && formatterViewPanel) {
	        this.add(new Ext.TabPanel({
	            items: [formatterViewPanel,showPanel],
	            activeTab: 0,
	            border: false,
	            frame: false,
	            autoScroll: true
	        }));
        }
        else {
        	if(showPanel){
        		this.add(showPanel);
        	}
        	else {
        		this.add(formatterViewPanel);
        	}
        }
        
//        if (this.permalink) {
//            // TODO : Add viewpanel state (ie. size for window, tab)
//            var l = GeoNetwork.Util.getBaseUrl(location.href) + "?uuid=" + this.metadataUuid;
//            this.getTopToolbar().add(GeoNetwork.Util.buildPermalinkMenu(l));
//        }
        
        this.addEvents(
                /** private: event[search]
                 *  Fires search.
                 */
                "aftermetadataload"
            );
        this.on({
            "aftermetadataload": this.afterMetadataLoad,
            scope: this
        });
    }
});

/** api: xtype = gn_view_viewpanel */
Ext.reg('cat', GeoNetwork.view.ViewPanel);
