Ext.namespace('cat');


cat.MetadataResultsView = Ext.extend(GeoNetwork.MetadataResultsView, {
	
	curMenu: undefined,
	
	/** current index in the dataview list of the selected MD **/
	curId: -1,
	
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
        		this.curId= idx;
        		
        		//don't create a menu if only 1 element
        		var a = Ext.DomQuery.jsSelect('div.'+type+'Link', node);
        		if(a && a.length==1 && a[0].firstChild){
        			this.menuAction(a[0].lastChild.innerHTML, type);
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
    
    menuAction: function(link, type) {
    	if(type == 'wms') {
	    	var c = link.split('|');
	    	
	    	Ext.get(Ext.query('input[id*=layergroup]')[0]).dom.value = this.getStore().getAt(this.curId).get("category")[0].value;
	    	Ext.get(Ext.query('input[id*=layername]')[0]).dom.value = c[0];
	    	Ext.get(Ext.query('input[id*=wmsurl]')[0]).dom.value = c[2];
	        
	    	var p='';
	    	switch (c[3]) {
	        case "OGC:WMS-1.0.0-http-get-map":
	            p = "WMS_1.0.0";
	            break;
	        case "OGC:WMS-1.1.1-http-get-map":
	            p = "WMS_1.1.1";
	            break;
	        case "OGC:WMS-1.3.0-http-get-map":
	            p = "WMS_1.3.0";
	            break;
	        }
	    	
	    	Ext.get(Ext.query('input[id*=wmsversion]')[0]).set({value:p});
	    	Ext.query('a[id*=viewerButton]')[0].onclick();
    	}
    	else if(type=='download') {
    		Ext.get(Ext.query('input[id*=layername]')[0]).dom.value = link;
    		Ext.get(Ext.query('input[id*=getrecordbyidurl]')[0]).dom.value = 
    			this.catalogue.services.csw + '?SERVICE=CSW&VERSION=2.0.2&outputSchema=csw:IsoRecord&REQUEST=GetRecordById&ID=' + this.getStore().getAt(this.curId).get("uuid");
    		Ext.query('a[id*=panierButton]')[0].onclick();
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
    		if(a[i].firstChild) {
    			its.push(new Ext.Action({
    				text: a[i].firstChild.wholeText,
    				type: type,
    				cfg: a[i].children ? a[i].lastChild.innerHTML:'',
    				handler: function(action) {
    					this.menuAction(action.cfg, action.type)
    				},
    				scope:this
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
    
    createMenu: function(id, dv){
        var record = this.getStore().getAt(id);
        
        if (!this.contextMenu) {
            this.contextMenu = new GeoNetwork.MetadataMenu({
                floating: true,
                catalogue: catalogue,
                record: record,
                resultsView: dv,
                composeMenu: function(){
                    this.add(this.editAction);
                    this.add(this.deleteAction);
                    this.add(this.duplicateAction);
                    this.add(this.createChildAction);
                    this.add(this.adminAction);
                    this.add(this.statusAction);
                    this.add(this.versioningAction);
                    this.add(this.categoryAction);
                },
            });
        } else {
            this.contextMenu.setRecord(record);
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
