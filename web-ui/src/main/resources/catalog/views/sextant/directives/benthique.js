(function() {
  goog.provide('sxt_benthique');

  var module = angular.module('sxt_benthique', []);


  module.directive('sxtbPanel', [
    '$http',
    'gnUtilityService',
    function ($http, gnUtilityService) {
      return {
        restrict: 'A',
        templateUrl: '../../catalog/views/sextant/directives/' +
            'partials/benthiquepanel.html',
        scope: {
          map: '=sxtbPanel'
        },
        link: function(scope, element, attrs) {

          var url = '../../catalog/views/sextant/' +
              'data/ATLASbenthos_ATL_SPECIES.csv';
          $http.get(url).then(function(response){
            var csv =  gnUtilityService.CSVToArray(response.data, ';');
            if(angular.isArray(csv)) {
              scope.speciesTree = {
                nodes: []
              };
              csv.splice(0,1); // remove title line
              angular.forEach(csv, function(species) {
                createNode(scope.speciesTree, species, 0);
              });
            }
          });

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
          var createNode = function(node, species, index) {
            var group = species[index];
            if (group && index < 3) {
              var newNode = findChild(node, group);
              if (!newNode) {
                newNode = {
                  name: group
                };
                if (!node.nodes) node.nodes = [];
                node.nodes.push(newNode);
              }
              createNode(newNode, species, index + 1);
            } else {
              if (!node.nodes) node.nodes = [];
              node.nodes.push(species);
            }
          };
        }
      };
    }]);

  module.directive('sxtbSpiciesCol', [
    function() {
      return {
        restrict: 'E',
        replace: true,
        scope: {
          collection: '=',
          map: '=map'
        },
        template: "<ul class='sxt-layertree-node'><sxtb-spicies-elt ng-repeat='member" +
            " in collection' member='member' map='map'></sxtb-spicies-elt></ul>"
      };
    }]);

  module.directive('sxtbSpiciesElt', [
    '$compile', 'gnMap', 'gnMdView',
    function($compile, gnMap, gnMdView) {
      return {
        restrict: 'E',
        replace: true,
        scope: {
          member: '=',
          map: '='
        },
        templateUrl: '../../catalog/views/sextant/directives/' +
            'partials/speciestreeitem.html',
        link: function(scope, element, attrs, controller) {
          var el = element;
          if (angular.isArray(scope.member.nodes)) {
            element.append("<sxtb-spicies-col class='list-group' " +
                "collection='member.nodes' map='map'></sxtb-spicies-col>");
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
