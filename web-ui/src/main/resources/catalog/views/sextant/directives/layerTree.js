(function() {
  goog.provide('sxt_layertree');

  var module = angular.module('sxt_layertree', []);

  module.directive('sxtLayertree', [
    'gnLayerFilters',
    '$filter',
    function (gnLayerFilters, $filter) {
      return {
        restrict: 'A',
        templateUrl: '../../catalog/views/sextant/directives/' +
            'partials/layertree.html',
        controller: [ '$scope', function($scope) {
          this.setNCWMS = function(layer) {
            $scope.active.layersTools = false;
            $scope.active.NCWMS = layer;
          };
        }],
        link: function(scope, element, attrs) {

          scope.layers = scope.map.getLayers().getArray();
          scope.layerFilterFn = gnLayerFilters.selected;

          var findChild = function(node, name) {
            var n;
            if (node.nodes) {
              for (var i = 0; i < node.nodes.length; i++) {
                n = node.nodes[i];
                if (name == n.name) {
                  return n;
                }
              }
            }
          };
          var createNode = function(layer, node, g, index) {
            var group = g[index];
            if (group) {
              var newNode = findChild(node, group);
              if (!newNode) {
                newNode = {
                  name: group
                };
                if (!node.nodes) node.nodes = [];
                node.nodes.push(newNode);
              }
              createNode(layer, newNode, g, index + 1);
            } else {
              if (!node.nodes) node.nodes = [];
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

  module.directive('sxtLayertreeCol', [
    function() {
      return {
        restrict: 'E',
        replace: true,
        scope: {
          collection: '=',
          map: '=map'
        },
        template: "<ul class='sxt-layertree-node'><sxt-layertree-elt ng-repeat='member" +
            " in collection' member='member' map='map'></sxt-layertree-elt></ul>"
      };
    }]);

  module.directive('sxtLayertreeElt', [
    '$compile', 'gnMap', 'gnMdView',
    function($compile, gnMap, gnMdView) {
      return {
        restrict: 'E',
        replace: true,
        require: '^sxtLayertree',
        scope: {
          member: '=',
          map: '='
        },
        templateUrl: '../../catalog/views/sextant/directives/' +
            'partials/layertreeitem.html',
        link: function(scope, element, attrs, controller) {
          var el = element;
          if (angular.isArray(scope.member.nodes)) {
            element.append("<sxt-layertree-col class='list-group' " +
                "collection='member.nodes' map='map'></sxt-layertree-col>");
            $compile(element.contents())(scope);
          }
          scope.toggleNode = function(evt) {
            el.find('.fa').first().toggleClass('fa-minus-square-o')
                .toggleClass('fa-plus-square-o');
            el.children('ul').toggle();
            evt.stopPropagation();
            return false;
          };
          scope.isParentNode = function() {
            return angular.isDefined(scope.member.nodes);
          };

          scope.mapService = gnMap;

          scope.setNCWMS = controller.setNCWMS;

          scope.showMetadata = function() {
            var md = scope.member.get('md');
            if(md) {
              gnMdView.feedMd(0, md, [md]);
            }
          };
        }
      };
    }]);

})();
