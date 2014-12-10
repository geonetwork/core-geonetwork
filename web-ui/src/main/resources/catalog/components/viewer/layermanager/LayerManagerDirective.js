(function() {
  goog.provide('gn_layermanager_directive');

  var module = angular.module('gn_layermanager_directive', [
  ]);

  /**
   * @ngdoc filter
   * @name gn_wmsimport_directive.filter:gnReverse
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
   * @name gn_wmsimport_directive.directive:gnWmsImport
   *
   * @description
   * Panel to load WMS capabilities service and pick layers.
   * The server list is given in global properties.
   */
  module.directive('gnLayermanager', [
    'gnLayerFilters',
    function(gnLayerFilters) {
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
        }
      };
    }]);

  module.directive('gnLayermanagerItem', ['gnPopup',
    function(gnPopup) {
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

          scope.showMetadata = function(url, title) {
            if (url) {
              gnPopup.create({
                title: title,
                url: 'http://sextant.ifremer.fr/geonetwork/srv/fre/' +
                    'metadata.formatter.html?xsl=mdviewer&style=sextant&url=' +
                    encodeURIComponent(url),
                content: '<div class="gn-popup-iframe">' +
                    '<iframe frameBorder="0" border="0" ' +
                    'style="width:100%;height:100%;" ' +
                    'src="{{options.url}}" ></iframe>' +
                    '</div>'
              });
            }
          };

          scope.zoomToExtent = function(layer, map) {
            if (layer.get('cextent')) {
              map.getView().fitExtent(layer.get('cextent'), map.getSize());
            }
          };
        }
      };
    }]);

})();
