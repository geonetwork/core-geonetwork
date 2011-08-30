OpenLayers.ProxyHostURL = '/geonetwork/proxy?url=';
OpenLayers.ProxyHost = function(url){
    /**
     * Do not use proxy for local domain.
     * This is required to keep the session activated.
     */
    if (url && url.indexOf(window.location.host) != -1) {
        return url;
    } else {
        return OpenLayers.ProxyHostURL + encodeURIComponent(url);
    }
};

var catalogue;

Ext.onReady(function(){

    catalogue = new GeoNetwork.Catalogue({
        servlet: GeoNetwork.URL
    });
    catalogue.metadataStore = GeoNetwork.data.MetadataResultsStore();
    
    var tb = new Ext.Toolbar({
        width: 155
    });
    tb.render('qr');
    
    var menu = new GeoNetwork.OGCServiceQuickRegister({
        catalogue: catalogue
    });
    
    tb.add({
        text: 'Add an OGC service',
        iconCls: 'md-mn md-mn-badd',
        scale: 'large',
        iconAlign: 'left',
        menu: menu
    });
    tb.doLayout();
});
