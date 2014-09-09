Ext.namespace('cat');


cat.MetadataResultsView = Ext.extend(GeoNetwork.MetadataResultsView, {
	
	curMenu: undefined,
	
	/** true if you want to display a popup for 5 seconds telling u just add a layer to basket or geoviewer **/
	popup: true,
	
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
		            	this.ownerCt.ownerCt.body.on('scroll', function(e,t,o){
		            		this.curMenu.hide();
		            	}, this, {
		            		single: true
		            	});
		            }
        		}
	        }, this);
        }
    },
    
    /**
     * Pass values to A4J hidden element and trigger the click to call java methods
     */
    menuAction: function(link, type) {
    	
    	var url;
    	var showPopup = true;
    	var c = link.split('|');
    	// For visualize button, check type of link.
    	if (c.length > 4) {
    		var linkType = c[3];
    		// If map context, override button type
    		// by link type
    		if (this.isMapContext(linkType)) {
    			type = 'ows';
    		}	
    	}
    		
    	if(type == 'wms') {
	    	var group, theme = this.getStore().getAt(this.curId).get("sextantTheme")[0];
	    	if(theme) {
	    		var translationStore = Ext.getCmp('E_sextantTheme').storeLabel;
	    		var idx = translationStore.findExact('name', theme.value);
	            if(idx >= 0 && translationStore.getAt(idx).get('label')) {
                group = translationStore.getAt(idx).get('label');
	            }
	    	}

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

        Ext.Ajax.request({
          url: catalogue.services.mdLayerSelect,
          params: {
            name: c[0],
            description: c[1],
            url: c[2],
            version:p,
            group: group,
            selected: 'add',
            type: 'wms'
          },
          success: function(res) {
            if(!res.responseXML) {
              var parser = new DOMParser();
              res.responseXML = parser.parseFromString(res.responseText, "application/xml");
            }
          }
        });

	    	url=catalogue.services.mdLayerSelect + '?action=get';
	    	
	    	//check if configgeoviewerurl is on the same host
	    	var aElt = document.createElement('a');
	    	aElt.setAttribute('href', url);
	    	if(aElt.host != window.location.host) {
	    	    showPopup = false;
	    	    url = url.replace('${wmsurl}', encodeURIComponent(c[2])).replace('${layername}', encodeURIComponent(c[0]));
	    	    window.open(url,'_blank');
	    	}
	    	else {
	    	    //Ext.query('a[id*=viewerButton]')[0].onclick();
          console.log('same host');
	    	}
    	}
    	else if(type=='ows') {
    		showPopup = false;
	    	url = Ext.get(Ext.query('input[id*=configgeoviewerurl]')[0]).getValue();
	    	var context = c[2];
	    	window.open(url + '?url=' + context, '_blank');
    	}
    	else if(type=='download') {
        // Get info : first token is the layername for file, db, wfs ... protocol
        // second is href for WWW:DOWNLOAD-1.0-link--download
        var token = link.split('|');
        if (token[0] === '') {
          window.open(token[1], '_blank');
          showPopup = false;
        } else {
          Ext.get(Ext.query('input[id*=layername]')[0]).dom.value = token[0];
          Ext.get(Ext.query('input[id*=getrecordbyidurl]')[0]).dom.value =
            this.catalogue.services.mdXMLGet + '?uuid=' + this.getStore().getAt(this.curId).get("uuid");
          Ext.query('a[id*=panierButton]')[0].onclick();
          url=Ext.get(Ext.query('input[id*=configpanierurl]')[0]).getValue();
        }
    	}
    	if(this.popup && showPopup) {
    		this.displayPopup(type,url);
    	}
    },
    isMapContext: function (type) {
    	return type == 'OGC:WMC' || type == 'OGC:OWS' || type == 'OGC:OWS-C';
    },
    /**
     * Display a modal popup on list view button click.
     * popup informs action is made and ask for redirection to basket or geoviewer. Stays 5 sec.
     */
    displayPopup: function(type, url) {
    	
    	win = new Ext.Window({
            id: 'clickPopup-win',
            layout: 'fit',
            closeAction: 'destroy',
            maximized: false,
            border: false,
            modal: true,
            draggable: false,
            movable: false,
            resizable: false,
            width: 350,
            height: 120,
            cls: 'view-win sxt-popup',
            title: OpenLayers.i18n(type+'ModalMsg'),
            html: OpenLayers.i18n('modalRedirect') + ' <a href="'+ url+'" >'
            		+OpenLayers.i18n(type+'Portlet')+'</a>',
            listeners: {
            	afterrender: {
            		fn: function(w) {
            			w.destroy();
            		},
            		delay: 5000
            	}
            }
        });
    	win.show();
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
    			var txt = a[i].firstChild.data;
    			its.push(new Ext.Action({
    				text: txt,
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
//            			m.hide();
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
        	if(!isAdmin || (!GeoNetwork.Settings.editor.editHarvested === 'true' && records[i].get('isharvested') == 'y') || records[i].get('edit') === 'false') {
        		Ext.get(adminMenu).addClass('mdHiddenBtn');
        		Ext.get(adminMenu[0]).next('div.btn-separator').addClass('mdHiddenBtn');
        	}
        	
        	// Hide button if doesn't contain any element
        	// Unable button if elements have class dynamic-false (no privilege)
        	var geoviewerrurl = Ext.query('input[id*=configgeoviewerurl]');
    		var a = Ext.DomQuery.jsSelect('div.wmsLink', lis[i]);
        	var elMenu = Ext.get(wmsMenu);
        	if(a && a.length > 0 && 
        			geoviewerrurl && geoviewerrurl[0] && geoviewerrurl[0].value) {
        		
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

        	var panierurl = Ext.query('input[id*=configpanierurl]');
        	a = Ext.DomQuery.jsSelect('div.downloadLink', lis[i]);
        	elMenu = Ext.get(downloadMenu);
        	var dlToShow = 0;
        	
        	if(a && a.length > 0) {
        		
        		for(j=0;j<a.length;++j) {
        		    var node = a[j].firstElementChild ||  a[j].children[0];
        		    var l = node.innerText || node.textContent || node.text;
        		    if(l.split('|')[0]) {
        		        if(panierurl && panierurl[0] && panierurl[0].value) {
        		            dlToShow++;
        		        }
        		        else {
        		            if(Ext.isIE) {
        		                a[j].parentNode.removeChild(a[j]);
        		            }
        		            else {
        		                a[j].remove();
        		            }
        		        }
        		    } else {
        		        dlToShow++;
        		    }
        		}
        		if(dlToShow>0){
                    elMenu.removeClass('mdHiddenBtn');
                    if(Ext.get(a[0]).hasClass('download-false')) {
                        elMenu.addClass('unabled');
                    }
                    if(dlToShow == 1) {
                        elMenu.addClass('one-elt');
                    }
        		}
        	}
        	if(dlToShow==0 ){
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
                    this.add(this.extEditorAction);
                    this.add(this.deleteAction);
                    this.add(this.duplicateAction);
                    this.add(this.adminAction);
//                    this.add(this.categoryAction);
                    this.add(this.createChildAction);
                    this.add(this.statusAction);
                    this.add(this.enableWorkflowAction);
                    this.add(this.versioningAction);
                    
                }
            });
        } else {
            this.contextMenu.setRecord(record);
        }
        
    },

    initComponent : function() {

        this.addListener('mouseenter', function(dv, idx, node,e) {
            this.linkMenuInit(idx, node, 'wms');
            this.linkMenuInit(idx, node, 'download');
        }, this);

        cat.MetadataResultsView.superclass.initComponent.call(this);
    }
});

Ext.reg('cat_metadataresultsview', cat.MetadataResultsView);
