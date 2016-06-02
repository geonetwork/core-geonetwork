Ext.namespace('cat.view');

cat.view.ViewWindow = Ext.extend(GeoNetwork.view.ViewWindow, {

	/** url for the metadata.formatter.html to custom MD format **/
	formatterServiceUrl: undefined,
	
	/** template use for custom formatter **/
	viewMode: undefined,
	
    initComponent: function(){
        Ext.applyIf(this, this.defaultConfig);
        
        
        GeoNetwork.view.ViewWindow.superclass.initComponent.call(this);
        if(!this.title) this.setTitle(this.record ? Ext.util.Format.ellipsis(this.record.get('title'), 150) : '');
        
        this.panel = new cat.view.ViewPanel({
            serviceUrl: this.serviceUrl,
            formatterServiceUrl: this.formatterServiceUrl,
            lang: this.lang,
            currTab: GeoNetwork.defaultViewMode || 'simple',
            printDefaultForTabs: GeoNetwork.printDefaultForTabs || false,
            catalogue: this.catalogue,
            metadataUuid: this.metadataUuid,
            record: this.record,
            resultsView: this.resultsView,
            border: false,
            frame: false,
            autoScroll: true,
            viewMode: this.viewMode,
            permalink: this.permalink
        });
        this.add(this.panel);
        
        this.on('beforeshow', function(el) {
            el.setSize(
                el.getWidth() > Ext.getBody().getWidth() ? Ext.getBody().getWidth() : el.getWidth(),
                el.getHeight() > Ext.getBody().getHeight() ? Ext.getBody().getHeight() : el.getHeight()); 
        });
    }
});

/** api: xtype = gn_view_viewwindow */
Ext.reg('cat_view_viewwindow', cat.view.ViewWindow);
