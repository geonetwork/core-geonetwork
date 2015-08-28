(function() {
  goog.provide('gn_layermanager');

  var module = angular.module('gn_layermanager', [
  ]);

  /**
   * @ngdoc filter
   * @name gn_viewer.filter:gnReverse
   *
   * @description
   * Filter for the gnLayermanager directive's ngRepeat. The filter
   * reverses the array of layers so layers in the layer manager UI
   * have the same order as in the map.
   */
  module.filter('gnReverse', function() {
    return function(items) {
      return items.slice().reverse();
    };
  });

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnLayermanager
   *
   * @description
   * The `gnLayermanager` directive display a list of all active layers
   * on the map and provides some tools/actions for each.
   * It also displays info if some layers failed to load.
   */
  module.directive('gnLayermanager', [
    'gnLayerFilters',
    'gnWmsQueue',
    function(gnLayerFilters, gnWmsQueue) {
      return {
        restrict: 'A',
        templateUrl: '../../catalog/components/viewer/layermanager/' +
            'partials/layermanager.html',
        scope: {
          map: '=gnLayermanagerMap'
        },
        controllerAs: 'gnLayermanagerCtrl',
        controller: ['$scope', function($scope) {

          /**
         * Change layer index in the map.
         *
         * @param {ol.layer} layer
         * @param {float} delta
         */
          this.moveLayer = function(layer, delta) {
            var index = $scope.layers.indexOf(layer);
            var layersCollection = $scope.map.getLayers();
            layersCollection.removeAt(index);
            layersCollection.insertAt(index + delta, layer);
          };

          /**
         * Set a property to the layer 'showInfo' to true and
         * false to all other layers. Used to display layer information
         * in the layer manager.
         *
         * @param {ol.layer} layer
         */
          this.showInfo = function(layer) {
            angular.forEach($scope.layers, function(l) {
              if (l != layer) {
                l.showInfo = false;
              }
            });
            layer.showInfo = !layer.showInfo;
          };
        }],
        link: function(scope, element, attrs) {

          scope.layers = scope.map.getLayers().getArray();
          scope.layerFilterFn = gnLayerFilters.selected;

          scope.failedLayers = gnWmsQueue.errors;
          scope.removeFailed = function(layer) {
            gnWmsQueue.removeFromError(layer);
          };
        }
      };
    }]);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnLayermanagerItem
   *
   * @description
   * The `gnLayermanagerItem` directive display one layer in the layer manager
   * list, and provides all tools to interacts with the layer.
   * <ul>
   *   <li>Show metadata</li>
   *   <li>Zoom to extent</li>
   *   <li>Change order</li>
   * </ul>
   */
  module.directive('gnLayermanagerItem', [
    'gnMdView',
    function(gnMdView) {
      return {
        require: '^gnLayermanager',
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/viewer/layermanager/' +
            'partials/layermanageritem.html',
        scope: true,
        link: function(scope, element, attrs, ctrl) {
          scope.layer = scope.$eval(attrs['gnLayermanagerItem']);
          scope.showInfo = ctrl.showInfo;
          scope.moveLayer = ctrl.moveLayer;

          scope.showMetadata = function() {
            gnMdView.openMdFromLayer(scope.layer);
          };

          scope.zoomToExtent = function(layer, map) {
            if (layer.get('cextent')) {
              map.getView().fit(layer.get('cextent'), map.getSize());
            } else if (layer.get('extent')) {
              map.getView().fit(layer.get('extent'), map.getSize());
            }
          };
        }
      };
    }]);

})();
