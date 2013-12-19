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
               scope.top = parseInt(scope.top);
               scope.bottom = parseInt(scope.bottom);
               scope.left = parseInt(scope.left);
               scope.right = parseInt(scope.right);
             });

             var map = new ol.Map({
               layers: [
                 new ol.layer.Tile({
                   source: new ol.source.OSM()
                 })
               ],

               renderer: ol.RendererHint.CANVAS,
               view: new ol.View2D({
                 center: [0, 0],
                 zoom: 2
               })
             });

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

             scope.renderMap = function() {
               map.setTarget('map');
             };
             scope.drawMap = function() {
               scope.drawing = !scope.drawing;
             };
           }
         };
       }]);
})();
