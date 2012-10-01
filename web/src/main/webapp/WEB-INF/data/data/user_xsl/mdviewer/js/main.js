var catalogue;


Ext.onReady(function() {
	
	urlParameters = GeoNetwork.Util.getParameters(location.href);
	
	var style = urlParameters.style || 'sextant';
	var uuid = urlParameters.uuid;
	
	catalogue = new GeoNetwork.Catalogue({
		lang : 'fre',
		hostUrl : 'http://localhost:8080/geonetwork'
	});
	
	var win = new cat.view.ViewWindow({
        serviceUrl: style == 'sextant' ? catalogue.services.mdView + '?uuid=' + escape(uuid) : null,
        formatterServiceUrl: catalogue.services.mdFormatter + '?uuid=' + escape(uuid) + '&xsl=' + style,
        lang: catalogue.lang,
        currTab: GeoNetwork.defaultViewMode || 'simple',
        printDefaultForTabs: GeoNetwork.printDefaultForTabs || false,
        catalogue: catalogue,
        maximized: false,
        metadataUuid: uuid,
        viewMode: style, 
        modal: true,
        draggable: false,
        movable: false,
        resizable: false,
        width: Ext.getBody().getViewSize().width-400,
        height: Ext.getBody().getViewSize().height-250,
        cls: 'view-win',
        bodyStyle:'padding:10px',
        title: title
        });
	
    win.show();
});