Ext.namespace('cat');

cat.where = function() {

    return {
        createCmp : function() {

            var epsg4326 = new OpenLayers.Projection("EPSG:4326");
            var epsg900913 = new OpenLayers.Projection("EPSG:900913");

            var maxSearchExtent;
            var maxExtent = Ext.query('input[id*=configmaxextent]');
            
            // if maxExtent is set in configuration, adjust resolutions and mapExtent
            // so the map can't go out the restrictedExtent
            if (maxExtent && maxExtent[0] && maxExtent[0].value) {
                var initialExtent = OpenLayers.Bounds.fromString(maxExtent[0].value);
                maxSearchExtent = initialExtent.clone();

                initialExtent.transform(epsg4326, epsg900913);
                GeoNetwork.map.EXTENT = initialExtent;

                var layerOptions = {
                    buffer : 0,
                    attribution : OpenLayers.i18n("map_attribution")
                };

                var maxRes = Math.max((initialExtent.top-initialExtent.bottom)/227, (initialExtent.right-initialExtent.left)/300); 
                // based on a 227 px height & 300 px width for map div
                var resolutions = OpenLayers.Layer.prototype.calculateResolutions({
                    // OpenLayers.Layer.OSM default's parameters:
                    maxExtent: new OpenLayers.Bounds(
                        -128 * 156543.0339,
                        -128 * 156543.0339,
                        128 * 156543.0339,
                        128 * 156543.0339
                    ),
                    maxResolution: 156543.0339,
                    numZoomLevels: 19
                });
                var numZoomLevels = Math.round(Math.log(maxRes/ Ext.min(resolutions))/ Math.log(2));
                OpenLayers.Util.extend(layerOptions, {
                    resolutions : resolutions.slice(resolutions.length - numZoomLevels - 1),
                    zoomOffset : resolutions.length - numZoomLevels - 1
                });
                GeoNetwork.map.BACKGROUND_LAYERS = [ new OpenLayers.Layer.OSM('OSM',
                        'http://tile.openstreetmap.org/${z}/${x}/${y}.png',layerOptions)
                ];
            }
			GeoNetwork.map.MAP_OPTIONS = {
				projection : GeoNetwork.map.PROJECTION,
				maxExtent : GeoNetwork.map.EXTENT,
				restrictedExtent : GeoNetwork.map.EXTENT,
				controls : [],
				theme: null
			};

			return new Ext.Panel({
				title : OpenLayers.i18n('Where'),
				autoHeight : true,
				autoWidth : true,
				collapsible : true,
				collapsed : false,
				defaultType : 'checkbox',
				padding : '10px 45px 20px 45px',
				layout: 'fit',
				items : GeoNetwork.util.SearchFormTools.getSimpleMap(
						GeoNetwork.map.BACKGROUND_LAYERS,
						GeoNetwork.map.MAP_OPTIONS, true, {
							cls : 'where-toolbar',
							manageNavBar : function() {
								this.getTopToolbar().add('->',
										this.zoomAllAction, this.clearAction, 
										this.ExtAction, this.zoomOutAction,
										this.zoomInAction, this.panAction);
							},
							mousePosition: false,
							bodyStyle: 'border: 1px solid #D0D0D0',
							autoWidth: true,
							height : 280
						}, false, maxSearchExtent),
				listeners: {
					'afterrender': function(o) {
						o.header.on('click', function() {
							if(o.collapsed) o.expand();
							else o.collapse();
						});
					}
				}
			});
		}
	}
}();
