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
             top: '@',
             bottom: '@',
             left: '@',
             right: '@',
             topRef: '@',
             bottomRef: '@',
             leftRef: '@',
             rightRef: '@'

           },
           link: function(scope, element, attrs) {
             scope.drawing = false;

             // need to parseFloat for number type of the input
             $timeout(function() {
               scope.top = parseFloat(scope.top);
               scope.bottom = parseFloat(scope.bottom);
               scope.left = parseFloat(scope.left);
               scope.right = parseFloat(scope.right);
             });

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
               target: 'map',
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
               });
             });

             map.addInteraction(dragbox);

             scope.drawMap = function() {
               scope.drawing = !scope.drawing;
             };

             scope.updateBbox = function() {
               var coordinates = [
                 [
                   [scope.left, scope.bottom],
                   [scope.left, scope.top],
                   [scope.right, scope.top],
                   [scope.right, scope.bottom]
                 ]
               ];
               feature.getGeometry().setCoordinates(coordinates);
             };
           }
         };
       }]);
})();
