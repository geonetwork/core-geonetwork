(function () {
  goog.provide('gn_wmsimport_directive');

  var module = angular.module('gn_wmsimport_directive', [
  ]);

  /**
   * @ngdoc directive
   * @name gn_wmsimport_directive.directive:gnWmsImport
   *
   * @description
   * Panel to load WMS capabilities service and pick layers.
   * The server list is given in global properties.
   */
  module.directive('gnWmsImport', [
    'gnOwsCapabilities',
    'gnMap',
    '$translate',
    function (gnOwsCapabilities, gnMap, $translate) {
    return {
      restrict: 'A',
      templateUrl: '../../catalog/components/viewer/wmsimport/' +
        'partials/wmsimport.html',
      scope: {
        map: '=gnWmsImportMap'
      },
      link: function (scope, element, attrs) {
        scope.gnOwsCapabilities = gnOwsCapabilities;
        scope.selection = [];

        scope.params = {};

        //TODO: remove
        scope.servicesList = [
          'http://ids.pigma.org/geoserver/wms',
          'http://ids.pigma.org/geoserver/ign/wms',
          'http://www.ifremer.fr/services/wms/oceanographie_physique'
        ];
        scope.load = function (url) {
          scope.searchFilter = '';
          scope.selection = [];
          gnOwsCapabilities.getCapabilities(url)
            .then(function (layers) {
              scope.layers = layers;
            });
        };

        scope.select = function (layer) {
          if (scope.selection.indexOf(layer) < 0) {
            scope.selection.push(layer);
          }
          else {
            scope.selection.splice(scope.selection.indexOf(layer), 1);
          }
        };

        scope.addLayer = function(getCapLayer, isPreview) {

          if (getCapLayer) {

            try {
              var layer = getCapLayer;

              return gnMap.addWmsToMap(scope.map,
                {
                  LAYERS: layer.name
                },
                {
                  url: scope.params.url,
                  label: layer.title,
                  extent: gnOwsCapabilities.getLayerExtentFromGetCap(scope.map, layer)
                }
              );

            } catch (e) {
              scope.userMessage = $translate('add_wms_layer_failed') +
                e.message;
              return null;
            }
          }
        };

        scope.loadLayers = function(layers) {
          angular.forEach(layers, function(layer) {
            scope.addLayer(layer);
          });
        };
      }
    };
  }]);

  /**
   * @ngdoc directive
   * @name gn_wmsimport_directive.directive:gnCapTreeCol
   *
   * @description
   * Directive to manage a collection of nested layers from
   * the capabilities document.
   */
  module.directive('gnCapTreeCol', [
    function () {
      return {
        restrict: "E",
        replace: true,
        scope: {
          collection: '=',
          selection: '='
        },
        template: "<ul><gn-cap-tree-elt ng-repeat='member in collection' member='member' selection='selection'></gn-cap-tree-elt></ul>"
      }
    }]);

  /**
   * @ngdoc directive
   * @name gn_wmsimport_directive.directive:gnCapTreeElt
   *
   * @description
   * Directive to manage recursively nested layers from a capabilities
   * document. Will call its own template to display the layer but also
   * call back the gnCapTreeCol for all its children.
   */
  module.directive('gnCapTreeElt', [
    '$compile',
    function ($compile) {
    return {
      restrict: "E",
      replace: true,
      scope: {
        member: '='
      },
      template: "<li data-ng-click='select(member)'>{{member.title}}</li>",
      link: function (scope, element, attrs, controller) {
        if (angular.isArray(scope.member.nestedLayers)) {
          element.append("<gn-cap-tree-col collection='member.nestedLayers' selection='selection'></gn-cap-tree-col>");
          $compile(element.contents())(scope)
        }
        scope.select = function (layer) {
          if (scope.selection.indexOf(layer) < 0) {
            scope.selection.push(layer);
          }
          else {
            scope.selection.splice(scope.selection.indexOf(layer), 1);
          }
        };
      }
    }
  }]);
})();
