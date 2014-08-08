(function() {
  goog.provide('gn_map_field_directive');

  angular.module('gn_map_field_directive', [
  ])
      .directive('gnMapField', [
        'gnMap',
        function(gnMap) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/components/search/map/' +
                'partials/mapfield.html',
            link: function(scope, element, attrs) {

              scope.map = scope.$eval(attrs['gnMapField']);
              scope.gnMap = gnMap;

              scope.maxExtent = function() {
                scope.map.getView().fitExtent(scope.map.getView().getProjection().getExtent(),
                    scope.map.getSize());
              }
            }
          };
        }])
      .directive('gnDrawBboxBtn', [
        'goDecorateInteraction',
        '$parse',
        'gnOlStyles',
        'gnMap',
        function(goDecorateInteraction, $parse, gnOlStyles, gnMap) {
          return {
            restrict: 'A',
            scope: true,
            controller: ['$scope', function($scope) {
              var dragbox = new ol.interaction.DragBox({
                style: gnOlStyles.bbox
              });
              goDecorateInteraction(dragbox, $scope.map);
              $scope.interaction = dragbox;
            }],
            link: function(scope, element, attrs) {

              // Assign drawn extent to given scope property
              var bboxGet = $parse(attrs['gnDrawBboxBtn']);
              var bboxSet = bboxGet.assign;

              // Create overlay to persist the bbox
              var feature = new ol.Feature();
              var featureOverlay = new ol.FeatureOverlay({
                style: gnOlStyles.bbox
              });
              featureOverlay.setMap(scope.map);
              featureOverlay.addFeature(feature);

              scope.interaction.on('boxend', function(mapBrowserEvent) {
                scope.$apply(function() {
                  feature.setGeometry(scope.interaction.getGeometry());

                  // Write the extent as 4326 WKT polygon
                  var lonlatFeat, writer, wkt;
                  lonlatFeat = feature.clone();
                  lonlatFeat.getGeometry().transform('EPSG:3857', 'EPSG:4326');
                  writer = new ol.format.WKT();
                  wkt = writer.writeFeature(lonlatFeat);
                  bboxSet(scope.$parent, wkt);
                });
              });

              // Remove the bbox when the interaction is not active
              scope.$watch('interaction.active', function(v){
                if(!v) {
                  feature.setGeometry(null);
                  bboxSet(scope.$parent, '');
                  scope.map.render();
                }
              })
            }
          };
        }])
})();
