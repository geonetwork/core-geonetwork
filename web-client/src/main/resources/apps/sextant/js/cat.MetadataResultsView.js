Ext.namespace('cat');


cat.MetadataResultsView = Ext.extend(GeoNetwork.MetadataResultsView, {
	
	/** Menu containing all download link of the MD **/
	downloadMenu: undefined,
	
	/** Menu containing all WMS link of the MD **/
	wmsMenu: undefined,
	
	wmsMenuInit: function(idx, node){
        this.wmsAcMenu = Ext.get(Ext.DomQuery.selectNode('div.wmsMenu', node));
        if(this.wmsAcMenu) {
	        this.wmsAcMenu.on('click', function(){
	            this.createLinksMenu(idx, this, node);
	            this.wmsMenu.showAt([this.wmsAcMenu.getX(), this.wmsAcMenu.getY() + this.wmsAcMenu.getHeight()]);
	        }.bind(this));
        }
    },
    
    /**
     * Get the element by the given type class (ex: 'wms' will get div.wmsMenu element)
     * and add a click event that will show the menu.
     */
    linkMenuInit: function(idx, node, type){
        var menuElt = Ext.get(Ext.DomQuery.selectNode('div.'+type+'Menu', node));
        if(menuElt) {
        	menuElt.on('click', function(){

	            var extMenu = this.createLinksMenu(idx, this, node, type);
	            if(extMenu) {
	            	extMenu.showAt([menuElt.getX(), menuElt.getY() + menuElt.getHeight()]);
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
    
    resultsLoaded: function(view, records, options){
    	
    	cat.MetadataResultsView.superclass.resultsLoaded.call(this);
    	
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
		
//		this.addListener('afterrender', function(it){
//            alert('render');
//        }, this);
		
        cat.MetadataResultsView.superclass.initComponent.call(this);
	}
	
});

Ext.reg('cat_metadataresultsview', cat.MetadataResultsView);
