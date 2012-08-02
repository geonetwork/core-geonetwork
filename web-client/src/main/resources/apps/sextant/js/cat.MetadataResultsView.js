Ext.namespace('cat');


cat.MetadataResultsView = Ext.extend(GeoNetwork.MetadataResultsView, {
	
	curMenu: undefined,
	
    /**
     * Get the element by the given type class (ex: 'wms' will get div.wmsMenu element)
     * and add a click event that will show the menu.
     */
    linkMenuInit: function(idx, node, type){
        var menuElt = Ext.get(Ext.DomQuery.selectNode('div.'+type+'Menu', node));
        if(menuElt) {
        	menuElt.on('click', function(){

        		if(this.curMenu) this.curMenu.destroy();
	            this.curMenu = this.createLinksMenu(idx, this, node, type);
	            if(this.curMenu) {
	            	this.curMenu.showAt([menuElt.getX(), menuElt.getY() + menuElt.getHeight()]);
	            }
	        }.bind(this));
        }
    },
    
    /**
     * Create a menu from all type class element (ex: 'wms' will take all <div> with the wmsLink class and
     * will create a menu from them).
     */
    createLinksMenu: function(id, dv, node, type) {
    	
    	var a = Ext.DomQuery.jsSelect('div.'+type+'Link', node);
    	var its = new Array();
    	
    	for (var i=0;i<a.length;i++) {
    		its.push({
    			text: a[i].firstChild.wholeText
    		});
    	}
    	if(its.length == 0) {
    		return;
    	}
        return new Ext.menu.Menu({
        	floating: true,
            resultsView: dv,
            items: its,
            listeners: {
            	mouseout: {
            		fn: function(m) {
            			//m.hide();
            		}
            	}
            }
        });
    },
    
    
    /**
     * Called after the ListView is rendered
     * Check if there are some WMS or Download links. Display buttons if needed
     */
    resultsLoaded: function(view, records, options){
    	
    	cat.MetadataResultsView.superclass.resultsLoaded.apply(this, arguments);
    	
        var lis = Ext.DomQuery.jsSelect('li.md-full', this.el.dom);

        for(var i=0;i<lis.length;i++) {
        	var wmsMenu = Ext.DomQuery.jsSelect('div.wmsMenu', lis[i]);
        	var downloadMenu = Ext.DomQuery.jsSelect('div.downloadMenu', lis[i]);
        	
        	var a = Ext.DomQuery.jsSelect('div.wmsLink', lis[i]);
        	if(a && a.length > 0) {
        		Ext.get(wmsMenu).removeClass('mdHiddenMenu');
        	} else {
        		Ext.get(wmsMenu).addClass('mdHiddenMenu');
        	}
        	a = Ext.DomQuery.jsSelect('div.downloadLink', lis[i]);
        	if(a && a.length > 0) {
        		Ext.get(downloadMenu).removeClass('mdHiddenMenu');
        	} else {
        		Ext.get(downloadMenu).addClass('mdHiddenMenu');
        	}
        }
    },
    
	initComponent: function(){
		this.addListener('mouseenter', function(dv, idx, node, e){
            this.linkMenuInit(idx, node, 'wms');
            this.linkMenuInit(idx, node, 'download');
        }, this);
		
        cat.MetadataResultsView.superclass.initComponent.call(this);
	}
	
});

Ext.reg('cat_metadataresultsview', cat.MetadataResultsView);
