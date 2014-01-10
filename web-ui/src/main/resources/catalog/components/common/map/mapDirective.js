(function() {
  goog.provide('gn_map_directive');

  angular.module('gn_map_directive', [])

    .directive(
      'gnDrawBbox',
      [
       '$timeout',
       function($timeout) {
         return {
           restrict: 'A',
           replace: true,
           templateUrl: '../../catalog/components/common/map/' +
           'partials/drawbbox.html',
           scope: {
             htop: '@',
             hbottom: '@',
             hleft: '@',
             hright: '@',
             htopRef: '@',
             hbottomRef: '@',
             hleftRef: '@',
             hrightRef: '@'

           },
           link: function(scope, element, attrs) {
             scope.drawing = false;
             scope.mapId = 'map-drawbbox-'+
               scope.htopRef.substring(1,scope.htopRef.length);
             
             /**
              * Reproj the bbox extent from map proj
              * to MD proj and fill the edit form.
              */
             var reprojFromMapToMd = function(extent) {
               
               var extent4326 = ol.proj.transform(extent, 
                   'EPSG:3857', 'EPSG:4326');
               
               scope.hleft = extent4326[0];
               scope.hbottom = extent4326[1];
               scope.hright = extent4326[2];
               scope.htop = extent4326[3];
             };
             
             // Get map proj coordinates from MD ones
             var bottomLeft4326 = ol.proj.transform(
                 [parseFloat(scope.hleft),parseFloat(scope.hbottom)],
                 'EPSG:4326', 'EPSG:3857');

             var topRight4326 = ol.proj.transform(
                 [parseFloat(scope.hright),parseFloat(scope.htop)],
                 'EPSG:4326', 'EPSG:3857');

             scope.top = topRight4326[1];
             scope.bottom = bottomLeft4326[1];
             scope.left = bottomLeft4326[0];
             scope.right = topRight4326[0];

             var boxStyle = new ol.style.Style({
               stroke: new ol.style.Stroke({
                 color: 'rgba(255,0,0,1)',
                 width: 2
               }),
               fill: new ol.style.Fill({
                 color: 'rgba(255,0,0,0.3)'
               })
             });

             var feature = new ol.Feature();
             var source = new ol.source.Vector();
             source.addFeature(feature);

             var bboxLayer = new ol.layer.Vector({
               source: source,
               styleFunction: function(feature, resolution) {
                 return [boxStyle];
               }
             });

             var map = new ol.Map({
               layers: [
                 new ol.layer.Tile({
                   source: new ol.source.OSM()
                 }),
                 bboxLayer
               ],
               renderer: ol.RendererHint.CANVAS,
               view: new ol.View2D({
                 center: [0, 0],
                 zoom: 2
               })
             });

             var dragbox = new ol.interaction.DragBox({
               style: boxStyle,
               condition: function() {
                  return scope.drawing;
               }
             });
             dragbox.on('boxstart', function(mapBrowserEvent) {
               feature.setGeometry(null);
             });
             dragbox.on('boxend', function(mapBrowserEvent) {
               var extent = dragbox.getGeometry().getExtent();
               feature.setGeometry(dragbox.getGeometry());
               
               scope.$apply(function() {
                 scope.left = extent[0];
                 scope.bottom = extent[1];
                 scope.right = extent[2];
                 scope.top = extent[3];
                 
                 reprojFromMapToMd(extent);
               });
             });

             map.addInteraction(dragbox);

             /**
              * When form is loaded
              * - set map div
              * - draw the feature with MD initial coordinates
              * - fit map extent
              */ 
             scope.$watch('gnCurrentEdit.version', function(newValue) {
               map.setTarget(scope.mapId);
               scope.updateBbox();
               map.getView().fitExtent(feature.getGeometry().getExtent(), map.getSize());
             });
             
             scope.drawMap = function() {
               scope.drawing = !scope.drawing;
             };

             /**
              * Redraw the Bbox when the user change the
              * coordinates in the form
              * If reproj is true, then reproj the bbox extent
              * to the MD proj.
              */
             scope.updateBbox = function(reproj) {
               var coordinates = [
                 [
                   [scope.left, scope.bottom],
                   [scope.left, scope.top],
                   [scope.right, scope.top],
                   [scope.right, scope.bottom]
                 ]
               ];
               
               if(!feature.getGeometry()) {
                 var geom = new ol.geom.Polygon([coordinates]);
                 feature.setGeometry(geom);
               }
               feature.getGeometry().setCoordinates(coordinates);
               
               if(reproj) {
                 reprojFromMapToMd(feature.getGeometry().getExtent());
               }
             };
           }
         };
       }]);
})();
