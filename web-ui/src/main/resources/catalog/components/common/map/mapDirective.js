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

             var map = new ol.Map({
               layers: [
                 new ol.layer.Tile({
                   source: new ol.source.OSM()
                 })
               ],
               renderer: ol.RendererHint.CANVAS,
               target: 'map',
               view: new ol.View2D({
                 center: [0, 0],
                 zoom: 2
               })
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

             var fo = new ol.render.FeaturesOverlay();
             var featuresCollection = new ol.Collection();

             fo.setMap(map);
             fo.setFeatures(featuresCollection);
             fo.setStyleFunction(function(feature, resolution) {
               return [boxStyle];
             });

             var feature = new ol.Feature();
             featuresCollection.push(feature);

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
               feature.setGeometry(dragbox.getGeometry());
               scope.bottom = extent[0];
               scope.left = extent[1];
               scope.top = extent[2];
               scope.right = extent[3];
             });

             map.addInteraction(dragbox);

             var bahavior = function(map, polygon) {
               var extent = polygon.getLinearRings()[1].getExtent();
               scope.$apply(function() {
                 scope.bottom = extent[0];
                 scope.left = extent[1];
                 scope.top = extent[2];
                 scope.right = extent[3];
               });
             };
             var boxInteraction = new ol.interaction.DragBox({
               behavior: bahavior,
               condition: function() {return scope.drawing;},
               style: new ol.style.Style({
                 stroke: new ol.style.Stroke({
                   color: 'rgba(255,0,0,1)'
                 })
               })
             });

             map.addInteraction(boxInteraction);
             scope.drawMap = function() {
               scope.drawing = !scope.drawing;
             };
           }
         };
       }]);
})();
