(function() {
  goog.provide('sxt_layertree');

  var module = angular.module('sxt_layertree', []);

  // Contains all layers that come from wps service
  var wpsLayers = [];

  module.directive('sxtLayertree', [
    'gnLayerFilters',
    '$filter',
    'gnWmsQueue',
    '$timeout',
    'gnViewerSettings',
    'sxtOgcLinksService',
    function (gnLayerFilters, $filter, gnWmsQueue, $timeout, gnViewerSettings,
              sxtOgcLinksService) {
      return {
        restrict: 'A',
        templateUrl: '../../catalog/views/sextant/directives/' +
            'partials/layertree.html',
        controller: [ '$scope', '$compile', function($scope, $compile) {
          var $this = this;
          this.user = $scope.user;

          this.setWPS = function(wpsLink, layer, wfsLink) {
            $scope.loadTool('wps', layer);

            wpsLink.layer = layer;
            var el = $('.sxt-wps-panel');
            el.empty();
            sxtOgcLinksService.wpsForm($scope.$new(), el, wpsLink, wfsLink)
          };

          /**
           * $compile the wfsFilter directive to build the facet form
           */
          this.setWFSFilter = function(layer, wfsLink) {
            $scope.loadTool('wfsfilter', layer);

            if (!layer.get('wfsfilter-el')) {
              var scope = $scope.$new();
              scope.layer = layer;
              var url = wfsLink.url;
              var featureTypeName = wfsLink.name;

              var el = angular.element('<div ng-show="active.WFSFILTER == layer && layer.visible" data-gn-wfs-filter-facets="" data-layer="layer" data-wfs-url="'+url+'" data-feature-type-name="'+featureTypeName+'"></div><!--<div data-gn-data-table="layer.get(\'solrQ\')" data-gn-data-table-solr-type="WfsFilter" data-gn-data-table-solr-name="facets" data-exclude-cols="excludeCols"></div>-->');
              $compile(el)(scope);
              var element = $('.sxt-wfsfilter-panel');
              element.append(el);
              layer.set('wfsfilter-el', el);
            }
          },

          this.setNCWMS = function(layer) {
            $scope.loadTool('ncwms', layer);
          };

          this.comboGroups = {};
          this.switchGroupCombo = function(groupcombo, layer) {
            var activeLayer = this.comboGroups[groupcombo];
            var fLayers = $filter('filter')($scope.layers,
                $scope.layerFilterFn);
            for (var i = 0; i < fLayers.length; i++) {
              var l = fLayers[i];
              if(l.get('groupcombo') == groupcombo && activeLayer != l) {
                l.visible = false;
              }
            }
          };

          $scope.setActiveComboGroup = function(l) {
            $this.comboGroups[l.get('groupcombo')] = l;
          };
          this.addToPanier = function(md, link) {
            $scope.resultviewFns.addMdLayerToPanier(link, md);
          }
        }],
        link: function(scope, element, attrs) {

          scope.layers = scope.map.getLayers().getArray();
          scope.layerFilterFn = gnLayerFilters.selected;

          scope.displayFilter = gnViewerSettings.layerFilter;

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

          var sortNodeFn = function(a, b) {
            var aName = a.name || a.get('label');
            var bName = b.name || b.get('label');
            return aName < bName ? -1 : 1;
          };

          var addWpsLayers = function(layer, nodes) {
            var nodeIdx = nodes.indexOf(layer);
            wpsLayers.forEach(function(l) {
              if (l.get('wpsParent') == layer) {
                nodes.splice(nodeIdx+1, 0, l);
              }
            });
          };

          /**
           * Look for the loading layer in the tree and swap it with the loaded
           * one.
           *
           * @param {Object} node to inspect, resursive.
           * @param {ol.Layer.image} loadingLayer Layer to switch
           * @param {ol.layer.BaseLayer} layer Final layer to put in the tree.
           * @returns {*}
           */
          var switchLoadingLayer = function(node, loadingLayer, layer) {
            if (node && node.nodes) {
              for (var i = 0; i < node.nodes.length; i++) {
                n = node.nodes[i];
                if (n == loadingLayer) {
                  node.nodes[i] = layer;
                  return true;
                }
                var found = switchLoadingLayer(n, loadingLayer, layer);
                if(found) {
                  return true;
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
                node.nodes.sort(sortNodeFn);
              }
              createNode(layer, newNode, g, index + 1);
            } else {
              if (!node.nodes) node.nodes = [];
              node.nodes.push(layer);
              node.nodes.sort(sortNodeFn);
              addWpsLayers(layer, node.nodes);
            }
          };

          // on OWS Context loading, we don't want to build the tree on each
          // layer remove or add. The delay also helps to get layer properties
          // (i.e 'group') that are set after layer is added to map.
          var debounce = 0;

          // Build the layer manager tree depending on layer groups
          var buildTree = function() {
            if(debounce > 0) {
              return;
            }

            // Remove active popovers
            $('[sxt-layertree] .dropdown-toggle').each(function(i, button) {
              $(button).popover('hide');
            });

            debounce++;
            $timeout(function() {
              scope.layerTree = {
                nodes: []
              };
              var sep = '/';
              var fLayers = $filter('filter')(scope.layers,
                  scope.layerFilterFn);

              wpsLayers = scope.layers.filter(function(l) {
                return !!l.get('fromWps');
              });

              if(scope.layerFilter.q.length > 1) {
                fLayers = $filter('filter')(fLayers, filterFn);
              }

              for (var i = 0; i < fLayers.length; i++) {
                var l = fLayers[i];
                var groups = l.get('group');
                if (!groups) {
                  scope.layerTree.nodes.push(l);
                  scope.layerTree.nodes.sort(sortNodeFn);
                }
                else {
                  if (groups[0] != '/') {
                    groups = '/' + groups;
                  }
                  var g = groups.split(sep);
                  createNode(l, scope.layerTree, g, 1);
                }
                if(l.visible && l.get('groupcombo')) {
                  scope.setActiveComboGroup(l);
                }
              }
              for (var i = 0; i < fLayers.length; i++) {
                var l = fLayers[i];
                var groups = l.get('group');
                if (!groups) {
                  addWpsLayers(l, scope.layerTree.nodes);
                }
              }
              debounce--;
            }, 100);
          };

          scope.map.getLayers().on('change:length', buildTree);

          // Swap the loaded layer with the loading layer after tree is created
          scope.map.getLayers().on('remove', function(event) {
            var loadingLayer = event.element;
            var idx = loadingLayer.get('index');
            var col = event.target;
            if(loadingLayer.get('loading') && angular.isDefined(idx)) {
              var layer = col.item(idx);
              switchLoadingLayer(scope.layerTree, loadingLayer, layer);
            }
          });

           scope.$on('owsContextReseted', function() {
             gnWmsQueue.errors.length = 0;
          });

          scope.failedLayers = gnWmsQueue.errors;
          scope.removeFailed = function(layer) {
            gnWmsQueue.removeFromError(layer);
          };

          scope.layerFilter = {
            q: ''
          };
          var filterFn = function(layer) {
            var labelLc = layer.get('label') && layer.get('label').
                toLowerCase();
            var groupLc = layer.get('group') && layer.get('group').
                toLowerCase();
            var filterLc = scope.layerFilter.q.toLowerCase();
            return (labelLc && labelLc.indexOf(filterLc)) >= 0 ||
                (groupLc && groupLc.indexOf(filterLc) >= 0);
          };

          scope.filterLayers = function() {
            if(scope.layerFilter.q == '' || scope.layerFilter.q.length > 2) {
              buildTree();
            }
          };

          scope.filterClear = function() {
            scope.layerFilter.q = '';
            scope.filterLayers();
          };

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
    '$compile', '$http', 'gnMap', 'gnMdView', 'gnIndexWfsFilterConfig',
    function($compile, $http, gnMap, gnMdView, gnIndexWfsFilterConfig) {
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
            el.find('.fa').first().toggleClass('fa-minus-square')
                .toggleClass('fa-plus-square');
            el.children('ul').toggle();
            evt.stopPropagation();
            return false;
          };
          scope.isParentNode = function() {
            return angular.isDefined(scope.member.nodes);
          };

          scope.indexWFSFeatures = function(url, type) {
            $http.get('wfs.harvest?' +
                'uuid=' + '' +
                '&url=' + encodeURIComponent(url) +
                '&typename=' + encodeURIComponent(type))
              .success(function(data) {
                console.log(data);
              }).error(function(response) {
              console.log(response);
            });
            //$http.get('wfs.harvest/' + md['geonet:info'].uuid)
            // .success(function(data) {
            //  console.log(data);
            //}).error(function(response) {
            //  console.log(response);
            //});
          };
          scope.mapService = gnMap;

          scope.setNCWMS = controller.setNCWMS;

          scope.remove = function() {
            wpsLayers.forEach(function(l) {
              if (l.get('wpsParent') == scope.member) {
                scope.map.removeLayer(l);
              }
            });

            scope.map.removeLayer(scope.member);
          };

          if(!scope.isParentNode()) {
            scope.groupCombo = scope.member.get('groupcombo');
            scope.comboGroups = controller.comboGroups;
            scope.switchGroupCombo = controller.switchGroupCombo;

            /**
             * On top of `switchGroupCombo` that set visible to false to all
             * layers exept the clicked one, here we set the clicked layer
             * visibility.
             *
             * @param {string} groupcombo Group id.
             * @param {ol.layer.Base} layer Node layer.
             */
            scope.setLayerVisibility = function(groupcombo, layer) {
              if(scope.comboGroups[groupcombo] == layer) {
                layer.visible = !layer.visible;
              }
              else {
                scope.comboGroups[groupcombo].visible = true;
              }
            };

            if (scope.member.get('md')) {
              var d =  scope.member.get('downloads');
              var downloadable =
                scope.member.get('md')['geonet:info'].download == 'true';
              if(angular.isArray(d) && downloadable) {
                scope.download = d[0];
              }

              var wfsLink =  scope.member.get('wfs');
              if(angular.isArray(wfsLink) && downloadable) {
                wfsLink = wfsLink[0];
              }
              else {
                wfsLink = undefined;
              }
              scope.user = controller.user;

              if(wfsLink) {
                scope.wfsLink = wfsLink;
                $http.get(gnIndexWfsFilterConfig.url + '/query',  {
                  params: {
                    rows: 1,
                    q: gnIndexWfsFilterConfig.docTypeIdField + ':"' +
                    gnIndexWfsFilterConfig.idDoc({
                      wfsUrl: wfsLink.url,
                      featureTypeName: wfsLink.name
                    }) + '"',
                    wt: 'json'
                  }}).then(function(data) {
                  if(data.response && data.response.numFound > 0) {
                    scope.wfs = wfsLink;
                  }
                });
              }

              var processable =
                scope.member.get('md')['geonet:info'].process == 'true';
              var p = scope.member.get('processes');
              if(angular.isArray(p) && processable) {
                scope.process = p;
              }
            }
          }

          scope.addToPanier = function(download) {
            controller.addToPanier(scope.member.get('md'), download);
          };

          scope.showMetadata = function() {
            gnMdView.openMdFromLayer(scope.member);
          };

          scope.showWPS = function(process) {
            controller.setWPS(process, scope.member, wfsLink);
            $('[sxt-layertree] .dropdown-toggle').each(function(i, button) {
              $(button).popover('hide');
            });
          };

          scope.showWFSFilter = function() {
            controller.setWFSFilter(scope.member, wfsLink);
          };
       }
      };
    }]);

})();
