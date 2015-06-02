var catalogue;
var app ={};

Ext.onReady(function() {
	
	var urlParameters = GeoNetwork.Util.getParameters(location.href);
	
	var style = urlParameters.style || 'sextant';
	var screenMode = urlParameters.screen || 'full';

	var uuid = urlParameters.uuid || urlParameters.id;
	
	if(!uuid && urlParameters.url) {
		var paramsCsw = GeoNetwork.Util.getParameters(urlParameters.url);
		uuid = paramsCsw.uuid || paramsCsw.id;
	}
	var geonetworkUrl = window.location.href.match(/((http).*\/.*)\/srv.*\/.*.format.*/, '')[1];
	var lang = window.location.href.substring(
			window.location.href.indexOf('/srv')+5, 
			window.location.href.indexOf('/md.format'))
		|| GeoNetwork.Util.defaultLocale;
	
	GeoNetwork.Util.setLang(lang, '../../apps/js');
	
	catalogue = new GeoNetwork.Catalogue({
		lang : lang,
		hostUrl : geonetworkUrl
	});
	
	var store = GeoNetwork.data.MetadataResultsFastStore();
	catalogue.kvpSearch("fast=index&_uuid=" + uuid, null, null, null, true, store, null, false);
	var record = store.getAt(store.find('uuid', uuid));
	
	var formatterServiceUrl;
	if(urlParameters.url) {
		formatterServiceUrl =  'xml.format.html?schema=iso19139&xsl=' + style + '&url=' + encodeURIComponent(urlParameters.url);
	} else {
		formatterServiceUrl = catalogue.services.mdFormatter + '?uuid=' + escape(uuid) + '&xsl=' + style;
	}
	
	if(screenMode == 'win') {
		var win = new cat.view.ViewWindow({
	        serviceUrl: style == 'sextant' ? catalogue.services.mdView + '?uuid=' + escape(uuid) : null,
	        formatterServiceUrl: formatterServiceUrl,
	        lang: catalogue.lang,
	        currTab: GeoNetwork.defaultViewMode || 'simple',
	        printDefaultForTabs: GeoNetwork.printDefaultForTabs || false,
	        catalogue: catalogue,
	        maximized: false,
	        record: record,
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
	}
	else {
		var panel = new cat.view.ViewPanel({
            serviceUrl: style == 'sextant' ? catalogue.services.mdView + '?uuid=' + escape(uuid) : null,
            formatterServiceUrl: formatterServiceUrl,
            lang: catalogue.lang,
            currTab: GeoNetwork.defaultViewMode || 'simple',
            printDefaultForTabs: GeoNetwork.printDefaultForTabs || false,
            catalogue: catalogue,
            metadataUuid: uuid,
            record:record,
            renderTo: Ext.getBody()
        });
	}
});