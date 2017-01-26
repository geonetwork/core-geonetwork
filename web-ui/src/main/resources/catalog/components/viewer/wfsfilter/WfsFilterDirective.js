/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

(function() {
  goog.provide('gn_wfsfilter_directive');


  var TMP_PROFILE =
  { "extendOnly": true,
    "fields":[
/*
      {
      "name":"WATER_KM",
      "aggs": {

        "histogram": {
          "interval": 5000,
          "extended_bounds" : {
            "min" : 2000,
            "max" : 100000
          }
        }
        ,"range" : {
          "ranges" : [
            { "to" : 7500 },
            { "from" : 7500, "to" : 50000 },
            { "from" : 50000 }
          ]
        }
      }
    },
      {
      "name": "P_SEXE",
      "aggs": {
        "filters": {
          "filters": {
            "48 - 50": {"query_string": {"query": "+ft_P_MALE_d:>0.48 +ft_P_FEMALE_d:<0.52"}},
            "49 - 53": {"query_string": {"query": "+ft_P_MALE_d:>0.48 +ft_P_FEMALE_d:<0.53"}}
          }
        }
      }
    },
      {
      "name": "CARPOOL"
    }

*/
      ],
    "treeFields": ["CD_REGION"],
    "tokenizedFields": {
      "CGENELIN": "-"
    }
  };

  var module = angular.module('gn_wfsfilter_directive', [
  ]);

  /**
   * @ngdoc directive
   * @name gn_wfsfilter.directive:gnWfsFilterFacets
   *
   * @description
   */
  module.directive('gnWfsFilterFacets', [
    '$http',
    'wfsFilterService',
    '$q',
    '$rootScope',
    'gnSolrRequestManager',
    'gnSolrService',
    'ngeoDebounce',
    'gnFeaturesTableManager',
    function($http, wfsFilterService, $q, $rootScope,
             gnSolrRequestManager, gnSolrService, ngeoDebounce,
             gnFeaturesTableManager) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/viewer/wfsfilter/' +
            'partials/wfsfilterfacet.html',
        scope: {
          featureTypeName: '@',
          wfsUrl: '@',
          displayCount: '@',
          layer: '=',
          heatmapConfig: '@'
        },
        controller: function(){},
        link: function(scope, element, attrs, ctrl) {

          var solrUrl, uuid, ftName, appProfile, appProfilePromise;
          scope.map = scope.$parent.map;
          var map = scope.map;

          // Only admin can index the features
          scope.user = $rootScope.user;

          // Display or not the results count
          scope.showCount = angular.isDefined(attrs['showcount']);

          scope.heatmapConfig = angular.fromJson(scope.heatmapConfig);

          scope.ctrl = {
            searchGeometry: undefined
          };

          // Get an instance of solr object
          var solrObject, geometry, extentFilter;

          var heatmapsRequest =
              gnSolrRequestManager.register('WfsFilter', 'heatmaps');
          var defaultHeatmapConfig = {
            radius: 30,
            blur: 55,
            opacity: .7,
            //gradient: ['#0f0', '#ff0', '#f00', '#fff'],
            visible: true
          };
          var hmEventKeys = [];

          scope.isHeatMapVisible = false;
          scope.heatmapLayer = null;
          scope.source = null;
          if (scope.map) {
            scope.source = new ol.source.Vector();
            scope.isHeatMapVisible = true;
            scope.heatmapLayer = new ol.layer.Heatmap(
                angular.extend({
                  source: scope.source,
                  visible: scope.isHeatMapVisible
                }, defaultHeatmapConfig, scope.heatmapConfig));
            scope.map.addLayer(scope.heatmapLayer);

            $('body').append('<div id="heatmap-info" data-content=""' +
                'style="position: absolute; z-index: 100;"/>');
            var info = $('#heatmap-info');
            var displayFeatureInfo = function(pixel) {
              var feature = scope.map.forEachFeatureAtPixel(pixel,
                  function(feature, layer) {
                    if (layer == scope.heatmapLayer) {
                      return feature;
                    }
                  });
              if (feature) {
                var mapTop = scope.map.getTarget().getBoundingClientRect().top;
                info.css({
                  left: pixel[0] + 'px',
                  top: (pixel[1] + mapTop) + 'px'
                });
                info.attr('data-original-title', feature.get('count'))
                    .tooltip('show');
              } else {
                info.tooltip('hide');
              }
            };

            scope.map.on('pointermove', ngeoDebounce(function(evt) {
              if (evt.dragging) {
                info.tooltip('hide');
                return;
              }
              displayFeatureInfo(scope.map.getEventPixel(evt.originalEvent));
            }, 300));
          }

          /**
           * Init the directive when the scope.layer has changed.
           * If the layer is given through the isolate scope object, the init
           * is called only once. Otherwise, the same directive is used for
           * all different feature types.
           */
          function init() {

            angular.extend(scope, {
              fields: [],
              isWfsAvailable: undefined,
              isFeaturesIndexed: false,
              status: null,
              md: scope.layer.get('md'),
              mdUrl: scope.layer.get('url'),
              url: scope.wfsUrl || scope.layer.get('url').replace(/wms/i, 'wfs')
            });

            uuid = scope.md && scope.md.getUuid();
            ftName = scope.featureTypeName ||
                scope.layer.getSource().getParams().LAYERS;

            appProfile = null;
            appProfilePromise = wfsFilterService.getApplicationProfile(uuid,
                ftName,
                scope.wfsUrl ? scope.url : scope.mdUrl,
                // A WFS URL is in the metadata or we're guessing WFS has
                // same URL as WMS
                scope.wfsUrl ? 'WFS' : 'WMS').then(
                function(response) {
                  if (response.status == 200) {
                    appProfile = angular.fromJson(response.data['0']);
                    return appProfile;
                  }
                }).catch(function() {});

            solrObject =
                gnSolrRequestManager.register('WfsFilter',
                    scope.url + '#' + ftName);
            scope.solrObject = solrObject;
            scope.layer.set('solrObject', solrObject);


            scope.checkWFSServerUrl();
            scope.initSolrRequest();

            if (scope.map) {
              resetHeatMap();
              hmEventKeys.push(map.on('moveend', refreshHeatmap));
            }
          };

          /**
           * Check if the provided WFS server url return a response.
           * @return {HttpPromise} promise
           */
          scope.checkWFSServerUrl = function() {
            return $http.get('../../proxy?url=' +
                encodeURIComponent(scope.url))
                .then(function() {
                  scope.isWfsAvailable = true;
                }, function() {
                  scope.isWfsAvailable = false;
                });
          };

          /**
           * Init the solr Request Object, either from meta index or from
           * application profile.
           */
          function loadFields() {

            // If an app profile is defined, then we update s
            // `olrObject.initialParams` with external config
            appProfile = TMP_PROFILE;
            if (appProfile && appProfile.fields) {

              solrObject.indexFields =
                  wfsFilterService.solrMergeApplicationProfile(
                  solrObject.filteredDocTypeFieldsInfo, appProfile);
              solrObject.initBaseParams();
            }
            scope.hmActive = appProfile && appProfile.heatmap || true;

            scope.resetFacets();
          }
          function getDataModelLabel(fieldId) {
            for (var j = 0; j < scope.md.attributeTable.length; j++) {
              if (fieldId ==
                  scope.md.attributeTable[j].name) {
                return scope.md.attributeTable[j].definition;
              }
            }
            return null;
          }

          scope.initSolrRequest = function() {
            var config = {
              wfsUrl: scope.url,
              featureTypeName: ftName
            };
            heatmapsRequest.init(config);
            solrObject.getDocTypeInfo(config).then(function() {
              scope.isFeaturesIndexed = true;
              scope.status = null;
              var docFields = solrObject.filteredDocTypeFieldsInfo;
              scope.countTotal = solrObject.totalCount;

              if (scope.md && scope.md.attributeTable) {
                for (var i = 0; i < docFields.length; i++) {
                  var label = getDataModelLabel(docFields[i].label);
                  if (label) {
                    // TODO: Multilingual
                    docFields[i].label = label;
                  }
                }
              }
              appProfilePromise.then(loadFields);
            }, function(error) {
              scope.status = error.data ? 'indexAccessError' : error.statusText;
              scope.statusTitle = error.statusText;
            });
          };
          scope.dropFeatures = function() {
            return $http.delete(
              '../api/workers/data/wfs/actions?serviceUrl=' +
                encodeURIComponent(scope.url)
              + '&typeName=' + encodeURIComponent(ftName)).then(function() {
              scope.initSolrRequest();
            });
          };
          /**
           * Update the state of the facet search.
           * The `scope.output` structure represent the state of the facet
           * checkboxes form.
           *
           * @param {string} fieldName index field name
           * @param {string} facetKey facet key for this field
           * @param {string} type facet type
           */
          scope.onCheckboxClick = function(field, facet) {

            var fieldName = field.name;
            var facetKey = facet.value;

            var output = scope.output;

            if (output[fieldName]) {
              if (output[fieldName].values[facetKey]) {
                delete output[fieldName].values[facetKey];
                if (Object.keys(output[fieldName].values).length == 0) {
                  delete output[fieldName];
                }
              }
              else {
                output[fieldName].values[facetKey] = true;
              }
            }
            else {
              output[fieldName] = {
                type: field.type,
                query: facet.query,
                values: {}
              };
              output[fieldName].values[facetKey] = true;
            }
            scope.searchInput = '';
            scope.filterFacets();
          };
          ctrl.onCheckboxClick = scope.onCheckboxClick;

          scope.isFacetSelected = function(fName, value) {
            try {
              var iS = scope.output[fName].values[value];
              return iS;
            }
            catch(e) {
              return false;
            }
          };

          scope.onUpdateDate = function(field) {
            var output = scope.output;
            var fieldName = field.name;
            var date = field.model;

            if((angular.isObject(date) && date.from && date.to) ||
              angular.isString(date)) {
              output[fieldName] = {
                type: 'date',
                value: date
              };
            }
            else {
              delete output[fieldName];
            }

            scope.searchInput = '';
            scope.filterFacets();
          };

          /**
           * Send a new filtered request to solr to update the facet ui
           * structure.
           * This method is called each time the user check or uncheck a box
           * from the ui, or when he updates the filter input.
           * @param {boolean} formInput the filter comes from input change
           */
          scope.filterFacets = function(formInput) {

            // Update the facet UI
            var expandedFields = [];
            angular.forEach(scope.fields, function(f) {
              if (f.expanded) {
                expandedFields.push(f.name);
              }
            });

            solrObject.searchWithFacets({
              params: scope.output,
              any: scope.searchInput,
              geometry: geometry
            }).
                then(function(resp) {
                  solrObject.pushState();
                  scope.fields = resp.facets;
                  scope.count = resp.count;
                  angular.forEach(scope.fields, function(f) {
                    if (expandedFields.indexOf(f.name) >= 0) {
                      f.expanded = true;
                    }
                  });
                  refreshHeatmap();
                });
          };

          function refreshHeatmap() {
            return;
            if (scope.isFeaturesIndexed && scope.isHeatMapVisible) {
              heatmapsRequest.searchWithFacets({
                params: scope.output,
                any: scope.searchInput
              },
              gnSolrService.getHeatmapParams(scope.map)).
                  then(function(resp) {
                    scope.heatmaps = resp.aggs;
                    // resp.solrData.facet_counts.facet_heatmaps;
                  });
            }
          }


          scope.getMore = function(field) {
            solrObject.getFacetMoreResults(field).then(function(response) {
              field.values = response.facets[0].values;
            });
          };

          /**
           * reset and init the facet structure.
           * call the solr service to get info on all facet fields and bind it
           * to the output structure to generate the ui.
           */
          scope.resetFacets = function() {

            // output structure to send to filter service
            scope.output = {};
            scope.searchInput = '';
            scope.resetSLDFilters();

            var boxElt = element.find('.gn-bbox-input');
            if (boxElt.length) {
              angular.element(boxElt).scope().clear();
            }

            scope.layer.set('esConfig', null);

            // load all facet and fill ui structure for the list
            return solrObject.searchWithFacets({}).
                then(function(resp) {
                  solrObject.pushState();
                  scope.fields = resp.facets;
                  scope.count = resp.count;
                  refreshHeatmap();
                });

          };

          scope.resetSLDFilters = function() {
            scope.layer.getSource().updateParams({
              SLD: null
            });
            scope.layer.setExtent();
          };

          /**
           * On filter click, build from the UI the SLD rules config object
           * that will be send to generateSLD service.
           */
          scope.filterWMS = function() {
            var defer = $q.defer();
            var sldConfig = wfsFilterService.createSLDConfig(scope.output);
            var layer = scope.layer;

            solrObject.pushState();
            layer.set('esConfig', solrObject.getState());
            if (!extentFilter) {
              layer.setExtent();
            }
            else {
              layer.setExtent(
                  ol.proj.transformExtent(extentFilter, 'EPSG:4326',
                      scope.map.getView().getProjection()));

            }
            if (sldConfig.filters.length > 0) {
              wfsFilterService.getSldUrl(sldConfig, layer.get('url'),
                  ftName).success(function(sldURL) {
                // Do not activate it
                // Usually return 414 Request-URI Too Large
                var useSldBody = false;
                if (useSldBody) {
                  $http.get(sldURL).then(function(response) {
                    layer.getSource().updateParams({
                      SLD_BODY: response.data
                    });
                  });
                } else {
                  layer.getSource().updateParams({
                    SLD: sldURL
                  });
                }
              }).finally(function() {
                defer.resolve();
              });
            } else {
              layer.getSource().updateParams({
                SLD: null
              });
              defer.resolve();
            }
            return defer.promise;
          };

          /**
           * Trigger the SOLR indexation of the feature type.
           * Only available for administrators.
           */
          scope.indexWFSFeatures = function(version) {
            appProfilePromise.then(function() {
              wfsFilterService.indexWFSFeatures(
                  scope.url,
                  ftName,
                  appProfile ? appProfile.tokenizedFields : null,
                  appProfile ? appProfile.treeFields : null,
                  uuid,
                  version);
            });
          };

          /**
           * Clear the search input
           */
          scope.clearInput = function() {
            scope.searchInput = '';
            scope.filterFacets();
          };
          scope.searchInput = '';

          // Init the directive
          if (scope.layer) {
            init();
          }
          else {
            scope.$watch('layer', function(n, o) {
              if (n === null && scope.map) {
                resetHeatMap();
              } else if (n !== o) {
                init();
              }
            });
          }

          //Manage geographic search
          scope.$watch('ctrl.searchGeometry', function(geom, old) {
            extentFilter = undefined;
            if (geom && geom != ',,,') {
              extentFilter = geom.split(',').map(function(val) {
                return parseFloat(val);
              });
              geometry = [
                [extentFilter[0], extentFilter[3]],
                [extentFilter[2], extentFilter[1]]
              ];
              scope.filterFacets();
            }
            // when reset from gnBbox directive
            else if (old && geom != '') {
              scope.filterFacets();
            }
            // do nothing when reset from wfsFilter directive
          });

          function resetHeatMap() {
            if (scope.source) {
              scope.source.clear();
            }
            while (hmEventKeys.length) {
              map.unByKey(hmEventKeys.pop());
            }
          }

          scope.$watch('isHeatMapVisible', function(n, o) {
            if (n != o) {
              refreshHeatmap();
              scope.heatmapLayer.setVisible(n);
            }
          });

          // Update heatmap layers from Solr response
          scope.$watch('heatmaps', function(n, o) {
            if (n != o) {
              // TODO: May contains multiple heatmaps
              if (angular.isArray(n.geom)) {
                scope.source.clear();
                scope.source.addFeatures(
                    gnSolrService.heatmapToFeatures(
                    n.geom,
                    scope.map.getView().getProjection())
                );
              }
            }
          });

          scope.showTable = function() {
            gnFeaturesTableManager.clear();
            gnFeaturesTableManager.addTable({
              name: scope.layer.get('label') || scope.layer.get('name'),
              type: 'solr'
            }, {
              map: scope.map,
              solrObject: solrObject,
              layer: scope.layer
            });
          };

          element.on('$destroy', function() {
            scope.$destroy();
            resetHeatMap();
          });


          scope.toSqlOgr = function() {
            solrObject.pushState();
            var state = solrObject.getState();

            if(!state.any) {
              var where = [];
              angular.forEach(state.qParams, function(fObj, fName) {
                var clause = [];
                angular.forEach(fObj.values, function(v, k) {
                  clause.push(fName + '=' + k);
                });
                where.push('(' + clause.join(' OR ') + ')');
              });
              console.log(where.join(' AND '));
            }
            else {
              solrObject.search_es({
                size: scope.count || 10000,
                aggs: {}
              }).then(function(data) {
                var where = data.hits.hits.map(function(res) {
                  return res._id;
                });
                console.log(where.join(' OR '));
              });
            }
          };
        }
      };
    }]);

  /**
   * @ngdoc directive
   * @name gn_wfsfilter.directive:gnWfsFilterFacetsTree
   *
   * @description
   * The global markup for the facet tree. Each node of the tree is a
   * sub directive `gnWfsFilterFacetsTreeItem`.
   *
   * This directive is used to be the controller of all the tree.
   */
  module.directive('gnWfsFilterFacetsTree', [
    function() {
      return {
        restrict: 'A',
        scope: {
          field: '<gnWfsFilterFacetsTree',
          isSelectedFn: '&gnWfsFilterFacetsTreeIsselected'
        },
        require: {
          wfsFilterCrl: '^^gnWfsFilterFacets'
        },
        template: '<div gn-wfs-filter-facets-tree-item="treeCtrl.field.tree"></div>',
        bindToController: true,
        controllerAs: 'treeCtrl',
        controller: function() {
          this.$onInit = function() {
            this.onCheckboxTreeClick = function(value) {

              this.wfsFilterCrl.onCheckboxClick(this.field, {
                value: value
              });
            };
            this.isSelected = function(value) {
              return this.isSelectedFn({
                name: this.field.name,
                value: value
              });
            }
          };
        }
      };
    }]);

  /**
   * @ngdoc directive
   * @name gn_wfsfilter.directive:gnWfsFilterFacetsTreeItem
   *
   * @description
   * The tree node structure of the wfs filter facet tree.
   */
  module.directive('gnWfsFilterFacetsTreeItem', [
    function() {
      return {
        restrict: 'A',
        templateUrl: '../../catalog/components/viewer/wfsfilter/' +
        'partials/wfsfilterfacetTreeItem.html',
        scope: {
          node: '<gnWfsFilterFacetsTreeItem'
        },
        require: {
          treeCtrl: '^^gnWfsFilterFacetsTree'
        },
        bindToController: true,
        controllerAs: 'ctrl',
        controller: ['$attrs', function($attrs) {
          this.isRoot = $attrs['gnWfsFilterFacetsTreeItemNotroot']
            === undefined;

          this.$onInit = function() {

            this.onCheckboxTreeClick = function() {
              this.treeCtrl.onCheckboxTreeClick(this.node.key);
            };
            this.isSelected = function() {
              return this.treeCtrl.isSelected(this.node.key);
            }
          };
        }],
        link: function (scope, el, attrs, ctrls) {
          scope.toggleNode = function(evt) {
            el.find('.fa').first().toggleClass('fa-minus-square')
              .toggleClass('fa-plus-square');
            el.children('.list-group').toggle();
            !evt || evt.preventDefault();
            evt.stopPropagation();
            return false;
          };
        }
      };
    }]);

})();
