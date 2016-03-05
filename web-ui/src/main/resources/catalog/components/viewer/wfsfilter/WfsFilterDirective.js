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

  var module = angular.module('gn_wfsfilter_directive', [
  ]);

  /**
   * @ngdoc directive
   * @name gn_wfsfilter.directive:gnWfsFilterFacets
   *
   * @description
   */
  module.directive('gnWfsFilterFacets', [
    '$http', 'wfsFilterService', '$q', '$rootScope',
    'gnSolrRequestManager', 'gnSolrService',
    function($http, wfsFilterService, $q, $rootScope,
             gnSolrRequestManager, gnSolrService) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/viewer/wfsfilter/' +
            'partials/wfsfilterfacet.html',
        scope: {
          featureTypeName: '@',
          wfsUrl: '@',
          displayCount: '@',
          layer: '='
        },
        link: function(scope, element, attrs) {

          var solrUrl, uuid, ftName, appProfile, appProfilePromise;
          scope.map = scope.$parent.map;

          // Only admin can index the features
          scope.user = $rootScope.user;

          // Display or not the results count
          scope.showCount = angular.isDefined(attrs['showcount']);

          // Get an instance of solr object
          var solrObject =
              gnSolrRequestManager.register('WfsFilter', 'facets');
          var heatmapsRequest =
              gnSolrRequestManager.register('WfsFilter', 'heatmaps');

          scope.isHeatMapVisible = false;
          scope.heatmapLayer = null;
          scope.source = null;
          if (scope.map) {
            scope.source = new ol.source.Vector();
            scope.isHeatMapVisible = true;
            scope.heatmapLayer = new ol.layer.Heatmap({
              source: scope.source,
              radius: 40,
              blur: 50,
              opacity: .8,
              visible: scope.isHeatMapVisible
            });
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
                info.css({
                  left: pixel[0] + 'px',
                  top: (pixel[1] + 50) + 'px'
                });
                info.attr('data-original-title', feature.get('count'))
                    .tooltip('show');
              } else {
                info.tooltip('hide');
              }
            };

            scope.map.on('pointermove', function(evt) {
              if (evt.dragging) {
                info.tooltip('hide');
                return;
              }
              displayFeatureInfo(scope.map.getEventPixel(evt.originalEvent));
            });
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
              url: scope.wfsUrl || scope.layer.get('url').replace(/wms/i, 'wfs')
            });

            uuid = scope.md && scope.md.getUuid();
            ftName = scope.featureTypeName ||
                scope.layer.getSource().getParams().LAYERS;

            appProfile = null;
            appProfilePromise = wfsFilterService.getApplicationProfile(uuid,
                ftName,
                scope.url,
                // A WFS URL is in the metadata or we're guessing WFS has
                // same URL as WMS
                scope.wfsUrl ? 'WFS' : 'WMS').then(
                function(response) {
                  appProfile = response.data;
                  return appProfile;
                }).catch (function() {});

            scope.checkWFSServerUrl();
            scope.initSolrRequest();

            if (scope.map) {
              resetHeatMap();
              scope.map.on('moveend', refreshHeatmap);
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
            if (appProfile && appProfile.fields) {
              wfsFilterService.solrMergeApplicationProfile(
                  solrObject.filteredDocTypeFieldsInfo, appProfile.fields);
              solrObject.initBaseParams();
            }
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
              scope.status = error.statusText;
            });
          };
          scope.dropFeatures = function () {
            return gnSolrService.deleteDocs('+featureTypeId:"' +
              scope.url + '#' + ftName + '"').then(function() {
              scope.initSolrRequest();
            });
          }
          /**
           * Update the state of the facet search.
           * The `scope.output` structure represent the state of the facet
           * checkboxes form.
           *
           * @param {string} fieldName index field name
           * @param {string} facetKey facet key for this field
           * @param {string} type facet type
           */
          scope.onCheckboxClick = function(fieldName, facetKey, type) {
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
                type: type,
                values: {}
              };
              output[fieldName].values[facetKey] = true;
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
            var collapsedFields = [];
            angular.forEach(scope.fields, function(f) {
              collapsedFields;
              if (f.collapsed) {
                collapsedFields.push(f.name);
              }
            });

            solrObject.searchWithFacets(scope.output, scope.searchInput).
                then(function(resp) {
                  scope.fields = resp.facets;
                  scope.count = resp.count;
                  if (formInput) {
                    angular.forEach(scope.fields, function(f) {
                      if (!collapsedFields ||
                          collapsedFields.indexOf(f.name) >= 0) {
                        f.collapsed = true;
                      }
                    });
                  }
                  refreshHeatmap();
                });
          };

          function refreshHeatmap() {
            if (scope.isFeaturesIndexed) {
              heatmapsRequest.searchWithFacets(
                  scope.output, scope.searchInput,
                  gnSolrService.getHeatmapParams(scope.map)).
                  then(function(resp) {
                    scope.heatmaps = resp.solrData.facet_counts.facet_heatmaps;
                  });
            }
          }


          /**
           * reset and init the facet structure.
           * call the solr service to get info on all facet fields and bind it
           * to the output structure to generate the ui.
           */
          scope.resetFacets = function() {

            // output structure to send to filter service
            scope.output = {};

            scope.searchInput = '';


            // load all facet and fill ui structure for the list
            solrObject.searchWithFacets(null, null).
                then(function(resp) {
                  scope.fields = resp.facets;
                  scope.count = resp.count;
                  angular.forEach(scope.fields, function(f) {
                    f.collapsed = true;
                  });
                  refreshHeatmap();
                });

            scope.resetSLDFilters();
          };

          scope.resetSLDFilters = function() {
            scope.layer.getSource().updateParams({
              SLD: null
            });
          };

          /**
           * On filter click, build from the UI the SLD rules config object
           * that will be send to generateSLD service.
           */
          scope.filterWMS = function() {
            var defer = $q.defer();
            var sldConfig = wfsFilterService.createSLDConfig(scope.output);
            if (sldConfig.filters.length > 0) {
              wfsFilterService.getSldUrl(sldConfig, scope.layer.get('url'),
                  ftName).success(function(sldURL) {
                // Do not activate it
                // Usually return 414 Request-URI Too Large
                var useSldBody = false;
                if (useSldBody) {
                  $http.get(sldURL).then(function(response) {
                    scope.layer.getSource().updateParams({
                      SLD_BODY: response.data
                    });
                  });
                } else {
                  scope.layer.getSource().updateParams({
                    SLD: sldURL
                  });
                }
              }).finally (function() {
                defer.resolve();
              });
            } else {
              scope.layer.getSource().updateParams({
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
          scope.indexWFSFeatures = function() {
            appProfilePromise.then(function() {
              wfsFilterService.indexWFSFeatures(
                  scope.url,
                  ftName,
                  appProfile ? appProfile.tokenize : null,
                  uuid);
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


          function resetHeatMap() {
            if (scope.source) {
              scope.source.clear();
            }
            scope.map.un('moveend', refreshHeatmap);
          }

          scope.$watch('isHeatMapVisible', function(n, o) {
            if (n != o) {
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
        }
      };
    }]);
})();
