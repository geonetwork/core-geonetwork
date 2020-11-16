(function() {
  goog.provide('sxt_layertree');

  goog.require('sxt_compositeLayer');


  var module = angular.module('sxt_layertree', [
    'sxt_compositeLayer'
  ]);

  // Contains all layers that come from wps service
  var wpsLayers = [];

  module.directive('sxtLayertree', [
    'gnLayerFilters',
    '$filter',
    'gnWmsQueue',
    '$timeout',
    'gnViewerSettings',
    function (gnLayerFilters, $filter, gnWmsQueue, $timeout, gnViewerSettings) {
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
            wpsLink.label = wpsLink.desc;
            $scope.wpsLink = wpsLink;
            $scope.wfsLink = wfsLink;
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

              var el = angular.element('<div ng-show="active.WFSFILTER == layer && layer.visible" data-gn-wfs-filter-facets="" data-layer="layer" data-wfs-url="'+url+'" data-feature-type-name="'+featureTypeName+'" data-mode="group"></div><!--<div data-gn-data-table="layer.get(\'solrQ\')" data-gn-data-table-solr-type="WfsFilter" data-gn-data-table-solr-name="facets" data-exclude-cols="excludeCols"></div>-->');
              $compile(el)(scope);
              var element = $('.sxt-wfsfilter-panel');
              element.append(el);
              layer.set('wfsfilter-el', el);
            }
          };

          this.setNCWMS = function(layer) {
            $scope.loadTool('ncwms', layer);
          };

          this.setAnnotations = function(layer) {
            $scope.loadTool('annotations', layer);
          }

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
            var aPos = undefined;
            if (a.groupPosition !== undefined) aPos = a.groupPosition;
            else if (a.get && a.get('owc_position') !== undefined) aPos = a.get('owc_position');

            var bPos = undefined;
            if (b.groupPosition !== undefined) bPos = b.groupPosition;
            else if (b.get && b.get('owc_position') !== undefined) bPos = b.get('owc_position');
            var aName = a.name || a.get('label');
            var bName = b.name || b.get('label');

            // comparing using positions (for groups or layers)
            // if one is positioned and not the other, put it first
            if (aPos !== undefined && bPos === undefined) {
              return -1;
            } else if (aPos === undefined && bPos !== undefined) {
              return 1;
            }

            // if both are positioned, compare based on that
            if (aPos !== undefined && bPos !== undefined) {
              return aPos < bPos ? -1 : (aPos > bPos) ? 1 : 0;
            }

            // other comparisons: based on label only
            return aName.localeCompare(bName);
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

          var createNode = function(layer, node, g, index, initGroupAsFolded) {
            var group = g[index];
            var groupPosition;
            if (group) {
              var newNode = findChild(node, group);
              if (!newNode) {
                // init group state
                groupPath = g.slice(0, index + 1).join('\\');
                var state = scope.groupStates[groupPath];
                if (!state) {
                  scope.groupStates[groupPath] = {
                    folded: initGroupAsFolded ? true : false
                  };
                  state = scope.groupStates[groupPath];
                }

                // save the state & path on the new node
                // (path is used to track nodes uniquely)
                // note: group position is set by the layer's `groupPosition` prop
                if (layer.get('owc_groupPosition') != undefined) {
                  groupPosition = layer.get('owc_groupPosition').toString();
                }

                newNode = {
                  name: group,
                  state: state,
                  path: groupPath,
                  groupPosition: groupPosition && parseInt(groupPosition.split('/')[index])
                };
                if (!node.nodes) node.nodes = [];
                node.nodes.push(newNode);
                node.nodes.sort(sortNodeFn);
              }
              createNode(layer, newNode, g, index + 1, initGroupAsFolded);
            } else {
              if (!node.nodes) node.nodes = [];
              node.nodes.push(layer);
              node.nodes.sort(sortNodeFn);
              addWpsLayers(layer, node.nodes);

              // save the group position on the layer
              // (for context consistency, i.e. if layer order is changed)
              layer.set('owc_groupPosition', groupPosition);
            }
          };

          // on OWS Context loading, we don't want to build the tree on each
          // layer remove or add. The delay also helps to get layer properties
          // (i.e 'group') that are set after layer is added to map.
          var debounce = 0;

          // this holds a state of the layer collection labels
          // keys are the path of the collection, ie: '\group A\subgroup 1'
          // values are objects with property 'folded' (bool)
          scope.groupStates = {};

          // layers loaded for the first time are always from a context
          scope.layersFromContext = true;

          // Build the layer manager tree depending on layer groups
          var buildTree = function() {
            if(debounce > 0) {
              return;
            }

            // are we loading the default context? if yes, should we load the
            // groups as folded? (check in API settings)
            var initGroupsAsFolded = false;
            if (typeof sxtSettings !== 'undefined' &&
              sxtSettings.loadContextFolded && scope.layersFromContext) {
              initGroupsAsFolded = true;
            }

            // do not fold layer tree for next rebuild
            scope.layersFromContext = false;

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
                  createNode(l, scope.layerTree, g, 1, initGroupsAsFolded);
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

              // adding WPS layers w/o parent layer to the root
              scope.layers.filter(function(l) {
                if(l.get('fromWps') && !l.get('wpsParent')) {
                  scope.layerTree.nodes.push(l);
                }
              });

              debounce--;
            }, 100);
          };

          scope.map.getLayers().on(['propertychange','change:length'], buildTree);

          // initial tree build
          buildTree();

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

          scope.$on('owsContextLoaded', function() {
            gnWmsQueue.errors.length = 0;

            // when a context is loaded, mark it for the next tree
            // rebuild & clear tree group states
            scope.layersFromContext = true;
            scope.groupStates = {};
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
        template:
          '<ul class="sxt-layertree-node">' +
            '<sxt-layertree-elt ' +
              'ng-repeat="member in collection track by tracker(member, $index)" ' +
              'member="member" map="map"></sxt-layertree-elt>' +
          '</ul>',
        link: function(scope) {
          // this function computes a unique id for the node
          scope.tracker = function(member, index) {
            // the node is a group header: return path
            if (member.path) {return member.path + index; }
            // the node is an ol layer: use url & name & ol_uid
            return member.get('name') + '@' + member.get('url') + '-' + (member.ol_uid || index);
          };
        }
      };
    }]);

  module.directive('sxtLayertreeElt', [
    '$compile',
    '$http',
    'gnMap',
    'gnMdView',
    'wfsFilterService',
    'sxtCompositeLayer',
    function($compile, $http, gnMap, gnMdView, wfsFilterService, sxtCompositeLayer) {
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
          scope.toggleNode = function(evt) {
            scope.member.state.folded = !scope.member.state.folded;
            evt && evt.stopPropagation();
            return false;
          };
          scope.isParentNode = function() {
            return angular.isDefined(scope.member.nodes);
          };
          scope.isFolded = function () {
            return scope.member.state && scope.member.state.folded;
          };
          scope.toggleChildrenNodes = function (event, e, forceVisible) {
            if (event && $(event.currentTarget).hasClass('inactiveLayerNode')) {return;}
            var visible = forceVisible !== undefined ? forceVisible : !scope.hasCheckedChildren(e);
            e.nodes.forEach(function(n) {
              if (n instanceof ol.layer.Base) {
                n.set('visible', visible);
              }
              else {
                scope.toggleChildrenNodes(null, n, visible);
              }
            });
          };

          scope.hasCheckedChildren = function (e) {
            var hasChecked = false;
            for (var n = 0 ; n < e.nodes.length; n++) {
              if (e.nodes[n] instanceof (ol.layer.Base)) {
                if (e.nodes[n].get('visible') === true) {
                  return true;
                }
              }
              else {
                hasChecked = hasChecked || scope.hasCheckedChildren(e.nodes[n]);
              }
            }
            return hasChecked;
          };

          scope.hasOnlyComboLayers = function (e) {
            var hasCombo = false;
            for (var n = 0 ; n < e.nodes.length; n++) {
              if (e.nodes[n] instanceof (ol.layer.Base)) {
                if (e.nodes[n].get('groupcombo')) {
                  return true;
                }
              } else {
                hasCombo = hasCombo || scope.hasOnlyComboLayers(e.nodes[n]);
              }
            }
            return hasCombo;

          }

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
             * layers except the clicked one, here we set the clicked layer
             * visibility.
             *
             * @param {string} groupcombo Group id.
             * @param {ol.layer.Base} layer Node layer.
             */
            scope.setComboLayerVisibility = function(groupcombo, layer) {
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
                scope.member.get('md').download == true;
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
                var indexObject = wfsFilterService.registerEsObject(wfsLink.url, wfsLink.name);
                indexObject.init({
                  wfsUrl: wfsLink.url,
                  featureTypeName: wfsLink.name
                });
                indexObject.searchWithFacets({}).then(function(data) {
                  if(data.count > 0) {
                    scope.wfs = wfsLink;

                    var appProfile;
                    try {
                      appProfile = wfsLink.applicationProfile ? JSON.parse(wfsLink.applicationProfile) : {};
                    } catch (e) {
                      appProfile = {};
                      console.warn('Erreur lors de la lecture de l\'élément applicationProfile', e);
                    }

                    if (appProfile.compositeLayer && !scope.member.get('compositeInitialized')) {
                      scope.member.set('compositeInitialized', true);
                      var featureType = wfsLink.url + '#' + wfsLink.name;
                      var minHeatmapCount = appProfile.compositeLayer.minHeatmapCount || 1000;
                      var maxTooltipCount = appProfile.compositeLayer.maxTooltipCount || 1000;

                      var tooltipTemplateUrl = appProfile.tooltipTemplateUrl;
                      if (!tooltipTemplateUrl) {
                        sxtCompositeLayer.init(scope.member, scope.map, featureType, minHeatmapCount, maxTooltipCount, undefined);
                      } else {
                        $http.get(tooltipTemplateUrl).then(function (response) {
                          var tooltipTemplate = response.data;
                          sxtCompositeLayer.init(scope.member, scope.map, featureType, minHeatmapCount, maxTooltipCount, tooltipTemplate);
                        }, function () {
                          console.warn('Le chargement du template de tooltip a échoué');
                        });
                      }
                    }
                  }
                });
              }

              var processable =
                scope.member.get('md').process == 'true';
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

          scope.showAnnotations = function() {
            controller.setAnnotations(scope.member);
          };

          scope.isOutOfRange = function () {
            if (scope.isParentNode() ||
              !scope.member instanceof ol.layer.Base) {
              return false;
            }
            var mapRes = scope.map.getView().getResolution();
            return scope.member.getMaxResolution() < mapRes ||
              scope.member.getMinResolution() > mapRes;
          }
        }
      };
    }]);

})();
