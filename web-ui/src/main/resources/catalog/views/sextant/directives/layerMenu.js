(function() {
  goog.provide('sxt_layermenu');

  var module = angular.module('sxt_layermenu', []);

  module.directive('sxtLayerMenu', ['gnMap', 'wfsFilterService', 'gnMdView', 'gnViewerSettings', '$timeout',
    function (gnMap, wfsFilterService, gnMdView, gnViewerSettings, $timeout) {
      return {
        restrict: 'E',
        templateUrl: '../../catalog/views/sextant/directives/' +
            'partials/layermenu.html',
        scope: {
          layer: '<',
          map: '<',
          user: '<'
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

          // this defines a custom property which will define the css height
          // of the host element when its value changes
          var currentSize;
          Object.defineProperty(this, 'panelSize', {
            get: function () {
              return currentSize;
            },
            set: function(value) {
              currentSize = value;
              switch(currentSize) {
                case 'collapsed':
                  $element.css('height', '26px');
                  break;
                case 'middle':
                  $element.css('height', '55%');
                  break;
                case 'full':
                  $element.css('height', '100%');
                  break;
              }
            }
          });

          /**
           * current menu panel size
           * @type {'collapsed'|'middle'|'full'}
           */
          this.panelSize = 'collapsed';

          this.togglePanel = function() {
            this.panelSize = this.panelSize === 'collapsed' ? 'middle' : 'collapsed';
          }
          this.panelOpened = function() {
            return this.panelSize !== 'collapsed';
          }
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

          scope.$watch('layer', function (newVal) {
            handleLayerChange(newVal);
          });

          scope.$watch('layer.get(\'legend\')', function (newVal) {
            // replace with a 1x1px image to clear the image before loading the new legend
            scope.legendUrl = 'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==';
            $timeout(function () {
              scope.legendUrl = newVal;
            });
          });
        }
      };
    }]);

})();
