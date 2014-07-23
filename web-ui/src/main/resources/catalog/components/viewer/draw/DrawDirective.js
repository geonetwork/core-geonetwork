(function() {
  goog.provide('gn_draw_directive');

  var module = angular.module('gn_draw_directive', [
  ]);

  /**
   * @ngdoc directive
   * @name gn_wmsimport_directive.directive:gnWmsImport
   *
   * @description
   * Panel to load WMS capabilities service and pick layers.
   * The server list is given in global properties.
   */
  module.directive('gnDraw', [
    'goDecorateInteraction',
    function(goDecorateInteraction) {
      return {
        restrict: 'A',
        replace: false,
        templateUrl: '../../catalog/components/viewer/draw/' +
            'partials/draw.html',
        scope: {
          map: '=',
          vector: '='
        },
        link: function (scope, element, attrs) {
          var map = scope.map;
          var source = new ol.source.Vector();

          var vector = new ol.layer.Vector({
            source: source,
            style: new ol.style.Style({
              fill: new ol.style.Fill({
                color: 'rgba(255, 255, 255, 0.2)'
              }),
              stroke: new ol.style.Stroke({
                color: '#ffcc33',
                width: 2
              }),
              image: new ol.style.Circle({
                radius: 7,
                fill: new ol.style.Fill({
                  color: '#ffcc33'
                })
              })
            })
          });
          scope.vector = vector;

          var drawPolygon = new ol.interaction.Draw(({
                type: 'Polygon',
                source: source
              }));
          goDecorateInteraction(drawPolygon, map);
          scope.drawPolygon = drawPolygon;

          var drawPoint = new ol.interaction.Draw(({
                type: 'Point',
                source: source
              }));
          goDecorateInteraction(drawPoint, map);
          scope.drawPoint = drawPoint;

          var drawLine = new ol.interaction.Draw(({
                type: 'LineString',
                source: source
              }));
          goDecorateInteraction(drawLine, map);
          scope.drawLine = drawLine;

          Object.defineProperty(vector, 'inmap', {
            get: function() {
              return map.getLayers().getArray().indexOf(vector) >= 0;
            },
            set: function(val) {
              if (val) {
                map.addLayer(vector);
              } else {
                scope.drawPolygon.active = false;
                scope.drawPoint.active = false;
                scope.drawLine.active = false;
                map.removeLayer(vector);
              }
            }
          });
        }
      }
    }]);
})();
