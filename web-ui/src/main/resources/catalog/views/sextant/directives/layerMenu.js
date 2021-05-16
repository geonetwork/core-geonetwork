(function() {
  goog.provide('sxt_layermenu');

  var module = angular.module('sxt_layermenu', []);

  module.directive('sxtLayerMenu', ['gnMap', 'wfsFilterService', 'gnMdView', 'gnViewerSettings',
    function (gnMap, wfsFilterService, gnMdView, gnViewerSettings) {
      return {
        restrict: 'E',
        templateUrl: '../../catalog/views/sextant/directives/' +
            'partials/layermenu.html',
        scope: {
          layer: '<',
          map: '<',
          user: '<',
          onClose: '&'
        },
        controller: ['$scope', '$element', function($scope, $element) {
          this.addToPanier = function() {
            gnViewerSettings.resultviewFns.addMdLayerToPanier($scope.download, $scope.layer.get('md'));
          }

          this.showMetadata = function() {
            gnMdView.openMdFromLayer($scope.layer);
          };

          this.zoomToExtent = function() {
            gnMap.zoomLayerToExtent($scope.layer, $scope.map);
          }

          this.deleteLayer = function() {
            var layers = $scope.map.getLayers().getArray();
            for (var i = 0; i < layers.length; i++) {
              if (layers[i].get('wpsParent') === $scope.layer) {
                $scope.map.removeLayer(layers[i]);
              }
            }
            $scope.map.removeLayer($scope.layer);
          }

          // this defines a custom property which will add a 'data-expanded' attribute
          // on the host element when its value changes
          var expanded;
          Object.defineProperty(this, 'expanded', {
            get: function () {
              return expanded;
            },
            set: function(value) {
              expanded = value;
              $element[0].toggleAttribute('data-expanded', expanded); // note: this will fail in IE11
            }
          });

          // initially not expanded
          this.expanded = false;
        }],
        controllerAs: 'ctrl',
        link: function(scope) {
          function handleLayerChange(newLayer) {
            // clear local variables
            scope.download = null;
            scope.wfsLink = null;
            scope.hasIndexedFeatures = false;
            scope.process = false;

            // layer has no associated metadata: do nothing
            if (!newLayer.get('md')) { return; }

            // look for downloads
            var d = newLayer.get('downloads');
            var downloadable = newLayer.get('md').download === true;
            if (Array.isArray(d) && downloadable) {
              scope.download = d[0];
            }

            // look for indexed wfs
            var wfsLink = newLayer.get('wfs');
            if (Array.isArray(wfsLink) && downloadable) {
              wfsLink = wfsLink[0];
            } else {
              wfsLink = undefined;
            }
            if (wfsLink) {
              scope.wfsLink = wfsLink;
              var indexObject = wfsFilterService.registerEsObject(wfsLink.url, wfsLink.name);
              indexObject.init({
                wfsUrl: wfsLink.url,
                featureTypeName: wfsLink.name
              });
              indexObject.searchWithFacets({}).then(function (data) {
                if (data.count > 0) {
                  scope.hasIndexedFeatures = true;
                }
              });
            }

            // look for processes
            var processable = newLayer.get('md').process === true;
            var p = newLayer.get('processes');
            if (Array.isArray(p) && processable) {
              scope.process = p;
            }
          }

          scope.handleClose = function () {
            scope.onClose();
          };

          scope.$watch('layer', function (newVal) {
            handleLayerChange(newVal);
          });
        }
      };
    }]);

})();
