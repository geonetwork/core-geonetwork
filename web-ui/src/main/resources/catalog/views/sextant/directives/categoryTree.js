(function() {
  goog.provide('sxt_categorytree');

  var module = angular.module('sxt_categorytree', [
  ]);

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
  var createNode = function(node, g, index) {
    var group = g[index];
    if (group) {
      var newNode = findChild(node, group);
      if (!newNode) {
        newNode = {
          name: group,
          selected: false
        };
        if (!node.nodes) node.nodes = [];
        node.nodes.push(newNode);
      }
      createNode(newNode, g, index + 1);
    } else {
      node.key = g.join('/');
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
            var labels = [];
            scope.member = {
              nodes: []
            };

            angular.forEach(ts, function(t) {
              if (conf.labelFromThesaurus) {
                t = findLabel(t);
              }
              if (t) {
                if (conf.tree) {
                  var g = t.split('/');
                  createNode(scope.member, g, 1);
                }
                else {
                  scope.member.nodes.push({
                    name: t,
                    key: t,
                    selected: false
                  });
                }
              }
            });
          };

          /**
         * Reload thesaurus depending on the Catalog field (_groupPublished)
         */
          scope.$watch('searchObj.params._groupPublished', function(val, old) {
            gnHttp.callService('suggest', {
              field: conf.field,
              threshold: 1,
              origin: 'RECORDS_FIELD_VALUES',
              groupPublished: val
            }).success(function(data) {
              if (angular.isArray(sxtGlobals.sextantTheme)) {
                processThemes(data[1]);
              } else {
                scope.$on('sextantThemeLoaded', function(evt) {
                  processThemes(data[1]);
                });
              }
            }).error(function(){
              scope.member = {
                nodes: []
              };
              console.warn('Could not load thesaurus: ' + conf.id);

            });
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
            scope.searchObj.params[conf.field] = nodesSelected.join(' or ');
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
        require: '^gnCategorytreeCol',
        templateUrl: '../../catalog/views/sextant/directives/' +
            'partials/categorytreeitem.html',
        link: function(scope, element, attrs, controller) {
          var el = element;
          if (angular.isArray(scope.member.nodes)) {
            element.append("<gn-categorytree-col class='list-grou' " +
                "collection='member.nodes' map='map'></gn-categorytree-col>");
            $compile(element.contents())(scope);
          }
          scope.toggleNode = function(evt) {
            el.find('.fa').first().toggleClass('fa-minus-square-o')
                .toggleClass('fa-plus-square-o');
            el.children('ul').toggle();
            evt.preventDefault();
            return false;
          };


          /**
           * If a checkbox is set to true, then set its prent
           * to true too (recursive)
           */
          scope.$watch('member.selected', function(val, old) {
            if (angular.isDefined(old)) {
              if (scope.member.selected) {
                controller.updateParentSelected();
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
           * Check if it is a root node
           * @return {*|boolean}
           */
          scope.isParentNode = function() {
            return angular.isDefined(scope.member.nodes);
          };
        }
      };
    }]);

})();
