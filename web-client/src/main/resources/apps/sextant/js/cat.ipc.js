Ext.namespace('cat');

cat.ipc = function() {
	
	var getViewerMenuItem = function() {
            //first looking for the function defined in liferay's hook,
            //if not found then use the redefined method from sextant with last changes.
            if(window.getViewerMenuItem){
                return window.getViewerMenuItem();
            }else {
                var navItems = document.getElementById("navigation").getElementsByTagName("a");
                var result = [];
                for (var i = 0; i < navItems.length; i++) {
                    var item = navItems[i];
                    var navItem;
                    if(item.innerHTML.indexOf("<span>") != -1){
                        navItem = item.getElementsByTagName("span")[0];
                    }else {
                        navItem = item;
                    }
                    if (navItem.innerHTML.toUpperCase().indexOf("GEOVIEWER") != -1) {
                        result["obj"] = navItem;
                        result["key"] = "Geoviewer";
                        return result;
                    }else if (navItem.innerHTML.toLowerCase().indexOf("géoviewer") != -1) {
                        result["obj"] = navItem;
                        result["key"] = "Géoviewer";
                        return result;
                    } else if (navItem.innerHTML.toUpperCase().indexOf("VIEWER") != -1) {
                        result["obj"] = navItem;
                        result["key"] = "Viewer";
                        return result;
                    } else if (navItem.innerHTML.toUpperCase().indexOf("VISUALISEUR") != -1) {
                        result["obj"] = navItem;
                        result["key"] = "Visualiseur";
                        return result;
                    }
                }
                return null;
            }
	};
	
	var getPanierMenuItem = function() {
            //first looking for the function defined in liferay's hook,
            //if not found then use the redefined method from sextant with last changes.
            if(window.getPanierMenuItem){
		return window.getPanierMenuItem();
            }else {
                var navItems = document.getElementById("navigation").getElementsByTagName("a");
                var result = [];
                for (var i = 0; i < navItems.length; i++) {
                    var item = navItems[i];
                    var navItem;
                    if(item.innerHTML.indexOf("<span>") != -1){
                        navItem = item.getElementsByTagName("span")[0];
                    }else {
                        navItem = item;
                    }
                    if (navItem.innerHTML.toUpperCase().indexOf("PANIER") != -1) {
                        result["obj"] = navItem;
                        result["key"] = "Panier";
                        return result;
                    } else if (navItem.innerHTML.toUpperCase().indexOf("BASKET") != -1) {
                        result["obj"] = navItem;
                        result["key"] = "Basket";
                        return result;
                    }
                }
                return null;
            }
	};
	
	return	{
		
		displayLayersNumber: function() {
			var panierMenuItem = getPanierMenuItem();
			if (panierMenuItem) {
				var nbLayersInput = Ext.query("span[id*=nbPanierLayers]")[0];
				var nbLayers = nbLayersInput.innerHTML;
				if (nbLayers != '0') {
					panierMenuItem["obj"].innerHTML = panierMenuItem["key"] + " (" + nbLayers + ")";
				}
			}
			
			var viewerMenuItem = getViewerMenuItem();
			if (viewerMenuItem) {
				var nbLayersInput = Ext.query("span[id*=nbViewerLayers]")[0];
				var nbViewers = nbLayersInput.innerHTML;
				if (nbViewers != '0') {
					viewerMenuItem["obj"].innerHTML = viewerMenuItem["key"] + " (+" + nbViewers + ")";
				}
			}
		}
	};
}();
