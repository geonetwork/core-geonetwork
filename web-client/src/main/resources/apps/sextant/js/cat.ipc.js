Ext.namespace('cat');

cat.ipc = function() {
	
	var getViewerMenuItem = function() {
		var result = null;
		var navItems = Ext.each(Ext.query("#navigation a"), function(){
			if (this.innerHTML.toUpperCase().indexOf("GEOVIEWER") != -1) {	
				result = [];
				result["obj"] = this;
				result["key"] = "Geoviewer";
			} else if (this.innerHTML.toUpperCase().indexOf("VIEWER") != -1) {	
				result = [];
				result["obj"] = this;
				result["key"] = "Viewer";
			} else if (this.innerHTML.toUpperCase().indexOf("VISUALISATEUR") != -1) {
				result = [];
				result["obj"] = this;
				result["key"] = "Visualisateur";
			}
		});
		return result;
	};
	
	var getPanierMenuItem = function() {
		var result = null;
		var navItems = Ext.each(Ext.query("#navigation a"), function(){
			if (this.innerHTML.toUpperCase().indexOf("PANIER") != -1) {	
				result = [];
				result["obj"] = this;
				result["key"] = "Panier";
			} else if (this.innerHTML.toUpperCase().indexOf("BASKET") != -1) {
				result = [];
				result["obj"] = this;
				result["key"] = "Basket";
			}
		});
		return result;
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