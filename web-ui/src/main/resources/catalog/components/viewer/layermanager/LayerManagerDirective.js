(function () {
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
    function (gnLayerFilters) {
    return {
      restrict: 'A',
      templateUrl: '../../catalog/components/viewer/layermanager/' +
        'partials/layermanager.html',
      scope: {
        map: '=gnLayermanagerMap',
        mode: '=gnLayermanager'
      },
      link: function (scope, element, attrs) {

        scope.layers = scope.map.getLayers().getArray();
        scope.layerFilterFn = gnLayerFilters.selected;

/* TEMP if we decide to $compile content of the div instead of displaying both views
        scope.$watch('mode', function(val) {
          if(val == 'tree') {
            var tplUrl = '../../catalog/components/viewer/layermanager/partials/layermanagertree.html';
            $http.get(tplUrl, {cache: $templateCache}).success(function(tplContent){
              element.empty();
              element.append($compile(tplContent.trim())(scope));
            });
          }
          if(val == 'flat') {
            var tplUrl = '../../catalog/components/viewer/layermanager/partials/layermanager.html';
            $http.get(tplUrl, {cache: $templateCache}).success(function(tplContent){
              element.empty();
              element.append($compile(tplContent.trim())(scope));
            });
          }
        });
*/
      }
    };
  }]);

  module.directive('gnLayermanagerTree', [
    'gnLayerFilters',
    '$filter',
    function (gnLayerFilters, $filter, $compile, $templateCache, $http) {
      return {
        restrict: 'A',
        templateUrl: '../../catalog/components/viewer/layermanager/' +
            'partials/layermanagertree.html',
        scope: {
          map: '=gnLayermanagerMap'
        },
        link: function (scope, element, attrs) {

          scope.layers = scope.map.getLayers().getArray();
          scope.layerFilterFn = gnLayerFilters.selected;

          var findChild = function(node, name) {
            var n;
            if(node.nodes) {
              for(var i=0;i<node.nodes.length;i++) {
                n = node.nodes[i];
                if(name == n.name) {
                  return n;
                }
              }
            }
          };
          var createNode = function(layer, node, g, index) {
            var group = g[index];
            if(group) {
              var newNode = findChild(node, group);
              if(!newNode) {
                newNode = {
                  name: group
                };
                if(!node.nodes) node.nodes = [];
                node.nodes.push(newNode);
              }
              createNode(layer, newNode, g, index+1);
            } else {
              if(!node.nodes) node.nodes = [];
              node.nodes.push(layer);
            }
          };

          // Build the layer manager tree depending on layer groups
          scope.map.getLayers().on('change:length', function(e) {
            scope.layerTree = {
              nodes: []
            };
            var sep = '/';
            var fLayers = $filter('filter')(scope.layers, scope.layerFilterFn);
            for (var i = 0; i < fLayers.length; i++) {
              var l = fLayers[i];
              var groups = l.get('group');
              if (!groups) {
                scope.layerTree.nodes.push(l);
              }
              else {
                var g = groups.split(sep);
                createNode(l, scope.layerTree, g, 1);
              }
            }
          });
        }
      };
    }]);

  module.directive('gnLayertreeCol', [
    function () {
      return {
        restrict: 'E',
        replace: true,
        scope: {
          collection: '=',
          map: '=map'
        },
        template: "<ul class='list-group'><gn-layertree-elt ng-repeat='member in collection' member='member' map='map'></gn-layertree-elt></ul>"
      }
    }]);
  module.directive('gnLayertreeElt', [
    '$compile',
    function ($compile) {
      return {
        restrict: "E",
        replace: true,
        scope: {
          member: '=',
          map: '='
        },
        templateUrl: '../../catalog/components/viewer/layermanager/' +
            'partials/layermanagertreeitem.html',
        link: function (scope, element, attrs, controller) {
          var el = element;
          if (angular.isArray(scope.member.nodes)) {
            element.append("<gn-layertree-col class='list-group' collection='member.nodes' map='map'></gn-layertree-col>");
            $compile(element.contents())(scope);
          }
          scope.toggleNode = function(evt) {
            el.find('.fa').first().toggleClass('fa-minus-square-o').toggleClass('fa-plus-square-o');
            el.children('ul').toggle();
            evt.stopPropagation();
            return false;
          };
          scope.isParentNode = function() {
            return angular.isDefined(scope.member.nodes);
          }
        }
      }
    }]);

  module.directive('gnLayermanagerItem', [ 'gnLayerManagerService',
    function (gnLayerManagerService) {
      return {
        restrict: 'A',
        replace: false,
        templateUrl: '../../catalog/components/viewer/layermanager/' +
            'partials/layermanageritem.html',
        scope: true,
        link: function (scope, element, attrs) {
          scope.layer = scope.$eval(attrs['gnLayermanagerItem']);
          scope.service = gnLayerManagerService;

          scope.mode = attrs['gnMode'];
        }
      };
    }]);

  module.service('gnLayerManagerService', [
    'gnPopup',
    function(gnPopup) {

      this.showMetadata = function(url, title) {
        if(url) {
          gnPopup.create({
            title: title,
            url : 'http://sextant.ifremer.fr/geonetwork/srv/fre/metadata.formatter.html?xsl=mdviewer&style=sextant&url=' + encodeURIComponent(url),
            content: '<div class="gn-popup-iframe">' +
                '<iframe frameBorder="0" border="0" style="width:100%;height:100%;" src="{{options.url}}" ></iframe>' +
                '</div>'
          });
        }
      };

      this.zoomToExtent = function(layer, map) {
        if (layer.get('cextent')) {
          map.getView().fitExtent(layer.get('cextent'), map.getSize());
        }
      };

      this.showInfo = function(layer, layers) {
        angular.forEach(layers, function(l) {
          if(l != layer){
            l.showInfo = false;
          }
        });
        layer.showInfo = !layer.showInfo;
      }

      this.moveLayer = function(map, layer, delta) {
        var index = map.getLayers().getArray().indexOf(layer);
        var layersCollection = map.getLayers();
        layersCollection.removeAt(index);
        layersCollection.insertAt(index + delta, layer);
      };
    }]);
})();
