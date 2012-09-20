Ext.namespace('cat');


cat.MetadataResultsView = Ext.extend(GeoNetwork.MetadataResultsView, {
	
	curMenu: undefined,
	
	layer_style_hover: new OpenLayers.Style({
        fillColor: "#000000",
        fillOpacity: 0,
        strokeColor: "blue",
        strokeWidth: 2, 
        strokeOpacity: 1,
        graphicZIndex: 5000
    }),
    
	/**
     * Get the element by the given type class (ex: 'wms' will get div.wmsMenu element)
     * and add a click event that will show the menu.
     */
    linkMenuInit: function(idx, node, type){
        var menuElt = Ext.get(Ext.DomQuery.selectNode('div.'+type+'Menu', node));
        if(menuElt && !menuElt.hasClass('unabled')) {
        	menuElt.removeAllListeners();
        	menuElt.on('click', function(){

        		if(this.curMenu) this.curMenu.destroy();
        		
        		//don't create a menu if only 1 element
        		var a = Ext.DomQuery.jsSelect('div.'+type+'Link', node);
        		if(a && a.length==1 && a[0].firstChild){
        			this.menuAction(a[0].firstChild.wholeText);
        		}
        		else {
		            this.curMenu = this.createLinksMenu(idx, this, node, type);
		            if(this.curMenu) {
		            	this.curMenu.showAt([menuElt.getX(), menuElt.getY() + menuElt.getHeight()]);
		            }
        		}
	        }.bind(this));
        }
    },
    
    menuAction: function(action) {
    	var txt='';
    	if(typeof(action) == 'object'){
    		txt=action.text;
    	}
    	else {
    		txt= action
    	}
    	
    	return alert(txt);
    },
    
    /**
     * Create a menu from all type class element (ex: 'wms' will take all <div> with the wmsLink class and
     * will create a menu from them).
     */
    createLinksMenu: function(id, dv, node, type) {
    	
    	var a = Ext.DomQuery.jsSelect('div.'+type+'Link', node);
    	var its = new Array();
    	
    	for (var i=0;i<a.length;i++) {
    		if(a[i].firstChild) {
    			its.push(new Ext.Action({
    				text: a[i].firstChild.wholeText,
    				handler: this.menuAction
    			}));
    		}
    	}
    	if(its.length == 0) {
    		return;
    	}
        return new Ext.menu.Menu({
        	floating: true,
            resultsView: dv,
            showSeparator: false,
            cls: 'no-icon-menu',
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
        var isAdmin = this.catalogue.isIdentified();
        
        for(var i=0;i<lis.length;i++) {
        	var wmsMenu = Ext.DomQuery.jsSelect('div.wmsMenu', lis[i]);
        	var downloadMenu = Ext.DomQuery.jsSelect('div.downloadMenu', lis[i]);
        	var adminMenu = Ext.DomQuery.jsSelect('div.md-action-menu', lis[i]);
        	
        	// Hide admin button (and sep)if not connected
        	if(!isAdmin) {
        		Ext.get(adminMenu).addClass('mdHiddenBtn');
        		Ext.get(adminMenu[0]).next('div.btn-separator').addClass('mdHiddenBtn');
        	}
        	
        	// Hide button if doesn't contain any element
        	// Unable button if elements have class dynamic-false (no privilege)
        	var a = Ext.DomQuery.jsSelect('div.wmsLink', lis[i]);
        	var elMenu = Ext.get(wmsMenu);
        	if(a && a.length > 0) {
        		elMenu.removeClass('mdHiddenBtn');
        		if(Ext.get(a[0]).hasClass('dynamic-false')) {
        			elMenu.addClass('unabled');
        		}
        		if(a.length == 1) {
        			elMenu.addClass('one-elt');
        		}
        	} else {
        		elMenu.addClass('mdHiddenBtn');
        	}
        	
        	a = Ext.DomQuery.jsSelect('div.downloadLink', lis[i]);
        	elMenu = Ext.get(downloadMenu);
        	if(a && a.length > 0) {
        		elMenu.removeClass('mdHiddenBtn');
        		if(Ext.get(a[0]).hasClass('download-false')) {
        			elMenu.addClass('unabled');
        		}
        		if(a.length == 1) {
        			elMenu.addClass('one-elt');
        		}
        	} else {
        		elMenu.addClass('mdHiddenBtn');
        		Ext.get(downloadMenu[0]).next('div.btn-separator').addClass('mdHiddenBtn');
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
