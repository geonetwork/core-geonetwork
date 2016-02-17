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
    'gnSolrRequestManager',
    function($http, wfsFilterService, $q, $rootScope, gnSolrRequestManager) {
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

          var solrUrl, uuid, ftName;

          // Only admin can index the features
          scope.user = $rootScope.user;

          // Display or not the results count
          scope.showCount = angular.isDefined(attrs['showcount']);

          // Get an instance of solr object
          var solrObject = gnSolrRequestManager.register('WfsFilter', 'facets');

          /**
           * Init the directive when the scope.layer has changed.
           * If the layer is given through the isolate scope object, the init
           * is called only once. Otherwise, the same directive is used for
           * all different feature types.
           */
          function init() {
            angular.extend(scope, {
              fields : [],
              isWfsAvailable: undefined,
              isFeaturesIndexed: false,
              status: null,
              md: scope.layer.get('md'),
              url: scope.wfsUrl || scope.layer.get('url').replace(/wms/i, 'wfs')
            });

            uuid = scope.md && scope.md.getUuid();
            ftName = scope.featureTypeName ||
                scope.layer.getSource().getParams().LAYERS;

            scope.checkWFSServerUrl();
            scope.checkFeatureTypeInSolr();
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
           * Create SOLR request to get facets values
           * Check if the feature has an applicationDefinition, else get the
           * indexed fields for the Feature. From this, build the solr request
           * and retrieve the facet config from solr response.
           * This config is stored in `scope.fields` and is used to build
           * the facet UI.
           */

          var appProfile = null;
          var appProfilePromise = wfsFilterService.getApplicationProfile(uuid,
              ftName,
              scope.url,
              // A WFS URL is in the metadata or we're guessing WFS has
              // same URL as WMS
              scope.wfsUrl ? 'WFS' : 'WMS').then(
                function(data) {
                  appProfile = data;
                  return data;
          });

          function loadFields() {
            var url;
            if (appProfile && appProfile.fields != null) {
              url = wfsFilterService.getSolrRequestFromApplicationProfile(
                  appProfile, ftName, scope.url,
                  solrObject.filteredDocTypeFieldsInfo);
            } else {
              url = solrObject.getBaseRequest({
                wfsUrl: scope.url,
                featureTypeName: ftName
              });
            }
            solrUrl = url;
            // Init the facets
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

          scope.checkFeatureTypeInSolr = function() {
            solrObject.getDocTypeInfo({
              wfsUrl: scope.url,
              featureTypeName: ftName
            }).then(function() {
              scope.isFeaturesIndexed = true;
              scope.status = null;
              var docFields = solrObject.filteredDocTypeFieldsInfo;
              scope.countTotal = solrObject.totalCount;

              if (scope.md && scope.md.attributeTable) {
                for (var i = 0; i < docFields.length; i++) {
                  var label = getDataModelLabel(docFields[i].attrName);
                  if (label) {
                    // TODO: Multilingual
                    docFields[i].label = label;
                  }
                }
              }
              appProfilePromise.finally(loadFields);
            }, function(error) {
              scope.status = error.statusText;
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
           * @param {boolean} fromInput the filter comes from input change
           */
          scope.filterFacets = function(fromInput) {

            // Update the facet UI
            var collapsedFields = [];
            angular.forEach(scope.fields, function(f) {
              collapsedFields;
              if (f.collapsed) {
                collapsedFields.push(f.name);
              }
            });

            solrObject.search(scope.output, scope.searchInput).
                then(function(facetsInfo) {
                  scope.fields = facetsInfo.facetConfig;
                  scope.count = facetsInfo.count;
                  scope.layer.set('featureCount', scope.count);
                  if (fromInput) {
                    angular.forEach(scope.fields, function(f) {
                      if (!collapsedFields ||
                          collapsedFields.indexOf(f.name) >= 0) {
                        f.collapsed = true;
                      }
                    });
                  }
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

            // load all facet and fill ui structure for the list
            solrObject.search().
                then(function(facetsInfo) {
                  scope.fields = facetsInfo.facetConfig;
                  scope.count = facetsInfo.count;
                  scope.layer.set('featureCount', scope.count);
                  scope.layer.set('featureCountT', scope.countTotal);
                  angular.forEach(scope.fields, function(f) {
                    f.collapsed = true;
                  });
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
            appProfilePromise.finally(function() {
              wfsFilterService.indexWFSFeatures(
                  scope.url,
                  ftName,
                  appProfile ? appProfile.tokenize: null,
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
              if (n && n != o) {
                init();
              }
            });
          }
        }
      };
    }]);
})();
