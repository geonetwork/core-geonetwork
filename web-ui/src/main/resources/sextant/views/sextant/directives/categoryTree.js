(function () {
  goog.provide("sxt_categorytree");

  var module = angular.module("sxt_categorytree", []);

  var delimiter = " or ";
  var themesInSearch;

  var findChild = function (node, name) {
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
  var sortNodeFn = function (a, b) {
    var aName = a.name || a.get("label");
    var bName = b.name || b.get("label");
    if (aName < bName) return -1;
    if (aName > bName) return 1;
    return 0;
  };
  var createNode = function (node, g, index, t) {
    var group = g[index];
    if (group) {
      var newNode = findChild(node, group);
      if (!newNode) {
        newNode = {
          name: group,
          selected: themesInSearch.indexOf(t["@name"]) >= 0 ? true : false
        };
        if (!node.nodes) node.nodes = [];
        node.nodes.push(newNode);
        node.nodes.sort(sortNodeFn);
      }
      createNode(newNode, g, index + 1, t);
    } else {
      node.key = t["@name"];
      node.count = t["@count"];
    }
  };

  module.directive("sxtCategoryTree", [
    "sxtGlobals",
    "$filter",
    "gnFacetConfigService",
    function (sxtGlobals, $filter, gnFacetConfigService) {
      return {
        restrict: "A",
        template:
          '<gn-categorytree-col class="list-group" ' +
          'collection="member.nodes"></gn-categorytree-col>',
        controller: [
          "$scope",
          "$timeout",
          function ($scope, $timeout) {
            this.updateSearch = function () {
              $scope.lastClicked = true;
              $timeout($scope.triggerSearch, 100);
            };
          }
        ],
        link: function (scope, element, attr) {
          var facetName = attr["sxtCategoryTree"];
          var facetKey, indexKey, translations;

          /**
           * Find thesaurus translation from its id
           * @param {string} key
           * @return {*}
           */
          var findLabel = function (key) {
            if (translations) {
              for (var i = 0; i < translations.length; i++) {
                var t = translations[i];
                if (t.props.uri == key) {
                  return t.label;
                }
              }
            }
            return key;
          };

          /**
           * Create tree structure from suggest service response
           * @param {Array} ts
           */
          var processThemes = function (ts) {
            scope.member = {
              nodes: []
            };
            themesInSearch = scope.searchObj.params[indexKey]
              ? scope.searchObj.params[indexKey].split(delimiter)
              : [];

            if (themesInSearch) {
              scope.member.selected = true;
            }

            if (scope.ctrl.activeFilter && scope.ctrl.activeFilter.length > 1) {
              ts = $filter("filter")(ts, filterFn);
            }

            angular.forEach(ts, function (t) {
              var key = t["@name"],
                name;
              name = findLabel(key, translations);
              if (name) {
                // make sure the name starts with /
                // see https://forge.ifremer.fr/mantis/view.php?id=45687
                if (name.substring(0, 1) !== "/") {
                  name = "/" + name;
                }
                var g = name.split("/");
                createNode(scope.member, g, 1, t);
              }
            });
          };

          scope.$watch("searchResults.facet", function (v) {
            if (scope.lastClicked) {
              scope.lastClicked = false;
              return;
            }

            if (v) {
              gnFacetConfigService.loadConfig("hits").then(function (facetConfig) {
                if (!facetKey) {
                  facetConfig.some(function (c) {
                    if (c.name == facetName) {
                      facetKey = c.label;
                      indexKey = c.key;
                      // to set in parent directive ...
                      scope.indexKey = indexKey;
                      scope.facetKey = facetKey;
                      return true;
                    }
                  });
                }
                var facets = v[facetKey];

                // keep active facets even when they don't have results anymore
                function alreadyHasValue(key) {
                  for (var i = 0; i < facets.length; i++) {
                    if (facets[i]["@name"] === key) return true;
                  }
                  return false;
                }
                function addActiveValue(node) {
                  if (node.nodes) {
                    for (var i = 0; i < node.nodes.length; i++) {
                      addActiveValue(node.nodes[i]);
                    }
                  } else if (node.selected && node.key && !alreadyHasValue(node.key)) {
                    facets.push({
                      "@name": node.key,
                      "@label": node.key,
                      "@count": 0
                    });
                  }
                }
                scope.member && addActiveValue(scope.member);

                var promise = sxtGlobals.keywords[facetName + "Promise"];
                if (promise) {
                  promise.then(function (keywords) {
                    translations = keywords;
                    processThemes(facets);
                  });
                } else {
                  processThemes(facets);
                }
              });
            }
          });

          /**
           * Recursive method to put in output all selected
           * thesaurus id from the tree
           * @param {object} node
           * @param {Array} output
           */
          var getNodesSelected = function (node, output) {
            if (node.selected) {
              if (node.nodes) {
                for (var i = 0; i < node.nodes.length; i++) {
                  getNodesSelected(node.nodes[i], output);
                }
              } else {
                output.push(node.key);
              }
            }
          };

          /**
           * Used to set tree values in search params after a node click that
           * triggers the search.
           */
          scope.$on("beforesearch", function (e) {
            if (!scope.member) return;
            var nodesSelected = [];
            getNodesSelected(scope.member, nodesSelected);
            scope.searchObj.params[indexKey] = nodesSelected.join(delimiter);
          });

          // Filter input
          scope.$watch("ctrl.activeFilter", function (v) {
            if (angular.isDefined(v)) {
              processThemes(scope.searchResults.facet[facetKey]);
            }
          });
          function filterFn(theme) {
            var name = findLabel(theme["@name"]);
            if (!name) {
              return false;
            }
            name = name.toLowerCase();
            var filter = scope.ctrl.activeFilter && scope.ctrl.activeFilter.toLowerCase();

            return name.indexOf(filter) >= 0;
          }

          scope.$on("beforeSearchReset", function () {
            scope.ctrl.activeFilter = "";
          });
        }
      };
    }
  ]);

  module.directive("gnCategorytreeCol", [
    function () {
      return {
        restrict: "E",
        replace: true,
        scope: {
          collection: "="
        },
        controller: ["$scope", function ($scope) {}],
        template:
          "<ul class='collapsed'><gn-categorytree-elt ng-repeat='" +
          "member in collection' member='member'></gn-categorytree-elt></ul>"
      };
    }
  ]);
  module.directive("gnCategorytreeElt", [
    "$compile",
    function ($compile) {
      return {
        restrict: "E",
        replace: true,
        scope: {
          member: "="
        },
        require: ["^gnCategorytreeCol", "^sxtCategoryTree"],
        templateUrl:
          "../../catalog/views/sextant/directives/" + "partials/categorytreeitem.html",
        link: function (scope, element, attrs, controllers) {
          var el = element;

          scope.toggleNode = function (evt) {
            el.find(".fa")
              .first()
              .toggleClass("fa-minus-square")
              .toggleClass("fa-plus-square");
            el.children("ul").toggle();
            !evt || evt.preventDefault();
            return false;
          };

          scope.selectParent = function () {
            var parent = this.$parent.$parent.$parent;
            if (parent.selectParent) {
              parent.selectParent();
            }
            // select parent and also root node (not a gnCategorytreeElt)
            if (parent.member) {
              parent.member.selected = true;
            }
          };

          /**
           * If a root node is changed, then propagated the selection
           * to all its children recursively
           * @param {Object} member
           * @param {boolean} selected
           * @param {boolean} clicked
           */
          scope.selectChildren = function (member, selected, clicked) {
            if (angular.isDefined(member.nodes)) {
              for (var i = 0; i < member.nodes.length; i++) {
                member.nodes[i].selected = selected;
                scope.selectChildren(member.nodes[i], selected);
              }
            }
            if (clicked) {
              if (selected) {
                scope.selectParent();
              }
              controllers[1].updateSearch();
            }
          };

          /**
           * If a checkbox is set to true, then set its prent
           * to true too (recursive)
           */
          scope.$on("beforeSearchReset", function () {
            scope.member.selected = false;
          });

          /**
           * Check if it is a root node
           * @return {*|boolean}
           */
          scope.isParentNode = function () {
            return angular.isDefined(scope.member.nodes);
          };

          if (angular.isArray(scope.member.nodes)) {
            element.append(
              "<gn-categorytree-col class='list-group' " +
                "collection='member.nodes' map='map'></gn-categorytree-col>"
            );
            $compile(element.contents())(scope);
          }

          if (scope.member.selected && scope.isParentNode()) {
            el.children("ul").toggle();
            scope.selectedOnInit = true;
          }
        }
      };
    }
  ]);

  module.directive("sxtFacetTree", [
    "sxtGlobals",
    "$filter",
    "gnFacetConfigService",
    function (sxtGlobals, $filter, gnFacetConfigService) {
      return {
        restrict: "A",
        scope: true,
        templateUrl:
          "../../catalog/views/sextant/directives/" + "partials/facettree.html",
        link: function (scope, element, attr) {
          scope.ctrl = {};
          scope.title = attr["sxtFacetTreeTitle"];
          scope.hasFilter = attr["sxtFacetTreeFilter"] == "true";

          scope.name = attr.sxtFacetTree;
          scope.contentCollapsed = attr.sxtFacetTreeCollapsed == "true";
        }
      };
    }
  ]);
})();
