(function() {
  goog.provide('sxt_categorytree');

  var module = angular.module('sxt_categorytree', []);

  var delimiter = ' or ';
  var indexField = 'sextantTheme';
  var themesInSearch;

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
  var createNode = function(node, g, index, t) {
    var group = g[index];
    if (group) {
      var newNode = findChild(node, group);
      if (!newNode) {
        newNode = {
          name: group,
          selected: themesInSearch.indexOf(t['@name']) >= 0 ? true : false
        };
        if (!node.nodes) node.nodes = [];
        node.nodes.push(newNode);
      }
      createNode(newNode, g, index + 1, t);
    } else {
      node.key = t['@name'];
      node.count = t['@count'];
    }
  };

  module.directive('sxtCategoryTree', [
    'sxtGlobals',
    'gnHttp',
    function(sxtGlobals, gnHttp) {
      return {
        restrict: 'A',
        template: '<gn-categorytree-col class="list-group" ' +
            'collection="member.nodes"></gn-categorytree-col>',
        controller: ['$scope', '$timeout', function($scope, $timeout) {
          this.updateSearch = function() {
            $timeout($scope.triggerSearch, 100);
          }
        }],
        link: function(scope, element, attr) {

          var conf = scope.$eval(attr['sxtCategoryTreeConf']);

          /**
         * Find thesaurus translation from its id
         * @param {string} key
         * @return {*}
         */
          var findLabel = function(key) {
            for (var i = 0; i < sxtGlobals.sextantTheme.length; i++) {
              var t = sxtGlobals.sextantTheme[i];
              if (t.props.uri == key) {
                return t.label;
              }
            }
          };

          /**
         * Create tree structure from suggest service response
         * @param {Array} ts
         */
          var processThemes = function(ts) {
            scope.member = {
              nodes: []
            };
            themesInSearch = scope.searchObj.params[indexField] ?
              scope.searchObj.params[indexField].split(delimiter) : [];

            angular.forEach(ts, function(t) {
              var key = t['@name'], name;
              if (conf.labelFromThesaurus) {
                name = findLabel(key);
              }
              if (name) {
                  var g = name.split('/');
                  createNode(scope.member, g, 1, t);
              }
            });
          };

          scope.$watch('searchResults.facet', function(v) {

            if (v) {
              var facets = v['sextantThemes'];

              if (angular.isArray(sxtGlobals.sextantTheme)) {
                processThemes(facets);
              } else {
                scope.$on('sextantThemeLoaded', function (evt) {
                  processThemes(facets);
                });
              }
            }
          });

          scope.$on('categorytree:changeda', function(v) {
            scope.triggerSearch();
          });

          /**
         * Recursive method to put in output all selected
         * thesaurus id from the tree
         * @param {object} node
         * @param {Array} output
         */
          var getNodesSelected = function(node, output) {
            if (node.selected) {
              if (node.nodes) {
                for (var i = 0; i < node.nodes.length; i++) {
                  getNodesSelected(node.nodes[i], output);
                }
              }
              else {
                output.push(node.key);
              }
            }
          };

          scope.$on('beforesearch', function(e) {
            var nodesSelected = [];
            getNodesSelected(scope.member, nodesSelected);
            scope.searchObj.params[conf.field] = nodesSelected.join(delimiter);
          });
        }
      };
    }]);

  module.directive('gnCategorytreeCol', [
    function() {
      return {
        restrict: 'E',
        replace: true,
        scope: {
          collection: '='
        },
        controller: ['$scope', function($scope) {
          this.updateParentSelected = function() {
            var m = $scope.$parent.member;
            m.selected = true;
          };
        }],
        template: "<ul class='collapsed'><gn-categorytree-elt ng-repeat='" +
            "member in collection' member='member'></gn-categorytree-elt></ul>"
      };
    }]);
  module.directive('gnCategorytreeElt', [
    '$compile',
    function($compile) {
      return {
        restrict: 'E',
        replace: true,
        scope: {
          member: '='
        },
        require: ['^gnCategorytreeCol','^sxtCategoryTree'],
        templateUrl: '../../catalog/views/sextant/directives/' +
            'partials/categorytreeitem.html',
        link: function(scope, element, attrs, controllers) {
          var el = element;

          scope.toggleNode = function(evt) {
            el.find('.fa').first().toggleClass('fa-minus-square')
                .toggleClass('fa-plus-square');
            el.children('ul').toggle();
            !evt || evt.preventDefault();
            return false;
          };

          /**
           * If a checkbox is set to true, then set its prent
           * to true too (recursive)
           */
          scope.$watch('member.selected', function(val, old) {
            if (angular.isDefined(old)) {
              if (scope.member.selected) {
                controllers[0].updateParentSelected();
              }
              scope.$emit('categorytree:changed');
            }
          });

          /**
           * If a root node is changed, then propagated the selection
           * to all its children recursively
           * @param {Object} member
           * @param {boolean} selected
           */
          scope.selectChildren = function(member, selected) {
            if (angular.isDefined(member.nodes)) {
              for (var i = 0; i < member.nodes.length; i++) {
                member.nodes[i].selected = selected;
                scope.selectChildren(member.nodes[i], selected);
              }
            }
          };

          /**
           * If a checkbox is set to true, then set its prent
           * to true too (recursive)
           */
          scope.$on('beforeSearchReset', function() {
            scope.member.selected = false;
          });

          /**
           * Check if it is a root node
           * @return {*|boolean}
           */
          scope.isParentNode = function() {
            return angular.isDefined(scope.member.nodes);
          };

          scope.onClick = function() {
            controllers[1].updateSearch();
          }

          if (angular.isArray(scope.member.nodes)) {
            element.append("<gn-categorytree-col class='list-group' " +
                "collection='member.nodes' map='map'></gn-categorytree-col>");
            $compile(element.contents())(scope);

          }

          if(scope.member.selected && scope.isParentNode()) {
            el.children('ul').toggle();
            scope.selectedOnInit = true;
          }


        }
      };
    }]);

})();
