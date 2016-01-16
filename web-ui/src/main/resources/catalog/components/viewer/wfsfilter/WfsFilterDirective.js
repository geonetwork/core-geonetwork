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

    function($http, wfsFilterService, $q, $rootScope) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/viewer/wfsfilter/' +
            'partials/wfsfilterfacet.html',
        scope: {
          featureTypeName: '@',
          wfsUrl: '@',
          bootMode: '@',
          layer: '='
        },
        link: function(scope, element, attrs) {

          scope.md = scope.layer.get('md');
          var solrUrl;
          var uuid = scope.md && scope.md.getUuid();
          var ftName = scope.featureTypeName;
          var wfsUrl = scope.wfsUrl;
          var indexedFields;
          scope.user = $rootScope.user;
          scope.initialized = false;
          scope.isWfsAvailable = false;
          scope.isFeaturesIndexed = false;
          scope.status = null;
          scope.indexingConfig = null;

          init();

          function init() {
            if (scope.bootMode == 'ondemand') {
              // User click to check
            } else if (scope.bootMode == 'wfs') {
              // First check WFS is responding
              // then Solr
              scope.checkWFSUrl();
            } else if (scope.bootMode == 'solr') {
              // Check if feature type is in Solr
              scope.checkFeatureTypeInSolr();
            } else {
              // default: Solr mode
              scope.checkFeatureTypeInSolr();
            }
          };

          /**
           * Check if the WFS url provided return a response.
           */
          scope.checkWFSUrl = function() {
            scope.initialized = true;
            return $http.get('../../proxy?url=' +
                encodeURIComponent(scope.wfsUrl))
              .then(function(data) {
                  scope.isWfsAvailable = true;
                  scope.checkFeatureTypeInSolr();
                }, function(response) {
                  scope.isWfsAvailable = false;
                  scope.status = response;
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
          scope.checkFeatureTypeInSolr = function() {
            wfsFilterService.getWfsIndexFields(
                ftName, wfsUrl).then(function(docFields) {
              scope.initialized = true;
              scope.isFeaturesIndexed = true;
              scope.status = null;

              indexedFields = docFields;
              wfsFilterService.getApplicationProfile(uuid,
                  ftName, wfsUrl).success(function(data) {

                var url;
                if (data) {
                  url = wfsFilterService.getSolrRequestFromApplicationProfile(
                      data, ftName, wfsUrl, docFields);
                  scope.indexingConfig = data.index;
                } else {
                  url = wfsFilterService.getSolrRequestFromFields(
                      docFields, ftName, wfsUrl);
                }
                solrUrl = url;
                // Init the facets
                scope.resetFacets();
              });
            }, function(error) {
              scope.status = error;
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
            scope.filterFacets();
          };

          /**
           * Send a new filtered request to solr to update the facet ui
           * structure.
           * This method is called each time the user check or uncheck a box
           * from the ui, or when he updates the filter input.
           */
          scope.filterFacets = function() {

            // Update the facet UI
            var collapsedFields;
            angular.forEach(scope.fields, function(f) {
              collapsedFields = [];
              if (f.collapsed) {
                collapsedFields.push(f.name);
              }
            });

            var url = wfsFilterService.updateSolrUrl(solrUrl, scope.output,
                scope.searchInput);
            wfsFilterService.getFacetsConfigFromSolr(url, indexedFields).
                then(function(facetsInfo) {
                  scope.fields = facetsInfo.facetConfig;
                  scope.count = facetsInfo.count;
                  angular.forEach(scope.fields, function(f) {
                    if (!collapsedFields ||
                        collapsedFields.indexOf(f.name) >= 0) {
                      f.collapsed = true;
                    }
                  });
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

            // load all facet and fill ui structure for the list
            wfsFilterService.getFacetsConfigFromSolr(solrUrl, indexedFields).
                then(function(facetsInfo) {
                  scope.fields = facetsInfo.facetConfig;
                  scope.count = facetsInfo.count;
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
                  ftName).success(function(data) {
                scope.layer.getSource().updateParams({
                  SLD: data.value
                });
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

          scope.indexWFSFeatures = function() {
            return wfsFilterService.indexWFSFeatures(
                wfsUrl,
                ftName,
                scope.indexingConfig);
          };


          scope.clearInput = function() {
            scope.searchInput = '';
            scope.filterFacets();
          };
          scope.searchInput = '';
        }
      };
    }]);
})();
